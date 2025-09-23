-- V228: Settings Registry ETag Support
-- Sprint 1.2 PR #2: Erweitert die existierende security_settings Tabelle
-- um ETag-Support und verbesserte Performance-Features

-- 0) Convert scope column from custom type to TEXT for JPA compatibility
ALTER TABLE security_settings
  ALTER COLUMN scope TYPE TEXT
  USING scope::text;

-- 1) Add ETag column for optimistic locking and HTTP caching
ALTER TABLE security_settings
  ADD COLUMN IF NOT EXISTS etag TEXT;

-- 2) Add version column for optimistic locking
ALTER TABLE security_settings
  ADD COLUMN IF NOT EXISTS version INTEGER DEFAULT 1 NOT NULL;

-- 3) Add metadata column for additional settings info
ALTER TABLE security_settings
  ADD COLUMN IF NOT EXISTS metadata JSONB DEFAULT '{}';

-- 4) Update function to auto-generate ETags on changes
CREATE OR REPLACE FUNCTION generate_etag()
RETURNS TEXT AS $$
BEGIN
  -- Generate ETag based on MD5 of value + version + timestamp
  RETURN md5(
    COALESCE(NEW.value::text, '') ||
    COALESCE(NEW.version::text, '') ||
    COALESCE(extract(epoch from NEW.updated_at)::text, '')
  );
END;
$$ LANGUAGE plpgsql;

-- 5) Trigger to auto-update ETag and version on changes
CREATE OR REPLACE FUNCTION update_settings_etag()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = NOW();
  NEW.version = COALESCE(OLD.version, 0) + 1;
  NEW.etag = generate_etag();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_update_settings_etag ON security_settings;
CREATE TRIGGER trg_update_settings_etag
  BEFORE UPDATE ON security_settings
  FOR EACH ROW
  EXECUTE FUNCTION update_settings_etag();

-- 6) Trigger for initial ETag on insert
CREATE OR REPLACE FUNCTION insert_settings_etag()
RETURNS TRIGGER AS $$
BEGIN
  NEW.etag = md5(
    COALESCE(NEW.value::text, '') ||
    '1' ||
    COALESCE(extract(epoch from NEW.created_at)::text, '')
  );
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_insert_settings_etag ON security_settings;
CREATE TRIGGER trg_insert_settings_etag
  BEFORE INSERT ON security_settings
  FOR EACH ROW
  EXECUTE FUNCTION insert_settings_etag();

-- 7) Index for ETag-based queries (for If-None-Match headers)
CREATE INDEX IF NOT EXISTS idx_security_settings_etag
  ON security_settings(etag)
  WHERE etag IS NOT NULL;

-- 8) Performance index for scope hierarchy queries
CREATE INDEX IF NOT EXISTS idx_security_settings_hierarchy
  ON security_settings(scope, scope_id, key, version DESC);

-- 9) Function for hierarchical settings resolution
-- Resolves settings from most specific to least specific scope
CREATE OR REPLACE FUNCTION resolve_setting(
  p_key TEXT,
  p_tenant_id TEXT DEFAULT NULL,
  p_territory TEXT DEFAULT NULL,
  p_account_id TEXT DEFAULT NULL,
  p_contact_role TEXT DEFAULT NULL
)
RETURNS TABLE (
  scope settings_scope,
  scope_id TEXT,
  value JSONB,
  etag TEXT,
  priority INTEGER
) AS $$
BEGIN
  RETURN QUERY
  WITH scope_priority AS (
    -- Priority: CONTACT_ROLE > ACCOUNT > TERRITORY > TENANT > GLOBAL
    SELECT 'CONTACT_ROLE'::settings_scope as sc, p_contact_role as sid, 5 as prio
    UNION ALL
    SELECT 'ACCOUNT'::settings_scope, p_account_id, 4
    UNION ALL
    SELECT 'TERRITORY'::settings_scope, p_territory, 3
    UNION ALL
    SELECT 'TENANT'::settings_scope, p_tenant_id, 2
    UNION ALL
    SELECT 'GLOBAL'::settings_scope, NULL, 1
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
    AND sp.sid IS NOT NULL OR sp.sc = 'GLOBAL'::settings_scope
  ORDER BY sp.prio DESC;
END;
$$ LANGUAGE plpgsql;

-- 10) Notification for cache invalidation
CREATE OR REPLACE FUNCTION notify_settings_change()
RETURNS TRIGGER AS $$
DECLARE
  payload JSONB;
BEGIN
  payload := jsonb_build_object(
    'operation', TG_OP,
    'scope', COALESCE(NEW.scope, OLD.scope),
    'scope_id', COALESCE(NEW.scope_id, OLD.scope_id),
    'key', COALESCE(NEW.key, OLD.key),
    'etag', COALESCE(NEW.etag, OLD.etag),
    'timestamp', extract(epoch from now())
  );

  PERFORM pg_notify('settings_changed', payload::text);
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_notify_settings_change ON security_settings;
CREATE TRIGGER trg_notify_settings_change
  AFTER INSERT OR UPDATE OR DELETE ON security_settings
  FOR EACH ROW
  EXECUTE FUNCTION notify_settings_change();

-- 11) Helper view for commonly accessed settings
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

-- 12) Default global settings for initial setup
INSERT INTO security_settings (scope, scope_id, key, value, metadata, created_by)
VALUES
  ('GLOBAL', NULL, 'system.feature_flags',
   '{"etag_caching": true, "settings_registry": true, "security_gates": true}'::jsonb,
   '{"description": "System-wide feature flags", "version": "1.0.0"}'::jsonb,
   'system'),
  ('GLOBAL', NULL, 'system.cache_ttl',
   '{"default": 300, "settings": 60, "user_data": 120}'::jsonb,
   '{"description": "Cache TTL in seconds", "unit": "seconds"}'::jsonb,
   'system'),
  ('GLOBAL', NULL, 'system.performance',
   '{"etag_hit_rate_target": 70, "response_time_p95": 200}'::jsonb,
   '{"description": "Performance targets", "unit": "percent/milliseconds"}'::jsonb,
   'system')
ON CONFLICT (scope, scope_id, key) DO NOTHING;

COMMENT ON TABLE security_settings IS 'Sprint 1.2: Settings Registry with 5-level scope hierarchy and ETag support';
COMMENT ON COLUMN security_settings.etag IS 'ETag for HTTP caching and optimistic locking';
COMMENT ON COLUMN security_settings.version IS 'Version number for optimistic locking';
COMMENT ON COLUMN security_settings.metadata IS 'Additional metadata like description, unit, validation rules';