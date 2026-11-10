package core;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

/**
 * Count-Min Sketch implementation with pairwise independent hash functions.
 * Provides add and estimate operations.
 */
public class CountMinSketch {
    private final int depth;
    private final int width;
    private final long[][] table;
    private final long[] hashA;
    private final long[] hashB;
    private final long PRIME = 4294967311L; // large prime > 2^32
    private long totalCount = 0L;

    /**
     * Constructs a Count-Min Sketch.
     *
     * @param depth number of hash rows (error probability)
     * @param width number of counters per row (inverse error)
     * @param seed  base seed for hash function generation
     */
    public CountMinSketch(int depth, int width, long seed) {
        if (depth <= 0 || width <= 0) {
            throw new IllegalArgumentException("Depth and width must be positive");
        }
        this.depth = depth;
        this.width = width;
        this.table = new long[depth][width];
        this.hashA = new long[depth];
        this.hashB = new long[depth];
        java.util.Random rng = new java.util.Random(seed);
        for (int i = 0; i < depth; i++) {
            // a must be non‑zero, b can be any value modulo PRIME
            hashA[i] = rng.nextLong() % (PRIME - 1) + 1;
            hashB[i] = rng.nextLong() % PRIME;
            if (hashA[i] < 0) hashA[i] += PRIME;
            if (hashB[i] < 0) hashB[i] += PRIME;
        }
    }

    private int hash(int row, Object item) {
        int x = item == null ? 0 : item.hashCode();
        long a = hashA[row];
        long b = hashB[row];
        long hash = (a * (x & 0xffffffffL) + b) % PRIME;
        return (int) (hash % width);
    }

    /**
     * Adds {@code count} occurrences of {@code item} to the sketch.
     *
     * @param item  element to add
     * @param count positive increment
     */
    public void add(Object item, long count) {
        if (count < 0) {
            throw new IllegalArgumentException("Count must be non‑negative");
        }
        for (int i = 0; i < depth; i++) {
            int col = hash(i, item);
            table[i][col] += count;
        }
        totalCount += count;
    }

    /**
     * Estimates the frequency of {@code item}.
     *
     * @param item element to query
     * @return upper‑bound estimate of the true frequency
     */
    public long estimate(Object item) {
        long min = Long.MAX_VALUE;
        for (int i = 0; i < depth; i++) {
            int col = hash(i, item);
            long val = table[i][col];
            if (val < min) {
                min = val;
            }
        }
        return min;
    }

    /**
     * Returns the total number of items added to the sketch.
     *
     * @return total count
     */
    public long getTotalCount() {
        return totalCount;
    }
}

/**
 * Heavy‑hitters detector built on top of Count‑Min Sketch.
 * Maintains a bounded set of candidate items.
 */
public class HeavyHitters {
    private final CountMinSketch sketch;
    private final int capacity;
    private final Map<Object, Long> candidates;

    /**
     * Constructs a HeavyHitters detector.
     *
     * @param depth    sketch depth
     * @param width    sketch width
     * @param capacity maximum number of candidates to retain
     * @param seed     random seed for hash functions
     */
    public HeavyHitters(int depth, int width, int capacity, long seed) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive");
        }
        this.sketch = new CountMinSketch(depth, width, seed);
        this.capacity = capacity;
        this.candidates = new HashMap<>(capacity);
    }

    /**
     * Adds {@code count} occurrences of {@code item}.
     *
     * @param item  element to add
     * @param count positive increment
     */
    public void add(Object item, long count) {
        sketch.add(item, count);
        Long cur = candidates.get(item);
        if (cur != null) {
            candidates.put(item, cur + count);
            return;
        }
        if (candidates.size() < capacity) {
            candidates.put(item, count);
            return;
        }
        // Find candidate with smallest estimated count
        Object minKey = null;
        long minEst = Long.MAX_VALUE;
        for (Map.Entry<Object, Long> e : candidates.entrySet()) {
            long est = sketch.estimate(e.getKey());
            if (est < minEst) {
                minEst = est;
                minKey = e.getKey();
            }
        }
        long newEst = sketch.estimate(item);
        if (newEst > minEst) {
            candidates.remove(minKey);
            candidates.put(item, count);
        }
    }

    /**
     * Returns the set of items whose estimated frequency meets or exceeds {@code threshold}.
     *
     * @param threshold frequency threshold
     * @return set of heavy hitters
     */
    public Set<Object> getHeavyHitters(long threshold) {
        Set<Object> result = new HashSet<>();
        for (Object key : candidates.keySet()) {
            if (sketch.estimate(key) >= threshold) {
                result.add(key);
            }
        }
        return result;
    }

    /**
     * Exposes the underlying sketch for testing purposes.
     *
     * @return the Count‑Min Sketch instance
     */
    public CountMinSketch getSketch() {
        return sketch;
    }
}
