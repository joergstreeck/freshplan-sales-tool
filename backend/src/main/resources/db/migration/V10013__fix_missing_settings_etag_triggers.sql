-- V272: Fix Missing Settings ETag Triggers
-- Sprint 2.1.6 Phase 5 - ETag-Trigger waren nicht in DB registriert
--
-- PROBLEM: V10011 hat die Funktionen erstellt, aber Trigger wurden nicht registriert
-- IMPACT: security_settings.etag war immer NULL → ETag-basiertes Caching funktionierte nicht
--
-- LÖSUNG: Trigger manuell registrieren (BEFORE INSERT/UPDATE)

-- =====================================================
-- CREATE MISSING TRIGGERS
-- =====================================================

DROP TRIGGER IF EXISTS trg_insert_settings_etag ON security_settings;
CREATE TRIGGER trg_insert_settings_etag
  BEFORE INSERT ON security_settings
  FOR EACH ROW
  EXECUTE FUNCTION insert_settings_etag();

DROP TRIGGER IF EXISTS trg_update_settings_etag ON security_settings;
CREATE TRIGGER trg_update_settings_etag
  BEFORE UPDATE ON security_settings
  FOR EACH ROW
  EXECUTE FUNCTION update_settings_etag();

-- =====================================================
-- BACKFILL EXISTING SETTINGS (if any have NULL ETags)
-- =====================================================

UPDATE security_settings
SET
  etag = md5(
    COALESCE(value::text, '') ||
    COALESCE(version::text, '1') ||
    COALESCE(extract(epoch from COALESCE(updated_at, created_at, NOW()))::text, '')
  )
WHERE etag IS NULL;

-- =====================================================
-- MIGRATION SUCCESS LOG
-- =====================================================

DO $$
DECLARE
  trigger_count INT;
  backfilled_count INT;
BEGIN
  -- Count registered triggers
  SELECT COUNT(*) INTO trigger_count
  FROM pg_trigger
  WHERE tgname IN ('trg_insert_settings_etag', 'trg_update_settings_etag');

  -- Count backfilled settings
  SELECT COUNT(*) INTO backfilled_count
  FROM security_settings
  WHERE etag IS NOT NULL;

  RAISE NOTICE 'V272 Migration completed successfully:';
  RAISE NOTICE '- Registered % ETag triggers', trigger_count;
  RAISE NOTICE '- Backfilled % settings with ETags', backfilled_count;
  RAISE NOTICE 'ETag-based caching is now fully functional';
END$$;
