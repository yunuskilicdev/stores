package kilic.yunus.stores.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Health check controller.
 */
@RestController
@RequestMapping("/api/v1/health")
@RequiredArgsConstructor
@Tag(name = "Health", description = "Application health check")
public class HealthController {

    private final HealthIndicator healthIndicator;

    @Operation(summary = "Health check", description = "Check if the application is running")
    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        Health health = healthIndicator.health();

        Map<String, Object> response = new HashMap<>();
        response.put("status", health.getStatus().getCode());
        response.put("details", health.getDetails());

        return ResponseEntity.ok(response);
    }
}
