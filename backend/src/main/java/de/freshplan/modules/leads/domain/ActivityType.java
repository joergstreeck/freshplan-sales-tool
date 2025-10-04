package de.freshplan.modules.leads.domain;

/**
 * Types of activities that can be tracked for leads.
 * Sprint 2.1.5: Expanded with Progress-Mapping (countsAsProgress) and System-Activities.
 *
 * Reference: ACTIVITY_TYPES_PROGRESS_MAPPING.md
 * DB Constraint: V258__expand_activity_type_constraint.sql (13 Types)
 *
 * @param meaningfulContact Legacy field - kept for backward compatibility
 * @param resetsTimer Maps to countsAsProgress (V257 Trigger behavior)
 */
public enum ActivityType {
  // Progress Activities (countsAsProgress=true, V257 Trigger fires)
  QUALIFIED_CALL(true, true),
  MEETING(true, true),
  DEMO(true, true),
  ROI_PRESENTATION(true, true),
  SAMPLE_SENT(true, true),

  // Non-Progress Activities (countsAsProgress=false, V257 Trigger NICHT)
  NOTE(false, false),
  FOLLOW_UP(false, false),
  EMAIL(true, false),
  CALL(true, false),
  SAMPLE_FEEDBACK(false, false),

  // System Activities (Sprint 2.1.5 - countsAsProgress=false, kein V257 Trigger)
  FIRST_CONTACT_DOCUMENTED(true, false),  // ⚠️ startet Schutz explizit via LeadService
  EMAIL_RECEIVED(false, false),
  LEAD_ASSIGNED(false, false),

  // Legacy System Activities (aus Sprint 2.1.0-2.1.4)
  STATUS_CHANGE(false, false),
  CREATED(false, false),
  DELETED(false, false),
  REMINDER_SENT(false, false),
  GRACE_PERIOD_STARTED(false, false),
  EXPIRED(false, false),
  REACTIVATED(false, true),
  CLOCK_STOPPED(false, false),
  CLOCK_RESUMED(false, false),
  ORDER(true, true);

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
