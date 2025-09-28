-- V247__leads_normalization_deduplication.sql
-- Sprint 2.1.4: Lead Normalization and Soft-Deduplication (Additive Only)
-- Author: team/leads-backend
-- Date: 2025-09-28
--
-- IMPORTANT: This migration is ADDITIVE ONLY to avoid conflicts with existing schema

-- =====================================================
-- 1. ENABLE EXTENSIONS (Idempotent for Postgres)
-- =====================================================
DO $$
BEGIN
  -- Enable unaccent for diacritic normalization
  PERFORM 1 FROM pg_extension WHERE extname = 'unaccent';
  IF NOT FOUND THEN
    CREATE EXTENSION unaccent;
  END IF;
END$$;

-- =====================================================
-- 2. ADD NEW COLUMNS (Only if missing)
-- =====================================================
DO $$
BEGIN
  -- company_name_normalized for company deduplication
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_name = 'leads' AND column_name = 'company_name_normalized'
  ) THEN
    ALTER TABLE leads ADD COLUMN company_name_normalized TEXT;
  END IF;

  -- phone_e164 for standardized phone numbers
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_name = 'leads' AND column_name = 'phone_e164'
  ) THEN
    ALTER TABLE leads ADD COLUMN phone_e164 TEXT;
  END IF;

  -- is_canonical for soft deduplication
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_name = 'leads' AND column_name = 'is_canonical'
  ) THEN
    ALTER TABLE leads ADD COLUMN is_canonical BOOLEAN NOT NULL DEFAULT true;
  END IF;

  -- website_domain for domain-based dedup
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_name = 'leads' AND column_name = 'website_domain'
  ) THEN
    ALTER TABLE leads ADD COLUMN website_domain TEXT;
  END IF;

  -- source for lead origin tracking
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_name = 'leads' AND column_name = 'source'
  ) THEN
    ALTER TABLE leads ADD COLUMN source TEXT
      CHECK (source IN ('manual', 'import', 'partner', 'marketing', 'other'));
  END IF;
END$$;

-- =====================================================
-- 3. CREATE IDEMPOTENCY STORE TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS idempotency_keys (
  id BIGSERIAL PRIMARY KEY,
  tenant_id VARCHAR(50) DEFAULT 'default',
  idempotency_key VARCHAR(255) NOT NULL,
  request_hash VARCHAR(64) NOT NULL,
  response_status INTEGER NOT NULL,
  response_body TEXT,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
  UNIQUE(idempotency_key)
);

CREATE INDEX IF NOT EXISTS idx_idempotency_keys_expires_at
  ON idempotency_keys(expires_at);

COMMENT ON TABLE idempotency_keys IS
  'Stores idempotency keys for deduplicating API requests within TTL window';

-- =====================================================
-- 4. CREATE CLEANUP FUNCTION FOR EXPIRED KEYS
-- =====================================================
CREATE OR REPLACE FUNCTION cleanup_expired_idempotency_keys()
RETURNS INTEGER AS $$
DECLARE
  deleted_count INTEGER;
BEGIN
  DELETE FROM idempotency_keys
  WHERE expires_at < CURRENT_TIMESTAMP;

  GET DIAGNOSTICS deleted_count = ROW_COUNT;
  RETURN deleted_count;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION cleanup_expired_idempotency_keys() IS
  'Removes expired idempotency keys. Should be called periodically via cron or scheduled job';

-- =====================================================
-- 5. CREATE NEW INDICES (Non-conflicting names)
-- =====================================================
-- Email unique index with new name to avoid conflicts
CREATE UNIQUE INDEX IF NOT EXISTS uq_leads_email_canonical_v2
  ON leads(email_normalized)
  WHERE email_normalized IS NOT NULL
    AND is_canonical = true
    AND status != 'DELETED';

-- Company name + city index for fuzzy matching
CREATE INDEX IF NOT EXISTS idx_leads_company_norm_city
  ON leads(company_name_normalized, city)
  WHERE company_name_normalized IS NOT NULL;

-- Phone index for dedup
CREATE INDEX IF NOT EXISTS idx_leads_phone_e164
  ON leads(phone_e164)
  WHERE phone_e164 IS NOT NULL AND status != 'DELETED';

-- Website domain index
CREATE INDEX IF NOT EXISTS idx_leads_website_domain
  ON leads(website_domain)
  WHERE website_domain IS NOT NULL;

-- =====================================================
-- 6. UPDATE COLUMN COMMENTS
-- =====================================================
COMMENT ON COLUMN leads.company_name_normalized IS
  'Normalized company name (lowercase, no diacritics, single spaces)';
COMMENT ON COLUMN leads.phone_e164 IS
  'Phone number in E.164 format (+4930123456)';
COMMENT ON COLUMN leads.is_canonical IS
  'Flag for soft-deduplication (true = primary record, false = duplicate)';
COMMENT ON COLUMN leads.website_domain IS
  'Extracted domain from website/email for deduplication';
COMMENT ON COLUMN leads.source IS
  'Lead origin: manual, import, partner, marketing, other';