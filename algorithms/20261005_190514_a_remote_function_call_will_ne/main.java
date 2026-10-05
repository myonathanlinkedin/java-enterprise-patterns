import java.util.concurrent.*;
import java.util.*;

public class Main {
    private static void assertEquals(Object expected, Object actual, String message) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError(message + " Expected: " + expected + " but was: " + actual);
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    public static void main(String[] args) {
        RemoteEngine engine = new RemoteEngine(4, 100);

        // Register functions
        engine.register("add", (RemoteFunction<Integer, Integer>) (Integer x) -> x + 3);
        engine.register("concat", (RemoteFunction<String, String>) (String s) -> s + " World");
        engine.register("fail", (RemoteFunction<Void, Void>) (Void v) -> { throw new RuntimeException("Intentional failure"); });

        // Test add
        Future<RemoteResult<Integer>> addFuture = engine.call("add", 7);
        try {
            RemoteResult<Integer> addResult = addFuture.get();
            assertTrue(addResult.isSuccess(), "Add function should succeed");
            assertEquals(10, addResult.getResult(), "Add result mismatch");
        } catch (Exception e) {
            throw new AssertionError("Add call threw exception: " + e.getMessage());
        }

        // Test concat
        Future<RemoteResult<String>> concatFuture = engine.call("concat", "Hello");
        try {
            RemoteResult<String> concatResult = concatFuture.get();
            assertTrue(concatResult.isSuccess(), "Concat function should succeed");
            assertEquals("Hello World", concatResult.getResult(), "Concat result mismatch");
        } catch (Exception e) {
            throw new AssertionError("Concat call threw exception: " + e.getMessage());
        }

        // Test fail
        Future<RemoteResult<Void>> failFuture = engine.call("fail", null);
        try {
            RemoteResult<Void> failResult = failFuture.get();
            assertTrue(!failResult.isSuccess(), "Fail function should not succeed");
            assertTrue(failResult.getException() instanceof RuntimeException, "Expected RuntimeException");
            assertEquals("Intentional failure", failResult.getException().getMessage(), "Exception message mismatch");
        } catch (Exception e) {
            throw new AssertionError("Fail call threw exception: " + e.getMessage());
        }

        // Test concurrency
        List<Future<RemoteResult<Integer>>> futures = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            futures.add(engine.call("add", i));
        }
        int sum = 0;
        for (Future<RemoteResult<Integer>> f : futures) {
            try {
                RemoteResult<Integer> r = f.get();
                assertTrue(r.isSuccess(), "Concurrent add should succeed");
                sum += r.getResult();
            } catch (Exception e) {
                throw new AssertionError("Concurrent add threw exception: " + e.getMessage());
            }
        }
        int expectedSum = 0;
        for (int i = 0; i < 10; i++) {
            expectedSum += i + 3;
        }
        assertEquals(expectedSum, sum, "Concurrent sum mismatch");

        // Test latency
        long start = System.nanoTime();
        Future<RemoteResult<Integer>> latencyFuture = engine.call("add", 0);
        try {
            latencyFuture.get();
        } catch (Exception e) {
            throw new AssertionError("Latency test call threw exception: " + e.getMessage());
        }
        long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
        assertTrue(elapsedMs >= 100, "Latency should be at least 100 ms, measured: " + elapsedMs + " ms");

        engine.shutdown();
        System.out.println("All tests passed.");
    }
}
