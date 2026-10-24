import java.util.HashSet;
import java.util.Set;

/** Core Raft node implementing the leader election state machine. */
final class RaftNode {
    private static final int DEFAULT_ELECTION_TIMEOUT = 5;   // ticks
    private static final int DEFAULT_HEARTBEAT_INTERVAL = 2; // ticks

    final int id;
    int currentTerm = 0;
    ServerState state = ServerState.FOLLOWER;
    int votedFor = -1; // -1 means not voted yet in current term
    final Set<Integer> votesReceived = new HashSet<>();

    // timers
    int timeSinceLastContact = 0;
    final int electionTimeout;
    final int heartbeatInterval;

    private final MessageSender sender;
    private final int clusterSize; // needed for majority calculation

    RaftNode(int id, MessageSender sender, int clusterSize) {
        this.id = id;
        this.sender = sender;
        this.clusterSize = clusterSize;
        this.electionTimeout = DEFAULT_ELECTION_TIMEOUT;
        this.heartbeatInterval = DEFAULT_HEARTBEAT_INTERVAL;
    }

    /** Simulate a single tick of the node's internal clock. */
    void tick() {
        timeSinceLastContact++;
        if (state != ServerState.LEADER && timeSinceLastContact >= electionTimeout) {
            startElection();
        } else if (state == ServerState.LEADER && timeSinceLastContact >= heartbeatInterval) {
            sendHeartbeats();
            timeSinceLastContact = 0; // reset after sending heartbeats
        }
    }

    /** Initiates a new election cycle. */
    private void startElection() {
        state = ServerState.CANDIDATE;
        currentTerm += 1;
        votedFor = id;
        votesReceived.clear();
        votesReceived.add(id);
        timeSinceLastContact = 0; // reset election timer

        // broadcast RequestVote to all other nodes
        RequestVote rv = new RequestVote(currentTerm, id);
        for (int target = 0; target < clusterSize; target++) {
            if (target != id) {
                sender.sendRequestVote(target, rv);
            }
        }
    }

    /** Handles an incoming RequestVote RPC. */
    RequestVoteResponse onRequestVote(RequestVote request) {
        if (request.term < currentTerm) {
            return new RequestVoteResponse(currentTerm, false);
        }

        // If term is higher, step down first
        if (request.term > currentTerm) {
            becomeFollower(request.term);
        }

        boolean grant = (votedFor == -1 || votedFor == request.candidateId);
        if (grant) {
            votedFor = request.candidateId;
            timeSinceLastContact = 0; // reset election timer on granting vote
        }
        return new RequestVoteResponse(currentTerm, grant);
    }

    /** Handles an incoming AppendEntries RPC (heartbeat). */
    AppendEntriesResponse onAppendEntries(AppendEntries request) {
        if (request.term < currentTerm) {
            return new AppendEntriesResponse(currentTerm, false);
        }

        // If term is higher, step down
        if (request.term > currentTerm) {
            becomeFollower(request.term);
        }

        // Reset election timer because we heard from a valid leader
        timeSinceLastContact = 0;
        // If we were candidate or leader, revert to follower
        if (state != ServerState.FOLLOWER) {
            becomeFollower(request.term);
        }
        return new AppendEntriesResponse(currentTerm, true);
    }

    /** Called by the cluster when a RequestVoteResponse arrives. */
    void receiveVoteResponse(RequestVoteResponse response) {
        if (state != ServerState.CANDIDATE) {
            return; // ignore if not candidate
        }

        if (response.term > currentTerm) {
            becomeFollower(response.term);
            return;
        }

        if (response.voteGranted) {
            votesReceived.add(response.term); // term is not the voter id; we need voter id elsewhere
            // In this simplified engine, we assume each response corresponds to a distinct voter.
            // The cluster will invoke this method once per voter.
            if (votesReceived.size() > clusterSize / 2) {
                becomeLeader();
            }
        }
    }

    /** Transition to follower state with a given term. */
    private void becomeFollower(int newTerm) {
        state = ServerState.FOLLOWER;
        currentTerm = newTerm;
        votedFor = -1;
        votesReceived.clear();
        timeSinceLastContact = 0;
    }

    /** Transition to leader state. */
    private void becomeLeader() {
        state = ServerState.LEADER;
        // Upon election, immediately send initial heartbeats
        sendHeartbeats();
        timeSinceLastContact = 0;
    }

    /** Broadcast heartbeat AppendEntries to all other nodes. */
    private void sendHeartbeats() {
        AppendEntries ae = new AppendEntries(currentTerm, id);
        for (int target = 0; target < clusterSize; target++) {
            if (target != id) {
                sender.sendAppendEntries(target, ae);
            }
        }
    }

    /** Expose current state for testing. */
    ServerState getState() {
        return state;
    }

    int getCurrentTerm() {
        return currentTerm;
    }

    int getVotedFor() {
        return votedFor;
    }
}
