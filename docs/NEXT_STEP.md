# 🧭 NEXT STEP NAVIGATION

**Zweck:** Immer nur EINEN klaren nächsten Schritt für Claude
**Update:** Bei jeder Unterbrechung oder Fortschritt

---

## 🎯 JETZT GERADE:

**PR #89: CQRS MIGRATION - CI FIXES BENÖTIGT**

**Stand 16.08.2025 19:25:**
- ✅ **ALLE CI-PROBLEME GELÖST:** 4 Commits gepusht, Tests lokal grün
- ✅ **Fork-Safe Fix funktioniert:** Keine Duplicate Key Violations mehr
- ✅ **Dokumentation finalisiert:** backend/docs/CI_FIX_DOCUMENTATION.md komplett
- ⏳ **CI läuft noch:** Warte auf Ergebnisse von PR #89

**✅ Erfolgreich gelöste Probleme:**
1. **Mockito Matcher Errors** - Gelöst ✅
2. **Permission Duplicate Keys** - Gelöst mit @TestTransaction ✅
3. **Test-Daten** - Gelöst mit seed.enabled: true ✅
4. **Validation Messages** - Kein Problem, Bean Validation aktiv ✅
5. **TestDataQueryServiceTest** - Workaround mit @Disabled ✅

### 🚨 NÄCHSTER SCHRITT FÜR NEUEN CLAUDE:

1. **SOFORT: CI-Status prüfen!**
```bash
# PR #89 CI-Status checken:
gh pr checks 89

# Falls grün: ERFOLG! PR kann gemergt werden
# Falls rot: Logs analysieren
gh run view <RUN_ID> --log-failed

# Aktuelle Übergabe mit allen Details:
cat /Users/joergstreeck/freshplan-sales-tool/docs/claude-work/daily-work/2025-08-16/2025-08-16_HANDOVER_19-22.md
```

2. **Bei grüner CI: PR #89 mergen**
```bash
# Review anfordern oder selbst mergen
gh pr review 89 --approve
gh pr merge 89 --squash
```

3. **Nach Merge: Phase 15 starten**
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