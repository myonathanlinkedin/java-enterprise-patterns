public class LamportClock {
    private int time;

    public LamportClock() {
        this.time = 0;
    }

    /** Internal event: increment and return new timestamp. */
    public synchronized int tick() {
        return ++time;
    }

    /** Return current timestamp without modification. */
    public synchronized int getTime() {
        return time;
    }

    /** Event of sending a message: increment and return timestamp to attach. */
    public synchronized int sendEvent() {
        return ++time;
    }

    /**
     * Event of receiving a message with remote timestamp.
     * Updates local time to max(local, remote) + 1.
     */
    public synchronized void receiveEvent(int remoteTimestamp) {
        time = Math.max(time, remoteTimestamp) + 1;
    }
}

/**
 * Simple immutable message carrying a payload and a Lamport timestamp.
 */
public class Message<T> {
    private final T payload;
    private final int timestamp;

    public Message(T payload, int timestamp) {
        this.payload = payload;
        this.timestamp = timestamp;
    }

    public T getPayload() {
        return payload;
    }

    public int getTimestamp() {
        return timestamp;
    }
}
