-- V251__idempotency_tenant_unique_forward_fix.sql
-- Sprint 2.1.4: Forward fix for idempotency tenant-scoped uniqueness
-- This is a safe forward migration to fix multi-tenancy without editing V247
--
-- Author: team/leads-backend
-- Date: 2025-09-28

-- =====================================================
-- Make uniqueness tenant-scoped (safe forward fix)
-- =====================================================
DO $$
BEGIN
  -- Add the correct tenant-scoped unique constraint if missing
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint
    WHERE conname = 'idempotency_keys_tenant_key_unique'
  ) THEN
    -- First check if the table has the wrong single-column unique
    IF EXISTS (
      SELECT 1 FROM pg_constraint
      WHERE conname = 'idempotency_keys_idempotency_key_key'
        AND conrelid = 'idempotency_keys'::regclass
    ) THEN
      -- Drop the incorrect single-column constraint
      ALTER TABLE idempotency_keys
        DROP CONSTRAINT idempotency_keys_idempotency_key_key;
    END IF;

    -- Now add the correct multi-column unique constraint
    ALTER TABLE idempotency_keys
      ADD CONSTRAINT idempotency_keys_tenant_key_unique
      UNIQUE (tenant_id, idempotency_key);

    RAISE NOTICE 'Added tenant-scoped unique constraint to idempotency_keys';
  ELSE
    RAISE NOTICE 'Tenant-scoped unique constraint already exists';
  END IF;

  -- Ensure the expires_at index exists for cleanup performance
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