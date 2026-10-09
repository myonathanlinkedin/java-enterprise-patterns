public class Main {
    public static void main(String[] args) {
        testIncrement();
        testMerge();
        testCompareEquality();
        testCompareBeforeAfter();
        testCompareConcurrent();
        testEdgeMissingPid();
        System.out.println("All VectorClock tests passed.");
    }

    private static void assertTrue(boolean condition, String msg) {
        if (!condition) throw new AssertionError(msg);
    }

    private static void testIncrement() {
        VectorClock vc = new VectorClock();
        vc.increment("A");
        vc.increment("A");
        vc.increment("B");
        assertTrue(vc.snapshot().get("A") == 2, "A should be 2");
        assertTrue(vc.snapshot().get("B") == 1, "B should be 1");
    }

    private static void testMerge() {
        VectorClock vc1 = new VectorClock();
        vc1.increment("A"); // A:1
        vc1.increment("B"); // B:1

        VectorClock vc2 = new VectorClock();
        vc2.increment("A"); // A:1
        vc2.increment("A"); // A:2
        vc2.increment("C"); // C:1

        vc1.merge(vc2);
        assertTrue(vc1.snapshot().get("A") == 2, "A should be max 2");
        assertTrue(vc1.snapshot().get("B") == 1, "B should stay 1");
        assertTrue(vc1.snapshot().get("C") == 1, "C should be added as 1");
    }

    private static void testCompareEquality() {
        VectorClock vc1 = new VectorClock();
        vc1.increment("X");
        vc1.increment("Y");

        VectorClock vc2 = new VectorClock(vc1);
        assertTrue(vc1.compare(vc2) == VectorClock.Relation.EQUAL,
                "Identical clocks must be EQUAL");
    }

    private static void testCompareBeforeAfter() {
        VectorClock before = new VectorClock();
        before.increment("P"); // P:1

        VectorClock after = new VectorClock(before);
        after.increment("Q"); // Q:1

        assertTrue(before.compare(after) == VectorClock.Relation.BEFORE,
                "before should be BEFORE after");
        assertTrue(after.compare(before) == VectorClock.Relation.AFTER,
                "after should be AFTER before");
    }

    private static void testCompareConcurrent() {
        VectorClock vc1 = new VectorClock();
        vc1.increment("A"); // A:1

        VectorClock vc2 = new VectorClock();
        vc2.increment("B"); // B:1

        assertTrue(vc1.compare(vc2) == VectorClock.Relation.CONCURRENT,
                "Disjoint clocks must be CONCURRENT");
    }

    private static void testEdgeMissingPid() {
        VectorClock vc1 = new VectorClock();
        vc1.increment("A"); // A:1

        VectorClock vc2 = new VectorClock();
        // vc2 has no entry for "A"
        assertTrue(vc1.compare(vc2) == VectorClock.Relation.AFTER,
                "vc1 with extra pid should be AFTER vc2");
        assertTrue(vc2.compare(vc1) == VectorClock.Relation.BEFORE,
                "vc2 missing pid should be BEFORE vc1");
    }
}
