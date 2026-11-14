import java.util.Collection;
import java.util.Objects;

/**
 * Driver class containing exhaustive unit‑style assertions.
 *
 * <p>Run with {@code java -ea main} to enable assertions.
 */
public final class main {
    private main() { /* utility class */ }

    public static void main(String[] args) {
        testSingleNodeOperations();
        testMultipleNodesDistribution();
        testNodeRemovalRehashes();
        testEdgeCases();
        System.out.println("All assertions passed.");
    }

    /** Basic put/get on a ring with a single node. */
    private static void testSingleNodeOperations() {
        ConsistentHashRing<String, Integer> ring = new ConsistentHashRing<>(3);
        InMemoryNode<String, Integer> node = new InMemoryNode<>();
        ring.addNode(node);

        KeyValue<String, Integer> kv1 = new KeyValue<>("alpha", 1);
        KeyValue<String, Integer> kv2 = new KeyValue<>("beta", 2);
        ring.put(kv1);
        ring.put(kv2);

        assert Objects.equals(ring.get("alpha"), 1) : "alpha should map to 1";
        assert Objects.equals(ring.get("beta"), 2) : "beta should map to 2";

        // Direct node inspection confirms all keys reside on the sole node.
        assert node.internalMap().size() == 2 : "node should contain exactly 2 entries";
    }

    /** Verify that keys are spread across multiple nodes. */
    private static void testMultipleNodesDistribution() {
        ConsistentHashRing<String, String> ring = new ConsistentHashRing<>(5);
        InMemoryNode<String, String> n1 = new InMemoryNode<>();
        InMemoryNode<String, String> n2 = new InMemoryNode<>();
        InMemoryNode<String, String> n3 = new InMemoryNode<>();

        ring.addNode(n1);
        ring.addNode(n2);
        ring.addNode(n3);

        // Insert a set of keys.
        String[] keys = {"k0","k1","k2","k3","k4","k5","k6","k7","k8","k9"};
        for (int i = 0; i < keys.length; i++) {
            ring.put(new KeyValue<>(keys[i], "v"+i));
        }

        // At least two nodes must hold data (distribution guarantee with >1 node).
        int nonEmpty = 0;
        for (InMemoryNode<String, String> n : new InMemoryNode[]{n1, n2, n3}) {
            if (!n.internalMap().isEmpty()) nonEmpty++;
        }
        assert nonEmpty >= 2 : "keys should be distributed across multiple nodes";

        // Verify retrieval correctness.
        for (int i = 0; i < keys.length; i++) {
            assert Objects.equals(ring.get(keys[i]), "v"+i) : "value mismatch for " + keys[i];
        }
    }

    /** Ensure that removing a node re‑hashes its keys onto remaining nodes. */
    private static void testNodeRemovalRehashes() {
        ConsistentHashRing<Integer, String> ring = new ConsistentHashRing<>(4);
        InMemoryNode<Integer, String> a = new InMemoryNode<>();
        InMemoryNode<Integer, String> b = new InMemoryNode<>();
        ring.addNode(a);
        ring.addNode(b);

        // Populate with keys 0..99
        for (int i = 0; i < 100; i++) {
            ring.put(new KeyValue<>(i, "val"+i));
        }

        // Capture distribution before removal.
        int countA = a.internalMap().size();
        int countB = b.internalMap().size();
        assert countA + countB == 100 : "all keys must be stored";

        // Remove node 'a' and verify all keys are still retrievable.
        ring.removeNode(a);
        for (int i = 0; i < 100; i++) {
            assert Objects.equals(ring.get(i), "val"+i) : "lost key after removal: " + i;
        }

        // After removal, node 'a' should hold no data (its map is untouched but irrelevant).
        assert a.internalMap().size() == countA : "node a's internal map remains unchanged (simulated storage)";
    }

    /** Test exceptional and edge conditions. */
    private static void testEdgeCases() {
        // Empty ring should reject operations.
        ConsistentHashRing<String, String> emptyRing = new ConsistentHashRing<>(2);
        boolean caught = false;
        try {
            emptyRing.get("anything");
        } catch (IllegalStateException e) {
            caught = true;
        }
        assert caught : "get on empty ring must throw IllegalStateException";

        // Adding duplicate node must fail.
        ConsistentHashRing<String, String> ring = new ConsistentHashRing<>(2);
        InMemoryNode<String, String> node = new InMemoryNode<>();
        ring.addNode(node);
        caught = false;
        try {
            ring.addNode(node);
        } catch (IllegalArgumentException e) {
            caught = true;
        }
        assert caught : "adding duplicate node should raise IllegalArgumentException";

        // Null arguments must be rejected.
        caught = false;
        try {
            ring.put(null);
        } catch (NullPointerException e) {
            caught = true;
        }
        assert caught : "null KeyValue must raise NullPointerException";

        caught = false;
        try {
            ring.get(null);
        } catch (NullPointerException e) {
            caught = true;
        }
        assert caught : "null key must raise NullPointerException";

        // Verify that the ring reports its nodes correctly.
        Collection<StorageNode<String, String>> nodes = ring.nodes();
        assert nodes.size() == 1 && nodes.iterator().next() == node : "nodes() should reflect current membership";
    }
}
