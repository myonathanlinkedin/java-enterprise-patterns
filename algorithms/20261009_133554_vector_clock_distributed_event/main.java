public class Main {

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError("Assertion failed: " + message);
        }
    }

    private static void testTickAndGet() {
        VectorClock vc = new VectorClock();
        vc.tick(1);
        vc.tick(1);
        vc.tick(2);
        assertTrue(vc.get(1) == 2, "Node 1 should have count 2");
        assertTrue(vc.get(2) == 1, "Node 2 should have count 1");
        assertTrue(vc.get(3) == 0, "Missing node should return 0");
    }

    private static void testMerge() {
        VectorClock a = new VectorClock();
        a.tick(1); // [1->1]
        a.tick(2); // [2->1]

        VectorClock b = new VectorClock();
        b.tick(2); // [2->1]
        b.tick(2); // [2->2]
        b.tick(3); // [3->1]

        VectorClock merged = new VectorClock(a);
        merged.merge(b); // expect max per node

        assertTrue(merged.get(1) == 1, "Merged node 1 should be 1");
        assertTrue(merged.get(2) == 2, "Merged node 2 should be 2");
        assertTrue(merged.get(3) == 1, "Merged node 3 should be 1");

        // Ensure original clocks unchanged
        assertTrue(a.get(2) == 1, "Original A unchanged");
        assertTrue(b.get(2) == 2, "Original B unchanged");
    }

    private static void testCompareBefore() {
        VectorClock a = new VectorClock();
        a.tick(1); // [1->1]

        VectorClock b = new VectorClock(a);
        b.tick(2); // [1->1,2->1]

        assertTrue(a.compare(b) == VectorClock.Relation.BEFORE,
                "A should be before B");
        assertTrue(b.compare(a) == VectorClock.Relation.AFTER,
                "B should be after A");
    }

    private static void testCompareEqual() {
        VectorClock a = new VectorClock();
        a.tick(1);
        a.tick(2);

        VectorClock b = new VectorClock(a);
        assertTrue(a.compare(b) == VectorClock.Relation.EQUAL,
                "Identical clocks must be equal");
    }

    private static void testCompareConcurrent() {
        VectorClock a = new VectorClock();
        a.tick(1); // [1->1]

        VectorClock b = new VectorClock();
        b.tick(2); // [2->1]

        assertTrue(a.compare(b) == VectorClock.Relation.CONCURRENT,
                "A and B are concurrent");
        assertTrue(b.compare(a) == VectorClock.Relation.CONCURRENT,
                "B and A are concurrent");
    }

    private static void testEdgeCases() {
        VectorClock empty = new VectorClock();
        VectorClock single = new VectorClock();
        single.tick(5);

        assertTrue(empty.compare(single) == VectorClock.Relation.BEFORE,
                "Empty before non-empty");
        assertTrue(single.compare(empty) == VectorClock.Relation.AFTER,
                "Non-empty after empty");

        VectorClock copy = new VectorClock(single);
        copy.tick(5); // now count 2
        assertTrue(single.compare(copy) == VectorClock.Relation.BEFORE,
                "Original before incremented copy");
    }

    public static void main(String[] args) {
        try {
            testTickAndGet();
            testMerge();
            testCompareBefore();
            testCompareEqual();
            testCompareConcurrent();
            testEdgeCases();

            System.out.println("All VectorClock tests passed.");
        } catch (AssertionError e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }
}
