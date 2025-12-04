-- Sprint 2.1.8 - Phase 2: Lead-Import Tracking
-- Import-Log Tabelle für Self-Service Lead-Import mit Quota-System

-- ============================================================================
-- IMPORT_LOGS: Trackt alle Import-Vorgänge für Audit und Quota-Prüfung
-- ============================================================================

CREATE TABLE IF NOT EXISTS import_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- User & Zeitstempel
    user_id VARCHAR(255) NOT NULL,  -- Keycloak Subject (String, nicht UUID FK)
    imported_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    -- Statistiken
    total_rows INTEGER NOT NULL DEFAULT 0,
    imported_count INTEGER NOT NULL DEFAULT 0,
    skipped_count INTEGER NOT NULL DEFAULT 0,
    error_count INTEGER NOT NULL DEFAULT 0,
    duplicate_rate DECIMAL(5,2) DEFAULT 0.00,  -- Prozentsatz 0.00-100.00

    -- Import-Details
    source VARCHAR(255),          -- z.B. "MESSE_FRANKFURT_2025"
    file_name VARCHAR(255),       -- Original-Dateiname
    file_size_bytes BIGINT,       -- Dateigröße
    file_type VARCHAR(50),        -- CSV, XLSX, XLS

    -- Status-Tracking
    status VARCHAR(50) NOT NULL DEFAULT 'COMPLETED',  -- PENDING, COMPLETED, PENDING_APPROVAL, REJECTED

    -- Approval-Workflow (bei >10% Duplikaten)
    approved_by VARCHAR(255),     -- Keycloak Subject des Genehmigenden
    approved_at TIMESTAMP WITH TIME ZONE,
    rejection_reason TEXT,

    -- Audit-Felder
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- ============================================================================
-- INDEXES für Performance
-- ============================================================================

-- User-bezogene Abfragen (Quota-Check: "Imports heute")
CREATE INDEX idx_import_logs_user_date ON import_logs(user_id, imported_at DESC);

-- Status-Filter (Admin: "Wartende Genehmigungen")
CREATE INDEX idx_import_logs_status ON import_logs(status) WHERE status = 'PENDING_APPROVAL';

-- Chronologische Sortierung (Import-Historie)
CREATE INDEX idx_import_logs_imported_at ON import_logs(imported_at DESC);

-- ============================================================================
-- TRIGGER: updated_at automatisch setzen
-- ============================================================================

CREATE OR REPLACE FUNCTION update_import_logs_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_import_logs_updated_at
    BEFORE UPDATE ON import_logs
    FOR EACH ROW
    EXECUTE FUNCTION update_import_logs_updated_at();

-- ============================================================================
-- KOMMENTARE für Dokumentation
-- ============================================================================

COMMENT ON TABLE import_logs IS 'Sprint 2.1.8 - Trackt Lead-Import-Vorgänge für Audit, Quota und Approval-Workflow';
COMMENT ON COLUMN import_logs.user_id IS 'Keycloak Subject des importierenden Users';
COMMENT ON COLUMN import_logs.duplicate_rate IS 'Duplikatrate in Prozent (0.00-100.00). Bei >10% wird Approval benötigt';
COMMENT ON COLUMN import_logs.status IS 'PENDING=Upload läuft, COMPLETED=Fertig, PENDING_APPROVAL=Wartet auf Manager, REJECTED=Abgelehnt';
