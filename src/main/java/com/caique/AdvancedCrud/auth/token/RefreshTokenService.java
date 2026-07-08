package com.caique.AdvancedCrud.auth.token;

import com.caique.AdvancedCrud.shared.exceptions.InvalidRefreshTokenException;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private static final Duration TTL = Duration.ofDays(7);
    private static final String TOKEN_PREFIX = "refresh_token:";
    private static final String FAMILY_PREFIX = "token_family:";
    private static final String USER_FAMILIES_PREFIX = "user_families:";

    private final RedisTemplate<String, RefreshToken> tokenRedis;
    private final StringRedisTemplate familyRedis;

    private final Counter loginRefreshTokenCounter;
    private final Counter rotationRefreshTokenCounter;

    public RefreshTokenService(RedisTemplate<String, RefreshToken> tokenRedis, StringRedisTemplate familyRedis, MeterRegistry meterRegistry) {
        this.tokenRedis = tokenRedis;
        this.familyRedis = familyRedis;

        this.loginRefreshTokenCounter = Counter.builder("refresh_token.created")
                .tag("reason", "login")
                .description("Generated Refresh Token Quantity")
                .register(meterRegistry);

        this.rotationRefreshTokenCounter = Counter.builder("refresh_token.created")
                .tag("reason", "rotation")
                .description("Generated Refresh Token Quantity")
                .register(meterRegistry);
    }

    public UUID createNewToken(UUID userId) {
        UUID familyId = UUID.randomUUID();
        UUID tokenId = UUID.randomUUID();

        familyRedis.opsForValue().set(FAMILY_PREFIX + familyId, "valid", TTL);

        familyRedis.opsForSet().add(USER_FAMILIES_PREFIX + userId, familyId.toString());
        familyRedis.expire(USER_FAMILIES_PREFIX + userId, TTL);

        RefreshToken token = new RefreshToken(tokenId, familyId, userId, false);
        tokenRedis.opsForValue().set(TOKEN_PREFIX + tokenId, token, TTL);

        loginRefreshTokenCounter.increment();
        return tokenId;
    }

    public RotationResult rotate(UUID tokenId) {
        RefreshToken token = tokenRedis.opsForValue().get(TOKEN_PREFIX + tokenId);

        if (token == null) {
            throw new InvalidRefreshTokenException();
        }

        String familyStatus = familyRedis.opsForValue().get(FAMILY_PREFIX + token.familyId());
        if (!"valid".equals(familyStatus)) {
            throw new InvalidRefreshTokenException();
        }

        if (token.used()) {
            familyRedis.opsForValue().set(FAMILY_PREFIX + token.familyId(), "compromised", TTL);
            throw new InvalidRefreshTokenException();
        }

        RefreshToken used = new RefreshToken(token.tokenId(), token.familyId(), token.userId(), true);
        tokenRedis.opsForValue().set(TOKEN_PREFIX + tokenId, used, TTL);

        UUID newTokenId = UUID.randomUUID();
        RefreshToken newToken = new RefreshToken(newTokenId, token.familyId(), token.userId(), false);
        tokenRedis.opsForValue().set(TOKEN_PREFIX + newTokenId, newToken, TTL);

        rotationRefreshTokenCounter.increment();
        return new RotationResult(newTokenId, token.userId());
    }

    public void revokeAllSessions(UUID userId) {
        String userKey = USER_FAMILIES_PREFIX + userId;
        Set<String> familyIds = familyRedis.opsForSet().members(userKey);

        if (familyIds != null) {
            for (String familyId : familyIds) {
                familyRedis.opsForValue().set(FAMILY_PREFIX + familyId, "compromised", TTL);
            }
        }

        familyRedis.delete(userKey);
    }

    public record RotationResult(UUID newTokenId, UUID userId) {
    }
}
