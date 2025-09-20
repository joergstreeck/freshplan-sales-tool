-- VXXX__admin_operations.sql
-- NOTE: Use './scripts/get-next-migration.sh' to determine the next migration number.
-- Replace 'VXXX' with the returned number before commit/deploy.
-- Foundation Standards: named constraints, indexes, RLS where applicable, and comments.

CREATE TABLE IF NOT EXISTS admin_smtp_config (
  id           uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id    uuid NOT NULL,
  host         text NOT NULL,
  port         int  NOT NULL CHECK (port between 1 and 65535),
  username     text,
  password_ref text,             -- reference to secret storage
  tls_mode     text NOT NULL CHECK (tls_mode IN ('STARTTLS','TLS','NONE')),
  truststore_ref text,
  updated_by   uuid NOT NULL,
  updated_at   timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS ix_admin_smtp_tenant ON admin_smtp_config(tenant_id, updated_at DESC);

CREATE TABLE IF NOT EXISTS admin_outbox_limits (
  tenant_id    uuid PRIMARY KEY,
  rate_per_min int NOT NULL CHECK (rate_per_min >= 0),
  paused       boolean NOT NULL DEFAULT false,
  updated_by   uuid NOT NULL,
  updated_at   timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS admin_bounce_policy (
  tenant_id     uuid PRIMARY KEY,
  hard_threshold int NOT NULL DEFAULT 1,
  soft_threshold int NOT NULL DEFAULT 3,
  block_duration text NOT NULL DEFAULT 'P7D', -- ISO-8601
  updated_by    uuid NOT NULL,
  updated_at    timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS admin_outbox_control_history (
  id           uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id    uuid NOT NULL,
  action       text NOT NULL, -- PAUSE|RESUME|RATE|BOUNCE
  details      jsonb,
  actor_user_id uuid NOT NULL,
  created_at   timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS admin_dsar_request (
  id             uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id      uuid NOT NULL,
  subject_id     uuid NOT NULL, -- contact/customer id
  req_type       text NOT NULL CHECK (req_type IN ('EXPORT','DELETE')),
  status         text NOT NULL CHECK (status IN ('QUEUED','PROCESSING','DONE','FAILED','CANCELLED')) DEFAULT 'QUEUED',
  requested_by   uuid NOT NULL,
  justification  text,
  result_url     text,
  error_message  text,
  created_at     timestamptz NOT NULL DEFAULT now(),
  updated_at     timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS ix_admin_dsar_status ON admin_dsar_request(status, created_at DESC);

ALTER TABLE admin_smtp_config ENABLE ROW LEVEL SECURITY;
ALTER TABLE admin_outbox_limits ENABLE ROW LEVEL SECURITY;
ALTER TABLE admin_bounce_policy ENABLE ROW LEVEL SECURITY;
ALTER TABLE admin_outbox_control_history ENABLE ROW LEVEL SECURITY;
ALTER TABLE admin_dsar_request ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS rls_smtp_rw ON admin_smtp_config;
CREATE POLICY rls_smtp_rw ON admin_smtp_config
  USING   (tenant_id::text = current_setting('app.tenant_id', true))
  WITH CHECK (tenant_id::text = current_setting('app.tenant_id', true));

DROP POLICY IF EXISTS rls_outbox_limits_rw ON admin_outbox_limits;
CREATE POLICY rls_outbox_limits_rw ON admin_outbox_limits
  USING   (tenant_id::text = current_setting('app.tenant_id', true))
  WITH CHECK (tenant_id::text = current_setting('app.tenant_id', true));

DROP POLICY IF EXISTS rls_bounce_policy_rw ON admin_bounce_policy;
CREATE POLICY rls_bounce_policy_rw ON admin_bounce_policy
  USING   (tenant_id::text = current_setting('app.tenant_id', true))
  WITH CHECK (tenant_id::text = current_setting('app.tenant_id', true));

DROP POLICY IF EXISTS rls_dsar_rw ON admin_dsar_request;
CREATE POLICY rls_dsar_rw ON admin_dsar_request
  USING   (tenant_id::text = current_setting('app.tenant_id', true))
  WITH CHECK (tenant_id::text = current_setting('app.tenant_id', true));
