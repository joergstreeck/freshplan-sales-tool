-- V248__leads_unique_indexes_simple.sql
-- Sprint 2.1.4: Create unique indexes for lead deduplication
-- SIMPLIFIED VERSION: No CONCURRENTLY to avoid CI hangs
--
-- ⚠️ WARNING: This migration is for DEV/TEST environments ONLY
-- ⚠️ Creates UNIQUE indexes WITHOUT CONCURRENTLY which locks tables
-- ⚠️ For PRODUCTION deployments:
-- ⚠️   1. Skip this migration (add to flyway.ignoreMigrationPatterns)
-- ⚠️   2. Use manual CONCURRENTLY index creation:
-- ⚠️      CREATE UNIQUE INDEX CONCURRENTLY uq_leads_email_canonical_v2 ...
--
-- This migration creates unique indexes for:
-- 1. Email deduplication
-- 2. Phone deduplication
-- 3. Company+City B2B deduplication

-- =====================================================
-- CREATE UNIQUE INDEXES (WITHOUT CONCURRENTLY)
-- =====================================================

-- Email unique index (only for canonical leads, excluding deleted)
CREATE UNIQUE INDEX IF NOT EXISTS uq_leads_email_canonical_v2
  ON leads(email_normalized)
  WHERE email_normalized IS NOT NULL
    AND is_canonical = true
    AND status != 'DELETED';

-- Phone unique index (only for canonical leads, excluding deleted)
CREATE UNIQUE INDEX IF NOT EXISTS ui_leads_phone_e164
  ON leads(phone_e164)
  WHERE phone_e164 IS NOT NULL
    AND is_canonical = true
    AND status != 'DELETED';

-- Combined company name + city unique index for B2B deduplication
CREATE UNIQUE INDEX IF NOT EXISTS ui_leads_company_city
  ON leads(company_name_normalized, city)
  WHERE company_name_normalized IS NOT NULL
    AND city IS NOT NULL
    AND is_canonical = true
    AND status != 'DELETED';

-- =====================================================
-- ADD COMMENTS FOR DOCUMENTATION
-- =====================================================
COMMENT ON INDEX uq_leads_email_canonical_v2 IS
  'Prevents duplicate canonical leads with same email';

COMMENT ON INDEX ui_leads_phone_e164 IS
  'Enforces unique phone numbers for canonical leads (excluding deleted)';

COMMENT ON INDEX ui_leads_company_city IS
  'Enforces unique company+city combination for B2B leads (excluding deleted)';

-- =====================================================
-- MIGRATION SUCCESS LOG
-- =====================================================
DO $$
BEGIN
  RAISE NOTICE 'V248 Migration completed successfully:';
  RAISE NOTICE '- Created unique email index';
  RAISE NOTICE '- Created unique phone index';
  RAISE NOTICE '- Created unique company+city index';
  RAISE NOTICE 'All indexes created WITHOUT CONCURRENTLY for CI compatibility';
END$$;