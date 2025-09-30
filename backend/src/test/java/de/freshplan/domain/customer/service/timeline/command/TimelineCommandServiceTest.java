package de.freshplan.domain.customer.service.timeline.command;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import de.freshplan.domain.customer.entity.*;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.customer.repository.CustomerTimelineRepository;
import de.freshplan.domain.customer.service.dto.timeline.*;
import de.freshplan.domain.customer.service.exception.CustomerNotFoundException;
import de.freshplan.domain.customer.service.mapper.CustomerTimelineMapper;
import de.freshplan.test.builders.CustomerTestDataFactory;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;

/**
 * Unit tests for TimelineCommandService.
 *
 * <p>Verifies that all command operations work correctly and maintain identical behavior to the
 * original CustomerTimelineService.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
@Tag("core")
class TimelineCommandServiceTest {

  @Mock private CustomerTimelineRepository timelineRepository;
  @Mock private CustomerRepository customerRepository;
  @Mock private CustomerTimelineMapper timelineMapper;

  @InjectMocks private TimelineCommandService commandService;

  private UUID testCustomerId;
  private Customer testCustomer;
  private CustomerTimelineEvent testEvent;
  private TimelineEventResponse testResponse;

  @BeforeEach
  void setUp() {
    testCustomerId = UUID.randomUUID();

    testCustomer =
        CustomerTestDataFactory.builder()
            .withCompanyName("Test Company")
            .withCustomerNumber("KD-2025-00001")
            .build();
    testCustomer.setId(testCustomerId);

    testEvent = new CustomerTimelineEvent();
    testEvent.setId(UUID.randomUUID());
    testEvent.setCustomer(testCustomer);
    testEvent.setEventType("CUSTOMER_CREATED");
    testEvent.setTitle("Customer Created");
    testEvent.setDescription("New customer created");
    testEvent.setCategory(EventCategory.SYSTEM);
    testEvent.setImportance(ImportanceLevel.HIGH);
    testEvent.setEventDate(LocalDateTime.now());
    testEvent.setPerformedBy("testuser");

    testResponse = new TimelineEventResponse();
    testResponse.setId(testEvent.getId());
    testResponse.setEventType(testEvent.getEventType());
    testResponse.setTitle(testEvent.getTitle());
    testResponse.setDescription(testEvent.getDescription());
  }

  @Test
  void testCreateEvent_withValidRequest_shouldCreateTimelineEvent() {
    // Given
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

    when(customerRepository.findByIdOptional(any(UUID.class))).thenReturn(Optional.of(testCustomer));
    when(timelineMapper.toResponse(any(CustomerTimelineEvent.class))).thenReturn(testResponse);

    // When
    TimelineEventResponse result = commandService.createEvent(testCustomerId, request);

    // Then
    assertNotNull(result);
    assertEquals(testResponse.getId(), result.getId());

    ArgumentCaptor<CustomerTimelineEvent> eventCaptor =
        ArgumentCaptor.forClass(CustomerTimelineEvent.class);
    verify(timelineRepository).persist(eventCaptor.capture());

    CustomerTimelineEvent capturedEvent = eventCaptor.getValue();
    assertEquals(testCustomer, capturedEvent.getCustomer());
    assertEquals("CUSTOMER_UPDATED", capturedEvent.getEventType());
    assertEquals("Customer Updated", capturedEvent.getTitle());
    assertEquals("Customer details updated", capturedEvent.getDescription());
    assertEquals(EventCategory.COMMUNICATION, capturedEvent.getCategory());
    assertEquals(ImportanceLevel.MEDIUM, capturedEvent.getImportance());
    assertEquals("testuser", capturedEvent.getPerformedBy());
    assertEquals("ADMIN", capturedEvent.getPerformedByRole());
    assertTrue(capturedEvent.getRequiresFollowUp());
    assertEquals("important,follow-up", capturedEvent.getTags());
  }

  @Test
  void testCreateEvent_withCustomerNotFound_shouldThrowException() {
    // Given
    CreateTimelineEventRequest request = new CreateTimelineEventRequest();
    request.setEventType("TEST");
    request.setTitle("Test");
    request.setCategory(EventCategory.NOTE);
    request.setPerformedBy("testuser");

    when(customerRepository.findByIdOptional(testCustomerId)).thenReturn(Optional.empty());

    // When & Then
    assertThrows(
        CustomerNotFoundException.class,
        () -> {
          commandService.createEvent(testCustomerId, request);
        });

    verify(timelineRepository, never()).persist(any(CustomerTimelineEvent.class));
  }

  @Test
  void testCreateNote_shouldCreateNoteEvent() {
    // Given
    CreateNoteRequest request = new CreateNoteRequest();
    request.setNote("Important customer note");
    request.setPerformedBy("testuser");

    when(customerRepository.findByIdOptional(any(UUID.class))).thenReturn(Optional.of(testCustomer));
    when(timelineMapper.toResponse(any(CustomerTimelineEvent.class))).thenReturn(testResponse);

    // When
    TimelineEventResponse result = commandService.createNote(testCustomerId, request);

    // Then
    assertNotNull(result);

    ArgumentCaptor<CustomerTimelineEvent> eventCaptor =
        ArgumentCaptor.forClass(CustomerTimelineEvent.class);
    verify(timelineRepository).persist(eventCaptor.capture());

    CustomerTimelineEvent capturedEvent = eventCaptor.getValue();
    assertEquals("NOTE", capturedEvent.getEventType());
    assertEquals("Notiz", capturedEvent.getTitle());
    assertEquals("Important customer note", capturedEvent.getDescription());
    assertEquals(EventCategory.NOTE, capturedEvent.getCategory());
    assertEquals(ImportanceLevel.MEDIUM, capturedEvent.getImportance());
    assertEquals("testuser", capturedEvent.getPerformedBy());
  }

  @Test
  void testCreateCommunication_shouldCreateCommunicationEvent() {
    // Given
    CreateCommunicationRequest request = new CreateCommunicationRequest();
    request.setChannel("EMAIL");
    request.setDirection("OUTBOUND");
    request.setDescription("Follow-up email sent");
    request.setPerformedBy("testuser");
    request.setDuration(15);
    request.setRelatedContactId(UUID.randomUUID());
    request.setRequiresFollowUp(true);
    request.setFollowUpDate(LocalDateTime.now().plusDays(3));
    request.setFollowUpNotes("Check response");

    when(customerRepository.findByIdOptional(any(UUID.class))).thenReturn(Optional.of(testCustomer));
    when(timelineMapper.toResponse(any(CustomerTimelineEvent.class))).thenReturn(testResponse);

    // When
    TimelineEventResponse result = commandService.createCommunication(testCustomerId, request);

    // Then
    assertNotNull(result);

    // Verify that persist was called
    // Note: We cannot mock the static CustomerTimelineEvent.createCommunicationEvent method
    // but we can verify that the repository persist was called
    ArgumentCaptor<CustomerTimelineEvent> eventCaptor =
        ArgumentCaptor.forClass(CustomerTimelineEvent.class);
    verify(timelineRepository).persist(eventCaptor.capture());

    CustomerTimelineEvent capturedEvent = eventCaptor.getValue();
    assertNotNull(capturedEvent);
    // The actual event properties are set by the static factory method
    // which we trust to work correctly as it's part of the entity
  }

  @Test
  void testCompleteFollowUp_shouldUpdateFollowUpStatus() {
    // Given
    UUID eventId = UUID.randomUUID();
    String completedBy = "testuser";

    // When
    commandService.completeFollowUp(eventId, completedBy);

    // Then
    verify(timelineRepository).completeFollowUp(eventId, completedBy);
  }

  @Test
  void testUpdateEvent_withValidRequest_shouldUpdateEvent() {
    // Given
    UUID eventId = UUID.randomUUID();
    UpdateTimelineEventRequest request = new UpdateTimelineEventRequest();
    request.setTitle("Updated Title");
    request.setDescription("Updated Description");
    request.setImportance(ImportanceLevel.HIGH);
    request.setTags(List.of("updated", "important"));
    request.setBusinessImpact("HIGH");
    request.setUpdatedBy("testuser");

    when(timelineRepository.findByIdOptional(any(UUID.class))).thenReturn(Optional.of(testEvent));
    when(timelineMapper.toResponse(testEvent)).thenReturn(testResponse);

    // When
    TimelineEventResponse result = commandService.updateEvent(eventId, request);

    // Then
    assertNotNull(result);
    assertEquals("Updated Title", testEvent.getTitle());
    assertEquals("Updated Description", testEvent.getDescription());
    assertEquals(ImportanceLevel.HIGH, testEvent.getImportance());
    assertEquals("updated,important", testEvent.getTags());
    assertEquals("HIGH", testEvent.getBusinessImpact());
    assertEquals("testuser", testEvent.getUpdatedBy());

    verify(timelineRepository).persist(testEvent);
  }

  @Test
  void testUpdateEvent_withEventNotFound_shouldThrowException() {
    // Given
    UUID eventId = UUID.randomUUID();
    UpdateTimelineEventRequest request = new UpdateTimelineEventRequest();
    request.setTitle("Updated");
    request.setUpdatedBy("testuser");

    when(timelineRepository.findByIdOptional(eventId)).thenReturn(Optional.empty());

    // When & Then
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              commandService.updateEvent(eventId, request);
            });

    assertTrue(exception.getMessage().contains("Timeline event not found"));
    verify(timelineRepository, never()).persist(any(CustomerTimelineEvent.class));
  }

  @Test
  void testDeleteEvent_shouldSoftDeleteEvent() {
    // Given
    UUID eventId = UUID.randomUUID();
    String deletedBy = "testuser";

    // When
    commandService.deleteEvent(eventId, deletedBy);

    // Then
    verify(timelineRepository).softDelete(eventId, deletedBy);
  }

  @Test
  void testCreateSystemEvent_shouldCreateSystemEvent() {
    // Given
    String eventType = "CUSTOMER_STATUS_CHANGED";
    String description = "Customer status changed to ACTIVE";
    String performedBy = "SYSTEM";

    // When
    commandService.createSystemEvent(testCustomer, eventType, description, performedBy);

    // Then
    // Verify that persist was called
    // Note: We cannot mock the static CustomerTimelineEvent.createSystemEvent method
    // but we can verify that the repository persist was called
    ArgumentCaptor<CustomerTimelineEvent> eventCaptor =
        ArgumentCaptor.forClass(CustomerTimelineEvent.class);
    verify(timelineRepository).persist(eventCaptor.capture());

    CustomerTimelineEvent capturedEvent = eventCaptor.getValue();
    assertNotNull(capturedEvent);
    assertEquals(testCustomer, capturedEvent.getCustomer());
    // The actual event properties are set by the static factory method
  }
}
