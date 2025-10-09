-- V275: Create lead_contacts table - 100% PARITY mit customer_contacts (ADR-007 Option C)
--
-- Architektur-Entscheidung: Sprint 2.1.6 Phase 5+
-- - VOLLSTÄNDIGE Harmonisierung mit Customer-Modul (ALLE Felder übernommen)
-- - Multi-Contact Support (N:1 Beziehung, mehrere Kontakte pro Lead)
-- - CRM Intelligence Ready (warmth_score, data_quality_score, relationship data)
-- - Future-Proof (keine zweite Migration nötig für Customer-Features)
--
-- Referenz: docs/planung/features-neu/02_neukundengewinnung/artefakte/LEAD_CONTACTS_ARCHITECTURE.md
-- Vergleich: customer_contacts Tabelle (identische Struktur)
--
-- Author: Sprint 2.1.6 Phase 5+
-- Date: 2025-10-08

-- ============================================================================
-- 1. CREATE TABLE lead_contacts (100% PARITY mit customer_contacts)
-- ============================================================================

CREATE TABLE lead_contacts (
  -- Primary Key & Foreign Key
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  lead_id BIGINT NOT NULL,

  -- Basic Info (IDENTICAL to customer_contacts lines 42-58)
  salutation VARCHAR(20),              -- herr, frau, divers
  title VARCHAR(50),                   -- Dr., Prof., etc.
  first_name VARCHAR(100) NOT NULL,
  last_name VARCHAR(100) NOT NULL,
  position VARCHAR(100),
  decision_level VARCHAR(50),          -- executive, manager, operational, influencer

  -- Contact Info (IDENTICAL to customer_contacts lines 60-68)
  email VARCHAR(255),
  phone VARCHAR(50),
  mobile VARCHAR(50),

  -- Flags (IDENTICAL to customer_contacts lines 70-75)
  is_primary BOOLEAN NOT NULL DEFAULT false,
  is_active BOOLEAN NOT NULL DEFAULT true,

  -- Relationship Data - CRM Intelligence (IDENTICAL to customer_contacts lines 82-96)
  birthday DATE,
  hobbies VARCHAR(500),                -- Comma-separated list
  family_status VARCHAR(50),           -- single, married, divorced, widowed
  children_count INTEGER,
  personal_notes TEXT,

  -- Intelligence Data - Sales Excellence (IDENTICAL to customer_contacts lines 98-109)
  warmth_score INTEGER DEFAULT 50,     -- 0-100, default neutral
  warmth_confidence INTEGER DEFAULT 0, -- 0-100, confidence in warmth score
  last_interaction_date TIMESTAMP,
  interaction_count INTEGER DEFAULT 0,

  -- Data Quality & Freshness (IDENTICAL to customer_contacts lines 111-116)
  data_quality_score INTEGER,          -- 0-100, overall data quality score
  data_quality_recommendations TEXT,   -- Semicolon-separated recommendations

  -- Legacy fields from customer_contacts (for future compatibility)
  is_decision_maker BOOLEAN NOT NULL DEFAULT false,
  is_deleted BOOLEAN NOT NULL DEFAULT false,

  -- Audit Fields (IDENTICAL to customer_contacts lines 125-138)
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
  created_by VARCHAR(100),
  updated_by VARCHAR(100),

  -- Constraints
  CONSTRAINT fk_lead_contacts_lead
    FOREIGN KEY (lead_id) REFERENCES leads(id) ON DELETE CASCADE,

  CONSTRAINT chk_lead_contact_email_or_phone_or_mobile
    CHECK (email IS NOT NULL OR phone IS NOT NULL OR mobile IS NOT NULL),

  CONSTRAINT chk_lead_contact_names_not_empty
    CHECK (
      LENGTH(TRIM(first_name)) > 0 AND
      LENGTH(TRIM(last_name)) > 0
    )
);

-- ============================================================================
-- 2. CREATE INDEXES (IDENTICAL pattern to customer_contacts)
-- ============================================================================

CREATE INDEX idx_lead_contacts_lead_id
  ON lead_contacts(lead_id);

CREATE INDEX idx_lead_contacts_primary
  ON lead_contacts(lead_id, is_primary)
  WHERE is_primary = true;

CREATE INDEX idx_lead_contacts_active
  ON lead_contacts(is_active)
  WHERE is_active = true;

CREATE INDEX idx_lead_contacts_email
  ON lead_contacts(email)
  WHERE email IS NOT NULL;

CREATE INDEX idx_lead_contacts_deleted
  ON lead_contacts(is_deleted)
  WHERE is_deleted = false;

-- ============================================================================
-- 3. CREATE FUNCTION for updated_at auto-update (if not exists)
-- ============================================================================

CREATE OR REPLACE FUNCTION update_lead_contacts_updated_at()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = NOW();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- ============================================================================
-- 4. CREATE TRIGGER for updated_at auto-update
-- ============================================================================

CREATE TRIGGER trg_update_lead_contacts_updated_at
  BEFORE UPDATE ON lead_contacts
  FOR EACH ROW
  EXECUTE FUNCTION update_lead_contacts_updated_at();

-- ============================================================================
-- 5. DATA MIGRATION from existing leads (contact_person → firstName + lastName)
-- ============================================================================

-- Split "Max Mustermann" into first_name="Max", last_name="Mustermann"
-- Handle edge cases:
-- - Single word: "Max" → first_name="Max", last_name="Contact"
-- - NULL: Skip (will be handled by constraint)
-- - Multiple spaces: "Dr. Max Mustermann" → first_name="Dr. Max", last_name="Mustermann"

INSERT INTO lead_contacts (
  lead_id,
  first_name,
  last_name,
  email,
  phone,
  is_primary,
  created_at,
  created_by
)
SELECT
  l.id AS lead_id,

  -- Split contact_person: Take everything before last space as first_name
  CASE
    WHEN l.contact_person IS NOT NULL AND l.contact_person LIKE '% %'
      THEN TRIM(SUBSTRING(l.contact_person FROM 1 FOR LENGTH(l.contact_person) - POSITION(' ' IN REVERSE(l.contact_person))))
    WHEN l.contact_person IS NOT NULL AND LENGTH(TRIM(l.contact_person)) > 0
      THEN TRIM(l.contact_person)
    ELSE 'Unknown'
  END AS first_name,

  -- Last word after last space as last_name
  CASE
    WHEN l.contact_person IS NOT NULL AND l.contact_person LIKE '% %'
      THEN TRIM(SUBSTRING(l.contact_person FROM LENGTH(l.contact_person) - POSITION(' ' IN REVERSE(l.contact_person)) + 2))
    ELSE 'Contact'
  END AS last_name,

  l.email,
  l.phone,
  true AS is_primary, -- All migrated contacts become primary
  COALESCE(l.created_at, NOW()) AS created_at,
  'migration_v275' AS created_by

FROM leads l
WHERE
  l.contact_person IS NOT NULL
  OR l.email IS NOT NULL
  OR l.phone IS NOT NULL;

-- ============================================================================
-- 5. DEPRECATE old contact fields in leads table (Backward Compatibility)
-- ============================================================================

-- Mark old columns as deprecated - will be removed in V280
COMMENT ON COLUMN leads.contact_person IS 'DEPRECATED: Use lead_contacts table. Will be removed in V280. See ADR-007.';
COMMENT ON COLUMN leads.email IS 'DEPRECATED: Use lead_contacts table for contact email. Will be removed in V280.';
COMMENT ON COLUMN leads.phone IS 'DEPRECATED: Use lead_contacts table for contact phone. Will be removed in V280.';

-- ============================================================================
-- MIGRATION SUMMARY
-- ============================================================================
-- Created: lead_contacts table with 100% customer_contacts parity (26 fields)
-- Indexes: 5 indexes for performance (lead_id, primary, active, email, deleted)
-- Migrated: Existing contact_person/email/phone data to structured lead_contacts
-- Deprecated: leads.contact_person/email/phone columns (removal in V280)
-- Next: V276 - Backward Compatibility Trigger (sync lead_contacts.primary → leads.contact_person)
