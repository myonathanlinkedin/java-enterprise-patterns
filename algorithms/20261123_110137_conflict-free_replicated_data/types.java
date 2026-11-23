import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents the state of a single node in a distributed system.
 * A node is identified by a unique string ID.
 */
public final class NodeId {
    private final String id;

    public NodeId(String id) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Node ID cannot be null or empty");
        }
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodeId nodeId = (NodeId) o;
        return id.equals(nodeId.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return id;
    }
}

/**
 * Represents the immutable state of a PN-Counter for a specific node.
 * A PN-Counter consists of two G-Counters:
 * - P (Positive) counter: tracks increments
 * - N (Negative) counter: tracks decrements
 * 
 * The value contributed by this node is P - N.
 */
public final class NodeState {
    private final NodeId nodeId;
    private final long pValue;
    private final long nValue;

    public NodeState(NodeId nodeId, long pValue, long nValue) {
        if (nodeId == null) {
            throw new IllegalArgumentException("NodeId cannot be null");
        }
        if (pValue < 0) {
            throw new IllegalArgumentException("P value cannot be negative");
        }
        if (nValue < 0) {
            throw new IllegalArgumentException("N value cannot be negative");
        }
        this.nodeId = nodeId;
        this.pValue = pValue;
        this.nValue = nValue;
    }

    public NodeId getNodeId() {
        return nodeId;
    }

    public long getPValue() {
        return pValue;
    }

    public long getNValue() {
        return nValue;
    }

    /**
     * Returns the net contribution of this node to the counter.
     */
    public long getValue() {
        return pValue - nValue;
    }

    /**
     * Creates a new NodeState with an incremented P value.
     */
    public NodeState incrementP(long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Increment amount must be positive");
        }
        return new NodeState(nodeId, pValue + amount, nValue);
    }

    /**
     * Creates a new NodeState with an incremented N value.
     */
    public NodeState incrementN(long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Increment amount must be positive");
        }
        return new NodeState(nodeId, pValue, nValue + amount);
    }

    /**
     * Merges this state with another state from the same node.
     * The merge operation takes the maximum of P and N values,
     * which is the join operation for G-Counters.
     */
    public NodeState merge(NodeState other) {
        if (!this.nodeId.equals(other.nodeId)) {
            throw new IllegalArgumentException("Cannot merge states from different nodes");
        }
        long mergedP = Math.max(this.pValue, other.pValue);
        long mergedN = Math.max(this.nValue, other.nValue);
        return new NodeState(this.nodeId, mergedP, mergedN);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodeState nodeState = (NodeState) o;
        return pValue == nodeState.pValue &&
               nValue == nodeState.nValue &&
               nodeId.equals(nodeState.nodeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeId, pValue, nValue);
    }

    @Override
    public String toString() {
        return "NodeState{" +
               "nodeId=" + nodeId +
               ", pValue=" + pValue +
               ", nValue=" + nValue +
               '}';
    }
}

/**
 * Represents the complete state of a PN-Counter.
 * A PN-Counter is a map from NodeId to NodeState.
 * The total value of the counter is the sum of all node contributions.
 */
public final class PNCounterState {
    private final Map<NodeId, NodeState> states;

    public PNCounterState() {
        this.states = new HashMap<>();
    }

    public PNCounterState(Map<NodeId, NodeState> states) {
        if (states == null) {
            throw new IllegalArgumentException("States map cannot be null");
        }
        this.states = new HashMap<>(states);
    }

    /**
     * Returns an unmodifiable view of the node states.
     */
    public Map<NodeId, NodeState> getStates() {
        return new HashMap<>(states);
    }

    /**
     * Returns the NodeState for a specific node, or null if not present.
     */
    public NodeState getNodeState(NodeId nodeId) {
        return states.get(nodeId);
    }

    /**
     * Returns the total value of the PN-Counter.
     * This is the sum of (P - N) for all nodes.
     */
    public long getValue() {
        long total = 0;
        for (NodeState state : states.values()) {
            total += state.getValue();
        }
        return total;
    }

    /**
     * Returns the number of nodes that have contributed to this counter.
     */
    public int getNodeCount() {
        return states.size();
    }

    /**
     * Creates a new PNCounterState with an increment applied to a specific node.
     */
    public PNCounterState increment(NodeId nodeId, long amount) {
        if (nodeId == null) {
            throw new IllegalArgumentException("NodeId cannot be null");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Increment amount must be positive");
        }
        Map<NodeId, NodeState> newStates = new HashMap<>(states);
        NodeState currentState = newStates.get(nodeId);
        if (currentState == null) {
            newStates.put(nodeId, new NodeState(nodeId, amount, 0));
        } else {
            newStates.put(nodeId, currentState.incrementP(amount));
        }
        return new PNCounterState(newStates);
    }

    /**
     * Creates a new PNCounterState with a decrement applied to a specific node.
     */
    public PNCounterState decrement(NodeId nodeId, long amount) {
        if (nodeId == null) {
            throw new IllegalArgumentException("NodeId cannot be null");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Decrement amount must be positive");
        }
        Map<NodeId, NodeState> newStates = new HashMap<>(states);
        NodeState currentState = newStates.get(nodeId);
        if (currentState == null) {
            newStates.put(nodeId, new NodeState(nodeId, 0, amount));
        } else {
            newStates.put(nodeId, currentState.incrementN(amount));
        }
        return new PNCounterState(newStates);
    }

    /**
     * Merges this state with another PNCounterState.
     * The merge operation is commutative, associative, and idempotent.
     * For each node, the merged state is the join (max) of the individual states.
     */
    public PNCounterState merge(PNCounterState other) {
        if (other == null) {
            throw new IllegalArgumentException("Other state cannot be null");
        }
        Map<NodeId, NodeState> mergedStates = new HashMap<>(this.states);
        for (Map.Entry<NodeId, NodeState> entry : other.states.entrySet()) {
            NodeId nodeId = entry.getKey();
            NodeState otherState = entry.getValue();
            NodeState thisState = mergedStates.get(nodeId);
            if (thisState == null) {
                mergedStates.put(nodeId, otherState);
            } else {
                mergedStates.put(nodeId, thisState.merge(otherState));
            }
        }
        return new PNCounterState(mergedStates);
    }

    /**
     * Checks if this state is equal to another state.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PNCounterState that = (PNCounterState) o;
        return states.equals(that.states);
    }

    @Override
    public int hashCode() {
        return Objects.hash(states);
    }

    @Override
    public String toString() {
        return "PNCounterState{" +
               "value=" + getValue() +
               ", nodes=" + states.size() +
               '}';
    }
}
