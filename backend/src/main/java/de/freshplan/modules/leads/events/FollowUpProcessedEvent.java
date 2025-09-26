package de.freshplan.modules.leads.events;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event für verarbeitete Follow-ups Wird nach erfolgreicher T+3/T+7 Follow-up Verarbeitung gefeuert
 *
 * <p>Integration mit Cockpit-Module für Real-time Dashboard Updates
 *
 * <p>Sprint 2.1.1 P0 HOTFIX - Erweitert für Dashboard Integration
 */
public record FollowUpProcessedEvent(
    UUID leadId,
    String followUpType,
    int t3Count,
    int t7Count,
    boolean success,
    String userId,
    String responseTime,
    LocalDateTime processedAt) {

  /** Legacy-Konstruktor für Backward Compatibility. */
  public FollowUpProcessedEvent(int t3Count, int t7Count, LocalDateTime processedAt) {
    this(null, "BATCH", t3Count, t7Count, true, null, null, processedAt);
  }

  /** Factory-Methode für T+3 Follow-up Events. */
  public static FollowUpProcessedEvent forT3(UUID leadId, String userId, boolean success) {
    return new FollowUpProcessedEvent(
        leadId, "T3", 1, 0, success, userId, null, LocalDateTime.now());
  }

  /** Factory-Methode für T+7 Follow-up Events. */
  public static FollowUpProcessedEvent forT7(UUID leadId, String userId, boolean success) {
    return new FollowUpProcessedEvent(
        leadId, "T7", 0, 1, success, userId, null, LocalDateTime.now());
  }

  /** Factory-Methode für Batch Follow-up Events. */
  public static FollowUpProcessedEvent forBatch(String userId, int t3Count, int t7Count) {
    return new FollowUpProcessedEvent(
        null, "BATCH", t3Count, t7Count, t3Count + t7Count > 0, userId, null, LocalDateTime.now());
  }

  /** Berechnet die Gesamtanzahl der verarbeiteten Follow-ups */
  public int getTotalCount() {
    return t3Count + t7Count;
  }

  // Record-Klassen generieren automatisch Accessor-Methoden
  // Die expliziten Getter wurden entfernt, da sie redundant sind

  @Override
  public String toString() {
    return String.format(
        "FollowUpProcessedEvent[lead=%s, type=%s, t3=%d, t7=%d, success=%s, at=%s]",
        leadId, followUpType, t3Count, t7Count, success, processedAt);
  }
}
