package de.freshplan.modules.leads.events;

import de.freshplan.modules.leads.domain.LeadStatus;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event für Lead-Status-Änderungen (SoT aus PR #110). Wird von LeadEventPublisher mit AFTER_COMMIT
 * Pattern publiziert.
 *
 * <p>Sprint 2.1: Lead-Management Event für Cross-Module Integration
 */
public record LeadStatusChangeEvent(
    UUID leadId,
    String companyName,
    LeadStatus oldStatus,
    LeadStatus newStatus,
    String userId,
    LocalDateTime changedAt,
    String reason) {

  /** Factory-Methode für Status-Änderungen. */
  public static LeadStatusChangeEvent of(
      UUID leadId,
      String companyName,
      LeadStatus oldStatus,
      LeadStatus newStatus,
      String userId,
      String reason) {
    return new LeadStatusChangeEvent(
        leadId, companyName, oldStatus, newStatus, userId, LocalDateTime.now(), reason);
  }

  /** Generiert deterministischen Idempotency-Key. */
  public String getIdempotencyKey() {
    String composite = leadId + "|" + oldStatus + "|" + newStatus + "|" + changedAt;
    return UUID.nameUUIDFromBytes(composite.getBytes()).toString();
  }
}
