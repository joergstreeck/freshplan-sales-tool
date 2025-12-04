package de.freshplan.modules.leads.api.selfservice.dto;

import java.util.UUID;

/**
 * Response für Schritt 4: Import-Ergebnis
 *
 * @since Sprint 2.1.8
 */
public record ImportExecuteResponse(
    boolean success,
    UUID importId,
    int imported,
    int skipped,
    int errors,
    String status, // COMPLETED, PENDING_APPROVAL
    String message) {

  public static ImportExecuteResponse success(
      UUID importId, int imported, int skipped, int errors) {
    return new ImportExecuteResponse(
        true,
        importId,
        imported,
        skipped,
        errors,
        "COMPLETED",
        String.format(
            "%d Leads importiert, %d übersprungen, %d Fehler", imported, skipped, errors));
  }

  public static ImportExecuteResponse pendingApproval(
      UUID importId, int totalRows, double duplicateRate) {
    return new ImportExecuteResponse(
        false,
        importId,
        0,
        0,
        0,
        "PENDING_APPROVAL",
        String.format(
            "Import wartet auf Freigabe (%.0f%% Duplikate bei %d Zeilen)",
            duplicateRate * 100, totalRows));
  }

  public static ImportExecuteResponse failed(String message) {
    return new ImportExecuteResponse(false, null, 0, 0, 0, "FAILED", message);
  }
}
