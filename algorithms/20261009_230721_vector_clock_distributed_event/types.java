package vectorclock;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable representation of a vector clock.
 * Internally stores a map from process identifier to counter.
 */
public final class VectorClock {
    private final Map<String, Integer> clock;

    /**
     * Creates an empty vector clock.
     */
    public VectorClock() {
        this.clock = Collections.emptyMap();
    }

    /**
     * Internal constructor used for cloning with a pre‑populated map.
     */
    private VectorClock(Map<String, Integer> map) {
        // Defensive copy and make unmodifiable
        this.clock = Collections.unmodifiableMap(new HashMap<>(map));
    }

    /**
     * Returns the counter for the given process identifier.
     * If the identifier is absent, returns 0.
     *
     * @param pid process identifier
     * @return counter value (non‑negative)
     */
    public int get(String pid) {
        return clock.getOrDefault(pid, 0);
    }

    /**
     * Returns an unmodifiable view of the underlying map.
     *
     * @return map from pid to counter
     */
    public Map<String, Integer> getClock() {
        return clock;
    }

    /**
     * Produces a new VectorClock where the counter for {@code pid}
     * is incremented by one. All other entries remain unchanged.
     *
     * @param pid process identifier to increment
     * @return new VectorClock with updated counter
     */
    public VectorClock increment(String pid) {
        Objects.requireNonNull(pid, "pid must not be null");
        Map<String, Integer> newMap = new HashMap<>(clock);
        int newValue = newMap.getOrDefault(pid, 0) + 1;
        newMap.put(pid, newValue);
        return new VectorClock(newMap);
    }

    /**
     * Merges this clock with another clock, producing a new clock
     * whose counters are the element‑wise maximum of the two inputs.
     *
     * @param other other vector clock
     * @return merged VectorClock
     */
    public VectorClock merge(VectorClock other) {
        Objects.requireNonNull(other, "other must not be null");
        Map<String, Integer> merged = new HashMap<>(clock);
        for (Map.Entry<String, Integer> e : other.clock.entrySet()) {
            merged.merge(e.getKey(), e.getValue(), Integer::max);
        }
        return new VectorClock(merged);
    }

    /**
     * Compares this clock with another clock according to the
     * partial order defined by vector clocks.
     *
     * @param other other vector clock
     * @return Relation describing the ordering
     */
    public Relation compare(VectorClock other) {
        Objects.requireNonNull(other, "other must not be null");
        boolean less = false;
        boolean greater = false;

        // Union of keys
        for (String pid : clock.keySet()) {
            int a = this.get(pid);
            int b = other.get(pid);
            if (a < b) less = true;
            if (a > b) greater = true;
        }
        for (String pid : other.clock.keySet()) {
            if (clock.containsKey(pid)) continue; // already examined
            int a = this.get(pid);
            int b = other.get(pid);
            if (a < b) less = true;
            if (a > b) greater = true;
        }

        if (!less && !greater) return Relation.EQUAL;
        if (less && !greater) return Relation.BEFORE;
        if (!less && greater) return Relation.AFTER;
        return Relation.CONCURRENT;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VectorClock)) return false;
        VectorClock that = (VectorClock) o;
        return this.clock.equals(that.clock);
    }

    @Override
    public int hashCode() {
        return clock.hashCode();
    }

    @Override
    public String toString() {
        return "VectorClock" + clock;
    }

    /**
     * Enumeration describing the partial order relationship between two vector clocks.
     */
    public enum Relation {
        BEFORE,      // this < other
        AFTER,       // this > other
        CONCURRENT,  // incomparable
        EQUAL        // identical
    }
}
