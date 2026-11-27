public class Main {
    public static void main(String[] args) {
        runTests();
        System.out.println("All tests passed.");
    }

    private static void runTests() {
        testBasicAllocation();
        testAllocationFailure();
        testFreeAndCoalesce();
        testFullPoolAllocation();
        testRandomPattern();
        testInvalidParameters();
    }

    private static void assertTrue(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
    }

    private static void testBasicAllocation() {
        BuddyAllocator ba = new BuddyAllocator(64);
        int a = ba.allocate(8);
        assertTrue(a == 0, "First 8-byte block should be at 0");
        int b = ba.allocate(8);
        assertTrue(b == 8, "Second 8-byte block should be at 8");
        assertTrue(ba.freeCount(3) == 0, "No free 8-byte blocks left");
    }

    private static void testAllocationFailure() {
        BuddyAllocator ba = new BuddyAllocator(32);
        int a = ba.allocate(64);
        assertTrue(a == -1, "Allocation larger than pool must fail");
        int b = ba.allocate(0);
        assertTrue(b == -1, "Zero-size allocation must fail");
    }

    private static void testFreeAndCoalesce() {
        BuddyAllocator ba = new BuddyAllocator(64);
        int a = ba.allocate(8); // 0
        int b = ba.allocate(8); // 8
        ba.free(a, 8);
        ba.free(b, 8);
        // After freeing both 8-byte buddies, they should coalesce into a 16-byte block
        assertTrue(ba.freeCount(4) == 1, "One 16-byte block expected after coalescing");
        int c = ba.allocate(16);
        assertTrue(c == 0, "Coalesced 16-byte block should start at 0");
    }

    private static void testFullPoolAllocation() {
        BuddyAllocator ba = new BuddyAllocator(128);
        int a = ba.allocate(64);
        int b = ba.allocate(64);
        assertTrue(a == 0 && b == 64, "Two 64-byte blocks should fill the pool");
        int c = ba.allocate(8);
        assertTrue(c == -1, "No space left, allocation must fail");
        ba.free(a, 64);
        ba.free(b, 64);
        int d = ba.allocate(128);
        assertTrue(d == 0, "After freeing, full 128-byte allocation should succeed");
    }

    private static void testRandomPattern() {
        BuddyAllocator ba = new BuddyAllocator(256);
        int a = ba.allocate(10); // rounds to 16, offset 0
        int b = ba.allocate(20); // rounds to 32, offset 16
        ba.free(a, 10); // free 16-byte block at 0
        int c = ba.allocate(8); // rounds to 8, should get offset 0
        assertTrue(c == 0, "8-byte allocation should reuse freed 16-byte block's first half");
        // Verify that the remaining 8 bytes of that original 16-byte block are free
        assertTrue(ba.freeCount(3) == 1, "One 8-byte free block expected");
    }

    private static void testInvalidParameters() {
        try {
            new BuddyAllocator(30);
            throw new AssertionError("Constructor must reject non-power-of-two size");
        } catch (IllegalArgumentException ignored) {
        }

        BuddyAllocator ba = new BuddyAllocator(64);
        try {
            ba.free(-1, 8);
            throw new AssertionError("Free with negative offset must fail");
        } catch (IllegalArgumentException ignored) {
        }

        try {
            ba.free(0, -5);
            throw new AssertionError("Free with negative size must fail");
        } catch (IllegalArgumentException ignored) {
        }
    }
}
