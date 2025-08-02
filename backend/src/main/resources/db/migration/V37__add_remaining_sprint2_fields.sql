-- =========================================
-- Migration: V37__add_remaining_sprint2_fields.sql
-- Autor: Claude
-- Datum: 2025-08-02
-- Ticket: FRESH-SPRINT2
-- Sprint: Sprint 2
-- Zweck: Fügt alle verbleibenden Sprint 2 Felder zu customers hinzu
-- =========================================

-- Sprint 2: Add remaining business model fields to customers table
-- Diese Felder vervollständigen die Sprint 2 Features

-- Business Model: Primary Financing Type
ALTER TABLE customers
ADD COLUMN IF NOT EXISTS primary_financing VARCHAR(20);

-- Add comment for documentation
COMMENT ON COLUMN customers.primary_financing IS 'Primary financing type (Sprint 2)';

-- Add a CHECK constraint to enforce valid values, even with VARCHAR
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint 
        WHERE conname = 'chk_customers_primary_financing' AND conrelid = 'customers'::regclass
    ) THEN
        ALTER TABLE customers 
        ADD CONSTRAINT chk_customers_primary_financing CHECK (
            primary_financing IS NULL OR 
            primary_financing IN ('CASH', 'CREDIT', 'LEASING', 'MIXED')
        );
    END IF;
END $$;

-- Create enum type if not exists (for future use)
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'financing_type') THEN
        CREATE TYPE financing_type AS ENUM (
            'CASH',
            'CREDIT',
            'LEASING',
            'MIXED'
        );
    END IF;
END $$;

-- Note: We use VARCHAR for now instead of the enum to maintain flexibility
-- Future migration can convert to enum type if needed

-- Log successful migration
DO $$
BEGIN
    RAISE NOTICE 'Sprint 2 business model fields successfully added to customers table';
END $$;