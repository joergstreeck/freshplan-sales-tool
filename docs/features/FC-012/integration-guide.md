# FC-012: Integration Guide für bestehende Features

**Parent:** [/Users/joergstreeck/freshplan-sales-tool/docs/features/2025-07-24_TECH_CONCEPT_FC-012-audit-trail-system.md](/Users/joergstreeck/freshplan-sales-tool/docs/features/2025-07-24_TECH_CONCEPT_FC-012-audit-trail-system.md)  
**Fokus:** Wie bestehende Features das Audit System integrieren müssen

## 🔄 M4 Opportunity Pipeline

### Backend Integration
```java
@ApplicationScoped
@Transactional
public class OpportunityService {
    
    @Inject
    AuditService auditService; // NEU
    
    @Auditable(
        eventType = AuditEventType.OPPORTUNITY_STAGE_CHANGED,
        entityType = "opportunity"
    )
    public Opportunity changeStage(
        UUID opportunityId, 
        OpportunityStage newStage,
        @AuditReason String reason // NEU: Pflicht-Parameter
    ) {
        var opportunity = repository.findById(opportunityId);
        var oldStage = opportunity.getStage();
        
        opportunity.setStage(newStage);
        
        // Audit wird automatisch erstellt
        return repository.persist(opportunity);
    }
    
    @Auditable(eventType = AuditEventType.OPPORTUNITY_WON)
    public Opportunity markAsWon(
        UUID opportunityId,
        @AuditReason String closeReason,
        BigDecimal finalValue
    ) {
        // Implementation
    }
}
```

### Frontend Integration
```typescript
// Reason-Dialog vor Stage-Änderung
const handleStageChange = async (newStage: OpportunityStage) => {
  const reason = await showReasonDialog({
    title: 'Grund für Stage-Wechsel',
    message: `Warum wird die Opportunity nach ${newStage} verschoben?`,
    required: true,
    suggestions: getReasonSuggestions(currentStage, newStage)
  });
  
  if (reason) {
    await opportunityApi.changeStage(opportunityId, newStage, reason);
  }
};
```

## 🔄 FC-009 Contract Renewal

### Vollständiges Contract Lifecycle Auditing
```java
@ApplicationScoped
public class ContractService {
    
    @Auditable(
        eventType = AuditEventType.CONTRACT_CREATED,
        entityType = "contract"
    )
    public Contract createContract(
        CreateContractRequest request,
        @AuditReason String creationReason
    ) {
        // Implementation
    }
    
    @Auditable(eventType = AuditEventType.CONTRACT_RENEWED)
    public Contract renewContract(
        UUID contractId,
        @AuditReason String renewalReason,
        RenewalTerms terms
    ) {
        var contract = repository.findById(contractId);
        var oldTerms = contract.getTerms();
        
        contract.renew(terms);
        
        // Preisänderungen separat auditieren
        if (!oldTerms.getPrice().equals(terms.getPrice())) {
            auditService.logEvent(
                AuditEventType.PRICE_ADJUSTMENT,
                "contract",
                contractId,
                oldTerms.getPrice(),
                terms.getPrice(),
                "Price adjustment during renewal"
            );
        }
        
        return repository.persist(contract);
    }
}
```

## 🔄 FC-005 Xentral Integration

### API Sync Tracking
```java
@ApplicationScoped
public class XentralSyncService {
    
    @Inject
    AuditService auditService;
    
    public void syncContract(Contract contract) {
        var syncStart = Instant.now();
        
        try {
            var response = xentralApi.updateContract(contract);
            
            // Erfolgreiche Sync auditieren
            auditService.logEvent(
                AuditEventType.XENTRAL_SYNC_SUCCESS,
                "contract",
                contract.getId(),
                null,
                Map.of(
                    "endpoint", "/api/contracts",
                    "duration", Duration.between(syncStart, Instant.now()),
                    "responseCode", response.getStatus()
                ),
                "Automatic sync to Xentral"
            );
            
        } catch (Exception e) {
            // Fehlgeschlagene Sync auditieren
            auditService.logEvent(
                AuditEventType.XENTRAL_SYNC_FAILURE,
                "contract",
                contract.getId(),
                null,
                Map.of(
                    "error", e.getMessage(),
                    "endpoint", "/api/contracts",
                    "duration", Duration.between(syncStart, Instant.now())
                ),
                "Sync to Xentral failed"
            );
            
            throw new XentralSyncException("Sync failed", e);
        }
    }
}
```

## 🔄 FC-011 Pipeline-Cockpit Integration

### Customer Access Tracking
```java
@Path("/api/customers")
public class CustomerResource {
    
    @GET
    @Path("/{customerId}/with-context")
    @Auditable(
        eventType = AuditEventType.CUSTOMER_DATA_ACCESSED,
        entityType = "customer"
    )
    public Response getCustomerWithContext(
        @PathParam("customerId") UUID customerId,
        @QueryParam("opportunityId") UUID opportunityId,
        @QueryParam("source") String source // "pipeline", "search", "direct"
    ) {
        // Track access pattern for smart preloading
        accessLogService.recordAccess(
            customerId,
            opportunityId,
            source,
            getCurrentUser()
        );
        
        return Response.ok(
            customerService.loadWithContext(customerId, opportunityId)
        ).build();
    }
}
```

## 🔄 M5 Customer Management

### Customer Data Changes
```java
@ApplicationScoped
public class CustomerService {
    
    @Auditable(
        eventType = AuditEventType.CUSTOMER_UPDATED,
        entityType = "customer"
    )
    public Customer updateCustomer(
        UUID customerId,
        UpdateCustomerRequest request,
        @AuditReason String updateReason
    ) {
        var customer = repository.findById(customerId);
        var oldData = CustomerSnapshot.from(customer);
        
        // Apply updates
        mapper.updateCustomerFromRequest(customer, request);
        
        // GDPR-relevante Änderungen extra auditieren
        if (hasPersonalDataChanged(oldData, customer)) {
            auditService.logEvent(
                AuditEventType.PERSONAL_DATA_MODIFIED,
                "customer",
                customerId,
                maskPersonalData(oldData),
                maskPersonalData(customer),
                "Personal data update: " + updateReason
            );
        }
        
        return repository.persist(customer);
    }
}
```

## 🔄 FC-003 Email Integration

### Email Activity Tracking
```java
@ApplicationScoped
public class EmailService {
    
    @Auditable(
        eventType = AuditEventType.EMAIL_SENT,
        entityType = "communication"
    )
    public void sendEmail(
        EmailRequest request,
        @AuditContext String context // "opportunity_follow_up", "contract_renewal"
    ) {
        // Send email
        var messageId = emailProvider.send(request);
        
        // Additional tracking for compliance
        if (request.hasAttachments()) {
            auditService.logEvent(
                AuditEventType.DOCUMENT_SHARED,
                "email",
                messageId,
                null,
                Map.of(
                    "recipient", request.getTo(),
                    "attachments", request.getAttachmentNames(),
                    "context", context
                ),
                "Documents shared via email"
            );
        }
    }
}
```

## 🎯 Best Practices für Integration

### 1. Reason-Erfassung
```java
// IMMER Reason bei kritischen Operationen
public void criticalOperation(
    UUID entityId,
    @AuditReason String reason // Pflicht!
) {
    // Implementation
}
```

### 2. Sensitive Daten
```java
// Maskierung sensibler Daten
@AuditSensitive
private String bankAccount;

@AuditSensitive(mask = "XXX-XXX-####")
private String phoneNumber;
```

### 3. Bulk Operations
```java
@Auditable(eventType = AuditEventType.BULK_ACTION_EXECUTED)
public void bulkUpdate(
    List<UUID> entityIds,
    BulkUpdate update,
    @AuditReason String reason
) {
    // Log once for bulk, not per item
}
```

### 4. Frontend Reason Dialogs
```typescript
// Standardisierte Reason-Dialoge
const REASON_TEMPLATES = {
  OPPORTUNITY_WON: [
    'Kunde hat Angebot angenommen',
    'Verhandlung erfolgreich abgeschlossen',
    'Konkurrenz ausgestochen'
  ],
  CONTRACT_RENEWED: [
    'Kunde zufrieden mit Service',
    'Neue Konditionen verhandelt',
    'Automatische Verlängerung'
  ]
};
```

## 📋 Integration Checklist

Für jedes Feature:
- [ ] `@Auditable` Annotations hinzugefügt
- [ ] Reason-Parameter bei kritischen Operationen
- [ ] Sensitive Daten markiert
- [ ] Frontend Reason-Dialoge implementiert
- [ ] Bulk Operations optimiert
- [ ] Tests für Audit-Einträge
- [ ] Dokumentation aktualisiert