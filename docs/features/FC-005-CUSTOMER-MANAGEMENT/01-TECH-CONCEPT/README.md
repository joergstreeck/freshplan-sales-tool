---
Navigation: [⬅️ Previous](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/README.md) | [🏠 Home](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/README.md) | [➡️ Next](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/02-BACKEND/README.md)
Parent: [📁 FC-005 Customer Management](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/README.md)
Related: [🔗 Master Plan V5](/Users/joergstreeck/freshplan-sales-tool/docs/CRM_COMPLETE_MASTER_PLAN_V5.md) | [🔗 CLAUDE.md](/Users/joergstreeck/freshplan-sales-tool/CLAUDE.md)
---

# 🏗️ FC-005 TECHNISCHES KONZEPT - ÜBERSICHT

**Modul:** FC-005 Customer Management  
**Version:** 2.0 - Field-basierte Architektur  
**Status:** 📋 In Planung  

## 📋 Konzept-Dokumentation

### [01-executive-summary.md](./01-executive-summary.md)
**Management-Übersicht & Vision**
- Kernprinzipien der Field-basierten Architektur
- Business Value & ROI
- Technologie-Stack Entscheidung
- Risiken & Mitigationsstrategien

### [02-architecture-decisions.md](./02-architecture-decisions.md)
**Architecture Decision Records (ADRs)**
- ADR-005-1: Hybrid Field System
- ADR-005-2: State Management (Zustand + React Query)
- ADR-005-3: Event-Driven Communication
- ADR-005-4: Draft Persistence Strategy

### [03-data-model.md](./03-data-model.md)
**Datenmodell & Entity Design**
- Entity Relationship Diagramm
- Field-basierte Datenstruktur
- Backend Entities (Java/JPA)
- Frontend Types (TypeScript)

### [04-implementation-plan.md](./04-implementation-plan.md)
**5-Tage Implementierungsplan**
- Tag 1: Backend Foundation
- Tag 2: Field System & Persistence
- Tag 3: Frontend Store & Components
- Tag 4: Integration & Workflows
- Tag 5: Testing & Deployment

## 🎯 Kernkonzepte

### Field-basierte Architektur
```typescript
interface FieldDefinition {
  id: string
  key: string                    
  label: string                  
  entityType: 'customer' | 'location' | 'detailedLocation'
  industry?: string[]            
  fieldType: 'text' | 'number' | 'select' | 'date' | 'boolean'
  validation?: string            
  defaultValue?: any
  isCustom: boolean             
}
```

### Wizard-Flow
1. **Kunde** - Basis-Informationen & Branche
2. **Standorte** - Nur wenn `chainCustomer='ja'`
3. **Detaillierte Standorte** - Ausgabestellen pro Standort

### MVP Field Catalog
- 10 Basis-Felder für alle Branchen
- 5-10 branchenspezifische Felder
- Vorbereitung für Custom Fields (Phase 2)

## 🔗 Wichtige Referenzen

- [Legacy-Analyse](/Users/joergstreeck/freshplan-sales-tool/docs/claude-work/daily-work/2025-07-26/2025-07-26_CUSTOMER_COMPLETE_FIELD_ANALYSIS.md)
- [Backend Architecture](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/02-BACKEND/README.md)
- [Frontend Architecture](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/03-FRONTEND/README.md)
- [Implementation Checklist](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/08-IMPLEMENTATION/README.md)

## 📊 Status & Entscheidungen

| Entscheidung | Status | Begründung |
|--------------|--------|------------|
| Field-basiert statt Entity | ✅ Akzeptiert | Maximale Flexibilität |
| Keine Legacy-Migration | ✅ Akzeptiert | Greenfield Approach |
| Wizard statt Single-Form | ✅ Akzeptiert | Bessere UX |
| Draft-Persistence | ✅ Akzeptiert | User Experience |
| Custom Fields | 📋 Phase 2 | MVP First |