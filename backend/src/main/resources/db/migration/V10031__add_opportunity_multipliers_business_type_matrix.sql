-- Sprint 2.1.7.3 - Bestandskunden-Workflow
-- Business-Type-Matrix für intelligente expectedValue-Schätzung
-- Migration V10031 - Production (läuft in allen Umgebungen)

-- ============================================================================
-- TABLE: opportunity_multipliers
-- ============================================================================
-- Purpose: Configurable multipliers for calculating expectedValue based on
--          Customer.businessType × Opportunity.opportunityType
--
-- Formula: expectedValue = baseVolume × multiplier
--   where baseVolume = customer.actualAnnualVolume (Xentral)
--                      OR customer.expectedAnnualVolume (Lead)
--                      OR 0 (manual entry)
--
-- Use Case: When creating Opportunity from existing Customer, pre-fill
--           expectedValue intelligently (not manual guessing)
--
-- Example: HOTEL customer (baseVolume=100k€) → SORTIMENTSERWEITERUNG
--          expectedValue = 100.000€ × 0.65 = 65.000€
-- ============================================================================

CREATE TABLE IF NOT EXISTS opportunity_multipliers (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

  -- Business Type (Customer.businessType)
  -- Values: RESTAURANT, HOTEL, CATERING, KANTINE, BILDUNG, GESUNDHEIT,
  --         GROSSHANDEL, LEH, SONSTIGES
  business_type VARCHAR(50) NOT NULL
    CHECK (business_type IN ('RESTAURANT', 'HOTEL', 'CATERING', 'KANTINE',
                              'BILDUNG', 'GESUNDHEIT', 'GROSSHANDEL', 'LEH', 'SONSTIGES')),

  -- Opportunity Type (Opportunity.opportunityType)
  -- Values: NEUGESCHAEFT, SORTIMENTSERWEITERUNG, NEUER_STANDORT, VERLAENGERUNG
  opportunity_type VARCHAR(50) NOT NULL
    CHECK (opportunity_type IN ('NEUGESCHAEFT', 'SORTIMENTSERWEITERUNG',
                                 'NEUER_STANDORT', 'VERLAENGERUNG')),

  -- Multiplier (0.00 - 1.00 for percentage-based calculation)
  multiplier NUMERIC(5,2) NOT NULL CHECK (multiplier >= 0 AND multiplier <= 10),

  -- Timestamps
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

  -- Constraints
  UNIQUE(business_type, opportunity_type)
);

-- Index for fast lookups by business_type + opportunity_type combination
CREATE INDEX IF NOT EXISTS idx_opportunity_multipliers_lookup
  ON opportunity_multipliers(business_type, opportunity_type);

-- ============================================================================
-- SEED DATA: Freshfoodz Default Multipliers (36 entries)
-- ============================================================================
-- 9 BusinessTypes × 4 OpportunityTypes = 36 combinations
--
-- Multiplier Logic:
--   NEUGESCHAEFT: 100% (new business = full volume)
--   SORTIMENTSERWEITERUNG: Varies by industry (15%-65%)
--   NEUER_STANDORT: High (40%-90% - expansion potential)
--   VERLAENGERUNG: Very high (70%-95% - renewal retention)
-- ============================================================================

INSERT INTO opportunity_multipliers (business_type, opportunity_type, multiplier) VALUES
  -- =========================================================================
  -- GASTRO/HOTELLERIE (Hauptzielgruppe Freshfoodz)
  -- =========================================================================

  -- RESTAURANT (Einzelbetriebe, kleinere Budgets, konservatives Wachstum)
  ('RESTAURANT', 'NEUGESCHAEFT', 1.00),
  ('RESTAURANT', 'SORTIMENTSERWEITERUNG', 0.25),
  ('RESTAURANT', 'NEUER_STANDORT', 0.80),
  ('RESTAURANT', 'VERLAENGERUNG', 0.95),

  -- HOTEL (Höchstes Potential: große Küchen, mehrere Standorte, hohe Budgets)
  ('HOTEL', 'NEUGESCHAEFT', 1.00),
  ('HOTEL', 'SORTIMENTSERWEITERUNG', 0.65),
  ('HOTEL', 'NEUER_STANDORT', 0.90),
  ('HOTEL', 'VERLAENGERUNG', 0.90),

  -- CATERING (Event-basiert, saisonales Wachstum, mittleres Potential)
  ('CATERING', 'NEUGESCHAEFT', 1.00),
  ('CATERING', 'SORTIMENTSERWEITERUNG', 0.50),
  ('CATERING', 'NEUER_STANDORT', 0.75),
  ('CATERING', 'VERLAENGERUNG', 0.85),

  -- =========================================================================
  -- INSTITUTIONELL (B2G/B2B Institutional)
  -- =========================================================================

  -- KANTINE (Betriebsgastronomie, stabile Verträge, konservativ)
  ('KANTINE', 'NEUGESCHAEFT', 1.00),
  ('KANTINE', 'SORTIMENTSERWEITERUNG', 0.30),
  ('KANTINE', 'NEUER_STANDORT', 0.60),
  ('KANTINE', 'VERLAENGERUNG', 0.95),

  -- BILDUNG (Schulen/Universitäten, enge Budgets, lange Entscheidungswege)
  ('BILDUNG', 'NEUGESCHAEFT', 1.00),
  ('BILDUNG', 'SORTIMENTSERWEITERUNG', 0.20),
  ('BILDUNG', 'NEUER_STANDORT', 0.50),
  ('BILDUNG', 'VERLAENGERUNG', 0.90),

  -- GESUNDHEIT (Krankenhäuser/Pflegeheime, hohe Standards, höheres Potential)
  ('GESUNDHEIT', 'NEUGESCHAEFT', 1.00),
  ('GESUNDHEIT', 'SORTIMENTSERWEITERUNG', 0.35),
  ('GESUNDHEIT', 'NEUER_STANDORT', 0.70),
  ('GESUNDHEIT', 'VERLAENGERUNG', 0.95),

  -- =========================================================================
  -- B2B-HANDEL
  -- =========================================================================

  -- GROSSHANDEL (B2B-Intermediäre, hohe Volumen, weniger Sortimentswachstum)
  ('GROSSHANDEL', 'NEUGESCHAEFT', 1.00),
  ('GROSSHANDEL', 'SORTIMENTSERWEITERUNG', 0.40),
  ('GROSSHANDEL', 'NEUER_STANDORT', 0.55),
  ('GROSSHANDEL', 'VERLAENGERUNG', 0.80),

  -- LEH (Lebensmitteleinzelhandel, größere Ketten, mittleres Wachstum)
  ('LEH', 'NEUGESCHAEFT', 1.00),
  ('LEH', 'SORTIMENTSERWEITERUNG', 0.45),
  ('LEH', 'NEUER_STANDORT', 0.60),
  ('LEH', 'VERLAENGERUNG', 0.85),

  -- =========================================================================
  -- SONSTIGES (Fallback)
  -- =========================================================================

  -- SONSTIGES (Unbekannte Branchen, konservative Schätzung)
  ('SONSTIGES', 'NEUGESCHAEFT', 1.00),
  ('SONSTIGES', 'SORTIMENTSERWEITERUNG', 0.15),
  ('SONSTIGES', 'NEUER_STANDORT', 0.40),
  ('SONSTIGES', 'VERLAENGERUNG', 0.70)
ON CONFLICT (business_type, opportunity_type) DO NOTHING;

-- ============================================================================
-- COMMENTS
-- ============================================================================

COMMENT ON TABLE opportunity_multipliers IS
  'Sprint 2.1.7.3 - Business-Type-Matrix for intelligent opportunity value estimation';

COMMENT ON COLUMN opportunity_multipliers.business_type IS
  'Customer business type (RESTAURANT, HOTEL, CATERING, etc.) - must match de.freshplan.domain.shared.BusinessType enum';

COMMENT ON COLUMN opportunity_multipliers.opportunity_type IS
  'Opportunity type (NEUGESCHAEFT, SORTIMENTSERWEITERUNG, NEUER_STANDORT, VERLAENGERUNG) - must match de.freshplan.domain.opportunity.entity.OpportunityType enum';

COMMENT ON COLUMN opportunity_multipliers.multiplier IS
  'Multiplier for calculating expectedValue from baseVolume (0.15 = 15%, 1.00 = 100%)';

-- ============================================================================
-- MIGRATION COMPLETE
-- ============================================================================
-- Next Steps (Backend):
--   1. Create OpportunityMultiplier entity (@Entity)
--   2. Create OpportunityMultiplierRepository (PanacheRepository)
--   3. Create OpportunityMultiplierService (getMultiplier method)
--   4. Create GET /api/settings/opportunity-multipliers endpoint
--
-- Next Steps (Frontend):
--   1. Load multipliers in CreateOpportunityForCustomerDialog
--   2. Calculate expectedValue = baseVolume × multiplier
--   3. Display in form (pre-filled, editable)
-- ============================================================================
