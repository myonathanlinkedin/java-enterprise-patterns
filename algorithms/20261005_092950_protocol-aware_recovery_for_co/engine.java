import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Core consensus node implementation (Raft‑like, protocol‑aware recovery).
 */
public final class ConsensusNode {
    private final NodeId nodeId;
    private Role role = Role.FOLLOWER;
    private long currentTerm = 0;
    private long commitIndex = 0;
    private long lastApplied = 0;
    private final List<LogEntry> log = new ArrayList<>();
    private StateMachine stateMachine;
    private Snapshot snapshot; // may be null

    public ConsensusNode(NodeId nodeId, StateMachine initialStateMachine) {
        this.nodeId = nodeId;
        this.stateMachine = initialStateMachine;
    }

    public NodeId id() {
        return nodeId;
    }

    public Role role() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public long currentTerm() {
        return currentTerm;
    }

    public void setCurrentTerm(long term) {
        this.currentTerm = term;
    }

    public long commitIndex() {
        return commitIndex;
    }

    public long lastApplied() {
        return lastApplied;
    }

    public Snapshot snapshot() {
        return snapshot;
    }

    public List<LogEntry> log() {
        return log;
    }

    /**
     * Append a new command to the log using the current term.
     */
    public LogEntry appendEntry(String command) {
        long nextIndex = getLastLogIndex() + 1;
        LogEntry entry = new LogEntry(currentTerm, nextIndex, command);
        log.add(entry);
        return entry;
    }

    /**
     * Advance the commit index (simulating leader commit).
     */
    public void advanceCommitIndex(long newCommitIndex) {
        if (newCommitIndex > getLastLogIndex()) {
            throw new IllegalArgumentException("Commit index cannot exceed last log index");
        }
        commitIndex = Math.max(commitIndex, newCommitIndex);
    }

    /**
     * Apply all entries up to commitIndex to the state machine.
     */
    public void applyCommittedEntries() {
        while (lastApplied < commitIndex) {
            LogEntry entry = log.get((int) lastApplied); // zero‑based list, index starts at 1
            stateMachine.apply(entry.command());
            lastApplied++;
        }
    }

    /**
     * Create a snapshot of the current state machine and truncate the log.
     */
    public void takeSnapshot() {
        if (!(stateMachine instanceof KeyValueStateMachine)) {
            throw new IllegalStateException("Snapshot only supported for KeyValueStateMachine");
        }
        Map<String, String> stateCopy = ((KeyValueStateMachine) stateMachine).getState();
        Snapshot snap = new Snapshot(lastApplied, currentTerm, stateCopy);
        this.snapshot = snap;

        // Truncate log entries up to and including lastApplied
        int truncateCount = (int) lastApplied;
        if (truncateCount > 0) {
            log.subList(0, truncateCount).clear();
        }
    }

    /**
     * Recover the node using its persisted snapshot and log.
     */
    public void recover() {
        RecoveryEngine.recover(this);
    }

    private long getLastLogIndex() {
        return log.isEmpty() ? 0 : log.get(log.size() - 1).index();
    }
}

/**
 * Recovery engine that restores a node from snapshot + log.
 */
public final class RecoveryEngine {
    /**
     * Rebuilds the state machine from snapshot (if any) and remaining log entries.
     */
    public static void recover(ConsensusNode node) {
        // Restore snapshot state if present
        if (node.snapshot() != null) {
            Snapshot snap = node.snapshot();
            KeyValueStateMachine kvsm = new KeyValueStateMachine();
            kvsm.restore(snap.state());
            node.stateMachine = kvsm;
            node.lastApplied = snap.lastIncludedIndex();
        } else {
            // No snapshot – start from empty state
            node.stateMachine = new KeyValueStateMachine();
            node.lastApplied = 0;
        }
}
}
