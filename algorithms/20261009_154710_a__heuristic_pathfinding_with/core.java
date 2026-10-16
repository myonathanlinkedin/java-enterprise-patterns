import java.util.*;

public class AStarPathfinder {

    public static final int INF = Integer.MAX_VALUE / 2;

    public static class Grid {
        private final int width;
        private final int height;
        private final int[][] cost;

        public Grid(int width, int height, int defaultCost) {
            this.width = width;
            this.height = height;
            this.cost = new int[height][width];
            for (int y = 0; y < height; y++) {
                Arrays.fill(this.cost[y], defaultCost);
            }
        }

        public boolean inBounds(int x, int y) {
            return x >= 0 && x < width && y >= 0 && y < height;
        }

        public int getCost(int x, int y) {
            return cost[y][x];
        }

        public void setCost(int x, int y, int c) {
            if (inBounds(x, y)) {
                cost[y][x] = c;
            }
        }

        public boolean isWalkable(int x, int y) {
            return inBounds(x, y) && cost[y][x] < INF;
        }
    }

    public static class Point {
        public final int x;
        public final int y;

        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Point)) return false;
            Point p = (Point) o;
            return x == p.x && y == p.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }

    private static class Node {
        final Point p;
        final int g; // cost from start
        final int h; // heuristic to goal
        final int f; // g + h
        final Node parent;

        Node(Point p, int g, int h, Node parent) {
            this.p = p;
            this.g = g;
            this.h = h;
            this.f = g + h;
            this.parent = parent;
        }
    }

    private static int heuristic(Point a, Point b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }

    public static List<Point> findPath(Grid grid, Point start, Point goal) {
        if (!grid.inBounds(start.x, start.y) || !grid.inBounds(goal.x, goal.y))
            return null;
        if (!grid.isWalkable(start.x, start.y) || !grid.isWalkable(goal.x, goal.y))
            return null;
        if (start.equals(goal)) {
            return Collections.singletonList(start);
        }

        PriorityQueue<Node> open = new PriorityQueue<>(Comparator.comparingInt(n -> n.f));
        Map<Point, Node> openMap = new HashMap<>();
        Set<Point> closed = new HashSet<>();

        Node startNode = new Node(start, 0, heuristic(start, goal), null);
        open.add(startNode);
        openMap.put(start, startNode);

        final int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1}};

        while (!open.isEmpty()) {
            Node current = open.poll();
            openMap.remove(current.p);
            if (current.p.equals(goal)) {
                return reconstructPath(current);
            }
            closed.add(current.p);

            for (int[] d : dirs) {
                int nx = current.p.x + d[0];
                int ny = current.p.y + d[1];
                if (!grid.isWalkable(nx, ny)) continue;
                Point neighbor = new Point(nx, ny);
                if (closed.contains(neighbor)) continue;

                int tentativeG = current.g + grid.getCost(nx, ny);
                Node existing = openMap.get(neighbor);
                if (existing == null || tentativeG < existing.g) {
                    Node neighborNode = new Node(neighbor, tentativeG, heuristic(neighbor, goal), current);
                    open.add(neighborNode);
                    openMap.put(neighbor, neighborNode);
                }
            }
        }
        return null; // no path
    }

    private static List<Point> reconstructPath(Node goalNode) {
        LinkedList<Point> path = new LinkedList<>();
        Node current = goalNode;
        while (current != null) {
            path.addFirst(current.p);
            current = current.parent;
        }
        return path;
    }
}
