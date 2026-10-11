import java.util.*;

public enum State { FOLLOWER, CANDIDATE, LEADER }

public class RaftNode {
    public final int id;
    public final List<RaftNode> peers;
    public State state;
    public int currentTerm;
    public Integer votedFor;
    public int electionTimeout;
    public int electionElapsed;
    public int votesReceived;
    private final Random random;

    public RaftNode(int id, List<RaftNode> peers, Random random) {
        this.id = id;
        this.peers = peers;
        this.random = random;
        this.state = State.FOLLOWER;
        this.currentTerm = 0;
        this.votedFor = null;
        this.resetElectionTimer();
        this.electionElapsed = 0;
        this.votesReceived = 0;
    }

    public void tick() {
        if (state == State.LEADER) {
            sendAppendEntries();
        } else {
            electionElapsed++;
            if (electionElapsed >= electionTimeout) {
                becomeCandidate();
            }
        }
    }

    public void receiveRequestVote(RequestVote msg) {
        if (msg.term < currentTerm) {
            sendVoteResponse(msg.candidateId, false);
            return;
        }
        if (msg.term > currentTerm) {
            becomeFollower(msg.term);
        }
        boolean voteGranted = false;
        if (votedFor == null || votedFor == msg.candidateId) {
            votedFor = msg.candidateId;
            voteGranted = true;
            resetElectionTimer();
        }
        sendVoteResponse(msg.candidateId, voteGranted);
    }

    public void receiveVoteResponse(VoteResponse msg) {
        if (state != State.CANDIDATE) return;
        if (msg.term > currentTerm) {
            becomeFollower(msg.term);
            return;
        }
        if (msg.term < currentTerm) return;
        if (msg.voteGranted) {
            votesReceived++;
            if (votesReceived > peers.size() / 2) {
                becomeLeader();
            }
        }
    }

    public void receiveAppendEntries(AppendEntries msg) {
        if (msg.term < currentTerm) {
            return;
        }
        if (msg.term > currentTerm) {
            becomeFollower(msg.term);
        }
        resetElectionTimer();
        if (state != State.FOLLOWER) {
            becomeFollower(currentTerm);
        }
    }

    private void sendRequestVote() {
        for (RaftNode peer : peers) {
            peer.receiveRequestVote(new RequestVote(currentTerm, id));
        }
    }

    private void sendVoteResponse(int targetId, boolean voteGranted) {
        for (RaftNode peer : peers) {
            if (peer.id == targetId) {
                peer.receiveVoteResponse(new VoteResponse(currentTerm, voteGranted));
                break;
            }
        }
    }

    private void sendAppendEntries() {
        for (RaftNode peer : peers) {
            peer.receiveAppendEntries(new AppendEntries(currentTerm, id));
        }
    }

    private void becomeFollower(int term) {
        state = State.FOLLOWER;
        currentTerm = term;
        votedFor = null;
        resetElectionTimer();
        electionElapsed = 0;
    }

    public void becomeCandidate() {
        state = State.CANDIDATE;
        currentTerm++;
        votedFor = id;
        votesReceived = 1;
        electionElapsed = 0;
        sendRequestVote();
    }

    private void becomeLeader() {
        state = State.LEADER;
        sendAppendEntries();
    }

    private void resetElectionTimer() {
        electionTimeout = 5 + random.nextInt(6);
    }
}

class RequestVote {
    public final int term;
    public final int candidateId;
    public RequestVote(int term, int candidateId) {
        this.term = term;
        this.candidateId = candidateId;
    }
}

class VoteResponse {
    public final int term;
    public final boolean voteGranted;
    public VoteResponse(int term, boolean voteGranted) {
        this.term = term;
        this.voteGranted = voteGranted;
    }
}

class AppendEntries {
    public final int term;
    public final int leaderId;
    public AppendEntries(int term
);

}
