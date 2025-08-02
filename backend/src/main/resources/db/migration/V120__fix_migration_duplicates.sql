-- =========================================
-- V120: Critical Fix - Only V33/V34 JSONB columns
-- =========================================
-- 
-- Author: Claude (FreshPlan Team)
-- Date: 2025-08-02
-- Ticket: FRESH-CRITICAL
-- Sprint: Emergency Fix
-- 
-- Purpose: Add ONLY the missing JSONB columns from V33/V34 that were out-of-order
-- WICHTIG: Keine anderen Strukturen hinzufügen - die sind bereits via applied migrations da!
-- =========================================

-- KRITISCH: Diese Migration fügt NUR die V33/V34 JSONB Spalten hinzu
-- Alle anderen Strukturen existieren bereits in der applied Flyway-Historie

-- Fix für fehlende JSONB Spalten aus V33/V34 (werden nach diesem Fix gelöscht)
-- Diese waren out-of-order und wurden von Flyway ignoriert
ALTER TABLE customer_locations 
ADD COLUMN IF NOT EXISTS location_details JSONB;

ALTER TABLE customer_locations 
ADD COLUMN IF NOT EXISTS service_offerings JSONB;

CREATE INDEX IF NOT EXISTS idx_customer_locations_details 
ON customer_locations USING GIN (location_details);

CREATE INDEX IF NOT EXISTS idx_customer_locations_offerings 
ON customer_locations USING GIN (service_offerings);

-- This migration ensures all structures exist regardless of which duplicate Flyway chose
-- V33 and V34 will be deleted after this migration succeeds
-- Future migrations should use numbers > 120