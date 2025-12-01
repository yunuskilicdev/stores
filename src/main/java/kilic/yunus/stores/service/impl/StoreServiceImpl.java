package kilic.yunus.stores.service.impl;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import kilic.yunus.stores.exception.InvalidCoordinatesException;
import kilic.yunus.stores.model.domain.Location;
import kilic.yunus.stores.model.domain.Store;
import kilic.yunus.stores.model.dto.StoreWithDistance;
import kilic.yunus.stores.repository.StoreRepository;
import kilic.yunus.stores.service.DistanceCalculator;
import kilic.yunus.stores.service.StoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

/**
 * Implementation of StoreService with caching support and metrics tracking.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StoreServiceImpl implements StoreService {

    private static final String STORE_SEARCH_ERROR_METRIC = "store.search.errors";
    private static final String METRIC_TAG = "reason";
    private final StoreRepository storeRepository;
    private final DistanceCalculator distanceCalculator;
    private final MeterRegistry meterRegistry;

    @Override
    @Cacheable(
            value = "nearestStores",
            key = "#location.latitude + '_' + #location.longitude + '_' + #limit")
    public List<StoreWithDistance> findNearestStores(Location location, int limit) {
        Timer.Sample sample = Timer.start(meterRegistry);

        try {
            log.info(
                    "Finding {} nearest stores to location ({}, {})",
                    limit,
                    location.getLatitude(),
                    location.getLongitude());

            // Validate location
            if (!location.isValid()) {
                meterRegistry.counter(STORE_SEARCH_ERROR_METRIC, METRIC_TAG, "invalid_coordinates").increment();
                throw new InvalidCoordinatesException(
                        String.format(
                                "Invalid coordinates: latitude=%.6f, longitude=%.6f",
                                location.getLatitude(), location.getLongitude()));
            }

            List<Store> allStores = storeRepository.findAll();
            log.debug("Total stores loaded: {}", allStores.size());

            meterRegistry.gauge("store.count", allStores.size());

            List<StoreWithDistance> storesWithDistances =
                    allStores.stream()
                            .filter(Store::hasValidLocation)
                            .map(
                                    store -> {
                                        double distance =
                                                distanceCalculator.calculateDistance(location, store.getLocation());

                                        return StoreWithDistance.builder()
                                                .store(store)
                                                .distance(Math.round(distance * 100.0) / 100.0) // Round to 2 decimals
                                                .build();
                                    })
                            .sorted(Comparator.comparingDouble(StoreWithDistance::getDistance))
                            .limit(limit)
                            .toList();

            log.info("Found {} nearest stores", storesWithDistances.size());

            meterRegistry.counter("store.search.requests", "status", "success").increment();
            meterRegistry
                    .counter("store.search.results", "count", String.valueOf(storesWithDistances.size()))
                    .increment();

            return storesWithDistances;

        } catch (InvalidCoordinatesException e) {
            meterRegistry.counter(STORE_SEARCH_ERROR_METRIC, METRIC_TAG, "invalid_coordinates").increment();
            throw e;
        } catch (Exception e) {
            meterRegistry.counter(STORE_SEARCH_ERROR_METRIC, METRIC_TAG, "unknown").increment();
            log.error("Error finding nearest stores", e);
            throw e;
        } finally {
            sample.stop(meterRegistry.timer("store.search.duration", "operation", "findNearest"));
        }
    }

    @Override
    public List<Store> getAllStores() {
        log.info("Retrieving all stores");
        return storeRepository.findAll();
    }

    @Override
    public int getTotalStoreCount() {
        return storeRepository.count();
    }
}
