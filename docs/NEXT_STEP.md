# 🧭 NEXT STEP NAVIGATION

**Zweck:** Immer nur EINEN klaren nächsten Schritt für Claude
**Update:** Bei jeder Unterbrechung oder Fortschritt

---

## 🎯 JETZT GERADE:

**M4 OPPORTUNITY PIPELINE - BACKEND TESTS VOLLSTÄNDIG**

**Stand 23.07.2025 01:25:**
- ✅ **ERFOLG:** OpportunityDatabaseIntegrationTest vollständig implementiert! (TODO-35)
- ✅ M4 Backend Tests: 5/6 Test-Klassen vollständig grün (83% fertig)
- ✅ Database Integration Pattern erfolgreich etabliert
- ✅ Business Logic Erkenntnisse gewonnen (setStage() Behavior, Audit Fields)
- ✅ Customer.java + CustomerRepository.java vollständig vorhanden
- ✅ M4 Backend: Vollständig implementiert mit umfassender Test-Coverage
- ❌ Security-Problem: 401 Unauthorized bei Integration Tests (TODO-41)
- ❌ CDI-Problem: OpportunityServiceStageTransitionTest ArcUndeclaredThrowable (TODO-40)

**🚀 NÄCHSTER SCHRITT:**

**M4 FRONTEND IMPLEMENTIEREN (TODO-26) - HIGH PRIORITY:**
```bash
# Kanban Board für Opportunity Pipeline erstellen
# React Components für CRUD Operations
# Integration mit Backend API

cd frontend
npm run dev
# Erstelle neue Komponenten:
# - OpportunityKanbanBoard.tsx
# - OpportunityCard.tsx
# - OpportunityForm.tsx
```

**ALTERNATIVER SCHRITT (Backend Tests):**
**STAGE TRANSITION TEST FIX (TODO-40):**
```bash
cd backend
./mvnw test -Dtest=OpportunityServiceStageTransitionTest
# Analysiere ArcUndeclaredThrowable Problem
# Evtl. ähnlicher Ansatz wie bei Database Integration Test
```

**STATUS:**
- M4 Backend: ✅ Vollständig implementiert
- M4 Database: ✅ Migrations erfolgreich 
- M4 Tests: ✅ 5/6 Klassen grün (OpportunityDatabaseIntegrationTest ✅ NEU HEUTE)
- M4 Frontend: ⏳ Noch nicht begonnen → **NÄCHSTES ZIEL**

---

## ⚠️ VOR JEDER IMPLEMENTATION - REALITY CHECK PFLICHT:
```bash
./scripts/reality-check.sh FC-002  # M4 Opportunity Pipeline Check
```

---

## 📊 OFFENE TODOS:
```
🔴 HIGH Priority: 2 TODOs (26, 40)
🟡 MEDIUM Priority: 3 TODOs (34, 35, 41)
🟢 LOW Priority: 1 TODO (11)
```

---

## 🚀 Nach Mapper-Fix:
**Nächste Schritte in Reihenfolge:**
1. OpportunityDatabaseIntegrationTest implementieren (TODO-35)
2. M4 Frontend Kanban Board (TODO-26)
3. Security-Problem reparieren (TODO-41)
4. OpportunityServiceStageTransitionTest ArcUndeclaredThrowable lösen (TODO-40)