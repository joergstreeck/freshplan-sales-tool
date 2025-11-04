package de.freshplan.domain.customer.service.events;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event fired when an interaction is captured.
 *
 * <p>Extracted from ContactEventCaptureService during Cycle 1 fix (Sprint 2.1.7.7) to break
 * circular dependency between service and service.command packages.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
public class ContactInteractionCaptured {
  private final UUID contactId;
  private final String interactionType;
  private final String userId;
  private final LocalDateTime timestamp;

  public ContactInteractionCaptured(
      UUID contactId, String interactionType, String userId, LocalDateTime timestamp) {
    this.contactId = contactId;
    this.interactionType = interactionType;
    this.userId = userId;
    this.timestamp = timestamp;
  }

  // Getters
  public UUID getContactId() {
    return contactId;
  }

  public String getInteractionType() {
    return interactionType;
  }

  public String getUserId() {
    return userId;
  }

  public LocalDateTime getTimestamp() {
    return timestamp;
  }
}
