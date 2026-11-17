package aether;

public class Main {
    public static void main(String[] args) {
        testBasicAllocation();
        testOutOfBoundsAccess();
        testDoubleFreeDetection();
        testFragmentationAndReuse();
        testCrossProgramIsolation();
        System.out.println("All AetherFramework tests passed.");
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static void testBasicAllocation() {
        AetherFramework framework = new AetherFramework(1024);
        AetherFramework.Program prog = framework.createProgram();

        AetherFramework.MemoryBlock block = prog.allocate(100);
        assertTrue(block.getSize() == 100, "Block size mismatch");

        byte[] payload = new byte[100];
        for (int i = 0; i < payload.length; i++) payload[i] = (byte) i;
        prog.write(block, 0, payload);

        byte[] readBack = prog.read(block, 0, 100);
        for (int i = 0; i < payload.length; i++) {
            assertTrue(readBack[i] == payload[i], "Data mismatch at index " + i);
        }

        prog.free(block);
    }

    private static void testOutOfBoundsAccess() {
        AetherFramework framework = new AetherFramework(256);
        AetherFramework.Program prog = framework.createProgram();
        AetherFramework.MemoryBlock block = prog.allocate(50);

        boolean caught = false;
        try {
            prog.write(block, 45, new byte[10]); // exceeds block size
        } catch (IndexOutOfBoundsException e) {
            caught = true;
        }
        assertTrue(caught, "Expected IndexOutOfBoundsException on write overflow");

        caught = false;
        try {
            prog.read(block, -1, 5);
        } catch (IndexOutOfBoundsException e) {
            caught = true;
        }
        assertTrue(caught, "Expected IndexOutOfBoundsException on negative offset");

        prog.free(block);
    }

    private static void testDoubleFreeDetection() {
        AetherFramework framework = new AetherFramework(512);
        AetherFramework.Program prog = framework.createProgram();
        AetherFramework.MemoryBlock block = prog.allocate(64);
        prog.free(block);

        boolean caught = false;
        try {
            prog.free(block);
        } catch (IllegalArgumentException e) {
            caught = true;
        }
        assertTrue(caught, "Expected IllegalArgumentException on double free");
    }

    private static void testFragmentationAndReuse() {
        AetherFramework framework = new AetherFramework(200);
        AetherFramework.Program prog = framework.createProgram();

        AetherFramework.MemoryBlock a = prog.allocate(50);
        AetherFramework.MemoryBlock b = prog.allocate(30);
        AetherFramework.MemoryBlock c = prog.allocate(40);
        prog.free(b); // create a hole of 30 bytes

        // Allocate a block that fits exactly into the freed hole
        AetherFramework.MemoryBlock d = prog.allocate(30);
        assertTrue(d.getStart() == b.getStart(), "Fragment reuse failed");

        prog.free(a);
        prog.free(c);
        prog.free(d);
        // After freeing all, a new large allocation should succeed
        AetherFramework.MemoryBlock large = prog.allocate(180);
        assertTrue(large.getSize() == 180, "Large allocation after defragmentation failed");
        prog.free(large);
    }

    private static void testCrossProgramIsolation() {
        AetherFramework framework = new AetherFramework(1024);
        AetherFramework.Program p1 = framework.createProgram();
        AetherFramework.Program p2 = framework.createProgram();

        AetherFramework.MemoryBlock block1 = p1.allocate(100);
        AetherFramework.MemoryBlock block2 = p2.allocate(100);

        // Ensure programs cannot access each other's blocks
        boolean caught = false;
        try {
            p1.write(block2, 0, new byte[10]);
        } catch (IllegalArgumentException e) {
            caught = true;
        }
        assertTrue(caught, "Program should not write to another's block");

        caught = false;
        try {
            p2.read(block1, 0, 10);
        } catch (IllegalArgumentException e) {
            caught = true;
        }
        assertTrue(caught, "Program should not read from another's block");

        p1.free(block1);
        p2.free(block2);
    }
}
