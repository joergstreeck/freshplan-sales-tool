package de.freshplan.domain.customer.service;

import de.freshplan.domain.customer.entity.ContactInteraction.InteractionType;
import de.freshplan.domain.customer.service.command.ContactEventCaptureCommandService;
import de.freshplan.domain.customer.service.dto.ContactInteractionDTO;
import de.freshplan.domain.customer.service.events.ContactDomainEvent;
import de.freshplan.domain.customer.service.events.ContactInteractionCaptured;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import java.util.UUID;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

/**
 * CQRS Facade for contact event capture functionality.
 *
 * <p>This service acts as a facade that routes capture requests to either the new CQRS-based
 * ContactEventCaptureCommandService or the legacy implementation, controlled by feature flag.
 *
 * <p>Since ContactEventCaptureService is write-only (no read operations), only a CommandService is
 * needed.
 *
 * @author FreshPlan Team
 * @since 2.0.0 (CQRS migration: Phase 13)
 */
@ApplicationScoped
public class ContactEventCaptureService {

  private static final Logger LOG = Logger.getLogger(ContactEventCaptureService.class);

  // Feature flag for CQRS migration
  @ConfigProperty(name = "features.cqrs.enabled", defaultValue = "false")
  boolean cqrsEnabled;

  // CQRS Services
  @Inject ContactEventCaptureCommandService commandService;

  // Legacy dependencies
  @Inject ContactInteractionService interactionService;

  @Inject Event<ContactInteractionCaptured> interactionCapturedEvent;

  /**
   * Capture when a contact is viewed. Routes to CQRS CommandService when enabled, otherwise uses
   * legacy implementation.
   */
  public void captureContactView(UUID contactId, String userId) {
    if (cqrsEnabled) {
      LOG.debugf("CQRS enabled - delegating captureContactView to CommandService");
      commandService.captureContactView(contactId, userId);
      return;
    }

    // Legacy implementation
    LOG.debugf("Capturing contact view for %s by user %s", contactId, userId);

    // We don't create an interaction for every view, but track it for analytics
    // This could be aggregated hourly/daily
    interactionCapturedEvent.fire(
        new ContactInteractionCaptured(contactId, "VIEW", userId, LocalDateTime.now()));
  }

  /**
   * Capture when contact details are updated. Routes to CQRS CommandService when enabled, otherwise
   * uses legacy implementation.
   */
  public void captureContactUpdate(UUID contactId, String userId, String fieldUpdated) {
    if (cqrsEnabled) {
      LOG.debugf("CQRS enabled - delegating captureContactUpdate to CommandService");
      commandService.captureContactUpdate(contactId, userId, fieldUpdated);
      return;
    }

    // Legacy implementation
    LOG.infof(
        "Capturing contact update for %s by user %s - field: %s", contactId, userId, fieldUpdated);

    ContactInteractionDTO interaction =
        ContactInteractionDTO.builder()
            .contactId(contactId)
            .type(InteractionType.NOTE)
            .timestamp(LocalDateTime.now())
            .initiatedBy("SALES")
            .subject("Kontaktdaten aktualisiert")
            .summary("Feld '" + fieldUpdated + "' wurde aktualisiert")
            .engagementScore(20) // Low engagement for data maintenance
            .createdBy(userId)
            .build();

    try {
      interactionService.createInteraction(interaction);
    } catch (Exception e) {
      LOG.error("Failed to capture contact update", e);
    }
  }

  /**
   * Capture email sent to contact. Routes to CQRS CommandService when enabled, otherwise uses
   * legacy implementation.
   */
  public void captureEmailSent(UUID contactId, String userId, String subject, String content) {
    if (cqrsEnabled) {
      LOG.debugf("CQRS enabled - delegating captureEmailSent to CommandService");
      commandService.captureEmailSent(contactId, userId, subject, content);
      return;
    }

    // Legacy implementation
    LOG.infof("Capturing email sent to %s by user %s", contactId, userId);

    ContactInteractionDTO interaction =
        ContactInteractionDTO.builder()
            .contactId(contactId)
            .type(InteractionType.EMAIL)
            .timestamp(LocalDateTime.now())
            .initiatedBy("SALES")
            .subject(subject)
            .fullContent(content)
            .summary(truncateSummary(content, 200))
            .channel("EMAIL")
            .engagementScore(50) // Medium engagement
            .createdBy(userId)
            .build();

    try {
      interactionService.createInteraction(interaction);
    } catch (Exception e) {
      LOG.error("Failed to capture email sent", e);
    }
  }

  /**
   * Capture phone call logged. Routes to CQRS CommandService when enabled, otherwise uses legacy
   * implementation.
   */
  public void capturePhoneCall(
      UUID contactId, String userId, Integer durationMinutes, String outcome, String notes) {
    if (cqrsEnabled) {
      LOG.debugf("CQRS enabled - delegating capturePhoneCall to CommandService");
      commandService.capturePhoneCall(contactId, userId, durationMinutes, outcome, notes);
      return;
    }

    // Legacy implementation
    LOG.infof(
        "Capturing phone call to %s by user %s - duration: %d min",
        contactId, userId, durationMinutes);

    // Calculate engagement based on call duration
    Integer engagementScore = calculateCallEngagement(durationMinutes);

    ContactInteractionDTO interaction =
        ContactInteractionDTO.builder()
            .contactId(contactId)
            .type(InteractionType.CALL)
            .timestamp(LocalDateTime.now())
            .initiatedBy("SALES")
            .subject("Telefonat - " + durationMinutes + " Minuten")
            .summary(notes)
            .channel("PHONE")
            .outcome(outcome)
            .engagementScore(engagementScore)
            .createdBy(userId)
            .build();

    try {
      interactionService.createInteraction(interaction);
    } catch (Exception e) {
      LOG.error("Failed to capture phone call", e);
    }
  }

  /**
   * Capture meeting scheduled. Routes to CQRS CommandService when enabled, otherwise uses legacy
   * implementation.
   */
  public void captureMeetingScheduled(
      UUID contactId, String userId, LocalDateTime meetingDate, String agenda) {
    if (cqrsEnabled) {
      LOG.debugf("CQRS enabled - delegating captureMeetingScheduled to CommandService");
      commandService.captureMeetingScheduled(contactId, userId, meetingDate, agenda);
      return;
    }

    // Legacy implementation
    LOG.infof("Capturing meeting scheduled with %s by user %s", contactId, userId);

    ContactInteractionDTO interaction =
        ContactInteractionDTO.builder()
            .contactId(contactId)
            .type(InteractionType.EVENT)
            .timestamp(LocalDateTime.now())
            .initiatedBy("SALES")
            .subject("Termin vereinbart")
            .summary(agenda)
            .nextAction("Termin wahrnehmen")
            .nextActionDate(meetingDate)
            .engagementScore(70) // High engagement for scheduling meeting
            .createdBy(userId)
            .build();

    try {
      interactionService.createInteraction(interaction);
    } catch (Exception e) {
      LOG.error("Failed to capture meeting scheduled", e);
    }
  }

  /**
   * Capture document shared. Routes to CQRS CommandService when enabled, otherwise uses legacy
   * implementation.
   */
  public void captureDocumentShared(
      UUID contactId, String userId, String documentName, String documentType) {
    if (cqrsEnabled) {
      LOG.debugf("CQRS enabled - delegating captureDocumentShared to CommandService");
      commandService.captureDocumentShared(contactId, userId, documentName, documentType);
      return;
    }

    // Legacy implementation
    LOG.infof(
        "Capturing document shared with %s by user %s - doc: %s", contactId, userId, documentName);

    ContactInteractionDTO interaction =
        ContactInteractionDTO.builder()
            .contactId(contactId)
            .type(InteractionType.DOCUMENT)
            .timestamp(LocalDateTime.now())
            .initiatedBy("SALES")
            .subject("Dokument geteilt: " + documentName)
            .summary("Dokumenttyp: " + documentType)
            .engagementScore(40) // Medium-low engagement
            .createdBy(userId)
            .build();

    try {
      interactionService.createInteraction(interaction);
    } catch (Exception e) {
      LOG.error("Failed to capture document shared", e);
    }
  }

  /**
   * Capture task created for contact. Routes to CQRS CommandService when enabled, otherwise uses
   * legacy implementation.
   */
  public void captureTaskCreated(
      UUID contactId, String userId, String taskDescription, LocalDateTime dueDate) {
    if (cqrsEnabled) {
      LOG.debugf("CQRS enabled - delegating captureTaskCreated to CommandService");
      commandService.captureTaskCreated(contactId, userId, taskDescription, dueDate);
      return;
    }

    // Legacy implementation
    LOG.infof("Capturing task created for %s by user %s", contactId, userId);

    ContactInteractionDTO interaction =
        ContactInteractionDTO.builder()
            .contactId(contactId)
            .type(InteractionType.TASK)
            .timestamp(LocalDateTime.now())
            .initiatedBy("SALES")
            .subject("Aufgabe erstellt")
            .summary(taskDescription)
            .nextActionDate(dueDate)
            .engagementScore(30) // Low-medium engagement
            .createdBy(userId)
            .build();

    try {
      interactionService.createInteraction(interaction);
    } catch (Exception e) {
      LOG.error("Failed to capture task created", e);
    }
  }

  /**
   * Listen for domain events and capture interactions. Routes to CQRS CommandService when enabled,
   * otherwise uses legacy implementation.
   *
   * <p>IMPORTANT: When CQRS is enabled, this method delegates to avoid duplicate event handling.
   */
  public void onContactEvent(@Observes ContactDomainEvent event) {
    if (cqrsEnabled) {
      // When CQRS is enabled, the CommandService handles events directly
      // We don't delegate here to avoid duplicate processing
      LOG.debugf("CQRS enabled - event handling is managed by CommandService");
      return;
    }

    // Legacy implementation
    LOG.debugf("Received contact domain event: %s", event.getEventType());

    switch (event.getEventType()) {
      case "CONTACT_VIEWED":
        captureContactView(event.getContactId(), event.getUserId());
        break;
      case "CONTACT_UPDATED":
        captureContactUpdate(
            event.getContactId(), event.getUserId(), event.getMetadata().get("field"));
        break;
      case "EMAIL_SENT":
        captureEmailSent(
            event.getContactId(),
            event.getUserId(),
            event.getMetadata().get("subject"),
            event.getMetadata().get("content"));
        break;
        // Add more event types as needed
    }
  }

  // Helper methods

  private String truncateSummary(String content, int maxLength) {
    if (content == null) return null;
    if (content.length() <= maxLength) return content;
    return content.substring(0, maxLength - 3) + "...";
  }

  private Integer calculateCallEngagement(Integer durationMinutes) {
    if (durationMinutes == null) return 50;
    if (durationMinutes < 2) return 20;
    if (durationMinutes < 5) return 40;
    if (durationMinutes < 15) return 60;
    if (durationMinutes < 30) return 80;
    return 100; // Very long calls indicate high engagement
  }

  // Event classes moved to de.freshplan.domain.customer.service.events package
  // (Sprint 2.1.7.7 - Cycle 1 fix: break service ↔ service.command circular dependency)
}
