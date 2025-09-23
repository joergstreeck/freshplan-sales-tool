# Mock Removal Tracker (Business-Logic)

**🎯 Ziel:** Alle Mock-Daten aus Business-Logic-Pfaden entfernen
**📅 Timeline:** Sprint 1.2+ nach CQRS Foundation
**🔗 Governance:** [03_MOCK_GOVERNANCE.md](03_MOCK_GOVERNANCE.md)

## 📋 Ent-Mocking Roadmap

### **Sprint 1.2 - Cockpit (Pilot)**
- [ ] **Cockpit Components:** `MyDayColumn.tsx`, `MyDayColumnMUI.tsx`
  - Mock-Imports durch React Query + CQRS Events ersetzen
  - Test-Coverage ≥80% behalten

### **Sprint 2.1+ - Opportunity Management**
- [ ] **Kanban Board:** `KanbanBoardDndKit.tsx`, `KanbanBoard.tsx`
  - Mock-Data → API-Integration via React Query
  - Drag&Drop State Management ohne Mocks

### **Sprint 2.2+ - Customer Management**
- [ ] **Filter Components:** `IntelligentFilterBar.tsx`
  - Mock-Data → Search Service Integration
  - Performance Tests <200ms P95

### **Sprint 2.3+ - Audit System**
- [ ] **Audit Components:** `EntityAuditTimeline.tsx`, `UserActivityPanel.tsx`
  - Mock-Data → Event-Sourcing Integration
  - Admin Store Cleanup: `auditAdminStore.ts`

## 🔍 Inventory (15 Dateien mit Mock-Data)

**Business-Logic Pfade (zu bereinigen):**
- `features/cockpit/components/MyDayColumn*.tsx` (2 files)
- `features/opportunity/components/kanban/*.tsx` (3 files)
- `features/audit/components/*.tsx` (2 files)
- `store/admin/auditAdminStore.ts` (1 file)

**Test/Story Pfade (bleiben):**
- `__tests__/*.test.tsx` (4 files)
- `tests/integration/*.test.tsx` (1 file)
- `components/contacts/SmartContactCardTest.tsx` (Test-Component)

## ✅ Success Criteria

**Pro Sprint:**
- [ ] Keine neuen Mock-Imports in Business-Logic
- [ ] Test-Coverage bleibt ≥80%
- [ ] Performance-Budget eingehalten
- [ ] ESLint mock-guard läuft grün

**Gesamt-Ziel:**
- [ ] 0 Mock-Daten in `src/{app,features,lib,hooks,store}`
- [ ] CI-Guard aktiv und durchsetzend
- [ ] Dev-Seeds komplett mock-frei

---

*Automatisch erstellt aus Mock-Governance Implementation*