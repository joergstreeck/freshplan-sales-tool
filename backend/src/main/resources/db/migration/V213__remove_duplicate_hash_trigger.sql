-- Migration V213: Remove duplicate hash calculation trigger
-- The hash calculation is handled in the Java application layer (AuditService)
-- Maintaining the same logic in two places is error-prone

-- Drop the trigger
DROP TRIGGER IF EXISTS audit_hash_trigger ON audit_logs;

-- Drop the trigger function
DROP FUNCTION IF EXISTS calculate_audit_hash();

-- Add comment explaining hash management
COMMENT ON COLUMN audit_logs.current_hash IS 'SHA-256 Hash des Eintrags, berechnet im AuditService (Java-Layer)';
COMMENT ON COLUMN audit_logs.previous_hash IS 'Hash des vorherigen Eintrags für Chain-Validation, gesetzt im AuditService';