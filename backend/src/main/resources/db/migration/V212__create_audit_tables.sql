-- V213: Create audit_logs table for comprehensive audit trail and DSGVO compliance
-- Author: FreshPlan Team
-- Date: 2025-08-08
-- Purpose: Complete audit trail for all system changes with tamper detection

CREATE TABLE IF NOT EXISTS audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    -- What was changed
    entity_type VARCHAR(100) NOT NULL CHECK (entity_type IN (
        'CUSTOMER', 'CONTACT', 'OPPORTUNITY', 'ORDER', 'USER', 
        'PERMISSION', 'CONFIGURATION', 'DOCUMENT', 'EMAIL', 'SYSTEM'
    )),
    entity_id UUID NOT NULL,
    entity_name VARCHAR(255),
    
    -- What action was performed
    action VARCHAR(50) NOT NULL CHECK (action IN (
        'CREATE', 'UPDATE', 'DELETE', 'VIEW', 'EXPORT', 'IMPORT',
        'BULK_UPDATE', 'BULK_DELETE', 'PERMISSION_CHANGE',
        'LOGIN', 'LOGOUT', 'FAILED_LOGIN', 'PASSWORD_CHANGE',
        'CONSENT_GIVEN', 'CONSENT_WITHDRAWN', 'DATA_REQUEST', 
        'DATA_DELETION', 'SYSTEM_EVENT'
    )),
    
    -- Who made the change
    user_id UUID NOT NULL,
    user_name VARCHAR(100) NOT NULL,
    user_role VARCHAR(50),
    
    -- When it happened
    occurred_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- From where
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    session_id VARCHAR(100),
    
    -- What changed
    old_values TEXT, -- JSON string of old values
    new_values TEXT, -- JSON string of new values
    changed_fields VARCHAR(1000), -- Comma-separated list
    
    -- Additional context
    reason VARCHAR(500),
    comment TEXT,
    request_id VARCHAR(100),
    
    -- DSGVO specific
    is_dsgvo_relevant BOOLEAN DEFAULT FALSE,
    legal_basis VARCHAR(50) CHECK (legal_basis IN (
        'CONSENT', 'CONTRACT', 'LEGAL_OBLIGATION', 
        'VITAL_INTERESTS', 'PUBLIC_TASK', 'LEGITIMATE_INTERESTS'
    )),
    consent_id UUID,
    retention_until TIMESTAMP,
    
    -- Technical metadata
    application_version VARCHAR(50),
    schema_version INTEGER,
    processing_time_ms BIGINT,
    
    -- Hierarchy for grouped changes
    parent_audit_id UUID,
    transaction_id VARCHAR(100),
    
    -- Security (for tamper detection)
    signature VARCHAR(500),
    previous_hash VARCHAR(100),
    current_hash VARCHAR(100)
);

-- Indizes für Performance (IF NOT EXISTS für Idempotenz)
CREATE INDEX IF NOT EXISTS idx_audit_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_audit_user ON audit_logs(user_id);
CREATE INDEX IF NOT EXISTS idx_audit_timestamp ON audit_logs(occurred_at DESC);
CREATE INDEX IF NOT EXISTS idx_audit_action ON audit_logs(action);
CREATE INDEX IF NOT EXISTS idx_audit_compliance ON audit_logs(is_dsgvo_relevant) WHERE is_dsgvo_relevant = TRUE;
CREATE INDEX IF NOT EXISTS idx_audit_transaction ON audit_logs(transaction_id) WHERE transaction_id IS NOT NULL;

-- Index für kritische Aktionen
CREATE INDEX IF NOT EXISTS idx_audit_critical ON audit_logs(action, occurred_at DESC) 
    WHERE action IN ('DELETE', 'BULK_DELETE', 'PERMISSION_CHANGE', 'DATA_DELETION');

-- Index für User-Activity Reports
CREATE INDEX IF NOT EXISTS idx_audit_user_activity ON audit_logs(user_id, occurred_at DESC);

-- Index für Entity-History
CREATE INDEX IF NOT EXISTS idx_audit_entity_history ON audit_logs(entity_type, entity_id, occurred_at DESC);

-- Index für DSGVO-Reports
CREATE INDEX IF NOT EXISTS idx_audit_dsgvo ON audit_logs(is_dsgvo_relevant, occurred_at DESC) 
    WHERE is_dsgvo_relevant = TRUE;

-- Index für Hash-Chain Validation
CREATE INDEX IF NOT EXISTS idx_audit_hash_chain ON audit_logs(previous_hash) 
    WHERE previous_hash IS NOT NULL;

-- Trigger-Funktion für automatische Hash-Berechnung
CREATE OR REPLACE FUNCTION calculate_audit_hash() RETURNS TRIGGER AS $$
DECLARE
    prev_hash VARCHAR(100);
    content_to_hash TEXT;
BEGIN
    -- Hole den Hash des vorherigen Eintrags
    SELECT current_hash INTO prev_hash 
    FROM audit_logs 
    ORDER BY occurred_at DESC, id DESC 
    LIMIT 1;
    
    -- Setze previous_hash
    NEW.previous_hash := prev_hash;
    
    -- Berechne den Hash für diesen Eintrag
    content_to_hash := COALESCE(NEW.entity_type::TEXT, '') || '|' ||
                      COALESCE(NEW.entity_id::TEXT, '') || '|' ||
                      COALESCE(NEW.action::TEXT, '') || '|' ||
                      COALESCE(NEW.user_id::TEXT, '') || '|' ||
                      COALESCE(NEW.occurred_at::TEXT, '') || '|' ||
                      COALESCE(NEW.old_values, '') || '|' ||
                      COALESCE(NEW.new_values, '') || '|' ||
                      COALESCE(prev_hash, 'GENESIS');
    
    NEW.current_hash := encode(digest(content_to_hash, 'sha256'), 'hex');
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger für Hash-Berechnung
CREATE TRIGGER audit_hash_trigger
    BEFORE INSERT ON audit_logs
    FOR EACH ROW
    EXECUTE FUNCTION calculate_audit_hash();

-- Kommentare zur Tabelle
COMMENT ON TABLE audit_logs IS 'Universeller Audit Trail für alle Systemänderungen mit DSGVO-Compliance und Tamper Detection';

COMMENT ON COLUMN audit_logs.entity_type IS 'Typ der geänderten Entität';
COMMENT ON COLUMN audit_logs.action IS 'Durchgeführte Aktion';
COMMENT ON COLUMN audit_logs.old_values IS 'JSON-String der alten Werte vor der Änderung';
COMMENT ON COLUMN audit_logs.new_values IS 'JSON-String der neuen Werte nach der Änderung';
COMMENT ON COLUMN audit_logs.changed_fields IS 'Komma-getrennte Liste der geänderten Felder';
COMMENT ON COLUMN audit_logs.is_dsgvo_relevant IS 'Markierung für DSGVO-relevante Vorgänge';
COMMENT ON COLUMN audit_logs.legal_basis IS 'Rechtsgrundlage für die Datenverarbeitung (DSGVO Art. 6)';
COMMENT ON COLUMN audit_logs.retention_until IS 'Aufbewahrungsfrist für DSGVO-Compliance';
COMMENT ON COLUMN audit_logs.transaction_id IS 'ID zur Gruppierung zusammengehöriger Änderungen';
COMMENT ON COLUMN audit_logs.signature IS 'Digitale Signatur für kritische Änderungen';
COMMENT ON COLUMN audit_logs.previous_hash IS 'Hash des vorherigen Audit-Eintrags für Hash-Chain';
COMMENT ON COLUMN audit_logs.current_hash IS 'SHA-256 Hash dieses Eintrags für Tamper Detection';