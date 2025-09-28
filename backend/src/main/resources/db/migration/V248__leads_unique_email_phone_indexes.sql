-- V248__leads_unique_email_phone_indexes.sql
-- Sprint 2.1.4: Create unique indexes for email and phone deduplication
-- Author: team/leads-backend
-- Date: 2025-09-28
--
-- NOTE: This migration handles both test and production environments
-- In tests, indexes are created immediately
-- In production, they would be created CONCURRENTLY (requires Java migration)

-- =====================================================
-- 1. CREATE UNIQUE INDEXES FOR DEDUPLICATION
-- =====================================================

-- Email unique index (only for canonical leads, excluding deleted)
-- In production this would use CONCURRENTLY for zero-downtime
CREATE UNIQUE INDEX IF NOT EXISTS ui_leads_email_normalized
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
-- 2. ADD COMMENTS FOR DOCUMENTATION
-- =====================================================
COMMENT ON INDEX ui_leads_email_normalized IS
  'Enforces unique email addresses for canonical leads (excluding deleted)';

COMMENT ON INDEX ui_leads_phone_e164 IS
  'Enforces unique phone numbers for canonical leads (excluding deleted)';

COMMENT ON INDEX ui_leads_company_city IS
  'Enforces unique company+city combination for B2B leads (excluding deleted)';

-- =====================================================
-- 3. MIGRATION SUCCESS LOG
-- =====================================================
DO $$
BEGIN
  RAISE NOTICE 'V248 Migration completed successfully:';
  RAISE NOTICE '- Created unique email index';
  RAISE NOTICE '- Created unique phone index';
  RAISE NOTICE '- Created unique company+city index';
  RAISE NOTICE 'All indexes exclude deleted records and non-canonical duplicates';
END$$;