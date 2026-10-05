import java.util.*;

public class Main {

    private static void assertEqual(int[] expected, int[] actual) {
        if (expected == null && actual == null) return;
        if (expected == null || actual == null) throw new AssertionError("Expected " + Arrays.toString(expected) + " but got " + Arrays.toString(actual));
        if (expected.length != actual.length) throw new AssertionError("Expected length " + expected.length + " but got " + actual.length);
        for (int i = 0; i < expected.length; i++) {
            if (expected[i] != actual[i]) throw new AssertionError("Mismatch at index " + i + ": expected " + expected[i] + " but got " + actual[i]);
        }
    }

    private static int[] bruteForce(int[] a, int target) {
        int n = a.length;
        for (int mask = 0; mask < (1 << n); mask++) {
            long sum = 0;
            for (int i = 0; i < n; i++) if ((mask & (1 << i)) != 0) sum += a[i];
            if (sum == target) {
                int count = Integer.bitCount(mask);
                int[] res = new int[count];
                int pos = 0;
                for (int i = 0; i < n; i++) if ((mask & (1 << i)) != 0) res[pos++] = i + 1;
                return res;
            }
        }
        return null;
    }

    private static void runTests() {
        // Simple deterministic tests
        assertEqual(new int[]{4,5}, SubsetSumSolver.findSubset(new int[]{1,2,3,4,5}, 9));
        assertEqual(new int[]{1,3,5}, SubsetSumSolver.findSubset(new int[]{1,2,3,4,5}, 9));
        assertEqual(new int[0], SubsetSumSolver.findSubset(new int[]{1,2,3}, 0));
        assertEqual(null, SubsetSumSolver.findSubset(new int[]{5,10,12}, 1));

        // Random small tests against brute force
        Random rnd = new Random(42);
        for (int t = 0; t < 100; t++) {
            int n = rnd.nextInt(10) + 1;
            int[] a = new int[n];
            for (int i = 0; i < n; i++) a[i] = rnd.nextInt(20) + 1;
            int target = rnd.nextInt(100);
            int[] expected = bruteForce(a, target);
            int[] actual = SubsetSumSolver.findSubset(a, target);
            if (expected == null) {
                if (actual != null) throw new AssertionError("Expected null but got " + Arrays.toString(actual));
            } else {
                // any valid subset is acceptable; verify sum
                long sum = 0;
                for (int idx : actual) sum += a[idx - 1];
                if (sum != target) throw new AssertionError("Sum mismatch: expected " + target + " but got " + sum);
            }
        }
        System.out.println("All tests passed.");
    }

    private static void benchmark() {
        Random rnd = new Random(12345);
        int n = 30;
        int[] a = new int[n];
        for (int i = 0; i < n; i++) a[i] = rnd.nextInt(1_000_000_000) + 1;
        int target = rnd.nextInt(1_000_000_000) + 1;
        long start = System.nanoTime();
        int[] res = SubsetSumSolver.findSubset(a, target);
        long end = System.nanoTime();
        System.out.printf("Benchmark: %d ms, result %s%n", (end - start) / 1_000_000, res == null ? "null" : Arrays.toString(res));
    }

    public static void main(String[] args) {
        runTests();
        benchmark();
    }
}
