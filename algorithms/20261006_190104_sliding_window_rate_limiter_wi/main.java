public class Main {
    public static void main(String[] args) throws InterruptedException {
        // Test 1: Basic rate limiting with bucket capacity 5, refill 1/s, window 1s, max 5 per second
        SlidingWindowTokenBucketLimiter limiter1 = new SlidingWindowTokenBucketLimiter(
                5, 1.0, 1000, 5);
        for (int i = 0; i < 5; i++) {
            assert limiter1.allow() : "Request " + (i + 1) + " should be allowed";
        }
        assert !limiter1.allow() : "6th request should be denied";
        Thread.sleep(1100);
        assert limiter1.allow() : "After window reset, request should be allowed";

        // Test 2: Token bucket capacity limits bursts
        SlidingWindowTokenBucketLimiter limiter2 = new SlidingWindowTokenBucketLimiter(
                2, 1.0, 1000, 10);
        assert limiter2.allow() : "First request allowed";
        assert limiter2.allow() : "Second request allowed";
        assert !limiter2.allow() : "Third request denied due to bucket capacity";
        Thread.sleep(1100);
        assert limiter2.allow() : "After refill, request allowed";
        assert limiter2.allow() : "Second request after refill allowed";
        assert !limiter2.allow() : "Third request after refill denied";

        // Test 3: Sliding window limits per second
        SlidingWindowTokenBucketLimiter limiter3 = new SlidingWindowTokenBucketLimiter(
                10, 10.0, 1000, 3);
        assert limiter3.allow() : "Request 1 allowed";
        assert limiter3.allow() : "Request 2 allowed";
        assert limiter3.allow() : "Request 3 allowed";
        assert !limiter3.allow() : "Request 4 denied due to sliding window";
        Thread.sleep(1100);
        assert limiter3.allow() : "After window reset, request allowed";

        // Test 4: Combined limits
        SlidingWindowTokenBucketLimiter limiter4 = new SlidingWindowTokenBucketLimiter(
                2, 1.0, 1000, 5);
        assert limiter4.allow() : "Request 1 allowed";
        assert limiter4.allow() : "Request 2 allowed";
        assert !limiter4.allow() : "Request 3 denied due to bucket";
        Thread.sleep(1100);
        assert limiter4.allow() : "After refill, request allowed";
        assert limiter4.allow() : "Second request after refill allowed";
        assert !limiter4.allow() : "Third request after refill denied due to bucket";

        System.out.println("All tests passed.");
    }
}
