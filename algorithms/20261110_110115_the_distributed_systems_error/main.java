package main;

import core.CircuitBreaker;
import core.CircuitBreaker.State;
import core.CircuitBreakerOpenException;
import core.RetryPolicy;
import core.ThrowingSupplier;

public class Main {
    public static void main(String[] args) throws Exception {
        testCircuitBreakerTransitions();
        testRetryPolicySuccess();
        testRetryPolicyExhaustion();
        System.out.println("All tests passed");
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    // ---------- CircuitBreaker Tests ----------
    private static void testCircuitBreakerTransitions() throws Exception {
        CircuitBreaker cb = new CircuitBreaker(3, 200, 2);

        // 1. Verify CLOSED state initially
        assertTrue(cb.getState() == State.CLOSED, "Initial state should be CLOSED");

        // 2. Cause failures to open the breaker
        ThrowingSupplier<String> failing = () -> { throw new RuntimeException("fail"); };
        for (int i = 0; i < 3; i++) {
            try { cb.execute(failing); } catch (Exception ignored) {}
        }
        assertTrue(cb.getState() == State.OPEN, "State should be OPEN after threshold");

        // 3. Calls while OPEN should throw CircuitBreakerOpenException
        try {
            cb.execute(() -> "ok");
            throw new AssertionError("Expected CircuitBreakerOpenException");
        } catch (CircuitBreakerOpenException ignored) {}

        // 4. Wait for timeout to move to HALF_OPEN
        Thread.sleep(250);
        assertTrue(cb.getState() == State.HALF_OPEN, "State should be HALF_OPEN after timeout");

        // 5. Successful call in HALF_OPEN increments successCount
        cb.execute(() -> "recovered");
        assertTrue(cb.getState() == State.HALF_OPEN, "Still HALF_OPEN after first success");

        // 6. Second success should close the breaker
        cb.execute(() -> "recovered");
        assertTrue(cb.getState() == State.CLOSED, "State should be CLOSED after enough successes");
    }

    // ---------- RetryPolicy Tests ----------
    private static void testRetryPolicySuccess() throws Exception {
        RetryPolicy rp = new RetryPolicy(5, 10, 50, 2.0);
        final int[] attempts = {0};

        String result = rp.execute(() -> {
            attempts[0]++;
            if (attempts[0] < 3) throw new RuntimeException("temporary failure");
            return "ok";
        });

        assertTrue("ok".equals(result), "RetryPolicy should eventually succeed");
        assertTrue(attempts[0] == 3, "Should have attempted 3 times");
    }

    private static void testRetryPolicyExhaustion() throws Exception {
        RetryPolicy rp = new RetryPolicy(3, 5, 20, 2.0);
        final int[] attempts = {0};

        try {
            rp.execute(() -> {
                attempts[0]++;
                throw new RuntimeException("always fail");
            });
            throw new AssertionError("Expected exception after retries exhausted");
        } catch (RuntimeException e) {
            assertTrue(attempts[0] == 3, "Should have attempted maxAttempts times");
            assertTrue(e.getMessage().equals("always fail"), "Exception message should propagate");
        }
    }
}
