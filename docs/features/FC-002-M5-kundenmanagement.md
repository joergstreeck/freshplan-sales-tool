# FC-002-M5: Kundenmanagement - Implementierungsplan

**Modul:** M5  
**Feature:** FC-002  
**Status:** 🔍 50% (Planung abgeschlossen)  
**Geschätzter Aufwand:** 8-10 Tage  
**Architektur-Entscheidung:** ✅ Modularer Monolith mit Event-Driven Communication

## 🎯 Strategische Entscheidungen (FINAL)

1. **Migration statt Neubau** - Bestehender Code wird refactored
2. **Modularer Monolith** - Service-ready, aber im Monolith starten
3. **Event-Driven** für lose Kopplung zwischen Modulen
4. **CQRS** nur für Read-Heavy Operations (Listen, Suche)

## 🏗️ Ziel-Architektur (ASCII)

```
┌─────────────────────────────────────────────────────────────┐
│                    Customer API Gateway                      │
│                 CustomerController.java                      │
└────────────┬───────────────────────────────┬────────────────┘
             │                               │
     ┌───────▼────────┐              ┌──────▼────────┐
     │ Command Side   │              │  Query Side   │
     │ (Write Ops)    │              │  (Read Ops)   │
     └───────┬────────┘              └──────┬────────┘
             │                               │
    ┌────────▼─────────────────────────────▼────────┐
    │              Event Bus (Domain Events)         │
    └────┬────────┬────────┬────────────┬───────────┘
         │        │        │            │
    ┌────▼───┐ ┌─▼────┐ ┌─▼─────┐ ┌───▼────┐
    │Customer│ │Contact│ │Finance│ │Timeline│
    │  Core  │ │Module │ │Module │ │ Module │
    └────────┘ └──────┘ └───────┘ └────────┘
```

## 📦 Neue Package-Struktur

```
backend/src/main/java/de/freshplan/
└── modules/
    ├── customer/
    │   ├── core/
    │   │   ├── domain/
    │   │   │   ├── Customer.java         // Nur: id, number, name, status
    │   │   │   ├── CustomerNumber.java   // Value Object
    │   │   │   └── CustomerStatus.java   // Enum
    │   │   ├── application/
    │   │   │   ├── CustomerCommandHandler.java
    │   │   │   └── CustomerQueryHandler.java
    │   │   └── infrastructure/
    │   │       └── CustomerRepository.java
    │   │
    │   ├── contact/                      // Analog aufgebaut
    │   ├── financial/                    // Analog aufgebaut
    │   └── timeline/                     // Event Store
    │
    └── shared/
        ├── events/
        │   ├── CustomerCreatedEvent.java
        │   ├── CustomerUpdatedEvent.java
        │   └── EventBus.java
        └── api/
            └── CustomerApiGateway.java
```

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

## 🚀 Implementierungsphasen (Der Bauplan)

### Phase 1: Foundation (Tag 1-2)
**Ziel:** Basis-Infrastruktur ohne Breaking Changes

```bash
# 1. Neue Package-Struktur anlegen
mkdir -p backend/src/main/java/de/freshplan/modules/customer/{core,contact,financial,timeline}
mkdir -p backend/src/main/java/de/freshplan/modules/shared/{events,api}

# 2. Event Bus implementieren
```

```java
// EventBus.java - Synchron für Start, Async-ready
@ApplicationScoped
public class EventBus {
    @Inject Event<DomainEvent> events;
    
    public void publish(DomainEvent event) {
        events.fire(event);  // Später: Async mit Kafka/Redis
    }
}
```

**Konkrete TODOs:**
- [ ] Package-Struktur erstellen
- [ ] EventBus + Base Events (CustomerCreatedEvent, etc.)
- [ ] Test-Infrastruktur für neue Module
- [ ] CI/CD anpassen für neue Struktur

### Phase 2: Customer Core Module (Tag 3-4)
**Ziel:** Minimaler Customer mit sauberer Architektur

```java
// Customer.java - Neue schlanke Entity
@Entity
@Table(name = "customers")  // GLEICHE Tabelle!
public class Customer {
    @Id UUID id;
    @Column String customerNumber;
    @Column String companyName;
    @Enumerated CustomerStatus status;
    
    // Business Logic
    public void activate() {
        if (status != LEAD) 
            throw new IllegalStateException();
        this.status = ACTIVE;
        // Event wird im Handler gefeuert
    }
}
```

```java
// CustomerCommandHandler.java
@ApplicationScoped
@Transactional
public class CustomerCommandHandler {
    @Inject CustomerRepository repo;
    @Inject EventBus eventBus;
    
    public UUID createCustomer(CreateCustomerCommand cmd) {
        // 1. Validierung
        // 2. Customer erstellen
        // 3. Event publizieren
        var customer = new Customer(cmd);
        repo.persist(customer);
        eventBus.publish(new CustomerCreatedEvent(customer));
        return customer.getId();
    }
}
```

**Migration-Trick:** Neue Entities nutzen GLEICHE Tabellen mit Subset der Spalten!

### Phase 3: Facade & Parallel-Betrieb (Tag 5)
**Ziel:** Alte API weiter bedienen, intern neue Module nutzen

```java
// CustomerServiceFacade.java - Ersetzt alten Service
@ApplicationScoped
public class CustomerServiceFacade implements CustomerService {
    @Inject CustomerCommandHandler commands;
    @Inject CustomerQueryHandler queries;
    @Inject @Named("legacy") CustomerService legacy;
    
    @ConfigProperty(name = "feature.new-customer-core")
    boolean useNewCore;
    
    public CustomerResponse createCustomer(CreateRequest req) {
        if (useNewCore) {
            var id = commands.createCustomer(toCommand(req));
            return queries.getCustomer(id);
        }
        return legacy.createCustomer(req);
    }
}
```

### Phase 4: Contact & Financial Module (Tag 6-7)
**Ziel:** Weitere Module analog zu Core

```java
// CustomerContact.java - Eigenes Aggregate
@Entity
@Table(name = "customer_contacts")
public class CustomerContact {
    @Id UUID id;
    @Column UUID customerId;  // Nur ID, keine @ManyToOne!
    // Rest der Felder
}

// ContactCommandHandler.java
public void addContact(AddContactCommand cmd) {
    // Prüfe Customer existiert via Query
    if (!customerQueries.exists(cmd.customerId))
        throw new CustomerNotFoundException();
    
    var contact = new CustomerContact(cmd);
    contactRepo.persist(contact);
    eventBus.publish(new ContactAddedEvent(contact));
}
```

### Phase 5: Timeline als Event Store (Tag 8)
**Ziel:** Timeline wird zum zentralen Event Log

```java
@ApplicationScoped
public class TimelineEventHandler {
    @Inject TimelineRepository repo;
    
    void onCustomerCreated(@Observes CustomerCreatedEvent e) {
        var entry = TimelineEntry.of(e, "Kunde erstellt");
        repo.persist(entry);
    }
    
    void onContactAdded(@Observes ContactAddedEvent e) {
        var entry = TimelineEntry.of(e, "Kontakt hinzugefügt");
        repo.persist(entry);
    }
}
```

### Phase 6: Read Models & CQRS (Tag 9)
**Ziel:** Optimierte Lese-Zugriffe

```java
// CustomerListReadModel.java - Denormalisierte View
@Entity
@Table(name = "customer_list_view")
@Immutable
public class CustomerListReadModel {
    @Id UUID id;
    String customerNumber;
    String companyName;
    String status;
    Integer contactCount;     // Vorberechnet!
    BigDecimal totalRevenue;  // Vorberechnet!
    LocalDateTime lastActivity;
}

// Projection Update via Events
void onCustomerUpdated(@Observes CustomerUpdatedEvent e) {
    // Update Read Model
}
```

### Phase 7: Migration & Cleanup (Tag 10)
**Ziel:** Alte Strukturen entfernen

1. Feature Flags auf 100%
2. Alte Services deprecaten
3. Unused Code entfernen
4. Performance-Tests

## 🎮 Feature Flags für sichere Migration

```properties
# application.properties
feature.new-customer-core=false      # Start: false
feature.new-customer-contacts=false  # Schrittweise
feature.new-customer-timeline=false  # aktivieren
feature.use-read-models=false        # Am Ende
```

## 🧪 Test-Strategie

```java
// Parallele Tests für Sicherheit
@Test
void customerCreation_shouldWorkInBothModes() {
    // Arrange
    var request = createValidRequest();
    
    // Act - Alter Weg
    System.setProperty("feature.new-customer-core", "false");
    var oldResult = service.createCustomer(request);
    
    // Act - Neuer Weg  
    System.setProperty("feature.new-customer-core", "true");
    var newResult = service.createCustomer(request);
    
    // Assert - Gleiche Ergebnisse!
    assertThat(newResult).isEqualTo(oldResult);
}
```

## 📊 Erfolgs-Metriken

- [ ] Alle Tests grün (alte + neue)
- [ ] Performance gleich oder besser
- [ ] Keine Breaking Changes in API
- [ ] Event Bus funktioniert
- [ ] Module unabhängig deploybar (theoretisch)

## 🚨 Kritische Punkte (Achtung!)

1. **Datenbank-Schema** bleibt IDENTISCH während Migration
2. **API-Kompatibilität** muss 100% gewährleistet sein
3. **Events** müssen idempotent sein
4. **Transaktionen** über Module-Grenzen vermeiden
5. **Feature Flags** täglich prüfen

## 💻 Quick-Start-Befehle

```bash
# Neue Tests ausführen
./mvnw test -Dtest="**/modules/customer/**"

# Feature Flag togglen
curl -X POST localhost:8080/admin/feature/new-customer-core/true

# Event Bus Monitor
curl localhost:8080/admin/events/stream

# Performance Vergleich
./scripts/perf-test-customer-api.sh
```

## 🎁 Das Wichtigste auf einen Blick

**Was machen wir?** 
→ Customer-Monolith in 4 Module aufteilen (Core, Contact, Financial, Timeline)

**Wie machen wir es?** 
→ Schrittweise mit Feature Flags, alte API bleibt stabil

**Wann ist es fertig?** 
→ 10 Tage, aber ab Tag 5 produktiv nutzbar

**Was ist das Risiko?** 
→ Minimal durch Parallel-Betrieb und umfassende Tests

---

**Status:** ✅ Planung komplett - Bereit für Implementierung
**Nächster Schritt:** Phase 1 starten - Package-Struktur + Event Bus