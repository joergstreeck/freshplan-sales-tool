package de.freshplan.infrastructure.settings;

/**
 * Settings scope hierarchy for Sprint 1.2 PR #2. Defines the 5-level scope hierarchy for settings
 * resolution.
 */
public enum SettingsScope {
  /** System-wide default settings. */
  GLOBAL,

  /** Tenant-specific settings (e.g., FreshFoodz). */
  TENANT,

  /** Territory-specific settings (e.g., DE, CH). */
  TERRITORY,

  /** Account/Customer-specific settings. */
  ACCOUNT,

  /** Contact role-specific settings (e.g., CHEF, BUYER). */
  CONTACT_ROLE;

  /**
   * Gets the priority of this scope for resolution. Higher priority scopes override lower priority
   * ones.
   *
   * @return priority value (1-5, higher is more specific)
   */
  public int getPriority() {
    return switch (this) {
      case GLOBAL -> 1;
      case TENANT -> 2;
      case TERRITORY -> 3;
      case ACCOUNT -> 4;
      case CONTACT_ROLE -> 5;
    };
  }
}
