# Way of Working - FreshPlan Sales Tool

## Sprache und Benennung

**Grundregel: Klarheit vor Komplexität**

Alle Begriffe im UI und in der Dokumentation müssen einfach und verständlich sein. Englische Fachbegriffe und Berater-Deutsch sind zu vermeiden. Wir sprechen die Sprache unserer Nutzer - klar, direkt und ohne unnötige Fremdwörter.

**📋 Vollständige Sprachregeln:** Siehe [`/docs/UI_SPRACHREGELN.md`](./UI_SPRACHREGELN.md)

### Unsere Kernprinzipien:

1. **Deutsche Begriffe bevorzugen**: Verwende verständliche deutsche Bezeichnungen statt englischer Fachbegriffe
2. **Einfachheit**: Wähle den einfachsten Begriff, der die Funktion klar beschreibt  
3. **Nutzerorientierung**: Benenne Funktionen aus Sicht des Nutzers, nicht aus technischer Sicht
4. **Konsistenz**: Verwende einmal gewählte Begriffe durchgängig im gesamten System

### Ausnahmen:

Technische Dokumentation für Entwickler darf etablierte Fachbegriffe verwenden (z.B. API, REST, Docker), aber auch hier gilt: Wenn ein guter deutscher Begriff existiert, diesen bevorzugen.

### Umsetzung:

1. **Bei neuen Features**: Begriff-Checkliste vor Implementierung
2. **Code-Reviews**: Prüfung auf verständliche Benennung
3. **UI-Texte**: Immer aus Nutzersicht formulieren
4. **Fehlermeldungen**: Klar und handlungsorientiert
5. **Dokumentation**: Zielgruppengerecht (Nutzer vs. Entwickler)

Diese Regel gilt ab sofort für alle neuen Entwicklungen und wird schrittweise auch in bestehenden Bereichen umgesetzt.

## 2. Das Konsistenz-Protokoll

**Zweck:** Sicherstellung der Widerspruchsfreiheit über alle Planungs- und Dokumentationsebenen hinweg.

### Grundregel
Nach jeder getroffenen Entscheidung oder signifikanten Änderung MÜSSEN die folgenden Dokumente in dieser Reihenfolge geprüft und bei Bedarf sofort aktualisiert werden:

### Update-Hierarchie

1. **Das relevante "Spoke"-Dokument (Detail-Planung)**
   - Primärer Ort für alle Details und Implementierungsspezifika
   - Enthält: Code-Beispiele, genaue Spezifikationen, Analyse-Matrizen
   - Update: Bei jeder Code-Änderung oder Erkenntnisgewinn

2. **Das "Hub"-Dokument (FC-XXX-hub.md)**
   - Nur Status-Updates und übergreifende Änderungen
   - Enthält: Modulstatus (%), Abhängigkeiten, grobe Timeline
   - Update: Bei Modul-Abschluss oder Meilenstein-Erreichen

3. **Die Anhänge (falls betroffen)**
   - Anhang A: Strategische Entscheidungen und technische Ansätze
   - Anhang B: Backend-Anforderungen und API-Spezifikationen
   - Update: Bei Architektur-Entscheidungen oder API-Änderungen

4. **Der CRM_COMPLETE_MASTER_PLAN.md**
   - Nur bei Änderungen der groben Roadmap oder Vision
   - Update: Sehr selten, nur bei fundamentalen Richtungsänderungen

### Praktische Umsetzung

```bash
# Nach jeder Arbeitssession:
1. Code committen
2. Spoke-Dokument aktualisieren (Details, Status)
3. Hub-Dokument prüfen (Status-%, Fortschritt)
4. Bei Bedarf Anhänge anpassen
5. Master Plan nur bei Major Changes
```

### Konsistenz-Checks

- **Täglicher Check**: Stimmen die Prozentangaben im Hub mit dem Spoke-Status überein?
- **Wöchentlicher Check**: Sind alle Entscheidungen aus den Anhängen in den Spokes reflektiert?
- **Sprint-Check**: Ist der Master Plan noch aktuell bezüglich der groben Timeline?

### Anti-Patterns vermeiden

❌ **Nicht**: Nur Code ändern ohne Doku-Update
❌ **Nicht**: Nur Hub updaten ohne Spoke-Details
❌ **Nicht**: Widersprüchliche Status in verschiedenen Dokumenten
✅ **Sondern**: Immer von Detail (Spoke) zu Übersicht (Hub) arbeiten

## 3. Event-Driven Development Pattern (NEU - 09.07.2025)

**Zweck:** Lose Kopplung zwischen Modulen durch asynchrone Kommunikation über Domain Events.

### Wann Event-Driven verwenden?

**✅ Verwenden bei:**
- Kommunikation zwischen Bounded Contexts
- Seiteneffekte die andere Module betreffen
- Audit-Trail und History-Anforderungen
- Eventually Consistency ist akzeptabel

**❌ Nicht verwenden bei:**
- Interne Modul-Kommunikation
- Synchrone Validierung erforderlich
- Transaktionale Konsistenz nötig

### Event Design Principles

1. **Events sind Fakten der Vergangenheit**
   ```java
   // ✅ Gut: CustomerCreatedEvent (ist passiert)
   // ❌ Schlecht: CreateCustomerEvent (Befehl)
   ```

2. **Events sind immutable**
   ```java
   public record CustomerCreatedEvent(
       UUID eventId,
       UUID customerId,
       String customerNumber,
       LocalDateTime occurredAt
   ) implements DomainEvent {}
   ```

3. **Events enthalten minimale Daten**
   - Nur IDs, keine kompletten Entities
   - Consumer holen sich Details bei Bedarf

### Implementation Pattern

```java
// 1. Command Handler publiziert Event
@Transactional
public UUID createCustomer(CreateCommand cmd) {
    var customer = new Customer(cmd);
    repository.persist(customer);
    
    eventBus.publish(new CustomerCreatedEvent(
        UUID.randomUUID(),
        customer.getId(),
        customer.getNumber(),
        LocalDateTime.now()
    ));
    
    return customer.getId();
}

// 2. Event Handler in anderem Modul
@ApplicationScoped
public class TimelineEventHandler {
    void onCustomerCreated(@Observes CustomerCreatedEvent e) {
        var entry = new TimelineEntry(
            e.customerId(),
            "Customer created",
            e.occurredAt()
        );
        timelineRepo.persist(entry);
    }
}
```

### Testing Event-Driven Code

```java
@Test
void shouldPublishEventOnCustomerCreation() {
    // Given
    var command = new CreateCustomerCommand(...);
    
    // When
    service.createCustomer(command);
    
    // Then
    verify(eventBus).publish(
        argThat(event -> 
            event instanceof CustomerCreatedEvent &&
            ((CustomerCreatedEvent) event).customerNumber().equals("C-12345")
        )
    );
}
```

