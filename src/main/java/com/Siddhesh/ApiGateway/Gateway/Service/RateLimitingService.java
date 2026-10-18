package com.Siddhesh.ApiGateway.Gateway.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RateLimitingService {

    final private RedisTemplate<String, Object> redisTemplate;

    public boolean isAllowed(String apiKey, int limit){
        long now = System.currentTimeMillis() / 1000;

        long current = now - (now % 60);
        long previous = current - 60;

        String currentKey = "rate:current:" + apiKey + ":" + current;
        String previousKey = "rate:previous:" + apiKey + ":" + previous;

        Object currentVal = redisTemplate.opsForValue().get(currentKey);
        Object previousVal = redisTemplate.opsForValue().get(previousKey);

        int currentCount = currentVal != null ? Integer.parseInt(currentVal.toString()) : 0;
        int previousCount = previousVal != null ? Integer.parseInt(previousVal.toString()) : 0;

        double overlap = (60 - (now % 60)) / 60.0;
        double effectiveCount = currentCount + (previousCount * overlap);

        if (effectiveCount >= limit) return false;
        redisTemplate.opsForValue().increment(currentKey);
        redisTemplate.expire(currentKey, Duration.ofSeconds(120));
        return true;
    }
}
