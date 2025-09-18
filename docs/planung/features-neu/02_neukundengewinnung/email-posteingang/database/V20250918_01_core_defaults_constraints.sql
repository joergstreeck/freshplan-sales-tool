-- V20250918_01_core_defaults_constraints.sql
BEGIN;

CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE EXTENSION IF NOT EXISTS btree_gist;

-- User-bezogene Lead-Defaults (Fallbacks)
CREATE TABLE IF NOT EXISTS user_lead_settings (
  id               uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id          uuid UNIQUE NOT NULL,
  protection_months integer NOT NULL DEFAULT 6 CHECK (protection_months BETWEEN 1 AND 24),
  activity_days     integer NOT NULL DEFAULT 60 CHECK (activity_days BETWEEN 7 AND 120),
  grace_days        integer NOT NULL DEFAULT 10 CHECK (grace_days BETWEEN 3 AND 30),
  first_year_commission_rate  numeric(5,4) NOT NULL DEFAULT 0.0700 CHECK (first_year_commission_rate BETWEEN 0 AND 1),
  subsequent_year_commission_rate numeric(5,4) NOT NULL DEFAULT 0.0200 CHECK (subsequent_year_commission_rate BETWEEN 0 AND 1),
  created_at       timestamptz NOT NULL DEFAULT now(),
  updated_at       timestamptz NOT NULL DEFAULT now()
);

-- Trigger: updated_at aktualisieren
CREATE OR REPLACE FUNCTION set_updated_at() RETURNS trigger AS $
BEGIN
  NEW.updated_at := now();
  RETURN NEW;
END; $ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_user_lead_settings_updated ON user_lead_settings;
CREATE TRIGGER trg_user_lead_settings_updated
BEFORE UPDATE ON user_lead_settings
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- Lead-Tabelle: Dubletten-Schutz & Performance
-- (vorausgesetzt: lead Tabelle existiert bereits)
ALTER TABLE lead
  ALTER COLUMN status SET NOT NULL,
  ALTER COLUMN company SET NOT NULL,
  ALTER COLUMN location SET NOT NULL;

-- Unique Company+Location (ggf. normalisiert in App-Layer)
DO $
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_indexes WHERE schemaname = 'public' AND indexname = 'ux_lead_company_location'
  ) THEN
    CREATE UNIQUE INDEX ux_lead_company_location ON lead (lower(company), lower(location));
  END IF;
END $;

-- Partial Index für häufige Abfragen
DO $
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_indexes WHERE schemaname = 'public' AND indexname = 'ix_lead_active_status'
  ) THEN
    CREATE INDEX ix_lead_active_status ON lead (status, channel_type, territory)
    WHERE status IN ('ACTIVE','REMINDER_SENT','GRACE_PERIOD');
  END IF;
END $;

-- Stop-Clock-Perioden: keine Überschneidungen pro Lead
-- (Exclusion Constraint über Zeiträume)
ALTER TABLE stop_clock_period
  ALTER COLUMN starts_at SET NOT NULL,
  ALTER COLUMN ends_at SET NOT NULL;

-- Range-Spalte on the fly
DO $
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint WHERE conname = 'excl_stop_clock_overlap'
  ) THEN
    ALTER TABLE stop_clock_period
      ADD COLUMN IF NOT EXISTS period tstzrange
        GENERATED ALWAYS AS (tstzrange(starts_at, ends_at, '[]')) STORED;
    ALTER TABLE stop_clock_period
      ADD CONSTRAINT excl_stop_clock_overlap
      EXCLUDE USING gist (lead_id WITH =, period WITH &&);
  END IF;
END $;

COMMIT;