package de.freshplan.infrastructure.security;

/**
 * Central definition of PostgreSQL GUC (Grand Unified Configuration) keys used for Row-Level
 * Security (RLS) context.
 *
 * <p>These keys are set on the database connection to provide context for RLS policies to filter
 * data based on user, role, tenant, and territory.
 */
public enum AppGuc {
  CURRENT_USER("app.user_context"),
  CURRENT_ROLE("app.role_context"),
  TENANT_ID("app.tenant_id"),
  CURRENT_TERRITORY("app.territory_context");

  private final String key;

  AppGuc(String key) {
    this.key = key;
  }

  public String getKey() {
    return key;
  }

  /**
   * Returns SQL for setting GUC value using set_config (transaction-scoped). Use with prepared
   * statement parameters for safety.
   */
  public String setLocalConfigSql() {
    return "SELECT set_config(?, ?, true)"; // true = transaction-local
  }

  /**
   * @deprecated Use setLocalConfigSql() with parameters instead
   */
  @Deprecated
  public String setConfigSql(String value) {
    if (value == null || value.isEmpty()) {
      return String.format("SET LOCAL %s = ''", key);
    }
    return String.format("SET LOCAL %s = '%s'", key, value.replace("'", "''"));
  }

  /** SQL to get a GUC value from the current connection. */
  public String getConfigSql() {
    return String.format("SELECT current_setting('%s', true)", key);
  }

  /**
   * Returns SQL for setting GUC value using set_config (session-scoped). Use with prepared
   * statement parameters for safety.
   */
  public String setSessionConfigSql() {
    return "SELECT set_config(?, ?, false)"; // false = session-level
  }

  /**
   * @deprecated Use setSessionConfigSql() with parameters instead
   */
  @Deprecated
  public String setSessionConfigSql(String value) {
    if (value == null || value.isEmpty()) {
      return String.format("SET %s = ''", key);
    }
    return String.format("SET %s = '%s'", key, value.replace("'", "''"));
  }
}
