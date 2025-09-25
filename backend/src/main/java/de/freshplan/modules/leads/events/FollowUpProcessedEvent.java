package de.freshplan.modules.leads.events;

import java.time.LocalDateTime;

/**
 * Event für verarbeitete Follow-ups Wird nach erfolgreicher T+3/T+7 Follow-up Verarbeitung gefeuert
 *
 * <p>Integration mit Cockpit-Module für Real-time Dashboard Updates
 */
public record FollowUpProcessedEvent(int t3Count, int t7Count, LocalDateTime processedAt) {

  /** Berechnet die Gesamtanzahl der verarbeiteten Follow-ups */
  public int getTotalCount() {
    return t3Count + t7Count;
  }

  @Override
  public String toString() {
    return String.format(
        "FollowUpProcessedEvent[t3=%d, t7=%d, at=%s]", t3Count, t7Count, processedAt);
  }
}
