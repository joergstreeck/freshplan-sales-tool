-- V264: Add Customer.businessType for Lead/Customer harmonization (Sprint 2.1.6 Phase 2)
-- Migration: Industry (Customer) → BusinessType (shared across Lead + Customer)
-- Strategy: Add businessType, migrate data, keep industry for backward compatibility (deprecate later)

-- ============================================================================
-- PHASE 1: Add businessType column to customers
-- ============================================================================
ALTER TABLE customers
ADD COLUMN business_type VARCHAR(30) NULL;

COMMENT ON COLUMN customers.business_type IS 'Unified business type classification (shared with leads). Replaces industry enum. Sprint 2.1.6';

-- ============================================================================
-- PHASE 2: Migrate existing Industry data to BusinessType
-- ============================================================================
-- Mapping based on BusinessType.fromLegacyIndustry() logic:
-- HOTEL → HOTEL
-- RESTAURANT → RESTAURANT
-- CATERING → CATERING
-- KANTINE → KANTINE
-- GESUNDHEITSWESEN → GESUNDHEIT
-- BILDUNG → BILDUNG
-- EINZELHANDEL → LEH (Lebensmitteleinzelhandel)
-- VERANSTALTUNG → SONSTIGES
-- SONSTIGE → SONSTIGES

UPDATE customers
SET business_type = CASE industry
    WHEN 'HOTEL' THEN 'HOTEL'
    WHEN 'RESTAURANT' THEN 'RESTAURANT'
    WHEN 'CATERING' THEN 'CATERING'
    WHEN 'KANTINE' THEN 'KANTINE'
    WHEN 'GESUNDHEITSWESEN' THEN 'GESUNDHEIT'
    WHEN 'BILDUNG' THEN 'BILDUNG'
    WHEN 'EINZELHANDEL' THEN 'LEH'
    WHEN 'VERANSTALTUNG' THEN 'SONSTIGES'
    WHEN 'SONSTIGE' THEN 'SONSTIGES'
    ELSE NULL
END
WHERE industry IS NOT NULL;

-- ============================================================================
-- PHASE 3: Add CHECK constraint for valid BusinessType values
-- ============================================================================
-- Must match BusinessType enum: RESTAURANT, HOTEL, CATERING, KANTINE, GROSSHANDEL, LEH, BILDUNG, GESUNDHEIT, SONSTIGES
ALTER TABLE customers
ADD CONSTRAINT chk_customer_business_type
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
));

-- ============================================================================
-- PHASE 4: Create index for business_type queries
-- ============================================================================
CREATE INDEX idx_customers_business_type ON customers(business_type)
WHERE business_type IS NOT NULL;

-- ============================================================================
-- VERIFICATION QUERIES (run manually to verify migration success)
-- ============================================================================
-- SELECT industry, business_type, COUNT(*) FROM customers GROUP BY industry, business_type ORDER BY industry;
-- SELECT COUNT(*) FROM customers WHERE industry IS NOT NULL AND business_type IS NULL; -- Should be 0
-- SELECT COUNT(*) FROM customers WHERE business_type IS NOT NULL; -- Should match non-NULL industry count

-- ============================================================================
-- ROLLBACK (if needed)
-- ============================================================================
-- DROP INDEX IF EXISTS idx_customers_business_type;
-- ALTER TABLE customers DROP CONSTRAINT IF EXISTS chk_customer_business_type;
-- ALTER TABLE customers DROP COLUMN IF EXISTS business_type;
