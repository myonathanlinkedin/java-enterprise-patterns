import java.util.*;
public class Main {
    private static int testsRun = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        try {
            testEmptyRing();
            testSingleNode();
            testMultipleNodesDistribution();
            testRemoveNode();
            testDuplicateAdd();
            System.out.println("All tests passed (" + testsRun + " run, " + testsFailed + " failed).");
        } catch (Throwable t) {
            System.err.println("Unexpected error during testing: " + t);
            t.printStackTrace();
        }
    }

    private static void assertTrue(boolean condition, String message) {
        testsRun++;
        if (!condition) {
            testsFailed++;
            throw new AssertionError("Test failed: " + message);
        }
    }

    private static void testEmptyRing() {
        ConsistentHashRouter router = new ConsistentHashRouter(3);
        assertTrue(router.getNode("anyKey") == null, "Empty ring should return null");
    }

    private static void testSingleNode() {
        ConsistentHashRouter router = new ConsistentHashRouter(5);
        router.addNode("NodeA");
        String[] keys = {"alpha", "beta", "gamma", "delta", "epsilon"};
        for (String k : keys) {
            assertTrue("NodeA".equals(router.getNode(k)), "All keys must map to the sole node");
        }
        assertTrue(router.getRingSize() == 5, "Ring size must equal virtual node count");
    }

    private static void testMultipleNodesDistribution() {
        ConsistentHashRouter router = new ConsistentHashRouter(10);
        router.addNode("NodeA");
        router.addNode("NodeB");
        router.addNode("NodeC");

        // Generate deterministic keys.
        int totalKeys = 3000;
        java.util.Map<String, Integer> counts = new java.util.HashMap<>();
        counts.put("NodeA", 0);
        counts.put("NodeB", 0);
        counts.put("NodeC", 0);

        for (int i = 0; i < totalKeys; i++) {
            String key = "key-" + i;
            String node = router.getNode(key);
            counts.put(node, counts.get(node) + 1);
        }

        // Simple uniformity check: each node should receive at least 20% of keys.
        int minExpected = totalKeys / 5; // 20%
        for (java.util.Map.Entry<String, Integer> e : counts.entrySet()) {
            assertTrue(e.getValue() >= minExpected,
                    "Node " + e.getKey() + " received too few keys: " + e.getValue());
        }

        // Ring size must be 30 (3 nodes * 10 virtual nodes)
        assertTrue(router.getRingSize() == 30, "Ring size should be 30 after adding 3 nodes");
    }

    private static void testRemoveNode() {
        ConsistentHashRouter router = new ConsistentHashRouter(4);
        router.addNode("NodeA");
        router.addNode("NodeB");
        router.addNode("NodeC");

        // Ensure all nodes are present.
        java.util.Set<String> before = new java.util.HashSet<>();
        for (int i = 0; i < 100; i++) {
            before.add(router.getNode("key-" + i));
        }
        assertTrue(before.contains("NodeB"), "NodeB should be present before removal");

        router.removeNode("NodeB");

        // After removal, NodeB must never appear.
        for (int i = 0; i < 100; i++) {
            String node = router.getNode("key-" + i);
            assertTrue(!"NodeB".equals(node), "NodeB should not be returned after removal");
        }

        // Ring size must have decreased by 4.
        assertTrue(router.getRingSize() == 8, "Ring size should be 8 after removing one node");
    }

    private static void testDuplicateAdd() {
        ConsistentHashRouter router = new ConsistentHashRouter(2);
        router.addNode("NodeX");
        int sizeAfterFirstAdd = router.getRingSize();
        router.addNode("NodeX"); // duplicate
        assertTrue(router.getRingSize() == sizeAfterFirstAdd,
                "Adding duplicate node must not change ring size");
    }
}
