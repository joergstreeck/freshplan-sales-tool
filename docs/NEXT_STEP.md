# 🧭 NEXT STEP NAVIGATION

**Zweck:** Immer nur EINEN klaren nächsten Schritt für Claude
**Update:** Bei jeder Unterbrechung oder Fortschritt

---

## 🎯 JETZT GERADE:

**CI FAST GRÜN - NUR NOCH PLAYWRIGHT TEST ROT**

**Stand 08.08.2025 01:38:**
- ✅ **Backend Tests:** ALLE GRÜN!
- ✅ **Integration Tests:** GRÜN!
- ✅ **E2E Smoke:** GRÜN!
- ✅ **Lint/Quality:** ALLE GRÜN!
- 🔄 **Playwright:** SPA-Routing Fix deployed, läuft noch
- 🎯 **PR #74:** 8/9 Tests grün (89%)

**🚀 NÄCHSTER SCHRITT:**

**CI-Status prüfen und PR mergen**

```bash
cd /Users/joergstreeck/freshplan-sales-tool

# 1. CI-Status prüfen
gh pr checks 74

# 2. Falls Playwright grün → PR mergen
gh pr merge 74 --squash

# 3. Falls noch rot → Logs checken
gh run view [RUN-ID] --log-failed | grep -A 10 "Error:"
```

**UNTERBROCHEN BEI:**
- Warten auf Playwright CI-Ergebnis mit SPA-Routing Fix
- Fix ist deployed: `npx serve -s` für SPA-Mode
- Erwarte grüne CI in wenigen Minuten

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