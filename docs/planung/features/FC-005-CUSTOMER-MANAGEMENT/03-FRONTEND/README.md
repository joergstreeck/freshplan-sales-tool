# 📚 FC-005 FRONTEND ARCHITECTURE

**Navigation:** [← Backend](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/02-BACKEND/README.md) | [Integration →](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/04-INTEGRATION/README.md)

---

**Datum:** 26.07.2025  
**Version:** 1.0  
**Stack:** React 18, TypeScript, Material-UI, Zustand, React Query  

## 📋 Übersicht

Diese Dokumentation beschreibt die Frontend-Architektur für das Field-Based Customer Management System (FC-005). Die Implementierung basiert auf React 18 mit TypeScript und nutzt ein dynamisches Field-Rendering-System für flexible Kundenfelder.

## 📁 Dokumente in diesem Bereich

| Dokument | Inhalt | Zeilen |
|----------|--------|--------|
| [01-components.md](01-components.md) | React Components und UI-Struktur | ~250 |
| [02-state-management.md](02-state-management.md) | Zustand Store und State Logic | ~240 |
| [03-field-rendering.md](03-field-rendering.md) | Dynamisches Field-Rendering System | ~200 |
| [04-validation.md](04-validation.md) | Validierung und Form Handling | ~180 |

## 🎯 Kern-Konzepte

### Wizard-basiertes Onboarding
- **3-Schritte-Prozess**: Stammdaten → Standorte → Details
- **Dynamische Steps**: Basierend auf Kundeneigenschaften
- **Auto-Save**: Kontinuierliche Draft-Speicherung
- **Progress Tracking**: Visueller Fortschrittsindikator

### Technologie-Stack
- **UI Framework:** React 18 mit TypeScript
- **Component Library:** Material-UI v5
- **State Management:** Zustand mit Persist Middleware
- **Server State:** React Query v5
- **Validation:** Zod Schema Validation
- **Forms:** React Hook Form Integration

### Folder Structure
```
frontend/src/features/customers/
├── components/     # UI Components
├── hooks/         # Custom React Hooks
├── stores/        # Zustand Stores
├── services/      # API Services
├── types/         # TypeScript Types
├── utils/         # Helper Functions
├── schemas/       # Validation Schemas
└── data/          # Static Data (Field Catalog)
```

## 🔗 Abhängigkeiten

- **Design System:** Freshfoodz Corporate Identity
- **Auth Context:** Keycloak Integration
- **API Client:** Axios mit Interceptors
- **Field Definitions:** Backend-gesteuert

## 🚀 Quick Links

### Frontend-Entwicklung
- [Components](01-components.md) - UI-Komponenten verstehen
- [State Management](02-state-management.md) - Zustand verwalten
- [Field Rendering](03-field-rendering.md) - Dynamische Felder
- [Validation](04-validation.md) - Form Validierung

### Related Documentation
- [Backend API](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/02-BACKEND/03-rest-api.md)
- [Testing](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/05-TESTING/README.md)
- [Performance](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/07-PERFORMANCE/README.md)

## 📊 Status

- ✅ Component Architecture definiert
- ✅ State Management spezifiziert
- ✅ Field Rendering System dokumentiert
- ✅ Validation Framework erstellt
- 🔄 Implementation ausstehend

## 🎨 UI/UX Highlights

- **Responsive Design**: Mobile-first Approach
- **Accessibility**: WCAG 2.1 AA konform
- **Performance**: Lazy Loading & Code Splitting
- **User Experience**: Inline Validation & Auto-Save