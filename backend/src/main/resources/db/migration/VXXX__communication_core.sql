-- Communication Core SQL – Foundation Standards (PostgreSQL)
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ParticipantSet stores email participants for a thread
CREATE TABLE IF NOT EXISTS participant_set (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  from_email text,
  to_emails jsonb NOT NULL DEFAULT '[]'::jsonb,
  cc_emails jsonb NOT NULL DEFAULT '[]'::jsonb,
  bcc_emails jsonb NOT NULL DEFAULT '[]'::jsonb
);

CREATE TYPE comm_channel AS ENUM ('EMAIL','PHONE','MEETING','NOTE');
CREATE TYPE msg_direction AS ENUM ('INBOUND','OUTBOUND');
CREATE TYPE msg_status AS ENUM ('RECEIVED','SENT','QUEUED','BOUNCED');
CREATE TYPE bounce_severity AS ENUM ('HARD','SOFT');

CREATE TABLE IF NOT EXISTS communication_threads (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  customer_id uuid NOT NULL,
  territory text NOT NULL,
  channel comm_channel NOT NULL DEFAULT 'EMAIL',
  subject text,
  participant_set_id uuid REFERENCES participant_set(id) ON DELETE SET NULL,
  last_message_at timestamptz NOT NULL DEFAULT now(),
  unread_count int NOT NULL DEFAULT 0,
  version int NOT NULL DEFAULT 0,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS ix_comm_threads_customer ON communication_threads(customer_id);
CREATE INDEX IF NOT EXISTS ix_comm_threads_territory_last ON communication_threads(territory, last_message_at DESC);

CREATE TABLE IF NOT EXISTS communication_messages (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  thread_id uuid NOT NULL REFERENCES communication_threads(id) ON DELETE CASCADE,
  territory text NOT NULL,
  direction msg_direction NOT NULL,
  status msg_status NOT NULL,
  subject text,
  body_text text,
  sender_email text,
  recipients jsonb NOT NULL DEFAULT '[]'::jsonb,
  mime_message_id text,
  in_reply_to text,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS ix_comm_msgs_thread_time ON communication_messages(thread_id, created_at DESC);
CREATE INDEX IF NOT EXISTS ix_comm_msgs_mime ON communication_messages(mime_message_id);

CREATE TABLE IF NOT EXISTS outbox_emails (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  message_id uuid NOT NULL REFERENCES communication_messages(id) ON DELETE CASCADE,
  rate_bucket text, -- e.g. recipient domain
  status text NOT NULL DEFAULT 'PENDING', -- TODO: Use ENUM in production: CREATE TYPE outbox_status AS ENUM ('PENDING','SENDING','SENT','FAILED')
  retry_count int NOT NULL DEFAULT 0,
  next_attempt_at timestamptz NOT NULL DEFAULT now(),
  locked_by text,
  locked_at timestamptz,
  last_error text,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS ix_outbox_status_time ON outbox_emails(status, next_attempt_at);
CREATE INDEX IF NOT EXISTS ix_outbox_rate_bucket ON outbox_emails(rate_bucket);

CREATE TABLE IF NOT EXISTS bounce_events (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  message_id uuid NOT NULL REFERENCES communication_messages(id) ON DELETE CASCADE,
  severity bounce_severity NOT NULL,
  smtp_code text,
  reason text,
  created_at timestamptz NOT NULL DEFAULT now()
);

CREATE TYPE activity_kind AS ENUM ('CALL','MEETING','FOLLOW_UP');
CREATE TABLE IF NOT EXISTS comm_activity (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  customer_id uuid NOT NULL,
  territory text NOT NULL,
  kind activity_kind NOT NULL,
  occurred_at timestamptz NOT NULL,
  summary text NOT NULL,
  participants jsonb NOT NULL DEFAULT '[]'::jsonb,
  created_at timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS ix_comm_activity_customer_time ON comm_activity(customer_id, occurred_at DESC);

-- SLA tasks
CREATE TABLE IF NOT EXISTS sla_task (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  customer_id uuid NOT NULL,
  territory text NOT NULL,
  rule_key text NOT NULL,
  due_at timestamptz NOT NULL,
  status text NOT NULL DEFAULT 'PENDING', -- TODO: Use ENUM in production: CREATE TYPE sla_status AS ENUM ('PENDING','DONE','CANCELED')
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS ix_sla_task_due ON sla_task(status, due_at);

-- RLS – territory isolation
ALTER TABLE communication_threads ENABLE ROW LEVEL SECURITY;
CREATE POLICY rls_threads_read ON communication_threads
  USING (territory = current_setting('app.territory', true));
CREATE POLICY rls_threads_write ON communication_threads
  FOR UPDATE USING (territory = current_setting('app.territory', true))
  WITH CHECK (territory = current_setting('app.territory', true));

ALTER TABLE communication_messages ENABLE ROW LEVEL SECURITY;
CREATE POLICY rls_msgs_read ON communication_messages
  USING (territory = current_setting('app.territory', true));
CREATE POLICY rls_msgs_write ON communication_messages
  FOR UPDATE USING (territory = current_setting('app.territory', true))
  WITH CHECK (territory = current_setting('app.territory', true));

ALTER TABLE comm_activity ENABLE ROW LEVEL SECURITY;
CREATE POLICY rls_act_read ON comm_activity
  USING (territory = current_setting('app.territory', true));
CREATE POLICY rls_act_write ON comm_activity
  FOR UPDATE USING (territory = current_setting('app.territory', true))
  WITH CHECK (territory = current_setting('app.territory', true));

ALTER TABLE sla_task ENABLE ROW LEVEL SECURITY;
CREATE POLICY rls_sla_read ON sla_task
  USING (territory = current_setting('app.territory', true));
CREATE POLICY rls_sla_write ON sla_task
  FOR UPDATE USING (territory = current_setting('app.territory', true))
  WITH CHECK (territory = current_setting('app.territory', true));
