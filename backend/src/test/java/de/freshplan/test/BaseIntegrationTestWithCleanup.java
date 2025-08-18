package de.freshplan.test;

import de.freshplan.domain.customer.repository.*;
import de.freshplan.domain.opportunity.repository.OpportunityRepository;
import de.freshplan.domain.user.repository.UserRepository;
import de.freshplan.test.builders.UserTestDataFactory;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

/**
 * Base class for integration tests with proper setup and FK-safe teardown.
 *
 * <p>Implements the team's recommendations: - Explicit test data setup per test - FK-safe cleanup
 * in correct order - Dynamic IDs for unique fields - DB constraint validation
 *
 * <p>Usage: Extend this class and use the helper methods for test data creation.
 */
public abstract class BaseIntegrationTestWithCleanup {

  private static final Logger LOG = Logger.getLogger(BaseIntegrationTestWithCleanup.class);

  @Inject protected EntityManager entityManager;
  @Inject protected CustomerRepository customerRepository;
  @Inject protected ContactRepository contactRepository;
  @Inject protected CustomerTimelineRepository timelineRepository;
  @Inject protected OpportunityRepository opportunityRepository;
  @Inject protected UserRepository userRepository;

  /** Setup method - runs before each test. Override in subclasses to add specific setup logic. */
  @BeforeEach
  protected void baseSetUp() {
    LOG.debug("Starting test setup - ensuring clean state");
    // Subclasses can override to add specific setup
  }

  /** Cleanup method - runs after each test. Performs FK-safe cleanup in correct order. */
  @AfterEach
  @Transactional
  protected void baseTearDown() {
    LOG.debug("Starting FK-safe test cleanup");

    try {
      // 1. Delete dependent data first (FK-safe order)
      cleanupTimelineEvents();
      cleanupContacts();
      cleanupOpportunities();

      // 2. Delete parent data
      cleanupCustomers();

      // 3. Flush to ensure all deletes are executed
      entityManager.flush();

      LOG.debug("Test cleanup completed successfully");
    } catch (Exception e) {
      LOG.error("Error during test cleanup: " + e.getMessage(), e);
      // Don't fail the test due to cleanup issues
    }
  }

  /** Cleanup timeline events (no FK dependencies). */
  private void cleanupTimelineEvents() {
    long deleted = timelineRepository.delete("isTestData = true OR customer.isTestData = true");
    if (deleted > 0) {
      LOG.debug("Deleted " + deleted + " test timeline events");
    }
  }

  /** Cleanup contacts (depends on customers). */
  private void cleanupContacts() {
    // Delete test contacts or contacts of test customers
    long deleted =
        contactRepository.delete(
            "isDeleted = true OR customer.isTestData = true OR email LIKE '%@test.%'");
    if (deleted > 0) {
      LOG.debug("Deleted " + deleted + " test contacts");
    }
  }

  /** Cleanup opportunities (depends on customers). */
  private void cleanupOpportunities() {
    long deleted =
        opportunityRepository.delete(
            "customer.isTestData = true OR name LIKE 'TEST%' OR name LIKE '%Test%'");
    if (deleted > 0) {
      LOG.debug("Deleted " + deleted + " test opportunities");
    }
  }

  /** Cleanup customers (parent entity). */
  private void cleanupCustomers() {
    // Delete test customers
    long deleted =
        customerRepository.delete(
            "isTestData = true OR customerNumber LIKE 'KD-TEST-%' OR companyName LIKE '%Test%'");
    if (deleted > 0) {
      LOG.debug("Deleted " + deleted + " test customers");
    }
  }

  /**
   * Helper to create a test customer with all required fields. Uses TestDataBuilder for dynamic
   * unique values.
   */
  protected de.freshplan.domain.customer.entity.Customer createTestCustomer(String name) {
    var customer = TestDataBuilder.createTestCustomer(name);
    customer.setIsTestData(true); // Mark as test data for cleanup
    customerRepository.persist(customer);
    return customer;
  }

  /** Helper to create a test user if not exists. */
  protected de.freshplan.domain.user.entity.User getOrCreateTestUser(String username) {
    return userRepository
        .findByUsername(username)
        .orElseGet(
            () -> {
              var user =
                  UserTestDataFactory.builder()
                      .withUsername(username)
                      .withFirstName("Test")
                      .withLastName("User")
                      .withEmail(username + "@test.de")
                      .build();
              user.addRole("admin");
              user.enable();
              userRepository.persist(user);
              return user;
            });
  }

  /** Flush and clear entity manager to ensure clean state. */
  protected void flushAndClear() {
    entityManager.flush();
    entityManager.clear();
  }
}
