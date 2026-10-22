import java.util.Random;

public class main {

    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        System.out.println("HyperLogLog Cardinality Estimation - Unit Tests");
        System.out.println("================================================");

        testBasicCardinality();
        testEmptyEstimator();
        testSingleElement();
        testDuplicateElements();
        testLargeCardinality();
        testPrecisionEffect();
        testRelativeError();
        testDeterminism();
        testEdgeCases();

        System.out.println("\n================================================");
        System.out.println("Results: " + testsPassed + " passed, " + testsFailed + " failed");
        if (testsFailed > 0) {
            System.exit(1);
        }
    }

    private static void testBasicCardinality() {
        core hll = new core(14);
        int n = 10000;
        Random rand = new Random(42);
        for (int i = 0; i < n; i++) {
            hll.add(rand.nextLong());
        }
        double estimate = hll.estimate();
        double error = Math.abs(estimate - n) / n;
        assertWithMessage("Basic cardinality (n=10000)", error < 0.05, 
            "Estimate: " + estimate + ", Expected: " + n + ", Error: " + (error * 100) + "%");
    }

    private static void testEmptyEstimator() {
        core hll = new core(14);
        double estimate = hll.estimate();
        assertWithMessage("Empty estimator", estimate < 1.0, 
            "Estimate for empty set: " + estimate);
    }

    private static void testSingleElement() {
        core hll = new core(14);
        hll.add(12345L);
        double estimate = hll.estimate();
        assertWithMessage("Single element", estimate >= 1.0 && estimate < 5.0, 
            "Estimate for single element: " + estimate);
    }

    private static void testDuplicateElements() {
        core hll = new core(14);
        for (int i = 0; i < 1000; i++) {
            hll.add(42L);
        }
        double estimate = hll.estimate();
        assertWithMessage("Duplicate elements", estimate >= 1.0 && estimate < 10.0, 
            "Estimate for 1000 duplicates of same element: " + estimate);
    }

    private static void testLargeCardinality() {
        core hll = new core(14);
        int n = 1000000;
        Random rand = new Random(123);
        for (int i = 0; i < n; i++) {
            hll.add(rand.nextLong());
        }
        double estimate = hll.estimate();
        double error = Math.abs(estimate - n) / n;
        assertWithMessage("Large cardinality (n=1M)", error < 0.05, 
            "Estimate: " + estimate + ", Expected: " + n + ", Error: " + (error * 100) + "%");
    }

    private static void testPrecisionEffect() {
        // Higher precision should give better estimates
        int n = 50000;
        Random rand = new Random(999);
        long[] elements = new long[n];
        for (int i = 0; i < n; i++) {
            elements[i] = rand.nextLong();
        }

        core hllLow = new core(10);
        core hllHigh = new core(16);
        for (long e : elements) {
            hllLow.add(e);
            hllHigh.add(e);
        }

        double errorLow = Math.abs(hllLow.estimate() - n) / n;
        double errorHigh = Math.abs(hllHigh.estimate() - n) / n;
        assertWithMessage("Precision effect", errorHigh <= errorLow + 0.02, 
            "Low precision error: " + (errorLow * 100) + "%, High precision error: " + (errorHigh * 100) + "%");
    }

    private static void testRelativeError() {
        core hll = new core(14);
        double rse = hll.relativeStandardError();
        double expected = 1.04 / Math.sqrt(1 << 14);
        assertWithMessage("Relative standard error", Math.abs(rse - expected) < 1e-10, 
            "RSE: " + rse + ", Expected: " + expected);
    }

    private static void testDeterminism() {
        core hll1 = new core(14);
        core hll2 = new core(14);
        Random rand = new Random(777);
        for (int i = 0; i < 1000; i++) {
            long e = rand.nextLong();
            hll1.add(e);
            hll2.add(e);
        }
        assertWithMessage("Determinism", hll1.estimate() == hll2.estimate(), 
            "Estimates differ: " + hll1.estimate() + " vs " + hll2.estimate());
    }

    private static void testEdgeCases() {
        // Test minimum precision
        core hllMin = new core(4);
        hllMin.add(1L);
        double estMin = hllMin.estimate();
        assertWithMessage("Min precision", estMin > 0, "Estimate with p=4: " + estMin);

        // Test maximum precision
        core hllMax = new core(18);
        hllMax.add(1L);
        double estMax = hllMax.estimate();
        assertWithMessage("Max precision", estMax > 0, "Estimate with p=18: " + estMax);

        // Test invalid precision
        boolean threw = false;
        try {
            new core(3);
        } catch (IllegalArgumentException e) {
            threw = true;
        }
        assertWithMessage("Invalid precision rejection", threw, "Should throw for p=3");
    }

    private static void assertWithMessage(String testName, boolean condition, String detail) {
        if (condition) {
            testsPassed++;
            System.out.println("[PASS] " + testName);
        } else {
            testsFailed++;
            System.out.println("[FAIL] " + testName + " - " + detail);
        }
    }
}
