import java.util.*;

public class Main {
    private static void assertTrue(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static void testNoDrift() {
        JointDistribution ref = new JointDistribution();
        Random rnd = new Random(42);
        for (int i = 0; i < 500; i++) {
            int a = rnd.nextInt(2);
            int b = rnd.nextInt(2);
            ref.update(a, b);
        }
        DriftDetector detector = new DriftDetector(ref, 0.05);
        for (int i = 0; i < 500; i++) {
            int a = rnd.nextInt(2);
            int b = rnd.nextInt(2);
            detector.update(a, b);
        }
        assertTrue(!detector.detect(), "No drift should not be detected");
    }

    private static void testDrift() {
        JointDistribution ref = new JointDistribution();
        Random rnd = new Random(123);
        for (int i = 0; i < 500; i++) {
            int a = rnd.nextInt(2);
            int b = rnd.nextInt(2);
            ref.update(a, b);
        }
        DriftDetector detector = new DriftDetector(ref, 0.05);
        for (int i = 0; i < 500; i++) {
            int a = rnd.nextDouble() < 0.7 ? 0 : 1;
            int b = rnd.nextDouble() < 0.7 ? 0 : 1;
            detector.update(a, b);
        }
        assertTrue(detector.detect(), "Drift should be detected");
    }

    private static void testEdgeCase() {
        JointDistribution ref = new JointDistribution();
        ref.update(0, 0);
        ref.update(0, 0);
        ref.update(1, 1);
        DriftDetector detector = new DriftDetector(ref, 0.05);
        detector.update(0, 0);
        detector.update(1, 1);
        detector.update(1, 1);
        assertTrue(!detector.detect(), "No drift with small counts");
    }

    private static void benchmark() {
        JointDistribution ref = new JointDistribution();
        Random rnd = new Random(999);
        for (int i = 0; i < 1000000; i++) {
            int a = rnd.nextInt(5);
            int b = rnd.nextInt(5);
            ref.update(a, b);
        }
        DriftDetector detector = new DriftDetector(ref, 0.05);
        long start = System.nanoTime();
        for (int i = 0; i < 1000000; i++) {
            int a = rnd.nextInt(5);
            int b = rnd.nextInt(5);
            detector.update(a, b);
        }
        long elapsed = System.nanoTime() - start;
        System.out.printf("Processed 1,000,000 updates in %.3f ms%n", elapsed / 1e6);
    }

    public static void main(String[] args) {
        try {
            testNoDrift();
            System.out.println("testNoDrift passed");
            testDrift();
            System.out.println("testDrift passed");
            testEdgeCase();
            System.out.println("testEdgeCase passed");
            benchmark();
        } catch (AssertionError e) {
            System.err.println("Test failed: " + e.getMessage());
        }
    }
}
