-- VXXX__user_lead_protection_foundation.sql
-- Template! Nummer vor Production via ./scripts/get-next-migration.sh setzen.
-- FreshFoodz – User‑Lead‑Protection (Zero‑Downtime, Expand→Migrate→Contract)
-- Assumptions:
--  - Tabelle "leads(id uuid PRIMARY KEY, territory text NOT NULL, ...)" existiert
--  - Session Settings: app.user_id (uuid), app.territory (text), app.org_id (optional)
--  - Territory Werte: 'DE','CH' (ggf. erweitern)
--  - Extension pgcrypto für gen_random_uuid() (nur für Audit Korrelation)

BEGIN;

-- Safety: kurze Timeouts verhindern harte Locks
SET LOCAL lock_timeout = '250ms';
SET LOCAL statement_timeout = '15s';

-- 0) Extensions (idempotent)
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- 1) ENUMS (oder CHECKs, falls ENUMs unerwünscht)
DO $$ BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'lead_action') THEN
    CREATE TYPE lead_action AS ENUM ('VIEW','EDIT','ASSIGN','UNASSIGN');
  END IF;
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'lead_access_outcome') THEN
    CREATE TYPE lead_access_outcome AS ENUM ('ALLOW','DENY');
  END IF;
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'protection_type') THEN
    CREATE TYPE protection_type AS ENUM ('SOLE_OWNER','COLLABORATOR','VIEW_ONLY');
  END IF;
END $$;

-- 2) Core Tables (Expand phase)
CREATE TABLE IF NOT EXISTS user_lead_assignments (
  lead_id      uuid NOT NULL,
  user_id      uuid NOT NULL,
  territory    text NOT NULL,
  assigned_at  timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (lead_id, territory),               -- genau ein Owner je Territory
  CONSTRAINT fk_ula_lead FOREIGN KEY (lead_id) REFERENCES leads(id) ON DELETE CASCADE,
  CONSTRAINT chk_ula_territory CHECK (territory IN ('DE','CH'))
);

CREATE TABLE IF NOT EXISTS lead_protection_rules (
  user_id     uuid NOT NULL,
  territory   text NOT NULL,
  protection  protection_type NOT NULL DEFAULT 'SOLE_OWNER',
  active      boolean NOT NULL DEFAULT true,
  effective_from timestamptz NOT NULL DEFAULT now(),
  effective_to   timestamptz,
  PRIMARY KEY (user_id, territory, protection),
  CONSTRAINT chk_lpr_territory CHECK (territory IN ('DE','CH'))
);

CREATE TABLE IF NOT EXISTS lead_access_audit (
  id            bigserial PRIMARY KEY,
  lead_id       uuid NOT NULL,
  user_id       uuid NOT NULL,
  territory     text NOT NULL,
  action        lead_action NOT NULL,
  outcome       lead_access_outcome NOT NULL,
  reason        text,
  correlation_id uuid NOT NULL DEFAULT gen_random_uuid(),
  at            timestamptz NOT NULL DEFAULT now(),
  CONSTRAINT fk_laa_lead FOREIGN KEY (lead_id) REFERENCES leads(id) ON DELETE CASCADE,
  CONSTRAINT chk_laa_territory CHECK (territory IN ('DE','CH'))
);

COMMENT ON TABLE user_lead_assignments IS 'Owner‑Zuweisung je Lead/Territory (ein Bearbeiter je Territory)';
COMMENT ON TABLE lead_protection_rules IS 'Regeln je User/Territory – derzeit nur SOLE_OWNER aktiv';
COMMENT ON TABLE lead_access_audit IS 'Audit aller Zugriffsentscheidungen (ALLOW/DENY) inkl. Grund';

-- 3) Index‑Set (Performance)
CREATE INDEX IF NOT EXISTS ix_ula_user ON user_lead_assignments(user_id, territory);
CREATE INDEX IF NOT EXISTS ix_ula_lead ON user_lead_assignments(lead_id, territory);

CREATE INDEX IF NOT EXISTS ix_laa_lead_at ON lead_access_audit(lead_id, at DESC);
CREATE INDEX IF NOT EXISTS ix_laa_outcome_at ON lead_access_audit(outcome, at DESC);

-- 4) RLS (fail‑closed) für neue Tabellen
ALTER TABLE user_lead_assignments ENABLE ROW LEVEL SECURITY;
ALTER TABLE lead_protection_rules ENABLE ROW LEVEL SECURITY;
ALTER TABLE lead_access_audit ENABLE ROW LEVEL SECURITY;

-- Deny by default
REVOKE ALL ON user_lead_assignments FROM PUBLIC;
REVOKE ALL ON lead_protection_rules FROM PUBLIC;
REVOKE ALL ON lead_access_audit FROM PUBLIC;

-- Policies – nur innerhalb gesetztem Territory; Admins über App‑Role/Session separat
CREATE POLICY rls_ula_rw ON user_lead_assignments
  USING (territory = current_setting('app.territory', true))
  WITH CHECK (territory = current_setting('app.territory', true));

CREATE POLICY rls_lpr_rw ON lead_protection_rules
  USING (territory = current_setting('app.territory', true))
  WITH CHECK (territory = current_setting('app.territory', true));

CREATE POLICY rls_laa_ro ON lead_access_audit
  USING (territory = current_setting('app.territory', true));

-- 5) Permission Check (fast path) + Audit (invoker‑security)
-- Hinweis: SECURITY INVOKER (Default) → kein RLS‑Bypass.
CREATE OR REPLACE FUNCTION can_edit_lead(p_lead uuid)
RETURNS boolean LANGUAGE sql STABLE AS $$
  SELECT EXISTS (
    SELECT 1
    FROM user_lead_assignments ula
    JOIN leads l ON l.id = ula.lead_id
    WHERE ula.lead_id = p_lead
      AND ula.user_id::text = current_setting('app.user_id', true)
      AND ula.territory = current_setting('app.territory', true)
      AND l.territory = ula.territory
  );
$$;

-- Kombinierte Assertion + Audit (eine Roundtrip)
CREATE OR REPLACE FUNCTION assert_edit_and_audit(p_lead uuid, p_action lead_action, p_reason text DEFAULT NULL)
RETURNS void LANGUAGE plpgsql AS $$
DECLARE v_can boolean; v_territory text;
BEGIN
  SELECT territory INTO v_territory FROM leads WHERE id = p_lead;
  v_can := can_edit_lead(p_lead);
  INSERT INTO lead_access_audit(lead_id, user_id, territory, action, outcome, reason)
  VALUES (p_lead, current_setting('app.user_id')::uuid, coalesce(v_territory,current_setting('app.territory', true)),
          coalesce(p_action,'EDIT'), CASE WHEN v_can THEN 'ALLOW' ELSE 'DENY' END, p_reason);
  IF NOT v_can THEN
    RAISE EXCEPTION 'Lead edit denied for user %', current_setting('app.user_id', true)
      USING ERRCODE = '28000'; -- invalid authorization specification
  END IF;
END $$;

-- 6) View für schnelle UI‑Checks
CREATE OR REPLACE VIEW v_lead_protection AS
SELECT
  l.id AS lead_id,
  l.territory,
  ula.user_id AS owner_user_id,
  (ula.user_id::text = current_setting('app.user_id', true)
   AND l.territory = current_setting('app.territory', true)) AS can_edit
FROM leads l
LEFT JOIN user_lead_assignments ula
  ON ula.lead_id = l.id AND ula.territory = l.territory;

COMMENT ON VIEW v_lead_protection IS 'Effektiver Owner und Edit‑Recht für current app.user_id/app.territory';

-- 7) Contract‑Schritt erfolgt später (Cleanup alter Schutzlogik)
-- (Platzhalter: DROP alter Tabellen/Spalten nach Beobachtungsfenster)

COMMIT;
