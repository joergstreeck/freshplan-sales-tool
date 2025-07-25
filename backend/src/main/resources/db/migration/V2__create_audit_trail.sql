-- V2__create_audit_trail.sql
-- Enterprise-grade Audit Trail Table with partitioning support
-- Author: FreshPlan Team
-- Date: 2025-07-25

-- Create audit trail table
CREATE TABLE audit_trail (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    timestamp TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    event_type VARCHAR(50) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id UUID NOT NULL,
    
    -- User Information
    user_id UUID NOT NULL,
    user_name VARCHAR(255) NOT NULL,
    user_role VARCHAR(50) NOT NULL,
    
    -- Change Details (JSONB for flexibility)
    old_value JSONB,
    new_value JSONB,
    change_reason VARCHAR(500),
    user_comment TEXT,
    
    -- Context
    ip_address INET,
    user_agent TEXT,
    session_id UUID,
    
    -- Source
    source VARCHAR(20) NOT NULL,
    api_endpoint TEXT,
    request_id UUID,
    
    -- Integrity
    data_hash VARCHAR(64) NOT NULL,
    previous_hash VARCHAR(64),
    schema_version INTEGER NOT NULL DEFAULT 1,
    
    -- Constraints
    CONSTRAINT audit_trail_source_check CHECK (source IN ('UI', 'API', 'SYSTEM', 'WEBHOOK', 'BATCH', 'MOBILE', 'CLI', 'INTEGRATION', 'TEST'))
);

-- Create indexes for performance
CREATE INDEX idx_audit_entity ON audit_trail(entity_type, entity_id, timestamp DESC);
CREATE INDEX idx_audit_user ON audit_trail(user_id, timestamp DESC);
CREATE INDEX idx_audit_type ON audit_trail(event_type, timestamp DESC);
CREATE INDEX idx_audit_timestamp ON audit_trail(timestamp DESC);
CREATE INDEX idx_audit_security ON audit_trail(event_type, timestamp DESC) 
    WHERE event_type LIKE '%PERMISSION%' 
       OR event_type LIKE '%ROLE%' 
       OR event_type LIKE '%LOGIN%' 
       OR event_type LIKE '%SECURITY%';

-- Enable row-level security
ALTER TABLE audit_trail ENABLE ROW LEVEL SECURITY;

-- Create policy for read access (admins and auditors only)
CREATE POLICY audit_trail_read_policy ON audit_trail
    FOR SELECT
    USING (
        EXISTS (
            SELECT 1 FROM user_roles ur
            WHERE ur.user_id = current_setting('app.current_user_id')::UUID
            AND ur.role_name IN ('admin', 'auditor', 'security')
        )
    );

-- Prevent any updates or deletes (audit trail is immutable)
CREATE POLICY audit_trail_no_update ON audit_trail
    FOR UPDATE
    USING (false);

CREATE POLICY audit_trail_no_delete ON audit_trail
    FOR DELETE
    USING (false);

-- Function to automatically set timestamp
CREATE OR REPLACE FUNCTION audit_trail_set_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.timestamp = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger to enforce timestamp
CREATE TRIGGER audit_trail_timestamp_trigger
    BEFORE INSERT ON audit_trail
    FOR EACH ROW
    EXECUTE FUNCTION audit_trail_set_timestamp();

-- Partitioning setup for scalability (monthly partitions)
-- Note: Actual partitions should be created by a maintenance job

-- Example partition for current month
CREATE TABLE audit_trail_2025_07 PARTITION OF audit_trail
    FOR VALUES FROM ('2025-07-01') TO ('2025-08-01');

-- Function to create monthly partitions automatically
CREATE OR REPLACE FUNCTION create_audit_partition(partition_date DATE)
RETURNS void AS $$
DECLARE
    partition_name TEXT;
    start_date DATE;
    end_date DATE;
BEGIN
    partition_name := 'audit_trail_' || to_char(partition_date, 'YYYY_MM');
    start_date := date_trunc('month', partition_date);
    end_date := start_date + interval '1 month';
    
    -- Check if partition exists
    IF NOT EXISTS (
        SELECT 1 FROM pg_tables 
        WHERE tablename = partition_name
    ) THEN
        EXECUTE format(
            'CREATE TABLE %I PARTITION OF audit_trail FOR VALUES FROM (%L) TO (%L)',
            partition_name,
            start_date,
            end_date
        );
        
        RAISE NOTICE 'Created partition: %', partition_name;
    END IF;
END;
$$ LANGUAGE plpgsql;

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

-- Refresh materialized view daily (should be scheduled externally)
-- REFRESH MATERIALIZED VIEW CONCURRENTLY audit_statistics;

-- Grant permissions
GRANT SELECT ON audit_trail TO freshplan_readonly;
GRANT SELECT ON recent_audit_entries TO freshplan_readonly;
GRANT SELECT ON audit_statistics TO freshplan_readonly;

-- Comments for documentation
COMMENT ON TABLE audit_trail IS 'Immutable audit log for compliance and security tracking';
COMMENT ON COLUMN audit_trail.data_hash IS 'SHA-256 hash of entry data for integrity verification';
COMMENT ON COLUMN audit_trail.previous_hash IS 'Hash of previous entry for blockchain-style chaining';
COMMENT ON COLUMN audit_trail.schema_version IS 'Schema version for future migrations';