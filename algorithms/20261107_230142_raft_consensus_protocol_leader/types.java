import java.util.*;

enum NodeState {
    FOLLOWER,
    CANDIDATE,
    LEADER
}

class RequestVote {
    final int term;
    final int candidateId;

    RequestVote(int term, int candidateId) {
        this.term = term;
        this.candidateId = candidateId;
    }
}

class VoteResponse {
    final int term;
    final boolean voteGranted;

    VoteResponse(int term, boolean voteGranted) {
        this.term = term;
        this.voteGranted = voteGranted;
    }
}

class Node {
    final int id;
    NodeState state;
    int currentTerm;
    Integer votedFor; // null if none
    int votesReceived; // only relevant for CANDIDATE

    Node(int id) {
        this.id = id;
        this.state = NodeState.FOLLOWER;
        this.currentTerm = 0;
        this.votedFor = null;
        this.votesReceived = 0;
    }

    synchronized boolean receiveRequestVote(RequestVote rv) {
        if (rv.term < currentTerm) {
            return false;
        }
        if (rv.term > currentTerm) {
            currentTerm = rv.term;
            state = NodeState.FOLLOWER;
            votedFor = null;
        }
        if (votedFor == null || votedFor == rv.candidateId) {
            votedFor = rv.candidateId;
            return true;
        }
        return false;
    }

    synchronized void receiveVoteResponse(VoteResponse vr) {
        if (vr.term > currentTerm) {
            currentTerm = vr.term;
            state = NodeState.FOLLOWER;
            votedFor = null;
            votesReceived = 0;
            return;
        }
        if (state != NodeState.CANDIDATE || vr.term != currentTerm) {
            return;
        }
        if (vr.voteGranted) {
            votesReceived++;
        }
    }
}
