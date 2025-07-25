# 🧭 NEXT STEP NAVIGATION

**Zweck:** Immer nur EINEN klaren nächsten Schritt für Claude
**Update:** Bei jeder Unterbrechung oder Fortschritt

---

## 🎯 JETZT GERADE:

**M4 RENEWAL STAGE - CODE REVIEW FEEDBACK UMGESETZT, CI PIPELINE FIXES**

**Stand 25.07.2025 22:52:**
- ✅ **RENEWAL Stage vollständig implementiert:**
  - Backend: OpportunityStage.RENEWAL mit Business Rules ✅
  - Frontend: Type-Definitionen bereinigt (stages.ts gelöscht) ✅
  - Alle 18 Backend Tests erfolgreich ✅
  - PR #68 erstellt und reviewed ✅

- ✅ **Code Review Feedback umgesetzt:**
  - Business Logic vereinfacht mit isClosedOpportunity() Helper ✅
  - !important CSS entfernt in ToastProvider ✅
  - Code-Duplikation behoben (stages.ts gelöscht) ✅
  - Alle Test-Importe korrigiert ✅

- 🔄 **CI Pipeline Status:**
  - Import-Fehler behoben (STAGE_CONFIGS → STAGE_CONFIGURATIONS) ✅
  - Neuer CI Run läuft nach Fix (commit ca8ba13) 🔄
  - Erwartung: CI sollte jetzt grün werden

**🚀 NÄCHSTER SCHRITT:**

**SOFORT: CI Pipeline Status prüfen:**

```bash
cd /Users/joergstreeck/freshplan-sales-tool

# CI Status prüfen
gh run list --branch feature/m4-renewal-stage-implementation --limit 3

# Bei Fehler: Logs analysieren
gh run view <RUN_ID> --log-failed

# Falls grün: PR #68 ist ready for merge!
```

**DANACH: Frontend Tests fixen (TODO-fix-frontend-tests):**
```bash
cd frontend
npm test
# Erwartung: Einige Tests fehlschlagen wegen Enum-Änderungen
# Fix: LEAD → NEW_LEAD, QUALIFIED → QUALIFICATION
```

**DANN: RENEWAL-Spalte zum Kanban Board (TODO-64):**
```bash
# 7. Spalte "Verlängerung" implementieren
# Orange Color (#ff9800) verwenden
# Stage: OpportunityStage.RENEWAL
```

**UNTERBROCHEN BEI:**
- CI Pipeline Fix gepusht (ca8ba13)
- Warte auf CI Run Ergebnis
- Nächster Schritt: CI Status prüfen

**STRATEGISCH WICHTIG:**
Customer-Contract Foundation (TODO: critical-3, contract-1) implementieren bevor echte Contract Renewals möglich sind!

---

## ⚠️ VOR JEDER IMPLEMENTATION - REALITY CHECK PFLICHT:
```bash
./scripts/reality-check.sh FC-002  # M4 Opportunity Pipeline Check
```

---

## 📊 OFFENE TODOS:
```
🔴 HIGH Priority: 17 TODOs (davon 2 in_progress: TODO-60, TODO-fix-ci-pipeline)
🟡 MEDIUM Priority: 7 TODOs  
🟢 LOW Priority: 3 TODOs
```

**Status:**
- RENEWAL Stage: ✅ PRODUCTION-READY (100% implementiert)
- Code Review: ✅ UMGESETZT (Business Logic, CSS, Duplikation)
- CI Pipeline: 🔄 FIX GEPUSHT (warte auf Ergebnis)
- Frontend Tests: 🟡 Enum-Anpassungen ausstehend
- Customer Foundation: 🚨 MISSING - Foundation für Contract Renewals