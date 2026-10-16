package actor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public final class Main {
    public static void main(String[] args) {
        ActorSystem system = new ActorSystem();

        // Test 1: Simple message ordering
        List<String> received = new ArrayList<>();
        Actor collector = new Actor() {
            @Override
            public void receive(Message msg, ActorRef self, ActorRef sender) {
                if (msg instanceof TextMessage) {
                    received.add(((TextMessage) msg).text);
                }
            }
        };
        ActorRef collectorRef = system.registerActor(collector);

        collectorRef.send(new TextMessage("msg1"), null);
        collectorRef.send(new TextMessage("msg2"), null);
        collectorRef.send(new TextMessage("msg3"), null);

        sleep(200);

        assert received.size() == 3 : "Expected 3 messages";
        assert received.get(0).equals("msg1") : "First message mismatch";
        assert received.get(1).equals("msg2") : "Second message mismatch";
        assert received.get(2).equals("msg3") : "Third message mismatch";

        // Test 2: Actor sending messages to another actor
        List<String> forwarded = new ArrayList<>();
        Actor collector2 = new Actor() {
            @Override
            public void receive(Message msg, ActorRef self, ActorRef sender) {
                if (msg instanceof TextMessage) {
                    forwarded.add(((TextMessage) msg).text);
                }
            }
        };
        ActorRef collectorRef2 = system.registerActor(collector2);

        Actor forwarder = new Actor() {
            @Override
            public void receive(Message msg, ActorRef self, ActorRef sender) {
                if (msg instanceof TextMessage) {
                    collectorRef2.send(new TextMessage(((TextMessage) msg).text + "-forwarded"), self);
                }
            }
        };
        ActorRef forwarderRef = system.registerActor(forwarder);

        forwarderRef.send(new TextMessage("hello"), null);

        sleep(200);

        assert forwarded.size() == 1 : "Expected one forwarded message";
        assert forwarded.get(0).equals("hello-forwarded") : "Forwarded message content mismatch";

        // Test 3: Concurrent sending
        int numMessages = 1000;
        CountDownLatch latch = new CountDownLatch(numMessages);
        AtomicInteger counter = new AtomicInteger(0);
        Actor counterActor = new Actor() {
            @Override
            public void receive(Message msg, ActorRef self, ActorRef sender) {
                if (msg instanceof TextMessage) {
                    counter.incrementAndGet();
                    latch.countDown();
                }
            }
        };
        ActorRef counterRef = system.registerActor(counterActor);

        for (int i = 0; i < numMessages; i++) {
            counterRef.send(new TextMessage("msg" + i), null);
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        assert counter.get() == numMessages : "All messages should be processed";

        system.shutdown();

        System.out.println("All tests passed.");
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static final class TextMessage implements Message {
        final String text;
        TextMessage(String text) {
            this.text = text;
        }
    }
}
