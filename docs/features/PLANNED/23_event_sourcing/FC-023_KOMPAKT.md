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