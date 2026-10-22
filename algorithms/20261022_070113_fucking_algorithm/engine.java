package algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Core implementation of Kruskal's Minimum Spanning Tree algorithm.
 */
public final class KruskalMST {

    /**
     * Computes the Minimum Spanning Tree (MST) of an undirected weighted graph.
     *
     * @param graph the input graph; must be connected
     * @return a list of edges forming the MST; the list is sorted by weight
     * @throws IllegalArgumentException if the graph is disconnected
     */
    public static List<Edge> computeMST(Graph graph) {
        int n = graph.vertexCount();
        List<Edge> edges = new ArrayList<>(graph.edges());
        Collections.sort(edges); // sort by weight ascending

        DisjointSetUnion dsu = new DisjointSetUnion(n);
        List<Edge> mst = new ArrayList<>(n - 1);
        int totalWeight = 0;

        for (Edge e : edges) {
            if (!dsu.connected(e.u, e.v)) {
                dsu.union(e.u, e.v);
                mst.add(e);
                totalWeight += e.weight;
                if (mst.size() == n - 1) {
                    break; // MST complete
                }
            }
        }

        // Verify connectivity: after processing, all vertices must share the same root
        int root = dsu.find(0);
        for (int i = 1; i < n; i++) {
            if (dsu.find(i) != root) {
                throw new IllegalArgumentException("Input graph is disconnected; MST undefined");
            }
        }

        // Optional: sort final MST edges for deterministic output
        Collections.sort(mst);
        return Collections.unmodifiableList(mst);
    }

    /**
     * Utility method to compute total weight of a set of edges.
     */
    public static int totalWeight(List<Edge> edges) {
        int sum = 0;
        for (Edge e : edges) {
            sum += e.weight;
        }
        return sum;
    }
}
