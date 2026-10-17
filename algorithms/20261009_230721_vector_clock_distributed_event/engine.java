package vectorclock;

/**
 * Engine class exposing static helper methods for vector‑clock based
 * distributed event ordering. All methods delegate to {@link VectorClock}
 * but are provided for a clean API surface.
 */
public final class Engine {

    private Engine() {
        // Prevent instantiation
    }

    /**
     * Increments the clock of the given process.
     *
     * @param clock current clock (may be null, interpreted as empty)
     * @param pid   process identifier
     * @return new clock with incremented counter
     */
    public static VectorClock increment(VectorClock clock, String pid) {
        if (clock == null) {
            clock = new VectorClock();
        }
        return clock.increment(pid);
    }

    /**
     * Merges two vector clocks.
     *
     * @param a first clock (may be null)
     * @param b second clock (may be null)
     * @return merged clock
     */
    public static VectorClock merge(VectorClock a, VectorClock b) {
        if (a == null) return b == null ? new VectorClock() : b;
        if (b == null) return a;
        return a.merge(b);
    }

    /**
     * Determines the ordering relation between two clocks.
     *
     * @param a first clock (may be null, treated as empty)
     * @param b second clock (may be null, treated as empty)
     * @return relation a ? b
     */
    public static VectorClock.Relation compare(VectorClock a, VectorClock b) {
        if (a == null) a = new VectorClock();
        if (b == null) b = new VectorClock();
        return a.compare(b);
    }
}
