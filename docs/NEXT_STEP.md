# 🧭 NEXT STEP NAVIGATION

**Zweck:** Immer nur EINEN klaren nächsten Schritt für Claude
**Update:** Bei jeder Unterbrechung oder Fortschritt

---

## 🎯 JETZT GERADE:

**PR #5: BACKEND CQRS REFACTORING - IN ARBEIT**

**Stand 13.08.2025 23:50:**
- ✅ **Phase 1 KOMPLETT:** CustomerService erfolgreich in CQRS gesplittet!
- ✅ **CustomerCommandService** 100% FERTIG - alle 8 Methoden implementiert (inkl. changeStatus)!
- ✅ **CustomerQueryService** 100% FERTIG - alle 9 Methoden implementiert!
- ✅ **CustomerResource als Facade** 100% FERTIG - Feature Flag implementiert!
- ✅ **40+ Integration Tests** beweisen identisches Verhalten (27 Commands + 13 Queries)
- ✅ **13 Bugs dokumentiert** im Original-Code (werden beibehalten für Kompatibilität)
- ⏳ **Nächster Schritt:** Backend testen, dann Phase 2 starten (OpportunityService)

### 🚨 NÄCHSTER SCHRITT FÜR NEUEN CLAUDE:

1. **ZUERST: Status-Dokumente lesen!**
```bash
# Zusammenfassung was heute gemacht wurde:
cat /Users/joergstreeck/freshplan-sales-tool/docs/features/Code_Verbesserung_08_25/PR_5_NEXT_CLAUDE_SUMMARY.md

# Implementation Log mit allen Details:
cat /Users/joergstreeck/freshplan-sales-tool/docs/features/Code_Verbesserung_08_25/PR_5_IMPLEMENTATION_LOG.md

# Kritischen Kontext verstehen:
cat /Users/joergstreeck/freshplan-sales-tool/docs/features/Code_Verbesserung_08_25/PR_5_CRITICAL_CONTEXT.md
```

2. **DANN: Backend testen und Phase 2 beginnen**
```bash
# Phase 1 ist KOMPLETT ABGESCHLOSSEN:
# ✅ CustomerCommandService - 8/8 Methoden FERTIG
# ✅ CustomerQueryService - 9/9 Methoden FERTIG  
# ✅ CustomerResource als Facade - FERTIG mit Feature Flag
# ✅ 40+ Integration Tests - ALLE GRÜN

# NÄCHSTE SCHRITTE:
# 1. Backend starten und testen:
cd /Users/joergstreeck/freshplan-sales-tool/backend
./mvnw quarkus:dev

# 2. Beide Modi testen:
# - Legacy-Modus: features.cqrs.enabled=false
# - CQRS-Modus: features.cqrs.enabled=true
# Verhalten MUSS identisch sein!

# 3. Phase 2 starten (OpportunityService CQRS Split)
```

### ⚠️ KRITISCHE REGELN:
- **CustomerService.java NICHT ÄNDERN** - nur parallel implementieren!
- **Timeline Events mit Category** - sonst DB-Fehler
- **Feature Flag bei false lassen** - noch nicht umschalten!
- **Migration V219** ist die nächste freie Nummer (NICHT V10!)
- **CustomerCommandService ist FERTIG** - nicht mehr ändern!
- **EXAKTE KOPIE** - auch Bugs und Probleme beibehalten für Kompatibilität!

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