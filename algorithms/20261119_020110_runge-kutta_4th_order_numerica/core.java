package rk4;

import java.util.Objects;

/**
 * Core classes for a 4th-order Runge‑Kutta ODE integrator.
 *
 * The implementation is deliberately simple, deterministic and uses only the Java
 * standard library. It works for systems of first‑order ODEs expressed as
 *
 *   dy/dt = f(t, y)
 *
 * where {@code y} is a vector of state variables.
 */
public final class RK4 {

    private RK4() {
        // Prevent instantiation.
    }

    /**
     * Functional interface representing the right‑hand side of an ODE system.
     */
    @FunctionalInterface
    public interface ODEFunction {
        /**
         * Computes the derivative dy/dt at a given time and state.
         *
         * @param t current time
         * @param y current state vector (must not be modified)
         * @return derivative vector of the same length as {@code y}
         */
        double[] compute(double t, double[] y);
    }

    /**
     * Performs a single RK4 step.
     *
     * @param f   ODE right‑hand side function
     * @param t   current time
     * @param y   current state vector (will not be modified)
     * @param h   step size (must be non‑zero)
     * @return new state vector after advancing by {@code h}
     * @throws IllegalArgumentException if {@code h == 0} or vectors have inconsistent length
     */
    public static double[] step(ODEFunction f, double t, double[] y, double h) {
        Objects.requireNonNull(f, "ODE function must not be null");
        Objects.requireNonNull(y, "State vector must not be null");
        if (h == 0.0) {
            throw new IllegalArgumentException("Step size h must be non‑zero");
        }

        int n = y.length;
        double[] k1 = f.compute(t, y);
        validateDerivative(k1, n, "k1");

        double[] yTemp = new double[n];
        for (int i = 0; i < n; i++) {
            yTemp[i] = y[i] + 0.5 * h * k1[i];
        }
        double[] k2 = f.compute(t + 0.5 * h, yTemp);
        validateDerivative(k2, n, "k2");

        for (int i = 0; i < n; i++) {
            yTemp[i] = y[i] + 0.5 * h * k2[i];
        }
        double[] k3 = f.compute(t + 0.5 * h, yTemp);
        validateDerivative(k3, n, "k3");

        for (int i = 0; i < n; i++) {
            yTemp[i] = y[i] + h * k3[i];
        }
        double[] k4 = f.compute(t + h, yTemp);
        validateDerivative(k4, n, "k4");

        double[] yNext = new double[n];
        for (int i = 0; i < n; i++) {
            yNext[i] = y[i] + (h / 6.0) * (k1[i] + 2.0 * k2[i] + 2.0 * k3[i] + k4[i]);
        }
        return yNext;
    }

    /**
     * Integrates the ODE from {@code t0} to {@code t0 + steps * h}.
     *
     * @param f      ODE right‑hand side function
     * @param t0     initial time
     * @param y0     initial state vector (will not be modified)
     * @param h      step size (must be non‑zero)
     * @param steps  number of RK4 steps (must be non‑negative)
     * @return state vector after integration
     * @throws IllegalArgumentException if arguments are invalid
     */
    public static double[] integrate(ODEFunction f, double t0, double[] y0, double h, int steps) {
        Objects.requireNonNull(f, "ODE function must not be null");
        Objects.requireNonNull(y0, "Initial state vector must not be null");
        if (h == 0.0) {
            throw new IllegalArgumentException("Step size h must be non‑zero");
        }
        if (steps < 0) {
            throw new IllegalArgumentException("Number of steps must be non‑negative");
        }

        double[] y = y0.clone();
        double t = t0;
        for (int i = 0; i < steps; i++) {
            y = step(f, t, y, h);
            t += h;
        }
        return y;
    }

    private static void validateDerivative(double[] d, int expected, String name) {
        if (d == null) {
            throw new IllegalArgumentException(name + " derivative returned null");
        }
        if (d.length != expected) {
            throw new IllegalArgumentException(name + " derivative length mismatch: expected "
                    + expected + ", got " + d.length);
        }
    }
}
