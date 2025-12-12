package kilic.yunus.stores.service.impl;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import kilic.yunus.stores.exception.InvalidCoordinatesException;
import kilic.yunus.stores.model.domain.Location;
import kilic.yunus.stores.model.domain.Store;
import kilic.yunus.stores.model.dto.StoreWithDistance;
import kilic.yunus.stores.repository.StoreRepository;
import kilic.yunus.stores.service.DistanceCalculator;
import kilic.yunus.stores.service.StoreService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

/**
 * Implementation of StoreService with caching support and metrics tracking.
 */
@Slf4j
@Service
public class StoreServiceImpl implements StoreService {

    private final StoreRepository storeRepository;
    private final DistanceCalculator distanceCalculator;
    private final MeterRegistry meterRegistry;
    private final Counter storeSearchCounter;
    private final Counter storeSearchErrorCounter;
    private final Timer storeSearchTimer;

    public StoreServiceImpl(
            StoreRepository storeRepository,
            DistanceCalculator distanceCalculator,
            MeterRegistry meterRegistry,
            @Qualifier("storeSearchCounter") Counter storeSearchCounter,
            @Qualifier("storeSearchErrorCounter") Counter storeSearchErrorCounter,
            @Qualifier("storeSearchTimer") Timer storeSearchTimer) {
        this.storeRepository = storeRepository;
        this.distanceCalculator = distanceCalculator;
        this.meterRegistry = meterRegistry;
        this.storeSearchCounter = storeSearchCounter;
        this.storeSearchErrorCounter = storeSearchErrorCounter;
        this.storeSearchTimer = storeSearchTimer;
    }

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
                storeSearchErrorCounter.increment();
                throw new InvalidCoordinatesException(
                        String.format(
                                "Invalid coordinates: latitude=%.6f, longitude=%.6f",
                                location.getLatitude(), location.getLongitude()));
            }

            List<Store> allStores = storeRepository.findAll();
            log.debug("Total stores loaded: {}", allStores.size());


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

            storeSearchCounter.increment();

            return storesWithDistances;

        } catch (InvalidCoordinatesException e) {
            storeSearchErrorCounter.increment();
            throw e;
        } catch (Exception e) {
            storeSearchErrorCounter.increment();
            log.error("Error finding nearest stores", e);
            throw e;
        } finally {
            sample.stop(storeSearchTimer);
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
