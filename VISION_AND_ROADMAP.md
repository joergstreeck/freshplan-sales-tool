# FreshPlan Sales Tool - Vision & Zukünftige Architektur (FreshPlan 2.0)

Dieses Dokument beschreibt die langfristige Vision und die geplante zukünftige Architektur für das FreshPlan Sales Tool. Es dient als Leitfaden für alle aktuellen und zukünftigen Entwicklungsentscheidungen, um Skalierbarkeit, Robustheit und Zukunftsfähigkeit sicherzustellen. Diese Vision ist als lebendiges Dokument gedacht und soll bei Bedarf um neue Ideen und Anforderungen erweitert werden.

## 1. Heutiger Stand (Ende Phase 1: Frontend-Migration & Stabilisierung)

* **Architektur:** Reines Frontend-Tool, entwickelt mit Vite + TypeScript. Die Basis ist eine 1:1-Portierung der Funktionalität und des Erscheinungsbildes des ursprünglichen `GOLDEN_REFERENCE.html` (Kopie der `freshplan-complete.html`).
* **Datenhaltung:** Lokal im Browser mittels LocalStorage (über `src/legacy-script.ts` und den Store).
* **Standalone-Build:** Eine einzelne, funktionierende `freshplan-complete.html` wird via `npm run build:standalone` generiert (Logo-Einbettung ist der letzte offene Punkt für den Abschluss von Phase 1b).
* **Ziel von Phase 1:** Ein stabiles, im Browser lauffähiges Frontend, das die Originalfunktionalität vollständig abbildet und als Grundlage für Weiterentwicklungen dient.

## 2. Langfristige Vision: FreshPlan 2.0 - API-zentriertes System mit Backend

Die strategische Weiterentwicklung sieht ein vollwertiges, API-zentriertes System vor, bestehend aus:

* **Frontend (Weiterentwicklung des aktuellen Tools):** Die bewährte Benutzeroberfläche für die Verkäufer, refaktorisiert zu sauberen TypeScript-Modulen.
* **Zentrales Backend:** Übernimmt Datenhaltung, komplexe Geschäftslogik, Rechteverwaltung und Integrationen. (Technologie: Quarkus mit Java).
* **Datenbank (z.B. PostgreSQL):** Für die persistente und zentrale Speicherung aller relevanten Daten.
* **Externe API-Anbindungen:**
    * Warenkreditversicherer (z.B. Allianz) für Bonitätsprüfungen.
    * ERP-System (Xentral) für Preislistenabruf, ggf. Übertragung von Auftragsdaten.
    * Authentifizierungs-Service für die Nutzerverwaltung.

Visualisierung der Zielarchitektur:

```
Frontend (Vite + TypeScript + HTML)
            ⇅ API
Backend (Quarkus + Java + DB)
            ⇅ APIs
┌────────────┬────────────┬────────────┬────────────┐
│ PostgreSQL │ Allianz API│ Xentral ERP│ Auth-Server│
└────────────┴────────────┴────────────┴────────────┘
```

### 2.1 API-Design Prinzipien

Um eine zukunftsfähige und wartbare API-Schnittstelle zu gewährleisten, werden folgende Design-Prinzipien von Anfang an berücksichtigt:

* **Versionierte APIs:** Alle API-Endpunkte werden versioniert (z.B. `/api/v1/customers`, `/api/v2/customers`), um Breaking Changes zu vermeiden und eine schrittweise Migration zu ermöglichen.
* **GraphQL Alternative:** Zusätzlich zur REST-API wird GraphQL als Alternative evaluiert, um flexible Datenabfragen zu ermöglichen und Over-/Underfetching zu vermeiden.
* **WebSocket Support:** Für Echtzeit-Funktionalitäten (z.B. Live-Updates bei Bonitätsprüfungen, Benachrichtigungen) wird WebSocket-Kommunikation implementiert.

## 3. Geplante Kernfunktionen für FreshPlan 2.0 (Backend-gestützt)

* **Zentrale Datenhaltung:** Alle Kunden-, Angebots-, Vertrags-, Standortdaten etc.
* **Rechte- und Rollenverwaltung** (3 Rollen):
    * sales: Verkäufer - eigene Kunden/Angebote, Kalkulationen, Bonitätsprüfungen
    * manager: Geschäftsleitung - erweiterte Sichten, alle Berichte, Credit Checks
    * admin: Software-Administration - User-Verwaltung, Systemkonfiguration
* **Bonitätsprüfungen:** Direkte Anbindung an Anbieter wie Allianz.
* **Dashboards:** Individuelle und aggregierte Ansichten für Verkäufer und Management.
* **Vertragsverwaltung:** Zentrale Speicherung und Abrufbarkeit von Vertragsdokumenten.
* **ERP-Integration (Xentral):** Automatisierter Abruf von Preislisten, Synchronisation von Kunden-/Auftragsdaten.
* **Interne Kommunikation:** Nachrichten- oder Benachrichtigungsfunktion zwischen Nutzern.
* **Kundenhistorie:** Lückenlose Aufzeichnung von Aktivitäten, Umsatzentwicklung, Zahlungsverhalten.
* **(Platz für zukünftige Ideen)**

### 3.1 Zusätzliche wichtige Kernaspekte

* **Offline-First Architektur:** 
    * Lokale Datensynchronisation bei Verbindungsproblemen ermöglicht unterbrechungsfreies Arbeiten
    * Intelligente Konfliktauflösung bei konkurrierenden Änderungen durch mehrere Nutzer
    * Progressive Web App (PWA) Funktionalität für mobile Nutzung und Installation auf Endgeräten
    * Automatische Hintergrund-Synchronisation bei wiederhergestellter Verbindung

## 4. Implikationen für die aktuelle und nächste Entwicklungsphase (Frontend-Refactoring - Phase 2)

Während des Refactorings von `src/legacy-script.ts` zu modularem TypeScript sollen folgende Aspekte besonders berücksichtigt werden, um die spätere Backend-Anbindung zu erleichtern:

* **Klare Modulstruktur:** Trennung von UI-Logik, Business-Logik und Daten-Services.
* **Definierte Datentypen/Interfaces:** Konsequente Nutzung von TypeScript-Interfaces (`src/types/index.ts`), die später auf API-Datenmodelle abbildbar sind.
* **Entkopplung von Datenquellen:** Datenzugriffe (aktuell LocalStorage über den Store) so kapseln, dass sie später leicht durch API-Aufrufe ersetzt werden können.

### 4.1 Technische Design-Entscheidungen für Phase 2

Um die spätere Backend-Integration optimal vorzubereiten, werden folgende technische Konzepte bereits in Phase 2 implementiert:

#### Repository Pattern für Data Layer Abstraction

Das Repository Pattern ermöglicht eine saubere Trennung zwischen Geschäftslogik und Datenzugriff:

```typescript
// Beispiel: CustomerRepository Interface
interface ICustomerRepository {
  findById(id: string): Promise<Customer>;
  save(customer: Customer): Promise<void>;
  search(criteria: SearchCriteria): Promise<Customer[]>;
  delete(id: string): Promise<void>;
}

// Phase 2: LocalStorage Implementation
class LocalStorageCustomerRepository implements ICustomerRepository {
  // Implementierung mit LocalStorage
}

// Phase 4: API Implementation
class APICustomerRepository implements ICustomerRepository {
  // Implementierung mit REST API Calls
}
```

#### Service Layer Architektur

Services kapseln die Geschäftslogik und nutzen Repositories für Datenzugriffe:

```typescript
// Beispiel: CustomerService
export class CustomerService {
  constructor(
    private repository: ICustomerRepository,
    private validator: ICustomerValidator,
    private eventBus: IEventBus
  ) {}
  
  async createCustomer(data: CustomerData): Promise<Customer> {
    // Geschäftslogik hier, nicht im UI-Code
    await this.validator.validate(data);
    const customer = await this.repository.save(data);
    this.eventBus.emit('customer:created', customer);
    return customer;
  }
}
```

#### Einheitliche Error Handling Strategy

Strukturierte Fehlerbehandlung für bessere Wartbarkeit und Nutzerfreundlichkeit:

```typescript
// Basis-Klasse für Business Errors
export class BusinessError extends Error {
  constructor(
    public code: string,
    public userMessage: string,
    public technicalDetails?: any
  ) {
    super(userMessage);
  }
}

// Spezifische Error-Typen
export class ValidationError extends BusinessError { }
export class NotFoundError extends BusinessError { }
export class ConflictError extends BusinessError { }
```

#### Feature Flags System

Ermöglicht schrittweise Migration und A/B-Testing:

```typescript
// Feature Flags für kontrollierte Rollouts
interface FeatureFlags {
  USE_API_FOR_CUSTOMERS: boolean;
  USE_API_FOR_CREDIT_CHECK: boolean;
  ENABLE_OFFLINE_MODE: boolean;
  USE_NEW_CALCULATOR_LOGIC: boolean;
}

// Verwendung im Code
if (featureFlags.USE_API_FOR_CUSTOMERS) {
  // Neue API-basierte Implementierung
} else {
  // Legacy LocalStorage Implementierung
}
```

## 5. Grobe Roadmap-Skizze

* **Phase 1 (fast abgeschlossen):** Fertigstellung der 1:1-Frontend-Migration.
    * **Letzter offener Punkt:** Einbettung des optimierten Logos und erfolgreicher, stabiler `build:standalone`-Lauf.
* **Phase 2 (direkt im Anschluss):** Refactoring des Frontend-Codes.
    * Umwandlung des Codes aus `src/legacy-script.ts` in saubere, modulare TypeScript-Module und -Services.
    * Vervollständigung der UI für Tabs "Profil", "Angebot" (inkl. PDF-Generierung), "Einstellungen".
    * Implementierung der detaillierten Kettenkunden-Rabattlogik im Rabattrechner.
* **Phase 3 (Zukunft):** Konzeption und Entwicklung des Backends und der Datenbank.
* **Phase 4 (Zukunft):** Integration Frontend & Backend; Implementierung der API-Anbindungen.

### 5.1 Strategie für die Backend-Migration (Phase 3-4)

Für die Migration zu einem Backend-gestützten System wird das **Strangler Fig Pattern** angewendet. Dieses bewährte Muster ermöglicht eine risikoarme, schrittweise Migration:

1. **Neue Features direkt mit Backend entwickeln:** Alle neuen Funktionalitäten werden von Anfang an mit Backend-Unterstützung implementiert.
2. **Alte Features schrittweise migrieren:** Bestehende Funktionen werden nach Priorität einzeln auf die neue Architektur umgestellt.
3. **Dual-Mode Betrieb:** Während der Übergangsphase können alte (LocalStorage) und neue (API) Implementierungen parallel existieren, gesteuert durch Feature Flags.

Vorteile dieser Strategie:
* Minimales Risiko durch schrittweise Migration
* Kontinuierlicher Betrieb ohne große Unterbrechungen
* Möglichkeit zum Rollback bei Problemen
* Lernen und Optimieren während der Migration

## 6. Technische Architektur-Prinzipien (Frontend & Backend)

Die folgenden Prinzipien leiten die technische Entwicklung sowohl im Frontend als auch im späteren Backend:

* **Domain-Driven Design (DDD):** Klare Trennung von Geschäftsdomänen (Customer, Calculator, CreditCheck, etc.) mit eigenen Bounded Contexts.
* **SOLID Prinzipien:** 
    * Single Responsibility: Jede Klasse/Modul hat genau eine Verantwortlichkeit
    * Open/Closed: Erweiterbar ohne Modifikation des bestehenden Codes
    * Liskov Substitution: Austauschbare Implementierungen (z.B. Repository Pattern)
    * Interface Segregation: Kleine, spezifische Interfaces statt großer, allgemeiner
    * Dependency Inversion: Abhängigkeiten von Abstraktionen, nicht von konkreten Implementierungen
* **Event-Driven Architecture:** Lose Kopplung zwischen Modulen durch Event-basierte Kommunikation (EventBus).
* **Security by Design:** 
    * OAuth2/JWT für Authentifizierung und Autorisierung
    * Row-Level Security in der Datenbank für mandantenfähige Datentrennung
    * Verschlüsselung sensibler Daten (at rest und in transit)
    * Input-Validierung und Sanitization auf allen Ebenen
    * Principle of Least Privilege für Zugriffsrechte

## 7. Qualitätssicherung & DevOps (Langfristig)

Um dauerhaft hohe Qualität und schnelle Iterationszyklen zu gewährleisten:

* **Automatisierte Tests:** 
    * Unit Tests für isolierte Geschäftslogik (Ziel: >80% Coverage)
    * Integration Tests für API-Endpunkte und Datenbankzugriffe
    * E2E Tests für kritische User Journeys (Playwright)
    * Performance Tests für skalierungskritische Operationen
* **CI/CD Pipeline:** 
    * Automatische Builds bei jedem Commit
    * Automatisierte Test-Durchläufe
    * Staging-Deployments für Vorab-Tests
    * Blue-Green Deployments für Zero-Downtime Updates
* **Monitoring & Logging:** 
    * Strukturierte Logs mit einheitlichem Format (JSON)
    * Application Performance Monitoring (APM)
    * Error Tracking und Alerting
    * Business Metrics Dashboard
* **API Documentation:** 
    * OpenAPI/Swagger Spezifikation für alle REST-Endpunkte
    * Automatisch generierte, interaktive API-Dokumentation
    * Versionierte API-Dokumentation
    * Code-Beispiele in verschiedenen Programmiersprachen

Diese Vision dient als Orientierung, um sicherzustellen, dass das FreshPlan Sales Tool nicht nur die aktuellen Anforderungen erfüllt, sondern auch für zukünftige Erweiterungen und Herausforderungen bestens gerüstet ist.