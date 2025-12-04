package de.freshplan.modules.leads.api.selfservice.dto;

import java.util.List;
import java.util.Map;

/**
 * Response für Schritt 3: Preview mit Validierung
 *
 * @since Sprint 2.1.8
 */
public record ImportPreviewResponse(
    String uploadId,
    ValidationSummary validation,
    List<PreviewRow> previewRows,
    List<ValidationError> errors,
    List<DuplicateMatch> duplicates,
    QuotaCheck quotaCheck) {

  public record ValidationSummary(int totalRows, int validRows, int errorRows, int duplicateRows) {}

  public record PreviewRow(
      int row, Map<String, String> data, String status, Long matchId // VALID, ERROR, DUPLICATE
      ) {}

  public record ValidationError(int row, String column, String message, String value) {}

  public record DuplicateMatch(
      int row, Long existingLeadId, String existingCompanyName, String type, double similarity) {}

  public record QuotaCheck(
      boolean approved,
      String message,
      long currentOpenLeads,
      int maxOpenLeads,
      long remainingCapacity) {}
}
