-- Fix audit trail trigger that causes partition constraint violations
-- The trigger was overwriting timestamps which can cause rows to move between partitions
-- This is not allowed in PostgreSQL and causes the error:
-- "moving row to another partition during a BEFORE trigger is not supported"

-- Drop the problematic trigger that overwrites timestamps
DROP TRIGGER IF EXISTS audit_trail_timestamp_trigger ON audit_trail;

-- Drop the function as it's no longer needed
DROP FUNCTION IF EXISTS audit_trail_set_timestamp();

-- The timestamp is already set correctly via DEFAULT CURRENT_TIMESTAMP in the table definition
-- No replacement trigger is needed

-- Only manage partitions if the table is actually partitioned
DO $$
DECLARE
    is_partitioned BOOLEAN;
    current_partition TEXT;
    next_partition TEXT;
BEGIN
    -- Check if audit_trail is partitioned
    SELECT EXISTS (
        SELECT 1 
        FROM pg_class c
        JOIN pg_namespace n ON n.oid = c.relnamespace
        WHERE c.relname = 'audit_trail'
        AND n.nspname = 'public'
        AND c.relkind = 'p'  -- 'p' means partitioned table
    ) INTO is_partitioned;
    
    IF is_partitioned THEN
        -- Generate partition names for current and next month
        current_partition := 'audit_trail_' || to_char(CURRENT_DATE, 'YYYY_MM');
        next_partition := 'audit_trail_' || to_char(CURRENT_DATE + interval '1 month', 'YYYY_MM');
        
        -- Check and create current month partition if needed
        IF NOT EXISTS (
            SELECT 1 FROM pg_tables 
            WHERE schemaname = 'public' 
            AND tablename = current_partition
        ) THEN
            -- Check if the function exists before calling it
            IF EXISTS (
                SELECT 1 FROM pg_proc p
                JOIN pg_namespace n ON n.oid = p.pronamespace
                WHERE p.proname = 'create_audit_partition'
                AND n.nspname = 'public'
            ) THEN
                PERFORM create_audit_partition(CURRENT_DATE);
                RAISE NOTICE 'Created partition %', current_partition;
            ELSE
                -- Manually create partition if function doesn't exist
                EXECUTE format(
                    'CREATE TABLE IF NOT EXISTS %I PARTITION OF audit_trail FOR VALUES FROM (%L) TO (%L)',
                    current_partition,
                    date_trunc('month', CURRENT_DATE),
                    date_trunc('month', CURRENT_DATE) + interval '1 month'
                );
                RAISE NOTICE 'Created partition % (manual)', current_partition;
            END IF;
        END IF;
        
        -- Check and create next month partition if needed
        IF NOT EXISTS (
            SELECT 1 FROM pg_tables 
            WHERE schemaname = 'public' 
            AND tablename = next_partition
        ) THEN
            -- Check if the function exists before calling it
            IF EXISTS (
                SELECT 1 FROM pg_proc p
                JOIN pg_namespace n ON n.oid = p.pronamespace
                WHERE p.proname = 'create_audit_partition'
                AND n.nspname = 'public'
            ) THEN
                PERFORM create_audit_partition(CURRENT_DATE + interval '1 month');
                RAISE NOTICE 'Created partition %', next_partition;
            ELSE
                -- Manually create partition if function doesn't exist
                EXECUTE format(
                    'CREATE TABLE IF NOT EXISTS %I PARTITION OF audit_trail FOR VALUES FROM (%L) TO (%L)',
                    next_partition,
                    date_trunc('month', CURRENT_DATE + interval '1 month'),
                    date_trunc('month', CURRENT_DATE + interval '1 month') + interval '1 month'
                );
                RAISE NOTICE 'Created partition % (manual)', next_partition;
            END IF;
        END IF;
    ELSE
        RAISE NOTICE 'audit_trail is not partitioned, skipping partition management';
    END IF;
END $$;

-- Add comment explaining the fix
COMMENT ON TABLE audit_trail IS 'Immutable audit log with partitioning. Timestamp is set via DEFAULT, no trigger needed to avoid partition constraint violations.';