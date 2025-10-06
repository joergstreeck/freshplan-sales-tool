package de.freshplan.modules.leads.events;

import java.time.LocalDateTime;

/**
 * Event für DSGVO-Pseudonymisierung
 *
 * <p>Wird gefeuert wenn Leads pseudonymisiert wurden (B2B PII-Daten entfernt).
 *
 * <p>Integration mit Compliance-Dashboard für DSGVO-Audit-Trail.
 *
 * <p>Sprint 2.1.6 Phase 3 - Automated Jobs
 */
public record LeadsPseudonymizedEvent(int count, LocalDateTime pseudonymizedAt) {

  /** Factory-Methode für Batch-Pseudonymization */
  public static LeadsPseudonymizedEvent forBatch(int count) {
    return new LeadsPseudonymizedEvent(count, LocalDateTime.now());
  }

  @Override
  public String toString() {
    return String.format(
        "LeadsPseudonymizedEvent[count=%d, pseudonymizedAt=%s]", count, pseudonymizedAt);
  }
}
