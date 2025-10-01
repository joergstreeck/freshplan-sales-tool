package de.freshplan.domain.customer.service.timeline.query;

import static org.junit.jupiter.api.Assertions.*;

import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.entity.CustomerTimelineEvent;
import de.freshplan.domain.customer.entity.EventCategory;
import de.freshplan.domain.customer.entity.ImportanceLevel;
import de.freshplan.domain.customer.repository.CustomerTimelineRepository;
import de.freshplan.domain.customer.service.dto.timeline.TimelineEventResponse;
import de.freshplan.domain.customer.service.dto.timeline.TimelineListResponse;
import de.freshplan.domain.customer.service.dto.timeline.TimelineSummaryResponse;
import de.freshplan.domain.customer.service.exception.CustomerNotFoundException;
import de.freshplan.test.builders.CustomerTestDataFactory;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for TimelineQueryService.
 *
 * <p>Converted from Mockito unit tests to @QuarkusTest integration tests in Phase 4B. Uses
 * self-managed test data (entity.persist()) instead of mocks.
 *
 * <p>Verifies that all query operations work correctly with real database interactions and
 * maintains identical behavior to the original CustomerTimelineService.
 *
 * <p>IMPORTANT: @TestTransaction is applied per-method (not class-level) to ensure proper test
 * isolation and rollback after each test.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@QuarkusTest
@Tag("integration")
class TimelineQueryServiceTest {

  @Inject TimelineQueryService queryService;

  @Inject CustomerTimelineRepository timelineRepository;

  /**
   * Creates and persists a test customer within the test transaction. Must be called at the
   * beginning of each test method.
   */
  private Customer createAndPersistTestCustomer() {
    Customer testCustomer =
        CustomerTestDataFactory.builder()
            .withCompanyName("Test Company")
            .withCustomerNumber("KD-" + System.nanoTime() % 1000000)
            .build();
    testCustomer.persist();
    return testCustomer;
  }

  /**
   * Creates and persists timeline events for a customer. Helper method for tests requiring existing
   * timeline data.
   */
  private void createTestTimelineEvents(Customer customer, int count) {
    for (int i = 0; i < count; i++) {
      CustomerTimelineEvent event = new CustomerTimelineEvent();
      event.setCustomer(customer);
      event.setEventType(i % 2 == 0 ? "CUSTOMER_CREATED" : "COMMUNICATION");
      event.setTitle(i % 2 == 0 ? "Customer Created" : "Email Sent");
      event.setDescription(i % 2 == 0 ? "New customer created" : "Follow-up email sent");
      event.setCategory(i % 2 == 0 ? EventCategory.SYSTEM : EventCategory.EMAIL);
      event.setImportance(ImportanceLevel.MEDIUM);
      event.setEventDate(LocalDateTime.now().minusDays(count - i));
      event.setPerformedBy(i % 2 == 0 ? "admin" : "sales");
      event.persist();
    }
  }

  @TestTransaction
  @Test
  void testGetCustomerTimeline_withoutFilters_shouldReturnAllEvents() {
    // Given - Create test customer within test transaction
    Customer testCustomer = createAndPersistTestCustomer();
    UUID testCustomerId = testCustomer.getId();
    createTestTimelineEvents(testCustomer, 25);

    // When
    TimelineListResponse result =
        queryService.getCustomerTimeline(testCustomerId, 0, 10, null, null);

    // Then
    assertNotNull(result);
    assertTrue(result.getContent().size() <= 10); // Page size respected
    assertEquals(0, result.getPage());
    assertEquals(10, result.getSize());
    assertEquals(25, result.getTotalElements());
    assertEquals(3, result.getTotalPages()); // 25 elements / 10 per page = 3 pages
  }

  @TestTransaction
  @Test
  void testGetCustomerTimeline_withSearchFilter_shouldSearchByText() {
    // Given - Create test customer within test transaction
    Customer testCustomer = createAndPersistTestCustomer();
    UUID testCustomerId = testCustomer.getId();
    createTestTimelineEvents(testCustomer, 10);

    // When
    TimelineListResponse result =
        queryService.getCustomerTimeline(testCustomerId, 0, 10, null, "email");

    // Then
    assertNotNull(result);
    // All events with "email" in title/description should be returned
    result
        .getContent()
        .forEach(
            event -> {
              String searchText = (event.getTitle() + " " + event.getDescription()).toLowerCase();
              assertTrue(
                  searchText.contains("email"),
                  "Event should contain 'email' in title or description");
            });
  }

  @TestTransaction
  @Test
  void testGetCustomerTimeline_withCategoryFilter_shouldFilterByCategory() {
    // Given - Create test customer within test transaction
    Customer testCustomer = createAndPersistTestCustomer();
    UUID testCustomerId = testCustomer.getId();
    createTestTimelineEvents(testCustomer, 10);

    // When
    TimelineListResponse result =
        queryService.getCustomerTimeline(testCustomerId, 0, 10, "EMAIL", null);

    // Then
    assertNotNull(result);
    // All returned events should be EMAIL category
    assertTrue(result.getContent().size() > 0, "Should have at least one EMAIL event");
    result
        .getContent()
        .forEach(
            event -> {
              // Category is returned as Enum toString() which gives displayName "E-Mail"
              assertTrue(
                  event.getCategory().toString().equals("E-Mail"),
                  "Event should be EMAIL category, was: " + event.getCategory());
            });
  }

  @TestTransaction
  @Test
  void testGetCustomerTimeline_withCustomerNotFound_shouldThrowException() {
    // Given
    UUID nonExistentCustomerId = UUID.randomUUID();

    // When & Then
    assertThrows(
        CustomerNotFoundException.class,
        () -> {
          queryService.getCustomerTimeline(nonExistentCustomerId, 0, 10, null, null);
        });
  }

  @TestTransaction
  @Test
  void testGetCustomerTimeline_withSizeExceedingMax_shouldLimitToMax() {
    // Given - Create test customer within test transaction
    Customer testCustomer = createAndPersistTestCustomer();
    UUID testCustomerId = testCustomer.getId();
    createTestTimelineEvents(testCustomer, 200);

    int requestedSize = 500; // Exceeds max of 100

    // When
    TimelineListResponse result =
        queryService.getCustomerTimeline(testCustomerId, 0, requestedSize, null, null);

    // Then
    assertNotNull(result);
    // Size should be capped at 100
    assertTrue(
        result.getContent().size() <= 100,
        "Result size should not exceed max of 100, was: " + result.getContent().size());
  }

  @TestTransaction
  @Test
  void testGetFollowUpEvents_shouldReturnEventsRequiringFollowUp() {
    // Given - Create test customer within test transaction
    Customer testCustomer = createAndPersistTestCustomer();
    UUID testCustomerId = testCustomer.getId();

    // Create events with follow-up required
    CustomerTimelineEvent event1 = new CustomerTimelineEvent();
    event1.setCustomer(testCustomer);
    event1.setEventType("NOTE");
    event1.setTitle("Follow-up Note");
    event1.setDescription("Needs follow-up");
    event1.setCategory(EventCategory.NOTE);
    event1.setImportance(ImportanceLevel.HIGH);
    event1.setEventDate(LocalDateTime.now());
    event1.setPerformedBy("testuser");
    event1.setRequiresFollowUp(true);
    event1.setFollowUpDate(LocalDateTime.now().plusDays(7));
    event1.persist();

    CustomerTimelineEvent event2 = new CustomerTimelineEvent();
    event2.setCustomer(testCustomer);
    event2.setEventType("CALL");
    event2.setTitle("Customer Call");
    event2.setDescription("Follow-up call needed");
    event2.setCategory(EventCategory.PHONE_CALL);
    event2.setImportance(ImportanceLevel.MEDIUM);
    event2.setEventDate(LocalDateTime.now());
    event2.setPerformedBy("testuser");
    event2.setRequiresFollowUp(true);
    event2.setFollowUpDate(LocalDateTime.now().plusDays(3));
    event2.persist();

    // When
    List<TimelineEventResponse> result = queryService.getFollowUpEvents(testCustomerId);

    // Then
    assertNotNull(result);
    assertEquals(2, result.size());
    result.forEach(
        event -> {
          assertTrue(event.getRequiresFollowUp(), "All returned events should require follow-up");
        });
  }

  @TestTransaction
  @Test
  void testGetOverdueFollowUps_shouldReturnOverdueEvents() {
    // Given - Create test customer within test transaction
    Customer testCustomer = createAndPersistTestCustomer();
    UUID testCustomerId = testCustomer.getId();

    // Create overdue event
    CustomerTimelineEvent overdueEvent = new CustomerTimelineEvent();
    overdueEvent.setCustomer(testCustomer);
    overdueEvent.setEventType("NOTE");
    overdueEvent.setTitle("Overdue Note");
    overdueEvent.setDescription("This is overdue");
    overdueEvent.setCategory(EventCategory.NOTE);
    overdueEvent.setImportance(ImportanceLevel.HIGH);
    overdueEvent.setEventDate(LocalDateTime.now().minusDays(10));
    overdueEvent.setPerformedBy("testuser");
    overdueEvent.setRequiresFollowUp(true);
    overdueEvent.setFollowUpDate(LocalDateTime.now().minusDays(2)); // Overdue
    overdueEvent.persist();

    // Create future follow-up (should not be in results)
    CustomerTimelineEvent futureEvent = new CustomerTimelineEvent();
    futureEvent.setCustomer(testCustomer);
    futureEvent.setEventType("CALL");
    futureEvent.setTitle("Future Call");
    futureEvent.setDescription("Not overdue yet");
    futureEvent.setCategory(EventCategory.PHONE_CALL);
    futureEvent.setImportance(ImportanceLevel.MEDIUM);
    futureEvent.setEventDate(LocalDateTime.now());
    futureEvent.setPerformedBy("testuser");
    futureEvent.setRequiresFollowUp(true);
    futureEvent.setFollowUpDate(LocalDateTime.now().plusDays(5)); // Future
    futureEvent.persist();

    // When
    List<TimelineEventResponse> result = queryService.getOverdueFollowUps(testCustomerId);

    // Then
    assertNotNull(result);
    assertEquals(1, result.size(), "Should only return overdue events");
    assertEquals("Overdue Note", result.get(0).getTitle());
  }

  @TestTransaction
  @Test
  void testGetRecentCommunications_shouldReturnRecentCommunicationEvents() {
    // Given - Create test customer within test transaction
    Customer testCustomer = createAndPersistTestCustomer();
    UUID testCustomerId = testCustomer.getId();

    // Create recent communication (within 7 days)
    CustomerTimelineEvent recentComm = new CustomerTimelineEvent();
    recentComm.setCustomer(testCustomer);
    recentComm.setEventType("COMMUNICATION");
    recentComm.setTitle("Recent Email");
    recentComm.setDescription("Recent communication");
    recentComm.setCategory(EventCategory.EMAIL);
    recentComm.setImportance(ImportanceLevel.MEDIUM);
    recentComm.setEventDate(LocalDateTime.now().minusDays(3));
    recentComm.setPerformedBy("sales");
    recentComm.persist();

    // Create old communication (outside 7 days)
    CustomerTimelineEvent oldComm = new CustomerTimelineEvent();
    oldComm.setCustomer(testCustomer);
    oldComm.setEventType("COMMUNICATION");
    oldComm.setTitle("Old Email");
    oldComm.setDescription("Old communication");
    oldComm.setCategory(EventCategory.EMAIL);
    oldComm.setImportance(ImportanceLevel.LOW);
    oldComm.setEventDate(LocalDateTime.now().minusDays(10));
    oldComm.setPerformedBy("sales");
    oldComm.persist();

    int days = 7;

    // When
    List<TimelineEventResponse> result = queryService.getRecentCommunications(testCustomerId, days);

    // Then
    assertNotNull(result);
    assertEquals(1, result.size(), "Should only return communications from last 7 days");
    assertEquals("Recent Email", result.get(0).getTitle());
  }

  @TestTransaction
  @Test
  void testGetTimelineSummary_shouldReturnSummaryStatistics() {
    // Given - Create test customer within test transaction
    Customer testCustomer = createAndPersistTestCustomer();
    UUID testCustomerId = testCustomer.getId();

    // Create diverse events for summary
    createEventOfCategory(testCustomer, EventCategory.SYSTEM, 25);
    createEventOfCategory(testCustomer, EventCategory.COMMUNICATION, 40);
    createEventOfCategory(testCustomer, EventCategory.MEETING, 20);
    createEventOfCategory(testCustomer, EventCategory.TASK, 15);

    // When
    TimelineSummaryResponse result = queryService.getTimelineSummary(testCustomerId);

    // Then
    assertNotNull(result);
    assertEquals(100, result.getTotalEvents());
    assertEquals(40, result.getCommunicationEvents()); // COMMUNICATION category
    assertEquals(20, result.getMeetingEvents());
    assertEquals(15, result.getTaskEvents()); // TASK category
    assertEquals(25, result.getSystemEvents());
  }

  @TestTransaction
  @Test
  void testNoWriteOperationsInQueryService() {
    // Given - Create test customer within test transaction
    Customer testCustomer = createAndPersistTestCustomer();
    UUID testCustomerId = testCustomer.getId();
    createTestTimelineEvents(testCustomer, 10);

    // Count events before operations
    long eventCountBefore = timelineRepository.count();

    // When - Execute all query methods
    queryService.getCustomerTimeline(testCustomerId, 0, 10, null, null);
    queryService.getFollowUpEvents(testCustomerId);
    queryService.getOverdueFollowUps(testCustomerId);
    queryService.getRecentCommunications(testCustomerId, 7);
    queryService.getTimelineSummary(testCustomerId);

    // Then - Verify NO write operations occurred
    long eventCountAfter = timelineRepository.count();
    assertEquals(
        eventCountBefore,
        eventCountAfter,
        "Query service should not create, update, or delete any events");
  }

  // ==================== Helper Methods ====================

  private void createEventOfCategory(Customer customer, EventCategory category, int count) {
    for (int i = 0; i < count; i++) {
      CustomerTimelineEvent event = new CustomerTimelineEvent();
      event.setCustomer(customer);
      event.setEventType(category.name());
      event.setTitle(category.name() + " Event " + i);
      event.setDescription("Event of category " + category.name());
      event.setCategory(category);
      event.setImportance(ImportanceLevel.MEDIUM);
      event.setEventDate(LocalDateTime.now().minusDays(i));
      event.setPerformedBy("testuser");
      event.persist();
    }
  }
}
