-- V252__leads_registered_at_backdating.sql
-- Sprint 2.1.4: Add registered_at for proper lead protection tracking
-- Allows backdating lead registration (Admin/Manager only) for import scenarios
--
-- Contractual basis: § 2(8)(a) - Protection starts with registration/first contact
-- Author: team/leads-backend
-- Date: 2025-09-28

-- =====================================================
-- 1. Add registered_at and audit columns for accurate protection tracking
-- =====================================================
ALTER TABLE leads
  ADD COLUMN IF NOT EXISTS registered_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  ADD COLUMN IF NOT EXISTS registered_at_set_by VARCHAR(100),
  ADD COLUMN IF NOT EXISTS registered_at_set_at TIMESTAMPTZ,
  ADD COLUMN IF NOT EXISTS registered_at_source VARCHAR(20) DEFAULT 'system'
    CHECK (registered_at_source IN ('system', 'manual', 'import', 'backdated'));

-- Backfill existing leads with their creation timestamp
UPDATE leads
   SET registered_at = COALESCE(registered_at, created_at),
       registered_at_source = COALESCE(registered_at_source, 'system')
 WHERE registered_at IS NULL OR registered_at > created_at;

-- =====================================================
-- 2. Add constraint to prevent future dates
-- =====================================================
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint
    WHERE conname = 'chk_leads_registered_at_not_future'
  ) THEN
    ALTER TABLE leads
      ADD CONSTRAINT chk_leads_registered_at_not_future
      CHECK (registered_at <= NOW() + INTERVAL '1 minute'); -- Small buffer for clock drift
  END IF;
END $$;

-- =====================================================
-- 3. Add index for registered_at queries (protection calculations)
-- =====================================================
CREATE INDEX IF NOT EXISTS idx_leads_registered_at
  ON leads(registered_at)
  WHERE status != 'DELETED';

-- =====================================================
-- 4. Update protection calculation columns
-- =====================================================
-- Note: These updates ensure all protection-related timestamps
-- are based on registered_at instead of created_at

-- Update protection_start_at to use registered_at
UPDATE leads
   SET protection_start_at = registered_at
 WHERE protection_start_at IS NOT NULL
   AND protection_start_at != registered_at;

-- =====================================================
-- 5. Add override reason column for backdating documentation
-- =====================================================
ALTER TABLE leads
  ADD COLUMN IF NOT EXISTS registered_at_override_reason TEXT;

-- =====================================================
-- 6. Documentation
-- =====================================================
COMMENT ON COLUMN leads.registered_at IS
  'When the lead was first registered/contacted. May differ from created_at for imported leads (Admin/Manager backdating only)';

COMMENT ON COLUMN leads.registered_at_override_reason IS
  'Reason for backdating registered_at (e.g., "Q2 2025 import from legacy system")';

COMMENT ON COLUMN leads.registered_at_set_by IS
  'User who set/backdated the registered_at timestamp (for audit trail)';

COMMENT ON COLUMN leads.registered_at_set_at IS
  'Timestamp when registered_at was set/backdated (for audit trail)';

COMMENT ON COLUMN leads.registered_at_source IS
  'Source of registered_at: system (default), manual (UI), import (bulk), backdated (Admin/Manager)';

COMMENT ON CONSTRAINT chk_leads_registered_at_not_future ON leads IS
  'Prevents registration dates in the future (with 1-minute buffer for clock drift)';

-- =====================================================
-- 7. Update protection deadline calculations
-- =====================================================
-- Note: Protection calculations now use registered_at as the base timestamp
-- If your application has protection_until columns or functions,
-- they should be updated to calculate from registered_at instead of created_at

DO $$
BEGIN
  -- Update protection_months calculation base if column exists
  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_name = 'leads' AND column_name = 'protection_until'
  ) THEN
    UPDATE leads
       SET protection_until = registered_at + (protection_months || ' months')::INTERVAL
     WHERE protection_months IS NOT NULL;
    RAISE NOTICE 'Updated protection_until calculations to use registered_at';
  ELSE
    RAISE NOTICE 'No protection_until column found - skipping update';
  END IF;
END $$;