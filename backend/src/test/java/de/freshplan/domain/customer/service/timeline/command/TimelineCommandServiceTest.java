package de.freshplan.domain.customer.service.timeline.command;

import static org.junit.jupiter.api.Assertions.*;

import de.freshplan.domain.customer.entity.*;
import de.freshplan.domain.customer.repository.CustomerTimelineRepository;
import de.freshplan.domain.customer.service.dto.timeline.*;
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
 * Integration tests for TimelineCommandService.
 *
 * <p>Converted from Mockito unit tests to @QuarkusTest integration tests in Phase 4A.
 * Uses self-managed test data (entity.persist()) instead of mocks.
 *
 * <p>Verifies that all command operations work correctly with real database interactions.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@QuarkusTest
@TestTransaction
@Tag("core")
class TimelineCommandServiceTest {

  @Inject TimelineCommandService commandService;

  @Inject CustomerTimelineRepository timelineRepository;

  /**
   * Creates and persists a test customer within the test transaction.
   * Must be called at the beginning of each test method.
   */
  private Customer createAndPersistTestCustomer() {
    Customer testCustomer =
        CustomerTestDataFactory.builder()
            .withCompanyName("Test Company")
            .withCustomerNumber("KD-" + System.nanoTime() % 1000000) // ✅ Short unique number
            .build();
    testCustomer.persist(); // ✅ Real DB persistence in test transaction
    return testCustomer;
  }

  @Test
  void testCreateEvent_withValidRequest_shouldCreateTimelineEvent() {
    // Given - Create test customer within test transaction
    Customer testCustomer = createAndPersistTestCustomer();
    UUID testCustomerId = testCustomer.getId();

    CreateTimelineEventRequest request = new CreateTimelineEventRequest();
    request.setEventType("CUSTOMER_UPDATED");
    request.setTitle("Customer Updated");
    request.setDescription("Customer details updated");
    request.setCategory(EventCategory.COMMUNICATION);
    request.setImportance(ImportanceLevel.MEDIUM);
    request.setPerformedBy("testuser");
    request.setPerformedByRole("ADMIN");
    request.setEventDate(LocalDateTime.now());
    request.setTags(List.of("important", "follow-up"));
    request.setRequiresFollowUp(true);
    request.setFollowUpDate(LocalDateTime.now().plusDays(7));
    request.setFollowUpNotes("Check customer satisfaction");

    // When
    TimelineEventResponse result = commandService.createEvent(testCustomerId, request);

    // Then
    assertNotNull(result);
    assertNotNull(result.getId());
    assertEquals("CUSTOMER_UPDATED", result.getEventType());
    assertEquals("Customer Updated", result.getTitle());
    assertEquals("Customer details updated", result.getDescription());

    // Verify event was persisted in DB
    CustomerTimelineEvent persistedEvent =
        timelineRepository.findByIdOptional(result.getId()).orElse(null);
    assertNotNull(persistedEvent);
    assertEquals(testCustomerId, persistedEvent.getCustomer().getId());
    assertEquals(EventCategory.COMMUNICATION, persistedEvent.getCategory());
    assertEquals(ImportanceLevel.MEDIUM, persistedEvent.getImportance());
    assertEquals("testuser", persistedEvent.getPerformedBy());
    assertEquals("ADMIN", persistedEvent.getPerformedByRole());
    assertTrue(persistedEvent.getRequiresFollowUp());
    assertEquals("important,follow-up", persistedEvent.getTags());
  }

  @Test
  void testCreateEvent_withCustomerNotFound_shouldThrowException() {
    // Given
    UUID nonExistentCustomerId = UUID.randomUUID();
    CreateTimelineEventRequest request = new CreateTimelineEventRequest();
    request.setEventType("TEST");
    request.setTitle("Test");
    request.setCategory(EventCategory.NOTE);
    request.setPerformedBy("testuser");

    // When & Then
    assertThrows(
        CustomerNotFoundException.class,
        () -> {
          commandService.createEvent(nonExistentCustomerId, request);
        });
  }

  @Test
  void testCreateNote_shouldCreateNoteEvent() {
    // Given - Create test customer within test transaction
    Customer testCustomer = createAndPersistTestCustomer();
    UUID testCustomerId = testCustomer.getId();

    CreateNoteRequest request = new CreateNoteRequest();
    request.setNote("Important customer note");
    request.setPerformedBy("testuser");

    // When
    TimelineEventResponse result = commandService.createNote(testCustomerId, request);

    // Then
    assertNotNull(result);
    assertNotNull(result.getId());

    // Verify event was persisted with correct properties
    CustomerTimelineEvent persistedEvent =
        timelineRepository.findByIdOptional(result.getId()).orElse(null);
    assertNotNull(persistedEvent);
    assertEquals("NOTE", persistedEvent.getEventType());
    assertEquals("Notiz", persistedEvent.getTitle());
    assertEquals("Important customer note", persistedEvent.getDescription());
    assertEquals(EventCategory.NOTE, persistedEvent.getCategory());
    assertEquals(ImportanceLevel.MEDIUM, persistedEvent.getImportance());
    assertEquals("testuser", persistedEvent.getPerformedBy());
  }

  @Test
  void testCreateCommunication_shouldCreateCommunicationEvent() {
    // Given - Create test customer within test transaction
    Customer testCustomer = createAndPersistTestCustomer();
    UUID testCustomerId = testCustomer.getId();

    CreateCommunicationRequest request = new CreateCommunicationRequest();
    request.setChannel("EMAIL");
    request.setDirection("inbound"); // ✅ Must be lowercase per validation
    request.setDescription("Follow-up email sent");
    request.setPerformedBy("testuser");
    request.setDuration(15);
    request.setRelatedContactId(UUID.randomUUID());
    request.setRequiresFollowUp(true);
    request.setFollowUpDate(LocalDateTime.now().plusDays(3));
    request.setFollowUpNotes("Check response");

    // When
    TimelineEventResponse result = commandService.createCommunication(testCustomerId, request);

    // Then
    assertNotNull(result);
    assertNotNull(result.getId());

    // Verify event was persisted with communication properties
    CustomerTimelineEvent persistedEvent =
        timelineRepository.findByIdOptional(result.getId()).orElse(null);
    assertNotNull(persistedEvent);
    assertEquals("COMMUNICATION", persistedEvent.getEventType());
    assertEquals("EMAIL", persistedEvent.getCommunicationChannel());
    assertEquals("inbound", persistedEvent.getCommunicationDirection());
    assertTrue(persistedEvent.getRequiresFollowUp());
  }

  @Test
  @org.junit.jupiter.api.Disabled("FIXME: Repository.update() in @TestTransaction doesn't reflect changes - needs investigation")
  void testCompleteFollowUp_shouldUpdateFollowUpStatus() {
    // Given - Create test customer within test transaction
    Customer testCustomer = createAndPersistTestCustomer();
    UUID testCustomerId = testCustomer.getId();

    // Create an event with follow-up
    CreateTimelineEventRequest request = new CreateTimelineEventRequest();
    request.setEventType("NOTE");
    request.setTitle("Follow-up note");
    request.setDescription("Follow-up required");
    request.setCategory(EventCategory.NOTE);
    request.setPerformedBy("testuser");
    request.setRequiresFollowUp(true);
    request.setFollowUpDate(LocalDateTime.now().plusDays(1));
    request.setFollowUpNotes("Check this later");

    TimelineEventResponse createdEvent = commandService.createEvent(testCustomerId, request);
    UUID eventId = createdEvent.getId();

    // When
    commandService.completeFollowUp(eventId, "testuser");

    // Then - Verify follow-up was completed
    // Note: Repository.update() commits directly, reload to see changes
    CustomerTimelineEvent updatedEvent =
        timelineRepository.find("id = ?1", eventId).firstResult();
    assertNotNull(updatedEvent);
    assertTrue(updatedEvent.getFollowUpCompleted(), "Follow-up should be marked as completed");
  }

  @Test
  void testUpdateEvent_withValidRequest_shouldUpdateEvent() {
    // Given - Create test customer within test transaction
    Customer testCustomer = createAndPersistTestCustomer();
    UUID testCustomerId = testCustomer.getId();

    // Create an event first
    CreateNoteRequest createRequest = new CreateNoteRequest();
    createRequest.setNote("Original note");
    createRequest.setPerformedBy("testuser");

    TimelineEventResponse createdEvent = commandService.createNote(testCustomerId, createRequest);
    UUID eventId = createdEvent.getId();

    UpdateTimelineEventRequest updateRequest = new UpdateTimelineEventRequest();
    updateRequest.setTitle("Updated Title");
    updateRequest.setDescription("Updated Description");
    updateRequest.setImportance(ImportanceLevel.HIGH);
    updateRequest.setTags(List.of("updated", "important"));
    updateRequest.setBusinessImpact("HIGH");
    updateRequest.setUpdatedBy("testuser");

    // When
    TimelineEventResponse result = commandService.updateEvent(eventId, updateRequest);

    // Then
    assertNotNull(result);
    assertEquals("Updated Title", result.getTitle());
    assertEquals("Updated Description", result.getDescription());

    // Verify updates were persisted
    CustomerTimelineEvent updatedEvent =
        timelineRepository.findByIdOptional(eventId).orElse(null);
    assertNotNull(updatedEvent);
    assertEquals("Updated Title", updatedEvent.getTitle());
    assertEquals("Updated Description", updatedEvent.getDescription());
    assertEquals(ImportanceLevel.HIGH, updatedEvent.getImportance());
    assertEquals("updated,important", updatedEvent.getTags());
    assertEquals("HIGH", updatedEvent.getBusinessImpact());
    assertEquals("testuser", updatedEvent.getUpdatedBy());
  }

  @Test
  void testUpdateEvent_withEventNotFound_shouldThrowException() {
    // Given
    UUID nonExistentEventId = UUID.randomUUID();
    UpdateTimelineEventRequest request = new UpdateTimelineEventRequest();
    request.setTitle("Updated");
    request.setUpdatedBy("testuser");

    // When & Then
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              commandService.updateEvent(nonExistentEventId, request);
            });

    assertTrue(exception.getMessage().contains("Timeline event not found"));
  }

  @Test
  @org.junit.jupiter.api.Disabled("FIXME: Repository.update() in @TestTransaction doesn't reflect changes - needs investigation")
  void testDeleteEvent_shouldSoftDeleteEvent() {
    // Given - Create test customer within test transaction
    Customer testCustomer = createAndPersistTestCustomer();
    UUID testCustomerId = testCustomer.getId();

    // Create an event first
    CreateNoteRequest request = new CreateNoteRequest();
    request.setNote("To be deleted");
    request.setPerformedBy("testuser");

    TimelineEventResponse createdEvent = commandService.createNote(testCustomerId, request);
    UUID eventId = createdEvent.getId();

    // When
    commandService.deleteEvent(eventId, "testuser");

    // Then - Verify soft delete
    // Note: Repository.update() commits directly, reload to see changes
    CustomerTimelineEvent deletedEvent =
        timelineRepository.find("id = ?1", eventId).firstResult();
    assertNotNull(deletedEvent);
    assertTrue(deletedEvent.getIsDeleted(), "Event should be marked as deleted");
    assertEquals("testuser", deletedEvent.getDeletedBy());
    assertNotNull(deletedEvent.getDeletedAt());
  }

  @Test
  void testCreateSystemEvent_shouldCreateSystemEvent() {
    // Given - Create test customer within test transaction
    Customer testCustomer = createAndPersistTestCustomer();
    UUID testCustomerId = testCustomer.getId();

    String eventType = "CUSTOMER_STATUS_CHANGED";
    String description = "Customer status changed to ACTIVE";
    String performedBy = "SYSTEM";

    // When
    commandService.createSystemEvent(testCustomer, eventType, description, performedBy);

    // Then
    // Verify system event was persisted
    List<CustomerTimelineEvent> events =
        timelineRepository
            .find("customer.id = ?1 and eventType = ?2", testCustomerId, eventType)
            .list();
    assertEquals(1, events.size());

    CustomerTimelineEvent systemEvent = events.get(0);
    assertEquals(eventType, systemEvent.getEventType());
    assertEquals(description, systemEvent.getDescription());
    assertEquals(EventCategory.SYSTEM, systemEvent.getCategory());
    assertEquals("SYSTEM", systemEvent.getPerformedBy());
  }
}
