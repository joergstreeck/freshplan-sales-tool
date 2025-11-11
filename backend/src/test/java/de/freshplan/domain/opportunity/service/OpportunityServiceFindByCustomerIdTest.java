package de.freshplan.domain.opportunity.service;

import static org.assertj.core.api.Assertions.*;

import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.entity.CustomerStatus;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.opportunity.entity.OpportunityStage;
import de.freshplan.domain.opportunity.repository.OpportunityRepository;
import de.freshplan.domain.opportunity.service.dto.CreateOpportunityForCustomerRequest;
import de.freshplan.domain.opportunity.service.dto.OpportunityResponse;
import de.freshplan.test.builders.CustomerTestDataFactory;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;

/**
 * Integration Tests für OpportunityService.findByCustomerId()
 *
 * <p>Sprint 2.1.7.3 - Customer → Opportunity Workflow (Bestandskunden)
 *
 * <p>Tests the GET /api/customers/{customerId}/opportunities endpoint backend logic:
 *
 * <ul>
 *   <li>Find all opportunities for a customer (regardless of stage)
 *   <li>Sort by createdAt descending (newest first)
 *   <li>Empty list for customer with no opportunities
 *   <li>Multiple opportunities with different stages
 * </ul>
 *
 * @author FreshPlan Team
 * @since Sprint 2.1.7.3
 */
@QuarkusTest
@Tag("integration")
  @TestSecurity(
    user = "testuser",
    roles = {"admin", "manager", "sales"})
@DisplayName("OpportunityService.findByCustomerId() Integration Tests")
public class OpportunityServiceFindByCustomerIdTest {

  @Inject OpportunityService opportunityService;

  @Inject jakarta.persistence.EntityManager em;

  @Inject OpportunityRepository opportunityRepository;

  @Inject CustomerRepository customerRepository;

  @AfterEach
  @Transactional
  void cleanup() {
    // Delete test data using pattern matching
    em.createNativeQuery("DELETE FROM opportunity_activities WHERE opportunity_id IN (SELECT id FROM opportunities WHERE test_marker LIKE 'TEST-%')").executeUpdate();
    em.createNativeQuery("DELETE FROM opportunities WHERE test_marker LIKE 'TEST-%'").executeUpdate();
    em.createNativeQuery("DELETE FROM customer_contacts WHERE customer_id IN (SELECT id FROM customers WHERE customer_number LIKE 'TEST-%')").executeUpdate();
    em.createNativeQuery("DELETE FROM customer_timeline_events WHERE customer_id IN (SELECT id FROM customers WHERE customer_number LIKE 'TEST-%')").executeUpdate();
    em.createNativeQuery("DELETE FROM customers WHERE customer_number LIKE 'TEST-%'").executeUpdate();
  }


  private Customer testCustomer;
  private Customer otherCustomer;

  @BeforeEach
  @Transactional
  void setUp() {
    // Clean up test data
    opportunityRepository.delete("name LIKE ?1", "[TEST-CUST-OPP]%");
    customerRepository.delete("companyName LIKE ?1", "[TEST-CUST-OPP]%");

    // Create test customers
    testCustomer =
        CustomerTestDataFactory.builder()
            .withCompanyName("[TEST-CUST-OPP] Hotel Vier Jahreszeiten")
            .withCity("München")
            .withPostalCode("80331")
            .withStreet("Maximilianstraße 17")
            .withCountry("Deutschland")
            .withStatus(CustomerStatus.AKTIV)
            .build();
    customerRepository.persist(testCustomer);

    otherCustomer =
        CustomerTestDataFactory.builder()
            .withCompanyName("[TEST-CUST-OPP] Other Customer")
            .withCity("Hamburg")
            .withPostalCode("20095")
            .withStreet("Jungfernstieg 1")
            .withCountry("Deutschland")
            .withStatus(CustomerStatus.AKTIV)
            .build();
    customerRepository.persist(otherCustomer);
  }

  // ==========================================================================
  // Happy Path Tests
  // ==========================================================================

  @Test
  @Transactional
  @DisplayName("Should return empty list for customer with no opportunities")
  void findByCustomerId_noOpportunities_shouldReturnEmptyList() {
    // Act
    List<OpportunityResponse> opportunities =
        opportunityService.findByCustomerId(testCustomer.getId());

    // Assert
    assertThat(opportunities).isEmpty();
  }

  @Test
  @Transactional
  @ActivateRequestContext
  @DisplayName("Should find all opportunities for customer (sorted newest first)")
  void findByCustomerId_multipleOpportunities_shouldReturnSortedDescending()
      throws InterruptedException {
    // Arrange - Create 3 opportunities (with small delay to ensure different timestamps)
    var request1 =
        CreateOpportunityForCustomerRequest.builder()
            .name("[TEST-CUST-OPP] Opportunity 1 (Oldest)")
            .opportunityType("SORTIMENTSERWEITERUNG")
            .expectedValue(BigDecimal.valueOf(10000))
            .expectedCloseDate(LocalDate.now().plusDays(30))
            .build();
    opportunityService.createForCustomer(testCustomer.getId(), request1);

    Thread.sleep(10); // Ensure different createdAt timestamps

    var request2 =
        CreateOpportunityForCustomerRequest.builder()
            .name("[TEST-CUST-OPP] Opportunity 2 (Middle)")
            .opportunityType("NEUER_STANDORT")
            .expectedValue(BigDecimal.valueOf(20000))
            .expectedCloseDate(LocalDate.now().plusDays(60))
            .build();
    opportunityService.createForCustomer(testCustomer.getId(), request2);

    Thread.sleep(10);

    var request3 =
        CreateOpportunityForCustomerRequest.builder()
            .name("[TEST-CUST-OPP] Opportunity 3 (Newest)")
            .opportunityType("VERLAENGERUNG")
            .expectedValue(BigDecimal.valueOf(30000))
            .expectedCloseDate(LocalDate.now().plusDays(90))
            .build();
    opportunityService.createForCustomer(testCustomer.getId(), request3);

    // Act
    List<OpportunityResponse> opportunities =
        opportunityService.findByCustomerId(testCustomer.getId());

    // Assert
    assertThat(opportunities).hasSize(3);

    // Verify sorting: Newest first (descending createdAt)
    assertThat(opportunities.get(0).getName()).contains("Opportunity 3 (Newest)");
    assertThat(opportunities.get(1).getName()).contains("Opportunity 2 (Middle)");
    assertThat(opportunities.get(2).getName()).contains("Opportunity 1 (Oldest)");
  }

  @Test
  @Transactional
  @ActivateRequestContext
  @DisplayName("Should only return opportunities for specified customer (data isolation)")
  void findByCustomerId_multipleCustomers_shouldReturnOnlyForSpecifiedCustomer() {
    // Arrange - Create opportunity for testCustomer
    var ownRequest =
        CreateOpportunityForCustomerRequest.builder()
            .name("[TEST-CUST-OPP] Own Opportunity")
            .opportunityType("SORTIMENTSERWEITERUNG")
            .expectedValue(BigDecimal.valueOf(10000))
            .expectedCloseDate(LocalDate.now().plusDays(30))
            .build();
    opportunityService.createForCustomer(testCustomer.getId(), ownRequest);

    // Create opportunity for otherCustomer
    var otherRequest =
        CreateOpportunityForCustomerRequest.builder()
            .name("[TEST-CUST-OPP] Other Opportunity")
            .opportunityType("NEUGESCHAEFT")
            .expectedValue(BigDecimal.valueOf(20000))
            .expectedCloseDate(LocalDate.now().plusDays(60))
            .build();
    opportunityService.createForCustomer(otherCustomer.getId(), otherRequest);

    // Act
    List<OpportunityResponse> opportunities =
        opportunityService.findByCustomerId(testCustomer.getId());

    // Assert
    assertThat(opportunities).hasSize(1);
    assertThat(opportunities.get(0).getName()).contains("Own Opportunity");
    assertThat(opportunities.get(0).getName()).doesNotContain("Other Opportunity");
    assertThat(opportunities.get(0).getCustomerId()).isEqualTo(testCustomer.getId());
  }

  @Test
  @Transactional
  @ActivateRequestContext
  @DisplayName("Should return opportunities regardless of stage (all categories)")
  void findByCustomerId_differentStages_shouldReturnAll() {
    // Arrange - Create opportunities, then manually change stages via service
    var request1 =
        CreateOpportunityForCustomerRequest.builder()
            .name("[TEST-CUST-OPP] Open Opportunity")
            .opportunityType("SORTIMENTSERWEITERUNG")
            .expectedValue(BigDecimal.valueOf(10000))
            .expectedCloseDate(LocalDate.now().plusDays(30))
            .build();
    var opp1 = opportunityService.createForCustomer(testCustomer.getId(), request1);

    var request2 =
        CreateOpportunityForCustomerRequest.builder()
            .name("[TEST-CUST-OPP] Won Opportunity")
            .opportunityType("NEUER_STANDORT")
            .expectedValue(BigDecimal.valueOf(20000))
            .expectedCloseDate(LocalDate.now().plusDays(60))
            .build();
    var opp2 = opportunityService.createForCustomer(testCustomer.getId(), request2);

    var request3 =
        CreateOpportunityForCustomerRequest.builder()
            .name("[TEST-CUST-OPP] Lost Opportunity")
            .opportunityType("VERLAENGERUNG")
            .expectedValue(BigDecimal.valueOf(15000))
            .expectedCloseDate(LocalDate.now().plusDays(90))
            .build();
    var opp3 = opportunityService.createForCustomer(testCustomer.getId(), request3);

    // Change stages (realistic transitions)
    // NEEDS_ANALYSIS → PROPOSAL → NEGOTIATION → CLOSED_WON
    opportunityService.changeStage(opp2.getId(), OpportunityStage.PROPOSAL, "Submitted proposal");
    opportunityService.changeStage(opp2.getId(), OpportunityStage.NEGOTIATION, "Negotiating terms");
    opportunityService.changeStage(opp2.getId(), OpportunityStage.CLOSED_WON, "Deal won");

    // NEEDS_ANALYSIS → CLOSED_LOST (direct loss allowed)
    opportunityService.changeStage(opp3.getId(), OpportunityStage.CLOSED_LOST, "Deal lost");

    // Act
    List<OpportunityResponse> opportunities =
        opportunityService.findByCustomerId(testCustomer.getId());

    // Assert
    assertThat(opportunities).hasSize(3);

    // Verify all stages are returned (Offen/Gewonnen/Verloren)
    assertThat(opportunities)
        .extracting(OpportunityResponse::getStage)
        .containsExactlyInAnyOrder(
            OpportunityStage.NEEDS_ANALYSIS, // Open (default for customer opportunities)
            OpportunityStage.CLOSED_WON, // Won
            OpportunityStage.CLOSED_LOST); // Lost
  }
}
