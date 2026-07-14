package com.caique.advancedcrud.auth.ratelimit;

import com.caique.advancedcrud.shared.exceptions.TooManyRequestsException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RefreshRateLimitService {

    private static final String IP_PREFIX = "refresh_attempts_ip:";
    private static final int MAX_ATTEMPTS = 30;
    private static final Duration WINDOW = Duration.ofMinutes(15);

    private final StringRedisTemplate redis;

    public RefreshRateLimitService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public void checkAllowed(String ip) {
        String key = IP_PREFIX + ip;
        Long count = redis.opsForValue().increment(key);
        if(count != null && count == 1L) {
            redis.expire(key, WINDOW);
        }

        if(count != null && count > MAX_ATTEMPTS) {
            throw new TooManyRequestsException("Too many refresh requests. Try again later.");
        }
    }
}
