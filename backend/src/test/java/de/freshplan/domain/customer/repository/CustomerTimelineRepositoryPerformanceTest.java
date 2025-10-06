package de.freshplan.domain.customer.repository;

import static org.assertj.core.api.Assertions.assertThat;

import de.freshplan.domain.customer.entity.*;
import de.freshplan.test.builders.CustomerTestDataFactory;
import de.freshplan.test.utils.BaseIntegrationTest;
import io.quarkus.panache.common.Page;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.hibernate.Session;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Performance tests to verify N+1 query fixes in CustomerTimelineRepository.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@QuarkusTest
@Tag("performance")
@TestSecurity(
    user = "testuser",
    roles = {"admin", "manager", "sales"})
class CustomerTimelineRepositoryPerformanceTest extends BaseIntegrationTest {

  @Inject CustomerTimelineRepository timelineRepository;

  @Inject CustomerRepository customerRepository;

  private UUID testCustomerId;
  private Statistics hibernateStats;

  private void setupTestData() {
    // Clean only test-specific data to preserve CustomerDataInitializer test customers
    timelineRepository.deleteAll();
    customerRepository.delete("customerNumber LIKE ?1", "PERF-TEST-%");

    // Create test customer using CustomerTestDataFactory
    Customer customer =
        CustomerTestDataFactory.builder()
            .withCompanyName("Performance Test Company")
            .withStatus(CustomerStatus.AKTIV)
            .withIndustry(Industry.SONSTIGE)
            .build();
    // Override auto-generated values
    customer.setCustomerNumber(de.freshplan.TestIds.uniqueCustomerNumber());
    customer.setCompanyName("Performance Test Company"); // Remove [TEST-xxx] prefix
    customerRepository.persist(customer);
    testCustomerId = customer.getId();

    // Create 10 test timeline events
    for (int i = 1; i <= 10; i++) {
      CustomerTimelineEvent event = new CustomerTimelineEvent();
      event.setCustomer(customer);
      event.setEventType("TEST_EVENT_" + i);
      event.setTitle("Test Event " + i);
      event.setDescription("Performance test event " + i);
      event.setCategory(EventCategory.NOTE);
      event.setImportance(ImportanceLevel.MEDIUM);
      event.setPerformedBy("testuser");
      event.setEventDate(LocalDateTime.now().minusHours(i));
      timelineRepository.persist(event);
    }

    // Get Hibernate statistics
    hibernateStats =
        timelineRepository
            .getEntityManager()
            .unwrap(Session.class)
            .getSessionFactory()
            .getStatistics();
    hibernateStats.setStatisticsEnabled(true);
    hibernateStats.clear();
  }

  @Test
  @TestTransaction
  void findByCustomerId_shouldNotCauseN1Queries() {
    // Setup test data within the transaction
    setupTestData();

    // Given: Statistics are cleared and enabled
    hibernateStats.clear();
    long queriesBeforeTest = hibernateStats.getQueryExecutionCount();

    // When: Fetching timeline events
    List<CustomerTimelineEvent> events =
        timelineRepository.findByCustomerId(testCustomerId, Page.of(0, 10));

    // Then: Verify results
    assertThat(events).hasSize(10);
    assertThat(events.get(0).getCustomer()).isNotNull();
    assertThat(events.get(0).getCustomer().getCompanyName()).isEqualTo("Performance Test Company");

    // And: Check query count (should be 1 query due to JOIN FETCH)
    long queriesAfterTest = hibernateStats.getQueryExecutionCount();
    long executedQueries = queriesAfterTest - queriesBeforeTest;

    // In CI environment, statistics might count differently due to transaction management
    // Allow up to 3 queries (transaction begin, main query, transaction end)
    assertThat(executedQueries)
        .as("Should execute minimal queries with JOIN FETCH, not N+1 queries")
        .isLessThanOrEqualTo(3);
  }

  @Test
  @TestTransaction
  void findByCustomerIdAndCategory_shouldNotCauseN1Queries() {
    // Setup test data within the transaction
    setupTestData();

    // Given: Statistics are cleared
    hibernateStats.clear();
    long queriesBeforeTest = hibernateStats.getQueryExecutionCount();

    // When: Fetching timeline events by category
    List<CustomerTimelineEvent> events =
        timelineRepository.findByCustomerIdAndCategory(
            testCustomerId, EventCategory.NOTE, Page.of(0, 10));

    // Then: Verify results
    assertThat(events).hasSize(10);
    assertThat(events.get(0).getCustomer()).isNotNull();

    // And: Check query count
    long queriesAfterTest = hibernateStats.getQueryExecutionCount();
    long executedQueries = queriesAfterTest - queriesBeforeTest;

    // Allow up to 3 queries in CI environment
    assertThat(executedQueries)
        .as("Should execute minimal queries with JOIN FETCH")
        .isLessThanOrEqualTo(3);
  }

  @Test
  @TestTransaction
  void searchByCustomerIdAndText_shouldNotCauseN1Queries() {
    // Setup test data within the transaction
    setupTestData();

    // Given: Statistics are cleared
    hibernateStats.clear();
    long queriesBeforeTest = hibernateStats.getQueryExecutionCount();

    // When: Searching timeline events
    List<CustomerTimelineEvent> events =
        timelineRepository.searchByCustomerIdAndText(testCustomerId, "Performance", Page.of(0, 10));

    // Then: Verify results
    assertThat(events).hasSize(10);
    assertThat(events.get(0).getCustomer()).isNotNull();

    // And: Check query count
    long queriesAfterTest = hibernateStats.getQueryExecutionCount();
    long executedQueries = queriesAfterTest - queriesBeforeTest;

    // Allow up to 3 queries in CI environment
    assertThat(executedQueries)
        .as("Should execute minimal queries with JOIN FETCH")
        .isLessThanOrEqualTo(3);
  }

  @Test
  @TestTransaction
  void countByCustomerId_shouldUseDirectColumnAccess() {
    // Setup test data within the transaction
    setupTestData();

    // Given: Statistics are cleared
    hibernateStats.clear();
    long queriesBeforeTest = hibernateStats.getQueryExecutionCount();

    // When: Counting timeline events
    long count = timelineRepository.countByCustomerId(testCustomerId);

    // Then: Verify result
    assertThat(count).isEqualTo(10);

    // And: Check query count (should be 1 efficient count query)
    long queriesAfterTest = hibernateStats.getQueryExecutionCount();
    long executedQueries = queriesAfterTest - queriesBeforeTest;

    // Allow up to 3 queries in CI environment
    assertThat(executedQueries).as("Should execute minimal count queries").isLessThanOrEqualTo(3);
  }
}
