-- =====================================================================================
-- Migration V10032: Add Lead Parity Fields to Customers
-- =====================================================================================
-- Purpose: Add missing Lead fields to Customer entity for seamless Lead→Customer conversion
-- Sprint: 2.1.7.2 - Customer Edit Mode + Lead Parity
-- Author: Claude (AI-assisted)
-- Date: 2025-10-19
--
-- Background:
-- Customer Wizard was missing critical Lead fields, preventing proper Lead→Customer conversion.
-- This migration adds 5 essential fields that exist in Lead but were missing in Customer.
--
-- Fields Added:
-- 1. kitchen_size      - KitchenSize enum (Klein/Mittel/Groß/Sehr Groß)
-- 2. employee_count    - Number of employees (important for company size assessment)
-- 3. branch_count      - Number of branches/locations (redundant with total_locations_eu but kept for Lead parity)
-- 4. is_chain          - Boolean flag for chain/franchise (redundant but kept for Lead parity)
-- 5. estimated_volume  - Estimated annual purchase volume (BigDecimal, Lead has this, Customer had expectedAnnualVolume)
--
-- Design Decisions:
-- - Using VARCHAR + CHECK constraint for kitchen_size (not PostgreSQL ENUM) for consistency with businessType
-- - branch_count and is_chain are redundant with total_locations_eu but kept for 100% Lead parity
-- - estimated_volume complements expected_annual_volume (Lead has estimatedVolume, Customer expected)
-- =====================================================================================

-- Add kitchen_size field (KitchenSize enum as VARCHAR with CHECK constraint)
ALTER TABLE customers
ADD COLUMN IF NOT EXISTS kitchen_size VARCHAR(20);

-- Add CHECK constraint for kitchen_size enum values (only if not exists)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'customers_kitchen_size_check'
    ) THEN
        ALTER TABLE customers
        ADD CONSTRAINT customers_kitchen_size_check
        CHECK (kitchen_size IN ('KLEIN', 'MITTEL', 'GROSS', 'SEHR_GROSS'));
    END IF;
END $$;

COMMENT ON COLUMN customers.kitchen_size IS 'Küchengröße-Klassifizierung (100% Lead Parity)';

-- Add employee_count field
ALTER TABLE customers
ADD COLUMN IF NOT EXISTS employee_count INTEGER;

COMMENT ON COLUMN customers.employee_count IS 'Anzahl Mitarbeiter (100% Lead Parity)';

-- Add branch_count field (redundant with total_locations_eu, but needed for Lead parity)
ALTER TABLE customers
ADD COLUMN IF NOT EXISTS branch_count INTEGER DEFAULT 1;

COMMENT ON COLUMN customers.branch_count IS 'Anzahl Filialen/Standorte (100% Lead Parity, redundant zu total_locations_eu)';

-- Add is_chain field (redundant logic, but needed for Lead parity)
ALTER TABLE customers
ADD COLUMN IF NOT EXISTS is_chain BOOLEAN DEFAULT false;

COMMENT ON COLUMN customers.is_chain IS 'Kettenbetrieb ja/nein (100% Lead Parity, redundant zu total_locations_eu check)';

-- Add estimated_volume field (complements expected_annual_volume)
ALTER TABLE customers
ADD COLUMN IF NOT EXISTS estimated_volume NUMERIC(12, 2);

COMMENT ON COLUMN customers.estimated_volume IS 'Geschätztes Einkaufsvolumen (100% Lead Parity, ergänzt expected_annual_volume)';

-- Create index for kitchen_size filtering (common filter in queries)
CREATE INDEX IF NOT EXISTS idx_customers_kitchen_size ON customers(kitchen_size) WHERE kitchen_size IS NOT NULL;

-- Create index for employee_count range queries
CREATE INDEX IF NOT EXISTS idx_customers_employee_count ON customers(employee_count) WHERE employee_count IS NOT NULL;

-- =====================================================================================
-- Migration Summary
-- =====================================================================================
-- ✅ Added kitchen_size (VARCHAR with CHECK constraint)
-- ✅ Added employee_count (INTEGER)
-- ✅ Added branch_count (INTEGER, default 1)
-- ✅ Added is_chain (BOOLEAN, default false)
-- ✅ Added estimated_volume (NUMERIC(12,2))
-- ✅ Created performance indexes
-- ✅ Added column comments for documentation
--
-- Impact:
-- - Customer entity now has 100% parity with Lead for core business fields
-- - Lead→Customer conversion can now preserve all business data
-- - Customer Wizard can display same fields as Lead Wizard
-- - No breaking changes (all fields nullable/default values)
-- =====================================================================================
