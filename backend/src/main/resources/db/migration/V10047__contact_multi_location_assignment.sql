-- =====================================================
-- V10047: Contact Multi-Location Assignment ENHANCEMENT
-- Sprint 2.1.7.7: Multi-Location Management
-- =====================================================
--
-- HINWEIS: Die Tabelle contact_location_assignments und die Spalte
-- responsibility_scope existieren bereits (V210).
--
-- Diese Migration:
--   1. Migriert bestehende assigned_location_id Daten in die Join-Tabelle
--   2. Setzt responsibility_scope basierend auf Zuweisungen
--
-- Use Cases:
--   - Geschäftsführer: Alle Standorte (responsibility_scope = 'ALL')
--   - Einkaufsleiter: Region Nord (3 Filialen)
--   - Küchenchef: Nur ein Standort
-- =====================================================

-- 1. Sicherstellen, dass responsibility_scope existiert (idempotent)
ALTER TABLE customer_contacts
ADD COLUMN IF NOT EXISTS responsibility_scope VARCHAR(20) DEFAULT 'ALL';

COMMENT ON COLUMN customer_contacts.responsibility_scope IS
'Zuständigkeitsbereich: ALL = alle Standorte, SPECIFIC = nur bestimmte Standorte';

-- 2. Migration bestehender Single-Location Daten
-- Wenn assigned_location_id gesetzt ist, übertrage in die Join-Tabelle
INSERT INTO contact_location_assignments (contact_id, location_id, assigned_by, is_primary)
SELECT
    cc.id AS contact_id,
    cc.assigned_location_id AS location_id,
    NULL AS assigned_by,
    true AS is_primary
FROM customer_contacts cc
WHERE cc.assigned_location_id IS NOT NULL
  AND cc.is_deleted = false
  AND NOT EXISTS (
      SELECT 1 FROM contact_location_assignments cla
      WHERE cla.contact_id = cc.id AND cla.location_id = cc.assigned_location_id
  );

-- 3. Setze responsibility_scope basierend auf Migration
-- Kontakte MIT Zuweisungen bekommen SPECIFIC
UPDATE customer_contacts
SET responsibility_scope = 'SPECIFIC'
WHERE id IN (SELECT DISTINCT contact_id FROM contact_location_assignments)
  AND (responsibility_scope IS NULL OR responsibility_scope = 'ALL')
  AND is_deleted = false;

-- Kontakte OHNE Zuweisungen bekommen ALL (Default)
UPDATE customer_contacts
SET responsibility_scope = 'ALL'
WHERE id NOT IN (SELECT DISTINCT contact_id FROM contact_location_assignments)
  AND responsibility_scope IS NULL
  AND is_deleted = false;

-- 4. Kommentar für deprecated Spalte
COMMENT ON COLUMN customer_contacts.assigned_location_id IS
'DEPRECATED: Verwende contact_location_assignments für Multi-Location. Wird für Abwärtskompatibilität beibehalten.';
