public enum NodeState {
    FOLLOWER,
    CANDIDATE,
    LEADER
}

public final class RequestVote {
    public final int term;
    public final int candidateId;
    public final int lastLogIndex;
    public final int lastLogTerm;

    public RequestVote(int term, int candidateId, int lastLogIndex, int lastLogTerm) {
        this.term = term;
        this.candidateId = candidateId;
        this.lastLogIndex = lastLogIndex;
        this.lastLogTerm = lastLogTerm;
    }
}

public final class RequestVoteResponse {
    public final int term;
    public final boolean voteGranted;

    public RequestVoteResponse(int term, boolean voteGranted) {
        this.term = term;
        this.voteGranted = voteGranted;
    }
}

public final class AppendEntries {
    public final int term;
    public final int leaderId;
    public final int prevLogIndex;
    public final int prevLogTerm;
    public final int[] entries; // empty for heartbeat
    public final int leaderCommit;

    public AppendEntries(int term, int leaderId, int prevLogIndex, int prevLogTerm,
                         int[] entries, int leaderCommit) {
        this.term = term;
        this.leaderId = leaderId;
        this.prevLogIndex = prevLogIndex;
        this.prevLogTerm = prevLogTerm;
        this.entries = entries;
        this.leaderCommit = leaderCommit;
    }
}

public final class AppendEntriesResponse {
    public final int term;
    public final boolean success;

    public AppendEntriesResponse(int term, boolean success) {
        this.term = term;
        this.success = success;
    }
}
