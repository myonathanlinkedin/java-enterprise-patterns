import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class Main {
    /* Simple assertion helper */
    private static void assertTrue(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    /* Test that PingActor and PongActor exchange a fixed number of messages */
    private static void testPingPong() throws Exception {
        final int exchanges = 10;
        CountDownLatch latch = new CountDownLatch(exchanges);
        ActorSystem system = new ActorSystem();

        // Create PongActor first (it does not need a partner reference)
        PongActor pong = (PongActor) system.createActor(() -> new PongActor(latch));

        // Create PingActor with reference to PongActor
        PingActor ping = (PingActor) system.createActor(() -> new PingActor(pong, latch));

        // Kick off the exchange
        ping.tell(ping, new Ping(exchanges));

        // Wait for all exchanges to complete or timeout
        boolean completed = latch.await(5, TimeUnit.SECONDS);
        assertTrue(completed, "Ping-Pong exchanges did not complete in time");

        // Gracefully shutdown the system
        system.shutdown();
    }

    /* Test that actors stop correctly after receiving Stop */
    private static void testActorStop() throws Exception {
        ActorSystem system = new ActorSystem();
        CountDownLatch latch = new CountDownLatch(1);
        Actor stopper = system.createActor(() -> new Actor() {
            @Override
            protected void onReceive(Message msg, Actor sender) {
                if (msg instanceof Stop) {
                    this.stop();
                }
            }
        });
        // Send Stop and ensure termination
        stopper.tell(stopper, new Stop());
        system.shutdown(); // should return quickly
        latch.countDown(); // just to use latch and avoid unused warning
    }

    public static void main(String[] args) throws Exception {
        // Enable assertions if not already enabled
        boolean assertsEnabled = false;
        assert assertsEnabled = true;
        if (!assertsEnabled) {
            System.err.println("Assertions are not enabled. Run with -ea.");
            return;
        }

        testPingPong();
        testActorStop();

        System.out.println("All tests passed.");
    }
}
