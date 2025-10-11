-- V276: Lead Contacts Backward Compatibility - Sync Trigger (ADR-007 Option C)
--
-- Purpose: Synchronisiert lead_contacts.primary → leads.contact_person/email/phone
-- - Legacy API clients können weiterhin leads.contact_person nutzen
-- - Trigger hält deprecated fields automatisch aktuell
-- - Ermöglicht graduelle Frontend-Migration ohne Breaking Changes
--
-- Removal: V280 (wenn alle Clients auf lead_contacts migriert sind)
--
-- Referenz: docs/planung/features-neu/02_neukundengewinnung/artefakte/LEAD_CONTACTS_ARCHITECTURE.md
--
-- Author: Sprint 2.1.6 Phase 5+
-- Date: 2025-10-08

-- ============================================================================
-- 1. TRIGGER FUNCTION: Sync primary contact to leads table (Backward Compat)
-- ============================================================================

CREATE OR REPLACE FUNCTION sync_primary_lead_contact_to_lead()
RETURNS TRIGGER AS $$
BEGIN
  -- Only sync if this is the primary contact (NULL-safe: COALESCE handles missing is_deleted)
  IF NEW.is_primary = true AND COALESCE(NEW.is_deleted, false) = false THEN
    UPDATE leads
    SET
      contact_person = NEW.first_name || ' ' || NEW.last_name,
      email = NEW.email,
      phone = COALESCE(NEW.phone, NEW.mobile) -- Prefer phone, fallback to mobile
    WHERE id = NEW.lead_id;
  END IF;

  -- If contact is no longer primary or deleted, check if another primary exists (NULL-safe)
  IF (NEW.is_primary = false OR COALESCE(NEW.is_deleted, false) = true) THEN
    -- Find another primary contact for this lead
    DECLARE
      other_primary RECORD;
    BEGIN
      SELECT * INTO other_primary
      FROM lead_contacts
      WHERE lead_id = NEW.lead_id
        AND is_primary = true
        AND COALESCE(is_deleted, false) = false
        AND id != NEW.id
      LIMIT 1;

      IF FOUND THEN
        -- Sync the other primary contact
        UPDATE leads
        SET
          contact_person = other_primary.first_name || ' ' || other_primary.last_name,
          email = other_primary.email,
          phone = COALESCE(other_primary.phone, other_primary.mobile)
        WHERE id = NEW.lead_id;
      ELSE
        -- No primary contact left, clear deprecated fields
        UPDATE leads
        SET
          contact_person = NULL,
          email = NULL,
          phone = NULL
        WHERE id = NEW.lead_id;
      END IF;
    END;
  END IF;

  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- ============================================================================
-- 2. TRIGGERS: Attach sync function to INSERT/UPDATE/DELETE events
-- ============================================================================

-- Trigger on INSERT: Sync when primary contact is created
DROP TRIGGER IF EXISTS trg_sync_primary_lead_contact_insert ON lead_contacts;
CREATE TRIGGER trg_sync_primary_lead_contact_insert
  AFTER INSERT ON lead_contacts
  FOR EACH ROW
  WHEN (NEW.is_primary = true)
  EXECUTE FUNCTION sync_primary_lead_contact_to_lead();

-- Trigger on UPDATE: Sync when primary contact is modified
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

-- Note: Using OLD for DELETE trigger since NEW is NULL on DELETE

-- ============================================================================
-- 3. UNIQUE CONSTRAINT: Only 1 primary contact per lead allowed
-- ============================================================================

DROP INDEX IF EXISTS idx_lead_contacts_one_primary_per_lead;
CREATE UNIQUE INDEX idx_lead_contacts_one_primary_per_lead
  ON lead_contacts(lead_id)
  WHERE is_primary = true AND COALESCE(is_deleted, false) = false;

-- ============================================================================
-- 4. VALIDATE MIGRATION: Ensure all leads have synced contact_person
-- ============================================================================

-- Check: All leads with primary contacts should have contact_person synced
DO $$
DECLARE
  unsynced_count INTEGER;
BEGIN
  SELECT COUNT(*) INTO unsynced_count
  FROM leads l
  INNER JOIN lead_contacts lc ON l.id = lc.lead_id
  WHERE lc.is_primary = true
    AND COALESCE(lc.is_deleted, false) = false
    AND (
      l.contact_person IS NULL
      OR l.contact_person != (lc.first_name || ' ' || lc.last_name)
    );

  IF unsynced_count > 0 THEN
    RAISE WARNING 'Found % leads with unsynced contact_person. Running manual sync...', unsynced_count;

    -- Manual sync for safety
    UPDATE leads l
    SET
      contact_person = lc.first_name || ' ' || lc.last_name,
      email = lc.email,
      phone = COALESCE(lc.phone, lc.mobile)
    FROM lead_contacts lc
    WHERE l.id = lc.lead_id
      AND lc.is_primary = true
      AND COALESCE(lc.is_deleted, false) = false;

    RAISE NOTICE 'Manual sync completed for % leads.', unsynced_count;
  ELSE
    RAISE NOTICE 'All leads have synced contact_person. Migration OK.';
  END IF;
END $$;

-- ============================================================================
-- MIGRATION SUMMARY
-- ============================================================================
-- Created: sync_primary_lead_contact_to_lead() trigger function
-- Triggers: INSERT/UPDATE/DELETE on lead_contacts → auto-sync to leads table
-- Constraint: UNIQUE INDEX (only 1 primary contact per lead)
-- Validated: All existing leads have synced contact_person
-- Backward Compatibility: Legacy API clients can still use leads.contact_person
-- Removal Plan: V280 (Drop trigger, drop deprecated columns)
