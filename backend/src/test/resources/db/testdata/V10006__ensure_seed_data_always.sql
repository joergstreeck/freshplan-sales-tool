-- ============================================================================
-- V10006: ALWAYS ensure SEED data exists (CI multi-run compatibility)
-- ============================================================================
-- This migration ALWAYS ensures SEED data exists, even if V10005 already ran
-- This is necessary because CI runs multiple Maven commands, each starting
-- a fresh Quarkus instance, but Flyway only runs migrations once.
-- ============================================================================

DO $$
DECLARE
  seed_count INTEGER;
  existing_count INTEGER;
BEGIN
  -- Guard: Only run in CI/Test environments
  IF current_setting('ci.build', true) <> 'true' THEN
    RAISE NOTICE 'V10006 ensure seeds skipped (not CI)';
    RETURN;
  END IF;

  -- Count existing SEED customers
  SELECT COUNT(*) INTO existing_count
  FROM customers
  WHERE customer_number LIKE 'SEED-%';
  
  IF existing_count = 20 THEN
    RAISE NOTICE 'V10006: All 20 SEED customers exist, checking integrity...';
    
    -- Ensure they are all marked correctly
    UPDATE customers 
    SET is_test_data = FALSE  -- Protect from cleanup
    WHERE customer_number LIKE 'SEED-%';
    
    RETURN;
  END IF;
  
  RAISE NOTICE 'V10006: Found only % SEED customers, recreating all 20', existing_count;
  
  -- Delete any partial SEED data
  DELETE FROM customers WHERE customer_number LIKE 'SEED-%';
  
  -- Ensure pgcrypto extension
  CREATE EXTENSION IF NOT EXISTS pgcrypto;
  
  -- Insert all 20 SEED customers
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
      -- All with is_test_data=false to protect from cleanup!
      (gen_random_uuid(), 'SEED-001', '[SEED] Restaurant München', 'UNTERNEHMEN', 'AKTIV', 500000, 5, false, false, 'seed', NOW() - INTERVAL '2 years'),
      (gen_random_uuid(), 'SEED-002', '[SEED] Hotel Berlin', 'UNTERNEHMEN', 'AKTIV', 300000, 10, false, false, 'seed', NOW() - INTERVAL '18 months'),
      (gen_random_uuid(), 'SEED-003', '[SEED] Catering Frankfurt', 'UNTERNEHMEN', 'AKTIV', 200000, 15, false, false, 'seed', NOW() - INTERVAL '1 year'),
      (gen_random_uuid(), 'SEED-004', '[SEED] Kantine Hamburg', 'UNTERNEHMEN', 'AKTIV', 150000, 20, false, false, 'seed', NOW() - INTERVAL '6 months'),
      (gen_random_uuid(), 'SEED-005', '[SEED] Bio-Markt Dresden', 'UNTERNEHMEN', 'AKTIV', 100000, 25, false, false, 'seed', NOW() - INTERVAL '3 months'),
      (gen_random_uuid(), 'SEED-006', '[SEED] Bäckerei Stuttgart', 'UNTERNEHMEN', 'RISIKO', 50000, 70, false, false, 'seed', NOW() - INTERVAL '2 years'),
      (gen_random_uuid(), 'SEED-007', '[SEED] Metzgerei Köln', 'UNTERNEHMEN', 'RISIKO', 40000, 80, false, false, 'seed', NOW() - INTERVAL '1 year'),
      (gen_random_uuid(), 'SEED-008', '[SEED] Café Leipzig', 'UNTERNEHMEN', 'INAKTIV', 0, 90, false, false, 'seed', NOW() - INTERVAL '3 years'),
      (gen_random_uuid(), 'SEED-009', '[SEED] Bar Düsseldorf', 'UNTERNEHMEN', 'INAKTIV', 0, 95, false, false, 'seed', NOW() - INTERVAL '4 years'),
      (gen_random_uuid(), 'SEED-010', '[SEED] Club Essen', 'UNTERNEHMEN', 'ARCHIVIERT', 0, 100, false, false, 'seed', NOW() - INTERVAL '5 years'),
      (gen_random_uuid(), 'SEED-011', '[SEED] New Lead Restaurant', 'NEUKUNDE', 'LEAD', 75000, 30, false, false, 'seed', NOW()),
      (gen_random_uuid(), 'SEED-012', '[SEED] Qualified Lead Hotel', 'NEUKUNDE', 'LEAD', 100000, 35, false, false, 'seed', NOW() - INTERVAL '1 week'),
      (gen_random_uuid(), 'SEED-013', '[SEED] Angebot Catering', 'NEUKUNDE', 'PROSPECT', 125000, 40, false, false, 'seed', NOW() - INTERVAL '2 weeks'),
      (gen_random_uuid(), 'SEED-014', '[SEED] Negotiation Kantine', 'NEUKUNDE', 'PROSPECT', 150000, 45, false, false, 'seed', NOW() - INTERVAL '3 weeks'),
      (gen_random_uuid(), 'SEED-015', '[SEED] Won Deal Bäckerei', 'UNTERNEHMEN', 'AKTIV', 175000, 50, false, false, 'seed', NOW() - INTERVAL '1 month'),
      (gen_random_uuid(), 'SEED-016', '[SEED] Großkunde AG', 'UNTERNEHMEN', 'AKTIV', 1000000, 5, false, false, 'seed', NOW()),
      (gen_random_uuid(), 'SEED-017', '[SEED] Privatkunde Klein', 'PRIVAT', 'AKTIV', 5000, 55, false, false, 'seed', NOW()),
      (gen_random_uuid(), 'SEED-018', '[SEED] Verein e.V.', 'VEREIN', 'AKTIV', 50000, 60, false, false, 'seed', NOW()),
      (gen_random_uuid(), 'SEED-019', '[SEED] Institution GmbH', 'INSTITUTION', 'LEAD', 0, 65, false, false, 'seed', NOW()),
      (gen_random_uuid(), 'SEED-020', '[SEED] Sonstige Corp', 'SONSTIGE', 'RISIKO', 10000, 100, false, false, 'seed', NOW());

  -- Count to verify
  SELECT COUNT(*) INTO seed_count 
  FROM customers 
  WHERE customer_number LIKE 'SEED-%';
  
  RAISE NOTICE 'V10006: Migration complete - % SEED customers now exist', seed_count;
END $$;