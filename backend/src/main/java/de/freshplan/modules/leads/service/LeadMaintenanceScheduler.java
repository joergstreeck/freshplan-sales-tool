package de.freshplan.modules.leads.service;

import de.freshplan.infrastructure.security.RlsContext;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

/**
 * Lead Maintenance Scheduler
 *
 * <p>@Scheduled Wrapper für LeadMaintenanceService (siehe ADR-005: Hybrid Test Strategy).
 *
 * <p><strong>Architektur:</strong> Scheduler-Schicht delegiert an Service-Schicht für bessere
 * Testbarkeit:
 *
 * <ul>
 *   <li>LeadMaintenanceScheduler (@Scheduled, dünn) → Integration Tests (langsam, komplett)
 *   <li>LeadMaintenanceService (Business-Logik, dick) → Service Tests (schnell, Mock)
 * </ul>
 *
 * <p><strong>Cron-Zeiten:</strong>
 *
 * <ul>
 *   <li>Job 1 - Progress Warning: 1:00 Uhr nachts (täglich)
 *   <li>Job 2 - Protection Expiry: 2:00 Uhr nachts (täglich)
 *   <li>Job 3 - Pseudonymization: 3:00 Uhr nachts (täglich)
 *   <li>Job 4 - Import Archival: 4:00 Uhr nachts (täglich)
 * </ul>
 *
 * <p><strong>Feature Flag:</strong> freshplan.lead.maintenance.enabled (default: false)
 *
 * <p><strong>Concurrent Execution:</strong> SKIP (verhindert parallele Läufe)
 */
@ApplicationScoped
public class LeadMaintenanceScheduler {

  private static final Logger LOG = Logger.getLogger(LeadMaintenanceScheduler.class);

  @Inject LeadMaintenanceService maintenanceService;

  /**
   * Job 1: Progress Warning Check (60-Day Activity Rule)
   *
   * <p>Läuft täglich um 1:00 Uhr nachts
   */
  @Scheduled(
      cron = "{freshplan.lead.maintenance.progress-warning.cron:0 0 1 * * ?}",
      identity = "lead-progress-warning-check",
      concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
  @Transactional
  @RlsContext
  public void scheduleProgressWarningCheck() {
    LOG.info("Starting scheduled Progress Warning Check");
    try {
      int warned = maintenanceService.checkProgressWarnings();
      LOG.infof("Scheduled Progress Warning Check completed: %d warnings issued", warned);
    } catch (Exception e) {
      LOG.error("Error during scheduled Progress Warning Check", e);
    }
  }

  /**
   * Job 2: Protection Expiry Check
   *
   * <p>Läuft täglich um 2:00 Uhr nachts
   */
  @Scheduled(
      cron = "{freshplan.lead.maintenance.protection-expiry.cron:0 0 2 * * ?}",
      identity = "lead-protection-expiry-check",
      concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
  @Transactional
  @RlsContext
  public void scheduleProtectionExpiryCheck() {
    LOG.info("Starting scheduled Protection Expiry Check");
    try {
      int expired = maintenanceService.checkProtectionExpiry();
      LOG.infof("Scheduled Protection Expiry Check completed: %d leads expired", expired);
    } catch (Exception e) {
      LOG.error("Error during scheduled Protection Expiry Check", e);
    }
  }

  /**
   * Job 3: DSGVO Pseudonymization
   *
   * <p>Läuft täglich um 3:00 Uhr nachts
   */
  @Scheduled(
      cron = "{freshplan.lead.maintenance.pseudonymization.cron:0 0 3 * * ?}",
      identity = "lead-pseudonymization",
      concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
  @Transactional
  @RlsContext
  public void schedulePseudonymization() {
    LOG.info("Starting scheduled DSGVO Pseudonymization");
    try {
      int pseudonymized = maintenanceService.pseudonymizeExpiredLeads();
      LOG.infof(
          "Scheduled DSGVO Pseudonymization completed: %d leads pseudonymized", pseudonymized);
    } catch (Exception e) {
      LOG.error("Error during scheduled DSGVO Pseudonymization", e);
    }
  }

  /**
   * Job 4: Import Jobs Archival
   *
   * <p>Läuft täglich um 4:00 Uhr nachts
   */
  @Scheduled(
      cron = "{freshplan.lead.maintenance.import-archival.cron:0 0 4 * * ?}",
      identity = "import-jobs-archival",
      concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
  @Transactional
  @RlsContext
  public void scheduleImportJobsArchival() {
    LOG.info("Starting scheduled Import Jobs Archival");
    try {
      int archived = maintenanceService.archiveCompletedImportJobs();
      LOG.infof("Scheduled Import Jobs Archival completed: %d jobs archived", archived);
    } catch (Exception e) {
      LOG.error("Error during scheduled Import Jobs Archival", e);
    }
  }
}
