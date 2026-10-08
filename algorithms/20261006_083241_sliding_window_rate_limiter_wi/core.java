import java.util.ArrayList;
import java.util.List;

public class SlidingWindowRateLimiter {
    private final int windowSize;
    private final int tokenBucketSize;
    private final int tokenBucketRate;
    private final List<Integer> tokens = new ArrayList<>();

    public SlidingWindowRateLimiter(int windowSize, int tokenBucketSize, int tokenBucketRate) {
        this.windowSize = windowSize;
        this.tokenBucketSize = tokenBucketSize;
        this.tokenBucketRate = tokenBucketRate;
    }

    public void addToken() {
        if (tokens.size() >= windowSize) {
            tokens.remove(0);
        }
        tokens.add(0);
    }

    public boolean consumeToken() {
        if (tokens.size() > 0) {
            tokens.remove(0);
            return true;
        }
        return false;
    }

    public int getTokenCount() {
        return tokens.size();
    }

    public boolean isRateLimited() {
        int remainingTokens = tokenBucketSize - getTokenCount();
        return remainingTokens <= 0;
    }

    public void updateWindow() {
        tokens.remove(tokens.size() - 1);
        tokens.add(0);
    }
}
