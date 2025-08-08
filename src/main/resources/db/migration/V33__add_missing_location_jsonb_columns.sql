-- Add missing JSONB columns to customer_locations table
-- These columns are defined in CustomerLocation entity but missing in the database

-- Add service_offerings JSONB column
ALTER TABLE customer_locations 
ADD COLUMN IF NOT EXISTS service_offerings JSONB DEFAULT '{}';

-- Add location_details JSONB column
ALTER TABLE customer_locations 
ADD COLUMN IF NOT EXISTS location_details JSONB DEFAULT '{}';

-- Add comments for documentation
COMMENT ON COLUMN customer_locations.service_offerings IS 'Sprint 2: JSON data for service offerings at this location';
COMMENT ON COLUMN customer_locations.location_details IS 'Sprint 2: JSON data for additional location details';

-- Create indexes for JSON queries (if needed later)
CREATE INDEX IF NOT EXISTS idx_customer_locations_service_offerings_gin 
ON customer_locations USING gin (service_offerings);

CREATE INDEX IF NOT EXISTS idx_customer_locations_location_details_gin 
ON customer_locations USING gin (location_details);