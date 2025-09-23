-- V226: Fix event_type constraint to allow multi-level namespacing
-- Issue: Dev seeds use patterns like "customer.contact.added" which were rejected
-- Fix: Allow more flexible event type patterns with numbers, hyphens, underscores

-- Drop the old constraint
ALTER TABLE domain_events DROP CONSTRAINT IF EXISTS chk_event_type;

-- Add new, more flexible constraint
-- Allows: module.submodule.action patterns
-- Examples: customer.contact.added, lead.qualification.completed
ALTER TABLE domain_events
ADD CONSTRAINT chk_event_type
CHECK (event_type ~ '^[a-z][a-z0-9_-]*(\.[a-z][a-z0-9_-]*)+$');

-- Validate existing data (should pass now)
DO $$
DECLARE
    invalid_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO invalid_count
    FROM domain_events
    WHERE event_type !~ '^[a-z][a-z0-9_-]*(\.[a-z][a-z0-9_-]*)+$';

    IF invalid_count > 0 THEN
        RAISE EXCEPTION 'Found % events with invalid event_type format', invalid_count;
    END IF;
END $$;