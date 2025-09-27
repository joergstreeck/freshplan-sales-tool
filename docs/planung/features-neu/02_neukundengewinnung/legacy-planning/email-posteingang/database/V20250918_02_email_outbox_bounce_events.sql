-- V20250918_02_email_outbox_bounce_events.sql
BEGIN;

-- Outbox für versendete/zu sendende Mails
CREATE TABLE IF NOT EXISTS email_outbox (
  id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  thread_id       uuid NULL REFERENCES email_thread(id) ON DELETE SET NULL,
  message_id      text NULL, -- SMTP Message-ID (nach Versand)
  recipient       text NOT NULL,
  subject         text NOT NULL,
  body_mime       text NOT NULL,
  status          text NOT NULL CHECK (status IN ('PENDING','SENDING','FAILED','SENT','DEAD')),
  attempts        integer NOT NULL DEFAULT 0 CHECK (attempts >= 0),
  next_attempt_at timestamptz NULL,
  last_error      text NULL,
  created_at      timestamptz NOT NULL DEFAULT now(),
  updated_at      timestamptz NOT NULL DEFAULT now()
);

DROP TRIGGER IF EXISTS trg_email_outbox_updated ON email_outbox;
CREATE TRIGGER trg_email_outbox_updated
BEFORE UPDATE ON email_outbox
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- Indizes (separat auch CONCURRENTLY möglich)
CREATE INDEX IF NOT EXISTS ix_email_outbox_status_next_attempt
  ON email_outbox (status, next_attempt_at);

CREATE INDEX IF NOT EXISTS ix_email_outbox_recipient
  ON email_outbox (recipient);

-- Bounces (normalisiert)
DO $ BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'email_bounce_type') THEN
    CREATE TYPE email_bounce_type AS ENUM ('HARD','SOFT');
  END IF;
END $;

CREATE TABLE IF NOT EXISTS email_bounce (
  id          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  type        email_bounce_type NOT NULL,
  reason      text NOT NULL,
  smtp_code   text NULL,
  provider    text NULL,
  message_id  text NULL,
  thread_id   uuid NULL REFERENCES email_thread(id) ON DELETE SET NULL,
  recipient   text NOT NULL,
  occurred_at timestamptz NOT NULL,
  received_at timestamptz NOT NULL DEFAULT now(),
  idempotency_key uuid NULL,
  UNIQUE (recipient, occurred_at)  -- verhindert Doppeleinträge bei Replays
);

CREATE INDEX IF NOT EXISTS ix_email_bounce_recipient ON email_bounce (recipient);
CREATE INDEX IF NOT EXISTS ix_email_bounce_occurred ON email_bounce (occurred_at);

-- Event-Outbox (Exactly-once Delivery)
CREATE TABLE IF NOT EXISTS event_outbox (
  id               uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  topic            text NOT NULL,
  payload          jsonb NOT NULL,
  headers          jsonb NOT NULL DEFAULT '{}'::jsonb,
  status           text NOT NULL CHECK (status IN ('PENDING','PUBLISHED','FAILED','DEAD')),
  attempts         integer NOT NULL DEFAULT 0 CHECK (attempts >= 0),
  next_attempt_at  timestamptz NULL,
  created_at       timestamptz NOT NULL DEFAULT now(),
  updated_at       timestamptz NOT NULL DEFAULT now(),
  idempotency_key  uuid NULL,
  UNIQUE (idempotency_key)
);

DROP TRIGGER IF EXISTS trg_event_outbox_updated ON event_outbox;
CREATE TRIGGER trg_event_outbox_updated
BEFORE UPDATE ON event_outbox
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

COMMIT;