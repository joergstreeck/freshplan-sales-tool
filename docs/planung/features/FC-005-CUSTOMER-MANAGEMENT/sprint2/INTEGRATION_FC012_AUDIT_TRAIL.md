# 🔗 Integration Guide: FC-005 Contact Management ↔ FC-012 Audit Trail

**Dokument:** Integration zwischen Contact Events und Audit Trail  
**Erstellt:** 31.07.2025  
**Status:** 📋 GEPLANT  
**Sprint:** Sprint 2, Woche 4  

## 📌 Overview

FC-012 Audit Trail nutzt die Event Sourcing Foundation von FC-005. Alle Contact Events werden automatisch im Audit Log erfasst, ohne zusätzlichen Code in FC-005.

## 🏗️ Integration Architecture

```
FC-005 Contact Events
    ↓
Event Store (Shared)
    ↓
FC-012 Event Listener → Audit Projections
                        ↓
                     Audit Log UI
```

## 📦 Shared Event Types

### Contact Events (von FC-005)
```typescript
// Diese Events werden automatisch auditiert
type AuditRelevantContactEvents = 
  | 'CONTACT_CREATED'
  | 'CONTACT_UPDATED'
  | 'CONTACT_DELETED'
  | 'CONTACT_ARCHIVED'
  | 'CONSENT_UPDATED'
  | 'LOCATION_ASSIGNED'
  | 'RELATIONSHIP_DATA_ADDED';
```

### Audit Metadata (von FC-012)
```typescript
interface AuditMetadata extends EventMetadata {
  // Zusätzlich zu Standard EventMetadata
  auditRelevance: 'high' | 'medium' | 'low';
  complianceCategory?: 'gdpr' | 'financial' | 'operational';
  retentionPeriod: Duration; // z.B. "7 years"
}
```

## 🔄 Event Flow

### 1. Contact Event wird erzeugt (FC-005)
```typescript
// In ContactService
async createContact(command: CreateContactCommand) {
  const event: ContactCreatedEvent = {
    type: 'CONTACT_CREATED',
    aggregateId: generateId(),
    payload: { ...contactData },
    metadata: {
      userId: currentUser.id,
      source: 'ui',
      // Audit-relevante Felder werden automatisch ergänzt
    }
  };
  
  await this.eventStore.append(event);
}
```

### 2. Audit Trail empfängt Event (FC-012)
```typescript
// In AuditEventListener
@EventHandler(['CONTACT_*', 'CONSENT_*'])
async handleContactEvents(event: ContactEvent) {
  const auditEntry = await this.createAuditEntry(event);
  await this.auditProjection.update(auditEntry);
}
```

### 3. Audit UI zeigt Events
```typescript
// Audit Log zeigt automatisch:
- Wer: event.metadata.userId
- Was: event.type
- Wann: event.timestamp
- Wo: event.metadata.source
- Warum: event.payload.reason (falls vorhanden)
```

## 📊 Audit-Specific Projections

### Contact Audit View
```typescript
interface ContactAuditView {
  contactId: string;
  changeHistory: Array<{
    timestamp: Date;
    changeType: string;
    changedBy: User;
    changedFields: string[];
    oldValues?: Record<string, any>;
    newValues?: Record<string, any>;
  }>;
  complianceFlags: {
    hasGdprConsent: boolean;
    lastConsentUpdate?: Date;
    dataRetentionExpiry?: Date;
  };
}
```

## 🛠️ Implementation Checklist

### FC-005 Seite (bereits erledigt durch Event Sourcing)
- [x] Events mit vollständigen Metadaten
- [x] User Context in allen Events
- [x] Timestamps korrekt
- [x] Event Types typisiert

### FC-012 Seite (zu implementieren)
- [ ] Event Listener für Contact Events
- [ ] Audit Projection erstellen
- [ ] Retention Policy für Contact Events
- [ ] Export Format für Contact Audits

## 🔒 Security Considerations

### Audit Log Zugriff
```typescript
// Nur autorisierte Rollen
@RequireRole(['admin', 'compliance_officer'])
class AuditLogController {
  // Contact-bezogene Audit Logs
  @Get('/audit/contacts/:contactId')
  async getContactAuditLog(contactId: string) {
    // Zusätzliche Berechtigungsprüfung
    await this.checkContactAccess(contactId);
    return this.auditService.getContactHistory(contactId);
  }
}
```

## 📈 Performance Impact

### Event Volume Schätzung
- Contact Events: ~100-500 pro Tag
- Audit Entries: 1:1 mit Events
- Storage: ~2KB pro Audit Entry
- Retention: 7 Jahre (gesetzlich)

### Optimierungen
1. **Async Processing**: Audit Logs werden asynchron erstellt
2. **Batch Inserts**: Audit Entries werden gebatcht
3. **Archivierung**: Alt-Daten nach 1 Jahr → Cold Storage

## 🧪 Testing Strategy

### Integration Tests
```typescript
describe('Contact-Audit Integration', () => {
  it('should create audit entry for contact creation', async () => {
    // When
    const contact = await contactService.create({...});
    
    // Then (eventually consistent)
    await eventually(async () => {
      const auditLog = await auditService.findByAggregateId(contact.id);
      expect(auditLog).toHaveLength(1);
      expect(auditLog[0].eventType).toBe('CONTACT_CREATED');
    });
  });
});
```

## 📚 Weitere Dokumente

- [FC-012 Audit Trail Konzept](/Users/joergstreeck/freshplan-sales-tool/docs/features/2025-07-24_TECH_CONCEPT_FC-012-audit-trail-system.md)
- [Event Sourcing Foundation](/Users/joergstreeck/freshplan-sales-tool/docs/architecture/EVENT_SOURCING_FOUNDATION.md)
- [Sprint 2 Master Plan](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/SPRINT2_MASTER_PLAN.md)

## ✅ Definition of Done

- [ ] Alle Contact Events werden auditiert
- [ ] Audit UI zeigt Contact History
- [ ] Export funktioniert für Compliance
- [ ] Performance innerhalb SLAs
- [ ] Integration Tests grün

---

**Navigation:**
- [← Sprint 2 Master Plan](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/SPRINT2_MASTER_PLAN.md)
- [→ FC-018 DSGVO Integration](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/INTEGRATION_FC018_DSGVO.md)