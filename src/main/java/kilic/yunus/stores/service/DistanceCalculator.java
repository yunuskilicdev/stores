package kilic.yunus.stores.service;

import kilic.yunus.stores.model.domain.Location;

/**
 * Interface for distance calculation between two geographic locations. Distances are calculated in
 * kilometers.
 */
public interface DistanceCalculator {

    /**
     * Calculate the distance between two locations in kilometers.
     *
     * @param from Starting location
     * @param to   Ending location
     * @return Distance in kilometers
     */
    double calculateDistance(Location from, Location to);
}
