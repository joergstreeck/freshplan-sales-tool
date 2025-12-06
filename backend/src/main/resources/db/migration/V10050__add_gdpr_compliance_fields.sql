-- ============================================================================
-- V10050: DSGVO Compliance Fields & Tables
-- Sprint 2.1.8: DSGVO Compliance & Lead-Import
-- ============================================================================
-- Art. 15: Auskunftsrecht (Datenexport-Requests)
-- Art. 17: Löschrecht (Soft-Delete + PII-Anonymisierung)
-- Art. 7.3: Einwilligungswiderruf (Consent Revocation + Contact Block)
-- ============================================================================

-- ============================================================================
-- 1. DSGVO-Felder in leads Tabelle
-- ============================================================================

-- Art. 7.3: Einwilligungswiderruf
ALTER TABLE leads ADD COLUMN IF NOT EXISTS consent_revoked_at TIMESTAMP;
ALTER TABLE leads ADD COLUMN IF NOT EXISTS consent_revoked_by VARCHAR(50);
ALTER TABLE leads ADD COLUMN IF NOT EXISTS contact_blocked BOOLEAN DEFAULT FALSE;

-- Art. 17: DSGVO-Löschung (Soft-Delete + PII-Anonymisierung)
ALTER TABLE leads ADD COLUMN IF NOT EXISTS gdpr_deleted BOOLEAN DEFAULT FALSE;
ALTER TABLE leads ADD COLUMN IF NOT EXISTS gdpr_deleted_at TIMESTAMP;
ALTER TABLE leads ADD COLUMN IF NOT EXISTS gdpr_deleted_by VARCHAR(50);
ALTER TABLE leads ADD COLUMN IF NOT EXISTS gdpr_deletion_reason VARCHAR(500);

-- Indexes für DSGVO-Queries
CREATE INDEX IF NOT EXISTS idx_leads_gdpr_deleted ON leads(gdpr_deleted) WHERE gdpr_deleted = TRUE;
CREATE INDEX IF NOT EXISTS idx_leads_contact_blocked ON leads(contact_blocked) WHERE contact_blocked = TRUE;
CREATE INDEX IF NOT EXISTS idx_leads_consent_revoked ON leads(consent_revoked_at) WHERE consent_revoked_at IS NOT NULL;

-- ============================================================================
-- 2. DSGVO Data Requests Table (Art. 15 Auskunftsrecht)
-- ============================================================================

CREATE TABLE IF NOT EXISTS gdpr_data_requests (
    id BIGSERIAL PRIMARY KEY,
    entity_type VARCHAR(50) NOT NULL,  -- 'LEAD', 'CUSTOMER'
    entity_id BIGINT NOT NULL,
    requested_by VARCHAR(50) NOT NULL,
    requested_at TIMESTAMP NOT NULL DEFAULT NOW(),
    pdf_generated BOOLEAN DEFAULT FALSE,
    pdf_generated_at TIMESTAMP,
    notes VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_gdpr_data_requests_entity ON gdpr_data_requests(entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_gdpr_data_requests_requested_at ON gdpr_data_requests(requested_at);
CREATE INDEX IF NOT EXISTS idx_gdpr_data_requests_requested_by ON gdpr_data_requests(requested_by);

COMMENT ON TABLE gdpr_data_requests IS 'DSGVO Art. 15 - Auskunftsrecht: Tracking von Datenauskunfts-Anfragen';
COMMENT ON COLUMN gdpr_data_requests.entity_type IS 'Entitätstyp: LEAD oder CUSTOMER';
COMMENT ON COLUMN gdpr_data_requests.entity_id IS 'ID der betroffenen Entität';
COMMENT ON COLUMN gdpr_data_requests.requested_by IS 'User-ID des Anfragenden (Manager/Admin)';
COMMENT ON COLUMN gdpr_data_requests.pdf_generated IS 'Wurde PDF erfolgreich generiert?';

-- ============================================================================
-- 3. DSGVO Deletion Logs Table (Art. 17 Löschrecht - Audit Trail)
-- ============================================================================

CREATE TABLE IF NOT EXISTS gdpr_deletion_logs (
    id BIGSERIAL PRIMARY KEY,
    entity_type VARCHAR(50) NOT NULL,  -- 'LEAD', 'CUSTOMER'
    entity_id BIGINT NOT NULL,
    deleted_by VARCHAR(50) NOT NULL,
    deleted_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deletion_reason VARCHAR(500) NOT NULL,
    original_data_hash VARCHAR(64),  -- SHA-256 für Audit-Nachweis
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_gdpr_deletion_logs_entity ON gdpr_deletion_logs(entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_gdpr_deletion_logs_deleted_at ON gdpr_deletion_logs(deleted_at);
CREATE INDEX IF NOT EXISTS idx_gdpr_deletion_logs_deleted_by ON gdpr_deletion_logs(deleted_by);

COMMENT ON TABLE gdpr_deletion_logs IS 'DSGVO Art. 17 - Löschrecht: Audit-Trail für DSGVO-konforme Löschungen';
COMMENT ON COLUMN gdpr_deletion_logs.entity_type IS 'Entitätstyp: LEAD oder CUSTOMER';
COMMENT ON COLUMN gdpr_deletion_logs.entity_id IS 'ID der gelöschten Entität';
COMMENT ON COLUMN gdpr_deletion_logs.deleted_by IS 'User-ID des Löschenden (Manager/Admin)';
COMMENT ON COLUMN gdpr_deletion_logs.deletion_reason IS 'DSGVO-konformer Löschgrund';
COMMENT ON COLUMN gdpr_deletion_logs.original_data_hash IS 'SHA-256 Hash der Original-Daten für Nachweisführung';

-- ============================================================================
-- 4. DSGVO-Felder in customers Tabelle (für Customer DSGVO-Support)
-- ============================================================================

-- Art. 7.3: Einwilligungswiderruf
ALTER TABLE customers ADD COLUMN IF NOT EXISTS consent_revoked_at TIMESTAMP;
ALTER TABLE customers ADD COLUMN IF NOT EXISTS consent_revoked_by VARCHAR(50);
ALTER TABLE customers ADD COLUMN IF NOT EXISTS contact_blocked BOOLEAN DEFAULT FALSE;

-- Art. 17: DSGVO-Löschung
ALTER TABLE customers ADD COLUMN IF NOT EXISTS gdpr_deleted BOOLEAN DEFAULT FALSE;
ALTER TABLE customers ADD COLUMN IF NOT EXISTS gdpr_deleted_at TIMESTAMP;
ALTER TABLE customers ADD COLUMN IF NOT EXISTS gdpr_deleted_by VARCHAR(50);
ALTER TABLE customers ADD COLUMN IF NOT EXISTS gdpr_deletion_reason VARCHAR(500);

-- Indexes für Customer DSGVO-Queries
CREATE INDEX IF NOT EXISTS idx_customers_gdpr_deleted ON customers(gdpr_deleted) WHERE gdpr_deleted = TRUE;
CREATE INDEX IF NOT EXISTS idx_customers_contact_blocked ON customers(contact_blocked) WHERE contact_blocked = TRUE;
