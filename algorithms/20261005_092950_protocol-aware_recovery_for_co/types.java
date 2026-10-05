import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * Role of a consensus node.
 */
public enum Role {
    LEADER,
    FOLLOWER,
    CANDIDATE
}

/**
 * Immutable log entry.
 */
public final class LogEntry {
    private final long term;
    private final long index;
    private final String command;

    public LogEntry(long term, long index, String command) {
        this.term = term;
        this.index = index;
        this.command = Objects.requireNonNull(command);
    }

    public long term() {
        return term;
    }

    public long index() {
        return index;
    }

    public String command() {
        return command;
    }
}

/**
 * Simple key/value state machine.
 */
public interface StateMachine {
    /**
     * Apply a command to the state machine.
     *
     * @param command command string, e.g. "SET key value" or "DELETE key"
     */
    void apply(String command);

    /**
     * @return a read‑only view of the current state.
     */
    Map<String, String> getState();
}

/**
 * In‑memory key/value implementation.
 */
public final class KeyValueStateMachine implements StateMachine {
    private final java.util.concurrent.ConcurrentHashMap<String, String> store = new java.util.concurrent.ConcurrentHashMap<>();

    @Override
    public void apply(String command) {
        String[] parts = command.trim().split("\\s+");
        if (parts.length == 0) {
            return;
        }
        String op = parts[0].toUpperCase();
        switch (op) {
            case "SET":
                if (parts.length != 3) {
                    throw new IllegalArgumentException("SET requires key and value");
                }
                store.put(parts[1], parts[2]);
                break;
            case "DELETE":
                if (parts.length != 2) {
                    throw new IllegalArgumentException("DELETE requires key");
                }
                store.remove(parts[1]);
                break;
            default:
                throw new IllegalArgumentException("Unsupported command: " + op);
        }
    }

    @Override
    public Map<String, String> getState() {
        return Collections.unmodifiableMap(store);
    }

    /**
     * Package‑private restoration used by the recovery engine.
     */
    void restore(Map<String, String> snapshotState) {
        store.clear();
        store.putAll(snapshotState);
    }
}

/**
 * Immutable snapshot of the state machine.
 */
public final class Snapshot {
    private final long lastIncludedIndex;
    private final long lastIncludedTerm;
    private final Map<String, String> state; // immutable copy

    public Snapshot(long lastIncludedIndex, long lastIncludedTerm, Map<String, String> state) {
        this.lastIncludedIndex = lastIncludedIndex;
        this.lastIncludedTerm = lastIncludedTerm;
        this.state = Collections.unmodifiableMap(state);
    }

    public long lastIncludedIndex() {
        return lastIncludedIndex;
    }

    public long lastIncludedTerm() {
        return lastIncludedTerm;
    }

    public Map<String, String> state() {
        return state;
    }
}

/**
 * Simple identifier for a node.
 */
public final class NodeId {
    private final int id;

    public NodeId(int id) {
        this.id = id;
    }

    public int id() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NodeId nodeId = (NodeId) o;
        return id == nodeId.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
    public String toString() {
        return "NodeId{" + "id=" + id + '}';
    }
}
