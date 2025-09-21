-- ops/SQL/reminders.sql
-- Send Reminder for leads where REMINDER_DUE and not reminded in this window (idempotent via unique key).

WITH due AS (
  SELECT f.lead_id, f.user_id, f.reminder_due_at
  FROM v_user_lead_protection f
  LEFT JOIN lead_reminders r
    ON r.lead_id=f.lead_id AND r.user_id=f.user_id
       AND r.window_start=f.reminder_due_at::date   -- window keyed by date of reminder_due_at
  WHERE f.state='REMINDER_DUE'
)
INSERT INTO lead_reminders (lead_id, user_id, window_start, sent_at, channel, payload)
SELECT lead_id, user_id, reminder_due_at::date, now(), 'EMAIL', jsonb_build_object('template','lead-reminder-v1')
FROM due
ON CONFLICT DO NOTHING;

-- Publisher picks new rows and emits `lead.protection.reminder` events (Outbox pattern).
