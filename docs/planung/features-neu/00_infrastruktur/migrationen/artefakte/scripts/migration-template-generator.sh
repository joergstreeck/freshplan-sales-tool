#!/usr/bin/env bash
# migration-template-generator.sh
# Kopiert ein VXXX__*.sql Template in euren Migrationsordner und vergibt eine Prod-Nummer
# via ./scripts/get-next-migration.sh – ohne Konflikte in parallelen Teams.
#
# Usage:
#   ./migration-template-generator.sh sql/VXXX__user_lead_protection_foundation.sql \
#     ../backend/src/main/resources/db/migration
#
set -euo pipefail
TEMPLATE="${1:-}"
DEST="${2:-}"
[ -z "$TEMPLATE" ] && { echo "Template path required"; exit 1; }
[ -z "$DEST" ] && { echo "Destination migration folder required"; exit 1; }

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
GET_NEXT="${ROOT}/../scripts/get-next-migration.sh"

if [ ! -x "$GET_NEXT" ]; then
  echo "WARN: get-next-migration.sh not found/executable at $GET_NEXT – falling back to naive numbering."
  LAST=$(ls -1 "$DEST" | grep -E '^V[0-9]+__' | sed -E 's/^V([0-9]+)__.*/\1/' | sort -n | tail -1)
  NEXT=$((LAST+1))
else
  NEXT="$("$GET_NEXT")"
fi

BASENAME="$(basename "$TEMPLATE")"
NEWNAME="$(echo "$BASENAME" | sed -E "s/^VXXX__/V${NEXT}__/")"

cp "$TEMPLATE" "$DEST/$NEWNAME"
echo "Created $DEST/$NEWNAME"
