-- Data Retention & Masking Policies (example)
-- 1) Field values: keep latest 2 years, older -> archive table
CREATE TABLE IF NOT EXISTS field_values_archive (LIKE field_values INCLUDING ALL);

CREATE OR REPLACE FUNCTION archive_old_field_values(cutoff interval DEFAULT interval '2 years') RETURNS int AS $$
DECLARE moved int;
BEGIN
  WITH to_move AS (
    SELECT * FROM field_values WHERE updated_at < now() - cutoff
  )
  INSERT INTO field_values_archive SELECT * FROM to_move;
  GET DIAGNOSTICS moved = ROW_COUNT;
  DELETE FROM field_values WHERE updated_at < now() - cutoff;
  RETURN moved;
END; $$ LANGUAGE plpgsql;

-- 2) Activities: soft-archive after 5 years
CREATE TABLE IF NOT EXISTS activities_archive (LIKE activities INCLUDING ALL);

CREATE OR REPLACE FUNCTION archive_old_activities(cutoff interval DEFAULT interval '5 years') RETURNS int AS $$
DECLARE moved int;
BEGIN
  WITH to_move AS (
    SELECT * FROM activities WHERE created_at < now() - cutoff
  )
  INSERT INTO activities_archive SELECT * FROM to_move;
  GET DIAGNOSTICS moved = ROW_COUNT;
  DELETE FROM activities WHERE created_at < now() - cutoff;
  RETURN moved;
END; $$ LANGUAGE plpgsql;

-- 3) event_outbox retention: delete delivered older than 30 days
CREATE OR REPLACE FUNCTION purge_event_outbox(days int DEFAULT 30) RETURNS int AS $$
DECLARE purged int;
BEGIN
  DELETE FROM event_outbox WHERE status='PUBLISHED' AND created_at < now() - (days || ' days')::interval;
  GET DIAGNOSTICS purged = ROW_COUNT;
  RETURN purged;
END; $$ LANGUAGE plpgsql;
