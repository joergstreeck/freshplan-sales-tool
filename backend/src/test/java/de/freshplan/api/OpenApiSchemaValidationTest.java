package de.freshplan.api;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * OpenAPI Schema Validation Tests - BATCH 3
 *
 * Tests API Contract completeness and correctness:
 * - All critical endpoints are documented
 * - Response schemas are defined
 * - Request schemas are defined for POST/PUT
 * - OpenAPI 3.0 compliance
 *
 * WHY: Frontend depends on accurate OpenAPI spec for type generation
 * BLOCKING: CI fails if critical endpoints missing from OpenAPI spec
 */
@QuarkusTest
@DisplayName("OpenAPI Schema Validation (API Contract Tests)")
public class OpenApiSchemaValidationTest {

    private static final String OPENAPI_PATH = "/openapi";

    @Test
    @DisplayName("OpenAPI schema should be accessible and valid JSON")
    void testOpenApiSchemaAccessible() {
        String schema = given()
            .when().get(OPENAPI_PATH)
            .then()
                .statusCode(200)
                .contentType(containsString("json"))
                .extract().asString();

        // Parse to ensure valid JSON
        JsonPath jsonPath = new JsonPath(schema);

        // Verify OpenAPI 3.0 structure
        assertThat("OpenAPI version should be 3.0.x",
                   jsonPath.getString("openapi"),
                   startsWith("3.0"));

        assertNotNull(jsonPath.getMap("info"), "OpenAPI info section should exist");
        assertNotNull(jsonPath.getMap("paths"), "OpenAPI paths section should exist");
    }

    @Test
    @DisplayName("Critical Customer endpoints should be documented")
    void testCustomerEndpointsDocumented() {
        JsonPath schema = getOpenApiSchema();

        // Customer CRUD endpoints
        assertThat("GET /api/customers should be documented",
                   schema.getMap("paths.'/api/customers'.get"),
                   is(notNullValue()));

        assertThat("POST /api/customers should be documented",
                   schema.getMap("paths.'/api/customers'.post"),
                   is(notNullValue()));

        assertThat("GET /api/customers/{id} should be documented",
                   schema.getMap("paths.'/api/customers/{id}'.get"),
                   is(notNullValue()));

        assertThat("PUT /api/customers/{id} should be documented",
                   schema.getMap("paths.'/api/customers/{id}'.put"),
                   is(notNullValue()));

        assertThat("DELETE /api/customers/{id} should be documented",
                   schema.getMap("paths.'/api/customers/{id}'.delete"),
                   is(notNullValue()));
    }

    @Test
    @DisplayName("Critical Lead endpoints should be documented")
    void testLeadEndpointsDocumented() {
        JsonPath schema = getOpenApiSchema();

        // Lead CRUD endpoints
        assertThat("GET /api/leads should be documented",
                   schema.getMap("paths.'/api/leads'.get"),
                   is(notNullValue()));

        assertThat("POST /api/leads should be documented",
                   schema.getMap("paths.'/api/leads'.post"),
                   is(notNullValue()));

        assertThat("GET /api/leads/{id} should be documented",
                   schema.getMap("paths.'/api/leads/{id}'.get"),
                   is(notNullValue()));

        assertThat("PUT /api/leads/{id} should be documented",
                   schema.getMap("paths.'/api/leads/{id}'.put"),
                   is(notNullValue()));
    }

    @Test
    @DisplayName("Enum endpoints should be documented")
    void testEnumEndpointsDocumented() {
        JsonPath schema = getOpenApiSchema();

        // Server-Driven Enum endpoints (critical for Frontend!)
        assertThat("GET /api/enums/business-types should be documented",
                   schema.getMap("paths.'/api/enums/business-types'.get"),
                   is(notNullValue()));

        assertThat("GET /api/enums/kitchen-sizes should be documented",
                   schema.getMap("paths.'/api/enums/kitchen-sizes'.get"),
                   is(notNullValue()));

        assertThat("GET /api/enums/salutations should be documented",
                   schema.getMap("paths.'/api/enums/salutations'.get"),
                   is(notNullValue()));
    }

    @Test
    @DisplayName("Response schemas should be defined for critical endpoints")
    void testResponseSchemasExist() {
        JsonPath schema = getOpenApiSchema();

        // Check GET /api/customers has response schema
        Object customersResponse = schema.get("paths.'/api/customers'.get.responses.'200'.content.'application/json'.schema");
        assertNotNull(customersResponse, "GET /api/customers should have 200 response schema");

        // Check POST /api/customers has response schema
        Object createCustomerResponse = schema.get("paths.'/api/customers'.post.responses.'201'.content.'application/json'.schema");
        assertNotNull(createCustomerResponse, "POST /api/customers should have 201 response schema");

        // Check GET /api/leads has response schema
        Object leadsResponse = schema.get("paths.'/api/leads'.get.responses.'200'.content.'application/json'.schema");
        assertNotNull(leadsResponse, "GET /api/leads should have 200 response schema");
    }

    @Test
    @DisplayName("Request schemas should be defined for POST/PUT operations")
    void testRequestSchemasExist() {
        JsonPath schema = getOpenApiSchema();

        // Check POST /api/customers has request body schema
        Object createCustomerRequest = schema.get("paths.'/api/customers'.post.requestBody.content.'application/json'.schema");
        assertNotNull(createCustomerRequest, "POST /api/customers should have request body schema");

        // Check PUT /api/customers/{id} has request body schema
        Object updateCustomerRequest = schema.get("paths.'/api/customers/{id}'.put.requestBody.content.'application/json'.schema");
        assertNotNull(updateCustomerRequest, "PUT /api/customers/{id} should have request body schema");

        // Check POST /api/leads has request body schema
        Object createLeadRequest = schema.get("paths.'/api/leads'.post.requestBody.content.'application/json'.schema");
        assertNotNull(createLeadRequest, "POST /api/leads should have request body schema");
    }

    @Test
    @DisplayName("Critical components schemas should be defined")
    void testCriticalComponentSchemasExist() {
        JsonPath schema = getOpenApiSchema();

        // Check components section exists
        assertNotNull(schema.getMap("components"), "Components section should exist");
        assertNotNull(schema.getMap("components.schemas"), "Components schemas should exist");

        // These DTOs are critical for Frontend TypeScript generation
        // If missing, Frontend types will be incomplete!
        assertNotNull(schema.getMap("components.schemas.CustomerResponse"),
                      "CustomerResponse schema should exist");
        assertNotNull(schema.getMap("components.schemas.LeadResponse"),
                      "LeadResponse schema should exist");
        assertNotNull(schema.getMap("components.schemas.CreateCustomerRequest"),
                      "CreateCustomerRequest schema should exist");
        assertNotNull(schema.getMap("components.schemas.UpdateCustomerRequest"),
                      "UpdateCustomerRequest schema should exist");
    }

    @Test
    @DisplayName("Security schemes should be documented")
    void testSecuritySchemesDocumented() {
        JsonPath schema = getOpenApiSchema();

        // Security schemes (JWT authentication)
        Object securitySchemes = schema.getMap("components.securitySchemes");
        assertNotNull(securitySchemes, "Security schemes should be documented");

        // Check if JWT or similar security scheme is defined
        assertThat("At least one security scheme should exist",
                   securitySchemes, is(notNullValue()));
    }

    @Test
    @DisplayName("OpenAPI should have API info metadata")
    void testApiInfoMetadata() {
        JsonPath schema = getOpenApiSchema();

        assertNotNull(schema.getString("info.title"), "API title should be defined");
        assertNotNull(schema.getString("info.version"), "API version should be defined");

        String title = schema.getString("info.title");
        assertThat("API title should contain FreshPlan",
                   title,
                   containsStringIgnoringCase("FreshPlan"));
    }

    /**
     * Helper: Download OpenAPI Schema from running application
     */
    private JsonPath getOpenApiSchema() {
        String schema = RestAssured.given()
            .when().get(OPENAPI_PATH)
            .then()
                .statusCode(200)
                .extract().asString();

        return new JsonPath(schema);
    }
}
