-- V10: Complete schema alignment with entities
-- Date: 2025-07-05
-- Author: Claude (FreshPlan Team)

-- This migration ensures all entity columns exist in the database
-- Run this after V9 to complete the schema alignment

-- Ensure customer_addresses has all columns from CustomerAddress entity
ALTER TABLE customer_addresses 
ALTER COLUMN country TYPE VARCHAR(3),
ALTER COLUMN country SET DEFAULT 'DEU';

-- The remaining columns that might be missing
-- Using IF NOT EXISTS to make it idempotent
DO $$ 
BEGIN
    -- Check and add latitude column
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name='customer_addresses' AND column_name='latitude') THEN
        ALTER TABLE customer_addresses ADD COLUMN latitude DOUBLE PRECISION;
    END IF;
    
    -- Check and add longitude column
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name='customer_addresses' AND column_name='longitude') THEN
        ALTER TABLE customer_addresses ADD COLUMN longitude DOUBLE PRECISION;
    END IF;
END $$;