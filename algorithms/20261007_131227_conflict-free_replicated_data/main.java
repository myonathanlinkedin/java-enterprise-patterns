package crdt;

public class Main {
    public static void main(String[] args) {
        runTests();
        System.out.println("All tests passed.");
    }

    private static void runTests() {
        testIncrementDecrement();
        testMerge();
        testIdempotence();
        testCommutativity();
    }

    private static void testIncrementDecrement() {
        PNCounter c = new PNCounter("A");
        assert c.value() == 0 : "Initial value should be 0";
        c.increment();
        assert c.value() == 1 : "After increment, value should be 1";
        c.decrement();
        assert c.value() == 0 : "After decrement, value should be 0";
        c.decrement();
        assert c.value() == -1 : "After decrement, value should be -1";
    }

    private static void testMerge() {
        PNCounter a = new PNCounter("A");
        PNCounter b = new PNCounter("B");
        a.increment(); // A: +1
        a.increment(); // A: +2
        b.decrement(); // B: -1
        b.decrement(); // B: -2
        a.merge(b);
        assert a.value() == -2 : "Merged value should be -2";
        // Merge again should be idempotent
        a.merge(b);
        assert a.value() == -2 : "Merge idempotent";
    }

    private static void testIdempotence() {
        PNCounter a = new PNCounter("A");
        a.increment();
        PNCounter b = new PNCounter("B");
        b.increment();
        a.merge(b);
        PNCounter aCopy = new PNCounter("A");
        aCopy.merge(a);
        a.merge(aCopy);
        assert a.value() == aCopy.value() : "Merge with copy should not change value";
    }

    private static void testCommutativity() {
        PNCounter a = new PNCounter("A");
        PNCounter b = new PNCounter("B");
        a.increment(); // A: +1
        b.increment(); // B: +1
        a.merge(b);
        long afterAB = a.value();
        PNCounter a2 = new PNCounter("A");
        PNCounter b2 = new PNCounter("B");
        b2.increment(); // B: +1
        a2.increment(); // A: +1
        b2.merge(a2);
        long afterBA = b2.value();
        assert afterAB == afterBA : "Merge should be commutative";
    }
}
