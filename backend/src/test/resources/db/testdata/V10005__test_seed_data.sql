-- ============================================================================
-- V10005: CI-only idempotent SEED data - Exactly 20 customers
-- ============================================================================
-- Creates or updates exactly 20 SEED customers for testing
-- Uses DO UPDATE to heal any incorrectly marked existing seeds
-- Only runs in CI/Test environments
-- ============================================================================

DO $$
DECLARE
  seed_count INTEGER;
BEGIN
  -- Guard: Only run in CI/Test environments
  IF current_setting('ci.build', true) <> 'true' THEN
    RAISE NOTICE 'V10005 seeds skipped (not CI, ci.build=%)', current_setting('ci.build', true);
    RETURN;
  END IF;

  RAISE NOTICE 'V10005 seed data starting in CI environment';
  
  -- Ensure pgcrypto extension for gen_random_uuid()
  CREATE EXTENSION IF NOT EXISTS pgcrypto;

  -- Always recreate SEED data if missing (idempotent)
  -- First, check if SEED data exists
  SELECT COUNT(*) INTO seed_count FROM customers WHERE customer_number LIKE 'SEED-%';
  IF seed_count < 20 THEN
    RAISE NOTICE 'V10005: Only % SEED customers found, recreating all 20', seed_count;
    -- Delete incomplete SEED data
    DELETE FROM customers WHERE customer_number LIKE 'SEED-%';
  ELSE
    RAISE NOTICE 'V10005: All 20 SEED customers already exist, skipping';
    RETURN;
  END IF;
  
  -- Insert or update all 20 SEED customers
  INSERT INTO customers (
      id, 
      customer_number, 
      company_name, 
      customer_type, 
      status,
      expected_annual_volume,
      risk_score,
      is_test_data, 
      is_deleted, 
      created_by, 
      created_at
  ) VALUES 
      -- Scenario 1-5: Active customers
      (gen_random_uuid(), 'SEED-001', '[SEED] Restaurant München', 'UNTERNEHMEN', 'AKTIV', 500000, 5, true, false, 'seed', NOW() - INTERVAL '2 years'),
      (gen_random_uuid(), 'SEED-002', '[SEED] Hotel Berlin', 'UNTERNEHMEN', 'AKTIV', 300000, 10, true, false, 'seed', NOW() - INTERVAL '18 months'),
      (gen_random_uuid(), 'SEED-003', '[SEED] Catering Frankfurt', 'UNTERNEHMEN', 'AKTIV', 200000, 15, true, false, 'seed', NOW() - INTERVAL '1 year'),
      (gen_random_uuid(), 'SEED-004', '[SEED] Kantine Hamburg', 'UNTERNEHMEN', 'AKTIV', 150000, 20, true, false, 'seed', NOW() - INTERVAL '6 months'),
      (gen_random_uuid(), 'SEED-005', '[SEED] Bio-Markt Dresden', 'UNTERNEHMEN', 'AKTIV', 100000, 25, true, false, 'seed', NOW() - INTERVAL '3 months'),
      
      -- Scenario 6-10: Risk and inactive customers  
      (gen_random_uuid(), 'SEED-006', '[SEED] Bäckerei Stuttgart', 'UNTERNEHMEN', 'RISIKO', 50000, 70, true, false, 'seed', NOW() - INTERVAL '2 years'),
      (gen_random_uuid(), 'SEED-007', '[SEED] Metzgerei Köln', 'UNTERNEHMEN', 'RISIKO', 40000, 80, true, false, 'seed', NOW() - INTERVAL '1 year'),
      (gen_random_uuid(), 'SEED-008', '[SEED] Café Leipzig', 'UNTERNEHMEN', 'INAKTIV', 0, 90, true, false, 'seed', NOW() - INTERVAL '3 years'),
      (gen_random_uuid(), 'SEED-009', '[SEED] Bar Düsseldorf', 'UNTERNEHMEN', 'INAKTIV', 0, 95, true, false, 'seed', NOW() - INTERVAL '4 years'),
      (gen_random_uuid(), 'SEED-010', '[SEED] Club Essen', 'UNTERNEHMEN', 'ARCHIVIERT', 0, 100, true, false, 'seed', NOW() - INTERVAL '5 years'),
      
      -- Scenario 11-15: Leads and prospects
      (gen_random_uuid(), 'SEED-011', '[SEED] New Lead Restaurant', 'NEUKUNDE', 'LEAD', 75000, 30, true, false, 'seed', NOW()),
      (gen_random_uuid(), 'SEED-012', '[SEED] Qualified Lead Hotel', 'NEUKUNDE', 'LEAD', 100000, 35, true, false, 'seed', NOW() - INTERVAL '1 week'),
      (gen_random_uuid(), 'SEED-013', '[SEED] Angebot Catering', 'NEUKUNDE', 'PROSPECT', 125000, 40, true, false, 'seed', NOW() - INTERVAL '2 weeks'),
      (gen_random_uuid(), 'SEED-014', '[SEED] Negotiation Kantine', 'NEUKUNDE', 'PROSPECT', 150000, 45, true, false, 'seed', NOW() - INTERVAL '3 weeks'),
      (gen_random_uuid(), 'SEED-015', '[SEED] Won Deal Bäckerei', 'UNTERNEHMEN', 'AKTIV', 175000, 50, true, false, 'seed', NOW() - INTERVAL '1 month'),
      
      -- Scenario 16-20: Edge cases  
      (gen_random_uuid(), 'SEED-016', '[SEED] Großkunde AG', 'UNTERNEHMEN', 'AKTIV', 1000000, 5, true, false, 'seed', NOW()),
      (gen_random_uuid(), 'SEED-017', '[SEED] Privatkunde Klein', 'PRIVAT', 'AKTIV', 5000, 55, true, false, 'seed', NOW()),
      (gen_random_uuid(), 'SEED-018', '[SEED] Verein e.V.', 'VEREIN', 'AKTIV', 50000, 60, true, false, 'seed', NOW()),
      (gen_random_uuid(), 'SEED-019', '[SEED] Institution GmbH', 'INSTITUTION', 'LEAD', 0, 65, true, false, 'seed', NOW()),
      (gen_random_uuid(), 'SEED-020', '[SEED] Sonstige Corp', 'SONSTIGE', 'RISIKO', 10000, 100, true, false, 'seed', NOW())
  ON CONFLICT (customer_number) DO UPDATE
    SET is_test_data = TRUE,  -- Always ensure test data flag is set
        company_name = EXCLUDED.company_name,  -- Update name to match seed
        customer_type = EXCLUDED.customer_type,
        status = EXCLUDED.status;

  RAISE NOTICE 'V10005 seed data completed - 20 SEED customers ensured';
END $$;