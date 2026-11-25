import java.util.ArrayList;
import java.util.List;

/**
 * A minimal stack‑based bytecode virtual machine.
 * Supported opcodes:
 *   PUSH n   – push integer n onto the stack
 *   POP      – discard top of stack
 *   ADD      – pop a, b; push a + b
 *   SUB      – pop a, b; push a - b
 *   MUL      – pop a, b; push a * b
 *   DIV      – pop a, b; push a / b (integer division)
 *   PRINT    – pop value and record it as output
 *   HALT     – stop execution
 *
 * Bytecode is encoded as an int array. Each instruction starts with an
 * opcode value; opcodes that require an operand are followed by that operand.
 */
public final class BytecodeVM {

    /** Opcode enumeration with explicit integer codes. */
    public enum Opcode {
        PUSH(0), POP(1), ADD(2), SUB(3), MUL(4), DIV(5), PRINT(6), HALT(7);

        private final int code;
        Opcode(int code) { this.code = code; }
        public int code() { return code; }

        /** Retrieve enum from integer code; throws if unknown. */
        public static Opcode fromCode(int code) {
            for (Opcode op : values()) {
                if (op.code == code) return op;
            }
            throw new IllegalArgumentException("Invalid opcode: " + code);
        }
    }

    /** Maximum stack depth – can be tuned as needed. */
    private static final int MAX_STACK = 1024;

    private final int[] stack = new int[MAX_STACK];
    private int sp = 0;                     // stack pointer (next free slot)
    private final List<Integer> output = new ArrayList<>();

    /**
     * Executes a program and returns the list of values produced by PRINT.
     *
     * @param program bytecode array
     * @return immutable list of printed integers
     */
    public List<Integer> execute(final int[] program) {
        sp = 0;
        output.clear();
        int pc = 0; // program counter

        while (pc < program.length) {
            Opcode op = Opcode.fromCode(program[pc++]);
            switch (op) {
                case PUSH -> {
                    ensureStackSpace(1);
                    stack[sp++] = program[pc++];
                }
                case POP -> {
                    ensureStackDepth(1);
                    sp--;
                }
                case ADD -> binaryOp((a, b) -> a + b);
                case SUB -> binaryOp((a, b) -> a - b);
                case MUL -> binaryOp((a, b) -> a * b);
                case DIV -> binaryOp((a, b) -> {
                    if (b == 0) throw new ArithmeticException("Division by zero");
                    return a / b;
                });
                case PRINT -> {
                    ensureStackDepth(1);
                    int val = stack[--sp];
                    output.add(val);
                }
                case HALT -> {
                    return List.copyOf(output);
                }
                default -> throw new IllegalStateException("Unreachable opcode");
            }
        }
        // Implicit halt if program runs out of bytes
        return List.copyOf(output);
    }

    /** Functional interface for binary operations on stack values. */
    @FunctionalInterface
    private interface IntBinaryOperator {
        int apply(int a, int b);
    }

    /** Pops two values, applies op, pushes result. */
    private void binaryOp(final IntBinaryOperator op) {
        ensureStackDepth(2);
        int b = stack[--sp];
        int a = stack[--sp];
        int res = op.apply(a, b);
        ensureStackSpace(1);
        stack[sp++] = res;
    }

    /** Ensures at least {@code n} values are present on the stack. */
    private void ensureStackDepth(final int n) {
        if (sp < n) {
            throw new IllegalStateException("Stack underflow: need " + n + " values, have " + sp);
        }
    }

    /** Ensures there is room for {@code n} additional values. */
    private void ensureStackSpace(final int n) {
        if (sp + n > MAX_STACK) {
            throw new IllegalStateException("Stack overflow: capacity " + MAX_STACK);
        }
    }
}
