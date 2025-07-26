# 🧭 NEXT STEP NAVIGATION

**Zweck:** Immer nur EINEN klaren nächsten Schritt für Claude
**Update:** Bei jeder Unterbrechung oder Fortschritt

---

## 🎯 JETZT GERADE:

**FC-005 DOKUMENTATIONS-UMSTRUKTURIERUNG - 100% FERTIG ✅**

**Stand 26.07.2025 17:15:**
- ✅ **Master Plan V5:** Auto-Sync durchgeführt + FC-005 auf 100%
- ✅ **FC-005 Umstrukturierung:** 33 von 33 Dokumenten fertig ✅
- ✅ **Fertige Bereiche:** ALLE 8 Verzeichnisse komplett (inkl. 08-IMPLEMENTATION/)
- 📋 **TODO-System:** 26 offene TODOs (4 completed)
- 🔄 **Services:** Alle 4 laufen stabil
- 📁 **Fortschritt:** Siehe RESTRUCTURING_PLAN.md

**🚀 NÄCHSTER SCHRITT:**

**FC-005 Implementation beginnen - Field Catalog JSON (todo-field-catalog)**

```bash
cd /Users/joergstreeck/freshplan-sales-tool

# OPTION A: Dokumentation Claude-tauglich machen (todo-fc005-docs-optimize)
cd docs/features/FC-005-CUSTOMER-MANAGEMENT/
# - Dokumente in 500-Zeilen Chunks aufteilen
# - Navigation mit absoluten Pfaden hinzufügen
# - Cross-References zwischen allen Docs

# OPTION B: Mit Implementation beginnen (todo-field-catalog)
cd frontend/src/features/customers/data
# - fieldCatalog.json mit 10 MVP Feldern erstellen
# - Validierungsregeln definieren
# - Industry-spezifische Felder
```

**WICHTIGE DOKUMENTE (NEUE STRUKTUR - 100% FERTIG!):**
- Hauptübersicht: `/docs/features/FC-005-CUSTOMER-MANAGEMENT/README.md` ⭐
- Quick Reference: `/docs/features/FC-005-CUSTOMER-MANAGEMENT/CLAUDE_QUICK_REFERENCE.md` 🚀
- Implementation: `/docs/features/FC-005-CUSTOMER-MANAGEMENT/08-IMPLEMENTATION/README.md` ✅ NEU
- Tech Konzept: `/docs/features/FC-005-CUSTOMER-MANAGEMENT/01-TECH-CONCEPT/README.md`
- Backend Docs: `/docs/features/FC-005-CUSTOMER-MANAGEMENT/02-BACKEND/README.md`
- Frontend Docs: `/docs/features/FC-005-CUSTOMER-MANAGEMENT/03-FRONTEND/README.md`
- Performance: `/docs/features/FC-005-CUSTOMER-MANAGEMENT/07-PERFORMANCE/README.md`
- Umstrukturierungs-Plan: `/docs/features/FC-005-CUSTOMER-MANAGEMENT/RESTRUCTURING_PLAN.md` ✅

**WICHTIGE DETAILS:**
- chainCustomer='ja' triggert Standorte-Tab
- industry bestimmt branchenspezifische Felder
- Validierungen: Deutsche PLZ, E-Mail, Telefon
- 3-stufiger Workflow: Kunde → Standorte → Details

**ABGESCHLOSSENE FEATURES:**
- ✅ M4 Opportunity Pipeline (100%)
- ✅ Customer Backend API
- ✅ Customer UI Analyse

**OFFENE PRIORITÄTEN:**
1. Customer UI Implementation (2-3 Tage)
2. FC-012 Audit Trail UI (1 Tag)
3. Security-Analyse Quarkus 3.17.4 (4h)

---

## ⚠️ VOR JEDER IMPLEMENTATION - REALITY CHECK PFLICHT:
```bash
./scripts/reality-check.sh FC-002  # M4 Opportunity Pipeline Check
```

---

## 📊 OFFENE TODOS:
```
🔴 HIGH Priority: 2 TODOs (TODO-2: CI Assertions, TODO-3: UserResourceIT)
🟡 MEDIUM Priority: 1 TODO (TODO-4: RENEWAL-Spalte)
🟢 LOW Priority: 1 TODO (TODO-5: Übergabe)
```

**Status:**
- FC-012 Audit Trail System: ✅ PRODUCTION-READY
- CI Integration Tests: 🟡 2 Assertion-Failures (lösbar in 30 Min)
- RENEWAL Backend: ✅ 100% implementiert
- RENEWAL Frontend UI: 🔄 Bereit für Implementation nach CI-Fix
- Debug-System: ✅ DEPLOYED (umfassende Dokumentation)