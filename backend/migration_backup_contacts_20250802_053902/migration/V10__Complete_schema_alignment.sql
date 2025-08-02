-- V10: Complete schema alignment with entities
-- Date: 2025-07-05
-- Author: Claude (FreshPlan Team)

-- This migration ensures all entity columns exist in the database
-- Run this after V9 to complete the schema alignment

-- For H2 compatibility, we need separate ALTER statements
-- H2 doesn't support ALTER COLUMN TYPE syntax
-- Since we're changing from VARCHAR(100) to VARCHAR(3), we need to drop and recreate
ALTER TABLE customer_addresses DROP COLUMN IF EXISTS country;
ALTER TABLE customer_addresses ADD COLUMN country VARCHAR(3) DEFAULT 'DEU';

-- Add missing columns if they don't exist
-- H2 doesn't support DO blocks, so we use ALTER TABLE ADD COLUMN IF NOT EXISTS
ALTER TABLE customer_addresses ADD COLUMN IF NOT EXISTS latitude DOUBLE PRECISION;
ALTER TABLE customer_addresses ADD COLUMN IF NOT EXISTS longitude DOUBLE PRECISION;