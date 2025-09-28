-- R__test_seeds.sql
-- Repeatable migration for test seed data
-- Ensures tests always have required base data

-- Create 20 test customers that tests expect
-- Uses ON CONFLICT DO NOTHING for idempotency
DO $$
DECLARE
  i INTEGER;
BEGIN
  -- Only insert in test/CI environments
  IF current_setting('server.environment', true) IN ('test', 'ci') OR
     current_database() IN ('test_db', 'freshplan_test') OR
     current_schema() LIKE 'ci_%' THEN

    -- Ensure we have at least 20 test customers
    FOR i IN 1..20 LOOP
      INSERT INTO customers (
        id,
        customer_number,
        company_name,
        created_at,
        updated_at,
        status,
        is_test_data
      ) VALUES (
        gen_random_uuid(),
        'TEST-SEED-' || i,
        'Test Customer ' || i,
        NOW(),
        NOW(),
        'ACTIVE',
        true
      ) ON CONFLICT (customer_number) DO NOTHING;
    END LOOP;

    RAISE NOTICE 'Test seeds: Ensured 20 test customers exist';
  ELSE
    RAISE NOTICE 'Test seeds: Skipped (not in test environment)';
  END IF;
END $$;