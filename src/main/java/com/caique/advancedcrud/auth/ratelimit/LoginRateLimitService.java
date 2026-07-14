package com.caique.advancedcrud.auth.ratelimit;

import com.caique.advancedcrud.shared.exceptions.TooManyRequestsException;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
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
    private final Counter rateLimitTriggeredCounter;

    public LoginRateLimitService(StringRedisTemplate redis, MeterRegistry meterRegistry) {
        this.redis = redis;
        this.rateLimitTriggeredCounter = Counter.builder("auth.rate_limit.triggered")
                .description("Number of times that rate limit was triggered")
                .register(meterRegistry);
    }

    public void checkAllowed(String email, String ip) {
        if (countOf(EMAIL_PREFIX + email) >= MAX_ATTEMPTS_EMAIL
                || countOf(IP_PREFIX + ip) >= MAX_ATTEMPTS_IP) {
            rateLimitTriggeredCounter.increment();
            throw new TooManyRequestsException("Too many login attempts. Try again later.");
        }
    }

    public void recordFailure(String email, String ip) {
        increment(EMAIL_PREFIX + email);
        increment(IP_PREFIX + ip);
    }

    public void reset(String email) {
        redis.delete(EMAIL_PREFIX + email);
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
