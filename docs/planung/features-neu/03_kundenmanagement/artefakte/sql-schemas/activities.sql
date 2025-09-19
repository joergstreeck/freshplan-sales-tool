-- activities.sql (Module 03) – B2B-Convenience-Food Aktivitäten
CREATE TABLE IF NOT EXISTS activities (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  customer_id uuid NOT NULL,
  territory text NOT NULL,
  kind text NOT NULL, -- PRODUCTTEST_FEEDBACK | ROI_CONSULTATION | MENU_INTEGRATION | DECISION_ALIGNMENT | TRAINING | QUALITY_CHECK
  occurred_at timestamptz NOT NULL,
  created_at timestamptz NOT NULL DEFAULT now(),
  payload jsonb NOT NULL DEFAULT '{}'::jsonb
);
CREATE INDEX IF NOT EXISTS ix_activities_customer_time ON activities(customer_id, occurred_at DESC);
CREATE INDEX IF NOT EXISTS ix_activities_kind_time ON activities(kind, occurred_at DESC);

ALTER TABLE activities ENABLE ROW LEVEL SECURITY;
CREATE POLICY rls_activities_read ON activities USING (territory = current_setting('app.territory', true));
CREATE POLICY rls_activities_write ON activities FOR UPDATE USING (territory = current_setting('app.territory', true)) WITH CHECK (territory = current_setting('app.territory', true));