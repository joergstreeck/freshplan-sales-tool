-- Migration V10030: OpportunityType Enum (VARCHAR + CHECK Constraint)
-- Sprint 2.1.7.1: Lead→Opportunity Quick Wins - Freshfoodz Business-Typen
-- Pattern: VARCHAR + CHECK Constraint (NICHT PostgreSQL ENUM Type!)
-- Begründung: JPA-Standard @Enumerated(STRING), flexibler, wartbarer
-- Ref: docs/planung/features-neu/02_neukundengewinnung/artefakte/ENUM_MIGRATION_STRATEGY.md

-- Step 1: Add opportunity_type column (nullable initially for existing data)
ALTER TABLE opportunities
ADD COLUMN opportunity_type VARCHAR(50);

-- Step 2: Set default values for existing opportunities (based on name patterns)
UPDATE opportunities SET opportunity_type =
  CASE
    WHEN name LIKE '%Upsell%' THEN 'SORTIMENTSERWEITERUNG'
    WHEN name LIKE '%Renewal%' OR name LIKE '%Verlängerung%' THEN 'VERLAENGERUNG'
    WHEN name LIKE '%Cross-Sell%' THEN 'SORTIMENTSERWEITERUNG'
    WHEN name LIKE '%Expansion%' OR name LIKE '%Standort%' THEN 'NEUER_STANDORT'
    ELSE 'NEUGESCHAEFT' -- Default für alle anderen (inkl. "vom Lead" Opportunities)
  END;

-- Step 3: Verify migration (should be 0)
DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM opportunities WHERE opportunity_type IS NULL) THEN
    RAISE EXCEPTION 'Migration failed: Found NULL opportunity_type values';
  END IF;
END $$;

-- Step 4: Set DEFAULT value for new INSERTs (important for tests!)
ALTER TABLE opportunities
ALTER COLUMN opportunity_type SET DEFAULT 'NEUGESCHAEFT';

-- Step 5: Make opportunity_type NOT NULL
ALTER TABLE opportunities
ALTER COLUMN opportunity_type SET NOT NULL;

-- Step 6: Add CHECK constraint (4 valid Freshfoodz business types)
ALTER TABLE opportunities
ADD CONSTRAINT chk_opportunity_type CHECK (opportunity_type IN (
  'NEUGESCHAEFT',           -- Kunde kauft zum ersten Mal
  'SORTIMENTSERWEITERUNG',  -- Neue Produktkategorie
  'NEUER_STANDORT',         -- Zusätzlicher Standort
  'VERLAENGERUNG'           -- Rahmenvertrag-Renewal
));

-- Step 7: Add B-Tree index for performance
CREATE INDEX idx_opportunities_opportunity_type ON opportunities(opportunity_type);

-- Step 8: Add column comment for documentation
COMMENT ON COLUMN opportunities.opportunity_type IS
  'Freshfoodz-spezifischer Business-Typ: NEUGESCHAEFT, SORTIMENTSERWEITERUNG, NEUER_STANDORT, VERLAENGERUNG. Pattern: VARCHAR + CHECK (Sprint 2.1.7.1)';

-- Rollback SQL (für Notfall):
-- DROP INDEX IF EXISTS idx_opportunities_opportunity_type;
-- ALTER TABLE opportunities DROP CONSTRAINT IF EXISTS chk_opportunity_type;
-- ALTER TABLE opportunities DROP COLUMN IF EXISTS opportunity_type;
