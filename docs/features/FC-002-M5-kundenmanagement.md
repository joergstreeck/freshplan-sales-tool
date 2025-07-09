# FC-002-M5: Kundenmanagement

**Modul:** M5  
**Feature:** FC-002  
**Status:** 📋 In Planung (0%)  
**Geschätzter Aufwand:** 4-5 Tage  

## 📋 Übersicht

Das zentrale CRM-Modul mit:
- Kundenliste (erweiterte Ansicht von FC-001)
- Kundendetails (360° Ansicht)
- Verkaufschancen (Opportunities)
- Aktivitätsverwaltung

## 🎯 Ziele

- Vollständige Kundenübersicht
- Effiziente Verwaltung von Verkaufschancen
- Lückenlose Aktivitätsdokumentation

## 📝 Detaillierte Spezifikation

### Vision der neuen Kundenmanagement-Architektur

Das Kundenmanagement-Modul wird das Herzstück des CRM-Systems. Es muss modular, erweiterbar und performant sein, während es gleichzeitig eine umfassende 360°-Sicht auf alle Kundeninteraktionen bietet.

## 🔍 Analyse & Integration des Backend-Bestandscodes

### Übersicht der vorhandenen Komponenten

Das aktuelle Customer-Modul umfasst **54 Java-Dateien** und ist ein massiver, eng gekoppelter Monolith. Die Hauptkomponenten sind:

#### 1. Entity-Layer (20 Dateien)
- **Customer.java** - Zentrale Entity mit >30 Feldern
- **CustomerContact.java** - Kontaktpersonen
- **CustomerAddress.java** - Adressverwaltung
- **CustomerLocation.java** - Standorte
- **CustomerTimelineEvent.java** - Historie/Timeline
- 15 Enum-Klassen für verschiedene Klassifizierungen

#### 2. Repository-Layer (3 Dateien)
- **CustomerRepository.java** - Haupt-Repository
- **CustomerTimelineRepository.java** - Timeline-Zugriff
- **CustomerTimelineEventRepository.java** - Event-Verwaltung

#### 3. Service-Layer (5 Dateien)
- **CustomerService.java** - Monolithischer Haupt-Service
- **CustomerSearchService.java** - Such-Funktionalität
- **CustomerTimelineService.java** - Timeline-Verwaltung
- **CustomerNumberGeneratorService.java** - Nummer-Generierung
- **CustomerQueryBuilder.java** - Query-Konstruktion

#### 4. DTO-Layer (20 Dateien)
- Request/Response DTOs für alle Operationen
- Spezielle Timeline-DTOs
- Such- und Filter-DTOs

#### 5. Exception & Mapper (6 Dateien)
- Domain-spezifische Exceptions
- Entity-zu-DTO Mapper

### Detaillierte Komponenten-Analyse

#### Customer Entity (Customer.java)

**Was ist vorhanden?**
- Massive JPA-Entity mit über 30 Feldern
- Hierarchie-Unterstützung (Parent/Child-Beziehungen)
- Soft-Delete-Funktionalität
- Umfangreiche Business-Felder (Financial, Status, Classification)
- 15+ Enum-Typen für verschiedene Klassifizierungen

**Was tut es?**
- Repräsentiert die zentrale Kunden-Entity
- Verwaltet komplexe Geschäftsbeziehungen
- Unterstützt Kunden-Hierarchien
- Trackt Lifecycle und Status

**Brauchen wir es in der neuen Welt?**
- ❗ **TEILWEISE** - Die Entity ist zu groß und verletzt Single Responsibility

**Was müssen wir ändern?**
1. **Aufspaltung in kleinere Aggregates:**
   - `Customer` (Kern-Daten: ID, Name, Nummer, Status)
   - `CustomerFinancials` (Finanzdaten separat)
   - `CustomerClassification` (Industry, Type, etc.)
   - `CustomerHierarchy` (Parent/Child als eigenes Aggregate)

2. **Value Objects einführen:**
   - `CustomerNumber` als Value Object
   - `CreditLimit` mit Validierung
   - `PaymentTerms` mit Business-Logik

**Wie fügen wir es ein?**
- Phase 1: Neue kleinere Entities parallel anlegen
- Phase 2: Migration der Daten
- Phase 3: Alte Entity deprecaten

#### CustomerService (CustomerService.java)

**Was ist vorhanden?**
- Monolithischer Service mit 1000+ Zeilen
- CRUD-Operationen
- Business-Logik (Duplikat-Checks, Nummer-Generierung)
- Timeline-Integration
- Hierarchie-Management

**Was tut es?**
- Orchestriert alle Customer-Operationen
- Validiert Business-Rules
- Managed Transaktionen
- Erstellt Timeline-Events

**Brauchen wir es in der neuen Welt?**
- ❗ **NEIN in dieser Form** - Muss aufgeteilt werden

**Was müssen wir ändern?**
1. **Domain Services aufteilen:**
   ```
   CustomerCommandService (Create, Update, Delete)
   CustomerQueryService (Read, Search)
   CustomerValidationService (Business Rules)
   CustomerNumberService (Nummer-Generierung)
   CustomerHierarchyService (Parent/Child)
   ```

2. **Event-Driven Architecture:**
   - Domain Events publizieren
   - Timeline als Event-Subscriber
   - Lose Kopplung zwischen Services

**Wie fügen wir es ein?**
- Facade-Pattern für Übergang
- Schrittweise Funktionen extrahieren
- Tests parallel aufbauen

#### CustomerRepository

**Was ist vorhanden?**
- Panache-basiertes Repository
- Custom Query-Methoden
- Such-Funktionalität
- Soft-Delete-Support

**Was tut es?**
- Datenzugriff auf Customer-Tabelle
- Komplexe Queries für Suche
- Performance-optimierte Abfragen

**Brauchen wir es in der neuen Welt?**
- ✅ **JA** - Aber mit Anpassungen

**Was müssen wir ändern?**
1. **Repository pro Aggregate:**
   - `CustomerRepository` (nur Kern)
   - `CustomerFinancialsRepository`
   - `CustomerContactRepository`

2. **CQRS-Pattern implementieren:**
   - `CustomerWriteRepository`
   - `CustomerReadRepository` (mit Projektionen)

**Wie fügen wir es ein?**
- Bestehende Queries analysieren
- Neue Repositories schrittweise einführen
- Read-Models für Performance

#### Timeline-System

**Was ist vorhanden?**
- `CustomerTimelineEvent` Entity
- `CustomerTimelineService`
- Verschiedene Event-Kategorien
- Timeline-DTOs

**Was tut es?**
- Trackt alle Kunden-Aktivitäten
- Erstellt Audit-Trail
- Bietet Historie-Ansicht

**Brauchen wir es in der neuen Welt?**
- ✅ **JA** - Aber als separates Bounded Context

**Was müssen wir ändern?**
1. **Event Sourcing Light:**
   - Timeline als Event Store
   - Domain Events als Quelle
   - Projektionen für Queries

2. **Separation of Concerns:**
   - Timeline als eigenes Modul
   - Async Event Processing
   - Eigene API für Timeline

### 🚨 Technische Risiken ("Versteckter Monolith")

#### 1. **Hohe Kopplung**
- Customer Entity ist mit allem verknüpft
- Zirkuläre Dependencies zwischen Services
- Keine klaren Bounded Contexts

#### 2. **Schwierige Wartbarkeit**
- 1000+ Zeilen Services
- Vermischung von Concerns
- Unklare Verantwortlichkeiten

#### 3. **Performance-Probleme**
- Eager Loading von Relations
- N+1 Query Probleme
- Keine Caching-Strategie

#### 4. **Testbarkeit**
- Schwer zu mocken
- Integrationstests dominieren
- Lange Test-Laufzeiten

### 💡 Alternative Architektur-Ideen

#### Vorschlag: Modulare Mikroservice-Ready Architektur

```
customer-management/
├── customer-core/           # Kern-Funktionalität
│   ├── domain/
│   │   ├── Customer.java   # Nur ID, Name, Nummer
│   │   ├── CustomerCommand.java
│   │   └── CustomerEvents.java
│   ├── application/
│   │   ├── CustomerCommandHandler.java
│   │   └── CustomerQueryHandler.java
│   └── infrastructure/
│       └── CustomerRepository.java
│
├── customer-contacts/       # Kontakt-Management
│   ├── domain/
│   ├── application/
│   └── infrastructure/
│
├── customer-financials/     # Finanz-Daten
│   ├── domain/
│   ├── application/
│   └── infrastructure/
│
├── customer-timeline/       # Event-History
│   ├── domain/
│   ├── application/
│   └── infrastructure/
│
└── customer-api/           # REST API Gateway
    ├── CustomerController.java
    ├── CustomerAggregator.java
    └── CustomerDTOs.java
```

**Vorteile:**
- Klare Bounded Contexts
- Unabhängige Deployments möglich
- Bessere Testbarkeit
- Schrittweise Migration

### 📋 Kontext & Verständlichkeit

**Ist die Planung ausreichend für Refactoring?**

✅ **Was gut dokumentiert ist:**
- API-Anforderungen (Anhang B)
- UI-Konzept (Anhang A)
- Gesamt-Vision

❌ **Was noch fehlt:**
1. **Migrations-Strategie:**
   - Wie migrieren wir 54 Dateien?
   - Datenbank-Migration Plan?
   - Backwards Compatibility?

2. **Domain Model Details:**
   - Exakte Aggregate-Grenzen
   - Event-Definitionen
   - Command/Query Separation

3. **Performance Requirements:**
   - Expected Load
   - Query Performance SLAs
   - Caching Strategy

4. **Integration Points:**
   - Wie integriert Timeline?
   - Opportunity-Verknüpfung?
   - Activity Management?

## 🏗️ Komponenten-Struktur

```
frontend/src/features/customers/
├── components/
├── hooks/
├── services/
└── store/
```

### 🎯 Zusammenfassung & Empfehlungen

#### Kritische Erkenntnisse

1. **Das Customer-Modul ist ein "versteckter Monolith"**
   - 54 Dateien, eng gekoppelt
   - Keine klare Separation of Concerns
   - Schwer testbar und wartbar

2. **Refactoring ist ZWINGEND notwendig**
   - Aktuelle Struktur nicht zukunftsfähig
   - Performance-Probleme vorprogrammiert
   - Erweiterbarkeit stark eingeschränkt

3. **Schrittweise Migration erforderlich**
   - Big-Bang-Ansatz zu riskant
   - Parallel-Betrieb alter/neuer Code
   - Feature-Flags für Umschaltung

#### Empfohlenes Vorgehen

**Phase 1: Analyse & Design (2 Tage)**
1. Domain Model Workshop
2. Aggregate-Grenzen definieren
3. Event-Storming für Timeline
4. API-Design finalisieren

**Phase 2: Infrastruktur (1 Tag)**
1. Neue Package-Struktur anlegen
2. Test-Framework aufsetzen
3. CI/CD anpassen

**Phase 3: Core Implementation (3-4 Tage)**
1. Customer-Core Modul
2. Contact Management
3. Timeline als Event Store
4. API Gateway

**Phase 4: Migration (2 Tage)**
1. Daten-Migration Scripts
2. Parallelbetrieb testen
3. Schrittweise Umstellung

#### Offene Entscheidungen

1. **Event-Driven vs. Direct Calls?**
   - Empfehlung: Event-Driven für lose Kopplung

2. **Monolith-First vs. Services?**
   - Empfehlung: Modularer Monolith, Service-Ready

3. **CQRS vollständig oder teilweise?**
   - Empfehlung: Nur für Read-Heavy Parts (Search, List)

---

**Status:** Analyse abgeschlossen - Bereit für Architektur-Entscheidungen