#!/usr/bin/env bash
# rollback-template.sh
# <5 min Rollback für User-Lead-Protection Foundation (Template).
# Achtung: Prüfe Objectnamen vor Ausführung in Prod!
set -euo pipefail
export PSQL="${PSQL:-psql}"

$PSQL "$PGURL" -v ON_ERROR_STOP=1 <<'SQL'
SET lock_timeout='250ms'; SET statement_timeout='15s';

-- 1) Policies deaktivieren (softe Rücknahme)
ALTER TABLE user_lead_assignments DISABLE ROW LEVEL SECURITY;
ALTER TABLE lead_protection_rules DISABLE ROW LEVEL SECURITY;
ALTER TABLE lead_access_audit DISABLE ROW LEVEL SECURITY;

-- 2) View entfernen (Client liest wieder alten Pfad)
DROP VIEW IF EXISTS v_lead_protection;

-- 3) Audit & Assignments vorerst belassen (kein Datenverlust). Optional: Rename als Quarantäne
-- ALTER TABLE user_lead_assignments RENAME TO _rollback_user_lead_assignments;
-- ALTER TABLE lead_access_audit RENAME TO _rollback_lead_access_audit;

-- 4) Funktionen neutralisieren
DROP FUNCTION IF EXISTS assert_edit_and_audit(uuid, lead_action, text);
DROP FUNCTION IF EXISTS can_edit_lead(uuid);

SQL

echo "Rollback executed (non-destructive). Re-enable old read path in app, schedule cleanup later."
