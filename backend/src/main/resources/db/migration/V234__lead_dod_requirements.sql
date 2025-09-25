-- V234: Lead DoD Requirements - Optimistic Locking, Email Normalization, Indexes
-- Sprint 2.1 PR #104: Complete DoD compliance

-- 1. Add optimistic locking to leads
ALTER TABLE leads ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;

-- 2. Email normalization for deduplication
ALTER TABLE leads ADD COLUMN IF NOT EXISTS email_normalized VARCHAR(320);

-- Create normalization function
CREATE OR REPLACE FUNCTION normalize_email(email_input TEXT) RETURNS TEXT AS $$
BEGIN
    IF email_input IS NULL THEN
        RETURN NULL;
    END IF;
    RETURN lower(trim(email_input));
END;
$$ LANGUAGE plpgsql IMMUTABLE;

-- Normalize existing emails
UPDATE leads SET email_normalized = normalize_email(email) WHERE email_normalized IS NULL;

-- Create unique index for normalized emails (single-tenant for now)
CREATE UNIQUE INDEX IF NOT EXISTS idx_leads_email_normalized
ON leads(email_normalized)
WHERE email_normalized IS NOT NULL AND status != 'DELETED';

-- 3. Performance indexes for hot paths
CREATE INDEX IF NOT EXISTS idx_leads_owner_status ON leads(owner_user_id, status);
CREATE INDEX IF NOT EXISTS idx_leads_updated_at ON leads(updated_at DESC);
CREATE INDEX IF NOT EXISTS idx_leads_company_name ON leads(company_name);
CREATE INDEX IF NOT EXISTS idx_leads_created_at ON leads(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_leads_status ON leads(status) WHERE status != 'DELETED';

-- 4. Lead activities performance index
CREATE INDEX IF NOT EXISTS idx_lead_activities_lead_created
ON lead_activities(lead_id, activity_date DESC);

-- 5. RLS preparation (enabled in next migration)
COMMENT ON TABLE leads IS 'Lead management with user-based protection (6/60/10 rule), no geographical protection';
COMMENT ON COLUMN leads.version IS 'Optimistic locking version for concurrent update detection';
COMMENT ON COLUMN leads.email_normalized IS 'Normalized email for deduplication (lowercase, trimmed)';

-- 6. Add missing columns if not exists
ALTER TABLE leads ADD COLUMN IF NOT EXISTS expired_at TIMESTAMP;
ALTER TABLE leads ADD COLUMN IF NOT EXISTS grace_period_start_at TIMESTAMP;
ALTER TABLE leads ADD COLUMN IF NOT EXISTS reminder_sent_at TIMESTAMP;