package de.freshplan.domain.opportunity.service;

import static org.assertj.core.api.Assertions.*;

import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.entity.CustomerStatus;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.opportunity.entity.Opportunity;
import de.freshplan.domain.opportunity.entity.OpportunityStage;
import de.freshplan.domain.opportunity.repository.OpportunityRepository;
import de.freshplan.domain.opportunity.service.dto.CreateOpportunityForCustomerRequest;
import de.freshplan.test.builders.CustomerTestDataFactory;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Integration Tests für OpportunityService.createForCustomer()
 *
 * <p>Testet die Customer → Opportunity Creation (Upsell/Cross-sell/Renewal) mit folgenden
 * Szenarien:
 * - Happy Path: AKTIV Customer → Opportunity (NEEDS_ANALYSIS Stage)
 * - Validation: Customer muss AKTIV sein (nicht INAKTIV/TEST/DELETED)
 * - Use Cases: Upsell, Cross-sell, Renewal
 * - Auto-Name-Generation: "{opportunityType} - {customer.name}"
 *
 * @author FreshPlan Team
 * @since Sprint 2.1.7 (Opportunity Backend Integration)
 */
@QuarkusTest
@Tag("integration")
@TestSecurity(user = "testuser", roles = {"admin", "manager", "sales"})
@DisplayName("OpportunityService.createForCustomer() Integration Tests")
public class OpportunityServiceCreateForCustomerTest {

  @Inject OpportunityService opportunityService;

  @Inject OpportunityRepository opportunityRepository;

  @Inject CustomerRepository customerRepository;

  private Customer activeCustomer;

  @BeforeEach
  @Transactional
  void setUp() {
    // Clean up test data
    opportunityRepository.deleteAll();
    customerRepository.delete("companyName LIKE ?1", "[TEST-OPP-CUST]%");

    // Create active Customer
    activeCustomer =
        CustomerTestDataFactory.builder()
            .withCompanyName("[TEST-OPP-CUST] Kantine Schulweg 45")
            .withCity("München")
            .withPostalCode("80331")
            .withStreet("Schulweg 45")
            .withCountry("Deutschland")
            .withStatus(CustomerStatus.AKTIV)
            .build();
    customerRepository.persist(activeCustomer);
  }

  @Test
  @Transactional
  @DisplayName("Should create Opportunity for active Customer (Upsell)")
  void createForCustomer_upsellScenario_shouldCreateOpportunity() {
    // Arrange
    var request =
        CreateOpportunityForCustomerRequest.builder()
            .name("Upsell: Kantine Schulweg 45 - Expansion")
            .opportunityType("Upsell")
            .timeframe("H2 2025")
            .expectedValue(BigDecimal.valueOf(18000))
            .expectedCloseDate(LocalDate.now().plusDays(45))
            .build();

    // Act
    Opportunity result = opportunityService.createForCustomer(activeCustomer.getId(), request);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getName()).isEqualTo("Upsell: Kantine Schulweg 45 - Expansion");
    assertThat(result.getStage())
        .as("Opportunity should start at NEEDS_ANALYSIS (Customer already qualified)")
        .isEqualTo(OpportunityStage.NEEDS_ANALYSIS);
    assertThat(result.getExpectedValue()).isEqualByComparingTo(BigDecimal.valueOf(18000));

    // Verify customer_id FK is set
    assertThat(result.getCustomer()).isNotNull();
    assertThat(result.getCustomer().getId()).isEqualTo(activeCustomer.getId());

    // Verify lead_id is NULL (no Lead involved)
    assertThat(result.getLead())
        .as("Opportunity from Customer should have lead_id = NULL")
        .isNull();
  }

  @Test
  @Transactional
  @DisplayName("Should create Opportunity for active Customer (Cross-sell)")
  void createForCustomer_crossSellScenario_shouldCreateOpportunity() {
    // Arrange
    var request =
        CreateOpportunityForCustomerRequest.builder()
            .name("Cross-Sell: Kantine Schulweg 45 - Bio-Produktlinie")
            .opportunityType("Cross-sell")
            .timeframe("Q4 2025")
            .expectedValue(BigDecimal.valueOf(25000))
            .expectedCloseDate(LocalDate.now().plusDays(120))
            .build();

    // Act
    Opportunity result = opportunityService.createForCustomer(activeCustomer.getId(), request);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getName()).contains("Cross-Sell");
    assertThat(result.getStage()).isEqualTo(OpportunityStage.NEEDS_ANALYSIS);
  }

  @Test
  @Transactional
  @DisplayName("Should create Opportunity for active Customer (Renewal)")
  void createForCustomer_renewalScenario_shouldCreateOpportunity() {
    // Arrange
    var request =
        CreateOpportunityForCustomerRequest.builder()
            .name("Renewal: Kantine Schulweg 45 - Vertragsverlängerung")
            .opportunityType("Renewal")
            .timeframe("Q1 2026")
            .expectedValue(BigDecimal.valueOf(40000))
            .expectedCloseDate(LocalDate.now().plusDays(180))
            .build();

    // Act
    Opportunity result = opportunityService.createForCustomer(activeCustomer.getId(), request);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getName()).contains("Renewal");
    assertThat(result.getStage()).isEqualTo(OpportunityStage.NEEDS_ANALYSIS);
  }

  @Test
  @Transactional
  @DisplayName("Should fail when Customer is not AKTIV")
  void createForCustomer_withInactiveCustomer_shouldThrowException() {
    // Arrange: Create INAKTIV Customer
    Customer inactiveCustomer =
        CustomerTestDataFactory.builder()
            .withCompanyName("[TEST-OPP-CUST] Inactive Customer GmbH")
            .withCity("Berlin")
            .withStatus(CustomerStatus.INAKTIV)
            .build();
    customerRepository.persist(inactiveCustomer);

    var request =
        CreateOpportunityForCustomerRequest.builder()
            .name("Test Opportunity")
            .opportunityType("Upsell")
            .timeframe("Q1 2025")
            .expectedValue(BigDecimal.valueOf(10000))
            .expectedCloseDate(LocalDate.now().plusDays(30))
            .build();

    // Act & Assert
    assertThatThrownBy(
            () -> opportunityService.createForCustomer(inactiveCustomer.getId(), request))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Customer must be active");
  }

  @Test
  @Transactional
  @DisplayName("Should fail when Customer does not exist")
  void createForCustomer_withNonExistentCustomer_shouldThrowException() {
    // Arrange
    var nonExistentId = java.util.UUID.randomUUID();
    var request =
        CreateOpportunityForCustomerRequest.builder()
            .name("Test Opportunity")
            .opportunityType("Upsell")
            .timeframe("Q1 2025")
            .expectedValue(BigDecimal.valueOf(10000))
            .expectedCloseDate(LocalDate.now().plusDays(30))
            .build();

    // Act & Assert
    assertThatThrownBy(() -> opportunityService.createForCustomer(nonExistentId, request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Customer not found");
  }

  @Test
  @Transactional
  @DisplayName("Should allow zero expectedValue (validation happens in DTO layer)")
  void createForCustomer_withZeroExpectedValue_shouldSucceed() {
    // Arrange: Zero value is allowed (validation happens at DTO level)
    var request =
        CreateOpportunityForCustomerRequest.builder()
            .name("Test Opportunity")
            .opportunityType("Upsell")
            .timeframe("Q1 2025")
            .expectedValue(BigDecimal.ZERO)
            .expectedCloseDate(LocalDate.now().plusDays(30))
            .build();

    // Act
    Opportunity result = opportunityService.createForCustomer(activeCustomer.getId(), request);

    // Assert: Opportunity created successfully
    assertThat(result).isNotNull();
    assertThat(result.getName()).isEqualTo("Test Opportunity");
    assertThat(result.getExpectedValue()).isEqualByComparingTo(BigDecimal.ZERO);
  }

  @Test
  @Transactional
  @DisplayName("Should auto-generate name when not provided")
  void createForCustomer_withoutName_shouldAutoGenerateName() {
    // Arrange
    var request =
        CreateOpportunityForCustomerRequest.builder()
            .name(null) // ← Auto-generate!
            .opportunityType("Upsell")
            .timeframe("H1 2025")
            .expectedValue(BigDecimal.valueOf(15000))
            .expectedCloseDate(LocalDate.now().plusDays(60))
            .build();

    // Act
    Opportunity result = opportunityService.createForCustomer(activeCustomer.getId(), request);

    // Assert: Name should be auto-generated: "{opportunityType} - {customer.name}"
    assertThat(result.getName())
        .as("Name should be auto-generated from opportunityType and customer.companyName")
        .contains("Upsell")
        .contains(activeCustomer.getCompanyName());
  }

  @Test
  @Transactional
  @DisplayName("Should validate multiple opportunities for same Customer")
  void createForCustomer_multipleOpportunities_shouldAllowMultiple() {
    // Arrange: Create first Opportunity (Upsell)
    var upsellRequest =
        CreateOpportunityForCustomerRequest.builder()
            .name("Upsell: Expansion")
            .opportunityType("Upsell")
            .timeframe("Q1 2025")
            .expectedValue(BigDecimal.valueOf(10000))
            .expectedCloseDate(LocalDate.now().plusDays(30))
            .build();

    // Act: Create first Opportunity
    Opportunity firstOpp = opportunityService.createForCustomer(activeCustomer.getId(), upsellRequest);
    assertThat(firstOpp).isNotNull();

    // Arrange: Create second Opportunity (Cross-sell)
    var crossSellRequest =
        CreateOpportunityForCustomerRequest.builder()
            .name("Cross-Sell: Bio-Linie")
            .opportunityType("Cross-sell")
            .timeframe("Q2 2025")
            .expectedValue(BigDecimal.valueOf(15000))
            .expectedCloseDate(LocalDate.now().plusDays(60))
            .build();

    // Act: Create second Opportunity
    Opportunity secondOpp = opportunityService.createForCustomer(activeCustomer.getId(), crossSellRequest);

    // Assert: Both Opportunities should exist for same Customer
    assertThat(secondOpp).isNotNull();
    assertThat(secondOpp.getId()).isNotEqualTo(firstOpp.getId());
    assertThat(secondOpp.getCustomer().getId()).isEqualTo(activeCustomer.getId());
    assertThat(firstOpp.getCustomer().getId()).isEqualTo(activeCustomer.getId());
  }
}
