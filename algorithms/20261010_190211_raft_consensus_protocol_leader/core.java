import java.util.*;

public class RaftNode {
    public enum State {FOLLOWER, CANDIDATE, LEADER}

    private final int id;
    private final int totalNodes;
    private int currentTerm = 0;
    private Integer votedFor = null;
    private State state = State.FOLLOWER;
    private int votesReceived = 0;

    public RaftNode(int id, int totalNodes) {
        this.id = id;
        this.totalNodes = totalNodes;
    }

    public int getId() { return id; }
    public int getCurrentTerm() { return currentTerm; }
    public State getState() { return state; }

    // ---------- RPC Messages ----------
    public static class RequestVoteRequest {
        public final int term;
        public final int candidateId;
        public RequestVoteRequest(int term, int candidateId) {
            this.term = term;
            this.candidateId = candidateId;
        }
    }

    public static class RequestVoteResponse {
        public final int term;
        public final boolean voteGranted;
        public RequestVoteResponse(int term, boolean voteGranted) {
            this.term = term;
            this.voteGranted = voteGranted;
        }
    }

    public static class VoteResponse {
        public final int term;
        public final boolean voteGranted;
        public VoteResponse(int term, boolean voteGranted) {
            this.term = term;
            this.voteGranted = voteGranted;
        }
    }

    // ---------- Core Logic ----------
    public RequestVoteResponse handleRequestVote(RequestVoteRequest req) {
        if (req.term < currentTerm) {
            return new RequestVoteResponse(currentTerm, false);
        }
        if (req.term > currentTerm) {
            currentTerm = req.term;
            state = State.FOLLOWER;
            votedFor = null;
        }
        boolean voteGranted = false;
        if (votedFor == null || votedFor == req.candidateId) {
            votedFor = req.candidateId;
            voteGranted = true;
        }
        return new RequestVoteResponse(currentTerm, voteGranted);
    }

    public void handleVoteResponse(VoteResponse resp) {
        if (state != State.CANDIDATE) return;
        if (resp.term > currentTerm) {
            currentTerm = resp.term;
            state = State.FOLLOWER;
            votedFor = null;
            return;
        }
        if (resp.voteGranted) {
            votesReceived++;
            if (votesReceived > totalNodes / 2) {
                becomeLeader();
            }
        }
    }

    public void startElection() {
        state = State.CANDIDATE;
        currentTerm++;
        votedFor = id;
        votesReceived = 1; // vote for self
    }

    private void becomeLeader() {
        state = State.LEADER;
    }

    public void stepDown(int term) {
        if (term > currentTerm) {
            currentTerm = term;
            state = State.FOLLOWER;
            votedFor = null;
        }
    }
}
