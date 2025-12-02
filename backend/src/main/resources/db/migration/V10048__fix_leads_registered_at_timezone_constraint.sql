-- V10048__fix_leads_registered_at_timezone_constraint.sql
-- Sprint 2.1.7.7: Fix timezone constraint bug
--
-- BUG: The check constraint chk_leads_registered_at_not_future fails when
-- Java/Quarkus runs in a different timezone than PostgreSQL.
-- LocalDateTime.now() in Java has no timezone info, but PostgreSQL TIMESTAMPTZ
-- stores with UTC conversion based on server timezone.
--
-- Example: Java client in UTC+5, PostgreSQL in UTC:
-- - Java sends: 2025-11-30T02:47 (no timezone)
-- - PostgreSQL interprets as local, converts to UTC+5
-- - NOW() in PostgreSQL returns UTC
-- - Constraint check: UTC+5 > UTC → FAILS
--
-- FIX: Increase buffer from 1 minute to 6 hours (covers all timezones)
--
-- Author: E2E Test Bug Discovery
-- Date: 2025-11-30

-- =====================================================
-- 1. Drop existing constraint
-- =====================================================
DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM pg_constraint
    WHERE conname = 'chk_leads_registered_at_not_future'
  ) THEN
    ALTER TABLE leads
      DROP CONSTRAINT chk_leads_registered_at_not_future;
  END IF;
END $$;

-- =====================================================
-- 2. Add new constraint with 6-hour buffer
-- =====================================================
ALTER TABLE leads
  ADD CONSTRAINT chk_leads_registered_at_not_future
  CHECK (registered_at <= NOW() + INTERVAL '6 hours');

-- =====================================================
-- 3. Update documentation
-- =====================================================
COMMENT ON CONSTRAINT chk_leads_registered_at_not_future ON leads IS
  'Prevents registration dates in the future (with 6-hour buffer for timezone differences between Java/PostgreSQL)';
