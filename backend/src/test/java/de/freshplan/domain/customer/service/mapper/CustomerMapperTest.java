package de.freshplan.domain.customer.service.mapper;

import static org.junit.jupiter.api.Assertions.*;

import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.entity.CustomerStatus;
import de.freshplan.domain.customer.service.dto.CustomerResponse;
import de.freshplan.test.builders.CustomerTestDataFactory;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for CustomerMapper - Seasonal Business Support.
 *
 * <p>Sprint 2.1.7.4 - Section 7: Seasonal Business Mapping Tests
 *
 * <p>Verifies that CustomerMapper correctly maps seasonal business fields from Customer entity to
 * CustomerResponse DTO, including edge cases with null values.
 *
 * @author FreshPlan Team
 * @since Sprint 2.1.7.4
 */
@QuarkusTest
@Tag("integration")
@DisplayName("CustomerMapper - Seasonal Business Support")
class CustomerMapperTest {

  @Inject CustomerMapper customerMapper;

  /**
   * Sprint 2.1.7.4 - Test 1: toResponse() maps seasonal fields correctly.
   *
   * <p>Verifies that toResponse() copies all seasonal business fields from Customer entity to
   * CustomerResponse DTO.
   */
  @Test
  @DisplayName("toResponse() should map seasonal business fields correctly")
  void testToResponse_shouldMapSeasonalFields() {
    // GIVEN: Customer entity with seasonal business data
    Customer customer =
        CustomerTestDataFactory.builder()
            .withCompanyName("Eiscafé Venezia")
            .withCustomerNumber("KD-SEASONAL-001")
            .withStatus(CustomerStatus.AKTIV)
            .build();

    // Set ID (required for mapping)
    customer.setId(java.util.UUID.randomUUID());

    // Set seasonal business fields
    customer.setIsSeasonalBusiness(true);
    customer.setSeasonalMonths(List.of(5, 6, 7, 8, 9)); // May - September
    customer.setSeasonalPattern("SUMMER");
    customer.setCreatedAt(LocalDateTime.now().minusDays(30));

    // WHEN: Mapping to CustomerResponse
    CustomerResponse response = customerMapper.toResponse(customer);

    // THEN: Response should not be null
    assertNotNull(response, "CustomerResponse should not be null");

    // THEN: Seasonal fields should be correctly mapped
    assertEquals(
        true, response.isSeasonalBusiness(), "isSeasonalBusiness should be mapped from entity");
    assertNotNull(response.seasonalMonths(), "seasonalMonths should not be null");
    assertEquals(
        List.of(5, 6, 7, 8, 9),
        response.seasonalMonths(),
        "seasonalMonths should match entity values");
    assertEquals(
        "SUMMER", response.seasonalPattern(), "seasonalPattern should be mapped from entity");

    // THEN: Other essential fields should also be mapped
    assertEquals("KD-SEASONAL-001", response.customerNumber(), "Customer number should be mapped");
    assertEquals("Eiscafé Venezia", response.companyName(), "Company name should be mapped");
    assertEquals(CustomerStatus.AKTIV, response.status(), "Status should be mapped");
  }

  /**
   * Sprint 2.1.7.4 - Test 2: toResponse() handles null seasonal fields gracefully.
   *
   * <p>Verifies that toResponse() correctly handles customers without seasonal business data (null
   * values).
   */
  @Test
  @DisplayName("toResponse() should handle null seasonal fields gracefully")
  void testToResponse_shouldHandleNullSeasonalFields() {
    // GIVEN: Regular customer (non-seasonal) with all seasonal fields as null
    Customer customer =
        CustomerTestDataFactory.builder()
            .withCompanyName("Regular Business GmbH")
            .withCustomerNumber("KD-REGULAR-001")
            .withStatus(CustomerStatus.AKTIV)
            .build();

    // Set ID (required for mapping)
    customer.setId(java.util.UUID.randomUUID());

    // Explicitly set seasonal fields to null (default state)
    customer.setIsSeasonalBusiness(null);
    customer.setSeasonalMonths(null);
    customer.setSeasonalPattern(null);

    // WHEN: Mapping to CustomerResponse
    CustomerResponse response = customerMapper.toResponse(customer);

    // THEN: Response should not be null
    assertNotNull(response, "CustomerResponse should not be null");

    // THEN: Seasonal fields should be null (not throwing exceptions)
    assertNull(
        response.isSeasonalBusiness(), "isSeasonalBusiness should be null for regular customer");
    assertNull(response.seasonalMonths(), "seasonalMonths should be null for regular customer");
    assertNull(response.seasonalPattern(), "seasonalPattern should be null for regular customer");

    // THEN: Other fields should still be mapped correctly
    assertEquals("KD-REGULAR-001", response.customerNumber(), "Customer number should be mapped");
    assertEquals("Regular Business GmbH", response.companyName(), "Company name should be mapped");
  }

  /**
   * Sprint 2.1.7.4 - Test 3: toMinimalResponse() maps seasonal fields correctly.
   *
   * <p>Verifies that toMinimalResponse() sets seasonal fields to null (minimal response for list
   * views).
   *
   * <p>NOTE: toMinimalResponse() is designed to reduce payload size for list views, so seasonal
   * fields should be null.
   */
  @Test
  @DisplayName("toMinimalResponse() should set seasonal fields to null (minimal payload)")
  void testToMinimalResponse_shouldSetSeasonalFieldsToNull() {
    // GIVEN: Seasonal customer with full data
    Customer customer =
        CustomerTestDataFactory.builder()
            .withCompanyName("Biergarten Oktoberfest")
            .withCustomerNumber("KD-SEASONAL-002")
            .withStatus(CustomerStatus.AKTIV)
            .build();

    // Set ID (required for mapping)
    customer.setId(java.util.UUID.randomUUID());

    customer.setIsSeasonalBusiness(true);
    customer.setSeasonalMonths(List.of(9, 10)); // September - October
    customer.setSeasonalPattern("AUTUMN");

    // WHEN: Mapping to minimal CustomerResponse
    CustomerResponse minimalResponse = customerMapper.toMinimalResponse(customer);

    // THEN: Response should not be null
    assertNotNull(minimalResponse, "Minimal CustomerResponse should not be null");

    // THEN: Seasonal fields should be null (excluded from minimal response)
    assertNull(
        minimalResponse.isSeasonalBusiness(),
        "isSeasonalBusiness should be null in minimal response");
    assertNull(
        minimalResponse.seasonalMonths(), "seasonalMonths should be null in minimal response");
    assertNull(
        minimalResponse.seasonalPattern(), "seasonalPattern should be null in minimal response");

    // THEN: Essential fields should still be present
    assertEquals(
        "KD-SEASONAL-002",
        minimalResponse.customerNumber(),
        "Customer number should be in minimal response");
    assertEquals(
        "Biergarten Oktoberfest",
        minimalResponse.companyName(),
        "Company name should be in minimal response");
    assertEquals(
        CustomerStatus.AKTIV, minimalResponse.status(), "Status should be in minimal response");
  }

  /**
   * Sprint 2.1.7.4 - Test 4: toMinimalResponse() handles null customer gracefully.
   *
   * <p>Verifies that toMinimalResponse() returns null when input customer is null (defensive
   * programming).
   */
  @Test
  @DisplayName("toMinimalResponse() should return null when customer is null")
  void testToMinimalResponse_shouldReturnNullWhenCustomerIsNull() {
    // GIVEN: Null customer
    Customer customer = null;

    // WHEN: Mapping to minimal CustomerResponse
    CustomerResponse minimalResponse = customerMapper.toMinimalResponse(customer);

    // THEN: Response should be null (defensive programming)
    assertNull(
        minimalResponse, "toMinimalResponse() should return null when input customer is null");
  }

  /**
   * Sprint 2.1.7.2 - D4 - Test 1: toResponse() maps default churnThresholdDays (90).
   *
   * <p>Verifies that toResponse() correctly maps churnThresholdDays from Customer entity to
   * CustomerResponse DTO with default value.
   */
  @Test
  @DisplayName("toResponse() should map default churnThresholdDays (90)")
  void testToResponse_shouldMapDefaultChurnThresholdDays() {
    // GIVEN: Customer entity with default churnThresholdDays (90)
    Customer customer =
        CustomerTestDataFactory.builder()
            .withCompanyName("Hotel Churn Test GmbH")
            .withCustomerNumber("KD-CHURN-001")
            .withStatus(CustomerStatus.AKTIV)
            .build();

    customer.setId(java.util.UUID.randomUUID());
    // churnThresholdDays defaults to 90 in Customer entity
    customer.setChurnThresholdDays(90);

    // WHEN: Mapping to CustomerResponse
    CustomerResponse response = customerMapper.toResponse(customer);

    // THEN: churnThresholdDays should be mapped correctly
    assertNotNull(response, "CustomerResponse should not be null");
    assertEquals(
        90, response.churnThresholdDays(), "churnThresholdDays should be 90 (default value)");
  }

  /**
   * Sprint 2.1.7.2 - D4 - Test 2: toResponse() maps custom churnThresholdDays (30).
   *
   * <p>Verifies that toResponse() correctly maps custom churnThresholdDays values.
   */
  @Test
  @DisplayName("toResponse() should map custom churnThresholdDays (30)")
  void testToResponse_shouldMapCustomChurnThresholdDays() {
    // GIVEN: Customer entity with custom churnThresholdDays (30)
    Customer customer =
        CustomerTestDataFactory.builder()
            .withCompanyName("Fast Food Chain GmbH")
            .withCustomerNumber("KD-CHURN-002")
            .withStatus(CustomerStatus.AKTIV)
            .build();

    customer.setId(java.util.UUID.randomUUID());
    customer.setChurnThresholdDays(30); // Custom value: 30 days

    // WHEN: Mapping to CustomerResponse
    CustomerResponse response = customerMapper.toResponse(customer);

    // THEN: churnThresholdDays should be mapped correctly
    assertNotNull(response, "CustomerResponse should not be null");
    assertEquals(
        30, response.churnThresholdDays(), "churnThresholdDays should be 30 (custom value)");
  }

  /**
   * Sprint 2.1.7.2 - D4 - Test 3: toMinimalResponse() sets churnThresholdDays to null.
   *
   * <p>Verifies that toMinimalResponse() excludes churnThresholdDays from minimal response
   * (performance optimization).
   */
  @Test
  @DisplayName("toMinimalResponse() should set churnThresholdDays to null (minimal payload)")
  void testToMinimalResponse_shouldSetChurnThresholdDaysToNull() {
    // GIVEN: Customer with custom churnThresholdDays
    Customer customer =
        CustomerTestDataFactory.builder()
            .withCompanyName("Restaurant Test GmbH")
            .withCustomerNumber("KD-CHURN-003")
            .withStatus(CustomerStatus.AKTIV)
            .build();

    customer.setId(java.util.UUID.randomUUID());
    customer.setChurnThresholdDays(60);

    // WHEN: Mapping to minimal CustomerResponse
    CustomerResponse minimalResponse = customerMapper.toMinimalResponse(customer);

    // THEN: churnThresholdDays should be null (excluded from minimal response)
    assertNotNull(minimalResponse, "Minimal CustomerResponse should not be null");
    assertNull(
        minimalResponse.churnThresholdDays(),
        "churnThresholdDays should be null in minimal response");
  }
}
