-- =========================================
-- Migration: V36__add_sprint2_pain_points_field.sql
-- Autor: Claude
-- Datum: 2025-08-02
-- Ticket: FRESH-SPRINT2
-- Sprint: Sprint 2
-- Zweck: Fügt pain_points JSONB Feld zu customers hinzu
-- =========================================

-- Sprint 2: Add pain_points as JSONB field to customers table
-- Diese Spalte speichert Pain Points als JSON Array
ALTER TABLE customers
ADD COLUMN IF NOT EXISTS pain_points jsonb DEFAULT '[]'::jsonb;

-- Add comment for documentation
COMMENT ON COLUMN customers.pain_points IS 'Customer pain points as JSON array (Sprint 2)';

-- Optional: Add GIN index for JSONB queries if needed
CREATE INDEX IF NOT EXISTS idx_customers_pain_points_gin 
ON customers USING GIN (pain_points);

-- Log successful migration
DO $$
BEGIN
    RAISE NOTICE 'Sprint 2 pain_points field successfully added to customers table';
END $$;