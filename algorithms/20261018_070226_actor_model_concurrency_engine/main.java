import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Simple test harness for the ActorSystem implementation.
 */
public final class Main {
    public static void main(String[] args) {
        testMessageOrdering();
        testConcurrentActors();
        testActorStopBehavior();
        System.out.println("All tests passed.");
    }

    // -------------------------------------------------------------------------
    // Test 1: Verify that messages are processed in FIFO order.
    // -------------------------------------------------------------------------
    private static void testMessageOrdering() {
        ActorSystem system = new ActorSystem();
        final int N = 1000;
        List<Integer> received = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch latch = new CountDownLatch(N);

        Consumer<Integer> behavior = msg -> {
            received.add(msg);
            latch.countDown();
        };

        ActorSystem.ActorRef<Integer> actor = system.actorOf(behavior);
        for (int i = 0; i < N; i++) {
            actor.tell(i);
        }

        awaitLatch(latch, 5, TimeUnit.SECONDS, "Message ordering test timed out");
        // Verify order
        for (int i = 0; i < N; i++) {
            if (received.get(i) != i) {
                throw new AssertionError("Message order mismatch at index " + i);
            }
        }
        actor.stop();
        system.shutdown();
    }

    // -------------------------------------------------------------------------
    // Test 2: Multiple actors increment a shared counter concurrently.
    // -------------------------------------------------------------------------
    private static void testConcurrentActors() {
        ActorSystem system = new ActorSystem();
        final int ACTORS = 10;
        final int MESSAGES_PER_ACTOR = 500;
        AtomicInteger counter = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(ACTORS * MESSAGES_PER_ACTOR);

        Consumer<String> inc = msg -> {
            counter.incrementAndGet();
            latch.countDown();
        };

        List<ActorSystem.ActorRef<String>> refs = new ArrayList<>();
        for (int i = 0; i < ACTORS; i++) {
            refs.add(system.actorOf(inc));
        }

        for (ActorSystem.ActorRef<String> ref : refs) {
            for (int j = 0; j < MESSAGES_PER_ACTOR; j++) {
                ref.tell("tick");
            }
        }

        awaitLatch(latch, 5, TimeUnit.SECONDS, "Concurrent actors test timed out");
        int expected = ACTORS * MESSAGES_PER_ACTOR;
        if (counter.get() != expected) {
            throw new AssertionError("Counter mismatch: expected " + expected + " but got " + counter.get());
        }

        refs.forEach(ActorSystem.ActorRef::stop);
        system.shutdown();
    }

    // -------------------------------------------------------------------------
    // Test 3: Ensure that sending to a stopped actor throws an exception.
    // -------------------------------------------------------------------------
    private static void testActorStopBehavior() {
        ActorSystem system = new ActorSystem();
        Consumer<Integer> noop = msg -> { /* no-op */ };
        ActorSystem.ActorRef<Integer> actor = system.actorOf(noop);
        actor.stop();

        boolean threw = false;
        try {
            actor.tell(42);
        } catch (IllegalStateException e) {
            threw = true;
        }

        if (!threw) {
            throw new AssertionError("Expected IllegalStateException when sending to stopped actor");
        }
        system.shutdown();
    }

    // -------------------------------------------------------------------------
    // Helper: Await a latch with a clear error message.
    // -------------------------------------------------------------------------
    private static void awaitLatch(CountDownLatch latch, long timeout, TimeUnit unit, String errMsg) {
        try {
            if (!latch.await(timeout, unit)) {
                throw new AssertionError(errMsg);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError("Test interrupted", e);
        }
    }
}
