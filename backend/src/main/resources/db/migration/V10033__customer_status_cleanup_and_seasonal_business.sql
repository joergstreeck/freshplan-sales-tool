-- =====================================================================================
-- Migration V10033: Customer Status Cleanup + Seasonal Business Support
-- =====================================================================================
-- Purpose: Remove LEAD from CustomerStatus enum + Add Seasonal Business fields
-- Sprint: 2.1.7.4 - Customer Status Architecture
-- Author: Claude (AI-assisted)
-- Date: 2025-10-22
--
-- Background:
-- CustomerStatus.LEAD is conceptually wrong - Leads belong in leads table, not customers.
-- Also adds seasonal business support for food industry (ice cream shops, beer gardens, etc.)
--
-- Changes:
-- 1. Migrate all LEAD customers → PROSPECT
-- 2. Remove LEAD from CustomerStatus enum (update CHECK constraint)
-- 3. Add Seasonal Business fields (is_seasonal_business, seasonal_months, seasonal_pattern)
--
-- Business Impact:
-- - LEAD status removed (was conceptually incorrect)
-- - New lifecycle: PROSPECT → AKTIV → RISIKO → INAKTIV → ARCHIVIERT
-- - Seasonal businesses won't trigger false churn alarms
-- =====================================================================================

-- =====================================================================================
-- PART 1: CustomerStatus Enum Cleanup
-- =====================================================================================

-- Step 1: Migrate any remaining LEAD customers → PROSPECT
-- Business Rule: Customers should NEVER have LEAD status (Leads belong in leads table)
-- Auto-conversion: Lead → Customer with status=PROSPECT (not LEAD!)
UPDATE customers
SET status = 'PROSPECT', updated_at = CURRENT_TIMESTAMP
WHERE status = 'LEAD';

-- Step 2: Remove old CHECK constraint (if exists)
ALTER TABLE customers
DROP CONSTRAINT IF EXISTS customer_status_check;

-- Step 3: Add new CHECK constraint (WITHOUT LEAD - clean architecture!)
-- Full lifecycle: PROSPECT → AKTIV → RISIKO → INAKTIV → ARCHIVIERT
ALTER TABLE customers
ADD CONSTRAINT customer_status_check
CHECK (status IN ('PROSPECT', 'AKTIV', 'RISIKO', 'INAKTIV', 'ARCHIVIERT'));

COMMENT ON CONSTRAINT customer_status_check ON customers IS 'Sprint 2.1.7.4: Clean lifecycle PROSPECT → AKTIV (LEAD removed - belongs in leads table only)';

-- Step 4: Change DEFAULT value for status column from 'LEAD' to 'PROSPECT'
-- This fixes test failures where customers are created without explicit status
ALTER TABLE customers
ALTER COLUMN status SET DEFAULT 'PROSPECT';

COMMENT ON COLUMN customers.status IS 'Customer lifecycle status: PROSPECT (new) → AKTIV (active) → RISIKO (at risk) → INAKTIV (inactive) → ARCHIVIERT (archived). Default: PROSPECT';

-- =====================================================================================
-- PART 2: Seasonal Business Support
-- =====================================================================================

-- Step 5: Add is_seasonal_business flag
ALTER TABLE customers
ADD COLUMN IF NOT EXISTS is_seasonal_business BOOLEAN DEFAULT FALSE;

COMMENT ON COLUMN customers.is_seasonal_business IS 'Saisonbetrieb ja/nein (z.B. Eisdiele, Ski-Hütte, Biergarten)';

-- Step 5: Add seasonal_months array (JSONB - project standard)
-- Example: [3,4,5,6,7,8,9,10] = März-Oktober für Eisdielen
ALTER TABLE customers
ADD COLUMN IF NOT EXISTS seasonal_months JSONB DEFAULT '[]'::jsonb;

COMMENT ON COLUMN customers.seasonal_months IS 'Aktive Monate (1-12) für Saisonbetriebe als JSONB Array, z.B. [3,4,5,6,7,8,9,10] = März-Oktober';

-- Add GIN index for JSONB queries (consistent with pain_points)
CREATE INDEX IF NOT EXISTS idx_customers_seasonal_months_gin
ON customers USING GIN (seasonal_months);

-- Step 6: Add seasonal_pattern (pre-defined patterns)
-- Patterns: SUMMER, WINTER, SPRING, AUTUMN, CHRISTMAS, CUSTOM
ALTER TABLE customers
ADD COLUMN IF NOT EXISTS seasonal_pattern VARCHAR(50);

-- Add CHECK constraint for seasonal_pattern enum values (only if not exists)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'customers_seasonal_pattern_check'
    ) THEN
        ALTER TABLE customers
        ADD CONSTRAINT customers_seasonal_pattern_check
        CHECK (seasonal_pattern IN ('SUMMER', 'WINTER', 'SPRING', 'AUTUMN', 'CHRISTMAS', 'CUSTOM'));
    END IF;
END $$;

COMMENT ON COLUMN customers.seasonal_pattern IS 'Saisonmuster: SUMMER (März-Okt), WINTER (Nov-März), SPRING, AUTUMN, CHRISTMAS, CUSTOM';

-- Step 7: Create index for seasonal business queries
CREATE INDEX IF NOT EXISTS idx_customers_seasonal ON customers(is_seasonal_business)
WHERE is_seasonal_business = TRUE;

-- =====================================================================================
-- Migration Summary
-- =====================================================================================
-- ✅ Migrated any LEAD customers → PROSPECT (clean architecture)
-- ✅ Updated CustomerStatus CHECK constraint (WITHOUT LEAD - belongs in leads table only)
-- ✅ Added is_seasonal_business flag
-- ✅ Added seasonal_months array (JSONB with GIN index)
-- ✅ Added seasonal_pattern enum (VARCHAR with CHECK constraint)
-- ✅ Created performance index for seasonal businesses
--
-- Full Customer Lifecycle (Sprint 2.1.7.4):
--   PROSPECT → AKTIV → RISIKO → INAKTIV → ARCHIVIERT
--
-- Auto-Conversion Flow:
--   Lead (leads table) --[Opportunity WON]--> Customer (status=PROSPECT)
--   Customer (PROSPECT) --[Manual Activation]--> Customer (status=AKTIV)
--   Customer (PROSPECT) --[First Order]--> Customer (status=AKTIV)
--
-- Seasonal Business Patterns:
--   SUMMER: März-Oktober (Eisdielen, Biergärten)
--   WINTER: November-März (Ski-Hütten, Glühwein-Stände)
--   SPRING: März-Mai (Spargel-Restaurants)
--   AUTUMN: September-November (Weinlese-Gastronomie)
--   CHRISTMAS: November-Dezember (Weihnachtsmarkt-Stände)
--   CUSTOM: Benutzerdefiniert via seasonal_months array
--
-- Impact:
--   - No breaking changes (all existing customers migrated to PROSPECT)
--   - Seasonal business detection prevents false churn alarms
--   - ChurnDetectionService can now exclude out-of-season businesses
-- =====================================================================================
