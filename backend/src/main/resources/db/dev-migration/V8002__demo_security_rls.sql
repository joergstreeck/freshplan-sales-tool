-- V8002: Dev-Demo für Security RLS Testing
-- NUR für Development-Environment!
-- Demonstriert RLS-Funktionalität ohne Business-Tabellen

BEGIN;

-- Demo-Tabelle für RLS-Tests
CREATE TABLE IF NOT EXISTS demo_security_items (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  org_id TEXT NOT NULL,
  territory TEXT NOT NULL,
  owner_id UUID NOT NULL,
  title TEXT NOT NULL,
  description TEXT,
  is_public BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMPTZ DEFAULT NOW()
);

-- RLS aktivieren
ALTER TABLE demo_security_items ENABLE ROW LEVEL SECURITY;

-- Drop existing policies if they exist
DROP POLICY IF EXISTS p_demo_items_select ON demo_security_items;
DROP POLICY IF EXISTS p_demo_items_insert ON demo_security_items;
DROP POLICY IF EXISTS p_demo_items_update ON demo_security_items;
DROP POLICY IF EXISTS p_demo_items_delete ON demo_security_items;

-- Select-Policy: gleiche org + (owner oder gleiche territory oder public)
CREATE POLICY p_demo_items_select ON demo_security_items
  FOR SELECT
  USING (
    org_id = current_app_org()
    AND (
      owner_id = current_app_user()
      OR territory = current_app_territory()
      OR is_public = TRUE
      OR has_role('admin')
    )
  );

-- Insert-Policy: nur in eigene org/territory
CREATE POLICY p_demo_items_insert ON demo_security_items
  FOR INSERT
  WITH CHECK (
    org_id = current_app_org()
    AND territory = current_app_territory()
  );

-- Update-Policy: nur eigene items
CREATE POLICY p_demo_items_update ON demo_security_items
  FOR UPDATE
  USING (
    org_id = current_app_org()
    AND owner_id = current_app_user()
  )
  WITH CHECK (
    org_id = current_app_org()
    AND owner_id = current_app_user()
  );

-- Delete-Policy: nur eigene items oder admin
CREATE POLICY p_demo_items_delete ON demo_security_items
  FOR DELETE
  USING (
    (org_id = current_app_org() AND owner_id = current_app_user())
    OR has_role('admin')
  );

-- Dev-Seeds zum Testen
INSERT INTO demo_security_items (org_id, territory, owner_id, title, description, is_public)
VALUES
  ('freshfoodz','DE','987fcdeb-51a2-43d1-9abc-def012345678','Mein Item (DE)','Sollte sichtbar sein für DE User', FALSE),
  ('freshfoodz','DE','987fcdeb-51a2-43d1-9abc-def012345678','Öffentliches Item','Sollte für alle sichtbar sein', TRUE),
  ('freshfoodz','CH','111aaaaa-2222-3333-4444-555555555555','Fremdes Item (CH)','Sollte NICHT sichtbar sein für DE User', FALSE),
  ('freshfoodz','DE','222bbbbb-3333-4444-5555-666666666666','Anderer DE User Item','Sollte sichtbar sein (gleiche Territory)', FALSE),
  ('competitor','DE','987fcdeb-51a2-43d1-9abc-def012345678','Andere Org','Sollte NICHT sichtbar sein (andere org)', FALSE)
ON CONFLICT DO NOTHING;

-- Test-Funktion für Entwicklung
CREATE OR REPLACE FUNCTION test_rls_visibility() RETURNS TABLE(
  scenario TEXT,
  expected_count INT,
  actual_count BIGINT,
  passed BOOLEAN
) AS $$
BEGIN
  -- Test 1: Als DE User
  PERFORM set_app_context(
    '987fcdeb-51a2-43d1-9abc-def012345678'::UUID,
    'freshfoodz',
    'DE',
    ARRAY['sales']
  );

  RETURN QUERY
  SELECT
    'DE User sieht eigene + territory + public items'::TEXT,
    3,
    COUNT(*),
    COUNT(*) = 3
  FROM demo_security_items;

  -- Test 2: Als CH User
  PERFORM set_app_context(
    '111aaaaa-2222-3333-4444-555555555555'::UUID,
    'freshfoodz',
    'CH',
    ARRAY['sales']
  );

  RETURN QUERY
  SELECT
    'CH User sieht nur CH + public items'::TEXT,
    2,
    COUNT(*),
    COUNT(*) = 2
  FROM demo_security_items;

  -- Test 3: Als Admin
  PERFORM set_app_context(
    '999fffff-eeee-dddd-cccc-bbbbbbbbbbbb'::UUID,
    'freshfoodz',
    'DE',
    ARRAY['admin']
  );

  RETURN QUERY
  SELECT
    'Admin sieht alle items der org'::TEXT,
    4,
    COUNT(*),
    COUNT(*) = 4
  FROM demo_security_items;
END;
$$ LANGUAGE plpgsql;

COMMIT;

-- ============================================================================
-- USAGE für Entwickler:
-- ============================================================================
-- 1. Context setzen:
--    SELECT set_app_context('987fcdeb-51a2-43d1-9abc-def012345678'::UUID,
--                           'freshfoodz', 'DE', ARRAY['sales']);
--
-- 2. Items abfragen:
--    SELECT * FROM demo_security_items;
--
-- 3. RLS testen:
--    SELECT * FROM test_rls_visibility();
-- ============================================================================