package com.medikasentosa.shared.config;

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
 * dalam format JSON (GenericJackson2JsonRedisSerializer) agar readable
 * saat debugging via redis-cli.</p>
 *
 * <p><b>Fallback:</b> Jika Redis tidak tersedia, set
 * {@code spring.cache.type=none} di application.properties agar aplikasi
 * tetap berjalan normal tanpa cache.</p>
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
     * CacheManager dengan serializer JSON.
     * Key disimpan sebagai String, value sebagai JSON.
     */
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(DEFAULT_TTL)
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues();

        // TTL spesifik per cache (semua 1 jam untuk master data)
        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();
        Duration oneHour = Duration.ofHours(1);
        for (String name : new String[]{"poli-all", "poli-by-id", "jadwal-by-dokter", "obat-all"}) {
            cacheConfigs.put(name, defaultConfig.entryTtl(oneHour));
        }

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigs)
                .build();
    }
}