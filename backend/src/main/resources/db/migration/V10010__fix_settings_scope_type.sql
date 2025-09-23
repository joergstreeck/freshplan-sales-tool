-- V229: Fix Settings Scope Type for JPA Compatibility
-- Converts the custom settings_scope type to TEXT for proper JPA mapping

-- 1) Drop dependent views and functions that use the custom type
DROP VIEW IF EXISTS v_active_settings CASCADE;
DROP FUNCTION IF EXISTS resolve_setting CASCADE;

-- 2) Drop triggers temporarily
DROP TRIGGER IF EXISTS trg_update_settings_etag ON security_settings;
DROP TRIGGER IF EXISTS trg_insert_settings_etag ON security_settings;
DROP TRIGGER IF EXISTS trg_notify_settings_change ON security_settings;

-- 3) Convert scope column to TEXT
ALTER TABLE security_settings
  ALTER COLUMN scope TYPE TEXT
  USING scope::text;

-- 4) Recreate triggers
CREATE TRIGGER trg_update_settings_etag
  BEFORE UPDATE ON security_settings
  FOR EACH ROW
  EXECUTE FUNCTION update_settings_etag();

CREATE TRIGGER trg_insert_settings_etag
  BEFORE INSERT ON security_settings
  FOR EACH ROW
  EXECUTE FUNCTION insert_settings_etag();

CREATE TRIGGER trg_notify_settings_change
  AFTER INSERT OR DELETE OR UPDATE ON security_settings
  FOR EACH ROW
  EXECUTE FUNCTION notify_settings_change();

-- 5) Recreate the hierarchical resolution function with TEXT type
CREATE OR REPLACE FUNCTION resolve_setting(
  p_key TEXT,
  p_tenant_id TEXT DEFAULT NULL,
  p_territory TEXT DEFAULT NULL,
  p_account_id TEXT DEFAULT NULL,
  p_contact_role TEXT DEFAULT NULL
)
RETURNS TABLE (
  scope TEXT,
  scope_id TEXT,
  value JSONB,
  etag TEXT,
  priority INTEGER
) AS $$
BEGIN
  RETURN QUERY
  WITH scope_priority AS (
    -- Priority: CONTACT_ROLE > ACCOUNT > TERRITORY > TENANT > GLOBAL
    SELECT 'CONTACT_ROLE' as sc, p_contact_role as sid, 5 as prio
    UNION ALL
    SELECT 'ACCOUNT', p_account_id, 4
    UNION ALL
    SELECT 'TERRITORY', p_territory, 3
    UNION ALL
    SELECT 'TENANT', p_tenant_id, 2
    UNION ALL
    SELECT 'GLOBAL', NULL, 1
  )
  SELECT
    s.scope,
    s.scope_id,
    s.value,
    s.etag,
    sp.prio as priority
  FROM security_settings s
  JOIN scope_priority sp ON s.scope = sp.sc
    AND (s.scope_id = sp.sid OR (s.scope_id IS NULL AND sp.sid IS NULL))
  WHERE s.key = p_key
    AND sp.sid IS NOT NULL OR sp.sc = 'GLOBAL'
  ORDER BY sp.prio DESC;
END;
$$ LANGUAGE plpgsql;

-- 6) Recreate the view with TEXT comparisons
CREATE OR REPLACE VIEW v_active_settings AS
SELECT
  s.scope,
  s.scope_id,
  s.key,
  s.value,
  s.etag,
  s.version,
  s.metadata,
  s.updated_at
FROM security_settings s
WHERE s.value->>'active' = 'true'
   OR s.value->>'enabled' = 'true'
   OR NOT EXISTS (
     SELECT 1 FROM security_settings s2
     WHERE s2.key = s.key
       AND s2.scope = s.scope
       AND s2.scope_id = s.scope_id
       AND (s2.value->>'active' = 'false' OR s2.value->>'enabled' = 'false')
   );

-- 7) Add check constraint to ensure valid scope values
ALTER TABLE security_settings
  DROP CONSTRAINT IF EXISTS chk_security_settings_scope;

ALTER TABLE security_settings
  ADD CONSTRAINT chk_security_settings_scope
  CHECK (scope IN ('GLOBAL', 'TENANT', 'TERRITORY', 'ACCOUNT', 'CONTACT_ROLE'));

COMMENT ON COLUMN security_settings.scope IS 'TEXT-based scope for JPA compatibility (GLOBAL/TENANT/TERRITORY/ACCOUNT/CONTACT_ROLE)';