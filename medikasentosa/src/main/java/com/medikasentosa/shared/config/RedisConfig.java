package com.medikasentosa.shared.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Konfigurasi Redis caching.
 *
 * <p>Mengaktifkan Spring Cache dengan Redis sebagai backend. Data disimpan
 * dalam format JSON yang mendukung DTO, List of DTOs, dan Java 8 Date/Time (JSR-310).</p>
 *
 * @author Ari
 * @since 1.0.0
 */
@Configuration
@EnableCaching
@ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis", matchIfMissing = false)
public class RedisConfig {

    /** TTL default 1 jam (fallback jika application.properties tidak terbaca). */
    private static final Duration DEFAULT_TTL = Duration.ofHours(1);

    /**
     * CacheManager dengan serializer JSON modern yang mendukung Java 8 Date/Time (JSR-310)
     * serta deserialisasi koleksi List tanpa TypeId mismatch.
     */
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType(Object.class)
                .build();

        mapper.activateDefaultTyping(
                ptv,
                ObjectMapper.DefaultTyping.EVERYTHING,
                JsonTypeInfo.As.WRAPPER_ARRAY
        );

        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(mapper);

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(DEFAULT_TTL)
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(jsonSerializer))
                .disableCachingNullValues();

        // TTL spesifik per cache (semua 1 jam untuk master data)
        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();
        Duration oneHour = Duration.ofHours(1);
        for (String name : new String[]{"poli-all", "poli-active", "poli-by-id", "jadwal-by-dokter", "obat-all"}) {
            cacheConfigs.put(name, defaultConfig.entryTtl(oneHour));
        }

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigs)
                .build();
    }
}
