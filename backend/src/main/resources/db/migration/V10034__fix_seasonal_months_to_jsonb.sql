-- =====================================================================================
-- Migration V10034: Fix seasonal_months INTEGER[] → JSONB
-- =====================================================================================
-- Purpose: Konvertiere seasonal_months von INTEGER[] zu JSONB (Projekt-Standard)
-- Sprint: 2.1.7.4 - Customer Status Architecture (Repair)
-- Author: Claude (AI-assisted)
-- Date: 2025-10-22
--
-- Background:
-- V10033 wurde versehentlich mit INTEGER[] statt JSONB deployed.
-- FreshPlan Standard für Arrays: JSONB (siehe pain_points in V36)
--
-- Changes:
-- 1. Convert existing INTEGER[] data to JSONB
-- 2. Drop INTEGER[] column
-- 3. Add JSONB column with GIN index
--
-- Impact: No data loss, backwards compatible
-- =====================================================================================

-- Step 1: Add temporary JSONB column
ALTER TABLE customers
ADD COLUMN IF NOT EXISTS seasonal_months_jsonb JSONB;

-- Step 2: Convert existing INTEGER[] data to JSONB
-- Example: {3,4,5,6,7,8,9,10} → [3,4,5,6,7,8,9,10]
UPDATE customers
SET seasonal_months_jsonb = to_jsonb(seasonal_months)
WHERE seasonal_months IS NOT NULL;

-- Step 3: Drop old INTEGER[] column
ALTER TABLE customers
DROP COLUMN IF EXISTS seasonal_months;

-- Step 4: Rename JSONB column to seasonal_months
ALTER TABLE customers
RENAME COLUMN seasonal_months_jsonb TO seasonal_months;

-- Step 5: Set default value for JSONB column
ALTER TABLE customers
ALTER COLUMN seasonal_months SET DEFAULT '[]'::jsonb;

-- Step 6: Add GIN index for JSONB queries (consistent with pain_points)
CREATE INDEX IF NOT EXISTS idx_customers_seasonal_months_gin
ON customers USING GIN (seasonal_months);

-- Step 7: Update column comment
COMMENT ON COLUMN customers.seasonal_months IS 'Aktive Monate (1-12) für Saisonbetriebe als JSONB Array, z.B. [3,4,5,6,7,8,9,10] = März-Oktober (Projekt-Standard: JSONB statt INTEGER[])';

-- =====================================================================================
-- Verification Query (for manual testing):
-- SELECT customer_number, seasonal_months,
--        jsonb_typeof(seasonal_months) as type
-- FROM customers
-- WHERE is_seasonal_business = TRUE;
-- =====================================================================================
