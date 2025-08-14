# 🧭 NEXT STEP NAVIGATION

**Zweck:** Immer nur EINEN klaren nächsten Schritt für Claude
**Update:** Bei jeder Unterbrechung oder Fortschritt

---

## 🎯 JETZT GERADE:

**PR #5: BACKEND CQRS REFACTORING - 83% ABGESCHLOSSEN**

**Stand 15.08.2025 00:54:**
- ✅ **Phase 1-6 KOMPLETT:** CustomerService, OpportunityService, AuditService, CustomerTimelineService, SalesCockpitService, ContactService
- ✅ **Phase 7 KOMPLETT:** UserService erfolgreich in CQRS gesplittet!
- ✅ **Phase 8 KOMPLETT:** ContactInteractionService erfolgreich in CQRS gesplittet!
- ✅ **Phase 9 KOMPLETT:** TestDataService erfolgreich in CQRS gesplittet!
- ✅ **Phase 10 KOMPLETT:** SearchService erfolgreich in CQRS gesplittet!
  - ✅ SearchQueryService mit allen Such-Operationen (erste Query-Only CQRS!)
  - ✅ 43 neue Tests hinzugefügt (alle grün!)
  - ✅ Intelligente Features: Query-Type-Detection, Relevance-Scoring
- ✅ **Phase 11 KOMPLETT:** ProfileService erfolgreich in CQRS gesplittet!
  - ✅ PDF→HTML Export Innovation mit FreshPlan CI-Styling
  - ✅ Alle Tests grün, externe Dependencies eliminiert
  - ✅ **COMMIT:** 44be7f011 (13 Dateien, 1904+ Zeilen)
- ✅ **KRITISCHES PROBLEM GELÖST:** Test-Daten-Strategie (74 Kunden verfügbar)
- ✅ **Gesamt:** 11 von 12 Services refactored (92% abgeschlossen)
- ⏳ **Nächster Schritt:** Phase 12 - HelpContentService/UserStruggleDetectionService

### 🚨 NÄCHSTER SCHRITT FÜR NEUEN CLAUDE:

1. **SOFORT: Letzte Übergabe lesen!**
```bash
# Aktuelle Session-Übergabe:
cat /Users/joergstreeck/freshplan-sales-tool/docs/claude-work/daily-work/2025-08-14/2025-08-14_HANDOVER_23-18.md
```

2. **DANN: Phase 11 starten - ProfileService**
```bash
cd /Users/joergstreeck/freshplan-sales-tool/backend

# System prüfen
git branch --show-current  # sollte: feature/refactor-large-services
./mvnw quarkus:dev        # Backend starten

# ProfileService analysieren
find . -name "*ProfileService*" -type f
```

3. **Phase 9 starten: TestDataService CQRS Migration**
```bash
# Analysiere nächsten Service:
cat backend/src/main/java/de/freshplan/domain/testdata/service/TestDataService.java

# Verwende bewährte CQRS Patterns aus Phase 1-8!
# 4 Test-Fixing Patterns stehen zur Verfügung (siehe Übergabe)
```

### ⚠️ KRITISCHE REGELN:
- **Migration V219** ist die nächste freie Nummer (NICHT V10!)
- **Feature Flag bei false lassen** - noch nicht umschalten!
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