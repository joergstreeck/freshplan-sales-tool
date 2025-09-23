#!/usr/bin/env bash
set -euo pipefail

# Update CLAUDE.md date automatically
current_date=$(date +"%d.%m.%Y")
sed -i.bak -E "s/(Aktuelles Datum: ).*/\1$current_date/" CLAUDE.md && rm -f CLAUDE.md.bak

echo "✅ CLAUDE.md date updated to: $current_date"