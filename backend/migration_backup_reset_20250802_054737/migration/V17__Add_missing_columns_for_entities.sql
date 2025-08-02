-- Add missing columns to align database schema with entity definitions
-- This migration adds columns that are defined in entities but missing in the database

-- Add missing columns to customer_locations
ALTER TABLE customer_locations 
ADD COLUMN IF NOT EXISTS description TEXT,
ADD COLUMN IF NOT EXISTS time_zone VARCHAR(50),
ADD COLUMN IF NOT EXISTS phone VARCHAR(50),
ADD COLUMN IF NOT EXISTS fax VARCHAR(50),
ADD COLUMN IF NOT EXISTS email VARCHAR(255),
ADD COLUMN IF NOT EXISTS tax_number VARCHAR(50),
ADD COLUMN IF NOT EXISTS vat_number VARCHAR(50),
ADD COLUMN IF NOT EXISTS commercial_register VARCHAR(100),
ADD COLUMN IF NOT EXISTS delivery_instructions TEXT,
ADD COLUMN IF NOT EXISTS access_instructions TEXT,
ADD COLUMN IF NOT EXISTS last_modified_at TIMESTAMP,
ADD COLUMN IF NOT EXISTS last_modified_by VARCHAR(100);

-- Rename existing columns to match entity field names (if column exists)
DO $$ 
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_name = 'customer_locations' 
               AND column_name = 'is_delivery_location') THEN
        ALTER TABLE customer_locations 
        RENAME COLUMN is_delivery_location TO is_shipping_location;
    END IF;
END $$;

-- Drop columns that don't exist in entity
ALTER TABLE customer_locations 
DROP COLUMN IF EXISTS location_type,
DROP COLUMN IF EXISTS max_delivery_weight,
DROP COLUMN IF EXISTS has_loading_dock,
DROP COLUMN IF EXISTS has_forklift,
DROP COLUMN IF EXISTS delivery_restrictions,
DROP COLUMN IF EXISTS contact_person,
DROP COLUMN IF EXISTS contact_phone,
DROP COLUMN IF EXISTS contact_email;