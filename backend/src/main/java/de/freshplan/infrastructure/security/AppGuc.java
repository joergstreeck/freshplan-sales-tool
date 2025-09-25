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

  /** SQL to set a GUC value on the current connection. */
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
   * SQL to set a GUC value at session level (for long-lived connections). Unlike setConfigSql which
   * uses SET LOCAL (transaction-scoped), this uses SET (session-scoped) for connections that live
   * beyond a single transaction.
   */
  public String setSessionConfigSql(String value) {
    if (value == null || value.isEmpty()) {
      return String.format("SET %s = ''", key);
    }
    return String.format("SET %s = '%s'", key, value.replace("'", "''"));
  }
}
