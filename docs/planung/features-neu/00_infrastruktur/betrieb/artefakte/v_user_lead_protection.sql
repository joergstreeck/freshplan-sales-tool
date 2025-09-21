-- ops/SQL/v_user_lead_protection.sql
-- Purpose: Effektive Zustände & Timer der userbasierten Lead-Protection (6M + 60T + 10T) mit Stop-Clock (Holds).
-- Assumptions: Tables leads, lead_ownership, activities, lead_holds, lead_reminders exist (UUID keys, timestamptz).

WITH base AS (
  SELECT
    l.id AS lead_id,
    o.user_id,
    o.assigned_at,
    -- letzter Nachweis qualifizierter Aktivität
    (
      SELECT MAX(a.created_at) FROM activities a
      WHERE a.lead_id = l.id AND a.user_id = o.user_id
        AND a.kind IN ('QUALIFIED_CALL','CUSTOMER_REACTION','SCHEDULED_FOLLOWUP','SAMPLE_FEEDBACK','ROI_PRESENTATION')
    ) AS last_qual_act_at
  FROM leads l
  JOIN lead_ownership o ON o.lead_id = l.id
),
hold_windows AS (
  -- alle Holds pro (lead,user)
  SELECT h.lead_id, h.user_id, h.start_at, COALESCE(h.end_at, now()) AS end_at
  FROM lead_holds h
),
hold_total AS (
  -- Gesamtdauer aller Holds seit Assignment
  SELECT b.lead_id, b.user_id,
         COALESCE(SUM(EXTRACT(EPOCH FROM (LEAST(hw.end_at, now()) - GREATEST(hw.start_at, b.assigned_at))) ) ,0) AS hold_sec_since_assign
  FROM base b
  LEFT JOIN hold_windows hw ON hw.lead_id=b.lead_id AND hw.user_id=b.user_id
                            AND hw.end_at > b.assigned_at
  GROUP BY b.lead_id, b.user_id
),
hold_since_last_act AS (
  -- Überlappung Holds seit letzter qualifizierter Aktivität
  SELECT b.lead_id, b.user_id,
         COALESCE(SUM(EXTRACT(EPOCH FROM (LEAST(hw.end_at, now()) - GREATEST(hw.start_at, COALESCE(b.last_qual_act_at, b.assigned_at)))) ),0) AS hold_sec_since_last_act
  FROM base b
  LEFT JOIN hold_windows hw ON hw.lead_id=b.lead_id AND hw.user_id=b.user_id
                            AND hw.end_at > COALESCE(b.last_qual_act_at, b.assigned_at)
  GROUP BY b.lead_id, b.user_id
),
calc AS (
  SELECT
    b.lead_id, b.user_id, b.assigned_at, b.last_qual_act_at,
    (b.assigned_at + INTERVAL '6 months' + make_interval(secs=>ht.hold_sec_since_assign)) AS base_expiry_at,
    EXTRACT(EPOCH FROM (now() - COALESCE(b.last_qual_act_at, b.assigned_at))) - hla.hold_sec_since_last_act AS inactivity_sec_eff
  FROM base b
  LEFT JOIN hold_total ht ON (ht.lead_id,ht.user_id)=(b.lead_id,b.user_id)
  LEFT JOIN hold_since_last_act hla ON (hla.lead_id,hla.user_id)=(b.lead_id,b.user_id)
),
st AS (
  SELECT
    c.*,
    (c.inactivity_sec_eff/86400.0)::numeric(10,2) AS inactivity_days_eff,
    (COALESCE(c.last_qual_act_at, c.assigned_at) + INTERVAL '60 days' + make_interval(secs=>GREATEST(0, hla.hold_sec_since_last_act))) AS reminder_due_at,
    (COALESCE(c.last_qual_act_at, c.assigned_at) + INTERVAL '70 days' + make_interval(secs=>GREATEST(0, hla.hold_sec_since_last_act))) AS grace_end_at
  FROM calc c
  JOIN hold_since_last_act hla ON (hla.lead_id,hla.user_id)=(c.lead_id,c.user_id)
),
final AS (
  SELECT
    lead_id, user_id, assigned_at, last_qual_act_at, base_expiry_at, inactivity_days_eff,
    reminder_due_at, grace_end_at,
    CASE
      WHEN now() >= grace_end_at THEN 'EXPIRED'
      WHEN now() >= reminder_due_at THEN 'GRACE'
      WHEN inactivity_days_eff >= 60 THEN 'REMINDER_DUE'
      WHEN now() < base_expiry_at THEN 'PROTECTED'
      ELSE 'EXPIRED'
    END AS state
  FROM st
)
SELECT * FROM final;
