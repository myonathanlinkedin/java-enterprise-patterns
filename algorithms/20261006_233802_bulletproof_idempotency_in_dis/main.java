import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple test harness without external dependencies.
 */
public class Main {
    public static void main(String[] args) throws Exception {
        runAllTests();
        System.out.println("All tests passed.");
    }

    private static void runAllTests() throws Exception {
        testIdempotentSuccess();
        testExpiration();
        testConcurrentProcessing();
    }

    /** Verify that a second call with the same key returns the cached result. */
    private static void testIdempotentSuccess() {
        IdempotencyKeyStore store = new IdempotencyKeyStore(5_000);
        AtomicInteger callCount = new AtomicInteger(0);
        PaymentProcessor processor = req -> {
            callCount.incrementAndGet();
            return new PaymentResult(true, UUID.randomUUID().toString(),
                    "Processed " + req.amount);
        };
        PaymentService service = new PaymentService(store, processor);

        PaymentRequest req = new PaymentRequest("key-123", "A", "B", 100.0);
        PaymentResult first = service.process(req);
        PaymentResult second = service.process(req);

        if (!first.equals(second))
            throw new AssertionError("Idempotent results differ");
        if (callCount.get() != 1)
            throw new AssertionError("Processor invoked more than once");
    }

    /** Verify that after TTL expiration a new processing occurs. */
    private static void testExpiration() throws InterruptedException {
        IdempotencyKeyStore store = new IdempotencyKeyStore(100); // 100 ms TTL
        AtomicInteger callCount = new AtomicInteger(0);
        PaymentProcessor processor = req -> {
            callCount.incrementAndGet();
            return new PaymentResult(true, UUID.randomUUID().toString(),
                    "Processed " + req.amount);
        };
        PaymentService service = new PaymentService(store, processor);

        PaymentRequest req = new PaymentRequest("exp-key", "A", "B", 50.0);
        PaymentResult first = service.process(req);
        Thread.sleep(150); // exceed TTL
        PaymentResult second = service.process(req);

        if (first.equals(second))
            throw new AssertionError("Result should differ after expiration");
        if (callCount.get() != 2)
            throw new AssertionError("Processor should have been called twice");
    }

    /** Verify that concurrent calls with the same key result in a single processing. */
    private static void testConcurrentProcessing() throws Exception {
        IdempotencyKeyStore store = new IdempotencyKeyStore(5_000);
        AtomicInteger callCount = new AtomicInteger(0);
        PaymentProcessor processor = req -> {
            // Simulate some work
            try { Thread.sleep(10); } catch (InterruptedException ignored) {}
            callCount.incrementAndGet();
            return new PaymentResult(true, UUID.randomUUID().toString(),
                    "Processed " + req.amount);
        };
        PaymentService service = new PaymentService(store, processor);

        final int THREADS = 20;
        ExecutorService exec = Executors.newFixedThreadPool(THREADS);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(THREADS);
        List<Future<PaymentResult>> futures = new ArrayList<>();

        for (int i = 0; i < THREADS; i++) {
            futures.add(exec.submit(() -> {
                startLatch.await(); // ensure all threads start together
                PaymentResult r = service.process(
                        new PaymentRequest("conc-key", "A", "B", 75.0));
                doneLatch.countDown();
                return r;
            }));
        }

        startLatch.countDown(); // release all threads
        doneLatch.await(); // wait for completion
        exec.shutdownNow();

        // All results must be equal
        PaymentResult reference = futures.get(0).get();
        for (Future<PaymentResult> f : futures) {
            if (!reference.equals(f.get()))
                throw new AssertionError("Concurrent results differ");
        }
        if (callCount.get() != 1)
            throw new AssertionError("Processor invoked more than once concurrently");
    }
}
