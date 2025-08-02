-- =========================================
-- Migration: V35__add_sprint2_location_fields.sql
-- Autor: Claude
-- Datum: 2025-08-02
-- Ticket: FRESH-SPRINT2
-- Sprint: Sprint 2
-- Zweck: Fügt fehlende Sprint 2 location-Felder zu customers hinzu
-- =========================================

-- Sprint 2: Add location count fields to customers table
-- Diese Felder sind in der Customer Entity definiert aber fehlten in der DB
ALTER TABLE customers
ADD COLUMN IF NOT EXISTS locations_germany INTEGER NOT NULL DEFAULT 0,
ADD COLUMN IF NOT EXISTS locations_austria INTEGER NOT NULL DEFAULT 0,
ADD COLUMN IF NOT EXISTS locations_switzerland INTEGER NOT NULL DEFAULT 0,
ADD COLUMN IF NOT EXISTS locations_rest_eu INTEGER NOT NULL DEFAULT 0,
ADD COLUMN IF NOT EXISTS total_locations_eu INTEGER NOT NULL DEFAULT 0;

-- Add comments for documentation
COMMENT ON COLUMN customers.locations_germany IS 'Number of locations in Germany (Sprint 2)';
COMMENT ON COLUMN customers.locations_austria IS 'Number of locations in Austria (Sprint 2)';
COMMENT ON COLUMN customers.locations_switzerland IS 'Number of locations in Switzerland (Sprint 2)';
COMMENT ON COLUMN customers.locations_rest_eu IS 'Number of locations in rest of EU (Sprint 2)';
COMMENT ON COLUMN customers.total_locations_eu IS 'Total number of locations in EU (Sprint 2)';

-- Optional: Add indexes if needed for performance
CREATE INDEX IF NOT EXISTS idx_customers_total_locations_eu 
ON customers(total_locations_eu);

-- Log successful migration
DO $$
BEGIN
    RAISE NOTICE 'Sprint 2 location fields successfully added to customers table';
END $$;