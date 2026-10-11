package crdt;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A state‑based PN‑Counter CRDT.
 *
 * Each replica maintains two grow‑only counters:
 *   - P (positive) records increments per replica.
 *   - N (negative) records decrements per replica.
 *
 * The observable value is ΣP – ΣN.
 * Merging two counters takes the element‑wise maximum of the two internal maps.
 *
 * This implementation is pure, deterministic and uses only the Java standard library.
 */
public final class PNCounter {
    private final String replicaId;
    private final Map<String, Long> p; // increments per replica
    private final Map<String, Long> n; // decrements per replica

    /**
     * Creates a new PN‑Counter for the given replica identifier.
     *
     * @param replicaId unique identifier for this replica (must be non‑null)
     */
    public PNCounter(String replicaId) {
        this.replicaId = Objects.requireNonNull(replicaId, "replicaId");
        this.p = new HashMap<>();
        this.n = new HashMap<>();
        // initialise own entries to zero to simplify merge logic
        this.p.put(replicaId, 0L);
        this.n.put(replicaId, 0L);
    }

    private PNCounter(String replicaId, Map<String, Long> p, Map<String, Long> n) {
        this.replicaId = replicaId;
        this.p = p;
        this.n = n;
    }

    /** Returns the identifier of this replica. */
    public String getReplicaId() {
        return replicaId;
    }

    /** Returns an unmodifiable view of the positive map. */
    public Map<String, Long> getP() {
        return Collections.unmodifiableMap(p);
    }

    /** Returns an unmodifiable view of the negative map. */
    public Map<String, Long> getN() {
        return Collections.unmodifiableMap(n);
    }

    /**
     * Increments the counter by {@code delta} (must be non‑negative).
     *
     * @param delta amount to add, must be ≥ 0
     */
    public void increment(long delta) {
        if (delta < 0) {
            throw new IllegalArgumentException("increment delta must be non‑negative");
        }
        p.merge(replicaId, delta, Long::sum);
    }

    /**
     * Decrements the counter by {@code delta} (must be non‑negative).
     *
     * @param delta amount to subtract, must be ≥ 0
     */
    public void decrement(long delta) {
        if (delta < 0) {
            throw new IllegalArgumentException("decrement delta must be non‑negative");
        }
        n.merge(replicaId, delta, Long::sum);
    }

    /** Returns the current logical value ΣP – ΣN. */
    public long value() {
        long sumP = p.values().stream().mapToLong(Long::longValue).sum();
        long sumN = n.values().stream().mapToLong(Long::longValue).sum();
        return sumP - sumN;
    }

    /**
     * Merges the state of {@code other} into this counter.
     * The operation is commutative, associative and idempotent.
     *
     * @param other another PN‑Counter (must have the same replica identifier space)
     */
    public void merge(PNCounter other) {
        // Merge positive map
        for (Map.Entry<String, Long> e : other.p.entrySet()) {
            p.merge(e.getKey(), e.getValue(), Math::max);
        }
        // Merge negative map
        for (Map.Entry<String, Long> e : other.n.entrySet()) {
            n.merge(e.getKey(), e.getValue(), Math::max);
        }
    }

    /** Returns a deep copy of this counter. */
    public PNCounter copy() {
        return new PNCounter(replicaId,
                new HashMap<>(p),
                new HashMap<>(n));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PNCounter)) return false;
        PNCounter that = (PNCounter) o;
        return replicaId.equals(that.replicaId) &&
                p.equals(that.p) &&
                n.equals(that.n);
    }

    @Override
    public int hashCode() {
        return Objects.hash(replicaId, p, n);
    }

    @Override
    public String toString() {
        return "PNCounter{" +
                "replicaId='" + replicaId + '\'' +
                ", value=" + value() +
                ", p=" + p +
                ", n=" + n +
                '}';
    }
}
