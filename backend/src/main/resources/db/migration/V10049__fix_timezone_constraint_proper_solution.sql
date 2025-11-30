-- V10049__fix_timezone_constraint_proper_solution.sql
-- Sprint 2.1.7.7: Proper timezone fix after adding quarkus.hibernate-orm.jdbc.timezone=UTC
--
-- ROOT CAUSE (FIXED):
-- Java LocalDateTime.now() was using JVM timezone (e.g., Europe/Berlin = UTC+1/2)
-- PostgreSQL NOW() uses server timezone (usually UTC in Docker/Cloud)
-- Without explicit timezone config, Hibernate sent local times that looked "in the future" to DB
--
-- SOLUTION:
-- Added quarkus.hibernate-orm.jdbc.timezone=UTC to application.properties
-- Now Hibernate interprets all LocalDateTime as UTC when storing/retrieving
--
-- This migration reduces the buffer from 6 hours to 1 minute (for NTP drift/clock skew)
-- The 6-hour buffer (V10048) was a workaround; now we have the proper fix.
--
-- Author: E2E Test Architecture - Root Cause Analysis
-- Date: 2025-11-30

-- =====================================================
-- 1. Drop existing constraint (the 6-hour workaround)
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
-- 2. Fix existing data that would violate the new constraint
--    This sets registered_at to NOW() for any rows that were
--    created with the old 6-hour buffer and are "in the future"
-- =====================================================
UPDATE leads
SET registered_at = NOW()
WHERE registered_at > NOW() + INTERVAL '1 minute';

-- =====================================================
-- 3. Add new constraint with 1-minute buffer
--    This accounts for:
--    - Network latency
--    - NTP synchronization drift
--    - Clock skew between app and DB servers
-- =====================================================
ALTER TABLE leads
  ADD CONSTRAINT chk_leads_registered_at_not_future
  CHECK (registered_at <= NOW() + INTERVAL '1 minute');

-- =====================================================
-- 4. Update documentation
-- =====================================================
COMMENT ON CONSTRAINT chk_leads_registered_at_not_future ON leads IS
  'Prevents registration dates in the future (with 1-minute buffer for clock skew).
   Requires quarkus.hibernate-orm.jdbc.timezone=UTC to be set in application.properties.';
