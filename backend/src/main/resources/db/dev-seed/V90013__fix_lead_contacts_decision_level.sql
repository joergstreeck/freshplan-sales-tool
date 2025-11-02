-- V90013: Fix invalid DecisionLevel values in lead_contacts
--
-- Problem: V90002__seed_dev_leads_complete.sql hatte ungültige deutsche Werte
-- die NICHT im DecisionLevel Enum existieren ('Entscheider', 'Beeinflusser', etc.)
--
-- Lösung: UPDATE Statements, die alle ungültigen Werte auf korrekte Enum-Konstanten mappen
--
-- Referenz: backend/src/main/java/de/freshplan/domain/customer/entity/DecisionLevel.java
-- Valid Enum Values:
--   EXECUTIVE    → "Geschäftsführer/Inhaber"
--   MANAGER      → "Führungskraft"
--   OPERATIONAL  → "Operative Ebene"
--   INFLUENCER   → "Berater/Einflussnehmer"
--
-- Author: Sprint 2.1.7.7 - DecisionLevel Enum Violation Fix
-- Date: 2025-11-02

-- ============================================================================
-- 1. Fix 'Entscheider' → 'EXECUTIVE'
-- ============================================================================
UPDATE lead_contacts
SET decision_level = 'EXECUTIVE'
WHERE decision_level = 'Entscheider';

-- ============================================================================
-- 2. Fix 'Genehmiger' → 'EXECUTIVE'
-- ============================================================================
UPDATE lead_contacts
SET decision_level = 'EXECUTIVE'
WHERE decision_level = 'Genehmiger';

-- ============================================================================
-- 3. Fix 'Beeinflusser' → 'INFLUENCER'
-- ============================================================================
UPDATE lead_contacts
SET decision_level = 'INFLUENCER'
WHERE decision_level = 'Beeinflusser';

-- ============================================================================
-- 4. Fix 'Champion' → 'INFLUENCER'
-- ============================================================================
UPDATE lead_contacts
SET decision_level = 'INFLUENCER'
WHERE decision_level = 'Champion';

-- ============================================================================
-- VERIFICATION: Count invalid values (should be 0 after migration)
-- ============================================================================
DO $$
DECLARE
  invalid_count INTEGER;
BEGIN
  SELECT COUNT(*) INTO invalid_count
  FROM lead_contacts
  WHERE decision_level NOT IN ('EXECUTIVE', 'MANAGER', 'OPERATIONAL', 'INFLUENCER')
    AND decision_level IS NOT NULL;

  IF invalid_count > 0 THEN
    RAISE WARNING 'Still % invalid decision_level values found!', invalid_count;
  ELSE
    RAISE NOTICE 'All decision_level values are now valid ✅';
  END IF;
END $$;

-- ============================================================================
-- MIGRATION SUMMARY
-- ============================================================================
-- Fixed: 4 types of invalid DecisionLevel values (Entscheider, Genehmiger, Beeinflusser, Champion)
-- Mapped to: EXECUTIVE (2 types), INFLUENCER (2 types)
-- Next: Backend restart to apply migration, then frontend refresh
