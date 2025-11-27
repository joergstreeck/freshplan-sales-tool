-- V90016: Fix remaining invalid DecisionLevel values in lead_contacts
--
-- Problem: V90002__seed_dev_leads_complete.sql hatte weitere ungültige Werte:
-- - 'Informant' (4x) - sammelt nur Informationen, keine Entscheidungsbefugnis
-- - 'Blocker' (1x) - kann Deals blockieren, Management-Autorität
--
-- Diese wurden NICHT in V90013 gefixt (dort nur: Entscheider, Beeinflusser, Genehmiger, Champion)
--
-- Author: Sprint 2.1.7.7 - DecisionLevel Enum Parity Fix v2
-- Date: 2025-11-02

-- 1. Fix 'Informant' → 'OPERATIONAL'
-- Rationale: Informant sammelt nur Informationen, hat keine Entscheidungsbefugnis
-- → Operative Ebene (OPERATIONAL)
UPDATE lead_contacts
SET decision_level = 'OPERATIONAL'
WHERE decision_level = 'Informant';

-- 2. Fix 'Blocker' → 'MANAGER'
-- Rationale: Blocker kann Deals blockieren, hat Management-Autorität
-- → Führungskraft (MANAGER)
UPDATE lead_contacts
SET decision_level = 'MANAGER'
WHERE decision_level = 'Blocker';

-- VERIFICATION: Count ALL remaining invalid values
DO $$
DECLARE
  invalid_count INTEGER;
  rec RECORD;
BEGIN
  SELECT COUNT(*) INTO invalid_count
  FROM lead_contacts
  WHERE decision_level NOT IN ('EXECUTIVE', 'MANAGER', 'OPERATIONAL', 'INFLUENCER')
    AND decision_level IS NOT NULL;

  IF invalid_count > 0 THEN
    RAISE WARNING '╔═══════════════════════════════════════════════════════════╗';
    RAISE WARNING '║  ⚠️  STILL % INVALID decision_level VALUES FOUND!         ║', invalid_count;
    RAISE WARNING '╠═══════════════════════════════════════════════════════════╣';

    -- Show each remaining invalid value
    FOR rec IN
      SELECT decision_level, COUNT(*) as count
      FROM lead_contacts
      WHERE decision_level NOT IN ('EXECUTIVE', 'MANAGER', 'OPERATIONAL', 'INFLUENCER')
        AND decision_level IS NOT NULL
      GROUP BY decision_level
    LOOP
      RAISE WARNING '║  → "%" (%x)                                              ║', rec.decision_level, rec.count;
    END LOOP;

    RAISE WARNING '╚═══════════════════════════════════════════════════════════╝';
  ELSE
    RAISE NOTICE '╔═══════════════════════════════════════════════════════════╗';
    RAISE NOTICE '║  ✅ All decision_level values are now valid!              ║';
    RAISE NOTICE '╠═══════════════════════════════════════════════════════════╣';
    RAISE NOTICE '║  Fixed by V90013: Entscheider, Beeinflusser, Genehmiger  ║';
    RAISE NOTICE '║  Fixed by V90016: Informant, Blocker                      ║';
    RAISE NOTICE '╚═══════════════════════════════════════════════════════════╝';
  END IF;
END $$;
