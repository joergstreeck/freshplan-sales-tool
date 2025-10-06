package de.freshplan.modules.leads.events;

import java.time.LocalDateTime;

/**
 * Event für archivierte Import-Jobs
 *
 * <p>Wird gefeuert wenn Import-Jobs archiviert wurden (Status='ARCHIVED').
 *
 * <p>Integration mit Admin-Dashboard für Import-Audit-Trail.
 *
 * <p>Sprint 2.1.6 Phase 3 - Automated Jobs
 */
public record ImportJobsArchivedEvent(int count, LocalDateTime archivedAt) {

  /** Factory-Methode für Batch-Archival */
  public static ImportJobsArchivedEvent forBatch(int count) {
    return new ImportJobsArchivedEvent(count, LocalDateTime.now());
  }

  @Override
  public String toString() {
    return String.format("ImportJobsArchivedEvent[count=%d, archivedAt=%s]", count, archivedAt);
  }
}
