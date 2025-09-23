#!/usr/bin/env bash
set -euo pipefail

max=150
lines=$(wc -l < CLAUDE.md)

if [ "$lines" -gt "$max" ]; then
  echo "❌ CLAUDE.md hat $lines Zeilen (> $max). Bitte kürzen!"
  exit 1
else
  echo "✅ CLAUDE.md Länge OK: $lines Zeilen (<= $max)"
fi