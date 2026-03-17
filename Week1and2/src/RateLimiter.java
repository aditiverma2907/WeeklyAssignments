import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class RateLimiter {

    // Token Bucket class
    static class TokenBucket {
        private final long maxTokens;
        private final long refillRatePerSec;

        private AtomicLong tokens;
        private AtomicLong lastRefillTime;

        public TokenBucket(long maxTokens, long refillRatePerSec) {
            this.maxTokens = maxTokens;
            this.refillRatePerSec = refillRatePerSec;
            this.tokens = new AtomicLong(maxTokens);
            this.lastRefillTime = new AtomicLong(System.currentTimeMillis());
        }

        // Try to consume 1 token
        public synchronized boolean allowRequest() {
            refill();

            if (tokens.get() > 0) {
                tokens.decrementAndGet();
                return true;
            }
            return false;
        }

        // Refill tokens based on time passed
        private void refill() {
            long now = System.currentTimeMillis();
            long lastTime = lastRefillTime.get();

            long elapsedMillis = now - lastTime;
            long tokensToAdd = (elapsedMillis / 1000) * refillRatePerSec;

            if (tokensToAdd > 0) {
                long newTokens = Math.min(maxTokens, tokens.get() + tokensToAdd);
                tokens.set(newTokens);
                lastRefillTime.set(now);
            }
        }

        public long getRemainingTokens() {
            return tokens.get();
        }

        public long getRetryAfterSeconds() {
            return (tokens.get() == 0) ? (1) : 0;
        }
    }

    // clientId -> TokenBucket
    private ConcurrentHashMap<String, TokenBucket> clients;

    private final long MAX_TOKENS = 1000;        // per hour
    private final long REFILL_RATE = 1000 / 3600; // tokens per second

    public RateLimiter() {
        clients = new ConcurrentHashMap<>();
    }

    // Check rate limit
    public String checkRateLimit(String clientId) {
        clients.putIfAbsent(clientId,
                new TokenBucket(MAX_TOKENS, REFILL_RATE));

        TokenBucket bucket = clients.get(clientId);

        if (bucket.allowRequest()) {
            return "Allowed (" + bucket.getRemainingTokens() + " requests remaining)";
        } else {
            return "Denied (0 requests remaining, retry after "
                    + bucket.getRetryAfterSeconds() + "s)";
        }
    }

    // Get status
    public String getRateLimitStatus(String clientId) {
        TokenBucket bucket = clients.get(clientId);

        if (bucket == null) {
            return "No data for client";
        }

        long used = MAX_TOKENS - bucket.getRemainingTokens();

        return "{used: " + used +
                ", limit: " + MAX_TOKENS +
                ", remaining: " + bucket.getRemainingTokens() + "}";
    }

    // MAIN METHOD (Simulation)
    public static void main(String[] args) {
        RateLimiter limiter = new RateLimiter();

        String client = "abc123";

        // Simulate requests
        for (int i = 0; i < 1005; i++) {
            System.out.println(limiter.checkRateLimit(client));
        }

        System.out.println(limiter.getRateLimitStatus(client));
    }
}
