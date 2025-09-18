# FC-XXX: [Feature Name] - Master-Dokument

**Feature-Code:** FC-XXX
**Erstellt:** [YYYY-MM-DD]
**Kategorie:** [Mein Cockpit / Neukundengewinnung / Kundenmanagement / etc.]
**Status:** 🟡 Planung / 🔵 In Entwicklung / 🟠 Review / 🟢 Fertig
**Priorität:** 🚀 High / 📋 Medium / 🔄 Low

## 🎯 Business Context

### Problem Statement
- **Problem:** [Welches konkrete Problem löst dieses Feature?]
- **Kontext:** [In welchem Nutzungskontext tritt das Problem auf?]
- **Impact:** [Welche Auswirkungen hat das Problem aktuell?]

### User Stories
```
Als [Rolle]
möchte ich [Funktionalität]
damit ich [Nutzen/Ziel erreiche]

Akzeptanzkriterien:
- [ ] [Kriterium 1]
- [ ] [Kriterium 2]
- [ ] [Kriterium 3]
```

### Success Criteria
- [ ] **Funktional:** [Was muss funktionieren?]
- [ ] **Performance:** [Welche Performance-Anforderungen?]
- [ ] **UX:** [Welche User Experience wird erwartet?]
- [ ] **Business:** [Welche Business-Metriken werden verbessert?]

## 🏗️ Technical Foundation

### Required Foundation Components
- [ ] **Component Library:** [Welche UI-Komponenten werden benötigt?]
- [ ] **API Standards:** [Welche API-Patterns werden verwendet?]
- [ ] **Authentication:** [Welche Auth-Requirements gibt es?]
- [ ] **Database:** [Welche DB-Änderungen sind nötig?]

### API Dependencies
```yaml
# Neue Endpoints
POST /api/[resource]
GET /api/[resource]/{id}
PUT /api/[resource]/{id}
DELETE /api/[resource]/{id}

# Externe Dependencies
- Keycloak: [Welche Rollen/Permissions?]
- Database: [Welche Tabellen/Relationen?]
- External APIs: [Welche externen Services?]
```

### Database Requirements
```sql
-- Neue Tabellen/Felder
CREATE TABLE [table_name] (
    id UUID PRIMARY KEY,
    [weitere_felder],
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Migrations
-- [Migration-Strategie]
```

## 📋 Sub-Features

### Hauptkomponenten
- [ ] **FC-XXX-1:** [Sub-Feature 1 Name] - [Kurzbeschreibung]
- [ ] **FC-XXX-2:** [Sub-Feature 2 Name] - [Kurzbeschreibung]
- [ ] **FC-XXX-3:** [Sub-Feature 3 Name] - [Kurzbeschreibung]

### Implementation-Reihenfolge
1. **Phase 1 - Foundation:** [Was zuerst implementieren?]
2. **Phase 2 - Core Features:** [Kern-Funktionalität]
3. **Phase 3 - Enhancements:** [Verbesserungen und Optimierungen]

## 🎨 UI/UX Design

### Wireframes/Mockups
- [ ] **Desktop:** [Link zu Designs oder Beschreibung]
- [ ] **Mobile:** [Responsive Verhalten]
- [ ] **Accessibility:** [A11y Anforderungen]

### Design System Integration
- **Komponenten:** [Welche bestehenden Komponenten werden verwendet?]
- **Neue Komponenten:** [Welche neuen Komponenten werden erstellt?]
- **Theme:** [Farbschema, Typography, Spacing]

## 🔧 Technical Implementation

### Frontend (React + TypeScript)
```typescript
// Hauptkomponenten
components/
├── [FeatureName]/
│   ├── [FeatureName].tsx          # Hauptkomponente
│   ├── [FeatureName].test.tsx     # Tests
│   ├── [FeatureName].stories.tsx  # Storybook
│   ├── hooks/
│   │   └── use[FeatureName].ts    # Custom Hooks
│   ├── types/
│   │   └── [feature].types.ts     # TypeScript Types
│   └── utils/
│       └── [feature].utils.ts     # Utilities

// State Management
- React Query für Server State
- Zustand für lokalen State (wenn nötig)
- Context für Cross-Component State
```

### Backend (Quarkus + Java)
```java
// Package-Struktur
de.freshplan.[feature]/
├── api/
│   └── [Feature]Resource.java     // REST Controller
├── domain/
│   ├── entity/
│   │   └── [Feature]Entity.java   // JPA Entity
│   ├── repository/
│   │   └── [Feature]Repository.java // Data Access
│   └── service/
│       ├── [Feature]Service.java  // Business Logic
│       ├── dto/
│       │   ├── [Feature]RequestDto.java
│       │   └── [Feature]ResponseDto.java
│       └── mapper/
│           └── [Feature]Mapper.java // DTO Mapping
```

### Integration Points
- **Authentication:** [Wie wird Auth integriert?]
- **Authorization:** [Welche Rollen/Permissions?]
- **External APIs:** [Welche externen Services?]
- **Event System:** [Welche Events werden publiziert/konsumiert?]

## 🧪 Testing Strategy

### Test Coverage
- [ ] **Unit Tests:** >80% Coverage für Business Logic
- [ ] **Integration Tests:** API Endpoints + Database
- [ ] **Component Tests:** React Testing Library
- [ ] **E2E Tests:** Kritische User Journeys

### Test Cases
```typescript
describe('[FeatureName]', () => {
  describe('Happy Path', () => {
    it('should [expected behavior]', () => {
      // Test implementation
    });
  });

  describe('Edge Cases', () => {
    it('should handle [edge case]', () => {
      // Test implementation
    });
  });

  describe('Error Cases', () => {
    it('should handle [error scenario]', () => {
      // Test implementation
    });
  });
});
```

## 📊 Performance Considerations

### Performance Targets
- **API Response Time:** < 200ms P95
- **Frontend Render:** < 100ms First Contentful Paint
- **Bundle Size Impact:** < 50KB additional
- **Database Queries:** Optimierte Queries, keine N+1 Probleme

### Optimization Strategies
- [ ] **Lazy Loading:** Komponenten und Daten
- [ ] **Caching:** React Query + Backend Caching
- [ ] **Pagination:** Für Listen mit vielen Einträgen
- [ ] **Memoization:** Für teure Berechnungen

## 🔗 Code References

### Frontend
- **Hauptkomponente:** `/frontend/src/features/[feature]/`
- **Shared Components:** `/frontend/src/components/`
- **API Client:** `/frontend/src/services/api/`
- **Types:** `/frontend/src/types/`

### Backend
- **REST API:** `/backend/src/main/java/de/freshplan/[feature]/api/`
- **Business Logic:** `/backend/src/main/java/de/freshplan/[feature]/domain/service/`
- **Database:** `/backend/src/main/java/de/freshplan/[feature]/domain/entity/`
- **Migrations:** `/backend/src/main/resources/db/migration/`

### Tests
- **Frontend Tests:** `/frontend/src/features/[feature]/__tests__/`
- **Backend Tests:** `/backend/src/test/java/de/freshplan/[feature]/`
- **E2E Tests:** `/e2e/tests/[feature]/`

## 📊 Implementation Status

### Foundation
- [ ] **Component Library:** 🟡 Vorbereitung / 🔵 In Arbeit / 🟢 Fertig
- [ ] **API Standards:** 🟡 Vorbereitung / 🔵 In Arbeit / 🟢 Fertig
- [ ] **Database Schema:** 🟡 Vorbereitung / 🔵 In Arbeit / 🟢 Fertig

### Backend
- [ ] **Entity Model:** 🟡 Vorbereitung / 🔵 In Arbeit / 🟢 Fertig
- [ ] **Repository Layer:** 🟡 Vorbereitung / 🔵 In Arbeit / 🟢 Fertig
- [ ] **Service Layer:** 🟡 Vorbereitung / 🔵 In Arbeit / 🟢 Fertig
- [ ] **REST API:** 🟡 Vorbereitung / 🔵 In Arbeit / 🟢 Fertig

### Frontend
- [ ] **Components:** 🟡 Vorbereitung / 🔵 In Arbeit / 🟢 Fertig
- [ ] **State Management:** 🟡 Vorbereitung / 🔵 In Arbeit / 🟢 Fertig
- [ ] **API Integration:** 🟡 Vorbereitung / 🔵 In Arbeit / 🟢 Fertig
- [ ] **Routing:** 🟡 Vorbereitung / 🔵 In Arbeit / 🟢 Fertig

### Testing
- [ ] **Unit Tests:** 🟡 Vorbereitung / 🔵 In Arbeit / 🟢 Fertig
- [ ] **Integration Tests:** 🟡 Vorbereitung / 🔵 In Arbeit / 🟢 Fertig
- [ ] **E2E Tests:** 🟡 Vorbereitung / 🔵 In Arbeit / 🟢 Fertig

## 🚀 Migration Notes

### Von Legacy-System
- [ ] **Daten-Migration:** [Welche Daten müssen migriert werden?]
- [ ] **Feature-Mapping:** [Wie mappt das Feature zu Legacy-Funktionen?]
- [ ] **Rollback-Strategie:** [Was passiert wenn Rollback nötig ist?]

### Deployment-Strategie
- [ ] **Feature Flags:** [Welche Feature Flags werden benötigt?]
- [ ] **Database Migrations:** [Welche DB-Änderungen sind nötig?]
- [ ] **Monitoring:** [Welche Metriken werden überwacht?]

## 📝 Notes & Decisions

### Architecture Decisions
- **[Datum]:** [Entscheidung 1 mit Begründung]
- **[Datum]:** [Entscheidung 2 mit Begründung]

### Open Questions
- [ ] **Question 1:** [Offene Frage mit Kontext]
- [ ] **Question 2:** [Offene Frage mit Kontext]

### Blockers & Dependencies
- [ ] **Blocker 1:** [Beschreibung und Owner]
- [ ] **Dependency 1:** [Abhängigkeit und Status]

---

**📋 Template verwendet:** FEATURE_TEMPLATE.md v1.0
**📅 Letzte Aktualisierung:** [YYYY-MM-DD]
**👨‍💻 Owner:** [Team/Person]

**🎯 Dieses Dokument ist der Single Source of Truth für Feature FC-XXX**