package kilic.yunus.stores.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for StoreController REST API endpoints.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class StoreControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "/api/v1/stores";
    }

    @Test
    void shouldReturnBadRequestForMissingLatitude() {
        given()
                .queryParam("longitude", 4.9041)
                .when()
                .get("/nearest")
                .then()
                .statusCode(400)
                .body("status", equalTo(400))
                .body("message", notNullValue());
    }

    @Test
    void shouldReturnBadRequestForMissingLongitude() {
        given()
                .queryParam("latitude", 52.3676)
                .when()
                .get("/nearest")
                .then()
                .statusCode(400)
                .body("status", equalTo(400))
                .body("message", notNullValue());
    }

    @Test
    void shouldReturnBadRequestForInvalidLatitude() {
        given()
                .queryParam("latitude", 100.0)
                .queryParam("longitude", 4.9041)
                .when()
                .get("/nearest")
                .then()
                .statusCode(400)
                .body("status", equalTo(400))
                .body("message", containsStringIgnoringCase("latitude"));
    }

    @Test
    void shouldReturnBadRequestForInvalidLongitude() {
        given()
                .queryParam("latitude", 52.3676)
                .queryParam("longitude", 200.0)
                .when()
                .get("/nearest")
                .then()
                .statusCode(400)
                .body("status", equalTo(400))
                .body("message", containsStringIgnoringCase("longitude"));
    }

    @Test
    void shouldReturnBadRequestForInvalidLimit() {
        given()
                .queryParam("latitude", 52.3676)
                .queryParam("longitude", 4.9041)
                .queryParam("limit", 100)
                .when()
                .get("/nearest")
                .then()
                .statusCode(400)
                .body("status", equalTo(400))
                .body("message", containsStringIgnoringCase("limit"));
    }

    @Test
    void shouldReturnStoresSortedByDistance() {
        var response =
                given()
                        .queryParam("latitude", 52.3676)
                        .queryParam("longitude", 4.9041)
                        .queryParam("limit", 5)
                        .when()
                        .get("/nearest")
                        .then()
                        .statusCode(200)
                        .extract()
                        .response();

        // Verify we have at least 2 results
        var results = response.jsonPath().getList("results");
        assert results.size() >= 2 : "Expected at least 2 results";

        // Verify distances are in ascending order
        Double firstDistance = response.jsonPath().getDouble("results[0].distance");
        Double secondDistance = response.jsonPath().getDouble("results[1].distance");

        assert firstDistance <= secondDistance
                : String.format(
                "First distance (%f) should be <= second distance (%f)", firstDistance, secondDistance);
    }

    @Test
    void shouldReturnNearestStoresWithValidData() {
        given()
                .queryParam("latitude", 52.3676)
                .queryParam("longitude", 4.9041)
                .queryParam("limit", 5)
                .when()
                .get("/nearest")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("query.latitude", equalTo(52.3676f))
                .body("query.longitude", equalTo(4.9041f))
                .body("query.limit", equalTo(5))
                .body("results", notNullValue())
                .body("results.size()", greaterThan(0))
                .body("results[0].store", notNullValue())
                .body("results[0].store.sapStoreID", notNullValue())
                .body("results[0].store.city", notNullValue())
                .body("results[0].store.addressName", notNullValue())
                .body("results[0].distance", notNullValue())
                .body("totalFound", greaterThan(0));
    }

    @Test
    void shouldReturnDefaultLimitWhenNotSpecified() {
        given()
                .queryParam("latitude", 52.3676)
                .queryParam("longitude", 4.9041)
                .when()
                .get("/nearest")
                .then()
                .statusCode(200)
                .body("query.limit", equalTo(5)) // Default is 5
                .body("results.size()", lessThanOrEqualTo(5));
    }

    @Test
    void shouldRespectLimitParameter() {
        int requestedLimit = 3;

        given()
                .queryParam("latitude", 52.3676)
                .queryParam("longitude", 4.9041)
                .queryParam("limit", requestedLimit)
                .when()
                .get("/nearest")
                .then()
                .statusCode(200)
                .body("query.limit", equalTo(requestedLimit))
                .body("results.size()", lessThanOrEqualTo(requestedLimit))
                .body("totalFound", lessThanOrEqualTo(requestedLimit));
    }

    @Test
    void shouldReturnStoresWithAllRequiredFields() {
        given()
                .queryParam("latitude", 52.3676)
                .queryParam("longitude", 4.9041)
                .queryParam("limit", 1)
                .when()
                .get("/nearest")
                .then()
                .statusCode(200)
                .body("results[0].store.city", notNullValue())
                .body("results[0].store.postalCode", notNullValue())
                .body("results[0].store.street", notNullValue())
                .body("results[0].store.addressName", notNullValue())
                .body("results[0].store.uuid", notNullValue())
                .body("results[0].store.longitude", notNullValue())
                .body("results[0].store.latitude", notNullValue())
                .body("results[0].store.complexNumber", notNullValue())
                .body("results[0].store.locationType", notNullValue())
                .body("results[0].store.sapStoreID", notNullValue());
    }

    @Test
    void shouldReturnBadRequestForNegativeLimit() {
        given()
                .queryParam("latitude", 52.3676)
                .queryParam("longitude", 4.9041)
                .queryParam("limit", -1)
                .when()
                .get("/nearest")
                .then()
                .statusCode(400)
                .body("status", equalTo(400))
                .body("message", containsStringIgnoringCase("limit"));
    }

    @Test
    void shouldReturnBadRequestForZeroLimit() {
        given()
                .queryParam("latitude", 52.3676)
                .queryParam("longitude", 4.9041)
                .queryParam("limit", 0)
                .when()
                .get("/nearest")
                .then()
                .statusCode(400)
                .body("status", equalTo(400))
                .body("message", containsStringIgnoringCase("limit"));
    }

    @Test
    void shouldHandleEdgeCaseCoordinates() {
        // Test with coordinates near edge of Netherlands
        given()
                .queryParam("latitude", 53.5) // North of Netherlands
                .queryParam("longitude", 6.0)
                .queryParam("limit", 5)
                .when()
                .get("/nearest")
                .then()
                .statusCode(200)
                .body("results", notNullValue());
    }

    @Test
    void shouldReturnDistancesInAscendingOrder() {
        var response =
                given()
                        .queryParam("latitude", 52.3676)
                        .queryParam("longitude", 4.9041)
                        .queryParam("limit", 5)
                        .when()
                        .get("/nearest")
                        .then()
                        .statusCode(200)
                        .extract()
                        .response();

        // Verify we have at least 2 results
        var results = response.jsonPath().getList("results");
        assert results.size() >= 2 : "Expected at least 2 results";

        // Verify all distances are in ascending order
        for (int i = 0; i < results.size() - 1; i++) {
            Double currentDistance = response.jsonPath().getDouble("results[" + i + "].distance");
            Double nextDistance = response.jsonPath().getDouble("results[" + (i + 1) + "].distance");

            assert currentDistance <= nextDistance
                    : String.format(
                    "Distance at index %d (%f) should be <= distance at index %d (%f)",
                    i, currentDistance, i + 1, nextDistance);
        }
    }
}
