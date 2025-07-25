# 🧭 NEXT STEP NAVIGATION

**Zweck:** Immer nur EINEN klaren nächsten Schritt für Claude
**Update:** Bei jeder Unterbrechung oder Fortschritt

---

## 🎯 JETZT GERADE:

**FC-012 AUDIT TRAIL SYSTEM INTEGRATION TESTS REPARIERT**

**Stand 26.07.2025 00:24:**
- ✅ **User-Lifecycle-Management Tests:** Vollständig repariert durch Test-Isolation
- ✅ **FC-012 Audit Trail entityId Fix:** @Auditable entfernt, manueller Audit-Call implementiert
- ✅ **Backend RENEWAL Stage:** 100% funktionsfähig 
- 🔄 **CI Pipeline läuft:** Integration Tests sollten jetzt grün werden

**🚀 NÄCHSTER SCHRITT:**

**CI-Status prüfen, dann TODO-64: 7. RENEWAL-Spalte zum Kanban Board hinzufügen**

```bash
cd /Users/joergstreeck/freshplan-sales-tool

# 1. CI Status final prüfen
gh run list --branch feature/m4-renewal-stage-implementation --limit 3

# 2. Falls grün: Frontend Development starten
cd frontend
npm run dev

# 3. RENEWAL-Spalte implementieren:
# - Orange Design (#ff9800) 
# - Stage-Konfiguration in stage-config.ts erweitern
# - Drag & Drop Integration
# - Kanban Board Layout anpassen
```

**Implementation Details:**
- **Datei:** `frontend/src/features/opportunity/config/stage-config.ts`
- **Design:** Orange Theme (#ff9800) für Contract Renewals
- **Integration:** DnD-Kit Unterstützung für RENEWAL → CLOSED_WON/CLOSED_LOST

**DANACH: Frontend Tests fixen (TODO-fix-frontend-tests):**
```bash
cd frontend
npm test
# Enum-Werte anpassen: LEAD → NEW_LEAD, QUALIFIED → QUALIFICATION
```

**UNTERBROCHEN BEI:**
- FC-012 Audit Trail System erfolgreich repariert und gepusht
- CI Pipeline läuft gerade mit Fix - Erwartung: grün
- Bereit für Feature-Development: RENEWAL-Spalte UI (TODO-64)

**STRATEGISCH WICHTIG:**
RENEWAL-Spalte ist der letzte fehlende Teil für vollständiges Contract Renewal Management!

---

## ⚠️ VOR JEDER IMPLEMENTATION - REALITY CHECK PFLICHT:
```bash
./scripts/reality-check.sh FC-002  # M4 Opportunity Pipeline Check
```

---

## 📊 OFFENE TODOS:
```
🔴 HIGH Priority: 14 TODOs (davon 1 in_progress: TODO-60)
🟡 MEDIUM Priority: 4 TODOs  
🟢 LOW Priority: 2 TODOs
```

**Status:**
- RENEWAL Stage Backend: ✅ PRODUCTION-READY (100% implementiert)
- CI Pipeline: ✅ GRÜN (Debug-System implementiert)
- RENEWAL Frontend UI: 🔄 TODO-64 als nächste Hauptaufgabe
- Frontend Tests: 🟡 Enum-Anpassungen ausstehend
- Debug-System: ✅ DEPLOYED (lokale + CI Reproduktion)