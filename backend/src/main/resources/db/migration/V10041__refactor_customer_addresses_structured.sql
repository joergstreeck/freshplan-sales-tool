-- Migration V10041: Refactor Customer Addresses to Structured Fields (Lead-Parity)
-- Sprint 2.1.7.2 D11 - Phase 1: Strukturierte Adressen statt Freetext
--
-- WICHTIG: Feldnamen EXAKT wie in Lead Entity für 1:1 Konversion!
-- Lead.street → Customer.street
-- Lead.postalCode → Customer.postalCode
-- Lead.city → Customer.city
-- Lead.countryCode → Customer.countryCode

-- Step 1: Entferne alte billing_address Freetext-Spalte (aus V10040)
ALTER TABLE customers
  DROP COLUMN IF EXISTS billing_address;

-- Step 2: Füge strukturierte Adressfelder hinzu (EXAKT wie Lead Entity!)
ALTER TABLE customers
  ADD COLUMN IF NOT EXISTS street VARCHAR(255),
  ADD COLUMN IF NOT EXISTS postal_code VARCHAR(20),
  ADD COLUMN IF NOT EXISTS city VARCHAR(100),
  ADD COLUMN IF NOT EXISTS country_code VARCHAR(2) DEFAULT 'DE';

-- Step 3: Füge Multi-Location Felder hinzu
ALTER TABLE customers
  ADD COLUMN IF NOT EXISTS locations_de INTEGER DEFAULT 0,
  ADD COLUMN IF NOT EXISTS locations_ch INTEGER DEFAULT 0,
  ADD COLUMN IF NOT EXISTS locations_at INTEGER DEFAULT 0,
  ADD COLUMN IF NOT EXISTS expansion_planned VARCHAR(20) DEFAULT 'UNKNOWN';

-- Step 4: Kommentare für Dokumentation
COMMENT ON COLUMN customers.street IS 'Street address (incl. house number) - matches Lead.street for 1:1 conversion';
COMMENT ON COLUMN customers.postal_code IS 'Postal code (PLZ) - matches Lead.postalCode for 1:1 conversion';
COMMENT ON COLUMN customers.city IS 'City name - matches Lead.city for 1:1 conversion';
COMMENT ON COLUMN customers.country_code IS 'Country code (DE, CH, AT) - matches Lead.countryCode for 1:1 conversion';

COMMENT ON COLUMN customers.locations_de IS 'Number of locations in Germany';
COMMENT ON COLUMN customers.locations_ch IS 'Number of locations in Switzerland';
COMMENT ON COLUMN customers.locations_at IS 'Number of locations in Austria';
COMMENT ON COLUMN customers.expansion_planned IS 'Planned expansion (YES, NO, UNKNOWN)';

-- Step 5: Constraint für expansion_planned Enum
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint WHERE conname = 'chk_expansion_planned'
  ) THEN
    ALTER TABLE customers
      ADD CONSTRAINT chk_expansion_planned CHECK (expansion_planned IN ('YES', 'NO', 'UNKNOWN'));
  END IF;
END$$;

-- Step 6: Index auf postal_code für Geo-Queries
CREATE INDEX IF NOT EXISTS idx_customers_postal_code ON customers (postal_code);

-- HINWEIS: delivery_addresses (JSONB) bleibt erhalten aus V10040!
-- Diese Spalte wird für Multi-Location Lieferadressen genutzt.
