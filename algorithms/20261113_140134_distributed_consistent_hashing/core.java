import java.util.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.ByteBuffer;

/**
 * ConsistentHashRouter implements a deterministic consistent hashing ring with virtual nodes.
 * It supports adding and removing physical nodes, each represented by a configurable number
 * of virtual nodes to improve key distribution.
 */
public class ConsistentHashRouter {
    private final int virtualNodeCount;
    private final TreeMap<Long, String> ring = new TreeMap<>();
    private final Map<String, List<Long>> nodeToHashes = new HashMap<>();
    private final MessageDigest md5Digest;

    /**
     * Constructs a router with the specified number of virtual nodes per physical node.
     *
     * @param virtualNodeCount number of virtual replicas for each physical node (must be >0)
     */
    public ConsistentHashRouter(int virtualNodeCount) {
        if (virtualNodeCount <= 0) {
            throw new IllegalArgumentException("virtualNodeCount must be positive");
        }
        this.virtualNodeCount = virtualNodeCount;
        try {
            this.md5Digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            // MD5 is guaranteed to be present in any Java SE implementation.
            throw new RuntimeException(e);
        }
    }

    /**
     * Adds a physical node to the ring. If the node already exists, the call is ignored.
     *
     * @param node identifier of the physical node (non‑null, non‑empty)
     */
    public synchronized void addNode(String node) {
        Objects.requireNonNull(node, "node must not be null");
        if (node.isEmpty()) {
            throw new IllegalArgumentException("node must not be empty");
        }
        if (nodeToHashes.containsKey(node)) {
            // Node already present – ignore to keep idempotence.
            return;
        }
        List<Long> hashes = new ArrayList<>(virtualNodeCount);
        for (int i = 0; i < virtualNodeCount; i++) {
            String virtualId = node + "##VN" + i;
            long hash = hash(virtualId);
            // In the extremely unlikely event of a hash collision, we still store both entries.
            ring.put(hash, node);
            hashes.add(hash);
        }
        nodeToHashes.put(node, hashes);
    }

    /**
     * Removes a physical node and all its virtual replicas from the ring.
     * If the node does not exist, the call is ignored.
     *
     * @param node identifier of the physical node to remove
     */
    public synchronized void removeNode(String node) {
        Objects.requireNonNull(node, "node must not be null");
        List<Long> hashes = nodeToHashes.remove(node);
        if (hashes == null) {
            return; // Node not present.
        }
        for (Long h : hashes) {
            ring.remove(h);
        }
    }

    /**
     * Returns the physical node responsible for the given key according to the current ring.
     *
     * @param key the key to locate (non‑null)
     * @return the node identifier, or {@code null} if the ring is empty
     */
    public synchronized String getNode(String key) {
        Objects.requireNonNull(key, "key must not be null");
        if (ring.isEmpty()) {
            return null;
        }
        long hash = hash(key);
        SortedMap<Long, String> tail = ring.tailMap(hash, true);
        Long nodeHash = tail.isEmpty() ? ring.firstKey() : tail.firstKey();
        return ring.get(nodeHash);
    }

    /**
     * Returns the total number of virtual nodes currently stored in the ring.
     *
     * @return size of the ring
     */
    public synchronized int getRingSize() {
        return ring.size();
    }

    /**
     * Computes a 64‑bit hash of the supplied string using MD5.
     *
     * @param key input string
     * @return 64‑bit hash value
     */
    private long hash(String key) {
        md5Digest.reset();
        md5Digest.update(key.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        byte[] digest = md5Digest.digest();
        // Use the first 8 bytes of the MD5 digest to form a long (big‑endian).
        ByteBuffer buffer = ByteBuffer.wrap(digest);
        return buffer.getLong();
    }
}
