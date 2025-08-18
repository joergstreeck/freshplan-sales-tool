-- ============================================================================
-- V10007: SEED Protection Trigger (TEST ONLY)
-- ============================================================================
-- Guard-Rail to prevent accidental deletion of SEED data during tests
-- This will help identify which code is deleting SEED customers
-- ============================================================================

-- Only create trigger in test/CI environments
DO $$
BEGIN
  -- Check if we're in test/CI environment
  IF current_setting('ci.build', true) <> 'true' THEN
    RAISE NOTICE 'V10007: Skipping SEED protection trigger (not in CI/test environment)';
    RETURN;
  END IF;

  -- Create or replace the trigger function
  CREATE OR REPLACE FUNCTION forbid_seed_delete() 
  RETURNS TRIGGER AS $func$
  BEGIN
    IF OLD.customer_number LIKE 'SEED-%' THEN
      RAISE EXCEPTION 'SEED PROTECTION: Attempting to delete SEED customer % (id=%, customer_number=%). This is forbidden in tests! Check your test cleanup code.', 
        OLD.company_name, OLD.id, OLD.customer_number
        USING HINT = 'SEED customers are protected test data and should never be deleted',
              DETAIL = 'If you need to modify SEED data, use UPDATE instead of DELETE/TRUNCATE';
    END IF;
    RETURN OLD;
  END;
  $func$ LANGUAGE plpgsql;

  -- Drop and recreate trigger to ensure it's active
  DROP TRIGGER IF EXISTS trg_forbid_seed_delete ON customers;
  
  CREATE TRIGGER trg_forbid_seed_delete
    BEFORE DELETE ON customers
    FOR EACH ROW 
    EXECUTE FUNCTION forbid_seed_delete();
    
  RAISE NOTICE 'V10007: SEED protection trigger installed successfully';
  
  -- Also protect against TRUNCATE (different approach needed)
  CREATE OR REPLACE FUNCTION forbid_truncate_customers()
  RETURNS EVENT_TRIGGER AS $func$
  DECLARE
    obj record;
  BEGIN
    FOR obj IN SELECT * FROM pg_event_trigger_ddl_commands()
    LOOP
      IF obj.command_tag = 'TRUNCATE TABLE' AND obj.object_identity = 'public.customers' THEN
        RAISE EXCEPTION 'SEED PROTECTION: TRUNCATE on customers table is forbidden in tests!'
          USING HINT = 'Use DELETE with proper WHERE clause instead';
      END IF;
    END LOOP;
  END;
  $func$ LANGUAGE plpgsql;
  
  -- Note: EVENT TRIGGER requires superuser, so we skip it if not possible
  BEGIN
    DROP EVENT TRIGGER IF EXISTS trg_forbid_truncate_customers;
    CREATE EVENT TRIGGER trg_forbid_truncate_customers
      ON sql_drop
      EXECUTE FUNCTION forbid_truncate_customers();
    RAISE NOTICE 'V10007: TRUNCATE protection also installed';
  EXCEPTION
    WHEN insufficient_privilege THEN
      RAISE NOTICE 'V10007: Could not install TRUNCATE protection (requires superuser)';
  END;
  
END $$;