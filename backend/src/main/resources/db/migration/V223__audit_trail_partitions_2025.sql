-- V223__audit_trail_partitions_2025.sql
-- Robust: runs only when audit_trail exists and is partitioned
-- Uses proper identifier quoting and guards
-- Created: 2025-08-16

DO $$
DECLARE
  is_partitioned boolean;
  part_name text;
  start_date date;
  end_date date;
  m int;
BEGIN
  -- Check if audit_trail exists
  IF to_regclass('public.audit_trail') IS NULL THEN
    RAISE NOTICE 'audit_trail does not exist -> skip partition creation';
    RETURN;
  END IF;

  -- Check if audit_trail is partitioned
  SELECT EXISTS(
    SELECT 1 FROM pg_partitioned_table
    WHERE partrelid = 'public.audit_trail'::regclass
  ) INTO is_partitioned;

  IF NOT is_partitioned THEN
    RAISE NOTICE 'audit_trail is not partitioned -> skip partition creation';
    RETURN;
  END IF;

  -- Create default partition to catch all data outside defined ranges
  IF to_regclass('public.audit_trail_default') IS NULL THEN
    EXECUTE 'CREATE TABLE public.audit_trail_default PARTITION OF public.audit_trail DEFAULT';
    RAISE NOTICE 'Created default partition audit_trail_default';
  END IF;

  -- Create monthly partitions for 2025 (using UTC dates)
  FOR m IN 1..12 LOOP
    start_date := make_date(2025, m, 1);
    end_date   := (start_date + INTERVAL '1 month')::date;
    part_name  := format('audit_trail_y%sm%02s', 2025, m);

    IF to_regclass('public.'||part_name) IS NULL THEN
      EXECUTE format(
        'CREATE TABLE %I PARTITION OF public.audit_trail
         FOR VALUES FROM (%L) TO (%L)',
        part_name, start_date, end_date
      );
      RAISE NOTICE 'Created partition %', part_name;
    END IF;
  END LOOP;
END $$;