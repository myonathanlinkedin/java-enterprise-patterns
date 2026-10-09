import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * VectorClock implements a classic vector clock for distributed event ordering.
 * It is mutable but provides a defensive copy when needed.
 */
public class VectorClock {

    /** Internal representation: nodeId -> counter */
    private final Map<Integer, Long> clock;

    /** Relation between two vector clocks */
    public enum Relation {
        BEFORE, AFTER, CONCURRENT, EQUAL
    }

    /** Creates an empty vector clock. */
    public VectorClock() {
        this.clock = new HashMap<>();
    }

    /** Copy constructor (deep copy). */
    public VectorClock(VectorClock other) {
        this.clock = new HashMap<>(other.clock);
    }

    /**
     * Increments the counter for the given node identifier.
     *
     * @param nodeId identifier of the node performing the event
     */
    public void tick(int nodeId) {
        clock.merge(nodeId, 1L, Long::sum);
    }

    /**
     * Merges another vector clock into this one.
     * For each entry, the maximum counter is kept.
     *
     * @param other the vector clock to merge from
     */
    public void merge(VectorClock other) {
        for (Map.Entry<Integer, Long> e : other.clock.entrySet()) {
            clock.merge(e.getKey(), e.getValue(),
                    (v1, v2) -> Math.max(v1, v2));
        }
    }

    /**
     * Retrieves the counter for a given node.
     *
     * @param nodeId identifier of the node
     * @return counter value (0 if absent)
     */
    public long get(int nodeId) {
        return clock.getOrDefault(nodeId, 0L);
    }

    /**
     * Returns an unmodifiable view of the internal map.
     *
     * @return map of nodeId to counter
     */
    public Map<Integer, Long> snapshot() {
        return Collections.unmodifiableMap(new HashMap<>(clock));
    }

    /**
     * Determines the causal relation between this clock and another.
     *
     * @param other the vector clock to compare with
     * @return Relation enum describing the ordering
     */
    public Relation compare(VectorClock other) {
        boolean less = false;
        boolean greater = false;

        Set<Integer> allKeys = clock.keySet();
        allKeys.addAll(other.clock.keySet());

        for (Integer nodeId : allKeys) {
            long a = this.get(nodeId);
            long b = other.get(nodeId);
            if (a < b) {
                less = true;
            } else if (a > b) {
                greater = true;
            }
            if (less && greater) {
                return Relation.CONCURRENT;
            }
        }

        if (!less && !greater) {
            return Relation.EQUAL;
        } else if (less) {
            return Relation.BEFORE;
        } else {
            return Relation.AFTER;
        }
    }

    @Override
    public String toString() {
        return "VectorClock" + clock;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof VectorClock)) {
            return false;
        }
        VectorClock other = (VectorClock) obj;
        return this.clock.equals(other.clock);
    }

    @Override
    public int hashCode() {
        return clock.hashCode();
    }
}
