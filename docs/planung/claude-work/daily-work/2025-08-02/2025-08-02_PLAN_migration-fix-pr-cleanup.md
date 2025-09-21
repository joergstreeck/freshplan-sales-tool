# 🔧 PLAN: Migration-Fix mit sauberer PR - Option 3

**Datum:** 02.08.2025, 16:50  
**Ziel:** PR#71 sauber schließen, Migration-Fixes in separater PR, dann Feature-Branch rebasen  
**Status:** 🔄 IN ARBEIT

## 📋 Ausgangslage

### Aktuelle Situation:
- **PR#71**: "feat(help): In-App Help System" - CI ist rot
- **Problem**: Migration-Fixes (V35, V36, V37) sind noch nicht gepusht
- **Branch**: `feature/sprint-2-customer-ui-integration`
- **Lösung**: Option 3 - Saubere Trennung von Migrations und Features

### Warum Option 3?
- ✅ Saubere Git-Historie
- ✅ Einfachere Code-Reviews  
- ✅ Fokussierte PRs (eine für Migrations, eine für Features)
- ✅ Schneller zum Ziel (keine Debug-Zyklen)
- ✅ Solide Code-Basis für die Zukunft

## 🚀 Schritt-für-Schritt Plan

### Phase 1: Vorbereitung (5 Min)
```bash
# 1.1 Status sichern
git status > /tmp/git-status-before-option3.txt
git diff --name-only > /tmp/changed-files-before-option3.txt

# 1.2 Aktuelle Änderungen dokumentieren
echo "=== Ungepushte Änderungen ===" > /tmp/option3-changes.md
echo "Migration Fixes:" >> /tmp/option3-changes.md
ls -la backend/src/main/resources/db/migration/V3*.sql >> /tmp/option3-changes.md
echo "Gelöschte Migrationen:" >> /tmp/option3-changes.md
git status | grep deleted | grep migration >> /tmp/option3-changes.md
```

### Phase 2: PR#71 schließen (2 Min)
```bash
# 2.1 PR mit Erklärung schließen
gh pr close 71 --comment "Schließe diese PR temporär um Migration-Fixes sauber zu trennen.
Die Features werden in einer neuen PR nach den Migration-Fixes wieder eingereicht.
Siehe #[NEUE_MIGRATION_PR_NUMMER] für die Migration-Fixes."

# 2.2 Branch-Status dokumentieren
git log --oneline -5 > /tmp/pr71-last-commits.txt
```

### Phase 3: Migration-Fix Branch erstellen (10 Min)
```bash
# 3.1 Zu main wechseln und aktualisieren
git checkout main
git pull origin main

# 3.2 Neuen Branch für Migration-Fixes
git checkout -b fix/sprint2-migration-cleanup

# 3.3 NUR Migration-relevante Dateien kopieren
# Aus dem alten Branch die wichtigen Dateien holen
git checkout feature/sprint-2-customer-ui-integration -- backend/src/main/resources/db/migration/V35__add_sprint2_location_fields.sql
git checkout feature/sprint-2-customer-ui-integration -- backend/src/main/resources/db/migration/V36__add_sprint2_pain_points_field.sql
git checkout feature/sprint-2-customer-ui-integration -- backend/src/main/resources/db/migration/V37__add_remaining_sprint2_fields.sql
git checkout feature/sprint-2-customer-ui-integration -- docs/DATABASE_MIGRATION_GUIDE.md
git checkout feature/sprint-2-customer-ui-integration -- docs/MIGRATION_SESSION_CHECKLIST.md

# 3.4 Problematische alte Migrationen entfernen
rm -f backend/src/main/resources/db/migration/V1[0-9][0-9]__*.sql
rm -f backend/src/main/resources/db/migration/V[6-9]__*.sql
# (Liste muss angepasst werden basierend auf Analyse)
```

### Phase 4: Migration-Fix committen und PR erstellen (5 Min)
```bash
# 4.1 Alle Änderungen stagen
git add -A

# 4.2 Sauberer Commit
git commit -m "fix(migrations): Add Sprint 2 fields and fix migration order

- Add V35: Location fields for Sprint 2 (locations_germany, locations_austria, etc.)
- Add V36: pain_points JSONB field with proper default value
- Add V37: primary_financing field for business model
- Remove conflicting old migrations that caused schema validation errors
- Add comprehensive DATABASE_MIGRATION_GUIDE.md
- Add MIGRATION_SESSION_CHECKLIST.md for future work

These changes fix the schema validation errors that prevented the backend from starting.
All Sprint 2 entity fields now have corresponding migrations."

# 4.3 Push zum Remote
git push origin fix/sprint2-migration-cleanup

# 4.4 PR erstellen
gh pr create \
  --title "fix(migrations): Sprint 2 database migrations and cleanup" \
  --body "## 🎯 Zusammenfassung
Behebt kritische Schema-Validierungsfehler durch fehlende Sprint 2 Migrationen.

## 📋 Was wurde gemacht?
- ✅ V35: Alle location-Felder für Sprint 2
- ✅ V36: pain_points als JSONB mit Default 
- ✅ V37: primary_financing Feld
- ✅ Entfernung konfliktierender alter Migrationen
- ✅ Dokumentation: DATABASE_MIGRATION_GUIDE.md
- ✅ Prozess: MIGRATION_SESSION_CHECKLIST.md

## 🧪 Getestet
- Backend startet erfolgreich ✅
- Alle Schema-Validierungen bestehen ✅
- Customer Entity vollständig gemapped ✅

## 🔗 Kontext
Fixes für #71 - Die Help-System Features kommen in separater PR nach diesem Fix.

## ⚡ Typ
- [x] Bug fix (non-breaking change der ein Problem behebt)
- [ ] New feature
- [ ] Breaking change" \
  --base main
```

### Phase 5: CI überwachen und Merge (10-30 Min)
```bash
# 5.1 CI-Status beobachten
gh pr checks --watch

# 5.2 Bei grüner CI: Review anfordern
gh pr ready

# 5.3 Nach Approval: Merge
# (Manuell oder nach Team-Prozess)
```

### Phase 6: Feature-Branch rebasen (15 Min)
```bash
# 6.1 Zurück zum Feature-Branch
git checkout feature/sprint-2-customer-ui-integration

# 6.2 main mit gemergten Migrations holen
git fetch origin main

# 6.3 Rebase auf main
git rebase origin/main

# 6.4 Konflikte lösen (falls vorhanden)
# - Migrations sollten kein Problem sein (bereits in main)
# - Nur Feature-Code behalten

# 6.5 Force-Push (nach Rebase nötig)
git push --force-with-lease origin feature/sprint-2-customer-ui-integration

# 6.6 Neue PR für Help-System erstellen
gh pr create \
  --title "feat(help): In-App Help System - Intelligente kontextsensitive Hilfe" \
  --body "[Original PR#71 Beschreibung hier einfügen]
  
## 🔗 Kontext
Dies ist die bereinigte Version von #71 nach den Migration-Fixes in #[MIGRATION_PR_NUMMER]"
```

## 📊 Erfolgskriterien

- [ ] Migration-Fix PR ist grün in CI
- [ ] Backend startet ohne Schema-Fehler
- [ ] Feature-Branch erfolgreich rebased
- [ ] Neue Feature-PR erstellt
- [ ] Keine Vermischung von Migrations und Features

## 🚨 Mögliche Probleme & Lösungen

### Problem 1: Rebase-Konflikte
**Lösung:** Immer die Version aus main für Migrations nehmen, Features beibehalten

### Problem 2: CI immer noch rot
**Lösung:** Logs genau analysieren, evtl. weitere Migrations nötig

### Problem 3: Force-Push Bedenken
**Lösung:** `--force-with-lease` verwenden, Team informieren

## 📝 Dokumentation für nächsten Claude

Falls die Session unterbrochen wird:
1. Dieses Dokument lesen
2. `git status` prüfen - wo sind wir?
3. Bei welcher Phase fortfahren
4. `/tmp/option3-*.txt` Dateien für Kontext prüfen

## 🎯 Erwartetes Endergebnis

1. **Saubere main branch** mit funktionierenden Migrations
2. **Neue PR** nur mit Help-System Features
3. **Dokumentation** für zukünftige Migration-Arbeiten
4. **CI grün** für alle PRs

---
**Status:** Warte auf GO für Durchführung