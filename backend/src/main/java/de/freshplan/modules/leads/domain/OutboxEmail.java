package de.freshplan.modules.leads.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * Outbox Email Entity - Minimal Outbox Pattern (Sprint 2.1.6 Phase 3)
 *
 * <p>Transactional email queue for Automated Jobs.
 *
 * <p><strong>Design Pattern: Transactional Outbox</strong>
 *
 * <ul>
 *   <li>Jobs write emails to outbox (same transaction as business logic)
 *   <li>Email is guaranteed to be sent (or retried) even if job crashes
 *   <li>Processor reads PENDING emails and sends them (Modul 05)
 * </ul>
 *
 * <p><strong>Future (Modul 05 - Kommunikation):</strong>
 *
 * <ul>
 *   <li>EmailOutboxProcessor with @Scheduled poller
 *   <li>Template Engine for email rendering
 *   <li>Exponential-Backoff Retry-Logic
 *   <li>SMTP Integration (SendGrid/AWS SES)
 * </ul>
 *
 * <p><strong>Migration:</strong> V268 (Sprint 2.1.6 Phase 3)
 *
 * @see ADR-001 Email-Integration über Outbox-Pattern
 */
@Entity
@Table(name = "outbox_emails")
@RegisterForReflection
public class OutboxEmail extends PanacheEntityBase {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  /** Recipient email address. */
  @NotNull
  @Size(max = 255)
  @Column(name = "recipient_email", nullable = false)
  public String recipientEmail;

  /** Email subject line. */
  @NotNull
  @Size(max = 500)
  @Column(nullable = false, length = 500)
  public String subject;

  /** Email body (plain text or HTML). */
  @NotNull
  @Column(nullable = false, columnDefinition = "TEXT")
  public String body;

  /**
   * Optional template name (for Modul 05 template engine).
   *
   * <p>Examples: "lead_progress_warning", "lead_protection_expired"
   */
  @Size(max = 100)
  @Column(name = "template_name", length = 100)
  public String templateName;

  /** Optional template data (JSON for template rendering in Modul 05). */
  @Column(name = "template_data")
  @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
  public String templateData;

  /** Email status (PENDING, SENT, FAILED). */
  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 50)
  public EmailStatus status = EmailStatus.PENDING;

  /** Number of send attempts (for retry logic in Modul 05). */
  @Column(nullable = false)
  public Integer attempts = 0;

  /** Last error message (if status = FAILED). */
  @Column(name = "last_error", columnDefinition = "TEXT")
  public String lastError;

  /** Email creation timestamp. */
  @NotNull
  @Column(name = "created_at", nullable = false)
  public LocalDateTime createdAt;

  /** Email sent timestamp (set when status → SENT). */
  @Column(name = "sent_at")
  public LocalDateTime sentAt;

  /** User/System who created this email. */
  @NotNull
  @Size(max = 50)
  @Column(name = "created_by", nullable = false)
  public String createdBy;

  /** Correlation ID for tracing (e.g., "lead:12345", "import_job:678"). */
  @Size(max = 255)
  @Column(name = "correlation_id")
  public String correlationId;

  /** Email status enum. */
  public enum EmailStatus {
    PENDING,
    SENT,
    FAILED
  }

  /**
   * Static finder: Find pending emails (for processor)
   *
   * @return List of pending emails
   */
  public static java.util.List<OutboxEmail> findPending() {
    return list("status = ?1 ORDER BY createdAt ASC", EmailStatus.PENDING);
  }

  /**
   * Static finder: Find by correlation ID
   *
   * @param correlationId Correlation ID (e.g., "lead:12345")
   * @return List of emails
   */
  public static java.util.List<OutboxEmail> findByCorrelation(String correlationId) {
    return list("correlationId", correlationId);
  }

  /** Mark email as sent. */
  public void markSent() {
    this.status = EmailStatus.SENT;
    this.sentAt = LocalDateTime.now();
  }

  /** Mark email as failed with error message. */
  public void markFailed(String errorMessage) {
    this.status = EmailStatus.FAILED;
    this.lastError = errorMessage;
    this.attempts++;
  }
}
