package lamport;

public class LamportClock {
    private long timestamp;

    public LamportClock() {
        this.timestamp = 0L;
    }

    public LamportClock(long initial) {
        if (initial < 0) {
            throw new IllegalArgumentException("Initial timestamp must be non‑negative");
        }
        this.timestamp = initial;
    }

    /**
     * Called when a local event occurs.
     * Increments the logical clock and returns the new timestamp.
     */
    public synchronized long tick() {
        timestamp++;
        return timestamp;
    }

    /**
     * Called when a message is received that carries a remote timestamp.
     * The clock is updated to max(local, remote) + 1 and the new value is returned.
     *
     * @param remoteTimestamp timestamp received from a peer
     * @return the updated local timestamp
     */
    public synchronized long receive(long remoteTimestamp) {
        if (remoteTimestamp < 0) {
            throw new IllegalArgumentException("Remote timestamp must be non‑negative");
        }
        timestamp = Math.max(timestamp, remoteTimestamp) + 1;
        return timestamp;
    }

    /**
     * Returns the current logical timestamp without modifying it.
     */
    public synchronized long get() {
        return timestamp;
    }

    @Override
    public synchronized String toString() {
        return "LamportClock{" + "timestamp=" + timestamp + '}';
    }
}
