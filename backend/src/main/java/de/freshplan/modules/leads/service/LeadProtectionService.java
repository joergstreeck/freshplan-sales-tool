package de.freshplan.modules.leads.service;

import de.freshplan.modules.leads.domain.Lead;
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
