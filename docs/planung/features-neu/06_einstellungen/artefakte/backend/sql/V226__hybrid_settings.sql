-- V226__hybrid_settings.sql
CREATE EXTENSION IF NOT EXISTS pgcrypto;

DO $$ BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'contact_role_enum') THEN
    CREATE TYPE contact_role_enum AS ENUM ('CHEF','BUYER');
  END IF;
END $$;

CREATE TABLE IF NOT EXISTS settings_registry (
  key              text PRIMARY KEY,
  type             text NOT NULL CHECK (type IN ('scalar','object','list')),
  json_schema      jsonb NOT NULL,
  scope            jsonb NOT NULL,
  merge_strategy   text NOT NULL CHECK (merge_strategy IN ('scalar','object','list')),
  default_value    jsonb,
  description      text,
  version          int  NOT NULL DEFAULT 1,
  created_at       timestamptz NOT NULL DEFAULT now(),
  updated_at       timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS settings_store (
  id               uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id        uuid,
  territory        text,
  account_id       uuid,
  contact_role     contact_role_enum,
  contact_id       uuid,
  user_id          uuid,
  key              text NOT NULL REFERENCES settings_registry(key) ON DELETE CASCADE,
  value            jsonb NOT NULL,
  updated_by       uuid,
  updated_at       timestamptz NOT NULL DEFAULT now(),
  scope_sig        text GENERATED ALWAYS AS (
    md5(
      coalesce(tenant_id::text,'')||'|'||coalesce(territory,'')||'|'||coalesce(account_id::text,'')||'|'||coalesce(contact_role::text,'')||'|'||coalesce(contact_id::text,'')||'|'||coalesce(user_id::text,'')
    )
  ) STORED
);
CREATE UNIQUE INDEX IF NOT EXISTS ux_settings_store_key_scope ON settings_store(key, scope_sig);
CREATE INDEX IF NOT EXISTS ix_settings_store_territory ON settings_store(territory);
CREATE INDEX IF NOT EXISTS ix_settings_store_user ON settings_store(user_id);
CREATE INDEX IF NOT EXISTS ix_settings_store_account ON settings_store(account_id);

CREATE TABLE IF NOT EXISTS settings_effective (
  id               uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id        uuid,
  territory        text,
  account_id       uuid,
  contact_role     contact_role_enum,
  contact_id       uuid,
  user_id          uuid,
  blob             jsonb NOT NULL,
  etag             text NOT NULL,
  computed_at      timestamptz NOT NULL DEFAULT now()
);
CREATE UNIQUE INDEX IF NOT EXISTS ux_settings_effective_scope ON settings_effective(
  coalesce(tenant_id, '00000000-0000-0000-0000-000000000000'::uuid),
  coalesce(territory,''),
  coalesce(account_id, '00000000-0000-0000-0000-000000000000'::uuid),
  coalesce(contact_role::text,''),
  coalesce(contact_id, '00000000-0000-0000-0000-000000000000'::uuid),
  coalesce(user_id, '00000000-0000-0000-0000-000000000000'::uuid)
);
CREATE INDEX IF NOT EXISTS ix_settings_effective_user_territory ON settings_effective(user_id, territory);

ALTER TABLE settings_store     ENABLE ROW LEVEL SECURITY;
ALTER TABLE settings_effective ENABLE ROW LEVEL SECURITY;

CREATE OR REPLACE FUNCTION app_get_setting(key text) RETURNS text
LANGUAGE plpgsql STABLE AS $$
DECLARE v text; BEGIN
  BEGIN v := current_setting(key, true); EXCEPTION WHEN OTHERS THEN v := NULL; END;
  RETURN v;
END $$;

DROP POLICY IF EXISTS rls_settings_store_read ON settings_store;
CREATE POLICY rls_settings_store_read ON settings_store
  USING (
    (tenant_id IS NULL OR tenant_id::text = app_get_setting('app.tenant_id')) AND
    (territory IS NULL OR territory = app_get_setting('app.territory'))
  );

DROP POLICY IF EXISTS rls_settings_store_write ON settings_store;
CREATE POLICY rls_settings_store_write ON settings_store
  FOR INSERT WITH CHECK (
    (tenant_id IS NULL OR tenant_id::text = app_get_setting('app.tenant_id')) AND
    (territory IS NULL OR territory = app_get_setting('app.territory'))
  );

CREATE POLICY rls_settings_store_update ON settings_store
  FOR UPDATE USING (
    (tenant_id IS NULL OR tenant_id::text = app_get_setting('app.tenant_id')) AND
    (territory IS NULL OR territory = app_get_setting('app.territory'))
  )
  WITH CHECK (
    (tenant_id IS NULL OR tenant_id::text = app_get_setting('app.tenant_id')) AND
    (territory IS NULL OR territory = app_get_setting('app.territory'))
  );

DROP POLICY IF EXISTS rls_settings_effective_read ON settings_effective;
CREATE POLICY rls_settings_effective_read ON settings_effective
  USING (
    (tenant_id IS NULL OR tenant_id::text = app_get_setting('app.tenant_id')) AND
    (territory IS NULL OR territory = app_get_setting('app.territory')) AND
    (user_id::text = app_get_setting('app.user_id') OR app_get_setting('app.allow_admin_read') = 'true')
  );

-- Step 5 will add LISTEN/NOTIFY triggers.
