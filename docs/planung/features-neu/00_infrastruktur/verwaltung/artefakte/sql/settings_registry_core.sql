-- Migration: $(./scripts/get-next-migration.sh)__settings_registry_core.sql
-- Foundation: Hybrid Registry (Meta in Tabellen, Values in JSONB), RLS fail-closed, LISTEN/NOTIFY for cache busting
-- Requires: pgcrypto (for gen_random_uuid), PostgreSQL 13+

CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS settings_registry (
  key             text PRIMARY KEY,
  type            text NOT NULL CHECK (type IN ('scalar','list','object')),
  scope           jsonb NOT NULL,
  schema          jsonb,
  default_value   jsonb,
  merge_strategy  text NOT NULL CHECK (merge_strategy IN ('replace','merge','append')),
  created_at      timestamptz NOT NULL DEFAULT now(),
  updated_at      timestamptz NOT NULL DEFAULT now()
);

CREATE OR REPLACE FUNCTION trg_settings_registry_ut() RETURNS trigger AS $$
BEGIN
  NEW.updated_at := now();
  RETURN NEW;
END; $$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_settings_registry_ut ON settings_registry;
CREATE TRIGGER trg_settings_registry_ut BEFORE UPDATE ON settings_registry
FOR EACH ROW EXECUTE PROCEDURE trg_settings_registry_ut();

CREATE TABLE IF NOT EXISTS settings_store (
  id             uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  key            text NOT NULL REFERENCES settings_registry(key) ON DELETE CASCADE,
  tenant_id      uuid NULL,
  org_id         uuid NULL,
  user_id        uuid NULL,
  value          jsonb NOT NULL,
  updated_by     uuid NOT NULL,
  updated_at     timestamptz NOT NULL DEFAULT now(),
  CONSTRAINT uq_settings_store UNIQUE (key, tenant_id, org_id, user_id)
);

CREATE TABLE IF NOT EXISTS settings_effective (
  key            text NOT NULL,
  tenant_id      uuid NULL,
  org_id         uuid NULL,
  user_id        uuid NULL,
  value          jsonb NOT NULL,
  etag           text NOT NULL,
  computed_at    timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (key, tenant_id, org_id, user_id)
);

ALTER TABLE settings_registry ENABLE ROW LEVEL SECURITY;
ALTER TABLE settings_store    ENABLE ROW LEVEL SECURITY;
ALTER TABLE settings_effective ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS rls_registry_read ON settings_registry;
CREATE POLICY rls_registry_read ON settings_registry
  USING (true);

DROP POLICY IF EXISTS rls_registry_write ON settings_registry;
CREATE POLICY rls_registry_write ON settings_registry
  USING ( current_setting('app.user_id', true) IS NOT NULL
          AND 'admin' = ANY( string_to_array( COALESCE(current_setting('app.roles', true), ''), ',' ) ) )
  WITH CHECK ( true );

DROP POLICY IF EXISTS rls_store_read ON settings_store;
CREATE POLICY rls_store_read ON settings_store
  USING (
    'admin' = ANY(string_to_array(COALESCE(current_setting('app.roles', true), ''), ','))
    OR 'auditor' = ANY(string_to_array(COALESCE(current_setting('app.roles', true), ''), ','))
    OR ( (tenant_id::text IS NULL OR tenant_id::text = current_setting('app.tenant_id', true))
         AND (org_id::text    IS NULL OR org_id::text    = current_setting('app.org_id', true))
         AND (user_id::text   IS NULL OR user_id::text   = current_setting('app.user_id', true)) )
  );

DROP POLICY IF EXISTS rls_store_write ON settings_store;
CREATE POLICY rls_store_write ON settings_store
  USING (
    'admin' = ANY(string_to_array(COALESCE(current_setting('app.roles', true), ''), ','))
    OR (
      'config' = ANY(string_to_array(COALESCE(current_setting('app.roles', true), ''), ','))
      AND (tenant_id::text IS NULL OR tenant_id::text = current_setting('app.tenant_id', true))
      AND (org_id::text    IS NULL OR org_id::text    = current_setting('app.org_id', true))
    )
    OR (
      user_id::text = current_setting('app.user_id', true)
    )
  )
  WITH CHECK (true);

DROP POLICY IF EXISTS rls_effective_read ON settings_effective;
CREATE POLICY rls_effective_read ON settings_effective
  USING (
    'admin' = ANY(string_to_array(COALESCE(current_setting('app.roles', true), ''), ','))
    OR 'auditor' = ANY(string_to_array(COALESCE(current_setting('app.roles', true), ''), ','))
    OR ( (tenant_id::text IS NULL OR tenant_id::text = current_setting('app.tenant_id', true))
         AND (org_id::text    IS NULL OR org_id::text    = current_setting('app.org_id', true))
         AND (user_id::text   IS NULL OR user_id::text   = current_setting('app.user_id', true)) )
  );

CREATE OR REPLACE FUNCTION settings_store_notify() RETURNS trigger AS $$
DECLARE
  payload text;
BEGIN
  IF (TG_OP = 'DELETE') THEN
    payload := json_build_object(
      'op','DELETE','key',OLD.key,
      'tenantId', OLD.tenant_id, 'orgId', OLD.org_id, 'userId', OLD.user_id
    )::text;
  ELSE
    payload := json_build_object(
      'op',TG_OP,'key',NEW.key,
      'tenantId', NEW.tenant_id, 'orgId', NEW.org_id, 'userId', NEW.user_id
    )::text;
  END IF;
  PERFORM pg_notify('settings_changed', payload);
  RETURN COALESCE(NEW, OLD);
END; $$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_settings_store_notify_iud ON settings_store;
CREATE TRIGGER trg_settings_store_notify_iud
AFTER INSERT OR UPDATE OR DELETE ON settings_store
FOR EACH ROW EXECUTE PROCEDURE settings_store_notify();

-- Seeds
INSERT INTO settings_registry(key, type, scope, schema, default_value, merge_strategy) VALUES
 ('lead.protection.baseMonths','scalar','["global"]','{"type":"object","required":["months"],"properties":{"months":{"type":"integer","minimum":1,"maximum":24}}}','{"months":6}','replace')
ON CONFLICT (key) DO NOTHING;

INSERT INTO settings_registry(key, type, scope, schema, default_value, merge_strategy) VALUES
 ('lead.protection.progressDays','scalar','["global"]','{"type":"object","required":["days"],"properties":{"days":{"type":"integer","minimum":7,"maximum":120}}}','{"days":60}','replace')
ON CONFLICT (key) DO NOTHING;

INSERT INTO settings_registry(key, type, scope, schema, default_value, merge_strategy) VALUES
 ('lead.protection.graceDays','scalar','["global"]','{"type":"object","required":["days"],"properties":{"days":{"type":"integer","minimum":1,"maximum":30}}}','{"days":10}','replace')
ON CONFLICT (key) DO NOTHING;

INSERT INTO settings_registry(key, type, scope, schema, default_value, merge_strategy) VALUES
 ('lead.protection.mode','scalar','["global"]','{"type":"object","properties":{"mode":{"type":"string","enum":["GREATEST","LEAST"]}}}','{"mode":"GREATEST"}','replace')
ON CONFLICT (key) DO NOTHING;

INSERT INTO settings_registry(key, type, scope, schema, default_value, merge_strategy) VALUES
 ('ai.budget.monthly.cap','scalar','["tenant","org"]','{"type":"number","minimum":0}', '1000','replace')
ON CONFLICT (key) DO NOTHING;

INSERT INTO settings_registry(key, type, scope, schema, default_value, merge_strategy) VALUES
 ('ai.routing.confidence.threshold','scalar','["user","org","tenant"]','{"type":"number","minimum":0,"maximum":1}', '0.7','replace')
ON CONFLICT (key) DO NOTHING;

INSERT INTO settings_registry(key, type, scope, schema, default_value, merge_strategy) VALUES
 ('ai.cache.ttl','scalar','["org","tenant"]','{"type":"string","pattern":"^P(T.*)$"}','"PT8H"','replace')
ON CONFLICT (key) DO NOTHING;

INSERT INTO settings_registry(key, type, scope, schema, default_value, merge_strategy) VALUES
 ('credit.cache.ttl','scalar','["org","tenant"]','{"type":"string","pattern":"^P(T.*)$"}','"PT8H"','replace')
ON CONFLICT (key) DO NOTHING;

INSERT INTO settings_registry(key, type, scope, schema, default_value, merge_strategy) VALUES
 ('credit.peak.slo.p95.ms','scalar','["global"]','{"type":"integer","minimum":100,"maximum":2000}','500','replace')
ON CONFLICT (key) DO NOTHING;

INSERT INTO settings_registry(key, type, scope, schema, default_value, merge_strategy) VALUES
 ('credit.batch.window.ms','scalar','["org","tenant"]','{"type":"integer","minimum":0,"maximum":2000}','50','replace')
ON CONFLICT (key) DO NOTHING;

INSERT INTO settings_registry(key, type, scope, schema, default_value, merge_strategy) VALUES
 ('help.space.enduser','object','["org","tenant"]','{"type":"object"}','{}','merge')
ON CONFLICT (key) DO NOTHING;

INSERT INTO settings_registry(key, type, scope, schema, default_value, merge_strategy) VALUES
 ('help.space.salesops','object','["org","tenant"]','{"type":"object"}','{}','merge')
ON CONFLICT (key) DO NOTHING;
