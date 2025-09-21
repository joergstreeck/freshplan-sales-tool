-- VXXX__admin_audit.sql
-- NOTE: Use './scripts/get-next-migration.sh' to determine the next migration number.
-- Replace 'VXXX' with the returned number before commit/deploy.
-- Foundation Standards: named constraints, indexes, RLS where applicable, and comments.

DO $$ BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname='risk_tier_enum') THEN
    CREATE TYPE risk_tier_enum AS ENUM ('TIER1','TIER2','TIER3');
  END IF;
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname='approval_state_enum') THEN
    CREATE TYPE approval_state_enum AS ENUM ('PENDING','APPROVED','REJECTED','OVERRIDDEN','EXPIRED');
  END IF;
END $$;

CREATE TABLE IF NOT EXISTS admin_audit (
  id               uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id        uuid NOT NULL,
  org_id           uuid,
  actor_user_id    uuid NOT NULL,
  actor_roles      text[] NOT NULL,
  actor_territories text[],
  action           text NOT NULL,
  resource_type    text NOT NULL,
  resource_id      text,
  risk_tier        risk_tier_enum NOT NULL DEFAULT 'TIER3',
  reason           text,
  before_json      jsonb,
  after_json       jsonb,
  correlation_id   uuid,
  created_at       timestamptz NOT NULL DEFAULT now()
);
COMMENT ON TABLE admin_audit IS 'Immutable audit trail of admin actions';

CREATE INDEX IF NOT EXISTS ix_admin_audit_tenant_created ON admin_audit(tenant_id, created_at DESC);
CREATE INDEX IF NOT EXISTS ix_admin_audit_resource ON admin_audit(resource_type, resource_id);
CREATE INDEX IF NOT EXISTS ix_admin_audit_actor ON admin_audit(actor_user_id, created_at DESC);

-- Optional RLS (read within tenant/org)
ALTER TABLE admin_audit ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS rls_admin_audit_read ON admin_audit;
CREATE POLICY rls_admin_audit_read ON admin_audit
  USING ((tenant_id::text = current_setting('app.tenant_id', true)));

