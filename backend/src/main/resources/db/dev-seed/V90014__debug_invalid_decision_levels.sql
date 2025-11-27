-- V90014: Debug - Show all invalid decision_level values
--
-- Temporary migration to identify remaining 5 invalid decision_level values
--
-- Author: Sprint 2.1.7.7 - DecisionLevel Debug
-- Date: 2025-11-02

DO $$
DECLARE
  rec RECORD;
BEGIN
  RAISE NOTICE '=== INVALID DECISION_LEVEL VALUES ===';

  FOR rec IN
    SELECT decision_level, COUNT(*) as cnt
    FROM lead_contacts
    WHERE decision_level NOT IN ('EXECUTIVE', 'MANAGER', 'OPERATIONAL', 'INFLUENCER')
      AND decision_level IS NOT NULL
    GROUP BY decision_level
    ORDER BY COUNT(*) DESC
  LOOP
    RAISE NOTICE 'Invalid value: % (% occurrences)', rec.decision_level, rec.cnt;
  END LOOP;

  RAISE NOTICE '=== END DEBUG ===';
END $$;
