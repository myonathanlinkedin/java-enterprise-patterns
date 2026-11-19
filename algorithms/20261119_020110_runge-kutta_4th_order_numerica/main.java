package rk4;

public class Main {

    private static final double TOL = 1e-9;

    public static void main(String[] args) {
        testExponentialGrowth();
        testExponentialDecay();
        testHarmonicOscillator();
        testZeroStep();
        testNegativeStepSize();
        System.out.println("All RK4 tests passed.");
    }

    /**
     * Test dy/dt = y, solution y(t) = y0 * exp(t).
     */
    private static void testExponentialGrowth() {
        RK4.ODEFunction f = (t, y) -> new double[]{ y[0] };
        double y0 = 1.0;
        double h = 0.1;
        int steps = 100; // integrate to t = 10
        double[] result = RK4.integrate(f, 0.0, new double[]{ y0 }, h, steps);
        double exact = y0 * Math.exp(h * steps);
        assertClose(result[0], exact, "Exponential growth");
    }

    /**
     * Test dy/dt = -y, solution y(t) = y0 * exp(-t).
     */
    private static void testExponentialDecay() {
        RK4.ODEFunction f = (t, y) -> new double[]{ -y[0] };
        double y0 = 2.0;
        double h = 0.05;
        int steps = 200; // integrate to t = 10
        double[] result = RK4.integrate(f, 0.0, new double[]{ y0 }, h, steps);
        double exact = y0 * Math.exp(-h * steps);
        assertClose(result[0], exact, "Exponential decay");
    }

    /**
     * Test a 2‑dimensional harmonic oscillator:
     *   dx/dt = v
     *   dv/dt = -x
     * Exact solution: x(t) = cos(t), v(t) = -sin(t) for initial (1,0).
     */
    private static void testHarmonicOscillator() {
        RK4.ODEFunction f = (t, y) -> new double[]{ y[1], -y[0] };
        double[] y0 = { 1.0, 0.0 };
        double h = 0.01;
        int steps = 628; // approx 2π ≈ 6.283185..., integrate to t≈6.28
        double[] result = RK4.integrate(f, 0.0, y0, h, steps);
        double t = h * steps;
        double exactX = Math.cos(t);
        double exactV = -Math.sin(t);
        assertClose(result[0], exactX, "Harmonic oscillator x");
        assertClose(result[1], exactV, "Harmonic oscillator v");
    }

    /**
     * Verify that zero steps returns the initial state unchanged.
     */
    private static void testZeroStep() {
        RK4.ODEFunction f = (t, y) -> new double[]{ y[0] * 2 };
        double[] y0 = { 3.14 };
        double[] result = RK4.integrate(f, 0.0, y0, 0.1, 0);
        if (result.length != y0.length) {
            throw new AssertionError("Zero step length mismatch");
        }
        for (int i = 0; i < y0.length; i++) {
            if (Math.abs(result[i] - y0[i]) > TOL) {
                throw new AssertionError("Zero step altered state at index " + i);
            }
        }
    }

    /**
     * Ensure that a negative step size works (integration backwards in time).
     */
    private static void testNegativeStepSize() {
        RK4.ODEFunction f = (t, y) -> new double[]{ y[0] };
        double y0 = 1.0;
        double h = -0.1;
        int steps = 10; // integrate backwards to t = -1
        double[] result = RK4.integrate(f, 0.0, new double[]{ y0 }, h, steps);
        double exact = y0 * Math.exp(h * steps);
        assertClose(result[0], exact, "Negative step size");
    }

    private static void assertClose(double actual, double expected, String message) {
        if (Double.isNaN(actual) || Double.isNaN(expected) ||
                Math.abs(actual - expected) > TOL) {
            throw new AssertionError(message + ": expected " + expected + ", got " + actual);
        }
    }
}
