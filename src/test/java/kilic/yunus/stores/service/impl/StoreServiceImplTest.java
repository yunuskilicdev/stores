package kilic.yunus.stores.service.impl;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import kilic.yunus.stores.exception.InvalidCoordinatesException;
import kilic.yunus.stores.model.domain.Location;
import kilic.yunus.stores.model.domain.Store;
import kilic.yunus.stores.model.dto.StoreWithDistance;
import kilic.yunus.stores.repository.StoreRepository;
import kilic.yunus.stores.service.DistanceCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit tests for StoreServiceImpl. All distances are in kilometers.
 */
@ExtendWith(MockitoExtension.class)
class StoreServiceImplTest {

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private DistanceCalculator distanceCalculator;

    private MeterRegistry meterRegistry;

    private StoreServiceImpl storeService;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        storeService = new StoreServiceImpl(storeRepository, distanceCalculator, meterRegistry);
    }

    @Test
    void shouldFindNearestStores() {
        // Given
        Location queryLocation = new Location(52.3676, 4.9041);

        Store store1 = createStore("1", "Store 1", 52.3700, 4.9000);
        Store store2 = createStore("2", "Store 2", 52.3650, 4.9100);
        Store store3 = createStore("3", "Store 3", 52.4000, 4.9500);

        when(storeRepository.findAll()).thenReturn(Arrays.asList(store1, store2, store3));
        when(distanceCalculator.calculateDistance(queryLocation, store1.getLocation()))
                .thenReturn(1.5);
        when(distanceCalculator.calculateDistance(queryLocation, store2.getLocation()))
                .thenReturn(0.8);
        when(distanceCalculator.calculateDistance(queryLocation, store3.getLocation()))
                .thenReturn(5.2);

        // When
        List<StoreWithDistance> result = storeService.findNearestStores(queryLocation, 2);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getStore().getUuid()).isEqualTo("test-uuid-2"); // Closest
        assertThat(result.get(1).getStore().getUuid()).isEqualTo("test-uuid-1");
        assertThat(result.get(0).getDistance()).isEqualTo(0.8);
    }

    @Test
    void shouldThrowExceptionForInvalidCoordinates() {
        Location invalidLocation = new Location(100.0, 4.9041); // Invalid latitude

        assertThrows(
                InvalidCoordinatesException.class,
                () -> storeService.findNearestStores(invalidLocation, 5));
    }

    @Test
    void shouldFilterOutStoresWithoutValidLocation() {
        // Given
        Location queryLocation = new Location(52.3676, 4.9041);

        Store validStore = createStore("1", "Valid Store", 52.3700, 4.9000);
        Store invalidStore = createStore("2", "Invalid Store", null, null);

        when(storeRepository.findAll()).thenReturn(Arrays.asList(validStore, invalidStore));
        when(distanceCalculator.calculateDistance(any(), any())).thenReturn(1.0);

        // When
        List<StoreWithDistance> result = storeService.findNearestStores(queryLocation, 5);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStore().getUuid()).isEqualTo("test-uuid-1");
    }

    @Test
    void shouldRoundDistancesToTwoDecimals() {
        // Given
        Location queryLocation = new Location(52.3676, 4.9041);
        Store store = createStore("1", "Store 1", 52.3700, 4.9000);

        when(storeRepository.findAll()).thenReturn(List.of(store));
        when(distanceCalculator.calculateDistance(any(), any())).thenReturn(1.2345678);

        // When
        List<StoreWithDistance> result = storeService.findNearestStores(queryLocation, 5);

        // Then
        assertThat(result.get(0).getDistance()).isEqualTo(1.23);
    }

    @Test
    void shouldReturnAllStores() {
        // Given
        List<Store> stores =
                Arrays.asList(
                        createStore("1", "Store 1", 52.3700, 4.9000),
                        createStore("2", "Store 2", 52.3650, 4.9100));
        when(storeRepository.findAll()).thenReturn(stores);

        // When
        List<Store> result = storeService.getAllStores();

        // Then
        assertThat(result).hasSize(2);
    }

    @Test
    void shouldReturnTotalStoreCount() {
        // Given
        when(storeRepository.count()).thenReturn(42);

        // When
        int count = storeService.getTotalStoreCount();

        // Then
        assertThat(count).isEqualTo(42);
    }

    @Test
    void shouldHandleRuntimeExceptionDuringSearch() {
        // Given
        Location validLocation = new Location(52.3676, 4.9041);
        when(storeRepository.findAll()).thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> storeService.findNearestStores(validLocation, 5));

        assertThat(exception.getMessage()).contains("Database connection failed");

        // Verify error metric was incremented
        assertThat(meterRegistry.counter("store.search.errors", "reason", "unknown").count())
                .isEqualTo(1.0);
    }

    private Store createStore(String id, String name, Double latitude, Double longitude) {
        return Store.builder()
                .sapStoreID(id)
                .addressName(name)
                .latitude(latitude)
                .longitude(longitude)
                .city("Amsterdam")
                .postalCode("1012 AB")
                .street("Test Street")
                .uuid("test-uuid-" + id)
                .complexNumber(id)
                .showWarningMessage(false)
                .locationType("Supermarkt")
                .collectionPoint(true)
                .build();
    }
}
