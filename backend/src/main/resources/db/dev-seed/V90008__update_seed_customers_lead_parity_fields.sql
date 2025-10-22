-- ============================================================================
-- V90008: Update SEED Customers - Lead Parity Fields
-- ============================================================================
-- Purpose: Update existing SEED customers with new Lead Parity Fields from V10032
-- Sprint: 2.1.7.4 - Customer Status Architecture & Lead Parity
-- Date: 2025-10-21
--
-- Context:
-- V10032 added 5 new fields to customers table (kitchen_size, employee_count, etc.)
-- V90001 was updated with INSERT statements including these fields, but
-- ON CONFLICT DO NOTHING prevents updating existing rows.
--
-- This migration UPDATEs the existing SEED customers with realistic values.
-- ============================================================================

-- KD-DEV-001: Restaurant Silbertanne (Multi-Location Restaurant, 5 Standorte)
UPDATE customers SET
    kitchen_size = 'MITTEL',
    employee_count = 22,
    branch_count = 5,
    is_chain = TRUE,
    estimated_volume = 180000.00
WHERE customer_number = 'KD-DEV-001';

-- KD-DEV-002: Hotel Nordwind (4-Sterne Hotel, 120 Zimmer)
UPDATE customers SET
    kitchen_size = 'GROSS',
    employee_count = 85,
    branch_count = 1,
    is_chain = FALSE,
    estimated_volume = 95000.00
WHERE customer_number = 'KD-DEV-002';

-- KD-DEV-003: FreshEvents Catering (Event-Catering, Berlin + Zürich)
UPDATE customers SET
    kitchen_size = 'GROSS',
    employee_count = 65,
    branch_count = 2,
    is_chain = TRUE,
    estimated_volume = 420000.00
WHERE customer_number = 'KD-DEV-003';

-- KD-DEV-004: Kantine TechPark (Edge Case: NULL Volume)
UPDATE customers SET
    kitchen_size = 'MITTEL',
    employee_count = 12,
    branch_count = 1,
    is_chain = FALSE,
    estimated_volume = NULL
WHERE customer_number = 'KD-DEV-004';

-- KD-DEV-005: Rheinland Gastro (Großhandel, 2 Standorte)
UPDATE customers SET
    kitchen_size = 'KLEIN',
    employee_count = 18,
    branch_count = 2,
    is_chain = FALSE,
    estimated_volume = 120000.00
WHERE customer_number = 'KD-DEV-005';

-- ============================================================================
-- VERIFICATION
-- ============================================================================
DO $$
DECLARE
  updated_count INTEGER;
BEGIN
  SELECT COUNT(*) INTO updated_count
  FROM customers
  WHERE customer_number LIKE 'KD-DEV-%'
    AND kitchen_size IS NOT NULL;

  RAISE NOTICE 'DEV-SEED Customers updated with Lead Parity Fields: % of 5', updated_count;

  IF updated_count != 5 THEN
    RAISE WARNING 'Expected 5 customers to be updated, but found %', updated_count;
  END IF;
END $$;

-- ============================================================================
-- SUMMARY
-- ============================================================================
-- ✅ Updated 5 SEED customers (KD-DEV-001 through KD-DEV-005)
-- ✅ Added kitchen_size, employee_count, branch_count, is_chain, estimated_volume
-- ✅ Realistic values based on business_type and company size
-- ✅ Enables testing of CustomerOnboardingWizard without manual data entry
-- ============================================================================
