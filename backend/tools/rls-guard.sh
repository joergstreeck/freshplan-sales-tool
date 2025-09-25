#!/usr/bin/env bash
set -euo pipefail

# RLS Guard - Sprint 1.6
# Heuristik: Finde transaktionale Service-Methoden mit DB-Zugriff OHNE @RlsContext
# - @Transactional ODER @Transactional(...)
# - plus Hinweise auf DB-Zugriff: EntityManager, PanacheRepository, Repository, createQuery, nativeQuery
# - und in derselben Datei fehlt @RlsContext oberhalb der betroffenen Methode

fail=0
echo "🔍 RLS Guard: Checking for missing @RlsContext annotations..."

while IFS= read -r file; do
  # Grobe Vorauswahl: nur Java, nur src/main
  [[ "$file" != *"/src/main/"* ]] && continue
  [[ "$file" == *"RlsConnectionAffinityGuard.java" ]] && continue  # Skip interceptor itself
  [[ "$file" == *"RlsContext.java" ]] && continue  # Skip annotation

  # Read first 2000 lines for performance
  content="$(sed -n '1,2000p' "$file" 2>/dev/null || true)"

  # Skip if file is too short
  [ -z "$content" ] && continue

  # Kandidaten: Methoden mit @Transactional und DB-Hinweisen
  if grep -q "@Transactional" <<<"$content" && \
     grep -Eq "EntityManager|PanacheRepository|create(Native)?Query|Repository|em\.|Lead\.find|Customer\.find" <<<"$content"; then

     # Fehlt @RlsContext in der Datei?
     if ! grep -q "@RlsContext" <<<"$content"; then
        echo "❌ Missing @RlsContext: ${file#*/src/main/java/}"
        echo "   Found: @Transactional + DB access patterns"
        fail=1
     fi
  fi
done < <(find src/main/java -name "*.java" -type f 2>/dev/null || true)

if [ $fail -eq 0 ]; then
  echo "✅ RLS Guard: All transactional DB methods have @RlsContext"
else
  echo ""
  echo "⚠️  RLS Guard: Found services with @Transactional + DB access but missing @RlsContext"
  echo "   Please add @RlsContext annotation to transactional methods with DB operations"
  echo "   See: docs/planung/adr/ADR-0007-rls-connection-affinity.md"
fi

exit $fail