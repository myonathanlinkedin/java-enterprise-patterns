import java.util.ArrayList;
import java.util.List;

/** Simple deterministic cluster driver with embedded unit tests. */
public class Main {
    /** Concrete MessageSender that routes messages within a single JVM cluster. */
    static final class RaftCluster implements MessageSender {
        private final List<RaftNode> nodes = new ArrayList<>();

        RaftCluster(int size) {
            for (int i = 0; i < size; i++) {
                nodes.add(new RaftNode(i, this, size));
            }
        }

        RaftNode getNode(int id) {
            return nodes.get(id);
        }

        List<RaftNode> getAllNodes() {
            return nodes;
        }

        @Override
        public void sendRequestVote(int targetId, RequestVote request) {
            RaftNode target = nodes.get(targetId);
            RequestVoteResponse resp = target.onRequestVote(request);
            // Deliver response back to candidate
            RaftNode candidate = nodes.get(request.candidateId);
            // In a real implementation we would include the responder's id.
            // Here we simply forward the response; the candidate counts each response once.
            candidate.receiveVoteResponse(resp);
        }

        @Override
        public void sendAppendEntries(int targetId, AppendEntries request) {
            RaftNode target = nodes.get(targetId);
            target.onAppendEntries(request);
        }

        /** Simulate a single tick for every node in the cluster. */
        void tickAll() {
            for (RaftNode node : nodes) {
                node.tick();
            }
        }
    }

    /** Helper assertion method to avoid disabled Java assertions. */
    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    /** Unit test: basic election in a 3‑node cluster. */
    private static void testThreeNodeElection() {
        RaftCluster cluster = new RaftCluster(3);

        // Simulate time until first election timeout fires
        for (int i = 0; i < 6; i++) { // > DEFAULT_ELECTION_TIMEOUT
            cluster.tickAll();
        }

        // After timeout, exactly one node must have become leader
        int leaderCount = 0;
        int leaderId = -1;
        int term = -1;
        for (RaftNode node : cluster.getAllNodes()) {
            if (node.getState() == ServerState.LEADER) {
                leaderCount++;
                leaderId = node.id;
                term = node.getCurrentTerm();
            }
        }
        assertTrue(leaderCount == 1, "Exactly one leader expected, found " + leaderCount);
        assertTrue(term > 0, "Leader term must be positive");
        // All followers must have the same term as the leader
        for (RaftNode node : cluster.getAllNodes()) {
            if (node.id != leaderId) {
                assertTrue(node.getState() == ServerState.FOLLOWER,
                        "Non‑leader must be follower");
                assertTrue(node.getCurrentTerm() == term,
                        "Follower term must match leader term");
            }
        }
    }

    /** Unit test: leader steps down when a higher‑term AppendEntries arrives. */
    private static void testLeaderStepDownOnHigherTerm() {
        RaftCluster cluster = new RaftCluster(3);

        // Force node 0 to become leader quickly
        for (int i = 0; i < 6; i++) {
            cluster.tickAll();
        }
        RaftNode leader = null;
        for (RaftNode node : cluster.getAllNodes()) {
            if (node.getState() == ServerState.LEADER) {
                leader = node;
                break;
            }
        }
        assertTrue(leader != null, "Leader should have been elected");

        // Simulate a higher‑term AppendEntries from node 1 (pretend it became leader)
        int higherTerm = leader.getCurrentTerm() + 1;
        AppendEntries fakeAe = new AppendEntries(higherTerm, 1);
        // Deliver directly to the current leader
        AppendEntriesResponse resp = leader.onAppendEntries(fakeAe);
        assertTrue(resp.success, "Leader should accept higher‑term AppendEntries");
        assertTrue(leader.getState() == ServerState.FOLLOWER,
                "Leader must step down to follower on higher term");
        assertTrue(leader.getCurrentTerm() == higherTerm,
                "Leader term must be updated to higher term");
    }

    /** Unit test: split vote scenario in a 4‑node cluster (no majority). */
    private static void testSplitVoteNoLeader() {
        RaftCluster cluster = new RaftCluster(4);

        // Manually trigger two candidates at the same time
        RaftNode candA = cluster.getNode(0);
        RaftNode candB = cluster.getNode(1);

        // Both start election in the same term
        candA.startElection(); // becomes candidate term 1
        candB.startElection(); // also becomes candidate term 1 (simulated simultaneous)

        // Deliver votes: each candidate will receive its own vote + one from a distinct follower
        // Node 2 votes for candA, Node 3 votes for candB
        // Simulate by invoking onRequestVote directly
        RequestVote rvA = new RequestVote(candA.getCurrentTerm(), candA.id);
        RequestVote rvB = new RequestVote(candB.getCurrentTerm(), candB.id);

        // Node 2 votes for A
        RequestVoteResponse r2 = cluster.getNode(2).onRequestVote(rvA);
        candA.receiveVoteResponse(r2);
        // Node 3 votes for B
        RequestVoteResponse r3 = cluster.getNode(3).onRequestVote(rvB);
        candB.receiveVoteResponse(r3);

        // At this point each candidate has exactly 2 votes (including self) -> no majority (need >2)
        assertTrue(candA.getState() != ServerState.LEADER,
                "Candidate A should not become leader without majority");
        assertTrue(candB.getState() != ServerState.LEADER,
                "Candidate B should not become leader without majority");
    }

    public static void main(String[] args) {
        testThreeNodeElection();
        testLeaderStepDownOnHigherTerm();
        testSplitVoteNoLeader();
        System.out.println("All Raft leader election tests passed.");
    }
}
