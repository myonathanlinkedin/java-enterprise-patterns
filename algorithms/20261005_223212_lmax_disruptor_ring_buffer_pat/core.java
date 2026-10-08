import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReferenceArray;

public final class RingBuffer<T> {
    private final int size;
    private final int mask;
    private final AtomicReferenceArray<T> buffer;
    private final AtomicLong cursor = new AtomicLong(-1);
    private final AtomicLong nextSequence = new AtomicLong(0);

    public RingBuffer(int size) {
        if (Integer.bitCount(size) != 1) {
            throw new IllegalArgumentException("Size must be power of two");
        }
        this.size = size;
        this.mask = size - 1;
        this.buffer = new AtomicReferenceArray<>(size);
    }

    public long publish(T event) {
        long seq = nextSequence.getAndIncrement();
        int index = (int) (seq & mask);
        buffer.set(index, event);
        // Ensure the event is visible before updating cursor
        cursor.set(seq);
        return seq;
    }

    public T get(long sequence) {
        if (sequence > cursor.get()) {
            throw new IllegalStateException("Event not yet published");
        }
        int index = (int) (sequence & mask);
        return buffer.get(index);
    }

    public long waitFor(long sequence) {
        while (cursor.get() < sequence) {
            Thread.yield();
        }
        return sequence;
    }

    public long getCursor() {
        return cursor.get();
    }

    public int getSize() {
        return size;
    }
}
