package kilic.yunus.stores.model.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Store implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // Time pattern for HH:mm format or "Gesloten" (closed in Dutch)
    private static final String TIME_PATTERN = "^(([0-1]\\d|2[0-3]):[0-5]\\d|Gesloten)$";

    // Postal code pattern for Dutch postal codes (e.g., 1234 AB)
    private static final String POSTAL_CODE_PATTERN = "^\\d{4}\\s?[A-Z]{2}$";

    // Constant for closed status
    private static final String CLOSED_STATUS = "Gesloten";

    @NotBlank(message = "City is mandatory and cannot be blank")
    @Size(min = 1, max = 100, message = "City name must be between 1 and 100 characters")
    @JsonProperty("city")
    private String city;

    @NotBlank(message = "Postal code is mandatory")
    @Pattern(
            regexp = POSTAL_CODE_PATTERN,
            message =
                    "Postal code must follow Dutch format: 4 digits followed by 2 uppercase letters (e.g., 1234 AB)")
    @JsonProperty("postalCode")
    private String postalCode;

    @NotBlank(message = "Street is mandatory")
    @Size(min = 1, max = 200, message = "Street name must be between 1 and 200 characters")
    @JsonProperty("street")
    private String street;

    @Size(max = 50, message = "Street2 must not exceed 50 characters")
    @JsonProperty("street2")
    private String street2;

    @Size(max = 50, message = "Street3 must not exceed 50 characters")
    @JsonProperty("street3")
    private String street3;

    @NotBlank(message = "Address name is mandatory")
    @Size(min = 1, max = 300, message = "Address name must be between 1 and 300 characters")
    @JsonProperty("addressName")
    private String addressName;

    @NotBlank(message = "UUID is mandatory")
    @Size(min = 10, max = 50, message = "UUID must be between 10 and 50 characters")
    @JsonProperty("uuid")
    private String uuid;

    @JsonDeserialize(using = CoordinateDeserializer.class)
    @NotNull(message = "Longitude is mandatory")
    @JsonProperty("longitude")
    private Double longitude;

    @JsonDeserialize(using = CoordinateDeserializer.class)
    @NotNull(message = "Latitude is mandatory")
    @JsonProperty("latitude")
    private Double latitude;

    @NotBlank(message = "Complex number is mandatory")
    @Size(min = 1, max = 20, message = "Complex number must be between 1 and 20 characters")
    @Pattern(regexp = "^\\d+$", message = "Complex number must contain only digits")
    @JsonProperty("complexNumber")
    private String complexNumber;

    @NotNull(message = "Show warning message flag must not be null")
    @JsonProperty("showWarningMessage")
    private Boolean showWarningMessage;

    @Pattern(
            regexp = TIME_PATTERN,
            message = "Today open time must be in HH:mm format (e.g., 08:00) or 'Gesloten'")
    @JsonProperty("todayOpen")
    private String todayOpen;

    /**
     * Type of store location (e.g., Supermarkt, SupermarktPuP). Must not be blank and limited to 50
     * characters.
     */
    @NotBlank(message = "Location type is mandatory")
    @Size(min = 1, max = 50, message = "Location type must be between 1 and 50 characters")
    @JsonProperty("locationType")
    private String locationType;

    @JsonProperty("collectionPoint")
    private Boolean collectionPoint;

    @Size(max = 20, message = "SAP Store ID must not exceed 20 characters")
    @Pattern(regexp = "^\\d*$", message = "SAP Store ID must contain only digits")
    @JsonProperty("sapStoreID")
    private String sapStoreID;

    @Pattern(
            regexp = TIME_PATTERN,
            message = "Today close time must be in HH:mm format (e.g., 20:00) or 'Gesloten'")
    @JsonProperty("todayClose")
    private String todayClose;

    /**
     * Validates the logical consistency of the store data. This method performs custom business
     * validation beyond field-level constraints.
     *
     * @return true if the store data is logically consistent
     */
    public boolean isValid() {
        // Check time consistency
        return isTimeLogicValid();
    }

    /**
     * Validates the time logic for opening and closing hours.
     *
     * <p>Edge Cases Handled: - Both null/empty: Valid (hours not specified) - Both "Gesloten": Valid
     * (store is closed) - Mixed "Gesloten" and time: Invalid - Open without Close: Invalid - Close
     * without Open: Invalid - Close before Open: Invalid (for same day)
     *
     * @return true if time logic is valid
     */
    private boolean isTimeLogicValid() {
        boolean hasOpen = todayOpen != null && !todayOpen.trim().isEmpty();
        boolean hasClose = todayClose != null && !todayClose.trim().isEmpty();

        // Case 1: Both times are empty/null - Valid (hours not specified)
        if (!hasOpen && !hasClose) {
            return true;
        }

        // Case 2: Both times are "Gesloten" - Valid (store is closed today)
        if (hasOpen
                && hasClose
                && CLOSED_STATUS.equals(todayOpen.trim())
                && CLOSED_STATUS.equals(todayClose.trim())) {
            return true;
        }

        // Case 3: One is "Gesloten" and other is not - Invalid
        if (hasOpen
                && CLOSED_STATUS.equals(todayOpen.trim())
                && hasClose
                && !CLOSED_STATUS.equals(todayClose.trim())) {
            return false;
        }
        if (hasClose
                && CLOSED_STATUS.equals(todayClose.trim())
                && hasOpen
                && !CLOSED_STATUS.equals(todayOpen.trim())) {
            return false;
        }

        // Case 4: Only one time is provided - Invalid (need both or neither)
        if (hasOpen && !hasClose) {
            return false;
        }
        if (!hasOpen && hasClose) {
            return false;
        }

        // Case 5: Both times are provided and are actual times (not "Gesloten")
        // Validate that close time is after open time
        if (hasOpen
                && hasClose
                && !CLOSED_STATUS.equals(todayOpen.trim())
                && !CLOSED_STATUS.equals(todayClose.trim())) {
            return isCloseTimeAfterOpenTime(todayOpen.trim(), todayClose.trim());
        }

        return true;
    }

    /**
     * Checks if the close time is after the open time. Assumes times are in HH:mm format and have
     * already passed regex validation.
     *
     * @param openTime  opening time in HH:mm format
     * @param closeTime closing time in HH:mm format
     * @return true if close time is after open time
     */
    private boolean isCloseTimeAfterOpenTime(String openTime, String closeTime) {
        try {
            String[] openParts = openTime.split(":");
            String[] closeParts = closeTime.split(":");

            int openHour = Integer.parseInt(openParts[0]);
            int openMinute = Integer.parseInt(openParts[1]);
            int closeHour = Integer.parseInt(closeParts[0]);
            int closeMinute = Integer.parseInt(closeParts[1]);

            // Convert to minutes since midnight for easy comparison
            int openTotalMinutes = openHour * 60 + openMinute;
            int closeTotalMinutes = closeHour * 60 + closeMinute;

            // Close time must be after open time
            // Edge case: if they're equal (e.g., 08:00 to 08:00), that's invalid
            return closeTotalMinutes > openTotalMinutes;

        } catch (Exception e) {
            // If parsing fails, consider it invalid
            return false;
        }
    }

    /**
     * Converts the store's latitude and longitude to a Location object.
     *
     * @return Location object with the store's coordinates, or null if coordinates are invalid
     */
    public Location getLocation() {
        if (latitude == null || longitude == null) {
            return null;
        }

        return new Location(latitude, longitude);
    }

    /**
     * Checks if the store has valid location coordinates. Valid means: both latitude and longitude
     * are present and are within valid ranges.
     *
     * @return true if the store has valid location coordinates
     */
    public boolean hasValidLocation() {
        if (latitude == null || longitude == null) {
            return false;
        }

        return latitude >= -90 && latitude <= 90 && longitude >= -180 && longitude <= 180;
    }
}
