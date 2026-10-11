import java.util.ArrayList;
import java.util.List;

public class Main {
    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void testSingleNodeElection() {
        List<RaftNode> cluster = new ArrayList<>();
        cluster.add(new RaftNode(1));
        RaftEngine engine = new RaftEngine(cluster);

        engine.startElection(1);
        RaftNode leader = engine.getLeader();

        assertTrue(leader != null, "Single node cluster should have a leader");
        assertTrue(leader.getId() == 1, "Leader should be node 1");
        assertTrue(leader.getState() == NodeState.LEADER, "Node 1 must be in LEADER state");
    }

    private static void testMajorityElection() {
        List<RaftNode> cluster = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            cluster.add(new RaftNode(i));
        }
        RaftEngine engine = new RaftEngine(cluster);

        engine.startElection(2); // node 2 initiates election
        RaftNode leader = engine.getLeader();

        assertTrue(leader != null, "A leader should be elected with majority");
        assertTrue(leader.getId() == 2, "Node 2 should become leader (smallest id among candidates)");
        assertTrue(leader.getState() == NodeState.LEADER, "Leader must be in LEADER state");
        // Verify all other nodes are followers and have term equal to leader's term
        int leaderTerm = leader.getCurrentTerm();
        for (RaftNode n : engine.snapshot()) {
            if (n.getId() != leader.getId()) {
                assertTrue(n.getState() == NodeState.FOLLOWER, "Non-leader nodes must be followers");
                assertTrue(n.getCurrentTerm() == leaderTerm, "Terms must be synchronized after election");
            }
        }
    }

    private static void testSplitVoteNoLeader() {
        // 4-node cluster: even number leads to possible split vote
        List<RaftNode> cluster = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            cluster.add(new RaftNode(i));
        }
        RaftEngine engine = new RaftEngine(cluster);

        // Force two candidates to start elections in the same term.
        // Candidate 1
        engine.startElection(1);
        // Candidate 3 (simulated as happening before node 1 receives majority)
        engine.startElection(3);

        // In this deterministic simulation, the second startElection will see a higher term
        // and cause the first candidate to step down. No node should be leader.
        RaftNode leader = engine.getLeader();
        assertTrue(leader == null, "No leader should be elected after split vote");
        // All nodes must be followers with the highest term observed
        int maxTerm = 0;
        for (RaftNode n : engine.snapshot()) {
            maxTerm = Math.max(maxTerm, n.getCurrentTerm());
        }
        for (RaftNode n : engine.snapshot()) {
            assertTrue(n.getState() == NodeState.FOLLOWER, "All nodes should be followers after split");
            assertTrue(n.getCurrentTerm() == maxTerm, "All nodes must share the highest term");
        }
    }

    private static void testHigherTermRejection() {
        List<RaftNode> cluster = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            cluster.add(new RaftNode(i));
        }
        RaftEngine engine = new RaftEngine(cluster);

        // Node 1 starts election, becomes leader at term 1
        engine.startElection(1);
        RaftNode leader = engine.getLeader();
        assertTrue(leader != null && leader.getId() == 1, "Node 1 should be leader");

        // Node 2 receives an AppendEntries with higher term (term 2) from an external leader
        AppendEntries fakeAppend = new AppendEntries(2, 99, 0, 0, new int[0], 0);
        AppendEntriesResponse resp = engine.getNode(2).onAppendEntries(fakeAppend);
        assertTrue(resp.success, "AppendEntries with higher term must succeed");
        assertTrue(engine.getNode(2).getCurrentTerm() == 2, "Node 2 term must advance to 2");
        assertTrue(engine.getNode(2).getState() == NodeState.FOLLOWER, "Node 2 must be follower after higher term");

        // Node 1 (old leader) receives the same AppendEntries and should step down
        AppendEntriesResponse resp1 = engine.getNode(1).onAppendEntries(fakeAppend);
        assertTrue(resp1.success, "Old leader should accept higher term AppendEntries");
        assertTrue(engine.getNode(1).getCurrentTerm() == 2, "Old leader term must be updated");
        assertTrue(engine.getNode(1).getState() == NodeState.FOLLOWER, "Old leader must become follower");
    }

    public static void main(String[] args) {
        testSingleNodeElection();
        testMajorityElection();
        testSplitVoteNoLeader();
        testHigherTermRejection();

        System.out.println("All Raft leader election tests passed.");
    }
}
