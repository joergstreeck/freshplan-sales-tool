-- ops/SQL/monitoring_user_lead.sql
-- KPIs for dashboards and alerts.
-- 1) Counts by state
SELECT state, COUNT(*) FROM v_user_lead_protection GROUP BY 1;

-- 2) Upcoming expiries in next 7d
SELECT lead_id, user_id, grace_end_at
FROM v_user_lead_protection
WHERE state IN ('GRACE','PROTECTED') AND grace_end_at BETWEEN now() AND now()+INTERVAL '7 days'
ORDER BY grace_end_at;

-- 3) Reminder SLA check (sent within 1h of becoming due)
SELECT COUNT(*) FILTER (WHERE delay_min <= 60) AS within_sla,
       COUNT(*) FILTER (WHERE delay_min  > 60) AS outside_sla
FROM (
  SELECT EXTRACT(EPOCH FROM (r.sent_at - f.reminder_due_at))/60 AS delay_min
  FROM lead_reminders r
  JOIN v_user_lead_protection f ON (f.lead_id,f.user_id)=(r.lead_id,r.user_id)
) s;
