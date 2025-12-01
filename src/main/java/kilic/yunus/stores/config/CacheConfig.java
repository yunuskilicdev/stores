package kilic.yunus.stores.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.cache.CaffeineCacheMetrics;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Cache configuration using Caffeine with metrics support.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager(MeterRegistry meterRegistry) {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("nearestStores");

        Caffeine<Object, Object> caffeineBuilder =
                Caffeine.newBuilder()
                        .maximumSize(10_000) // Increased from 1000
                        .expireAfterWrite(6, TimeUnit.HOURS) // Increased from 10 minutes
                        .recordStats(); // Enable statistics recording

        cacheManager.setCaffeine(caffeineBuilder);

        // Register cache metrics with Micrometer
        cacheManager
                .getCacheNames()
                .forEach(
                        cacheName -> {
                            var cache = cacheManager.getCache(cacheName);
                            if (cache instanceof CaffeineCache caffeineCache) {
                                CaffeineCacheMetrics.monitor(
                                        meterRegistry, caffeineCache.getNativeCache(), cacheName);
                            }
                        });

        return cacheManager;
    }
}
