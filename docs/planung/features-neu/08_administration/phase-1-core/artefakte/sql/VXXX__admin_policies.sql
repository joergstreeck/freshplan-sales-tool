-- VXXX__admin_policies.sql
-- NOTE: Use './scripts/get-next-migration.sh' to determine the next migration number.
-- Replace 'VXXX' with the returned number before commit/deploy.
-- Foundation Standards: named constraints, indexes, RLS where applicable, and comments.

DO $$ BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname='approval_status_enum') THEN
    CREATE TYPE approval_status_enum AS ENUM ('PENDING','SCHEDULED','APPROVED','REJECTED','CANCELLED','EXECUTED','OVERRIDDEN');
  END IF;
END $$;

CREATE TABLE IF NOT EXISTS admin_policy_definition (
  id            uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  key           text UNIQUE NOT NULL,
  description   text,
  json_schema   jsonb NOT NULL,
  created_at    timestamptz NOT NULL DEFAULT now(),
  updated_at    timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS admin_approval_request (
  id               uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id        uuid NOT NULL,
  org_id           uuid,
  risk_tier        risk_tier_enum NOT NULL,
  action           text NOT NULL,
  resource_type    text NOT NULL,
  resource_id      text,
  requested_by     uuid NOT NULL,
  justification    text,
  emergency        boolean NOT NULL DEFAULT false,
  status           approval_status_enum NOT NULL DEFAULT 'PENDING',
  time_delay_until timestamptz,      -- scheduled execution time (Tier1/2 delays)
  approved_by      uuid,
  approved_at      timestamptz,
  executed_at      timestamptz,
  details          jsonb,            -- arbitrary payload (new values, etc.)
  created_at       timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS ix_admin_approval_status ON admin_approval_request(status, time_delay_until);
CREATE INDEX IF NOT EXISTS ix_admin_approval_tenant ON admin_approval_request(tenant_id, created_at DESC);

ALTER TABLE admin_policy_definition ENABLE ROW LEVEL SECURITY;
ALTER TABLE admin_approval_request ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS rls_admin_policy_def_read ON admin_policy_definition;
CREATE POLICY rls_admin_policy_def_read ON admin_policy_definition
  USING (true);

DROP POLICY IF EXISTS rls_admin_approval_rw ON admin_approval_request;
CREATE POLICY rls_admin_approval_rw ON admin_approval_request
  USING   (tenant_id::text = current_setting('app.tenant_id', true))
  WITH CHECK (tenant_id::text = current_setting('app.tenant_id', true));

