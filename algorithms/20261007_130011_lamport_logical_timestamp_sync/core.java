public class LamportClock {
    private int timestamp;

    public LamportClock() {
        this.timestamp = 0;
    }

    public LamportClock(int initial) {
        if (initial < 0) throw new IllegalArgumentException("Timestamp cannot be negative");
        this.timestamp = initial;
    }

    /** Increment the clock for a local event. */
    public void tick() {
        timestamp++;
    }

    /** Return the current timestamp. */
    public int getTimestamp() {
        return timestamp;
    }

    /**
     * Simulate sending a message: increment clock and return the timestamp to send.
     * @return the timestamp to attach to the message
     */
    public int send() {
        tick();
        return timestamp;
    }

    /**
     * Receive a message with a given timestamp.
     * Update local clock to max(local, received) + 1.
     * @param receivedTimestamp the timestamp from the received message
     */
    public void receive(int receivedTimestamp) {
        if (receivedTimestamp < 0) throw new IllegalArgumentException("Received timestamp cannot be negative");
        timestamp = Math.max(timestamp, receivedTimestamp) + 1;
    }
}
