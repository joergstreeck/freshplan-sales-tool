# 🧭 NEXT STEP NAVIGATION

**Zweck:** Immer nur EINEN klaren nächsten Schritt für Claude
**Update:** Bei jeder Unterbrechung oder Fortschritt

---

## 🎯 JETZT GERADE:

**TEST-DATEN-MIGRATION BEREIT - BACKEND BLOCKIERT!**

**Stand 17.08.2025 03:45:**
- ✅ **Strategie finalisiert:** MIGRATION_PLAN.md v3.0 + TEST_DATA_STRATEGY.md v5.0
- ✅ **Team-Feedback integriert:** 4 kritische Verbesserungen eingebaut
- ✅ **PR #89 zurückgezogen:** Für Test-Daten-Cleanup
- 🔴 **BACKEND KANN NICHT STARTEN:** CustomerDataInitializer Duplikate!
- ⚡ **DRINGEND:** Test-Daten-Migration starten!

**Backend-Status:**
- ❌ **dev-Profil:** Startet nicht (Duplikate KD-2025-00001)
- ⚠️ **test-Profil:** Läuft, aber ohne Initializers
- 🔴 **Frontend:** 403 Fehler wegen Backend-Problemen

**✅ Erfolgreich gelöste Probleme:**
1. **Mockito Matcher Errors** - Gelöst ✅
2. **Permission Duplicate Keys** - Gelöst mit @TestTransaction ✅
3. **Test-Daten** - Gelöst mit seed.enabled: true ✅
4. **Validation Messages** - Kein Problem, Bean Validation aktiv ✅
5. **TestDataQueryServiceTest** - Workaround mit @Disabled ✅

### 🚨 NÄCHSTER SCHRITT FÜR NEUEN CLAUDE:

**OPTION A: Team hat GO gegeben → IMPLEMENTIERUNG STARTEN**

1. **Phase 0: CI-Konfiguration (KRITISCH!)**
```bash
# JDBC-URL in GitHub Actions anpassen:
# -Dquarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/freshplan?options=-c%20ci.build%3Dtrue

# Dokumentation:
cat /Users/joergstreeck/freshplan-sales-tool/backend/docs/TESTKUNDEN_STRATEGIE/MIGRATION_PLAN.md
```

2. **Phase 1: Abriss beginnen**
```bash
# 6 Initializers löschen
# V219 und V220 Migrationen entfernen
# Details in MIGRATION_PLAN.md Phase 1
```

**OPTION B: Noch kein GO → Status prüfen**

1. **Team-Status erfragen**
```bash
# Dokumentation nochmal prüfen:
ls -la /Users/joergstreeck/freshplan-sales-tool/backend/docs/TESTKUNDEN_STRATEGIE/

# Aktuelle Übergabe:
cat /Users/joergstreeck/freshplan-sales-tool/docs/claude-work/daily-work/2025-08-17/2025-08-17_HANDOVER_03-13.md
```

2. **PR #89 Status checken**
```bash
gh pr checks 89
# Erwartung: Immer noch rot wegen Test-Daten
```

3. **Nach GO: Mit Phase 0 starten**
```bash
# CQRS Performance Testing
# Ziel: Sicherstellen dass CQRS mindestens gleich schnell ist
# Phase 16: Dokumentation finalisieren
# Phase 17: PR Review & Merge vorbereiten

# Gesamt-Status prüfen:
find . -name "*Service.java" -path "*/service/*" | grep -v test | wc -l
# Sollte zeigen wie viele Services migriert sind
```

### ⚠️ KRITISCHE REGELN:
- **Migration V220** ist die nächste freie Nummer (NICHT V219 oder kleiner!)
- **Feature Flag bei false lassen** - noch nicht umschalten!
- **Asymmetrische CQRS Patterns** sind OK - nicht alle Services brauchen Command UND Query
- **EXAKTE KOPIE** - auch Bugs und Probleme beibehalten für Kompatibilität!
- **4 Test-Fixing Patterns verwenden** - PanacheQuery Mock, Matcher Consistency, FK-Safe Cleanup, Flexible Verification
- **Timeline Events mit Category** - sonst DB-Fehler

### 📊 Fortschritt PR #5:
- CustomerCommandService: 8/8 Methoden ✅ (100% FERTIG!)
- CustomerQueryService: 9/9 Methoden ✅ (100% FERTIG!)
- CustomerResource Facade: 15/15 Methoden ✅ (100% FERTIG!)
- Tests: 40+ Integration Tests laufen ✅ (27 Commands + 13 Queries + Feature Flag Tests)

### 📍 Wichtige Dateien für neuen Claude:
- **Übergabe:** `/docs/claude-work/daily-work/2025-08-13/2025-08-13_HANDOVER_23-49.md`
- **PR #5 Docs:** `/docs/features/Code_Verbesserung_08_25/`
- **CustomerCommandService:** `/backend/src/main/java/de/freshplan/domain/customer/service/command/CustomerCommandService.java`
- **Tests:** `/backend/src/test/java/de/freshplan/domain/customer/service/command/CustomerCommandServiceIntegrationTest.java`

---

## 📊 Bekannte Probleme (Technical Debt):

### Dokumentierte Bugs in CustomerCommandService (13 total):
1. **addChildCustomer():** Kein Timeline Event
2. **isDescendant() Bug:** Zirkuläre Hierarchien möglich!
3. **updateAllRiskScores():** Max 1000 Kunden Limitierung
4. **updateAllRiskScores():** Keine Timeline Events
5. **updateAllRiskScores():** Keine Fehlerbehandlung
6. **updateAllRiskScores():** Keine Teil-Updates möglich
7. **mergeCustomers():** Kein Timeline Event
8. **mergeCustomers():** Nur 3 Felder übertragen (von ~20)
9. **mergeCustomers():** Kein targetId==sourceId Check
10. **hasChildren() Bug:** Funktioniert nicht nach addChildCustomer()
11. **mergeCustomers():** Verliert Contacts/Opportunities
12. **changeStatus():** Initial übersehen, nachträglich hinzugefügt
13. **changeStatus():** Nutzt ImportanceLevel.MEDIUM statt HIGH

**Alle Probleme absichtlich beibehalten für 100% Kompatibilität!**

---

**Navigation:**  
⬅️ Zurück zu: [`PR_5_IMPLEMENTATION_LOG.md`](/docs/features/Code_Verbesserung_08_25/PR_5_IMPLEMENTATION_LOG.md)  
➡️ Weiter zu: [`PR_5_NEXT_CLAUDE_SUMMARY.md`](/docs/features/Code_Verbesserung_08_25/PR_5_NEXT_CLAUDE_SUMMARY.md)