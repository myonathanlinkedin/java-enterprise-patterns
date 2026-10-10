public class RingBuffer<T> {
    private final int size;
    private final Object[] buffer;
    private final int indexMask;
    private volatile long cursor = -1;
    private volatile long gatingSequence = -1;

    public RingBuffer(int size) {
        if (Integer.bitCount(size) != 1) {
            throw new IllegalArgumentException("Size must be a power of two");
        }
        this.size = size;
        this.buffer = new Object[size];
        this.indexMask = size - 1;
    }

    public long next() {
        long nextSeq = cursor + 1;
        while (nextSeq - size > gatingSequence) {
            Thread.yield();
        }
        cursor = nextSeq;
        return nextSeq;
    }

    public void publish(long sequence) {
        // No-op in this simplified implementation
    }

    public void set(long sequence, T value) {
        int index = (int) (sequence & indexMask);
        buffer[index] = value;
    }

    @SuppressWarnings("unchecked")
    public T get(long sequence) {
        int index = (int) (sequence & indexMask);
        return (T) buffer[index];
    }

    public void setGatingSequence(long seq) {
        gatingSequence = seq;
    }

    public long getCursor() {
        return cursor;
    }
}
