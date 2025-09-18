# FC-012: Export Formats für Audit Trail

**Parent:** [/Users/joergstreeck/freshplan-sales-tool/docs/features/2025-07-24_TECH_CONCEPT_FC-012-audit-trail-system.md](/Users/joergstreeck/freshplan-sales-tool/docs/features/2025-07-24_TECH_CONCEPT_FC-012-audit-trail-system.md)  
**Fokus:** Export-Formate für Compliance und Audits

## 📄 CSV Export

### Standard CSV Format
```csv
"Timestamp","User","Role","Event Type","Entity Type","Entity ID","Old Value","New Value","Reason","IP Address","Source"
"2025-07-24T14:30:00Z","Max Müller","sales","OPPORTUNITY_STAGE_CHANGED","opportunity","uuid-123","LEAD","QUALIFIED","Kunde hat Budget bestätigt","192.168.1.100","UI"
"2025-07-24T14:35:00Z","System","system","CONTRACT_RENEWAL_INITIATED","contract","uuid-456","","","Automatic renewal process started","","SYSTEM"
```

### Erweiterte CSV mit Metadaten
```csv
# Export-Datum: 2025-07-24T16:00:00Z
# Export-User: compliance@freshplan.de
# Filter: eventType=CONTRACT_*, dateRange=2025-01-01/2025-07-24
# Datensätze: 1523
# Hash-Validierung: PASSED
"Timestamp","User","Role","Event Type","Entity Type","Entity ID","Customer Name","Contract Value","Old Value","New Value","Reason","Comment","IP Address","User Agent","Session ID","Request ID","Data Hash","Previous Hash"
```

## 📊 Excel Export (XLSX)

### Worksheet-Struktur
```
Workbook: audit_trail_2025-07-24.xlsx
├── Summary (Übersicht)
├── Audit_Trail (Hauptdaten)
├── User_Activity (Benutzer-Statistik)
├── Critical_Events (Kritische Ereignisse)
├── Integrity_Report (Hash-Validierung)
└── Metadata (Export-Informationen)
```

### Summary Sheet
```
| Metric | Value |
|--------|-------|
| Export Date | 2025-07-24 16:00:00 |
| Period | 2025-01-01 to 2025-07-24 |
| Total Events | 15,234 |
| Unique Users | 42 |
| Critical Events | 156 |
| Hash Validation | ✓ PASSED |
| Compliance Status | ✓ COMPLIANT |
```

## 📑 PDF Export

### Struktur für Wirtschaftsprüfer
```
FreshPlan CRM - Audit Trail Report
==================================

1. Executive Summary
   - Berichtszeitraum
   - Compliance-Status
   - Kritische Findings
   - Integritätsprüfung

2. Detaillierte Transaktionen
   - Chronologische Auflistung
   - Gruppiert nach Event-Typ
   - Mit Kontext-Informationen

3. Benutzer-Aktivitätsmatrix
   - Aktivitäten je Benutzer
   - Zugriffszeiten
   - Kritische Operationen

4. Compliance-Nachweis
   - GoBD-Konformität
   - DSGVO-Compliance
   - Aufbewahrungsfristen

5. Technische Validierung
   - Hash-Chain Integrität
   - Lückenlose Nummerierung
   - Zeitstempel-Konsistenz

6. Anhänge
   - Glossar Event-Types
   - Technische Spezifikation
   - Unterschriften-Seite
```

### PDF-Generation Code
```java
@ApplicationScoped
public class AuditPdfExporter {
    
    @Inject
    PdfGenerator pdfGenerator;
    
    public byte[] generateComplianceReport(
        List<AuditEntry> entries,
        AuditReportMetadata metadata
    ) {
        var document = new Document();
        
        // Cover Page
        document.add(new CoverPage(
            "Audit Trail Compliance Report",
            metadata.getPeriod(),
            metadata.getGeneratedBy(),
            LocalDateTime.now()
        ));
        
        // Executive Summary
        document.add(new ExecutiveSummary(
            entries.size(),
            countCriticalEvents(entries),
            validateIntegrity(entries),
            checkCompliance(entries)
        ));
        
        // Table of Contents
        document.add(new TableOfContents());
        
        // Detailed Sections
        document.add(new DetailedTransactions(entries));
        document.add(new UserActivityMatrix(entries));
        document.add(new ComplianceChecklist(entries));
        document.add(new TechnicalValidation(entries));
        
        // Digital Signature
        document.add(new DigitalSignature(
            metadata.getSignatureKey(),
            calculateDocumentHash(document)
        ));
        
        return pdfGenerator.generate(document);
    }
}
```

## 🔐 Verschlüsselte Exports

### Encrypted ZIP Archive
```bash
# Struktur
audit_export_2025-07-24.zip.enc
├── manifest.json         # Export-Metadaten
├── audit_trail.csv      # Hauptdaten
├── integrity_proof.txt  # Hash-Chain Nachweis
├── signature.sig        # Digitale Signatur
└── README.txt          # Entschlüsselungs-Anleitung
```

### Verschlüsselungs-Implementation
```java
public class SecureAuditExporter {
    
    public EncryptedExport createSecureExport(
        List<AuditEntry> entries,
        String recipientPublicKey
    ) {
        // Create ZIP with all files
        var zipData = createZipArchive(entries);
        
        // Generate AES key
        var aesKey = generateAESKey();
        
        // Encrypt ZIP
        var encryptedData = encryptWithAES(zipData, aesKey);
        
        // Encrypt AES key with recipient's public key
        var encryptedKey = encryptWithRSA(aesKey, recipientPublicKey);
        
        // Create manifest
        var manifest = ExportManifest.builder()
            .exportId(UUID.randomUUID())
            .timestamp(Instant.now())
            .recordCount(entries.size())
            .checksum(calculateSHA256(zipData))
            .encryptedKey(Base64.encode(encryptedKey))
            .build();
        
        return new EncryptedExport(encryptedData, manifest);
    }
}
```

## 🎯 Spezial-Exporte

### 1. DSGVO-Auskunft Export
```json
{
  "dataSubjectId": "user-123",
  "exportDate": "2025-07-24T16:00:00Z",
  "personalDataProcessing": [
    {
      "timestamp": "2025-07-20T10:30:00Z",
      "operation": "CUSTOMER_CREATED",
      "dataCategories": ["name", "email", "phone"],
      "purpose": "Contract fulfillment",
      "legalBasis": "Contract",
      "retention": "10 years after contract end"
    }
  ],
  "accessLog": [
    {
      "timestamp": "2025-07-22T14:00:00Z",
      "accessedBy": "sales-rep-42",
      "dataAccessed": ["contact_details", "order_history"],
      "purpose": "Customer service"
    }
  ],
  "dataSharingLog": [],
  "consentHistory": []
}
```

### 2. API Integration Export (für Xentral)
```xml
<?xml version="1.0" encoding="UTF-8"?>
<AuditExport xmlns="https://freshplan.de/audit/v1">
  <Metadata>
    <ExportId>550e8400-e29b-41d4-a716-446655440000</ExportId>
    <Timestamp>2025-07-24T16:00:00Z</Timestamp>
    <System>FreshPlan CRM</System>
    <Version>2.0.0</Version>
  </Metadata>
  <Events>
    <Event id="evt-001">
      <Type>XENTRAL_SYNC_SUCCESS</Type>
      <Timestamp>2025-07-24T14:30:00Z</Timestamp>
      <Entity type="contract" id="con-123"/>
      <ApiCall>
        <Endpoint>POST /api/contracts</Endpoint>
        <RequestId>req-456</RequestId>
        <Duration>234ms</Duration>
      </ApiCall>
      <Result>SUCCESS</Result>
    </Event>
  </Events>
</AuditExport>
```

### 3. Forensic Analysis Export
```json
{
  "analysisId": "forensic-2025-07-24",
  "suspiciousPatterns": [
    {
      "pattern": "BULK_DELETE",
      "occurrences": 3,
      "users": ["user-99"],
      "timeframe": "2025-07-24T02:00:00Z - 02:15:00Z",
      "severity": "HIGH"
    }
  ],
  "anomalies": [
    {
      "type": "UNUSUAL_ACCESS_TIME",
      "user": "user-42",
      "accessTime": "2025-07-24T03:30:00Z",
      "normalPattern": "09:00-18:00",
      "deviation": "9.5 hours"
    }
  ],
  "integrityIssues": [],
  "recommendations": [
    "Review bulk operations by user-99",
    "Verify after-hours access by user-42"
  ]
}
```

## 📋 Export-Validierung

### Checksums & Hashes
```
audit_export_2025-07-24.csv: SHA256=a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3
audit_export_2025-07-24.pdf: SHA256=b3a8e0e47a1c1e2f8b9c0d1e2f3a4b5c6d7e8f9a0b1c2d3e4f5a6b7c8d9e0f1a2
manifest.json: SHA256=c890d4e5f6a7b8c9d0e1f2a3b4c5d6e7f8a9b0c1d2e3f4a5b6c7d8e9f0a1b2c3
```

### Validierungs-Script
```bash
#!/bin/bash
# validate_export.sh

EXPORT_FILE=$1
CHECKSUM_FILE="${EXPORT_FILE}.sha256"

# Verify checksum
echo "Validating export integrity..."
sha256sum -c "$CHECKSUM_FILE"

# Verify signature
echo "Verifying digital signature..."
gpg --verify "${EXPORT_FILE}.sig" "$EXPORT_FILE"

# Check completeness
echo "Checking export completeness..."
python3 validate_audit_export.py "$EXPORT_FILE"

echo "Export validation complete!"
```