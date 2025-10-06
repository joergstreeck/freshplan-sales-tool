package de.freshplan.modules.leads.service;

import de.freshplan.modules.leads.api.admin.dto.LeadImportRequest;
import de.freshplan.modules.leads.api.admin.dto.LeadImportRequest.ActivityImportData;
import de.freshplan.modules.leads.api.admin.dto.LeadImportRequest.LeadImportData;
import de.freshplan.modules.leads.api.admin.dto.LeadImportResponse;
import de.freshplan.modules.leads.api.admin.dto.LeadImportResponse.ImportStatistics;
import de.freshplan.modules.leads.api.admin.dto.LeadImportResponse.LeadImportResult;
import de.freshplan.modules.leads.domain.Lead;
import de.freshplan.modules.leads.domain.LeadStage;
import de.freshplan.modules.leads.domain.LeadStatus;
import de.freshplan.modules.leads.domain.Territory;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Set;

/**
 * Bestandsleads-Migration Service (Modul 08 - Admin).
 *
 * <p>Batch-Import mit Dry-Run Mode, Duplikaten-Check, Validation und Audit-Log.
 *
 * <p>Sprint 2.1.6 - User Story 1
 */
@ApplicationScoped
public class LeadImportService {

  @Inject Validator validator;

  /**
   * Import Bestandsleads (Batch).
   *
   * @param request Import-Request mit dryRun flag und Lead-Daten
   * @param currentUserId Admin-User der den Import durchführt
   * @return Detaillierte Response mit Erfolg/Fehler pro Lead
   */
  @Transactional
  public LeadImportResponse importLeads(LeadImportRequest request, String currentUserId) {
    Log.infof(
        "Starting lead import - dryRun=%s, leads=%d, user=%s",
        request.dryRun, request.leads.size(), currentUserId);

    LeadImportResponse response = new LeadImportResponse();
    response.dryRun = request.dryRun;
    response.requestHash = calculateRequestHash(request);
    response.statistics = new ImportStatistics();
    response.statistics.totalLeads = request.leads.size();
    response.statistics.importedAt = LocalDateTime.now();
    response.statistics.importedBy = currentUserId;

    // Check for duplicate import (idempotency)
    if (isDuplicateImport(response.requestHash)) {
      response.warnings.add(
          "DUPLICATE_IMPORT: This exact import was already processed. Request Hash: "
              + response.requestHash);
      return response;
    }

    // Process each lead
    for (int i = 0; i < request.leads.size(); i++) {
      LeadImportData leadData = request.leads.get(i);
      LeadImportResult result = processLead(leadData, i, request.dryRun, currentUserId);
      response.results.add(result);

      // Update statistics
      switch (result.status) {
        case "SUCCESS" -> response.statistics.successCount++;
        case "FAILED" -> response.statistics.failureCount++;
        case "DUPLICATE_WARNING" -> response.statistics.duplicateWarnings++;
        case "VALIDATION_ERROR" -> response.statistics.validationErrors++;
      }
    }

    // Record import in audit log (only for real imports)
    if (!request.dryRun && response.statistics.successCount > 0) {
      recordImportAudit(response, currentUserId);
    }

    Log.infof(
        "Lead import completed - success=%d, failed=%d, warnings=%d, validation_errors=%d",
        response.statistics.successCount,
        response.statistics.failureCount,
        response.statistics.duplicateWarnings,
        response.statistics.validationErrors);

    return response;
  }

  private LeadImportResult processLead(
      LeadImportData leadData, int index, Boolean dryRun, String currentUserId) {

    // 1. Validation
    List<String> validationErrors = validateLead(leadData);
    if (!validationErrors.isEmpty()) {
      return LeadImportResult.validationError(index, leadData.companyName, validationErrors);
    }

    // 2. Duplicate check (same logic as manual entry)
    List<String> duplicates = checkDuplicates(leadData);
    if (!duplicates.isEmpty()) {
      // MIGRATION POLICY: Duplicates are WARNINGS, not errors
      // Admin can review duplicates after import and merge manually
      // This allows importing legacy data where duplicates exist
      LeadImportResult result =
          LeadImportResult.duplicateWarning(index, leadData.companyName, duplicates);
      // NOTE: We still import with isCanonical=false to avoid unique constraint violation
      // This is a MIGRATION-SPECIFIC exception to the normal DEDUPE_POLICY
      if (!dryRun) {
        result.leadId = persistLead(leadData, currentUserId, false); // isCanonical=false
      }
      return result;
    }

    // 3. Import lead
    if (dryRun) {
      return LeadImportResult.success(index, leadData.companyName, null);
    }

    try {
      Long leadId = persistLead(leadData, currentUserId, true); // isCanonical=true
      return LeadImportResult.success(index, leadData.companyName, leadId);
    } catch (Exception e) {
      Log.errorf(e, "Failed to import lead: %s", leadData.companyName);
      return LeadImportResult.failed(index, leadData.companyName, e.getMessage());
    }
  }

  private List<String> validateLead(LeadImportData leadData) {
    List<String> errors = new ArrayList<>();

    // JSR-303 Bean Validation
    Set<ConstraintViolation<LeadImportData>> violations = validator.validate(leadData);
    for (ConstraintViolation<LeadImportData> violation : violations) {
      errors.add(violation.getPropertyPath() + ": " + violation.getMessage());
    }

    // Custom Business Rules
    if (leadData.registeredAt != null && leadData.registeredAt.isAfter(LocalDateTime.now())) {
      errors.add("registeredAt: Cannot be in the future");
    }

    // Activity dates must be chronological and not in future
    if (leadData.activities != null) {
      for (int i = 0; i < leadData.activities.size(); i++) {
        ActivityImportData activity = leadData.activities.get(i);
        if (activity.activityDate != null && activity.activityDate.isAfter(LocalDateTime.now())) {
          errors.add("activities[" + i + "].activityDate: Cannot be in the future");
        }
        if (activity.countsAsProgress == null) {
          errors.add(
              "activities[" + i + "].countsAsProgress: Must be set explicitly (not calculated)");
        }
      }
    }

    return errors;
  }

  private List<String> checkDuplicates(LeadImportData leadData) {
    // TODO: Implement fuzzy matching logic (Sprint 2.1.7)
    // For now, simple exact match on companyName + city
    List<String> duplicates = new ArrayList<>();

    @SuppressWarnings("unchecked")
    List<Lead> existingLeads =
        Lead.find(
                "companyName = ?1 AND city = ?2 AND isCanonical = true",
                leadData.companyName,
                leadData.city)
            .list();

    for (Lead lead : existingLeads) {
      duplicates.add("ID=" + lead.id + " (Owner: " + lead.ownerUserId + ")");
    }

    return duplicates;
  }

  private Long persistLead(LeadImportData leadData, String currentUserId, boolean isCanonical) {
    Lead lead = new Lead();

    // Basic Information
    lead.companyName = leadData.companyName;
    lead.contactPerson = leadData.contactPerson;
    lead.email = leadData.email;
    lead.phone = leadData.phone;
    lead.website = leadData.website;

    // Address
    lead.street = leadData.street;
    lead.postalCode = leadData.postalCode;
    lead.city = leadData.city;
    lead.countryCode = leadData.countryCode != null ? leadData.countryCode : "DE";

    // Territory (resolve from territoryCode or default)
    lead.territory = resolveTerritory(leadData.territoryCode, leadData.countryCode);

    // Business Details
    lead.businessType = leadData.businessType;
    lead.kitchenSize = leadData.kitchenSize;
    lead.employeeCount = leadData.employeeCount;
    lead.estimatedVolume = leadData.estimatedVolume;

    // Historical Dates (KRITISCH!)
    lead.registeredAt = leadData.registeredAt;
    lead.registeredAtOverrideReason = leadData.importReason;
    lead.registeredAtSetBy = currentUserId;
    lead.registeredAtSetAt = LocalDateTime.now();
    lead.registeredAtSource = "import"; // lowercase - matches DB CHECK constraint

    // Protection (MIGRATION-AUSNAHME: Bestandsleads → sofortiger Schutz!)
    lead.protectionStartAt = leadData.registeredAt;
    lead.lastActivityAt = leadData.registeredAt;

    // Ownership
    lead.ownerUserId = leadData.ownerUserId != null ? leadData.ownerUserId : currentUserId;

    // Status & Stage
    lead.status = LeadStatus.REGISTERED;
    lead.stage = LeadStage.VORMERKUNG; // Default stage

    // Canonical flag (duplicates get isCanonical=false to avoid unique constraint violation)
    lead.isCanonical = isCanonical;

    // Metadata
    lead.source = leadData.source != null ? leadData.source : "BESTAND_IMPORT";
    lead.sourceCampaign = leadData.sourceCampaign;
    lead.createdBy = currentUserId;
    lead.updatedBy = currentUserId;
    lead.createdAt = LocalDateTime.now();
    lead.updatedAt = LocalDateTime.now();

    // Persistence
    lead.persist();

    // TODO: Import activities (separate service in Sprint 2.1.7)

    return lead.id;
  }

  private Territory resolveTerritory(String territoryCode, String countryCode) {
    // Default Territory resolution
    if (territoryCode != null) {
      Territory territory = Territory.findByCode(territoryCode);
      if (territory != null) {
        return territory;
      }
    }

    // Fallback: Default territory for country
    String defaultCode = "DE".equals(countryCode) ? "DE" : "CH";
    Territory territory = Territory.findByCode(defaultCode);
    if (territory != null) {
      return territory;
    }

    // Last resort: First territory or default Germany
    Territory firstTerritory = Territory.findAll().firstResult();
    return firstTerritory != null ? firstTerritory : Territory.getDefault();
  }

  private String calculateRequestHash(LeadImportRequest request) {
    try {
      // Hash based on leads data (for idempotency)
      StringBuilder data = new StringBuilder();
      for (LeadImportData lead : request.leads) {
        data.append(lead.companyName)
            .append(lead.city)
            .append(lead.registeredAt)
            .append(lead.importReason);
      }

      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(data.toString().getBytes(StandardCharsets.UTF_8));
      return Base64.getEncoder().encodeToString(hash);
    } catch (Exception e) {
      Log.warn("Failed to calculate request hash", e);
      return "HASH_ERROR_" + System.currentTimeMillis();
    }
  }

  private boolean isDuplicateImport(String requestHash) {
    // TODO: Check against import_audit table (Sprint 2.1.7)
    // For now, always allow (no persistence)
    return false;
  }

  private void recordImportAudit(LeadImportResponse response, String currentUserId) {
    // TODO: Persist to import_audit table (Sprint 2.1.7)
    Log.infof(
        "AUDIT: leads_batch_imported - user=%s, count=%d, hash=%s",
        currentUserId, response.statistics.successCount, response.requestHash);
  }
}
