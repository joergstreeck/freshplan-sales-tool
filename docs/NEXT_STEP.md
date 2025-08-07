# 🧭 NEXT STEP NAVIGATION

**Zweck:** Immer nur EINEN klaren nächsten Schritt für Claude
**Update:** Bei jeder Unterbrechung oder Fortschritt

---

## 🎯 JETZT GERADE:

**CI-VERBESSERUNGEN DURCHGEFÜHRT - BACKEND TESTS GRÜN**

**Stand 08.08.2025 00:54:**
- ✅ **Backend Test Job:** GRÜN!
- ✅ **Playwright Debug:** Umfassende Debug-Features hinzugefügt
- ✅ **Foreign Key Issues:** CustomerRepositoryTest gefixt
- 🔄 **Backend Integration Tests:** Noch kleinere Issues
- 🔄 **Playwright Tests:** Läuft mit Debug-Infos
- 🎯 **PR #74:** Fast grün (2 grün, 5 pending)

**🚀 NÄCHSTER SCHRITT:**

**CI-Ergebnisse analysieren und letzte Fixes**

```bash
cd /Users/joergstreeck/freshplan-sales-tool

# 1. CI-Status prüfen
gh pr checks 74

# 2. Playwright Artifacts downloaden (falls Timeout)
# → GitHub Actions → Run → Artifacts → playwright-report & playwright-traces

# 3. Backend Integration Test finale Fixes
# warmthScore: Integer vs Float Issue
# sentimentScore: Type Mismatch
```

**UNTERBROCHEN BEI:**
- Warten auf CI-Ergebnisse mit neuen Debug-Features
- Backend Integration Tests: warmthScore/sentimentScore Format-Issues
- Playwright läuft noch mit erweiterten Traces

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