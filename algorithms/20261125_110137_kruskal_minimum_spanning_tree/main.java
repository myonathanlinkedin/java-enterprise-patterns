import java.util.*;

public class Main {
    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void testSimpleTriangle() {
        List<KruskalMST.Edge> edges = new ArrayList<>();
        edges.add(new KruskalMST.Edge(0, 1, 1));
        edges.add(new KruskalMST.Edge(1, 2, 2));
        edges.add(new KruskalMST.Edge(0, 2, 3));
        KruskalMST.Result res = KruskalMST.computeMST(3, edges);
        assertTrue(res.mstEdges.size() == 2, "Triangle MST should have 2 edges");
        assertTrue(res.totalWeight == 3, "Total weight should be 3");
    }

    private static void testDisconnectedGraph() {
        List<KruskalMST.Edge> edges = new ArrayList<>();
        edges.add(new KruskalMST.Edge(0, 1, 1));
        // Node 2 is isolated
        KruskalMST.Result res = KruskalMST.computeMST(3, edges);
        assertTrue(res.mstEdges.size() == 1, "Disconnected graph MST edges count");
        assertTrue(res.totalWeight == 1, "Total weight should be 1");
    }

    private static void testMultipleEdges() {
        List<KruskalMST.Edge> edges = new ArrayList<>();
        edges.add(new KruskalMST.Edge(0, 1, 5));
        edges.add(new KruskalMST.Edge(0, 1, 3)); // parallel edge with lower weight
        edges.add(new KruskalMST.Edge(1, 2, 4));
        KruskalMST.Result res = KruskalMST.computeMST(3, edges);
        assertTrue(res.mstEdges.size() == 2, "Multiple edges MST edges count");
        assertTrue(res.totalWeight == 7, "Total weight should be 7");
    }

    private static void testNegativeWeights() {
        List<KruskalMST.Edge> edges = new ArrayList<>();
        edges.add(new KruskalMST.Edge(0, 1, -10));
        edges.add(new KruskalMST.Edge(1, 2, -5));
        edges.add(new KruskalMST.Edge(0, 2, 1));
        KruskalMST.Result res = KruskalMST.computeMST(3, edges);
        assertTrue(res.mstEdges.size() == 2, "Negative weights MST edges count");
        assertTrue(res.totalWeight == -15, "Total weight should be -15");
    }

    private static void testSingleNode() {
        List<KruskalMST.Edge> edges = new ArrayList<>();
        KruskalMST.Result res = KruskalMST.computeMST(1, edges);
        assertTrue(res.mstEdges.isEmpty(), "Single node MST should have no edges");
        assertTrue(res.totalWeight == 0, "Total weight should be 0");
    }

    public static void main(String[] args) {
        testSimpleTriangle();
        testDisconnectedGraph();
        testMultipleEdges();
        testNegativeWeights();
        testSingleNode();
        System.out.println("All tests passed.");
    }
}
