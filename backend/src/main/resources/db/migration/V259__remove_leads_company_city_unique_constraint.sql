-- V259__remove_leads_company_city_unique_constraint.sql
-- Sprint 2.1.5: Remove hard unique constraint on company_name_normalized + city
--
-- REASON: According to DEDUPE_POLICY.md, "company + city" should be a SOFT collision (WARNING),
--         not a HARD collision (BLOCK). The unique constraint enforces a hard block at DB level.
--
-- Business Rule:
--   - Hard Collision (BLOCK): Email OR Phone OR Company + PostalCode
--   - Soft Collision (WARNING): Company + City
--
-- The dedupe logic will be moved to application layer (LeadService) to allow:
--   1. Soft warnings with user override
--   2. Audit logging
--   3. Flexible business rules
--
-- References:
--   - /docs/planung/features-neu/02_neukundengewinnung/artefakte/SPRINT_2_1_5/DEDUPE_POLICY.md
--   - V10012 created this constraint (TEST/DEV migration)

-- =====================================================
-- DROP UNIQUE INDEX
-- =====================================================

DROP INDEX IF EXISTS ui_leads_company_city;

-- =====================================================
-- MIGRATION SUCCESS LOG
-- =====================================================

DO $$
BEGIN
  RAISE NOTICE 'V259 Migration completed successfully:';
  RAISE NOTICE '- Removed ui_leads_company_city unique constraint';
  RAISE NOTICE '- Dedupe logic for company+city moved to application layer';
  RAISE NOTICE '- Hard constraints remain for: email, phone';
END$$;
