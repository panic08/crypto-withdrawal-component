package com.casino.replenishmentapi.configuration;

import com.casino.replenishmentapi.model.CryptoReplenishmentSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfiguration {
    @Bean
    public ReactiveRedisTemplate<String, CryptoReplenishmentSession> reactiveRedisTemplate(
            ReactiveRedisConnectionFactory factory) {
        RedisSerializationContext<String, CryptoReplenishmentSession> serializationContext =
                RedisSerializationContext.<String, CryptoReplenishmentSession>newSerializationContext(
                                new StringRedisSerializer())
                        .value(new Jackson2JsonRedisSerializer<>(CryptoReplenishmentSession.class))
                        .build();

        return new ReactiveRedisTemplate<>(factory, serializationContext);
    }
}
