package com.Siddhesh.ApiGateway.Gateway.Service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Weighted Sliding-Window Counter Rate Limiter.
 *
 * Algorithm Explanation & Inherent Approximation:
 * The window spans 60 seconds. Time is split into discrete 1-minute buckets: previous and current.
 * When a request arrives at epoch second `now`:
 * - overlap = (60 - (now % 60)) / 60.0 represents the portion of the previous minute bucket that still falls
 *   within the trailing 60-second sliding window.
 * - effectiveCount = currentCount + (previousCount * overlap)
 *
 * Approximation Note:
 * This algorithm assumes requests in the previous 1-minute bucket were uniformly distributed across its 60 seconds.
 * While an approximation (e.g., if all requests in the previous minute happened in the first second, it slightly
 * overestimates; if all happened in the 59th second, it slightly underestimates), it provides an O(1) memory and
 * computation footprint with very high accuracy, avoiding the O(N) storage cost of sorted-set sliding logs.
 *
 * Atomicity:
 * Executed as a single atomic Redis Lua script to prevent race conditions between reading and incrementing.
 */
@Slf4j
@Service
public class RateLimitingService {

    private final StringRedisTemplate redisTemplate;
    private final RedisScript<Long> rateLimitScript;

    private static final String RATE_LIMIT_LUA =
            "local current = tonumber(redis.call('GET', KEYS[1]) or '0')\n" +
            "local previous = tonumber(redis.call('GET', KEYS[2]) or '0')\n" +
            "local limit = tonumber(ARGV[1])\n" +
            "local overlap = tonumber(ARGV[2])\n" +
            "local ttl = tonumber(ARGV[3])\n" +
            "local effectiveCount = current + (previous * overlap)\n" +
            "if effectiveCount >= limit then\n" +
            "    return 0\n" +
            "else\n" +
            "    redis.call('INCR', KEYS[1])\n" +
            "    redis.call('EXPIRE', KEYS[1], ttl)\n" +
            "    return 1\n" +
            "end";

    public RateLimitingService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.rateLimitScript = new DefaultRedisScript<>(RATE_LIMIT_LUA, Long.class);
    }

    public boolean isAllowed(String apiKey, int limit) {
        return isAllowedAtTime(apiKey, limit, System.currentTimeMillis() / 1000);
    }

    public boolean isAllowedAtTime(String apiKey, int limit, long now) {
        if (limit <= 0) {
            return false;
        }

        long current = now - (now % 60);
        long previous = current - 60;

        // Bucket keys isolated by apiKey and timestamp
        String currentKey = "rate:current:" + apiKey + ":" + current;
        String previousKey = "rate:previous:" + apiKey + ":" + previous;

        double overlap = (60.0 - (now % 60)) / 60.0;

        try {
            Long result = redisTemplate.execute(
                    rateLimitScript,
                    List.of(currentKey, previousKey),
                    String.valueOf(limit),
                    String.valueOf(overlap),
                    "120"
            );
            return result != null && result == 1L;
        } catch (Exception e) {
            log.warn("Redis error during rate limiting check, failing open: {}", e.getMessage());
            return true;
        }
    }
}
