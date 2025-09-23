-- V227: Security Context Core (ABAC/RLS Foundation - minimal)
-- Keine Abhängigkeit zu Business-Tabellen
-- Part of Sprint 1.2: Security + Foundation (FP-228)

BEGIN;

-- 0) Extensions
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- 1) ENUM für Settings Scope (idempotent)
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'settings_scope') THEN
    CREATE TYPE settings_scope AS ENUM (
      'GLOBAL','TENANT','TERRITORY','ACCOUNT','CONTACT_ROLE','CONTACT','USER'
    );
  END IF;
END $$;

-- 2) Security Settings Registry (generisch, für Module 06)
CREATE TABLE IF NOT EXISTS security_settings (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  scope settings_scope NOT NULL,
  scope_id TEXT,                        -- z.B. tenant-id, territory code, user-id
  key TEXT NOT NULL,
  value JSONB NOT NULL,
  created_at TIMESTAMPTZ DEFAULT NOW(),
  updated_at TIMESTAMPTZ DEFAULT NOW(),
  created_by TEXT,
  updated_by TEXT
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_security_settings
  ON security_settings(scope, scope_id, key);

CREATE INDEX IF NOT EXISTS idx_security_settings_scope
  ON security_settings(scope, scope_id);

-- 3) Session-Kontext Helper Functions (GUC helpers)
-- Achtung: SET LOCAL gilt transaktional; Aufrufer setzt pro Request.

-- Funktion zum Setzen des App-Kontexts
CREATE OR REPLACE FUNCTION set_app_context(
  p_user_id UUID,
  p_org_id TEXT,
  p_territory TEXT,
  p_roles TEXT[]
) RETURNS VOID AS $$
BEGIN
  PERFORM set_config('app.user_id', COALESCE(p_user_id::TEXT, ''), TRUE);
  PERFORM set_config('app.org_id', COALESCE(p_org_id, ''), TRUE);
  PERFORM set_config('app.territory', COALESCE(p_territory, ''), TRUE);
  PERFORM set_config('app.roles', COALESCE(array_to_string(p_roles, ','), ''), TRUE);
END;
$$ LANGUAGE plpgsql;

-- Funktion zum Abrufen des aktuellen App-Kontexts
CREATE OR REPLACE FUNCTION current_app_context() RETURNS JSONB AS $$
DECLARE
  v_user_id TEXT := current_setting('app.user_id', true);
  v_org_id TEXT := current_setting('app.org_id', true);
  v_territory TEXT := current_setting('app.territory', true);
  v_roles TEXT := current_setting('app.roles', true);
BEGIN
  RETURN jsonb_build_object(
    'user_id', v_user_id,
    'org_id', v_org_id,
    'territory', v_territory,
    'roles', v_roles
  );
END;
$$ LANGUAGE plpgsql;

-- Helper: Check if user has specific role
CREATE OR REPLACE FUNCTION has_role(p_role TEXT) RETURNS BOOLEAN AS $$
DECLARE
  v_roles TEXT := current_setting('app.roles', true);
BEGIN
  RETURN COALESCE(position(p_role in COALESCE(v_roles,'')) > 0, FALSE);
END;
$$ LANGUAGE plpgsql;

-- Helper: Get current user ID
CREATE OR REPLACE FUNCTION current_app_user() RETURNS UUID AS $$
DECLARE
  v_user_id TEXT := current_setting('app.user_id', true);
BEGIN
  IF v_user_id IS NULL OR v_user_id = '' THEN
    RETURN NULL;
  END IF;
  RETURN v_user_id::UUID;
END;
$$ LANGUAGE plpgsql STABLE;

-- Helper: Get current territory
CREATE OR REPLACE FUNCTION current_app_territory() RETURNS TEXT AS $$
BEGIN
  RETURN NULLIF(current_setting('app.territory', true), '');
END;
$$ LANGUAGE plpgsql STABLE;

-- Helper: Get current organization
CREATE OR REPLACE FUNCTION current_app_org() RETURNS TEXT AS $$
BEGIN
  RETURN NULLIF(current_setting('app.org_id', true), '');
END;
$$ LANGUAGE plpgsql STABLE;

-- 4) Security Audit Log (für zukünftige Verwendung)
CREATE TABLE IF NOT EXISTS security_audit_log (
  id BIGSERIAL PRIMARY KEY,
  user_id TEXT,
  org_id TEXT,
  territory TEXT,
  resource_type TEXT,
  resource_id TEXT,
  action TEXT,
  outcome TEXT CHECK (outcome IN ('ALLOW', 'DENY', 'PARTIAL')),
  reason TEXT,
  metadata JSONB DEFAULT '{}',
  created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Indexes für Performance
CREATE INDEX IF NOT EXISTS idx_security_audit_user
  ON security_audit_log(user_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_security_audit_resource
  ON security_audit_log(resource_type, resource_id, created_at DESC);

-- 5) Grant permissions (falls Role existiert)
DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'authenticated') THEN
    GRANT USAGE ON SCHEMA public TO authenticated;
    GRANT SELECT, INSERT, UPDATE, DELETE ON security_settings TO authenticated;
    GRANT SELECT, INSERT ON security_audit_log TO authenticated;
    GRANT USAGE ON ALL SEQUENCES IN SCHEMA public TO authenticated;
    GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA public TO authenticated;
  END IF;
END $$;

COMMIT;

-- ============================================================================
-- HINWEIS: RLS-Policies werden NICHT hier definiert!
-- ============================================================================
-- RLS-Policies für Business-Tabellen werden in den jeweiligen Modul-Migrationen
-- definiert, wenn die Tabellen erstellt werden (Sprint 2.x).
--
-- Diese Migration liefert nur die Foundation:
-- - Session-Kontext (GUCs)
-- - Helper Functions
-- - Settings Registry
-- - Audit Log
-- ============================================================================