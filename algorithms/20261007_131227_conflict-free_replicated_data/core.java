package crdt;

import java.util.*;

public class PNCounter {
    private final String id;
    private final Map<String, Long> p;
    private final Map<String, Long> n;

    public PNCounter(String id) {
        this.id = Objects.requireNonNull(id);
        this.p = new HashMap<>();
        this.n = new HashMap<>();
    }

    public void increment() {
        p.merge(id, 1L, Long::sum);
    }

    public void decrement() {
        n.merge(id, 1L, Long::sum);
    }

    public long value() {
        long sumP = p.values().stream().mapToLong(Long::longValue).sum();
        long sumN = n.values().stream().mapToLong(Long::longValue).sum();
        return sumP - sumN;
    }

    public void merge(PNCounter other) {
        Objects.requireNonNull(other);
        mergeMaps(this.p, other.p);
        mergeMaps(this.n, other.n);
    }

    private void mergeMaps(Map<String, Long> local, Map<String, Long> other) {
        for (Map.Entry<String, Long> e : other.entrySet()) {
            local.merge(e.getKey(), e.getValue(), Math::max);
        }
    }

    @Override
    public String toString() {
        return "PNCounter{id='" + id + "', value=" + value() + '}';
    }
}
