package kilic.yunus.stores.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Unit tests for HealthController. Tests the health check endpoint functionality.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("HealthController Tests")
class HealthControllerTest {

    @Mock
    private HealthIndicator healthIndicator;

    @InjectMocks
    private HealthController healthController;

    @Test
    @DisplayName("Should return UP status when application is healthy")
    void shouldReturnUpStatusWhenHealthy() {
        // Given
        Map<String, Object> details = new HashMap<>();
        details.put("storeCount", 600);
        details.put("message", "All systems operational");

        Health health = Health.up().withDetails(details).build();

        when(healthIndicator.health()).thenReturn(health);

        // When
        ResponseEntity<Map<String, Object>> response = healthController.health();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isNotNull()
                .containsEntry("status", "UP")
                .containsKey("details");

        @SuppressWarnings("unchecked")
        Map<String, Object> responseDetails = (Map<String, Object>) response.getBody().get("details");
        assertThat(responseDetails)
                .containsEntry("storeCount", 600)
                .containsEntry("message", "All systems operational");
    }

    @Test
    @DisplayName("Should return DOWN status when application is unhealthy")
    void shouldReturnDownStatusWhenUnhealthy() {
        // Given
        Map<String, Object> details = new HashMap<>();
        details.put("error", "Store data not loaded");
        details.put("reason", "File not found");

        Health health = Health.down().withDetails(details).build();

        when(healthIndicator.health()).thenReturn(health);

        // When
        ResponseEntity<Map<String, Object>> response = healthController.health();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isNotNull()
                .containsEntry("status", "DOWN")
                .containsKey("details");

        @SuppressWarnings("unchecked")
        Map<String, Object> responseDetails = (Map<String, Object>) response.getBody().get("details");
        assertThat(responseDetails)
                .containsEntry("error", "Store data not loaded")
                .containsEntry("reason", "File not found");
    }

    @Test
    @DisplayName("Should return UNKNOWN status when health status is unknown")
    void shouldReturnUnknownStatusWhenStatusIsUnknown() {
        // Given
        Health health = Health.unknown().withDetail("message", "Unable to determine health").build();

        when(healthIndicator.health()).thenReturn(health);

        // When
        ResponseEntity<Map<String, Object>> response = healthController.health();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isNotNull()
                .extracting("status")
                .isEqualTo("UNKNOWN");
    }

    @Test
    @DisplayName("Should return OUT_OF_SERVICE status when service is unavailable")
    void shouldReturnOutOfServiceStatus() {
        // Given
        Health health =
                Health.outOfService().withDetail("message", "Service temporarily unavailable").build();

        when(healthIndicator.health()).thenReturn(health);

        // When
        ResponseEntity<Map<String, Object>> response = healthController.health();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isNotNull()
                .extracting("status")
                .isEqualTo("OUT_OF_SERVICE");
    }

    @Test
    @DisplayName("Should return empty details when no details provided")
    void shouldReturnEmptyDetailsWhenNoDetailsProvided() {
        // Given
        Health health = Health.up().build();

        when(healthIndicator.health()).thenReturn(health);

        // When
        ResponseEntity<Map<String, Object>> response = healthController.health();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isNotNull()
                .containsEntry("status", "UP")
                .containsKey("details");

        @SuppressWarnings("unchecked")
        Map<String, Object> responseDetails = (Map<String, Object>) response.getBody().get("details");
        assertThat(responseDetails).isEmpty();
    }

    @Test
    @DisplayName("Should return custom status when custom health status is used")
    void shouldReturnCustomStatus() {
        // Given
        Status customStatus = new Status("DEGRADED");
        Health health = Health.status(customStatus).withDetail("performance", "degraded").build();

        when(healthIndicator.health()).thenReturn(health);

        // When
        ResponseEntity<Map<String, Object>> response = healthController.health();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isNotNull()
                .extracting("status")
                .isEqualTo("DEGRADED");
    }

    @Test
    @DisplayName("Should return multiple details in health response")
    void shouldReturnMultipleDetailsInHealthResponse() {
        // Given
        Map<String, Object> details = new HashMap<>();
        details.put("storeCount", 663);
        details.put("cacheSize", 1000);
        details.put("lastUpdated", "2025-12-01T10:00:00");
        details.put("version", "1.0.0");
        details.put("environment", "production");

        Health health = Health.up().withDetails(details).build();

        when(healthIndicator.health()).thenReturn(health);

        // When
        ResponseEntity<Map<String, Object>> response = healthController.health();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        @SuppressWarnings("unchecked")
        Map<String, Object> responseDetails = (Map<String, Object>) response.getBody().get("details");
        assertThat(responseDetails).hasSize(5)
                .containsEntry("storeCount", 663)
                .containsEntry("cacheSize", 1000)
                .containsEntry("lastUpdated", "2025-12-01T10:00:00")
                .containsEntry("version", "1.0.0")
                .containsEntry("environment", "production");
    }

    @Test
    @DisplayName("Should handle numeric details correctly")
    void shouldHandleNumericDetailsCorrectly() {
        // Given
        Health health =
                Health.up()
                        .withDetail("count", 100)
                        .withDetail("percentage", 95.5)
                        .withDetail("isActive", true)
                        .build();

        when(healthIndicator.health()).thenReturn(health);

        // When
        ResponseEntity<Map<String, Object>> response = healthController.health();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        @SuppressWarnings("unchecked")
        Map<String, Object> responseDetails = (Map<String, Object>) response.getBody().get("details");
        assertThat(responseDetails)
                .containsEntry("count", 100)
                .containsEntry("percentage", 95.5)
                .containsEntry("isActive", true);
    }

    @Test
    @DisplayName("Should return 200 OK regardless of health status")
    void shouldReturn200OkRegardlessOfHealthStatus() {
        // Given - test with DOWN status
        Health health = Health.down().withDetail("error", "Critical failure").build();

        when(healthIndicator.health()).thenReturn(health);

        // When
        ResponseEntity<Map<String, Object>> response = healthController.health();

        // Then - should still return 200 OK
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsKey("status");
        assertThat(response.getBody()).containsKey("details");
    }
}
