import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple immutable representation of a payment request.
 */
final class PaymentRequest {
    final String idempotencyKey;
    final String fromAccount;
    final String toAccount;
    final double amount;

    PaymentRequest(String idempotencyKey, String fromAccount, String toAccount, double amount) {
        if (idempotencyKey == null || idempotencyKey.isEmpty())
            throw new IllegalArgumentException("Idempotency key must be non‑empty");
        this.idempotencyKey = idempotencyKey;
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.amount = amount;
    }
}

/**
 * Simple immutable representation of a payment result.
 */
final class PaymentResult {
    final boolean success;
    final String transactionId;
    final String message;

    PaymentResult(boolean success, String transactionId, String message) {
        this.success = success;
        this.transactionId = transactionId;
        this.message = message;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PaymentResult)) return false;
        PaymentResult other = (PaymentResult) o;
        return success == other.success &&
               transactionId.equals(other.transactionId) &&
               message.equals(other.message);
    }

    @Override
    public int hashCode() {
        return transactionId.hashCode();
    }
}

/**
 * Functional interface for the actual business logic of processing a payment.
 */
@FunctionalInterface
interface PaymentProcessor {
    PaymentResult process(PaymentRequest request);
}

/**
 * Thread‑safe store for idempotency keys with TTL support.
 */
final class IdempotencyKeyStore {
    private final ConcurrentHashMap<String, PaymentResult> resultMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> timestampMap = new ConcurrentHashMap<>();
    private final long ttlMillis;

    IdempotencyKeyStore(long ttlMillis) {
        if (ttlMillis <= 0) throw new IllegalArgumentException("TTL must be positive");
        this.ttlMillis = ttlMillis;
    }

    /**
     * Returns a stored result if the key exists and is not expired, otherwise null.
     */
    PaymentResult getIfPresent(String key) {
        cleanupIfExpired(key);
        return resultMap.get(key);
    }

    /**
     * Stores the result associated with the key.
     */
    void put(String key, PaymentResult result) {
        long now = System.currentTimeMillis();
        resultMap.put(key, result);
        timestampMap.put(key, now);
    }

    /**
     * Removes entries whose age exceeds the configured TTL.
     */
    void purgeExpired() {
        long now = System.currentTimeMillis();
        for (String key : timestampMap.keySet()) {
            Long ts = timestampMap.get(key);
            if (ts != null && now - ts > ttlMillis) {
                resultMap.remove(key);
                timestampMap.remove(key);
            }
        }
    }

    private void cleanupIfExpired(String key) {
        Long ts = timestampMap.get(key);
        if (ts != null && System.currentTimeMillis() - ts > ttlMillis) {
            resultMap.remove(key);
            timestampMap.remove(key);
        }
    }
}

/**
 * Service that guarantees idempotent processing of payments.
 */
final class PaymentService {
    private final IdempotencyKeyStore store;
    private final PaymentProcessor processor;

    PaymentService(IdempotencyKeyStore store, PaymentProcessor processor) {
        this.store = store;
        this.processor = processor;
    }

    /**
     * Processes a payment request exactly once per idempotency key.
     * Subsequent calls with the same key return the original result.
     */
    PaymentResult process(PaymentRequest request) {
        // Fast path: already processed and still valid.
        PaymentResult cached = store.getIfPresent(request.idempotencyKey);
        if (cached != null) {
            return cached;
        }

        // Synchronize on the key to avoid race conditions for the same key.
        synchronized (internKey(request.idempotencyKey)) {
            // Double‑check after acquiring lock.
            cached = store.getIfPresent(request.idempotencyKey);
            if (cached != null) {
                return cached;
            }
            // Perform real processing.
            PaymentResult result = processor.process(request);
            store.put(request.idempotencyKey, result);
            return result;
        }
    }

    /**
     * Helper that returns a canonical String object for synchronization.
     */
    private String internKey(String key) {
        return key.intern();
    }
}
