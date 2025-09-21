# 📚 FC-005 BACKEND ARCHITECTURE

**Navigation:** [← Zurück zur Übersicht](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/README.md) | [Tech Concept →](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/01-TECH-CONCEPT/README.md) | [Frontend →](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/03-FRONTEND/README.md)

---

**Datum:** 26.07.2025  
**Version:** 1.0  
**Status:** 🔄 In Entwicklung  

## 📋 Übersicht

Diese Dokumentation beschreibt die Backend-Architektur für das Field-Based Customer Management System (FC-005). Die Architektur basiert auf Quarkus 3.17.4 mit Java 17 und implementiert ein flexibles Field-System für dynamische Kundenfelder.

## 📁 Dokumente in diesem Bereich

| Dokument | Inhalt | Zeilen |
|----------|--------|--------|
| [01-entities.md](01-entities.md) | JPA Entities und Domain Model | ~230 |
| [02-services.md](02-services.md) | Service Layer und Business Logic | ~200 |
| [03-rest-api.md](03-rest-api.md) | REST API Endpoints und Resources | ~150 |
| [04-database.md](04-database.md) | Datenbank-Schema und Migrationen | ~190 |

## 🎯 Kern-Konzepte

### Field-Based Architecture
- **Dynamische Felder** statt starrer Entitäten
- **Field Definitions** konfigurieren verfügbare Felder
- **Field Values** speichern die tatsächlichen Werte
- **Branchenspezifische** Felder je nach Industry

### Entity-Hierarchie
```
Customer (1) ──> (n) Location (1) ──> (n) DetailedLocation
    │                    │                      │
    └──> FieldValues <───┴──────────────────────┘
```

### Technologie-Stack
- **Framework:** Quarkus 3.17.4
- **Java:** 17 LTS
- **Database:** PostgreSQL mit JSONB
- **ORM:** Hibernate mit Panache
- **Validation:** Bean Validation + Custom Validators

## 🔗 Abhängigkeiten

- **FC-012:** Audit Trail System für Change Tracking
- **Security Foundation:** Authentication & Authorization
- **Event System:** Für Module-übergreifende Kommunikation

## 🚀 Quick Links

### Backend-Entwicklung
- [Entity Model](01-entities.md) - Datenmodell verstehen
- [Service Layer](02-services.md) - Business Logic implementieren
- [REST API](03-rest-api.md) - Endpoints nutzen
- [Database](04-database.md) - Schema und Migrationen

### Related Documentation
- [Tech Concept](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/01-TECH-CONCEPT/README.md)
- [Testing Strategy](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/05-TESTING/README.md)
- [Security](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/06-SECURITY/README.md)

## 📊 Status

- ✅ Entity Design definiert
- ✅ Service Layer spezifiziert
- ✅ REST API dokumentiert
- ✅ Datenbank-Schema erstellt
- 🔄 Implementation ausstehend