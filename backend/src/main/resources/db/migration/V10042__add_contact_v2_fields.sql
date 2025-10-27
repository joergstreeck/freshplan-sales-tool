-- Sprint 2.1.7.2 D11.1: Contact Management - V2 Fields (Backend/Frontend Parity)
-- Add LinkedIn, XING, and notes fields to customer_contacts table
--
-- PARITY FIX: ContactEditDialog.tsx already has these fields in UI (birthday, linkedin, xing, notes)
-- but Contact.java Entity was missing linkedin, xing, notes (birthday already existed)

-- ============================================================================
-- FELD 1: customer_contacts.linkedin
-- ============================================================================
-- Zweck: LinkedIn-Profil-URL für professionelles Networking
-- Befüllung: Manuell via ContactEditDialog
-- Verwendung: Relationship Management, Professional Network Tracking
ALTER TABLE customer_contacts
ADD COLUMN IF NOT EXISTS linkedin VARCHAR(500);

CREATE INDEX IF NOT EXISTS idx_customer_contacts_linkedin
ON customer_contacts(linkedin)
WHERE linkedin IS NOT NULL;

COMMENT ON COLUMN customer_contacts.linkedin IS
  'LinkedIn Profile URL - for professional networking and relationship management';


-- ============================================================================
-- FELD 2: customer_contacts.xing
-- ============================================================================
-- Zweck: XING-Profil-URL (DACH-Region professional network)
-- Befüllung: Manuell via ContactEditDialog
-- Verwendung: Relationship Management (especially German-speaking markets)
ALTER TABLE customer_contacts
ADD COLUMN IF NOT EXISTS xing VARCHAR(500);

CREATE INDEX IF NOT EXISTS idx_customer_contacts_xing
ON customer_contacts(xing)
WHERE xing IS NOT NULL;

COMMENT ON COLUMN customer_contacts.xing IS
  'XING Profile URL - DACH-Region professional network for relationship management';


-- ============================================================================
-- FELD 3: customer_contacts.notes
-- ============================================================================
-- Zweck: Allgemeine Notizen zum Kontakt (Sales Notes, Follow-up Reminders, etc.)
-- Befüllung: Manuell via ContactEditDialog
-- Verwendung: Quick reference notes for sales team
-- NOTE: Unterschied zu personal_notes (Beziehungsebene: Hobbies, Familie)
--       → notes = geschäftliche Notizen, personal_notes = persönliche Beziehungsebene
ALTER TABLE customer_contacts
ADD COLUMN IF NOT EXISTS notes TEXT;

COMMENT ON COLUMN customer_contacts.notes IS
  'General business notes about this contact - quick reference for sales team';


-- ============================================================================
-- SUCCESS MESSAGE
-- ============================================================================
DO $$
BEGIN
  RAISE NOTICE '✅ Migration V10042 erfolgreich!';
  RAISE NOTICE '   - customer_contacts.linkedin (LinkedIn Profile URL)';
  RAISE NOTICE '   - customer_contacts.xing (XING Profile URL)';
  RAISE NOTICE '   - customer_contacts.notes (Business Notes - distinct from personal_notes)';
  RAISE NOTICE '';
  RAISE NOTICE '📋 NEXT STEPS:';
  RAISE NOTICE '   1. Contact-Entity: @Column linkedin, xing, notes hinzufügen';
  RAISE NOTICE '   2. ContactDTO: Felder hinzufügen';
  RAISE NOTICE '   3. ContactEditDialog.tsx: Bereits vorhanden (V2 fields aktiviert)';
  RAISE NOTICE '   4. Backend/Frontend Parity: ✅ FIXED';
END $$;
