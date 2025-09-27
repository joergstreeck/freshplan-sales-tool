-- V247__leads_normalization_deduplication.sql
-- Sprint 2.1.4: Lead Normalization and Soft-Deduplication
-- Author: team/leads-backend
-- Date: 2025-09-28

-- =====================================================
-- 1. ENABLE CITEXT EXTENSION (if not exists)
-- =====================================================
CREATE EXTENSION IF NOT EXISTS citext;
CREATE EXTENSION IF NOT EXISTS unaccent;

-- =====================================================
-- 2. ADD NORMALIZED COLUMNS TO LEADS TABLE
-- =====================================================
ALTER TABLE leads
  ADD COLUMN IF NOT EXISTS email_normalized CITEXT,
  ADD COLUMN IF NOT EXISTS name_normalized TEXT,
  ADD COLUMN IF NOT EXISTS phone_e164 TEXT;

COMMENT ON COLUMN leads.email_normalized IS 'Normalized email for deduplication (lowercase, trimmed)';
COMMENT ON COLUMN leads.name_normalized IS 'Normalized name (lowercase, no diacritics, single spaces)';
COMMENT ON COLUMN leads.phone_e164 IS 'Phone number in E.164 format (+4930123456)';

-- =====================================================
-- 3. CREATE NORMALIZATION FUNCTIONS
-- =====================================================

-- Email normalization function
CREATE OR REPLACE FUNCTION normalize_email(email_input TEXT)
RETURNS CITEXT AS $$
BEGIN
  IF email_input IS NULL OR TRIM(email_input) = '' THEN
    RETURN NULL;
  END IF;

  -- Remove leading/trailing whitespace and convert to lowercase
  -- CITEXT handles case-insensitivity automatically
  RETURN LOWER(TRIM(email_input));
END;
$$ LANGUAGE plpgsql IMMUTABLE;

-- Name normalization function
CREATE OR REPLACE FUNCTION normalize_name(name_input TEXT)
RETURNS TEXT AS $$
BEGIN
  IF name_input IS NULL OR TRIM(name_input) = '' THEN
    RETURN NULL;
  END IF;

  -- Remove diacritics, normalize whitespace, lowercase
  RETURN LOWER(
    regexp_replace(
      unaccent(TRIM(name_input)),
      '\s+',
      ' ',
      'g'
    )
  );
END;
$$ LANGUAGE plpgsql IMMUTABLE;

-- =====================================================
-- 4. BACKFILL EXISTING DATA (in batches for large tables)
-- =====================================================

-- Update existing records with normalized values
-- Using COALESCE to avoid overwriting if already set
UPDATE leads
SET
  email_normalized = COALESCE(email_normalized, normalize_email(email)),
  name_normalized = COALESCE(name_normalized, normalize_name(name))
WHERE email_normalized IS NULL
   OR name_normalized IS NULL;

-- Note: phone_e164 requires application-level normalization with libphonenumber
-- It will be populated by the application during insert/update

-- =====================================================
-- 5. CREATE PARTIAL UNIQUE INDEX FOR SOFT-DEDUPLICATION
-- =====================================================

-- This prevents new duplicates while allowing historical ones
-- CONCURRENTLY to avoid locking the table
CREATE UNIQUE INDEX CONCURRENTLY IF NOT EXISTS uq_leads_tenant_email_normalized
ON leads(tenant_id, email_normalized)
WHERE email_normalized IS NOT NULL;

COMMENT ON INDEX uq_leads_tenant_email_normalized IS
'Prevents duplicate leads with same email within a tenant (soft-deduplication)';

-- Optional: Index for phone deduplication (can be added later)
-- CREATE UNIQUE INDEX CONCURRENTLY IF NOT EXISTS uq_leads_tenant_phone_e164
-- ON leads(tenant_id, phone_e164)
-- WHERE phone_e164 IS NOT NULL;

-- =====================================================
-- 6. CREATE IDEMPOTENCY STORE TABLE
-- =====================================================

CREATE TABLE IF NOT EXISTS idempotency_keys (
  id BIGSERIAL PRIMARY KEY,
  tenant_id VARCHAR(50) NOT NULL,
  idempotency_key VARCHAR(255) NOT NULL,
  request_hash VARCHAR(64) NOT NULL,
  response_status INTEGER NOT NULL,
  response_body TEXT,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
  UNIQUE(tenant_id, idempotency_key)
);

CREATE INDEX IF NOT EXISTS idx_idempotency_keys_expires_at
ON idempotency_keys(expires_at);

COMMENT ON TABLE idempotency_keys IS
'Stores idempotency keys for deduplicating API requests within TTL window';

-- =====================================================
-- 7. CREATE CLEANUP FUNCTION FOR EXPIRED KEYS
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
-- 8. ADD TRIGGERS FOR AUTO-NORMALIZATION (optional)
-- =====================================================

-- Trigger function to auto-normalize on insert/update
CREATE OR REPLACE FUNCTION leads_normalize_trigger()
RETURNS TRIGGER AS $$
BEGIN
  NEW.email_normalized := normalize_email(NEW.email);
  NEW.name_normalized := normalize_name(NEW.name);
  -- phone_e164 must be set by application (requires libphonenumber)
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create trigger
CREATE TRIGGER leads_normalize_before_insert_update
BEFORE INSERT OR UPDATE ON leads
FOR EACH ROW
EXECUTE FUNCTION leads_normalize_trigger();

-- =====================================================
-- 9. GRANT PERMISSIONS
-- =====================================================

-- Ensure application user has necessary permissions
-- Replace 'freshplan_app' with your actual application user
-- GRANT SELECT, INSERT, UPDATE, DELETE ON leads TO freshplan_app;
-- GRANT SELECT, INSERT, UPDATE, DELETE ON idempotency_keys TO freshplan_app;
-- GRANT USAGE, SELECT ON SEQUENCE idempotency_keys_id_seq TO freshplan_app;

-- =====================================================
-- ROLLBACK SCRIPT (save separately as V247__rollback.sql)
-- =====================================================
-- DROP TRIGGER IF EXISTS leads_normalize_before_insert_update ON leads;
-- DROP FUNCTION IF EXISTS leads_normalize_trigger();
-- DROP FUNCTION IF EXISTS cleanup_expired_idempotency_keys();
-- DROP TABLE IF EXISTS idempotency_keys;
-- DROP INDEX IF EXISTS uq_leads_tenant_email_normalized;
-- DROP FUNCTION IF EXISTS normalize_name(TEXT);
-- DROP FUNCTION IF EXISTS normalize_email(TEXT);
-- ALTER TABLE leads
--   DROP COLUMN IF EXISTS email_normalized,
--   DROP COLUMN IF EXISTS name_normalized,
--   DROP COLUMN IF EXISTS phone_e164;