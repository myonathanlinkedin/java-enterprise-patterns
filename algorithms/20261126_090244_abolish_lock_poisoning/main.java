import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) {
        testPoisoning();
        testReset();
        testConcurrent();
        System.out.println("All tests passed.");
    }

    private static void testPoisoning() {
        PoisonableLock lock = new PoisonableLock();
        try {
            lock.runWithLock(() -> {
                throw new RuntimeException("Test exception");
            });
            throw new AssertionError("Expected exception not thrown");
        } catch (RuntimeException e) {
            // expected
        }
        if (!lock.isPoisoned()) {
            throw new AssertionError("Lock should be poisoned after exception");
        }
    }

    private static void testReset() {
        PoisonableLock lock = new PoisonableLock();
        // poison it
        try {
            lock.runWithLock(() -> {
                throw new RuntimeException("Test exception");
            });
        } catch (RuntimeException ignored) {}
        if (!lock.isPoisoned()) {
            throw new AssertionError("Lock should be poisoned");
        }
        lock.resetPoison();
        if (lock.isPoisoned()) {
            throw new AssertionError("Lock should be reset");
        }
        // normal run
        lock.runWithLock(() -> {
            // nothing
        });
        if (lock.isPoisoned()) {
            throw new AssertionError("Lock should not be poisoned after normal run");
        }
    }

    private static void testConcurrent() {
        PoisonableLock lock = new PoisonableLock();
        CountDownLatch latch = new CountDownLatch(2);
        AtomicInteger counter = new AtomicInteger(0);

        Thread t1 = new Thread(() -> {
            lock.runWithLock(() -> {
                counter.incrementAndGet();
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ignored) {}
            });
            latch.countDown();
        });

        Thread t2 = new Thread(() -> {
            lock.runWithLock(() -> {
                counter.incrementAndGet();
            });
            latch.countDown();
        });

        t1.start();
        t2.start();

        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (counter.get() != 2) {
            throw new AssertionError("Both threads should have incremented counter");
        }
        if (lock.isPoisoned()) {
            throw new AssertionError("Lock should not be poisoned in concurrent normal runs");
        }
    }
}
