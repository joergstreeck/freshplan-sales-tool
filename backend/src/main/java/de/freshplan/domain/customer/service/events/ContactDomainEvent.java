package de.freshplan.domain.customer.service.events;

import java.util.Map;
import java.util.UUID;

/**
 * Generic contact domain event.
 *
 * <p>Extracted from ContactEventCaptureService during Cycle 1 fix (Sprint 2.1.7.7) to break
 * circular dependency between service and service.command packages.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
public class ContactDomainEvent {
  private final String eventType;
  private final UUID contactId;
  private final String userId;
  private final Map<String, String> metadata;

  public ContactDomainEvent(
      String eventType, UUID contactId, String userId, Map<String, String> metadata) {
    this.eventType = eventType;
    this.contactId = contactId;
    this.userId = userId;
    this.metadata = metadata;
  }

  // Getters
  public String getEventType() {
    return eventType;
  }

  public UUID getContactId() {
    return contactId;
  }

  public String getUserId() {
    return userId;
  }

  public Map<String, String> getMetadata() {
    return metadata;
  }
}
