-- V10023: Add missing constraints for lead_contacts table
--
-- Root Cause: V10016/V10017 constraints were not properly applied due to Hibernate Auto-DDL conflict
-- - Flyway reported migrations as "Success" but constraints were not created
-- - Hibernate Auto-DDL created table schema without custom constraints
-- - Tests failed because CHECK/UNIQUE/TRIGGER were missing
--
-- Solution: Explicitly add all missing constraints from V10016/V10017
-- - CHECK constraint: At least one contact method required (email OR phone OR mobile)
-- - UNIQUE index: Only one PRIMARY contact per lead allowed
-- - Trigger: Sync PRIMARY contact to leads table (Backward Compatibility)
--
-- Author: Sprint 2.1.6 Phase 5+ Fix
-- Date: 2025-10-10
-- References: V10016, V10017, ADR-007 Option C

-- ============================================================================
-- 1. CHECK CONSTRAINT: At least one contact method required
-- ============================================================================

-- Ensures that every LeadContact has at least one way to be contacted
-- Prevents invalid contacts with no email, phone, or mobile
ALTER TABLE lead_contacts
ADD CONSTRAINT chk_lead_contact_email_or_phone_or_mobile
CHECK (email IS NOT NULL OR phone IS NOT NULL OR mobile IS NOT NULL);

-- ============================================================================
-- 2. UNIQUE INDEX: Only one PRIMARY contact per lead
-- ============================================================================

-- Ensures business rule: Each lead can have exactly one primary contact
-- Partial index: Only active (is_deleted=false) primary contacts count
CREATE UNIQUE INDEX IF NOT EXISTS idx_lead_contacts_one_primary_per_lead
ON lead_contacts(lead_id)
WHERE is_primary = true AND is_deleted = false;

-- ============================================================================
-- 3. TRIGGER FUNCTION: Sync PRIMARY contact to leads table
-- ============================================================================

-- Backward Compatibility: Sync primary contact data to legacy fields in leads table
-- - leads.contact_person (deprecated, will be removed in V280)
-- - leads.email (deprecated, will be removed in V280)
-- - leads.phone (deprecated, will be removed in V280)
--
-- This allows legacy API clients to continue using flat fields during migration phase
CREATE OR REPLACE FUNCTION sync_primary_lead_contact_to_lead()
RETURNS TRIGGER AS $$
BEGIN
  -- Only sync if this is the primary contact and not deleted
  IF NEW.is_primary = true AND NEW.is_deleted = false THEN
    UPDATE leads
    SET
      contact_person = NEW.first_name || ' ' || NEW.last_name,
      email = NEW.email,
      phone = COALESCE(NEW.phone, NEW.mobile) -- Prefer phone, fallback to mobile
    WHERE id = NEW.lead_id;
  END IF;

  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- ============================================================================
-- 4. TRIGGERS: Attach sync function to INSERT/UPDATE events
-- ============================================================================

-- Trigger on INSERT: Sync when primary contact is created
DROP TRIGGER IF EXISTS trg_sync_primary_lead_contact_insert ON lead_contacts;
CREATE TRIGGER trg_sync_primary_lead_contact_insert
  AFTER INSERT ON lead_contacts
  FOR EACH ROW
  WHEN (NEW.is_primary = true)
  EXECUTE FUNCTION sync_primary_lead_contact_to_lead();

-- Trigger on UPDATE: Sync when primary contact is modified OR primary flag changes
DROP TRIGGER IF EXISTS trg_sync_primary_lead_contact_update ON lead_contacts;
CREATE TRIGGER trg_sync_primary_lead_contact_update
  AFTER UPDATE ON lead_contacts
  FOR EACH ROW
  WHEN (NEW.is_primary = true OR OLD.is_primary = true)
  EXECUTE FUNCTION sync_primary_lead_contact_to_lead();

-- ============================================================================
-- MIGRATION SUMMARY
-- ============================================================================
-- Fixed: Missing CHECK constraint (email OR phone OR mobile required)
-- Fixed: Missing UNIQUE index (only 1 primary contact per lead)
-- Fixed: Missing trigger function (sync primary contact to leads table)
-- Tests: LeadContactTest should now pass (16/16 tests)
-- Persistence: Constraints are now in Flyway migration (survive flyway:clean)
