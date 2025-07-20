# 📊 FC-023 EVENT SOURCING INFRASTRUCTURE (KOMPAKT)

**Erstellt:** 18.07.2025  
**Status:** 📋 IN M4 INTEGRIERT  
**Feature-Typ:** 🔧 BACKEND  
**Priorität:** HIGH - Architektur-Basis  
**Geschätzt:** In M4 enthalten (+1.5 Tage)  

---

## 🧠 WAS WIR BAUEN

**Problem:** Keine Historie, schwache Audit-Trails  
**Lösung:** Event Sourcing von Anfang an  
**Value:** Perfekte Timeline, Audit, Analytics  

> **Business Case:** Jede Aktion nachvollziehbar = Compliance + Insights

### 🎯 Event-Driven Architecture:
- **Event Store:** Alle Business Events speichern
- **Event Bus:** Publish/Subscribe Pattern
- **Projections:** Read Models für Performance
- **Replay:** Zeit zurückdrehen möglich

---

## 🚀 SOFORT LOSLEGEN (15 MIN)

### 1. **Event Base Classes:**
```bash
cd backend/src/main/java/de/freshplan/shared/events
touch DomainEvent.java EventStore.java
# → Templates unten
```

### 2. **Event Tables:**
```sql
-- V5.0__create_event_store.sql
CREATE TABLE domain_events (
    event_id UUID PRIMARY KEY,
    aggregate_id UUID NOT NULL,
    event_type VARCHAR(255) NOT NULL,
    event_data JSONB NOT NULL,
    occurred_at TIMESTAMP NOT NULL,
    user_id UUID
);
```

### 3. **First Events:**
```java
// CustomerCreatedEvent
// OpportunityStageChangedEvent  
// CreditCheckPerformedEvent
```

---

## 🔧 CORE COMPONENTS

### Domain Event Interface:
```java
public interface DomainEvent {
    UUID getEventId();
    UUID getAggregateId();
    String getEventType();
    LocalDateTime getOccurredAt();
    UUID getUserId();
}
```

### Event Store Service:
```java
@ApplicationScoped
public class EventStore {
    
    public void append(DomainEvent event) {
        // 1. Persist to event_store
        // 2. Publish to event bus
        // 3. Update projections
    }
    
    public List<DomainEvent> getEvents(
        UUID aggregateId, 
        LocalDateTime since
    ) {
        // Replay events
    }
}
```

### Event Projections:
```java
// Automatic View Updates
@EventHandler
public class CustomerProjection {
    
    @HandleEvent(CustomerCreatedEvent.class)
    void on(CustomerCreatedEvent e) {
        // Update read model
    }
    
    @HandleEvent(CustomerUpdatedEvent.class)
    void on(CustomerUpdatedEvent e) {
        // Update read model
    }
}
```

---

## 📈 USE CASES

### 1. Activity Timeline:
```java
// Alle Events eines Kunden
eventStore.getEvents(customerId)
    .stream()
    .map(this::toActivity)
    .sorted(by(occurredAt))
```

### 2. Audit Log:
```java
// Wer hat was wann gemacht
SELECT * FROM domain_events 
WHERE user_id = ? 
AND occurred_at BETWEEN ? AND ?
```

### 3. Time Travel:
```java
// Zustand zu bestimmtem Zeitpunkt
Customer customer = eventStore
    .replay(customerId, untilDate);
```

---

## 🔗 INTEGRATION POINTS

- **M4 Pipeline:** OpportunityEvents
- **FC-014 Timeline:** Event Aggregation
- **FC-025 DSGVO:** Audit Trail
- **FC-026 Analytics:** Event Stream

---

## 📞 IMPLEMENTATION IN M4

1. **Event Classes** für Opportunity
2. **Event Store** Basis-Implementation
3. **Event Bus** mit CDI Events
4. **First Projection** für Pipeline
5. **Event Listeners** für Timeline

**WICHTIG:** Von Anfang an dabei = saubere Historie!

---

## 🧭 NAVIGATION & VERWEISE

### 📋 Zurück zum Überblick:
- **[📊 Master Plan V5](/docs/CRM_COMPLETE_MASTER_PLAN_V5.md)** - Vollständige Feature-Roadmap
- **[🗺️ Feature Overview](/docs/features/MASTER/FEATURE_OVERVIEW.md)** - Alle 40 Features im Überblick

### 🔗 Dependencies (Required):
- **[📊 M4 Opportunity Pipeline](/docs/features/ACTIVE/02_opportunity_pipeline/M4_KOMPAKT.md)** - Erste Event-Implementation
- **[🔒 FC-008 Security Foundation](/docs/features/ACTIVE/01_security_foundation/FC-008_KOMPAKT.md)** - User-Context für Events
- **[👥 M5 Customer Refactor](/docs/features/PLANNED/12_customer_refactor_m5/M5_KOMPAKT.md)** - Customer Events

### ⚡ Event-Produzenten:
- **[📧 FC-003 E-Mail Integration](/docs/features/PLANNED/06_email_integration/FC-003_KOMPAKT.md)** - E-Mail Events
- **[🔄 FC-016 Opportunity Cloning](/docs/features/PLANNED/18_opportunity_cloning/FC-016_KOMPAKT.md)** - Clone Events
- **[🔌 FC-021 Integration Hub](/docs/features/PLANNED/21_integration_hub/FC-021_KOMPAKT.md)** - Sync Events

### 🚀 Ermöglicht folgende Features:
- **[📈 FC-014 Activity Timeline](/docs/features/PLANNED/16_activity_timeline/FC-014_KOMPAKT.md)** - Event Aggregation
- **[🔒 FC-025 DSGVO Compliance](/docs/features/PLANNED/25_dsgvo_compliance/FC-025_KOMPAKT.md)** - Audit Trail
- **[📊 FC-026 Analytics Platform](/docs/features/PLANNED/26_analytics_platform/FC-026_KOMPAKT.md)** - Event Analytics

### 🎨 UI Integration:
- **[📊 M3 Sales Cockpit](/docs/features/ACTIVE/05_ui_foundation/M3_SALES_COCKPIT_KOMPAKT.md)** - Real-time Updates
- **[📊 M6 Analytics Module](/docs/features/PLANNED/13_analytics_m6/M6_KOMPAKT.md)** - Event-basierte Reports
- **[🎯 FC-027 Magic Moments](/docs/features/PLANNED/27_magic_moments/FC-027_KOMPAKT.md)** - Event Pattern Detection

### 🔧 Technische Details:
- **[FC-023_IMPLEMENTATION_GUIDE.md](./FC-023_IMPLEMENTATION_GUIDE.md)** - Event Store Pattern
- **[FC-023_DECISION_LOG.md](./FC-023_DECISION_LOG.md)** - Event Sourcing vs. CRUD
- **[EVENT_CATALOG.md](./EVENT_CATALOG.md)** - Alle Domain Events