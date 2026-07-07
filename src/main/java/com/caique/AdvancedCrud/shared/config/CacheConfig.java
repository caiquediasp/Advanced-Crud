package com.caique.AdvancedCrud.shared.config;

import com.caique.AdvancedCrud.address.viacep.ViaCepResponse;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofDays(3))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new JacksonJsonRedisSerializer<>(ViaCepResponse.class)));

        return RedisCacheManager
                .builder(connectionFactory)
                .cacheDefaults(config)
                .build();
    }
}
