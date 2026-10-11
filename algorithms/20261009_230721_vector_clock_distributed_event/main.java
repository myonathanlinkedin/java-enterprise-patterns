package vectorclock;

public class Main {
    public static void main(String[] args) {
        // Enable assertions programmatically for demonstration purposes.
        // In production, run with -ea.
        ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);

        // Test 1: Increment behavior
        VectorClock c1 = new VectorClock();
        c1 = Engine.increment(c1, "P1");
        assert c1.get("P1") == 1 : "P1 should be 1 after first increment";
        c1 = Engine.increment(c1, "P1");
        assert c1.get("P1") == 2 : "P1 should be 2 after second increment";

        // Test 2: Independent processes
        VectorClock c2 = Engine.increment(new VectorClock(), "P2");
        assert c2.get("P2") == 1 : "P2 should be 1";
        assert c2.get("P1") == 0 : "P1 absent => 0";

        // Test 3: Merge (max) semantics
        VectorClock merged = Engine.merge(c1, c2);
        assert merged.get("P1") == 2 : "Merged P1 should be 2 (max)";
        assert merged.get("P2") == 1 : "Merged P2 should be 1";

        // Test 4: Comparison - BEFORE
        VectorClock before = new VectorClock()
                .increment("A")
                .increment("B"); // A=1, B=1
        VectorClock after = before.increment("A"); // A=2, B=1
        assert Engine.compare(before, after) == VectorClock.Relation.BEFORE
                : "before should be BEFORE after";

        // Test 5: Comparison - AFTER
        assert Engine.compare(after, before) == VectorClock.Relation.AFTER
                : "after should be AFTER before";

        // Test 6: Comparison - EQUAL
        VectorClock copy = new VectorClock()
                .increment("X")
                .increment("Y");
        VectorClock copy2 = new VectorClock()
                .increment("Y")
                .increment("X");
        assert Engine.compare(copy, copy2) == VectorClock.Relation.EQUAL
                : "Identical clocks must be EQUAL";

        // Test 7: Comparison - CONCURRENT
        VectorClock cA = new VectorClock().increment("A"); // A=1
        VectorClock cB = new VectorClock().increment("B"); // B=1
        assert Engine.compare(cA, cB) == VectorClock.Relation.CONCURRENT
                : "Disjoint clocks are CONCURRENT";

        // Test 8: Edge case - empty vs non‑empty
        VectorClock empty = new VectorClock();
        VectorClock nonEmpty = Engine.increment(empty, "Z");
        assert Engine.compare(empty, nonEmpty) == VectorClock.Relation.BEFORE
                : "Empty should be BEFORE any non‑empty clock";

        // Test 9: Merge with null arguments
        VectorClock nullMerged = Engine.merge(null, cA);
        assert nullMerged.equals(cA) : "Merging null with a clock yields the non‑null clock";

        // Test 10: Compare with null arguments
        assert Engine.compare(null, null) == VectorClock.Relation.EQUAL
                : "Two null clocks are considered EQUAL";

        // Demo output
        System.out.println("All vector clock unit tests passed.");
        System.out.println("Demo merged clock: " + merged);
    }
}
