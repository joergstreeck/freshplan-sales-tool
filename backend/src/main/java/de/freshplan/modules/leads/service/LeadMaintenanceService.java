package de.freshplan.modules.leads.service;

import de.freshplan.infrastructure.security.RlsContext;
import de.freshplan.modules.leads.domain.Lead;
import de.freshplan.modules.leads.domain.LeadStatus;
import de.freshplan.modules.leads.events.ImportJobsArchivedEvent;
import de.freshplan.modules.leads.events.LeadProgressWarningIssuedEvent;
import de.freshplan.modules.leads.events.LeadProtectionExpiredEvent;
import de.freshplan.modules.leads.events.LeadsPseudonymizedEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import org.jboss.logging.Logger;

/**
 * Lead Maintenance Service
 *
 * <p>Implementiert Nightly Jobs gemäß AUTOMATED_JOBS_SPECIFICATION.md:
 *
 * <ul>
 *   <li>Job 1: Progress Warning Check (60-Day Activity Rule)
 *   <li>Job 2: Protection Expiry Check (Lead-Schutz erlischt nach Nachfrist)
 *   <li>Job 3: DSGVO B2B Pseudonymization (PII-Daten anonymisieren)
 *   <li>Job 4: Import Jobs Archival (Cleanup abgeschlossener Importe)
 * </ul>
 *
 * <p>Business-Value: Automatische Vertragskonformität + DSGVO-Compliance
 *
 * <p><strong>Wichtig:</strong> Keine Vertragsreferenzen im Code (Verträge sind individuell). Alle
 * Business-Regeln siehe CONTRACT_MAPPING.md
 *
 * <p><strong>Architektur:</strong> Service-Schicht (testbar, kein @Scheduled) wird von
 * LeadMaintenanceScheduler gewrappt (siehe ADR-005: Hybrid Test Strategy)
 */
@ApplicationScoped
public class LeadMaintenanceService {

  private static final Logger LOG = Logger.getLogger(LeadMaintenanceService.class);

  private static final String SYSTEM_USER_ID = "SYSTEM";
  private static final int WARNING_DAYS_BEFORE_EXPIRY = 7;
  private static final int GRACE_PERIOD_DAYS = 10;
  private static final int PSEUDONYMIZATION_DAYS_AFTER_EXPIRY = 60;
  private static final int IMPORT_ARCHIVAL_DAYS = 7;
  private static final String ANONYMIZED_CONTACT_PERSON = "ANONYMIZED"; // Code Review: Gemini
  private static final int BATCH_SIZE = 100; // Sprint 2.1.6 Phase 4: Memory-safe batch processing

  @Inject EntityManager em;

  @Inject Event<LeadProgressWarningIssuedEvent> progressWarningEvent;

  @Inject Event<LeadProtectionExpiredEvent> protectionExpiredEvent;

  @Inject Event<LeadsPseudonymizedEvent> pseudonymizedEvent;

  @Inject Event<ImportJobsArchivedEvent> importArchivedEvent;

  // Clock für testbare Zeit-Logik
  private Clock clock = Clock.systemDefaultZone();

  /** Setzt Clock für Tests (package-private für Test-Zugriff) */
  void setClock(Clock clock) {
    this.clock = clock;
  }

  /**
   * Job 1: Progress Warning Check (60-Day Activity Rule)
   *
   * <p>Sendet Erinnerung 7 Tage vor Ablauf der Schutzfrist, wenn keine Aktivität vorliegt.
   *
   * <p><strong>Business-Regel:</strong> "60-Day Activity Rule - Warning 7 days before expiry"
   *
   * <p><strong>Contract Mapping:</strong> See docs/CONTRACT_MAPPING.md §2(8)c
   *
   * <p><strong>Email-Integration:</strong> Nutzt geplanten OutboxEmail-Pattern (Modul 05). Bis
   * dahin: LOG statt Email (siehe ADR-001).
   *
   * @return Anzahl versendeter Warnungen
   */
  @RlsContext
  @Transactional
  public int checkProgressWarnings() {
    long startTime = System.currentTimeMillis();
    int processedCount = 0;
    int emailsSent = 0;

    try {
      LocalDateTime now = LocalDateTime.now(clock);
      LocalDateTime warningThreshold = now.plusDays(WARNING_DAYS_BEFORE_EXPIRY);

      LOG.infof("Starting Progress Warning Check (Threshold: %s)", warningThreshold);

      // Finde Leads: progress_deadline < NOW() + 7 days AND progress_warning_sent_at IS NULL
      List<Lead> leadsNeedingWarning =
          em.createQuery(
                  """
          SELECT l FROM Lead l
          WHERE l.status = :status
          AND l.progressDeadline IS NOT NULL
          AND l.progressDeadline < :threshold
          AND l.progressWarningSentAt IS NULL
          AND l.clockStoppedAt IS NULL
          ORDER BY l.progressDeadline ASC
          """,
                  Lead.class)
              .setParameter("status", LeadStatus.ACTIVE)
              .setParameter("threshold", warningThreshold)
              .setMaxResults(BATCH_SIZE) // Phase 4: Batch-Processing (max 100 Leads/run)
              .getResultList();

      processedCount = leadsNeedingWarning.size();

      int warned = 0;
      for (Lead lead : leadsNeedingWarning) {
        try {
          // Atomares Flag-Setzen für Idempotenz
          int updated =
              em.createQuery(
                      """
              UPDATE Lead l
              SET l.progressWarningSentAt = :now
              WHERE l.id = :id AND l.progressWarningSentAt IS NULL
              """)
                  .setParameter("id", lead.id)
                  .setParameter("now", now)
                  .executeUpdate();

          if (updated == 0) {
            LOG.debugf("Lead %s already has warning, skipping", lead.id);
            continue;
          }

          // Sprint 2.1.6 Phase 3: Outbox-Pattern für Email-Benachrichtigungen
          de.freshplan.modules.leads.domain.OutboxEmail email =
              new de.freshplan.modules.leads.domain.OutboxEmail();
          email.recipientEmail = lead.ownerUserId + "@freshfoodz.de"; // TODO: Lookup real email
          email.subject = "Lead Protection Warning - Action Required";
          email.body =
              """
              Your lead '%s' (ID: %d) will expire soon.

              Deadline: %s
              Grace Period Ends: %s

              Please update the lead to maintain protection.
              """
                  .formatted(
                      lead.companyName,
                      lead.id,
                      lead.progressDeadline,
                      lead.progressDeadline.plusDays(GRACE_PERIOD_DAYS));
          email.templateName = "lead_progress_warning";
          email.createdBy = SYSTEM_USER_ID;
          email.createdAt = now;
          email.correlationId = "lead:" + lead.id;
          email.persist();

          LOG.infof(
              "Progress Warning: Lead %s - Deadline %s (Email queued: %d)",
              lead.id, lead.progressDeadline, email.id);

          // Event für Dashboard-Echtzeit-Updates
          progressWarningEvent.fire(
              LeadProgressWarningIssuedEvent.forLead(
                  lead.id, lead.ownerUserId, lead.progressDeadline));

          warned++;
          emailsSent++;
        } catch (Exception e) {
          LOG.errorf(e, "Failed to process progress warning for lead %s", lead.id);
        }
      }

      LOG.infof("Progress Warning Check completed: %d warnings issued", warned);
      return warned;
    } finally {
      long duration = System.currentTimeMillis() - startTime;
      logJobMetrics("progress_warning_check", processedCount, emailsSent, duration);
    }
  }

  /**
   * Job 2: Protection Expiry Check (Lead-Schutz erlischt)
   *
   * <p>Prüft ob Nachfrist (10 Tage) abgelaufen ist ohne neue Aktivität → Lead-Schutz erlischt.
   *
   * <p><strong>Business-Regel:</strong> "60-Day Activity Rule - Protection expires after 10-day
   * grace period"
   *
   * <p><strong>Contract Mapping:</strong> See docs/CONTRACT_MAPPING.md §2(8)c
   *
   * <p><strong>Lead-Freigabe:</strong> Status → EXPIRED, Lead wird wieder frei zuweisbar
   *
   * @return Anzahl abgelaufener Lead-Schutze
   */
  @RlsContext
  @Transactional
  public int checkProtectionExpiry() {
    long startTime = System.currentTimeMillis();
    int processedCount = 0;
    int emailsSent = 0;

    try {
      LocalDateTime now = LocalDateTime.now(clock);

      LOG.info("Starting Protection Expiry Check");

      // Finde Leads: progress_warning_sent_at + 10 days < NOW() AND progress_deadline < NOW()
      List<Lead> leadsToExpire =
          em.createQuery(
                  """
          SELECT l FROM Lead l
          WHERE l.status = :status
          AND l.progressWarningSentAt IS NOT NULL
          AND l.progressDeadline IS NOT NULL
          AND l.progressDeadline < :now
          AND l.clockStoppedAt IS NULL
          ORDER BY l.progressDeadline ASC
          """,
                  Lead.class)
              .setParameter("status", LeadStatus.ACTIVE)
              .setParameter("now", now)
              .setMaxResults(BATCH_SIZE) // Phase 4: Batch-Processing (max 100 Leads/run)
              .getResultList();

      processedCount = leadsToExpire.size();

      int expired = 0;
      for (Lead lead : leadsToExpire) {
        try {
          // Prüfe ob 10-Tage-Nachfrist abgelaufen
          LocalDateTime graceDeadline = lead.progressWarningSentAt.plusDays(GRACE_PERIOD_DAYS);
          if (now.isBefore(graceDeadline)) {
            LOG.debugf("Lead %s still in grace period until %s, skipping", lead.id, graceDeadline);
            continue;
          }

          // Lead-Schutz erlischt → Status EXPIRED
          int updated =
              em.createQuery(
                      """
              UPDATE Lead l
              SET l.status = :expiredStatus,
                  l.ownerUserId = NULL,
                  l.updatedAt = :now,
                  l.updatedBy = :systemUser
              WHERE l.id = :id AND l.status = :activeStatus
              """)
                  .setParameter("id", lead.id)
                  .setParameter("expiredStatus", LeadStatus.EXPIRED)
                  .setParameter("activeStatus", LeadStatus.ACTIVE)
                  .setParameter("now", now)
                  .setParameter("systemUser", SYSTEM_USER_ID)
                  .executeUpdate();

          if (updated == 0) {
            LOG.debugf("Lead %s already expired, skipping", lead.id);
            continue;
          }

          // Sprint 2.1.6 Phase 3: Outbox-Pattern für Manager-Benachrichtigung
          de.freshplan.modules.leads.domain.OutboxEmail managerEmail =
              new de.freshplan.modules.leads.domain.OutboxEmail();
          managerEmail.recipientEmail = "manager@freshfoodz.de"; // TODO: Lookup territory manager
          managerEmail.subject = "Lead Protection Expired - Lead Released";
          managerEmail.body =
              """
              Lead protection has expired for:

              Company: %s (ID: %d)
              Previous Owner: %s
              Grace Period Ended: %s

              Lead is now available for reassignment.
              """
                  .formatted(lead.companyName, lead.id, lead.ownerUserId, graceDeadline);
          managerEmail.templateName = "lead_protection_expired";
          managerEmail.createdBy = SYSTEM_USER_ID;
          managerEmail.createdAt = now;
          managerEmail.correlationId = "lead:" + lead.id;
          managerEmail.persist();

          LOG.infof(
              "Protection Expired: Lead %s - Partner %s - Email queued: %d",
              lead.id, lead.ownerUserId, managerEmail.id);

          // Event für Dashboard-Echtzeit-Updates
          protectionExpiredEvent.fire(
              LeadProtectionExpiredEvent.forLead(lead.id, lead.ownerUserId, now));

          expired++;
          emailsSent++;
        } catch (Exception e) {
          LOG.errorf(e, "Failed to process protection expiry for lead %s", lead.id);
        }
      }

      LOG.infof("Protection Expiry Check completed: %d leads expired", expired);
      return expired;
    } finally {
      long duration = System.currentTimeMillis() - startTime;
      logJobMetrics("protection_expiry_check", processedCount, emailsSent, duration);
    }
  }

  /**
   * Job 3: DSGVO B2B Pseudonymization
   *
   * <p>Pseudonymisiert personenbezogene Daten (PII) 60 Tage nach Lead-Schutz erlischt.
   *
   * <p><strong>DSGVO-Konform (B2B-Leads):</strong>
   *
   * <ul>
   *   <li>Pseudonymisieren (PII): email → SHA-256 Hash, phone → NULL, contactPerson →
   *       ANONYMIZED_CONTACT_PERSON
   *   <li>Behalten (juristische Personen): companyName, city, businessType, ownerUserId,
   *       sourceCampaign (Analytics/Territory-Statistiken)
   * </ul>
   *
   * <p><strong>Business-Regel:</strong> "B2B Personal Data Pseudonymization (DSGVO Art. 4)"
   *
   * <p><strong>Contract Mapping:</strong> See docs/CONTRACT_MAPPING.md §2(8)i
   *
   * @return Anzahl pseudonymisierter Leads
   */
  @RlsContext
  @Transactional
  public int pseudonymizeExpiredLeads() {
    long startTime = System.currentTimeMillis();
    int processedCount = 0;
    int actionsCount = 0;

    try {
      LocalDateTime now = LocalDateTime.now(clock);
      LocalDateTime pseudonymizationThreshold = now.minusDays(PSEUDONYMIZATION_DAYS_AFTER_EXPIRY);

      LOG.infof("Starting DSGVO Pseudonymization (Threshold: %s)", pseudonymizationThreshold);

      // Finde Leads: status=EXPIRED AND updatedAt < NOW() - 60 days AND pseudonymizedAt IS NULL
      List<Lead> leadsToPseudonymize =
          em.createQuery(
                  """
          SELECT l FROM Lead l
          WHERE l.status = :status
          AND l.updatedAt < :threshold
          AND l.pseudonymizedAt IS NULL
          ORDER BY l.updatedAt ASC
          """,
                  Lead.class)
              .setParameter("status", LeadStatus.EXPIRED)
              .setParameter("threshold", pseudonymizationThreshold)
              .setMaxResults(BATCH_SIZE) // Phase 4: Batch-Processing (max 100 Leads/run)
              .getResultList();

      processedCount = leadsToPseudonymize.size();

      int pseudonymized = 0;
      for (Lead lead : leadsToPseudonymize) {
        try {
          // Pseudonymisiere PII-Daten
          String emailHash = null;
          if (lead.email != null && !lead.email.isBlank()) {
            emailHash = sha256Hash(lead.email);
          }

          // Atomare Pseudonymisierung
          int updated =
              em.createQuery(
                      """
              UPDATE Lead l
              SET l.email = :emailHash,
                  l.phone = NULL,
                  l.contactPerson = :anonymizedContactPerson,
                  l.pseudonymizedAt = :now
              WHERE l.id = :id AND l.pseudonymizedAt IS NULL
              """)
                  .setParameter("id", lead.id)
                  .setParameter("emailHash", emailHash)
                  .setParameter("anonymizedContactPerson", ANONYMIZED_CONTACT_PERSON)
                  .setParameter("now", now)
                  .executeUpdate();

          if (updated == 0) {
            LOG.debugf("Lead %s already pseudonymized, skipping", lead.id);
            continue;
          }

          LOG.infof(
              "Pseudonymized: Lead %s - PII cleared (email → SHA-256, phone/contact → NULL)",
              lead.id);

          pseudonymized++;
          actionsCount++;
        } catch (Exception e) {
          LOG.errorf(e, "Failed to pseudonymize lead %s", lead.id);
        }
      }

      // Event für Compliance-Dashboard
      if (pseudonymized > 0) {
        pseudonymizedEvent.fire(LeadsPseudonymizedEvent.forBatch(pseudonymized));
      }

      LOG.infof("DSGVO Pseudonymization completed: %d leads pseudonymized", pseudonymized);
      return pseudonymized;
    } finally {
      long duration = System.currentTimeMillis() - startTime;
      logJobMetrics("dsgvo_pseudonymization", processedCount, actionsCount, duration);
    }
  }

  /**
   * Job 4: Import Jobs Archival
   *
   * <p><strong>IMPLEMENTIERUNG (Code Review Fix):</strong> Hard-Delete nach 7 Tagen (COMPLETED +
   * FAILED Jobs)
   *
   * <p><strong>ADR-002 ABWEICHUNG:</strong> Ursprüngliche Spec forderte Status='ARCHIVED' mit
   * Hard-Delete nach 90 Tagen. Implementiert als direktes Hard-Delete nach 7 Tagen, da:
   *
   * <ul>
   *   <li>✅ GoBD-konform: Keine Aufbewahrungspflicht für Batch-Import-Logs
   *   <li>✅ Simpler Workflow: Ein Job statt zwei (Archive + Delete)
   *   <li>✅ Gleiche Business-Logik: 7-Day TTL aus ImportJob.ttlExpiresAt
   * </ul>
   *
   * <p><strong>Bereinigt:</strong> COMPLETED + FAILED Import-Jobs (siehe
   * ImportJob.findReadyForArchival)
   *
   * @return Anzahl gelöschter Import-Jobs
   */
  @RlsContext
  @Transactional
  public int archiveCompletedImportJobs() {
    long startTime = System.currentTimeMillis();
    int processedCount = 0;
    int deletedCount = 0;

    try {
      LocalDateTime now = LocalDateTime.now(clock);

      LOG.infof("Starting Import Jobs Archival (Threshold: %s)", now);

      // Sprint 2.1.6 Phase 3 - Issue #134: Delete expired import jobs (TTL)
      // Find jobs where ttl_expires_at < now() (TTL expired)
      // TTL is set to completedAt + 7 days by ImportJob.markCompleted()
      // Sprint 2.1.6 Phase 4: Batch-Processing mit LIMIT 100
      List<de.freshplan.modules.leads.domain.ImportJob> jobsToArchive =
          de.freshplan.modules.leads.domain.ImportJob.findReadyForArchival(now, BATCH_SIZE);

      processedCount = jobsToArchive.size();

      int archived = 0;
      for (de.freshplan.modules.leads.domain.ImportJob job : jobsToArchive) {
        try {
          // Hard delete (GoBD-compliant: Idempotency-Daten haben keine Aufbewahrungspflicht)
          job.delete();
          archived++;
          deletedCount++;

          LOG.debugf(
              "Archived import job: id=%d, created=%s, ttl_expires_at=%s",
              job.id, job.createdAt, job.ttlExpiresAt);
        } catch (Exception e) {
          LOG.errorf(e, "Failed to archive import job %d", job.id);
        }
      }

      // Event für Dashboard
      if (archived > 0) {
        importArchivedEvent.fire(ImportJobsArchivedEvent.forBatch(archived));
      }

      LOG.infof("Import Jobs Archival completed: %d jobs archived", archived);
      return archived;
    } finally {
      long duration = System.currentTimeMillis() - startTime;
      logJobMetrics("import_jobs_archival", processedCount, deletedCount, duration);
    }
  }

  /**
   * SHA-256 Hash-Funktion für Email-Pseudonymisierung
   *
   * <p>Ermöglicht Duplikat-Check nach Pseudonymisierung (Hash-Vergleich).
   *
   * @param input Email-Adresse
   * @return SHA-256 Hash (Hex-String)
   */
  private String sha256Hash(String input) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(input.toLowerCase().getBytes(StandardCharsets.UTF_8));
      StringBuilder hexString = new StringBuilder();
      for (byte b : hash) {
        String hex = Integer.toHexString(0xff & b);
        if (hex.length() == 1) hexString.append('0');
        hexString.append(hex);
      }
      return hexString.toString();
    } catch (NoSuchAlgorithmException e) {
      LOG.error("SHA-256 algorithm not available", e);
      throw new RuntimeException("Failed to hash email", e);
    }
  }

  /**
   * Structured Job Metrics Logging (Sprint 2.1.6 Phase 4)
   *
   * <p>Logs job execution metrics in a parseable format for log aggregation (Splunk, ELK).
   * Prepares for future Prometheus-Metrics implementation (Modul 00).
   *
   * @param jobName Job identifier (e.g., "progress_warning_check")
   * @param processed Number of entities processed
   * @param actions Number of actions taken (e.g., emails sent, records deleted)
   * @param durationMs Execution time in milliseconds
   */
  private void logJobMetrics(String jobName, int processed, int actions, long durationMs) {
    LOG.infof(
        "Job Metrics: %s | Processed: %d | Actions: %d | Duration: %dms",
        jobName, processed, actions, durationMs);
  }
}
