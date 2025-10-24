package de.freshplan.infrastructure.jobs;

import de.freshplan.domain.user.entity.User;
import de.freshplan.domain.user.repository.UserRepository;
import de.freshplan.modules.xentral.dto.XentralEmployeeDTO;
import de.freshplan.modules.xentral.service.XentralApiService;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sales-Rep Sync Job
 *
 * <p>Sprint 2.1.7.2 D6 - Nightly Sync Job für automatische Sales-Rep-Mapping
 *
 * <p><b>Purpose:</b> Auto-sync Xentral Employee IDs to FreshPlan Users
 *
 * <p><b>Business Logic:</b>
 *
 * <ol>
 *   <li>Fetch all sales reps from Xentral API (via XentralApiService)
 *   <li>Match by email address (email is unique in both systems)
 *   <li>Update user.xentralSalesRepId for matched users
 *   <li>Log unmatched Xentral sales reps (for manual follow-up)
 * </ol>
 *
 * <p><b>Schedule:</b> Daily at 2:00 AM (cron: "0 0 2 * * ?")
 *
 * <p><b>RLS Integration:</b> xentralSalesRepId enables filtering:
 *
 * <ul>
 *   <li>Sales users see only customers assigned to them in Xentral
 *   <li>GET /api/xentral/customers?salesRepId={xentralSalesRepId}
 *   <li>Dashboard revenue metrics filtered by sales rep
 * </ul>
 *
 * <p><b>Error Handling:</b>
 *
 * <ul>
 *   <li>If Xentral API fails → logs error, no state change
 *   <li>If email not found → logs warning (manual admin follow-up needed)
 *   <li>Transaction rollback on database errors
 * </ul>
 *
 * @author FreshPlan Team
 * @since Sprint 2.1.7.2
 * @see XentralApiService#getAllSalesReps()
 * @see User#xentralSalesRepId
 */
@ApplicationScoped
public class SalesRepSyncJob {

  private static final Logger logger = LoggerFactory.getLogger(SalesRepSyncJob.class);

  @Inject XentralApiService xentralApiService;

  @Inject UserRepository userRepository;

  /**
   * Sync Sales-Rep-IDs from Xentral to FreshPlan Users.
   *
   * <p>Runs daily at 2:00 AM (low-traffic time).
   *
   * <p><b>Matching Logic:</b> Email-based (email must match exactly)
   *
   * <p><b>Metrics Logged:</b>
   *
   * <ul>
   *   <li>syncedCount: Successfully matched and updated users
   *   <li>unmatchedCount: Xentral sales reps without matching FreshPlan user
   * </ul>
   */
  @Scheduled(cron = "0 0 2 * * ?")
  @Transactional
  public void syncSalesRepIds() {
    logger.info("🔄 Starting Sales-Rep sync from Xentral...");

    try {
      // Fetch sales reps from Xentral API (Mock-Mode aware)
      List<XentralEmployeeDTO> salesReps = xentralApiService.getAllSalesReps();
      logger.info("📥 Fetched {} sales reps from Xentral API", salesReps.size());

      int syncedCount = 0;
      int unmatchedCount = 0;

      // Match by email and update xentralSalesRepId
      for (XentralEmployeeDTO salesRep : salesReps) {
        Optional<User> userOpt = userRepository.findByEmail(salesRep.email());

        if (userOpt.isPresent()) {
          User user = userOpt.get();

          // Only update if changed (avoid unnecessary writes)
          if (!salesRep.employeeId().equals(user.getXentralSalesRepId())) {
            user.setXentralSalesRepId(salesRep.employeeId());
            userRepository.persist(user);

            logger.info(
                "✅ Synced: {} ({}) → Xentral-ID: {}",
                user.getEmail(),
                user.getFullName(),
                salesRep.employeeId());
            syncedCount++;
          } else {
            logger.debug(
                "⏭️  Skipped (no change): {} → Xentral-ID: {}",
                user.getEmail(),
                salesRep.employeeId());
          }
        } else {
          logger.warn(
              "⚠️  Unmatched Xentral Sales Rep: {} {} ({}) - Xentral-ID: {}",
              salesRep.firstName(),
              salesRep.lastName(),
              salesRep.email(),
              salesRep.employeeId());
          unmatchedCount++;
        }
      }

      logger.info(
          "✅ Sales-Rep sync completed: {} synced, {} unmatched", syncedCount, unmatchedCount);

      // Warn if too many unmatched (might indicate data issue)
      if (unmatchedCount > 0 && unmatchedCount > salesReps.size() / 2) {
        logger.warn(
            "🚨 HIGH UNMATCHED RATE: {}/{} sales reps unmatched - check email alignment!",
            unmatchedCount,
            salesReps.size());
      }

    } catch (Exception e) {
      logger.error("❌ Sales-Rep sync failed - will retry tomorrow at 2:00 AM", e);
      // Note: No rethrow - job should not fail entire scheduler
    }
  }

  /**
   * Manual trigger endpoint (for testing/admin UI).
   *
   * <p>Can be called via: POST /api/admin/jobs/sync-sales-reps
   *
   * <p>Useful for:
   *
   * <ul>
   *   <li>Initial setup (sync before 2 AM)
   *   <li>After adding new users (immediate sync)
   *   <li>Debugging/testing sync logic
   * </ul>
   */
  @Transactional
  public void triggerManualSync() {
    logger.info("🔧 Manual Sales-Rep sync triggered by admin");
    syncSalesRepIds();
  }
}
