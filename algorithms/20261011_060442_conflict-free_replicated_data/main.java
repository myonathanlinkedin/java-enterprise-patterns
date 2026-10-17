package crdt;

public class Main {
    public static void main(String[] args) {
        try {
            testSingleReplicaOperations();
            testConcurrentUpdatesAndMerge();
            testIdempotentMerge();
            testMergeWithMissingEntries();
            testLargeNumbers();
            System.out.println("All tests passed.");
        } catch (AssertionError e) {
            System.err.println("Test failure: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void testSingleReplicaOperations() {
        PNCounter c = new PNCounter("A");
        assert c.value() == 0 : "Initial value must be 0";

        c.increment(5);
        assert c.value() == 5 : "After +5, value should be 5";

        c.decrement(2);
        assert c.value() == 3 : "After -2, value should be 3";

        c.increment(0);
        c.decrement(0);
        assert c.value() == 3 : "Zero increments/decrements must not change value";
    }

    private static void testConcurrentUpdatesAndMerge() {
        PNCounter a = new PNCounter("A");
        PNCounter b = new PNCounter("B");

        // Simulate concurrent ops
        a.increment(4);
        a.decrement(1); // A = 3

        b.increment(2);
        b.decrement(5); // B = -3

        // Merge B into A
        a.merge(b);
        assert a.value() == 0 : "After merge, total should be 0 (3 + -3)";

        // Merge back to B (should be symmetric)
        b.merge(a);
        assert b.value() == 0 : "Symmetric merge must yield same value";

        // Ensure internal maps contain both replicas
        assert a.getP().size() == 2 && a.getN().size() == 2 : "Maps must have entries for both replicas";
    }

    private static void testIdempotentMerge() {
        PNCounter c1 = new PNCounter("X");
        c1.increment(10);
        c1.decrement(3);

        PNCounter c2 = c1.copy();
        c1.merge(c2); // merging identical state

        assert c1.value() == 7 : "Idempotent merge must not alter value";
        assert c1.equals(c2) : "State must remain equal after idempotent merge";
    }

    private static void testMergeWithMissingEntries() {
        PNCounter a = new PNCounter("A");
        PNCounter b = new PNCounter("B");

        a.increment(8); // only A has positive entry
        // B has only its own zero entries (created in constructor)

        a.merge(b);
        assert a.value() == 8 : "Merging with empty replica must keep original value";

        b.merge(a);
        assert b.value() == 8 : "After reciprocal merge, both should see the increment";
    }

    private static void testLargeNumbers() {
        PNCounter c = new PNCounter("L");
        long big = Long.MAX_VALUE / 2;
        c.increment(big);
        c.increment(big);
        c.decrement(1);
        long expected = big + big - 1;
        assert c.value() == expected : "Large number handling failed";

        // Merge with another replica that has a larger decrement
        PNCounter d = new PNCounter("M");
        d.decrement(big);
        c.merge(d);
        // After merge, negative part for replica M is max(0, big) = big
        long expectedAfterMerge = expected - big;
        assert c.value() == expectedAfterMerge : "Merge with large numbers incorrect";
    }
}
