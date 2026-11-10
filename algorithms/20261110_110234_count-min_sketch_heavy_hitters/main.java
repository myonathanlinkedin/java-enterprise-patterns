import core.CountMinSketch;
import core.HeavyHitters;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        testCountMinSketchBasic();
        testHeavyHittersDetection();
        System.out.println("All tests passed.");
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void testCountMinSketchBasic() {
        CountMinSketch cms = new CountMinSketch(5, 1000, 12345L);
        cms.add("apple", 5);
        cms.add("banana", 3);
        cms.add("apple", 2);
        long estApple = cms.estimate("apple");
        long estBanana = cms.estimate("banana");
        long estCherry = cms.estimate("cherry"); // never added

        // Exact counts are lower bounds of estimates
        assertTrue(estApple >= 7, "Apple estimate too low");
        assertTrue(estBanana >= 3, "Banana estimate too low");
        assertTrue(estCherry == 0, "Cherry should be zero");

        // Total count must match sum of inserts
        assertTrue(cms.getTotalCount() == 10, "Total count mismatch");
    }

    private static void testHeavyHittersDetection() {
        // Parameters chosen to give reasonable error bounds
        HeavyHitters hh = new HeavyHitters(4, 500, 10, 98765L);
        // Simulate a stream with a few heavy items
        String[] heavy = {"A", "B", "C"};
        for (int i = 0; i < 1000; i++) {
            hh.add(heavy[i % heavy.length], 1);
        }
        // Add many light items
        for (int i = 0; i < 5000; i++) {
            hh.add("light-" + i, 1);
        }

        long threshold = 900; // items appearing at least 900 times should be reported
        Set<Object> hitters = hh.getHeavyHitters(threshold);
        assertTrue(hitters.contains("A"), "Heavy hitter A missing");
        assertTrue(hitters.contains("B"), "Heavy hitter B missing");
        assertTrue(hitters.contains("C"), "Heavy hitter C missing");
        // No light item should be reported
        for (Object obj : hitters) {
            assertTrue(obj instanceof String && ((String) obj).startsWith("light") == false,
                    "False positive heavy hitter: " + obj);
        }

        // Verify that estimate for a heavy item respects error bound
        long estA = hh.getSketch().estimate("A");
        long trueA = 1000 / heavy.length;
        long error = estA - trueA;
        // Theoretical error ≤ totalCount / width
        long bound = hh.getSketch().getTotalCount() / 500 + 1;
        assertTrue(error <= bound, "Error exceeds theoretical bound");
    }
}
