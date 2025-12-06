package de.freshplan.modules.leads.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Import Log Entity - Sprint 2.1.8 Phase 2: Lead-Import Tracking
 *
 * <p>Trackt Self-Service Lead-Imports für:
 *
 * <ul>
 *   <li>Quota-Prüfung (Imports pro Tag, offene Leads)
 *   <li>Approval-Workflow (bei >10% Duplikaten)
 *   <li>Audit-Trail und Compliance
 * </ul>
 *
 * @since Sprint 2.1.8
 */
@Entity
@Table(name = "import_logs")
public class ImportLog extends PanacheEntityBase {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  public UUID id;

  /** Keycloak Subject des importierenden Users */
  @NotNull @Size(max = 255)
  @Column(name = "user_id", nullable = false)
  public String userId;

  /** Zeitpunkt des Imports */
  @NotNull @Column(name = "imported_at", nullable = false)
  public LocalDateTime importedAt = LocalDateTime.now();

  /** Gesamtzahl der Zeilen in der Import-Datei */
  @NotNull @Column(name = "total_rows", nullable = false)
  public Integer totalRows = 0;

  /** Anzahl erfolgreich importierter Leads */
  @NotNull @Column(name = "imported_count", nullable = false)
  public Integer importedCount = 0;

  /** Anzahl übersprungener Zeilen (Duplikate etc.) */
  @NotNull @Column(name = "skipped_count", nullable = false)
  public Integer skippedCount = 0;

  /** Anzahl fehlerhafter Zeilen (Validierungsfehler) */
  @NotNull @Column(name = "error_count", nullable = false)
  public Integer errorCount = 0;

  /** Duplikatrate in Prozent (0.00-100.00) */
  @Column(name = "duplicate_rate", precision = 5, scale = 2)
  public BigDecimal duplicateRate = BigDecimal.ZERO;

  /** Import-Quelle (z.B. "MESSE_FRANKFURT_2025") */
  @Size(max = 255)
  @Column(name = "source")
  public String source;

  /** Original-Dateiname */
  @Size(max = 255)
  @Column(name = "file_name")
  public String fileName;

  /** Dateigröße in Bytes */
  @Column(name = "file_size_bytes")
  public Long fileSizeBytes;

  /** Dateiformat (CSV, XLSX, XLS) */
  @Size(max = 50)
  @Column(name = "file_type")
  public String fileType;

  /** Import-Status */
  @NotNull @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 50)
  public ImportLogStatus status = ImportLogStatus.PENDING;

  /** Keycloak Subject des Genehmigenden (bei Approval-Workflow) */
  @Size(max = 255)
  @Column(name = "approved_by")
  public String approvedBy;

  /** Zeitpunkt der Genehmigung */
  @Column(name = "approved_at")
  public LocalDateTime approvedAt;

  /** Ablehnungsgrund (bei REJECTED) */
  @Column(name = "rejection_reason", columnDefinition = "TEXT")
  public String rejectionReason;

  @NotNull @Column(name = "created_at", nullable = false)
  public LocalDateTime createdAt = LocalDateTime.now();

  @NotNull @Column(name = "updated_at", nullable = false)
  public LocalDateTime updatedAt = LocalDateTime.now();

  /** Import Status Enum */
  public enum ImportLogStatus {
    /** Upload läuft noch */
    PENDING,
    /** Import erfolgreich abgeschlossen */
    COMPLETED,
    /** Wartet auf Manager/Admin-Genehmigung (>10% Duplikate) */
    PENDING_APPROVAL,
    /** Von Manager/Admin abgelehnt */
    REJECTED
  }

  // ============================================================================
  // Static Finder Methods
  // ============================================================================

  /** Findet alle Imports eines Users */
  public static List<ImportLog> findByUserId(String userId) {
    return list("userId = ?1 ORDER BY importedAt DESC", userId);
  }

  /** Zählt Imports eines Users am heutigen Tag (für Quota-Prüfung) */
  public static long countTodayImports(String userId) {
    LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
    LocalDateTime endOfDay = startOfDay.plusDays(1);
    return count(
        "userId = ?1 AND importedAt >= ?2 AND importedAt < ?3 AND status != ?4",
        userId,
        startOfDay,
        endOfDay,
        ImportLogStatus.REJECTED);
  }

  /** Findet alle Imports mit PENDING_APPROVAL Status (für Admin-UI) */
  public static List<ImportLog> findPendingApprovals() {
    return list("status = ?1 ORDER BY importedAt ASC", ImportLogStatus.PENDING_APPROVAL);
  }

  /** Findet alle Imports eines Users mit bestimmtem Status */
  public static List<ImportLog> findByUserIdAndStatus(String userId, ImportLogStatus status) {
    return list("userId = ?1 AND status = ?2 ORDER BY importedAt DESC", userId, status);
  }

  // ============================================================================
  // Business Methods
  // ============================================================================

  /** Markiert den Import als erfolgreich abgeschlossen */
  public void markCompleted(int imported, int skipped, int errors) {
    this.status = ImportLogStatus.COMPLETED;
    this.importedCount = imported;
    this.skippedCount = skipped;
    this.errorCount = errors;
    this.updatedAt = LocalDateTime.now();
  }

  /** Markiert den Import als wartend auf Genehmigung */
  public void markPendingApproval(BigDecimal duplicateRate) {
    this.status = ImportLogStatus.PENDING_APPROVAL;
    this.duplicateRate = duplicateRate;
    this.updatedAt = LocalDateTime.now();
  }

  /** Genehmigt den Import */
  public void approve(String approvedByUserId) {
    this.status = ImportLogStatus.COMPLETED;
    this.approvedBy = approvedByUserId;
    this.approvedAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
  }

  /** Lehnt den Import ab */
  public void reject(String approvedByUserId, String reason) {
    this.status = ImportLogStatus.REJECTED;
    this.approvedBy = approvedByUserId;
    this.approvedAt = LocalDateTime.now();
    this.rejectionReason = reason;
    this.updatedAt = LocalDateTime.now();
  }

  @PrePersist
  protected void onCreate() {
    if (createdAt == null) {
      createdAt = LocalDateTime.now();
    }
    if (updatedAt == null) {
      updatedAt = LocalDateTime.now();
    }
    if (importedAt == null) {
      importedAt = LocalDateTime.now();
    }
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }
}
