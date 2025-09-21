# FC-003: E-Mail-Integration (BCC-to-CRM)

**Feature Code:** FC-003  
**Status:** 📋 Planungsphase  
**Geschätzter Aufwand:** 5-7 Tage  
**Priorität:** HOCH - Direktes Nutzerfeedback  
**Phase:** 4.1 (Team-Funktionen)  

## 🎯 Zusammenfassung

Vertriebsmitarbeiter schreiben täglich dutzende E-Mails. Diese manuell ins CRM zu kopieren ist Zeitverschwendung. Unsere BCC-to-CRM Lösung automatisiert diesen Prozess vollständig.

## 📧 Kernkonzept

```
Spezielle E-Mail-Adresse: kunde-123@crm.freshplan.de
BCC an diese Adresse → E-Mail wird automatisch beim Kunden 123 gespeichert
```

## 🏗️ Technische Architektur

### 1. E-Mail-Empfang (Backend)

**Komponenten:**
- **SMTP-Receiver Service**: Empfängt eingehende E-Mails
- **E-Mail-Parser**: Extrahiert Metadaten und Content
- **Customer-Matcher**: Ordnet E-Mails Kunden zu
- **Attachment-Handler**: Speichert Anhänge in S3

**Tech-Stack:**
- Java Mail API für SMTP-Handling
- Apache James oder eigener SMTP-Server
- S3 für Attachment-Storage
- Event-Driven über Domain Events

### 2. E-Mail-Verarbeitung (Business Logic)

```java
// Pseudo-Code für E-Mail-Verarbeitung
@ApplicationScoped
public class EmailIngestionService {
    
    public void processIncomingEmail(IncomingEmail email) {
        // 1. Parse Customer-ID aus Empfänger
        Optional<UUID> customerId = extractCustomerId(email.getTo());
        
        // 2. Falls zugeordnet → Customer Activity
        if (customerId.isPresent()) {
            createCustomerActivity(customerId.get(), email);
        } else {
            // 3. Sonst → Triage Inbox
            addToTriageInbox(email);
        }
        
        // 4. Event publizieren
        eventBus.publish(new EmailReceivedEvent(email));
    }
}
```

### 3. Frontend-Integration

**Betroffene Module:**
- **M3 Cockpit**: Triage-Inbox zeigt unzugeordnete E-Mails
- **M5 Kundenmanagement**: E-Mail-Historie in Timeline
- **M4 Aktivitäten**: E-Mails als Activity-Type

**UI-Komponenten:**
- `EmailTimeline.tsx`: Zeigt E-Mail-Konversationen
- `TriageInbox.tsx`: Unzugeordnete E-Mails (bereits vorhanden)
- `EmailComposer.tsx`: Integrierter E-Mail-Editor

## 🔗 Abhängigkeiten

### Zu bestehenden Modulen:
- **Customer Timeline (M5)**: E-Mails werden als Events gespeichert
- **Activity Module (M4)**: E-Mail ist ein Activity-Type
- **Cockpit (M3)**: Integration der Triage-Inbox

### Zu neuen Features:
- **FC-004 (Verkäuferschutz)**: E-Mails dokumentieren Kundenkontakt
- **FC-006 (Mobile App)**: E-Mail-Benachrichtigungen auf Mobile

## 🏛️ Architektur-Entscheidungen

### ADR-003-001: E-Mail-Speicherung
**Entscheidung:** E-Mails werden als strukturierte Daten gespeichert, nicht als Blobs
- Header-Informationen in DB
- Body als Text + HTML in DB
- Attachments in S3 mit Referenz

### ADR-003-002: Thread-Erkennung
**Entscheidung:** Nutze Message-ID und References Header für Thread-Bildung
- Konversationen werden automatisch gruppiert
- Chronologische Darstellung in Timeline

## 🚀 Implementierungsphasen

### Phase 1: Backend-Infrastruktur (2 Tage)
1. SMTP-Receiver implementieren
2. E-Mail-Parser entwickeln
3. Customer-Matching-Logik
4. Event-Integration

### Phase 2: Datenmodell & API (1 Tag)
1. Email-Entity und Repository
2. REST-Endpoints für E-Mail-Zugriff
3. Integration in Customer-Timeline

### Phase 3: Frontend-Integration (2 Tage)
1. E-Mail-Timeline-Komponente
2. Triage-Inbox erweitern
3. E-Mail-Composer (Basic)

### Phase 4: Polish & Features (2 Tage)
1. Thread-Gruppierung
2. Attachment-Handling
3. E-Mail-Templates
4. Performance-Optimierung

## 🧪 Test-Strategie

```java
@Test
void shouldAssignEmailToCorrectCustomer() {
    // Given
    var email = createTestEmail("kunde-123@crm.freshplan.de");
    
    // When
    emailService.processIncomingEmail(email);
    
    // Then
    verify(customerTimeline).addEmailActivity(customerId(123), email);
}
```

## 📊 Erfolgsmetriken

- Zeitersparnis: 30-45 Min/Tag pro Verkäufer
- Automatisierungsgrad: >95% der E-Mails korrekt zugeordnet
- Performance: E-Mail-Verarbeitung <2 Sekunden

## 🔍 Offene Fragen

1. **E-Mail-Server**: Eigener SMTP oder Service (SendGrid)?
2. **Speicher-Retention**: Wie lange E-Mails aufbewahren?
3. **DSGVO**: Zustimmung für E-Mail-Speicherung nötig?
4. **Verschlüsselung**: E-Mails verschlüsselt speichern?