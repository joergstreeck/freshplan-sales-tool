-- ============================================================================
-- Migration V10046: Normalize expansion_planned to ExpansionPlan Enum values
-- ============================================================================
-- Sprint 2.1.7.x: Best Practice - Backend = Single Source of Truth
--
-- Problem: expansion_planned field has inconsistent values:
--   - V6 migration: 'ja', 'nein' (lowercase)
--   - V10041 migration: 'YES', 'NO', 'UNKNOWN' (uppercase English)
--   - ExpansionPlan.java Enum: 'JA', 'NEIN', 'GEPLANT', 'UNKLAR' (uppercase German)
--
-- Solution: Normalize all values to match Java Enum (Single Source of Truth)
-- ============================================================================

-- Step 1: Drop existing constraint if it exists
DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM pg_constraint WHERE conname = 'chk_expansion_planned'
  ) THEN
    ALTER TABLE customers DROP CONSTRAINT chk_expansion_planned;
  END IF;
END $$;

-- Step 2: Normalize existing values to Java Enum values
-- Map: YES -> JA, NO -> NEIN, UNKNOWN -> UNKLAR, ja -> JA, nein -> NEIN
UPDATE customers SET expansion_planned =
  CASE
    WHEN UPPER(expansion_planned) IN ('YES', 'JA') THEN 'JA'
    WHEN UPPER(expansion_planned) IN ('NO', 'NEIN') THEN 'NEIN'
    WHEN UPPER(expansion_planned) IN ('UNKNOWN', 'UNKLAR') THEN 'UNKLAR'
    WHEN UPPER(expansion_planned) = 'GEPLANT' THEN 'GEPLANT'
    ELSE NULL  -- Unknown values become NULL
  END
WHERE expansion_planned IS NOT NULL;

-- Step 3: Add new constraint matching Java Enum values
-- ExpansionPlan.java: JA, NEIN, GEPLANT, UNKLAR
ALTER TABLE customers
  ADD CONSTRAINT chk_expansion_planned
  CHECK (expansion_planned IS NULL OR expansion_planned IN ('JA', 'NEIN', 'GEPLANT', 'UNKLAR'));

-- Step 4: Update column comment to reflect Single Source of Truth
COMMENT ON COLUMN customers.expansion_planned IS
  'Expansion planned: JA, NEIN, GEPLANT, UNKLAR (matches ExpansionPlan.java Enum)';

-- ============================================================================
-- Post-Migration Verification Query (for debugging):
-- SELECT expansion_planned, COUNT(*) FROM customers GROUP BY expansion_planned;
-- ============================================================================
