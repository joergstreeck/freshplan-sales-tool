# FC-012: Audit Trail & Compliance System - Technisches Konzept

**Feature Code:** FC-012  
**Status:** 🟡 KONZEPT  
**Priorität:** CRITICAL (Compliance-Anforderung)  
**Geschätzter Aufwand:** 3-4 Tage  

## 📋 Übersicht

Ein lückenloses Audit-Trail System für alle geschäftskritischen Operationen im FreshPlan CRM. Jede Statusänderung, jeder Contract Renewal und jede Preisanpassung wird revisionssicher protokolliert.

## 🎯 Compliance-Anforderungen

### Must-Have Features
1. **Lückenlose Protokollierung** aller kritischen Aktionen
2. **Unveränderlichkeit** der Audit-Einträge
3. **Export-Funktionen** für Audits/Zertifizierungen
4. **API-Sync Tracking** mit Xentral
5. **User-Attribution** für jede Aktion
6. **Reason/Comment** Erfassung bei manuellen Aktionen

## 🏗️ Architektur

### Audit Event Types
```typescript
enum AuditEventType {
  // Opportunity Events
  OPPORTUNITY_CREATED = 'OPPORTUNITY_CREATED',
  OPPORTUNITY_STAGE_CHANGED = 'OPPORTUNITY_STAGE_CHANGED',
  OPPORTUNITY_WON = 'OPPORTUNITY_WON',
  OPPORTUNITY_LOST = 'OPPORTUNITY_LOST',
  OPPORTUNITY_REACTIVATED = 'OPPORTUNITY_REACTIVATED',
  
  // Contract Events  
  CONTRACT_CREATED = 'CONTRACT_CREATED',
  CONTRACT_RENEWED = 'CONTRACT_RENEWED',
  CONTRACT_EXPIRED = 'CONTRACT_EXPIRED',
  CONTRACT_TERMINATED = 'CONTRACT_TERMINATED',
  
  // Price Events
  PRICE_ADJUSTMENT = 'PRICE_ADJUSTMENT',
  DISCOUNT_APPLIED = 'DISCOUNT_APPLIED',
  PRICE_INDEX_UPDATE = 'PRICE_INDEX_UPDATE',
  
  // Sync Events
  XENTRAL_SYNC_SUCCESS = 'XENTRAL_SYNC_SUCCESS',
  XENTRAL_SYNC_FAILURE = 'XENTRAL_SYNC_FAILURE',
  API_WEBHOOK_RECEIVED = 'API_WEBHOOK_RECEIVED',
  
  // User Actions
  BULK_ACTION_EXECUTED = 'BULK_ACTION_EXECUTED',
  DATA_EXPORT = 'DATA_EXPORT',
  GDPR_DATA_REQUEST = 'GDPR_DATA_REQUEST',
  
  // Activity Events (FC-013)
  ACTIVITY_CREATED = 'ACTIVITY_CREATED',
  TASK_COMPLETED = 'TASK_COMPLETED',
  REMINDER_SENT = 'REMINDER_SENT',
  NOTE_ADDED = 'NOTE_ADDED',
  
  // Permission Events (FC-015)
  PERMISSION_CHECK = 'PERMISSION_CHECK',
  PERMISSION_DENIED = 'PERMISSION_DENIED',
  ROLE_ASSIGNED = 'ROLE_ASSIGNED',
  ROLE_REMOVED = 'ROLE_REMOVED',
  DELEGATION_CREATED = 'DELEGATION_CREATED',
  DELEGATION_ACTIVATED = 'DELEGATION_ACTIVATED',
  APPROVAL_REQUESTED = 'APPROVAL_REQUESTED',
  APPROVAL_DECISION = 'APPROVAL_DECISION'
}
```

### Audit Entry Structure
```typescript
interface AuditEntry {
  id: string;
  timestamp: DateTime;
  eventType: AuditEventType;
  entityType: 'opportunity' | 'contract' | 'customer' | 'price';
  entityId: string;
  userId: string;
  userName: string;
  userRole: string;
  
  // Change Details
  oldValue?: any;
  newValue?: any;
  changeReason?: string;
  userComment?: string;
  
  // Context
  ipAddress: string;
  userAgent: string;
  sessionId: string;
  
  // API/System
  source: 'UI' | 'API' | 'SYSTEM' | 'WEBHOOK';
  apiEndpoint?: string;
  requestId?: string;
  
  // Compliance
  dataHash: string; // SHA-256 of entry for integrity
  previousHash: string; // Blockchain-style chaining
}
```

## 📐 Datenbank-Design

### Audit Tables
```sql
-- Haupttabelle für Audit-Einträge (append-only)
CREATE TABLE audit_trail (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    timestamp TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    event_type VARCHAR(50) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id UUID NOT NULL,
    
    -- User Information
    user_id UUID NOT NULL,
    user_name VARCHAR(255) NOT NULL,
    user_role VARCHAR(50) NOT NULL,
    
    -- Change Details (JSONB for flexibility)
    change_data JSONB NOT NULL,
    
    -- Context
    ip_address INET,
    user_agent TEXT,
    session_id UUID,
    
    -- Source
    source VARCHAR(20) NOT NULL,
    api_endpoint TEXT,
    request_id UUID,
    
    -- Integrity
    data_hash VARCHAR(64) NOT NULL,
    previous_hash VARCHAR(64),
    
    -- Indexes
    INDEX idx_audit_entity (entity_type, entity_id, timestamp DESC),
    INDEX idx_audit_user (user_id, timestamp DESC),
    INDEX idx_audit_type (event_type, timestamp DESC),
    INDEX idx_audit_timestamp (timestamp DESC)
);

-- Partitionierung nach Monat für Performance
CREATE TABLE audit_trail_2025_07 PARTITION OF audit_trail
    FOR VALUES FROM ('2025-07-01') TO ('2025-08-01');
```

## 📊 Implementation Details

### 1. Audit Service
```java
@ApplicationScoped
public class AuditService {
    
    @Inject
    AuditRepository auditRepository;
    
    @Inject
    SecurityContext securityContext;
    
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void logEvent(AuditEventType type, 
                        String entityType,
                        UUID entityId,
                        Object oldValue,
                        Object newValue,
                        String reason) {
        
        // Neue Transaktion damit Audit auch bei Rollback erhalten bleibt
        AuditEntry entry = AuditEntry.builder()
            .eventType(type)
            .entityType(entityType)
            .entityId(entityId)
            .userId(getCurrentUserId())
            .userName(getCurrentUserName())
            .userRole(getCurrentUserRole())
            .oldValue(toJson(oldValue))
            .newValue(toJson(newValue))
            .changeReason(reason)
            .timestamp(Instant.now())
            .source(determineSource())
            .dataHash(calculateHash())
            .previousHash(getLastHash())
            .build();
            
        auditRepository.persist(entry);
    }
}
```

### 2. Audit Interceptor
```java
@Interceptor
@Auditable
public class AuditInterceptor {
    
    @AroundInvoke
    public Object audit(InvocationContext context) throws Exception {
        Method method = context.getMethod();
        Auditable annotation = method.getAnnotation(Auditable.class);
        
        Object[] params = context.getParameters();
        Object result = null;
        
        try {
            result = context.proceed();
            
            // Log successful operation
            auditService.logEvent(
                annotation.eventType(),
                annotation.entityType(),
                extractEntityId(params),
                extractOldValue(params),
                result,
                extractReason(params)
            );
            
        } catch (Exception e) {
            // Log failed operation
            auditService.logFailure(annotation, params, e);
            throw e;
        }
        
        return result;
    }
}
```

### 3. Export Functionality
```java
@Path("/api/audit")
@RolesAllowed({"admin", "auditor"})
public class AuditResource {
    
    @GET
    @Path("/export")
    @Produces("text/csv")
    public Response exportAuditTrail(
        @QueryParam("from") LocalDate from,
        @QueryParam("to") LocalDate to,
        @QueryParam("entityType") String entityType,
        @QueryParam("eventType") String eventType
    ) {
        var entries = auditService.findByFilters(from, to, entityType, eventType);
        
        var csv = AuditCsvExporter.export(entries);
        
        // Log the export itself
        auditService.logEvent(
            AuditEventType.DATA_EXPORT,
            "audit_trail",
            null,
            null,
            Map.of("recordCount", entries.size()),
            "Audit export requested"
        );
        
        return Response.ok(csv)
            .header("Content-Disposition", 
                   "attachment; filename=audit_trail_" + LocalDate.now() + ".csv")
            .build();
    }
}
```

## 📚 Detail-Dokumente

1. **Backend Implementation:** [./FC-012/backend-implementation.md](./FC-012/backend-implementation.md)
2. **Frontend Audit Viewer:** [./FC-012/frontend-audit-viewer.md](./FC-012/frontend-audit-viewer.md)
3. **Compliance Requirements:** [./FC-012/compliance-requirements.md](./FC-012/compliance-requirements.md)
4. **Export Formats:** [./FC-012/export-formats.md](./FC-012/export-formats.md)

## 🔗 Integration mit anderen Features

### M4 Opportunity Pipeline
- Alle Stage-Wechsel werden geloggt
- Quick Actions (Win/Loss) mit Reason
- Drag & Drop Tracking

### FC-009 Contract Renewal
- Contract Lifecycle komplett auditiert
- Renewal-Prozess mit Begründungen
- Preisanpassungen nachvollziehbar

### FC-005 Xentral Integration
- Alle API-Calls protokolliert
- Sync-Status und Fehler
- Webhook-Empfang dokumentiert

## 🚀 Implementierungs-Priorität

**KRITISCH!** Sollte parallel zu anderen Features implementiert werden, da es eine Querschnittsfunktion ist.

### Phasen:
1. **Phase 1:** Basis Audit-Service (1 Tag)
2. **Phase 2:** Integration in bestehende Features (1 Tag)
3. **Phase 3:** Audit Viewer UI (1 Tag)
4. **Phase 4:** Export & Compliance Features (1 Tag)

## ⚠️ Wichtige Überlegungen

1. **Performance:** Audit darf Business-Logic nicht verlangsamen
2. **Storage:** Monatliche Partitionierung für große Datenmengen
3. **Retention:** Aufbewahrungsfristen beachten (7 Jahre?)
4. **GDPR:** Personenbezogene Daten in Audit-Logs

## 📊 Messbare Erfolge

- 100% Coverage aller kritischen Operationen
- < 10ms Overhead pro Operation
- Export in < 5 Sekunden für 1 Jahr Daten
- Zero Data Loss bei System-Crashes