public class Main {
    private static final double RELATIVE_TOLERANCE = 0.15; // 15 % acceptable error

    public static void main(String[] args) {
        testEmptySketch();
        testDistinctElements();
        testDuplicateElements();
        testMergeSketches();
        System.out.println("All HyperLogLog tests passed.");
    }

    private static void assertClose(double estimated, double actual, double tolerance) {
        double relError = Math.abs(estimated - actual) / actual;
        if (relError > tolerance) {
            throw new AssertionError(
                String.format("Estimate %.2f differs from actual %d by %.2f%% (tolerance %.2f%%)",
                              estimated, (int) actual, relError * 100, tolerance * 100));
        }
    }

    private static void testEmptySketch() {
        HyperLogLog hll = new HyperLogLog(4); // m = 16
        double est = hll.estimate();
        if (Math.abs(est) > 1e-9) {
            throw new AssertionError("Empty sketch should estimate 0, got " + est);
        }
    }

    private static void testDistinctElements() {
        int precision = 10; // m = 1024
        HyperLogLog hll = new HyperLogLog(precision);
        int n = 1000;
        for (int i = 0; i < n; i++) {
            hll.add(i); // distinct integers
        }
        double est = hll.estimate();
        assertClose(est, n, RELATIVE_TOLERANCE);
    }

    private static void testDuplicateElements() {
        HyperLogLog hll = new HyperLogLog(8); // m = 256
        int n = 500;
        for (int i = 0; i < n; i++) {
            hll.add(42); // same element many times
        }
        double est = hll.estimate();
        // With only one distinct element, estimate should be close to 1
        assertClose(est, 1, RELATIVE_TOLERANCE);
    }

    private static void testMergeSketches() {
        HyperLogLog h1 = new HyperLogLog(9); // m = 512
        HyperLogLog h2 = new HyperLogLog(9);
        int total = 1500;
        // First sketch gets first 800 distinct values
        for (int i = 0; i < 800; i++) {
            h1.add(i);
        }
        // Second sketch gets values 600..1499 (overlap of 200)
        for (int i = 600; i < 1500; i++) {
            h2.add(i);
        }
        h1.merge(h2);
        double est = h1.estimate();
        assertClose(est, total, RELATIVE_TOLERANCE);
    }
}
