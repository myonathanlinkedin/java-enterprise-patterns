import java.util.*;

public class CountMinSketch {
    private final int depth;
    private final int width;
    private final long[][] counters;
    private final long seed1;
    private final long seed2;
    private long totalCount;

    public CountMinSketch(int depth, int width, long seed) {
        if (depth <= 0 || width <= 0) {
            throw new IllegalArgumentException("Depth and width must be positive");
        }
        this.depth = depth;
        this.width = width;
        this.counters = new long[depth][width];
        this.seed1 = seed;
        this.seed2 = seed ^ 0x5bd1e995L;
        this.totalCount = 0;
    }

    public void add(Object item, long count) {
        if (count < 0) {
            throw new IllegalArgumentException("Count must be non-negative");
        }
        if (count == 0) {
            return;
        }
        int hash = item.hashCode();
        long h1 = mix(hash, seed1);
        long h2 = mix(hash, seed2);
        for (int i = 0; i < depth; i++) {
            int idx = (int) ((h1 + i * h2) % width);
            if (idx < 0) idx += width;
            counters[i][idx] += count;
        }
        totalCount += count;
    }

    public long estimate(Object item) {
        int hash = item.hashCode();
        long h1 = mix(hash, seed1);
        long h2 = mix(hash, seed2);
        long min = Long.MAX_VALUE;
        for (int i = 0; i < depth; i++) {
            int idx = (int) ((h1 + i * h2) % width);
            if (idx < 0) idx += width;
            long val = counters[i][idx];
            if (val < min) min = val;
        }
        return min;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public List<Object> getHeavyHitters(double threshold, Iterable<Object> candidates) {
        if (threshold < 0 || threshold > 1) {
            throw new IllegalArgumentException("Threshold must be between 0 and 1");
        }
        List<Object> heavy = new ArrayList<>();
        long minCount = (long) Math.ceil(threshold * totalCount);
        for (Object item : candidates) {
            if (estimate(item) >= minCount) {
                heavy.add(item);
            }
        }
        return heavy;
    }

    private static long mix(int h, long seed) {
        long x = h ^ seed;
        x ^= (x >>> 20) ^ (x >>> 12);
        return x ^ (x >>> 7) ^ (x >>> 4);
    }
}
