#!/usr/bin/env bash
set -euo pipefail

# Check for hardcoded migration numbers in staged files
if git diff --cached --name-only | grep -E '\.(sql|md)$' | xargs grep -nE 'V[0-9]{3}__' --with-filename 2>/dev/null; then
  echo "❌ Harte Migrationsnummern gefunden! Bitte Script get-next-migration.sh verwenden."
  exit 1
else
  echo "✅ Keine hardcodierten Migrationsnummern gefunden."
fi