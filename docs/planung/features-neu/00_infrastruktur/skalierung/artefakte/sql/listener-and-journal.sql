
-- FreshPlan · LISTEN/NOTIFY Resilience + Event Journal (CQRS Light)
-- Requires: pgcrypto extension for gen_random_uuid()
CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE SCHEMA IF NOT EXISTS integration;

-- Journal for critical events (backup when NOTIFY queue is busy)
CREATE TABLE IF NOT EXISTS integration.event_journal (
  id            uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  event         text        NOT NULL,
  tenant        text        NOT NULL DEFAULT 'freshfoodz',
  org           text        NOT NULL,
  territory     text        NOT NULL CHECK (territory IN ('DE','CH','AT')),
  correlation_id text,
  payload       jsonb       NOT NULL,
  status        text        NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING','HANDLED','ERROR')),
  attempts      int         NOT NULL DEFAULT 0,
  error         text,
  created_at    timestamptz NOT NULL DEFAULT now(),
  handled_at    timestamptz
);

CREATE INDEX IF NOT EXISTS ix_event_journal_event_time ON integration.event_journal(event, created_at DESC);
CREATE INDEX IF NOT EXISTS ix_event_journal_territory_time ON integration.event_journal(territory, created_at DESC);
CREATE INDEX IF NOT EXISTS ix_event_journal_gin_payload ON integration.event_journal USING gin (payload jsonb_path_ops);

-- Helper: current NOTIFY queue usage
CREATE OR REPLACE FUNCTION integration.notify_queue_usage()
RETURNS numeric LANGUAGE sql STABLE AS $$
  SELECT coalesce(pg_notification_queue_usage(), 0.0)
$$;

-- Enqueue + notify with backpressure guard
CREATE OR REPLACE FUNCTION integration.enqueue_event(
  p_event text,
  p_org text,
  p_territory text,
  p_correlation_id text,
  p_payload jsonb,
  p_critical boolean DEFAULT false,
  p_notify_threshold numeric DEFAULT 0.80
) RETURNS uuid
LANGUAGE plpgsql AS $$
DECLARE v_id uuid; v_usage numeric;
BEGIN
  INSERT INTO integration.event_journal(event, org, territory, correlation_id, payload)
  VALUES (p_event, p_org, p_territory, p_correlation_id, p_payload)
  RETURNING id INTO v_id;

  SELECT integration.notify_queue_usage() INTO v_usage;
  IF v_usage < p_notify_threshold THEN
    PERFORM pg_notify('ffz_events', jsonb_build_object(
      'event', p_event,
      'id', v_id,
      'time', now(),
      'tenant', 'freshfoodz',
      'org', p_org,
      'territory', p_territory,
      'correlationId', coalesce(p_correlation_id,''),
      'version', 1,
      'data', p_payload
    )::text);
  ELSIF p_critical AND v_usage < 0.95 THEN
    -- attempt notify anyway if critical and buffer not near saturation
    PERFORM pg_notify('ffz_events', jsonb_build_object(
      'event', p_event,
      'id', v_id,
      'time', now(),
      'tenant', 'freshfoodz',
      'org', p_org,
      'territory', p_territory,
      'correlationId', coalesce(p_correlation_id,''),
      'version', 1,
      'data', p_payload
    )::text);
  END IF;
  RETURN v_id;
END $$;

-- Markers for consumers
CREATE OR REPLACE FUNCTION integration.mark_event_handled(p_id uuid, p_ok boolean, p_error text DEFAULT NULL)
RETURNS void LANGUAGE plpgsql AS $$
BEGIN
  UPDATE integration.event_journal
     SET status = CASE WHEN p_ok THEN 'HANDLED' ELSE 'ERROR' END,
         error = CASE WHEN p_ok THEN NULL ELSE p_error END,
         attempts = attempts + 1,
         handled_at = now()
   WHERE id = p_id;
END $$;

-- Replay pending (used when listener detects drift)
CREATE OR REPLACE FUNCTION integration.replay_pending(p_limit int DEFAULT 500)
RETURNS SETOF integration.event_journal
LANGUAGE plpgsql AS $$
BEGIN
  RETURN QUERY
  WITH cte AS (
    SELECT id,event,org,territory,correlation_id,payload
      FROM integration.event_journal
     WHERE status = 'PENDING'
     ORDER BY created_at ASC
     LIMIT p_limit
  )
  SELECT * FROM cte;
END $$;

-- Metrics view
CREATE OR REPLACE VIEW integration.event_metrics AS
SELECT
  now() AS ts,
  integration.notify_queue_usage() AS notify_queue_usage,
  count(*) FILTER (WHERE status='PENDING' AND created_at < now() - interval '1 minute') AS pending_over_1m,
  count(*) FILTER (WHERE status='ERROR' AND handled_at > now() - interval '10 minutes') AS errors_last_10m
FROM integration.event_journal;

-- Example triggers: create only if tables exist
DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema='public' AND table_name='leads') THEN
    EXECUTE $$
      CREATE OR REPLACE FUNCTION public.trg_lead_status_notify()
      RETURNS trigger LANGUAGE plpgsql AS $$$$
      BEGIN
        IF TG_OP = 'UPDATE' AND NEW.status IS DISTINCT FROM OLD.status THEN
          PERFORM integration.enqueue_event(
            'lead.status.changed', NEW.org_id, NEW.territory, current_setting('app.correlation_id', true),
            jsonb_build_object('leadId', NEW.id, 'old', OLD.status, 'new', NEW.status),
            true, 0.80);
        END IF;
        RETURN NEW;
      END;
      $$$$;
    $$;
    IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname='lead_status_notify') THEN
      EXECUTE 'CREATE TRIGGER lead_status_notify AFTER UPDATE ON public.leads FOR EACH ROW EXECUTE FUNCTION public.trg_lead_status_notify()';
    END IF;
  END IF;
END $$;

-- NOTE: Add similar DO blocks for samples (sample.status.changed) and credit checks (credit.checked).
-- Monitoring query examples:
--   SELECT * FROM integration.event_metrics;
--   SELECT integration.notify_queue_usage();
