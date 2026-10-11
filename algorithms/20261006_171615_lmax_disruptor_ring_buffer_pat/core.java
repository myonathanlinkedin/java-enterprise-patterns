import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class RingBuffer<T> {
    private final int size;
    private final AtomicReferenceArray<T> buffer;
    private final AtomicLong cursor = new AtomicLong(-1);          // last published sequence
    private final AtomicLong nextSeq = new AtomicLong(0);         // next sequence to claim
    private final AtomicLong consumerSeq = new AtomicLong(-1);    // last consumed sequence

    public RingBuffer(int size) {
        if (size <= 0) throw new IllegalArgumentException("Size must be positive");
        this.size = size;
        this.buffer = new AtomicReferenceArray<>(size);
    }

    /** Producer: claim the next sequence to write to. Blocks if buffer is full. */
    public long next() {
        while (true) {
            long seq = nextSeq.getAndIncrement();
            long wrapPoint = seq - size;
            long consumer = consumerSeq.get();
            if (wrapPoint <= consumer) {
                return seq;
            }
            // buffer full, wait
            Thread.yield();
        }
    }

    /** Producer: publish a value at the given sequence. */
    public void publish(long seq, T value) {
        int index = (int)(seq % size);
        buffer.set(index, value);
        cursor.set(seq);
    }

    /** Consumer: check if there is a new value to consume. */
    public boolean hasNext() {
        return consumerSeq.get() < cursor.get();
    }

    /** Consumer: get the next value. */
    @SuppressWarnings("unchecked")
    public T nextValue() {
        long seq = consumerSeq.get() + 1;
        int index = (int)(seq % size);
        T value = (T) buffer.get(index);
        consumerSeq.set(seq);
        return value;
    }

    /** For testing: get current cursor. */
    public long getCursor() {
        return cursor.get();
    }

    /** For testing: get current consumer sequence. */
    public long getConsumerSeq() {
        return consumerSeq.get();
    }
}
