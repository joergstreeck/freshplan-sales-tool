-- V224: Remove redundant trigger and function from audit_trail
-- Purpose: Clean up trigger that was causing issues with partitioned tables
-- The timestamp DEFAULT constraint handles this functionality already
-- Created: 2025-08-16

-- Idempotent: cleans up historical installations
DO $$
BEGIN
  IF to_regclass('public.audit_trail') IS NOT NULL THEN
    -- Remove trigger if it exists
    IF EXISTS (
      SELECT 1 FROM pg_trigger
      WHERE tgrelid = 'public.audit_trail'::regclass
        AND tgname = 'audit_trail_timestamp_trigger'
    ) THEN
      EXECUTE 'DROP TRIGGER IF EXISTS audit_trail_timestamp_trigger ON public.audit_trail';
      RAISE NOTICE 'Dropped trigger audit_trail_timestamp_trigger';
    END IF;

    -- Remove function if it exists
    IF EXISTS (
      SELECT 1 FROM pg_proc 
      WHERE proname = 'audit_trail_set_timestamp'
        AND pronamespace = (SELECT oid FROM pg_namespace WHERE nspname = 'public')
    ) THEN
      EXECUTE 'DROP FUNCTION IF EXISTS public.audit_trail_set_timestamp() CASCADE';
      RAISE NOTICE 'Dropped function audit_trail_set_timestamp';
    END IF;

    -- Ensure DEFAULT constraint is set
    EXECUTE 'ALTER TABLE public.audit_trail
             ALTER COLUMN "timestamp" SET DEFAULT (CURRENT_TIMESTAMP),
             ALTER COLUMN "timestamp" SET NOT NULL';
    RAISE NOTICE 'Ensured timestamp DEFAULT constraint is set';
  END IF;
END $$;