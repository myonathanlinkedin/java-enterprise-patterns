import java.util.*;

public class Main {
    public static void main(String[] args) {
        runAllTests();
        System.out.println("All tests passed.");
    }

    private static void runAllTests() {
        testEmptyGraph();
        testSingleNode();
        testSimpleDAG();
        testMultipleComponents();
        testCycleDetection();
        testSelfLoop();
        testComplexDAG();
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError(message + " Expected: " + expected + ", Actual: " + actual);
        }
    }

    private static boolean isValidTopologicalOrder(List<Integer> order, DirectedGraph g) {
        int[] position = new int[g.vertexCount()];
        for (int i = 0; i < order.size(); i++) {
            position[order.get(i)] = i;
        }
        List<Integer>[] adj = g.adjacencyList();
        for (int u = 0; u < adj.length; u++) {
            for (int v : adj[u]) {
                if (position[u] >= position[v]) {
                    return false;
                }
            }
        }
        return true;
    }

    private static void testEmptyGraph() {
        DirectedGraph g = new DirectedGraph(0);
        List<Integer> order = g.topologicalSort();
        assertEquals(0, order.size(), "Empty graph should produce empty order");
        assertTrue(isValidTopologicalOrder(order, g), "Empty graph order validation");
    }

    private static void testSingleNode() {
        DirectedGraph g = new DirectedGraph(1);
        List<Integer> order = g.topologicalSort();
        assertEquals(1, order.size(), "Single node graph size");
        assertEquals(Integer.valueOf(0), order.get(0), "Single node should be 0");
        assertTrue(isValidTopologicalOrder(order, g), "Single node order validation");
    }

    private static void testSimpleDAG() {
        DirectedGraph g = new DirectedGraph(4);
        g.addEdge(0, 1);
        g.addEdge(0, 2);
        g.addEdge(1, 3);
        g.addEdge(2, 3);
        List<Integer> order = g.topologicalSort();
        assertEquals(4, order.size(), "Simple DAG size");
        assertTrue(isValidTopologicalOrder(order, g), "Simple DAG order validation");
    }

    private static void testMultipleComponents() {
        DirectedGraph g = new DirectedGraph(5);
        g.addEdge(0, 1);
        g.addEdge(2, 3);
        // vertex 4 is isolated
        List<Integer> order = g.topologicalSort();
        assertEquals(5, order.size(), "Multiple components size");
        assertTrue(isValidTopologicalOrder(order, g), "Multiple components order validation");
    }

    private static void testCycleDetection() {
        DirectedGraph g = new DirectedGraph(3);
        g.addEdge(0, 1);
        g.addEdge(1, 2);
        g.addEdge(2, 0);
        boolean threw = false;
        try {
            g.topologicalSort();
        } catch (IllegalStateException e) {
            threw = true;
        }
        assertTrue(threw, "Cycle detection should throw IllegalStateException");
        assertTrue(g.hasCycle(), "hasCycle should return true for cyclic graph");
    }

    private static void testSelfLoop() {
        DirectedGraph g = new DirectedGraph(2);
        g.addEdge(0, 0);
        boolean threw = false;
        try {
            g.topologicalSort();
        } catch (IllegalStateException e) {
            threw = true;
        }
        assertTrue(threw, "Self-loop should be detected as a cycle");
        assertTrue(g.hasCycle(), "hasCycle should detect self-loop");
    }

    private static void testComplexDAG() {
        DirectedGraph g = new DirectedGraph(6);
        g.addEdge(5, 2);
        g.addEdge(5, 0);
        g.addEdge(4, 0);
        g.addEdge(4, 1);
        g.addEdge(2, 3);
        g.addEdge(3, 1);
        List<Integer> order = g.topologicalSort();
        assertEquals(6, order.size(), "Complex DAG size");
        assertTrue(isValidTopologicalOrder(order, g), "Complex DAG order validation");
    }
}
