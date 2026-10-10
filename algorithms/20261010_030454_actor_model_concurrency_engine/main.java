import java.util.*;
import java.util.concurrent.*;

public class Main {
    public static void main(String[] args) {
        runTests();
        System.out.println("All tests passed.");
    }

    private static void runTests() {
        testSingleMessage();
        testMultipleMessages();
        testConcurrentSend();
        testActorStop();
    }

    private static void testSingleMessage() {
        ActorSystem system = new ActorSystem(2);
        List<Object> received = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch latch = new CountDownLatch(1);

        Actor actor = system.actor(msg -> {
            received.add(msg);
            latch.countDown();
        });

        actor.send("hello");
        await(latch, 1);
        if (received.size() != 1 || !received.get(0).equals("hello")) {
            throw new AssertionError("Single message test failed");
        }
        system.shutdown();
    }

    private static void testMultipleMessages() {
        ActorSystem system = new ActorSystem(2);
        List<Object> received = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch latch = new CountDownLatch(5);

        Actor actor = system.actor(msg -> {
            received.add(msg);
            latch.countDown();
        });

        for (int i = 0; i < 5; i++) {
            actor.send(i);
        }
        await(latch, 1);
        if (received.size() != 5) {
            throw new AssertionError("Multiple messages count mismatch");
        }
        for (int i = 0; i < 5; i++) {
            if (!received.get(i).equals(i)) {
                throw new AssertionError("Message order incorrect");
            }
        }
        system.shutdown();
    }

    private static void testConcurrentSend() {
        ActorSystem system = new ActorSystem(4);
        List<Object> received = Collections.synchronizedList(new ArrayList<>());
        int totalMessages = 100;
        CountDownLatch latch = new CountDownLatch(totalMessages);

        Actor actor = system.actor(msg -> {
            received.add(msg);
            latch.countDown();
        });

        ExecutorService senderPool = Executors.newFixedThreadPool(10);
        for (int i = 0; i < 10; i++) {
            final int threadId = i;
            senderPool.submit(() -> {
                for (int j = 0; j < 10; j++) {
                    actor.send(threadId * 10 + j);
                }
            });
        }
        senderPool.shutdown();
        await(latch, 2);
        if (received.size() != totalMessages) {
            throw new AssertionError("Concurrent send count mismatch");
        }
        Set<Object> unique = new HashSet<>(received);
        if (unique.size() != totalMessages) {
            throw new AssertionError("Duplicate messages detected");
        }
        system.shutdown();
    }

    private static void testActorStop() {
        ActorSystem system = new ActorSystem(2);
        List<Object> received = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch latch = new CountDownLatch(1);

        Actor actor = system.actor(msg -> {
            received.add(msg);
            latch.countDown();
        });

        actor.send("first");
        await(latch, 1);
        actor.stop();
        try {
            actor.send("second");
        } catch (IllegalArgumentException e) {
            // expected if null, but send should accept; we test that message is not processed
        }
        // Wait to ensure no further processing
        Thread.sleep(200);
        if (received.size() != 1 || !received.get(0).equals("first")) {
            throw new AssertionError("Actor stop test failed");
        }
        system.shutdown();
    }

    private static void await(CountDownLatch latch, long seconds) {
        try {
            if (!latch.await(seconds, TimeUnit.SECONDS)) {
                throw new AssertionError("Timeout waiting for latch");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError("Interrupted while waiting");
        }
    }
}
