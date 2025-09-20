-- VXXX__help_core.sql
-- WICHTIG: Migrationsnummer wird zur Implementierungszeit über Scripts ermittelt
-- Nutze: ./scripts/get-next-migration.sh für korrekte Nummer
DO $$ BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'persona_enum') THEN
    CREATE TYPE persona_enum AS ENUM ('CHEF','BUYER','GF','REP');
  END IF;
END $$;

CREATE TABLE IF NOT EXISTS help_article (
  id            uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id     uuid,
  territory     text,
  persona       persona_enum,
  module        text NOT NULL CHECK (module ~ '^(01|02|03|04|05|06|07)$'),
  kind          text NOT NULL CHECK (kind IN ('FAQ','HowTo','Playbook','Video')),
  slug          text UNIQUE,
  title         text NOT NULL,
  summary       text,
  body_md       text NOT NULL,
  locale        text NOT NULL DEFAULT 'de-DE',
  keywords      text[] DEFAULT '{}',
  cta           jsonb,
  version       int NOT NULL DEFAULT 1,
  is_published  boolean NOT NULL DEFAULT false,
  published_at  timestamptz,
  updated_at    timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS ix_help_article_module ON help_article(module);
CREATE INDEX IF NOT EXISTS ix_help_article_persona ON help_article(persona);
CREATE INDEX IF NOT EXISTS ix_help_article_territory ON help_article(territory);
CREATE INDEX IF NOT EXISTS ix_help_article_keywords ON help_article USING GIN (keywords);

CREATE TABLE IF NOT EXISTS help_event (
  id            uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id       uuid,
  tenant_id     uuid,
  territory     text,
  module        text,
  context       text,
  topic         text,
  event_type    text NOT NULL CHECK (event_type IN ('NUDGE_SHOWN','NUDGE_ACCEPTED','NUDGE_DISMISSED','ARTICLE_VIEWED','GUIDED_FOLLOWUP_STARTED','GUIDED_FOLLOWUP_COMPLETED','ROI_MINICHECK_STARTED','ROI_MINICHECK_COMPLETED')),
  score         numeric,
  article_id    uuid REFERENCES help_article(id) ON DELETE SET NULL,
  details       jsonb,
  created_at    timestamptz NOT NULL DEFAULT now()
);

-- Optional RLS for multi-tenant/territory scoping (read)
CREATE OR REPLACE FUNCTION app_get_setting(key text) RETURNS text LANGUAGE plpgsql STABLE AS $$
DECLARE v text; BEGIN
  BEGIN v := current_setting(key, true); EXCEPTION WHEN OTHERS THEN v := NULL; END;
  RETURN v;
END $$;

ALTER TABLE help_article ENABLE ROW LEVEL SECURITY;
ALTER TABLE help_event   ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS rls_help_article_read ON help_article;
CREATE POLICY rls_help_article_read ON help_article
  USING (
    (tenant_id IS NULL OR tenant_id::text = app_get_setting('app.tenant_id')) AND
    (territory IS NULL OR territory = app_get_setting('app.territory')) AND
    is_published = true
  );

DROP POLICY IF EXISTS rls_help_event_ins ON help_event;
CREATE POLICY rls_help_event_ins ON help_event
  FOR INSERT WITH CHECK (
    (tenant_id IS NULL OR tenant_id::text = app_get_setting('app.tenant_id')) AND
    (territory IS NULL OR territory = app_get_setting('app.territory'))
  );