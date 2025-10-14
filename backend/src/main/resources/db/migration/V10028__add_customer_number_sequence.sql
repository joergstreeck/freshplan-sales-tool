-- ================================================================================
-- Migration V10028: Add Customer Number Sequence (Race Condition Fix)
-- ================================================================================
-- Author: Claude (Code Review Fix - Gemini Code Assist)
-- Date: 2025-10-14
-- Sprint: 2.1.7 Code Review Fixes
-- Issue: GitHub PR #139 Code Review - Fix Race Condition in generateCustomerNumber()
--
-- Problem:
--   OpportunityService.generateCustomerNumber() uses count() + 1 for customer IDs
--   This causes race condition when two concurrent requests get same count value
--   → Duplicate customer numbers → Business logic breaks
--
-- Solution:
--   Create PostgreSQL sequence for atomic, guaranteed-unique customer numbers
--   Format: KD-00001, KD-00002, KD-00003...
--
-- Rollback Strategy:
--   DROP SEQUENCE IF EXISTS customer_number_seq CASCADE;
-- ================================================================================

-- Create sequence for customer numbers
-- Starts at 1, increments by 1, no max limit
CREATE SEQUENCE IF NOT EXISTS customer_number_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO CYCLE
    CACHE 1;

-- Set sequence to current max customer number + 1 (if customers already exist)
-- Pattern: KD-XXXXX → extract XXXXX as integer → find max → set sequence
-- This ensures new customers get numbers higher than existing ones
DO $$
DECLARE
    max_customer_number INTEGER;
BEGIN
    -- Extract numeric part from customer_number (format: KD-XXXXX)
    -- Example: 'KD-00042' → 42
    SELECT COALESCE(MAX(CAST(SUBSTRING(customer_number FROM 4) AS INTEGER)), 0)
    INTO max_customer_number
    FROM customers
    WHERE customer_number ~ '^KD-[0-9]{5}$';

    -- Set sequence to max + 1 (or 1 if no customers exist)
    PERFORM setval('customer_number_seq', max_customer_number + 1, false);

    -- Log for migration audit
    RAISE NOTICE 'Customer number sequence initialized at: % (max existing: %)',
                 max_customer_number + 1, max_customer_number;
END $$;

-- ================================================================================
-- TESTING QUERIES (Dev/Staging only)
-- ================================================================================
-- Test sequence generation:
--   SELECT nextval('customer_number_seq');  -- Returns 1, 2, 3...
--   SELECT 'KD-' || LPAD(nextval('customer_number_seq')::TEXT, 5, '0');  -- Returns KD-00001
--
-- Check current sequence value:
--   SELECT last_value FROM customer_number_seq;
--
-- Reset sequence (for testing only):
--   ALTER SEQUENCE customer_number_seq RESTART WITH 1;
-- ================================================================================
