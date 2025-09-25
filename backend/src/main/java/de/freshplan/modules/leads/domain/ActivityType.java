package de.freshplan.modules.leads.domain;

/**
 * Types of activities that can be tracked for leads. Sprint 2.1: Defines which activities are
 * meaningful contacts and reset protection timers.
 */
public enum ActivityType {
  EMAIL(true, true),
  CALL(true, true),
  MEETING(true, true),
  SAMPLE_SENT(true, true),
  ORDER(true, true),
  NOTE(false, false),
  STATUS_CHANGE(false, false),
  CREATED(false, false),
  DELETED(false, false),
  REMINDER_SENT(false, false),
  GRACE_PERIOD_STARTED(false, false),
  EXPIRED(false, false),
  REACTIVATED(false, true),
  CLOCK_STOPPED(false, false),
  CLOCK_RESUMED(false, false);

  private final boolean meaningfulContact;
  private final boolean resetsTimer;

  ActivityType(boolean meaningfulContact, boolean resetsTimer) {
    this.meaningfulContact = meaningfulContact;
    this.resetsTimer = resetsTimer;
  }

  public boolean isMeaningfulContact() {
    return meaningfulContact;
  }

  public boolean resetsTimer() {
    return resetsTimer;
  }
}
