-- rls_v2.sql – Complete Core-Objects-Set (Territory-RLS + User-Assignment + Multi-Contact Visibility)
-- Safe to run multiple times (IF NOT EXISTS patterns).

BEGIN;
SET LOCAL lock_timeout='250ms'; SET LOCAL statement_timeout='15s';

-- === Enums (idempotent) ===
DO $$ BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname='note_visibility') THEN
    CREATE TYPE note_visibility AS ENUM ('OWNER_ONLY','COLLABORATORS','ACCOUNT_TEAM','ORG_READ');
  END IF;
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname='note_category') THEN
    CREATE TYPE note_category AS ENUM ('GENERAL','COMMERCIAL','PRODUCT');
  END IF;
END $$;

-- === Helper functions (STABLE; index-friendly) ===
CREATE OR REPLACE FUNCTION app_user_id() RETURNS uuid
LANGUAGE sql STABLE AS $$ SELECT NULLIF(current_setting('app.user_id', true),'')::uuid $$;

CREATE OR REPLACE FUNCTION app_org_id() RETURNS text
LANGUAGE sql STABLE AS $$ SELECT NULLIF(current_setting('app.org_id', true),'') $$;

CREATE OR REPLACE FUNCTION app_territory() RETURNS text
LANGUAGE sql STABLE AS $$ SELECT NULLIF(current_setting('app.territory', true),'') $$;

CREATE OR REPLACE FUNCTION app_scopes() RETURNS text[]
LANGUAGE sql STABLE AS $$ SELECT CASE WHEN current_setting('app.scopes', true) IS NULL
  OR current_setting('app.scopes', true) = '' THEN ARRAY[]::text[]
ELSE string_to_array(current_setting('app.scopes', true), ',') END $$;

CREATE OR REPLACE FUNCTION app_contact_roles() RETURNS text[]
LANGUAGE sql STABLE AS $$ SELECT CASE WHEN current_setting('app.contact_roles', true) IS NULL
  OR current_setting('app.contact_roles', true) = '' THEN ARRAY[]::text[]
ELSE string_to_array(current_setting('app.contact_roles', true), ',') END $$;

CREATE OR REPLACE FUNCTION app_has_scope(s text) RETURNS boolean
LANGUAGE sql STABLE AS $$ SELECT s = ANY(app_scopes()) $$;

CREATE OR REPLACE FUNCTION app_has_contact_role(r text) RETURNS boolean
LANGUAGE sql STABLE AS $$ SELECT r = ANY(app_contact_roles()) $$;

-- Ownership helpers
CREATE OR REPLACE FUNCTION is_lead_owner(p_lead uuid) RETURNS boolean
LANGUAGE sql STABLE AS $$
  SELECT EXISTS (
    SELECT 1 FROM user_lead_assignments ula
    JOIN leads l ON l.id = ula.lead_id
    WHERE ula.lead_id = p_lead
      AND ula.user_id = app_user_id()
      AND ula.territory = app_territory()
      AND l.territory = ula.territory
      AND l.org_id = app_org_id()
  );
$$;

CREATE TABLE IF NOT EXISTS lead_collaborators(
  lead_id uuid NOT NULL,
  user_id uuid NOT NULL,
  PRIMARY KEY(lead_id,user_id),
  FOREIGN KEY (lead_id) REFERENCES leads(id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS ix_lead_collab_user ON lead_collaborators(user_id);

CREATE OR REPLACE FUNCTION is_lead_collaborator(p_lead uuid) RETURNS boolean
LANGUAGE sql STABLE AS $$
  SELECT EXISTS (
    SELECT 1 FROM lead_collaborators c WHERE c.lead_id = p_lead AND c.user_id = app_user_id()
  );
$$;

-- Audit function (re-usable)
CREATE OR REPLACE FUNCTION assert_edit_and_audit(p_lead uuid, p_action text DEFAULT 'EDIT', p_reason text DEFAULT NULL)
RETURNS void LANGUAGE plpgsql AS $$
DECLARE v_can boolean; v_territory text;
BEGIN
  SELECT territory INTO v_territory FROM leads WHERE id = p_lead;
  v_can := is_lead_owner(p_lead) OR app_has_scope('lead:override');
  INSERT INTO lead_access_audit(lead_id, user_id, territory, action, outcome, reason)
  VALUES (p_lead, app_user_id(), coalesce(v_territory, app_territory()),
          CASE WHEN p_action IS NULL THEN 'EDIT' ELSE p_action END::lead_action,
          CASE WHEN v_can THEN 'ALLOW' ELSE 'DENY' END::lead_access_outcome,
          p_reason);
  IF NOT v_can THEN
    RAISE EXCEPTION 'Lead edit denied for user %', app_user_id() USING ERRCODE='28000';
  END IF;
END $$;

-- === Base tables (create if not exists; otherwise policies attach) ===
-- Leads
CREATE TABLE IF NOT EXISTS leads(
  id uuid PRIMARY KEY,
  org_id text NOT NULL,
  territory text NOT NULL CHECK (territory IN ('DE','CH')),
  status text NOT NULL CHECK (status IN ('COLD','WARM','HOT','QUALIFIED','CONVERTED')),
  data jsonb DEFAULT '{}'::jsonb
);
CREATE INDEX IF NOT EXISTS ix_leads_territory ON leads(territory, org_id, status);

-- Activities (belong to a lead)
CREATE TABLE IF NOT EXISTS activities(
  id uuid PRIMARY KEY,
  lead_id uuid NOT NULL REFERENCES leads(id) ON DELETE CASCADE,
  kind text NOT NULL,
  created_at timestamptz NOT NULL DEFAULT now(),
  meta jsonb DEFAULT '{}'::jsonb
);
CREATE INDEX IF NOT EXISTS ix_act_lead ON activities(lead_id, created_at DESC);

-- Samples (belong to a lead)
CREATE TABLE IF NOT EXISTS samples(
  id uuid PRIMARY KEY,
  lead_id uuid NOT NULL REFERENCES leads(id) ON DELETE CASCADE,
  status text NOT NULL,
  created_at timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS ix_samples_lead ON samples(lead_id, created_at DESC);

-- Contacts (simplified, org/territory scoped)
CREATE TABLE IF NOT EXISTS contacts(
  id uuid PRIMARY KEY,
  org_id text NOT NULL,
  territory text NOT NULL CHECK (territory IN ('DE','CH')),
  role text NOT NULL CHECK (role IN ('GF','BUYER','CHEF')),
  data jsonb DEFAULT '{}'::jsonb
);
CREATE INDEX IF NOT EXISTS ix_contacts_scope ON contacts(territory, org_id, role);

-- Lead notes with visibility & category
CREATE TABLE IF NOT EXISTS lead_notes(
  id uuid PRIMARY KEY,
  lead_id uuid NOT NULL REFERENCES leads(id) ON DELETE CASCADE,
  author_user_id uuid NOT NULL,
  visibility note_visibility NOT NULL DEFAULT 'ACCOUNT_TEAM',
  category note_category NOT NULL DEFAULT 'GENERAL',
  created_at timestamptz NOT NULL DEFAULT now(),
  body text
);
CREATE INDEX IF NOT EXISTS ix_notes_lead ON lead_notes(lead_id, created_at DESC);
CREATE INDEX IF NOT EXISTS ix_notes_visibility ON lead_notes(visibility, category);

-- === Enable RLS (deny by default) ===
ALTER TABLE leads ENABLE ROW LEVEL SECURITY;
ALTER TABLE activities ENABLE ROW LEVEL SECURITY;
ALTER TABLE samples ENABLE ROW LEVEL SECURITY;
ALTER TABLE contacts ENABLE ROW LEVEL SECURITY;
ALTER TABLE lead_notes ENABLE ROW LEVEL SECURITY;

REVOKE ALL ON leads FROM PUBLIC;
REVOKE ALL ON activities FROM PUBLIC;
REVOKE ALL ON samples FROM PUBLIC;
REVOKE ALL ON contacts FROM PUBLIC;
REVOKE ALL ON lead_notes FROM PUBLIC;

-- === Policies v2 ===

-- Leads
DROP POLICY IF EXISTS rls_leads_read_v2 ON leads;
CREATE POLICY rls_leads_read_v2 ON leads FOR SELECT
USING (org_id = app_org_id() AND territory = app_territory());

DROP POLICY IF EXISTS rls_leads_ins_v2 ON leads;
CREATE POLICY rls_leads_ins_v2 ON leads FOR INSERT
WITH CHECK (org_id = app_org_id() AND territory = app_territory() AND app_has_scope('lead:create'));

DROP POLICY IF EXISTS rls_leads_upd_v2 ON leads;
CREATE POLICY rls_leads_upd_v2 ON leads FOR UPDATE
USING (org_id = app_org_id() AND territory = app_territory())
WITH CHECK (
  org_id = app_org_id() AND territory = app_territory()
  AND (is_lead_owner(id) OR app_has_scope('lead:override'))
);

DROP POLICY IF EXISTS rls_leads_del_v2 ON leads;
CREATE POLICY rls_leads_del_v2 ON leads FOR DELETE
USING (org_id = app_org_id() AND territory = app_territory() AND app_has_scope('lead:delete'));

-- Activities (inherit lead scope; collaborators can read/write own kinds if scope)
DROP POLICY IF EXISTS rls_act_sel_v2 ON activities;
CREATE POLICY rls_act_sel_v2 ON activities FOR SELECT
USING (EXISTS (SELECT 1 FROM leads l WHERE l.id = activities.lead_id AND l.org_id = app_org_id() AND l.territory = app_territory()));

DROP POLICY IF EXISTS rls_act_ins_v2 ON activities;
CREATE POLICY rls_act_ins_v2 ON activities FOR INSERT
WITH CHECK (
  EXISTS (SELECT 1 FROM leads l WHERE l.id = activities.lead_id AND l.org_id = app_org_id() AND l.territory = app_territory())
  AND (is_lead_owner(activities.lead_id) OR is_lead_collaborator(activities.lead_id) OR app_has_scope('activity:create'))
);

DROP POLICY IF EXISTS rls_act_upd_v2 ON activities;
CREATE POLICY rls_act_upd_v2 ON activities FOR UPDATE
USING (EXISTS (SELECT 1 FROM leads l WHERE l.id = activities.lead_id AND l.org_id = app_org_id() AND l.territory = app_territory()))
WITH CHECK (is_lead_owner(activities.lead_id) OR app_has_scope('activity:override'));

-- Samples (bind to lead scope; edit predominantly owner)
DROP POLICY IF EXISTS rls_smpl_sel_v2 ON samples;
CREATE POLICY rls_smpl_sel_v2 ON samples FOR SELECT
USING (EXISTS (SELECT 1 FROM leads l WHERE l.id = samples.lead_id AND l.org_id = app_org_id() AND l.territory = app_territory()));

DROP POLICY IF EXISTS rls_smpl_ins_v2 ON samples;
CREATE POLICY rls_smpl_ins_v2 ON samples FOR INSERT
WITH CHECK (
  EXISTS (SELECT 1 FROM leads l WHERE l.id = samples.lead_id AND l.org_id = app_org_id() AND l.territory = app_territory())
  AND (is_lead_owner(samples.lead_id) OR app_has_scope('sample:create'))
);

DROP POLICY IF EXISTS rls_smpl_upd_v2 ON samples;
CREATE POLICY rls_smpl_upd_v2 ON samples FOR UPDATE
USING (EXISTS (SELECT 1 FROM leads l WHERE l.id = samples.lead_id AND l.org_id = app_org_id() AND l.territory = app_territory()))
WITH CHECK (is_lead_owner(samples.lead_id) OR app_has_scope('sample:override'));

-- Contacts (org/territory scoped; visibility per role done at notes-level)
DROP POLICY IF EXISTS rls_contacts_sel_v2 ON contacts;
CREATE POLICY rls_contacts_sel_v2 ON contacts FOR SELECT
USING (org_id = app_org_id() AND territory = app_territory());

-- Lead Notes (visibility + roles)
DROP POLICY IF EXISTS rls_notes_sel_v2 ON lead_notes;
CREATE POLICY rls_notes_sel_v2 ON lead_notes FOR SELECT
USING (
  EXISTS (SELECT 1 FROM leads l WHERE l.id = lead_notes.lead_id AND l.org_id = app_org_id() AND l.territory = app_territory())
  AND (
    author_user_id = app_user_id()
    OR (visibility = 'OWNER_ONLY'      AND is_lead_owner(lead_id))
    OR (visibility = 'COLLABORATORS'   AND (is_lead_owner(lead_id) OR is_lead_collaborator(lead_id)))
    OR (visibility = 'ACCOUNT_TEAM')
    OR (visibility = 'ORG_READ')
  )
  AND (
    -- Category-based gating: COMMERCIAL -> BUYER/GF; PRODUCT -> CHEF/GF
    (category = 'COMMERCIAL' AND (app_has_contact_role('BUYER') OR app_has_contact_role('GF')))
    OR (category = 'PRODUCT' AND (app_has_contact_role('CHEF')  OR app_has_contact_role('GF')))
    OR (category = 'GENERAL')
  )
);

DROP POLICY IF EXISTS rls_notes_ins_v2 ON lead_notes;
CREATE POLICY rls_notes_ins_v2 ON lead_notes FOR INSERT
WITH CHECK (
  EXISTS (SELECT 1 FROM leads l WHERE l.id = lead_notes.lead_id AND l.org_id = app_org_id() AND l.territory = app_territory())
  AND (is_lead_owner(lead_id) OR is_lead_collaborator(lead_id) OR app_has_scope('note:create'))
);

DROP POLICY IF EXISTS rls_notes_upd_v2 ON lead_notes;
CREATE POLICY rls_notes_upd_v2 ON lead_notes FOR UPDATE
USING (
  EXISTS (SELECT 1 FROM leads l WHERE l.id = lead_notes.lead_id AND l.org_id = app_org_id() AND l.territory = app_territory())
)
WITH CHECK (
  author_user_id = app_user_id() OR app_has_scope('note:override')
);

COMMIT;
