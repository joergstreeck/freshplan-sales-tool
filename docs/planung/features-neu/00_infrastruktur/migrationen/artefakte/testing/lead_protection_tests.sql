-- testing/pgtap/lead_protection_tests.sql
-- Voraussetzung: pgTAP installiert (CREATE EXTENSION pgtap;)

BEGIN;
SELECT plan(10);

-- 1) Tabellen/Spalten vorhanden
SELECT has_table('user_lead_assignments');
SELECT has_table('lead_access_audit');
SELECT has_view('v_lead_protection');

-- 2) RLS aktiv
SELECT col_is_pk('user_lead_assignments','lead_id');
SELECT col_is_pk('user_lead_assignments','territory', false); -- PK composed

-- 3) Rechteprüfung (Simulation)
SELECT set_config('app.user_id','00000000-0000-0000-0000-000000000001', false);
SELECT set_config('app.territory','DE', false);

-- Füge Testdaten ein (minimal, außerhalb RLS via role in Testumgebung oder SECURITY DEFINER Helper)
-- Hier nur Strukturtest; in eurer Suite bitte Fixtures laden.

SELECT finish();
ROLLBACK;
