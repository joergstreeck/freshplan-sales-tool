package de.freshplan.domain.customer.service.dto;

import static org.junit.jupiter.api.Assertions.*;

import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.entity.CustomerStatus;
import de.freshplan.test.builders.CustomerTestDataFactory;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for CustomerResponseBuilder - Seasonal Business Support.
 *
 * <p>Sprint 2.1.7.4 - Section 7: Seasonal Business Builder Tests
 *
 * <p>Verifies that CustomerResponseBuilder correctly builds CustomerResponse DTOs with seasonal
 * business fields, including the fromEntity() convenience method.
 *
 * @author FreshPlan Team
 * @since Sprint 2.1.7.4
 */
@DisplayName("CustomerResponseBuilder - Seasonal Business Support")
class CustomerResponseBuilderTest {

  /**
   * Sprint 2.1.7.4 - Test 1: Builder with all seasonal fields.
   *
   * <p>Verifies that the fluent builder API correctly sets all seasonal business fields and builds
   * a CustomerResponse with those values.
   */
  @Test
  @DisplayName("Builder should set all seasonal business fields correctly")
  void testBuilder_shouldSetAllSeasonalFields() {
    // GIVEN: Builder with seasonal business data
    List<Integer> activeMonths = List.of(5, 6, 7, 8, 9); // May - September

    // WHEN: Building CustomerResponse with seasonal fields
    CustomerResponse response =
        CustomerResponseBuilder.builder()
            .id("550e8400-e29b-41d4-a716-446655440000")
            .customerNumber("KD-SEASONAL-001")
            .companyName("Eiscafé Venezia")
            .status(CustomerStatus.AKTIV)
            .isSeasonalBusiness(true) // Sprint 2.1.7.4 - NEW
            .seasonalMonths(activeMonths) // Sprint 2.1.7.4 - NEW
            .seasonalPattern("SUMMER") // Sprint 2.1.7.4 - NEW
            .build();

    // THEN: Response should contain all seasonal fields
    assertNotNull(response, "CustomerResponse should not be null");
    assertEquals(true, response.isSeasonalBusiness(), "isSeasonalBusiness should be true");
    assertNotNull(response.seasonalMonths(), "seasonalMonths should not be null");
    assertEquals(
        List.of(5, 6, 7, 8, 9), response.seasonalMonths(), "seasonalMonths should match builder");
    assertEquals("SUMMER", response.seasonalPattern(), "seasonalPattern should match builder");

    // THEN: Other fields should also be present
    assertEquals("550e8400-e29b-41d4-a716-446655440000", response.id(), "ID should match builder");
    assertEquals(
        "KD-SEASONAL-001", response.customerNumber(), "Customer number should match builder");
    assertEquals("Eiscafé Venezia", response.companyName(), "Company name should match builder");
    assertEquals(CustomerStatus.AKTIV, response.status(), "Status should match builder");
  }

  /**
   * Sprint 2.1.7.4 - Test 2: fromEntity() maps seasonal fields correctly.
   *
   * <p>Verifies that the fromEntity() convenience method correctly copies all seasonal business
   * fields from Customer entity to CustomerResponseBuilder.
   */
  @Test
  @DisplayName("fromEntity() should map seasonal fields from Customer entity")
  void testFromEntity_shouldMapSeasonalFields() {
    // GIVEN: Customer entity with seasonal business data
    Customer customer =
        CustomerTestDataFactory.builder()
            .withCompanyName("Biergarten Oktoberfest")
            .withCustomerNumber("KD-SEASONAL-002")
            .withStatus(CustomerStatus.AKTIV)
            .build();

    // Set ID and seasonal fields
    customer.setId(java.util.UUID.fromString("660e8400-e29b-41d4-a716-446655440000"));
    customer.setIsSeasonalBusiness(true);
    customer.setSeasonalMonths(List.of(9, 10)); // September - October
    customer.setSeasonalPattern("AUTUMN");

    // WHEN: Building CustomerResponse from entity
    CustomerResponse response = CustomerResponseBuilder.builder().fromEntity(customer).build();

    // THEN: Response should contain all seasonal fields from entity
    assertNotNull(response, "CustomerResponse should not be null");
    assertEquals(
        true, response.isSeasonalBusiness(), "isSeasonalBusiness should be copied from entity");
    assertNotNull(response.seasonalMonths(), "seasonalMonths should not be null");
    assertEquals(
        List.of(9, 10), response.seasonalMonths(), "seasonalMonths should be copied from entity");
    assertEquals(
        "AUTUMN", response.seasonalPattern(), "seasonalPattern should be copied from entity");

    // THEN: Other essential fields should also be mapped
    assertEquals(
        "660e8400-e29b-41d4-a716-446655440000", response.id(), "ID should be copied from entity");
    assertEquals(
        "KD-SEASONAL-002",
        response.customerNumber(),
        "Customer number should be copied from entity");
    assertEquals(
        "Biergarten Oktoberfest",
        response.companyName(),
        "Company name should be copied from entity");
    assertEquals(CustomerStatus.AKTIV, response.status(), "Status should be copied from entity");
  }
}
