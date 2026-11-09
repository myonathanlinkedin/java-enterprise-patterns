import java.util.concurrent.TimeUnit;

/**
 * Minimal test harness for {@link IdempotencyKeyStore}.
 *
 * <p>All tests are executed via plain {@code assert} statements.
 * Run with the VM option {@code -ea} to enable assertions.
 */
public class Main {

    public static void main(String[] args) {
        testBasicAcquireAndComplete();
        testIdempotentAcquire();
        testExpiration();
        testScopeIsolation();
        testPurgeExpired();
        System.out.println("All tests passed.");
    }

    private static void testBasicAcquireAndComplete() {
        IdempotencyKeyStore store = new IdempotencyKeyStore(10, TimeUnit.SECONDS);
        assert store.tryAcquire("order", "key-1") : "First acquisition must succeed";
        assert !store.tryAcquire("order", "key-1") : "Duplicate acquisition must fail";
        assert !store.isCompleted("order", "key-1") : "Not completed yet";
        store.complete("order", "key-1");
        assert store.isCompleted("order", "key-1") : "Should be marked completed";
    }

    private static void testIdempotentAcquire() {
        IdempotencyKeyStore store = new IdempotencyKeyStore(5, TimeUnit.SECONDS);
        assert store.tryAcquire("payment", "pay-123") : "First acquire";
        store.complete("payment", "pay-123");
        // Subsequent acquire should still fail because key exists and not expired
        assert !store.tryAcquire("payment", "pay-123") : "Acquire after completion must fail";
        assert store.isCompleted("payment", "pay-123") : "Completion flag persists";
    }

    private static void testExpiration() {
        IdempotencyKeyStore store = new IdempotencyKeyStore(1, TimeUnit.SECONDS);
        assert store.tryAcquire("session", "sess-42") : "Acquire before expiry";
        // Sleep longer than TTL
        sleep(1500);
        // After expiry a new acquisition should be allowed
        assert store.tryAcquire("session", "sess-42") : "Acquire after expiry must succeed";
        // The old completed flag must not be visible
        assert !store.isCompleted("session", "sess-42") : "Completed flag cleared after expiry";
    }

    private static void testScopeIsolation() {
        IdempotencyKeyStore store = new IdempotencyKeyStore(10, TimeUnit.SECONDS);
        assert store.tryAcquire("userA", "shared-key") : "UserA first acquire";
        assert !store.tryAcquire("userB", "shared-key") : "UserB should see same key as conflict";
        store.complete("userA", "shared-key");
        assert store.isCompleted("userA", "shared-key") : "UserA completed";
        assert !store.isCompleted("userB", "shared-key") : "UserB never completed";
    }

    private static void testPurgeExpired() {
        IdempotencyKeyStore store = new IdempotencyKeyStore(1, TimeUnit.SECONDS);
        assert store.tryAcquire("cache", "c1") : "Acquire c1";
        assert store.tryAcquire("cache", "c2") : "Acquire c2";
        sleep(1200);
        store.purgeExpired();
        // After purge, new acquisitions should be possible
        assert store.tryAcquire("cache", "c1") : "c1 re‑acquire after purge";
        assert store.tryAcquire("cache", "c2") : "c2 re‑acquire after purge";
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError("Sleep interrupted", e);
        }
    }
}
