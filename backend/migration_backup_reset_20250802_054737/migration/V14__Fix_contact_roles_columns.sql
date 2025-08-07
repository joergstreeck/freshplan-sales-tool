-- V9: Fix entity-database schema mismatches
-- Date: 2025-07-05
-- Author: Claude (FreshPlan Team)

-- Fix contact_roles table - add missing columns
ALTER TABLE contact_roles 
ADD COLUMN IF NOT EXISTS is_decision_maker_role BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE contact_roles 
ADD COLUMN IF NOT EXISTS hierarchy_level INTEGER;

-- Drop columns that shouldn't be there (from different schema version)
ALTER TABLE contact_roles 
DROP COLUMN IF EXISTS is_active;

ALTER TABLE contact_roles 
DROP COLUMN IF EXISTS created_at;

ALTER TABLE contact_roles 
DROP COLUMN IF EXISTS updated_at;

-- Fix customer_addresses table - add all missing columns
ALTER TABLE customer_addresses 
ADD COLUMN IF NOT EXISTS access_instructions TEXT;

ALTER TABLE customer_addresses 
ADD COLUMN IF NOT EXISTS additional_line VARCHAR(255);

ALTER TABLE customer_addresses 
ADD COLUMN IF NOT EXISTS po_box VARCHAR(50);

ALTER TABLE customer_addresses 
ADD COLUMN IF NOT EXISTS care_of VARCHAR(100);

ALTER TABLE customer_addresses 
ADD COLUMN IF NOT EXISTS building_name VARCHAR(100);

ALTER TABLE customer_addresses 
ADD COLUMN IF NOT EXISTS floor_apartment VARCHAR(50);

ALTER TABLE customer_addresses 
ADD COLUMN IF NOT EXISTS geocoded_at TIMESTAMP;

ALTER TABLE customer_addresses 
ADD COLUMN IF NOT EXISTS is_validated BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE customer_addresses 
ADD COLUMN IF NOT EXISTS validated_at TIMESTAMP;

ALTER TABLE customer_addresses 
ADD COLUMN IF NOT EXISTS validation_service VARCHAR(50);

ALTER TABLE customer_addresses 
ADD COLUMN IF NOT EXISTS is_primary_for_type BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE customer_addresses 
ADD COLUMN IF NOT EXISTS is_active BOOLEAN NOT NULL DEFAULT TRUE;

ALTER TABLE customer_addresses 
ADD COLUMN IF NOT EXISTS delivery_instructions TEXT;

ALTER TABLE customer_addresses 
ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE customer_addresses 
ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;

ALTER TABLE customer_addresses 
ADD COLUMN IF NOT EXISTS deleted_by VARCHAR(100);

-- Add missing audit columns to customer_addresses
ALTER TABLE customer_addresses 
ADD COLUMN IF NOT EXISTS created_by VARCHAR(100) NOT NULL DEFAULT 'system';

ALTER TABLE customer_addresses 
ADD COLUMN IF NOT EXISTS updated_by VARCHAR(100);