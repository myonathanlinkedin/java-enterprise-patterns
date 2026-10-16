import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class RaftNode {
    private final int id;
    private int currentTerm;
    private Integer votedFor; // null if not voted in current term
    private NodeState state;
    // For simplicity, log is omitted; we only keep dummy lastLogIndex/Term
    private final int lastLogIndex = 0;
    private final int lastLogTerm = 0;

    public RaftNode(int id) {
        this.id = id;
        this.currentTerm = 0;
        this.votedFor = null;
        this.state = NodeState.FOLLOWER;
    }

    public int getId() {
        return id;
    }

    public int getCurrentTerm() {
        return currentTerm;
    }

    public NodeState getState() {
        return state;
    }

    public void becomeFollower(int term) {
        if (term > currentTerm) {
            currentTerm = term;
            votedFor = null;
        }
        state = NodeState.FOLLOWER;
    }

    public void becomeCandidate() {
        state = NodeState.CANDIDATE;
        currentTerm += 1;
        votedFor = id; // vote for self
    }

    public void becomeLeader() {
        state = NodeState.LEADER;
        // In a full Raft implementation, leader would initialize nextIndex/ matchIndex, etc.
    }

    /** Process a RequestVote RPC from a candidate. */
    public RequestVoteResponse onRequestVote(RequestVote req) {
        if (req.term < currentTerm) {
            return new RequestVoteResponse(currentTerm, false);
        }

        // If term is higher, step down
        if (req.term > currentTerm) {
            becomeFollower(req.term);
        }

        boolean voteGranted = false;
        if ((votedFor == null || votedFor == req.candidateId) &&
            isCandidateLogAtLeastAsUpToDate(req.lastLogIndex, req.lastLogTerm)) {
            voteGranted = true;
            votedFor = req.candidateId;
        }

        return new RequestVoteResponse(currentTerm, voteGranted);
    }

    /** Process an AppendEntries RPC (heartbeat or log replication). */
    public AppendEntriesResponse onAppendEntries(AppendEntries req) {
        if (req.term < currentTerm) {
            return new AppendEntriesResponse(currentTerm, false);
        }

        // If term is higher, step down to follower
        if (req.term > currentTerm) {
            becomeFollower(req.term);
        } else {
            // term == currentTerm, ensure we are follower
            state = NodeState.FOLLOWER;
        }

        // In a full implementation we would check log consistency here.
        // For the election engine we treat any AppendEntries as successful.
        return new AppendEntriesResponse(currentTerm, true);
    }

    private boolean isCandidateLogAtLeastAsUpToDate(int candidateLastLogIndex, int candidateLastLogTerm) {
        // Simplified: all nodes have identical empty logs, so always true.
        return true;
    }

    /** Helper used by the engine to reset vote for a new term. */
    public void resetVoteForNewTerm(int term) {
        if (term > currentTerm) {
            currentTerm = term;
            votedFor = null;
            state = NodeState.FOLLOWER;
        }
    }

    @Override
    public String toString() {
        return "RaftNode{id=" + id + ", term=" + currentTerm + ", state=" + state + ", votedFor=" + votedFor + '}';
    }
}

public class RaftEngine {
    private final List<RaftNode> nodes;

    public RaftEngine(List<RaftNode> nodes) {
        this.nodes = new ArrayList<>(Objects.requireNonNull(nodes));
        if (nodes.isEmpty()) {
            throw new IllegalArgumentException("Cluster must contain at least one node");
        }
    }

    /** Returns the node with the given id, or null if not present. */
    public RaftNode getNode(int id) {
        for (RaftNode n : nodes) {
            if (n.getId() == id) {
                return n;
            }
        }
        return null;
    }

    /** Initiates an election on behalf of the node with the given id. */
    public void startElection(int candidateId) {
        RaftNode candidate = getNode(candidateId);
        if (candidate == null) {
            throw new IllegalArgumentException("Candidate id not found: " + candidateId);
        }

        // Transition to candidate, increment term, vote for self
        candidate.becomeCandidate();

        int term = candidate.getCurrentTerm();
        int votesGranted = 1; // self-vote

        // Broadcast RequestVote to all other nodes
        for (RaftNode peer : nodes) {
            if (peer.getId() == candidateId) {
                continue;
            }
            RequestVote req = new RequestVote(term, candidateId, 0, 0);
            RequestVoteResponse resp = peer.onRequestVote(req);
            if (resp.term > term) {
                // A higher term was observed; candidate steps down
                candidate.becomeFollower(resp.term);
                return;
            }
            if (resp.voteGranted) {
                votesGranted++;
            }
        }

        // Determine if majority achieved
        if (votesGranted > nodes.size() / 2) {
            candidate.becomeLeader();
            // Upon election, leader sends initial empty AppendEntries (heartbeat) to assert authority
            broadcastHeartbeat(candidate);
        } else {
            // No majority: revert to follower for the same term (Raft spec says stay candidate until timeout)
            candidate.becomeFollower(term);
        }
    }

    /** Leader sends a heartbeat (empty AppendEntries) to all followers. */
    public void broadcastHeartbeat(RaftNode leader) {
        if (leader.getState() != NodeState.LEADER) {
            throw new IllegalStateException("Only a leader can broadcast heartbeats");
        }
        int term = leader.getCurrentTerm();
        AppendEntries hb = new AppendEntries(term, leader.getId(), 0, 0, new int[0], 0);
        for (RaftNode peer : nodes) {
            if (peer.getId() == leader.getId()) {
                continue;
            }
            peer.onAppendEntries(hb);
        }
    }

    /** Utility to retrieve the current leader, or null if none. */
    public RaftNode getLeader() {
        for (RaftNode n : nodes) {
            if (n.getState() == NodeState.LEADER) {
                return n;
            }
        }
        return null;
    }

    /** Returns an immutable snapshot of the cluster nodes for inspection. */
    public List<RaftNode> snapshot() {
        return Collections.unmodifiableList(new ArrayList<>(nodes));
    }
}
