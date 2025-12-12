package kilic.yunus.stores.repository.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import kilic.yunus.stores.exception.StoreDataException;
import kilic.yunus.stores.model.domain.Store;
import kilic.yunus.stores.repository.StoreRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Repository implementation that loads stores from JSON file. Stores are loaded once at startup and
 * kept in memory.
 */
@Slf4j
@Repository
public class JsonStoreRepository implements StoreRepository {

    private final Map<String, Store> storeCache = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;
    private final Validator validator;
    private final Resource storeDataFile;

    public JsonStoreRepository(
            ObjectMapper objectMapper,
            Validator validator,
            @Value("${stores.data.file:classpath:stores.json}") Resource storeDataFile) {
        this.objectMapper = objectMapper;
        this.validator = validator;
        this.storeDataFile = storeDataFile;
    }

    @PostConstruct
    public void loadStores() {
        log.info("Loading stores from: {}", storeDataFile);

        try (InputStream inputStream = storeDataFile.getInputStream()) {
            // Read JSON structure
            JsonNode rootNode = objectMapper.readTree(inputStream);

            // The JSON structure has "stores" as the root key
            JsonNode storesNode = rootNode.get("stores");

            if (storesNode == null) {
                throw new StoreDataException("Invalid JSON structure: 'stores' key not found");
            }

            // Parse stores array
            List<Store> stores =
                    objectMapper.convertValue(storesNode, new TypeReference<>() {
                    });

            // Validate each store
            int invalidCount = 0;
            StringBuilder validationErrors = new StringBuilder();

            for (int i = 0; i < stores.size(); i++) {
                Store store = stores.get(i);
                Set<ConstraintViolation<Store>> violations = validator.validate(store);

                if (!violations.isEmpty()) {
                    invalidCount++;
                    validationErrors.append(String.format("%n[Store #%d - UUID: %s]:%n",
                            i + 1, store.getUuid()));

                    for (ConstraintViolation<Store> violation : violations) {
                        validationErrors.append(String.format("  - %s: %s (value: %s)%n",
                                violation.getPropertyPath(),
                                violation.getMessage(),
                                violation.getInvalidValue()));
                    }
                }
            }

            // Fail fast if any validation errors found
            if (invalidCount > 0) {
                String errorMessage = String.format(
                        "Store data validation failed! %d out of %d stores have validation errors:%s",
                        invalidCount, stores.size(), validationErrors);

                log.error(errorMessage);
                throw new StoreDataException(errorMessage);
            }

            stores.forEach(
                    store -> {
                        if (store.getUuid() != null) {
                            storeCache.put(store.getUuid(), store);
                        }
                    });

            log.info("Successfully validated and loaded {} stores", storeCache.size());
            long storesWithLocation = stores.stream().filter(Store::hasValidLocation).count();
            log.info("Stores with valid location: {}", storesWithLocation);

        } catch (StoreDataException e) {
            log.error("FATAL: Cannot start application - store data invalid", e);
            throw e;
        } catch (IOException e) {
            log.error("Failed to load stores from JSON file", e);
            throw new StoreDataException("Failed to load store data", e);
        }
    }

    @Override
    public List<Store> findAll() {
        return new ArrayList<>(storeCache.values());
    }

    @Override
    public int count() {
        return storeCache.size();
    }
}
