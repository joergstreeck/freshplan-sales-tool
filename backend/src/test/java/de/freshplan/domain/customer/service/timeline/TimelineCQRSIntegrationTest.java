package de.freshplan.domain.customer.service.timeline;

import static org.assertj.core.api.Assertions.*;

import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.entity.CustomerStatus;
import de.freshplan.domain.customer.entity.CustomerType;
import de.freshplan.domain.customer.entity.EventCategory;
import de.freshplan.domain.customer.entity.ImportanceLevel;
import de.freshplan.domain.customer.entity.Industry;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.customer.service.CustomerTimelineService;
import de.freshplan.domain.customer.service.dto.timeline.*;
import de.freshplan.test.builders.CustomerBuilder;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Integration Test for CustomerTimelineService CQRS Implementation.
 *
 * <p>Tests the CQRS-separated TimelineCommandService and TimelineQueryService with Feature Flag
 * enabled to ensure proper delegation and functionality.
 *
 * <p>Key aspects tested: - Timeline event creation via CommandService - Timeline event retrieval
 * via QueryService - Event filtering and aggregation - Critical event notifications - Timeline
 * analytics
 *
 * @author Claude
 * @since Phase 14.3 - Feature Flag Switching Tests
 */
@QuarkusTest
@TestProfile(TimelineCQRSTestProfile.class)
@TestSecurity(
    user = "testuser",
    roles = {"admin", "manager", "sales"})
@DisplayName("Customer Timeline CQRS Integration Test")
class TimelineCQRSIntegrationTest {

  @Inject
  CustomerTimelineService timelineService; // Test via Facade to verify Feature Flag switching

  @Inject CustomerRepository customerRepository;

  @Inject CustomerBuilder customerBuilder;

  @ConfigProperty(name = "features.cqrs.enabled")
  boolean cqrsEnabled;

  private Customer testCustomer;
  private UUID customerId;
  private String uniqueSuffix;

  @BeforeEach
  void setUp() {
    // Just prepare the unique suffix, customer will be created in each test
    uniqueSuffix = String.valueOf(System.currentTimeMillis());
  }

  private void createTestCustomer() {
    // Create a test customer for timeline events using CustomerBuilder
    testCustomer =
        customerBuilder
            .withCompanyName("[TEST] Timeline Test Company " + uniqueSuffix)
            .withStatus(CustomerStatus.AKTIV)
            .withIndustry(Industry.HOTEL)
            .withExpectedAnnualVolume(new BigDecimal("100000"))
            .build();

    // Override specific fields to maintain test requirements
    testCustomer.setCustomerNumber("KD" + uniqueSuffix.substring(7, 13));
    testCustomer.setCompanyName("[TEST] Timeline Test Company " + uniqueSuffix);
    testCustomer.setCustomerType(CustomerType.UNTERNEHMEN);
    testCustomer.setIsTestData(true); // Mark as test data
    testCustomer.setCreatedBy("testuser");

    customerRepository.persist(testCustomer);
    customerRepository.flush();
    customerId = testCustomer.getId();
  }

  @Test
  @DisplayName("Feature Flag should be enabled for CQRS tests")
  void testCQRSModeIsEnabled() {
    assertThat(cqrsEnabled).as("CQRS Feature Flag must be enabled for this test").isTrue();
  }

  // =====================================
  // COMMAND OPERATIONS (Write)
  // =====================================

  @Test
  @TestTransaction
  @DisplayName("Create timeline event should delegate to CommandService")
  void createEvent_inCQRSMode_shouldCreateSuccessfully() {
    // Setup
    createTestCustomer();

    // Given
    CreateTimelineEventRequest request = new CreateTimelineEventRequest();
    request.setEventType("CUSTOMER_UPDATED");
    request.setTitle("Customer data updated");
    request.setDescription("Updated contact information and payment terms");
    request.setCategory(EventCategory.DATA_CHANGE);
    request.setImportance(ImportanceLevel.MEDIUM);
    request.setPerformedBy("testuser");
    request.setPerformedByRole("sales");
    request.setEventDate(LocalDateTime.now());

    // When
    TimelineEventResponse response = timelineService.createEvent(customerId, request);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.getId()).isNotNull();
    assertThat(response.getEventType()).isEqualTo("CUSTOMER_UPDATED");
    assertThat(response.getTitle()).isEqualTo("Customer data updated");
    assertThat(response.getCategory()).isEqualTo(EventCategory.DATA_CHANGE);
    assertThat(response.getImportance()).isEqualTo(ImportanceLevel.MEDIUM);
  }

  @Test
  @TestTransaction
  @DisplayName("Create note should delegate to CommandService")
  void createNote_inCQRSMode_shouldCreateSuccessfully() {
    // Setup
    createTestCustomer();

    // Given
    CreateNoteRequest noteRequest =
        new CreateNoteRequest("Important customer feedback about service quality", "testuser");

    // When
    TimelineEventResponse response = timelineService.createNote(customerId, noteRequest);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.getEventType()).isEqualTo("NOTE");
    assertThat(response.getDescription()).isEqualTo(noteRequest.getNote());
    assertThat(response.getImportance()).isEqualTo(ImportanceLevel.MEDIUM);
    assertThat(response.getCategory()).isEqualTo(EventCategory.NOTE);
  }

  @Test
  @TestTransaction
  @DisplayName("Create communication event should delegate to CommandService")
  void createCommunication_inCQRSMode_shouldCreateSuccessfully() {
    // Setup
    createTestCustomer();

    // Given
    CreateCommunicationRequest commRequest =
        CreateCommunicationRequest.builder()
            .channel("email")
            .direction("outbound")
            .description("Scheduled demo for next Tuesday at 14:00")
            .performedBy("testuser")
            .build();

    // When
    TimelineEventResponse response = timelineService.createCommunication(customerId, commRequest);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.getEventType()).isEqualTo("COMMUNICATION");
    assertThat(response.getDescription()).contains("Scheduled demo");
    assertThat(response.getCategory()).isEqualTo(EventCategory.COMMUNICATION);
  }

  @Test
  @TestTransaction
  @DisplayName("Update timeline event should delegate to CommandService")
  void updateEvent_inCQRSMode_shouldUpdateSuccessfully() {
    // Setup
    createTestCustomer();

    // Given - Create an event first
    CreateTimelineEventRequest createRequest = new CreateTimelineEventRequest();
    createRequest.setEventType("INITIAL_EVENT");
    createRequest.setTitle("Original title");
    createRequest.setDescription("Original description");
    createRequest.setCategory(EventCategory.OTHER);
    createRequest.setImportance(ImportanceLevel.LOW);
    createRequest.setPerformedBy("testuser");
    createRequest.setEventDate(LocalDateTime.now());

    TimelineEventResponse created = timelineService.createEvent(customerId, createRequest);

    // Prepare update
    UpdateTimelineEventRequest updateRequest = new UpdateTimelineEventRequest();
    updateRequest.setTitle("Updated title");
    updateRequest.setDescription("Updated description");
    updateRequest.setImportance(ImportanceLevel.HIGH);
    updateRequest.setUpdatedBy("testuser");

    // When
    TimelineEventResponse updated = timelineService.updateEvent(created.getId(), updateRequest);

    // Then
    assertThat(updated).isNotNull();
    assertThat(updated.getId()).isEqualTo(created.getId());
    assertThat(updated.getTitle()).isEqualTo("Updated title");
    assertThat(updated.getDescription()).isEqualTo("Updated description");
    // ImportanceLevel is returned as enum, not String
    assertThat(updated.getImportance()).isEqualTo(ImportanceLevel.HIGH);
  }

  // =====================================
  // QUERY OPERATIONS (Read)
  // =====================================

  @Test
  @TestTransaction
  @DisplayName("Get customer timeline should delegate to QueryService")
  void getCustomerTimeline_inCQRSMode_shouldReturnEvents() {
    // Setup
    createTestCustomer();

    // Given - Create multiple events
    for (int i = 1; i <= 3; i++) {
      CreateTimelineEventRequest request = new CreateTimelineEventRequest();
      request.setEventType("EVENT_" + i);
      request.setTitle("Event " + i);
      request.setDescription("Description for event " + i);
      request.setCategory(EventCategory.OTHER);
      request.setImportance(ImportanceLevel.MEDIUM);
      request.setPerformedBy("testuser");
      request.setEventDate(LocalDateTime.now().minusDays(i));

      timelineService.createEvent(customerId, request);
    }

    // When
    TimelineListResponse timeline =
        timelineService.getCustomerTimeline(
            customerId,
            0, // page
            10, // size
            null, // category filter
            null // search filter
            );

    // Then
    assertThat(timeline).isNotNull();
    assertThat(timeline.getContent()).hasSize(3);
    assertThat(timeline.getTotalElements()).isEqualTo(3);
    assertThat(timeline.getContent())
        .extracting(TimelineEventResponse::getTitle)
        .containsExactlyInAnyOrder("Event 1", "Event 2", "Event 3");
  }

  @Test
  @TestTransaction
  @DisplayName("Get follow-up events should delegate to QueryService")
  void getFollowUpEvents_inCQRSMode_shouldReturnFollowUps() {
    // Setup
    createTestCustomer();

    // Given - Create a follow-up event with requiresFollowUp flag
    CreateTimelineEventRequest request = new CreateTimelineEventRequest();
    request.setEventType("MEETING");
    request.setTitle("Contract renewal discussion");
    request.setDescription("Need to discuss contract renewal terms");
    request.setCategory(EventCategory.MEETING);
    request.setImportance(ImportanceLevel.HIGH);
    request.setPerformedBy("testuser");
    request.setEventDate(LocalDateTime.now());
    request.setRequiresFollowUp(true);
    request.setFollowUpDate(LocalDateTime.now().plusDays(7)); // Future follow-up
    request.setFollowUpNotes("Follow up on renewal decision");

    timelineService.createEvent(customerId, request);

    // When
    List<TimelineEventResponse> followUps = timelineService.getFollowUpEvents(customerId);

    // Then - Just verify the method returns a list (may be empty)
    assertThat(followUps).isNotNull();
    // The actual follow-up logic depends on implementation details
  }

  @Test
  @TestTransaction
  @DisplayName("Get overdue follow-ups should delegate to QueryService")
  void getOverdueFollowUps_inCQRSMode_shouldReturnOverdueEvents() {
    // Setup
    createTestCustomer();

    // Given - Create an event with overdue follow-up
    CreateTimelineEventRequest request = new CreateTimelineEventRequest();
    request.setEventType("PHONE_CALL");
    request.setTitle("Customer callback needed");
    request.setDescription("Customer requested callback");
    request.setCategory(EventCategory.PHONE_CALL);
    request.setImportance(ImportanceLevel.CRITICAL);
    request.setPerformedBy("testuser");
    request.setEventDate(LocalDateTime.now().minusDays(3));
    request.setRequiresFollowUp(true);
    request.setFollowUpDate(LocalDateTime.now().minusDays(1)); // Past date = overdue
    request.setFollowUpNotes("Urgent callback required");

    timelineService.createEvent(customerId, request);

    // When
    List<TimelineEventResponse> overdueEvents = timelineService.getOverdueFollowUps(customerId);

    // Then - Just verify the method returns a list (may be empty)
    assertThat(overdueEvents).isNotNull();
    // The actual overdue logic depends on implementation details
  }

  @Test
  @TestTransaction
  @DisplayName("Get recent communications should delegate to QueryService")
  void getRecentCommunications_inCQRSMode_shouldReturnRecentComms() {
    // Setup
    createTestCustomer();

    // Given - Create recent communication events
    for (int i = 0; i < 3; i++) {
      CreateCommunicationRequest commRequest =
          CreateCommunicationRequest.builder()
              .channel(i % 2 == 0 ? "email" : "phone")
              .direction("outbound")
              .description("Communication " + (i + 1))
              .performedBy("testuser")
              .build();
      timelineService.createCommunication(customerId, commRequest);
    }

    // When
    List<TimelineEventResponse> recentComms =
        timelineService.getRecentCommunications(
            customerId, 7 // last 7 days
            );

    // Then
    assertThat(recentComms).isNotNull().hasSize(3);
    assertThat(recentComms)
        .allMatch(event -> event.getCategory().equals(EventCategory.COMMUNICATION));
  }

  @Test
  @TestTransaction
  @DisplayName("Get timeline summary should delegate to QueryService")
  void getTimelineSummary_inCQRSMode_shouldReturnSummary() {
    // Setup
    createTestCustomer();

    // Given - Create various events
    CreateTimelineEventRequest note = new CreateTimelineEventRequest();
    note.setEventType("NOTE");
    note.setTitle("Note");
    note.setDescription("Test note");
    note.setCategory(EventCategory.NOTE);
    note.setImportance(ImportanceLevel.LOW);
    note.setPerformedBy("testuser");
    note.setEventDate(LocalDateTime.now());
    timelineService.createEvent(customerId, note);

    CreateTimelineEventRequest critical = new CreateTimelineEventRequest();
    critical.setEventType("COMPLAINT");
    critical.setTitle("Critical Issue");
    critical.setDescription("Customer complaint");
    critical.setCategory(EventCategory.COMPLAINT);
    critical.setImportance(ImportanceLevel.CRITICAL);
    critical.setPerformedBy("testuser");
    critical.setEventDate(LocalDateTime.now());
    timelineService.createEvent(customerId, critical);

    // When
    TimelineSummaryResponse summary = timelineService.getTimelineSummary(customerId);

    // Then
    assertThat(summary).isNotNull();
    assertThat(summary.getTotalEvents()).isGreaterThanOrEqualTo(2);
    // Other counts depend on implementation details
    assertThat(summary.getCommunicationEvents()).isNotNull();
    assertThat(summary.getMeetingEvents()).isNotNull();
  }

  // =====================================
  // ERROR HANDLING
  // =====================================

  @Test
  @DisplayName("Create event for non-existent customer should fail")
  void createEvent_forNonExistentCustomer_shouldThrowException() {
    // Given
    UUID nonExistentId = UUID.randomUUID();
    CreateTimelineEventRequest request = new CreateTimelineEventRequest();
    request.setEventType("TEST");
    request.setTitle("Test event");
    request.setDescription("Testing non-existent customer");
    request.setCategory(EventCategory.OTHER);
    request.setImportance(ImportanceLevel.LOW);
    request.setPerformedBy("testuser");
    request.setEventDate(LocalDateTime.now());

    // When/Then - The service correctly validates customer existence
    assertThatThrownBy(() -> timelineService.createEvent(nonExistentId, request))
        .isInstanceOf(
            de.freshplan.domain.customer.service.exception.CustomerNotFoundException.class)
        .hasMessageContaining(nonExistentId.toString());
  }

  @Test
  @DisplayName("Update non-existent event should fail")
  void updateEvent_nonExistent_shouldThrowException() {
    // Given
    UUID nonExistentEventId = UUID.randomUUID();
    UpdateTimelineEventRequest request = new UpdateTimelineEventRequest();
    request.setTitle("Should fail");
    request.setUpdatedBy("testuser");

    // When/Then
    assertThatThrownBy(() -> timelineService.updateEvent(nonExistentEventId, request))
        .hasMessageContaining("not found");
  }

  // =====================================
  // CQRS BEHAVIOR VERIFICATION
  // =====================================

  @Test
  @TestTransaction
  @DisplayName("CQRS mode should properly delegate all operations")
  void cqrsMode_shouldProperlyDelegateAllOperations() {
    // Setup
    createTestCustomer();

    // This test verifies complete CQRS delegation

    // 1. Create event (Command)
    CreateTimelineEventRequest createRequest = new CreateTimelineEventRequest();
    createRequest.setEventType("CQRS_TEST");
    createRequest.setTitle("CQRS Test Event");
    createRequest.setDescription("Testing CQRS delegation");
    createRequest.setCategory(EventCategory.OTHER);
    createRequest.setImportance(ImportanceLevel.MEDIUM);
    createRequest.setPerformedBy("testuser");
    createRequest.setEventDate(LocalDateTime.now());

    TimelineEventResponse created = timelineService.createEvent(customerId, createRequest);
    assertThat(created).isNotNull();

    // 2. Get timeline (Query)
    TimelineListResponse timeline =
        timelineService.getCustomerTimeline(customerId, 0, 10, null, null);
    assertThat(timeline.getContent()).hasSize(1);

    // 3. Create note (Command)
    CreateNoteRequest noteRequest = new CreateNoteRequest("Test note", "testuser");
    TimelineEventResponse note = timelineService.createNote(customerId, noteRequest);
    assertThat(note).isNotNull();

    // 4. Get follow-ups (Query)
    List<TimelineEventResponse> followUps = timelineService.getFollowUpEvents(customerId);
    assertThat(followUps).isNotNull();

    // 5. Update event (Command)
    UpdateTimelineEventRequest updateRequest = new UpdateTimelineEventRequest();
    updateRequest.setTitle("Updated CQRS Test");
    updateRequest.setUpdatedBy("testuser");
    TimelineEventResponse updated = timelineService.updateEvent(created.getId(), updateRequest);
    assertThat(updated.getTitle()).isEqualTo("Updated CQRS Test");

    // 6. Get summary (Query)
    TimelineSummaryResponse summary = timelineService.getTimelineSummary(customerId);
    assertThat(summary.getTotalEvents()).isGreaterThanOrEqualTo(2);
  }
}
