import java.util.*;

public class Main {
    public static void main(String[] args) {
        testLeaderElectionSimple();
        testLeaderElectionHigherTermPreempts();
        testLogReplication();
        System.out.println("All tests passed.");
    }

    private static void assertTrue(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
    }

    private static void testLeaderElectionSimple() {
        RaftCluster cluster = new RaftCluster(1, 2, 3);
        cluster.triggerElection(1);
        RaftNode n1 = cluster.getNode(1);
        RaftNode n2 = cluster.getNode(2);
        RaftNode n3 = cluster.getNode(3);
        assertTrue(n1.state == NodeState.LEADER, "Node1 should be leader");
        assertTrue(n2.state == NodeState.FOLLOWER, "Node2 should be follower");
        assertTrue(n3.state == NodeState.FOLLOWER, "Node3 should be follower");
        assertTrue(n1.currentTerm == 1, "Term should be 1");
        assertTrue(n2.currentTerm == 1, "Followers term should match leader");
    }

    private static void testLeaderElectionHigherTermPreempts() {
        RaftCluster cluster = new RaftCluster(1, 2, 3, 4, 5);
        // First election by node1
        cluster.triggerElection(1);
        RaftNode n1 = cluster.getNode(1);
        assertTrue(n1.state == NodeState.LEADER, "Node1 first leader");
        // Now node2 starts election with higher term
        cluster.triggerElection(2);
        RaftNode n2 = cluster.getNode(2);
        assertTrue(n2.state == NodeState.LEADER, "Node2 should preempt with higher term");
        assertTrue(n2.currentTerm == 2, "Node2 term should be 2");
        assertTrue(n1.state == NodeState.FOLLOWER, "Node1 demoted to follower");
    }

    private static void testLogReplication() {
        RaftCluster cluster = new RaftCluster(1, 2, 3);
        cluster.triggerElection(1);
        RaftNode leader = cluster.getNode(1);
        assertTrue(leader.state == NodeState.LEADER, "Leader elected");
        // Propose a command
        cluster.propose(1, "set x=1");
        // Verify all nodes have the entry
        for (RaftNode n : cluster.getAllNodes()) {
            assertTrue(n.log.size() == 1, "Node " + n.id + " should have 1 log entry");
            LogEntry e = n.log.get(0);
            assertTrue(e.command.equals("set x=1"), "Command mismatch on node " + n.id);
            assertTrue(e.term == leader.currentTerm, "Term mismatch on node " + n.id);
        }
        // Commit index should be at least 1 on leader
        assertTrue(leader.commitIndex == 1, "Leader commit index should be 1");
        // Followers should also have commitIndex updated
        for (RaftNode n : cluster.getAllNodes()) {
            if (n.id != leader.id) {
                assertTrue(n.commitIndex == 1, "Follower " + n.id + " commit index should be 1");
            }
        }
    }
}
