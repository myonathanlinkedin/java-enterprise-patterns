import java.util.*;

public class Main {
    public static void main(String[] args) {
        testSingleNodeElection();
        testMajorityElection();
        testTermUpdateOnRequestVote();
        testElectionTie();
        testStepDownOnHigherTerm();
        System.out.println("All tests passed.");
    }

    private static void testSingleNodeElection() {
        RaftNode node = new RaftNode(1, 1);
        node.startElection();
        assert node.getState() == RaftNode.State.CANDIDATE : "Should be candidate";
        // Simulate self vote
        node.handleVoteResponse(new RaftNode.VoteResponse(node.getCurrentTerm(), true));
        assert node.getState() == RaftNode.State.LEADER : "Should become leader";
    }

    private static void testMajorityElection() {
        int n = 5;
        List<RaftNode> nodes = new ArrayList<>();
        for (int i = 1; i <= n; i++) nodes.add(new RaftNode(i, n));
        RaftNode leaderCandidate = nodes.get(0);
        leaderCandidate.startElection();

        // Simulate votes from majority
        for (int i = 1; i <= 2; i++) { // nodes 2 and 3
            RaftNode voter = nodes.get(i);
            RaftNode.RequestVoteResponse resp = voter.handleRequestVote(
                new RaftNode.RequestVoteRequest(leaderCandidate.getCurrentTerm(), leaderCandidate.getId()));
            leaderCandidate.handleVoteResponse(new RaftNode.VoteResponse(resp.term, resp.voteGranted));
        }
        assert leaderCandidate.getState() == RaftNode.State.LEADER : "Should become leader after majority";
    }

    private static void testTermUpdateOnRequestVote() {
        RaftNode nodeA = new RaftNode(1, 3);
        RaftNode nodeB = new RaftNode(2, 3);

        nodeA.startElection(); // term 1
        nodeB.startElection(); // term 1

        // nodeB receives higher term from nodeA
        RaftNode.RequestVoteResponse resp = nodeB.handleRequestVote(
            new RaftNode.RequestVoteRequest(2, nodeA.getId()));
        assert resp.term == 2 : "Term should be updated to 2";
        assert nodeB.getState() == RaftNode.State.FOLLOWER : "Should step down to follower";
    }

    private static void testElectionTie() {
        int n = 3;
        List<RaftNode> nodes = new ArrayList<>();
        for (int i = 1; i <= n; i++) nodes.add(new RaftNode(i, n));
        RaftNode node1 = nodes.get(0);
        node1.startElection();

        // node2 votes for node1
        RaftNode node2 = nodes.get(1);
        RaftNode.RequestVoteResponse resp2 = node2.handleRequestVote(
            new RaftNode.RequestVoteRequest(node1.getCurrentTerm(), node1.getId()));
        node1.handleVoteResponse(new RaftNode.VoteResponse(resp2.term, resp2.voteGranted));

        // node3 votes for node2
        RaftNode node3 = nodes.get(2);
        RaftNode.RequestVoteResponse resp3 = node3.handleRequestVote(
            new RaftNode.RequestVoteRequest(node1.getCurrentTerm(), node2.getId()));
        node1.handleVoteResponse(new RaftNode.VoteResponse(resp3.term, resp3.voteGranted));

        assert node1.getState() == RaftNode.State.CANDIDATE : "Should remain candidate due to tie";
    }

    private static void testStepDownOnHigherTerm() {
        RaftNode leader = new RaftNode(1, 3);
        leader.startElection();
        // Simulate leader becoming leader
        leader.handleVoteResponse(new RaftNode.VoteResponse(leader.getCurrentTerm(), true));

        RaftNode follower = new RaftNode(2, 3);
        follower.startElection(); // term 1
        // follower receives higher term from leader
        follower.stepDown(leader.getCurrentTerm() + 1);
        assert follower.getState() == RaftNode.State.FOLLOWER : "Should step down to follower";
        assert follower.getCurrentTerm() == leader.getCurrentTerm() + 1 : "Term should be updated";
    }
}
