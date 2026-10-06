public class SlidingWindowTokenBucketLimiter implements Limiter {
    private final TokenBucket bucket;
    private final SlidingWindow window;

    public SlidingWindowTokenBucketLimiter(int bucketCapacity, double refillRatePerSecond,
                                           long windowSizeMs, int maxRequestsPerWindow) {
        this.bucket = new TokenBucket(bucketCapacity, refillRatePerSecond);
        this.window = new SlidingWindow(windowSizeMs, maxRequestsPerWindow);
    }

    @Override
    public boolean allow() {
        if (!bucket.consume(1)) {
            return false;
        }
        return window.allow();
    }
}
