public class Main {
    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void testInternalEvents() {
        LamportClock clock = new LamportClock();
        assertTrue(clock.getTime() == 0, "Initial time must be 0");
        int t1 = clock.tick();
        assertTrue(t1 == 1, "First tick should be 1");
        int t2 = clock.tick();
        assertTrue(t2 == 2, "Second tick should be 2");
        int t3 = clock.sendEvent();
        assertTrue(t3 == 3, "Send event should increment to 3");
        clock.receiveEvent(5);
        assertTrue(clock.getTime() == 6, "Receive with remote 5 should set time to 6");
    }

    private static void testSendReceiveOrdering() {
        LamportClock p1 = new LamportClock(); // Process A
        LamportClock p2 = new LamportClock(); // Process B

        // A performs two internal events
        p1.tick(); // 1
        p1.tick(); // 2

        // A sends a message to B
        int tsSend = p1.sendEvent(); // 3
        Message<String> msg = new Message<>("hello", tsSend);

        // B receives the message
        p2.receiveEvent(msg.getTimestamp()); // max(0,3)+1 = 4
        assertTrue(p2.getTime() == 4, "B's time after receive should be 4");

        // B sends a reply
        int tsReply = p2.sendEvent(); // 5
        Message<String> reply = new Message<>("world", tsReply);

        // A receives reply
        p1.receiveEvent(reply.getTimestamp()); // max(3,5)+1 = 6
        assertTrue(p1.getTime() == 6, "A's time after receiving reply should be 6");
    }

    private static void testCausalMonotonicity() {
        LamportClock a = new LamportClock();
        LamportClock b = new LamportClock();
        LamportClock c = new LamportClock();

        // A internal
        a.tick(); //1
        // A sends to B
        Message<Integer> m1 = new Message<>(1, a.sendEvent()); //2
        b.receiveEvent(m1.getTimestamp()); // max(0,2)+1=3

        // B sends to C
        Message<Integer> m2 = new Message<>(2, b.sendEvent()); //4
        c.receiveEvent(m2.getTimestamp()); // max(0,4)+1=5

        // C internal
        c.tick(); //6

        // C sends back to A
        Message<Integer> m3 = new Message<>(3, c.sendEvent()); //7
        a.receiveEvent(m3.getTimestamp()); // max(2,7)+1=8

        assertTrue(a.getTime() == 8, "A final time 8");
        assertTrue(b.getTime() == 4, "B final time 4");
        assertTrue(c.getTime() == 7, "C final time 7");
    }

    private static void testReceiveLowerTimestamp() {
        LamportClock d = new LamportClock();
        d.tick(); //1
        d.receiveEvent(0); // max(1,0)+1 = 2
        assertTrue(d.getTime() == 2, "Receiving lower timestamp should still increment");
    }

    public static void main(String[] args) {
        try {
            testInternalEvents();
            testSendReceiveOrdering();
            testCausalMonotonicity();
            testReceiveLowerTimestamp();
            System.out.println("All Lamport Clock tests passed.");
        } catch (AssertionError e) {
            System.err.println("Test failed: " + e.getMessage());
            System.exit(1);
        }
    }
}
