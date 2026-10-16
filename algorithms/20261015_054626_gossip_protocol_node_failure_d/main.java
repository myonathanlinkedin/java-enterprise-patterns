import java.util.*;

public final class Main {
    public static void main(String[] args) {
        testNoFailure();
        testSingleFailure();
        testPartialRounds();
        System.out.println("All tests passed.");
    }

    private static void testNoFailure() {
        int nodeCount = 5;
        int threshold = 3;
        int rounds = 10;
        Map<Integer, Node> nodes = new HashMap<>();
        Set<Integer> allIds = new HashSet<>();
        for (int i = 0; i < nodeCount; i++) allIds.add(i);
        for (int i = 0; i < nodeCount; i++) {
            Set<Integer> neighbors = new HashSet<>(allIds);
            neighbors.remove(i);
            nodes.put(i, new Node(i, neighbors, threshold));
        }
        GossipEngine engine = new GossipEngine(nodes, rounds, 42L);
        engine.run();
        for (Node node : engine.getAllNodes()) {
            for (int neighborId : node.getNeighbors()) {
                assert !node.isSuspectedFailed(neighborId) :
                    "Node " + node.getId() + " incorrectly suspects failure of neighbor " + neighborId;
            }
        }
    }

    private static void testSingleFailure() {
        int nodeCount = 5;
        int threshold = 3;
        int roundsBeforeFailure = 5;
        int roundsAfterFailure = 5;
        Map<Integer, Node> nodes = new HashMap<>();
        Set<Integer> allIds = new HashSet<>();
        for (int i = 0; i < nodeCount; i++) allIds.add(i);
        for (int i = 0; i < nodeCount; i++) {
            Set<Integer> neighbors = new HashSet<>(allIds);
            neighbors.remove(i);
            nodes.put(i, new Node(i, neighbors, threshold));
        }
        GossipEngine engine = new GossipEngine(nodes, roundsBeforeFailure, 123L);
        engine.run();
        Node failedNode = engine.getNode(2);
        failedNode.setAlive(false);
        engine = new GossipEngine(nodes, roundsAfterFailure, 456L);
        engine.run();
        for (Node node : engine.getAllNodes()) {
            if (node.getId() == 2) continue;
            Set<Integer> suspected = node.getSuspectedFailedNodes();
            assert suspected.contains(2) :
                "Node " + node.getId() + " did not suspect failed node 2.";
        }
    }

    private static void testPartialRounds() {
        int nodeCount = 5;
        int threshold = 3;
        int roundsBeforeFailure = 5;
        int roundsAfterFailure = 2; // less than threshold
        Map<Integer, Node> nodes = new HashMap<>();
        Set<Integer> allIds = new HashSet<>();
        for (int i = 0; i < nodeCount; i++) allIds.add(i);
        for (int i = 0; i < nodeCount; i++) {
            Set<Integer> neighbors = new HashSet<>(allIds);
            neighbors.remove(i);
            nodes.put(i, new Node(i, neighbors, threshold));
        }
        GossipEngine engine = new GossipEngine(nodes, roundsBeforeFailure, 789L);
        engine.run();
        Node failedNode = engine.getNode(3);
        failedNode.setAlive(false);
        engine = new GossipEngine(nodes, roundsAfterFailure, 101112L);
        engine.run();
        for (Node node : engine.getAllNodes()) {
            if (node.getId() == 3) continue;
            Set<Integer> suspected = node.getSuspectedFailedNodes();
            assert !suspected.contains(3) :
                "Node " + node.getId() + " incorrectly suspects node 3 with insufficient rounds.";
        }
    }
}
