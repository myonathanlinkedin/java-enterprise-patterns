import java.util.Arrays;

public class SlidingWindowRateLimiterTest {
    public static void main(String[] args) {
        int windowSize = 3;
        int tokenBucketSize = 10;
        int tokenBucketRate = 2;

        SlidingWindowRateLimiter limiter = new SlidingWindowRateLimiter(windowSize, tokenBucketSize, tokenBucketRate);

        for (int i = 0; i < 100; i++) {
            limiter.addToken();
            if (limiter.isRateLimited()) {
                System.out.println("Rate limited!");
            }
        }

        limiter.updateWindow();
        limiter.addToken();
        if (limiter.isRateLimited()) {
            System.out.println("Rate limited!");
        }
    }
}
