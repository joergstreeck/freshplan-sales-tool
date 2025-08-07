# 🏛️ Event Sourcing Foundation - FreshPlan CRM

**Dokument:** Architektur-Grundlage für Event Sourcing  
**Erstellt:** 31.07.2025  
**Status:** 🟢 APPROVED  
**Gültigkeit:** Verbindlich für alle Module ab FC-005 Sprint 2  

## 📌 Executive Summary

Event Sourcing wird zur zentralen Architektur-Grundlage des FreshPlan CRM. Alle geschäftskritischen Änderungen werden als unveränderliche Events gespeichert, was uns vollständige Audit-Trails, DSGVO-Compliance und flexible Analytics ermöglicht.

## 🎯 Warum Event Sourcing?

### Business Benefits
- **Vollständige Historie**: Jede Änderung nachvollziehbar
- **Audit-Ready**: Compliance ohne Zusatzaufwand
- **Time-Travel**: Zustand zu jedem Zeitpunkt rekonstruierbar
- **Analytics**: Events als Basis für BI und KI

### Technische Benefits
- **Skalierbarkeit**: Read/Write Separation durch CQRS
- **Flexibilität**: Neue Projections ohne Datenmigration
- **Resilience**: Event Replay bei Fehlern
- **Integration**: Events als universelle Schnittstelle

## 🏗️ Core Architecture

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   Command   │────▶│Event Store  │────▶│ Projections │
│   Handler   │     │  (Append)   │     │  (Views)    │
└─────────────┘     └─────────────┘     └─────────────┘
                           │
                           ▼
                    ┌─────────────┐
                    │ Event Bus   │
                    └─────────────┘
                     ↙    ↓    ↘
              Analytics  Audit  DSGVO
```

## 📦 Event Structure

### Base Event Interface
```typescript
interface BaseEvent<T = any> {
  // Identifikation
  eventId: string;          // UUID v4
  aggregateId: string;      // Entity ID
  aggregateType: string;    // z.B. "Contact", "Customer"
  
  // Versionierung
  version: number;          // Event Schema Version
  sequenceNumber: number;   // Position im Stream
  
  // Zeitstempel
  timestamp: Date;          // Wann ist es passiert
  
  // Payload
  type: string;            // Event Type Discriminator
  payload: T;              // Typisierte Event-Daten
  
  // Metadata
  metadata: EventMetadata;
}

interface EventMetadata {
  // Versioning
  schemaVersion: string;    // "1.0.0"
  
  // Tracing
  correlationId: string;    // Business Transaction
  causationId?: string;     // Vorheriges Event
  
  // Context
  userId: string;           // Wer hat es ausgelöst
  tenantId?: string;        // Multi-Tenancy Ready
  source: EventSource;      // Woher kommt es
  
  // Optional
  userAgent?: string;       // Browser/App Info
  ipAddress?: string;       // Für Security Audits
}

type EventSource = 'ui' | 'api' | 'import' | 'system' | 'migration';
```

## 🔄 Event Versioning Strategy

### Schema Evolution Rules
1. **Additive Only**: Neue Felder sind optional
2. **Never Remove**: Alte Felder bleiben (deprecated)
3. **Type Changes**: Neue Version erforderlich
4. **Backward Compatible**: V2 kann V1 lesen

### Migration Example
```typescript
// Version 1
interface ContactCreatedV1 {
  type: 'CONTACT_CREATED';
  version: 1;
  name: string;
  email: string;
}

// Version 2 - Split name field
interface ContactCreatedV2 {
  type: 'CONTACT_CREATED';
  version: 2;
  firstName: string;
  lastName: string;
  email: string;
  // Neu & Optional
  gdprConsent?: boolean;
}

// Migration Handler
class EventMigrationService {
  migrate<T extends BaseEvent>(event: T): BaseEvent {
    const migrator = this.getMigrator(event.type, event.version);
    return migrator ? migrator(event) : event;
  }
}
```

## 📊 Projection Strategy

### Projection Types
```typescript
enum ProjectionType {
  SYNC = 'sync',           // Sofort, blockierend
  ASYNC = 'async',         // Eventually consistent
  ON_DEMAND = 'on-demand'  // Bei Bedarf generiert
}

interface ProjectionConfig {
  name: string;
  type: ProjectionType;
  priority: 'critical' | 'high' | 'normal' | 'low';
  rebuildable: boolean;
  retention?: Duration;
}
```

### Standard Projections

| Projection | Type | Purpose | Update Frequency |
|------------|------|---------|------------------|
| ContactList | SYNC | UI Liste | Realtime |
| ContactDetail | SYNC | UI Detail | Realtime |
| ContactSearch | ASYNC | Volltextsuche | < 5s |
| ContactAnalytics | ASYNC | Reporting | < 1min |
| ContactExport | ON_DEMAND | DSGVO Export | Bei Anfrage |

## 🔒 DSGVO Compliance

### Crypto-Shredding Pattern
```typescript
interface EncryptedEvent extends BaseEvent {
  payload: EncryptedPayload | PlainPayload;
  encryption?: EncryptionInfo;
}

interface EncryptedPayload {
  ciphertext: string;
  algorithm: 'AES-256-GCM';
  nonce: string;
}

interface EncryptionInfo {
  keyId: string;
  encryptedFields: string[];
  purpose: 'personal_data' | 'sensitive_business';
}

// Löschung = Key vernichten
class GDPRService {
  async deletePersonalData(aggregateId: string) {
    await this.keyVault.shredKey(aggregateId);
    await this.eventStore.append({
      type: 'PERSONAL_DATA_DELETED',
      aggregateId,
      reason: 'gdpr_request'
    });
  }
}
```

## 📈 Performance & Scaling

### Event Store Partitioning
```yaml
Partitioning Strategy:
  - By aggregate_type (Contact, Customer, etc.)
  - By tenant_id (Multi-Tenancy)
  - By time (Year/Month für Archivierung)

Retention Policy:
  - Hot: 3 Monate (PostgreSQL)
  - Warm: 3-12 Monate (S3 + Index)
  - Cold: > 12 Monate (Glacier)
```

### Snapshot Strategy
```typescript
interface Snapshot<T> {
  aggregateId: string;
  version: number;
  data: T;
  createdAt: Date;
}

// Snapshot alle 100 Events
const SNAPSHOT_FREQUENCY = 100;
```

## 🔗 Module Integration

### Event Flow zwischen Modulen
```
FC-005 Contact Events
    ↓
FC-012 Audit Trail (automatisch)
    ↓
FC-018 DSGVO Export (on-demand)
    ↓
FC-016 Analytics (async)
```

### Standard Event Types

```typescript
// Basis Events (alle Module)
type SystemEvent = 
  | 'ENTITY_CREATED'
  | 'ENTITY_UPDATED'
  | 'ENTITY_DELETED'
  | 'ENTITY_ARCHIVED';

// Contact Events (FC-005)
type ContactEvent = 
  | 'CONTACT_CREATED'
  | 'CONTACT_UPDATED'
  | 'RELATIONSHIP_DATA_ADDED'
  | 'LOCATION_ASSIGNED'
  | 'CONSENT_UPDATED';

// Audit Events (FC-012)
type AuditEvent = 
  | 'AUDIT_LOG_CREATED'
  | 'COMPLIANCE_CHECK_PERFORMED'
  | 'EXPORT_GENERATED';

// DSGVO Events (FC-018)
type GDPREvent = 
  | 'CONSENT_GRANTED'
  | 'CONSENT_REVOKED'
  | 'DATA_EXPORT_REQUESTED'
  | 'DATA_DELETION_REQUESTED';
```

## 🛠️ Implementation Guidelines

### 1. Event Naming Convention
```
<AGGREGATE>_<ACTION>_<DETAIL>
Beispiele:
- CONTACT_CREATED
- CONTACT_LOCATION_ASSIGNED
- CUSTOMER_STATUS_CHANGED
```

### 2. Event Handler Pattern
```typescript
@EventHandler(ContactCreated)
class ContactCreatedHandler {
  async handle(event: ContactCreated): Promise<void> {
    // 1. Validate
    // 2. Process
    // 3. Update Projections
    // 4. Emit Side Effects
  }
}
```

### 3. Testing Strategy
```typescript
describe('Contact Aggregate', () => {
  it('should emit ContactCreated event', () => {
    // Given
    const command = new CreateContactCommand(...);
    
    // When
    const events = aggregate.handle(command);
    
    // Then
    expect(events).toContainEqual(
      expect.objectContaining({
        type: 'CONTACT_CREATED'
      })
    );
  });
});
```

## 📚 Weiterführende Dokumente

- [GDPR Compliance Architecture](/Users/joergstreeck/freshplan-sales-tool/docs/architecture/GDPR_COMPLIANCE_ARCHITECTURE.md)
- [Module Integration Map](/Users/joergstreeck/freshplan-sales-tool/docs/architecture/MODULE_INTEGRATION_MAP.md)
- [Event Migration Guide](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/guides/EVENT_MIGRATION_GUIDE.md)

## ✅ Checkliste für neue Module

- [ ] Event Types definiert
- [ ] Aggregate Boundaries klar
- [ ] Projections geplant
- [ ] DSGVO-Felder markiert
- [ ] Audit-Relevanz geprüft
- [ ] Performance-Impact bewertet
- [ ] Integration Points dokumentiert

---

**Genehmigt von:** Jörg Streeck  
**Technischer Review:** Development Team  
**Nächstes Review:** Q4 2025