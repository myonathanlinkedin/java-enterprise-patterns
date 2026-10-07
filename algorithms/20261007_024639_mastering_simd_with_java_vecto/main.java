import java.util.Random;

/**
 * Simple test harness for SimdVectorOps.
 * Run with assertions enabled: java -ea Main
 */
public class Main {
    private static final float EPS = 1e-5f;

    public static void main(String[] args) {
        testAdd();
        testMultiply();
        testDotProduct();
        System.out.println("All SIMD Vector API tests passed.");
    }

    private static void testAdd() {
        int size = 1024;
        float[] a = randomArray(size);
        float[] b = randomArray(size);
        float[] expected = new float[size];
        for (int i = 0; i < size; i++) {
            expected[i] = a[i] + b[i];
        }
        float[] result = SimdVectorOps.add(a, b);
        assertArraysClose(expected, result, "add");
    }

    private static void testMultiply() {
        int size = 2048;
        float[] a = randomArray(size);
        float[] b = randomArray(size);
        float[] expected = new float[size];
        for (int i = 0; i < size; i++) {
            expected[i] = a[i] * b[i];
        }
        float[] result = SimdVectorOps.multiply(a, b);
        assertArraysClose(expected, result, "multiply");
    }

    private static void testDotProduct() {
        int size = 1500;
        float[] a = randomArray(size);
        float[] b = randomArray(size);
        float expected = 0f;
        for (int i = 0; i < size; i++) {
            expected += a[i] * b[i];
        }
        float result = SimdVectorOps.dotProduct(a, b);
        assertClose(expected, result, "dotProduct");
    }

    private static float[] randomArray(int length) {
        Random rng = new Random(0xDEADBEEF);
        float[] arr = new float[length];
        for (int i = 0; i < length; i++) {
            arr[i] = rng.nextFloat() * 100f - 50f; // range [-50, 50]
        }
        return arr;
    }

    private static void assertArraysClose(float[] expected, float[] actual, String op) {
        assert expected.length == actual.length : op + ": length mismatch";
        for (int i = 0; i < expected.length; i++) {
            if (Math.abs(expected[i] - actual[i]) > EPS) {
                throw new AssertionError(op + ": mismatch at index " + i +
                        " expected=" + expected[i] + " actual=" + actual[i]);
            }
        }
    }

    private static void assertClose(float expected, float actual, String op) {
        if (Math.abs(expected - actual) > EPS) {
            throw new AssertionError(op + ": expected=" + expected + " actual=" + actual);
        }
    }
}
