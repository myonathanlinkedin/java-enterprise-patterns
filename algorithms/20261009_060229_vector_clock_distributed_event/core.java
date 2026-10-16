import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * VectorClock implements a classic vector clock for distributed event ordering.
 * It is mutable but all public methods are thread‑safe for single‑threaded use
 * (the class is not synchronized; external synchronization is required for
 * concurrent access).
 */
public class VectorClock {
    /** Internal representation: process identifier → counter */
    private final Map<String, Integer> clock;

    /** Relation between two vector clocks */
    public enum Relation {
        /** Both clocks are identical */
        EQUAL,
        /** This clock happened before the other */
        BEFORE,
        /** This clock happened after the other */
        AFTER,
        /** Neither happened before the other (concurrent) */
        CONCURRENT
    }

    /** Creates an empty vector clock. */
    public VectorClock() {
        this.clock = new HashMap<>();
    }

    /** Creates a copy of another vector clock. */
    public VectorClock(VectorClock other) {
        this.clock = new HashMap<>(other.clock);
    }

    /** Returns an unmodifiable view of the internal map (read‑only). */
    public Map<String, Integer> snapshot() {
        return Collections.unmodifiableMap(new HashMap<>(clock));
    }

    /** Increments the counter for the given process identifier. */
    public void increment(String pid) {
        Objects.requireNonNull(pid, "pid must not be null");
        clock.merge(pid, 1, Integer::sum);
    }

    /**
     * Merges another vector clock into this one.
     * For each identifier, the resulting counter is the maximum of the two.
     */
    public void merge(VectorClock other) {
        Objects.requireNonNull(other, "other must not be null");
        for (Map.Entry<String, Integer> e : other.clock.entrySet()) {
            clock.merge(e.getKey(), e.getValue(),
                (v1, v2) -> Math.max(v1, v2));
        }
    }

    /**
     * Determines the causal relation between this clock and another.
     *
     * @return Relation enum describing the ordering.
     */
    public Relation compare(VectorClock other) {
        Objects.requireNonNull(other, "other must not be null");
        boolean less = false;
        boolean greater = false;

        // Union of keys from both clocks
        for (String pid : unionKeys(this.clock, other.clock)) {
            int a = this.clock.getOrDefault(pid, 0);
            int b = other.clock.getOrDefault(pid, 0);
            if (a < b) less = true;
            if (a > b) greater = true;
            if (less && greater) return Relation.CONCURRENT;
        }

        if (!less && !greater) return Relation.EQUAL;
        return less ? Relation.BEFORE : Relation.AFTER;
    }

    /** Helper to compute the union of key sets. */
    private static java.util.Set<String> unionKeys(Map<String, Integer> a,
                                                   Map<String, Integer> b) {
        java.util.Set<String> set = new java.util.HashSet<>(a.keySet());
        set.addAll(b.keySet());
        return set;
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
        return Objects.hash(clock);
    }

    @Override
    public String toString() {
        return "VectorClock" + clock;
    }
}
