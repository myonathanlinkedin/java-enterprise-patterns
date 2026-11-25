import java.util.*;

public class DirectedGraph {
    private final int V;
    private final List<Integer>[] adj;

    @SuppressWarnings("unchecked")
    public DirectedGraph(int vertices) {
        if (vertices < 0) {
            throw new IllegalArgumentException("Number of vertices cannot be negative");
        }
        this.V = vertices;
        this.adj = new List[vertices];
        for (int i = 0; i < vertices; i++) {
            adj[i] = new ArrayList<>();
        }
    }

    public int vertexCount() {
        return V;
    }

    public void addEdge(int from, int to) {
        validateVertex(from);
        validateVertex(to);
        adj[from].add(to);
    }

    private void validateVertex(int v) {
        if (v < 0 || v >= V) {
            throw new IndexOutOfBoundsException("Vertex " + v + " out of bounds");
        }
    }

    /**
     * Returns a topological ordering of the vertices.
     * If the graph contains a cycle, throws IllegalStateException.
     */
    public List<Integer> topologicalSort() {
        int[] indegree = new int[V];
        for (int u = 0; u < V; u++) {
            for (int v : adj[u]) {
                indegree[v]++;
            }
        }

        Deque<Integer> queue = new ArrayDeque<>();
        for (int i = 0; i < V; i++) {
            if (indegree[i] == 0) {
                queue.addLast(i);
            }
        }

        List<Integer> order = new ArrayList<>(V);
        while (!queue.isEmpty()) {
            int u = queue.removeFirst();
            order.add(u);
            for (int v : adj[u]) {
                indegree[v]--;
                if (indegree[v] == 0) {
                    queue.addLast(v);
                }
            }
        }

        if (order.size() != V) {
            throw new IllegalStateException("Graph contains a cycle");
        }
        return order;
    }

    /**
     * Returns true if the graph contains at least one directed cycle.
     */
    public boolean hasCycle() {
        try {
            topologicalSort();
            return false;
        } catch (IllegalStateException e) {
            return true;
        }
    }

    public List<Integer>[] adjacencyList() {
        // Return a shallow copy to preserve encapsulation
        @SuppressWarnings("unchecked")
        List<Integer>[] copy = new List[V];
        for (int i = 0; i < V; i++) {
            copy[i] = new ArrayList<>(adj[i]);
        }
        return copy;
    }
}
