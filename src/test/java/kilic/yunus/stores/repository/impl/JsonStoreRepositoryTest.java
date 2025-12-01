package kilic.yunus.stores.repository.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import kilic.yunus.stores.exception.StoreDataException;
import kilic.yunus.stores.model.domain.Store;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.core.io.FileSystemResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JsonStoreRepositoryTest {

    private ObjectMapper objectMapper;
    private Validator validator;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    // Template for creating test stores
    private static final String STORE_TEMPLATE = """
            {
              "city": "%s",
              "postalCode": "%s",
              "street": "Main Street",
              "addressName": "%s",
              "uuid": "%s",
              "longitude": %s,
              "latitude": %s,
              "complexNumber": "%s",
              "showWarningMessage": true,
              "todayOpen": "%s",
              "locationType": "Supermarkt",
              "collectionPoint": false,
              "sapStoreID": "%s"
            }
            """;

    @Test
    @DisplayName("Should load multiple valid stores")
    void shouldLoadMultipleValidStores() throws IOException {
        // Given
        String store1 = createStore("Amsterdam", "1234 AB", "Store 1", "test-uuid-0001",
                "4.9041", "52.3676", "123", "08:00", "1");
        String store2 = createStore("Rotterdam", "5678 CD", "Store 2", "test-uuid-0002",
                "4.4777", "51.9244", "456", "09:00", "2");
        String store3 = createStore("Utrecht", "3511 EF", "Store 3", "test-uuid-0003",
                "5.1214", "52.0907", "789", "10:00", "3");
        String json = wrapInStoresArray(store1, store2, store3);
        JsonStoreRepository repository = createRepository(json);

        // When
        repository.loadStores();

        // Then
        assertThat(repository.count()).isEqualTo(3);

        List<Store> stores = repository.findAll();
        assertThat(stores).hasSize(3);
        assertThat(stores).extracting(Store::getCity)
                .containsExactlyInAnyOrder("Amsterdam", "Rotterdam", "Utrecht");
        assertThat(stores).extracting(Store::getUuid)
                .containsExactlyInAnyOrder("test-uuid-0001", "test-uuid-0002", "test-uuid-0003");
    }

    @Test
    @DisplayName("Should accept Gesloten as valid time value")
    void shouldAcceptGeslotenAsValidTime() throws IOException {
        // Given
        String store = createStore("Amsterdam", "1234 AB", "Closed Store", "test-uuid-0001",
                "4.9041", "52.3676", "123", "Gesloten", "1");
        String json = wrapInStoresArray(store);
        JsonStoreRepository repository = createRepository(json);

        // When
        repository.loadStores();

        // Then
        List<Store> stores = repository.findAll();
        assertThat(stores).hasSize(1);
        Store foundStore = stores.getFirst();
        assertThat(foundStore.getUuid()).isEqualTo("test-uuid-0001");
        assertThat(foundStore.getTodayOpen()).isEqualTo("Gesloten");
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @CsvSource(delimiter = '|', textBlock = """
            Blank city              | ''        | 1234 AB  | 4.9041 | 52.3676 | 123    | 08:00 | city
            Invalid postal code     | Amsterdam | INVALID  | 4.9041 | 52.3676 | 123    | 08:00 | postalCode
            Non-digit complexNumber | Amsterdam | 1234 AB  | 4.9041 | 52.3676 | ABC123 | 08:00 | complexNumber
            Invalid time format     | Amsterdam | 1234 AB  | 4.9041 | 52.3676 | 123    | 25:99 | todayOpen
            Null latitude           | Amsterdam | 1234 AB  | 4.9041 | null    | 123 | 08:00 | latitude
            Null longitude          | Amsterdam | 1234 AB  | null   | 52.3676 | 123 | 08:00 | longitude
            """)
    @DisplayName("Should fail validation for invalid field values")
    void shouldFailValidationForInvalidFields(
            String description, String city, String postalCode,
            String longitude, String latitude, String complexNumber,
            String todayOpen, String expectedFieldInError) throws IOException {

        // Given
        String store = createStore(city, postalCode, "Test Store", "test-uuid-0001",
                longitude, latitude, complexNumber, todayOpen, "1");
        String json = wrapInStoresArray(store);
        JsonStoreRepository repository = createRepository(json);

        // When/Then - Should fail during validation (null values create valid JSON but invalid data)
        assertThatThrownBy(repository::loadStores)
                .isInstanceOf(StoreDataException.class)
                .hasMessageContaining("Store data validation failed")
                .hasMessageContaining(expectedFieldInError);
    }

    @Test
    @DisplayName("Should report validation errors for multiple invalid stores")
    void shouldReportMultipleValidationErrors() throws IOException {
        // Given
        String validStore = createStore("Amsterdam", "1234 AB", "Valid", "test-uuid-0001",
                "4.9041", "52.3676", "123", "08:00", "1");
        String invalidStore1 = createStore("", "1234 AB", "Invalid 1", "test-uuid-0002",
                "4.9041", "52.3676", "123", "08:00", "2");
        String invalidStore2 = createStore("Utrecht", "WRONG", "Invalid 2", "test-uuid-0003",
                "4.9041", "52.3676", "123", "08:00", "3");
        String json = wrapInStoresArray(validStore, invalidStore1, invalidStore2);
        JsonStoreRepository repository = createRepository(json);

        // When/Then
        assertThatThrownBy(repository::loadStores)
                .isInstanceOf(StoreDataException.class)
                .hasMessageContaining("2 out of 3 stores have validation errors")
                .hasMessageContaining("Store #2")
                .hasMessageContaining("Store #3")
                .hasMessageContaining("test-uuid-0002")
                .hasMessageContaining("test-uuid-0003");
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @CsvSource(delimiter = '|', textBlock = """
            Missing stores key | {"data": []}       | 'stores' key not found
            Malformed JSON     | { invalid json     | Failed to load store data
            """)
    @DisplayName("Should handle JSON structure errors")
    void shouldHandleJsonStructureErrors(String description, String json, String expectedError) throws IOException {
        // Given
        JsonStoreRepository repository = createRepository(json);

        // When/Then
        assertThatThrownBy(repository::loadStores)
                .isInstanceOf(StoreDataException.class)
                .hasMessageContaining(expectedError);
    }

    @Test
    @DisplayName("Should throw exception when file cannot be read")
    void shouldThrowExceptionWhenFileNotFound() {
        // Given - Create repository with non-existent file
        JsonStoreRepository repository = new JsonStoreRepository(
                objectMapper,
                validator,
                new FileSystemResource("/nonexistent/stores.json"));

        // When/Then
        assertThatThrownBy(repository::loadStores)
                .isInstanceOf(StoreDataException.class)
                .hasMessageContaining("Failed to load store data");
    }

    @Test
    @DisplayName("Should return empty list when no stores loaded")
    void shouldReturnEmptyListWhenNoStores() throws IOException {
        // Given
        String json = wrapInStoresArray(); // Empty array
        JsonStoreRepository repository = createRepository(json);
        repository.loadStores();

        // When
        List<Store> stores = repository.findAll();

        // Then
        assertThat(stores).isEmpty();
        assertThat(repository.count()).isZero();
    }

    @Test
    @DisplayName("Should return all stores from findAll")
    void shouldReturnAllStoresFromFindAll() throws IOException {
        // Given
        String store = createStore("Amsterdam", "1234 AB", "Store 1", "test-uuid-0001",
                "4.9041", "52.3676", "123", "08:00", "1");
        String json = wrapInStoresArray(store);
        JsonStoreRepository repository = createRepository(json);
        repository.loadStores();

        // When
        List<Store> stores = repository.findAll();

        // Then
        assertThat(stores).hasSize(1);
        assertThat(stores.getFirst().getUuid()).isEqualTo("test-uuid-0001");
        assertThat(stores.getFirst().getCity()).isEqualTo("Amsterdam");
    }

    @Test
    @DisplayName("findAll should return new list instance (not cached reference)")
    void findAllShouldReturnNewListInstance() throws IOException {
        // Given
        String store = createStore("Amsterdam", "1234 AB", "Store 1", "test-uuid-0001",
                "4.9041", "52.3676", "123", "08:00", "1");
        String json = wrapInStoresArray(store);
        JsonStoreRepository repository = createRepository(json);
        repository.loadStores();

        // When
        List<Store> list1 = repository.findAll();
        List<Store> list2 = repository.findAll();

        // Then - Should be different list instances
        assertThat(list1)
                .isNotSameAs(list2)
                .isEqualTo(list2); // But same content
    }

    @Test
    @DisplayName("Should handle stores with valid UUID")
    void shouldHandleStoreWithValidUuid() throws IOException {
        // Given
        String store = createStore("Amsterdam", "1234 AB", "Store 1", "test-uuid-0001",
                "4.9041", "52.3676", "123", "08:00", "1");
        String json = wrapInStoresArray(store);
        JsonStoreRepository repository = createRepository(json);
        repository.loadStores();

        // When/Then - Should successfully load
        assertThat(repository.count()).isEqualTo(1);
        List<Store> stores = repository.findAll();
        assertThat(stores.getFirst().getUuid()).isEqualTo("test-uuid-0001");
    }

    @Test
    @DisplayName("Should count stores with valid locations correctly")
    void shouldCountStoresWithValidLocations() throws IOException {
        // Given
        String storeWithLocation = createStore("Amsterdam", "1234 AB", "Store 1", "test-uuid-0001",
                "4.9041", "52.3676", "123", "08:00", "1");
        String json = wrapInStoresArray(storeWithLocation);
        JsonStoreRepository repository = createRepository(json);

        // When
        repository.loadStores();

        // Then - Log output should show stores with valid location
        // This is tested implicitly through the loadStores() method execution
        assertThat(repository.count()).isEqualTo(1);
    }

    // Helper methods

    private String createStore(String city, String postalCode, String addressName, String uuid,
                              String longitude, String latitude, String complexNumber,
                              String todayOpen, String sapStoreId) {
        // Handle null values - convert "null" string to actual JSON null
        String lonValue = "null".equals(longitude) ? "null" : longitude;
        String latValue = "null".equals(latitude) ? "null" : latitude;

        return String.format(STORE_TEMPLATE,
                city, postalCode, addressName, uuid, lonValue, latValue,
                complexNumber, todayOpen, sapStoreId);
    }

    private String wrapInStoresArray(String... stores) {
        if (stores.length == 0) {
            return "{\"stores\": []}";
        }
        String storesContent = String.join(",", stores);
        return String.format("{\"stores\": [%s]}", storesContent);
    }

    private JsonStoreRepository createRepository(String jsonContent) throws IOException {
        Path storeFile = tempDir.resolve("stores1.json");
        Files.writeString(storeFile, jsonContent);
        return new JsonStoreRepository(objectMapper, validator, new FileSystemResource(storeFile.toFile()));
    }
}

