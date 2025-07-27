# 🧭 NEXT STEP NAVIGATION

**Zweck:** Immer nur EINEN klaren nächsten Schritt für Claude
**Update:** Bei jeder Unterbrechung oder Fortschritt

---

## 🎯 JETZT GERADE:

**SPRINT 2 TAG 1 - TYPESCRIPT IMPORT TYPE FEHLER SYSTEMATISCH BEHEBEN**

**Stand 27.07.2025 20:45:**
- ✅ **Sprint 2 Tag 1 ABGESCHLOSSEN:** Alle Features implementiert, alle Import-Fehler behoben
- ✅ **Dokumentation VOLLSTÄNDIG:** TypeScript Import Type Guide + Integration in alle Hauptdokumente
- ✅ **CustomersPageV2 läuft perfekt!** Bereit zum Testen
- 🎯 **Status:** Erfolgreiche Session - bereit für Sprint 2 Tag 2

**🚀 NÄCHSTER SCHRITT:**

**Sprint 2 Tag 1 Features testen, committen, dann Sprint 2 Tag 2 beginnen (Task Engine)**

```bash
cd /Users/joergstreeck/freshplan-sales-tool

# Option A: CR-003 Configuration Data externalisieren  
# 1. DetailedLocationsStep.tsx:72-134 categoryIcons, industryTemplates auslagern
# 2. Separate config files für bessere Wartbarkeit erstellen
# 3. Template-System für branchenspezifische Vorkonfigurationen

# Option B: FC-005 UI Integration - CustomerOnboardingWizard einbinden
# 1. Komponenten sind bereit, müssen nur in UI integriert werden  
# 2. "Neuen Kunden anlegen" Button zur bestehenden Kundenliste hinzufügen
# 3. Field-Catalog JSON aktivieren für Dynamic Forms

# Tests validieren (sollten alle grün sein):
cd frontend
npm test -- --run DynamicFieldRenderer
npm test -- --run ConditionalFieldsLive
npm test -- --run StoreDynamicValidationSimple

# Pull Request Status prüfen:
gh pr view 70
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