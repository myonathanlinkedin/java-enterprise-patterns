package lamport;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public class Main {

    private static void assertEquals(long expected, long actual, String msg) {
        if (expected != actual) {
            throw new AssertionError(msg + " Expected: " + expected + ", Actual: " + actual);
        }
    }

    private static void testSingleThreadBehaviour() {
        LamportClock clock = new LamportClock();

        // Initial state
        assertEquals(0L, clock.get(), "Initial timestamp");

        // Local events
        assertEquals(1L, clock.tick(), "After first tick");
        assertEquals(2L, clock.tick(), "After second tick");

        // Receive a message with lower timestamp
        assertEquals(3L, clock.receive(1L), "Receive lower remote timestamp");

        // Receive a message with higher timestamp
        assertEquals(6L, clock.receive(5L), "Receive higher remote timestamp");

        // Further local event
        assertEquals(7L, clock.tick(), "After subsequent tick");
    }

    private static void testConcurrentUpdates() throws InterruptedException {
        final LamportClock clock = new LamportClock();
        final int threadCount = 8;
        final int opsPerThread = 10_000;
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch doneLatch = new CountDownLatch(threadCount);
        final AtomicBoolean failed = new AtomicBoolean(false);

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            new Thread(() -> {
                try {
                    startLatch.await();
                    for (int j = 0; j < opsPerThread; j++) {
                        // Alternate between local tick and receive of a remote timestamp
                        if ((j & 1) == 0) {
                            clock.tick();
                        } else {
                            // Simulate a remote timestamp that may be ahead or behind
                            long remote = threadId * opsPerThread + j;
                            clock.receive(remote);
                        }
                    }
                } catch (Exception e) {
                    failed.set(true);
                } finally {
                    doneLatch.countDown();
                }
            }).start();
        }

        // Release all threads simultaneously
        startLatch.countDown();
        doneLatch.await();

        if (failed.get()) {
            throw new AssertionError("Concurrent execution threw an exception");
        }

        // The exact value is nondeterministic but must be at least the number of ticks performed
        long minExpected = threadCount * opsPerThread / 2; // half of ops are ticks
        long actual = clock.get();
        if (actual < minExpected) {
            throw new AssertionError("Timestamp after concurrency test too low. Expected at least " + minExpected + ", got " + actual);
        }
    }

    private static void testConstructorValidation() {
        try {
            new LamportClock(-5);
            throw new AssertionError("Negative initial timestamp should have thrown");
        } catch (IllegalArgumentException ignored) {
        }

        try {
            LamportClock c = new LamportClock();
            c.receive(-1);
            throw new AssertionError("Negative remote timestamp should have thrown");
        } catch (IllegalArgumentException ignored) {
        }
    }

    public static void main(String[] args) throws Exception {
        testSingleThreadBehaviour();
        testConstructorValidation();
        testConcurrentUpdates();

        System.out.println("All LamportClock tests passed.");
    }
}
