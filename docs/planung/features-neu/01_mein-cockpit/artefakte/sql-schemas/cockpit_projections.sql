-- Cockpit projections with RLS
CREATE TABLE IF NOT EXISTS cockpit_kpi_daily (
  day date NOT NULL,
  territory text NOT NULL,
  channel text NOT NULL, -- DIRECT | PARTNER
  sample_success_rate_pct numeric(5,2) NOT NULL DEFAULT 0,
  roi_pipeline_value numeric(14,2) NOT NULL DEFAULT 0,
  partner_share_pct numeric(5,2) NOT NULL DEFAULT 0,
  PRIMARY KEY(day, territory, channel)
);
CREATE INDEX IF NOT EXISTS ix_kpi_territory_day ON cockpit_kpi_daily(territory, day DESC);
CREATE INDEX IF NOT EXISTS ix_kpi_channel_day ON cockpit_kpi_daily(channel, day DESC);

CREATE TABLE IF NOT EXISTS cockpit_channel_mix_daily (
  day date NOT NULL,
  territory text NOT NULL,
  channel text NOT NULL,
  value_pct numeric(5,2) NOT NULL DEFAULT 0,
  PRIMARY KEY(day, territory, channel)
);

-- RLS
ALTER TABLE cockpit_kpi_daily ENABLE ROW LEVEL SECURITY;
CREATE POLICY rls_kpi_read ON cockpit_kpi_daily
  USING (territory = current_setting('app.territory', true) AND channel = ANY(string_to_array(current_setting('app.channels', true), ',')));

ALTER TABLE cockpit_channel_mix_daily ENABLE ROW LEVEL SECURITY;
CREATE POLICY rls_mix_read ON cockpit_channel_mix_daily
  USING (territory = current_setting('app.territory', true) AND channel = ANY(string_to_array(current_setting('app.channels', true), ',')));
