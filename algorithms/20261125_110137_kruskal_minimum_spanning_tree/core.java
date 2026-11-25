import java.util.*;

public class KruskalMST {

    public static class Edge implements Comparable<Edge> {
        public final int u, v;
        public final long weight;

        public Edge(int u, int v, long weight) {
            this.u = u;
            this.v = v;
            this.weight = weight;
        }

        @Override
        public int compareTo(Edge other) {
            return Long.compare(this.weight, other.weight);
        }

        @Override
        public String toString() {
            return "(" + u + "-" + v + ", " + weight + ")";
        }
    }

    private static class DSU {
        private final int[] parent;
        private final int[] rank;

        public DSU(int n) {
            parent = new int[n];
            rank = new int[n];
            for (int i = 0; i < n; i++) {
                parent[i] = i;
                rank[i] = 0;
            }
        }

        public int find(int x) {
            if (parent[x] != x) {
                parent[x] = find(parent[x]); // path compression
            }
            return parent[x];
        }

        public boolean union(int x, int y) {
            int xr = find(x);
            int yr = find(y);
            if (xr == yr) {
                return false;
            }
            if (rank[xr] < rank[yr]) {
                parent[xr] = yr;
            } else if (rank[xr] > rank[yr]) {
                parent[yr] = xr;
            } else {
                parent[yr] = xr;
                rank[xr]++;
            }
            return true;
        }
    }

    public static class Result {
        public final long totalWeight;
        public final List<Edge> mstEdges;

        public Result(long totalWeight, List<Edge> mstEdges) {
            this.totalWeight = totalWeight;
            this.mstEdges = mstEdges;
        }
    }

    public static Result computeMST(int n, List<Edge> edges) {
        Collections.sort(edges);
        DSU dsu = new DSU(n);
        List<Edge> mst = new ArrayList<>();
        long total = 0;
        for (Edge e : edges) {
            if (dsu.union(e.u, e.v)) {
                mst.add(e);
                total += e.weight;
                if (mst.size() == n - 1) {
                    break;
                }
            }
        }
        return new Result(total, mst);
    }
}
