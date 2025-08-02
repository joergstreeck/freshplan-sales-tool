-- V108__fix_audit_trail_value_columns_type.sql
-- Fix column types for old_value and new_value to match Entity definition
-- Author: Claude Code
-- Date: 2025-07-25

-- Step 1: Drop dependent views and materialized views (like in V107)
DROP VIEW IF EXISTS recent_audit_entries;
DROP MATERIALIZED VIEW IF EXISTS audit_statistics;

-- Step 2: Change old_value and new_value from JSONB to TEXT for Hibernate compatibility
-- Convert JSONB to TEXT representation 
ALTER TABLE audit_trail 
ALTER COLUMN old_value TYPE TEXT USING old_value::text;

ALTER TABLE audit_trail 
ALTER COLUMN new_value TYPE TEXT USING new_value::text;

-- Step 3: Recreate the views with updated column types

-- Create view for easy access to recent audit entries
CREATE VIEW recent_audit_entries AS
SELECT * FROM audit_trail
WHERE timestamp > CURRENT_TIMESTAMP - INTERVAL '7 days'
ORDER BY timestamp DESC;

-- Create materialized view for audit statistics
CREATE MATERIALIZED VIEW audit_statistics AS
SELECT 
    date_trunc('day', timestamp) as day,
    event_type,
    entity_type,
    COUNT(*) as event_count,
    COUNT(DISTINCT user_id) as unique_users
FROM audit_trail
WHERE timestamp > CURRENT_TIMESTAMP - INTERVAL '30 days'
GROUP BY date_trunc('day', timestamp), event_type, entity_type;

-- Create index on materialized view
CREATE INDEX idx_audit_stats_day ON audit_statistics(day DESC);

-- Update comments to reflect new column types
COMMENT ON COLUMN audit_trail.old_value IS 'Previous value as TEXT (for Hibernate compatibility)';
COMMENT ON COLUMN audit_trail.new_value IS 'New value as TEXT (for Hibernate compatibility)';