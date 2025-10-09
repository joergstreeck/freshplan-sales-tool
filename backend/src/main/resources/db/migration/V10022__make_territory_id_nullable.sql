-- Migration V10022: Make leads.territory_id nullable
--
-- Problem: After flyway:clean, territories table is empty → Territory.findByCountryCode("DE") returns null
--          → Lead creation fails with "ERROR: null value in column territory_id violates not-null constraint"
--
-- Root Cause: territory_id has NOT NULL constraint but Territory data might not exist after DB reset
--
-- Solution: Make territory_id nullable
--           Territory is only used for Currency/Tax/Business-Rules (no geographical protection)
--           Lead can function without Territory assignment
--
-- Related: LeadResource.java lines 256-261 (Territory lookup with fallback)
--          Lead.java line 82 (changed to nullable = true)

ALTER TABLE leads ALTER COLUMN territory_id DROP NOT NULL;
