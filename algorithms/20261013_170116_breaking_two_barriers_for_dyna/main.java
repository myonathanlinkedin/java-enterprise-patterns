import java.util.Random;

public class Main {
    public static void main(String[] args) {
        runAllTests();
        System.out.println("All tests passed.");
    }

    private static void runAllTests() {
        testBasicOperations();
        testDuplicateEdges();
        testSelfLoop();
        testLargeTree();
        testInvalidOperations();
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    // -------------------------------------------------------------------------
    // Unit tests
    // -------------------------------------------------------------------------

    private static void testBasicOperations() {
        DynamicPlanarReachability dpr = new DynamicPlanarReachability(5);
        dpr.addEdge(0, 1);
        dpr.addEdge(1, 2);
        dpr.addEdge(2, 3);
        dpr.addEdge(3, 4);
        assertTrue(dpr.isReachable(0, 4), "0 should reach 4 in a chain");
        dpr.removeEdge(2, 3);
        assertTrue(!dpr.isReachable(0, 4), "0 should not reach 4 after cutting the chain");
        dpr.addEdge(0, 4);
        assertTrue(dpr.isReachable(0, 4), "Direct edge restores reachability");
    }

    private static void testDuplicateEdges() {
        DynamicPlanarReachability dpr = new DynamicPlanarReachability(3);
        dpr.addEdge(0, 1);
        dpr.addEdge(0, 1); // duplicate
        dpr.addEdge(1, 2);
        dpr.addEdge(1, 2); // duplicate
        assertTrue(dpr.isReachable(0, 2), "0 should reach 2 despite duplicate insertions");
        dpr.removeEdge(0, 1);
        assertTrue(!dpr.isReachable(0, 2), "Removal of the only 0‑1 edge disconnects 0 from 2");
    }

    private static void testSelfLoop() {
        DynamicPlanarReachability dpr = new DynamicPlanarReachability(4);
        dpr.addEdge(2, 2); // self‑loop
        assertTrue(dpr.isReachable(2, 2), "Vertex should always reach itself");
        dpr.removeEdge(2, 2);
        assertTrue(dpr.isReachable(2, 2), "Self‑loop removal must not affect zero‑length reachability");
    }

    private static void testLargeTree() {
        final int N = 1000;
        DynamicPlanarReachability dpr = new DynamicPlanarReachability(N);
        // Build a simple path (which is a planar tree)
        for (int i = 0; i < N - 1; i++) {
            dpr.addEdge(i, i + 1);
        }
        assertTrue(dpr.isReachable(0, N - 1), "Endpoints of a long path must be reachable");
        // Cut the middle edge
        int cut = N / 2;
        dpr.removeEdge(cut - 1, cut);
        assertTrue(!dpr.isReachable(0, N - 1), "Path should be broken after removing a middle edge");
    }

    private static void testInvalidOperations() {
        DynamicPlanarReachability dpr = new DynamicPlanarReachability(3);
        boolean caught = false;
        try {
            dpr.addEdge(-1, 0);
        } catch (IndexOutOfBoundsException e) {
            caught = true;
        }
        assertTrue(caught, "Adding edge with invalid vertex should throw");

        caught = false;
        try {
            dpr.isReachable(0, 3);
        } catch (IndexOutOfBoundsException e) {
            caught = true;
        }
        assertTrue(caught, "Query with out‑of‑range vertex should throw");
    }
}
