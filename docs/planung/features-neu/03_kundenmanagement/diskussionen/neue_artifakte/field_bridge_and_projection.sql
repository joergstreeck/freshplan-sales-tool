-- field_bridge_and_projection.sql (Module 03)
-- Foundation SQL Standards: performance (indexes), security (RLS), docs.
-- RLS: uses `current_setting('app.territory', true)` for ABAC scoping.
CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS field_values (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  customer_id uuid NOT NULL,
  field_key text NOT NULL,
  value jsonb NOT NULL,
  territory text NOT NULL,
  updated_by uuid,
  updated_at timestamptz NOT NULL DEFAULT now(),
  UNIQUE (customer_id, field_key)
);
CREATE INDEX IF NOT EXISTS ix_field_values_customer ON field_values(customer_id);
CREATE INDEX IF NOT EXISTS ix_field_values_ck ON field_values(customer_id, field_key);

CREATE TABLE IF NOT EXISTS cm_customer_hot_proj (
  customer_id uuid PRIMARY KEY,
  territory text NOT NULL,
  sample_status text,
  sample_last_event_at timestamptz,
  roi_bucket text,
  roi_value numeric(12,2),
  decision_maker_count int,
  has_exec_alignment boolean,
  season_start date,
  season_end date,
  renewal_date date,
  exclusivity boolean,
  updated_at timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS ix_hot_proj_territory ON cm_customer_hot_proj(territory);
CREATE INDEX IF NOT EXISTS ix_hot_proj_renewal ON cm_customer_hot_proj(renewal_date);

-- RLS (read/write) per territory
ALTER TABLE field_values ENABLE ROW LEVEL SECURITY;
CREATE POLICY rls_field_values_read ON field_values
  USING (territory = current_setting('app.territory', true));
CREATE POLICY rls_field_values_write ON field_values
  FOR UPDATE USING (territory = current_setting('app.territory', true))
  WITH CHECK (territory = current_setting('app.territory', true));

ALTER TABLE cm_customer_hot_proj ENABLE ROW LEVEL SECURITY;
CREATE POLICY rls_hot_proj_read ON cm_customer_hot_proj
  USING (territory = current_setting('app.territory', true));
CREATE POLICY rls_hot_proj_write ON cm_customer_hot_proj
  FOR UPDATE USING (territory = current_setting('app.territory', true))
  WITH CHECK (territory = current_setting('app.territory', true));
