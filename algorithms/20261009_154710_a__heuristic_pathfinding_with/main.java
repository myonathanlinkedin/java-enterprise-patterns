import java.util.*;

public class Main {
    public static void main(String[] args) {
        testBasicPath();
        testUnreachable();
        testDynamicCost();
        testStartGoalSame();
        testOutOfBounds();
        testObstacleStartGoal();
        System.out.println("All tests passed.");
    }

    private static void assertTrue(boolean condition, String msg) {
        if (!condition) throw new AssertionError(msg);
    }

    private static void testBasicPath() {
        AStarPathfinder.Grid g = new AStarPathfinder.Grid(5,5,1);
        // place obstacles
        g.setCost(2,2, AStarPathfinder.INF);
        List<AStarPathfinder.Point> path = AStarPathfinder.findPath(g, new AStarPathfinder.Point(0,0), new AStarPathfinder.Point(4,4));
        assertTrue(path != null, "Path should exist");
        assertTrue(path.size() == 9, "Path length should be 9");
        // verify path does not cross obstacle
        for (AStarPathfinder.Point p : path) {
            assertTrue(p.x != 2 || p.y != 2, "Path should avoid obstacle");
        }
    }

    private static void testUnreachable() {
        AStarPathfinder.Grid g = new AStarPathfinder.Grid(3,3,1);
        // surround goal with obstacles
        g.setCost(1,1, AStarPathfinder.INF);
        g.setCost(0,1, AStarPathfinder.INF);
        g.setCost(1,0, AStarPathfinder.INF);
        List<AStarPathfinder.Point> path = AStarPathfinder.findPath(g, new AStarPathfinder.Point(0,0), new AStarPathfinder.Point(1,1));
        assertTrue(path == null, "No path should exist");
    }

    private static void testDynamicCost() {
        AStarPathfinder.Grid g = new AStarPathfinder.Grid(3,3,1);
        // high cost corridor
        g.setCost(1,0, 10);
        g.setCost(1,1, 10);
        g.setCost(1,2, 10);
        List<AStarPathfinder.Point> path = AStarPathfinder.findPath(g, new AStarPathfinder.Point(0,1), new AStarPathfinder.Point(2,1));
        assertTrue(path != null, "Path should exist");
        int cost = 0;
        for (int i=1;i<path.size();i++) {
            AStarPathfinder.Point p = path.get(i);
            cost += g.getCost(p.x, p.y);
        }
        assertTrue(cost == 3, "Cost should be minimal (3) via top or bottom row");

        // lower corridor cost
        g.setCost(1,0, 1);
        g.setCost(1,1, 1);
        g.setCost(1,2, 1);
        path = AStarPathfinder.findPath(g, new AStarPathfinder.Point(0,1), new AStarPathfinder.Point(2,1));
        cost = 0;
        for (int i=1;i<path.size();i++) {
            AStarPathfinder.Point p = path.get(i);
            cost += g.getCost(p.x, p.y);
        }
        assertTrue(cost == 3, "Cost should still be 3 but path may use corridor");
    }

    private static void testStartGoalSame() {
        AStarPathfinder.Grid g = new AStarPathfinder.Grid(2,2,1);
        List<AStarPathfinder.Point> path = AStarPathfinder.findPath(g, new AStarPathfinder.Point(0,0), new AStarPathfinder.Point(0,0));
        assertTrue(path != null && path.size() == 1, "Path should be single point");
    }

    private static void testOutOfBounds() {
        AStarPathfinder.Grid g = new AStarPathfinder.Grid(2,2,1);
        List<AStarPathfinder.Point> path = AStarPathfinder.findPath(g, new AStarPathfinder.Point(-1,0), new AStarPathfinder.Point(0,0));
        assertTrue(path == null, "Start out of bounds should return null");
        path = AStarPathfinder.findPath(g, new AStarPathfinder.Point(0,0), new AStarPathfinder.Point(2,2));
        assertTrue(path == null, "Goal out of bounds should return null");
    }

    private static void testObstacleStartGoal() {
        AStarPathfinder.Grid g = new AStarPathfinder.Grid(2,2,1);
        g.setCost(0,0, AStarPathfinder.INF);
        List<AStarPathfinder.Point> path = AStarPathfinder.findPath(g, new AStarPathfinder.Point(0,0), new AStarPathfinder.Point(1,1));
        assertTrue(path == null, "Start on obstacle should return null");
        g.setCost(0,0, 1);
        g.setCost(1,1, AStarPathfinder.INF);
        path = AStarPathfinder.findPath(g, new AStarPathfinder.Point(0,0), new AStarPathfinder.Point(1,1));
        assertTrue(path == null, "Goal on obstacle should return null");
    }
}
