import java.util.*;

public class Main {
    public static void main(String[] args) {
        // Run unit tests
        testZeroDrift();
        testPositiveDrift();
        testNegativeDrift();
        testOptimizerConvergence();
        System.out.println("All tests passed.");
    }

    // Test that with zero drift, the controller keeps the laser at target
    private static void testZeroDrift() {
        double target = 500.0;
        double initial = 500.0;
        double drift = 0.0;
        double kp = 0.1, ki = 0.01, kd = 0.05;
        double mse = Core.Simulation.run(kp, ki, kd, initial, drift, target, 100, 0.01);
        assert mse < 1e-6 : "Zero drift test failed: mse=" + mse;
    }

    // Test with positive drift
    private static void testPositiveDrift() {
        double target = 500.0;
        double initial = 490.0;
        double drift = 0.5; // Hz per second
        double kp = 0.2, ki = 0.02, kd = 0.1;
        double mse = Core.Simulation.run(kp, ki, kd, initial, drift, target, 200, 0.01);
        assert mse < 1.0 : "Positive drift test failed: mse=" + mse;
    }

    // Test with negative drift
    private static void testNegativeDrift() {
        double target = 500.0;
        double initial = 510.0;
        double drift = -0.3; // Hz per second
        double kp = 0.15, ki = 0.015, kd = 0.08;
        double mse = Core.Simulation.run(kp, ki, kd, initial, drift, target, 200, 0.01);
        assert mse < 1.0 : "Negative drift test failed: mse=" + mse;
    }

    // Test that the AI optimizer finds parameters that reduce MSE below a threshold
    private static void testOptimizerConvergence() {
        double target = 500.0;
        double initial = 480.0;
        double drift = 0.4;
        int steps = 200;
        double dt = 0.01;

        double[] kpCands = {0.05, 0.1, 0.15, 0.2};
        double[] kiCands = {0.005, 0.01, 0.015};
        double[] kdCands = {0.02, 0.04, 0.06};

        Core.AIOptimizer optimizer = new Core.AIOptimizer(
                kpCands, kiCands, kdCands, steps, dt, target, initial, drift);

        double[] bestParams = optimizer.optimize();
        double mse = Core.Simulation.run(bestParams[0], bestParams[1], bestParams[2],
                initial, drift, target, steps, dt);
        assert mse < 0.5 : "Optimizer convergence test failed: mse=" + mse;
    }
}
