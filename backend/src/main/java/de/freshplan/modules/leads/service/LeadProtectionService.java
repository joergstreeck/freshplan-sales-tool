package de.freshplan.modules.leads.service;

import de.freshplan.modules.leads.domain.Lead;
import de.freshplan.modules.leads.domain.LeadStage;
import de.freshplan.modules.leads.domain.LeadStatus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import org.jboss.logging.Logger;

/**
 * Service handling lead protection rules and state transitions. Implements the 6M+60T+10T
 * protection system with stop-the-clock feature.
 */
@ApplicationScoped
public class LeadProtectionService {

  private static final Logger LOG = Logger.getLogger(LeadProtectionService.class);

  @Inject UserLeadSettingsService settingsService;

  /**
   * Check if a status transition is valid according to the state machine.
   *
   * <p>Valid transitions: - REGISTERED → ACTIVE, QUALIFIED, EXPIRED, DELETED - ACTIVE → QUALIFIED,
   * REMINDER, EXPIRED, DELETED - REMINDER → ACTIVE (via activity), GRACE_PERIOD, EXPIRED, DELETED -
   * GRACE_PERIOD → ACTIVE (via activity), EXPIRED, DELETED - QUALIFIED → CONVERTED, LOST, DELETED -
   * EXPIRED → ACTIVE (reactivation), DELETED - Any → DELETED (soft delete)
   */
  public boolean canTransitionStatus(Lead lead, LeadStatus newStatus, String userId) {
    LeadStatus currentStatus = lead.status;

    // Deletion is always allowed
    if (newStatus == LeadStatus.DELETED) {
      return true;
    }

    // Clock stopped leads can only be deleted or have clock resumed
    if (lead.clockStoppedAt != null && newStatus != currentStatus) {
      LOG.warnf("Cannot change status of lead %s while clock is stopped", lead.id);
      return false;
    }

    switch (currentStatus) {
      case REGISTERED:
        return newStatus == LeadStatus.ACTIVE
            || newStatus == LeadStatus.QUALIFIED
            || newStatus == LeadStatus.EXPIRED;

      case ACTIVE:
        return newStatus == LeadStatus.QUALIFIED
            || newStatus == LeadStatus.REMINDER
            || newStatus == LeadStatus.EXPIRED;

      case REMINDER:
        return newStatus == LeadStatus.ACTIVE
            || newStatus == LeadStatus.GRACE_PERIOD
            || newStatus == LeadStatus.EXPIRED
            || newStatus == LeadStatus.QUALIFIED;

      case GRACE_PERIOD:
        return newStatus == LeadStatus.ACTIVE
            || newStatus == LeadStatus.EXPIRED
            || newStatus == LeadStatus.QUALIFIED;

      case QUALIFIED:
        return newStatus == LeadStatus.CONVERTED || newStatus == LeadStatus.LOST;

      case EXPIRED:
        // Only reactivation to ACTIVE allowed (requires special permission)
        if (newStatus == LeadStatus.ACTIVE) {
          var settings = settingsService.getOrCreateForUser(userId);
          return settings.canOverrideProtection;
        }
        return false;

      case CONVERTED:
      case LOST:
        // Terminal states - no transitions except deletion
        return false;

      case DELETED:
        // Cannot undelete
        return false;

      default:
        LOG.errorf("Unknown lead status: %s", currentStatus);
        return false;
    }
  }

  /**
   * Calculate remaining protection time for a lead.
   *
   * @return remaining days of protection, or -1 if expired
   */
  public int getRemainingProtectionDays(Lead lead) {
    if (lead.clockStoppedAt != null) {
      return Integer.MAX_VALUE; // Clock stopped = infinite protection
    }

    if (lead.status == LeadStatus.EXPIRED) {
      return -1;
    }

    LocalDateTime now = LocalDateTime.now();
    LocalDateTime protectionEnd = lead.protectionStartAt.plusMonths(lead.protectionMonths);

    if (now.isAfter(protectionEnd)) {
      return -1; // Protection expired
    }

    // Calculate remaining days
    long remainingDays = java.time.Duration.between(now, protectionEnd).toDays();
    return (int) remainingDays;
  }

  /**
   * Calculate days until next status transition for a lead.
   *
   * @return days until next transition, or -1 if no transition scheduled
   */
  public int getDaysUntilNextTransition(Lead lead) {
    if (lead.clockStoppedAt != null) {
      return -1; // No transitions while clock is stopped
    }

    LocalDateTime now = LocalDateTime.now();

    switch (lead.status) {
      case ACTIVE:
      case REGISTERED:
        // Calculate days until reminder (60 days after last activity)
        LocalDateTime lastActivity =
            lead.lastActivityAt != null ? lead.lastActivityAt : lead.registeredAt;
        LocalDateTime reminderDate = lastActivity.plusDays(lead.protectionDays60);

        if (now.isBefore(reminderDate)) {
          return (int) java.time.Duration.between(now, reminderDate).toDays();
        }
        return 0; // Should transition now

      case REMINDER:
        // Calculate days until grace period (10 days after reminder)
        if (lead.reminderSentAt != null) {
          LocalDateTime graceDate = lead.reminderSentAt.plusDays(lead.protectionDays10);
          if (now.isBefore(graceDate)) {
            return (int) java.time.Duration.between(now, graceDate).toDays();
          }
        }
        return 0; // Should transition now

      case GRACE_PERIOD:
        // Calculate days until expiration (10 days after grace period start)
        if (lead.gracePeriodStartAt != null) {
          LocalDateTime expiryDate = lead.gracePeriodStartAt.plusDays(lead.protectionDays10);
          if (now.isBefore(expiryDate)) {
            return (int) java.time.Duration.between(now, expiryDate).toDays();
          }
        }
        return 0; // Should expire now

      default:
        return -1; // No scheduled transitions
    }
  }

  /**
   * Check if a user can stop the clock for a lead.
   *
   * @return true if user has permission and lead is in valid state
   */
  public boolean canStopClock(Lead lead, String userId) {
    // Check user permission
    var settings = settingsService.getOrCreateForUser(userId);
    if (!settings.canStopClock) {
      LOG.warnf("User %s does not have permission to stop clock", userId);
      return false;
    }

    // Check lead state
    if (lead.status == LeadStatus.EXPIRED
        || lead.status == LeadStatus.CONVERTED
        || lead.status == LeadStatus.LOST
        || lead.status == LeadStatus.DELETED) {
      LOG.warnf("Cannot stop clock for lead %s in status %s", lead.id, lead.status);
      return false;
    }

    // Check if clock is already stopped
    if (lead.clockStoppedAt != null) {
      LOG.warnf("Clock already stopped for lead %s", lead.id);
      return false;
    }

    return true;
  }

  /**
   * Check if a user can resume the clock for a lead.
   *
   * @return true if user has permission and clock is stopped
   */
  public boolean canResumeClock(Lead lead, String userId) {
    // Check if clock is stopped
    if (lead.clockStoppedAt == null) {
      LOG.warnf("Clock not stopped for lead %s", lead.id);
      return false;
    }

    // Check user permission (same as stop permission or admin)
    var settings = settingsService.getOrCreateForUser(userId);
    if (!settings.canStopClock) {
      LOG.warnf("User %s does not have permission to resume clock", userId);
      return false;
    }

    return true;
  }

  /**
   * Check if a lead's protection is about to expire.
   *
   * @param warningDays number of days before expiry to trigger warning
   * @return true if lead will expire within warningDays
   */
  public boolean isExpiringSoon(Lead lead, int warningDays) {
    if (lead.clockStoppedAt != null) {
      return false; // Clock stopped = no expiry
    }

    if (lead.status == LeadStatus.EXPIRED) {
      return false; // Already expired
    }

    int remainingDays = getRemainingProtectionDays(lead);
    return remainingDays >= 0 && remainingDays <= warningDays;
  }

  /** Get protection status summary for a lead. */
  public ProtectionStatus getProtectionStatus(Lead lead) {
    ProtectionStatus status = new ProtectionStatus();
    status.leadId = lead.id;
    status.currentStatus = lead.status;
    status.isProtected = lead.isProtectionActive();
    status.clockStopped = lead.clockStoppedAt != null;
    status.remainingDays = getRemainingProtectionDays(lead);
    status.daysUntilTransition = getDaysUntilNextTransition(lead);

    if (lead.clockStoppedAt != null) {
      status.stopReason = lead.stopReason;
      status.stoppedBy = lead.stopApprovedBy;
      status.stoppedAt = lead.clockStoppedAt;
    }

    return status;
  }

  // ============================================================================
  // Sprint 2.1.5: Progressive Profiling & Progress Tracking
  // ============================================================================

  /** Progressive Profiling Stage: Vormerkung (Minimal Company Data). */
  private static final int STAGE_MIN = 0;

  /** Progressive Profiling Stage: Qualifiziert (Full Qualification). */
  private static final int STAGE_MAX = 2;

  /** Contract §3.3: 60-day activity standard - belegbarer Fortschritt alle 60 Tage. */
  private static final int PROGRESS_DEADLINE_DAYS = 60;

  /** Contract §3.3: Warning at 53 days (7 days before 60-day deadline). */
  private static final int PROGRESS_WARNING_DAYS_BEFORE_DEADLINE = 7;

  /**
   * Validate stage transition for Progressive Profiling (Sprint 2.1.5).
   *
   * <p>Valid transitions: - 0 → 1 (Vormerkung → Registrierung) - 1 → 2 (Registrierung →
   * Qualifiziert) - No stage skipping allowed (0 → 2 is invalid)
   *
   * @param currentStage current stage (0, 1, 2)
   * @param newStage target stage (0, 1, 2)
   * @return true if transition is valid
   */
  /**
   * Validates stage transitions using LeadStage enum (type-safe).
   *
   * @param currentStage Current lead stage
   * @param newStage Target lead stage
   * @return true if transition is allowed, false otherwise
   * @since 2.1.6 (Issue #125)
   */
  public boolean canTransitionStage(LeadStage currentStage, LeadStage newStage) {
    if (currentStage == null || newStage == null) {
      LOG.error("Stage values cannot be null");
      return false;
    }

    // Delegate to enum's canTransitionTo method
    boolean allowed = currentStage.canTransitionTo(newStage);

    if (!allowed) {
      LOG.warnf("Cannot transition stage from %s to %s", currentStage, newStage);
    }

    return allowed;
  }

  /**
   * Validates stage transitions using int values (backward compatibility).
   *
   * @param currentStage Current lead stage (0, 1, or 2)
   * @param newStage Target lead stage (0, 1, or 2)
   * @return true if transition is allowed, false otherwise
   * @deprecated Use {@link #canTransitionStage(LeadStage, LeadStage)} instead
   */
  @Deprecated(since = "2.1.6", forRemoval = true)
  public boolean canTransitionStage(int currentStage, int newStage) {
    // Stage range validation
    if (currentStage < STAGE_MIN
        || currentStage > STAGE_MAX
        || newStage < STAGE_MIN
        || newStage > STAGE_MAX) {
      LOG.errorf(
          "Invalid stage values: current=%d, new=%d (valid range: %d-%d)",
          currentStage, newStage, STAGE_MIN, STAGE_MAX);
      return false;
    }

    // Convert to enum and delegate
    try {
      LeadStage current = LeadStage.fromValue(currentStage);
      LeadStage target = LeadStage.fromValue(newStage);
      return canTransitionStage(current, target);
    } catch (IllegalArgumentException e) {
      LOG.errorf(
          e, "Failed to convert stage values to enum: current=%d, new=%d", currentStage, newStage);
      return false;
    }
  }

  /**
   * Calculate protection end date using V257 function logic (Sprint 2.1.5).
   *
   * <p>Implements: calculate_protection_until(registered_at, protection_months)
   *
   * @param lead the lead to calculate for
   * @return protection end date, or null if lead/registeredAt is null
   */
  public LocalDateTime calculateProtectionUntil(Lead lead) {
    if (lead == null || lead.registeredAt == null) {
      LOG.warn("Lead or registeredAt is null, cannot calculate protection date");
      return null;
    }
    // Use V257 function logic: registered_at + protection_months
    return lead.registeredAt.plusMonths(lead.protectionMonths);
  }

  /**
   * Calculate progress deadline using V257 function logic (Sprint 2.1.5).
   *
   * <p>Implements: calculate_progress_deadline(last_activity_at)
   *
   * <p>§3.3: 60-Tage-Aktivitätsstandard - belegbarer Fortschritt alle 60 Tage
   *
   * @param lastActivityAt last activity timestamp
   * @return progress deadline (last_activity_at + 60 days)
   */
  public LocalDateTime calculateProgressDeadline(LocalDateTime lastActivityAt) {
    if (lastActivityAt == null) {
      LOG.warn("lastActivityAt is null, cannot calculate progress deadline");
      return null;
    }
    // Use V257 function logic: last_activity_at + PROGRESS_DEADLINE_DAYS
    return lastActivityAt.plusDays(PROGRESS_DEADLINE_DAYS);
  }

  /**
   * Check if progress deadline is approaching (Sprint 2.1.5).
   *
   * <p>§3.3: Warning at 53 days (7 days before deadline)
   *
   * @param lead the lead to check
   * @return true if warning should be sent
   */
  public boolean needsProgressWarning(Lead lead) {
    if (lead.progressDeadline == null) {
      return false; // No deadline set
    }

    if (lead.progressWarningSentAt != null) {
      return false; // Warning already sent
    }

    LocalDateTime now = LocalDateTime.now();
    LocalDateTime warningThreshold =
        lead.progressDeadline.minusDays(PROGRESS_WARNING_DAYS_BEFORE_DEADLINE);

    return now.isAfter(warningThreshold);
  }

  /** DTO for protection status information. */
  public static class ProtectionStatus {
    public Long leadId;
    public LeadStatus currentStatus;
    public boolean isProtected;
    public boolean clockStopped;
    public int remainingDays;
    public int daysUntilTransition;
    public String stopReason;
    public String stoppedBy;
    public LocalDateTime stoppedAt;
  }
}
