import java.util.*;

public class Main {
    public static void main(String[] args) {
        testSingleElection();
        testElectionAfterLeader();
        testElectionWithDifferentStarter();
        testMajorityCalculation();
        System.out.println("All tests passed.");
    }

    private static void testSingleElection() {
        RaftEngine engine = new RaftEngine();
        engine.addNode(new Node(1));
        engine.addNode(new Node(2));
        engine.addNode(new Node(3));

        engine.startElection(1);

        Node n1 = engine.getNode(1);
        Node n2 = engine.getNode(2);
        Node n3 = engine.getNode(3);

        if (n1.state != NodeState.LEADER) {
            throw new AssertionError("Node 1 should be leader");
        }
        if (n1.currentTerm != 1) {
            throw new AssertionError("Node 1 term should be 1");
        }
        if (n2.state != NodeState.FOLLOWER || n2.currentTerm != 1) {
            throw new AssertionError("Node 2 should be follower with term 1");
        }
        if (n3.state != NodeState.FOLLOWER || n3.currentTerm != 1) {
            throw new AssertionError("Node 3 should be follower with term 1");
        }
    }

    private static void testElectionAfterLeader() {
        RaftEngine engine = new RaftEngine();
        engine.addNode(new Node(1));
        engine.addNode(new Node(2));
        engine.addNode(new Node(3));

        engine.startElection(1); // Node 1 becomes leader

        engine.startElection(2); // Node 2 attempts election

        Node n2 = engine.getNode(2);
        if (n2.state != NodeState.FOLLOWER) {
            throw new AssertionError("Node 2 should remain follower after leader exists");
        }
        if (n2.currentTerm != 1) {
            throw new AssertionError("Node 2 term should be 1 after election attempt");
        }
    }

    private static void testElectionWithDifferentStarter() {
        RaftEngine engine = new RaftEngine();
        engine.addNode(new Node(1));
        engine.addNode(new Node(2));
        engine.addNode(new Node(3));

        engine.startElection(2); // Node 2 starts first

        Node n2 = engine.getNode(2);
        Node n1 = engine.getNode(1);
        Node n3 = engine.getNode(3);

        if (n2.state != NodeState.LEADER) {
            throw new AssertionError("Node 2 should be leader");
        }
        if (n1.state != NodeState.FOLLOWER || n1.currentTerm != 1) {
            throw new AssertionError("Node 1 should be follower with term 1");
        }
        if (n3.state != NodeState.FOLLOWER || n3.currentTerm != 1) {
            throw new AssertionError("Node 3 should be follower with term 1");
        }
    }

    private static void testMajorityCalculation() {
        RaftEngine engine = new RaftEngine();
        engine.addNode(new Node(1));
        engine.addNode(new Node(2));
        engine.addNode(new Node(3));
        engine.addNode(new Node(4));

        if (engine.getMajority() != 3) {
            throw new AssertionError("Majority for 4 nodes should be 3");
        }

        engine.startElection(1);
        Node n1 = engine.getNode(1);
        if (n1.state != NodeState.LEADER) {
            throw new AssertionError("Node 1 should be leader in 4-node cluster");
        }
    }
}
