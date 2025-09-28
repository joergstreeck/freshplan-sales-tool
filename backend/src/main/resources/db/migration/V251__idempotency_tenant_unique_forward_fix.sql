-- V251__idempotency_tenant_unique_forward_fix.sql
-- Sprint 2.1.4: Forward fix for idempotency tenant-scoped uniqueness
-- This is a safe forward migration to fix multi-tenancy without editing V247
--
-- Author: team/leads-backend
-- Date: 2025-09-28

-- =====================================================
-- Make uniqueness tenant-scoped (safe forward fix)
-- =====================================================

-- Step A: Create the correct tenant-scoped unique index (needed for ON CONFLICT)
CREATE UNIQUE INDEX CONCURRENTLY IF NOT EXISTS ui_idk_tenant_key
  ON idempotency_keys(tenant_id, idempotency_key);

-- Step B: Remove any old single-column uniqueness constraints/indexes
DO $$
DECLARE
  idx_name text;
  con_name text;
BEGIN
  -- 1) Find and drop any unique INDEX on just (idempotency_key)
  SELECT i.indexname INTO idx_name
  FROM pg_indexes i
  WHERE i.tablename = 'idempotency_keys'
    AND i.indexdef ILIKE '%UNIQUE INDEX%'
    AND i.indexdef ILIKE '%(idempotency_key)%'
    AND i.indexdef NOT ILIKE '%tenant_id%'
  LIMIT 1;

  IF idx_name IS NOT NULL THEN
    RAISE NOTICE 'Dropping old single-column unique index: %', idx_name;
    EXECUTE format('DROP INDEX CONCURRENTLY IF EXISTS %I', idx_name);
  END IF;

  -- 2) Find and drop any UNIQUE CONSTRAINT on just (idempotency_key)
  SELECT c.conname INTO con_name
  FROM pg_constraint c
  JOIN pg_class t ON t.oid = c.conrelid AND t.relname = 'idempotency_keys'
  WHERE c.contype = 'u'
    AND array_length(c.conkey, 1) = 1
    AND EXISTS (
      SELECT 1 FROM pg_attribute a
      WHERE a.attrelid = c.conrelid
        AND a.attnum = c.conkey[1]
        AND a.attname = 'idempotency_key'
    );

  IF con_name IS NOT NULL THEN
    RAISE NOTICE 'Dropping old single-column unique constraint: %', con_name;
    EXECUTE format('ALTER TABLE idempotency_keys DROP CONSTRAINT %I', con_name);
  END IF;

  -- 3) Ensure the expires_at index exists for cleanup performance
  IF NOT EXISTS (
    SELECT 1 FROM pg_indexes
    WHERE indexname = 'idx_idempotency_keys_expires_at'
  ) THEN
    CREATE INDEX idx_idempotency_keys_expires_at
      ON idempotency_keys (expires_at);
    RAISE NOTICE 'Created index on idempotency_keys.expires_at';
  END IF;
END $$;

-- Add comments for documentation
COMMENT ON CONSTRAINT idempotency_keys_tenant_key_unique ON idempotency_keys IS
  'Ensures idempotency keys are unique per tenant (multi-tenant isolation)';