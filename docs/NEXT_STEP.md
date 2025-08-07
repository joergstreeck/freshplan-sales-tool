# 🧭 NEXT STEP NAVIGATION

**Zweck:** Immer nur EINEN klaren nächsten Schritt für Claude
**Update:** Bei jeder Unterbrechung oder Fortschritt

---

## 🎯 JETZT GERADE:

**CI FAST GRÜN - NUR NOCH 2 TESTS ROT**

**Stand 08.08.2025 00:33:**
- ✅ **9 von 11 CI-Checks grün**
- ✅ **React Downgrade:** 19.1.0 → 18.3.1 erfolgreich
- ✅ **ContactInteractionResourceIT:** API-Pfade gefixt
- ✅ **CustomerRepositoryTest:** Pflichtfelder ergänzt
- 🔄 **Backend Integration Tests:** Noch rot (Response-Validierung)
- 🔄 **Playwright Tests:** Timeouts in CI
- 🎯 **PR #74:** Wartet auf grüne CI

**🚀 NÄCHSTER SCHRITT:**

**Letzte 2 CI-Tests fixen**

```bash
cd /Users/joergstreeck/freshplan-sales-tool

# 1. CI-Status prüfen
gh pr checks 74

# 2. Backend Integration Test Logs analysieren
gh run list --branch feature/fc-005-data-quality-fixes --limit 1
gh run view <RUN_ID> --log-failed | grep -A20 "ContactInteractionResourceIT"

# 3. Response-Validierung in Tests anpassen
# Problem: Tests erwarten andere Response-Felder als API liefert
```

**UNTERBROCHEN BEI:**
- Warten auf CI-Ergebnisse der letzten Fixes (Commits f2450d2)
- ContactInteractionResourceIT braucht noch Response-Field-Anpassungen
- Playwright-Timeouts müssen erhöht werden

**AKTUELLE POSITION:**
- ✅ Sprint 2 Integration: 100% abgeschlossen
- ✅ 58 Testkunden + 31 Opportunities: FUNKTIONSFÄHIG
- 🔄 CI-Status: 82% grün (9/11 Tests)
- 🎯 Ziel: 100% grüne CI für PR #74

**WICHTIGE DOKUMENTE:**
- **AKTUELLE Übergabe:** `/docs/claude-work/daily-work/2025-08-08/2025-08-08_HANDOVER_00-33.md` ⭐ **NEU!**
- **PR #74:** https://github.com/joergstreeck/freshplan-sales-tool/pull/74
- Migration Status: V209 als nächste verfügbare Migration  
- Sprint 2 Integration: Erfolgreich abgeschlossen

---

## ⚠️ VOR JEDER IMPLEMENTATION - REALITY CHECK PFLICHT:
```bash
# CI Status prüfen:
gh pr checks 74 | grep -E "fail|pass"
# Sollte: Nur noch 2 rote Tests zeigen

# Backend Service Status:
curl http://localhost:8080/api/ping
# Sollte: JSON Response

# Database Migration Status:
./scripts/get-next-migration.sh
# Sollte: V209 als nächste Migration anzeigen
```

---

## 📊 AKTUELLER STATUS:
```
🟢 React Kompatibilität: ✅ GELÖST (18.3.1)
🟢 Frontend Tests: ✅ 469 BESTANDEN
🟢 E2E Smoke Tests: ✅ GRÜN
🟡 Backend Integration: 🔄 FAST FERTIG (Response-Validierung)
🟡 Playwright: 🔄 TIMEOUT-ISSUES
🟢 Gesamt-CI: 82% GRÜN (9/11)
```

**Status:**
- FC-005 Sprint 2 Integration: ✅ CODE FERTIG
- CI-Stabilität: 🔄 82% → Ziel 100%
- PR #74: ⏳ Wartet auf grüne CI
- Verbleibende Arbeit: ~30-60 Minuten