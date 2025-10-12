-- V10025: Fix Missing Lead Contacts Triggers (Hotfix for V10017)
--
-- Root Cause: V10017 was modified after initial execution
-- - Added COALESCE(is_deleted, false) for NULL-safety
-- - But Flyway checksum mismatch prevented re-execution
-- - Trigger FUNCTION exists, but TRIGGERs were never created
--
-- This migration creates the missing triggers that should have been created by V10017
--
-- Author: Sprint 2.1.6 Phase 5+ Hotfix
-- Date: 2025-10-11
-- References: V10017, ADR-007 Option C

-- ============================================================================
-- CREATE TRIGGERS: Attach sync function to INSERT/UPDATE/DELETE events
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

-- Trigger on DELETE: Sync when primary contact is deleted
DROP TRIGGER IF EXISTS trg_sync_primary_lead_contact_delete ON lead_contacts;
CREATE TRIGGER trg_sync_primary_lead_contact_delete
  AFTER DELETE ON lead_contacts
  FOR EACH ROW
  WHEN (OLD.is_primary = true)
  EXECUTE FUNCTION sync_primary_lead_contact_to_lead();

-- ============================================================================
-- MIGRATION SUMMARY
-- ============================================================================
-- Fixed: Created 3 missing triggers (INSERT/UPDATE/DELETE)
-- Note: Trigger function sync_primary_lead_contact_to_lead() already exists from V10017
-- Tests: LeadContactTest.testPrimaryContactSyncToLead should now pass
