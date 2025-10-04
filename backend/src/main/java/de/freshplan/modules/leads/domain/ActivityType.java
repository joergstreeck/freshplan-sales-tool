package de.freshplan.modules.leads.domain;

/**
 * Types of activities that can be tracked for leads.
 * Sprint 2.1.5: Updated with countsAsProgress flag for progress tracking.
 *
 * @see V258 Migration - Database constraint for countsAsProgress
 */
public enum ActivityType {
  // Progress Activities (countsAsProgress = true) - 5 Types
  QUALIFIED_CALL(true, true, true),
  MEETING(true, true, true),
  DEMO(true, true, true),
  ROI_PRESENTATION(true, true, true),
  SAMPLE_SENT(true, true, true),

  // Non-Progress Activities (countsAsProgress = false) - 5 Types
  NOTE(false, false, false),
  FOLLOW_UP(false, false, false),
  EMAIL(true, true, false),
  CALL(true, true, false),
  SAMPLE_FEEDBACK(false, false, false),

  // System Activities (countsAsProgress = false) - 3 Types (Sprint 2.1.5)
  FIRST_CONTACT_DOCUMENTED(false, false, false),
  EMAIL_RECEIVED(false, false, false),
  LEAD_ASSIGNED(false, false, false),

  // Legacy Activities (kept for backward compatibility)
  ORDER(true, true, false),
  STATUS_CHANGE(false, false, false),
  CREATED(false, false, false),
  DELETED(false, false, false),
  REMINDER_SENT(false, false, false),
  GRACE_PERIOD_STARTED(false, false, false),
  EXPIRED(false, false, false),
  REACTIVATED(false, true, false),
  CLOCK_STOPPED(false, false, false),
  CLOCK_RESUMED(false, false, false);

  private final boolean meaningfulContact;
  private final boolean resetsTimer;
  private final boolean countsAsProgress; // Sprint 2.1.5

  ActivityType(boolean meaningfulContact, boolean resetsTimer, boolean countsAsProgress) {
    this.meaningfulContact = meaningfulContact;
    this.resetsTimer = resetsTimer;
    this.countsAsProgress = countsAsProgress;
  }

  public boolean isMeaningfulContact() {
    return meaningfulContact;
  }

  public boolean resetsTimer() {
    return resetsTimer;
  }

  public boolean countsAsProgress() {
    return countsAsProgress;
  }
}
