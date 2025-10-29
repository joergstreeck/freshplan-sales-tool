package de.freshplan.modules.leads.domain;

/**
 * Types of activities that can be tracked for leads. Sprint 2.1.5: Updated with countsAsProgress
 * flag for progress tracking.
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

  /**
   * Check if this activity type is user-selectable in ActivityDialog.
   *
   * <p>User-selectable activities (10 types):
   * - Progress Activities (5): QUALIFIED_CALL, MEETING, DEMO, ROI_PRESENTATION, SAMPLE_SENT
   * - Non-Progress Activities (5): NOTE, FOLLOW_UP, EMAIL, CALL, SAMPLE_FEEDBACK
   *
   * <p>NOT user-selectable (13 types):
   * - System Activities (3): FIRST_CONTACT_DOCUMENTED, EMAIL_RECEIVED, LEAD_ASSIGNED
   * - Legacy Activities (10): ORDER, STATUS_CHANGE, CREATED, etc.
   *
   * @return true if users can manually select this activity type
   */
  public boolean isUserSelectable() {
    return switch (this) {
      // Progress Activities (5)
      case QUALIFIED_CALL, MEETING, DEMO, ROI_PRESENTATION, SAMPLE_SENT ->
          true;
      // Non-Progress Activities (5)
      case NOTE, FOLLOW_UP, EMAIL, CALL, SAMPLE_FEEDBACK ->
          true;
      // System & Legacy Activities (13) - NOT user-selectable
      default ->
          false;
    };
  }

  /**
   * Get user-friendly display name for this activity type.
   *
   * <p>Used for: EnumResource API, ActivityDialog dropdown
   *
   * @return Localized display name (German)
   */
  public String getDisplayName() {
    return switch (this) {
      case QUALIFIED_CALL -> "Qualifizierter Anruf";
      case MEETING -> "Meeting";
      case DEMO -> "Demo";
      case ROI_PRESENTATION -> "ROI-Präsentation";
      case SAMPLE_SENT -> "Muster versendet";
      case NOTE -> "Notiz";
      case FOLLOW_UP -> "Follow-up";
      case EMAIL -> "E-Mail";
      case CALL -> "Anruf";
      case SAMPLE_FEEDBACK -> "Muster-Feedback";
      case FIRST_CONTACT_DOCUMENTED -> "Erstkontakt dokumentiert";
      case EMAIL_RECEIVED -> "E-Mail erhalten";
      case LEAD_ASSIGNED -> "Lead zugewiesen";
      case ORDER -> "Bestellung";
      case STATUS_CHANGE -> "Statusänderung";
      case CREATED -> "Erstellt";
      case DELETED -> "Gelöscht";
      case REMINDER_SENT -> "Erinnerung gesendet";
      case GRACE_PERIOD_STARTED -> "Schonfrist gestartet";
      case EXPIRED -> "Abgelaufen";
      case REACTIVATED -> "Reaktiviert";
      case CLOCK_STOPPED -> "Uhr gestoppt";
      case CLOCK_RESUMED -> "Uhr fortgesetzt";
    };
  }
}
