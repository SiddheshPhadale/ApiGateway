package com.Siddhesh.ApiGateway.Gateway.Service;

import com.Siddhesh.ApiGateway.Gateway.Dto.CachedSubscriptionDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class CacheService {

    final private RedisTemplate<String, Object> redisTemplate;

    private String buildKey(String apiKey){
        return "subscription:" + apiKey;
    }

    public void saveSubscription(String apiKey, CachedSubscriptionDto cachedSubscriptionDto){
        String key = buildKey(apiKey);
        redisTemplate.opsForValue().set(key, cachedSubscriptionDto, Duration.ofMinutes(5));
    }

    public CachedSubscriptionDto getSubscription(String apiKey){
        return (CachedSubscriptionDto) redisTemplate.opsForValue().get(buildKey(apiKey));
    }

    public void deleteSubscription(String apiKey){
        redisTemplate.delete(buildKey(apiKey));
    }
}
