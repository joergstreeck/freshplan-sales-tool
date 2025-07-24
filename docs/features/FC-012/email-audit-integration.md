# FC-012: Audit Trail - E-Mail Integration (FC-003)

## E-Mail Audit Events

### Audit-relevante E-Mail Aktionen

```java
@Auditable
@ApplicationScoped
public class EmailAuditService {
    
    @Inject AuditService auditService;
    
    @AuditAction("EMAIL_SENT")
    public void auditEmailSent(EmailMessage email, User sender) {
        AuditEntry entry = AuditEntry.builder()
            .action("EMAIL_SENT")
            .entityType("EMAIL")
            .entityId(email.getId().toString())
            .userId(sender.getId())
            .timestamp(Instant.now())
            .details(Map.of(
                "to", email.getRecipients(),
                "subject", email.getSubject(),
                "hasAttachments", email.hasAttachments(),
                "templateUsed", email.getTemplateId(),
                "customerId", email.getCustomerId(),
                "opportunityId", email.getOpportunityId()
            ))
            .ipAddress(getClientIp())
            .userAgent(getUserAgent())
            .build();
            
        auditService.log(entry);
    }
    
    @AuditAction("EMAIL_TEMPLATE_CREATED")
    public void auditTemplateCreation(EmailTemplate template, User creator) {
        auditService.log(AuditEntry.builder()
            .action("EMAIL_TEMPLATE_CREATED")
            .entityType("EMAIL_TEMPLATE")
            .entityId(template.getId().toString())
            .userId(creator.getId())
            .details(Map.of(
                "templateName", template.getName(),
                "category", template.getCategory(),
                "variables", template.getRequiredVariables()
            ))
            .build());
    }
    
    @AuditAction("EMAIL_TRACKING_ACCESSED")
    public void auditTrackingAccess(UUID messageId, User accessor) {
        auditService.log(AuditEntry.builder()
            .action("EMAIL_TRACKING_ACCESSED")
            .entityType("EMAIL")
            .entityId(messageId.toString())
            .userId(accessor.getId())
            .details(Map.of(
                "accessType", "TRACKING_METRICS",
                "dataViewed", List.of("opens", "clicks", "replies")
            ))
            .build());
    }
}
```

## Compliance-Anforderungen

### DSGVO-konforme E-Mail-Speicherung

```java
@ApplicationScoped
public class EmailComplianceService {
    
    @Inject AuditService auditService;
    @Inject EncryptionService encryptionService;
    
    public void handleEmailDeletion(EmailMessage email, String reason) {
        // Audit vor Löschung
        auditService.log(AuditEntry.builder()
            .action("EMAIL_DELETED")
            .entityType("EMAIL")
            .entityId(email.getId().toString())
            .details(Map.of(
                "reason", reason,
                "subject", email.getSubject(),
                "sentAt", email.getSentAt(),
                "recipients", email.getRecipients(),
                "hasAttachments", email.hasAttachments()
            ))
            .compliance(Map.of(
                "gdprRequest", reason.contains("GDPR"),
                "retentionPeriodExceeded", reason.contains("RETENTION")
            ))
            .build());
            
        // Sichere Löschung
        securelyDeleteEmail(email);
    }
    
    public void exportEmailDataForGDPR(UUID customerId) {
        List<EmailMessage> emails = emailRepository
            .findByCustomerId(customerId);
            
        // Audit Export
        auditService.log(AuditEntry.builder()
            .action("EMAIL_DATA_EXPORTED")
            .entityType("CUSTOMER")
            .entityId(customerId.toString())
            .details(Map.of(
                "emailCount", emails.size(),
                "exportReason", "GDPR_REQUEST",
                "includesAttachments", hasAttachments(emails)
            ))
            .build());
    }
}
```

## Audit Trail für E-Mail Templates

### Template-Änderungen nachvollziehen

```java
@Auditable
public class EmailTemplateAuditService {
    
    @AuditAction("TEMPLATE_MODIFIED")
    public EmailTemplate updateTemplate(UUID templateId, 
                                        TemplateUpdateRequest request,
                                        User modifier) {
        EmailTemplate existing = templateRepository.find(templateId);
        
        // Capture changes
        Map<String, Change> changes = captureChanges(existing, request);
        
        // Audit mit Details
        auditService.log(AuditEntry.builder()
            .action("TEMPLATE_MODIFIED")
            .entityType("EMAIL_TEMPLATE")
            .entityId(templateId.toString())
            .userId(modifier.getId())
            .changes(changes)
            .previousVersion(createTemplateSnapshot(existing))
            .build());
            
        return templateRepository.update(existing, request);
    }
    
    private Map<String, Change> captureChanges(EmailTemplate existing, 
                                               TemplateUpdateRequest request) {
        Map<String, Change> changes = new HashMap<>();
        
        if (!existing.getSubject().equals(request.getSubject())) {
            changes.put("subject", new Change(
                existing.getSubject(), 
                request.getSubject()
            ));
        }
        
        if (!existing.getBody().equals(request.getBody())) {
            changes.put("body", new Change(
                "[HTML Content]", // Don't log full HTML
                "[HTML Content Updated]"
            ));
        }
        
        return changes;
    }
}
```

## E-Mail Access Audit

### Sensitive E-Mail-Zugriffe protokollieren

```java
@ApplicationScoped
public class EmailAccessAudit {
    
    @Inject AuditService auditService;
    
    public void auditEmailView(EmailMessage email, User viewer) {
        // Nur für sensitive E-Mails
        if (isSensitive(email)) {
            auditService.log(AuditEntry.builder()
                .action("SENSITIVE_EMAIL_VIEWED")
                .entityType("EMAIL")
                .entityId(email.getId().toString())
                .userId(viewer.getId())
                .details(Map.of(
                    "customerId", email.getCustomerId(),
                    "opportunityValue", getOpportunityValue(email),
                    "viewContext", getViewContext()
                ))
                .sensitivity("HIGH")
                .build());
        }
    }
    
    private boolean isSensitive(EmailMessage email) {
        // E-Mails mit hohem Opportunity-Wert
        if (email.getOpportunityId() != null) {
            Opportunity opp = opportunityRepository
                .find(email.getOpportunityId());
            if (opp.getValue() > 50000) return true;
        }
        
        // E-Mails mit bestimmten Keywords
        return containsSensitiveKeywords(email);
    }
}
```

## Audit Reports für E-Mail-Aktivitäten

### E-Mail Audit Dashboard

```typescript
export const EmailAuditDashboard: React.FC = () => {
    const {data: auditStats} = useEmailAuditStats();
    
    return (
        <Grid container spacing={3}>
            <Grid item xs={12}>
                <Typography variant="h6">E-Mail Audit Übersicht</Typography>
            </Grid>
            
            <Grid item xs={12} md={4}>
                <StatCard
                    title="E-Mails gesendet (30 Tage)"
                    value={auditStats.emailsSent}
                    subtext={`${auditStats.uniqueSenders} Benutzer`}
                />
            </Grid>
            
            <Grid item xs={12} md={4}>
                <StatCard
                    title="Templates geändert"
                    value={auditStats.templateModifications}
                    subtext="Letzte 30 Tage"
                />
            </Grid>
            
            <Grid item xs={12} md={4}>
                <StatCard
                    title="DSGVO-Exporte"
                    value={auditStats.gdprExports}
                    subtext="Dieses Jahr"
                />
            </Grid>
            
            <Grid item xs={12}>
                <AuditTimeline 
                    filter={{
                        entityTypes: ['EMAIL', 'EMAIL_TEMPLATE'],
                        actions: ['EMAIL_SENT', 'TEMPLATE_MODIFIED']
                    }}
                />
            </Grid>
        </Grid>
    );
};
```

## Retention Policy Integration

### E-Mail Retention mit Audit Trail

```java
@ApplicationScoped
public class EmailRetentionAudit {
    
    @Scheduled(every = "24h")
    public void enforceRetentionPolicy() {
        RetentionPolicy policy = retentionService.getEmailPolicy();
        
        List<EmailMessage> expiredEmails = emailRepository
            .findOlderThan(policy.getRetentionPeriod());
            
        for (EmailMessage email : expiredEmails) {
            // Audit before deletion
            auditService.log(AuditEntry.builder()
                .action("EMAIL_RETENTION_DELETE")
                .entityType("EMAIL")
                .entityId(email.getId().toString())
                .systemAction(true)
                .details(Map.of(
                    "age", calculateAge(email),
                    "policy", policy.getName(),
                    "retentionDays", policy.getRetentionDays()
                ))
                .build());
                
            emailRepository.delete(email);
        }
        
        // Summary Audit
        auditService.log(AuditEntry.builder()
            .action("RETENTION_POLICY_EXECUTED")
            .entityType("SYSTEM")
            .details(Map.of(
                "deletedEmails", expiredEmails.size(),
                "policy", "EMAIL_RETENTION",
                "executionTime", Instant.now()
            ))
            .build());
    }
}
```