package com.caique.AdvancedCrud.auth.rateLimit;

import com.caique.AdvancedCrud.shared.exceptions.TooManyLoginAttemptsException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class LoginRateLimitService {

    private static final String EMAIL_PREFIX = "login_attempts_email:";
    private static final String IP_PREFIX = "login_attempts_ip:";

    private static final int MAX_ATTEMPTS_EMAIL = 5;
    private static final int MAX_ATTEMPTS_IP = 20;
    private static final Duration WINDOW = Duration.ofMinutes(15);

    private final StringRedisTemplate redis;

    public LoginRateLimitService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public void checkAllowed(String email, String ip) {
        if (countOf(EMAIL_PREFIX + email) >= MAX_ATTEMPTS_EMAIL
                || countOf(IP_PREFIX + ip) >= MAX_ATTEMPTS_IP) {
            throw new TooManyLoginAttemptsException();
        }
    }

    public void recordFailure(String email, String ip) {
        increment(EMAIL_PREFIX + email);
        increment(IP_PREFIX + ip);
    }

    public void reset(String email, String ip) {
        redis.delete(EMAIL_PREFIX + email);
        redis.delete(IP_PREFIX + ip);
    }

    private long countOf(String key) {
        String value = redis.opsForValue().get(key);
        return value == null ? 0 : Long.parseLong(value);
    }

    private void increment(String key) {
        Long count = redis.opsForValue().increment(key);
        if (count != null && count == 1L) {
            redis.expire(key, WINDOW);
        }
    }

}
