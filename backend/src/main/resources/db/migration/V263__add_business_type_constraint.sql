-- Sprint 2.1.6 Phase 2: Harmonize Business Type values
-- Issue: Frontend hardcoded 5 values, Customer has 9 values
-- Solution: Single Source of Truth with 9 unified values
-- 2025-10-06

-- =============================================================================
-- 1. Migrate existing lowercase Lead.businessType values to uppercase
-- =============================================================================

UPDATE leads
SET business_type = 'RESTAURANT'
WHERE business_type = 'restaurant';

UPDATE leads
SET business_type = 'HOTEL'
WHERE business_type = 'hotel';

UPDATE leads
SET business_type = 'CATERING'
WHERE business_type = 'catering';

UPDATE leads
SET business_type = 'KANTINE'
WHERE business_type IN ('canteen', 'kantine');

UPDATE leads
SET business_type = 'SONSTIGES'
WHERE business_type = 'other';

-- =============================================================================
-- 2. Add CHECK constraint to leads.business_type
-- =============================================================================

ALTER TABLE leads
ADD CONSTRAINT leads_business_type_check
CHECK (business_type IN (
  'RESTAURANT',
  'HOTEL',
  'CATERING',
  'KANTINE',
  'GROSSHANDEL',
  'LEH',
  'BILDUNG',
  'GESUNDHEIT',
  'SONSTIGES'
) OR business_type IS NULL);

COMMENT ON COLUMN leads.business_type IS
  'Business type classification. Values: RESTAURANT, HOTEL, CATERING, KANTINE, GROSSHANDEL, LEH, BILDUNG, GESUNDHEIT, SONSTIGES.
   Frontend fetches values from GET /api/enums/business-types.
   Sprint 2.1.6: Harmonized with Customer.industry for consistent dropdown options.';
