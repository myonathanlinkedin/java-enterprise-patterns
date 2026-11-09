import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Thread‑safe in‑memory store for idempotency keys with optional scoping and TTL.
 *
 * <p>Typical usage:
 *
 * <pre>
 * IdempotencyKeyStore store = new IdempotencyKeyStore(5, TimeUnit.MINUTES);
 * if (store.tryAcquire("order", "order-123")) {
 *     // perform operation
 *     store.complete("order", "order-123");
 * } else if (store.isCompleted("order", "order-123")) {
 *     // operation already finished – safe to ignore / return cached result
 * }
 * </pre>
 *
 * The store automatically discards entries older than the configured TTL when
 * {@link #purgeExpired()} is invoked. Clients may call this method periodically
 * (e.g., via a scheduled executor) or rely on lazy eviction performed during
 * normal operations.
 */
public class IdempotencyKeyStore {

    /** Per‑scope map of keys to their metadata. */
    private final Map<String, Map<String, Entry>> store = new ConcurrentHashMap<>();

    /** Time‑to‑live for each key, in nanoseconds. */
    private final long ttlNanos;

    /**
     * Constructs a store with the given TTL.
     *
     * @param ttl   time‑to‑live value
     * @param unit  unit of the TTL
     */
    public IdempotencyKeyStore(long ttl, TimeUnit unit) {
        if (ttl <= 0) {
            throw new IllegalArgumentException("TTL must be positive");
        }
        this.ttlNanos = unit.toNanos(ttl);
    }

    /**
     * Attempts to acquire a key for the given scope.
     *
     * @param scope logical grouping (e.g., client id, operation type)
     * @param key   the idempotency key
     * @return {@code true} if the key was not present (or expired) and is now
     *         registered as “in‑progress”; {@code false} if the key already exists
     *         and is still within its TTL.
     */
    public boolean tryAcquire(String scope, String key) {
        long now = System.nanoTime();
        Map<String, Entry> scopedMap = store.computeIfAbsent(scope, k -> new ConcurrentHashMap<>());
        Entry existing = scopedMap.get(key);
        if (existing != null && !existing.isExpired(now, ttlNanos)) {
            return false; // still valid
        }
        // Either absent or expired – replace with fresh entry
        scopedMap.put(key, new Entry(now));
        return true;
    }

    /**
     * Marks a previously acquired key as successfully completed.
     *
     * @param scope the scope used during acquisition
     * @param key   the idempotency key
     * @throws IllegalStateException if the key was never acquired or already expired
     */
    public void complete(String scope, String key) {
        long now = System.nanoTime();
        Map<String, Entry> scopedMap = store.get(scope);
        if (scopedMap == null) {
            throw new IllegalStateException("Scope not found");
        }
        Entry entry = scopedMap.get(key);
        if (entry == null || entry.isExpired(now, ttlNanos)) {
            throw new IllegalStateException("Key not present or expired");
        }
        entry.completed = true;
    }

    /**
     * Checks whether a key has been completed.
     *
     * @param scope the scope used during acquisition
     * @param key   the idempotency key
     * @return {@code true} if the key exists, is not expired, and was marked completed
     */
    public boolean isCompleted(String scope, String key) {
        long now = System.nanoTime();
        Map<String, Entry> scopedMap = store.get(scope);
        if (scopedMap == null) {
            return false;
        }
        Entry entry = scopedMap.get(key);
        return entry != null && !entry.isExpired(now, ttlNanos) && entry.completed;
    }

    /**
     * Removes all entries that have exceeded the configured TTL.
     * This method is safe to call concurrently with other operations.
     */
    public void purgeExpired() {
        long now = System.nanoTime();
        for (Map.Entry<String, Map<String, Entry>> scopeEntry : store.entrySet()) {
            Map<String, Entry> scopedMap = scopeEntry.getValue();
            scopedMap.entrySet().removeIf(e -> e.getValue().isExpired(now, ttlNanos));
            // Clean up empty scopes to avoid memory leak
            if (scopedMap.isEmpty()) {
                store.remove(scopeEntry.getKey(), scopedMap);
            }
        }
    }

    /** Simple holder for timestamp and completion flag. */
    private static final class Entry {
        final long createdAtNanos;
        volatile boolean completed = false;

        Entry(long createdAtNanos) {
            this.createdAtNanos = createdAtNanos;
        }

        boolean isExpired(long nowNanos, long ttlNanos) {
            return nowNanos - createdAtNanos > ttlNanos;
        }
    }
}
