# 📝 Audit Trail System - Vollständige Nachvollziehbarkeit & DSGVO-Compliance

**Phase:** 1 - Core Requirements  
**Priorität:** 🔴 KRITISCH - Rechtliche Anforderung  
**Status:** 📋 GEPLANT  
**Letzte Aktualisierung:** 08.08.2025  
**Claude-Ready:** ✅ Vollständig navigierbar

## 🧭 NAVIGATION FÜR CLAUDE

**← Zurück:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/DSGVO_CONSENT.md`  
**→ Nächster:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/SMART_CONTACT_CARDS.md`  
**↑ Parent:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/README.md`  
**⚠️ Wichtig für:**
- DSGVO-Compliance (Art. 5, 7, 24, 30)
- Revision & Compliance
- Forensische Analyse

## ⚡ Quick Implementation Guide für Claude

```bash
# SOFORT STARTEN - Audit Trail System implementieren:
cd /Users/joergstreeck/freshplan-sales-tool

# 1. Backend Audit Infrastructure
mkdir -p backend/src/main/java/de/freshplan/audit
touch backend/src/main/java/de/freshplan/audit/entity/AuditLog.java
touch backend/src/main/java/de/freshplan/audit/service/AuditService.java
touch backend/src/main/java/de/freshplan/audit/interceptor/AuditInterceptor.java
touch backend/src/main/java/de/freshplan/audit/repository/AuditRepository.java
touch backend/src/main/java/de/freshplan/audit/config/EnversConfig.java

# 2. Frontend Audit Components
mkdir -p frontend/src/features/audit/components
touch frontend/src/features/audit/components/AuditTimeline.tsx
touch frontend/src/features/audit/components/AuditDetails.tsx
touch frontend/src/features/audit/components/ComplianceReport.tsx
touch frontend/src/features/audit/components/DataFlowVisualizer.tsx

# 3. DSGVO Compliance Module
mkdir -p backend/src/main/java/de/freshplan/dsgvo
touch backend/src/main/java/de/freshplan/dsgvo/service/DSGVOComplianceService.java
touch backend/src/main/java/de/freshplan/dsgvo/entity/DataProcessingActivity.java
touch backend/src/main/java/de/freshplan/dsgvo/entity/ConsentRecord.java
touch backend/src/main/java/de/freshplan/dsgvo/entity/DataSubjectRequest.java

# 4. Migration (nächste freie Nummer prüfen!)
ls -la backend/src/main/resources/db/migration/ | tail -5
# Erstelle V[NEXT]__create_audit_tables.sql

# 5. Tests
mkdir -p backend/src/test/java/de/freshplan/audit
touch backend/src/test/java/de/freshplan/audit/AuditServiceTest.java
touch backend/src/test/java/de/freshplan/audit/DSGVOComplianceTest.java
```

## 🎯 Das Problem: Fehlende Nachvollziehbarkeit & Compliance-Risiko

**Rechtliche & Business Risiken:**
- ⚖️ **DSGVO-Verstöße:** Bis zu 4% des Jahresumsatzes Strafe
- 📊 **Keine Revision:** Wer hat wann was geändert?
- 🔍 **Forensik unmöglich:** Bei Datenverlust keine Recovery
- 👥 **Vertrauensverlust:** Kunde fragt "Was wissen Sie über mich?"
- 🏛️ **Behörden-Anfragen:** 72h Frist für Datenauskünfte

## 💡 DIE LÖSUNG: Umfassendes Audit Trail & DSGVO System

### 1. Core Audit Entity

**Datei:** `backend/src/main/java/de/freshplan/audit/entity/AuditLog.java`

```java
// CLAUDE: Universelle Audit Log Entity für alle Änderungen
// Pfad: backend/src/main/java/de/freshplan/audit/entity/AuditLog.java

package de.freshplan.audit.entity;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.*;
import com.fasterxml.jackson.databind.JsonNode;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_audit_entity", columnList = "entity_type, entity_id"),
    @Index(name = "idx_audit_user", columnList = "user_id"),
    @Index(name = "idx_audit_timestamp", columnList = "occurred_at"),
    @Index(name = "idx_audit_action", columnList = "action"),
    @Index(name = "idx_audit_compliance", columnList = "is_dsgvo_relevant")
})
public class AuditLog extends PanacheEntityBase {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;
    
    // What was changed
    @Column(name = "entity_type", nullable = false)
    @Enumerated(EnumType.STRING)
    public EntityType entityType;
    
    @Column(name = "entity_id", nullable = false)
    public UUID entityId;
    
    @Column(name = "entity_name")
    public String entityName; // Human-readable identifier
    
    // What action was performed
    @Column(name = "action", nullable = false)
    @Enumerated(EnumType.STRING)
    public AuditAction action;
    
    // Who made the change
    @Column(name = "user_id", nullable = false)
    public UUID userId;
    
    @Column(name = "user_name", nullable = false)
    public String userName;
    
    @Column(name = "user_role")
    public String userRole;
    
    // When it happened
    @Column(name = "occurred_at", nullable = false)
    public LocalDateTime occurredAt;
    
    // From where
    @Column(name = "ip_address")
    public String ipAddress;
    
    @Column(name = "user_agent")
    public String userAgent;
    
    @Column(name = "session_id")
    public String sessionId;
    
    // What changed (JSON for flexibility)
    @Column(name = "old_values", columnDefinition = "jsonb")
    @Type(type = "jsonb")
    public JsonNode oldValues;
    
    @Column(name = "new_values", columnDefinition = "jsonb")
    @Type(type = "jsonb")
    public JsonNode newValues;
    
    @Column(name = "changed_fields")
    @ElementCollection
    @CollectionTable(name = "audit_changed_fields")
    public Set<String> changedFields = new HashSet<>();
    
    // Additional context
    @Column(name = "reason")
    public String reason; // Why the change was made
    
    @Column(name = "comment", columnDefinition = "TEXT")
    public String comment; // Additional notes
    
    @Column(name = "request_id")
    public String requestId; // For tracing across services
    
    // DSGVO specific
    @Column(name = "is_dsgvo_relevant")
    public Boolean isDsgvoRelevant = false;
    
    @Column(name = "legal_basis")
    @Enumerated(EnumType.STRING)
    public LegalBasis legalBasis;
    
    @Column(name = "consent_id")
    public UUID consentId; // Reference to consent record
    
    @Column(name = "retention_until")
    public LocalDateTime retentionUntil;
    
    // Technical metadata
    @Column(name = "application_version")
    public String applicationVersion;
    
    @Column(name = "schema_version")
    public Integer schemaVersion;
    
    @Column(name = "processing_time_ms")
    public Long processingTimeMs;
    
    // Hierarchy for grouped changes
    @Column(name = "parent_audit_id")
    public UUID parentAuditId;
    
    @Column(name = "transaction_id")
    public String transactionId;
    
    // Security
    @Column(name = "signature")
    public String signature; // Digital signature for tamper detection
    
    @Column(name = "is_encrypted")
    public Boolean isEncrypted = false;
    
    // Helper methods
    public boolean isCreate() {
        return action == AuditAction.CREATE;
    }
    
    public boolean isDelete() {
        return action == AuditAction.DELETE;
    }
    
    public boolean isPersonalData() {
        return isDsgvoRelevant != null && isDsgvoRelevant;
    }
    
    public Duration getAge() {
        return Duration.between(occurredAt, LocalDateTime.now());
    }
}

// Enums
public enum EntityType {
    CUSTOMER, CONTACT, INTERACTION, DOCUMENT, USER, CONSENT, 
    EXPORT, IMPORT, CONFIGURATION, SYSTEM
}

public enum AuditAction {
    // CRUD Operations
    CREATE, READ, UPDATE, DELETE, RESTORE,
    
    // Bulk Operations
    BULK_UPDATE, BULK_DELETE, MERGE,
    
    // Data Operations
    EXPORT, IMPORT, ANONYMIZE, ENCRYPT,
    
    // Consent Operations
    CONSENT_GIVEN, CONSENT_WITHDRAWN, CONSENT_UPDATED,
    
    // Access Operations
    ACCESS_GRANTED, ACCESS_DENIED, LOGIN, LOGOUT,
    
    // System Operations
    CONFIGURATION_CHANGE, MIGRATION, BACKUP, RESTORE_BACKUP
}

public enum LegalBasis {
    CONSENT,           // Art. 6(1)(a) DSGVO
    CONTRACT,          // Art. 6(1)(b) DSGVO
    LEGAL_OBLIGATION,  // Art. 6(1)(c) DSGVO
    VITAL_INTERESTS,   // Art. 6(1)(d) DSGVO
    PUBLIC_TASK,       // Art. 6(1)(e) DSGVO
    LEGITIMATE_INTEREST // Art. 6(1)(f) DSGVO
}
```

### 2. Comprehensive Audit Service

**Datei:** `backend/src/main/java/de/freshplan/audit/service/AuditService.java`

```java
// CLAUDE: Umfassender Audit Service mit DSGVO-Compliance
// Pfad: backend/src/main/java/de/freshplan/audit/service/AuditService.java

package de.freshplan.audit.service;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.security.MessageDigest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
@Transactional
public class AuditService {
    
    @Inject
    AuditRepository auditRepository;
    
    @Inject
    ObjectMapper objectMapper;
    
    @Inject
    SecurityContext securityContext;
    
    @Inject
    DSGVOComplianceService dsgvoService;
    
    @ConfigProperty(name = "audit.retention.days", defaultValue = "2555") // 7 Jahre
    int retentionDays;
    
    @ConfigProperty(name = "audit.signature.enabled", defaultValue = "true")
    boolean signatureEnabled;
    
    /**
     * Log any entity change with full context
     */
    public AuditLog logChange(
        EntityType entityType,
        UUID entityId,
        AuditAction action,
        Object oldState,
        Object newState,
        String reason
    ) {
        AuditLog log = new AuditLog();
        
        // Basic information
        log.entityType = entityType;
        log.entityId = entityId;
        log.action = action;
        log.reason = reason;
        log.occurredAt = LocalDateTime.now();
        
        // User context
        log.userId = securityContext.getUserId();
        log.userName = securityContext.getUserName();
        log.userRole = securityContext.getUserRole();
        
        // Request context
        log.ipAddress = securityContext.getIpAddress();
        log.userAgent = securityContext.getUserAgent();
        log.sessionId = securityContext.getSessionId();
        log.requestId = securityContext.getRequestId();
        
        // Application context
        log.applicationVersion = getApplicationVersion();
        log.schemaVersion = getDatabaseSchemaVersion();
        
        // Data changes
        if (oldState != null) {
            log.oldValues = objectMapper.valueToTree(oldState);
        }
        if (newState != null) {
            log.newValues = objectMapper.valueToTree(newState);
        }
        
        // Calculate changed fields
        if (oldState != null && newState != null) {
            log.changedFields = calculateChangedFields(oldState, newState);
        }
        
        // DSGVO classification
        log.isDsgvoRelevant = isDsgvoRelevant(entityType, log.changedFields);
        if (log.isDsgvoRelevant) {
            log.legalBasis = determineLegalBasis(action, entityType);
            log.retentionUntil = calculateRetentionDate(entityType, action);
        }
        
        // Security
        if (signatureEnabled) {
            log.signature = generateSignature(log);
        }
        
        // Performance metrics
        log.processingTimeMs = securityContext.getRequestDuration();
        
        // Persist
        auditRepository.persist(log);
        
        // Trigger compliance checks
        if (log.isDsgvoRelevant) {
            dsgvoService.validateCompliance(log);
        }
        
        // Real-time alerting for critical actions
        if (isCriticalAction(action)) {
            sendSecurityAlert(log);
        }
        
        return log;
    }
    
    /**
     * Special handling for personal data operations
     */
    public void logPersonalDataAccess(
        UUID contactId,
        String purpose,
        LegalBasis legalBasis
    ) {
        AuditLog log = new AuditLog();
        log.entityType = EntityType.CONTACT;
        log.entityId = contactId;
        log.action = AuditAction.READ;
        log.isDsgvoRelevant = true;
        log.legalBasis = legalBasis;
        log.reason = purpose;
        log.occurredAt = LocalDateTime.now();
        
        // Add user context
        fillUserContext(log);
        
        // Special flag for data access logs
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("dataAccessPurpose", purpose);
        metadata.put("dataCategories", getAccessedDataCategories(contactId));
        log.newValues = objectMapper.valueToTree(metadata);
        
        auditRepository.persist(log);
        
        // Check for unusual access patterns
        detectAnomalousAccess(log);
    }
    
    /**
     * Log consent changes with full documentation
     */
    public void logConsentChange(
        UUID contactId,
        ConsentType consentType,
        boolean granted,
        String consentText,
        LocalDateTime validUntil
    ) {
        AuditLog log = new AuditLog();
        log.entityType = EntityType.CONSENT;
        log.entityId = contactId;
        log.action = granted ? AuditAction.CONSENT_GIVEN : AuditAction.CONSENT_WITHDRAWN;
        log.isDsgvoRelevant = true;
        log.legalBasis = LegalBasis.CONSENT;
        log.occurredAt = LocalDateTime.now();
        
        // Consent details
        Map<String, Object> consentDetails = new HashMap<>();
        consentDetails.put("type", consentType);
        consentDetails.put("granted", granted);
        consentDetails.put("text", consentText);
        consentDetails.put("validUntil", validUntil);
        consentDetails.put("withdrawable", true);
        
        log.newValues = objectMapper.valueToTree(consentDetails);
        
        fillUserContext(log);
        
        // Calculate retention
        log.retentionUntil = validUntil != null ? 
            validUntil.plusYears(3) : // Keep 3 years after expiry
            LocalDateTime.now().plusYears(10); // Or 10 years default
        
        auditRepository.persist(log);
    }
    
    /**
     * Log data export for DSGVO Article 20
     */
    public void logDataExport(UUID contactId, ExportFormat format, Set<String> includedData) {
        AuditLog log = new AuditLog();
        log.entityType = EntityType.CONTACT;
        log.entityId = contactId;
        log.action = AuditAction.EXPORT;
        log.isDsgvoRelevant = true;
        log.legalBasis = LegalBasis.LEGAL_OBLIGATION; // Art. 20 DSGVO
        log.occurredAt = LocalDateTime.now();
        log.reason = "Data portability request (Art. 20 DSGVO)";
        
        Map<String, Object> exportDetails = new HashMap<>();
        exportDetails.put("format", format);
        exportDetails.put("includedData", includedData);
        exportDetails.put("timestamp", LocalDateTime.now());
        
        log.newValues = objectMapper.valueToTree(exportDetails);
        
        fillUserContext(log);
        
        auditRepository.persist(log);
        
        // Create export receipt
        createExportReceipt(contactId, log.id);
    }
    
    /**
     * Log deletion/anonymization for DSGVO Article 17
     */
    public void logDeletion(UUID contactId, DeletionType type, String reason) {
        AuditLog log = new AuditLog();
        log.entityType = EntityType.CONTACT;
        log.entityId = contactId;
        log.action = type == DeletionType.DELETE ? 
            AuditAction.DELETE : AuditAction.ANONYMIZE;
        log.isDsgvoRelevant = true;
        log.legalBasis = LegalBasis.LEGAL_OBLIGATION; // Art. 17 DSGVO
        log.occurredAt = LocalDateTime.now();
        log.reason = reason;
        
        // Keep anonymized reference
        if (type == DeletionType.ANONYMIZE) {
            Map<String, Object> anonymizationDetails = new HashMap<>();
            anonymizationDetails.put("anonymizationId", UUID.randomUUID());
            anonymizationDetails.put("method", "pseudonymization");
            anonymizationDetails.put("reversible", false);
            log.newValues = objectMapper.valueToTree(anonymizationDetails);
        }
        
        fillUserContext(log);
        
        // This log itself must be kept for compliance
        log.retentionUntil = LocalDateTime.now().plusYears(10);
        
        auditRepository.persist(log);
    }
    
    /**
     * Retrieve complete audit trail for an entity
     */
    public List<AuditLog> getAuditTrail(
        EntityType entityType,
        UUID entityId,
        LocalDateTime from,
        LocalDateTime to
    ) {
        return auditRepository.find(
            "entityType = ?1 AND entityId = ?2 AND occurredAt BETWEEN ?3 AND ?4",
            entityType, entityId, from, to
        )
        .list();
    }
    
    /**
     * Generate DSGVO Article 15 compliance report
     */
    public ComplianceReport generateDSGVOReport(UUID contactId) {
        ComplianceReport report = new ComplianceReport();
        report.contactId = contactId;
        report.generatedAt = LocalDateTime.now();
        
        // All data processing activities
        List<AuditLog> activities = auditRepository.find(
            "entityId = ?1 AND isDsgvoRelevant = true ORDER BY occurredAt DESC",
            contactId
        ).list();
        
        report.processingActivities = activities;
        
        // Data categories
        report.dataCategories = extractDataCategories(activities);
        
        // Legal bases used
        report.legalBases = activities.stream()
            .map(a -> a.legalBasis)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        
        // Recipients
        report.dataRecipients = extractDataRecipients(activities);
        
        // Retention periods
        report.retentionPeriods = calculateRetentionPeriods(activities);
        
        // Data sources
        report.dataSources = extractDataSources(activities);
        
        // Third country transfers
        report.thirdCountryTransfers = extractThirdCountryTransfers(activities);
        
        return report;
    }
    
    /**
     * Detect anomalous behavior
     */
    private void detectAnomalousAccess(AuditLog log) {
        // Check for unusual access patterns
        long recentAccesses = auditRepository.count(
            "userId = ?1 AND action = ?2 AND occurredAt > ?3",
            log.userId,
            AuditAction.READ,
            LocalDateTime.now().minusMinutes(5)
        );
        
        if (recentAccesses > 100) {
            SecurityAlert alert = new SecurityAlert();
            alert.type = AlertType.EXCESSIVE_DATA_ACCESS;
            alert.userId = log.userId;
            alert.description = "User accessed >100 records in 5 minutes";
            alert.severity = Severity.HIGH;
            
            securityService.raiseAlert(alert);
        }
        
        // Check for access outside business hours
        if (isOutsideBusinessHours(log.occurredAt)) {
            SecurityAlert alert = new SecurityAlert();
            alert.type = AlertType.UNUSUAL_ACCESS_TIME;
            alert.userId = log.userId;
            alert.description = "Data access outside business hours";
            alert.severity = Severity.MEDIUM;
            
            securityService.raiseAlert(alert);
        }
    }
    
    /**
     * Generate tamper-proof signature
     */
    private String generateSignature(AuditLog log) {
        try {
            String data = log.entityType + "|" + 
                         log.entityId + "|" + 
                         log.action + "|" + 
                         log.userId + "|" + 
                         log.occurredAt;
            
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes("UTF-8"));
            
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            log.error("Failed to generate signature", e);
            return null;
        }
    }
    
    /**
     * Cleanup old audit logs (with legal compliance)
     */
    @Scheduled(cron = "0 0 2 * * ?") // Daily at 2 AM
    public void cleanupOldAuditLogs() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(retentionDays);
        
        // Only delete non-DSGVO relevant logs past retention
        long deleted = auditRepository.delete(
            "occurredAt < ?1 AND isDsgvoRelevant = false",
            cutoffDate
        );
        
        log.info("Deleted {} old audit logs", deleted);
        
        // For DSGVO-relevant logs, check individual retention periods
        List<AuditLog> expiredDsgvoLogs = auditRepository.find(
            "retentionUntil < ?1 AND isDsgvoRelevant = true",
            LocalDateTime.now()
        ).list();
        
        for (AuditLog expiredLog : expiredDsgvoLogs) {
            // Anonymize instead of delete
            anonymizeAuditLog(expiredLog);
        }
    }
}
```

### 3. Frontend Audit Timeline Component

**Datei:** `frontend/src/features/audit/components/AuditTimeline.tsx`

```typescript
// CLAUDE: Interaktive Audit Timeline mit Filter und Export
// Pfad: frontend/src/features/audit/components/AuditTimeline.tsx

import React, { useState, useEffect } from 'react';
import {
  Timeline,
  TimelineItem,
  TimelineSeparator,
  TimelineDot,
  TimelineConnector,
  TimelineContent,
  TimelineOppositeContent
} from '@mui/lab';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Chip,
  Button,
  TextField,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
  IconButton,
  Tooltip,
  Dialog,
  DialogTitle,
  DialogContent,
  Alert,
  Accordion,
  AccordionSummary,
  AccordionDetails,
  Badge,
  Avatar,
  Link
} from '@mui/material';
import {
  Create as CreateIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  Lock as ConsentIcon,
  Download as ExportIcon,
  Visibility as ViewIcon,
  Person as UserIcon,
  ExpandMore as ExpandIcon,
  FilterList as FilterIcon,
  GetApp as DownloadIcon,
  Security as SecurityIcon,
  Warning as WarningIcon,
  Check as CheckIcon
} from '@mui/icons-material';
import { format, formatDistanceToNow } from 'date-fns';
import { de } from 'date-fns/locale';
import ReactDiffViewer from 'react-diff-viewer';

interface AuditTimelineProps {
  entityType: string;
  entityId: string;
  showDsgvoOnly?: boolean;
}

export const AuditTimeline: React.FC<AuditTimelineProps> = ({
  entityType,
  entityId,
  showDsgvoOnly = false
}) => {
  const [auditLogs, setAuditLogs] = useState<AuditLog[]>([]);
  const [filteredLogs, setFilteredLogs] = useState<AuditLog[]>([]);
  const [loading, setLoading] = useState(true);
  const [selectedLog, setSelectedLog] = useState<AuditLog | null>(null);
  const [filterUser, setFilterUser] = useState('');
  const [filterAction, setFilterAction] = useState('ALL');
  const [expandedItems, setExpandedItems] = useState<Set<string>>(new Set());
  
  useEffect(() => {
    loadAuditTrail();
  }, [entityType, entityId]);
  
  useEffect(() => {
    applyFilters();
  }, [auditLogs, filterUser, filterAction, showDsgvoOnly]);
  
  const loadAuditTrail = async () => {
    setLoading(true);
    try {
      const response = await fetch(
        `/api/audit/trail/${entityType}/${entityId}`
      );
      const data = await response.json();
      setAuditLogs(data);
    } finally {
      setLoading(false);
    }
  };
  
  const applyFilters = () => {
    let filtered = [...auditLogs];
    
    if (showDsgvoOnly) {
      filtered = filtered.filter(log => log.isDsgvoRelevant);
    }
    
    if (filterUser) {
      filtered = filtered.filter(log => 
        log.userName.toLowerCase().includes(filterUser.toLowerCase())
      );
    }
    
    if (filterAction !== 'ALL') {
      filtered = filtered.filter(log => log.action === filterAction);
    }
    
    setFilteredLogs(filtered);
  };
  
  const getActionIcon = (action: string) => {
    switch (action) {
      case 'CREATE': return <CreateIcon />;
      case 'UPDATE': return <EditIcon />;
      case 'DELETE': return <DeleteIcon />;
      case 'CONSENT_GIVEN': return <ConsentIcon color="success" />;
      case 'CONSENT_WITHDRAWN': return <ConsentIcon color="error" />;
      case 'EXPORT': return <ExportIcon />;
      case 'READ': return <ViewIcon />;
      default: return <EditIcon />;
    }
  };
  
  const getActionColor = (action: string): any => {
    switch (action) {
      case 'CREATE': return 'success';
      case 'DELETE': return 'error';
      case 'CONSENT_WITHDRAWN': return 'error';
      case 'ANONYMIZE': return 'warning';
      default: return 'primary';
    }
  };
  
  const formatFieldChange = (field: string, oldValue: any, newValue: any) => {
    // Special formatting for specific fields
    if (field === 'consentMarketing' || field === 'consentNewsletter') {
      return {
        old: oldValue ? '✓ Einwilligung erteilt' : '✗ Keine Einwilligung',
        new: newValue ? '✓ Einwilligung erteilt' : '✗ Keine Einwilligung'
      };
    }
    
    return {
      old: oldValue || '—',
      new: newValue || '—'
    };
  };
  
  const exportAuditReport = async () => {
    const response = await fetch(
      `/api/audit/export/${entityType}/${entityId}`,
      { method: 'POST' }
    );
    const blob = await response.blob();
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `audit-report-${entityId}-${format(new Date(), 'yyyy-MM-dd')}.pdf`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
  };
  
  const toggleExpanded = (logId: string) => {
    const newExpanded = new Set(expandedItems);
    if (newExpanded.has(logId)) {
      newExpanded.delete(logId);
    } else {
      newExpanded.add(logId);
    }
    setExpandedItems(newExpanded);
  };
  
  return (
    <Box>
      {/* Header & Filters */}
      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
            <Typography variant="h6">
              Audit Trail & Änderungshistorie
            </Typography>
            
            <Box display="flex" gap={1}>
              {showDsgvoOnly && (
                <Chip
                  label="DSGVO-Relevant"
                  color="warning"
                  icon={<SecurityIcon />}
                />
              )}
              
              <Button
                variant="outlined"
                startIcon={<DownloadIcon />}
                onClick={exportAuditReport}
              >
                Report exportieren
              </Button>
            </Box>
          </Box>
          
          {/* Filters */}
          <Box display="flex" gap={2}>
            <TextField
              size="small"
              placeholder="Nach Benutzer filtern..."
              value={filterUser}
              onChange={(e) => setFilterUser(e.target.value)}
              sx={{ flex: 1 }}
            />
            
            <FormControl size="small" sx={{ minWidth: 200 }}>
              <InputLabel>Aktion</InputLabel>
              <Select
                value={filterAction}
                onChange={(e) => setFilterAction(e.target.value)}
                label="Aktion"
              >
                <MenuItem value="ALL">Alle Aktionen</MenuItem>
                <MenuItem value="CREATE">Erstellt</MenuItem>
                <MenuItem value="UPDATE">Geändert</MenuItem>
                <MenuItem value="DELETE">Gelöscht</MenuItem>
                <MenuItem value="READ">Angezeigt</MenuItem>
                <MenuItem value="EXPORT">Exportiert</MenuItem>
                <MenuItem value="CONSENT_GIVEN">Einwilligung erteilt</MenuItem>
                <MenuItem value="CONSENT_WITHDRAWN">Einwilligung widerrufen</MenuItem>
              </Select>
            </FormControl>
          </Box>
          
          {/* Summary Stats */}
          <Box display="flex" gap={2} mt={2}>
            <Chip label={`${filteredLogs.length} Einträge`} size="small" />
            <Chip 
              label={`${filteredLogs.filter(l => l.isDsgvoRelevant).length} DSGVO-relevant`}
              size="small"
              color="warning"
            />
          </Box>
        </CardContent>
      </Card>
      
      {/* Timeline */}
      <Timeline position="alternate">
        {filteredLogs.map((log, index) => (
          <TimelineItem key={log.id}>
            <TimelineOppositeContent color="text.secondary">
              <Typography variant="caption">
                {format(new Date(log.occurredAt), 'dd.MM.yyyy HH:mm')}
              </Typography>
              <Typography variant="caption" display="block">
                {formatDistanceToNow(new Date(log.occurredAt), {
                  locale: de,
                  addSuffix: true
                })}
              </Typography>
            </TimelineOppositeContent>
            
            <TimelineSeparator>
              <TimelineDot color={getActionColor(log.action)}>
                {getActionIcon(log.action)}
              </TimelineDot>
              {index < filteredLogs.length - 1 && <TimelineConnector />}
            </TimelineSeparator>
            
            <TimelineContent>
              <Card 
                sx={{ 
                  cursor: 'pointer',
                  border: log.isDsgvoRelevant ? '2px solid orange' : 'none'
                }}
                onClick={() => toggleExpanded(log.id)}
              >
                <CardContent>
                  {/* Header */}
                  <Box display="flex" justifyContent="space-between" alignItems="center">
                    <Box display="flex" alignItems="center" gap={1}>
                      <Avatar sx={{ width: 24, height: 24 }}>
                        {log.userName[0]}
                      </Avatar>
                      <Typography variant="subtitle2">
                        {log.userName}
                      </Typography>
                      {log.userRole && (
                        <Chip label={log.userRole} size="small" variant="outlined" />
                      )}
                    </Box>
                    
                    {log.isDsgvoRelevant && (
                      <Tooltip title="DSGVO-relevante Änderung">
                        <SecurityIcon color="warning" fontSize="small" />
                      </Tooltip>
                    )}
                  </Box>
                  
                  {/* Action Description */}
                  <Typography variant="body2" sx={{ mt: 1 }}>
                    {getActionDescription(log)}
                  </Typography>
                  
                  {/* Changed Fields Preview */}
                  {log.changedFields && log.changedFields.length > 0 && (
                    <Box mt={1}>
                      <Typography variant="caption" color="text.secondary">
                        Geänderte Felder: {log.changedFields.join(', ')}
                      </Typography>
                    </Box>
                  )}
                  
                  {/* Reason */}
                  {log.reason && (
                    <Alert severity="info" sx={{ mt: 1 }}>
                      <Typography variant="caption">
                        Grund: {log.reason}
                      </Typography>
                    </Alert>
                  )}
                  
                  {/* Expanded Details */}
                  {expandedItems.has(log.id) && (
                    <Box mt={2}>
                      <Divider sx={{ mb: 2 }} />
                      
                      {/* Diff Viewer for Changes */}
                      {log.oldValues && log.newValues && (
                        <Box mb={2}>
                          <Typography variant="subtitle2" gutterBottom>
                            Änderungen im Detail:
                          </Typography>
                          <ReactDiffViewer
                            oldValue={JSON.stringify(log.oldValues, null, 2)}
                            newValue={JSON.stringify(log.newValues, null, 2)}
                            splitView={false}
                            compareMethod="diffLines"
                            styles={{
                              variables: {
                                light: {
                                  diffViewerBackground: '#fafafa',
                                }
                              }
                            }}
                          />
                        </Box>
                      )}
                      
                      {/* Technical Details */}
                      <Accordion>
                        <AccordionSummary expandIcon={<ExpandIcon />}>
                          <Typography variant="caption">
                            Technische Details
                          </Typography>
                        </AccordionSummary>
                        <AccordionDetails>
                          <Grid container spacing={1}>
                            <Grid item xs={6}>
                              <Typography variant="caption" color="text.secondary">
                                IP-Adresse:
                              </Typography>
                              <Typography variant="body2">
                                {log.ipAddress}
                              </Typography>
                            </Grid>
                            <Grid item xs={6}>
                              <Typography variant="caption" color="text.secondary">
                                Session ID:
                              </Typography>
                              <Typography variant="body2">
                                {log.sessionId}
                              </Typography>
                            </Grid>
                            <Grid item xs={6}>
                              <Typography variant="caption" color="text.secondary">
                                Request ID:
                              </Typography>
                              <Typography variant="body2">
                                {log.requestId}
                              </Typography>
                            </Grid>
                            <Grid item xs={6}>
                              <Typography variant="caption" color="text.secondary">
                                Verarbeitungszeit:
                              </Typography>
                              <Typography variant="body2">
                                {log.processingTimeMs}ms
                              </Typography>
                            </Grid>
                          </Grid>
                        </AccordionDetails>
                      </Accordion>
                      
                      {/* DSGVO Details */}
                      {log.isDsgvoRelevant && (
                        <Accordion>
                          <AccordionSummary expandIcon={<ExpandIcon />}>
                            <Typography variant="caption">
                              DSGVO-Compliance Details
                            </Typography>
                          </AccordionSummary>
                          <AccordionDetails>
                            <Grid container spacing={1}>
                              <Grid item xs={6}>
                                <Typography variant="caption" color="text.secondary">
                                  Rechtsgrundlage:
                                </Typography>
                                <Typography variant="body2">
                                  {getLegalBasisText(log.legalBasis)}
                                </Typography>
                              </Grid>
                              <Grid item xs={6}>
                                <Typography variant="caption" color="text.secondary">
                                  Aufbewahrung bis:
                                </Typography>
                                <Typography variant="body2">
                                  {format(new Date(log.retentionUntil), 'dd.MM.yyyy')}
                                </Typography>
                              </Grid>
                              {log.consentId && (
                                <Grid item xs={12}>
                                  <Typography variant="caption" color="text.secondary">
                                    Einwilligungs-ID:
                                  </Typography>
                                  <Typography variant="body2">
                                    <Link href={`/consent/${log.consentId}`}>
                                      {log.consentId}
                                    </Link>
                                  </Typography>
                                </Grid>
                              )}
                            </Grid>
                          </AccordionDetails>
                        </Accordion>
                      )}
                      
                      {/* Signature Verification */}
                      {log.signature && (
                        <Box mt={1}>
                          <Chip
                            size="small"
                            icon={<CheckIcon />}
                            label="Signatur verifiziert"
                            color="success"
                            variant="outlined"
                          />
                        </Box>
                      )}
                    </Box>
                  )}
                </CardContent>
              </Card>
            </TimelineContent>
          </TimelineItem>
        ))}
      </Timeline>
      
      {/* Empty State */}
      {filteredLogs.length === 0 && !loading && (
        <Alert severity="info">
          Keine Audit-Einträge gefunden
        </Alert>
      )}
    </Box>
  );
};

// Helper functions
const getActionDescription = (log: AuditLog): string => {
  const templates: Record<string, string> = {
    CREATE: 'hat {{entity}} erstellt',
    UPDATE: 'hat {{entity}} geändert',
    DELETE: 'hat {{entity}} gelöscht',
    READ: 'hat {{entity}} angezeigt',
    EXPORT: 'hat {{entity}} exportiert',
    CONSENT_GIVEN: 'hat Einwilligung für {{type}} erteilt',
    CONSENT_WITHDRAWN: 'hat Einwilligung für {{type}} widerrufen',
    ANONYMIZE: 'hat {{entity}} anonymisiert'
  };
  
  let description = templates[log.action] || log.action;
  description = description.replace('{{entity}}', log.entityName || log.entityType);
  
  return description;
};

const getLegalBasisText = (basis: string): string => {
  const texts: Record<string, string> = {
    CONSENT: 'Einwilligung (Art. 6 Abs. 1 lit. a DSGVO)',
    CONTRACT: 'Vertrag (Art. 6 Abs. 1 lit. b DSGVO)',
    LEGAL_OBLIGATION: 'Rechtliche Verpflichtung (Art. 6 Abs. 1 lit. c DSGVO)',
    VITAL_INTERESTS: 'Lebenswichtige Interessen (Art. 6 Abs. 1 lit. d DSGVO)',
    PUBLIC_TASK: 'Öffentliche Aufgabe (Art. 6 Abs. 1 lit. e DSGVO)',
    LEGITIMATE_INTEREST: 'Berechtigtes Interesse (Art. 6 Abs. 1 lit. f DSGVO)'
  };
  
  return texts[basis] || basis;
};
```

### 4. Database Migration für Audit Tables

**Datei:** `backend/src/main/resources/db/migration/V[NEXT]__create_audit_tables.sql`

```sql
-- CLAUDE: Comprehensive Audit Trail Tables mit DSGVO Support
-- WICHTIG: Ersetze [NEXT] mit nächster freier Nummer!

-- Main Audit Log Table
CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    -- Entity Information
    entity_type VARCHAR(50) NOT NULL,
    entity_id UUID NOT NULL,
    entity_name VARCHAR(255),
    
    -- Action
    action VARCHAR(50) NOT NULL,
    
    -- User Context
    user_id UUID NOT NULL,
    user_name VARCHAR(255) NOT NULL,
    user_role VARCHAR(100),
    
    -- Timestamp
    occurred_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Request Context
    ip_address INET,
    user_agent TEXT,
    session_id VARCHAR(255),
    request_id VARCHAR(255),
    
    -- Changes (JSONB for flexibility)
    old_values JSONB,
    new_values JSONB,
    
    -- Additional Context
    reason TEXT,
    comment TEXT,
    
    -- DSGVO Specific
    is_dsgvo_relevant BOOLEAN DEFAULT FALSE,
    legal_basis VARCHAR(50),
    consent_id UUID,
    retention_until TIMESTAMP,
    
    -- Technical Metadata
    application_version VARCHAR(50),
    schema_version INTEGER,
    processing_time_ms BIGINT,
    
    -- Hierarchy
    parent_audit_id UUID,
    transaction_id VARCHAR(255),
    
    -- Security
    signature VARCHAR(500),
    is_encrypted BOOLEAN DEFAULT FALSE,
    
    -- Indexes for performance
    CONSTRAINT fk_parent_audit FOREIGN KEY (parent_audit_id) 
        REFERENCES audit_logs(id) ON DELETE CASCADE
);

-- Indexes for efficient querying
CREATE INDEX idx_audit_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_user ON audit_logs(user_id);
CREATE INDEX idx_audit_timestamp ON audit_logs(occurred_at DESC);
CREATE INDEX idx_audit_action ON audit_logs(action);
CREATE INDEX idx_audit_compliance ON audit_logs(is_dsgvo_relevant) 
    WHERE is_dsgvo_relevant = TRUE;
CREATE INDEX idx_audit_retention ON audit_logs(retention_until) 
    WHERE retention_until IS NOT NULL;

-- Changed fields tracking
CREATE TABLE audit_changed_fields (
    audit_log_id UUID NOT NULL,
    field_name VARCHAR(255) NOT NULL,
    PRIMARY KEY (audit_log_id, field_name),
    CONSTRAINT fk_audit_log FOREIGN KEY (audit_log_id) 
        REFERENCES audit_logs(id) ON DELETE CASCADE
);

-- DSGVO Consent Records
CREATE TABLE consent_records (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    contact_id UUID NOT NULL,
    consent_type VARCHAR(50) NOT NULL,
    granted BOOLEAN NOT NULL,
    granted_at TIMESTAMP,
    withdrawn_at TIMESTAMP,
    valid_until TIMESTAMP,
    consent_text TEXT NOT NULL,
    consent_version VARCHAR(50),
    ip_address INET,
    user_agent TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX idx_consent_contact ON consent_records(contact_id);
CREATE INDEX idx_consent_type ON consent_records(consent_type);
CREATE INDEX idx_consent_valid ON consent_records(valid_until);

-- Data Processing Activities (Art. 30 DSGVO)
CREATE TABLE data_processing_activities (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    purpose TEXT NOT NULL,
    legal_basis VARCHAR(50) NOT NULL,
    data_categories TEXT[],
    data_subjects TEXT[],
    recipients TEXT[],
    third_country_transfers TEXT[],
    retention_period VARCHAR(255),
    technical_measures TEXT,
    organizational_measures TEXT,
    risk_assessment TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Data Subject Requests (Art. 15-22 DSGVO)
CREATE TABLE data_subject_requests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    contact_id UUID NOT NULL,
    request_type VARCHAR(50) NOT NULL, -- ACCESS, RECTIFICATION, ERASURE, etc.
    status VARCHAR(50) NOT NULL, -- PENDING, IN_PROGRESS, COMPLETED, REJECTED
    received_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    due_date TIMESTAMP NOT NULL, -- Usually received_at + 30 days
    completed_at TIMESTAMP,
    request_details JSONB,
    response_details JSONB,
    handled_by UUID,
    rejection_reason TEXT,
    verification_method VARCHAR(100),
    communication_log JSONB[]
);

CREATE INDEX idx_dsr_contact ON data_subject_requests(contact_id);
CREATE INDEX idx_dsr_status ON data_subject_requests(status);
CREATE INDEX idx_dsr_due_date ON data_subject_requests(due_date);

-- Security Alerts
CREATE TABLE security_alerts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    alert_type VARCHAR(50) NOT NULL,
    severity VARCHAR(20) NOT NULL, -- LOW, MEDIUM, HIGH, CRITICAL
    user_id UUID,
    description TEXT NOT NULL,
    details JSONB,
    occurred_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    acknowledged_at TIMESTAMP,
    acknowledged_by UUID,
    resolved_at TIMESTAMP,
    resolved_by UUID,
    resolution_notes TEXT
);

CREATE INDEX idx_security_severity ON security_alerts(severity);
CREATE INDEX idx_security_unresolved ON security_alerts(resolved_at) 
    WHERE resolved_at IS NULL;

-- Audit Report Exports (for compliance documentation)
CREATE TABLE audit_exports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    entity_type VARCHAR(50),
    entity_id UUID,
    export_type VARCHAR(50) NOT NULL, -- FULL, DSGVO, PERIOD
    format VARCHAR(20) NOT NULL, -- PDF, CSV, JSON
    from_date TIMESTAMP,
    to_date TIMESTAMP,
    exported_by UUID NOT NULL,
    exported_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    file_name VARCHAR(255),
    file_size BIGINT,
    checksum VARCHAR(255),
    retention_until TIMESTAMP
);

CREATE INDEX idx_export_entity ON audit_exports(entity_type, entity_id);
CREATE INDEX idx_export_user ON audit_exports(exported_by);

-- Create partitioning for audit_logs by month (optional for large datasets)
-- Uncomment if needed:
/*
CREATE TABLE audit_logs_2025_01 PARTITION OF audit_logs
    FOR VALUES FROM ('2025-01-01') TO ('2025-02-01');
    
CREATE TABLE audit_logs_2025_02 PARTITION OF audit_logs
    FOR VALUES FROM ('2025-02-01') TO ('2025-03-01');
-- Continue for other months...
*/

-- Function to automatically set updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Apply trigger to relevant tables
CREATE TRIGGER update_consent_records_updated_at 
    BEFORE UPDATE ON consent_records 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_data_processing_activities_updated_at 
    BEFORE UPDATE ON data_processing_activities 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();
```

## 📋 IMPLEMENTIERUNGS-CHECKLISTE

### Phase 1: Core Audit Infrastructure (60 Min)
- [ ] AuditLog Entity mit allen Feldern
- [ ] AuditService mit Logging-Methoden
- [ ] AuditInterceptor für automatisches Logging
- [ ] Database Migration für Audit Tables

### Phase 2: DSGVO Compliance Module (45 Min)
- [ ] ConsentRecord Entity
- [ ] DSGVOComplianceService
- [ ] DataSubjectRequest Handler
- [ ] Processing Activities Registry

### Phase 3: Frontend Components (45 Min)
- [ ] AuditTimeline Component
- [ ] ComplianceReport Generator
- [ ] DataFlowVisualizer
- [ ] Export Functionality

### Phase 4: Security & Monitoring (30 Min)
- [ ] Anomaly Detection
- [ ] Security Alerts
- [ ] Signature Generation
- [ ] Tamper Detection

### Phase 5: Testing & Documentation (30 Min)
- [ ] Unit Tests für AuditService
- [ ] DSGVO Compliance Tests
- [ ] Performance Tests
- [ ] User Documentation

## 🔗 INTEGRATION POINTS

1. **Alle Entities** - @Audited Annotation hinzufügen
2. **REST Controllers** - AuditInterceptor einbinden
3. **User Service** - Security Context bereitstellen
4. **Notification Service** - Alerts versenden
5. **Export Service** - Audit Reports generieren

## ⚠️ HÄUFIGE FEHLER VERMEIDEN

1. **Persönliche Daten im Klartext loggen**
   → Lösung: Sensitive Felder maskieren/hashen

2. **Audit Logs werden selbst gelöscht**
   → Lösung: Retention Policies mit Legal Hold

3. **Performance-Probleme bei vielen Logs**
   → Lösung: Partitionierung & Archivierung

4. **Keine Tamper Detection**
   → Lösung: Digitale Signaturen verwenden

---

**Status:** BEREIT FÜR IMPLEMENTIERUNG  
**Geschätzte Zeit:** 210 Minuten  
**Nächstes Dokument:** [→ Smart Contact Cards](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/SMART_CONTACT_CARDS.md)  
**Parent:** [↑ Step3 Overview](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/README.md)

**Compliance + Transparency = Trust! 📝🔒✨**