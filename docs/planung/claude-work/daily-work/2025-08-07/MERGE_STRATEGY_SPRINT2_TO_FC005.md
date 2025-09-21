# 🔀 MERGE-STRATEGIE: Sprint-2 Code in FC-005 retten

**Datum:** 07.08.2025  
**Author:** Claude  
**Ziel:** Wichtigen Code aus `feature/sprint-2-customer-ui-integration` in `feature/fc-005-data-quality-fixes` sichern

## 📊 AUSGANGSLAGE

### Branches:
1. **main:** Basis ohne V35-37 Migrations
2. **feature/sprint-2-customer-ui-integration:** 
   - ❌ PR #73 geschlossen (zu viele CI-Fehler)
   - ⚠️ Enthält V120 Migration (nicht mehr benötigt)
   - ✅ Enthält wichtigen Code:
     - ContactResource + ContactInteractionResource
     - CostManagementResource + komplettes Cost-Tracking System  
     - HelpSystemResource + Help System Implementation
     - Contact Entity (447 Zeilen!)
     - CustomerChainResource
   - 38 Commits nach gemeinsamer Basis

3. **feature/fc-005-data-quality-fixes:** (AKTUELLER BRANCH)
   - ✅ Hat V35-37 Migrations
   - ✅ Hat Data Quality APIs
   - ✅ Hat Step3 Contact Management Planung (41 Docs)
   - ✅ Hat get-next-migration.sh Script
   - ❌ Hat NICHT den Code aus sprint-2!

## 🚨 PROBLEM

Der wichtige Code aus sprint-2 (Contact, Cost, Help System) ist NICHT in fc-005 und würde verloren gehen!

## 🎯 LÖSUNG: SICHERER MERGE MIT LOKALEM TEST

### PHASE 1: Vorbereitung & Backup
```bash
# 1.1 Uncommitted changes sichern
git stash

# 1.2 Backup-Branch erstellen (Sicherheit!)
git branch backup-fc-005-$(date +%Y%m%d-%H%M%S)

# 1.3 Status prüfen
git status
git log --oneline -5
```

### PHASE 2: Code-Rettung durch Merge
```bash
# 2.1 Sprint-2 Code in fc-005 mergen
git checkout feature/fc-005-data-quality-fixes
git merge feature/sprint-2-customer-ui-integration

# 2.2 Bei Merge-Konflikten:
# - V120 Migration NICHT übernehmen (delete)
# - Alle Code-Dateien (Java, TypeScript) übernehmen
# - Bei Unsicherheit: "both" wählen und später bereinigen

# 2.3 Commit
git commit -m "feat: Merge sprint-2 code into fc-005 (Contact, Cost, Help Systems)"
```

### PHASE 3: V120 Bereinigung
```bash
# 3.1 V120 Migration entfernen (falls vorhanden)
ls backend/src/main/resources/db/migration/V120*.sql
rm -f backend/src/main/resources/db/migration/V120*.sql

# 3.2 Änderungen committen
git add -u
git commit -m "chore: Remove obsolete V120 migration"
```

### PHASE 4: Lokale Tests (KRITISCH!)
```bash
# 4.1 Services starten
./scripts/start-services.sh

# 4.2 Backend testen
cd backend
./mvnw clean compile
./mvnw test
# Bei Fehlern: Notieren für Fixing

# 4.3 Frontend testen  
cd ../frontend
npm install
npm run lint
npm test
# Bei Fehlern: Notieren für Fixing

# 4.4 Migration-Check
./scripts/get-next-migration.sh
# Sollte V201 anzeigen

# 4.5 Manuelle Tests
# - Backend: http://localhost:8080/q/health
# - Frontend: http://localhost:5173
# - API: curl http://localhost:8080/api/customers
```

### PHASE 5: Fehler beheben
```bash
# 5.1 Typische Probleme:
# - Doppelte Imports → Bereinigen
# - Fehlende Dependencies → pom.xml/package.json ergänzen
# - Test-Fehler → Tests anpassen oder @Disabled temporär

# 5.2 Nach jedem Fix:
./mvnw test  # Backend
npm test     # Frontend
```

### PHASE 6: Finale Vorbereitung
```bash
# 6.1 Code-Cleanup
./scripts/quick-cleanup.sh

# 6.2 Finaler Test
cd backend && ./mvnw test
cd ../frontend && npm test

# 6.3 Wenn alles grün → Push vorbereiten
git push origin feature/fc-005-data-quality-fixes
```

## 📋 CHECKLISTE VOR PR-ERSTELLUNG

- [ ] Backend kompiliert ohne Fehler
- [ ] Frontend kompiliert ohne Fehler  
- [ ] Backend Tests laufen (mindestens 80% grün)
- [ ] Frontend Tests laufen
- [ ] Services starten ohne Fehler
- [ ] Keine V120 Migration vorhanden
- [ ] V201 ist nächste freie Migration
- [ ] Manuelle Smoke-Tests bestanden

## 🎯 ERWARTETES ERGEBNIS

Der `feature/fc-005-data-quality-fixes` Branch enthält dann:
- ✅ Alle Code-Features aus sprint-2 (Contact, Cost, Help)
- ✅ V35-37 Migrations
- ✅ Data Quality APIs
- ✅ Step3 Contact Management Planung
- ✅ get-next-migration.sh Script
- ❌ KEINE V120 Migration

## 🚀 NÄCHSTE SCHRITTE (nach erfolgreichen Tests)

1. **Mini-PR für Migration-Script:**
   ```bash
   git checkout main
   git checkout -b feature/add-migration-helper
   git checkout feature/fc-005-data-quality-fixes -- scripts/get-next-migration.sh
   git push origin feature/add-migration-helper
   # → Kleine PR erstellen, schnell mergen
   ```

2. **Große PR für fc-005:**
   - Titel: "feat(FC-005): Complete Customer Management with Data Quality, Contact, Cost & Help Systems"
   - Beschreibung: Alle Features auflisten
   - CI muss grün sein!

## ⚠️ WICHTIGE HINWEISE

1. **NIEMALS** direkt pushen ohne lokale Tests
2. **IMMER** Backup-Branch behalten bis PR gemerged
3. **Bei Unsicherheit:** Lieber zu viel mergen als Code verlieren
4. **V120 muss weg:** Diese Migration ist obsolet
5. **CI-Fehler erwartet:** Sprint-2 hatte CI-Probleme, diese müssen gefixt werden

## 🔴 NOTFALL-ROLLBACK

Falls etwas schief geht:
```bash
# Zum Backup zurück
git reset --hard backup-fc-005-[timestamp]
git clean -fd

# Oder Stash wiederherstellen
git stash pop
```

## 📊 STATUS-TRACKING

| Schritt | Status | Notizen |
|---------|--------|---------|
| Backup erstellt | ⏳ | |
| Sprint-2 gemerged | ⏳ | |
| V120 entfernt | ⏳ | |
| Backend Tests | ⏳ | |
| Frontend Tests | ⏳ | |
| Lokale Validierung | ⏳ | |
| PR ready | ⏳ | |

---

**Letzte Aktualisierung:** 07.08.2025 19:14  
**Nächster Schritt:** PHASE 1 - Backup erstellen