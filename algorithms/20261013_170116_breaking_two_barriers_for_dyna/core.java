import java.util.HashSet;
import java.util.ArrayDeque;
import java.util.Deque;

public class DynamicPlanarReachability {
    private final int n;
    @SuppressWarnings("unchecked")
    private final HashSet<Integer>[] adj;

    public DynamicPlanarReachability(int n) {
        if (n <= 0) {
            throw new IllegalArgumentException("Number of vertices must be positive");
        }
        this.n = n;
        adj = new HashSet[n];
        for (int i = 0; i < n; i++) {
            adj[i] = new HashSet<>();
        }
    }

    /** Adds an undirected edge between u and v. Duplicate insertions are ignored. */
    public void addEdge(int u, int v) {
        validateVertex(u);
        validateVertex(v);
        if (u == v) {
            // Self‑loop does not affect adjacency for reachability, but we store it for completeness.
            adj[u].add(v);
            return;
        }
        adj[u].add(v);
        adj[v].add(u);
    }

    /** Removes an undirected edge between u and v. If the edge does not exist, the call is a no‑op. */
    public void removeEdge(int u, int v) {
        validateVertex(u);
        validateVertex(v);
        if (u == v) {
            adj[u].remove(v);
            return;
        }
        adj[u].remove(v);
        adj[v].remove(u);
    }

    /** Returns true iff there exists a path (possibly of length zero) from src to dst. */
    public boolean isReachable(int src, int dst) {
        validateVertex(src);
        validateVertex(dst);
        if (src == dst) {
            return true; // zero‑length path
        }
        boolean[] visited = new boolean[n];
        Deque<Integer> queue = new ArrayDeque<>();
        visited[src] = true;
        queue.add(src);
        while (!queue.isEmpty()) {
            int cur = queue.poll();
            for (int nb : adj[cur]) {
                if (!visited[nb]) {
                    if (nb == dst) {
                        return true;
                    }
                    visited[nb] = true;
                    queue.add(nb);
                }
            }
        }
        return false;
    }

    private void validateVertex(int v) {
        if (v < 0 || v >= n) {
            throw new IndexOutOfBoundsException("Vertex " + v + " out of bounds [0," + (n - 1) + "]");
        }
    }
}
