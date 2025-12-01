package kilic.yunus.stores.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response containing nearest stores and query information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response containing nearest stores")
public class NearestStoresResponse {

    @Schema(description = "Query parameters used")
    private QueryInfo query;

    @Schema(description = "List of stores with distances")
    private List<StoreWithDistance> results;

    @Schema(description = "Total number of stores found", example = "5")
    private int totalFound;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Query information")
    public static class QueryInfo {
        @Schema(description = "Query latitude", example = "52.3676")
        private double latitude;

        @Schema(description = "Query longitude", example = "4.9041")
        private double longitude;

        @Schema(description = "Requested limit", example = "5")
        private int limit;
    }
}
