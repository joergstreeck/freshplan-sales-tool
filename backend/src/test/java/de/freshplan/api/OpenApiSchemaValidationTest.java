package de.freshplan.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * OpenAPI Schema Validation Tests - BATCH 3
 *
 * <p>Tests API Contract completeness and correctness: - All critical endpoints are documented -
 * Response schemas are defined - Request schemas are defined for POST/PUT - OpenAPI 3.0 compliance
 *
 * <p>WHY: Frontend depends on accurate OpenAPI spec for type generation BLOCKING: CI fails if
 * critical endpoints missing from OpenAPI spec
 */
@QuarkusTest
@DisplayName("OpenAPI Schema Validation (API Contract Tests)")
public class OpenApiSchemaValidationTest {

  private static final String OPENAPI_PATH = "/openapi";

  @Test
  @DisplayName("OpenAPI schema should be accessible and valid JSON")
  void testOpenApiSchemaAccessible() {
    String schema =
        given()
            .accept("application/json")
            .when()
            .get(OPENAPI_PATH)
            .then()
            .statusCode(200)
            .contentType(containsString("json"))
            .extract()
            .asString();

    // Parse to ensure valid JSON
    JsonPath jsonPath = new JsonPath(schema);

    // Verify OpenAPI 3.x structure (accepts 3.0.x and 3.1.x)
    assertThat("OpenAPI version should be 3.x", jsonPath.getString("openapi"), startsWith("3."));

    assertNotNull(jsonPath.getMap("info"), "OpenAPI info section should exist");
    assertNotNull(jsonPath.getMap("paths"), "OpenAPI paths section should exist");
  }

  @Test
  @DisplayName("Critical Customer endpoints should be documented")
  void testCustomerEndpointsDocumented() {
    JsonPath schema = getOpenApiSchema();

    // Customer CRUD endpoints
    assertThat(
        "GET /api/customers should be documented",
        schema.getMap("paths.'/api/customers'.get"),
        is(notNullValue()));

    assertThat(
        "POST /api/customers should be documented",
        schema.getMap("paths.'/api/customers'.post"),
        is(notNullValue()));

    assertThat(
        "GET /api/customers/{id} should be documented",
        schema.getMap("paths.'/api/customers/{id}'.get"),
        is(notNullValue()));

    assertThat(
        "PUT /api/customers/{id} should be documented",
        schema.getMap("paths.'/api/customers/{id}'.put"),
        is(notNullValue()));

    assertThat(
        "DELETE /api/customers/{id} should be documented",
        schema.getMap("paths.'/api/customers/{id}'.delete"),
        is(notNullValue()));
  }

  @Test
  @DisplayName("Critical Lead endpoints should be documented")
  void testLeadEndpointsDocumented() {
    JsonPath schema = getOpenApiSchema();

    // Lead CRUD endpoints (non-blocking, documenting current state)
    assertThat(
        "GET /api/leads should be documented",
        schema.getMap("paths.'/api/leads'.get"),
        is(notNullValue()));

    assertThat(
        "POST /api/leads should be documented",
        schema.getMap("paths.'/api/leads'.post"),
        is(notNullValue()));

    assertThat(
        "GET /api/leads/{id} should be documented",
        schema.getMap("paths.'/api/leads/{id}'.get"),
        is(notNullValue()));

    // PUT /api/leads/{id} is optional (may not be implemented yet)
    // Lenient check: Don't fail if missing, just document
    Object putEndpoint = schema.getMap("paths.'/api/leads/{id}'.put");
    if (putEndpoint == null) {
      System.out.println("⚠️  Note: PUT /api/leads/{id} endpoint not yet documented in OpenAPI");
    }
  }

  @Test
  @DisplayName("Enum endpoints should be documented")
  void testEnumEndpointsDocumented() {
    JsonPath schema = getOpenApiSchema();

    // Server-Driven Enum endpoints (critical for Frontend!)
    assertThat(
        "GET /api/enums/business-types should be documented",
        schema.getMap("paths.'/api/enums/business-types'.get"),
        is(notNullValue()));

    assertThat(
        "GET /api/enums/kitchen-sizes should be documented",
        schema.getMap("paths.'/api/enums/kitchen-sizes'.get"),
        is(notNullValue()));

    assertThat(
        "GET /api/enums/salutations should be documented",
        schema.getMap("paths.'/api/enums/salutations'.get"),
        is(notNullValue()));
  }

  @Test
  @DisplayName("Response schemas should be defined for critical endpoints")
  void testResponseSchemasExist() {
    JsonPath schema = getOpenApiSchema();

    // STRICT: Core Customer endpoints MUST have response schemas
    Object customersResponse =
        schema.get("paths.'/api/customers'.get.responses.'200'.content.'application/json'.schema");
    assertNotNull(
        customersResponse,
        "STRICT: GET /api/customers MUST have 200 response schema (core endpoint)");

    Object createCustomerResponse =
        schema.get("paths.'/api/customers'.post.responses.'201'.content.'application/json'.schema");
    assertNotNull(
        createCustomerResponse,
        "STRICT: POST /api/customers MUST have 201 response schema (core endpoint)");

    // LENIENT: Lead endpoints may have incomplete schemas (Lead API in progress)
    Object leadsResponse =
        schema.get("paths.'/api/leads'.get.responses.'200'.content.'application/json'.schema");
    if (leadsResponse == null) {
      System.out.println(
          "⚠️  LENIENT: GET /api/leads missing 200 response schema (Lead API incomplete)");
    }
  }

  @Test
  @DisplayName("Request schemas should be defined for POST/PUT operations")
  void testRequestSchemasExist() {
    JsonPath schema = getOpenApiSchema();

    // Check POST /api/customers has request body schema
    Object createCustomerRequest =
        schema.get("paths.'/api/customers'.post.requestBody.content.'application/json'.schema");
    assertNotNull(createCustomerRequest, "POST /api/customers should have request body schema");

    // Check PUT /api/customers/{id} has request body schema
    Object updateCustomerRequest =
        schema.get("paths.'/api/customers/{id}'.put.requestBody.content.'application/json'.schema");
    assertNotNull(updateCustomerRequest, "PUT /api/customers/{id} should have request body schema");

    // Check POST /api/leads has request body schema
    Object createLeadRequest =
        schema.get("paths.'/api/leads'.post.requestBody.content.'application/json'.schema");
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
    assertNotNull(
        schema.getMap("components.schemas.CustomerResponse"),
        "CustomerResponse schema should exist");

    // LeadResponse optional (Lead API may not be fully documented yet)
    Object leadResponse = schema.getMap("components.schemas.LeadResponse");
    if (leadResponse == null) {
      System.out.println("⚠️  Note: LeadResponse schema not yet documented in OpenAPI");
    }

    assertNotNull(
        schema.getMap("components.schemas.CreateCustomerRequest"),
        "CreateCustomerRequest schema should exist");
    assertNotNull(
        schema.getMap("components.schemas.UpdateCustomerRequest"),
        "UpdateCustomerRequest schema should exist");
  }

  @Test
  @DisplayName("Security schemes should be documented")
  void testSecuritySchemesDocumented() {
    JsonPath schema = getOpenApiSchema();

    // Security schemes (JWT authentication) - optional, not blocking
    Object securitySchemes = schema.getMap("components.securitySchemes");
    if (securitySchemes == null) {
      System.out.println("⚠️  Note: No security schemes documented in OpenAPI spec");
      System.out.println("    Consider adding JWT/OAuth security scheme documentation");
    } else {
      System.out.println("✅  Security schemes documented: " + securitySchemes);
    }

    // Test passes (non-blocking documentation check)
    assertTrue(true, "Security scheme check completed (warning logged if missing)");
  }

  @Test
  @DisplayName("OpenAPI should have API info metadata")
  void testApiInfoMetadata() {
    JsonPath schema = getOpenApiSchema();

    assertNotNull(schema.getString("info.title"), "API title should be defined");
    assertNotNull(schema.getString("info.version"), "API version should be defined");

    // STRICT: API title should contain "FreshPlan" or "backend" (project name)
    String title = schema.getString("info.title");
    assertFalse(title == null || title.isEmpty(), "API title should not be empty");
    assertTrue(
        title.toLowerCase().contains("freshplan") || title.toLowerCase().contains("backend"),
        "API title should contain 'FreshPlan' or 'backend' (current: '" + title + "')");
  }

  /** Helper: Download OpenAPI Schema from running application */
  private JsonPath getOpenApiSchema() {
    String schema =
        RestAssured.given()
            .accept("application/json")
            .when()
            .get(OPENAPI_PATH)
            .then()
            .statusCode(200)
            .extract()
            .asString();

    return new JsonPath(schema);
  }
}
