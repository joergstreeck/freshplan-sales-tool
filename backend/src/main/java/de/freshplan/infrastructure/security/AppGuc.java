package de.freshplan.infrastructure.security;

/**
 * Central definition of PostgreSQL GUC (Grand Unified Configuration) keys
 * used for Row-Level Security (RLS) context.
 *
 * These keys are set on the database connection to provide context
 * for RLS policies to filter data based on user, role, tenant, and territory.
 */
public enum AppGuc {
    CURRENT_USER("app.current_user"),
    CURRENT_ROLE("app.current_role"),
    TENANT_ID("app.tenant_id"),
    CURRENT_TERRITORY("app.current_territory");

    private final String key;

    AppGuc(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    /**
     * SQL to set a GUC value on the current connection.
     */
    public String setConfigSql(String value) {
        if (value == null) {
            return String.format("SET LOCAL %s = ''", key);
        }
        return String.format("SET LOCAL %s = '%s'", key, value.replace("'", "''"));
    }

    /**
     * SQL to get a GUC value from the current connection.
     */
    public String getConfigSql() {
        return String.format("SELECT current_setting('%s', true)", key);
    }
}