import java.util.List;

/**
 * Simple test harness for BytecodeVM.
 * All tests are executed from the main method; failures throw AssertionError.
 */
public final class Main {

    private static final BytecodeVM VM = new BytecodeVM();

    public static void main(final String[] args) {
        testSimpleArithmetic();
        testDivisionByZero();
        testStackUnderflow();
        testUnknownOpcode();
        testPrintAndHalt();
        System.out.println("All BytecodeVM tests passed.");
    }

    /** Helper to compare expected and actual output lists. */
    private static void assertEquals(final List<Integer> expected, final List<Integer> actual) {
        if (!expected.equals(actual)) {
            throw new AssertionError("Expected " + expected + " but got " + actual);
        }
    }

    /** Test basic arithmetic: ((2 + 3) * 4) - 5 = 15 */
    private static void testSimpleArithmetic() {
        int[] prog = {
            BytecodeVM.Opcode.PUSH.code(), 2,
            BytecodeVM.Opcode.PUSH.code(), 3,
            BytecodeVM.Opcode.ADD.code(),
            BytecodeVM.Opcode.PUSH.code(), 4,
            BytecodeVM.Opcode.MUL.code(),
            BytecodeVM.Opcode.PUSH.code(), 5,
            BytecodeVM.Opcode.SUB.code(),
            BytecodeVM.Opcode.PRINT.code(),
            BytecodeVM.Opcode.HALT.code()
        };
        List<Integer> out = VM.execute(prog);
        assertEquals(List.of(15), out);
    }

    /** Verify division by zero throws ArithmeticException. */
    private static void testDivisionByZero() {
        int[] prog = {
            BytecodeVM.Opcode.PUSH.code(), 10,
            BytecodeVM.Opcode.PUSH.code(), 0,
            BytecodeVM.Opcode.DIV.code(),
            BytecodeVM.Opcode.HALT.code()
        };
        boolean caught = false;
        try {
            VM.execute(prog);
        } catch (ArithmeticException e) {
            caught = true;
        }
        if (!caught) throw new AssertionError("Division by zero not detected");
    }

    /** Verify stack underflow detection. */
    private static void testStackUnderflow() {
        int[] prog = {
            BytecodeVM.Opcode.ADD.code(),
            BytecodeVM.Opcode.HALT.code()
        };
        boolean caught = false;
        try {
            VM.execute(prog);
        } catch (IllegalStateException e) {
            caught = true;
        }
        if (!caught) throw new AssertionError("Stack underflow not detected");
    }

    /** Verify unknown opcode handling. */
    private static void testUnknownOpcode() {
        int[] prog = { 99, BytecodeVM.Opcode.HALT.code() }; // 99 is not a defined opcode
        boolean caught = false;
        try {
            VM.execute(prog);
        } catch (IllegalArgumentException e) {
            caught = true;
        }
        if (!caught) throw new AssertionError("Unknown opcode not detected");
    }

    /** Test multiple PRINTs and implicit halt (no explicit HALT). */
    private static void testPrintAndHalt() {
        int[] prog = {
            BytecodeVM.Opcode.PUSH.code(), 7,
            BytecodeVM.Opcode.PRINT.code(),
            BytecodeVM.Opcode.PUSH.code(), 8,
            BytecodeVM.Opcode.PRINT.code()
            // No HALT; VM should stop at end of array
        };
        List<Integer> out = VM.execute(prog);
        assertEquals(List.of(7, 8), out);
    }
}
