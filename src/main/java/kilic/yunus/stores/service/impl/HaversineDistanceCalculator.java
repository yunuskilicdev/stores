package kilic.yunus.stores.service.impl;

import kilic.yunus.stores.model.domain.Location;
import kilic.yunus.stores.service.DistanceCalculator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class HaversineDistanceCalculator implements DistanceCalculator {

    private static final double EARTH_RADIUS_KM = 6371.0;

    @Override
    public double calculateDistance(Location from, Location to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("Locations cannot be null");
        }

        double lat1 = Math.toRadians(from.getLatitude());
        double lon1 = Math.toRadians(from.getLongitude());
        double lat2 = Math.toRadians(to.getLatitude());
        double lon2 = Math.toRadians(to.getLongitude());

        // Haversine formula
        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;

        double a =
                Math.sin(dLat / 2) * Math.sin(dLat / 2)
                        + Math.cos(lat1) * Math.cos(lat2) * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double distance = EARTH_RADIUS_KM * c;

        log.debug(
                "Calculated distance from ({}, {}) to ({}, {}): {} km",
                from.getLatitude(),
                from.getLongitude(),
                to.getLatitude(),
                to.getLongitude(),
                distance);

        return distance;
    }
}
