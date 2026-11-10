package core;

import java.util.function.Supplier;

/**
 * Exception thrown when a CircuitBreaker is in OPEN state.
 */
public final class CircuitBreakerOpenException extends RuntimeException {
    public CircuitBreakerOpenException(String message) {
        super(message);
    }
}

/**
 * Functional interface allowing checked exceptions.
 */
@FunctionalInterface
public interface ThrowingSupplier<T> {
    T get() throws Exception;
}

/**
 * Simple exponential backoff retry policy.
 */
public final class RetryPolicy {
    private final int maxAttempts;
    private final long initialDelayMillis;
    private final long maxDelayMillis;
    private final double multiplier;

    public RetryPolicy(int maxAttempts, long initialDelayMillis, long maxDelayMillis, double multiplier) {
        if (maxAttempts < 1) throw new IllegalArgumentException("maxAttempts must be >=1");
        if (initialDelayMillis < 0 || maxDelayMillis < 0) throw new IllegalArgumentException("delays must be >=0");
        if (multiplier < 1.0) throw new IllegalArgumentException("multiplier must be >=1.0");
        this.maxAttempts = maxAttempts;
        this.initialDelayMillis = initialDelayMillis;
        this.maxDelayMillis = maxDelayMillis;
        this.multiplier = multiplier;
    }

    public <T> T execute(ThrowingSupplier<T> supplier) throws Exception {
        long delay = initialDelayMillis;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return supplier.get();
            } catch (Exception e) {
                if (attempt == maxAttempts) {
                    throw e;
                }
                Thread.sleep(delay);
                delay = Math.min((long) (delay * multiplier), maxDelayMillis);
            }
        }
        throw new IllegalStateException("Unreachable");
    }
}

/**
 * Circuit Breaker implementation for error handling in distributed systems.
 */
public final class CircuitBreaker {
    public enum State { CLOSED, OPEN, HALF_OPEN }

    private final int failureThreshold;
    private final long openTimeoutMillis;
    private final int halfOpenSuccessThreshold;

    private State state = State.CLOSED;
    private int failureCount = 0;
    private int successCount = 0;
    private long stateEnteredAt = System.nanoTime();

    public CircuitBreaker(int failureThreshold, long openTimeoutMillis, int halfOpenSuccessThreshold) {
        if (failureThreshold < 1) throw new IllegalArgumentException("failureThreshold >=1");
        if (openTimeoutMillis < 0) throw new IllegalArgumentException("openTimeoutMillis >=0");
        if (halfOpenSuccessThreshold < 1) throw new IllegalArgumentException("halfOpenSuccessThreshold >=1");
        this.failureThreshold = failureThreshold;
        this.openTimeoutMillis = openTimeoutMillis;
        this.halfOpenSuccessThreshold = halfOpenSuccessThreshold;
    }

    private long nowMillis() {
        return System.nanoTime() / 1_000_000;
    }

    private synchronized void transitionTo(State newState) {
        state = newState;
        stateEnteredAt = System.nanoTime();
        failureCount = 0;
        successCount = 0;
    }

    private synchronized void maybeTransitionFromOpen() {
        if (state == State.OPEN && (nowMillis() - (stateEnteredAt / 1_000_000)) >= openTimeoutMillis) {
            transitionTo(State.HALF_OPEN);
        }
    }

    public <T> T execute(ThrowingSupplier<T> supplier) throws Exception {
        maybeTransitionFromOpen();

        if (state == State.OPEN) {
            throw new CircuitBreakerOpenException("Circuit breaker is OPEN");
        }

        try {
            T result = supplier.get();
            onSuccess();
            return result;
        } catch (Exception e) {
            onFailure();
            throw e;
        }
    }

    private synchronized void onSuccess() {
        if (state == State.HALF_OPEN) {
            successCount++;
            if (successCount >= halfOpenSuccessThreshold) {
                transitionTo(State.CLOSED);
            }
        } else if (state == State.CLOSED) {
            // reset failure count on successful call
            failureCount = 0;
        }
    }

    private synchronized void onFailure() {
        if (state == State.HALF_OPEN) {
            transitionTo(State.OPEN);
        } else if (state == State.CLOSED) {
            failureCount++;
            if (failureCount >= failureThreshold) {
                transitionTo(State.OPEN);
            }
        }
    }

    public synchronized State getState() {
        maybeTransitionFromOpen();
        return state;
    }
}
