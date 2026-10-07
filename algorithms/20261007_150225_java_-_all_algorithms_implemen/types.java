package algos;

import java.util.*;

public class Types {
    // Singly linked list node
    public static class ListNode {
        public int val;
        public ListNode next;
        public ListNode(int val) {
            this.val = val;
            this.next = null;
        }
    }

    // Edge for weighted graph
    public static class Edge {
        public final int to;
        public final int weight;
        public Edge(int to, int weight) {
            this.to = to;
            this.weight = weight;
        }
    }

    // Graph represented by adjacency list
    public static class Graph {
        private final int vertices;
        private final List<List<Edge>> adj;

        public Graph(int vertices) {
            this.vertices = vertices;
            this.adj = new ArrayList<>(vertices);
            for (int i = 0; i < vertices; i++) {
                adj.add(new ArrayList<>());
            }
        }

        public int size() {
            return vertices;
        }

        public void addEdge(int from, int to) {
            addEdge(from, to, 1);
        }

        public void addEdge(int from, int to, int weight) {
            adj.get(from).add(new Edge(to, weight));
        }

        public List<Edge> neighbors(int v) {
            return Collections.unmodifiableList(adj.get(v));
        }
    }
}
