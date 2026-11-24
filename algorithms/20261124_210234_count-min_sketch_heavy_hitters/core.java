package core;

import java.util.*;

public class CountMinSketch<T> {
    private final int depth;
    private final int width;
    private final long[][] table;
    private final long[] hashA;
    private final long[] hashB;
    private final long prime = 0xFFFFFFFFFFFFFL; // 2^52-1, fits in long
    private final Set<T> observed;

    /**
     * Constructs a Count‑Min Sketch.
     *
     * @param depth number of hash functions (rows), must be > 0
     * @param width number of counters per row, must be > 0
     * @param seed  seed for deterministic hash function generation
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
        this.observed = new HashSet<>();

        Random rng = new Random(seed);
        for (int i = 0; i < depth; i++) {
            // a must be non‑zero for a universal hash; b can be any value
            hashA[i] = rng.nextLong() & Long.MAX_VALUE;
            if (hashA[i] == 0) hashA[i] = 1;
            hashB[i] = rng.nextLong() & Long.MAX_VALUE;
        }
    }

    /**
     * Adds {@code count} occurrences of {@code item} to the sketch.
     *
     * @param item  element to add
     * @param count positive increment
     */
    public void add(T item, long count) {
        if (count <= 0) {
            throw new IllegalArgumentException("Count must be positive");
        }
        int hash = item.hashCode();
        long x = ((long) hash) & 0xffffffffL; // treat as unsigned
        for (int i = 0; i < depth; i++) {
            int idx = (int) ((hashA[i] * x + hashB[i]) % prime % width);
            table[i][idx] += count;
        }
        observed.add(item);
    }

    /**
     * Convenience overload for increment of one.
     */
    public void add(T item) {
        add(item, 1L);
    }

    /**
     * Estimates the frequency of {@code item}.
     *
     * @param item element to query
     * @return estimated count (upper bound)
     */
    public long estimate(T item) {
        int hash = item.hashCode();
        long x = ((long) hash) & 0xffffffffL;
        long min = Long.MAX_VALUE;
        for (int i = 0; i < depth; i++) {
            int idx = (int) ((hashA[i] * x + hashB[i]) % prime % width);
            long val = table[i][idx];
            if (val < min) {
                min = val;
            }
        }
        return min;
    }

    /**
     * Returns all observed items whose estimated frequency is at least {@code threshold}.
     *
     * @param threshold minimum estimated count to be considered a heavy hitter
     * @return set of heavy hitter items
     */
    public Set<T> heavyHitters(long threshold) {
        if (threshold <= 0) {
            throw new IllegalArgumentException("Threshold must be positive");
        }
        Set<T> result = new HashSet<>();
        for (T item : observed) {
            if (estimate(item) >= threshold) {
                result.add(item);
            }
        }
        return result;
    }

    // Getters for testing / introspection
    public int getDepth() { return depth; }
    public int getWidth() { return width; }
    public long[][] getTable() { return table; }
}
