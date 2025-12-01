package kilic.yunus.stores.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for custom application metrics.
 */
@Configuration
public class MetricsConfig {

    @Bean
    public Counter storeSearchCounter(MeterRegistry registry) {
        return Counter.builder("store.search.requests")
                .description("Total number of store search requests")
                .tag("type", "nearest")
                .register(registry);
    }

    @Bean
    public Counter storeSearchErrorCounter(MeterRegistry registry) {
        return Counter.builder("store.search.errors")
                .description("Total number of store search errors")
                .tag("type", "error")
                .register(registry);
    }

    @Bean
    public Timer storeSearchTimer(MeterRegistry registry) {
        return Timer.builder("store.search.duration")
                .description("Time taken to search for nearest stores")
                .tag("operation", "findNearest")
                .register(registry);
    }

    @Bean
    public Counter cacheHitCounter(MeterRegistry registry) {
        return Counter.builder("store.cache.hits")
                .description("Number of cache hits")
                .register(registry);
    }

    @Bean
    public Counter cacheMissCounter(MeterRegistry registry) {
        return Counter.builder("store.cache.misses")
                .description("Number of cache misses")
                .register(registry);
    }
}
