package kilic.yunus.stores.service;

import kilic.yunus.stores.model.domain.Location;
import kilic.yunus.stores.model.domain.Store;
import kilic.yunus.stores.model.dto.StoreWithDistance;

import java.util.List;

/**
 * Service for store-related operations.
 */
public interface StoreService {

    /**
     * Find the nearest stores to a given location.
     *
     * @param location The location to search from
     * @param limit    Maximum number of stores to return
     * @return List of stores with distances, sorted by distance
     */
    List<StoreWithDistance> findNearestStores(Location location, int limit);

    /**
     * Get all stores.
     *
     * @return List of all stores
     */
    List<Store> getAllStores();

    /**
     * Get total number of stores.
     *
     * @return Total count of stores
     */
    int getTotalStoreCount();
}
