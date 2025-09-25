package de.freshplan.modules.leads.service;

import de.freshplan.modules.leads.domain.ActivityType;
import de.freshplan.modules.leads.domain.Lead;
import de.freshplan.modules.leads.domain.LeadActivity;
import de.freshplan.modules.leads.domain.LeadStatus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import org.jboss.logging.Logger;

/**
 * Service for lead management operations. Handles lead lifecycle, state transitions, and
 * protection system logic.
 */
@ApplicationScoped
public class LeadService {

  private static final Logger LOG = Logger.getLogger(LeadService.class);

  @Inject EntityManager em;

  @Inject UserLeadSettingsService settingsService;

  /**
   * Process leads that need reminders (60-day rule). Transitions leads from ACTIVE to REMINDER
   * status.
   */
  @Transactional
  public int processReminders() {
    LocalDateTime cutoffDate = LocalDateTime.now().minusDays(60);

    List<Lead> leadsNeedingReminder =
        Lead.find(
                "status = ?1 and reminderSentAt is null and "
                    + "(lastActivityAt < ?2 or (lastActivityAt is null and registeredAt < ?2)) "
                    + "and clockStoppedAt is null",
                LeadStatus.ACTIVE,
                cutoffDate)
            .list();

    int count = 0;
    for (Lead lead : leadsNeedingReminder) {
      // Transition to REMINDER status
      lead.status = LeadStatus.REMINDER;
      lead.reminderSentAt = LocalDateTime.now();
      lead.persist();

      // Create activity
      LeadActivity activity = new LeadActivity();
      activity.lead = lead;
      activity.userId = "SYSTEM";
      activity.activityType = ActivityType.REMINDER_SENT;
      activity.description = "60-day reminder sent - no activity detected";
      activity.persist();

      LOG.infof("Sent reminder for lead %s (owner: %s)", lead.id, lead.ownerUserId);
      count++;
    }

    return count;
  }

  /**
   * Process leads entering grace period (10-day rule after reminder). Transitions from REMINDER to
   * GRACE_PERIOD.
   */
  @Transactional
  public int processGracePeriod() {
    LocalDateTime cutoffDate = LocalDateTime.now().minusDays(10);

    List<Lead> leadsEnteringGrace =
        Lead.find(
                "status = ?1 and reminderSentAt < ?2 and gracePeriodStartAt is null "
                    + "and clockStoppedAt is null",
                LeadStatus.REMINDER,
                cutoffDate)
            .list();

    int count = 0;
    for (Lead lead : leadsEnteringGrace) {
      // Transition to GRACE_PERIOD
      lead.status = LeadStatus.GRACE_PERIOD;
      lead.gracePeriodStartAt = LocalDateTime.now();
      lead.persist();

      // Create activity
      LeadActivity activity = new LeadActivity();
      activity.lead = lead;
      activity.userId = "SYSTEM";
      activity.activityType = ActivityType.GRACE_PERIOD_STARTED;
      activity.description = "10-day grace period started";
      activity.persist();

      LOG.infof("Lead %s entered grace period (owner: %s)", lead.id, lead.ownerUserId);
      count++;
    }

    return count;
  }

  /**
   * Process expired leads (after 6 months or grace period ends). Transitions to EXPIRED status.
   */
  @Transactional
  public int processExpirations() {
    int count = 0;

    // 1. Expire leads after 6 months of protection
    LocalDateTime sixMonthsCutoff = LocalDateTime.now().minusMonths(6);
    List<Lead> sixMonthExpired =
        Lead.find(
                "status in ?1 and protectionStartAt < ?2 and expiredAt is null "
                    + "and clockStoppedAt is null",
                List.of(LeadStatus.REGISTERED, LeadStatus.ACTIVE, LeadStatus.REMINDER),
                sixMonthsCutoff)
            .list();

    for (Lead lead : sixMonthExpired) {
      lead.status = LeadStatus.EXPIRED;
      lead.expiredAt = LocalDateTime.now();
      lead.persist();

      LeadActivity activity = new LeadActivity();
      activity.lead = lead;
      activity.userId = "SYSTEM";
      activity.activityType = ActivityType.EXPIRED;
      activity.description = "Lead expired after 6 months of protection";
      activity.persist();

      LOG.infof("Lead %s expired after 6 months (owner: %s)", lead.id, lead.ownerUserId);
      count++;
    }

    // 2. Expire leads after grace period ends (10 days)
    LocalDateTime gracePeriodCutoff = LocalDateTime.now().minusDays(10);
    List<Lead> gracePeriodExpired =
        Lead.find(
                "status = ?1 and gracePeriodStartAt < ?2 and expiredAt is null "
                    + "and clockStoppedAt is null",
                LeadStatus.GRACE_PERIOD,
                gracePeriodCutoff)
            .list();

    for (Lead lead : gracePeriodExpired) {
      lead.status = LeadStatus.EXPIRED;
      lead.expiredAt = LocalDateTime.now();
      lead.persist();

      LeadActivity activity = new LeadActivity();
      activity.lead = lead;
      activity.userId = "SYSTEM";
      activity.activityType = ActivityType.EXPIRED;
      activity.description = "Lead expired after grace period ended";
      activity.persist();

      LOG.infof("Lead %s expired after grace period (owner: %s)", lead.id, lead.ownerUserId);
      count++;
    }

    return count;
  }

  /**
   * Reactivate lead when activity is recorded. Resets reminder and grace period timestamps.
   */
  @Transactional
  public void reactivateLead(Lead lead) {
    if (lead.status == LeadStatus.REMINDER || lead.status == LeadStatus.GRACE_PERIOD) {
      lead.status = LeadStatus.ACTIVE;
      lead.reminderSentAt = null;
      lead.gracePeriodStartAt = null;
      lead.lastActivityAt = LocalDateTime.now();
      lead.persist();

      LeadActivity activity = new LeadActivity();
      activity.lead = lead;
      activity.userId = "SYSTEM";
      activity.activityType = ActivityType.REACTIVATED;
      activity.description = "Lead reactivated due to new activity";
      activity.persist();

      LOG.infof("Lead %s reactivated due to activity", lead.id);
    }
  }

  /**
   * Check if lead protection is about to expire and send warning. Used for dashboard
   * notifications.
   */
  public List<Lead> getExpiringLeads(String userId, int daysBeforeExpiry) {
    LocalDateTime expiryWarningDate = LocalDateTime.now().plusDays(daysBeforeExpiry);
    LocalDateTime sixMonthsFromNow = LocalDateTime.now().plusMonths(6);

    return Lead.find(
            "ownerUserId = ?1 and status in ?2 and clockStoppedAt is null "
                + "and (protectionStartAt < ?3 or "
                + "(status = ?4 and gracePeriodStartAt < ?5))",
            userId,
            List.of(LeadStatus.ACTIVE, LeadStatus.REMINDER, LeadStatus.GRACE_PERIOD),
            sixMonthsFromNow.minusDays(daysBeforeExpiry),
            LeadStatus.GRACE_PERIOD,
            expiryWarningDate.minusDays(10))
        .list();
  }

  /**
   * Get lead statistics for a user. Used for dashboard widgets.
   */
  public LeadStatistics getStatistics(String userId) {
    LeadStatistics stats = new LeadStatistics();

    // Total leads owned
    stats.totalLeads = Lead.count("ownerUserId = ?1 and status != ?2", userId, LeadStatus.DELETED);

    // Active leads
    stats.activeLeads = Lead.count("ownerUserId = ?1 and status = ?2", userId, LeadStatus.ACTIVE);

    // Leads in reminder status
    stats.reminderLeads = Lead.count("ownerUserId = ?1 and status = ?2", userId, LeadStatus.REMINDER);

    // Leads in grace period
    stats.gracePeriodLeads = Lead.count("ownerUserId = ?1 and status = ?2", userId, LeadStatus.GRACE_PERIOD);

    // Expired leads
    stats.expiredLeads = Lead.count("ownerUserId = ?1 and status = ?2", userId, LeadStatus.EXPIRED);

    // Clock stopped leads
    stats.clockStoppedLeads = Lead.count("ownerUserId = ?1 and clockStoppedAt is not null", userId);

    // Leads expiring soon (within 7 days)
    stats.expiringSoonLeads = getExpiringLeads(userId, 7).size();

    return stats;
  }

  /**
   * Statistics DTO for lead dashboard.
   */
  public static class LeadStatistics {
    public long totalLeads;
    public long activeLeads;
    public long reminderLeads;
    public long gracePeriodLeads;
    public long expiredLeads;
    public long clockStoppedLeads;
    public long expiringSoonLeads;
  }
}