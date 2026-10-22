package algorithm;

import java.util.ArrayList;
import java.util.List;

/**
 * Verification driver containing exhaustive unit‑style assertions.
 * Run with the JVM option -ea to enable assertions.
 */
public final class Main {

    public static void main(String[] args) {
        testSimpleTriangle();
        testSquareWithDiagonal();
        testDisconnectedGraph();
        testSingleVertex();
        System.out.println("All tests passed.");
    }

    /**
     * Triangle graph: 3 vertices, edges (0‑1,2), (1‑2,3), (0‑2,1).
     * Expected MST weight = 1 + 2 = 3, edges (0‑2) and (0‑1).
     */
    private static void testSimpleTriangle() {
        List<Edge> edges = List.of(
                new Edge(0, 1, 2),
                new Edge(1, 2, 3),
                new Edge(0, 2, 1)
        );
        Graph g = new Graph(3, edges);
        List<Edge> mst = KruskalMST.computeMST(g);
        assert mst.size() == 2 : "MST should contain 2 edges";
        assert KruskalMST.totalWeight(mst) == 3 : "MST total weight should be 3";

        // Verify exact edge set (order‑independent)
        assert mst.contains(new Edge(0, 2, 1));
        assert mst.contains(new Edge(0, 1, 2));
    }

    /**
     * Square with a diagonal: 4 vertices, edges forming a square of weight 1,
     * plus a diagonal of weight 2. Expected MST weight = 3 (three edges of weight 1).
     */
    private static void testSquareWithDiagonal() {
        List<Edge> edges = List.of(
                new Edge(0, 1, 1),
                new Edge(1, 2, 1),
                new Edge(2, 3, 1),
                new Edge(3, 0, 1),
                new Edge(0, 2, 2) // diagonal
        );
        Graph g = new Graph(4, edges);
        List<Edge> mst = KruskalMST.computeMST(g);
        assert mst.size() == 3 : "MST should contain 3 edges";
        assert KruskalMST.totalWeight(mst) == 3 : "MST total weight should be 3";

        // Ensure no diagonal edge appears
        assert !mst.contains(new Edge(0, 2, 2));
    }

    /**
     * Disconnected graph: two components. The algorithm must throw an exception.
     */
    private static void testDisconnectedGraph() {
        List<Edge> edges = List.of(
                new Edge(0, 1, 5),
                new Edge(2, 3, 7)
        );
        Graph g = new Graph(4, edges);
        boolean threw = false;
        try {
            KruskalMST.computeMST(g);
        } catch (IllegalArgumentException e) {
            threw = true;
        }
        assert threw : "Disconnected graph should cause IllegalArgumentException";
    }

    /**
     * Single‑vertex graph: MST is empty and weight zero.
     */
    private static void testSingleVertex() {
        List<Edge> edges = new ArrayList<>();
        Graph g = new Graph(1, edges);
        List<Edge> mst = KruskalMST.computeMST(g);
        assert mst.isEmpty() : "MST of a single‑vertex graph must be empty";
        assert KruskalMST.totalWeight(mst) == 0 : "Total weight must be zero";
    }
}
