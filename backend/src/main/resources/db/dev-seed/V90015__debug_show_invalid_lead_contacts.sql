-- V90015: Debug - Show DETAILED invalid decision_level values with lead_id
--
-- Shows EXACTLY which lead contacts have invalid decision_level values
--
-- Author: Sprint 2.1.7.7 - DecisionLevel Debug v2
-- Date: 2025-11-02

DO $$
DECLARE
  rec RECORD;
  total_invalid INTEGER;
BEGIN
  -- Count total invalid
  SELECT COUNT(*) INTO total_invalid
  FROM lead_contacts
  WHERE decision_level NOT IN ('EXECUTIVE', 'MANAGER', 'OPERATIONAL', 'INFLUENCER')
    AND decision_level IS NOT NULL;

  RAISE WARNING '╔════════════════════════════════════════════════════════════╗';
  RAISE WARNING '║  DEBUG: Invalid DecisionLevel Values in lead_contacts     ║';
  RAISE WARNING '╠════════════════════════════════════════════════════════════╣';
  RAISE WARNING '║  Total Invalid: % records                                 ║', total_invalid;
  RAISE WARNING '╚════════════════════════════════════════════════════════════╝';

  -- Show each invalid record
  FOR rec IN
    SELECT
      lc.id as contact_id,
      lc.lead_id,
      lc.first_name,
      lc.last_name,
      lc.decision_level,
      l.company_name
    FROM lead_contacts lc
    JOIN leads l ON l.id = lc.lead_id
    WHERE lc.decision_level NOT IN ('EXECUTIVE', 'MANAGER', 'OPERATIONAL', 'INFLUENCER')
      AND lc.decision_level IS NOT NULL
    ORDER BY lc.lead_id, lc.id
  LOOP
    RAISE WARNING '→ Lead % (%) | Contact: % % | decision_level: "%"',
      rec.lead_id,
      rec.company_name,
      rec.first_name,
      rec.last_name,
      rec.decision_level;
  END LOOP;

  IF total_invalid = 0 THEN
    RAISE WARNING '✅ All decision_level values are valid!';
  END IF;

  RAISE WARNING '════════════════════════════════════════════════════════════';
END $$;
