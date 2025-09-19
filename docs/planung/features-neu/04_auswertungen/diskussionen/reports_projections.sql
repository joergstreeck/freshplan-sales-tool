-- Reports Projections & Views (PostgreSQL)
-- Annahmen: Tabellen `activities`, `field_values`, `cm_customer_hot_proj`, `sample_request` existieren (siehe Module 02/03).
-- Optional: `commission_event` (Deals/Revenue). Bei Abwesenheit wird Partner-Share 0 gesetzt.

CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- 1) Activities-Basisview (90 Tage, nur relevante Typen)
CREATE OR REPLACE VIEW rpt_activities_90d AS
SELECT
  a.id,
  a.customer_id,
  a.kind,                        -- 'PRODUCTTEST_FEEDBACK' | 'ROI_CONSULTATION'
  a.occurred_at,
  a.created_at,
  a.payload
FROM activities a
WHERE a.occurred_at >= now() - interval '90 days';

-- 2) Sample Success (90 Tage) – auf Basis der Activities
CREATE OR REPLACE VIEW rpt_sample_success_90d AS
SELECT
  COUNT(*) FILTER (WHERE kind='PRODUCTTEST_FEEDBACK')                 AS total_tests,
  COUNT(*) FILTER (WHERE kind='PRODUCTTEST_FEEDBACK'
                    AND (payload->>'outcome') = 'SUCCESS')            AS success_tests,
  CASE
    WHEN COUNT(*) FILTER (WHERE kind='PRODUCTTEST_FEEDBACK') = 0 THEN NULL
    ELSE ROUND(
      100.0 * COUNT(*) FILTER (WHERE kind='PRODUCTTEST_FEEDBACK' AND (payload->>'outcome')='SUCCESS')
      / COUNT(*) FILTER (WHERE kind='PRODUCTTEST_FEEDBACK'), 2)
  END AS sample_success_rate_pct
FROM rpt_activities_90d;

-- 3) ROI-Pipeline (90 Tage) – Kombination aus Field-Value & Activity
-- Gewichtung: LOW=0.25, MID=0.6, HIGH=1.0
CREATE OR REPLACE VIEW rpt_roi_pipeline_90d AS
WITH roi_fields AS (
  SELECT fv.customer_id,
         (fv.value->>'bucket')::text AS bucket,
         COALESCE((fv.value->>'value')::numeric, 0) AS value
  FROM field_values fv
  WHERE fv.field_key='roi_potential'
),
weights AS (
  SELECT 'LOW'::text AS b, 0.25::numeric AS w UNION ALL
  SELECT 'MID', 0.60 UNION ALL
  SELECT 'HIGH', 1.00
),
commitment AS (
  SELECT customer_id,
         MAX(CASE (payload->>'commitmentLevel')
               WHEN 'HIGH' THEN 1.0
               WHEN 'MID'  THEN 0.6
               WHEN 'LOW'  THEN 0.25
               ELSE 0.0 END) AS commitment_weight
  FROM rpt_activities_90d
  WHERE kind='ROI_CONSULTATION'
  GROUP BY customer_id
)
SELECT
  SUM(r.value * COALESCE(w.w,1.0) * COALESCE(c.commitment_weight,1.0)) AS roi_pipeline_value
FROM roi_fields r
LEFT JOIN weights w ON UPPER(r.bucket)=w.b
LEFT JOIN commitment c ON c.customer_id=r.customer_id;

-- 4) Partner-Share (90 Tage) – optional auf commission_event oder Fallback Channel-Field
DO $$
BEGIN
  IF to_regclass('public.commission_event') IS NOT NULL THEN
    EXECUTE $$
      CREATE OR REPLACE VIEW rpt_partner_share_90d AS
      SELECT
        SUM(CASE WHEN e.channel IN ('PARTNER','RESELLER') THEN e.net_revenue ELSE 0 END) AS revenue_partner,
        SUM(e.net_revenue) AS revenue_total,
        CASE WHEN SUM(e.net_revenue)=0 THEN NULL
             ELSE ROUND(100.0 * SUM(CASE WHEN e.channel IN ('PARTNER','RESELLER') THEN e.net_revenue ELSE 0 END) / SUM(e.net_revenue), 2)
        END AS partner_share_pct
      FROM commission_event e
      WHERE e.won_at >= now() - interval '90 days';
    $$;
  ELSE
    -- Fallback über field_values.channel_type ('DIRECT'|'PARTNER')
    EXECUTE $$
      CREATE OR REPLACE VIEW rpt_partner_share_90d AS
      WITH ch AS (
        SELECT customer_id, upper(value->>'channel_type') AS channel_type
        FROM field_values WHERE field_key IN ('channel_type','channelType')
      ),
      approx_rev AS (
        -- Annahme: ROI-Wert als Proxy für potenziellen Umsatz (nur für Fallback; kann ersetzt werden)
        SELECT fv.customer_id, COALESCE((fv.value->>'value')::numeric,0) AS revenue_proxy
        FROM field_values fv WHERE fv.field_key='roi_potential'
      )
      SELECT
        SUM(CASE WHEN ch.channel_type='PARTNER' THEN revenue_proxy ELSE 0 END) AS revenue_partner,
        SUM(revenue_proxy) AS revenue_total,
        CASE WHEN SUM(revenue_proxy)=0 THEN NULL
             ELSE ROUND(100.0 * SUM(CASE WHEN ch.channel_type='PARTNER' THEN revenue_proxy ELSE 0 END) / SUM(revenue_proxy), 2)
        END AS partner_share_pct
      FROM approx_rev ar
      LEFT JOIN ch ON ch.customer_id=ar.customer_id;
    $$;
  END IF;
END$$;

-- 5) At-Risk Customers (60 Tage ohne Activity ODER Hard Bounce)
CREATE OR REPLACE VIEW rpt_at_risk_customers AS
WITH last_act AS (
  SELECT customer_id, MAX(occurred_at) AS last_contact_at
  FROM rpt_activities_90d
  GROUP BY customer_id
),
contactability AS (
  SELECT customer_id, upper(value->>'status') AS status
  FROM field_values WHERE field_key='contactability_status'
)
SELECT
  hp.customer_id,
  COALESCE(la.last_contact_at, TIMESTAMP 'epoch') AS last_contact_at,
  CASE
    WHEN c.status='HARD_BOUNCE' THEN true
    WHEN la.last_contact_at IS NULL THEN true
    WHEN la.last_contact_at < now() - interval '60 days' THEN true
    ELSE false
  END AS is_at_risk
FROM cm_customer_hot_proj hp
LEFT JOIN last_act la ON la.customer_id=hp.customer_id
LEFT JOIN contactability c ON c.customer_id=hp.customer_id
WHERE
  CASE
    WHEN c.status='HARD_BOUNCE' THEN true
    WHEN la.last_contact_at IS NULL THEN true
    WHEN la.last_contact_at < now() - interval '60 days' THEN true
    ELSE false
  END;

-- 6) Daily Snapshot Tabelle + Recompute
CREATE TABLE IF NOT EXISTS rpt_sales_daily (
  day date PRIMARY KEY,
  sample_success_rate_pct numeric(5,2),
  roi_pipeline_value numeric(12,2),
  partner_share_pct numeric(5,2),
  at_risk_customers int,
  updated_at timestamptz DEFAULT now()
);

CREATE OR REPLACE FUNCTION recompute_rpt_sales_daily(p_day date DEFAULT CURRENT_DATE) RETURNS void AS $$
DECLARE v_ss numeric(5,2);
DECLARE v_roi numeric(12,2);
DECLARE v_ps  numeric(5,2);
DECLARE v_ar  int;
BEGIN
  SELECT sample_success_rate_pct INTO v_ss FROM rpt_sample_success_90d;
  SELECT roi_pipeline_value INTO v_roi FROM rpt_roi_pipeline_90d;
  SELECT partner_share_pct INTO v_ps FROM rpt_partner_share_90d;
  SELECT COUNT(*) INTO v_ar FROM rpt_at_risk_customers;

  INSERT INTO rpt_sales_daily(day, sample_success_rate_pct, roi_pipeline_value, partner_share_pct, at_risk_customers, updated_at)
  VALUES (p_day, v_ss, v_roi, v_ps, v_ar, now())
  ON CONFLICT (day) DO UPDATE SET
    sample_success_rate_pct = EXCLUDED.sample_success_rate_pct,
    roi_pipeline_value      = EXCLUDED.roi_pipeline_value,
    partner_share_pct       = EXCLUDED.partner_share_pct,
    at_risk_customers       = EXCLUDED.at_risk_customers,
    updated_at              = now();
END; $$ LANGUAGE plpgsql;

-- 7) Indizes für schnelle Filter (Views nutzen bestehende Indizes der Basistabellen)
-- Empfohlen: INDEX ON activities(kind, occurred_at DESC), field_values(customer_id, field_key), cm_customer_hot_proj(sample_status, sample_last_event_at)

