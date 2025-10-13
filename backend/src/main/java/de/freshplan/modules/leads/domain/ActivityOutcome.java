package de.freshplan.modules.leads.domain;

/**
 * Activity Outcome Enum (Sprint 2.1.7 - Issue #126)
 *
 * <p>Standardized outcomes for lead activities to track interaction success.
 *
 * <p>Business Logic:
 * - SUCCESSFUL: Positive outcome (e.g., meeting scheduled, sample sent)
 * - UNSUCCESSFUL: Negative outcome (e.g., not interested, wrong contact)
 * - NO_ANSWER: Contact attempt failed (e.g., no pickup, voicemail)
 * - CALLBACK_REQUESTED: Follow-up needed (e.g., call back later)
 * - INFO_SENT: Information provided (e.g., email sent, brochure mailed)
 * - QUALIFIED: Lead qualified for next stage
 * - DISQUALIFIED: Lead does not meet criteria
 *
 * <p>Migration: V10027__add_activity_outcome_enum.sql
 */
public enum ActivityOutcome {
  /**
   * Positive outcome - activity achieved its goal.
   * Examples: Meeting scheduled, sample sent, order placed
   */
  SUCCESSFUL("Erfolgreich", "Activity achieved its goal"),

  /**
   * Negative outcome - activity did not achieve its goal.
   * Examples: Not interested, wrong contact, competitor chosen
   */
  UNSUCCESSFUL("Nicht erfolgreich", "Activity did not achieve its goal"),

  /**
   * No answer - contact attempt failed.
   * Examples: No pickup, voicemail, out of office
   */
  NO_ANSWER("Keine Antwort", "Contact attempt failed"),

  /**
   * Callback requested - follow-up needed.
   * Examples: Call back later, busy now, schedule follow-up
   */
  CALLBACK_REQUESTED("Rückruf gewünscht", "Follow-up needed"),

  /**
   * Information sent - materials provided.
   * Examples: Email sent, brochure mailed, catalog delivered
   */
  INFO_SENT("Info versendet", "Information materials provided"),

  /**
   * Qualified - lead meets criteria for next stage.
   * Examples: Budget confirmed, decision maker identified, timeline set
   */
  QUALIFIED("Qualifiziert", "Lead meets criteria for next stage"),

  /**
   * Disqualified - lead does not meet criteria.
   * Examples: No budget, wrong industry, no need
   */
  DISQUALIFIED("Disqualifiziert", "Lead does not meet criteria");

  private final String displayNameDe;
  private final String description;

  ActivityOutcome(String displayNameDe, String description) {
    this.displayNameDe = displayNameDe;
    this.description = description;
  }

  /**
   * Get German display name for UI.
   *
   * @return Localized display name
   */
  public String getDisplayName() {
    return displayNameDe;
  }

  /**
   * Get English description for documentation.
   *
   * @return Technical description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Check if outcome is positive.
   *
   * @return true if SUCCESSFUL or QUALIFIED
   */
  public boolean isPositive() {
    return this == SUCCESSFUL || this == QUALIFIED;
  }

  /**
   * Check if outcome requires follow-up.
   *
   * @return true if CALLBACK_REQUESTED or NO_ANSWER
   */
  public boolean requiresFollowUp() {
    return this == CALLBACK_REQUESTED || this == NO_ANSWER;
  }

  /**
   * Check if outcome indicates end of process.
   *
   * @return true if DISQUALIFIED or UNSUCCESSFUL
   */
  public boolean isTerminal() {
    return this == DISQUALIFIED || this == UNSUCCESSFUL;
  }
}
