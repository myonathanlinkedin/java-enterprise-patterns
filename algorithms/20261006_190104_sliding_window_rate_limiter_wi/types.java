import java.util.Deque;
import java.util.ArrayDeque;

public interface Limiter {
    boolean allow();
}

public class TokenBucket {
    private final int capacity;
    private double tokens;
    private final double refillRatePerSecond;
    private long lastRefillTimestamp;

    public TokenBucket(int capacity, double refillRatePerSecond) {
        this.capacity = capacity;
        this.tokens = capacity;
        this.refillRatePerSecond = refillRatePerSecond;
        this.lastRefillTimestamp = System.nanoTime();
    }

    private synchronized void refill() {
        long now = System.nanoTime();
        double elapsedSeconds = (now - lastRefillTimestamp) / 1_000_000_000.0;
        if (elapsedSeconds <= 0) {
            return;
        }
        double tokensToAdd = elapsedSeconds * refillRatePerSecond;
        tokens = Math.min(capacity, tokens + tokensToAdd);
        lastRefillTimestamp = now;
    }

    public synchronized boolean consume(int amount) {
        refill();
        if (tokens >= amount) {
            tokens -= amount;
            return true;
        }
        return false;
    }
}

public class SlidingWindow {
    private final long windowSizeMs;
    private final int maxRequests;
    private final Deque<Long> timestamps = new ArrayDeque<>();

    public SlidingWindow(long windowSizeMs, int maxRequests) {
        this.windowSizeMs = windowSizeMs;
        this.maxRequests = maxRequests;
    }

    public synchronized boolean allow() {
        long now = System.currentTimeMillis();
        long threshold = now - windowSizeMs;
        while (!timestamps.isEmpty() && timestamps.peekFirst() < threshold) {
            timestamps.pollFirst();
        }
        if (timestamps.size() < maxRequests) {
            timestamps.addLast(now);
            return true;
        }
        return false;
    }
}
