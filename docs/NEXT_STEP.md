# 🧭 NEXT STEP NAVIGATION

**Zweck:** Immer nur EINEN klaren nächsten Schritt für Claude
**Update:** Bei jeder Unterbrechung oder Fortschritt

---

## 🎯 JETZT GERADE:

**FC-005 STEP3 PHASE 2 - CODE ALIGNMENT ENTSCHEIDUNG NÖTIG**

**Stand 08.08.2025 03:22:**
- ✅ **PR #75:** ERFOLGREICH GEMERGED
- ✅ **Plan vs Code Analyse:** Abgeschlossen
- ✅ **Backend:** Läuft stabil mit 58 Testkunden + 31 Opportunities
- ⚠️ **Entscheidung nötig:** Alignment-Strategie wählen

**🚀 NÄCHSTER SCHRITT:**

**Entscheidung treffen und Code-Alignment starten**

```bash
cd /Users/joergstreeck/freshplan-sales-tool

# 1. Backend Status prüfen
cd backend && ./mvnw quarkus:dev

# 2. Branch Status prüfen
git status
# Aktuell: feature/fc-005-data-quality-fixes

# 3. ENTSCHEIDUNG ERFORDERLICH:
# Option 1: Vollständiges Alignment (23h) - EMPFOHLEN
# Option 2: Mit vorhandenem arbeiten (0h)
# Option 3: Hybrid-Ansatz (12h)

# Bei Option 1 (empfohlen):
touch backend/src/main/java/de/freshplan/domain/customer/entity/ContactRole.java
touch backend/src/main/resources/db/migration/V209__add_contact_roles.sql
```

**UNTERBROCHEN BEI:**
- Übergabe-Dokument erstellt
- Entscheidung über Alignment-Strategie ausstehend

**AKTUELLE POSITION:**
- ✅ Plan vs Code Analyse: KOMPLETT
- ✅ Backend: LÄUFT FEHLERFREI
- 🔄 Code-Alignment: BEREIT ZUM START
- 🎯 Nächstes: Alignment-Strategie wählen und implementieren

**WICHTIGE DOKUMENTE:**
- **AKTUELLE Übergabe:** `/docs/claude-work/daily-work/2025-08-08/2025-08-08_HANDOVER_03-22.md` ⭐ **NEU!**
- **Plan vs Code:** `/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/step3/PLAN_VS_CODE_COMPARISON.md`
- **Alignment Plan:** `/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/step3/ALIGNMENT_PLAN.md`
- Migration Status: V208 aktuell, V209 als nächste verfügbar
- Branch: `feature/fc-005-data-quality-fixes`

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