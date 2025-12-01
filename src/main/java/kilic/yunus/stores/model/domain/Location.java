package kilic.yunus.stores.model.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a geographical location with latitude and longitude coordinates.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Location {
    private double latitude;
    private double longitude;

    /**
     * Validates if the coordinates are within valid ranges. Latitude: -90 to 90, Longitude: -180 to
     * 180
     */
    public boolean isValid() {
        return latitude >= -90 && latitude <= 90 && longitude >= -180 && longitude <= 180;
    }
}
