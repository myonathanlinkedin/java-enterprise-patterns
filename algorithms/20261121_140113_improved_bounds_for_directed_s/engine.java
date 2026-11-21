import java.util.*;

public final class ShortestPath {
    private static final long INF = Long.MAX_VALUE / 4;

    public static long[] dijkstraSparse(Graph g, int source) {
        int V = g.V;
        long[] dist = new long[V];
        Arrays.fill(dist, INF);
        dist[source] = 0L;

        PriorityQueue<VertexDist> pq = new PriorityQueue<>(Comparator.comparingLong(vd -> vd.dist));
        pq.offer(new VertexDist(source, 0L));

        boolean[] visited = new boolean[V];
        List<Edge>[] adj = g.getAdjList();

        while (!pq.isEmpty()) {
            VertexDist cur = pq.poll();
            int u = cur.vertex;
            if (visited[u]) continue;
            visited[u] = true;

            for (Edge e : adj[u]) {
                int v = e.to;
                long newDist = cur.dist + e.weight;
                if (newDist < dist[v]) {
                    dist[v] = newDist;
                    pq.offer(new VertexDist(v, newDist));
                }
            }
        }
        return dist;
    }

    public static long[] dijkstraDense(Graph g, int source) {
        int V = g.V;
        long[] dist = new long[V];
        boolean[] visited = new boolean[V];
        Arrays.fill(dist, INF);
        dist[source] = 0L;

        long[][] mat = g.getMatrix();

        for (int i = 0; i < V; i++) {
            int u = -1;
            long best = INF;
            for (int v = 0; v < V; v++) {
                if (!visited[v] && dist[v] < best) {
                    best = dist[v];
                    u = v;
                }
            }
            if (u == -1) break;
            visited[u] = true;

            for (int v = 0; v < V; v++) {
                long w = mat[u][v];
                if (w != INF && dist[u] + w < dist[v]) {
                    dist[v] = dist[u] + w;
                }
            }
        }
        return dist;
    }

    public static long[] compute(Graph g, int source) {
        if (source < 0 || source >= g.V) {
            throw new IllegalArgumentException("Source vertex out of bounds");
        }
        if (g.isDense()) {
            return dijkstraDense(g, source);
        } else {
            return dijkstraSparse(g, source);
        }
    }

    private static final class VertexDist {
        final int vertex;
        final long dist;

        VertexDist(int vertex, long dist) {
            this.vertex = vertex;
            this.dist = dist;
        }
    }

    public static long INF() {
        return INF;
    }
}
