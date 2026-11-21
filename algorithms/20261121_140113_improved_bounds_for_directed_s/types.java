import java.util.*;

public final class Edge {
    public final int from;
    public final int to;
    public final long weight;

    public Edge(int from, int to, long weight) {
        if (weight < 0) {
            throw new IllegalArgumentException("Negative weight not allowed for Dijkstra");
        }
        this.from = from;
        this.to = to;
        this.weight = weight;
    }
}

public final class Graph {
    public final int V;
    private final List<Edge>[] adjList;
    private long[][] matrix;
    private final double densityThreshold = 0.5; // ratio E/(V*(V-1))
    private int edgeCount = 0;

    @SuppressWarnings("unchecked")
    public Graph(int V) {
        if (V < 0) {
            throw new IllegalArgumentException("Number of vertices cannot be negative");
        }
        this.V = V;
        this.adjList = (List<Edge>[]) new List[V];
        for (int i = 0; i < V; i++) {
            adjList[i] = new ArrayList<>();
        }
    }

    public void addEdge(int from, int to, long weight) {
        validateVertex(from);
        validateVertex(to);
        Edge e = new Edge(from, to, weight);
        adjList[from].add(e);
        edgeCount++;
    }

    private void validateVertex(int v) {
        if (v < 0 || v >= V) {
            throw new IllegalArgumentException("Vertex " + v + " out of bounds");
        }
    }

    public long[][] getMatrix() {
        if (matrix == null) {
            matrix = new long[V][V];
            for (int i = 0; i < V; i++) {
                Arrays.fill(matrix[i], Long.MAX_VALUE);
                matrix[i][i] = 0;
            }
            for (int u = 0; u < V; u++) {
                for (Edge e : adjList[u]) {
                    long existing = matrix[e.from][e.to];
                    if (e.weight < existing) {
                        matrix[e.from][e.to] = e.weight;
                    }
                }
            }
        }
        return matrix;
    }

    public List<Edge>[] getAdjList() {
        return adjList;
    }

    public boolean isDense() {
        if (V <= 1) {
            return false;
        }
        double density = (double) edgeCount / (V * (V - 1));
        return density >= densityThreshold;
    }

    public int edgeCount() {
        return edgeCount;
    }
}
