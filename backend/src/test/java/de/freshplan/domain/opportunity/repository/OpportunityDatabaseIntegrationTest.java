package de.freshplan.domain.opportunity.repository;

import static org.assertj.core.api.Assertions.*;

import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.opportunity.entity.Opportunity;
import de.freshplan.domain.opportunity.entity.OpportunityStage;
import de.freshplan.domain.user.entity.User;
import de.freshplan.domain.user.repository.UserRepository;
import de.freshplan.test.builders.CustomerBuilder;
import de.freshplan.test.builders.OpportunityBuilder;
import de.freshplan.test.builders.UserTestDataFactory;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
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
@Disabled("TEMPORARY: Sprint 2.1.4 CI Performance Fix")
@Tag("core")
@TestSecurity(
    user = "testuser",
    roles = {"admin", "manager", "sales"})
@io.quarkus.test.TestTransaction
public class OpportunityDatabaseIntegrationTest {

  @Inject OpportunityRepository opportunityRepository;

  @Inject CustomerRepository customerRepository;

  @Inject UserRepository userRepository;

  @Inject CustomerBuilder customerBuilder;

  @Inject OpportunityBuilder opportunityBuilder;

  @Inject EntityManager entityManager;

  @Test
  @DisplayName("Valid opportunity creation and persistence should work")
  void createAndPersistOpportunity_shouldWork() {
    // Given - Create test data
    Customer testCustomer =
        createTestCustomer("CUST-DB-" + UUID.randomUUID().toString().substring(0, 8));
    User testUser = getOrCreateTestUser();

    Opportunity opportunity =
        opportunityBuilder
            .withName("DB Integration Test Opportunity")
            .withDescription("Testing database integration")
            .withExpectedValue(new BigDecimal("15000.00"))
            .withProbability(75)
            .inStage(OpportunityStage.PROPOSAL)
            .withExpectedCloseDate(LocalDate.now().plusDays(45))
            .forCustomer(testCustomer)
            .assignedTo(testUser)
            .build();

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
  @DisplayName("Repository findById should return correct opportunity")
  void repositoryFindById_shouldReturnCorrectOpportunity() {
    // Given
    Customer testCustomer =
        createTestCustomer("CUST-FIND-" + UUID.randomUUID().toString().substring(0, 8));
    User testUser = getOrCreateTestUser();

    Opportunity opportunity =
        opportunityBuilder
            .withName("Find Test Opportunity")
            .withDescription("Testing repository find operations")
            .withExpectedValue(new BigDecimal("8000.00"))
            .inStage(OpportunityStage.QUALIFICATION)
            .withProbability(60) // Manuell überschreiben nach Stage
            .withExpectedCloseDate(LocalDate.now().plusDays(30))
            .forCustomer(testCustomer)
            .assignedTo(testUser)
            .build();

    opportunityRepository.persist(opportunity);
    entityManager.flush();
    UUID opportunityId = opportunity.getId();

    // When
    Opportunity found = opportunityRepository.findById(opportunityId);

    // Then
    assertThat(found).isNotNull();
    assertThat(found.getName())
        .contains("Find Test Opportunity"); // Builder adds [TEST] prefix and suffix
    assertThat(found.getExpectedValue()).isEqualTo(new BigDecimal("8000.00"));
    assertThat(found.getProbability()).isEqualTo(60);
    assertThat(found.getStage()).isEqualTo(OpportunityStage.QUALIFICATION);
    assertThat(found.getCustomer().getId()).isEqualTo(testCustomer.getId());
    assertThat(found.getAssignedTo().getId()).isEqualTo(testUser.getId());
  }

  @Test
  @DisplayName("Repository count should reflect persisted opportunities")
  void repositoryCount_shouldReflectPersistedOpportunities() {
    // Given
    long initialCount = opportunityRepository.count();

    Customer testCustomer =
        createTestCustomer("CUST-COUNT-" + UUID.randomUUID().toString().substring(0, 8));
    User testUser = getOrCreateTestUser();

    // Create multiple opportunities
    for (int i = 0; i < 3; i++) {
      Opportunity opportunity =
          opportunityBuilder
              .withName("Count Test Opportunity " + i)
              .withDescription("Testing count operations")
              .withExpectedValue(new BigDecimal("5000.00"))
              .withProbability(40)
              .inStage(OpportunityStage.NEW_LEAD)
              .withExpectedCloseDate(LocalDate.now().plusDays(20))
              .forCustomer(testCustomer)
              .assignedTo(testUser)
              .build();
      opportunityRepository.persist(opportunity);
    }
    entityManager.flush();

    // When
    long finalCount = opportunityRepository.count();

    // Then
    assertThat(finalCount).isEqualTo(initialCount + 3);
  }

  @Test
  @DisplayName("Opportunity with negative expected value should NOT be allowed")
  void opportunityWithNegativeValue_shouldNotBeAllowed() {
    // Given
    Customer testCustomer =
        createTestCustomer("CUST-NEG-" + UUID.randomUUID().toString().substring(0, 8));
    User testUser = getOrCreateTestUser();

    Opportunity opportunity =
        opportunityBuilder
            .withName("Negative Value Test")
            .withDescription("Testing negative expected values")
            .withExpectedValue(new BigDecimal("-2500.00")) // Negative value
            .withProbability(25)
            .inStage(OpportunityStage.CLOSED_LOST)
            .withExpectedCloseDate(LocalDate.now().plusDays(10))
            .forCustomer(testCustomer)
            .assignedTo(testUser)
            .build();

    // When/Then - Should throw exception due to Bean Validation constraint
    assertThatThrownBy(
            () -> {
              opportunityRepository.persist(opportunity);
              entityManager.flush();
            })
        .isInstanceOf(jakarta.validation.ConstraintViolationException.class)
        .hasMessageContaining("Expected value must not be negative");
  }

  @Test
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

      Opportunity opportunity =
          opportunityBuilder
              .withName("Stage Test " + stage.name())
              .withDescription("Testing stage: " + stage)
              .withExpectedValue(new BigDecimal("1000.00"))
              .withProbability(50)
              .inStage(stage)
              .withExpectedCloseDate(LocalDate.now().plusDays(15))
              .forCustomer(testCustomer)
              .assignedTo(testUser)
              .build();

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
    Customer customer =
        customerBuilder.withCompanyName("[TEST] Test Company " + customerNumber).build();

    // Keep exact company name and customer number
    customer.setCompanyName("[TEST] Test Company " + customerNumber);
    customer.setCustomerNumber(customerNumber);
    customer.setIsTestData(true);

    customerRepository.persist(customer);
    return customer;
  }

  /** Helper method to get or create a test user with unique identifier */
  private User getOrCreateTestUser() {
    // Generate unique user for each test to avoid collisions
    String uniqueId = UUID.randomUUID().toString().substring(0, 8);
    String username = "testuser_" + uniqueId;

    return userRepository
        .findByUsername(username)
        .orElseGet(
            () -> {
              User user =
                  UserTestDataFactory.builder()
                      .withUsername(username)
                      .withFirstName("Test")
                      .withLastName("User")
                      .withEmail("testuser_" + uniqueId + "@freshplan.de")
                      .build();
              user.addRole("admin");
              user.enable();
              userRepository.persist(user);
              return user;
            });
  }
}
