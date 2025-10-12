-- ============================================================================
-- Migration: Customer BusinessType (Enum-Migration Phase 2)
-- Version: V10026
-- Date: 2025-10-12
-- Description: Adds business_type column to customers table, migrates data
--              from legacy industry field, and marks industry as deprecated.
--
-- Pattern: VARCHAR(50) + CHECK Constraint (consistent with Lead migration V273)
-- Reason:  Better JPA compatibility, easier schema evolution vs PostgreSQL ENUM
--
-- Related:
--   - docs/planung/features-neu/02_neukundengewinnung/artefakte/ENUM_MIGRATION_STRATEGY.md
--   - Sprint 2.1.6.1 Phase 1 (Customer BusinessType Migration)
--   - Auto-Sync Setter in Customer.java (lines 394-420)
-- ============================================================================

-- Step 1: Add business_type column (nullable initially for data migration)
ALTER TABLE customers
ADD COLUMN business_type VARCHAR(50);

COMMENT ON COLUMN customers.business_type IS 'Business classification (replaces industry). Valid values: RESTAURANT, HOTEL, CATERING, KANTINE, GROSSHANDEL, LEH, BILDUNG, GESUNDHEIT, SONSTIGES';

-- Step 2: Migrate data from industry to business_type
-- Mapping based on BusinessType.fromLegacyIndustry() logic
UPDATE customers
SET business_type = CASE
    -- Direct 1:1 mappings
    WHEN industry = 'HOTEL' THEN 'HOTEL'
    WHEN industry = 'RESTAURANT' THEN 'RESTAURANT'
    WHEN industry = 'CATERING' THEN 'CATERING'
    WHEN industry = 'KANTINE' THEN 'KANTINE'

    -- Semantic mappings
    WHEN industry = 'GESUNDHEITSWESEN' THEN 'GESUNDHEIT'
    WHEN industry = 'BILDUNG' THEN 'BILDUNG'
    WHEN industry = 'EINZELHANDEL' THEN 'LEH'

    -- Catch-all mappings
    WHEN industry = 'VERANSTALTUNG' THEN 'SONSTIGES'
    WHEN industry = 'SONSTIGE' THEN 'SONSTIGES'

    -- Fallback for NULL or unexpected values
    ELSE 'SONSTIGES'
END;

-- Step 3: Make business_type mandatory
ALTER TABLE customers
ALTER COLUMN business_type SET NOT NULL;

-- Step 4: Add CHECK constraint for valid enum values
ALTER TABLE customers
ADD CONSTRAINT chk_customer_business_type CHECK (
    business_type IN (
        'RESTAURANT',
        'HOTEL',
        'CATERING',
        'KANTINE',
        'GROSSHANDEL',
        'LEH',
        'BILDUNG',
        'GESUNDHEIT',
        'SONSTIGES'
    )
);

-- Step 5: Create B-Tree index for query performance
-- (Benchmark: VARCHAR + B-Tree ~5% slower than PostgreSQL ENUM, acceptable tradeoff)
CREATE INDEX idx_customers_business_type ON customers(business_type);

-- Step 6: Mark industry column as deprecated (keep for backward compatibility)
COMMENT ON COLUMN customers.industry IS 'DEPRECATED since Sprint 2.1.6 - Use business_type instead. Maintained via Auto-Sync Setter for backward compatibility. Scheduled for removal in Sprint 2.1.7';

-- ============================================================================
-- Migration complete!
--
-- Next steps:
--   1. Backend: Test Auto-Sync Setter (Customer.java lines 394-420)
--   2. Backend: Refactor CustomerMapper to use setBusinessType()
--   3. Frontend: Update CustomerForm.tsx to use useBusinessTypes() hook
--   4. Tests: Add comprehensive Auto-Sync Setter tests
-- ============================================================================
