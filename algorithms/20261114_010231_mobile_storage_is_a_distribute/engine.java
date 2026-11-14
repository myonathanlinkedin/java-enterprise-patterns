import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.Collection;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Consistent‑hash ring that distributes keys across a set of {@link StorageNode}s.
 *
 * <p>The ring uses MD5 hashing to map both nodes and keys onto a 128‑bit space.
 * Each node may have multiple virtual replicas to improve key distribution.
 *
 * @param <K> key type (must be immutable and have a stable {@code toString()} representation)
 * @param <V> value type
 */
public final class ConsistentHashRing<K, V> {
    private static final String HASH_ALGORITHM = "MD5";

    /** Number of virtual replicas per physical node. */
    private final int replicas;

    /** Mapping from hash → node. */
    private final NavigableMap<BigInteger, StorageNode<K, V>> ring = new TreeMap<>();

    /** All physical nodes currently in the ring. */
    private final List<StorageNode<K, V>> nodes = new ArrayList<>();

    private final MessageDigest digest;

    /**
     * Creates a new ring.
     *
     * @param replicas number of virtual replicas per node; must be &gt; 0
     */
    public ConsistentHashRing(int replicas) {
        if (replicas <= 0) {
            throw new IllegalArgumentException("replicas must be > 0");
        }
        this.replicas = replicas;
        try {
            this.digest = MessageDigest.getInstance(HASH_ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            // MD5 is guaranteed to be present in any Java SE implementation.
            throw new RuntimeException(e);
        }
    }

    /**
     * Adds a new physical node to the ring.
     *
     * @param node the node to add; must be non‑null and not already present
     */
    public void addNode(StorageNode<K, V> node) {
        Objects.requireNonNull(node, "node must not be null");
        if (nodes.contains(node)) {
            throw new IllegalArgumentException("node already present in the ring");
        }
        nodes.add(node);
        for (int i = 0; i < replicas; i++) {
            BigInteger hash = hash(node.toString() + "#" + i);
            ring.put(hash, node);
        }
    }

    /**
     * Removes a node from the ring. All its virtual replicas are discarded.
     *
     * @param node the node to remove; must be present in the ring
     */
    public void removeNode(StorageNode<K, V> node) {
        Objects.requireNonNull(node, "node must not be null");
        if (!nodes.remove(node)) {
            throw new IllegalArgumentException("node not present in the ring");
        }
        // Remove all virtual replicas belonging to this node.
        for (int i = 0; i < replicas; i++) {
            BigInteger hash = hash(node.toString() + "#" + i);
            ring.remove(hash);
        }
    }

    /**
     * Stores a key/value pair on the appropriate node according to consistent hashing.
     *
     * @param kv the pair to store; key must be non‑null
     */
    public void put(KeyValue<K, V> kv) {
        Objects.requireNonNull(kv, "kv must not be null");
        StorageNode<K, V> node = locateNode(kv.getKey());
        node.put(kv);
    }

    /**
     * Retrieves a value for the given key from the node responsible for it.
     *
     * @param key the key to look up; must be non‑null
     * @return the stored value, or {@code null} if absent
     */
    public V get(K key) {
        Objects.requireNonNull(key, "key must not be null");
        StorageNode<K, V> node = locateNode(key);
        return node.get(key);
    }

    /**
     * Returns a snapshot of the current physical nodes.
     */
    public Collection<StorageNode<K, V>> nodes() {
        return new ArrayList<>(nodes);
    }

    /**
     * Locate the node responsible for a given key.
     *
     * @throws IllegalStateException if the ring is empty
     */
    private StorageNode<K, V> locateNode(K key) {
        if (ring.isEmpty()) {
            throw new IllegalStateException("consistent hash ring is empty");
        }
        BigInteger keyHash = hash(key.toString());
        // ceilingEntry returns the first entry >= keyHash; if none, wrap around to first entry.
        var entry = ring.ceilingEntry(keyHash);
        if (entry == null) {
            entry = ring.firstEntry();
        }
        return entry.getValue();
    }

    /**
     * Computes the MD5 hash of a string and returns it as an unsigned {@link BigInteger}.
     */
    private BigInteger hash(String data) {
        byte[] bytes = digest.digest(data.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        // Reset digest for next use
        digest.reset();
        return new BigInteger(1, bytes);
    }
}
