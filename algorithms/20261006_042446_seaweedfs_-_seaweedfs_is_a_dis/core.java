import java.util.*;

public class ConsistentHashRing {
    private final TreeMap<Integer, String> ring = new TreeMap<>();
    private final int virtualNodes;
    private final Random random = new Random();

    public ConsistentHashRing(int virtualNodes) {
        if (virtualNodes <= 0) throw new IllegalArgumentException("virtualNodes must be positive");
        this.virtualNodes = virtualNodes;
    }

    public void addNode(String node) {
        for (int i = 0; i < virtualNodes; i++) {
            int hash = hash(node + "#" + i);
            ring.put(hash, node);
        }
    }

    public void removeNode(String node) {
        for (int i = 0; i < virtualNodes; i++) {
            int hash = hash(node + "#" + i);
            ring.remove(hash);
        }
    }

    public String getNode(String key) {
        if (ring.isEmpty()) return null;
        int hash = hash(key);
        Map.Entry<Integer, String> entry = ring.ceilingEntry(hash);
        if (entry == null) {
            entry = ring.firstEntry();
        }
        return entry.getValue();
    }

    private int hash(String key) {
        int h = key.hashCode();
        return h ^ (h >>> 16);
    }
}

class FileMetadata {
    private final String path;
    private final long size;
    private final List<String> replicas;

    public FileMetadata(String path, long size, List<String> replicas) {
        this.path = path;
        this.size = size;
        this.replicas = Collections.unmodifiableList(new ArrayList<>(replicas));
    }

    public String getPath() { return path; }
    public long getSize() { return size; }
    public List<String> getReplicas() { return replicas; }

    @Override
    public String toString() {
        return "FileMetadata{" +
                "path='" + path + '\'' +
                ", size=" + size +
                ", replicas=" + replicas +
                '}';
    }
}

class MetadataStore {
    private final Map<String, FileMetadata> store = new HashMap<>();

    public void addFile(FileMetadata meta) {
        store.put(meta.getPath(), meta);
    }

    public FileMetadata getFile(String path) {
        return store.get(path);
    }

    public void deleteFile(String path) {
        store.remove(path);
    }

    public List<FileMetadata> listFiles() {
        return new ArrayList<>(store.values());
    }
}

class SeaweedFS {
    private final ConsistentHashRing ring;
    private final MetadataStore store;
    private final int replicaCount;

    public SeaweedFS(List<String> nodes, int virtualNodes, int replicaCount) {
        this.ring = new ConsistentHashRing(virtualNodes);
        for (String node : nodes) {
            ring.addNode(node);
        }
        this.store = new MetadataStore();
        this.replicaCount = replicaCount;
    }

    public void createFile(String path, long size) {
        List<String> replicas = new ArrayList<>();
        for (int i = 0; i < replicaCount; i++) {
            String node = ring.getNode(path + "#" + i);
            replicas.add(node);
        }
        FileMetadata meta = new FileMetadata(path, size, replicas);
        store.addFile(meta);
    }

    public FileMetadata readFile(String path) {
        return store.getFile(path);
    }

    public void deleteFile(String path) {
        store.deleteFile(path);
    }

    public List<FileMetadata> listFiles() {
        return store.listFiles();
    }
}
