#!/usr/bin/env bash
set -euo pipefail
# Prints next migration number based on existing VNNN__*.sql files in ./backend/sql or ./db/migration
DIRS=("backend/sql" "db/migration" ".")
CUR=$(ls ${DIRS[@]} 2>/dev/null | tr ' ' '\n' | grep -E 'V[0-9]+__.*\.sql$' | sed -E 's/^.*V([0-9]+)__.*$/\1/' | sort -n | tail -n1 || echo "")
NEXT=$(( ${CUR:-0} + 1 ))
printf "%03d\n" "$NEXT"
