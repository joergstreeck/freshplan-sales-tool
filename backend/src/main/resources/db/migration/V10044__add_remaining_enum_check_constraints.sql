-- Sprint 2.1.7.7 - Add Remaining Database CHECK Constraints for Enum Fields
-- Migration V10044: Complete 3-Layer Validation for All Enum Fields
--
-- Context: V10043 added constraints for 5 enum fields, this completes the remaining 2
-- Affected Tables: customers
--
-- 3-Layer Validation Strategy:
--   1. Frontend: MUI Select mit Server-Driven Enums (useEnumOptions)
--   2. Backend: Java Enum Validierung (@Enumerated)
--   3. Database: CHECK Constraints (THIS MIGRATION)
--
-- Remaining Fields:
--   - customerType (CustomerType enum)
--   - primaryFinancing (FinancingType enum)

-- =============================================================================
-- STEP 1: DATA CLEANUP - Set invalid values to NULL
-- =============================================================================

-- Cleanup: CustomerType (customers)
UPDATE customers
SET customer_type = NULL
WHERE customer_type IS NOT NULL
  AND customer_type NOT IN (
    'NEUKUNDE', 'UNTERNEHMEN', 'INSTITUTION', 'PRIVAT', 'VEREIN', 'SONSTIGE'
  );

-- Cleanup: PrimaryFinancing (customers)
UPDATE customers
SET primary_financing = NULL
WHERE primary_financing IS NOT NULL
  AND primary_financing NOT IN ('PRIVATE', 'PUBLIC', 'MIXED');

-- =============================================================================
-- STEP 2: ADD CHECK CONSTRAINTS
-- =============================================================================

-- =============================================================================
-- 1. CustomerType Enum (Customer.customerType)
-- =============================================================================
-- Values: NEUKUNDE, UNTERNEHMEN, INSTITUTION, PRIVAT, VEREIN, SONSTIGE
-- Entity: de.freshplan.domain.customer.entity.CustomerType

ALTER TABLE customers
ADD CONSTRAINT chk_customers_customer_type
CHECK (customer_type IN (
  'NEUKUNDE',
  'UNTERNEHMEN',
  'INSTITUTION',
  'PRIVAT',
  'VEREIN',
  'SONSTIGE'
) OR customer_type IS NULL);

COMMENT ON CONSTRAINT chk_customers_customer_type ON customers IS
'Sprint 2.1.7.7 V10044: Validates customer_type against CustomerType enum (Backend: de.freshplan.domain.customer.entity.CustomerType)';

-- =============================================================================
-- 2. PrimaryFinancing Enum (Customer.primaryFinancing)
-- =============================================================================
-- Values: PRIVATE, PUBLIC, MIXED
-- Entity: de.freshplan.domain.customer.entity.FinancingType

ALTER TABLE customers
ADD CONSTRAINT chk_customers_primary_financing
CHECK (primary_financing IN (
  'PRIVATE',
  'PUBLIC',
  'MIXED'
) OR primary_financing IS NULL);

COMMENT ON CONSTRAINT chk_customers_primary_financing ON customers IS
'Sprint 2.1.7.7 V10044: Validates primary_financing against FinancingType enum (Backend: de.freshplan.domain.customer.entity.FinancingType)';

-- =============================================================================
-- SUMMARY
-- =============================================================================
-- ✅ 2 CHECK Constraints hinzugefügt:
--    - customers.customer_type      (6 Werte: NEUKUNDE, UNTERNEHMEN, INSTITUTION, PRIVAT, VEREIN, SONSTIGE)
--    - customers.primary_financing  (3 Werte: PRIVATE, PUBLIC, MIXED)
--
-- ✅ COMPLETE 3-Layer Validation:
--    - Combined with V10043: 7 total enum CHECK constraints
--    - All enum fields in customers/leads tables now have DB-level validation
--
-- ✅ Database Integrity:
--    - Ungültige Enum-Werte können NICHT mehr eingefügt werden
--    - Validation auf allen 3 Ebenen: Frontend → Backend → Database
--
-- 🔒 ZERO TOLERANCE für Backend/Frontend Parity eingehalten!
