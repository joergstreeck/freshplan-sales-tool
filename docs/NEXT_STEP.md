# 🧭 NEXT STEP NAVIGATION

**Zweck:** Immer nur EINEN klaren nächsten Schritt für Claude
**Update:** Bei jeder Unterbrechung oder Fortschritt

---

## 🎯 JETZT GERADE: 

**CI GRÜN BEKOMMEN - MIGRATIONS-CHAOS SYSTEMATISCH LÖSEN**

**Stand 02.08.2025 23:12:**
- ✅ Vollständige systematische Analyse des Migration-Problems abgeschlossen
- ✅ Kern-Problem identifiziert: V8 Migration sucht `contacts` statt `customer_contacts`
- ✅ Lösungsstrategie A gewählt: V8 direkt korrigieren (erlaubt, da nicht im main)
- ✅ Strategischer Plan erstellt mit allen Erkenntnissen
- 🔄 Bereit für Implementierung in nächster Session

**🚀 NÄCHSTER SCHRITT:**

**V8 Migration korrigieren und CI grün bekommen (TODO-007)**

```bash
# 1. V8 Migration korrigieren
# In backend/src/main/resources/db/migration/V8__add_data_quality_fields.sql:
# Alle "contacts" → "customer_contacts" ersetzen

# 2. V121 Workaround löschen
rm backend/src/main/resources/db/migration/V121__fix_v8_table_name_error.sql

# 3. Lokaler Test
MAVEN_OPTS="-Dmaven.multiModuleProjectDirectory=$PWD" ./mvnw test -Dtest=PingResourceTest -q

# 4. Git Commit + Push
git add -A
git commit -m "fix(migration): Correct V8 table name from contacts to customer_contacts"
git push
```

**DANACH:**
- CI-Status verifizieren (sollte grün werden)
- Sprint 2 Contact Management weiterführen
- Backend Tests für ContactInteractionServiceIT fixen

**UNTERBROCHEN BEI:**
- TODO-006: PHASE 3 Implementierung bereit
- Exakte Stelle: V8__add_data_quality_fields.sql editieren - alle `contacts` durch `customer_contacts` ersetzen
- Plan vollständig in: `/docs/claude-work/daily-work/2025-08-02/2025-08-02_PLAN_ci-fix-migrations-chaos.md`

**🚨 WICHTIG FÜR NÄCHSTE SESSION:**
Bei neuen Migrationen IMMER:
1. `cat docs/FLYWAY_MIGRATION_HISTORY.md | grep "NÄCHSTE"`
2. Nach Migration: `./scripts/update-flyway-history.sh V121 "name" "Beschreibung"`
Nächste freie Nummer: **V121**
**NEU:** Flyway-Historie ist jetzt sauber und dokumentiert!

---

**Status:** ✅ BEREIT für Commit!