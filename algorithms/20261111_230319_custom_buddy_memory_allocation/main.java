public class Main {
    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void testAllocationAndFree() {
        BuddyAllocator allocator = new BuddyAllocator(1024, 16);
        int a = allocator.allocate(100);
        assertTrue(a != -1, "Allocation failed");
        assertTrue(allocator.getAllocatedMemory() == 128, "Allocated size mismatch");
        assertTrue(allocator.getFreeMemory() == 1024 - 128, "Free memory mismatch");
        boolean freed = allocator.free(a, 128);
        assertTrue(freed, "Free failed");
        assertTrue(allocator.getAllocatedMemory() == 0, "Allocated memory after free");
        assertTrue(allocator.getFreeMemory() == 1024, "Free memory after free");
    }

    private static void testFragmentation() {
        BuddyAllocator allocator = new BuddyAllocator(64, 8);
        int a = allocator.allocate(8);
        int b = allocator.allocate(8);
        int c = allocator.allocate(8);
        int d = allocator.allocate(8);
        assertTrue(a != -1 && b != -1 && c != -1 && d != -1, "Allocation failed");
        assertTrue(allocator.getAllocatedMemory() == 32, "Allocated memory mismatch");
        boolean freed = allocator.free(b, 8);
        assertTrue(freed, "Free failed");
        int e = allocator.allocate(16);
        assertTrue(e != -1, "Allocation of 16 failed");
        assertTrue(allocator.getAllocatedMemory() == 48, "Allocated memory after 16 allocation");
    }

    private static void testMerge() {
        BuddyAllocator allocator = new BuddyAllocator(128, 8);
        int a = allocator.allocate(32);
        int b = allocator.allocate(32);
        assertTrue(a != -1 && b != -1, "Allocation failed");
        boolean freedA = allocator.free(a, 32);
        boolean freedB = allocator.free(b, 32);
        assertTrue(freedA && freedB, "Free failed");
        int c = allocator.allocate(64);
        assertTrue(c != -1, "Allocation of 64 failed");
        assertTrue(allocator.getAllocatedMemory() == 64, "Allocated memory after merge");
    }

    private static void testEdgeCases() {
        BuddyAllocator allocator = new BuddyAllocator(256, 16);
        int a = allocator.allocate(256);
        assertTrue(a == 0, "Full allocation failed");
        int b = allocator.allocate(16);
        assertTrue(b == -1, "Allocation should fail when memory full");
        boolean freed = allocator.free(a, 256);
        assertTrue(freed, "Free full block failed");
        try {
            allocator.allocate(0);
            assertTrue(false, "Zero size allocation should throw");
        } catch (IllegalArgumentException e) {}
    }

    public static void main(String[] args) {
        testAllocationAndFree();
        testFragmentation();
        testMerge();
        testEdgeCases();
        System.out.println("All tests passed");
    }
}
