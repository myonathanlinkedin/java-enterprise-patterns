import java.util.*;

public class Main {
    public static void main(String[] args) {
        int passed = 0, failed = 0;
        List<String> failures = new ArrayList<>();

        runTest("testSingleReplica", () -> {
            PNCounter c = new PNCounter("A");
            c.increment(5);
            c.decrement(2);
            assert c.getValue() == 3 : "Expected 3, got " + c.getValue();
        }, failures);

        runTest("testMerge", () -> {
            PNCounter a = new PNCounter("A");
            PNCounter b = new PNCounter("B");
            a.increment(3);
            b.increment(2);
            a.merge(b);
            assert a.getValue() == 5 : "Expected 5, got " + a.getValue();
        }, failures);

        runTest("testMergeWithDecrements", () -> {
            PNCounter a = new PNCounter("A");
            PNCounter b = new PNCounter("B");
            a.increment(5);
            a.decrement(2);
            b.increment(3);
            b.decrement(1);
            a.merge(b);
            assert a.getValue() == 5 : "Expected 5, got " + a.getValue();
        }, failures);

        runTest("testIdempotence", () -> {
            PNCounter a = new PNCounter("A");
            a.increment(4);
            a.merge(a);
            assert a.getValue() == 4 : "Expected 4, got " + a.getValue();
        }, failures);

        runTest("testCommutativity", () -> {
            PNCounter a = new PNCounter("A");
            PNCounter b = new PNCounter("B");
            a.increment(2);
            b.increment(3);
            PNCounter aCopy = new PNCounter("A");
            PNCounter bCopy = new PNCounter("B");
            aCopy.increment(2);
            bCopy.increment(3);
            a.merge(b);
            bCopy.merge(aCopy);
            assert a.getValue() == bCopy.getValue() : "Values differ: " + a.getValue() + " vs " + bCopy.getValue();
        }, failures);

        runTest("testAssociativity", () -> {
            PNCounter a = new PNCounter("A");
            PNCounter b = new PNCounter("B");
            PNCounter c = new PNCounter("C");
            a.increment(1);
            b.increment(2);
            c.increment(3);
            PNCounter ab = new PNCounter("AB");
            ab.merge(a);
            ab.merge(b);
            ab.merge(c);
            PNCounter bc = new PNCounter("BC");
            bc.merge(b);
            bc.merge(c);
            PNCounter a_bc = new PNCounter("A");
            a_bc.merge(a);
            a_bc.merge(bc);
            assert ab.getValue() == a_bc.getValue() : "Associativity failed: " + ab.getValue() + " vs " + a_bc.getValue();
        }, failures);

        runTest("testConcurrentUpdates", () -> {
            PNCounter a = new PNCounter("A");
            PNCounter b = new PNCounter("B");
            a.increment(5);
            b.decrement(3);
            a.merge(b);
            assert a.getValue() == 2 : "Expected 2, got " + a.getValue();
        }, failures);

        runTest("testLargeValues", () -> {
            PNCounter a = new PNCounter("A");
            long large = Long.MAX_VALUE / 2;
            a.increment(large);
            a.decrement(large / 2);
            assert a.getValue() == large / 2 : "Expected " + (large / 2) + ", got " + a.getValue();
        }, failures);

        runTest("benchmark", () -> {
            int ops = 100000;
            PNCounter a = new PNCounter("A");
            long start = System.nanoTime();
            for (int i = 0; i < ops; i++) a.increment(1);
            long mid = System.nanoTime();
            PNCounter b = new PNCounter("B");
            for (int i = 0; i < ops; i++) b.decrement(1);
            long mid2 = System.nanoTime();
            a.merge(b);
            long end = System.nanoTime();
            System.out.println("Increment time: " + (mid - start) / 1e6 + " ms");
            System.out.println("Decrement time: " + (mid2 - mid) / 1e6 + " ms");
            System.out.println("Merge time: " + (end - mid2) / 1e6 + " ms");
            assert a.getValue() == 0 : "Expected 0 after merge, got " + a.getValue();
        }, failures);

        passed = 9 - failures.size();
        failed = failures.size();
        System.out.println("\nTest results: " + passed + " passed, " + failed + " failed.");
        if (!failures.isEmpty()) {
            System.out.println("Failures:");
            failures.forEach(System.out::println);
        }
    }

    private static void runTest(String name, Runnable test, List<String> failures) {
        try {
            test.run();
            System.out.println("[PASS] " + name);
        } catch (AssertionError e) {
            System.out.println("[FAIL] " + name + ": " + e.getMessage());
            failures.add(name + ": " + e.getMessage());
        } catch (Exception e) {
            System.out.println("[ERROR] " + name + ": " + e);
            failures.add(name + ": " + e);
        }
    }
}
