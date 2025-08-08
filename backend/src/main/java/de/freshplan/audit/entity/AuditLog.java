package de.freshplan.audit.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.*;
import org.hibernate.annotations.UuidGenerator;

/**
 * Universelle Audit Log Entity für vollständige Nachvollziehbarkeit aller Änderungen.
 *
 * <p>Features: - DSGVO-Compliance (Art. 5, 7, 24, 30) - Revision & Forensik - Tamper Detection
 * durch Hash-Chain - Hierarchische Gruppierung von Änderungen
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@Entity
@Table(
    name = "audit_logs",
    indexes = {
      @Index(name = "idx_audit_entity", columnList = "entity_type, entity_id"),
      @Index(name = "idx_audit_user", columnList = "user_id"),
      @Index(name = "idx_audit_timestamp", columnList = "occurred_at"),
      @Index(name = "idx_audit_action", columnList = "action"),
      @Index(name = "idx_audit_compliance", columnList = "is_dsgvo_relevant"),
      @Index(name = "idx_audit_transaction", columnList = "transaction_id")
    })
public class AuditLog extends PanacheEntityBase {

  @Id
  @GeneratedValue
  @UuidGenerator
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  // What was changed
  @Column(name = "entity_type", nullable = false, length = 100)
  @Enumerated(EnumType.STRING)
  private EntityType entityType;

  @Column(name = "entity_id", nullable = false)
  private UUID entityId;

  @Column(name = "entity_name", length = 255)
  private String entityName; // Human-readable identifier

  // What action was performed
  @Column(name = "action", nullable = false, length = 50)
  @Enumerated(EnumType.STRING)
  private AuditAction action;

  // Who made the change
  @Column(name = "user_id", nullable = false, length = 100)
  private String userId; // Stored as String for Keycloak sub claim compatibility

  @Column(name = "user_name", nullable = false, length = 100)
  private String userName;

  @Column(name = "user_role", length = 50)
  private String userRole;

  // When it happened
  @Column(name = "occurred_at", nullable = false)
  private LocalDateTime occurredAt;

  // From where
  @Column(name = "ip_address", length = 45)
  private String ipAddress;

  @Column(name = "user_agent", length = 500)
  private String userAgent;

  @Column(name = "session_id", length = 100)
  private String sessionId;

  // What changed (stored as JSON strings for flexibility)
  @Column(name = "old_values", columnDefinition = "TEXT")
  private String oldValues;

  @Column(name = "new_values", columnDefinition = "TEXT")
  private String newValues;

  @Column(name = "changed_fields", length = 1000)
  private String changedFields; // Comma-separated list

  // Additional context
  @Column(name = "reason", length = 500)
  private String reason; // Why the change was made

  @Column(name = "comment", columnDefinition = "TEXT")
  private String comment; // Additional notes

  @Column(name = "request_id", length = 100)
  private String requestId; // For tracing across services

  // DSGVO specific
  @Column(name = "is_dsgvo_relevant")
  private Boolean isDsgvoRelevant = false;

  @Column(name = "legal_basis", length = 50)
  @Enumerated(EnumType.STRING)
  private LegalBasis legalBasis;

  @Column(name = "consent_id")
  private UUID consentId; // Reference to consent record

  @Column(name = "retention_until")
  private LocalDateTime retentionUntil;

  // Technical metadata
  @Column(name = "application_version", length = 50)
  private String applicationVersion;

  @Column(name = "schema_version")
  private Integer schemaVersion;

  @Column(name = "processing_time_ms")
  private Long processingTimeMs;

  // Hierarchy for grouped changes
  @Column(name = "parent_audit_id")
  private UUID parentAuditId;

  @Column(name = "transaction_id", length = 100)
  private String transactionId;

  // Security
  @Column(name = "signature", length = 500)
  private String signature; // Digital signature for tamper detection

  @Column(name = "previous_hash", length = 100)
  private String previousHash; // For hash chain

  @Column(name = "current_hash", length = 100)
  private String currentHash; // SHA-256 of this record

  // Enums
  public enum EntityType {
    CUSTOMER("Kunde"),
    CONTACT("Kontakt"),
    OPPORTUNITY("Verkaufschance"),
    ORDER("Auftrag"),
    USER("Benutzer"),
    PERMISSION("Berechtigung"),
    CONFIGURATION("Konfiguration"),
    DOCUMENT("Dokument"),
    EMAIL("E-Mail"),
    SYSTEM("System");

    private final String displayName;

    EntityType(String displayName) {
      this.displayName = displayName;
    }

    public String getDisplayName() {
      return displayName;
    }
  }

  public enum AuditAction {
    CREATE("Erstellt"),
    UPDATE("Aktualisiert"),
    DELETE("Gelöscht"),
    VIEW("Angesehen"),
    EXPORT("Exportiert"),
    IMPORT("Importiert"),
    BULK_UPDATE("Massenänderung"),
    BULK_DELETE("Massenlöschung"),
    PERMISSION_CHANGE("Berechtigung geändert"),
    LOGIN("Anmeldung"),
    LOGOUT("Abmeldung"),
    FAILED_LOGIN("Fehlgeschlagene Anmeldung"),
    PASSWORD_CHANGE("Passwort geändert"),
    CONSENT_GIVEN("Einwilligung erteilt"),
    CONSENT_WITHDRAWN("Einwilligung widerrufen"),
    DATA_REQUEST("Datenauskunft"),
    DATA_DELETION("Datenlöschung"),
    SYSTEM_EVENT("System-Ereignis");

    private final String displayName;

    AuditAction(String displayName) {
      this.displayName = displayName;
    }

    public String getDisplayName() {
      return displayName;
    }

    public boolean isCritical() {
      return this == DELETE
          || this == BULK_DELETE
          || this == PERMISSION_CHANGE
          || this == DATA_DELETION;
    }

    public boolean isDsgvoRelevant() {
      return this == CONSENT_GIVEN
          || this == CONSENT_WITHDRAWN
          || this == DATA_REQUEST
          || this == DATA_DELETION
          || this == VIEW
          || this == EXPORT;
    }
  }

  public enum LegalBasis {
    CONSENT("Einwilligung"),
    CONTRACT("Vertrag"),
    LEGAL_OBLIGATION("Rechtliche Verpflichtung"),
    VITAL_INTERESTS("Lebenswichtige Interessen"),
    PUBLIC_TASK("Öffentliche Aufgabe"),
    LEGITIMATE_INTERESTS("Berechtigte Interessen");

    private final String displayName;

    LegalBasis(String displayName) {
      this.displayName = displayName;
    }

    public String getDisplayName() {
      return displayName;
    }
  }

  @PrePersist
  protected void onCreate() {
    if (occurredAt == null) {
      occurredAt = LocalDateTime.now();
    }
    if (isDsgvoRelevant == null) {
      isDsgvoRelevant = action != null && action.isDsgvoRelevant();
    }
  }

  // Getters und Setters
  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public EntityType getEntityType() {
    return entityType;
  }

  public void setEntityType(EntityType entityType) {
    this.entityType = entityType;
  }

  public UUID getEntityId() {
    return entityId;
  }

  public void setEntityId(UUID entityId) {
    this.entityId = entityId;
  }

  public String getEntityName() {
    return entityName;
  }

  public void setEntityName(String entityName) {
    this.entityName = entityName;
  }

  public AuditAction getAction() {
    return action;
  }

  public void setAction(AuditAction action) {
    this.action = action;
  }

  public UUID getUserId() {
    // Convert String to UUID for backward compatibility
    if (userId == null) return null;
    try {
      return UUID.fromString(userId);
    } catch (IllegalArgumentException e) {
      // If not a valid UUID, return a name-based UUID
      return UUID.nameUUIDFromBytes(userId.getBytes());
    }
  }

  public void setUserId(UUID userId) {
    this.userId = userId != null ? userId.toString() : null;
  }

  public String getUserIdAsString() {
    return userId;
  }

  public void setUserIdAsString(String userId) {
    this.userId = userId;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public String getUserRole() {
    return userRole;
  }

  public void setUserRole(String userRole) {
    this.userRole = userRole;
  }

  public LocalDateTime getOccurredAt() {
    return occurredAt;
  }

  public void setOccurredAt(LocalDateTime occurredAt) {
    this.occurredAt = occurredAt;
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public void setIpAddress(String ipAddress) {
    this.ipAddress = ipAddress;
  }

  public String getUserAgent() {
    return userAgent;
  }

  public void setUserAgent(String userAgent) {
    this.userAgent = userAgent;
  }

  public String getSessionId() {
    return sessionId;
  }

  public void setSessionId(String sessionId) {
    this.sessionId = sessionId;
  }

  public String getOldValues() {
    return oldValues;
  }

  public void setOldValues(String oldValues) {
    this.oldValues = oldValues;
  }

  public String getNewValues() {
    return newValues;
  }

  public void setNewValues(String newValues) {
    this.newValues = newValues;
  }

  public String getChangedFields() {
    return changedFields;
  }

  public void setChangedFields(String changedFields) {
    this.changedFields = changedFields;
  }

  public Set<String> getChangedFieldsAsSet() {
    if (changedFields == null || changedFields.isEmpty()) {
      return Collections.emptySet();
    }
    return new HashSet<>(Arrays.asList(changedFields.split(",")));
  }

  public void setChangedFieldsFromSet(Set<String> fields) {
    if (fields == null || fields.isEmpty()) {
      this.changedFields = null;
    } else {
      this.changedFields = String.join(",", fields);
    }
  }

  public String getReason() {
    return reason;
  }

  public void setReason(String reason) {
    this.reason = reason;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public String getRequestId() {
    return requestId;
  }

  public void setRequestId(String requestId) {
    this.requestId = requestId;
  }

  public Boolean getIsDsgvoRelevant() {
    return isDsgvoRelevant;
  }

  public void setIsDsgvoRelevant(Boolean isDsgvoRelevant) {
    this.isDsgvoRelevant = isDsgvoRelevant;
  }

  public LegalBasis getLegalBasis() {
    return legalBasis;
  }

  public void setLegalBasis(LegalBasis legalBasis) {
    this.legalBasis = legalBasis;
  }

  public UUID getConsentId() {
    return consentId;
  }

  public void setConsentId(UUID consentId) {
    this.consentId = consentId;
  }

  public LocalDateTime getRetentionUntil() {
    return retentionUntil;
  }

  public void setRetentionUntil(LocalDateTime retentionUntil) {
    this.retentionUntil = retentionUntil;
  }

  public String getApplicationVersion() {
    return applicationVersion;
  }

  public void setApplicationVersion(String applicationVersion) {
    this.applicationVersion = applicationVersion;
  }

  public Integer getSchemaVersion() {
    return schemaVersion;
  }

  public void setSchemaVersion(Integer schemaVersion) {
    this.schemaVersion = schemaVersion;
  }

  public Long getProcessingTimeMs() {
    return processingTimeMs;
  }

  public void setProcessingTimeMs(Long processingTimeMs) {
    this.processingTimeMs = processingTimeMs;
  }

  public UUID getParentAuditId() {
    return parentAuditId;
  }

  public void setParentAuditId(UUID parentAuditId) {
    this.parentAuditId = parentAuditId;
  }

  public String getTransactionId() {
    return transactionId;
  }

  public void setTransactionId(String transactionId) {
    this.transactionId = transactionId;
  }

  public String getSignature() {
    return signature;
  }

  public void setSignature(String signature) {
    this.signature = signature;
  }

  public String getPreviousHash() {
    return previousHash;
  }

  public void setPreviousHash(String previousHash) {
    this.previousHash = previousHash;
  }

  public String getCurrentHash() {
    return currentHash;
  }

  public void setCurrentHash(String currentHash) {
    this.currentHash = currentHash;
  }

  // Helper Methods
  public boolean isCritical() {
    return action != null && action.isCritical();
  }

  public boolean isUserAction() {
    return action != AuditAction.SYSTEM_EVENT;
  }

  public boolean isDataModification() {
    return action == AuditAction.CREATE
        || action == AuditAction.UPDATE
        || action == AuditAction.DELETE
        || action == AuditAction.BULK_UPDATE
        || action == AuditAction.BULK_DELETE;
  }
}
