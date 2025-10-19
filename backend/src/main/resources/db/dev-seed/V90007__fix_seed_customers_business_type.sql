-- =============================================================================
-- V90007: Fix SEED Customers - Add businessType for Multiplier Testing
-- =============================================================================
-- Sprint: 2.1.7.3 - Customer → Opportunity Workflow
-- Purpose: Enable Business-Type-Matrix testing with SEED customers
--
-- Problem:
--   - SEED customers have business_type = NULL
--   - CreateOpportunityForCustomerDialog cannot find multipliers
--   - Formula: expectedValue = baseVolume × multiplier
--   - Multiplier lookup: customer.businessType × opportunityType
--
-- Solution:
--   - Update SEED customers with realistic businessType values
--   - Based on company name patterns
--
-- Migration Type: DEV-SEED (%dev only)
-- =============================================================================

-- 1. Restaurant Silbertanne München GmbH → RESTAURANT
UPDATE customers
SET business_type = 'RESTAURANT'
WHERE customer_number = 'KD-DEV-001'
  AND company_name LIKE '%Restaurant Silbertanne%';

-- 2. Hotel Nordwind Hamburg → HOTEL
UPDATE customers
SET business_type = 'HOTEL'
WHERE customer_number = 'KD-DEV-002'
  AND company_name LIKE '%Hotel Nordwind%';

-- 3. FreshEvents Catering AG → CATERING
UPDATE customers
SET business_type = 'CATERING'
WHERE customer_number = 'KD-DEV-003'
  AND company_name LIKE '%Catering%';

-- 4. Betriebsgastronomie TechPark Frankfurt → KANTINE
UPDATE customers
SET business_type = 'KANTINE'
WHERE customer_number = 'KD-DEV-004'
  AND company_name LIKE '%Betriebsgastronomie%';

-- 5. Rheinland Gastro Depot GmbH → GROSSHANDEL
UPDATE customers
SET business_type = 'GROSSHANDEL'
WHERE customer_number = 'KD-DEV-005'
  AND company_name LIKE '%Gastro Depot%';

-- Verify: All 5 SEED customers should now have business_type
DO $$
DECLARE
    null_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO null_count
    FROM customers
    WHERE customer_number LIKE 'KD-DEV-%'
      AND business_type IS NULL;

    IF null_count > 0 THEN
        RAISE WARNING 'DEV-SEED: % customers still have NULL business_type', null_count;
    ELSE
        RAISE NOTICE 'DEV-SEED: All 5 customers have business_type assigned ✓';
    END IF;
END $$;
