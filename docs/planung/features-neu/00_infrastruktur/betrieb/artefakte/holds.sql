-- ops/SQL/holds.sql
-- Create/Release Stop-Clock Holds (idempotent-ish patterns with unique constraints recommended).

-- Create Hold
INSERT INTO lead_holds (lead_id, user_id, reason, start_at, created_by)
VALUES (:lead_id, :user_id, :reason, now(), :actor_user_id);

-- Release Hold
UPDATE lead_holds
SET end_at = now(), ended_by = :actor_user_id
WHERE hold_id = :hold_id AND end_at IS NULL;
