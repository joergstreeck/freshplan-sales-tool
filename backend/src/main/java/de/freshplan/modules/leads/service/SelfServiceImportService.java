package de.freshplan.modules.leads.service;

import de.freshplan.domain.shared.LeadSource;
import de.freshplan.modules.leads.api.selfservice.dto.*;
import de.freshplan.modules.leads.api.selfservice.dto.ImportExecuteRequest.DuplicateAction;
import de.freshplan.modules.leads.api.selfservice.dto.ImportPreviewResponse.*;
import de.freshplan.modules.leads.domain.*;
import de.freshplan.modules.leads.service.FileParserService.FileParseException;
import de.freshplan.modules.leads.service.FileParserService.ParseResult;
import de.freshplan.modules.leads.service.ImportQuotaService.QuotaCheckResult;
import de.freshplan.modules.leads.service.ImportQuotaService.UserRole;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import org.jboss.logging.Logger;

/**
 * Self-Service Import Service - Sprint 2.1.8 Phase 2
 *
 * <p>Orchestriert den 4-Schritt Self-Service Lead-Import:
 *
 * <ol>
 *   <li>Upload: File parsen, Spalten extrahieren
 *   <li>Mapping: Spalten zu Lead-Feldern zuordnen
 *   <li>Preview: Validierung + Duplikat-Check + Quota-Check
 *   <li>Execute: Leads persistieren + ImportLog erstellen
 * </ol>
 *
 * @since Sprint 2.1.8
 */
@ApplicationScoped
public class SelfServiceImportService {

  private static final Logger LOG = Logger.getLogger(SelfServiceImportService.class);

  /** Duplikat-Schwelle für Approval-Workflow: 10% */
  private static final double DUPLICATE_THRESHOLD = 0.10;

  /** Upload-Cache TTL: 30 Minuten */
  private static final long UPLOAD_TTL_MS = 30 * 60 * 1000;

  /** Email-Pattern für Validierung */
  private static final Pattern EMAIL_PATTERN =
      Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

  /** In-Memory Cache für Upload-Daten (TTL: 30 min) */
  private final Map<String, UploadCache> uploadCache = new ConcurrentHashMap<>();

  @Inject FileParserService fileParserService;

  @Inject ImportQuotaService quotaService;

  @Inject EntityManager em;

  // ============================================================================
  // Schritt 1: Upload
  // ============================================================================

  /**
   * Verarbeitet einen File-Upload und gibt Spalten + Mapping-Vorschläge zurück.
   *
   * @param inputStream Datei-InputStream
   * @param fileName Original-Dateiname
   * @param fileSize Dateigröße in Bytes
   * @return Upload-Response mit Spalten und Mapping-Vorschlägen
   */
  public ImportUploadResponse uploadFile(InputStream inputStream, String fileName, long fileSize)
      throws FileParseException {

    // File parsen
    ParseResult parseResult = fileParserService.parseFile(inputStream, fileName, fileSize);

    // Upload-ID generieren
    String uploadId = UUID.randomUUID().toString();

    // Im Cache speichern
    uploadCache.put(
        uploadId,
        new UploadCache(
            parseResult.columns(),
            parseResult.rows(),
            fileName,
            parseResult.fileType(),
            System.currentTimeMillis()));

    // Lead-Felder für Frontend
    List<ImportUploadResponse.LeadFieldInfo> leadFields =
        fileParserService.getLeadFields().stream()
            .map(f -> new ImportUploadResponse.LeadFieldInfo(f.key(), f.label(), f.required()))
            .toList();

    LOG.infof(
        "Upload completed: %s (%d rows, %d columns, uploadId=%s)",
        fileName, parseResult.totalRows(), parseResult.columns().size(), uploadId);

    return new ImportUploadResponse(
        uploadId,
        parseResult.columns(),
        parseResult.suggestedMapping(),
        parseResult.totalRows(),
        parseResult.fileType(),
        parseResult.charset(),
        leadFields);
  }

  // ============================================================================
  // Schritt 2/3: Preview mit Validierung
  // ============================================================================

  /**
   * Erstellt eine Vorschau mit Validierung und Duplikat-Check.
   *
   * @param uploadId Upload-ID aus Schritt 1
   * @param mapping Spalten-Mapping (Datei-Spalte → Lead-Feld)
   * @param userId Keycloak Subject
   * @param role User-Rolle
   * @return Preview-Response mit Validierung und Quota-Info
   */
  public ImportPreviewResponse preview(
      String uploadId, Map<String, String> mapping, String userId, UserRole role) {

    UploadCache cache = getUploadCache(uploadId);

    List<PreviewRow> previewRows = new ArrayList<>();
    List<ValidationError> errors = new ArrayList<>();
    List<DuplicateMatch> duplicates = new ArrayList<>();

    int validRows = 0;
    int errorRows = 0;
    int duplicateRows = 0;

    // Nur erste 5 Zeilen für Preview
    int previewLimit = Math.min(cache.rows.size(), 5);

    for (int i = 0; i < cache.rows.size(); i++) {
      Map<String, String> row = cache.rows.get(i);
      Map<String, String> mappedData = applyMapping(row, mapping);

      // Validierung
      List<ValidationError> rowErrors = validateRow(i + 1, mappedData, mapping, row);
      if (!rowErrors.isEmpty()) {
        errors.addAll(rowErrors);
        errorRows++;
        if (i < previewLimit) {
          previewRows.add(new PreviewRow(i + 1, mappedData, "ERROR", null));
        }
        continue;
      }

      // Duplikat-Check
      DuplicateMatch duplicate = checkDuplicate(i + 1, mappedData);
      if (duplicate != null) {
        duplicates.add(duplicate);
        duplicateRows++;
        if (i < previewLimit) {
          previewRows.add(
              new PreviewRow(i + 1, mappedData, "DUPLICATE", duplicate.existingLeadId()));
        }
        continue;
      }

      validRows++;
      if (i < previewLimit) {
        previewRows.add(new PreviewRow(i + 1, mappedData, "VALID", null));
      }
    }

    // Quota-Check
    QuotaCheckResult quotaResult = quotaService.checkQuota(userId, role, validRows);
    QuotaCheck quotaCheck =
        new QuotaCheck(
            quotaResult.approved(),
            quotaResult.message(),
            quotaResult.currentOpenLeads(),
            quotaResult.maxOpenLeads(),
            quotaResult.maxOpenLeads() - quotaResult.currentOpenLeads());

    ValidationSummary validation =
        new ValidationSummary(cache.rows.size(), validRows, errorRows, duplicateRows);

    LOG.infof(
        "Preview for uploadId=%s: total=%d, valid=%d, errors=%d, duplicates=%d",
        uploadId, cache.rows.size(), validRows, errorRows, duplicateRows);

    return new ImportPreviewResponse(
        uploadId, validation, previewRows, errors, duplicates, quotaCheck);
  }

  // ============================================================================
  // Schritt 4: Execute
  // ============================================================================

  /**
   * Führt den Import aus.
   *
   * @param uploadId Upload-ID aus Schritt 1
   * @param request Import-Request mit Mapping und Optionen
   * @param userId Keycloak Subject
   * @param role User-Rolle
   * @return Import-Ergebnis
   */
  @Transactional
  @SuppressWarnings("PMD.NPathComplexity") // Import-Orchestration hat notwendige Verzweigungen
  public ImportExecuteResponse execute(
      String uploadId, ImportExecuteRequest request, String userId, UserRole role) {

    UploadCache cache = getUploadCache(uploadId);

    // 1. Validierung + Duplikat-Check (erneut, da Daten sich ändern könnten)
    List<Map<String, String>> validRows = new ArrayList<>();
    List<Map<String, String>> duplicateRows = new ArrayList<>();
    int errorCount = 0;

    for (int i = 0; i < cache.rows.size(); i++) {
      Map<String, String> row = cache.rows.get(i);
      Map<String, String> mappedData = applyMapping(row, request.mapping());

      // Validierung
      List<ValidationError> rowErrors = validateRow(i + 1, mappedData, request.mapping(), row);
      if (!rowErrors.isEmpty()) {
        if (request.ignoreErrors()) {
          errorCount++;
          continue;
        } else {
          return ImportExecuteResponse.failed(
              "Zeile " + (i + 1) + ": " + rowErrors.get(0).message());
        }
      }

      // Duplikat-Check
      DuplicateMatch duplicate = checkDuplicate(i + 1, mappedData);
      if (duplicate != null) {
        duplicateRows.add(mappedData);
        continue;
      }

      validRows.add(mappedData);
    }

    // 2. Duplikat-Rate prüfen
    double duplicateRate =
        cache.rows.size() > 0 ? (double) duplicateRows.size() / cache.rows.size() : 0;

    if (duplicateRate > DUPLICATE_THRESHOLD && role != UserRole.ADMIN) {
      // Approval erforderlich
      ImportLog importLog = createImportLog(userId, cache, 0, 0, errorCount, duplicateRate);
      importLog.markPendingApproval(BigDecimal.valueOf(duplicateRate * 100));
      importLog.persist();

      LOG.infof(
          "Import requires approval: uploadId=%s, duplicateRate=%.2f%%",
          uploadId, duplicateRate * 100);

      return ImportExecuteResponse.pendingApproval(importLog.id, cache.rows.size(), duplicateRate);
    }

    // 3. Quota-Check
    int totalToImport =
        validRows.size()
            + (request.duplicateAction() == DuplicateAction.CREATE ? duplicateRows.size() : 0);

    QuotaCheckResult quotaResult = quotaService.checkQuota(userId, role, totalToImport);
    if (!quotaResult.approved()) {
      return ImportExecuteResponse.failed(quotaResult.message());
    }

    // 4. Leads erstellen
    int imported = 0;
    int skipped = 0;

    // Valide Zeilen importieren
    for (Map<String, String> mappedData : validRows) {
      createLead(mappedData, userId, request.source());
      imported++;
    }

    // Duplikate je nach Aktion
    if (request.duplicateAction() == DuplicateAction.CREATE) {
      for (Map<String, String> mappedData : duplicateRows) {
        createLead(mappedData, userId, request.source());
        imported++;
      }
    } else {
      skipped = duplicateRows.size();
    }

    // 5. Import-Log erstellen
    ImportLog importLog =
        createImportLog(userId, cache, imported, skipped, errorCount, duplicateRate);
    importLog.markCompleted(imported, skipped, errorCount);
    importLog.persist();

    // 6. Cache aufräumen
    uploadCache.remove(uploadId);

    LOG.infof(
        "Import completed: uploadId=%s, imported=%d, skipped=%d, errors=%d",
        uploadId, imported, skipped, errorCount);

    return ImportExecuteResponse.success(importLog.id, imported, skipped, errorCount);
  }

  // ============================================================================
  // Helper Methods
  // ============================================================================

  private UploadCache getUploadCache(String uploadId) {
    UploadCache cache = uploadCache.get(uploadId);
    if (cache == null) {
      throw new IllegalArgumentException("Upload nicht gefunden oder abgelaufen: " + uploadId);
    }

    // TTL prüfen
    if (System.currentTimeMillis() - cache.timestamp > UPLOAD_TTL_MS) {
      uploadCache.remove(uploadId);
      throw new IllegalArgumentException("Upload abgelaufen (max. 30 Minuten): " + uploadId);
    }

    return cache;
  }

  private Map<String, String> applyMapping(Map<String, String> row, Map<String, String> mapping) {
    Map<String, String> mapped = new LinkedHashMap<>();
    for (Map.Entry<String, String> entry : mapping.entrySet()) {
      String fileColumn = entry.getKey();
      String leadField = entry.getValue();
      String value = row.getOrDefault(fileColumn, "");
      mapped.put(leadField, value.trim());
    }
    return mapped;
  }

  private List<ValidationError> validateRow(
      int rowNum,
      Map<String, String> mappedData,
      Map<String, String> mapping,
      Map<String, String> originalRow) {

    List<ValidationError> errors = new ArrayList<>();

    // Pflichtfeld: companyName
    String companyName = mappedData.get("companyName");
    if (companyName == null || companyName.isBlank()) {
      String sourceColumn = getSourceColumn(mapping, "companyName");
      errors.add(
          new ValidationError(
              rowNum,
              sourceColumn != null ? sourceColumn : "companyName",
              "Pflichtfeld fehlt",
              ""));
    }

    // E-Mail-Format
    String email = mappedData.get("email");
    if (email != null && !email.isBlank() && !EMAIL_PATTERN.matcher(email).matches()) {
      String sourceColumn = getSourceColumn(mapping, "email");
      errors.add(
          new ValidationError(
              rowNum,
              sourceColumn != null ? sourceColumn : "email",
              "Ungültiges E-Mail-Format",
              email));
    }

    return errors;
  }

  private String getSourceColumn(Map<String, String> mapping, String leadField) {
    for (Map.Entry<String, String> entry : mapping.entrySet()) {
      if (entry.getValue().equals(leadField)) {
        return entry.getKey();
      }
    }
    return null;
  }

  private DuplicateMatch checkDuplicate(int rowNum, Map<String, String> mappedData) {
    String companyName = mappedData.get("companyName");
    String city = mappedData.get("city");

    if (companyName == null || companyName.isBlank()) {
      return null;
    }

    // Exakte Übereinstimmung (Name + Stadt)
    @SuppressWarnings("unchecked")
    List<Lead> existingLeads =
        Lead.find(
                "LOWER(companyName) = LOWER(?1) AND (city IS NULL OR LOWER(city) = LOWER(?2))",
                companyName,
                city != null ? city : "")
            .list();

    if (!existingLeads.isEmpty()) {
      Lead existing = existingLeads.get(0);
      return new DuplicateMatch(rowNum, existing.id, existing.companyName, "HARD_COLLISION", 1.0);
    }

    return null;
  }

  private void createLead(Map<String, String> mappedData, String userId, String source) {
    Lead lead = new Lead();

    // Basis-Daten
    lead.companyName = mappedData.get("companyName");
    lead.email = mappedData.get("email");
    lead.phone = mappedData.get("phone");
    lead.city = mappedData.get("city");
    lead.postalCode = mappedData.get("postalCode");
    lead.street = mappedData.get("street");
    lead.website = mappedData.get("website");
    lead.contactPerson = mappedData.get("contactPerson");

    // Optionale Felder
    String businessType = mappedData.get("businessType");
    if (businessType != null && !businessType.isBlank()) {
      try {
        lead.businessType = de.freshplan.domain.shared.BusinessType.fromString(businessType);
      } catch (Exception e) {
        LOG.debugf("Unknown businessType: %s", businessType);
      }
    }

    // Status und Metadaten
    lead.status = LeadStatus.REGISTERED;
    lead.stage = LeadStage.VORMERKUNG;
    lead.registeredAt = LocalDateTime.now();
    lead.protectionStartAt = LocalDateTime.now();
    lead.ownerUserId = userId;
    lead.createdBy = userId;
    lead.updatedBy = userId;
    lead.createdAt = LocalDateTime.now();
    lead.updatedAt = LocalDateTime.now();

    // Quelle
    if (source != null && !source.isBlank()) {
      try {
        lead.source = LeadSource.fromString(source);
      } catch (Exception e) {
        lead.source = LeadSource.SONSTIGES;
        lead.sourceCampaign = source;
      }
    } else {
      lead.source = LeadSource.SONSTIGES;
      lead.sourceCampaign = "Self-Service Import";
    }

    // Territory (Default: DE)
    lead.territory = Territory.findByCode("DE");
    if (lead.territory == null) {
      lead.territory = Territory.findAll().firstResult();
    }

    lead.persist();
  }

  private ImportLog createImportLog(
      String userId,
      UploadCache cache,
      int imported,
      int skipped,
      int errors,
      double duplicateRate) {

    ImportLog log = new ImportLog();
    log.userId = userId;
    log.importedAt = LocalDateTime.now();
    log.totalRows = cache.rows.size();
    log.importedCount = imported;
    log.skippedCount = skipped;
    log.errorCount = errors;
    log.duplicateRate = BigDecimal.valueOf(duplicateRate * 100).setScale(2, RoundingMode.HALF_UP);
    log.fileName = cache.fileName;
    log.fileType = cache.fileType;
    log.status = ImportLog.ImportLogStatus.PENDING;

    return log;
  }

  /** Cleanup abgelaufener Uploads (aufgerufen via Scheduler) */
  public void cleanupExpiredUploads() {
    long now = System.currentTimeMillis();
    uploadCache
        .entrySet()
        .removeIf(
            entry -> {
              boolean expired = now - entry.getValue().timestamp > UPLOAD_TTL_MS;
              if (expired) {
                LOG.debugf("Removing expired upload: %s", entry.getKey());
              }
              return expired;
            });
  }

  // ============================================================================
  // Inner Classes
  // ============================================================================

  private record UploadCache(
      List<String> columns,
      List<Map<String, String>> rows,
      String fileName,
      String fileType,
      long timestamp) {}
}
