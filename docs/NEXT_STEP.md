# 🧭 NEXT STEP NAVIGATION

**Zweck:** Immer nur EINEN klaren nächsten Schritt für Claude
**Update:** Bei jeder Unterbrechung oder Fortschritt

---

## 🎯 JETZT GERADE:

**M4 RENEWAL-SPALTE IMPLEMENTATION - KANBAN BOARD ERWEITERUNG**

**Stand 25.07.2025 23:58:**
- ✅ **CI Pipeline ist GRÜN!** ESLint strategisch behoben (23 → 7 warnings)
- ✅ **Debug-System implementiert!** Umfassendes CI-Debugging für Zukunft
- ✅ **Backend RENEWAL Stage:** 100% funktionsfähig
- 🔄 **Frontend UI fehlt:** 7. Kanban-Spalte für RENEWAL Stage

**🚀 NÄCHSTER SCHRITT:**

**TODO-64: 7. RENEWAL-Spalte zum Kanban Board hinzufügen**

```bash
cd /Users/joergstreeck/freshplan-sales-tool

# 1. CI Status final prüfen
gh run list --branch feature/m4-renewal-stage-implementation --limit 3

# 2. Frontend Development starten
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
- CI Pipeline erfolgreich debuggt und repariert
- Debug-System für zukünftige CI-Probleme implementiert  
- Bereit für Feature-Development: RENEWAL-Spalte UI

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