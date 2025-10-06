package de.freshplan.modules.leads.events;

import java.time.LocalDateTime;

/**
 * Event für abgelaufenen Lead-Schutz
 *
 * <p>Wird gefeuert wenn die 10-Tage-Nachfrist abgelaufen ist und der Lead-Schutz erlischt.
 *
 * <p>Integration mit Cockpit-Module für Real-time Dashboard Updates.
 *
 * <p>Sprint 2.1.6 Phase 3 - Automated Jobs
 */
public record LeadProtectionExpiredEvent(
    Long leadId, String previouslyAssignedTo, LocalDateTime expiredAt) {

  /** Factory-Methode für Lead-Protection-Expiry */
  public static LeadProtectionExpiredEvent forLead(
      Long leadId, String previouslyAssignedTo, LocalDateTime expiredAt) {
    return new LeadProtectionExpiredEvent(leadId, previouslyAssignedTo, expiredAt);
  }

  @Override
  public String toString() {
    return String.format(
        "LeadProtectionExpiredEvent[lead=%s, previouslyAssignedTo=%s, expiredAt=%s]",
        leadId, previouslyAssignedTo, expiredAt);
  }
}
