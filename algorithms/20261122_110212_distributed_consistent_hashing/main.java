package consistenthash;

import java.util.*;

public class Main {
    public static void main(String[] args) {
        // Enable assertions when running: java -ea Main
        runAllTests();
        demoUsage();
    }

    private static void runAllTests() {
        testEmptyRing();
        testSingleNodeRouting();
        testMultipleNodesDistribution();
        testNodeAdditionMinimalMovement();
        testNodeRemovalMinimalMovement();
        testIdempotentAddRemove();
        System.out.println("All tests passed.");
    }

    private static void testEmptyRing() {
        ConsistentHashRouter router = new ConsistentHashRouter(10);
        assert router.getNode("any-key") == null : "Empty ring should return null";
    }

    private static void testSingleNodeRouting() {
        ConsistentHashRouter router = new ConsistentHashRouter(5);
        Node n1 = new Node("A");
        router.addNode(n1);
        for (int i = 0; i < 100; i++) {
            String key = "key-" + i;
            assert router.getNode(key).equals(n1) : "All keys must map to the sole node";
        }
    }

    private static void testMultipleNodesDistribution() {
        ConsistentHashRouter router = new ConsistentHashRouter(100);
        Node n1 = new Node("Node1");
        Node n2 = new Node("Node2");
        Node n3 = new Node("Node3");
        router.addNode(n1);
        router.addNode(n2);
        router.addNode(n3);

        Map<Node, Integer> counts = new HashMap<>();
        counts.put(n1, 0);
        counts.put(n2, 0);
        counts.put(n3, 0);

        int totalKeys = 10_000;
        for (int i = 0; i < totalKeys; i++) {
            String key = "user-" + i;
            Node owner = router.getNode(key);
            counts.merge(owner, 1, Integer::sum);
        }

        // Expect roughly even distribution (tolerance ±10%)
        double expected = totalKeys / 3.0;
        double tolerance = expected * 0.10;
        for (Map.Entry<Node, Integer> e : counts.entrySet()) {
            double diff = Math.abs(e.getValue() - expected);
            assert diff <= tolerance :
                    "Distribution imbalance for " + e.getKey() + ": " + e.getValue();
        }
    }

    private static void testNodeAdditionMinimalMovement() {
        ConsistentHashRouter router = new ConsistentHashRouter(200);
        Node n1 = new Node("Alpha");
        Node n2 = new Node("Beta");
        router.addNode(n1);
        router.addNode(n2);

        // Snapshot before adding third node
        Map<String, Node> before = mapKeysToNodes(router, 5000);

        Node n3 = new Node("Gamma");
        router.addNode(n3);

        Map<String, Node> after = mapKeysToNodes(router, 5000);

        // Compute fraction of keys that changed owner
        int changed = 0;
        for (String key : before.keySet()) {
            if (!before.get(key).equals(after.get(key))) {
                changed++;
            }
        }
        double changeRatio = changed / (double) before.size();
        // Consistent hashing should move roughly 1/(N+1) of keys; allow 20% margin.
        double expected = 1.0 / 3.0;
        assert Math.abs(changeRatio - expected) < 0.20 :
                "Key movement after addition too high: " + changeRatio;
    }

    private static void testNodeRemovalMinimalMovement() {
        ConsistentHashRouter router = new ConsistentHashRouter(150);
        Node n1 = new Node("One");
        Node n2 = new Node("Two");
        Node n3 = new Node("Three");
        router.addNode(n1);
        router.addNode(n2);
        router.addNode(n3);

        Map<String, Node> before = mapKeysToNodes(router, 5000);

        router.removeNode(n2);

        Map<String, Node> after = mapKeysToNodes(router, 5000);

        int changed = 0;
        for (String key : before.keySet()) {
            if (!before.get(key).equals(after.get(key))) {
                changed++;
            }
        }
        double changeRatio = changed / (double) before.size();
        // Expected ~1/3 of keys to move (those that were on removed node)
        double expected = 1.0 / 3.0;
        assert Math.abs(changeRatio - expected) < 0.20 :
                "Key movement after removal too high: " + changeRatio;
    }

    private static void testIdempotentAddRemove() {
        ConsistentHashRouter router = new ConsistentHashRouter(50);
        Node n = new Node("Solo");
        router.addNode(n);
        router.addNode(n); // duplicate add should be ignored
        router.removeNode(n);
        router.removeNode(n); // duplicate remove ignored
        assert router.getNode("any") == null : "Ring should be empty after removal";
    }

    private static Map<String, Node> mapKeysToNodes(ConsistentHashRouter router, int count) {
        Map<String, Node> map = new HashMap<>(count);
        for (int i = 0; i < count; i++) {
            String key = "key-" + i;
            map.put(key, router.getNode(key));
        }
        return map;
    }

    private static void demoUsage() {
        System.out.println("\n--- Demo of ConsistentHashRouter ---");
        ConsistentHashRouter router = new ConsistentHashRouter(100);
        Node a = new Node("A");
        Node b = new Node("B");
        Node c = new Node("C");

        router.addNode(a);
        router.addNode(b);
        router.addNode(c);

        String[] sampleKeys = {"apple", "banana", "cherry", "date", "elderberry"};
        for (String key : sampleKeys) {
            System.out.printf("Key %-12s -> %s%n", key, router.getNode(key).getIdentifier());
        }

        System.out.println("\nRemoving node B...");
        router.removeNode(b);
        for (String key : sampleKeys) {
            System.out.printf("Key %-12s -> %s%n", key, router.getNode(key).getIdentifier());
        }
    }
}
