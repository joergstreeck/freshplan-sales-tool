# FC-012: Compliance Requirements & Standards

**Parent:** [/Users/joergstreeck/freshplan-sales-tool/docs/features/2025-07-24_TECH_CONCEPT_FC-012-audit-trail-system.md](/Users/joergstreeck/freshplan-sales-tool/docs/features/2025-07-24_TECH_CONCEPT_FC-012-audit-trail-system.md)  
**Fokus:** Regulatorische Anforderungen und Standards

## 📋 Regulatorische Anforderungen

### 1. DSGVO/GDPR Compliance

**Artikel 30 - Verzeichnis von Verarbeitungstätigkeiten**
```
Anforderung: Dokumentation aller Datenverarbeitungen
Umsetzung: Audit Trail enthält alle relevanten Operationen
```

**Artikel 32 - Sicherheit der Verarbeitung**
```
Anforderung: Integrität und Vertraulichkeit
Umsetzung: Hash-Chain, Verschlüsselung, Access Control
```

**Artikel 33 - Meldung von Datenschutzverletzungen**
```
Anforderung: Nachweis von Verletzungen binnen 72h
Umsetzung: Audit-Export mit Zeitfilter, Anomalie-Detection
```

### 2. GoBD Compliance (Deutschland)

**Grundsätze ordnungsmäßiger Buchführung**
- ✅ **Nachvollziehbarkeit:** Lückenlose Protokollierung
- ✅ **Unveränderbarkeit:** Immutable Audit Entries
- ✅ **Vollständigkeit:** Alle geschäftsrelevanten Vorgänge
- ✅ **Zeitgerechte Erfassung:** Timestamp bei Entstehung
- ✅ **Ordnung:** Strukturierte Ablage und Indexierung
- ✅ **Unveränderbarkeit:** Hash-Chain verhindert Manipulation

### 3. ISO 27001 Requirements

**A.12.4 - Logging and Monitoring**
```yaml
Requirement: Log all access to information assets
Implementation:
  - User access logs
  - Data modification logs
  - Failed access attempts
  - Administrative actions
```

**A.18.1.3 - Protection of Records**
```yaml
Requirement: Protect records from loss, destruction, falsification
Implementation:
  - Backup strategy
  - Hash-chain integrity
  - Access restrictions
  - Retention policies
```

## 🔒 Technische Sicherheitsmaßnahmen

### 1. Unveränderlichkeit (Immutability)

```java
// Datenbank-Level: Keine UPDATE/DELETE Rechte auf audit_trail
REVOKE UPDATE, DELETE ON audit_trail FROM application_user;

// Application-Level: Immutable Entity
@Entity
@Immutable
@Table(name = "audit_trail")
public class AuditEntry {
    // Keine Setter für bestehende Einträge
}

// API-Level: Keine Update/Delete Endpoints
@Path("/api/audit")
public class AuditResource {
    // Nur GET Methoden, keine PUT/DELETE
}
```

### 2. Integritätssicherung

```java
public class AuditIntegrityValidator {
    
    // Tägliche Integritätsprüfung
    @Scheduled(cron = "0 2 * * *") // 2 Uhr nachts
    public void dailyIntegrityCheck() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        
        ValidationResult result = auditHashChain.verifyIntegrity(
            yesterday, 
            yesterday
        );
        
        if (!result.isValid()) {
            // Kritischer Alert
            alertService.sendCritical(
                "Audit integrity violation detected!",
                result.getErrors()
            );
            
            // Compliance Officer benachrichtigen
            emailService.notifyComplianceOfficer(result);
            
            // System in Read-Only Mode versetzen
            systemService.enableReadOnlyMode();
        }
    }
}
```

### 3. Zugriffskontrolle

```java
@RolesAllowed({"admin", "auditor", "compliance_officer"})
public class AuditAccessControl {
    
    @Inject
    SecurityIdentity identity;
    
    public boolean canViewAuditTrail(AuditQuery query) {
        // Admins: Vollzugriff
        if (identity.hasRole("admin")) {
            return true;
        }
        
        // Auditors: Nur lesend, keine persönlichen Daten
        if (identity.hasRole("auditor")) {
            return !query.includesPersonalData();
        }
        
        // Compliance Officers: Vollzugriff für Compliance-Events
        if (identity.hasRole("compliance_officer")) {
            return query.isComplianceRelevant();
        }
        
        return false;
    }
}
```

## 📊 Aufbewahrungsfristen

### Gesetzliche Mindestaufbewahrung

| Datentyp | Aufbewahrungsfrist | Rechtsgrundlage |
|----------|-------------------|-----------------|
| **Geschäftsvorfälle** | 10 Jahre | § 147 AO, § 257 HGB |
| **Handelsbriefe** | 6 Jahre | § 147 AO, § 257 HGB |
| **Preisänderungen** | 10 Jahre | Kartellrecht |
| **Zugriffsprotokolle** | 6 Monate | DSGVO (Nachweis) |
| **Sicherheitsvorfälle** | 3 Jahre | ISO 27001 |

### Implementierung der Retention Policy

```java
@Scheduled(cron = "0 3 1 * *") // Monatlich
public void enforceRetentionPolicy() {
    
    retentionPolicies.forEach(policy -> {
        LocalDate cutoffDate = LocalDate.now()
            .minus(policy.getRetentionPeriod());
            
        if (policy.requiresArchival()) {
            // Archivierung vor Löschung
            var entries = auditRepository.findOlderThan(
                cutoffDate, 
                policy.getEventTypes()
            );
            
            archiveService.archive(entries);
        }
        
        if (policy.allowsDeletion()) {
            // Anonymisierung statt Löschung für DSGVO
            auditRepository.anonymizeOlderThan(
                cutoffDate,
                policy.getEventTypes()
            );
        }
    });
}
```

## 🚨 Kritische Events für Compliance

### Pflicht-Events (MUSS protokolliert werden)

```java
public enum ComplianceCriticalEvents {
    // Vertragsänderungen
    CONTRACT_CREATED,
    CONTRACT_MODIFIED,
    CONTRACT_TERMINATED,
    CONTRACT_RENEWED,
    
    // Preisänderungen
    PRICE_INCREASED,
    PRICE_DECREASED,
    DISCOUNT_GRANTED,
    DISCOUNT_REVOKED,
    
    // Datenschutz
    PERSONAL_DATA_ACCESSED,
    PERSONAL_DATA_EXPORTED,
    PERSONAL_DATA_DELETED,
    CONSENT_GIVEN,
    CONSENT_WITHDRAWN,
    
    // Sicherheit
    LOGIN_SUCCESSFUL,
    LOGIN_FAILED,
    PERMISSION_CHANGED,
    PASSWORD_CHANGED,
    
    // Systemänderungen
    CONFIGURATION_CHANGED,
    INTEGRATION_ACTIVATED,
    INTEGRATION_DEACTIVATED
}
```

### Event Details Requirements

```java
public class ComplianceEventDetails {
    
    // Minimum Required Fields
    @NotNull private Instant timestamp;
    @NotNull private String userId;
    @NotNull private String userName;
    @NotNull private String userRole;
    @NotNull private String ipAddress;
    @NotNull private String action;
    @NotNull private String entityType;
    @NotNull private String entityId;
    
    // Conditional Required Fields
    private String oldValue; // Bei Änderungen
    private String newValue; // Bei Änderungen
    private String reason; // Bei manuellen Aktionen
    private String legalBasis; // Bei Datenverarbeitung
    private String consentId; // Bei consent-basierten Aktionen
    
    // Compliance Metadata
    private boolean gdprRelevant;
    private boolean gobdRelevant;
    private String dataCategory; // personal, financial, operational
    private String riskLevel; // low, medium, high, critical
}
```

## 📤 Export-Anforderungen

### 1. Audit-Report für Wirtschaftsprüfer

```java
public class AuditExportService {
    
    public File exportForAuditor(
        LocalDate from,
        LocalDate to,
        String auditorId
    ) {
        // Log the export request itself
        auditService.logEvent(
            AuditEventType.COMPLIANCE_EXPORT,
            "audit_trail",
            null,
            null,
            Map.of(
                "auditorId", auditorId,
                "period", from + " to " + to,
                "exportFormat", "PDF"
            ),
            "Auditor report requested"
        );
        
        // Generate comprehensive report
        var report = ComplianceReportGenerator.builder()
            .period(from, to)
            .includeExecutiveSummary()
            .includeDetailedTransactions()
            .includeAnomalies()
            .includeIntegrityProof()
            .includeUserActivityMatrix()
            .generatePDF();
            
        return report;
    }
}
```

### 2. DSGVO-Auskunft

```java
public class GdprExportService {
    
    public PersonalDataReport exportPersonalData(String dataSubjectId) {
        // Find all audit entries related to this person
        var entries = auditRepository.findByDataSubject(dataSubjectId);
        
        return PersonalDataReport.builder()
            .dataSubjectId(dataSubjectId)
            .processingActivities(extractActivities(entries))
            .dataCategories(extractCategories(entries))
            .recipients(extractRecipients(entries))
            .retentionPeriods(getRetentionPeriods())
            .exportDate(LocalDateTime.now())
            .build();
    }
}
```

## 🔍 Compliance Monitoring

### Automated Compliance Checks

```java
@ApplicationScoped
public class ComplianceMonitor {
    
    @Scheduled(every = "1h")
    public void runComplianceChecks() {
        var violations = new ArrayList<ComplianceViolation>();
        
        // Check 1: Alle kritischen Events haben Reason
        var eventsWithoutReason = auditRepository
            .findCriticalEventsWithoutReason(lastHour());
        if (!eventsWithoutReason.isEmpty()) {
            violations.add(new ComplianceViolation(
                "Critical events without reason",
                Severity.HIGH,
                eventsWithoutReason
            ));
        }
        
        // Check 2: Keine unauthorisierten Zugriffe
        var unauthorizedAccess = detectUnauthorizedAccess();
        if (!unauthorizedAccess.isEmpty()) {
            violations.add(new ComplianceViolation(
                "Unauthorized access detected",
                Severity.CRITICAL,
                unauthorizedAccess
            ));
        }
        
        // Check 3: Retention Policy Violations
        var retentionViolations = checkRetentionCompliance();
        violations.addAll(retentionViolations);
        
        if (!violations.isEmpty()) {
            complianceOfficer.notify(violations);
        }
    }
}
```

## 📋 Compliance Checkliste

- [ ] Alle geschäftskritischen Operationen werden geloggt
- [ ] Audit-Einträge sind unveränderlich (Immutable)
- [ ] Hash-Chain sichert Integrität
- [ ] Zugriff nur für autorisierte Rollen
- [ ] Aufbewahrungsfristen implementiert
- [ ] Export-Funktionen für Prüfer vorhanden
- [ ] DSGVO-Auskunft automatisiert
- [ ] Anomalie-Erkennung aktiv
- [ ] Regelmäßige Integrity Checks
- [ ] Compliance-Reports verfügbar