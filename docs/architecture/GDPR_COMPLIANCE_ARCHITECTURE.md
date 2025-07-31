# 🛡️ GDPR Compliance Architecture - FreshPlan CRM

**Dokument:** DSGVO/GDPR Compliance Architektur  
**Erstellt:** 31.07.2025  
**Status:** 🟢 APPROVED  
**Compliance Officer:** [Name]  
**Letztes Audit:** [Datum]  

## 📌 Executive Summary

FreshPlan CRM implementiert DSGVO-Compliance durch **Privacy by Design** und **Privacy by Default**. Die Event Sourcing Architektur ermöglicht vollständige Transparenz bei gleichzeitigem Schutz personenbezogener Daten durch Crypto-Shredding.

## 🎯 Compliance-Ziele

### Rechtliche Anforderungen
- ✅ EU-DSGVO / GDPR Compliance
- ✅ BDSG-neu (Deutschland)
- ✅ ePrivacy-Richtlinie
- ✅ Schweizer DSG (für CH-Kunden)

### Technische Umsetzung
- ✅ Privacy by Design
- ✅ Privacy by Default
- ✅ Datenminimierung
- ✅ Zweckbindung
- ✅ Transparenz

## 🏗️ Architektur-Übersicht

```
┌─────────────────────────────────────────────────────┐
│                  User Interface                     │
│            (Consent Forms, Privacy Center)          │
└─────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────┐
│              Consent Management Layer               │
│        (Consent Storage, Version Control)           │
└─────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────┐
│               Business Logic Layer                  │
│         (with Privacy Enforcement)                  │
└─────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────┐
│            Encrypted Event Store                    │
│         (Crypto-Shredding Enabled)                  │
└─────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────┐
│              Key Management Service                 │
│            (HSM-backed Key Vault)                   │
└─────────────────────────────────────────────────────┘
```

## 🔐 Crypto-Shredding Implementation

### Konzept
Personenbezogene Daten werden verschlüsselt gespeichert. Bei Löschung wird nur der Schlüssel vernichtet - die Daten werden unlesbar.

### Implementation
```typescript
interface EncryptedPersonalData {
  // Verschlüsselte Felder
  encryptedPayload: {
    ciphertext: string;
    algorithm: 'AES-256-GCM';
    nonce: string;
    authTag: string;
  };
  
  // Schlüssel-Referenz
  keyMetadata: {
    keyId: string;
    keyVersion: number;
    createdAt: Date;
    purpose: 'personal_data_encryption';
  };
  
  // Unverschlüsselte Metadaten
  metadata: {
    dataSubjectId: string;
    dataCategories: DataCategory[];
    sensitivityLevel: 'public' | 'internal' | 'confidential' | 'restricted';
  };
}

type DataCategory = 
  | 'identification'    // Name, Geburtsdatum
  | 'contact'          // Email, Telefon
  | 'financial'        // Bankdaten, Kreditkarte
  | 'behavioral'       // Klickverhalten, Präferenzen
  | 'special';         // Gesundheit, Religion (Art. 9 DSGVO)
```

### Key Lifecycle
```typescript
interface KeyLifecycle {
  states: {
    ACTIVE: 'active';           // Verschlüsselung + Entschlüsselung
    DEACTIVATED: 'deactivated'; // Nur Entschlüsselung
    SCHEDULED_DELETE: 'scheduled'; // Löschung geplant
    DESTROYED: 'destroyed';     // Unwiderruflich gelöscht
  };
  
  transitions: {
    activate: () => void;
    deactivate: () => void;
    scheduleDestruction: (date: Date) => void;
    destroy: () => void; // Unwiderruflich!
  };
}
```

## 📋 Consent Management

### Consent Model
```typescript
interface ConsentRecord {
  // Identifikation
  consentId: string;
  dataSubjectId: string;
  
  // Consent Details
  purposes: ConsentPurpose[];
  grantedAt: Date;
  expiresAt?: Date;
  withdrawnAt?: Date;
  
  // Nachweis
  collectionMethod: 'form' | 'import' | 'verbal' | 'written';
  collectionDetails: {
    ipAddress?: string;
    userAgent?: string;
    formVersion: string;
    legalTextVersion: string;
  };
  
  // Gültigkeit
  scope: {
    dataCategories: DataCategory[];
    processingTypes: ProcessingType[];
    thirdParties?: string[];
  };
}

interface ConsentPurpose {
  purpose: string; // z.B. "marketing", "analytics"
  legalBasis: LegalBasis;
  description: string;
  mandatory: boolean;
}

type LegalBasis = 
  | 'consent'              // Art. 6 Abs. 1 lit. a
  | 'contract'             // Art. 6 Abs. 1 lit. b
  | 'legal_obligation'     // Art. 6 Abs. 1 lit. c
  | 'vital_interests'      // Art. 6 Abs. 1 lit. d
  | 'public_task'          // Art. 6 Abs. 1 lit. e
  | 'legitimate_interest'; // Art. 6 Abs. 1 lit. f
```

### Consent Enforcement
```typescript
// Decorator für Consent-Check
@RequireConsent('marketing')
async sendMarketingEmail(contactId: string) {
  // Wird nur ausgeführt wenn Consent vorhanden
}

// Programmatische Prüfung
class ConsentService {
  async checkConsent(
    dataSubjectId: string,
    purpose: string,
    dataCategories: DataCategory[]
  ): Promise<ConsentStatus> {
    const consents = await this.getActiveConsents(dataSubjectId);
    
    // Prüfe ob Consent für Purpose und Categories vorhanden
    const hasValidConsent = consents.some(c => 
      c.purposes.includes(purpose) &&
      c.scope.dataCategories.includes(...dataCategories) &&
      !c.withdrawnAt &&
      (!c.expiresAt || c.expiresAt > new Date())
    );
    
    return {
      granted: hasValidConsent,
      consent: hasValidConsent ? consents[0] : null,
      missingPurposes: hasValidConsent ? [] : [purpose]
    };
  }
}
```

## 🗂️ Betroffenenrechte (Data Subject Rights)

### 1. Auskunftsrecht (Art. 15)
```typescript
interface DataExportService {
  async exportPersonalData(
    dataSubjectId: string,
    format: 'json' | 'pdf' | 'csv'
  ): Promise<DataExport> {
    // Sammle alle Daten
    const data = await this.collectAllData(dataSubjectId);
    
    // Erstelle Export
    return {
      exportId: generateId(),
      requestedAt: new Date(),
      dataSubject: data.profile,
      personalData: data.personal,
      processingActivities: data.activities,
      consents: data.consents,
      thirdPartySharing: data.sharing,
      retentionPeriods: data.retention,
      format,
      downloadUrl: await this.generateSecureUrl(data, format),
      expiresAt: addDays(new Date(), 30)
    };
  }
}
```

### 2. Berichtigungsrecht (Art. 16)
```typescript
interface DataCorrectionService {
  async correctData(
    dataSubjectId: string,
    corrections: DataCorrection[]
  ): Promise<CorrectionResult> {
    // Audit Trail
    await this.eventStore.append({
      type: 'DATA_CORRECTION_REQUESTED',
      dataSubjectId,
      corrections,
      requestedAt: new Date()
    });
    
    // Anwende Korrekturen
    for (const correction of corrections) {
      await this.applyCorrection(correction);
    }
    
    return { status: 'completed', correctedFields: corrections.length };
  }
}
```

### 3. Löschungsrecht (Art. 17)
```typescript
interface DataDeletionService {
  async deletePersonalData(
    dataSubjectId: string,
    request: DeletionRequest
  ): Promise<DeletionResult> {
    // Prüfe Aufbewahrungspflichten
    const obligations = await this.checkLegalObligations(dataSubjectId);
    
    if (obligations.mustRetain) {
      // Anonymisierung statt Löschung
      return this.anonymizeData(dataSubjectId, obligations);
    }
    
    // Crypto-Shredding
    await this.keyService.scheduleKeyDestruction(
      dataSubjectId,
      this.calculateDeletionDate()
    );
    
    // Audit Event
    await this.eventStore.append({
      type: 'DATA_DELETION_SCHEDULED',
      dataSubjectId,
      scheduledFor: this.calculateDeletionDate(),
      reason: request.reason
    });
    
    return {
      status: 'scheduled',
      deletionDate: this.calculateDeletionDate()
    };
  }
}
```

### 4. Datenportabilität (Art. 20)
```typescript
interface DataPortabilityService {
  async exportPortableData(
    dataSubjectId: string
  ): Promise<PortableDataPackage> {
    // Nur Daten die auf Consent/Vertrag basieren
    const portableData = await this.getPortableData(dataSubjectId);
    
    return {
      format: 'application/json', // Maschinenlesbar
      data: {
        profile: portableData.profile,
        contacts: portableData.contacts,
        activities: portableData.activities,
        preferences: portableData.preferences
      },
      checksum: await this.calculateChecksum(portableData),
      signature: await this.signPackage(portableData)
    };
  }
}
```

## 🔍 Privacy Impact Assessment (PIA)

### Automatisierte Risikobewertung
```typescript
interface PrivacyRiskAssessment {
  assessRisk(processing: ProcessingActivity): RiskLevel {
    let score = 0;
    
    // Datenkategorien
    if (processing.includesSpecialCategories) score += 3;
    if (processing.includesFinancialData) score += 2;
    
    // Umfang
    if (processing.dataSubjects > 10000) score += 2;
    if (processing.isSystematic) score += 1;
    
    // Zweck
    if (processing.includesProfiling) score += 2;
    if (processing.includesAutomatedDecisions) score += 3;
    
    return score > 5 ? 'high' : score > 2 ? 'medium' : 'low';
  }
}
```

## 📊 Compliance Monitoring

### Automated Compliance Checks
```typescript
@Scheduled('0 0 * * *') // Täglich
class ComplianceMonitor {
  async runDailyChecks() {
    await this.checkExpiredConsents();
    await this.checkRetentionPeriods();
    await this.checkDataMinimization();
    await this.checkThirdPartySharing();
    await this.generateComplianceReport();
  }
  
  async checkExpiredConsents() {
    const expired = await this.consentRepo.findExpired();
    for (const consent of expired) {
      await this.eventStore.append({
        type: 'CONSENT_EXPIRED',
        consentId: consent.id,
        dataSubjectId: consent.dataSubjectId,
        action: 'processing_stopped'
      });
    }
  }
}
```

### Compliance Dashboard Metriken
```typescript
interface ComplianceMetrics {
  consents: {
    active: number;
    expired: number;
    withdrawn: number;
    renewalNeeded: number;
  };
  
  dataRequests: {
    pending: RequestCount;
    completed: RequestCount;
    averageResponseTime: Duration;
  };
  
  risks: {
    high: RiskItem[];
    medium: RiskItem[];
    low: RiskItem[];
  };
  
  incidents: {
    breaches: number;
    nearMisses: number;
    reportedToDPA: number; // Data Protection Authority
  };
}
```

## 🚨 Breach Response Plan

### Automatisierte Breach Detection
```typescript
interface BreachDetectionService {
  detectAnomalies(): void {
    // Ungewöhnliche Datenzugriffe
    // Massen-Exports
    // Unauthorized Access Attempts
  }
  
  async handleBreach(breach: SecurityBreach) {
    // 1. Eindämmung
    await this.containBreach(breach);
    
    // 2. Bewertung
    const assessment = await this.assessImpact(breach);
    
    // 3. Benachrichtigung (72h Frist!)
    if (assessment.requiresNotification) {
      await this.notifyDPA(breach, assessment);
      await this.notifyDataSubjects(breach, assessment);
    }
    
    // 4. Dokumentation
    await this.documentBreach(breach, assessment);
  }
}
```

## 📚 Compliance-Dokumentation

### Verarbeitungsverzeichnis (Art. 30)
```typescript
interface ProcessingRecord {
  // Verantwortlicher
  controller: {
    name: string;
    contact: ContactInfo;
    representative?: ContactInfo;
    dpo: ContactInfo; // Data Protection Officer
  };
  
  // Verarbeitungstätigkeit
  activity: {
    name: string;
    purposes: string[];
    legalBasis: LegalBasis[];
    dataCategories: DataCategory[];
    dataSubjects: string[];
    recipients: string[];
    thirdCountryTransfers?: TransferInfo[];
    retentionPeriod: Duration;
    technicalMeasures: string[];
    organizationalMeasures: string[];
  };
}
```

## ✅ Compliance Checklist

### Technische Maßnahmen
- [x] Verschlüsselung at-rest (AES-256)
- [x] Verschlüsselung in-transit (TLS 1.3)
- [x] Crypto-Shredding implementiert
- [x] Zugriffskontrolle (RBAC)
- [x] Audit Logging
- [x] Backup & Recovery
- [x] Pseudonymisierung

### Organisatorische Maßnahmen
- [ ] Datenschutzbeauftragter benannt
- [ ] Mitarbeiterschulungen
- [ ] Verarbeitungsverzeichnis
- [ ] Auftragsverarbeiter-Verträge
- [ ] Datenschutz-Folgenabschätzung
- [ ] Incident Response Plan
- [ ] Regular Audits

## 🔗 Integration mit Modulen

- **FC-005**: Contact PII Encryption
- **FC-012**: Audit Trail für Compliance
- **FC-018**: DSGVO UI & Automation
- **Alle Module**: Consent Enforcement

## 📚 Weitere Dokumente

- [Data Processing Agreements](/legal/dpa/)
- [Privacy Policy](/legal/privacy/)
- [Cookie Policy](/legal/cookies/)
- [Retention Policy](/docs/policies/retention/)

---

**Compliance Officer:** [Name]  
**DPO Contact:** dpo@freshplan.de  
**Letztes Audit:** [Datum]  
**Nächstes Review:** [Datum]