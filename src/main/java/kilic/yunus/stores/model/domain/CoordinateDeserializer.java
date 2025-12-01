package kilic.yunus.stores.model.domain;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

/**
 * Custom Jackson deserializer for coordinate values. Handles deserialization from both string and
 * numeric JSON values.
 *
 * <p>Supports: - String format: "4.9041" or "-180.0" - Numeric format: 4.9041 or -180.0
 *
 * <p>This allows flexibility in JSON input format while maintaining type safety with double values
 * internally.
 */
public class CoordinateDeserializer extends JsonDeserializer<Double> {

    @Override
    public Double deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        JsonNode node = parser.getCodec().readTree(parser);

        // Handle null values
        if (node.isNull()) {
            return null;
        }

        // Handle numeric values directly
        if (node.isNumber()) {
            return node.asDouble();
        }

        // Handle string values
        if (node.isTextual()) {
            String value = node.asText().trim();

            // Handle empty strings
            if (value.isEmpty()) {
                return null;
            }

            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException e) {
                throw new IOException(
                        String.format("Cannot deserialize coordinate value '%s' to double", value), e);
            }
        }

        // Unsupported type
        throw new IOException(
                String.format(
                        "Cannot deserialize coordinate from type: %s, value: %s",
                        node.getNodeType(), node.toString()));
    }
}
