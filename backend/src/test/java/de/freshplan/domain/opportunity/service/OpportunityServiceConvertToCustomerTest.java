package de.freshplan.domain.opportunity.service;

import static org.assertj.core.api.Assertions.*;

import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.entity.CustomerStatus;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.opportunity.entity.Opportunity;
import de.freshplan.domain.opportunity.entity.OpportunityStage;
import de.freshplan.domain.opportunity.repository.OpportunityRepository;
import de.freshplan.domain.opportunity.service.dto.ConvertToCustomerRequest;
import de.freshplan.modules.leads.domain.Lead;
import de.freshplan.modules.leads.domain.LeadStatus;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Integration Tests für OpportunityService.convertToCustomer()
 *
 * <p>Testet die Opportunity → Customer Conversion mit folgenden Szenarien: - Happy Path: CLOSED_WON
 * Opportunity → Customer (AKTIV Status) - originalLeadId Traceability (V261): Wenn Opportunity from
 * Lead → Customer.originalLeadId wird gesetzt - Validation: Nur CLOSED_WON Opportunities können
 * konvertiert werden - Duplicate Prevention: Keine doppelte Conversion
 *
 * @author FreshPlan Team
 * @since Sprint 2.1.7 (Opportunity Backend Integration)
 */
@QuarkusTest
@Tag("integration")
@TestSecurity(
    user = "testuser",
    roles = {"admin", "manager", "sales"})
@DisplayName("OpportunityService.convertToCustomer() Integration Tests")
public class OpportunityServiceConvertToCustomerTest {

  @Inject OpportunityService opportunityService;

  @Inject jakarta.persistence.EntityManager em;

  @Inject OpportunityRepository opportunityRepository;

  @Inject CustomerRepository customerRepository;

  @AfterEach
  @Transactional
  void cleanup() {
    // Cleanup handled by @BeforeEach setUp() which calls repository.deleteAll()
    // No test_marker column exists in opportunities/customers tables
  }

  private Opportunity testOpportunity;
  private Lead originLead;

  @BeforeEach
  @Transactional
  void setUp() {
    // Clean up test data
    customerRepository.delete("companyName LIKE ?1", "[TEST-OPP-CONV]%");
    opportunityRepository.deleteAll();
    Lead.delete("companyName LIKE ?1", "[TEST-OPP-CONV]%");

    // Create origin Lead (for traceability test)
    originLead = new Lead();
    originLead.companyName = "[TEST-OPP-CONV] Hotel Seeblick GmbH";
    originLead.status = LeadStatus.CONVERTED;
    originLead.registeredAt = LocalDateTime.now().minusDays(60);
    originLead.createdBy = "test-user";
    originLead.persist();

    // Create CLOSED_WON Opportunity from Lead
    testOpportunity = new Opportunity();
    testOpportunity.setName("Hotel Seeblick - Vollversorgung Q2 2025");
    testOpportunity.setStage(OpportunityStage.CLOSED_WON);
    testOpportunity.setExpectedValue(BigDecimal.valueOf(336000));
    testOpportunity.setExpectedCloseDate(LocalDate.now().minusDays(5));
    testOpportunity.setProbability(100);
    testOpportunity.setLead(originLead); // ← Link to Lead (for originalLeadId)
    opportunityRepository.persist(testOpportunity);
  }

  @Test
  @Transactional
  @DisplayName("Should convert CLOSED_WON Opportunity to Customer")
  void convertToCustomer_withClosedWonOpportunity_shouldCreateCustomer() {
    // Arrange
    var request =
        ConvertToCustomerRequest.builder()
            .companyName("Hotel Seeblick GmbH")
            .street("Seestraße 42")
            .postalCode("82319")
            .city("Starnberg")
            .country("Deutschland")
            .createContactFromLead(false) // Simplified test
            .build();

    // Act
    Customer result = opportunityService.convertToCustomer(testOpportunity.getId(), request);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getCompanyName()).isEqualTo("Hotel Seeblick GmbH");
    assertThat(result.getStatus()).isEqualTo(CustomerStatus.AKTIV);

    // Verify Customer was persisted
    Customer persistedCustomer = customerRepository.findById(result.getId());
    assertThat(persistedCustomer).isNotNull();
    assertThat(persistedCustomer.getCompanyName()).isEqualTo("Hotel Seeblick GmbH");
  }

  @Test
  @Transactional
  @DisplayName("Should set originalLeadId when Opportunity originated from Lead (V261)")
  void convertToCustomer_fromLeadOpportunity_shouldSetOriginalLeadId() {
    // Arrange
    var request =
        ConvertToCustomerRequest.builder()
            .companyName("Hotel Seeblick GmbH")
            .street("Seestraße 42")
            .postalCode("82319")
            .city("Starnberg")
            .country("Deutschland")
            .createContactFromLead(false)
            .build();

    // Act
    Customer result = opportunityService.convertToCustomer(testOpportunity.getId(), request);

    // Assert: originalLeadId should be set (V261 field)
    assertThat(result.getOriginalLeadId())
        .as("Customer.originalLeadId should be set when Opportunity came from Lead")
        .isNotNull()
        .isEqualTo(originLead.id);

    // Verify full traceability: Lead → Opportunity → Customer
    assertThat(testOpportunity.getLead().id)
        .as("Opportunity.lead_id should match")
        .isEqualTo(originLead.id);
    assertThat(result.getOriginalLeadId())
        .as("Customer.originalLeadId should match Lead.id")
        .isEqualTo(originLead.id);
  }

  @Test
  @Transactional
  @DisplayName("Should fail when Opportunity is not CLOSED_WON")
  void convertToCustomer_withNonClosedWonOpportunity_shouldThrowException() {
    // Arrange: Create NEGOTIATION Opportunity
    Opportunity negotiationOpp = new Opportunity();
    negotiationOpp.setName("Test Opportunity - NEGOTIATION");
    negotiationOpp.setStage(OpportunityStage.NEGOTIATION); // ← Not CLOSED_WON!
    negotiationOpp.setExpectedValue(BigDecimal.valueOf(20000));
    negotiationOpp.setExpectedCloseDate(LocalDate.now().plusDays(15));
    negotiationOpp.setProbability(80);
    opportunityRepository.persist(negotiationOpp);

    var request =
        ConvertToCustomerRequest.builder()
            .companyName("Test Company GmbH")
            .street("Teststraße 1")
            .postalCode("12345")
            .city("Berlin")
            .country("Deutschland")
            .build();

    // Act & Assert
    assertThatThrownBy(() -> opportunityService.convertToCustomer(negotiationOpp.getId(), request))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Only won opportunities");
  }

  @Test
  @Transactional
  @DisplayName("Should fail when Opportunity does not exist")
  void convertToCustomer_withNonExistentOpportunity_shouldThrowException() {
    // Arrange
    var nonExistentId = java.util.UUID.randomUUID();
    var request =
        ConvertToCustomerRequest.builder()
            .companyName("Test Company GmbH")
            .street("Teststraße 1")
            .postalCode("12345")
            .city("Berlin")
            .country("Deutschland")
            .build();

    // Act & Assert
    assertThatThrownBy(() -> opportunityService.convertToCustomer(nonExistentId, request))
        .isInstanceOf(
            de.freshplan.domain.opportunity.service.exception.OpportunityNotFoundException.class);
  }

  @Test
  @Transactional
  @DisplayName("Should prevent duplicate Customer conversion")
  void convertToCustomer_alreadyConverted_shouldThrowException() {
    // Arrange: Convert once
    var request =
        ConvertToCustomerRequest.builder()
            .companyName("Hotel Seeblick GmbH")
            .street("Seestraße 42")
            .postalCode("82319")
            .city("Starnberg")
            .country("Deutschland")
            .createContactFromLead(false)
            .build();

    Customer firstConversion =
        opportunityService.convertToCustomer(testOpportunity.getId(), request);
    assertThat(firstConversion).isNotNull();

    // Act & Assert: Try to convert again
    assertThatThrownBy(() -> opportunityService.convertToCustomer(testOpportunity.getId(), request))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("already linked to customer");
  }

  // NOTE: The test "convertToCustomer_fromCustomerOpportunity_shouldNotSetOriginalLeadId" was
  // removed
  // because it tested an invalid scenario: An Opportunity created FOR a Customer
  // (Upsell/Cross-sell)
  // should NOT be converted BACK to Customer. The convertToCustomer() method is only for
  // Lead → Opportunity → Customer workflow, NOT for Customer → Opportunity → Customer.
  // The database constraint chk_opportunity_has_source enforces that opportunity must have
  // either customer_id OR lead_id, and convertToCustomer() checks if customer_id is already set
  // (throws "already linked to customer" exception).
}
