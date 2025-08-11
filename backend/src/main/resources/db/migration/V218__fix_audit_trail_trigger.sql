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

-- Verify partitions exist for current and next month
DO $$
DECLARE
    current_partition TEXT;
    next_partition TEXT;
BEGIN
    -- Generate partition names for current and next month
    current_partition := 'audit_trail_' || to_char(CURRENT_DATE, 'YYYY_MM');
    next_partition := 'audit_trail_' || to_char(CURRENT_DATE + interval '1 month', 'YYYY_MM');
    
    -- Check and create current month partition if needed
    IF NOT EXISTS (
        SELECT 1 FROM pg_tables 
        WHERE schemaname = 'public' 
        AND tablename = current_partition
    ) THEN
        PERFORM create_audit_partition(CURRENT_DATE);
        RAISE NOTICE 'Created partition %', current_partition;
    END IF;
    
    -- Check and create next month partition if needed
    IF NOT EXISTS (
        SELECT 1 FROM pg_tables 
        WHERE schemaname = 'public' 
        AND tablename = next_partition
    ) THEN
        PERFORM create_audit_partition(CURRENT_DATE + interval '1 month');
        RAISE NOTICE 'Created partition %', next_partition;
    END IF;
END $$;

-- Add comment explaining the fix
COMMENT ON TABLE audit_trail IS 'Immutable audit log with partitioning. Timestamp is set via DEFAULT, no trigger needed to avoid partition constraint violations.';