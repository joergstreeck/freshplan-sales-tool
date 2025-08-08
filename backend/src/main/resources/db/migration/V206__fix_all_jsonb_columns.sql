-- V206: Fix all JSONB columns in customer_locations table
-- Both location_details and service_offerings should be JSONB

-- Fix service_offerings column type
ALTER TABLE customer_locations 
DROP COLUMN IF EXISTS service_offerings;

ALTER TABLE customer_locations 
ADD COLUMN service_offerings JSONB DEFAULT '[]'::jsonb;

-- Ensure location_details is also correct (in case V205 didn't apply properly)
DO $$
BEGIN
    -- Check if location_details exists and is not JSONB
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'customer_locations' 
        AND column_name = 'location_details'
        AND data_type != 'jsonb'
    ) THEN
        ALTER TABLE customer_locations DROP COLUMN location_details;
        ALTER TABLE customer_locations ADD COLUMN location_details JSONB DEFAULT '{}'::jsonb;
    END IF;
END $$;

-- Add comments
COMMENT ON COLUMN customer_locations.service_offerings IS 'JSONB array of services offered at this location';
COMMENT ON COLUMN customer_locations.location_details IS 'JSONB field for flexible location metadata and details';