-- Security Audit Table Setup for FreshFoodz B2B CRM
-- GDPR-compliant audit logging with automatic retention management

BEGIN;
SET LOCAL lock_timeout='250ms'; SET LOCAL statement_timeout='15s';

-- === Security Audit Log Table ===
CREATE TABLE IF NOT EXISTS security_audit_log (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id text NOT NULL,
    org_id text NOT NULL,
    territory text NOT NULL CHECK (territory IN ('DE','CH')),
    operation text NOT NULL,
    target_class text NOT NULL,
    parameters jsonb DEFAULT '{}'::jsonb,
    outcome text NOT NULL CHECK (outcome IN ('SUCCESS','SECURITY_DENIED','ERROR','PENDING')),
    error_details text,
    duration_ms bigint NOT NULL DEFAULT 0,
    created_at timestamptz NOT NULL DEFAULT now(),

    -- GDPR Data Retention (7 Jahre für B2B-Compliance)
    retention_until timestamptz GENERATED ALWAYS AS (created_at + INTERVAL '7 years') STORED
);

-- === Performance Indexes ===
CREATE INDEX IF NOT EXISTS idx_audit_user_time ON security_audit_log(user_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_audit_operation ON security_audit_log(operation, outcome, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_audit_territory ON security_audit_log(territory, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_audit_retention ON security_audit_log(retention_until) WHERE retention_until < now();
CREATE INDEX IF NOT EXISTS idx_audit_error_analysis ON security_audit_log(outcome, target_class, created_at DESC) WHERE outcome IN ('SECURITY_DENIED', 'ERROR');

-- === Security-specific Indexes for fast queries ===
CREATE INDEX IF NOT EXISTS idx_audit_security_violations ON security_audit_log(user_id, outcome, created_at DESC) WHERE outcome = 'SECURITY_DENIED';
CREATE INDEX IF NOT EXISTS idx_audit_performance_analysis ON security_audit_log(operation, duration_ms, created_at DESC) WHERE duration_ms > 1000;

-- === RLS Policies für Audit-Access ===
ALTER TABLE security_audit_log ENABLE ROW LEVEL SECURITY;

-- Drop existing policies if they exist
DROP POLICY IF EXISTS audit_admin_read ON security_audit_log;
DROP POLICY IF EXISTS audit_security_team_read ON security_audit_log;
DROP POLICY IF EXISTS audit_user_own_read ON security_audit_log;

-- Admin Full Access Policy
CREATE POLICY audit_admin_read ON security_audit_log
FOR SELECT USING (app_has_scope('audit:admin') OR app_has_scope('admin:all'));

-- Security Team Access Policy (alle Security-Events)
CREATE POLICY audit_security_team_read ON security_audit_log
FOR SELECT USING (
    app_has_scope('audit:security') AND outcome IN ('SECURITY_DENIED', 'ERROR')
);

-- User Self-Access Policy (eigene Audit-Logs nur)
CREATE POLICY audit_user_own_read ON security_audit_log
FOR SELECT USING (
    user_id = app_user_id()::text AND
    org_id = app_org_id() AND
    app_has_scope('audit:self')
);

-- === GDPR-Compliance Functions ===

-- Function: Automatische Audit-Log-Bereinigung
CREATE OR REPLACE FUNCTION cleanup_expired_audit_logs()
RETURNS TABLE(deleted_count bigint, oldest_kept timestamptz)
LANGUAGE plpgsql AS $$
DECLARE
    deleted bigint := 0;
    oldest_ts timestamptz;
BEGIN
    -- Lösche abgelaufene Audit-Logs (retention_until < now())
    DELETE FROM security_audit_log
    WHERE retention_until < now() - INTERVAL '30 days';

    GET DIAGNOSTICS deleted = ROW_COUNT;

    -- Ermittle ältesten verbleibenden Eintrag
    SELECT min(created_at) INTO oldest_ts FROM security_audit_log;

    -- Log cleanup activity
    INSERT INTO security_audit_log(user_id, org_id, territory, operation, target_class, parameters, outcome)
    VALUES ('system', 'system', 'SYSTEM', 'cleanup_expired_audit_logs', 'SecurityAuditLog',
            format('{"deleted_count": %s, "oldest_kept": "%s"}', deleted, oldest_ts)::jsonb,
            'SUCCESS');

    RETURN QUERY SELECT deleted, oldest_ts;
END $$;

-- Function: Audit-Statistiken für GDPR-Reports
CREATE OR REPLACE FUNCTION get_audit_statistics(
    start_date timestamptz DEFAULT now() - INTERVAL '30 days',
    end_date timestamptz DEFAULT now()
)
RETURNS TABLE(
    total_operations bigint,
    successful_operations bigint,
    security_denials bigint,
    error_operations bigint,
    avg_duration_ms numeric,
    unique_users bigint,
    territories text[]
) LANGUAGE sql STABLE AS $$
    SELECT
        count(*) as total_operations,
        count(*) FILTER (WHERE outcome = 'SUCCESS') as successful_operations,
        count(*) FILTER (WHERE outcome = 'SECURITY_DENIED') as security_denials,
        count(*) FILTER (WHERE outcome = 'ERROR') as error_operations,
        round(avg(duration_ms), 2) as avg_duration_ms,
        count(DISTINCT user_id) as unique_users,
        array_agg(DISTINCT territory) as territories
    FROM security_audit_log
    WHERE created_at BETWEEN start_date AND end_date;
$$;

-- Function: Security-Incident-Report für kritische Ereignisse
CREATE OR REPLACE FUNCTION get_security_incidents(
    hours_back integer DEFAULT 24
)
RETURNS TABLE(
    incident_time timestamptz,
    user_id text,
    territory text,
    operation text,
    error_details text,
    risk_level text
) LANGUAGE sql STABLE AS $$
    SELECT
        created_at as incident_time,
        user_id,
        territory,
        operation,
        error_details,
        CASE
            WHEN operation LIKE '%territory%' AND outcome = 'SECURITY_DENIED' THEN 'CRITICAL'
            WHEN operation LIKE '%lead%' AND outcome = 'SECURITY_DENIED' THEN 'HIGH'
            WHEN outcome = 'ERROR' AND duration_ms > 5000 THEN 'MEDIUM'
            ELSE 'LOW'
        END as risk_level
    FROM security_audit_log
    WHERE created_at > now() - (hours_back || ' hours')::interval
      AND outcome IN ('SECURITY_DENIED', 'ERROR')
    ORDER BY created_at DESC,
             CASE
                WHEN operation LIKE '%territory%' THEN 1
                WHEN operation LIKE '%lead%' THEN 2
                ELSE 3
             END;
$$;

-- === Monitoring Views für Grafana ===

-- View: Security Metrics für Prometheus/Grafana
CREATE OR REPLACE VIEW security_metrics_hourly AS
SELECT
    date_trunc('hour', created_at) as hour,
    territory,
    operation,
    outcome,
    count(*) as operation_count,
    avg(duration_ms) as avg_duration_ms,
    percentile_cont(0.95) WITHIN GROUP (ORDER BY duration_ms) as p95_duration_ms
FROM security_audit_log
WHERE created_at > now() - INTERVAL '7 days'
GROUP BY date_trunc('hour', created_at), territory, operation, outcome
ORDER BY hour DESC;

-- View: Territory Security Health
CREATE OR REPLACE VIEW territory_security_health AS
SELECT
    territory,
    count(*) as total_operations,
    count(*) FILTER (WHERE outcome = 'SUCCESS') as successful_ops,
    count(*) FILTER (WHERE outcome = 'SECURITY_DENIED') as denied_ops,
    round(
        (count(*) FILTER (WHERE outcome = 'SUCCESS'))::numeric /
        nullif(count(*), 0) * 100, 2
    ) as success_rate_percent
FROM security_audit_log
WHERE created_at > now() - INTERVAL '24 hours'
GROUP BY territory;

-- === Automated Cleanup Trigger ===

-- Trigger: Warnung bei kritischen Security-Events
CREATE OR REPLACE FUNCTION notify_critical_security_event()
RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
    -- Benachrichtigung bei Territory-Isolation-Verstößen
    IF NEW.operation LIKE '%territory%' AND NEW.outcome = 'SECURITY_DENIED' THEN
        PERFORM pg_notify('critical_security_alert',
            format('Territory violation: user %s attempted %s', NEW.user_id, NEW.operation));
    END IF;

    -- Benachrichtigung bei Lead-Protection-Verstößen
    IF NEW.operation LIKE '%lead%' AND NEW.outcome = 'SECURITY_DENIED' THEN
        PERFORM pg_notify('security_alert',
            format('Lead protection violation: user %s attempted %s', NEW.user_id, NEW.operation));
    END IF;

    RETURN NEW;
END $$;

-- Trigger nur für Security-Violations aktivieren
DROP TRIGGER IF EXISTS security_event_notification ON security_audit_log;
CREATE TRIGGER security_event_notification
    AFTER INSERT ON security_audit_log
    FOR EACH ROW
    WHEN (NEW.outcome = 'SECURITY_DENIED')
    EXECUTE FUNCTION notify_critical_security_event();

-- === Permission Grants ===

-- Grant permissions für verschiedene Rollen
GRANT SELECT ON security_audit_log TO security_team;
GRANT SELECT ON security_metrics_hourly TO monitoring_role;
GRANT SELECT ON territory_security_health TO dashboard_role;

-- Function permissions
GRANT EXECUTE ON FUNCTION cleanup_expired_audit_logs() TO admin_role;
GRANT EXECUTE ON FUNCTION get_audit_statistics(timestamptz, timestamptz) TO security_team;
GRANT EXECUTE ON FUNCTION get_security_incidents(integer) TO security_team;

COMMIT;

-- === Usage Examples ===

-- Example 1: Daily Security Report
-- SELECT * FROM get_security_incidents(24);

-- Example 2: Monthly GDPR Compliance Report
-- SELECT * FROM get_audit_statistics(now() - INTERVAL '30 days', now());

-- Example 3: Territory Health Check
-- SELECT * FROM territory_security_health;

-- Example 4: Manual Cleanup (Admin only)
-- SELECT * FROM cleanup_expired_audit_logs();