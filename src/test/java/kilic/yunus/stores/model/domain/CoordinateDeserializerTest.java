package kilic.yunus.stores.model.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for CoordinateDeserializer.
 * Tests all supported input formats and error scenarios.
 */
class CoordinateDeserializerTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("Should deserialize null value to null")
    void shouldDeserializeNullValue() throws IOException {
        // Given
        String json = "{\"coordinate\": null}";

        // When
        TestCoordinate result = objectMapper.readValue(json, TestCoordinate.class);

        // Then
        assertThat(result.coordinate).isNull();
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @CsvSource(delimiter = '|', textBlock = """
            Numeric double value               | 52.3676         | 52.3676
            Integer value                      | 52              | 52.0
            Negative numeric value             | -4.9041         | -4.9041
            Zero value                         | 0               | 0.0
            String with double value           | "52.3676"       | 52.3676
            Negative string value              | "-4.9041"       | -4.9041
            String with leading/trailing spaces| "  52.3676  "   | 52.3676
            Very large number                  | 999999.999999   | 999999.999999
            Very small number                  | 0.000001        | 0.000001
            Scientific notation                | 1.23e2          | 123.0
            String with scientific notation    | "1.23e2"        | 123.0
            Positive sign in string            | "+52.3676"      | 52.3676
            """)
    @DisplayName("Should deserialize valid coordinate values")
    void shouldDeserializeValidValues(String description, String input, Double expected) throws IOException {
        // Given
        String json = "{\"coordinate\": " + input + "}";

        // When
        TestCoordinate result = objectMapper.readValue(json, TestCoordinate.class);

        // Then
        assertThat(result.coordinate).isEqualTo(expected);
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @CsvSource(delimiter = '|', textBlock = """
            Empty string          | ""
            Whitespace only string| "   "
            """)
    @DisplayName("Should return null for empty or whitespace-only strings")
    void shouldReturnNullForEmptyStrings(String description, String input) throws IOException {
        // Given
        String json = "{\"coordinate\": " + input + "}";

        // When
        TestCoordinate result = objectMapper.readValue(json, TestCoordinate.class);

        // Then
        assertThat(result.coordinate).isNull();
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @CsvSource(delimiter = '|', textBlock = """
            Invalid string format     | "invalid"         | Cannot deserialize coordinate value 'invalid' to double
            Text with numbers         | "52.3676abc"      | Cannot deserialize coordinate value '52.3676abc' to double
            Special characters        | "@#$%"            | Cannot deserialize coordinate value '@#$%' to double
            Multiple decimal points   | "52.36.76"        | Cannot deserialize coordinate value '52.36.76' to double
            """)
    @DisplayName("Should throw exception for invalid string formats")
    void shouldThrowExceptionForInvalidStringFormats(String description, String input, String expectedMessage) {
        // Given
        String json = "{\"coordinate\": " + input + "}";

        // When/Then
        assertThatThrownBy(() -> objectMapper.readValue(json, TestCoordinate.class))
                .isInstanceOf(IOException.class)
                .hasMessageContaining(expectedMessage);
    }

    @org.junit.jupiter.params.ParameterizedTest(name = "[{index}] {0}")
    @org.junit.jupiter.params.provider.CsvSource(delimiter = '|', textBlock = """
            Boolean value | true                        | BOOLEAN
            Array value   | [52.3676]                   | ARRAY
            Object value  | {"value": 52.3676}          | OBJECT
            """)
    @DisplayName("Should throw exception for unsupported types")
    void shouldThrowExceptionForUnsupportedTypes(String description, String input, String nodeType) {
        // Given
        String json = "{\"coordinate\": " + input + "}";

        // When/Then
        assertThatThrownBy(() -> objectMapper.readValue(json, TestCoordinate.class))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("Cannot deserialize coordinate from type: " + nodeType);
    }

    /**
     * Test class with a coordinate field using CoordinateDeserializer.
     */
    static class TestCoordinate {
        @com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = CoordinateDeserializer.class)
        public Double coordinate;
    }
}

