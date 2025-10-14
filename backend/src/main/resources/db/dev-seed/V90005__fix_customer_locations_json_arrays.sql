-- ============================================================================
-- V90005: Fix CustomerLocation JSON Arrays → Objects
-- ============================================================================
-- Problem: service_offerings field contains JSON arrays '[]' instead of objects '{}'
-- This causes Jackson deserialization errors when ContactMapper tries to load
-- the CustomerLocation entity (expects Map<String, Object>, gets Array)
--
-- Root Cause: V90001 seed data incorrectly initialized service_offerings as []
--
-- Solution: Convert all array values to empty objects
-- ============================================================================

-- Fix all customer_locations with array type in service_offerings
UPDATE customer_locations
SET service_offerings = '{}'::jsonb
WHERE jsonb_typeof(service_offerings) = 'array';

-- Fix all customer_locations with array type in location_details (safety check)
UPDATE customer_locations
SET location_details = '{}'::jsonb
WHERE jsonb_typeof(location_details) = 'array';

-- Verification: Count affected rows
DO $$
DECLARE
    affected_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO affected_count
    FROM customer_locations
    WHERE jsonb_typeof(service_offerings) = 'array'
       OR jsonb_typeof(location_details) = 'array';

    IF affected_count > 0 THEN
        RAISE WARNING 'Found % locations still with array types - migration may have failed', affected_count;
    ELSE
        RAISE NOTICE 'Successfully converted all location JSON arrays to objects';
    END IF;
END $$;
