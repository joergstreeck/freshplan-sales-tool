# 🔗 Integration Guide: FC-005 Contact Management ↔ FC-018 DSGVO Compliance

**Dokument:** Integration zwischen Contact Management und DSGVO System  
**Erstellt:** 31.07.2025  
**Status:** 📋 GEPLANT  
**Sprint:** Sprint 2, Woche 2 & 4  

## 📌 Overview

FC-005 implementiert DSGVO-Compliance von Grund auf mit Consent Management, Crypto-Shredding und Export-Funktionen. FC-018 nutzt diese Foundation für systemweite Compliance.

## 🏗️ Integration Architecture

```
FC-005 Contact Management
    ├── Consent Fields
    ├── Encrypted PII
    └── Export API
           ↓
    FC-018 DSGVO System
    ├── Consent Registry
    ├── Deletion Service
    └── Compliance Dashboard
```

## 🔒 Privacy by Design Implementation

### 1. Consent Management (Woche 2)
```typescript
// In Contact Entity
interface ContactGDPRData {
  consents: ConsentRecord[];
  dataProcessingBasis: ProcessingBasis;
  retentionExpiry?: Date;
  encryptionKeyId: string; // Für Crypto-Shredding
}

interface ConsentRecord {
  type: 'marketing' | 'communication' | 'data_processing';
  granted: boolean;
  grantedAt?: Date;
  revokedAt?: Date;
  version: string; // Consent Text Version
  ipAddress?: string; // Für Nachweis
}

type ProcessingBasis = 
  | 'consent'
  | 'contract'
  | 'legal_obligation'
  | 'legitimate_interest';
```

### 2. Crypto-Shredding Pattern
```typescript
// Verschlüsselung personenbezogener Daten
interface EncryptedContactData {
  // Verschlüsselt
  personalData: EncryptedPayload;
  
  // Unverschlüsselt (für Geschäftslogik)
  id: string;
  customerId: string;
  createdAt: Date;
  status: ContactStatus;
}

interface EncryptedPayload {
  ciphertext: string;
  algorithm: 'AES-256-GCM';
  nonce: string;
  keyId: string; // Referenz zum Key Vault
}
```

## 📤 DSGVO Export API

### Export Request (FC-005)
```typescript
@Controller('/api/contacts')
class ContactGDPRController {
  @Post('/:contactId/gdpr-export')
  @RequireConsent('data_export')
  async exportContactData(contactId: string): Promise<GDPRExport> {
    // 1. Verify Request
    await this.verifyDataSubject(contactId);
    
    // 2. Collect All Data
    const data = await this.collectAllContactData(contactId);
    
    // 3. Create Export
    return {
      exportId: generateId(),
      requestedAt: new Date(),
      data: {
        personalData: data.personal,
        communicationHistory: data.communications,
        consents: data.consents,
        processingActivities: data.activities
      },
      format: 'json', // oder 'pdf'
      expiresAt: addDays(new Date(), 30)
    };
  }
}
```

### Export Format
```json
{
  "exportMetadata": {
    "requestId": "exp_123",
    "contactId": "con_456",
    "requestedAt": "2025-08-01T10:00:00Z",
    "dataController": "FreshPlan GmbH"
  },
  "personalData": {
    "name": "Max Mustermann",
    "email": "max@example.com",
    "phone": "+49 123 456789",
    "addresses": [...],
    "customFields": {...}
  },
  "relationships": {
    "hobbies": ["Golf", "Wine"],
    "birthday": "1980-01-01",
    "notes": "..."
  },
  "processingHistory": [
    {
      "timestamp": "2025-01-01T10:00:00Z",
      "action": "CONTACT_CREATED",
      "purpose": "contract",
      "legalBasis": "Art. 6 Abs. 1 lit. b DSGVO"
    }
  ],
  "consents": [...]
}
```

## 🗑️ Right to Erasure (Löschung)

### Deletion Flow
```typescript
// FC-005 Deletion Service
class ContactDeletionService {
  async deleteContact(
    contactId: string, 
    request: DeletionRequest
  ): Promise<DeletionResult> {
    // 1. Validate Request
    await this.validateDeletionRequest(request);
    
    // 2. Check Legal Obligations
    const obligations = await this.checkLegalObligations(contactId);
    if (obligations.mustRetain) {
      return this.anonymizeInstead(contactId, obligations.reason);
    }
    
    // 3. Create Deletion Event
    await this.eventStore.append({
      type: 'CONTACT_DELETION_REQUESTED',
      aggregateId: contactId,
      payload: {
        reason: request.reason,
        requestedBy: request.dataSubject,
        scheduledFor: this.calculateDeletionDate()
      }
    });
    
    // 4. Crypto-Shred Personal Data
    await this.keyVault.scheduleKeyDeletion(
      contactId,
      this.calculateDeletionDate()
    );
    
    return {
      status: 'scheduled',
      deletionDate: this.calculateDeletionDate(),
      retainedData: obligations.retainedFields
    };
  }
}
```

### Anonymisierung statt Löschung
```typescript
interface AnonymizedContact {
  id: string; // Bleibt für Referenzen
  customerId: string;
  
  // Anonymisierte Daten
  name: 'ANONYMIZED',
  email: 'deleted@anonymous.invalid',
  phone: null,
  
  // Geschäftsdaten bleiben (wenn erlaubt)
  orderCount?: number;
  totalRevenue?: number;
  lastOrderDate?: Date;
  
  // Anonymisierung Metadata
  anonymizedAt: Date;
  reason: 'gdpr_request' | 'retention_expired';
}
```

## 📊 Consent Lifecycle Automation

### Automated Consent Renewal
```typescript
// Background Job in FC-018
@Cron('0 0 * * *') // Täglich
async checkConsentExpiry() {
  const expiringConsents = await this.findExpiringConsents(30); // 30 Tage
  
  for (const consent of expiringConsents) {
    await this.eventStore.append({
      type: 'CONSENT_RENEWAL_REQUIRED',
      aggregateId: consent.contactId,
      payload: {
        consentType: consent.type,
        expiresAt: consent.expiresAt,
        renewalUrl: this.generateRenewalUrl(consent)
      }
    });
  }
}
```

## 🛡️ Compliance Monitoring

### GDPR Compliance Metrics (FC-018 Dashboard)
```typescript
interface GDPRComplianceMetrics {
  contacts: {
    total: number;
    withValidConsent: number;
    consentRate: number; // Percentage
  };
  
  dataRequests: {
    exports: { pending: number; completed: number };
    deletions: { pending: number; completed: number };
    corrections: { pending: number; completed: number };
  };
  
  risks: Array<{
    type: 'expired_consent' | 'missing_basis' | 'retention_exceeded';
    count: number;
    severity: 'high' | 'medium' | 'low';
  }>;
}
```

## 🧪 Compliance Testing

### DSGVO Test Scenarios
```typescript
describe('GDPR Compliance', () => {
  describe('Data Export', () => {
    it('should export all personal data within 30 days', async () => {
      const exportRequest = await requestDataExport(contactId);
      expect(exportRequest.status).toBe('processing');
      
      // Verify export contains all data
      const exportData = await getExport(exportRequest.id);
      expect(exportData).toMatchSchema(GDPRExportSchema);
    });
  });
  
  describe('Consent Management', () => {
    it('should prevent processing without consent', async () => {
      const contact = await createContact({ consents: [] });
      
      await expect(
        sendMarketingEmail(contact.id)
      ).rejects.toThrow('Missing marketing consent');
    });
  });
});
```

## 📋 Implementation Checklist

### FC-005 Tasks (Sprint 2)
- [ ] Woche 2: Consent Fields im Contact Model
- [ ] Woche 2: Encryption für PII implementieren
- [ ] Woche 2: Consent UI Components
- [ ] Woche 4: Export API implementieren
- [ ] Woche 4: Deletion/Anonymization Service

### FC-018 Integration (später)
- [ ] Consent Registry aufbauen
- [ ] Compliance Dashboard
- [ ] Automated Compliance Checks
- [ ] Reporting für Behörden

## 📚 Weitere Dokumente

- [FC-018 DSGVO Konzept](/Users/joergstreeck/freshplan-sales-tool/docs/features/2025-07-25_TECH_CONCEPT_FC-018-datenschutz-dsgvo-compliance.md)
- [GDPR Compliance Architecture](/Users/joergstreeck/freshplan-sales-tool/docs/architecture/GDPR_COMPLIANCE_ARCHITECTURE.md)
- [Sprint 2 Master Plan](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/SPRINT2_MASTER_PLAN.md)

---

**Navigation:**
- [← FC-012 Audit Trail Integration](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/INTEGRATION_FC012_AUDIT_TRAIL.md)
- [→ Sprint 2 Master Plan](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/SPRINT2_MASTER_PLAN.md)