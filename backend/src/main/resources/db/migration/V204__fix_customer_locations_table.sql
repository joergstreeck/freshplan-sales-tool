-- V204: Fix customer_locations table - add missing columns for Sprint 2 integration
-- Critical: Table was missing primary key and other required fields

-- 1. Add ID column as primary key if not exists
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_schema = 'public' 
                   AND table_name = 'customer_locations' 
                   AND column_name = 'id') THEN
        ALTER TABLE customer_locations 
        ADD COLUMN id UUID DEFAULT gen_random_uuid() PRIMARY KEY;
    END IF;
END $$;

-- 2. Add location_details column as JSONB
ALTER TABLE customer_locations 
ADD COLUMN IF NOT EXISTS location_details JSONB DEFAULT '{}'::jsonb;

-- 3. Add service_offerings column  
ALTER TABLE customer_locations 
ADD COLUMN IF NOT EXISTS service_offerings TEXT;

-- Add comments for documentation
COMMENT ON COLUMN customer_locations.id IS 'Primary key for customer location';
COMMENT ON COLUMN customer_locations.location_details IS 'Detailed description of the location including special characteristics';
COMMENT ON COLUMN customer_locations.service_offerings IS 'List of services offered at this location';