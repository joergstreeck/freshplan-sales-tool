# 🔗 FC-005 INTEGRATION - ÜBERSICHT

**Navigation:**
- **Parent:** [FC-005 Customer Management](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/README.md)
- **Related:** [01-TECH-CONCEPT](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/01-TECH-CONCEPT/README.md) | [02-BACKEND](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/02-BACKEND/README.md) | [03-FRONTEND](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/03-FRONTEND/README.md)

---

## 📋 Übersicht

Dieses Verzeichnis dokumentiert alle Integrationspunkte des Customer Management Moduls mit anderen Systembereichen. Customer ist die zentrale Entity und hat weitreichende Verbindungen zu fast allen anderen Modulen.

## 📂 Struktur

### [01-module-dependencies.md](./01-module-dependencies.md)
Detaillierte Dokumentation der Abhängigkeiten zu anderen Modulen:
- M4 Opportunity Pipeline Integration
- FC-012 Audit Trail Integration
- FC-009 Contract Management Integration
- FC-003 Email Integration
- Security & Permissions Integration
- Abhängigkeitsmatrix

### [02-event-system.md](./02-event-system.md)
Event-driven Architecture für Customer Management:
- Domain Events Definition
- Event Publisher Implementation
- Event Subscriber Patterns
- Event Flow und Orchestrierung

### [03-api-gateway.md](./03-api-gateway.md)
API Gateway Pattern für aggregierte Daten:
- Customer Aggregate API
- Parallel Data Fetching
- Frontend Integration Hooks
- Performance Optimierungen

## 🎯 Schlüssel-Integrationen

### Kritische Abhängigkeiten
- **M4 Opportunity Pipeline:** Customer-Daten für Opportunities
- **FC-009 Contracts:** Vertrags-Customer-Beziehung
- **Security Service:** Berechtigungsprüfungen

### Event-getriebene Updates
- CustomerCreatedEvent
- CustomerStatusChangedEvent
- CustomerFieldUpdatedEvent
- LocationAddedEvent

### API Aggregation
- Complete Customer View mit allen verknüpften Daten
- Optimierte Queries durch paralleles Fetching
- Caching-Strategien für Performance

## 🚀 Quick Links

- **Technisches Konzept:** [Integration Architecture](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/01-TECH-CONCEPT/02-architecture-decisions.md)
- **Backend Services:** [Service Layer](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/02-BACKEND/02-services.md)
- **Frontend State:** [State Management](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/03-FRONTEND/02-state-management.md)
- **Testing:** [Integration Tests](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/05-TESTING/02-integration-tests.md)