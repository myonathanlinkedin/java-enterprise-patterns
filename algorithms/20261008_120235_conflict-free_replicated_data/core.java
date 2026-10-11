import java.util.*;

public class PNCounter {
    private final String replicaId;
    private final Map<String, Long> pMap;
    private final Map<String, Long> nMap;

    public PNCounter(String replicaId) {
        this.replicaId = Objects.requireNonNull(replicaId);
        this.pMap = new HashMap<>();
        this.nMap = new HashMap<>();
    }

    public void increment(long delta) {
        if (delta <= 0) throw new IllegalArgumentException("delta must be positive");
        pMap.merge(replicaId, delta, Long::sum);
    }

    public void decrement(long delta) {
        if (delta <= 0) throw new IllegalArgumentException("delta must be positive");
        nMap.merge(replicaId, delta, Long::sum);
    }

    public long getValue() {
        long pSum = pMap.values().stream().mapToLong(Long::longValue).sum();
        long nSum = nMap.values().stream().mapToLong(Long::longValue).sum();
        return pSum - nSum;
    }

    public void merge(PNCounter other) {
        Objects.requireNonNull(other);
        for (Map.Entry<String, Long> e : other.pMap.entrySet()) {
            pMap.merge(e.getKey(), e.getValue(), Math::max);
        }
        for (Map.Entry<String, Long> e : other.nMap.entrySet()) {
            nMap.merge(e.getKey(), e.getValue(), Math::max);
        }
    }

    public Map<String, Long> getPMap() {
        return Collections.unmodifiableMap(pMap);
    }

    public Map<String, Long> getNMap() {
        return Collections.unmodifiableMap(nMap);
    }

    @Override
    public String toString() {
        return "PNCounter{" + "replicaId='" + replicaId + '\'' + ", value=" + getValue() + '}';
    }
}
