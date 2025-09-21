#!/usr/bin/env bash
# backfill_user_lead_assignments.sh (Template)
# Zweck: Bestehende Leads initial einem Owner zuweisen (z. B. last_touch_user),
#        in kleinen Batches ohne Locks. App muss app.user_id/app.territory setzen.
#
# Nutzung:
#   PGURL=... ./backfill_user_lead_assignments.sh DE
#   PGURL=... ./backfill_user_lead_assignments.sh CH
#
set -euo pipefail
TERRITORY="${1:-DE}"
BATCH="${BATCH:-20000}"
export PSQL="${PSQL:-psql}"
export PGOPTIONS="${PGOPTIONS---client-min-messages=warning}"

echo "Backfill user_lead_assignments for territory=${TERRITORY} batch=${BATCH}"

$PSQL "$PGURL" -v ON_ERROR_STOP=1 <<SQL
SET lock_timeout='250ms'; SET statement_timeout='15s';
WITH cte AS (
  SELECT l.id AS lead_id,
         (SELECT a.user_id FROM activities a WHERE a.lead_id = l.id AND a.kind IN ('QUALIFIED_CALL','SCHEDULED_FOLLOWUP','ROI_PRESENTATION','SAMPLE_FEEDBACK')
          ORDER BY a.created_at DESC LIMIT 1) AS owner_user_id
  FROM leads l
  LEFT JOIN user_lead_assignments ula ON ula.lead_id=l.id AND ula.territory=l.territory
  WHERE l.territory = $${TERRITORY}$$ AND ula.lead_id IS NULL
  LIMIT ${BATCH}
)
INSERT INTO user_lead_assignments(lead_id, user_id, territory)
SELECT lead_id, owner_user_id, $${TERRITORY}$$ FROM cte WHERE owner_user_id IS NOT NULL
ON CONFLICT (lead_id, territory) DO NOTHING;
SQL

echo "Done batch for ${TERRITORY}."
