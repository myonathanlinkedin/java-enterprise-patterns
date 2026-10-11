import java.util.*;

public class Main {
    public static void main(String[] args) {
        runTests();
        benchmark();
    }

    private static void runTests() {
        testTick();
        testSendReceive();
        testMultipleProcesses();
        System.out.println("All tests passed.");
    }

    private static void testTick() {
        LamportClock clock = new LamportClock();
        int start = clock.getTimestamp();
        clock.tick();
        assert clock.getTimestamp() == start + 1 : "Tick should increment by 1";
        clock.tick();
        assert clock.getTimestamp() == start + 2 : "Second tick should increment by 1 again";
    }

    private static void testSendReceive() {
        LamportClock sender = new LamportClock();
        LamportClock receiver = new LamportClock();

        int msgTimestamp = sender.send(); // sender ticks to 1
        assert msgTimestamp == 1 : "Send should return timestamp 1";
        assert sender.getTimestamp() == 1 : "Sender timestamp should be 1 after send";

        receiver.receive(msgTimestamp); // receiver updates to max(0,1)+1 = 2
        assert receiver.getTimestamp() == 2 : "Receiver should update to 2 after receiving 1";
    }

    private static void testMultipleProcesses() {
        LamportClock p1 = new LamportClock();
        LamportClock p2 = new LamportClock();
        LamportClock p3 = new LamportClock();

        // p1 sends message to p2
        int t1 = p1.send(); // p1=1
        p2.receive(t1);     // p2=2

        // p2 sends to p3
        int t2 = p2.send(); // p2=3
        p3.receive(t2);     // p3=4

        // p3 sends back to p1
        int t3 = p3.send(); // p3=5
        p1.receive(t3);     // p1=max(1,5)+1=6

        assert p1.getTimestamp() == 6 : "p1 should be 6";
        assert p2.getTimestamp() == 3 : "p2 should be 3";
        assert p3.getTimestamp() == 5 : "p3 should be 5";
    }

    private static void benchmark() {
        final int ITERATIONS = 1_000_000;
        LamportClock clock = new LamportClock();
        long start = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) {
            clock.tick();
        }
        long duration = System.nanoTime() - start;
        System.out.printf("Ticked %d times in %.3f ms%n", ITERATIONS, duration / 1_000_000.0);
    }
}
