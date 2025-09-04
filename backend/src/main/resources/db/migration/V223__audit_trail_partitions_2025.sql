-- V223__audit_trail_partitions_2025.sql
-- Robust: runs only when audit_trail exists and is partitioned
-- Fixed: Unified naming with V9, range checking, overlap prevention
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

  -- Create monthly partitions for 2025 with unified naming scheme
  FOR m IN 1..12 LOOP
    start_date := make_date(2025, m, 1);
    end_date   := (start_date + INTERVAL '1 month')::date;

    -- Unified naming scheme like V9: audit_trail_YYYY_MM
    part_name  := format('audit_trail_%s_%s', 2025, to_char(m, 'FM00'));

    -- 1) Skip if partition with this name already exists
    IF to_regclass(format('%I.%I', 'public', part_name)) IS NOT NULL THEN
      RAISE NOTICE 'Partition % already exists by name -> skip', part_name;
      CONTINUE;
    END IF;

    -- 2) Skip if a partition with the same range already exists (regardless of name)
    IF EXISTS (
      SELECT 1
      FROM pg_inherits i
      JOIN pg_class c ON c.oid = i.inhrelid
      WHERE i.inhparent = 'public.audit_trail'::regclass
        AND pg_get_expr(c.relpartbound, c.oid)
            = format('FOR VALUES FROM (%L) TO (%L)', start_date, end_date)
    ) THEN
      RAISE NOTICE 'A partition for % to % already exists (by range) -> skip', start_date, end_date;
      CONTINUE;
    END IF;

    -- 3) Create partition - defensively protected against race/overlap
    BEGIN
      EXECUTE format(
        'CREATE TABLE %I PARTITION OF public.audit_trail
         FOR VALUES FROM (%L) TO (%L)',
        part_name, start_date, end_date
      );
      RAISE NOTICE 'Created partition %', part_name;

    EXCEPTION
      WHEN OTHERS THEN
        -- If a identical/overlapping partition was created concurrently, skip cleanly
        IF position('would overlap partition' in SQLERRM) > 0
           OR position('already exists' in SQLERRM) > 0 THEN
          RAISE NOTICE 'Partition for %..% already present (concurrent), skipping', start_date, end_date;
        ELSE
          RAISE; -- Re-raise real errors
        END IF;
    END;
  END LOOP;
END $$ LANGUAGE plpgsql;