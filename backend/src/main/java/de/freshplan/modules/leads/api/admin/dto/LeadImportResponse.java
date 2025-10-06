package de.freshplan.modules.leads.api.admin.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Bestandsleads-Migration Import Response.
 *
 * <p>Detaillierte Erfolg/Fehler-Statistik pro Lead + Gesamt-Statistik.
 *
 * <p>Sprint 2.1.6 - User Story 1
 */
public class LeadImportResponse {

  public Boolean dryRun;
  public ImportStatistics statistics;
  public List<LeadImportResult> results = new ArrayList<>();
  public List<String> warnings = new ArrayList<>();

  /** Request hash for idempotency tracking. */
  public String requestHash;

  public static class ImportStatistics {
    public int totalLeads;
    public int successCount;
    public int failureCount;
    public int duplicateWarnings;
    public int validationErrors;
    public java.time.LocalDateTime importedAt;
    public String importedBy;
  }

  public static class LeadImportResult {
    public int index; // Array-Index in Request
    public String companyName;
    public String status; // SUCCESS, FAILED, DUPLICATE_WARNING, VALIDATION_ERROR
    public Long leadId; // nur bei SUCCESS
    public String message;
    public List<String> validationErrors;

    public static LeadImportResult success(int index, String companyName, Long leadId) {
      LeadImportResult result = new LeadImportResult();
      result.index = index;
      result.companyName = companyName;
      result.status = "SUCCESS";
      result.leadId = leadId;
      result.message = "Lead imported successfully";
      return result;
    }

    public static LeadImportResult duplicateWarning(
        int index, String companyName, List<String> duplicates) {
      LeadImportResult result = new LeadImportResult();
      result.index = index;
      result.companyName = companyName;
      result.status = "DUPLICATE_WARNING";
      result.message = "Potential duplicates found: " + String.join(", ", duplicates);
      return result;
    }

    public static LeadImportResult validationError(
        int index, String companyName, List<String> errors) {
      LeadImportResult result = new LeadImportResult();
      result.index = index;
      result.companyName = companyName;
      result.status = "VALIDATION_ERROR";
      result.message = "Validation failed";
      result.validationErrors = errors;
      return result;
    }

    public static LeadImportResult failed(int index, String companyName, String errorMessage) {
      LeadImportResult result = new LeadImportResult();
      result.index = index;
      result.companyName = companyName;
      result.status = "FAILED";
      result.message = errorMessage;
      return result;
    }
  }
}
