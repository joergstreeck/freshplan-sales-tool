-- V10011: Fix Settings ETag Functions
-- Behebt SQL-Fehler in den ETag-Trigger-Funktionen

-- 1) Fix the generate_etag function to work with triggers properly
CREATE OR REPLACE FUNCTION generate_etag()
RETURNS TEXT AS $$
BEGIN
  -- Generate ETag based on MD5 of value + version + timestamp
  RETURN md5(
    COALESCE(NEW.value::text, '') ||
    COALESCE(NEW.version::text, '') ||
    COALESCE(extract(epoch from COALESCE(NEW.updated_at, NEW.created_at, NOW()))::text, '')
  );
END;
$$ LANGUAGE plpgsql;

-- 2) Fix the update_settings_etag function
CREATE OR REPLACE FUNCTION update_settings_etag()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = NOW();
  NEW.version = COALESCE(OLD.version, 0) + 1;
  -- Generate ETag directly in trigger function (no separate function call)
  NEW.etag = md5(
    COALESCE(NEW.value::text, '') ||
    COALESCE(NEW.version::text, '') ||
    COALESCE(extract(epoch from NEW.updated_at)::text, '')
  );
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 3) Fix the insert_settings_etag function
CREATE OR REPLACE FUNCTION insert_settings_etag()
RETURNS TRIGGER AS $$
BEGIN
  -- Set created_at if not set
  IF NEW.created_at IS NULL THEN
    NEW.created_at = NOW();
  END IF;

  -- Generate ETag directly in trigger function
  NEW.etag = md5(
    COALESCE(NEW.value::text, '') ||
    '1' ||
    COALESCE(extract(epoch from NEW.created_at)::text, '')
  );
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 4) Recreate triggers with correct functions
DROP TRIGGER IF EXISTS trg_update_settings_etag ON security_settings;
CREATE TRIGGER trg_update_settings_etag
  BEFORE UPDATE ON security_settings
  FOR EACH ROW
  EXECUTE FUNCTION update_settings_etag();

DROP TRIGGER IF EXISTS trg_insert_settings_etag ON security_settings;
CREATE TRIGGER trg_insert_settings_etag
  BEFORE INSERT ON security_settings
  FOR EACH ROW
  EXECUTE FUNCTION insert_settings_etag();

COMMENT ON FUNCTION update_settings_etag() IS 'Fixed trigger function for updating ETag on security_settings UPDATE';
COMMENT ON FUNCTION insert_settings_etag() IS 'Fixed trigger function for generating ETag on security_settings INSERT';