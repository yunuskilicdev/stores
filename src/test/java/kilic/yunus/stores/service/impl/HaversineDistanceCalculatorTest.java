package kilic.yunus.stores.service.impl;

import kilic.yunus.stores.model.domain.Location;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HaversineDistanceCalculatorTest {

    private HaversineDistanceCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new HaversineDistanceCalculator();
    }

    @Test
    void shouldCalculateDistanceBetweenAmsterdamAndRotterdam() {
        // Amsterdam coordinates
        Location amsterdam = new Location(52.3676, 4.9041);
        // Rotterdam coordinates
        Location rotterdam = new Location(51.9244, 4.4777);

        double distanceKm = calculator.calculateDistance(amsterdam, rotterdam);

        // Actual distance is approximately 57-58 km
        assertThat(distanceKm).isCloseTo(57.6, within(1.0));
    }

    @Test
    void shouldCalculateDistanceBetweenAmsterdamAndUtrecht() {
        // Amsterdam coordinates
        Location amsterdam = new Location(52.3676, 4.9041);
        // Utrecht coordinates
        Location utrecht = new Location(52.0907, 5.1214);

        double distanceKm = calculator.calculateDistance(amsterdam, utrecht);

        assertThat(distanceKm).isCloseTo(34.2, within(1.0));
    }

    @Test
    void shouldCalculateZeroDistanceForSameLocation() {
        Location location = new Location(52.3676, 4.9041);

        double distance = calculator.calculateDistance(location, location);

        assertThat(distance).isCloseTo(0.0, within(0.001));
    }

    @Test
    void shouldCalculateSameDistanceRegardlessOfDirection() {
        Location location1 = new Location(52.3676, 4.9041);
        Location location2 = new Location(51.9244, 4.4777);

        double distance1to2 = calculator.calculateDistance(location1, location2);
        double distance2to1 = calculator.calculateDistance(location2, location1);

        assertThat(distance1to2).isEqualTo(distance2to1);
    }

    @Test
    void shouldHandleSmallDistances() {
        Location location1 = new Location(52.3676, 4.9041);
        Location location2 = new Location(52.3685, 4.9041);

        double distanceKm = calculator.calculateDistance(location1, location2);

        // Should be approximately 0.1 km
        assertThat(distanceKm).isLessThan(0.2).isGreaterThan(0.08);
    }

    @Test
    void shouldHandleLongDistances() {
        Location amsterdam = new Location(52.3676, 4.9041);
        Location barcelona = new Location(41.3851, 2.1734);

        double distanceKm = calculator.calculateDistance(amsterdam, barcelona);

        assertThat(distanceKm).isCloseTo(1238, within(5.0));
    }

    @Test
    void shouldThrowExceptionForNullFromLocation() {
        Location to = new Location(52.3676, 4.9041);

        assertThrows(IllegalArgumentException.class, () -> calculator.calculateDistance(null, to));
    }

    @Test
    void shouldThrowExceptionForNullToLocation() {
        Location from = new Location(52.3676, 4.9041);

        assertThrows(IllegalArgumentException.class, () -> calculator.calculateDistance(from, null));
    }

    @Test
    void shouldCalculateDistanceForNetherlandsCities() {
        // Test several Dutch cities to verify calculator accuracy
        Location amsterdam = new Location(52.3676, 4.9041);
        Location denHaag = new Location(52.0705, 4.3007);
        Location eindhoven = new Location(51.4381, 5.4697);
        Location groningen = new Location(53.2194, 6.5665);

        double amsToDenHaag = calculator.calculateDistance(amsterdam, denHaag);
        assertThat(amsToDenHaag).isCloseTo(52.7, within(1.0));

        double amsToEindhoven = calculator.calculateDistance(amsterdam, eindhoven);
        assertThat(amsToEindhoven).isCloseTo(110.0, within(1.0));

        double amsToGroningen = calculator.calculateDistance(amsterdam, groningen);
        assertThat(amsToGroningen).isCloseTo(146.0, within(1.0));
    }
}
