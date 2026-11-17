import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * In‑memory sorted table (MemTable) based on TreeMap.
 */
public class MemTable {
    private final TreeMap<String, String> map = new TreeMap<>();

    /** Insert or update a key/value pair. */
    public void put(String key, String value) {
        map.put(Objects.requireNonNull(key), Objects.requireNonNull(value));
    }

    /** Retrieve a value, or {@code null} if absent. */
    public String get(String key) {
        return map.get(key);
    }

    /** Number of entries currently stored. */
    public int size() {
        return map.size();
    }

    /** Flush the current contents to a new immutable SSTable on disk. */
    public SSTable flush(Path directory, long sequence) throws IOException {
        if (!Files.isDirectory(directory)) {
            throw new IllegalArgumentException("directory must exist");
        }
        // File name encodes sequence to preserve order (newer > older)
        String fileName = String.format("sstable-%020d.dat", sequence);
        Path file = directory.resolve(fileName);
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
            for (Map.Entry<String, String> e : map.entrySet()) {
                writer.write(e.getKey());
                writer.write('\t');
                writer.write(e.getValue());
                writer.newLine();
            }
        }
        // After successful write, clear the memtable.
        map.clear();
        return new SSTable(file);
    }
}

/**
 * Immutable on‑disk sorted string table (SSTable).
 * The file format is line‑oriented: {@code key<TAB>value}.
 * Upon construction an in‑memory index of keys is built for O(log n) lookups.
 */
public class SSTable implements Iterable<KVPair> {
    private final Path path;
    private final List<String> keys;   // sorted
    private final List<String> values; // parallel to keys

    /** Load an existing SSTable file and build the index. */
    public SSTable(Path path) throws IOException {
        this.path = Objects.requireNonNull(path);
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        keys = new ArrayList<>(lines.size());
        values = new ArrayList<>(lines.size());
        for (String line : lines) {
            int tab = line.indexOf('\t');
            if (tab < 0) {
                throw new IOException("corrupt SSTable line: " + line);
            }
            String k = line.substring(0, tab);
            String v = line.substring(tab + 1);
            keys.add(k);
            values.add(v);
        }
    }

    /** Retrieve a value by binary searching the in‑memory index. */
    public String get(String key) {
        int idx = Collections.binarySearch(keys, key);
        return idx >= 0 ? values.get(idx) : null;
    }

    /** Return the underlying file path (useful for debugging). */
    public Path path() {
        return path;
    }

    /** Number of entries stored. */
    public int size() {
        return keys.size();
    }

    @Override
    public Iterator<KVPair> iterator() {
        return new Iterator<KVPair>() {
            private int pos = 0;
            @Override
            public boolean hasNext() {
                return pos < keys.size();
            }
            @Override
            public KVPair next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                KVPair kv = new KVPair(keys.get(pos), values.get(pos));
                pos++;
                return kv;
            }
        };
    }

    /** Merge multiple sorted SSTables into a new one, keeping the newest value for duplicate keys. */
    public static SSTable merge(List<SSTable> inputs, Path outFile) throws IOException {
        // Priority queue ordered by current key; each entry holds iterator state.
        class PQEntry {
            final KVPair kv;
            final Iterator<KVPair> it;
            PQEntry(KVPair kv, Iterator<KVPair> it) { this.kv = kv; this.it = it; }
        }
        PriorityQueue<PQEntry> pq = new PriorityQueue<>(Comparator.comparing(e -> e.kv.key()));
        // Initialize iterators
        for (SSTable s : inputs) {
            Iterator<KVPair> it = s.iterator();
            if (it.hasNext()) {
                pq.add(new PQEntry(it.next(), it));
            }
        }
        // Write merged output
        try (BufferedWriter writer = Files.newBufferedWriter(outFile, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
            String lastKey = null;
            String lastValue = null;
            while (!pq.isEmpty()) {
                PQEntry entry = pq.poll();
                String curKey = entry.kv.key();
                String curValue = entry.kv.value();
                // If the same key appears later (newer SSTable), it will be processed later because
                // we inserted tables in order from oldest to newest. To guarantee newest wins,
                // we process tables from newest to oldest. Simpler: after gathering all entries,
                // we keep the last seen value for a key.
                if (!curKey.equals(lastKey)) {
                    // flush previous key
                    if (lastKey != null) {
                        writer.write(lastKey);
                        writer.write('\t');
                        writer.write(lastValue);
                        writer.newLine();
                    }
                    lastKey = curKey;
                    lastValue = curValue;
                } else {
                    // duplicate key: overwrite with newer value (the later one in iteration order)
                    lastValue = curValue;
                }
                // advance iterator
                if (entry.it.hasNext()) {
                    pq.add(new PQEntry(entry.it.next(), entry.it));
                }
            }
            // flush final pending key
            if (lastKey != null) {
                writer.write(lastKey);
                writer.write('\t');
                writer.write(lastValue);
                writer.newLine();
            }
        }
        return new SSTable(outFile);
    }
}

/**
 * Core LSM‑Tree engine managing a MemTable, a list of SSTables, and automatic flush/compaction.
 */
public class LSMEngine {
    private final MemTable memTable = new MemTable();
    private final List<SSTable> sstables = new ArrayList<>();
    private final Path storageDir;
    private final int memTableFlushThreshold;      // number of entries before flush
    private final int compactionTrigger;           // number of SSTables before compaction
    private long nextSequence = 0L;                // monotonically increasing for file naming

    public LSMEngine(Path storageDir, int memTableFlushThreshold, int compactionTrigger) throws IOException {
        this.storageDir = Objects.requireNonNull(storageDir);
        if (!Files.isDirectory(storageDir)) {
            Files.createDirectories(storageDir);
        }
        this.memTableFlushThreshold = memTableFlushThreshold;
        this.compactionTrigger = compactionTrigger;
        // Load existing SSTables (if any) ordered by sequence number (oldest first)
        List<Path> existing = Files.list(storageDir)
                .filter(p -> p.getFileName().toString().startsWith("sstable-"))
                .sorted()
                .collect(Collectors.toList());
        for (Path p : existing) {
            sstables.add(new SSTable(p));
        }
        // Determine next sequence number from existing files
        if (!existing.isEmpty()) {
            String last = existing.get(existing.size() - 1).getFileName().toString();
            String numPart = last.substring("sstable-".length(), last.length() - ".dat".length());
            nextSequence = Long.parseLong(numPart) + 1;
        }
    }

    /** Insert or update a key/value pair. May trigger flush/compaction. */
    public void put(String key, String value) throws IOException {
        memTable.put(key, value);
        if (memTable.size() >= memTableFlushThreshold) {
            flushMemTable();
        }
    }

    /** Retrieve a value, searching MemTable first then SSTables from newest to oldest. */
    public String get(String key) throws IOException {
        String v = memTable.get(key);
        if (v != null) {
            return v;
        }
        // Search SSTables from newest to oldest (last element is newest)
        for (int i = sstables.size() - 1; i >= 0; i--) {
            v = sstables.get(i).get(key);
            if (v != null) {
                return v;
            }
        }
        return null;
    }

    /** Force a MemTable flush (used internally). */
    private void flushMemTable() throws IOException {
        if (memTable.size() == 0) {
            return;
        }
        SSTable flushed = memTable.flush(storageDir, nextSequence++);
        sstables.add(flushed);
        if (sstables.size() >= compactionTrigger) {
            compact();
        }
    }

    /** Compact all existing SSTables into a single new SSTable. */
    private void compact() throws IOException {
        if (sstables.isEmpty()) {
            return;
        }
        // Merge all SSTables; newer tables appear later in the list, so we reverse to give priority.
        List<SSTable> toMerge = new ArrayList<>(sstables);
        Collections.reverse(toMerge);
        Path mergedPath = storageDir.resolve(String.format("sstable-%020d.dat", nextSequence++));
        SSTable merged = SSTable.merge(toMerge, mergedPath);
        // Delete old files
        for (SSTable s : sstables) {
            Files.deleteIfExists(s.path());
        }
        sstables.clear();
        sstables.add(merged);
    }

    /** Expose current number of SSTables (useful for tests). */
    public int sstableCount() {
        return sstables.size();
    }

    /** Expose current MemTable size (useful for tests). */
    public int memTableSize() {
        return memTable.size();
    }
}
