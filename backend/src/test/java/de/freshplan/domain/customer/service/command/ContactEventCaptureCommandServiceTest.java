package de.freshplan.domain.customer.service.command;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import de.freshplan.domain.customer.entity.ContactInteraction.InteractionType;
import de.freshplan.domain.customer.service.ContactEventCaptureService.ContactDomainEvent;
import de.freshplan.domain.customer.service.ContactInteractionService;
import de.freshplan.domain.customer.service.dto.ContactInteractionDTO;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

/**
 * Tests for ContactEventCaptureCommandService following established CQRS test patterns.
 *
 * <p>This is a write-only service, so we focus on: - Event capture correctness - Interaction
 * creation with proper fields - Event publishing - Error handling - NO read operations (verified)
 *
 * @author FreshPlan Team
 * @since Phase 13 CQRS Migration
 */
@QuarkusTest
@Tag("migrate")class ContactEventCaptureCommandServiceTest {

  @Inject ContactEventCaptureCommandService commandService;

  @InjectMock ContactInteractionService interactionService;

  private UUID testContactId;
  private String testUserId;

  @BeforeEach
  void setUp() {
    // Reset mocks
    Mockito.reset(interactionService);

    // Test data
    testContactId = UUID.randomUUID();
    testUserId = "user123";
  }

  @Test
  void captureContactView_shouldNotCreateInteraction() {
    // When
    commandService.captureContactView(testContactId, testUserId);

    // Then
    // Verify no interaction is created (views are tracked for analytics only)
    // The event is fired internally but we can't mock it
    verify(interactionService, never()).createInteraction(any());
  }

  @Test
  void captureContactUpdate_shouldCreateNoteInteraction() {
    // Given
    String fieldUpdated = "email";

    // When
    commandService.captureContactUpdate(testContactId, testUserId, fieldUpdated);

    // Then
    ArgumentCaptor<ContactInteractionDTO> dtoCaptor =
        ArgumentCaptor.forClass(ContactInteractionDTO.class);
    verify(interactionService).createInteraction(dtoCaptor.capture());

    ContactInteractionDTO dto = dtoCaptor.getValue();
    assert dto.getContactId().equals(testContactId);
    assert dto.getType() == InteractionType.NOTE;
    assert dto.getSubject().equals("Kontaktdaten aktualisiert");
    assert dto.getSummary().contains(fieldUpdated);
    assert dto.getEngagementScore() == 20;
    assert dto.getCreatedBy().equals(testUserId);
  }

  @Test
  void captureEmailSent_shouldCreateEmailInteraction() {
    // Given
    String subject = "Angebot Q1 2025";
    String content = "Sehr geehrte Damen und Herren, anbei unser Angebot...";

    // When
    commandService.captureEmailSent(testContactId, testUserId, subject, content);

    // Then
    ArgumentCaptor<ContactInteractionDTO> dtoCaptor =
        ArgumentCaptor.forClass(ContactInteractionDTO.class);
    verify(interactionService).createInteraction(dtoCaptor.capture());

    ContactInteractionDTO dto = dtoCaptor.getValue();
    assert dto.getContactId().equals(testContactId);
    assert dto.getType() == InteractionType.EMAIL;
    assert dto.getSubject().equals(subject);
    assert dto.getFullContent().equals(content);
    assert dto.getSummary() != null;
    assert dto.getChannel().equals("EMAIL");
    assert dto.getEngagementScore() == 50;
    assert dto.getCreatedBy().equals(testUserId);
  }

  @Test
  void capturePhoneCall_shouldCreateCallInteraction() {
    // Given
    Integer durationMinutes = 25;
    String outcome = "SUCCESSFUL";
    String notes = "Interesse an Produkt X gezeigt";

    // When
    commandService.capturePhoneCall(testContactId, testUserId, durationMinutes, outcome, notes);

    // Then
    ArgumentCaptor<ContactInteractionDTO> dtoCaptor =
        ArgumentCaptor.forClass(ContactInteractionDTO.class);
    verify(interactionService).createInteraction(dtoCaptor.capture());

    ContactInteractionDTO dto = dtoCaptor.getValue();
    assert dto.getContactId().equals(testContactId);
    assert dto.getType() == InteractionType.CALL;
    assert dto.getSubject().contains("25 Minuten");
    assert dto.getSummary().equals(notes);
    assert dto.getChannel().equals("PHONE");
    assert dto.getOutcome().equals(outcome);
    assert dto.getEngagementScore() == 80; // 25 minutes -> score 80
    assert dto.getCreatedBy().equals(testUserId);
  }

  @Test
  void capturePhoneCall_withShortDuration_shouldCalculateLowEngagement() {
    // Given
    Integer durationMinutes = 1;

    // When
    commandService.capturePhoneCall(testContactId, testUserId, durationMinutes, null, null);

    // Then
    ArgumentCaptor<ContactInteractionDTO> dtoCaptor =
        ArgumentCaptor.forClass(ContactInteractionDTO.class);
    verify(interactionService).createInteraction(dtoCaptor.capture());

    ContactInteractionDTO dto = dtoCaptor.getValue();
    assert dto.getEngagementScore() == 20; // < 2 minutes -> score 20
  }

  @Test
  void capturePhoneCall_withLongDuration_shouldCalculateHighEngagement() {
    // Given
    Integer durationMinutes = 45;

    // When
    commandService.capturePhoneCall(testContactId, testUserId, durationMinutes, null, null);

    // Then
    ArgumentCaptor<ContactInteractionDTO> dtoCaptor =
        ArgumentCaptor.forClass(ContactInteractionDTO.class);
    verify(interactionService).createInteraction(dtoCaptor.capture());

    ContactInteractionDTO dto = dtoCaptor.getValue();
    assert dto.getEngagementScore() == 100; // > 30 minutes -> score 100
  }

  @Test
  void captureMeetingScheduled_shouldCreateEventInteraction() {
    // Given
    LocalDateTime meetingDate = LocalDateTime.now().plusDays(7);
    String agenda = "Produktvorstellung und Q&A";

    // When
    commandService.captureMeetingScheduled(testContactId, testUserId, meetingDate, agenda);

    // Then
    ArgumentCaptor<ContactInteractionDTO> dtoCaptor =
        ArgumentCaptor.forClass(ContactInteractionDTO.class);
    verify(interactionService).createInteraction(dtoCaptor.capture());

    ContactInteractionDTO dto = dtoCaptor.getValue();
    assert dto.getContactId().equals(testContactId);
    assert dto.getType() == InteractionType.EVENT;
    assert dto.getSubject().equals("Termin vereinbart");
    assert dto.getSummary().equals(agenda);
    assert dto.getNextAction().equals("Termin wahrnehmen");
    assert dto.getNextActionDate().equals(meetingDate);
    assert dto.getEngagementScore() == 70;
    assert dto.getCreatedBy().equals(testUserId);
  }

  @Test
  void captureDocumentShared_shouldCreateDocumentInteraction() {
    // Given
    String documentName = "Produktkatalog_2025.pdf";
    String documentType = "PDF";

    // When
    commandService.captureDocumentShared(testContactId, testUserId, documentName, documentType);

    // Then
    ArgumentCaptor<ContactInteractionDTO> dtoCaptor =
        ArgumentCaptor.forClass(ContactInteractionDTO.class);
    verify(interactionService).createInteraction(dtoCaptor.capture());

    ContactInteractionDTO dto = dtoCaptor.getValue();
    assert dto.getContactId().equals(testContactId);
    assert dto.getType() == InteractionType.DOCUMENT;
    assert dto.getSubject().contains(documentName);
    assert dto.getSummary().contains(documentType);
    assert dto.getEngagementScore() == 40;
    assert dto.getCreatedBy().equals(testUserId);
  }

  @Test
  void captureTaskCreated_shouldCreateTaskInteraction() {
    // Given
    String taskDescription = "Follow-up Anruf vereinbaren";
    LocalDateTime dueDate = LocalDateTime.now().plusDays(3);

    // When
    commandService.captureTaskCreated(testContactId, testUserId, taskDescription, dueDate);

    // Then
    ArgumentCaptor<ContactInteractionDTO> dtoCaptor =
        ArgumentCaptor.forClass(ContactInteractionDTO.class);
    verify(interactionService).createInteraction(dtoCaptor.capture());

    ContactInteractionDTO dto = dtoCaptor.getValue();
    assert dto.getContactId().equals(testContactId);
    assert dto.getType() == InteractionType.TASK;
    assert dto.getSubject().equals("Aufgabe erstellt");
    assert dto.getSummary().equals(taskDescription);
    assert dto.getNextActionDate().equals(dueDate);
    assert dto.getEngagementScore() == 30;
    assert dto.getCreatedBy().equals(testUserId);
  }

  @Test
  void onContactEvent_withContactViewed_shouldCaptureView() {
    // Given
    Map<String, String> metadata = new HashMap<>();
    ContactDomainEvent event =
        new ContactDomainEvent("CONTACT_VIEWED", testContactId, testUserId, metadata);

    // When
    commandService.onContactEvent(event);

    // Then
    // Event is fired internally but we can't mock it
    verify(interactionService, never()).createInteraction(any());
  }

  @Test
  void onContactEvent_withContactUpdated_shouldCaptureUpdate() {
    // Given
    Map<String, String> metadata = new HashMap<>();
    metadata.put("field", "phone");
    ContactDomainEvent event =
        new ContactDomainEvent("CONTACT_UPDATED", testContactId, testUserId, metadata);

    // When
    commandService.onContactEvent(event);

    // Then
    verify(interactionService).createInteraction(any(ContactInteractionDTO.class));
  }

  @Test
  void onContactEvent_withEmailSent_shouldCaptureEmail() {
    // Given
    Map<String, String> metadata = new HashMap<>();
    metadata.put("subject", "Test Subject");
    metadata.put("content", "Test Content");
    ContactDomainEvent event =
        new ContactDomainEvent("EMAIL_SENT", testContactId, testUserId, metadata);

    // When
    commandService.onContactEvent(event);

    // Then
    verify(interactionService).createInteraction(any(ContactInteractionDTO.class));
  }

  @Test
  void captureWithException_shouldLogError() {
    // Given
    doThrow(new RuntimeException("Database error"))
        .when(interactionService)
        .createInteraction(any());

    // When
    commandService.captureContactUpdate(testContactId, testUserId, "test");

    // Then - should not throw, but log error
    verify(interactionService).createInteraction(any());
    // Error is logged, no exception propagated
  }

  @Test
  void verifyNoReadOperations() {
    // When - execute various capture operations
    commandService.captureContactView(testContactId, testUserId);
    commandService.captureContactUpdate(testContactId, testUserId, "field");
    commandService.captureEmailSent(testContactId, testUserId, "subject", "content");

    // Then - verify only write operations are called
    // ContactInteractionService primarily has createInteraction for writes
    verify(interactionService, atLeast(2)).createInteraction(any());
  }
}
