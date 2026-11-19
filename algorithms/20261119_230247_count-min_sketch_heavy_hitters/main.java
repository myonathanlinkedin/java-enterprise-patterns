import java.util.*;

public class Main {
    public static void main(String[] args) {
        runTests();
        benchmark();
    }

    private static void runTests() {
        testAddAndEstimate();
        testHeavyHitters();
        testEdgeCases();
        testLargeCounts();
        testDeterminism();
        testMultipleSeeds();
        System.out.println("All tests passed.");
    }

    private static void testAddAndEstimate() {
        CountMinSketch cms = new CountMinSketch(5, 1000, 12345L);
        cms.add("a", 10);
        cms.add("b", 5);
        long estA = cms.estimate("a");
        long estB = cms.estimate("b");
        if (estA < 10) throw new AssertionError("Estimate for 'a' too low");
        if (estB < 5) throw new AssertionError("Estimate for 'b' too low");
    }

    private static void testHeavyHitters() {
        CountMinSketch cms = new CountMinSketch(5, 2000, 67890L);
        Map<String, Integer> freq = new HashMap<>();
        for (int i = 0; i < 5000; i++) { freq.merge("a", 1, Integer::sum); }
        for (int i = 0; i < 3000; i++) { freq.merge("b", 1, Integer::sum); }
        for (int i = 0; i < 2000; i++) { freq.merge("c", 1, Integer::sum); }
        for (Map.Entry<String, Integer> e : freq.entrySet()) {
            cms.add(e.getKey(), e.getValue());
        }
        Set<Object> candidates = new HashSet<>(freq.keySet());
        List<Object> heavy = cms.getHeavyHitters(0.4, candidates);
        Set<Object> expected = new HashSet<>(Arrays.asList("a", "b"));
        if (!heavy.containsAll(expected) || heavy.size() != expected.size()) {
            throw new AssertionError("Heavy hitters detection failed");
        }
    }

    private static void testEdgeCases() {
        CountMinSketch cms = new CountMinSketch(3, 500, 11111L);
        cms.add("x", 0);
        if (cms.estimate("x") != 0) throw new AssertionError("Zero count added incorrectly");
        try {
            cms.add("y", -5);
            throw new AssertionError("Negative count did not throw");
        } catch (IllegalArgumentException ignored) {}
    }

    private static void testLargeCounts() {
        CountMinSketch cms = new CountMinSketch(4, 5000, 22222L);
        long large = 1_000_000_000L;
        cms.add("big", large);
        long est = cms.estimate("big");
        if (est < large) throw new AssertionError("Large count underestimated");
        if (est > large + 1000) throw new AssertionError("Large count overestimated too much");
    }

    private static void testDeterminism() {
        CountMinSketch cms1 = new CountMinSketch(5, 1500, 33333L);
        CountMinSketch cms2 = new CountMinSketch(5, 1500, 33333L);
        for (int i = 0; i < 1000; i++) {
            cms1.add("item" + i, 1);
            cms2.add("item" + i, 1);
        }
        for (int i = 0; i < 1000; i++) {
            long e1 = cms1.estimate("item" + i);
            long e2 = cms2.estimate("item" + i);
            if (e1 != e2) throw new AssertionError("Determinism failed");
        }
    }

    private static void testMultipleSeeds() {
        CountMinSketch cms1 = new CountMinSketch(5, 1500, 44444L);
        CountMinSketch cms2 = new CountMinSketch(5, 1500, 55555L);
        for (int i = 0; i < 1000; i++) {
            cms1.add("item" + i, 1);
            cms2.add("item" + i, 1);
        }
        int diff = 0;
        for (int i = 0; i < 1000; i++) {
            if (cms1.estimate("item" + i) != cms2.estimate("item" + i)) diff++;
        }
        if (diff == 0) throw new AssertionError("Different seeds produced identical estimates");
    }

    private static void benchmark() {
        CountMinSketch cms = new CountMinSketch(5, 2000, 77777L);
        Random rnd = new Random(0);
        int ops = 1_000_000;
        long start = System.nanoTime();
        for (int i = 0; i < ops; i++) {
            cms.add("key" + rnd.nextInt(5000), 1);
        }
        long duration = System.nanoTime() - start;
        System.out.printf("Added %d items in %.3f ms%n", ops, duration / 1_000_000.0);
    }
}
