-- ============================================================================
-- V10029: Fix CustomerLocation JSON Default Values
-- ============================================================================
-- Problem: V206 set DEFAULT '[]'::jsonb (Array) but Java Entity expects '{}'::jsonb (Object)
-- Root Cause: Jackson deserialization fails when loading Map<String, Object> from Array
-- Impact: New customer locations would get wrong default, causing API crashes
--
-- Solution: Change DEFAULT from Array [] to Object {} for both JSON fields
-- ============================================================================

-- Fix service_offerings default (Array → Object)
ALTER TABLE customer_locations
ALTER COLUMN service_offerings SET DEFAULT '{}'::jsonb;

-- Fix location_details default (safety check, should already be correct)
ALTER TABLE customer_locations
ALTER COLUMN location_details SET DEFAULT '{}'::jsonb;

-- Verification: Check existing data is already migrated (V90005 fixed DEV-SEED data)
DO $$
DECLARE
    array_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO array_count
    FROM customer_locations
    WHERE jsonb_typeof(service_offerings) = 'array'
       OR jsonb_typeof(location_details) = 'array';

    IF array_count > 0 THEN
        RAISE WARNING 'Found % locations with array types - run V90005 in dev environment', array_count;
    ELSE
        RAISE NOTICE 'All existing locations have correct object types - defaults updated for future inserts';
    END IF;
END $$;

-- Comments for documentation
COMMENT ON COLUMN customer_locations.service_offerings IS 'JSONB object for flexible service metadata (e.g., {breakfast: true, catering: true})';
COMMENT ON COLUMN customer_locations.location_details IS 'JSONB object for flexible location metadata (e.g., {parking: "available", capacity: 100})';
