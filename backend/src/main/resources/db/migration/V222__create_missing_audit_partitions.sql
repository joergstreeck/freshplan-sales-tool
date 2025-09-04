-- V222: Create missing audit_trail partitions for current and future months
-- Purpose: Ensure partitions exist for August 2025 and beyond
-- Created: 2025-08-16

-- Create partitions only if audit_trail is a partitioned table
DO $$
BEGIN
    -- Check if the table is partitioned
    IF EXISTS (
        SELECT 1 
        FROM pg_class c
        JOIN pg_namespace n ON n.oid = c.relnamespace
        WHERE c.relname = 'audit_trail' 
        AND n.nspname = 'public'
        AND c.relkind = 'p'  -- 'p' for partitioned table
    ) THEN
        -- Table is partitioned, create partitions
        
        -- Create August 2025 partition
        IF NOT EXISTS (
            SELECT 1 FROM pg_tables 
            WHERE schemaname = 'public' 
            AND tablename = 'audit_trail_2025_08'
        ) THEN
            EXECUTE 'CREATE TABLE audit_trail_2025_08 PARTITION OF audit_trail FOR VALUES FROM (''2025-08-01'') TO (''2025-09-01'')';
            RAISE NOTICE 'Created partition audit_trail_2025_08';
        END IF;
        
        -- Create September 2025 partition
        IF NOT EXISTS (
            SELECT 1 FROM pg_tables 
            WHERE schemaname = 'public' 
            AND tablename = 'audit_trail_2025_09'
        ) THEN
            EXECUTE 'CREATE TABLE audit_trail_2025_09 PARTITION OF audit_trail FOR VALUES FROM (''2025-09-01'') TO (''2025-10-01'')';
            RAISE NOTICE 'Created partition audit_trail_2025_09';
        END IF;
        
        -- Create October 2025 partition
        IF NOT EXISTS (
            SELECT 1 FROM pg_tables 
            WHERE schemaname = 'public' 
            AND tablename = 'audit_trail_2025_10'
        ) THEN
            EXECUTE 'CREATE TABLE audit_trail_2025_10 PARTITION OF audit_trail FOR VALUES FROM (''2025-10-01'') TO (''2025-11-01'')';
            RAISE NOTICE 'Created partition audit_trail_2025_10';
        END IF;
        
        -- Create November 2025 partition
        IF NOT EXISTS (
            SELECT 1 FROM pg_tables 
            WHERE schemaname = 'public' 
            AND tablename = 'audit_trail_2025_11'
        ) THEN
            EXECUTE 'CREATE TABLE audit_trail_2025_11 PARTITION OF audit_trail FOR VALUES FROM (''2025-11-01'') TO (''2025-12-01'')';
            RAISE NOTICE 'Created partition audit_trail_2025_11';
        END IF;
        
        -- Create December 2025 partition
        IF NOT EXISTS (
            SELECT 1 FROM pg_tables 
            WHERE schemaname = 'public' 
            AND tablename = 'audit_trail_2025_12'
        ) THEN
            EXECUTE 'CREATE TABLE audit_trail_2025_12 PARTITION OF audit_trail FOR VALUES FROM (''2025-12-01'') TO (''2026-01-01'')';
            RAISE NOTICE 'Created partition audit_trail_2025_12';
        END IF;
        
        -- Create January 2026 partition (for year transition)
        IF NOT EXISTS (
            SELECT 1 FROM pg_tables 
            WHERE schemaname = 'public' 
            AND tablename = 'audit_trail_2026_01'
        ) THEN
            EXECUTE 'CREATE TABLE audit_trail_2026_01 PARTITION OF audit_trail FOR VALUES FROM (''2026-01-01'') TO (''2026-02-01'')';
            RAISE NOTICE 'Created partition audit_trail_2026_01';
        END IF;
        
    ELSE
        RAISE NOTICE 'audit_trail is not a partitioned table, skipping partition creation';
    END IF;
END;
$$;

-- Also update or create the function to ensure partitions exist
CREATE OR REPLACE FUNCTION ensure_audit_partition_exists()
RETURNS void AS $$
DECLARE
    partition_name text;
    start_date date;
    end_date date;
    is_partitioned boolean;
BEGIN
    -- Check if table is partitioned
    SELECT EXISTS (
        SELECT 1 
        FROM pg_class c
        JOIN pg_namespace n ON n.oid = c.relnamespace
        WHERE c.relname = 'audit_trail' 
        AND n.nspname = 'public'
        AND c.relkind = 'p'
    ) INTO is_partitioned;
    
    IF NOT is_partitioned THEN
        -- Table is not partitioned, nothing to do
        RETURN;
    END IF;
    
    -- Get current month partition
    partition_name := 'audit_trail_' || to_char(CURRENT_DATE, 'YYYY_MM');
    start_date := date_trunc('month', CURRENT_DATE);
    end_date := start_date + interval '1 month';
    
    -- Create current month partition if not exists
    IF NOT EXISTS (
        SELECT 1 FROM pg_tables 
        WHERE schemaname = 'public' 
        AND tablename = partition_name
    ) THEN
        EXECUTE format(
            'CREATE TABLE IF NOT EXISTS %I PARTITION OF audit_trail FOR VALUES FROM (%L) TO (%L)',
            partition_name,
            start_date,
            end_date
        );
        RAISE NOTICE 'Created audit partition %', partition_name;
    END IF;
    
    -- Also create next month's partition proactively
    partition_name := 'audit_trail_' || to_char(CURRENT_DATE + interval '1 month', 'YYYY_MM');
    start_date := date_trunc('month', CURRENT_DATE + interval '1 month');
    end_date := start_date + interval '1 month';
    
    IF NOT EXISTS (
        SELECT 1 FROM pg_tables 
        WHERE schemaname = 'public' 
        AND tablename = partition_name
    ) THEN
        EXECUTE format(
            'CREATE TABLE IF NOT EXISTS %I PARTITION OF audit_trail FOR VALUES FROM (%L) TO (%L)',
            partition_name,
            start_date,
            end_date
        );
        RAISE NOTICE 'Created audit partition %', partition_name;
    END IF;
END;
$$ LANGUAGE plpgsql;

-- Call function immediately to ensure current partitions exist
SELECT ensure_audit_partition_exists();