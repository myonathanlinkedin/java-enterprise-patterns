package lsm;

import java.util.*;

class Entry {
    final String key;
    final String value; // null indicates tombstone (deletion)

    Entry(String key, String value) {
        this.key = key;
        this.value = value;
    }
}

/**
 * In‑memory sorted table (MemTable) using TreeMap.
 */
class MemTable {
    private final TreeMap<String, String> map = new TreeMap<>();
    private final int maxEntries;

    MemTable(int maxEntries) {
        this.maxEntries = maxEntries;
    }

    /** Insert or update a key. */
    void put(String key, String value) {
        map.put(key, value);
    }

    /** Retrieve a value; may be null if key absent or tombstone. */
    String get(String key) {
        return map.get(key);
    }

    /** Returns true if the memtable reached its capacity. */
    boolean isFull() {
        return map.size() >= maxEntries;
    }

    /** Flushes current contents into an immutable sorted list of entries. */
    List<Entry> flush() {
        List<Entry> list = new ArrayList<>(map.size());
        for (Map.Entry<String, String> e : map.entrySet()) {
            list.add(new Entry(e.getKey(), e.getValue()));
        }
        map.clear();
        return Collections.unmodifiableList(list);
    }

    /** Returns true if empty. */
    boolean isEmpty() {
        return map.isEmpty();
    }
}

/**
 * Immutable on‑disk representation (SSTable) simulated in memory.
 */
class SSTable {
    private final List<Entry> entries; // sorted by key ascending

    SSTable(List<Entry> sortedEntries) {
        // Assume caller provides sorted list.
        this.entries = Collections.unmodifiableList(new ArrayList<>(sortedEntries));
    }

    /** Binary search for a key; returns value (may be null) or null if not found. */
    String get(String key) {
        int lo = 0, hi = entries.size() - 1;
        while (lo <= hi) {
            int mid = (lo + hi) >>> 1;
            Entry e = entries.get(mid);
            int cmp = e.key.compareTo(key);
            if (cmp == 0) {
                return e.value;
            } else if (cmp < 0) {
                lo = mid + 1;
            } else {
                hi = mid - 1;
            }
        }
        return null;
    }

    /** Returns the underlying sorted entries (read‑only). */
    List<Entry> entries() {
        return entries;
    }
}

/**
 * Simplified LSM‑Tree engine handling a single level of SSTables.
 */
public class LSMEngine {
    private final MemTable memTable;
    private final List<SSTable> sstables = new ArrayList<>();

    public LSMEngine(int memTableMaxEntries) {
        this.memTable = new MemTable(memTableMaxEntries);
    }

    /** Insert or delete (value == null) a key. */
    public void put(String key, String value) {
        memTable.put(key, value);
        if (memTable.isFull()) {
            flush();
        }
    }

    /** Retrieve the most recent value for a key, respecting tombstones. */
    public String get(String key) {
        // Check MemTable first (most recent writes)
        String v = memTable.get(key);
        if (v != null) {
            return v;
        } else if (memTable.get(key) == null && memTable.map.containsKey(key)) {
            // explicit tombstone in memtable
            return null;
        }

        // Scan SSTables from newest to oldest
        for (int i = sstables.size() - 1; i >= 0; i--) {
            SSTable s = sstables.get(i);
            v = s.get(key);
            if (v != null) {
                return v;
            } else if (v == null) {
                // Need to differentiate between "not found" and "tombstone".
                // Since get returns null for both, we must check presence via binary search.
                // Perform a binary search to see if key exists with null value.
                // Re‑use SSTable's internal list.
                List<Entry> list = s.entries();
                int lo = 0, hi = list.size() - 1;
                while (lo <= hi) {
                    int mid = (lo + hi) >>> 1;
                    Entry e = list.get(mid);
                    int cmp = e.key.compareTo(key);
                    if (cmp == 0) {
                        // tombstone found
                        return null;
                    } else if (cmp < 0) {
                        lo = mid + 1;
                    } else {
                        hi = mid - 1;
                    }
                }
            }
        }
        return null; // not found
    }

    /** Flush current MemTable to a new SSTable. */
    public void flush() {
        if (memTable.isEmpty()) {
            return;
        }
        List<Entry> flushed = memTable.flush();
        // Ensure entries are sorted (TreeMap guarantees order)
        SSTable s = new SSTable(flushed);
        sstables.add(s);
    }

    /** Compact all existing SSTables into a single sorted SSTable. */
    public void compact() {
        if (sstables.isEmpty()) {
            return;
        }
        // Merge k sorted lists using a priority queue.
        PriorityQueue<IterState> pq = new PriorityQueue<>(Comparator.comparing(is -> is.entry.key));
        for (SSTable s : sstables) {
            Iterator<Entry> it = s.entries().iterator();
            if (it.hasNext()) {
                pq.add(new IterState(it.next(), it));
            }
        }
        List<Entry> merged = new ArrayList<>();
        String lastKey = null;
        while (!pq.isEmpty()) {
            IterState cur = pq.poll();
            Entry e = cur.entry;
            // Keep the most recent value for duplicate keys (newer SSTable overrides older).
            if (!e.key.equals(lastKey)) {
                merged.add(e);
                lastKey = e.key;
            } else {
                // duplicate key: replace previous entry (which is older) with newer one.
                merged.set(merged.size() - 1, e);
            }
            if (cur.it.hasNext()) {
                pq.add(new IterState(cur.it.next(), cur.it));
            }
        }
        // Replace old SSTables with the compacted one.
        sstables.clear();
        sstables.add(new SSTable(merged));
    }

    /** Return number of SSTables (useful for tests). */
    public int sstableCount() {
        return sstables.size();
    }

    /** Internal helper for priority‑queue merging. */
    private static class IterState {
        final Entry entry;
        final Iterator<Entry> it;

        IterState(Entry entry, Iterator<Entry> it) {
            this.entry = entry;
            this.it = it;
        }
    }
}
