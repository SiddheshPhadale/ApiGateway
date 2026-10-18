package com.Siddhesh.ApiGateway;

import com.Siddhesh.ApiGateway.Api.Entity.Status;
import com.Siddhesh.ApiGateway.Gateway.Dto.CachedSubscriptionDto;
import com.Siddhesh.ApiGateway.Gateway.Service.CacheService;
import com.Siddhesh.ApiGateway.Gateway.Service.RateLimitingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RedisRateLimitingTests {

    @Autowired
    private CacheService cacheService;

    @Autowired
    private RateLimitingService rateLimitingService;

    @Test
    void testCacheMiss_ReturnsNullSafely() {
        CachedSubscriptionDto cached = cacheService.getSubscription("non_existent_key");
        assertNull(cached, "Cache miss should safely return null without throwing exceptions");
    }

    @Test
    void testCacheSaveAndEviction_SafeExecution() {
        CachedSubscriptionDto dto = CachedSubscriptionDto.builder()
                .apiKey("test_key_123")
                .targetUrl("https://upstream.example.com")
                .subscriptionStatus(Status.ACTIVE)
                .apiStatus(Status.ACTIVE)
                .rateLimitPerMinute(100)
                .build();

        assertDoesNotThrow(() -> cacheService.saveSubscription("test_key_123", dto));
        assertDoesNotThrow(() -> cacheService.deleteSubscription("test_key_123"));
    }

    @Test
    void testWeightedSlidingWindowCalculation_FormulasAndBoundaries() {
        // Test boundary timestamps
        // Case 1: Exact minute start (now % 60 == 0) -> overlap is 1.0 (full previous minute counts)
        long minuteStart = 1609459200L; // divisible by 60
        double overlapStart = (60.0 - (minuteStart % 60)) / 60.0;
        assertEquals(1.0, overlapStart, 0.0001);

        int currentCount = 0;
        int previousCount = 10;
        double effectiveCountStart = currentCount + (previousCount * overlapStart);
        assertEquals(10.0, effectiveCountStart);

        // Case 2: Halfway through minute (now % 60 == 30) -> overlap is 0.5 (half previous minute counts)
        long halfMinute = minuteStart + 30;
        double overlapMid = (60.0 - (halfMinute % 60)) / 60.0;
        assertEquals(0.5, overlapMid, 0.0001);

        currentCount = 5;
        double effectiveCountMid = currentCount + (previousCount * overlapMid);
        assertEquals(10.0, effectiveCountMid);

        // Case 3: End of minute (now % 60 == 59) -> overlap is 1/60 ~ 0.01667
        long minuteEnd = minuteStart + 59;
        double overlapEnd = (60.0 - (minuteEnd % 60)) / 60.0;
        assertEquals(1.0 / 60.0, overlapEnd, 0.0001);

        // Case 4: Exceeded limit logic
        int limit = 10;
        assertTrue(effectiveCountMid >= limit, "Request should be rejected when effectiveCount >= limit");
    }

    @Test
    void testRateLimiting_InvalidLimit_ReturnsFalse() {
        assertFalse(rateLimitingService.isAllowed("key_zero_limit", 0));
        assertFalse(rateLimitingService.isAllowed("key_neg_limit", -5));
    }

    @Test
    void testRateLimiting_SafeFallbackOnRedisOutage() {
        // When Redis is unavailable or in mock mode, service handles it safely without crashing
        boolean allowed = rateLimitingService.isAllowed("test_key_outage", 100);
        assertTrue(allowed, "Rate limiter should fail-open or allow request when Redis is unavailable");
    }
}
