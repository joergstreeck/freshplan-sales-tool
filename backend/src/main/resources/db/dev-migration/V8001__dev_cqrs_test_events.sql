-- V8001: Dev Seeds for CQRS Light Testing
-- Purpose: Development-only test data for CQRS event patterns
-- Profile: Only loaded in dev profile via application-dev.properties
-- Note: Idempotent - can be run multiple times

-- =====================================================
-- DEV-ONLY CHECK (Safety guard)
-- =====================================================
DO $$
BEGIN
    -- Only execute in dev environments
    IF current_database() NOT LIKE '%dev%' AND
       current_database() NOT LIKE '%local%' AND
       current_database() != 'freshplan' THEN
        RAISE EXCEPTION 'Dev migrations should only run in development databases';
    END IF;
END $$;

-- =====================================================
-- CQRS TEST EVENTS
-- =====================================================

-- Clean up old dev events (idempotent)
DELETE FROM domain_events
WHERE user_id LIKE 'dev-%'
   OR metadata->>'environment' = 'development';

-- Helper function for dev event creation
CREATE OR REPLACE FUNCTION create_dev_event(
    p_event_type VARCHAR,
    p_aggregate_type VARCHAR,
    p_payload JSONB,
    p_delay_seconds INT DEFAULT 0
) RETURNS UUID AS $$
DECLARE
    v_event_id UUID;
    v_aggregate_id UUID := gen_random_uuid();
BEGIN
    INSERT INTO domain_events (
        event_type,
        aggregate_id,
        aggregate_type,
        payload,
        metadata,
        user_id,
        created_at
    ) VALUES (
        p_event_type,
        v_aggregate_id,
        p_aggregate_type,
        p_payload,
        jsonb_build_object('environment', 'development', 'test_data', true),
        'dev-seed-user',
        NOW() - (p_delay_seconds || ' seconds')::INTERVAL
    ) RETURNING id INTO v_event_id;

    RETURN v_event_id;
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- LEAD MANAGEMENT TEST EVENTS
-- =====================================================

-- Lead creation flow
SELECT create_dev_event(
    'lead.created',
    'Lead',
    jsonb_build_object(
        'name', 'Gasthaus Zur Post',
        'email', 'info@gasthaus-post.de',
        'phone', '+49 89 12345',
        'territory', 'DE',
        'source', 'website',
        'assigned_to', 'sales-rep-1'
    ),
    300 -- 5 minutes ago
);

SELECT create_dev_event(
    'lead.qualified',
    'Lead',
    jsonb_build_object(
        'name', 'Gasthaus Zur Post',
        'qualification_score', 85,
        'budget_range', '10000-25000',
        'decision_timeline', '3_months'
    ),
    240 -- 4 minutes ago
);

SELECT create_dev_event(
    'lead.assigned',
    'Lead',
    jsonb_build_object(
        'name', 'Gasthaus Zur Post',
        'previous_owner', 'sales-rep-1',
        'new_owner', 'sales-rep-2',
        'reason', 'Territory reassignment'
    ),
    180 -- 3 minutes ago
);

-- =====================================================
-- CUSTOMER MANAGEMENT TEST EVENTS
-- =====================================================

-- Customer lifecycle events
SELECT create_dev_event(
    'customer.created',
    'Customer',
    jsonb_build_object(
        'name', 'Hotel Sonnenschein GmbH',
        'type', 'HOTEL',
        'territory', 'DE',
        'contract_value', 50000,
        'locations', jsonb_build_array(
            jsonb_build_object('city', 'München', 'type', 'headquarters'),
            jsonb_build_object('city', 'Berlin', 'type', 'branch')
        )
    ),
    600 -- 10 minutes ago
);

SELECT create_dev_event(
    'customer.contact.added',
    'Customer',
    jsonb_build_object(
        'customer_name', 'Hotel Sonnenschein GmbH',
        'contact', jsonb_build_object(
            'name', 'Max Müller',
            'role', 'CHEF',
            'email', 'chef@sonnenschein.de',
            'phone', '+49 89 98765'
        )
    ),
    540 -- 9 minutes ago
);

SELECT create_dev_event(
    'customer.updated',
    'Customer',
    jsonb_build_object(
        'name', 'Hotel Sonnenschein GmbH',
        'changes', jsonb_build_object(
            'contract_value', jsonb_build_object('old', 50000, 'new', 75000),
            'status', jsonb_build_object('old', 'ACTIVE', 'new', 'PREMIUM')
        )
    ),
    60 -- 1 minute ago
);

-- =====================================================
-- COMMUNICATION TEST EVENTS
-- =====================================================

-- Communication workflow events
SELECT create_dev_event(
    'communication.email.sent',
    'Communication',
    jsonb_build_object(
        'recipient', 'chef@restaurant.de',
        'subject', 'Neue Spargel-Spezialitäten',
        'template', 'seasonal_offer',
        'campaign', 'SPARGEL_2025'
    ),
    120 -- 2 minutes ago
);

SELECT create_dev_event(
    'communication.email.opened',
    'Communication',
    jsonb_build_object(
        'recipient', 'chef@restaurant.de',
        'subject', 'Neue Spargel-Spezialitäten',
        'opened_at', NOW() - INTERVAL '30 seconds'
    ),
    30 -- 30 seconds ago
);

-- =====================================================
-- COCKPIT DASHBOARD TEST EVENTS
-- =====================================================

-- Dashboard activity events
SELECT create_dev_event(
    'cockpit.task.created',
    'Cockpit',
    jsonb_build_object(
        'title', 'Follow-up Gasthaus Zur Post',
        'type', 'FOLLOW_UP',
        'priority', 'HIGH',
        'due_date', (NOW() + INTERVAL '2 days')::DATE,
        'assigned_to', 'sales-rep-2'
    ),
    90 -- 1.5 minutes ago
);

SELECT create_dev_event(
    'cockpit.metric.updated',
    'Cockpit',
    jsonb_build_object(
        'metric_type', 'DAILY_REVENUE',
        'value', 15750.50,
        'currency', 'EUR',
        'period', 'DAY',
        'date', CURRENT_DATE
    ),
    15 -- 15 seconds ago
);

-- =====================================================
-- PERFORMANCE TEST EVENTS (Batch)
-- =====================================================

-- Generate 100 events for performance testing
DO $$
DECLARE
    i INT;
    event_types TEXT[] := ARRAY['lead.viewed', 'customer.searched', 'report.generated'];
    territories TEXT[] := ARRAY['DE', 'CH', 'AT'];
BEGIN
    FOR i IN 1..100 LOOP
        PERFORM create_dev_event(
            event_types[1 + (i % 3)],
            'Performance',
            jsonb_build_object(
                'index', i,
                'territory', territories[1 + (i % 3)],
                'test_batch', 'performance_validation',
                'timestamp', NOW()
            ),
            i * 2 -- Spread events over time
        );
    END LOOP;
END $$;

-- =====================================================
-- VALIDATION
-- =====================================================

DO $$
DECLARE
    event_count INT;
BEGIN
    SELECT COUNT(*) INTO event_count
    FROM domain_events
    WHERE metadata->>'environment' = 'development';

    RAISE NOTICE '✅ Dev CQRS Seeds created: % events', event_count;
    RAISE NOTICE '   - Lead events: 3';
    RAISE NOTICE '   - Customer events: 3';
    RAISE NOTICE '   - Communication events: 2';
    RAISE NOTICE '   - Cockpit events: 2';
    RAISE NOTICE '   - Performance test events: 100';
    RAISE NOTICE '   Total: 110 dev events ready for testing';
END $$;

-- Clean up helper function (keeps data)
DROP FUNCTION IF EXISTS create_dev_event;