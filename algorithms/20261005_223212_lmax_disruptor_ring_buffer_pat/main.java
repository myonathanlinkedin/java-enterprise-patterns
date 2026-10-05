import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.CountDownLatch;

public class Main {
    private static final int BUFFER_SIZE = 1024;
    private static final int EVENT_COUNT = 1_000_000;

    public static void main(String[] args) throws Exception {
        System.out.println("Running single-threaded test...");
        testSingleThreaded();
        System.out.println("Single-threaded test passed.");

        System.out.println("Running multi-threaded test...");
        testMultiThreaded();
        System.out.println("Multi-threaded test passed.");

        System.out.println("Running benchmark...");
        benchmark();
    }

    private static void testSingleThreaded() {
        RingBuffer<Integer> rb = new RingBuffer<>(BUFFER_SIZE);
        for (int i = 0; i < 1000; i++) {
            rb.publish(i);
        }
        for (int i = 0; i < 1000; i++) {
            int val = rb.get(i);
            if (val != i) {
                throw new AssertionError("Expected " + i + " but got " + val);
            }
        }
    }

    private static void testMultiThreaded() throws InterruptedException {
        final RingBuffer<Integer> rb = new RingBuffer<>(BUFFER_SIZE);
        final int producers = 4;
        final int consumers = 4;
        final int perProducer = EVENT_COUNT / producers;
        final AtomicInteger produced = new AtomicInteger(0);
        final AtomicInteger consumed = new AtomicInteger(0);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(producers + consumers);

        // Producers
        for (int p = 0; p < producers; p++) {
            new Thread(() -> {
                try {
                    start.await();
                    for (int i = 0; i < perProducer; i++) {
                        rb.publish(produced.getAndIncrement());
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            }).start();
        }

        // Consumers
        for (int c = 0; c < consumers; c++) {
            new Thread(() -> {
                try {
                    start.await();
                    long seq = 0;
                    while (consumed.get() < EVENT_COUNT) {
                        seq = rb.waitFor(seq);
                        Integer val = rb.get(seq);
                        if (val == null) {
                            throw new AssertionError("Null event at seq " + seq);
                        }
                        consumed.incrementAndGet();
                        seq++;
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            }).start();
        }

        start.countDown();
        done.await();

        if (produced.get() != EVENT_COUNT || consumed.get() != EVENT_COUNT) {
            throw new AssertionError("Mismatch: produced=" + produced.get() + " consumed=" + consumed.get());
        }
    }

    private static void benchmark() {
        RingBuffer<Long> rb = new RingBuffer<>(BUFFER_SIZE);
        long start = System.nanoTime();
        for (long i = 0; i < EVENT_COUNT; i++) {
            rb.publish(i);
        }
        long end = System.nanoTime();
        double seconds = (end - start) / 1_000_000_000.0;
        System.out.printf("Published %d events in %.3f seconds (%.2f events/sec)%n",
                EVENT_COUNT, seconds, EVENT_COUNT / seconds);
    }
}
