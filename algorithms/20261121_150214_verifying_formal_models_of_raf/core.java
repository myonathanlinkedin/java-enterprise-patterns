import java.util.*;

public class LogEntry {
    public final int term;
    public final String command;

    public LogEntry(int term, String command) {
        this.term = term;
        this.command = command;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof LogEntry)) return false;
        LogEntry other = (LogEntry) o;
        return term == other.term && Objects.equals(command, other.command);
    }

    @Override
    public int hashCode() {
        return Objects.hash(term, command);
    }
}

enum NodeState {
    FOLLOWER, CANDIDATE, LEADER
}

abstract class Message {
    public final int term;
    public final int fromId;
    public final int toId;

    protected Message(int term, int fromId, int toId) {
        this.term = term;
        this.fromId = fromId;
        this.toId = toId;
    }
}

class RequestVote extends Message {
    public final int lastLogIndex;
    public final int lastLogTerm;

    public RequestVote(int term, int fromId, int toId, int lastLogIndex, int lastLogTerm) {
        super(term, fromId, toId);
        this.lastLogIndex = lastLogIndex;
        this.lastLogTerm = lastLogTerm;
    }
}

class RequestVoteResponse extends Message {
    public final boolean voteGranted;

    public RequestVoteResponse(int term, int fromId, int toId, boolean voteGranted) {
        super(term, fromId, toId);
        this.voteGranted = voteGranted;
    }
}

class AppendEntries extends Message {
    public final int prevLogIndex;
    public final int prevLogTerm;
    public final List<LogEntry> entries;
    public final int leaderCommit;

    public AppendEntries(int term, int fromId, int toId,
                         int prevLogIndex, int prevLogTerm,
                         List<LogEntry> entries, int leaderCommit) {
        super(term, fromId, toId);
        this.prevLogIndex = prevLogIndex;
        this.prevLogTerm = prevLogTerm;
        this.entries = entries;
        this.leaderCommit = leaderCommit;
    }
}

class AppendEntriesResponse extends Message {
    public final boolean success;
    public final int matchIndex; // index of last entry matched

    public AppendEntriesResponse(int term, int fromId, int toId,
                                 boolean success, int matchIndex) {
        super(term, fromId, toId);
        this.success = success;
        this.matchIndex = matchIndex;
    }
}

class RaftNode {
    public final int id;
    public int currentTerm = 0;
    public Integer votedFor = null;
    public final List<LogEntry> log = new ArrayList<>();
    public int commitIndex = 0;
    public int lastApplied = 0;
    public NodeState state = NodeState.FOLLOWER;
    private final Set<Integer> votesReceived = new HashSet<>();

    public RaftNode(int id) {
        this.id = id;
    }

    // Helper to get last log index/term
    private int lastLogIndex() {
        return log.size();
    }

    private int lastLogTerm() {
        return log.isEmpty() ? 0 : log.get(log.size() - 1).term;
    }

    // Called by external driver to start election
    public void startElection(RaftCluster cluster) {
        state = NodeState.CANDIDATE;
        currentTerm += 1;
        votedFor = id;
        votesReceived.clear();
        votesReceived.add(id);
        for (RaftNode peer : cluster.getAllNodes()) {
            if (peer.id == this.id) continue;
            RequestVote rv = new RequestVote(currentTerm, this.id, peer.id,
                    lastLogIndex(), lastLogTerm());
            cluster.sendMessage(rv);
        }
    }

    // Process incoming messages
    public void receive(Message msg, RaftCluster cluster) {
        if (msg.term > currentTerm) {
            currentTerm = msg.term;
            state = NodeState.FOLLOWER;
            votedFor = null;
        }
        switch (msg.getClass().getSimpleName()) {
            case "RequestVote":
                handleRequestVote((RequestVote) msg, cluster);
                break;
            case "RequestVoteResponse":
                handleRequestVoteResponse((RequestVoteResponse) msg, cluster);
                break;
            case "AppendEntries":
                handleAppendEntries((AppendEntries) msg, cluster);
                break;
            case "AppendEntriesResponse":
                handleAppendEntriesResponse((AppendEntriesResponse) msg, cluster);
                break;
            default:
                // ignore unknown
        }
    }

    private void handleRequestVote(RequestVote rv, RaftCluster cluster) {
        boolean voteGranted = false;
        if (rv.term < currentTerm) {
            voteGranted = false;
        } else {
            boolean notVoted = (votedFor == null || votedFor == rv.fromId);
            boolean upToDate = (rv.lastLogTerm > lastLogTerm()) ||
                    (rv.lastLogTerm == lastLogTerm() && rv.lastLogIndex >= lastLogIndex());
            if (notVoted && upToDate) {
                votedFor = rv.fromId;
                voteGranted = true;
            }
        }
        RequestVoteResponse resp = new RequestVoteResponse(currentTerm, this.id, rv.fromId, voteGranted);
        cluster.sendMessage(resp);
    }

    private void handleRequestVoteResponse(RequestVoteResponse rvr, RaftCluster cluster) {
        if (state != NodeState.CANDIDATE) return;
        if (rvr.term > currentTerm) {
            currentTerm = rvr.term;
            state = NodeState.FOLLOWER;
            votedFor = null;
            return;
        }
        if (rvr.voteGranted) {
            votesReceived.add(rvr.fromId);
            int majority = (cluster.size() / 2) + 1;
            if (votesReceived.size() >= majority) {
                becomeLeader(cluster);
            }
        }
    }

    private void becomeLeader(RaftCluster cluster) {
        state = NodeState.LEADER;
        // Initialize nextIndex & matchIndex implicitly via AppendEntries
        // Immediately send empty AppendEntries as heartbeat
        broadcastAppendEntries(cluster, Collections.emptyList());
    }

    private void broadcastAppendEntries(RaftCluster cluster, List<LogEntry> newEntries) {
        for (RaftNode peer : cluster.getAllNodes()) {
            if (peer.id == this.id) continue;
            int prevIdx = peer.lastLogIndex();
            int prevTerm = prevIdx == 0 ? 0 : peer.log.get(prevIdx - 1).term;
            AppendEntries ae = new AppendEntries(currentTerm, this.id, peer.id,
                    prevIdx, prevTerm, newEntries, commitIndex);
            cluster.sendMessage(ae);
        }
    }

    private void handleAppendEntries(AppendEntries ae, RaftCluster cluster) {
        if (ae.term < currentTerm) {
            AppendEntriesResponse resp = new AppendEntriesResponse(currentTerm, this.id, ae.fromId,
                    false, 0);
            cluster.sendMessage(resp);
            return;
        }
        // term is at least currentTerm (already updated above)
        state = NodeState.FOLLOWER;
        // consistency check
        if (ae.prevLogIndex > log.size()) {
            AppendEntriesResponse resp = new AppendEntriesResponse(currentTerm, this.id, ae.fromId,
                    false, log.size());
            cluster.sendMessage(resp);
            return;
        }
        if (ae.prevLogIndex > 0) {
            int termAtPrev = log.get(ae.prevLogIndex - 1).term;
            if (termAtPrev != ae.prevLogTerm) {
                AppendEntriesResponse resp = new AppendEntriesResponse(currentTerm, this.id, ae.fromId,
                        false, ae.prevLogIndex - 1);
                cluster.sendMessage(resp);
                return;
            }
        }
        // Append any new entries
        int idx = ae.prevLogIndex;
        for (LogEntry entry : ae.entries) {
            if (idx < log.size()) {
                if (log.get(idx).term != entry.term) {
                    // delete conflict entry and all that follow
                    while (log.size() > idx) log.remove(log.size() - 1);
                    log.add(entry);
                }
            } else {
                log.add(entry);
            }
            idx++;
        }
        // Update commit index
        if (ae.leaderCommit > commitIndex) {
            commitIndex = Math.min(ae.leaderCommit, log.size());
        }
        AppendEntriesResponse resp = new AppendEntriesResponse(currentTerm, this.id, ae.fromId,
                true, idx);
        cluster.sendMessage(resp);
    }

    private void handleAppendEntriesResponse(AppendEntriesResponse aer, RaftCluster cluster) {
        // For this minimal implementation we ignore matchIndex tracking.
        // In a full Raft, leader would advance commitIndex based on majority matchIndex.
    }

    // Public API for tests to propose a command (only leader may call)
    public void propose(String command, RaftCluster cluster) {
        if (state != NodeState.LEADER) {
            throw new IllegalStateException("Only leader can propose");
        }
        LogEntry entry = new LogEntry(currentTerm, command);
        log.add(entry);
        broadcastAppendEntries(cluster, Collections.singletonList(entry));
    }
}

class RaftCluster {
    private final Map<Integer, RaftNode> nodes = new HashMap<>();

    public RaftCluster(int... ids) {
        for (int id : ids) nodes.put(id, new RaftNode(id));
    }

    public Collection<RaftNode> getAllNodes() {
        return nodes.values();
    }

    public int size() {
        return nodes.size();
    }

    public RaftNode getNode(int id) {
        return nodes.get(id);
    }

    // Synchronous delivery: message is processed immediately
    public void sendMessage(Message msg) {
        RaftNode receiver = nodes.get(msg.toId);
        if (receiver != null) {
            receiver.receive(msg, this);
        }
    }

    // Helper to trigger election from a specific node
    public void triggerElection(int nodeId) {
        RaftNode n = nodes.get(nodeId);
        if (n != null) n.startElection(this);
    }

    // Helper to propose a command via current leader
    public void propose(int leaderId, String command) {
        RaftNode leader = nodes.get(leaderId);
        if (leader != null) leader.propose(command, this);
    }
}
