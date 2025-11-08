-- V10045: Normalize decision_level values (lowercase → UPPERCASE)
-- Sprint 2.1.7.7: Multi-Location Management - Production Bugfix
-- Context: MUI Select validation error with old lowercase enum values
--
-- Tables affected:
-- - lead_contacts (created in V10016)
-- - contacts (decision_level added in V201)
--
-- Valid values: EXECUTIVE, MANAGER, OPERATIONAL, INFLUENCER

DO $$
DECLARE
  lead_contacts_updated INT;
  contacts_updated INT;
  total_updated INT;
BEGIN
  -- ┌─────────────────────────────────────────────────────────────────┐
  -- │ Fix lead_contacts table                                         │
  -- └─────────────────────────────────────────────────────────────────┘

  -- German legacy values → English uppercase
  UPDATE lead_contacts
  SET decision_level = CASE decision_level
    WHEN 'entscheider' THEN 'EXECUTIVE'
    WHEN 'Entscheider' THEN 'EXECUTIVE'
    WHEN 'genehmiger' THEN 'EXECUTIVE'
    WHEN 'Genehmiger' THEN 'EXECUTIVE'
    WHEN 'mitentscheider' THEN 'MANAGER'
    WHEN 'Mitentscheider' THEN 'MANAGER'
    WHEN 'manager' THEN 'MANAGER'
    WHEN 'Manager' THEN 'MANAGER'
    WHEN 'beeinflusser' THEN 'INFLUENCER'
    WHEN 'Beeinflusser' THEN 'INFLUENCER'
    WHEN 'einflussnehmer' THEN 'INFLUENCER'
    WHEN 'Einflussnehmer' THEN 'INFLUENCER'
    WHEN 'champion' THEN 'INFLUENCER'
    WHEN 'Champion' THEN 'INFLUENCER'
    WHEN 'nutzer' THEN 'OPERATIONAL'
    WHEN 'Nutzer' THEN 'OPERATIONAL'
    WHEN 'operational' THEN 'OPERATIONAL'
    WHEN 'Operational' THEN 'OPERATIONAL'
    WHEN 'informant' THEN 'OPERATIONAL'
    WHEN 'Informant' THEN 'OPERATIONAL'
    WHEN 'gatekeeper' THEN 'OPERATIONAL'
    WHEN 'Gatekeeper' THEN 'OPERATIONAL'
    WHEN 'blocker' THEN 'MANAGER'
    WHEN 'Blocker' THEN 'MANAGER'
    -- Already uppercase but might have wrong capitalization
    WHEN 'executive' THEN 'EXECUTIVE'
    WHEN 'Executive' THEN 'EXECUTIVE'
    WHEN 'influencer' THEN 'INFLUENCER'
    WHEN 'Influencer' THEN 'INFLUENCER'
    ELSE decision_level -- Keep unchanged if already valid
  END
  WHERE decision_level IS NOT NULL
    AND decision_level NOT IN ('EXECUTIVE', 'MANAGER', 'OPERATIONAL', 'INFLUENCER');

  GET DIAGNOSTICS lead_contacts_updated = ROW_COUNT;

  -- ┌─────────────────────────────────────────────────────────────────┐
  -- │ Fix contacts table                                              │
  -- └─────────────────────────────────────────────────────────────────┘

  UPDATE contacts
  SET decision_level = CASE decision_level
    WHEN 'entscheider' THEN 'EXECUTIVE'
    WHEN 'Entscheider' THEN 'EXECUTIVE'
    WHEN 'genehmiger' THEN 'EXECUTIVE'
    WHEN 'Genehmiger' THEN 'EXECUTIVE'
    WHEN 'mitentscheider' THEN 'MANAGER'
    WHEN 'Mitentscheider' THEN 'MANAGER'
    WHEN 'manager' THEN 'MANAGER'
    WHEN 'Manager' THEN 'MANAGER'
    WHEN 'beeinflusser' THEN 'INFLUENCER'
    WHEN 'Beeinflusser' THEN 'INFLUENCER'
    WHEN 'einflussnehmer' THEN 'INFLUENCER'
    WHEN 'Einflussnehmer' THEN 'INFLUENCER'
    WHEN 'champion' THEN 'INFLUENCER'
    WHEN 'Champion' THEN 'INFLUENCER'
    WHEN 'nutzer' THEN 'OPERATIONAL'
    WHEN 'Nutzer' THEN 'OPERATIONAL'
    WHEN 'operational' THEN 'OPERATIONAL'
    WHEN 'Operational' THEN 'OPERATIONAL'
    WHEN 'informant' THEN 'OPERATIONAL'
    WHEN 'Informant' THEN 'OPERATIONAL'
    WHEN 'gatekeeper' THEN 'OPERATIONAL'
    WHEN 'Gatekeeper' THEN 'OPERATIONAL'
    WHEN 'blocker' THEN 'MANAGER'
    WHEN 'Blocker' THEN 'MANAGER'
    WHEN 'executive' THEN 'EXECUTIVE'
    WHEN 'Executive' THEN 'EXECUTIVE'
    WHEN 'influencer' THEN 'INFLUENCER'
    WHEN 'Influencer' THEN 'INFLUENCER'
    ELSE decision_level
  END
  WHERE decision_level IS NOT NULL
    AND decision_level NOT IN ('EXECUTIVE', 'MANAGER', 'OPERATIONAL', 'INFLUENCER');

  GET DIAGNOSTICS contacts_updated = ROW_COUNT;

  total_updated := lead_contacts_updated + contacts_updated;

  -- ┌─────────────────────────────────────────────────────────────────┐
  -- │ Report Results                                                  │
  -- └─────────────────────────────────────────────────────────────────┘

  IF total_updated > 0 THEN
    RAISE NOTICE '╔═══════════════════════════════════════════════════════════════╗';
    RAISE NOTICE '║  ✅ Normalized % decision_level values                       ║', total_updated;
    RAISE NOTICE '║     - lead_contacts: % records updated                       ║', lead_contacts_updated;
    RAISE NOTICE '║     - contacts:      % records updated                       ║', contacts_updated;
    RAISE NOTICE '╚═══════════════════════════════════════════════════════════════╝';
  ELSE
    RAISE NOTICE '✅ All decision_level values are already normalized';
  END IF;

  -- Verify no invalid values remain
  DECLARE
    invalid_count INT;
  BEGIN
    SELECT COUNT(*) INTO invalid_count
    FROM (
      SELECT decision_level FROM lead_contacts
      WHERE decision_level IS NOT NULL
        AND decision_level NOT IN ('EXECUTIVE', 'MANAGER', 'OPERATIONAL', 'INFLUENCER')
      UNION ALL
      SELECT decision_level FROM contacts
      WHERE decision_level IS NOT NULL
        AND decision_level NOT IN ('EXECUTIVE', 'MANAGER', 'OPERATIONAL', 'INFLUENCER')
    ) AS invalid;

    IF invalid_count > 0 THEN
      RAISE WARNING '⚠️  STILL % INVALID decision_level VALUES FOUND!', invalid_count;
      RAISE WARNING 'Manual investigation required!';
    END IF;
  END;
END $$;
