-- Observability Views
CREATE OR REPLACE VIEW sample_metrics_daily AS
SELECT
  DATE(created_at) AS day,
  status::text     AS status,
  COUNT(*)         AS cnt
FROM sample_request
GROUP BY 1,2;

CREATE OR REPLACE VIEW roi_activity_metrics_daily AS
SELECT
  DATE(a.created_at) AS day,
  COUNT(*) FILTER (WHERE a.kind = 'ROI_CONSULTATION') AS roi_consults,
  SUM((a.payload->>'savingsPerMonth')::numeric)        AS total_savings_pm
FROM activities a
GROUP BY 1;

-- Example: stale cockpit detection via lag in hot projection updates
CREATE OR REPLACE VIEW hot_projection_staleness AS
SELECT
  customer_id,
  EXTRACT(EPOCH FROM (now() - updated_at))::int AS staleness_seconds
FROM cm_customer_hot_proj;
