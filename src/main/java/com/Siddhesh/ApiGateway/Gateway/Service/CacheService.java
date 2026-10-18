package com.Siddhesh.ApiGateway.Gateway.Service;

import com.Siddhesh.ApiGateway.Gateway.Dto.CachedSubscriptionDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class CacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String KEY_PREFIX = "subscription:";
    private static final Duration DEFAULT_TTL = Duration.ofMinutes(10);

    private String buildKey(String apiKey){
        return KEY_PREFIX + apiKey;
    }

    public void saveSubscription(String apiKey, CachedSubscriptionDto cachedSubscriptionDto){
        try {
            if (apiKey == null || cachedSubscriptionDto == null) return;
            String key = buildKey(apiKey);
            redisTemplate.opsForValue().set(key, cachedSubscriptionDto, DEFAULT_TTL);
        } catch (Exception e) {
            log.warn("Redis unavailable: Failed to cache subscription for key: {}", maskApiKey(apiKey), e);
        }
    }

    public CachedSubscriptionDto getSubscription(String apiKey){
        try {
            if (apiKey == null) return null;
            Object val = redisTemplate.opsForValue().get(buildKey(apiKey));
            if (val instanceof CachedSubscriptionDto) {
                return (CachedSubscriptionDto) val;
            }
            return null;
        } catch (Exception e) {
            log.warn("Redis unavailable: Failed to read cache for key: {}, falling back to DB", maskApiKey(apiKey));
            return null;
        }
    }

    public void deleteSubscription(String apiKey){
        try {
            if (apiKey == null) return;
            redisTemplate.delete(buildKey(apiKey));
        } catch (Exception e) {
            log.warn("Redis unavailable: Failed to delete cache for key: {}", maskApiKey(apiKey));
        }
    }

    private String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() <= 8) return "***";
        return apiKey.substring(0, 4) + "..." + apiKey.substring(apiKey.length() - 4);
    }
}
