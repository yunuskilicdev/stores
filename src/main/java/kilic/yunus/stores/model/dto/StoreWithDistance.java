package kilic.yunus.stores.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kilic.yunus.stores.model.domain.Store;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response object containing store information and its distance from query location. Distance is in
 * kilometers.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Store with distance information in kilometers")
public class StoreWithDistance {

    @Schema(description = "Store details")
    private Store store;

    @Schema(description = "Distance from query location in kilometers", example = "0.85")
    private double distance;
}
