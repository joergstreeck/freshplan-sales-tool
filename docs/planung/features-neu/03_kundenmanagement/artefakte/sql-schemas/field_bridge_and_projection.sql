-- field_bridge_and_projection.sql (Module 03) - Foundation Standards Compliant
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

CREATE OR REPLACE FUNCTION recompute_customer_hot(p_customer_id uuid) RETURNS void AS $$
DECLARE
  v_roi_bucket text; v_roi_value numeric(12,2);
  v_dm_count int; v_has_exec bool;
  v_season_start date; v_season_end date;
  v_renewal date; v_excl bool;
  v_sample_status text; v_sample_at timestamptz;
BEGIN
  SELECT (value->>'bucket')::text, (value->>'value')::numeric
    INTO v_roi_bucket, v_roi_value
    FROM field_values WHERE customer_id=p_customer_id AND field_key='roi_potential';

  SELECT COALESCE((value->>'count')::int, jsonb_array_length(value)),
         COALESCE((value->>'has_exec_alignment')::boolean, false)
    INTO v_dm_count, v_has_exec
    FROM field_values WHERE customer_id=p_customer_id AND field_key='decision_makers';

  SELECT (value->>'season_start')::date, (value->>'season_end')::date
    INTO v_season_start, v_season_end
    FROM field_values WHERE customer_id=p_customer_id AND field_key='seasonal_menu_cycle';

  SELECT (value->>'renewal_date')::date, (value->>'exclusivity')::boolean
    INTO v_renewal, v_excl
    FROM field_values WHERE customer_id=p_customer_id AND field_key='current_supplier_contracts';

  SELECT s.status::text, s.updated_at
    INTO v_sample_status, v_sample_at
    FROM sample_request s
    WHERE s.customer_id=p_customer_id
    ORDER BY s.updated_at DESC NULLS LAST LIMIT 1;

  INSERT INTO cm_customer_hot_proj AS hp(customer_id, sample_status, sample_last_event_at, roi_bucket, roi_value,
                                         decision_maker_count, has_exec_alignment, season_start, season_end,
                                         renewal_date, exclusivity, updated_at)
  VALUES (p_customer_id, v_sample_status, v_sample_at, v_roi_bucket, v_roi_value, v_dm_count, v_has_exec,
          v_season_start, v_season_end, v_renewal, v_excl, now())
  ON CONFLICT (customer_id) DO UPDATE
  SET sample_status = EXCLUDED.sample_status,
      sample_last_event_at = EXCLUDED.sample_last_event_at,
      roi_bucket = EXCLUDED.roi_bucket,
      roi_value = EXCLUDED.roi_value,
      decision_maker_count = EXCLUDED.decision_maker_count,
      has_exec_alignment = EXCLUDED.has_exec_alignment,
      season_start = EXCLUDED.season_start,
      season_end = EXCLUDED.season_end,
      renewal_date = EXCLUDED.renewal_date,
      exclusivity = EXCLUDED.exclusivity,
      updated_at = now();
END; $$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION trg_recompute_hot_on_field() RETURNS trigger AS $$
BEGIN
  PERFORM recompute_customer_hot(NEW.customer_id);
  RETURN NEW;
END; $$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS t_field_values_recompute ON field_values;
CREATE TRIGGER t_field_values_recompute
AFTER INSERT OR UPDATE ON field_values
FOR EACH ROW EXECUTE FUNCTION trg_recompute_hot_on_field();

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
