package de.freshplan.modules.leads.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * Import Job Entity for Batch Import Idempotency (Sprint 2.1.6 Phase 3 - Issue #134)
 *
 * <p>Tracks batch import requests to prevent duplicate imports on retry.
 *
 * <p><strong>Idempotency Strategy:</strong>
 *
 * <ul>
 *   <li>Client sends Idempotency-Key header (UUID v4 recommended)
 *   <li>Server stores request + result in import_jobs
 *   <li>On retry: Return cached result instead of re-importing
 *   <li>Fallback: request_fingerprint (SHA-256 hash of request body)
 * </ul>
 *
 * <p><strong>TTL:</strong> 7 days after completion → Cleanup by Nightly Job
 *
 * <p><strong>Migration:</strong> V262 (created in Sprint 2.1.6 Phase 2)
 *
 * @see de.freshplan.modules.leads.service.LeadImportService
 * @see de.freshplan.modules.leads.service.LeadMaintenanceService#archiveCompletedImportJobs()
 */
@Entity
@Table(name = "import_jobs")
@RegisterForReflection
public class ImportJob extends PanacheEntityBase {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  /** Client-provided unique key for idempotency (Header: Idempotency-Key). */
  @NotNull @Size(max = 255)
  @Column(name = "idempotency_key", unique = true, nullable = false)
  public String idempotencyKey;

  /** SHA-256 hash of request data (fallback check if no idempotency key provided). */
  @NotNull @Column(name = "request_fingerprint", nullable = false, columnDefinition = "TEXT")
  public String requestFingerprint;

  /** Import job status. */
  @NotNull @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 50)
  public ImportStatus status;

  /** Total number of leads in import request. */
  @Column(name = "total_leads", nullable = false)
  public Integer totalLeads = 0;

  /** Number of successfully imported leads. */
  @Column(name = "success_count", nullable = false)
  public Integer successCount = 0;

  /** Number of failed imports. */
  @Column(name = "failure_count", nullable = false)
  public Integer failureCount = 0;

  /** Number of duplicate warnings (soft duplicates). */
  @Column(name = "duplicate_warnings", nullable = false)
  public Integer duplicateWarnings = 0;

  /**
   * JSONB with detailed import statistics: {successCount, failureCount, results[]}.
   *
   * <p>Note: PostgreSQL JSONB column requires explicit cast. Use @JdbcTypeCode for proper mapping.
   */
  @Column(name = "result_summary")
  @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
  public String resultSummary;

  /** User who initiated the import. */
  @NotNull @Size(max = 50)
  @Column(name = "created_by", nullable = false)
  public String createdBy;

  /** Import job creation timestamp. */
  @NotNull @Column(name = "created_at", nullable = false)
  public LocalDateTime createdAt;

  /** Import job completion timestamp (set when status = COMPLETED or FAILED). */
  @Column(name = "completed_at")
  public LocalDateTime completedAt;

  /** TTL expiry timestamp (7 days after completion). */
  @NotNull @Column(name = "ttl_expires_at", nullable = false)
  public LocalDateTime ttlExpiresAt;

  /** Import job status enum. */
  public enum ImportStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED
  }

  /**
   * Static finder: Find by idempotency key
   *
   * @param idempotencyKey Client-provided idempotency key
   * @return ImportJob or null
   */
  public static ImportJob findByIdempotencyKey(String idempotencyKey) {
    return find("idempotency_key", idempotencyKey).firstResult();
  }

  /**
   * Static finder: Find by request fingerprint (fallback)
   *
   * @param fingerprint SHA-256 hash of request body
   * @return ImportJob or null
   */
  public static ImportJob findByFingerprint(String fingerprint) {
    return find("request_fingerprint", fingerprint).firstResult();
  }

  /**
   * Static finder: Find completed jobs ready for archival (TTL expired)
   *
   * @param threshold Archival threshold timestamp
   * @return List of import jobs
   */
  public static java.util.List<ImportJob> findReadyForArchival(LocalDateTime threshold) {
    // Code Review (Gemini): Berücksichtige auch FAILED Jobs für Cleanup
    return list(
        "status IN (?1, ?2) AND ttlExpiresAt < ?3 ORDER BY ttlExpiresAt ASC",
        ImportStatus.COMPLETED,
        ImportStatus.FAILED,
        threshold);
  }

  /** Mark job as completed and set TTL. */
  public void markCompleted() {
    this.status = ImportStatus.COMPLETED;
    this.completedAt = LocalDateTime.now();
    this.ttlExpiresAt = this.completedAt.plusDays(7);
  }

  /** Mark job as failed and set TTL (Code Review: Gemini - Konsistenz mit markCompleted). */
  public void markFailed() {
    this.status = ImportStatus.FAILED;
    this.completedAt = LocalDateTime.now();
    this.ttlExpiresAt = this.completedAt.plusDays(7); // Auch FAILED Jobs nach 7 Tagen bereinigen
  }
}
