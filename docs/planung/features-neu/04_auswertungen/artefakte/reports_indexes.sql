-- Index Hints für Reports-Performance
-- Activities (Zeitfenster + Kind + Kunde)
CREATE INDEX IF NOT EXISTS ix_activities_kind_time ON activities(kind, occurred_at DESC);
CREATE INDEX IF NOT EXISTS ix_activities_customer_time ON activities(customer_id, occurred_at DESC);

-- Field Values (Kunde + Key)
CREATE INDEX IF NOT EXISTS ix_field_values_ck ON field_values(customer_id, field_key);

-- Commission Events (optional)
DO $$ BEGIN
  IF to_regclass('public.commission_event') IS NOT NULL THEN
    CREATE INDEX IF NOT EXISTS ix_commission_event_time ON commission_event(won_at DESC);
    CREATE INDEX IF NOT EXISTS ix_commission_event_channel ON commission_event(channel);
  END IF;
END $$;

-- Hot Projection Filters
CREATE INDEX IF NOT EXISTS ix_hot_proj_season ON cm_customer_hot_proj(season_start, season_end);
CREATE INDEX IF NOT EXISTS ix_hot_proj_renewal ON cm_customer_hot_proj(renewal_date);
