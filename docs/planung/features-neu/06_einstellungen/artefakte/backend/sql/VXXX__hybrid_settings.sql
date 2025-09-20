-- VXXX__hybrid_settings.sql
-- WICHTIG: Migrationsnummer wird zur Implementierungszeit über Scripts ermittelt
-- Nutze: ./scripts/get-next-migration.sh für korrekte Nummer
CREATE EXTENSION IF NOT EXISTS pgcrypto;

DO $$ BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'contact_role_enum') THEN
    CREATE TYPE contact_role_enum AS ENUM ('CHEF','BUYER');
  END IF;
END $$;

CREATE TABLE IF NOT EXISTS settings_registry (
  key text PRIMARY KEY,
  default_value jsonb NOT NULL,
  allowed_scopes text[] NOT NULL DEFAULT '{}',
  json_schema jsonb NOT NULL,
  merge_strategy text NOT NULL DEFAULT 'scalar',
  version text NOT NULL DEFAULT '1.0',
  enabled boolean NOT NULL DEFAULT true,
  description text,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now()
);

-- RLS für settings_registry
ALTER TABLE settings_registry ENABLE ROW LEVEL SECURITY;
CREATE POLICY settings_registry_read ON settings_registry FOR SELECT USING (true);

-- Grant für lesen
GRANT SELECT ON settings_registry TO authenticated_user;

CREATE TABLE IF NOT EXISTS settings_store (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  key text NOT NULL,
  value jsonb NOT NULL,

  -- Scope Information
  tenant_id uuid NOT NULL,
  territory text,
  account_id uuid,
  contact_role contact_role_enum,
  contact_id uuid,
  user_id uuid,

  -- Performance und Consistency
  scope_sig text GENERATED ALWAYS AS (
    md5(
      coalesce(tenant_id::text,'') || '|' ||
      coalesce(territory::text,'') || '|' ||
      coalesce(account_id::text,'') || '|' ||
      coalesce(contact_role::text,'') || '|' ||
      coalesce(contact_id::text,'') || '|' ||
      coalesce(user_id::text,'')
    )
  ) STORED,

  -- Audit
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now(),
  created_by uuid NOT NULL,
  updated_by uuid NOT NULL,

  -- Constraints
  CONSTRAINT settings_store_key_scope_unique UNIQUE (key, scope_sig),
  CONSTRAINT settings_store_at_least_one_scope CHECK (
    tenant_id IS NOT NULL
  )
);

-- Performance-Indizes
CREATE INDEX IF NOT EXISTS idx_settings_store_tenant ON settings_store (tenant_id);
CREATE INDEX IF NOT EXISTS idx_settings_store_key ON settings_store (key);
CREATE INDEX IF NOT EXISTS idx_settings_store_scope_sig ON settings_store (scope_sig);
CREATE INDEX IF NOT EXISTS idx_settings_store_user ON settings_store (user_id) WHERE user_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_settings_store_contact ON settings_store (contact_id) WHERE contact_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_settings_store_account ON settings_store (account_id) WHERE account_id IS NOT NULL;

-- GIN Index für JSONB Value-Queries
CREATE INDEX IF NOT EXISTS idx_settings_store_value_gin ON settings_store USING gin (value);

-- RLS Policies für settings_store
ALTER TABLE settings_store ENABLE ROW LEVEL SECURITY;

-- Policy: Mandanten-Isolation
CREATE POLICY settings_store_tenant_isolation ON settings_store
FOR ALL TO authenticated_user
USING (tenant_id = current_tenant_id());

-- Policy: Territory-basierte Zugriffskontrolle
CREATE POLICY settings_store_territory_access ON settings_store
FOR ALL TO authenticated_user
USING (
  -- Eigenes Territory oder Admin
  territory IS NULL OR
  territory = current_user_territory() OR
  has_role('admin')
);

-- Policy: User-spezifische Settings
CREATE POLICY settings_store_user_access ON settings_store
FOR ALL TO authenticated_user
USING (
  user_id IS NULL OR
  user_id = current_user_id() OR
  has_role('admin')
);

-- Grants
GRANT SELECT, INSERT, UPDATE, DELETE ON settings_store TO authenticated_user;
GRANT USAGE ON SEQUENCE settings_store_id_seq TO authenticated_user;

-- Computed/Effective Settings View für Performance
CREATE TABLE IF NOT EXISTS settings_effective (
  scope_sig text NOT NULL,
  computed_settings jsonb NOT NULL,
  etag text NOT NULL,
  computed_at timestamptz NOT NULL DEFAULT now(),
  expires_at timestamptz,

  PRIMARY KEY (scope_sig)
);

-- Performance Index für Effective Settings
CREATE INDEX IF NOT EXISTS idx_settings_effective_etag ON settings_effective (etag);
CREATE INDEX IF NOT EXISTS idx_settings_effective_expires ON settings_effective (expires_at);

-- RLS für settings_effective
ALTER TABLE settings_effective ENABLE ROW LEVEL SECURITY;
CREATE POLICY settings_effective_access ON settings_effective
FOR ALL TO authenticated_user
USING (true); -- Computed data, access controlled by scope computation

-- Grants
GRANT SELECT, INSERT, UPDATE, DELETE ON settings_effective TO authenticated_user;

-- Audit-Tabelle für DSGVO-Compliance
CREATE TABLE IF NOT EXISTS settings_audit (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  key text NOT NULL,
  operation text NOT NULL, -- INSERT, UPDATE, DELETE
  old_value jsonb,
  new_value jsonb,

  -- Scope (denormalized für Audit-Queries)
  tenant_id uuid NOT NULL,
  territory text,
  account_id uuid,
  contact_role contact_role_enum,
  contact_id uuid,
  user_id uuid,

  -- Audit-Metadaten
  changed_at timestamptz NOT NULL DEFAULT now(),
  changed_by uuid NOT NULL,
  session_id text,
  ip_address inet,
  user_agent text
);

-- Audit-Indizes
CREATE INDEX IF NOT EXISTS idx_settings_audit_tenant ON settings_audit (tenant_id);
CREATE INDEX IF NOT EXISTS idx_settings_audit_key ON settings_audit (key);
CREATE INDEX IF NOT EXISTS idx_settings_audit_changed_at ON settings_audit (changed_at);
CREATE INDEX IF NOT EXISTS idx_settings_audit_changed_by ON settings_audit (changed_by);

-- RLS für Audit
ALTER TABLE settings_audit ENABLE ROW LEVEL SECURITY;
CREATE POLICY settings_audit_tenant_isolation ON settings_audit
FOR SELECT TO authenticated_user
USING (tenant_id = current_tenant_id());

-- Grants
GRANT SELECT ON settings_audit TO authenticated_user;
GRANT INSERT ON settings_audit TO settings_service; -- Nur Service darf Audit schreiben

-- Trigger für Updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = now();
  RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_settings_registry_updated_at
  BEFORE UPDATE ON settings_registry
  FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_settings_store_updated_at
  BEFORE UPDATE ON settings_store
  FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Hilfsfunktionen (müssen entsprechend der bestehenden Security-Implementation angepasst werden)
-- Diese sind Platzhalter und müssen durch echte Implementation ersetzt werden

CREATE OR REPLACE FUNCTION current_tenant_id()
RETURNS uuid AS $$
BEGIN
  -- TODO: Integration mit bestehendem Security-System
  RETURN coalesce(
    nullif(current_setting('app.current_tenant_id', true), ''),
    '00000000-0000-0000-0000-000000000000'
  )::uuid;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

CREATE OR REPLACE FUNCTION current_user_territory()
RETURNS text AS $$
BEGIN
  -- TODO: Integration mit bestehendem User-System
  RETURN coalesce(
    nullif(current_setting('app.current_user_territory', true), ''),
    'DE'
  );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

CREATE OR REPLACE FUNCTION current_user_id()
RETURNS uuid AS $$
BEGIN
  -- TODO: Integration mit bestehendem User-System
  RETURN coalesce(
    nullif(current_setting('app.current_user_id', true), ''),
    '00000000-0000-0000-0000-000000000000'
  )::uuid;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

CREATE OR REPLACE FUNCTION has_role(role_name text)
RETURNS boolean AS $$
BEGIN
  -- TODO: Integration mit bestehendem RBAC-System
  RETURN current_setting('app.user_roles', true) LIKE '%' || role_name || '%';
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Kommentare für Dokumentation
COMMENT ON TABLE settings_registry IS 'Schema-Registry für Settings mit JSON Schema Validation';
COMMENT ON TABLE settings_store IS 'Haupt-Tabelle für alle Settings mit Scope-basierter Hierarchie';
COMMENT ON TABLE settings_effective IS 'Precomputed merged Settings für Performance';
COMMENT ON TABLE settings_audit IS 'Audit-Log für DSGVO-Compliance';

COMMENT ON COLUMN settings_store.scope_sig IS 'Generated MD5 über alle Scope-Parameter für Performance';
COMMENT ON COLUMN settings_effective.etag IS 'SHA-256 ETag für HTTP-Caching';