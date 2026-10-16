import java.util.*;

public class Core {
    // Represents a laser with a frequency that drifts over time
    public static class Laser {
        private double frequency; // Hz
        private final double driftRate; // Hz per second

        public Laser(double initialFrequency, double driftRate) {
            this.frequency = initialFrequency;
            this.driftRate = driftRate;
        }

        public double getFrequency() {
            return frequency;
        }

        // Advance the laser frequency by dt seconds
        public void update(double dt) {
            frequency += driftRate * dt;
        }
    }

    // Simple PID controller for laser locking
    public static class PIDController {
        private double kp, ki, kd;
        private double integral;
        private double prevError;

        public PIDController(double kp, double ki, double kd) {
            this.kp = kp;
            this.ki = ki;
            this.kd = kd;
            this.integral = 0.0;
            this.prevError = 0.0;
        }

        public void setParameters(double kp, double ki, double kd) {
            this.kp = kp;
            this.ki = ki;
            this.kd = kd;
        }

        // Compute control adjustment
        public double compute(double error, double dt) {
            integral += error * dt;
            double derivative = (error - prevError) / dt;
            prevError = error;
            return kp * error + ki * integral + kd * derivative;
        }
    }

    // Simple AI optimizer that searches a discrete grid of PID parameters
    public static class AIOptimizer {
        private final double[] kpCandidates;
        private final double[] kiCandidates;
        private final double[] kdCandidates;
        private final int steps;
        private final double dt;
        private final double targetFrequency;
        private final double initialFrequency;
        private final double driftRate;

        public AIOptimizer(double[] kpCandidates, double[] kiCandidates, double[] kdCandidates,
                           int steps, double dt, double targetFrequency,
                           double initialFrequency, double driftRate) {
            this.kpCandidates = kpCandidates;
            this.kiCandidates = kiCandidates;
            this.kdCandidates = kdCandidates;
            this.steps = steps;
            this.dt = dt;
            this.targetFrequency = targetFrequency;
            this.initialFrequency = initialFrequency;
            this.driftRate = driftRate;
        }

        // Returns the best PID parameters as an array [kp, ki, kd]
        public double[] optimize() {
            double bestMSE = Double.MAX_VALUE;
            double[] bestParams = new double[3];

            for (double kp : kpCandidates) {
                for (double ki : kiCandidates) {
                    for (double kd : kdCandidates) {
                        double mse = simulate(kp, ki, kd);
                        if (mse < bestMSE) {
                            bestMSE = mse;
                            bestParams[0] = kp;
                            bestParams[1] = ki;
                            bestParams[2] = kd;
                        }
                    }
                }
            }
            return bestParams;
        }

        // Simulate the laser locking with given PID parameters and return mean squared error
        private double simulate(double kp, double ki, double kd) {
            Laser laser = new Laser(initialFrequency, driftRate);
            PIDController controller = new PIDController(kp, ki, kd);
            double sumSqError = 0.0;

            for (int i = 0; i < steps; i++) {
                double error = targetFrequency - laser.getFrequency();
                double adjustment = controller.compute(error, dt);
                laser.update(dt);
                laser.frequency += adjustment; // apply control
                sumSqError += error * error;
            }
            return sumSqError / steps;
        }
    }

    // Utility class for running a simulation with given parameters
    public static class Simulation {
        public static double run(double kp, double ki, double kd,
                                 double initialFrequency, double driftRate,
                                 double targetFrequency, int steps, double dt) {
            Laser laser = new Laser(initialFrequency, driftRate);
            PIDController controller = new PIDController(kp, ki, kd);
            double sumSqError = 0.0;

            for (int i = 0; i < steps; i++) {
                double error = targetFrequency - laser.getFrequency();
                double adjustment = controller.compute(error, dt);
                laser.update(dt);
                laser.frequency += adjustment;
                sumSqError += error * error;
            }
            return sumSqError / steps;
        }
    }
}
