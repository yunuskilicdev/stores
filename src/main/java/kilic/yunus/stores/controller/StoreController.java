package kilic.yunus.stores.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kilic.yunus.stores.model.domain.Location;
import kilic.yunus.stores.model.dto.ErrorResponse;
import kilic.yunus.stores.model.dto.NearestStoresRequest;
import kilic.yunus.stores.model.dto.NearestStoresResponse;
import kilic.yunus.stores.model.dto.StoreWithDistance;
import kilic.yunus.stores.service.StoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for store operations.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/stores")
@RequiredArgsConstructor
@Validated
@Tag(name = "Stores", description = "Store location and search APIs")
public class StoreController {

    private final StoreService storeService;

    @Operation(
            summary = "Find nearest stores",
            description = "Returns the nearest stores to a given location, sorted by distance")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved nearest stores",
                            content =
                            @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = NearestStoresResponse.class))),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid request parameters",
                            content =
                            @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error",
                            content =
                            @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)))
            })
    @GetMapping("/nearest")
    public ResponseEntity<NearestStoresResponse> findNearestStores(
            @Valid @ModelAttribute NearestStoresRequest request) {
        log.info(
                "Finding nearest stores - lat: {}, lon: {}, limit: {}",
                request.getLatitude(),
                request.getLongitude(),
                request.getLimit());

        Location location = new Location(request.getLatitude(), request.getLongitude());

        List<StoreWithDistance> nearestStores =
                storeService.findNearestStores(location, request.getLimit());

        NearestStoresResponse response =
                NearestStoresResponse.builder()
                        .query(
                                NearestStoresResponse.QueryInfo.builder()
                                        .latitude(request.getLatitude())
                                        .longitude(request.getLongitude())
                                        .limit(request.getLimit())
                                        .build())
                        .results(nearestStores)
                        .totalFound(nearestStores.size())
                        .build();

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get store statistics",
            description = "Returns statistics about the store data")
    @GetMapping("/stats")
    public ResponseEntity<StoreStats> getStats() {
        log.info("Getting store statistics");

        int totalStores = storeService.getTotalStoreCount();

        StoreStats stats = new StoreStats(totalStores);
        return ResponseEntity.ok(stats);
    }

    /**
     * Simple stats DTO
     */
    @Schema(description = "Store statistics")
    public record StoreStats(
            @Schema(description = "Total number of stores", example = "663") int totalStores) {
    }

}
