package algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Immutable edge representation for an undirected weighted graph.
 */
public final class Edge implements Comparable<Edge> {
    public final int u;
    public final int v;
    public final int weight;

    public Edge(int u, int v, int weight) {
        if (u < 0 || v < 0) {
            throw new IllegalArgumentException("Vertex indices must be non‑negative");
        }
        this.u = u;
        this.v = v;
        this.weight = weight;
    }

    @Override
    public int compareTo(Edge other) {
        return Integer.compare(this.weight, other.weight);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Edge)) return false;
        Edge edge = (Edge) o;
        // Since the graph is undirected, (u,v) == (v,u)
        return (u == edge.u && v == edge.v || u == edge.v && v == edge.u)
                && weight == edge.weight;
    }

    @Override
    public int hashCode() {
        // Order‑independent hash for undirected edge
        int a = Math.min(u, v);
        int b = Math.max(u, v);
        return Objects.hash(a, b, weight);
    }

    @Override
    public String toString() {
        return String.format("Edge(%d‑%d, w=%d)", u, v, weight);
    }
}

/**
 * Simple graph container holding a vertex count and a list of edges.
 */
public final class Graph {
    private final int vertexCount;
    private final List<Edge> edges;

    public Graph(int vertexCount, List<Edge> edges) {
        if (vertexCount <= 0) {
            throw new IllegalArgumentException("Vertex count must be positive");
        }
        this.vertexCount = vertexCount;
        this.edges = new ArrayList<>(edges);
    }

    public int vertexCount() {
        return vertexCount;
    }

    public List<Edge> edges() {
        return Collections.unmodifiableList(edges);
    }
}

/**
 * Disjoint Set Union (Union‑Find) with path compression and union by rank.
 */
public final class DisjointSetUnion {
    private final int[] parent;
    private final int[] rank;
    private final int[] size;

    public DisjointSetUnion(int n) {
        if (n <= 0) {
            throw new IllegalArgumentException("Size must be positive");
        }
        parent = new int[n];
        rank = new int[n];
        size = new int[n];
        for (int i = 0; i < n; i++) {
            parent[i] = i;
            rank[i] = 0;
            size[i] = 1;
        }
    }

    /**
     * Finds the representative of the set containing {@code x}.
     * Path compression is applied.
     */
    public int find(int x) {
        validateIndex(x);
        if (parent[x] != x) {
            parent[x] = find(parent[x]); // recursion with compression
        }
        return parent[x];
    }

    /**
     * Unites the sets containing {@code x} and {@code y}.
     * Returns {@code true} if a merge happened, {@code false} if already in the same set.
     */
    public boolean union(int x, int y) {
        int rootX = find(x);
        int rootY = find(y);
        if (rootX == rootY) {
            return false;
        }
        // Union by rank
        if (rank[rootX] < rank[rootY]) {
            parent[rootX] = rootY;
            size[rootY] += size[rootX];
        } else if (rank[rootX] > rank[rootY]) {
            parent[rootY] = rootX;
            size[rootX] += size[rootY];
        } else {
            parent[rootY] = rootX;
            rank[rootX]++;
            size[rootX] += size[rootY];
        }
        return true;
    }

    /**
     * Returns {@code true} if {@code x} and {@code y} belong to the same set.
     */
    public boolean connected(int x, int y) {
        return find(x) == find(y);
    }

    /**
     * Returns the size of the set containing {@code x}.
     */
    public int setSize(int x) {
        return size[find(x)];
    }

    private void validateIndex(int x) {
        if (x < 0 || x >= parent.length) {
            throw new IndexOutOfBoundsException("Vertex index out of bounds: " + x);
        }
    }
}
