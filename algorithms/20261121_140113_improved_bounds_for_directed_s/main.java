import java.util.*;

public class Main {
    public static void main(String[] args) {
        runAllTests();
        System.out.println("All tests passed.");
    }

    private static void runAllTests() {
        testEmptyGraph();
        testSingleVertex();
        testSimpleSparseGraph();
        testDenseGraph();
        testUnreachableVertices();
        testParallelEdges();
        testNegativeWeightRejection();
    }

    private static void assertEquals(long expected, long actual, String msg) {
        if (expected != actual) {
            throw new AssertionError(msg + " Expected: " + expected + " Actual: " + actual);
        }
    }

    private static void assertArrayEquals(long[] expected, long[] actual, String msg) {
        if (expected.length != actual.length) {
            throw new AssertionError(msg + " Length mismatch");
        }
        for (int i = 0; i < expected.length; i++) {
            if (expected[i] != actual[i]) {
                throw new AssertionError(msg + " At index " + i + " expected " + expected[i] + " but got " + actual[i]);
            }
        }
    }

    private static void testEmptyGraph() {
        Graph g = new Graph(0);
        boolean threw = false;
        try {
            ShortestPath.compute(g, 0);
        } catch (IllegalArgumentException e) {
            threw = true;
        }
        if (!threw) {
            throw new AssertionError("Empty graph should reject source index");
        }
    }

    private static void testSingleVertex() {
        Graph g = new Graph(1);
        long[] dist = ShortestPath.compute(g, 0);
        assertEquals(0L, dist[0], "Single vertex distance to itself");
    }

    private static void testSimpleSparseGraph() {
        Graph g = new Graph(5);
        g.addEdge(0, 1, 10);
        g.addEdge(0, 2, 3);
        g.addEdge(1, 2, 1);
        g.addEdge(1, 3, 2);
        g.addEdge(2, 1, 4);
        g.addEdge(2, 3, 8);
        g.addEdge(2, 4, 2);
        g.addEdge(3, 4, 7);
        g.addEdge(4, 3, 9);

        long[] expected = new long[]{0, 7, 3, 9, 5};
        long[] actual = ShortestPath.compute(g, 0);
        assertArrayEquals(expected, actual, "Sparse graph distances");
    }

    private static void testDenseGraph() {
        int V = 4;
        Graph g = new Graph(V);
        for (int i = 0; i < V; i++) {
            for (int j = 0; j < V; j++) {
                if (i != j) {
                    g.addEdge(i, j, 1);
                }
            }
        }
        g.addEdge(0, 3, 10);
        long[] expected = new long[]{0, 1, 1, 1};
        long[] actual = ShortestPath.compute(g, 0);
        assertArrayEquals(expected, actual, "Dense graph distances");
    }

    private static void testUnreachableVertices() {
        Graph g = new Graph(3);
        g.addEdge(0, 1, 5);
        long[] dist = ShortestPath.compute(g, 0);
        long INF = ShortestPath.INF();
        assertEquals(0L, dist[0], "Source distance");
        assertEquals(5L, dist[1], "Reachable vertex distance");
        assertEquals(INF, dist[2], "Unreachable vertex should be INF");
    }

    private static void testParallelEdges() {
        Graph g = new Graph(2);
        g.addEdge(0, 1, 100);
        g.addEdge(0, 1, 1);
        long[] dist = ShortestPath.compute(g, 0);
        assertEquals(0L, dist[0], "Source");
        assertEquals(1L, dist[1], "Shortest among parallel edges");
    }

    private static void testNegativeWeightRejection() {
        Graph g = new Graph(2);
        boolean threw = false;
        try {
            g.addEdge(0, 1, -5);
        } catch (IllegalArgumentException e) {
            threw = true;
        }
        if (!threw) {
            throw new AssertionError("Negative weight should be rejected");
        }
    }
}
// === END OF FILE ===
