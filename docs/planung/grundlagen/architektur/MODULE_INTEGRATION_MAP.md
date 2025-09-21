# 🗺️ Module Integration Map - FreshPlan CRM

**Dokument:** Übersicht aller Modul-Integrationen und Dependencies  
**Erstellt:** 31.07.2025  
**Status:** 🟢 ACTIVE  
**Aktualisierung:** Bei jedem neuen Modul  

## 📌 Executive Summary

Diese Map zeigt alle Integrationen zwischen FreshPlan Modulen. Event Sourcing (ab FC-005) bildet das Rückgrat für lose gekoppelte, skalierbare Module.

## 🏗️ Core Architecture Layers

```
┌─────────────────────────────────────────────────────┐
│                    UI Layer                         │
│          (React Components, Mobile PWA)             │
└─────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────┐
│                  API Gateway                        │
│              (REST, GraphQL, WebSocket)             │
└─────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────┐
│                 Business Logic                      │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐           │
│  │ Commands │ │  Queries │ │ Event    │           │
│  │          │ │          │ │ Handlers │           │
│  └──────────┘ └──────────┘ └──────────┘           │
└─────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────┐
│               Event Store (Core)                    │
│         All Business Events Flow Here               │
└─────────────────────────────────────────────────────┘
     ↙               ↓                ↘
┌──────────┐    ┌──────────┐    ┌──────────┐
│ Audit    │    │ Analytics│    │ DSGVO    │
│ (FC-012) │    │ (FC-016) │    │ (FC-018) │
└──────────┘    └──────────┘    └──────────┘
```

## 🔗 Module Dependency Matrix

### Direct Dependencies (→ uses)

| Module | FC-005 Customer | FC-012 Audit | FC-018 DSGVO | M4 Pipeline | FC-003 Email |
|--------|----------------|--------------|--------------|-------------|--------------|
| **FC-005 Customer** | - | Events→ | Events→ | ←API | Future |
| **FC-012 Audit** | ←Events | - | Shared | ←Events | ←Events |
| **FC-018 DSGVO** | ←Events | Reads | - | ←Events | ←Events |
| **M4 Pipeline** | API→ | Events→ | Events→ | - | Future |
| **FC-003 Email** | API→ | Events→ | Events→ | API→ | - |
| **FC-009 Renewal** | API→ | Events→ | Events→ | API→ | Trigger→ |
| **FC-013 Activities** | API→ | Events→ | Events→ | Link→ | Link→ |
| **FC-016 Analytics** | ←Events | Reads | - | ←Events | ←Events |

**Legende:**
- `→` Abhängigkeit (nutzt)
- `←` Wird genutzt von
- `↔` Bidirektional
- `Events→` Sendet Events
- `←Events` Empfängt Events
- `API→` API Calls
- `Shared` Gemeinsame Komponenten

## 📊 Event Flow Diagram

### Primary Event Flows
```
Customer Creation Flow:
FC-005 → CONTACT_CREATED → Event Store
                              ↓
         ┌────────────────────┼────────────────────┐
         ↓                    ↓                    ↓
    FC-012 Audit         FC-018 DSGVO        FC-016 Analytics
    (Log Entry)          (Consent Check)      (KPI Update)

Pipeline Stage Change:
M4 → OPPORTUNITY_STAGE_CHANGED → Event Store
                                     ↓
              ┌──────────────────────┼──────────────────┐
              ↓                      ↓                  ↓
         FC-012 Audit           FC-009 Renewal     FC-013 Activity
         (Compliance)           (Check Triggers)    (Timeline)
```

## 🎯 Integration Patterns

### 1. Event-Driven Integration (Preferred)
```typescript
// Producer (FC-005)
await eventStore.publish({
  type: 'CONTACT_CREATED',
  payload: contactData
});

// Consumer (FC-012)
@EventHandler('CONTACT_CREATED')
async handleContactCreated(event) {
  await this.createAuditLog(event);
}
```

### 2. API Integration (When Needed)
```typescript
// Direct API Call (M4 → FC-005)
const customer = await customerApi.getCustomer(customerId);
```

### 3. Shared Projections
```typescript
// Shared Read Model
interface CustomerSummaryProjection {
  // Used by multiple modules
  customerId: string;
  name: string;
  status: string;
  // Read-only, updated by events
}
```

## 🚀 Future Integration Points

### Phase 2: Communication Hub
```
FC-003 Email ←→ FC-005 Contacts
     ↓              ↓
 Email Events   Contact Events
     ↓              ↓
     └──── Merge ────┘
           ↓
    Communication Timeline
```

### Phase 3: External Systems
```
Xentral ERP ←→ API Gateway ←→ Event Store
                                   ↓
                          Sync Events to all Modules
```

### Phase 4: AI/ML Integration
```
Event Store → Data Lake → ML Pipeline
                              ↓
                    Predictions & Insights
                              ↓
                    Back to Business Logic
```

## 📋 Integration Checklist for New Modules

### When adding a new module:

1. **Event Design**
   - [ ] Define module-specific events
   - [ ] Identify events to consume
   - [ ] Design event payloads
   - [ ] Plan projections

2. **API Design**
   - [ ] Define REST endpoints
   - [ ] Document OpenAPI spec
   - [ ] Plan rate limiting
   - [ ] Design error responses

3. **Security**
   - [ ] Define permissions
   - [ ] Plan data access rules
   - [ ] DSGVO compliance check
   - [ ] Audit requirements

4. **Performance**
   - [ ] Estimate event volume
   - [ ] Plan caching strategy
   - [ ] Design async processing
   - [ ] Set SLAs

5. **Testing**
   - [ ] Unit test events
   - [ ] Integration test APIs
   - [ ] E2E test workflows
   - [ ] Performance test

## 🔒 Security Considerations

### Data Flow Security
```
1. All events through Event Store (central audit point)
2. API Gateway handles authentication
3. Module-level authorization
4. Encrypted event payloads for PII
5. Separate read/write permissions
```

### Cross-Module Access Control
```typescript
// Module boundaries enforced
@RequireModuleAccess('FC-005', 'read')
@RequireRole(['sales', 'admin'])
async getCustomerData(customerId: string) {
  // Access granted only with proper permissions
}
```

## 📈 Performance Guidelines

### Event Processing SLAs
- **Synchronous**: < 50ms (UI blocking)
- **Asynchronous**: < 5s (background)
- **Batch**: < 1min (bulk operations)
- **Analytics**: < 5min (reporting)

### Scaling Strategies
1. **Event Partitioning**: By aggregate type
2. **Read Model Caching**: Redis for hot data
3. **Async Processing**: Queue for heavy tasks
4. **CDC for Projections**: Change Data Capture

## 🚨 Common Integration Pitfalls

### ❌ Avoid These:
1. **Tight Coupling**: Direct DB access between modules
2. **Sync Everything**: Not all operations need to be synchronous
3. **Fat Events**: Keep payloads focused
4. **Missing Correlation**: Always include correlation IDs

### ✅ Do These Instead:
1. **Event-Driven**: Loose coupling through events
2. **Async by Default**: Sync only when necessary
3. **Lean Events**: Just enough data
4. **Trace Everything**: Full request tracing

## 📚 Related Documents

- [Event Sourcing Foundation](/Users/joergstreeck/freshplan-sales-tool/docs/architecture/EVENT_SOURCING_FOUNDATION.md)
- [API Design Patterns](/Users/joergstreeck/freshplan-sales-tool/docs/technical/API_DESIGN_PATTERNS.md)
- [Security Architecture](/Users/joergstreeck/freshplan-sales-tool/docs/technical/SECURITY_ARCHITECTURE.md)

---

**Maintained by:** Architecture Team  
**Review Cycle:** Monthly  
**Last Update:** 31.07.2025