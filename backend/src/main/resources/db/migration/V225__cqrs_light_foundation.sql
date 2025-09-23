-- V225: CQRS Light Foundation - PostgreSQL LISTEN/NOTIFY Event System
-- Purpose: Foundation für alle 8 Business-Module mit Event-Driven Architecture
-- Performance Target: <200ms P95 für Event-Publishing
-- Scale: 5-50 interne Benutzer

-- =====================================================
-- 0. EXTENSIONS CHECK
-- =====================================================
-- Ensure UUID generation is available
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- =====================================================
-- 1. EVENT STORE TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS domain_events (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    event_type VARCHAR(100) NOT NULL,
    aggregate_id UUID NOT NULL,
    aggregate_type VARCHAR(50) NOT NULL,
    payload JSONB NOT NULL,
    metadata JSONB DEFAULT '{}',
    user_id VARCHAR(255),
    correlation_id UUID,
    causation_id UUID,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    -- published flag removed: LISTEN/NOTIFY is synchronous, no polling needed

    -- Performance-Indizes
    CONSTRAINT chk_event_type CHECK (event_type ~ '^[a-z]+\.[a-z]+$'),
    CONSTRAINT chk_payload_size CHECK (pg_column_size(payload) < 7900) -- 7.9KB limit für PostgreSQL NOTIFY (max 8KB)
);

-- Indizes für Query-Performance
CREATE INDEX idx_domain_events_aggregate ON domain_events(aggregate_type, aggregate_id);
CREATE INDEX idx_domain_events_created ON domain_events(created_at DESC);
-- unpublished index removed: not needed with synchronous LISTEN/NOTIFY
CREATE INDEX idx_domain_events_correlation ON domain_events(correlation_id) WHERE correlation_id IS NOT NULL;

-- Idempotenz-Schutz: Verhindert doppelte Events bei Retries
CREATE UNIQUE INDEX idx_domain_events_causation_unique
    ON domain_events(causation_id)
    WHERE causation_id IS NOT NULL;

-- =====================================================
-- 2. COMMAND HANDLERS REGISTRY
-- =====================================================
CREATE TABLE IF NOT EXISTS command_handlers (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    command_type VARCHAR(100) NOT NULL UNIQUE,
    handler_class VARCHAR(255) NOT NULL,
    aggregate_type VARCHAR(50) NOT NULL,
    enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Sample Command Handlers für Module
INSERT INTO command_handlers (command_type, handler_class, aggregate_type) VALUES
    ('CreateLeadCommand', 'de.freshplan.modules.lead.commands.CreateLeadHandler', 'Lead'),
    ('UpdateCustomerCommand', 'de.freshplan.modules.customer.commands.UpdateCustomerHandler', 'Customer'),
    ('SendCommunicationCommand', 'de.freshplan.modules.communication.commands.SendCommunicationHandler', 'Communication')
ON CONFLICT (command_type) DO NOTHING;

-- =====================================================
-- 3. QUERY PROJECTIONS REGISTRY
-- =====================================================
CREATE TABLE IF NOT EXISTS query_projections (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    projection_name VARCHAR(100) NOT NULL UNIQUE,
    event_types TEXT[] NOT NULL,
    handler_class VARCHAR(255) NOT NULL,
    last_processed_event_id UUID,
    last_processed_at TIMESTAMP WITH TIME ZONE,
    enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Sample Query Projections für Module
INSERT INTO query_projections (projection_name, event_types, handler_class) VALUES
    ('LeadListProjection', ARRAY['lead.created', 'lead.updated', 'lead.assigned'], 'de.freshplan.modules.lead.projections.LeadListProjection'),
    ('CustomerOverviewProjection', ARRAY['customer.created', 'customer.updated'], 'de.freshplan.modules.customer.projections.CustomerOverviewProjection'),
    ('CockpitDashboardProjection', ARRAY['lead.created', 'customer.created', 'communication.sent'], 'de.freshplan.modules.cockpit.projections.DashboardProjection')
ON CONFLICT (projection_name) DO NOTHING;

-- =====================================================
-- 4. LISTEN/NOTIFY TRIGGER FUNCTION
-- =====================================================
CREATE OR REPLACE FUNCTION notify_domain_event()
RETURNS TRIGGER AS $$
DECLARE
    channel_name TEXT;
    event_payload TEXT;
    payload_size INTEGER;
BEGIN
    -- Channel basierend auf aggregate_type
    channel_name := 'cqrs_' || LOWER(NEW.aggregate_type);

    -- Kompaktes JSON für LISTEN/NOTIFY
    event_payload := json_build_object(
        'id', NEW.id,
        'event_type', NEW.event_type,
        'aggregate_id', NEW.aggregate_id,
        'payload', NEW.payload,
        'created_at', NEW.created_at,
        'user_id', NEW.user_id,
        'correlation_id', NEW.correlation_id
    )::text;

    -- Check payload size (PostgreSQL NOTIFY limit is 8000 bytes)
    payload_size := octet_length(event_payload);
    IF payload_size > 7900 THEN
        -- If payload too large, send only pointer
        RAISE WARNING 'Event payload too large (% bytes), sending pointer only', payload_size;
        event_payload := json_build_object(
            'id', NEW.id,
            'event_type', NEW.event_type,
            'aggregate_id', NEW.aggregate_id,
            'aggregate_type', NEW.aggregate_type,
            'created_at', NEW.created_at,
            'large_payload', true
        )::text;
    END IF;

    -- Notify auf spezifischem Channel
    PERFORM pg_notify(channel_name, event_payload);

    -- Auch auf globalem Channel für Dashboard/Monitoring
    PERFORM pg_notify('cqrs_all_events', event_payload);

    -- No published flag needed with synchronous LISTEN/NOTIFY

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger für automatisches Event-Publishing
CREATE TRIGGER trigger_notify_domain_event
    AFTER INSERT ON domain_events
    FOR EACH ROW
    EXECUTE FUNCTION notify_domain_event();

-- =====================================================
-- 5. EVENT STORE HELPER FUNCTIONS
-- =====================================================

-- Funktion zum Publizieren von Events
CREATE OR REPLACE FUNCTION publish_event(
    p_event_type VARCHAR,
    p_aggregate_id UUID,
    p_aggregate_type VARCHAR,
    p_payload JSONB,
    p_user_id VARCHAR DEFAULT NULL,
    p_correlation_id UUID DEFAULT NULL
) RETURNS UUID AS $$
DECLARE
    v_event_id UUID;
BEGIN
    INSERT INTO domain_events (
        event_type,
        aggregate_id,
        aggregate_type,
        payload,
        user_id,
        correlation_id
    ) VALUES (
        p_event_type,
        p_aggregate_id,
        p_aggregate_type,
        p_payload,
        p_user_id,
        p_correlation_id
    ) RETURNING id INTO v_event_id;

    RETURN v_event_id;
END;
$$ LANGUAGE plpgsql;

-- Funktion für Event-Replay (für Projektionen)
CREATE OR REPLACE FUNCTION replay_events_for_projection(
    p_projection_name VARCHAR,
    p_from_timestamp TIMESTAMP WITH TIME ZONE DEFAULT NULL
) RETURNS TABLE(
    event_id UUID,
    event_type VARCHAR,
    aggregate_id UUID,
    payload JSONB,
    created_at TIMESTAMP WITH TIME ZONE
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        de.id,
        de.event_type,
        de.aggregate_id,
        de.payload,
        de.created_at
    FROM domain_events de
    INNER JOIN query_projections qp ON de.event_type = ANY(qp.event_types)
    WHERE qp.projection_name = p_projection_name
        AND (p_from_timestamp IS NULL OR de.created_at > p_from_timestamp)
        AND qp.enabled = TRUE
    ORDER BY de.created_at ASC;
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- 6. MONITORING & HEALTH CHECKS
-- =====================================================
CREATE TABLE IF NOT EXISTS cqrs_health_metrics (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    metric_type VARCHAR(50) NOT NULL,
    metric_value NUMERIC NOT NULL,
    measured_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    metadata JSONB DEFAULT '{}'
);

-- Index für Performance-Queries
CREATE INDEX idx_health_metrics_type_time ON cqrs_health_metrics(metric_type, measured_at DESC);

-- Health-Check Function
CREATE OR REPLACE FUNCTION check_cqrs_health()
RETURNS TABLE(
    check_name VARCHAR,
    status VARCHAR,
    details JSONB
) AS $$
BEGIN
    -- Check 1: Recent Events Count
    RETURN QUERY
    SELECT
        'recent_events'::VARCHAR,
        CASE
            WHEN COUNT(*) > 0 THEN 'healthy'::VARCHAR
            ELSE 'warning'::VARCHAR
        END,
        json_build_object('count', COUNT(*))::JSONB
    FROM domain_events
    WHERE created_at > NOW() - INTERVAL '5 minutes';

    -- Check 2: Event Publishing Latency
    RETURN QUERY
    SELECT
        'event_latency'::VARCHAR,
        CASE
            WHEN AVG(EXTRACT(EPOCH FROM (NOW() - created_at))) < 0.2 THEN 'healthy'::VARCHAR
            WHEN AVG(EXTRACT(EPOCH FROM (NOW() - created_at))) < 1 THEN 'warning'::VARCHAR
            ELSE 'critical'::VARCHAR
        END,
        json_build_object(
            'avg_latency_seconds', ROUND(AVG(EXTRACT(EPOCH FROM (NOW() - created_at)))::NUMERIC, 3)
        )::JSONB
    FROM domain_events
    WHERE created_at > NOW() - INTERVAL '1 minute';

    -- Check 3: Projection Lag
    RETURN QUERY
    SELECT
        'projection_lag'::VARCHAR,
        CASE
            WHEN MAX(NOW() - COALESCE(last_processed_at, created_at)) < INTERVAL '1 minute' THEN 'healthy'::VARCHAR
            WHEN MAX(NOW() - COALESCE(last_processed_at, created_at)) < INTERVAL '5 minutes' THEN 'warning'::VARCHAR
            ELSE 'critical'::VARCHAR
        END,
        json_build_object(
            'max_lag_minutes', ROUND(EXTRACT(EPOCH FROM MAX(NOW() - COALESCE(last_processed_at, created_at))) / 60, 2)
        )::JSONB
    FROM query_projections
    WHERE enabled = TRUE;
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- 7. SAMPLE TEST DATA (Dev-Profile only)
-- =====================================================
-- Nur für Entwicklung - wird durch Dev-Seeds ersetzt
DO $$
BEGIN
    -- Nur wenn keine Events existieren (erste Installation)
    IF NOT EXISTS (SELECT 1 FROM domain_events LIMIT 1) THEN
        -- Test Lead Created Event
        PERFORM publish_event(
            'lead.created',
            gen_random_uuid(),
            'Lead',
            '{"name": "Test Restaurant GmbH", "email": "test@restaurant.de", "territory": "DE"}'::JSONB,
            'system-init'
        );

        -- Test Customer Created Event
        PERFORM publish_event(
            'customer.created',
            gen_random_uuid(),
            'Customer',
            '{"name": "Premium Hotel AG", "contract_value": 50000, "territory": "CH"}'::JSONB,
            'system-init'
        );

        RAISE NOTICE 'CQRS Light Foundation: Test events created for development';
    END IF;
END $$;

-- =====================================================
-- 8. PERMISSIONS (für Application User)
-- =====================================================
-- Assuming application user 'freshplan_app' exists
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'freshplan_app') THEN
        GRANT SELECT, INSERT, UPDATE ON domain_events TO freshplan_app;
        GRANT SELECT ON command_handlers TO freshplan_app;
        GRANT SELECT, UPDATE ON query_projections TO freshplan_app;
        GRANT SELECT, INSERT ON cqrs_health_metrics TO freshplan_app;
        GRANT EXECUTE ON FUNCTION publish_event TO freshplan_app;
        GRANT EXECUTE ON FUNCTION replay_events_for_projection TO freshplan_app;
        GRANT EXECUTE ON FUNCTION check_cqrs_health TO freshplan_app;
    END IF;
END $$;

-- =====================================================
-- Success Message
-- =====================================================
DO $$
BEGIN
    RAISE NOTICE '✅ CQRS Light Foundation successfully installed!';
    RAISE NOTICE '   - Event Store: domain_events table ready';
    RAISE NOTICE '   - LISTEN/NOTIFY: Automatic event publishing configured';
    RAISE NOTICE '   - Command/Query: Handler registries created';
    RAISE NOTICE '   - Health Checks: Monitoring functions available';
    RAISE NOTICE '   - Performance Target: <200ms P95 for 5-50 users';
END $$;