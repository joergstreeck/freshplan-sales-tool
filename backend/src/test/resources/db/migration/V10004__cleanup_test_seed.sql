-- ============================================================================
-- V10004: CI-only Cleanup of spurious test SEED customers
-- ============================================================================
-- Removes any test SEED customers that are not in our canonical list
-- Only runs in CI/Test environments
-- ============================================================================

DO $$
BEGIN
  -- Guard: Only run in CI/Test environments
  IF current_setting('ci.build', true) <> 'true' THEN
    RAISE NOTICE 'V10004 cleanup skipped (not CI, ci.build=%)', current_setting('ci.build', true);
    RETURN;
  END IF;

  RAISE NOTICE 'V10004 cleanup starting in CI environment';

  -- Remove any spurious test SEED customers not in our canonical list
  DELETE FROM customers
  WHERE is_test_data = true
    AND customer_number LIKE 'SEED-%'
    AND customer_number NOT IN (
      'SEED-001','SEED-002','SEED-003','SEED-004','SEED-005',
      'SEED-006','SEED-007','SEED-008','SEED-009','SEED-010',
      'SEED-011','SEED-012','SEED-013','SEED-014','SEED-015',
      'SEED-016','SEED-017','SEED-018','SEED-019','SEED-020'
    );

  -- Also remove any SEED-TEST-* entries from debug tests
  DELETE FROM customers
  WHERE is_test_data = true
    AND customer_number LIKE 'SEED-TEST-%';

  RAISE NOTICE 'V10004 cleanup completed';
END $$;