import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Demonstration and unit‑test driver for the Actor Model engine.
 */
public class Main {
    // Simple payload types
    private static final class Ping {
        final int remaining;

        Ping(int remaining) {
            this.remaining = remaining;
        }
    }

    private static final class Pong {
        final int remaining;

        Pong(int remaining) {
            this.remaining = remaining;
        }
    }

    /**
     * Actor that initiates a ping‑pong exchange.
     */
    private static final class PingActor implements Actor {
        private final ActorRef partner;
        private final CountDownLatch doneSignal;
        private final int maxExchanges;

        PingActor(ActorRef self, ActorRef partner, int maxExchanges, CountDownLatch doneSignal) {
            this.partner = partner;
            this.maxExchanges = maxExchanges;
            this.doneSignal = doneSignal;
        }

        @Override
        public void receive(Message msg) {
            Object payload = msg.getPayload();
            if (payload instanceof Pong) {
                Pong pong = (Pong) payload;
                if (pong.remaining > 0) {
                    partner.send(new Message(new Ping(pong.remaining - 1)));
                } else {
                    // Exchange finished
                    doneSignal.countDown();
                }
            } else {
                throw new IllegalArgumentException("PingActor received unexpected message: " + payload);
            }
        }
    }

    /**
     * Actor that replies to ping messages with pong messages.
     */
    private static final class PongActor implements Actor {
        private final ActorRef partner;
        private final CountDownLatch doneSignal;

        PongActor(ActorRef self, ActorRef partner, CountDownLatch doneSignal) {
            this.partner = partner;
            this.doneSignal = doneSignal;
        }

        @Override
        public void receive(Message msg) {
            Object payload = msg.getPayload();
            if (payload instanceof Ping) {
                Ping ping = (Ping) payload;
                if (ping.remaining > 0) {
                    partner.send(new Message(new Pong(ping.remaining - 1)));
                } else {
                    // Final ping received; reply with final pong to let ping actor finish
                    partner.send(new Message(new Pong(0)));
                }
            } else {
                throw new IllegalArgumentException("PongActor received unexpected message: " + payload);
            }
        }
    }

    /**
     * Simple assertion helper that throws AssertionError on failure.
     */
    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    /**
     * Unit test: verifies a deterministic ping‑pong exchange of N rounds.
     */
    private static void testPingPong() throws Exception {
        final int rounds = 10;
        final CountDownLatch done = new CountDownLatch(1);
        ActorSystem system = ActorSystem.create();

        // Placeholders for refs (will be set after creation)
        final ActorRef[] pingRef = new ActorRef[1];
        final ActorRef[] pongRef = new ActorRef[1];

        // Spawn Pong first (needs reference to Ping)
        pongRef[0] = system.spawn(self -> new PongActor(self, null, done)); // partner to be set later

        // Spawn Ping (needs reference to Pong)
        pingRef[0] = system.spawn(self -> new PingActor(self, pongRef[0], rounds, done));

        // Now that Ping ref exists, set Pong's partner
        ((PongActor) ((ActorWorker) ((java.util.ArrayList<?>) system.workers).get(0)).actor).partner = pingRef[0];

        system.start();

        // Kick off the exchange
        pingRef[0].send(new Message(new Ping(rounds)));

        // Wait for completion or timeout
        boolean finished = done.await(5, TimeUnit.SECONDS);
        assertTrue(finished, "Ping‑Pong exchange did not complete in time");

        system.shutdown();
    }

    /**
     * Edge‑case test: sending a null message must raise IllegalArgumentException.
     */
    private static void testNullMessage() {
        ActorSystem system = ActorSystem.create();
        ActorRef ref = system.spawn(self -> msg -> {
            // no‑op
        });
        system.start();

        boolean threw = false;
        try {
            ref.send(null);
        } catch (IllegalArgumentException e) {
            threw = true;
        }
        assertTrue(threw, "Sending null message should throw IllegalArgumentException");

        try {
            system.shutdown();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws Exception {
        // Run unit tests
        testPingPong();
        testNullMessage();

        // Demo: simple fire‑and‑forget actor that prints messages
        ActorSystem demoSystem = ActorSystem.create();
        ActorRef printer = demoSystem.spawn(self -> msg -> {
            System.out.println("Printer received: " + msg.getPayload());
        });
        demoSystem.start();
        printer.send(new Message("Hello, Actor Model!"));
        // Give the printer a moment to output before shutdown
        Thread.sleep(100);
        demoSystem.shutdown();

        System.out.println("All tests passed.");
    }
}
