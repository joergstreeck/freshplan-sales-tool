# ⚠️ WICHTIGE KORREKTUR zum PR #5 Plan

**Datum:** 13.08.2025 19:50  
**Erstellt von:** Claude  
**Priorität:** 🔴 KRITISCH

---

## 🚨 DER URSPRÜNGLICHE PLAN IST FALSCH!

### ❌ Was der Plan sagt (FALSCH):
- Implementiere Domain Events
- Nutze Event Bus
- Fire CustomerCreatedEvent, CustomerUpdatedEvent, etc.
- Event-Driven Architecture mit @Observes

### ✅ Was TATSÄCHLICH gemacht werden muss (RICHTIG):
- **KEINE Domain Events implementieren**
- **KEIN Event Bus verwenden**
- **Timeline Events** direkt in DB speichern (wie CustomerService es macht)
- **Category** für Timeline Events setzen (NOT NULL constraint!)

---

## 📝 Korrekte Implementierung:

### CustomerCommandService muss:
```java
// RICHTIG - Timeline Event erstellen:
private void createTimelineEvent(
    Customer customer,
    String eventType,
    String description,
    String performedBy,
    ImportanceLevel importance
) {
    CustomerTimelineEvent event = new CustomerTimelineEvent();
    event.setCustomer(customer);
    event.setEventType(eventType);
    event.setEventDate(LocalDateTime.now());
    event.setTitle(generateEventTitle(eventType));
    event.setDescription(description);
    event.setPerformedBy(performedBy);
    event.setImportance(importance);
    
    // KRITISCH: Category MUSS gesetzt werden!
    event.setCategory(mapEventTypeToCategory(eventType));
    
    customer.getTimelineEvents().add(event);
}
```

### Was NICHT implementiert werden soll:
```java
// FALSCH - Keine Domain Events!
@Inject Event<CustomerDomainEvent> eventBus;

// FALSCH - Kein Event firing!
eventBus.fire(new CustomerCreatedEvent(...));
```

---

## 🎯 Aktueller Stand (13.08.2025 19:50):

### ✅ Bereits korrekt implementiert:
1. **CustomerCommandService** mit 2 von 7 Methoden:
   - `createCustomer()` - mit Timeline Events
   - `updateCustomer()` - identisch zu Original
   
2. **Integration Tests** beweisen identisches Verhalten

### ⏳ Noch zu implementieren:
- 5 weitere Command-Methoden (alle mit Timeline Events)
- CustomerQueryService (8 Query-Methoden)
- CustomerResource mit Feature Flag

---

## 📚 Relevante Dokumente:

1. **ANALYSIS_RESULTS.md** - Enthält die korrigierte Analyse
2. **PR_5_BACKEND_SERVICES_REFACTORING.md** - Wurde korrigiert mit Warnhinweisen
3. **PR_5_IMPLEMENTATION_LOG.md** - Zeigt den tatsächlichen Fortschritt

---

**WICHTIG:** Folge NICHT dem Domain Events Teil des Plans! CustomerService nutzt Timeline Events, und die neue Implementierung muss identisch sein für nahtloses Switching via Feature Flag.