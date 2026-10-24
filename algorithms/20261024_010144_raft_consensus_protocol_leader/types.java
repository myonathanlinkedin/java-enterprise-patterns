import java.util.Objects;

/** Server roles in the Raft protocol. */
enum ServerState {
    FOLLOWER,
    CANDIDATE,
    LEADER
}

/** RequestVote RPC arguments. */
final class RequestVote {
    final int term;
    final int candidateId;

    RequestVote(int term, int candidateId) {
        this.term = term;
        this.candidateId = candidateId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RequestVote)) return false;
        RequestVote that = (RequestVote) o;
        return term == that.term && candidateId == that.candidateId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(term, candidateId);
    }
}

/** Response to a RequestVote RPC. */
final class RequestVoteResponse {
    final int term;
    final boolean voteGranted;

    RequestVoteResponse(int term, boolean voteGranted) {
        this.term = term;
        this.voteGranted = voteGranted;
    }
}

/** AppendEntries RPC arguments (heartbeat only, no log entries). */
final class AppendEntries {
    final int term;
    final int leaderId;

    AppendEntries(int term, int leaderId) {
        this.term = term;
        this.leaderId = leaderId;
    }
}

/** Response to an AppendEntries RPC. */
final class AppendEntriesResponse {
    final int term;
    final boolean success;

    AppendEntriesResponse(int term, boolean success) {
        this.term = term;
        this.success = success;
    }
}

/** Minimal interface for message delivery between nodes. */
interface MessageSender {
    void sendRequestVote(int targetId, RequestVote request);
    void sendAppendEntries(int targetId, AppendEntries request);
}
