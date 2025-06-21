package com.fourstars.FourStars.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;

import java.time.Duration;

@Configuration
@EnableCaching
public class RedisCacheConfig {

        @Bean
        public RedisCacheConfiguration cacheConfiguration() {
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.registerModule(new JavaTimeModule());
                objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance,
                                ObjectMapper.DefaultTyping.NON_FINAL);

                return RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(Duration.ofMinutes(30))
                                .disableCachingNullValues()
                                .serializeValuesWith(SerializationPair
                                                .fromSerializer(new GenericJackson2JsonRedisSerializer(objectMapper)));
        }

        @Bean
        public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {
                RedisCacheConfiguration defaultConfiguration = cacheConfiguration();

                return (builder) -> builder
                                .withCacheConfiguration("roles",
                                                defaultConfiguration.entryTtl(Duration.ofHours(1)))

                                .withCacheConfiguration("plans",
                                                defaultConfiguration.entryTtl(Duration.ofDays(1)))

                                .withCacheConfiguration("categories",
                                                defaultConfiguration.entryTtl(Duration.ofHours(4)))

                                .withCacheConfiguration("badges",
                                                defaultConfiguration.entryTtl(Duration.ofDays(1)));
        }
}
