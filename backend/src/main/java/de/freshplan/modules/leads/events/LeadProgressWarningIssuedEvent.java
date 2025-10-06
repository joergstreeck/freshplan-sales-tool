package de.freshplan.modules.leads.events;

import java.time.LocalDateTime;

/**
 * Event für ausgestellte Progress-Warnungen
 *
 * <p>Wird gefeuert wenn ein Lead eine 60-Day-Activity-Warnung erhält (7 Tage vor Ablauf).
 *
 * <p>Integration mit Cockpit-Module für Real-time Dashboard Updates.
 *
 * <p>Sprint 2.1.6 Phase 3 - Automated Jobs
 */
public record LeadProgressWarningIssuedEvent(
    Long leadId, String assignedTo, LocalDateTime progressDeadline, LocalDateTime issuedAt) {

  /** Factory-Methode für Lead-Progress-Warning */
  public static LeadProgressWarningIssuedEvent forLead(
      Long leadId, String assignedTo, LocalDateTime progressDeadline) {
    return new LeadProgressWarningIssuedEvent(
        leadId, assignedTo, progressDeadline, LocalDateTime.now());
  }

  @Override
  public String toString() {
    return String.format(
        "LeadProgressWarningIssuedEvent[lead=%s, assignedTo=%s, deadline=%s, at=%s]",
        leadId, assignedTo, progressDeadline, issuedAt);
  }
}
