package de.freshplan.domain.customer.performance;

import static org.assertj.core.api.Assertions.assertThat;

import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.entity.CustomerContact;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.test.builders.ContactTestDataFactory;
import de.freshplan.test.builders.CustomerBuilder;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

/**
 * Performance tests for Contact-related operations. Tests bulk operations, query performance, and
 * concurrent access patterns.
 *
 * <p>Run with: mvn test -Dtest=ContactPerformanceTest -Dperformance.tests.enabled=true
 */
@QuarkusTest
@DisplayName("Contact Performance Tests")
@EnabledIfSystemProperty(named = "performance.tests.enabled", matches = "true")
public class ContactPerformanceTest {

  @Inject EntityManager entityManager;

  @Inject CustomerRepository customerRepository;

  @Inject CustomerBuilder customerBuilder;

  private Customer testCustomer;

  @BeforeEach
  @TestTransaction
  void setUp() {
    // Create test customer using CustomerBuilder
    testCustomer = customerBuilder.withCompanyName("Performance Test Company").build();
    // Override auto-generated values for performance test
    testCustomer.setCustomerNumber("PERF-" + UUID.randomUUID().toString().substring(0, 8));
    testCustomer.setCompanyName("Performance Test Company"); // Remove [TEST-xxx] prefix
    customerRepository.persist(testCustomer);
  }

  @Test
  @DisplayName("Should handle bulk insert of 1000 contacts efficiently")
  @TestTransaction
  void testBulkInsertPerformance() {
    // Arrange
    int contactCount = 1000;
    List<CustomerContact> contacts = new ArrayList<>();

    // Act & Measure
    Instant start = Instant.now();

    for (int i = 0; i < contactCount; i++) {
      CustomerContact contact = createTestContact(i);
      contacts.add(contact);

      // Batch insert every 50 contacts
      if (i % 50 == 0) {
        for (CustomerContact c : contacts) {
          entityManager.persist(c);
        }
        entityManager.flush();
        contacts.clear();
      }
    }

    // Persist remaining
    for (CustomerContact c : contacts) {
      entityManager.persist(c);
    }
    entityManager.flush();

    Duration duration = Duration.between(start, Instant.now());

    // Assert
    assertThat(duration.toMillis()).isLessThan(5000); // Should complete within 5 seconds
    Long count =
        entityManager
            .createQuery("SELECT COUNT(c) FROM CustomerContact c", Long.class)
            .getSingleResult();
    assertThat(count).isGreaterThanOrEqualTo(contactCount);

    System.out.println(
        String.format(
            "Bulk insert of %d contacts completed in %d ms", contactCount, duration.toMillis()));
  }

  @Test
  @DisplayName("Should query contacts with roles efficiently")
  @TestTransaction
  void testRoleQueryPerformance() {
    // Arrange - Create contacts with various roles
    createContactsWithRoles(100);

    // Act & Measure
    Instant start = Instant.now();

    List<CustomerContact> decisionMakers =
        entityManager
            .createQuery(
                "SELECT c FROM CustomerContact c WHERE c.isDecisionMaker = true",
                CustomerContact.class)
            .getResultList();

    Duration duration = Duration.between(start, Instant.now());

    // Assert
    assertThat(duration.toMillis()).isLessThan(100); // Query should complete within 100ms
    assertThat(decisionMakers).isNotEmpty();

    System.out.println(
        String.format(
            "Query for decision makers found %d contacts in %d ms",
            decisionMakers.size(), duration.toMillis()));
  }

  @Test
  @DisplayName("Should handle concurrent reads efficiently")
  void testConcurrentReadPerformance() throws Exception {
    // Arrange
    createContactsWithRoles(50);
    int threadCount = 10;
    int operationsPerThread = 100;
    ExecutorService executor = Executors.newFixedThreadPool(threadCount);

    // Act & Measure
    Instant start = Instant.now();
    List<CompletableFuture<Void>> futures = new ArrayList<>();

    for (int i = 0; i < threadCount; i++) {
      CompletableFuture<Void> future =
          CompletableFuture.runAsync(
              () -> {
                for (int j = 0; j < operationsPerThread; j++) {
                  performReadOperation();
                }
              },
              executor);
      futures.add(future);
    }

    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get(10, TimeUnit.SECONDS);
    Duration duration = Duration.between(start, Instant.now());

    // Assert
    assertThat(duration.toMillis()).isLessThan(5000); // Should complete within 5 seconds
    int totalOperations = threadCount * operationsPerThread;
    double opsPerSecond = (totalOperations * 1000.0) / duration.toMillis();

    System.out.println(
        String.format(
            "Concurrent read test: %d operations in %d ms (%.2f ops/sec)",
            totalOperations, duration.toMillis(), opsPerSecond));

    assertThat(opsPerSecond).isGreaterThan(200); // Should handle at least 200 ops/sec

    executor.shutdown();
  }

  @Test
  @DisplayName("Should update contacts in bulk efficiently")
  @TestTransaction
  void testBulkUpdatePerformance() {
    // Arrange
    createContactsWithRoles(500);

    // Act & Measure
    Instant start = Instant.now();

    int updated =
        entityManager
            .createQuery(
                "UPDATE CustomerContact c SET c.isActive = false WHERE c.isPrimary = false")
            .executeUpdate();

    entityManager.flush();
    Duration duration = Duration.between(start, Instant.now());

    // Assert
    assertThat(duration.toMillis()).isLessThan(1000); // Should complete within 1 second
    assertThat(updated).isGreaterThan(0);

    System.out.println(
        String.format(
            "Bulk update of %d contacts completed in %d ms", updated, duration.toMillis()));
  }

  @Test
  @DisplayName("Should handle complex hierarchy queries efficiently")
  @TestTransaction
  void testHierarchyQueryPerformance() {
    // Arrange - Create hierarchical structure
    createHierarchicalContacts(3, 5); // 3 levels, 5 contacts per level

    // Act & Measure
    Instant start = Instant.now();

    // Query for all subordinates
    List<CustomerContact> results =
        entityManager
            .createQuery(
                "SELECT c FROM CustomerContact c WHERE c.reportsTo IS NOT NULL",
                CustomerContact.class)
            .getResultList();

    Duration duration = Duration.between(start, Instant.now());

    // Assert
    assertThat(duration.toMillis()).isLessThan(200); // Should complete within 200ms
    assertThat(results).isNotEmpty();

    System.out.println(
        String.format(
            "Hierarchy query found %d subordinates in %d ms", results.size(), duration.toMillis()));
  }

  @Test
  @DisplayName("Should paginate large result sets efficiently")
  @TestTransaction
  void testPaginationPerformance() {
    // Arrange
    createContactsWithRoles(1000);
    int pageSize = 20;
    int totalPages = 10;

    // Act & Measure
    Instant start = Instant.now();

    for (int page = 0; page < totalPages; page++) {
      List<CustomerContact> pageResults =
          entityManager
              .createQuery(
                  "SELECT c FROM CustomerContact c ORDER BY c.lastName", CustomerContact.class)
              .setFirstResult(page * pageSize)
              .setMaxResults(pageSize)
              .getResultList();

      assertThat(pageResults).hasSizeLessThanOrEqualTo(pageSize);
    }

    Duration duration = Duration.between(start, Instant.now());

    // Assert
    assertThat(duration.toMillis()).isLessThan(500); // All pages should load within 500ms
    double avgPageLoadTime = duration.toMillis() / (double) totalPages;

    System.out.println(
        String.format(
            "Pagination test: %d pages loaded in %d ms (avg %.2f ms/page)",
            totalPages, duration.toMillis(), avgPageLoadTime));

    assertThat(avgPageLoadTime).isLessThan(50); // Average page load should be under 50ms
  }

  @Test
  @DisplayName("Should handle role assignments efficiently")
  @TestTransaction
  void testRoleAssignmentPerformance() {
    // Arrange
    CustomerContact contact = createTestContact(1);
    entityManager.persist(contact);
    entityManager.flush();

    // Act & Measure
    Instant start = Instant.now();

    for (int i = 0; i < 100; i++) {
      contact.addRole("ROLE_" + i);
      if (i % 2 == 0) {
        contact.removeRole("ROLE_" + (i - 1));
      }
    }

    entityManager.flush();
    Duration duration = Duration.between(start, Instant.now());

    // Assert
    assertThat(duration.toMillis()).isLessThan(100); // Should complete within 100ms
    assertThat(contact.getRoles()).hasSizeGreaterThan(40);

    System.out.println(
        String.format("Role assignment operations completed in %d ms", duration.toMillis()));
  }

  // Helper methods

  private CustomerContact createTestContact(int index) {
    CustomerContact contact =
        ContactTestDataFactory.builder()
            .forCustomer(testCustomer)
            .withFirstName("Test" + index)
            .withLastName("Contact" + index)
            .withEmail("test" + index + "@example.com")
            .withIsPrimary(index == 0)
            .build();
    contact.setIsActive(true);
    return contact;
  }

  @TestTransaction
  void createContactsWithRoles(int count) {
    for (int i = 0; i < count; i++) {
      CustomerContact contact = createTestContact(i);

      // Add various roles
      if (i % 3 == 0) {
        contact.addRole("DECISION_MAKER");
      }
      if (i % 5 == 0) {
        contact.addRole("TECHNICAL_CONTACT");
      }
      if (i % 7 == 0) {
        contact.addRole("SALES_MANAGER");
      }

      entityManager.persist(contact);
    }
    entityManager.flush();
  }

  @TestTransaction
  void createHierarchicalContacts(int levels, int contactsPerLevel) {
    CustomerContact topLevel = null;

    for (int level = 0; level < levels; level++) {
      CustomerContact currentLevelParent = null;

      for (int i = 0; i < contactsPerLevel; i++) {
        CustomerContact contact = createTestContact(level * contactsPerLevel + i);

        if (topLevel != null && level > 0) {
          contact.setReportsTo(currentLevelParent != null ? currentLevelParent : topLevel);
        }

        entityManager.persist(contact);

        if (level == 0 && i == 0) {
          topLevel = contact;
        }
        if (i == 0) {
          currentLevelParent = contact;
        }
      }
    }
    entityManager.flush();
  }

  @TestTransaction
  void performReadOperation() {
    entityManager
        .createQuery("SELECT COUNT(c) FROM CustomerContact c WHERE c.isActive = true", Long.class)
        .getSingleResult();
  }
}
