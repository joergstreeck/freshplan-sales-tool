-- V10005__seed_test_customers_fixed.sql
-- Creates exactly 20 seed customers that tests expect
-- Referenced by SeedDataVerificationTest
-- FIXED: Only uses columns that actually exist in customers table

DO $$
DECLARE
  i INTEGER;
  customer_id UUID;
BEGIN
  -- Create exactly 20 seed customers with minimal required fields
  FOR i IN 1..20 LOOP
    customer_id := gen_random_uuid();

    INSERT INTO customers (
      id,
      customer_number,
      company_name,
      created_at,
      created_by,
      updated_at,
      updated_by,
      status,
      is_test_data
    ) VALUES (
      customer_id,
      'SEED-V10005-' || i,
      'Seed Customer ' || i,
      NOW(),
      'migration',
      NOW(),
      'migration',
      'AKTIV',  -- German enum value
      true
    ) ON CONFLICT (customer_number) DO NOTHING;

    -- Add a primary contact for each customer
    INSERT INTO customer_contacts (
      id,
      customer_id,
      first_name,
      last_name,
      email,
      phone,
      position,
      is_primary,
      is_active,
      created_at,
      created_by,
      updated_at,
      updated_by
    ) VALUES (
      gen_random_uuid(),
      customer_id,
      'Contact',
      'Person ' || i,
      'contact' || i || '@seed-test.de',
      '+49 30 ' || (1000000 + i),
      'Test Position',
      true,
      true,
      NOW(),
      'migration',
      NOW(),
      'migration'
    ) ON CONFLICT DO NOTHING;
  END LOOP;

  RAISE NOTICE 'V10005: Created 20 seed customers with contacts for tests';
END $$;