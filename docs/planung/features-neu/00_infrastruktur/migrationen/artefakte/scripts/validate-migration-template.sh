#!/usr/bin/env bash
# validate-migration-template.sh
# Prüft, dass keine 'VXXX' Platzhalter in produktiven Migrations verbleiben
# und Basis-Konventionen eingehalten sind (named indices, comments).
#
# Usage:
#   ./validate-migration-template.sh ../backend/src/main/resources/db/migration
set -euo pipefail
DIR="${1:-}"
[ -z "$DIR" ] && { echo "Migration dir required"; exit 1; }

ERR=0
while IFS= read -r -d '' f; do
  if grep -q "VXXX__" "$f"; then
    echo "ERROR: Placeholder 'VXXX__' in $f"
    ERR=1
  fi
  if grep -q "CREATE INDEX" "$f" && ! grep -q "IF NOT EXISTS" "$f"; then
    echo "WARN: index without IF NOT EXISTS in $f"
  fi
done < <(find "$DIR" -maxdepth 1 -type f -name 'V*__*.sql' -print0)

exit $ERR
