package de.freshplan.audit.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.freshplan.audit.entity.AuditLog;
import de.freshplan.audit.entity.AuditLog.*;
import de.freshplan.audit.repository.AuditRepository;
import io.quarkus.security.identity.SecurityIdentity;
import io.vertx.core.http.HttpServerRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Core Service für Audit Logging und DSGVO-Compliance. Implementiert automatisches Tracking aller
 * kritischen Operationen.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@ApplicationScoped
public class AuditService {

  private static final Logger log = LoggerFactory.getLogger(AuditService.class);

  // Constants for better maintainability
  private static final int DEFAULT_RETENTION_DAYS_7_YEARS = 2555; // 7 Jahre (365 * 7)
  private static final int RETENTION_DAYS_DATA_DELETION = 3650; // 10 Jahre
  private static final int RETENTION_DAYS_VIEW_LOGS = 365; // 1 Jahr

  @Inject AuditRepository auditRepository;

  @Inject SecurityIdentity securityIdentity;

  @Inject HttpServerRequest request;

  @Inject ObjectMapper objectMapper;

  @ConfigProperty(name = "quarkus.application.version", defaultValue = "1.0.0")
  String applicationVersion;

  @ConfigProperty(name = "audit.retention.days", defaultValue = "2555")
  int defaultRetentionDays;

  @ConfigProperty(name = "audit.async.enabled", defaultValue = "true")
  boolean asyncEnabled;

  /** Hauptmethode zum Loggen von Audit-Events. */
  @Transactional
  public AuditLog audit(
      EntityType entityType,
      UUID entityId,
      String entityName,
      AuditAction action,
      Object oldValue,
      Object newValue,
      String reason,
      String comment) {

    try {
      AuditLog auditLog = createAuditLog(entityType, entityId, entityName, action);

      enrichAuditLog(auditLog, oldValue, newValue, reason, comment);

      persistAuditLog(auditLog, action);

      handlePostPersist(auditLog, action);

      return auditLog;

    } catch (Exception e) {
      log.error("Failed to create audit log", e);
      // Audit darf niemals die Hauptoperation blockieren
      return null;
    }
  }

  private AuditLog createAuditLog(
      EntityType entityType, UUID entityId, String entityName, AuditAction action) {
    AuditLog auditLog = new AuditLog();
    auditLog.setEntityType(entityType);
    auditLog.setEntityId(entityId);
    auditLog.setEntityName(entityName);
    auditLog.setAction(action);
    auditLog.setOccurredAt(LocalDateTime.now()); // Set occurred at immediately

    extractUserInfo(auditLog);
    extractRequestInfo(auditLog);

    return auditLog;
  }

  private void enrichAuditLog(
      AuditLog auditLog, Object oldValue, Object newValue, String reason, String comment) {
    processChanges(auditLog, oldValue, newValue);

    auditLog.setReason(reason);
    auditLog.setComment(comment);
    auditLog.setApplicationVersion(applicationVersion);

    determineDsgvoRelevance(auditLog);
    if (auditLog.getIsDsgvoRelevant()) {
      setRetentionPolicy(auditLog);
    }

    auditLog.setTransactionId(generateTransactionId());
    calculateHash(auditLog);
  }

  private void persistAuditLog(AuditLog auditLog, AuditAction action) {
    if (asyncEnabled && !action.isCritical()) {
      // Async für non-kritische Events
      CompletableFuture.runAsync(
          () -> {
            try {
              auditRepository.persist(auditLog);
            } catch (Exception e) {
              log.error("Failed to persist audit log async", e);
            }
          });
    } else {
      // Sync für kritische Events
      auditRepository.persist(auditLog);
    }
  }

  private void handlePostPersist(AuditLog auditLog, AuditAction action) {
    if (action.isCritical()) {
      triggerSecurityAlert(auditLog);
    }

    log.debug(
        "Audit logged: {} {} by {}", action, auditLog.getEntityType(), auditLog.getUserName());
  }

  /** Vereinfachte Methode für einfache Aktionen. */
  public void auditSimple(
      EntityType entityType, UUID entityId, String entityName, AuditAction action) {
    audit(entityType, entityId, entityName, action, null, null, null, null);
  }

  /** Audit für CREATE Operationen. */
  public void auditCreate(
      EntityType entityType, UUID entityId, String entityName, Object newEntity) {
    audit(entityType, entityId, entityName, AuditAction.CREATE, null, newEntity, null, null);
  }

  /** Audit für UPDATE Operationen. */
  public void auditUpdate(
      EntityType entityType, UUID entityId, String entityName, Object oldEntity, Object newEntity) {
    audit(entityType, entityId, entityName, AuditAction.UPDATE, oldEntity, newEntity, null, null);
  }

  /** Audit für DELETE Operationen. */
  public void auditDelete(
      EntityType entityType,
      UUID entityId,
      String entityName,
      Object deletedEntity,
      String reason) {
    audit(entityType, entityId, entityName, AuditAction.DELETE, deletedEntity, null, reason, null);
  }

  /** Audit für VIEW/EXPORT Operationen (DSGVO relevant). */
  public void auditDataAccess(
      EntityType entityType, UUID entityId, String entityName, AuditAction action) {
    AuditLog log = audit(entityType, entityId, entityName, action, null, null, null, null);
    if (log != null) {
      log.setIsDsgvoRelevant(true);
      log.setLegalBasis(LegalBasis.LEGITIMATE_INTERESTS);
    }
  }

  /** Audit für Consent-Operationen. */
  public void auditConsent(
      UUID customerId, String customerName, boolean given, UUID consentId, String purpose) {
    AuditLog log =
        audit(
            EntityType.CUSTOMER,
            customerId,
            customerName,
            given ? AuditAction.CONSENT_GIVEN : AuditAction.CONSENT_WITHDRAWN,
            null,
            null,
            purpose,
            null);

    if (log != null) {
      log.setConsentId(consentId);
      log.setIsDsgvoRelevant(true);
      log.setLegalBasis(LegalBasis.CONSENT);
    }
  }

  /** Prüft die Hash-Chain Integrität. */
  public boolean verifyIntegrity(LocalDateTime from, LocalDateTime to) {
    return auditRepository.verifyHashChain(from, to);
  }

  /** Holt Audit-Historie für eine Entität. */
  public List<AuditLog> getEntityHistory(EntityType entityType, UUID entityId) {
    return auditRepository.findByEntity(entityType, entityId);
  }

  /** Holt User-Aktivitäten. */
  public List<AuditLog> getUserActivity(UUID userId, LocalDateTime since) {
    return auditRepository.find("userId = ?1 AND occurredAt >= ?2", userId, since).list();
  }

  /** Generiert DSGVO-Compliance Report. */
  public Map<String, Object> generateComplianceReport(LocalDateTime from, LocalDateTime to) {
    Map<String, Object> report = new HashMap<>();

    // DSGVO-relevante Events
    List<AuditLog> dsgvoEvents = auditRepository.findDsgvoRelevant(from, to);
    report.put("dsgvoEventCount", dsgvoEvents.size());

    // Nach Legal Basis gruppiert
    Map<LegalBasis, Long> byLegalBasis =
        dsgvoEvents.stream()
            .filter(a -> a.getLegalBasis() != null)
            .collect(
                java.util.stream.Collectors.groupingBy(
                    AuditLog::getLegalBasis, java.util.stream.Collectors.counting()));
    report.put("byLegalBasis", byLegalBasis);

    // Data Subject Requests
    long dataRequests =
        dsgvoEvents.stream()
            .filter(
                a ->
                    a.getAction() == AuditAction.DATA_REQUEST
                        || a.getAction() == AuditAction.DATA_DELETION)
            .count();
    report.put("dataSubjectRequests", dataRequests);

    // Consent Operations
    long consentOps =
        dsgvoEvents.stream()
            .filter(
                a ->
                    a.getAction() == AuditAction.CONSENT_GIVEN
                        || a.getAction() == AuditAction.CONSENT_WITHDRAWN)
            .count();
    report.put("consentOperations", consentOps);

    // Integrity Check
    report.put("hashChainValid", verifyIntegrity(from, to));

    return report;
  }

  // Private Helper Methods

  private void extractUserInfo(AuditLog auditLog) {
    if (securityIdentity != null && securityIdentity.getPrincipal() != null) {
      String username = securityIdentity.getPrincipal().getName();
      auditLog.setUserName(username);

      // Try to get sub claim directly as String
      if (securityIdentity.getAttributes().containsKey("sub")) {
        // Store the sub claim directly as String
        auditLog.setUserIdAsString(securityIdentity.getAttribute("sub").toString());
      } else {
        // Fallback: use username as userId
        auditLog.setUserIdAsString(username);
      }

      // Get role
      if (!securityIdentity.getRoles().isEmpty()) {
        auditLog.setUserRole(securityIdentity.getRoles().iterator().next());
      }
    } else {
      // System user
      auditLog.setUserName("SYSTEM");
      auditLog.setUserIdAsString("SYSTEM");
      auditLog.setUserRole("SYSTEM");
    }
  }

  private void extractRequestInfo(AuditLog auditLog) {
    if (request != null) {
      auditLog.setIpAddress(request.remoteAddress().hostAddress());
      auditLog.setUserAgent(request.getHeader("User-Agent"));
      auditLog.setSessionId(request.getHeader("X-Session-Id"));
      auditLog.setRequestId(request.getHeader("X-Request-Id"));
    }
  }

  private void processChanges(AuditLog auditLog, Object oldValue, Object newValue) {
    try {
      if (oldValue != null) {
        String oldJson = objectMapper.writeValueAsString(oldValue);
        auditLog.setOldValues(oldJson);
      }

      if (newValue != null) {
        String newJson = objectMapper.writeValueAsString(newValue);
        auditLog.setNewValues(newJson);
      }

      // Calculate changed fields
      if (oldValue != null && newValue != null) {
        Set<String> changedFields = detectChangedFields(oldValue, newValue);
        auditLog.setChangedFieldsFromSet(changedFields);
      }

    } catch (Exception e) {
      log.error("Failed to process changes", e);
    }
  }

  private Set<String> detectChangedFields(Object oldValue, Object newValue) {
    Set<String> changedFields = new HashSet<>();

    try {
      JsonNode oldNode = objectMapper.valueToTree(oldValue);
      JsonNode newNode = objectMapper.valueToTree(newValue);

      Iterator<String> fieldNames = oldNode.fieldNames();
      while (fieldNames.hasNext()) {
        String fieldName = fieldNames.next();
        if (!oldNode.get(fieldName).equals(newNode.get(fieldName))) {
          changedFields.add(fieldName);
        }
      }

      // Check for new fields
      Iterator<String> newFieldNames = newNode.fieldNames();
      while (newFieldNames.hasNext()) {
        String fieldName = newFieldNames.next();
        if (!oldNode.has(fieldName)) {
          changedFields.add(fieldName);
        }
      }

    } catch (Exception e) {
      log.error("Failed to detect changed fields", e);
    }

    return changedFields;
  }

  private void determineDsgvoRelevance(AuditLog auditLog) {
    // Automatisch DSGVO-relevant bei bestimmten Actions
    if (auditLog.getAction().isDsgvoRelevant()) {
      auditLog.setIsDsgvoRelevant(true);
    }

    // Auch relevant bei Customer/Contact Entities
    if (auditLog.getEntityType() == EntityType.CUSTOMER
        || auditLog.getEntityType() == EntityType.CONTACT) {
      auditLog.setIsDsgvoRelevant(true);
    }
  }

  private void setRetentionPolicy(AuditLog auditLog) {
    LocalDateTime retentionUntil;

    // Spezielle Retention-Policies je nach Action
    switch (auditLog.getAction()) {
      case DATA_DELETION:
        // Löschprotokolle länger aufbewahren (10 Jahre)
        retentionUntil = LocalDateTime.now().plusDays(RETENTION_DAYS_DATA_DELETION);
        break;
      case VIEW:
      case EXPORT:
        // View/Export-Logs kürzer (1 Jahr)
        retentionUntil = LocalDateTime.now().plusDays(RETENTION_DAYS_VIEW_LOGS);
        break;
      default:
        // Standard: 7 Jahre Aufbewahrung
        retentionUntil = LocalDateTime.now().plusDays(defaultRetentionDays);
    }

    auditLog.setRetentionUntil(retentionUntil);
  }

  private String generateTransactionId() {
    // Nutze Request-ID wenn vorhanden, sonst generiere neue
    if (request != null && request.getHeader("X-Transaction-Id") != null) {
      return request.getHeader("X-Transaction-Id");
    }
    return UUID.randomUUID().toString();
  }

  private void calculateHash(AuditLog auditLog) {
    try {
      // Hole letzten Hash mit Recovery-Strategie
      String previousHash = getPreviousHashWithRecovery();
      auditLog.setPreviousHash(previousHash);

      // Berechne Hash mit strukturiertem JSON-Format (robuster als String-Concatenation)
      Map<String, Object> contentMap = new LinkedHashMap<>();
      contentMap.put("entityType", auditLog.getEntityType());
      contentMap.put("entityId", auditLog.getEntityId());
      contentMap.put("action", auditLog.getAction());
      contentMap.put("userId", auditLog.getUserId());
      contentMap.put(
          "occurredAt",
          auditLog.getOccurredAt() != null ? auditLog.getOccurredAt().toString() : "UNKNOWN");
      contentMap.put("oldValues", auditLog.getOldValues());
      contentMap.put("newValues", auditLog.getNewValues());
      contentMap.put("previousHash", previousHash);
      String content = objectMapper.writeValueAsString(contentMap);

      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(content.getBytes(StandardCharsets.UTF_8));
      String hashHex = bytesToHex(hash);
      auditLog.setCurrentHash(hashHex);

    } catch (Exception e) {
      log.error("Failed to calculate hash", e);
      // Fallback: Setze einen Recovery-Hash
      auditLog.setPreviousHash("RECOVERY_" + System.currentTimeMillis());
      auditLog.setCurrentHash("RECOVERY_HASH_" + UUID.randomUUID());
    }
  }

  private String getPreviousHashWithRecovery() {
    try {
      Optional<AuditLog> lastEntry = auditRepository.findLastEntry();

      if (lastEntry.isPresent()) {
        String hash = lastEntry.get().getCurrentHash();
        if (hash != null && !hash.isEmpty()) {
          return hash;
        }
        // Hash ist null/leer - Recovery nötig
        log.warn("Last audit entry has no hash, starting recovery chain");
        return "RECOVERY_CHAIN_" + lastEntry.get().getId();
      }

      // Keine vorherigen Einträge - Genesis
      return "GENESIS";

    } catch (Exception e) {
      log.error("Failed to get previous hash, using recovery", e);
      return "RECOVERY_ERROR_" + System.currentTimeMillis();
    }
  }

  /**
   * Repariert eine gebrochene Hash-Chain. Sollte nur von Administratoren mit entsprechenden Rechten
   * ausgeführt werden.
   */
  public void repairHashChain(LocalDateTime from, LocalDateTime to, String reason) {
    log.warn("Starting hash chain repair from {} to {} - Reason: {}", from, to, reason);

    List<AuditLog> logs = auditRepository.findInPeriod(from, to);

    String previousHash = "REPAIR_START_" + System.currentTimeMillis();

    for (AuditLog log : logs) {
      if (log.getCurrentHash() == null || log.getCurrentHash().isEmpty()) {
        // Repariere fehlenden Hash
        log.setPreviousHash(previousHash);

        try {
          String content =
              String.format(
                  "%s|%s|%s|REPAIR", log.getEntityType(), log.getEntityId(), log.getOccurredAt());
          MessageDigest digest = MessageDigest.getInstance("SHA-256");
          byte[] hash = digest.digest(content.getBytes(StandardCharsets.UTF_8));
          String hashHex = bytesToHex(hash);
          log.setCurrentHash(hashHex);
          previousHash = hashHex;

          auditRepository.persist(log);

        } catch (Exception ex) {
          AuditService.log.error("Failed to repair hash for audit log: {}", log.getId(), ex);
        }
      } else {
        previousHash = log.getCurrentHash();
      }
    }

    // Logge die Reparatur selbst
    auditSimple(
        EntityType.SYSTEM, UUID.randomUUID(), "Hash Chain Repair", AuditAction.SYSTEM_EVENT);

    log.info("Hash chain repair completed for {} entries", logs.size());
  }

  private String bytesToHex(byte[] bytes) {
    StringBuilder result = new StringBuilder();
    for (byte b : bytes) {
      result.append(String.format("%02x", b));
    }
    return result.toString();
  }

  private void triggerSecurityAlert(AuditLog auditLog) {
    // TODO: Implement notification service integration
    log.warn(
        "SECURITY ALERT: Critical action {} on {} by {}",
        auditLog.getAction(),
        auditLog.getEntityType(),
        auditLog.getUserName());
  }
}
