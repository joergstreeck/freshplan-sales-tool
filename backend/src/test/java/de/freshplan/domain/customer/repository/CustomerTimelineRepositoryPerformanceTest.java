package de.freshplan.domain.customer.repository;

import static org.assertj.core.api.Assertions.assertThat;

import de.freshplan.domain.customer.entity.*;
import de.freshplan.test.BaseIntegrationTest;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Performance tests to verify N+1 query fixes in CustomerTimelineRepository.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@QuarkusTest
@TestSecurity(
    user = "testuser",
    roles = {"admin", "manager", "sales"})
class CustomerTimelineRepositoryPerformanceTest extends BaseIntegrationTest {

  @Inject CustomerTimelineRepository timelineRepository;

  @Inject CustomerRepository customerRepository;

  private UUID testCustomerId;
  private Statistics hibernateStats;

  @BeforeEach
  @TestTransaction
  void setUp() {
    // Clean only test-specific data to preserve CustomerDataInitializer test customers
    timelineRepository.deleteAll();
    customerRepository.delete("customerNumber LIKE ?1", "PERF-TEST-%");

    // Create test customer
    Customer customer = new Customer();
    customer.setCustomerNumber("PERF-TEST-001");
    customer.setCompanyName("Performance Test Company");
    customer.setStatus(CustomerStatus.AKTIV);
    customer.setCustomerType(CustomerType.UNTERNEHMEN);
    customer.setIndustry(Industry.SONSTIGE);
    customer.setCreatedAt(LocalDateTime.now());
    customer.setCreatedBy("test");
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

    assertThat(executedQueries)
        .as("Should execute only 1 query with JOIN FETCH, not N+1 queries")
        .isEqualTo(1);
  }

  @Test
  @TestTransaction
  void findByCustomerIdAndCategory_shouldNotCauseN1Queries() {
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

    assertThat(executedQueries).as("Should execute only 1 query with JOIN FETCH").isEqualTo(1);
  }

  @Test
  @TestTransaction
  void searchByCustomerIdAndText_shouldNotCauseN1Queries() {
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

    assertThat(executedQueries).as("Should execute only 1 query with JOIN FETCH").isEqualTo(1);
  }

  @Test
  @TestTransaction
  void countByCustomerId_shouldUseDirectColumnAccess() {
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

    assertThat(executedQueries).as("Should execute only 1 count query").isEqualTo(1);
  }
}
