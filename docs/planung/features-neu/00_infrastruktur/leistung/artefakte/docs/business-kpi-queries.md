---
Title: Business KPI Queries (PromQL/SQL)
Last Updated: 2025-09-21

## PromQL
- Lead Conversion Rate:
  `sum(rate(business_lead_converted_total[5m])) / sum(rate(business_lead_created_total[5m]))`

- Cost per Lead (exponierte Metrik):
  `avg(finops_cost_per_lead)`

- Web Vitals (LCP p75 global):
  `histogram_quantile(0.75, sum(rate(web_vitals_lcp_bucket[5m])) by (le))`

## SQL (Event store → counters exporter)
-- Beispiel: Leads erstellt / konvertiert pro 5 Minuten zur Exporter-Bildung
SELECT date_trunc('minute', created_at) AS ts, COUNT(*) AS leads_created
FROM leads
WHERE created_at >= now() - INTERVAL '7 days'
GROUP BY 1
ORDER BY 1;
