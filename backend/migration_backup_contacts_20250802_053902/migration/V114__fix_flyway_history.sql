-- Fix Flyway history after branch switching
-- Remove references to migrations V110-V112 that were applied on another branch
DO $$
BEGIN
    -- Only delete if entries exist
    IF EXISTS (SELECT 1 FROM flyway_schema_history WHERE version IN ('110', '111', '112')) THEN
        DELETE FROM flyway_schema_history WHERE version IN ('110', '111', '112');
        RAISE NOTICE 'Removed orphaned migration entries V110-V112 from flyway_schema_history';
    ELSE
        RAISE NOTICE 'No orphaned migration entries found';
    END IF;
END $$;