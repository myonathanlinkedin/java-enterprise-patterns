import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;

public class RingBuffer<T> {
    private final int size;
    private final long mask;
    private final Object[] buffer;
    private final AtomicLong cursor = new AtomicLong(-1);
    private final AtomicLong nextSequence = new AtomicLong(0);
    private final AtomicLongArray gatingSequences;

    public RingBuffer(int size) {
        if (Integer.bitCount(size) != 1) {
            throw new IllegalArgumentException("Size must be power of 2");
        }
        this.size = size;
        this.mask = size - 1;
        this.buffer = new Object[size];
        this.gatingSequences = new AtomicLongArray(1);
        this.gatingSequences.set(0, -1);
    }

    public long next() {
        long seq = nextSequence.getAndIncrement();
        long wrapPoint = seq - size;
        while (gatingSequences.get(0) <= wrapPoint) {
            // busy spin
        }
        return seq;
    }

    public void publish(long seq, T event) {
        buffer[(int)(seq & mask)] = event;
        cursor.set(seq);
    }

    @SuppressWarnings("unchecked")
    public T get(long seq) {
        return (T) buffer[(int)(seq & mask)];
    }

    public long getCursor() {
        return cursor.get();
    }

    public void setGatingSequence(long seq) {
        gatingSequences.set(0, seq);
    }

    public long getGatingSequence() {
        return gatingSequences.get(0);
    }
}

public class EventProcessor<T> implements Runnable {
    private final RingBuffer<T> ringBuffer;
    private long nextSequence = 0;
    private volatile boolean running = true;

    public EventProcessor(RingBuffer<T> ringBuffer) {
        this.ringBuffer = ringBuffer;
        ringBuffer.setGatingSequence(-1);
    }

    public void run() {
        while (running) {
            long cursor = ringBuffer.getCursor();
            while (nextSequence <= cursor) {
                T event = ringBuffer.get(nextSequence);
                // process event (noop)
                nextSequence++;
                ringBuffer.setGatingSequence(nextSequence - 1);
            }
            Thread.yield();
        }
    }

    public void stop() {
        running = false;
    }
}
