package consistenthash;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Core implementation of a consistent hashing router with virtual nodes.
 * Thread‑safe for read‑only operations; mutating methods are synchronized.
 */
public final class ConsistentHashRouter {
    // Ring mapping from hash value to physical node.
    private final NavigableMap<Long, Node> ring = new TreeMap<>();
    // Mapping from physical node to its virtual node hashes (for removal).
    private final Map<Node, List<Long>> nodeToVirtualHashes = new HashMap<>();
    // Number of virtual nodes per physical node (default if not specified per add).
    private final int defaultVirtualNodeCount;
    private final MessageDigest md5Digest;

    public ConsistentHashRouter(int defaultVirtualNodeCount) {
        if (defaultVirtualNodeCount <= 0) {
            throw new IllegalArgumentException("virtual node count must be positive");
        }
        this.defaultVirtualNodeCount = defaultVirtualNodeCount;
        try {
            this.md5Digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            // MD5 is guaranteed to exist in any Java SE implementation.
            throw new RuntimeException(e);
        }
    }

    /**
     * Adds a node with the default number of virtual nodes.
     */
    public synchronized void addNode(Node node) {
        addNode(node, defaultVirtualNodeCount);
    }

    /**
     * Adds a node with a specific number of virtual nodes.
     *
     * @param node               the physical node to add
     * @param virtualNodeCount   number of virtual replicas
     */
    public synchronized void addNode(Node node, int virtualNodeCount) {
        Objects.requireNonNull(node, "node");
        if (virtualNodeCount <= 0) {
            throw new IllegalArgumentException("virtualNodeCount must be positive");
        }
        if (nodeToVirtualHashes.containsKey(node)) {
            // Node already present – ignore to keep idempotence.
            return;
        }
        List<Long> hashes = new ArrayList<>(virtualNodeCount);
        for (int i = 0; i < virtualNodeCount; i++) {
            String virtualNodeId = node.getIdentifier() + "##VN" + i;
            long hash = hash(virtualNodeId);
            // Resolve collisions (extremely unlikely) by linear probing.
            while (ring.containsKey(hash)) {
                hash = (hash + 1) & 0xFFFFFFFFFFFFFFFFL;
            }
            ring.put(hash, node);
            hashes.add(hash);
        }
        nodeToVirtualHashes.put(node, hashes);
    }

    /**
     * Removes a node and all its virtual replicas from the ring.
     *
     * @param node the node to remove
     */
    public synchronized void removeNode(Node node) {
        Objects.requireNonNull(node, "node");
        List<Long> hashes = nodeToVirtualHashes.remove(node);
        if (hashes == null) {
            // Node not present – ignore.
            return;
        }
        for (Long h : hashes) {
            ring.remove(h);
        }
    }

    /**
     * Returns the physical node responsible for the given key.
     *
     * @param key the key to route
     * @return the responsible node, or null if the ring is empty
     */
    public Node getNode(String key) {
        Objects.requireNonNull(key, "key");
        if (ring.isEmpty()) {
            return null;
        }
        long hash = hash(key);
        // ceilingEntry gives the first node with hash >= key's hash; wrap around if needed.
        Map.Entry<Long, Node> entry = ring.ceilingEntry(hash);
        if (entry == null) {
            entry = ring.firstEntry();
        }
        return entry.getValue();
    }

    /**
     * Returns an immutable snapshot of the current ring (hash → node).
     * Primarily for testing/verification.
     */
    public synchronized Map<Long, Node> getRingSnapshot() {
        return Collections.unmodifiableMap(new TreeMap<>(ring));
    }

    // -----------------------------------------------------------------------
    // Private utilities
    // -----------------------------------------------------------------------

    /**
     * Computes a 64‑bit hash from the given string using MD5.
     *
     * @param key input string
     * @return 64‑bit hash value
     */
    private long hash(String key) {
        byte[] digest;
        synchronized (md5Digest) {
            md5Digest.reset();
            md5Digest.update(key.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            digest = md5Digest.digest();
        }
        // Use the first 8 bytes of the MD5 digest to form a long (big‑endian).
        long h = 0;
        for (int i = 0; i < 8; i++) {
            h = (h << 8) | (digest[i] & 0xffL);
        }
        return h & 0xFFFFFFFFFFFFFFFFL; // ensure unsigned semantics
    }
}
