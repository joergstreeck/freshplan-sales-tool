-- V247__leads_normalization_deduplication.sql
-- Sprint 2.1.4: Lead Normalization and Soft-Deduplication (Idempotent Version)
-- Author: team/leads-backend
-- Date: 2025-09-28
--
-- NOTE: This migration extends V234 which already created email_normalized column as VARCHAR(320)

-- =====================================================
-- 1. ENABLE EXTENSIONS (Idempotent for Postgres)
-- =====================================================
DO $$
BEGIN
  -- Enable CITEXT extension for case-insensitive text
  PERFORM 1 FROM pg_extension WHERE extname = 'citext';
  IF NOT FOUND THEN
    CREATE EXTENSION citext;
  END IF;

  -- Enable unaccent for diacritic normalization
  PERFORM 1 FROM pg_extension WHERE extname = 'unaccent';
  IF NOT FOUND THEN
    CREATE EXTENSION unaccent;
  END IF;
END$$;

-- =====================================================
-- 2. ADD NEW NORMALIZED COLUMNS (Only if missing)
-- =====================================================
DO $$
BEGIN
  -- name_normalized for name deduplication
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_name = 'leads' AND column_name = 'name_normalized'
  ) THEN
    ALTER TABLE leads ADD COLUMN name_normalized TEXT;
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
END$$;

-- =====================================================
-- 3. DROP OLD INDEXES FIRST (BEFORE ANY DATA UPDATES)
-- =====================================================
-- Must drop ALL old unique indexes on email_normalized before updating data
-- to avoid constraint violations during the update
DROP INDEX IF EXISTS idx_leads_email_normalized;  -- From V234
DROP INDEX IF EXISTS ux_leads_email_norm_active;  -- May exist from other migrations

-- =====================================================
-- 4. UPDATE NORMALIZATION FUNCTIONS (Replace V234 version)
-- =====================================================
-- Drop old function from V234 if exists (returns TEXT instead of VARCHAR)
DROP FUNCTION IF EXISTS normalize_email(TEXT);

-- Create enhanced email normalization (returns VARCHAR to match column type)
CREATE OR REPLACE FUNCTION normalize_email(s TEXT) RETURNS VARCHAR AS $$
  SELECT NULLIF(LOWER(TRIM(s)), '')::VARCHAR;
$$ LANGUAGE sql IMMUTABLE;

-- Create name normalization function
CREATE OR REPLACE FUNCTION normalize_name(s TEXT) RETURNS TEXT AS $$
BEGIN
  IF s IS NULL OR TRIM(s) = '' THEN
    RETURN NULL;
  END IF;
  RETURN regexp_replace(LOWER(unaccent(TRIM(s))), '\s+', ' ', 'g');
END;
$$ LANGUAGE plpgsql IMMUTABLE;

-- =====================================================
-- 5. BACKFILL DATA & HANDLE EXISTING DUPLICATES
-- =====================================================
-- Re-normalize emails with new function (idempotent)
UPDATE leads
SET email_normalized = normalize_email(email)
WHERE email IS NOT NULL
  AND email_normalized IS DISTINCT FROM normalize_email(email);

-- Note: Name normalization skipped - leads table uses company_name, not name
-- This can be added in a future migration if needed

-- Mark duplicates as non-canonical BEFORE creating unique index
-- This handles existing duplicate data from before Sprint 2.1.4
-- NOTE: System is currently single-tenant, no tenant_id field yet
WITH duplicate_emails AS (
  SELECT id,
         email_normalized,
         ROW_NUMBER() OVER (
           PARTITION BY email_normalized
           ORDER BY created_at, id
         ) AS rn
  FROM leads
  WHERE email_normalized IS NOT NULL
    AND status != 'DELETED'
)
UPDATE leads
SET is_canonical = false
FROM duplicate_emails
WHERE leads.id = duplicate_emails.id
  AND duplicate_emails.rn > 1
  AND leads.is_canonical = true;  -- Only update if not already marked

-- =====================================================
-- 10. CREATE NEW UNIQUE INDEX

-- Create new tenant-scoped unique index with soft-delete support
-- Note: Using regular CREATE INDEX for transactional safety
-- Production will use V248 Java migration for CONCURRENTLY
-- NOTE: Single-tenant system, no tenant_id yet
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM pg_indexes
    WHERE schemaname = 'public'
    AND indexname = 'uq_leads_email_canonical'
  ) THEN
    CREATE UNIQUE INDEX uq_leads_email_canonical
      ON leads(email_normalized)
      WHERE email_normalized IS NOT NULL
        AND is_canonical = true
        AND status != 'DELETED';
  END IF;
END$$;

-- Create index for phone deduplication (optional, not unique yet)
-- NOTE: Single-tenant, no tenant_id
CREATE INDEX IF NOT EXISTS idx_leads_phone_e164
  ON leads(phone_e164)
  WHERE phone_e164 IS NOT NULL AND status != 'DELETED';

-- =====================================================
-- 10. CREATE IDEMPOTENCY STORE TABLE
-- =====================================================
-- NOTE: Currently single-tenant, but keeping tenant_id for future multi-tenancy
CREATE TABLE IF NOT EXISTS idempotency_keys (
  id BIGSERIAL PRIMARY KEY,
  tenant_id VARCHAR(50) DEFAULT 'default',  -- Default tenant for now
  idempotency_key VARCHAR(255) NOT NULL,
  request_hash VARCHAR(64) NOT NULL,
  response_status INTEGER NOT NULL,
  response_body TEXT,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
  UNIQUE(idempotency_key)  -- Unique per key (single-tenant for now)
);

CREATE INDEX IF NOT EXISTS idx_idempotency_keys_expires_at
  ON idempotency_keys(expires_at);

COMMENT ON TABLE idempotency_keys IS
  'Stores idempotency keys for deduplicating API requests within TTL window';

-- =====================================================
-- 10. CREATE CLEANUP FUNCTION FOR EXPIRED KEYS
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
-- 10. ADD TRIGGERS FOR AUTO-NORMALIZATION
-- =====================================================
CREATE OR REPLACE FUNCTION leads_normalize_trigger()
RETURNS TRIGGER AS $$
BEGIN
  NEW.email_normalized := normalize_email(NEW.email);
  -- name_normalized not set here - leads table has company_name, not name
  -- phone_e164 must be set by application (requires libphonenumber)
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Drop and recreate trigger to ensure idempotency
DROP TRIGGER IF EXISTS leads_normalize_before_insert_update ON leads;
CREATE TRIGGER leads_normalize_before_insert_update
  BEFORE INSERT OR UPDATE ON leads
  FOR EACH ROW
  EXECUTE FUNCTION leads_normalize_trigger();

-- =====================================================
-- 10. UPDATE COLUMN COMMENTS
-- =====================================================
COMMENT ON COLUMN leads.email_normalized IS
  'Normalized email for deduplication (lowercase, trimmed) - VARCHAR(320)';
COMMENT ON COLUMN leads.name_normalized IS
  'Normalized name (lowercase, no diacritics, single spaces)';
COMMENT ON COLUMN leads.phone_e164 IS
  'Phone number in E.164 format (+4930123456)';
COMMENT ON COLUMN leads.is_canonical IS
  'Flag for soft-deduplication (true = primary record, false = duplicate)';
COMMENT ON INDEX uq_leads_email_canonical IS
  'Prevents duplicate canonical leads with same email (single-tenant system)';