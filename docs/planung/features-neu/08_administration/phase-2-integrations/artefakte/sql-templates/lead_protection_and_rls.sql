-- Migration: $(./scripts/get-next-migration.sh)__lead_protection_and_rls.sql
-- Foundation: RLS fail-closed, named indices, idempotent DDL

DO $$ BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname='lead_protection_status') THEN
    CREATE TYPE lead_protection_status AS ENUM ('ACTIVE','GRACE','EXPIRED');
  END IF;
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname='lead_protection_reminder_type') THEN
    CREATE TYPE lead_protection_reminder_type AS ENUM ('REMINDER_60D','GRACE_EXPIRY');
  END IF;
END $$;

CREATE TABLE IF NOT EXISTS lead_protection_hold (
  id            uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id     uuid NOT NULL,
  lead_id       uuid NOT NULL REFERENCES leads(id) ON DELETE CASCADE,
  reason        text NOT NULL CHECK (reason IN ('FFZ_PRICE_APPROVAL','FFZ_SAMPLE_DELAY','FFZ_SUPPLY_ISSUE','CUSTOMER_BLOCKED','OTHER')),
  start_at      timestamptz NOT NULL,
  end_at        timestamptz,
  created_by    uuid NOT NULL,
  created_at    timestamptz NOT NULL DEFAULT now()
);
COMMENT ON TABLE lead_protection_hold IS 'Stop-the-clock Intervalle für Lead-Schutz (vertraglich definierte Verzögerungen).';
CREATE INDEX IF NOT EXISTS ix_lph_lead_time ON lead_protection_hold(lead_id, start_at);

CREATE TABLE IF NOT EXISTS lead_protection_reminder (
  id         uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id  uuid NOT NULL,
  lead_id    uuid NOT NULL REFERENCES leads(id) ON DELETE CASCADE,
  type       lead_protection_reminder_type NOT NULL,
  sent_at    timestamptz NOT NULL DEFAULT now(),
  channel    text,
  UNIQUE (lead_id, type)
);
COMMENT ON TABLE lead_protection_reminder IS 'Gesendete Reminder für die 60T/10T Pipeline (idempotent).';

CREATE TABLE IF NOT EXISTS lead_collaborator (
  lead_id     uuid NOT NULL REFERENCES leads(id) ON DELETE CASCADE,
  user_id     uuid NOT NULL,
  role        text NOT NULL CHECK (role IN ('VIEW','ASSIST','NEGOTIATE')),
  added_by    uuid NOT NULL,
  added_at    timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (lead_id, user_id)
);
COMMENT ON TABLE lead_collaborator IS 'Co-Working ohne Eigentümerwechsel; Rollen: VIEW/ASSIST/NEGOTIATE.';

CREATE OR REPLACE FUNCTION lead_hold_duration_since(p_lead uuid, p_anchor timestamptz)
RETURNS interval
LANGUAGE sql STABLE AS $$
  SELECT COALESCE(
    SUM( GREATEST( LEAST(COALESCE(h.end_at, now()), now()) - GREATEST(h.start_at, p_anchor), interval '0') ),
    interval '0'
  )
  FROM lead_protection_hold h
  WHERE h.lead_id = p_lead
    AND COALESCE(h.end_at, now()) > p_anchor
$$;

CREATE OR REPLACE VIEW v_lead_protection AS
WITH params AS (
  SELECT
    COALESCE((SELECT (schema->>'months')::int FROM settings_effective WHERE key='lead.protection.baseMonths'  LIMIT 1), 6)  AS base_months,
    COALESCE((SELECT (schema->>'days')::int   FROM settings_effective WHERE key='lead.protection.progressDays' LIMIT 1), 60) AS progress_days,
    COALESCE((SELECT (schema->>'days')::int   FROM settings_effective WHERE key='lead.protection.graceDays'    LIMIT 1), 10) AS grace_days,
    COALESCE((SELECT schema->>'mode'          FROM settings_effective WHERE key='lead.protection.mode'         LIMIT 1), 'GREATEST') AS mode
),
last_act AS (
  SELECT
    a.lead_id,
    MAX(a.at) FILTER (WHERE a.kind = ANY (ARRAY['QUALIFIED_CALL','CUSTOMER_REACTION','SCHEDULED_FOLLOWUP','SAMPLE_FEEDBACK','ROI_PRESENTATION'])) AS last_qual
  FROM activity a
  GROUP BY a.lead_id
),
curr_owner AS (
  SELECT la.lead_id, la.owner_user_id, la.assigned_at, l.tenant_id, l.org_id
  FROM lead_assignment la
  JOIN leads l ON l.id = la.lead_id
  WHERE la.is_current = true
)
SELECT
  c.tenant_id,
  c.org_id,
  c.lead_id,
  c.owner_user_id,
  c.assigned_at,
  COALESCE(l.last_qual, c.assigned_at) AS last_qual_activity_at,
  (c.assigned_at + make_interval(months => (SELECT base_months FROM params)) + lead_hold_duration_since(c.lead_id, c.assigned_at)) AS base_until,
  (COALESCE(l.last_qual, c.assigned_at) + make_interval(days => (SELECT progress_days FROM params)) + lead_hold_duration_since(c.lead_id, COALESCE(l.last_qual, c.assigned_at))) AS progress_until,
  (COALESCE(l.last_qual, c.assigned_at) + make_interval(days => (SELECT progress_days FROM params)) + lead_hold_duration_since(c.lead_id, COALESCE(l.last_qual, c.assigned_at))) AS reminder_due_at,
  (COALESCE(l.last_qual, c.assigned_at) + make_interval(days => (SELECT progress_days FROM params) + (SELECT grace_days FROM params)) + lead_hold_duration_since(c.lead_id, COALESCE(l.last_qual, c.assigned_at))) AS grace_until,
  CASE
    WHEN (SELECT mode FROM params) = 'LEAST'
      THEN LEAST(
             (c.assigned_at + make_interval(months => (SELECT base_months FROM params)) + lead_hold_duration_since(c.lead_id, c.assigned_at)),
             (COALESCE(l.last_qual, c.assigned_at) + make_interval(days => (SELECT progress_days FROM params)) + lead_hold_duration_since(c.lead_id, COALESCE(l.last_qual, c.assigned_at)))
           )
    ELSE GREATEST(
             (c.assigned_at + make_interval(months => (SELECT base_months FROM params)) + lead_hold_duration_since(c.lead_id, c.assigned_at)),
             (COALESCE(l.last_qual, c.assigned_at) + make_interval(days => (SELECT progress_days FROM params)) + lead_hold_duration_since(c.lead_id, COALESCE(l.last_qual, c.assigned_at)))
           )
  END AS valid_until,
  CASE
    WHEN now() <= (COALESCE(l.last_qual, c.assigned_at) + make_interval(days => (SELECT progress_days FROM params)) + lead_hold_duration_since(c.lead_id, COALESCE(l.last_qual, c.assigned_at)))
      THEN 'ACTIVE'::lead_protection_status
    WHEN now() <= (COALESCE(l.last_qual, c.assigned_at) + make_interval(days => (SELECT progress_days FROM params) + (SELECT grace_days FROM params)) + lead_hold_duration_since(c.lead_id, COALESCE(l.last_qual, c.assigned_at)))
      THEN 'GRACE'::lead_protection_status
    ELSE 'EXPIRED'::lead_protection_status
  END AS status
FROM curr_owner c
LEFT JOIN last_act l ON l.lead_id = c.lead_id;

COMMENT ON VIEW v_lead_protection IS 'Berechnete Schutzfenster inkl. Stop-the-Clock und Status (ACTIVE/GRACE/EXPIRED).';

ALTER TABLE leads ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS rls_leads_read ON leads;
CREATE POLICY rls_leads_read ON leads
  USING (
    EXISTS (
      SELECT 1 FROM v_lead_protection p
      WHERE p.lead_id = leads.id
        AND p.owner_user_id::text = current_setting('app.user_id', true)
        AND p.status IN ('ACTIVE','GRACE')
    )
    OR EXISTS (
      SELECT 1 FROM app_user u
      WHERE u.id::text = current_setting('app.user_id', true)
        AND ('auditor' = ANY(u.roles) OR 'manager' = ANY(u.roles))
    )
    OR EXISTS (
      SELECT 1 FROM lead_collaborator c
      WHERE c.lead_id = leads.id
        AND c.user_id::text = current_setting('app.user_id', true)
    )
  );

DROP POLICY IF EXISTS rls_leads_write ON leads;
CREATE POLICY rls_leads_write ON leads
  USING (
    EXISTS (
      SELECT 1 FROM v_lead_protection p
      WHERE p.lead_id = leads.id
        AND p.owner_user_id::text = current_setting('app.user_id', true)
        AND p.status IN ('ACTIVE','GRACE')
    )
  )
  WITH CHECK (
    EXISTS (
      SELECT 1 FROM v_lead_protection p
      WHERE p.lead_id = leads.id
        AND p.owner_user_id::text = current_setting('app.user_id', true)
        AND p.status IN ('ACTIVE','GRACE')
    )
  );

ALTER TABLE activity ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS rls_activity_rw ON activity;
CREATE POLICY rls_activity_rw ON activity
  USING (EXISTS (SELECT 1 FROM leads l WHERE l.id = activity.lead_id))
  WITH CHECK (EXISTS (SELECT 1 FROM leads l WHERE l.id = activity.lead_id));

-- Index-Empfehlungen für Scale
CREATE INDEX IF NOT EXISTS ix_activity_lead_kind_at ON activity(lead_id, at) WHERE kind IN ('QUALIFIED_CALL','CUSTOMER_REACTION','SCHEDULED_FOLLOWUP','SAMPLE_FEEDBACK','ROI_PRESENTATION');
CREATE INDEX IF NOT EXISTS ix_lead_assignment_current ON lead_assignment(lead_id) WHERE is_current = true;
CREATE INDEX IF NOT EXISTS ix_leads_org ON leads(org_id);
