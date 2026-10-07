import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.VectorMask;
import jdk.incubator.vector.VectorSpecies;

/**
 * Utility class providing SIMD‑accelerated operations on float arrays
 * using the Java Vector API (jdk.incubator.vector).
 */
public final class SimdVectorOps {
    // Use the preferred species for the running platform (e.g., 256‑bit on x86).
    private static final VectorSpecies<Float> SPECIES = FloatVector.SPECIES_PREFERRED;

    private SimdVectorOps() {
        // Prevent instantiation.
    }

    /**
     * Element‑wise addition of two float arrays.
     *
     * @param a first operand (must be non‑null)
     * @param b second operand (must be same length as {@code a})
     * @return a new float array containing a[i] + b[i]
     */
    public static float[] add(float[] a, float[] b) {
        checkLengths(a, b);
        float[] result = new float[a.length];
        int i = 0;
        int upperBound = SPECIES.loopBound(a.length);
        for (; i < upperBound; i += SPECIES.length()) {
            var va = FloatVector.fromArray(SPECIES, a, i);
            var vb = FloatVector.fromArray(SPECIES, b, i);
            var vr = va.add(vb);
            vr.intoArray(result, i);
        }
        // Tail processing for remaining elements.
        if (i < a.length) {
            VectorMask<Float> mask = SPECIES.indexInRange(i, a.length);
            var va = FloatVector.fromArray(SPECIES, a, i, mask);
            var vb = FloatVector.fromArray(SPECIES, b, i, mask);
            var vr = va.add(vb);
            vr.intoArray(result, i, mask);
        }
        return result;
    }

    /**
     * Element‑wise multiplication of two float arrays.
     *
     * @param a first operand
     * @param b second operand
     * @return a new float array containing a[i] * b[i]
     */
    public static float[] multiply(float[] a, float[] b) {
        checkLengths(a, b);
        float[] result = new float[a.length];
        int i = 0;
        int upperBound = SPECIES.loopBound(a.length);
        for (; i < upperBound; i += SPECIES.length()) {
            var va = FloatVector.fromArray(SPECIES, a, i);
            var vb = FloatVector.fromArray(SPECIES, b, i);
            var vr = va.mul(vb);
            vr.intoArray(result, i);
        }
        if (i < a.length) {
            VectorMask<Float> mask = SPECIES.indexInRange(i, a.length);
            var va = FloatVector.fromArray(SPECIES, a, i, mask);
            var vb = FloatVector.fromArray(SPECIES, b, i, mask);
            var vr = va.mul(vb);
            vr.intoArray(result, i, mask);
        }
        return result;
    }

    /**
     * Computes the dot product of two float vectors.
     *
     * @param a first operand
     * @param b second operand
     * @return Σ a[i] * b[i]
     */
    public static float dotProduct(float[] a, float[] b) {
        checkLengths(a, b);
        var acc = FloatVector.zero(SPECIES);
        int i = 0;
        int upperBound = SPECIES.loopBound(a.length);
        for (; i < upperBound; i += SPECIES.length()) {
            var va = FloatVector.fromArray(SPECIES, a, i);
            var vb = FloatVector.fromArray(SPECIES, b, i);
            acc = va.fma(vb, acc); // acc += va * vb
        }
        float sum = acc.reduceLanes(VectorOperators.ADD);
        // Tail reduction
        if (i < a.length) {
            VectorMask<Float> mask = SPECIES.indexInRange(i, a.length);
            var va = FloatVector.fromArray(SPECIES, a, i, mask);
            var vb = FloatVector.fromArray(SPECIES, b, i, mask);
            var tail = va.fma(vb, FloatVector.zero(SPECIES));
            sum += tail.reduceLanes(VectorOperators.ADD);
        }
        return sum;
    }

    private static void checkLengths(float[] a, float[] b) {
        if (a == null || b == null) {
            throw new IllegalArgumentException("Input arrays must not be null");
        }
        if (a.length != b.length) {
            throw new IllegalArgumentException("Array lengths must match");
        }
    }
}
