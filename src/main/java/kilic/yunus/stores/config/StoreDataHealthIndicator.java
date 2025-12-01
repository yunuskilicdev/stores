package kilic.yunus.stores.config;

import kilic.yunus.stores.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * Custom health indicator to check store data availability.
 */
@Primary
@Slf4j
@Component
@RequiredArgsConstructor
public class StoreDataHealthIndicator implements HealthIndicator {

    private final StoreRepository storeRepository;

    @Override
    public Health health() {
        try {
            int storeCount = storeRepository.count();

            if (storeCount > 0) {
                return Health.up()
                        .withDetail("totalStores", storeCount)
                        .withDetail("status", "Store data loaded successfully")
                        .build();
            } else {
                return Health.down()
                        .withDetail("totalStores", 0)
                        .withDetail("status", "No store data available")
                        .build();
            }
        } catch (Exception e) {
            log.error("Health check failed", e);
            return Health.down().withDetail("error", e.getMessage()).build();
        }
    }
}
