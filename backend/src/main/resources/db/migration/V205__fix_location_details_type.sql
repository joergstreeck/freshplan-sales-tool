-- V205: Fix location_details column type - should be JSONB not TEXT
-- This corrects the type mismatch from V204

-- Drop the TEXT column if it exists
ALTER TABLE customer_locations 
DROP COLUMN IF EXISTS location_details;

-- Add it back as JSONB with proper type
ALTER TABLE customer_locations 
ADD COLUMN location_details JSONB DEFAULT '{}'::jsonb;

-- Add comment
COMMENT ON COLUMN customer_locations.location_details IS 'JSONB field for flexible location metadata and details';