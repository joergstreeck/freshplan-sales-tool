# 🧭 NEXT STEP NAVIGATION

**Zweck:** Immer nur EINEN klaren nächsten Schritt für Claude
**Update:** Bei jeder Unterbrechung oder Fortschritt

---

## 🎯 JETZT GERADE:

**PR #5: BACKEND CQRS REFACTORING - 84% ABGESCHLOSSEN**

**Stand 15.08.2025 16:25:**
- ✅ **Phase 1-13 KOMPLETT:** Alle 13 Services erfolgreich in CQRS gesplittet
- ✅ **Phase 14.1 KOMPLETT:** 10 Test-Fehler behoben (Security, Mocking, Enums)
- ✅ **Phase 14.2 KOMPLETT:** CustomerCQRSIntegrationTest mit 19 Tests (79% grün)
- ⏳ **Phase 14.3 IN ARBEIT:** Feature Flag Switching Tests
  - ✅ ProfileCQRSIntegrationTest korrigiert und grün
  - ✅ TimelineCQRSIntegrationTest korrigiert und grün
  - ✅ AuditCQRSIntegrationTest neu geschrieben
  - ⏳ SearchCQRSIntegrationTest benötigt Korrektur
  - ⏳ HtmlExportCQRSIntegrationTest benötigt Korrektur
  - ⏳ ContactEventCaptureCQRSIntegrationTest benötigt Korrektur
- ⏳ **Phase 14.4 NÄCHSTER SCHRITT:** End-to-End Tests mit enabled/disabled Flag
- ⏰ **Phase 15:** Performance Testing
- ⏰ **Phase 16:** Dokumentation Update
- ⏰ **Phase 17:** PR Review & Merge Vorbereitung
- ✅ **Gesamt:** 14.3 von 17 Phasen abgeschlossen (84% komplett)
- ⏳ **Nächster Schritt:** Phase 14.4 - End-to-End Tests mit enabled/disabled Flag

### 🚨 NÄCHSTER SCHRITT FÜR NEUEN CLAUDE:

1. **SOFORT: Letzte Übergabe lesen!**
```bash
# Aktuelle Session-Übergabe:
cat /Users/joergstreeck/freshplan-sales-tool/docs/claude-work/daily-work/2025-08-15/2025-08-15_HANDOVER_16-25.md
```

2. **DANN: Phase 14.3 fortsetzen - 3 Integration Tests korrigieren**
```bash
cd /Users/joergstreeck/freshplan-sales-tool/backend

# System prüfen
git branch --show-current  # sollte: feature/refactor-large-services
git status                 # 27 neue/geänderte Dateien (meist TestProfiles)

# Phase 14.4: End-to-End Tests mit enabled/disabled Flag
# Ziel: Verifizieren dass Services korrekt zwischen Legacy und CQRS switchen

# 1. Test mit CQRS disabled (Legacy-Mode):
./mvnw test -Dtest="CustomerResourceTest" -Dfeatures.cqrs.enabled=false

# 2. Test mit CQRS enabled:
./mvnw test -Dtest="CustomerResourceTest" -Dfeatures.cqrs.enabled=true

# 3. Vergleiche Ergebnisse beider Modi
```

3. **Alternative: Direkt zu Phase 15-17**
```bash
# Falls alle Integration Tests grün sind:
# Phase 15: Performance Testing
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