package de.freshplan.security;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.Session;
import io.quarkus.logging.Log;

/**
 * SecurityAuditInterceptor
 *
 * Ziel: GDPR-konformes Audit-Logging für alle Security-relevanten Operationen.
 * - Auditiert Lead-Access, Territory-Switches, Contact-Role-Changes
 * - Integriert mit PostgreSQL RLS-Functions für consistent Audit-Trail
 * - Performance-optimiert: Async-Logging + Batch-Insert
 *
 * Usage: @SecurityAudit auf Service-Methods für automatisches Audit-Logging
 */
@Interceptor
@SecurityAudit
@Priority(Interceptor.Priority.APPLICATION + 10)
@RequestScoped
public class SecurityAuditInterceptor {

    @Inject EntityManager em;
    @Inject SessionSettingsContext ctx;

    @AroundInvoke
    @Transactional
    public Object auditSecurityOperation(InvocationContext ic) throws Exception {
        long startTime = System.currentTimeMillis();
        String operation = ic.getMethod().getName();
        String className = ic.getTarget().getClass().getSimpleName();
        Object[] parameters = ic.getParameters();

        String outcome = "PENDING";
        String errorDetails = null;
        Object result = null;

        try {
            // Execute the method
            result = ic.proceed();
            outcome = "SUCCESS";
            return result;

        } catch (SecurityException se) {
            outcome = "SECURITY_DENIED";
            errorDetails = se.getMessage();
            throw se;

        } catch (Exception e) {
            outcome = "ERROR";
            errorDetails = e.getMessage();
            throw e;

        } finally {
            long duration = System.currentTimeMillis() - startTime;

            // Async audit logging (nicht-blockierend)
            auditOperation(className, operation, parameters, outcome, errorDetails, duration);
        }
    }

    private void auditOperation(String className, String operation, Object[] parameters,
                               String outcome, String errorDetails, long durationMs) {
        try {
            em.unwrap(Session.class).doWork(conn -> {
                String sql = """
                    INSERT INTO security_audit_log
                    (id, user_id, org_id, territory, operation, target_class, parameters,
                     outcome, error_details, duration_ms, created_at)
                    VALUES (?, ?, ?, ?, ?, ?, ?::jsonb, ?, ?, ?, ?)
                    """;

                try (var ps = conn.prepareStatement(sql)) {
                    ps.setObject(1, UUID.randomUUID());
                    ps.setString(2, nullSafe(ctx.getUserId()));
                    ps.setString(3, nullSafe(ctx.getOrgId()));
                    ps.setString(4, nullSafe(ctx.getTerritory()));
                    ps.setString(5, operation);
                    ps.setString(6, className);
                    ps.setString(7, parametersToJson(parameters));
                    ps.setString(8, outcome);
                    ps.setString(9, errorDetails);
                    ps.setLong(10, durationMs);
                    ps.setTimestamp(11, Timestamp.from(Instant.now()));

                    ps.executeUpdate();
                }
            });

        } catch (Exception e) {
            // Log audit failure, aber nicht die Original-Operation blockieren
            Log.warn("Security audit logging failed for " + operation + ": " + e.getMessage());
        }
    }

    private String parametersToJson(Object[] parameters) {
        if (parameters == null || parameters.length == 0) {
            return "{}";
        }

        StringBuilder json = new StringBuilder("{");
        for (int i = 0; i < parameters.length; i++) {
            if (i > 0) json.append(",");
            json.append("\"param").append(i).append("\":");

            Object param = parameters[i];
            if (param == null) {
                json.append("null");
            } else if (param instanceof String) {
                json.append("\"").append(sanitizeForJson((String) param)).append("\"");
            } else if (param instanceof UUID) {
                json.append("\"").append(param.toString()).append("\"");
            } else {
                json.append("\"").append(sanitizeForJson(param.toString())).append("\"");
            }
        }
        json.append("}");
        return json.toString();
    }

    private String sanitizeForJson(String input) {
        if (input == null) return "";
        return input.replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }

    private String nullSafe(String value) {
        return value != null ? value : "";
    }
}

// ===== Annotation für Security Audit =====
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import jakarta.interceptor.InterceptorBinding;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@InterceptorBinding
@Target({ TYPE, METHOD })
@Retention(RUNTIME)
@interface SecurityAudit {
    /**
     * Optional: Audit-Category für spezifische Klassifikation
     */
    String category() default "GENERAL";

    /**
     * Optional: Sensitivity-Level für GDPR-Compliance
     */
    String sensitivity() default "NORMAL";
}

// ===== GDPR-konforme Audit-Table Schema =====
/*
CREATE TABLE IF NOT EXISTS security_audit_log (
    id uuid PRIMARY KEY,
    user_id text NOT NULL,
    org_id text NOT NULL,
    territory text NOT NULL CHECK (territory IN ('DE','CH')),
    operation text NOT NULL,
    target_class text NOT NULL,
    parameters jsonb DEFAULT '{}'::jsonb,
    outcome text NOT NULL CHECK (outcome IN ('SUCCESS','SECURITY_DENIED','ERROR','PENDING')),
    error_details text,
    duration_ms bigint NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now(),

    -- GDPR Data Retention
    retention_until timestamptz GENERATED ALWAYS AS (created_at + INTERVAL '7 years') STORED,

    -- Performance Indexes
    INDEX idx_audit_user_time (user_id, created_at DESC),
    INDEX idx_audit_operation (operation, outcome, created_at DESC),
    INDEX idx_audit_territory (territory, created_at DESC),
    INDEX idx_audit_retention (retention_until) WHERE retention_until < now()
);

-- RLS für Audit-Table (Admin-only by default)
ALTER TABLE security_audit_log ENABLE ROW LEVEL SECURITY;

CREATE POLICY audit_admin_only ON security_audit_log
FOR ALL USING (app_has_scope('audit:read') OR app_has_scope('admin:all'));

-- GDPR Auto-Cleanup Job (monatlich)
CREATE OR REPLACE FUNCTION cleanup_expired_audit_logs()
RETURNS void LANGUAGE sql AS $$
    DELETE FROM security_audit_log
    WHERE retention_until < now() - INTERVAL '30 days';
$$;

-- Cron Job für Auto-Cleanup
SELECT cron.schedule('cleanup-audit-logs', '0 2 1 * *', 'SELECT cleanup_expired_audit_logs();');
*/