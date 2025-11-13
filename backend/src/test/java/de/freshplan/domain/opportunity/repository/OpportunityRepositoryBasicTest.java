package de.freshplan.domain.opportunity.repository;

import static org.assertj.core.api.Assertions.*;

import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.opportunity.entity.Opportunity;
import de.freshplan.domain.opportunity.entity.OpportunityStage;
import de.freshplan.domain.user.entity.User;
import de.freshplan.domain.user.repository.UserRepository;
import de.freshplan.test.builders.CustomerTestDataFactory;
import de.freshplan.test.builders.OpportunityTestDataFactory;
import de.freshplan.test.builders.UserTestDataFactory;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Basic Repository Operations Tests für OpportunityRepository
 *
 * <p>Tests für grundlegende Repository-Operationen: - Find by Stage - Find by Customer - Count
 * Operations - Delete Operations
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@QuarkusTest
@Tag("integration")
@TestSecurity(
    user = "testuser",
    roles = {"admin", "manager", "sales"})
@DisplayName("Opportunity Repository - Basic Operations")
public class OpportunityRepositoryBasicTest {

  @Inject OpportunityRepository opportunityRepository;
  @Inject CustomerRepository customerRepository;
  @Inject UserRepository userRepository;
  @Inject jakarta.persistence.EntityManager em;

  @AfterEach
  @Transactional
  void cleanup() {
    // Delete in correct order
    em.createNativeQuery(
            "DELETE FROM opportunity_activities WHERE opportunity_id IN (SELECT id FROM opportunities WHERE test_marker LIKE 'TEST-%')")
        .executeUpdate();
    em.createNativeQuery("DELETE FROM opportunities WHERE test_marker LIKE 'TEST-%'")
        .executeUpdate();
  }

  @Test
  @TestTransaction
  @DisplayName("Should find opportunities by stage")
  void findByStage_shouldReturnCorrectOpportunities() {
    // Given
    Customer testCustomer = createTestCustomer("Basic Test Company");
    User testUser = getOrCreateTestUser("testuser_" + UUID.randomUUID().toString().substring(0, 8));

    Opportunity newLead1 =
        createTestOpportunity("New Lead 1", OpportunityStage.NEW_LEAD, testUser, testCustomer);
    Opportunity newLead2 =
        createTestOpportunity("New Lead 2", OpportunityStage.NEW_LEAD, testUser, testCustomer);
    Opportunity qualification =
        createTestOpportunity(
            "Qualification 1", OpportunityStage.QUALIFICATION, testUser, testCustomer);

    // When
    var newLeadOpportunities = opportunityRepository.findByStage(OpportunityStage.NEW_LEAD);
    var qualificationOpportunities =
        opportunityRepository.findByStage(OpportunityStage.QUALIFICATION);

    // Then - Use relative assertions
    assertThat(newLeadOpportunities)
        .extracting(Opportunity::getName)
        .anySatisfy(name -> assertThat(name).contains("New Lead 1"))
        .anySatisfy(name -> assertThat(name).contains("New Lead 2"));

    assertThat(qualificationOpportunities)
        .extracting(Opportunity::getName)
        .anySatisfy(name -> assertThat(name).contains("Qualification 1"));
  }

  @Test
  @TestTransaction
  @DisplayName("Should find opportunities by customer")
  void findByCustomer_shouldReturnOnlyCustomerOpportunities() {
    // Given
    Customer customer1 = createTestCustomer("Customer One");
    Customer customer2 = createTestCustomer("Customer Two");
    User testUser = getOrCreateTestUser("testuser_" + UUID.randomUUID().toString().substring(0, 8));

    Opportunity opp1 =
        createTestOpportunity("Opp for Customer 1", OpportunityStage.NEW_LEAD, testUser, customer1);
    Opportunity opp2 =
        createTestOpportunity(
            "Another for Customer 1", OpportunityStage.PROPOSAL, testUser, customer1);
    Opportunity opp3 =
        createTestOpportunity("Opp for Customer 2", OpportunityStage.NEW_LEAD, testUser, customer2);

    // When
    var customer1Opportunities = opportunityRepository.findByCustomer(customer1);
    var customer2Opportunities = opportunityRepository.findByCustomer(customer2);

    // Then
    assertThat(customer1Opportunities)
        .hasSize(2)
        .extracting(Opportunity::getName)
        .anySatisfy(name -> assertThat(name).contains("Opp for Customer 1"))
        .anySatisfy(name -> assertThat(name).contains("Another for Customer 1"));

    assertThat(customer2Opportunities)
        .hasSize(1)
        .extracting(Opportunity::getName)
        .anySatisfy(name -> assertThat(name).contains("Opp for Customer 2"));
  }

  @Test
  @TestTransaction
  @DisplayName("Should count opportunities by stage")
  void countByStage_shouldReturnCorrectCounts() {
    // Given - Record initial counts
    long initialNewLeadCount = opportunityRepository.count("stage", OpportunityStage.NEW_LEAD);
    long initialQualificationCount =
        opportunityRepository.count("stage", OpportunityStage.QUALIFICATION);
    long initialProposalCount = opportunityRepository.count("stage", OpportunityStage.PROPOSAL);
    long initialClosedWonCount = opportunityRepository.count("stage", OpportunityStage.CLOSED_WON);

    Customer testCustomer = createTestCustomer("Count Test Company");
    User testUser = getOrCreateTestUser("testuser_" + UUID.randomUUID().toString().substring(0, 8));

    // Create 3 NEW_LEAD, 2 QUALIFICATION, 1 PROPOSAL
    createTestOpportunity("Lead 1", OpportunityStage.NEW_LEAD, testUser, testCustomer);
    createTestOpportunity("Lead 2", OpportunityStage.NEW_LEAD, testUser, testCustomer);
    createTestOpportunity("Lead 3", OpportunityStage.NEW_LEAD, testUser, testCustomer);
    createTestOpportunity("Qual 1", OpportunityStage.QUALIFICATION, testUser, testCustomer);
    createTestOpportunity("Qual 2", OpportunityStage.QUALIFICATION, testUser, testCustomer);
    createTestOpportunity("Proposal 1", OpportunityStage.PROPOSAL, testUser, testCustomer);

    // When
    long newLeadCount = opportunityRepository.count("stage", OpportunityStage.NEW_LEAD);
    long qualificationCount = opportunityRepository.count("stage", OpportunityStage.QUALIFICATION);
    long proposalCount = opportunityRepository.count("stage", OpportunityStage.PROPOSAL);
    long closedWonCount = opportunityRepository.count("stage", OpportunityStage.CLOSED_WON);

    // Then - Use relative assertions
    assertThat(newLeadCount - initialNewLeadCount).isEqualTo(3);
    assertThat(qualificationCount - initialQualificationCount).isEqualTo(2);
    assertThat(proposalCount - initialProposalCount).isEqualTo(1);
    assertThat(closedWonCount - initialClosedWonCount).isEqualTo(0);
  }

  @Test
  @TestTransaction
  @DisplayName("Should find active opportunities (not closed)")
  void findActiveOpportunities_shouldExcludeClosedOpportunities() {
    // Given
    Customer testCustomer = createTestCustomer("Active Test Company");
    User testUser = getOrCreateTestUser("testuser_" + UUID.randomUUID().toString().substring(0, 8));

    Opportunity active1 =
        createTestOpportunity("Active 1", OpportunityStage.NEW_LEAD, testUser, testCustomer);
    Opportunity active2 =
        createTestOpportunity("Active 2", OpportunityStage.PROPOSAL, testUser, testCustomer);
    Opportunity closedWon =
        createTestOpportunity("Closed Won", OpportunityStage.CLOSED_WON, testUser, testCustomer);
    Opportunity closedLost =
        createTestOpportunity("Closed Lost", OpportunityStage.CLOSED_LOST, testUser, testCustomer);

    // When
    var activeOpportunities =
        opportunityRepository.findAllActive(io.quarkus.panache.common.Page.of(0, 100));

    // Then - Use relative assertions
    assertThat(activeOpportunities)
        .extracting(Opportunity::getName)
        .anySatisfy(name -> assertThat(name).contains("Active 1"))
        .anySatisfy(name -> assertThat(name).contains("Active 2"));
    assertThat(activeOpportunities)
        .extracting(Opportunity::getName)
        .noneSatisfy(name -> assertThat(name).contains("Closed Won"))
        .noneSatisfy(name -> assertThat(name).contains("Closed Lost"));
  }

  @Test
  @TestTransaction
  @DisplayName("Should delete opportunities by customer")
  void deleteByCustomer_shouldRemoveAllCustomerOpportunities() {
    // Given
    Customer customer1 = createTestCustomer("Delete Test Customer 1");
    Customer customer2 = createTestCustomer("Delete Test Customer 2");
    User testUser = getOrCreateTestUser("testuser_" + UUID.randomUUID().toString().substring(0, 8));

    createTestOpportunity("Opp 1 for Customer 1", OpportunityStage.NEW_LEAD, testUser, customer1);
    createTestOpportunity("Opp 2 for Customer 1", OpportunityStage.PROPOSAL, testUser, customer1);
    createTestOpportunity("Opp for Customer 2", OpportunityStage.NEW_LEAD, testUser, customer2);

    // Verify initial state
    assertThat(opportunityRepository.findByCustomer(customer1)).hasSize(2);
    assertThat(opportunityRepository.findByCustomer(customer2)).hasSize(1);

    // When
    opportunityRepository.delete("customer", customer1);
    opportunityRepository.flush();

    // Then
    assertThat(opportunityRepository.findByCustomer(customer1)).isEmpty();
    assertThat(opportunityRepository.findByCustomer(customer2)).hasSize(1);
  }

  // ========== Helper Methods ==========

  private Customer createTestCustomer(String companyName) {
    Customer customer =
        CustomerTestDataFactory.builder().withCompanyName("[TEST] " + companyName).build();

    // Ensure unique customer number
    customer.setCustomerNumber(de.freshplan.TestIds.uniqueCustomerNumber());
    customer.setIsTestData(true);

    customerRepository.persist(customer);
    return customer;
  }

  private User getOrCreateTestUser(String username) {
    return userRepository
        .findByUsername(username)
        .orElseGet(
            () -> {
              User user =
                  UserTestDataFactory.builder()
                      .withUsername(username)
                      .withFirstName("Test")
                      .withLastName("User")
                      .withEmail(username + "@test.com")
                      .withRoles(Arrays.asList("admin", "manager", "sales"))
                      .build();
              userRepository.persist(user);
              return user;
            });
  }

  private Opportunity createTestOpportunity(
      String name, OpportunityStage stage, User assignedTo, Customer customer) {
    Opportunity opportunity =
        OpportunityTestDataFactory.builder()
            .withName(name)
            .inStage(stage)
            .assignedTo(assignedTo)
            .forCustomer(customer)
            .withExpectedValue(new BigDecimal("10000.00"))
            .withProbability(50)
            .withExpectedCloseDate(LocalDate.now().plusDays(30))
            .build();
    opportunityRepository.persist(opportunity);
    return opportunity;
  }
}
