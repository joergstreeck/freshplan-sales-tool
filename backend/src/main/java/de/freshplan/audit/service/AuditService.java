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
@Transactional
public class AuditService {

  private static final Logger log = LoggerFactory.getLogger(AuditService.class);

  @Inject AuditRepository auditRepository;

  @Inject SecurityIdentity securityIdentity;

  @Inject HttpServerRequest request;

  @Inject ObjectMapper objectMapper;

  @ConfigProperty(name = "quarkus.application.version", defaultValue = "1.0.0")
  String applicationVersion;

  @ConfigProperty(name = "audit.retention.days", defaultValue = "2555") // 7 Jahre default
  int defaultRetentionDays;

  @ConfigProperty(name = "audit.async.enabled", defaultValue = "true")
  boolean asyncEnabled;

  /** Hauptmethode zum Loggen von Audit-Events. */
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
      AuditLog auditLog = new AuditLog();

      // Entity Info
      auditLog.setEntityType(entityType);
      auditLog.setEntityId(entityId);
      auditLog.setEntityName(entityName);
      auditLog.setAction(action);

      // User Info
      extractUserInfo(auditLog);

      // Request Info
      extractRequestInfo(auditLog);

      // Changes
      processChanges(auditLog, oldValue, newValue);

      // Context
      auditLog.setReason(reason);
      auditLog.setComment(comment);
      auditLog.setApplicationVersion(applicationVersion);

      // DSGVO
      determineDsgvoRelevance(auditLog);
      if (auditLog.getIsDsgvoRelevant()) {
        setRetentionPolicy(auditLog);
      }

      // Transaction ID für Gruppierung
      auditLog.setTransactionId(generateTransactionId());

      // Hash für Tamper Detection
      calculateHash(auditLog);

      // Persist
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

      // Alert bei kritischen Events
      if (action.isCritical()) {
        triggerSecurityAlert(auditLog);
      }

      log.debug("Audit logged: {} {} by {}", action, entityType, auditLog.getUserName());

      return auditLog;

    } catch (Exception e) {
      log.error("Failed to create audit log", e);
      // Audit darf niemals die Hauptoperation blockieren
      return null;
    }
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

      // Try to get UUID from claims
      if (securityIdentity.getAttributes().containsKey("sub")) {
        try {
          auditLog.setUserId(UUID.fromString(securityIdentity.getAttribute("sub").toString()));
        } catch (Exception e) {
          // Fallback: use hash of username as UUID
          auditLog.setUserId(UUID.nameUUIDFromBytes(username.getBytes()));
        }
      } else {
        auditLog.setUserId(UUID.nameUUIDFromBytes(username.getBytes()));
      }

      // Get role
      if (!securityIdentity.getRoles().isEmpty()) {
        auditLog.setUserRole(securityIdentity.getRoles().iterator().next());
      }
    } else {
      // System user
      auditLog.setUserName("SYSTEM");
      auditLog.setUserId(UUID.nameUUIDFromBytes("SYSTEM".getBytes()));
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
    // Standard: 7 Jahre Aufbewahrung
    LocalDateTime retentionUntil = LocalDateTime.now().plusDays(defaultRetentionDays);

    // Spezielle Fälle
    if (auditLog.getAction() == AuditAction.DATA_DELETION) {
      // Löschprotokolle länger aufbewahren (10 Jahre)
      retentionUntil = LocalDateTime.now().plusYears(10);
    } else if (auditLog.getAction() == AuditAction.VIEW) {
      // View-Logs kürzer (1 Jahr)
      retentionUntil = LocalDateTime.now().plusYears(1);
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
      // Hole letzten Hash
      Optional<AuditLog> lastEntry = auditRepository.findLastEntry();
      String previousHash = lastEntry.map(AuditLog::getCurrentHash).orElse("GENESIS");
      auditLog.setPreviousHash(previousHash);

      // Berechne Hash für diesen Eintrag
      String content =
          String.format(
              "%s|%s|%s|%s|%s|%s|%s|%s",
              auditLog.getEntityType(),
              auditLog.getEntityId(),
              auditLog.getAction(),
              auditLog.getUserId(),
              auditLog.getOccurredAt(),
              auditLog.getOldValues() != null ? auditLog.getOldValues() : "",
              auditLog.getNewValues() != null ? auditLog.getNewValues() : "",
              previousHash);

      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(content.getBytes(StandardCharsets.UTF_8));
      String hashHex = bytesToHex(hash);
      auditLog.setCurrentHash(hashHex);

    } catch (Exception e) {
      log.error("Failed to calculate hash", e);
    }
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
