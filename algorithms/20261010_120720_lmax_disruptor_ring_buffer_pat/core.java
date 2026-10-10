import java.util.concurrent.atomic.AtomicLong;

class Sequence {
    private final AtomicLong value = new AtomicLong(-1);
    public long get() { return value.get(); }
    public void set(long val) { value.set(val); }
    public long incrementAndGet() { return value.incrementAndGet(); }
}

class RingBuffer<T> {
    private final int size;
    private final int mask;
    private final Object[] buffer;
    private final Sequence cursor = new Sequence();
    private final Sequence[] gatingSequences;

    public RingBuffer(int size, Sequence... gatingSequences) {
        if (Integer.bitCount(size) != 1) throw new IllegalArgumentException("size must be power of 2");
        this.size = size;
        this.mask = size - 1;
        this.buffer = new Object[size];
        this.gatingSequences = gatingSequences;
    }

    public long next() {
        long nextSeq = cursor.incrementAndGet();
        long wrapPoint = nextSeq - size;
        while (wrapPoint > getMinimumGatingSequence()) {
            Thread.yield();
        }
        return nextSeq;
    }

    public void publish(long sequence) {
        cursor.set(sequence);
    }

    @SuppressWarnings("unchecked")
    public T get(long sequence) {
        return (T) buffer[(int)(sequence & mask)];
    }

    public void set(long sequence, T event) {
        buffer[(int)(sequence & mask)] = event;
    }

    private long getMinimumGatingSequence() {
        long min = Long.MAX_VALUE;
        for (Sequence seq : gatingSequences) {
            long v = seq.get();
            if (v < min) min = v;
        }
        return min;
    }
}

class SequenceBarrier {
    private final RingBuffer<?> ringBuffer;
    public SequenceBarrier(RingBuffer<?> ringBuffer) {
        this.ringBuffer = ringBuffer;
    }
    public long waitFor(long sequence) {
        while (ringBuffer.cursor.get() < sequence) {
            Thread.yield();
        }
        return ringBuffer.cursor.get();
    }
}

class Consumer implements Runnable {
    private final RingBuffer<String> ringBuffer;
    private final Sequence sequence = new Sequence();
    private final SequenceBarrier barrier;
    private final java.util.List<String> processed = new java.util.ArrayList<>();

    public Consumer(RingBuffer<String> ringBuffer) {
        this.ringBuffer = ringBuffer;
        this.barrier = new SequenceBarrier(ringBuffer);
    }

    public java.util.List<String> getProcessed() { return processed; }

    @Override
    public void run() {
        while (true) {
            long seq = sequence.incrementAndGet();
            long available = barrier.waitFor(seq);
            if (available < seq) break;
            String event = ringBuffer.get(seq);
            processed.add(event);
            if ("END".equals(event)) break;
        }
    }
}

class Producer {
    private final RingBuffer<String> ringBuffer;
    public Producer(RingBuffer<String> ringBuffer) { this.ringBuffer = ringBuffer; }
    public void publish(String event) {
        long seq = ringBuffer.next();
        ringBuffer.set(seq, event);
        ringBuffer.publish(seq);
    }
}
