-- V107__fix_audit_trail_ip_address_type.sql
-- Fix ip_address column type compatibility for Hibernate
-- Author: FreshPlan Team
-- Date: 2025-07-25

-- Step 1: Drop dependent views and materialized views
DROP VIEW IF EXISTS recent_audit_entries;
DROP MATERIALIZED VIEW IF EXISTS audit_statistics;

-- Step 2: Change ip_address from INET to VARCHAR(45) to support both IPv4 and IPv6
-- IPv4: xxx.xxx.xxx.xxx (15 chars max)
-- IPv6: xxxx:xxxx:xxxx:xxxx:xxxx:xxxx:xxxx:xxxx (39 chars max)
-- 45 chars provides safe buffer for any IP format
ALTER TABLE audit_trail 
ALTER COLUMN ip_address TYPE VARCHAR(45) USING ip_address::text;

-- Step 3: Recreate the views
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

-- Update comment to reflect the change
COMMENT ON COLUMN audit_trail.ip_address IS 'Client IP address (IPv4 or IPv6) - stored as string for Hibernate compatibility';