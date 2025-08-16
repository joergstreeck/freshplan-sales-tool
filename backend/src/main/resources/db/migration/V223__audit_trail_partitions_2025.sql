-- V223: Legt Monats-Partitionen für 2025 an (idempotent) und ein Default-Fallback
-- Purpose: Ensure all 2025 months have partitions + catch-all default partition
-- Created: 2025-08-16

DO $$
DECLARE
  m int;
  start_date date;
  end_date   date;
  part_name  text;
BEGIN
  -- Only proceed if audit_trail is a partitioned table
  IF EXISTS (
    SELECT 1 
    FROM pg_class c
    JOIN pg_namespace n ON n.oid = c.relnamespace
    WHERE c.relname = 'audit_trail' 
    AND n.nspname = 'public'
    AND c.relkind = 'p'  -- 'p' for partitioned table
  ) THEN
    
    -- Default-Partition (nur eine erlaubt) - fängt alle Daten außerhalb definierter Ranges
    IF to_regclass('public.audit_trail_default') IS NULL THEN
      EXECUTE 'CREATE TABLE audit_trail_default PARTITION OF audit_trail DEFAULT';
      RAISE NOTICE 'Created default partition for audit_trail';
    END IF;

    -- Monats-Partitionen für 2025
    FOR m IN 1..12 LOOP
      start_date := make_date(2025, m, 1);
      end_date   := (start_date + INTERVAL '1 month')::date;
      part_name  := format('audit_trail_y%sm%02s', 2025, m);

      IF to_regclass('public.'||part_name) IS NULL THEN
        EXECUTE format($f$
          CREATE TABLE %I PARTITION OF audit_trail
          FOR VALUES FROM (%L) TO (%L)
        $f$, part_name, start_date, end_date);
        RAISE NOTICE 'Created partition % for audit_trail', part_name;
      END IF;
    END LOOP;
    
    -- Zusätzlich 2026 Q1 für Jahreswechsel
    FOR m IN 1..3 LOOP
      start_date := make_date(2026, m, 1);
      end_date   := (start_date + INTERVAL '1 month')::date;
      part_name  := format('audit_trail_y%sm%02s', 2026, m);

      IF to_regclass('public.'||part_name) IS NULL THEN
        EXECUTE format($f$
          CREATE TABLE %I PARTITION OF audit_trail
          FOR VALUES FROM (%L) TO (%L)
        $f$, part_name, start_date, end_date);
        RAISE NOTICE 'Created partition % for audit_trail', part_name;
      END IF;
    END LOOP;
    
  ELSE
    RAISE NOTICE 'audit_trail is not a partitioned table, skipping partition creation';
  END IF;
END;
$$;