package de.freshplan.domain.opportunity.repository;

import static org.assertj.core.api.Assertions.*;

import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.opportunity.entity.Opportunity;
import de.freshplan.domain.opportunity.entity.OpportunityStage;
import de.freshplan.domain.user.entity.User;
import de.freshplan.domain.user.repository.UserRepository;
import de.freshplan.test.builders.CustomerBuilder;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Database Integration Tests für Opportunity Entity
 *
 * <p>Fokus auf grundlegende Database-Integration: - Opportunity Creation/Persistence - Audit Fields
 * Behavior - Basic Constraints - Repository Operations
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@QuarkusTest
@TestSecurity(
    user = "testuser",
    roles = {"admin", "manager", "sales"})
public class OpportunityDatabaseIntegrationTest {

  @Inject OpportunityRepository opportunityRepository;

  @Inject CustomerRepository customerRepository;

  @Inject UserRepository userRepository;
  
  @Inject CustomerBuilder customerBuilder;

  @Inject EntityManager entityManager;

  @Test
  @Transactional
  @DisplayName("Valid opportunity creation and persistence should work")
  void createAndPersistOpportunity_shouldWork() {
    // Given - Create test data
    Customer testCustomer =
        createTestCustomer("CUST-DB-" + UUID.randomUUID().toString().substring(0, 8));
    User testUser = getOrCreateTestUser();

    Opportunity opportunity = new Opportunity();
    opportunity.setName("DB Integration Test Opportunity");
    opportunity.setDescription("Testing database integration");
    opportunity.setExpectedValue(new BigDecimal("15000.00"));
    opportunity.setProbability(75);
    opportunity.setStage(OpportunityStage.PROPOSAL);
    opportunity.setExpectedCloseDate(LocalDate.now().plusDays(45));
    opportunity.setCustomer(testCustomer);
    opportunity.setAssignedTo(testUser);

    // When
    opportunityRepository.persist(opportunity);
    entityManager.flush();

    // Then - Verify persistence and audit fields
    assertThat(opportunity.getId()).isNotNull();
    assertThat(opportunity.getCreatedAt()).isNotNull();
    assertThat(opportunity.getUpdatedAt()).isNotNull();
    assertThat(opportunity.getStageChangedAt()).isNotNull();

    // Verify relationships are maintained
    assertThat(opportunity.getCustomer().getId()).isEqualTo(testCustomer.getId());
    assertThat(opportunity.getAssignedTo().getId()).isEqualTo(testUser.getId());
  }

  @Test
  @Transactional
  @DisplayName("Repository findById should return correct opportunity")
  void repositoryFindById_shouldReturnCorrectOpportunity() {
    // Given
    Customer testCustomer =
        createTestCustomer("CUST-FIND-" + UUID.randomUUID().toString().substring(0, 8));
    User testUser = getOrCreateTestUser();

    Opportunity opportunity = new Opportunity();
    opportunity.setName("Find Test Opportunity");
    opportunity.setDescription("Testing repository find operations");
    opportunity.setExpectedValue(new BigDecimal("8000.00"));
    opportunity.setStage(OpportunityStage.QUALIFICATION); // Dies setzt probability auf 25
    opportunity.setProbability(60); // Manuell überschreiben nach Stage
    opportunity.setExpectedCloseDate(LocalDate.now().plusDays(30));
    opportunity.setCustomer(testCustomer);
    opportunity.setAssignedTo(testUser);

    opportunityRepository.persist(opportunity);
    entityManager.flush();
    UUID opportunityId = opportunity.getId();

    // When
    Opportunity found = opportunityRepository.findById(opportunityId);

    // Then
    assertThat(found).isNotNull();
    assertThat(found.getName()).isEqualTo("Find Test Opportunity");
    assertThat(found.getExpectedValue()).isEqualTo(new BigDecimal("8000.00"));
    assertThat(found.getProbability()).isEqualTo(60);
    assertThat(found.getStage()).isEqualTo(OpportunityStage.QUALIFICATION);
    assertThat(found.getCustomer().getId()).isEqualTo(testCustomer.getId());
    assertThat(found.getAssignedTo().getId()).isEqualTo(testUser.getId());
  }

  @Test
  @Transactional
  @DisplayName("Repository count should reflect persisted opportunities")
  void repositoryCount_shouldReflectPersistedOpportunities() {
    // Given
    long initialCount = opportunityRepository.count();

    Customer testCustomer =
        createTestCustomer("CUST-COUNT-" + UUID.randomUUID().toString().substring(0, 8));
    User testUser = getOrCreateTestUser();

    // Create multiple opportunities
    for (int i = 0; i < 3; i++) {
      Opportunity opportunity = new Opportunity();
      opportunity.setName("Count Test Opportunity " + i);
      opportunity.setDescription("Testing count operations");
      opportunity.setExpectedValue(new BigDecimal("5000.00"));
      opportunity.setProbability(40);
      opportunity.setStage(OpportunityStage.NEW_LEAD);
      opportunity.setExpectedCloseDate(LocalDate.now().plusDays(20));
      opportunity.setCustomer(testCustomer);
      opportunity.setAssignedTo(testUser);
      opportunityRepository.persist(opportunity);
    }
    entityManager.flush();

    // When
    long finalCount = opportunityRepository.count();

    // Then
    assertThat(finalCount).isEqualTo(initialCount + 3);
  }

  @Test
  @Transactional
  @DisplayName("Opportunity with negative expected value should NOT be allowed")
  void opportunityWithNegativeValue_shouldNotBeAllowed() {
    // Given
    Customer testCustomer =
        createTestCustomer("CUST-NEG-" + UUID.randomUUID().toString().substring(0, 8));
    User testUser = getOrCreateTestUser();

    Opportunity opportunity = new Opportunity();
    opportunity.setName("Negative Value Test");
    opportunity.setDescription("Testing negative expected values");
    opportunity.setExpectedValue(new BigDecimal("-2500.00")); // Negative value
    opportunity.setProbability(25);
    opportunity.setStage(OpportunityStage.CLOSED_LOST);
    opportunity.setExpectedCloseDate(LocalDate.now().plusDays(10));
    opportunity.setCustomer(testCustomer);
    opportunity.setAssignedTo(testUser);

    // When/Then - Should throw exception due to check constraint
    assertThatThrownBy(
            () -> {
              opportunityRepository.persist(opportunity);
              entityManager.flush();
            })
        .isInstanceOf(jakarta.persistence.PersistenceException.class)
        .hasMessageContaining("expected_value");
  }

  @Test
  @Transactional
  @DisplayName("All opportunity stages should be persistable")
  void allOpportunityStages_shouldBePersistable() {
    // Given
    Customer testCustomer =
        createTestCustomer("CUST-STAGES-" + UUID.randomUUID().toString().substring(0, 8));
    User testUser = getOrCreateTestUser();

    OpportunityStage[] allStages = OpportunityStage.values();

    // When/Then - Test each stage
    for (int i = 0; i < allStages.length; i++) {
      OpportunityStage stage = allStages[i];

      Opportunity opportunity = new Opportunity();
      opportunity.setName("Stage Test " + stage.name());
      opportunity.setDescription("Testing stage: " + stage);
      opportunity.setExpectedValue(new BigDecimal("1000.00"));
      opportunity.setProbability(50);
      opportunity.setStage(stage);
      opportunity.setExpectedCloseDate(LocalDate.now().plusDays(15));
      opportunity.setCustomer(testCustomer);
      opportunity.setAssignedTo(testUser);

      // Should not throw exception
      assertThatCode(
              () -> {
                opportunityRepository.persist(opportunity);
                entityManager.flush();
              })
          .doesNotThrowAnyException();

      // Verify stage is persisted correctly
      assertThat(opportunity.getStage()).isEqualTo(stage);
    }
  }

  /** Helper method to create a test customer with unique customer number */
  private Customer createTestCustomer(String customerNumber) {
    Customer customer = customerBuilder
        .withCompanyName("[TEST] Test Company " + customerNumber)
        .build();
    
    // Keep exact company name and customer number
    customer.setCompanyName("[TEST] Test Company " + customerNumber);
    customer.setCustomerNumber(customerNumber);
    customer.setIsTestData(true);
    
    customerRepository.persist(customer);
    return customer;
  }

  /** Helper method to get or create a test user */
  private User getOrCreateTestUser() {
    return userRepository
        .findByUsername("testuser")
        .orElseGet(
            () -> {
              User user = new User("testuser", "Test", "User", "testuser@freshplan.de");
              user.addRole("admin");
              user.enable();
              userRepository.persist(user);
              return user;
            });
  }
}
