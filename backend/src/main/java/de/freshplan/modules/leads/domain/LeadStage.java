package de.freshplan.modules.leads.domain;

/**
 * Progressive Profiling Stages for Lead Management.
 *
 * <p>Defines the three stages of lead qualification according to §2(8)(a) of the sales
 * representative contract:
 *
 * <ul>
 *   <li><b>VORMERKUNG (0):</b> Minimal company data - Pre-Claim stage (10-day window)
 *   <li><b>REGISTRIERUNG (1):</b> Contact person OR documented first contact - Protection activated
 *       (6 months)
 *   <li><b>QUALIFIZIERT (2):</b> Full business data - Ready for conversion to customer
 * </ul>
 *
 * <p><b>Important:</b> Uses {@code EnumType.ORDINAL} for database persistence (0, 1, 2) to maintain
 * backward compatibility with existing {@code Short stage} column.
 *
 * @author FreshPlan Team
 * @since 2.1.6
 * @see <a href="https://github.com/joergstreeck/freshplan-sales-tool/issues/125">Issue #125</a>
 */
public enum LeadStage {
  /**
   * Stage 0: Vormerkung (Pre-Claim).
   *
   * <p>Minimal company data entered. Lead has 10 days to add contact information or document first
   * contact. No protection active yet ({@code registeredAt = NULL}).
   *
   * <p><b>Required fields:</b> Company name, City
   *
   * <p><b>Transition to REGISTRIERUNG:</b> Add contact person OR document first contact (meeting,
   * call, etc.)
   */
  VORMERKUNG(0, "Vormerkung"),

  /**
   * Stage 1: Registrierung (Registered).
   *
   * <p>Contact person added OR first contact documented. Lead protection activated for 6 months
   * ({@code registeredAt != NULL}).
   *
   * <p><b>Required fields:</b> Company name, City, (Contact person OR first contact activity)
   *
   * <p><b>Transition to QUALIFIZIERT:</b> Add business data (industry, employee count, estimated
   * volume)
   */
  REGISTRIERUNG(1, "Registrierung"),

  /**
   * Stage 2: Qualifiziert (Qualified).
   *
   * <p>Full business data entered. Lead is ready for conversion to customer ({@code status =
   * CONVERTED}).
   *
   * <p><b>Required fields:</b> All Stage 1 fields + (Industry, Employee count, Estimated volume)
   *
   * <p><b>Transition to Customer:</b> Manual conversion via "Convert to Customer" action
   */
  QUALIFIZIERT(2, "Qualifiziert");

  private final int value;
  private final String displayName;

  LeadStage(int value, String displayName) {
    this.value = value;
    this.displayName = displayName;
  }

  /**
   * Returns the numeric value for database persistence.
   *
   * <p><b>Note:</b> This method is primarily for documentation. JPA
   * {@code @Enumerated(EnumType.ORDINAL)} uses {@code ordinal()} method instead.
   *
   * @return 0 for VORMERKUNG, 1 for REGISTRIERUNG, 2 for QUALIFIZIERT
   */
  public int getValue() {
    return value;
  }

  /**
   * Returns the German display name for UI rendering.
   *
   * @return Human-readable stage name
   */
  public String getDisplayName() {
    return displayName;
  }

  /**
   * Checks if this stage can transition to the target stage.
   *
   * <p><b>Transition Rules:</b>
   *
   * <ul>
   *   <li>Sequential forward transitions allowed (VORMERKUNG → REGISTRIERUNG, REGISTRIERUNG →
   *       QUALIFIZIERT)
   *   <li>Stage skipping NOT allowed (VORMERKUNG → QUALIFIZIERT is invalid)
   *   <li>Backward transitions NOT allowed (no downgrade)
   *   <li>Same-stage transitions allowed (idempotent)
   * </ul>
   *
   * @param targetStage The target stage to transition to
   * @return {@code true} if transition is allowed, {@code false} otherwise
   */
  public boolean canTransitionTo(LeadStage targetStage) {
    // Same-stage transitions are allowed (idempotent)
    if (targetStage.ordinal() == this.ordinal()) {
      return true;
    }

    // Backward transitions are NOT allowed (no downgrade)
    if (targetStage.ordinal() < this.ordinal()) {
      return false;
    }

    // Forward transitions: Only allow sequential progression (no skipping)
    // VORMERKUNG (0) can only go to REGISTRIERUNG (1)
    // REGISTRIERUNG (1) can only go to QUALIFIZIERT (2)
    return (targetStage.ordinal() - this.ordinal()) == 1;
  }

  /**
   * Returns the LeadStage enum constant from its numeric value.
   *
   * @param value 0, 1, or 2
   * @return Corresponding LeadStage enum
   * @throws IllegalArgumentException if value is not 0, 1, or 2
   */
  public static LeadStage fromValue(int value) {
    for (LeadStage stage : values()) {
      if (stage.value == value) {
        return stage;
      }
    }
    throw new IllegalArgumentException("Invalid LeadStage value: " + value);
  }
}
