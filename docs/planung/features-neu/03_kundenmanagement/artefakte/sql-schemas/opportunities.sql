-- opportunities.sql (Module 03) – ROI-basierte Verkaufschancen
CREATE TYPE opportunity_stage AS ENUM ('DISCOVERY','TEST_PHASE','NEGOTIATION','WON','LOST');

CREATE TABLE IF NOT EXISTS opportunity (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  customer_id uuid NOT NULL,
  territory text NOT NULL,
  stage opportunity_stage NOT NULL DEFAULT 'DISCOVERY',
  roi_value_estimate numeric(12,2),
  labor_savings_minutes int,
  food_waste_reduction_pct numeric(5,2),
  product_fit_score int, -- 0..100
  seasonal_window boolean DEFAULT false,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS ix_opportunity_customer ON opportunity(customer_id);
CREATE INDEX IF NOT EXISTS ix_opportunity_stage ON opportunity(stage);

ALTER TABLE opportunity ENABLE ROW LEVEL SECURITY;
CREATE POLICY rls_opp_read ON opportunity USING (territory = current_setting('app.territory', true));
CREATE POLICY rls_opp_write ON opportunity FOR UPDATE USING (territory = current_setting('app.territory', true)) WITH CHECK (territory = current_setting('app.territory', true));