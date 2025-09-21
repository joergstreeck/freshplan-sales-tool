-- ops/SQL/provision_validation.sql
-- Validate sales commission: 7% in year 1 since conversion, 2% thereafter. Flag mismatches over tolerance.

WITH order_base AS (
  SELECT
    o.id AS order_id, o.customer_id, o.lead_id, o.placed_at, o.currency,
    SUM(ol.net_amount) AS net_amount
  FROM orders o
  JOIN order_lines ol ON ol.order_id=o.id
  GROUP BY 1,2,3,4,5
),
lead_conv AS (
  SELECT lead_id, converted_at FROM leads WHERE converted_at IS NOT NULL
),
expected AS (
  SELECT
    ob.order_id, ob.lead_id, ob.customer_id, ob.net_amount, ob.currency, lc.converted_at,
    CASE WHEN ob.placed_at < lc.converted_at + INTERVAL '1 year' THEN 0.07 ELSE 0.02 END AS rate,
    ROUND(ob.net_amount * CASE WHEN ob.placed_at < lc.converted_at + INTERVAL '1 year' THEN 0.07 ELSE 0.02 END, 2) AS expected_commission
  FROM order_base ob
  JOIN lead_conv lc USING (lead_id)
),
actual AS (
  SELECT c.order_id, SUM(c.amount) AS paid_commission
  FROM commissions c
  GROUP BY 1
)
SELECT e.order_id, e.lead_id, e.customer_id, e.net_amount, e.currency, e.expected_commission, COALESCE(a.paid_commission,0) AS paid_commission,
       (COALESCE(a.paid_commission,0) - e.expected_commission) AS delta
FROM expected e
LEFT JOIN actual a USING (order_id)
WHERE ABS(COALESCE(a.paid_commission,0) - e.expected_commission) > 0.01
ORDER BY ABS(delta) DESC;
