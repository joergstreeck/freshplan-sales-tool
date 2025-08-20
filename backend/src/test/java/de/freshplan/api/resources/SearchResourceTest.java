package de.freshplan.api.resources;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import de.freshplan.domain.search.service.SearchService;
import de.freshplan.domain.search.service.dto.ContactSearchDto;
import de.freshplan.domain.search.service.dto.CustomerSearchDto;
import de.freshplan.domain.search.service.dto.SearchResult;
import de.freshplan.domain.search.service.dto.SearchResults;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
/**
 * Integration tests for SearchResource API endpoints.
 *
 * <p>Tests cover: - Universal search endpoint with various query types - Quick search endpoint for
 * autocomplete - Security role validation - Input validation and error handling - Response format
 * verification
 *
 * <p>Applied Test Patterns: - TestSecurity for role-based access - @InjectMock for service layer
 * isolation - RestAssured for HTTP API testing
 */
@QuarkusTest
@Tag("migrate")class SearchResourceTest {

  @InjectMock SearchService searchService;

  @BeforeEach
  void setUp() {
    reset(searchService);
  }

  @Test
  @TestSecurity(
      user = "testuser",
      roles = {"sales"})
  void universalSearch_withValidParams_shouldReturnSearchResults() {
    // Given
    String query = "schmidt";
    SearchResults mockResults = createMockSearchResults();

    when(searchService.universalSearch(eq(query), eq(true), eq(false), eq(20)))
        .thenReturn(mockResults);

    // When & Then
    given()
        .queryParam("query", query)
        .queryParam("includeContacts", true)
        .queryParam("includeInactive", false)
        .queryParam("limit", 20)
        .when()
        .get("/api/search/universal")
        .then()
        .statusCode(200)
        .contentType(MediaType.APPLICATION_JSON)
        .body("customers", hasSize(1))
        .body("contacts", hasSize(1))
        .body("totalCount", equalTo(2))
        .body("executionTime", greaterThan(0))
        .body("customers[0].type", equalTo("customer"))
        .body("customers[0].relevanceScore", greaterThan(0))
        .body("contacts[0].type", equalTo("contact"))
        .body("contacts[0].relevanceScore", greaterThan(0));

    verify(searchService).universalSearch(eq(query), eq(true), eq(false), eq(20));
  }

  @Test
  @TestSecurity(
      user = "testuser",
      roles = {"admin"})
  void universalSearch_withAdminRole_shouldHaveAccess() {
    // Given
    SearchResults mockResults = createEmptySearchResults();
    when(searchService.universalSearch(anyString(), anyBoolean(), anyBoolean(), anyInt()))
        .thenReturn(mockResults);

    // When & Then
    given().queryParam("query", "test").when().get("/api/search/universal").then().statusCode(200);
  }

  @Test
  @TestSecurity(
      user = "testuser",
      roles = {"manager"})
  void universalSearch_withManagerRole_shouldHaveAccess() {
    // Given
    SearchResults mockResults = createEmptySearchResults();
    when(searchService.universalSearch(anyString(), anyBoolean(), anyBoolean(), anyInt()))
        .thenReturn(mockResults);

    // When & Then
    given().queryParam("query", "test").when().get("/api/search/universal").then().statusCode(200);
  }

  @Test
  @TestSecurity(
      user = "testuser",
      roles = {"viewer"})
  void universalSearch_withViewerRole_shouldHaveAccess() {
    // Given
    SearchResults mockResults = createEmptySearchResults();
    when(searchService.universalSearch(anyString(), anyBoolean(), anyBoolean(), anyInt()))
        .thenReturn(mockResults);

    // When & Then
    given().queryParam("query", "test").when().get("/api/search/universal").then().statusCode(200);
  }

  @Test
  void universalSearch_withoutAuthentication_shouldReturn401() {
    // When & Then
    given().queryParam("query", "test").when().get("/api/search/universal").then().statusCode(401);
  }

  @Test
  @TestSecurity(
      user = "testuser",
      roles = {"unauthorized"})
  void universalSearch_withUnauthorizedRole_shouldReturn403() {
    // When & Then
    given().queryParam("query", "test").when().get("/api/search/universal").then().statusCode(403);
  }

  @Test
  @TestSecurity(
      user = "testuser",
      roles = {"sales"})
  void universalSearch_withMissingQuery_shouldReturn400() {
    // When & Then
    given()
        .queryParam("includeContacts", true)
        .when()
        .get("/api/search/universal")
        .then()
        .statusCode(400);
  }

  @Test
  @TestSecurity(
      user = "testuser",
      roles = {"sales"})
  void universalSearch_withTooShortQuery_shouldReturn400() {
    // When & Then
    given()
        .queryParam("query", "a") // Too short (min 2 chars)
        .when()
        .get("/api/search/universal")
        .then()
        .statusCode(400);
  }

  @Test
  @TestSecurity(
      user = "testuser",
      roles = {"sales"})
  void universalSearch_withTooLongQuery_shouldReturn400() {
    // Given
    String longQuery = "a".repeat(101); // Too long (max 100 chars)

    // When & Then
    given()
        .queryParam("query", longQuery)
        .when()
        .get("/api/search/universal")
        .then()
        .statusCode(400);
  }

  @Test
  @TestSecurity(
      user = "testuser",
      roles = {"sales"})
  void universalSearch_withInvalidLimit_shouldReturn400() {
    // When & Then
    given()
        .queryParam("query", "test")
        .queryParam("limit", 0) // Too small (min 1)
        .when()
        .get("/api/search/universal")
        .then()
        .statusCode(400);
  }

  @Test
  @TestSecurity(
      user = "testuser",
      roles = {"sales"})
  void universalSearch_withTooHighLimit_shouldReturn400() {
    // When & Then
    given()
        .queryParam("query", "test")
        .queryParam("limit", 101) // Too high (max 100)
        .when()
        .get("/api/search/universal")
        .then()
        .statusCode(400);
  }

  @Test
  @TestSecurity(
      user = "testuser",
      roles = {"sales"})
  void universalSearch_withDefaultValues_shouldUseDefaults() {
    // Given
    SearchResults mockResults = createEmptySearchResults();
    when(searchService.universalSearch(eq("test"), eq(true), eq(false), eq(20)))
        .thenReturn(mockResults);

    // When & Then
    given()
        .queryParam("query", "test") // Only required param
        .when()
        .get("/api/search/universal")
        .then()
        .statusCode(200);

    // Verify default values were used
    verify(searchService).universalSearch(eq("test"), eq(true), eq(false), eq(20));
  }

  @Test
  @TestSecurity(
      user = "testuser",
      roles = {"sales"})
  void universalSearch_withServiceException_shouldReturn500() {
    // Given
    when(searchService.universalSearch(anyString(), anyBoolean(), anyBoolean(), anyInt()))
        .thenThrow(new RuntimeException("Database connection failed"));

    // When & Then
    given().queryParam("query", "test").when().get("/api/search/universal").then().statusCode(500);
  }

  @Test
  @TestSecurity(
      user = "testuser",
      roles = {"sales"})
  void quickSearch_withValidParams_shouldReturnQuickResults() {
    // Given
    String query = "bäckerei";
    SearchResults mockResults = createMockQuickSearchResults();

    when(searchService.quickSearch(eq(query), eq(5))).thenReturn(mockResults);

    // When & Then
    given()
        .queryParam("query", query)
        .queryParam("limit", 5)
        .when()
        .get("/api/search/quick")
        .then()
        .statusCode(200)
        .contentType(MediaType.APPLICATION_JSON)
        .body("customers", hasSize(1))
        .body("contacts", hasSize(0)) // Quick search doesn't include contacts
        .body("totalCount", equalTo(1))
        .body("executionTime", greaterThan(0));

    verify(searchService).quickSearch(eq(query), eq(5));
  }

  @Test
  @TestSecurity(
      user = "testuser",
      roles = {"sales"})
  void quickSearch_withDefaultLimit_shouldUseDefault() {
    // Given
    SearchResults mockResults = createMockQuickSearchResults();
    when(searchService.quickSearch(eq("test"), eq(5))).thenReturn(mockResults);

    // When & Then
    given()
        .queryParam("query", "test") // Only required param
        .when()
        .get("/api/search/quick")
        .then()
        .statusCode(200);

    // Verify default limit was used
    verify(searchService).quickSearch(eq("test"), eq(5));
  }

  @Test
  @TestSecurity(
      user = "testuser",
      roles = {"sales"})
  void quickSearch_withMissingQuery_shouldReturn400() {
    // When & Then
    given().queryParam("limit", 5).when().get("/api/search/quick").then().statusCode(400);
  }

  @Test
  @TestSecurity(
      user = "testuser",
      roles = {"sales"})
  void quickSearch_withTooLongQuery_shouldReturn400() {
    // Given
    String longQuery = "a".repeat(51); // Too long (max 50 chars)

    // When & Then
    given().queryParam("query", longQuery).when().get("/api/search/quick").then().statusCode(400);
  }

  @Test
  @TestSecurity(
      user = "testuser",
      roles = {"sales"})
  void quickSearch_withInvalidLimit_shouldReturn400() {
    // When & Then
    given()
        .queryParam("query", "test")
        .queryParam("limit", 21) // Too high (max 20)
        .when()
        .get("/api/search/quick")
        .then()
        .statusCode(400);
  }

  @Test
  @TestSecurity(
      user = "testuser",
      roles = {"sales"})
  void quickSearch_withServiceException_shouldReturn500() {
    // Given
    when(searchService.quickSearch(anyString(), anyInt()))
        .thenThrow(new RuntimeException("Search index unavailable"));

    // When & Then
    given().queryParam("query", "test").when().get("/api/search/quick").then().statusCode(500);
  }

  // Helper methods for creating test data
  private SearchResults createMockSearchResults() {
    // Customer result
    CustomerSearchDto customerDto = new CustomerSearchDto();
    customerDto.setId("customer-1");
    customerDto.setCompanyName("Bäckerei Schmidt GmbH");
    customerDto.setCustomerNumber("KD-2025-00001");
    customerDto.setStatus("AKTIV");

    SearchResult customerResult =
        SearchResult.builder()
            .type("customer")
            .id("customer-1")
            .data(customerDto)
            .relevanceScore(85)
            .matchedFields(List.of("companyName"))
            .build();

    // Contact result
    ContactSearchDto contactDto = new ContactSearchDto();
    contactDto.setId("contact-1");
    contactDto.setFirstName("Hans");
    contactDto.setLastName("Schmidt");
    contactDto.setEmail("schmidt@baeckerei.de");
    contactDto.setCustomerId("customer-1");
    contactDto.setCustomerName("Bäckerei Schmidt GmbH");
    contactDto.setIsPrimary(true);

    SearchResult contactResult =
        SearchResult.builder()
            .type("contact")
            .id("contact-1")
            .data(contactDto)
            .relevanceScore(90)
            .matchedFields(List.of("lastName"))
            .build();

    return SearchResults.builder()
        .customers(List.of(customerResult))
        .contacts(List.of(contactResult))
        .totalCount(2)
        .executionTime(25L)
        .build();
  }

  private SearchResults createMockQuickSearchResults() {
    CustomerSearchDto customerDto = new CustomerSearchDto();
    customerDto.setId("customer-1");
    customerDto.setCompanyName("Bäckerei Schmidt GmbH");
    customerDto.setCustomerNumber("KD-2025-00001");
    customerDto.setStatus("AKTIV");

    SearchResult customerResult =
        SearchResult.builder()
            .type("customer")
            .id("customer-1")
            .data(customerDto)
            .relevanceScore(100) // Quick search uses default score
            .matchedFields(List.of("companyName", "customerNumber"))
            .build();

    return SearchResults.builder()
        .customers(List.of(customerResult))
        .contacts(List.of()) // Quick search doesn't include contacts
        .totalCount(1)
        .executionTime(15L)
        .build();
  }

  private SearchResults createEmptySearchResults() {
    return SearchResults.builder()
        .customers(List.of())
        .contacts(List.of())
        .totalCount(0)
        .executionTime(5L)
        .build();
  }
}
