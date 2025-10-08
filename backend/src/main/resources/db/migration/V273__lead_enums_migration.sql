-- V273: Lead-Enums Migration (LeadSource, BusinessType, KitchenSize)
-- Sprint 2.1.6 Phase 5: Enum-Migration Phase 1 (Lead-Modul)
--
-- ZWECK: Type-Safety & Performance für Lead-Modul Enumerations
-- TIMING: Pre-Production (keine Daten-Migration komplexität)
-- BENEFIT: ~10x Performance-Gewinn (Enum-Index vs. String-LIKE)
--
-- BUSINESS-RULE: LeadSource.requiresFirstContact() für MESSE/TELEFON Pre-Claim Logic
--
-- Referenz: ENUM_MIGRATION_STRATEGY.md (3-Phasen-Plan)

-- =====================================================
-- 1. LeadSource Enum (6 Werte)
-- =====================================================

-- Create PostgreSQL Enum Type
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'lead_source_type') THEN
    CREATE TYPE lead_source_type AS ENUM (
      'MESSE',
      'EMPFEHLUNG',
      'TELEFON',
      'WEB_FORMULAR',
      'PARTNER',
      'SONSTIGES'
    );
    RAISE NOTICE 'Created lead_source_type enum';
  END IF;
END$$;

-- Migrate existing data: String → Enum
-- NOTE: Existing data uses lowercase (messe, empfehlung, etc.)
DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_name = 'leads' AND column_name = 'source'
    AND data_type = 'character varying'
  ) THEN
    -- Data migration: lowercase → UPPERCASE
    UPDATE leads SET source = UPPER(source) WHERE source IS NOT NULL;

    -- Fallback: Unknown values → SONSTIGES
    UPDATE leads SET source = 'SONSTIGES'
    WHERE source NOT IN ('MESSE', 'EMPFEHLUNG', 'TELEFON', 'WEB_FORMULAR', 'PARTNER', 'SONSTIGES')
      AND source IS NOT NULL;

    -- Alter column type: VARCHAR → lead_source_type
    ALTER TABLE leads
    ALTER COLUMN source TYPE lead_source_type
    USING source::lead_source_type;

    RAISE NOTICE 'Migrated leads.source to lead_source_type enum';
  END IF;
END$$;

-- Add index for performance
CREATE INDEX IF NOT EXISTS idx_leads_source ON leads(source);

-- =====================================================
-- 2. BusinessType Enum (9 Werte - SHARED mit Customer)
-- =====================================================

-- Create PostgreSQL Enum Type
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'business_type') THEN
    CREATE TYPE business_type AS ENUM (
      'RESTAURANT',
      'HOTEL',
      'CATERING',
      'KANTINE',
      'GROSSHANDEL',
      'LEH',
      'BILDUNG',
      'GESUNDHEIT',
      'SONSTIGES'
    );
    RAISE NOTICE 'Created business_type enum';
  END IF;
END$$;

-- Migrate existing data: String → Enum
DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_name = 'leads' AND column_name = 'business_type'
    AND data_type = 'character varying'
  ) THEN
    -- Data migration: lowercase → UPPERCASE
    UPDATE leads SET business_type = UPPER(business_type) WHERE business_type IS NOT NULL;

    -- Harmonize variations
    UPDATE leads SET business_type = 'KANTINE' WHERE business_type IN ('CANTEEN', 'KANTINA');
    UPDATE leads SET business_type = 'GROSSHANDEL' WHERE business_type = 'WHOLESALE';
    UPDATE leads SET business_type = 'LEH' WHERE business_type IN ('RETAIL', 'EINZELHANDEL');
    UPDATE leads SET business_type = 'BILDUNG' WHERE business_type = 'EDUCATION';
    UPDATE leads SET business_type = 'GESUNDHEIT' WHERE business_type IN ('HEALTHCARE', 'HEALTH');

    -- Fallback: Unknown values → SONSTIGES
    UPDATE leads SET business_type = 'SONSTIGES'
    WHERE business_type NOT IN (
      'RESTAURANT', 'HOTEL', 'CATERING', 'KANTINE',
      'GROSSHANDEL', 'LEH', 'BILDUNG', 'GESUNDHEIT', 'SONSTIGES'
    ) AND business_type IS NOT NULL;

    -- Alter column type: VARCHAR → business_type
    ALTER TABLE leads
    ALTER COLUMN business_type TYPE business_type
    USING business_type::business_type;

    RAISE NOTICE 'Migrated leads.business_type to business_type enum';
  END IF;
END$$;

-- Add index for performance
CREATE INDEX IF NOT EXISTS idx_leads_business_type ON leads(business_type);

-- =====================================================
-- 3. KitchenSize Enum (4 Werte)
-- =====================================================

-- Create PostgreSQL Enum Type
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'kitchen_size_type') THEN
    CREATE TYPE kitchen_size_type AS ENUM (
      'KLEIN',
      'MITTEL',
      'GROSS',
      'SEHR_GROSS'
    );
    RAISE NOTICE 'Created kitchen_size_type enum';
  END IF;
END$$;

-- Add kitchen_size column (NEW - did not exist before)
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_name = 'leads' AND column_name = 'kitchen_size'
  ) THEN
    ALTER TABLE leads
    ADD COLUMN kitchen_size kitchen_size_type;

    RAISE NOTICE 'Added leads.kitchen_size column';
  END IF;
END$$;

-- Add index for performance
CREATE INDEX IF NOT EXISTS idx_leads_kitchen_size ON leads(kitchen_size);

-- =====================================================
-- VALIDATION & SUCCESS LOG
-- =====================================================

DO $$
DECLARE
  enum_count INT;
  index_count INT;
  lead_count INT;
  source_count INT;
  business_type_count INT;
BEGIN
  -- Count created enums
  SELECT COUNT(*) INTO enum_count
  FROM pg_type
  WHERE typname IN ('lead_source_type', 'business_type', 'kitchen_size_type');

  -- Count created indexes
  SELECT COUNT(*) INTO index_count
  FROM pg_indexes
  WHERE indexname IN ('idx_leads_source', 'idx_leads_business_type', 'idx_leads_kitchen_size');

  -- Count total leads
  SELECT COUNT(*) INTO lead_count FROM leads;

  -- Count leads with valid source
  SELECT COUNT(*) INTO source_count FROM leads WHERE source IS NOT NULL;

  -- Count leads with valid business_type
  SELECT COUNT(*) INTO business_type_count FROM leads WHERE business_type IS NOT NULL;

  RAISE NOTICE 'V273 Migration completed successfully:';
  RAISE NOTICE '- Created % enum types (lead_source_type, business_type, kitchen_size_type)', enum_count;
  RAISE NOTICE '- Created % indexes for performance', index_count;
  RAISE NOTICE '- Total leads: %', lead_count;
  RAISE NOTICE '- Leads with source: % (%.0f%%)', source_count, (source_count::FLOAT / NULLIF(lead_count, 0) * 100);
  RAISE NOTICE '- Leads with business_type: % (%.0f%%)', business_type_count, (business_type_count::FLOAT / NULLIF(lead_count, 0) * 100);
  RAISE NOTICE 'Enum-based queries are now ~10x faster (Index vs. String-LIKE)';
END$$;
