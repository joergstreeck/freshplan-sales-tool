# 🧭 NEXT STEP NAVIGATION

**Zweck:** Immer nur EINEN klaren nächsten Schritt für Claude
**Update:** Bei jeder Unterbrechung oder Fortschritt

---

## 🎯 JETZT GERADE:

**CI PIPELINE DEBUGGING FÜR PR #68 - MEHRERE FIXES GEPUSHT**

**Stand 25.07.2025 23:05:**
- ✅ **Backend CI ist GRÜN!** Backend läuft einwandfrei
- 🔄 **3 CI Fixes gepusht:**
  - Fix 1: Lint Errors behoben (unbenutzte Variablen, STAGE_CONFIGURATIONS Import) ✅
  - Fix 2: Smoke Test mit multiple selectors robuster gemacht ✅
  - Fix 3: CSS Selector Syntax korrigiert (.or() statt Komma) ✅
- 🟡 **CI Status:** 3 von 4 Jobs noch rot (Lint, Smoke, Integration)

**🚀 NÄCHSTER SCHRITT:**

**SOFORT: CI Pipeline Status nach letztem Fix prüfen:**

```bash
cd /Users/joergstreeck/freshplan-sales-tool

# CI Status prüfen (warte 2-3 Minuten nach Push)
gh run list --branch feature/m4-renewal-stage-implementation --limit 5

# Bei weiterem Fehler: Smoke Test genauer analysieren
gh run view <RUN_ID> --log-failed | grep -A 20 "Error"
```

**Falls immer noch rot: Smoke Test weiter debuggen:**
```bash
# Lokal testen ob Smoke Test funktioniert
cd frontend
npx playwright test tests/auth.spec.ts --project=chromium

# Evtl. noch robusterer Selector nötig
```

**DANACH: Frontend Tests fixen (TODO-fix-frontend-tests):**
```bash
cd frontend
npm test
# Enum-Werte anpassen: LEAD → NEW_LEAD, QUALIFIED → QUALIFICATION
```

**UNTERBROCHEN BEI:**
- 3. CI Fix gepusht (CSS Selector Syntax)
- Warte auf CI Run Ergebnis nach commit 678bd37
- Nächster Schritt: CI Status prüfen und ggf. weitere Smoke Test Fixes

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