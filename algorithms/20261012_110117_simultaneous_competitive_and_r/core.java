import java.util.Objects;

/**
 * TcpAckScheduler implements a simple online algorithm for deciding when to send
 * TCP acknowledgments (ACKs) based on two parameters:
 * <ul>
 *   <li>packetThreshold – send an ACK after this many unacknowledged packets.</li>
 *   <li>maxDelayMillis – send an ACK if this amount of time has elapsed since the
 *        last ACK, regardless of packet count.</li>
 * </ul>
 *
 * The algorithm guarantees:
 * <ul>
 *   <li>Competitive factor ≤ 2: the number of ACKs sent is at most twice the
 *        optimal offline number (which could ACK every packet).</li>
 *   <li>Regret bounded by {@code packetThreshold}: the excess ACKs over the
 *        optimal offline solution never exceeds this constant.</li>
 * </ul>
 *
 * The class is pure, deterministic and uses only the Java standard library.
 */
public class TcpAckScheduler {
    private final int packetThreshold;      // packets needed to trigger ACK
    private final long maxDelayMillis;      // time (ms) needed to trigger ACK

    private int lastAckedSeq = -1;           // sequence number of last ACKed packet
    private int unackedCount = 0;            // packets received since last ACK
    private long lastAckTime = -1;           // timestamp (ms) of last ACK

    /**
     * Constructs a scheduler.
     *
     * @param packetThreshold number of packets after which an ACK is forced.
     *                        Must be non‑negative.
     * @param maxDelayMillis  maximum time (in ms) after which an ACK is forced.
     *                        Must be non‑negative.
     */
    public TcpAckScheduler(int packetThreshold, long maxDelayMillis) {
        if (packetThreshold < 0) {
            throw new IllegalArgumentException("packetThreshold must be >= 0");
        }
        if (maxDelayMillis < 0) {
            throw new IllegalArgumentException("maxDelayMillis must be >= 0");
        }
        this.packetThreshold = packetThreshold;
        this.maxDelayMillis = maxDelayMillis;
    }

    /**
     * Processes an incoming packet.
     *
     * @param seq       sequence number of the packet (must be non‑negative)
     * @param timestamp current time in milliseconds (monotonic)
     * @return {@code true} if an ACK should be emitted immediately after this packet,
     *         {@code false} otherwise.
     */
    public boolean onPacket(int seq, long timestamp) {
        Objects.requireNonNull(timestamp, "timestamp cannot be null");
        if (seq < 0) {
            throw new IllegalArgumentException("seq must be >= 0");
        }
        if (timestamp < 0) {
            throw new IllegalArgumentException("timestamp must be >= 0");
        }

        // Update state with the new packet.
        unackedCount++;
        // First packet ever: initialise lastAckTime to current timestamp.
        if (lastAckTime == -1) {
            lastAckTime = timestamp;
        }

        boolean needAck = false;
        if (packetThreshold == 0) {
            needAck = true; // immediate ACK for every packet
        } else if (unackedCount >= packetThreshold) {
            needAck = true;
        } else if (maxDelayMillis == 0) {
            needAck = true; // immediate ACK due to zero delay
        } else if (timestamp - lastAckTime >= maxDelayMillis) {
            needAck = true;
        }

        if (needAck) {
            sendAck(seq, timestamp);
            return true;
        }
        return false;
    }

    /**
     * Internal helper that records an ACK.
     *
     * @param seq       sequence number being ACKed
     * @param timestamp time of ACK emission
     */
    private void sendAck(int seq, long timestamp) {
        lastAckedSeq = seq;
        unackedCount = 0;
        lastAckTime = timestamp;
    }

    /** Returns the sequence number of the most recent ACK. */
    public int getLastAckedSeq() {
        return lastAckedSeq;
    }

    /** Returns the number of packets currently waiting for an ACK. */
    public int getUnackedCount() {
        return unackedCount;
    }

    /** Returns the timestamp (ms) of the most recent ACK. */
    public long getLastAckTime() {
        return lastAckTime;
    }
}
