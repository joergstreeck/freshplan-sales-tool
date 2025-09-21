# FC-015: Approval Workflows - Freigabeprozesse

**Zurück:** [FC-015 Hauptkonzept](/Users/joergstreeck/freshplan-sales-tool/docs/features/2025-07-24_TECH_CONCEPT_FC-015-rights-roles-concept.md)  
**Vorheriges:** [Delegation & Vertretung](./delegation-vertretung.md)

## 📋 Übersicht

Implementierung flexibler Freigabeprozesse für kritische Aktionen wie hohe Rabatte, Vertragsänderungen und Opportunity-Löschungen.

## 🏗️ Workflow Engine

### Approval Request Entity
```java
@Entity
@Table(name = "approval_requests")
public class ApprovalRequest extends AuditedEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;
    
    @Enumerated(EnumType.STRING)
    public ApprovalType type; // DISCOUNT, CONTRACT_CHANGE, OPPORTUNITY_DELETE, etc.
    
    @ManyToOne(optional = false)
    public User requester;
    
    @Column(nullable = false)
    public String resourceType; // "Opportunity", "Contract", etc.
    
    @Column(nullable = false)
    public UUID resourceId;
    
    @Column(columnDefinition = "jsonb")
    @Convert(converter = JsonConverter.class)
    public Map<String, Object> requestData;
    
    @Enumerated(EnumType.STRING)
    public ApprovalStatus status; // PENDING, APPROVED, REJECTED, ESCALATED, EXPIRED
    
    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL)
    @OrderBy("level ASC")
    public List<ApprovalLevel> levels;
    
    @Column(nullable = false)
    public LocalDateTime expiresAt;
    
    @Column(length = 1000)
    public String businessJustification;
    
    @OneToMany(mappedBy = "request")
    @OrderBy("createdAt DESC")
    public List<ApprovalAction> actions;
}
```

### Workflow Definition
```java
@Entity
@Table(name = "workflow_definitions")
public class WorkflowDefinition {
    @Id
    public String workflowKey; // z.B. "discount_approval"
    
    @Column(nullable = false)
    public String name;
    
    @OneToMany(mappedBy = "workflow", cascade = CascadeType.ALL)
    @OrderBy("level ASC")
    public List<WorkflowStep> steps;
    
    @Column(columnDefinition = "jsonb")
    @Convert(converter = JsonConverter.class)
    public WorkflowRules rules;
    
    public boolean active = true;
}

@Embeddable
public class WorkflowRules {
    public Integer autoApproveThreshold; // z.B. Rabatte < 5%
    public Integer escalationTimeoutHours;
    public boolean allowSelfApproval = false;
    public boolean requireAllApprovers = false; // vs. one-of
    public Map<String, String> conditions; // Dynamische Bedingungen
}
```

### Workflow Service
```java
@ApplicationScoped
@Transactional
public class ApprovalWorkflowService {
    @Inject
    ApprovalRequestRepository requestRepo;
    
    @Inject
    WorkflowDefinitionRepository workflowRepo;
    
    @Inject
    NotificationService notificationService;
    
    @Inject
    SecurityContextProvider securityContext;
    
    @Inject
    Event<ApprovalEvent> approvalEvents;
    
    public ApprovalRequest createApprovalRequest(CreateApprovalRequest request) {
        var workflow = workflowRepo.findById(request.workflowKey)
            .orElseThrow(() -> new IllegalArgumentException("Unknown workflow"));
            
        // Auto-Approve Check
        if (shouldAutoApprove(workflow, request)) {
            return autoApprove(request);
        }
        
        var approvalRequest = new ApprovalRequest();
        approvalRequest.type = request.type;
        approvalRequest.requester = securityContext.getCurrentUser();
        approvalRequest.resourceType = request.resourceType;
        approvalRequest.resourceId = request.resourceId;
        approvalRequest.requestData = request.data;
        approvalRequest.businessJustification = request.justification;
        approvalRequest.status = ApprovalStatus.PENDING;
        approvalRequest.expiresAt = calculateExpiry(workflow);
        
        // Create approval levels based on workflow
        approvalRequest.levels = createApprovalLevels(workflow, request);
        
        requestRepo.persist(approvalRequest);
        
        // Notify first level approvers
        notifyApprovers(approvalRequest.levels.get(0));
        
        approvalEvents.fire(new ApprovalRequestCreated(approvalRequest));
        
        return approvalRequest;
    }
    
    public void processApproval(UUID requestId, ApprovalDecision decision) {
        var request = requestRepo.findById(requestId)
            .orElseThrow(() -> new NotFoundException("Approval request not found"));
            
        validateApprover(request, securityContext.getCurrentUser());
        
        var action = new ApprovalAction();
        action.request = request;
        action.approver = securityContext.getCurrentUser();
        action.decision = decision.decision; // APPROVE, REJECT, ESCALATE
        action.comment = decision.comment;
        action.timestamp = LocalDateTime.now();
        
        request.actions.add(action);
        
        // Update current level
        var currentLevel = getCurrentLevel(request);
        updateLevelStatus(currentLevel, action);
        
        // Check if level is complete
        if (isLevelComplete(currentLevel)) {
            if (currentLevel.status == ApprovalStatus.APPROVED) {
                // Move to next level or complete
                if (hasNextLevel(request)) {
                    activateNextLevel(request);
                    notifyApprovers(getNextLevel(request));
                } else {
                    completeApproval(request);
                }
            } else {
                rejectApproval(request);
            }
        }
        
        requestRepo.persist(request);
    }
    
    @Scheduled(every = "1h")
    void processEscalations() {
        var overdueRequests = requestRepo.findOverdue(LocalDateTime.now());
        
        overdueRequests.forEach(request -> {
            var currentLevel = getCurrentLevel(request);
            if (currentLevel.escalationTo != null) {
                escalateToNextLevel(request, currentLevel);
            } else {
                expireRequest(request);
            }
        });
    }
}
```

## 🎯 Vordefinierte Workflows

### Rabatt-Freigabe
```java
public class DiscountApprovalWorkflow {
    public static WorkflowDefinition create() {
        var workflow = new WorkflowDefinition();
        workflow.workflowKey = "discount_approval";
        workflow.name = "Rabatt-Freigabeprozess";
        
        // Level 1: Team Lead (10-20%)
        var step1 = new WorkflowStep();
        step1.level = 1;
        step1.name = "Team Lead Approval";
        step1.approverRole = "SALES_MANAGER";
        step1.condition = "discount_percentage BETWEEN 10 AND 20";
        step1.escalationTimeout = Duration.ofHours(24);
        
        // Level 2: Director (20-30%)
        var step2 = new WorkflowStep();
        step2.level = 2;
        step2.name = "Director Approval";
        step2.approverRole = "SALES_DIRECTOR";
        step2.condition = "discount_percentage BETWEEN 20 AND 30";
        step2.escalationTimeout = Duration.ofHours(12);
        
        // Level 3: C-Level (>30%)
        var step3 = new WorkflowStep();
        step3.level = 3;
        step3.name = "Executive Approval";
        step3.approverRole = "C_LEVEL";
        step3.condition = "discount_percentage > 30";
        step3.escalationTimeout = Duration.ofHours(6);
        
        workflow.steps = List.of(step1, step2, step3);
        
        workflow.rules = new WorkflowRules();
        workflow.rules.autoApproveThreshold = 10; // <10% auto-approve
        workflow.rules.allowSelfApproval = false;
        
        return workflow;
    }
}
```

### Contract Renewal Approval
```java
public class ContractRenewalWorkflow {
    public static WorkflowDefinition create() {
        var workflow = new WorkflowDefinition();
        workflow.workflowKey = "contract_renewal";
        workflow.name = "Vertrags-Verlängerung";
        
        var step1 = new WorkflowStep();
        step1.level = 1;
        step1.name = "Account Manager Review";
        step1.approverExpression = "opportunity.assignedTo"; // Dynamic
        
        var step2 = new WorkflowStep();
        step2.level = 2;
        step2.name = "Legal Review";
        step2.approverRole = "LEGAL_TEAM";
        step2.condition = "contract.hasSpecialTerms == true";
        step2.optional = true;
        
        var step3 = new WorkflowStep();
        step3.level = 3;
        step3.name = "Finance Approval";
        step3.approverRole = "FINANCE";
        step3.condition = "contract.totalValue > 100000";
        
        workflow.steps = List.of(step1, step2, step3);
        return workflow;
    }
}

### Bonitätsprüfungs-Workflow (NEU)
```java
public class CreditCheckApprovalWorkflow {
    public static WorkflowDefinition create() {
        var workflow = new WorkflowDefinition();
        workflow.workflowKey = "credit_check_approval";
        workflow.name = "Bonitätsprüfung & Zahlungsziel";
        
        // Level 1: Automatische Prüfung
        var step1 = new WorkflowStep();
        step1.level = 1;
        step1.name = "Auto Credit Check";
        step1.type = WorkflowStepType.AUTOMATED;
        step1.automationExpression = "creditScore >= 700 && paymentHistory == 'GOOD'";
        step1.autoApprove = true;
        
        // Level 2: Sales Manager bei mittlerem Risiko
        var step2 = new WorkflowStep();
        step2.level = 2;
        step2.name = "Sales Manager Review";
        step2.approverRole = "SALES_MANAGER";
        step2.condition = "creditScore BETWEEN 500 AND 699 OR requestedPaymentTerms > 30";
        step2.requiredData = List.of("creditReport", "paymentHistory", "orderVolume");
        
        // Level 3: Finance bei hohem Risiko
        var step3 = new WorkflowStep();
        step3.level = 3;
        step3.name = "Finance Risk Assessment";
        step3.approverRole = "FINANCE_MANAGER";
        step3.condition = "creditScore < 500 OR requestedCreditLimit > 50000 OR hasOutstandingDebts";
        step3.escalationTimeout = Duration.ofHours(4);
        
        // Level 4: C-Level bei sehr hohem Risiko
        var step4 = new WorkflowStep();
        step4.level = 4;
        step4.name = "Executive Decision";
        step4.approverRole = "C_LEVEL";
        step4.condition = "totalRiskValue > 100000 OR previousPaymentDefaults > 0";
        
        workflow.steps = List.of(step1, step2, step3, step4);
        
        workflow.rules = new WorkflowRules();
        workflow.rules.autoRejectThreshold = 300; // Credit Score < 300
        workflow.rules.requireExternalData = true; // Schufa/Creditreform
        
        return workflow;
    }
}
```

## 🎨 Frontend Integration

### Approval Request Component
```typescript
export const ApprovalRequestForm: React.FC<ApprovalRequestProps> = ({
  type,
  resourceId,
  data,
  onSubmit
}) => {
  const [justification, setJustification] = useState('');
  const { data: workflow } = useQuery(['workflow', type], () => fetchWorkflow(type));
  
  const previewApprovers = useMemo(() => {
    if (!workflow) return [];
    return calculateApprovers(workflow, data);
  }, [workflow, data]);

  return (
    <Dialog open onClose={onClose}>
      <DialogTitle>Freigabe anfordern: {type}</DialogTitle>
      <DialogContent>
        <Alert severity="info" sx={{ mb: 2 }}>
          Diese Aktion erfordert eine Freigabe durch:
          <List dense>
            {previewApprovers.map((level, idx) => (
              <ListItem key={idx}>
                <ListItemText 
                  primary={`Level ${idx + 1}: ${level.roleName}`}
                  secondary={level.condition}
                />
              </ListItem>
            ))}
          </List>
        </Alert>
        
        <TextField
          fullWidth
          multiline
          rows={4}
          label="Geschäftliche Begründung"
          value={justification}
          onChange={(e) => setJustification(e.target.value)}
          helperText="Erklären Sie, warum diese Ausnahme notwendig ist"
          required
        />
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Abbrechen</Button>
        <Button 
          onClick={() => onSubmit({ justification })}
          disabled={!justification.trim()}
          variant="contained"
        >
          Freigabe anfordern
        </Button>
      </DialogActions>
    </Dialog>
  );
};
```

### Approval Dashboard
```typescript
export const ApprovalDashboard: React.FC = () => {
  const { data: pendingApprovals } = useQuery(
    ['approvals', 'pending'],
    fetchPendingApprovals,
    { refetchInterval: 30000 }
  );

  const handleDecision = useMutation(
    (data: { requestId: string; decision: ApprovalDecision }) =>
      submitApprovalDecision(data.requestId, data.decision),
    {
      onSuccess: () => {
        queryClient.invalidateQueries(['approvals']);
        toast.success('Entscheidung gespeichert');
      }
    }
  );

  return (
    <Grid container spacing={3}>
      <Grid item xs={12}>
        <Typography variant="h5" gutterBottom>
          Ausstehende Freigaben ({pendingApprovals?.length || 0})
        </Typography>
      </Grid>
      
      {pendingApprovals?.map(request => (
        <Grid item xs={12} md={6} key={request.id}>
          <ApprovalCard
            request={request}
            onApprove={(comment) => handleDecision.mutate({
              requestId: request.id,
              decision: { decision: 'APPROVE', comment }
            })}
            onReject={(comment) => handleDecision.mutate({
              requestId: request.id,
              decision: { decision: 'REJECT', comment }
            })}
          />
        </Grid>
      ))}
    </Grid>
  );
};
```

## 📧 Notification Templates

```java
@ApplicationScoped
public class ApprovalNotificationTemplates {
    
    public EmailTemplate getApprovalRequestTemplate(ApprovalRequest request) {
        return EmailTemplate.builder()
            .subject("Freigabe erforderlich: " + request.type)
            .template("approval-request")
            .variables(Map.of(
                "requesterName", request.requester.name,
                "requestType", request.type,
                "resourceType", request.resourceType,
                "justification", request.businessJustification,
                "expiresAt", request.expiresAt,
                "approvalLink", buildApprovalLink(request.id)
            ))
            .build();
    }
    
    public EmailTemplate getEscalationTemplate(ApprovalRequest request) {
        return EmailTemplate.builder()
            .subject("ESKALATION: Dringende Freigabe - " + request.type)
            .priority(EmailPriority.HIGH)
            .template("approval-escalation")
            .build();
    }
}
```

## 🏦 Bonitätsprüfungs-Integration

### External Credit Check Service
```java
@ApplicationScoped
public class CreditCheckService {
    @Inject
    @ConfigProperty(name = "creditcheck.api.key")
    String apiKey;
    
    @Inject
    XentralClient xentralClient;
    
    @Inject
    ApprovalWorkflowService approvalService;
    
    public CreditCheckResult performCreditCheck(Customer customer) {
        // 1. Xentral-Daten holen (Zahlungshistorie)
        var paymentHistory = xentralClient.getPaymentHistory(customer.xentralId);
        
        // 2. Externe Bonitätsprüfung (Schufa/Creditreform)
        var externalScore = performExternalCheck(customer);
        
        // 3. Interne Scoring-Berechnung
        var internalScore = calculateInternalScore(customer, paymentHistory);
        
        // 4. Kombiniertes Ergebnis
        return CreditCheckResult.builder()
            .customerId(customer.id)
            .externalScore(externalScore)
            .internalScore(internalScore)
            .paymentHistory(paymentHistory)
            .recommendedCreditLimit(calculateCreditLimit(externalScore, internalScore))
            .recommendedPaymentTerms(calculatePaymentTerms(externalScore, paymentHistory))
            .riskLevel(determineRiskLevel(externalScore, internalScore))
            .requiresApproval(externalScore < 700 || internalScore < 600)
            .build();
    }
    
    @Transactional
    public ApprovalRequest requestCreditApproval(
        Customer customer, 
        BigDecimal requestedLimit,
        Integer requestedPaymentTerms
    ) {
        var creditCheck = performCreditCheck(customer);
        
        // Automatische Genehmigung möglich?
        if (creditCheck.externalScore >= 700 && 
            requestedLimit.compareTo(creditCheck.recommendedCreditLimit) <= 0 &&
            requestedPaymentTerms <= creditCheck.recommendedPaymentTerms) {
            
            // Auto-approve und dokumentieren
            approveCreditLimit(customer, requestedLimit, requestedPaymentTerms, "AUTO");
            return null;
        }
        
        // Approval Workflow starten
        return approvalService.createApprovalRequest(
            CreateApprovalRequest.builder()
                .workflowKey("credit_check_approval")
                .type(ApprovalType.CREDIT_CHECK)
                .resourceType("Customer")
                .resourceId(customer.id)
                .data(Map.of(
                    "creditScore", creditCheck.externalScore,
                    "internalScore", creditCheck.internalScore,
                    "requestedCreditLimit", requestedLimit,
                    "recommendedCreditLimit", creditCheck.recommendedCreditLimit,
                    "requestedPaymentTerms", requestedPaymentTerms,
                    "paymentHistory", creditCheck.paymentHistory,
                    "totalOrderVolume", calculateTotalOrderVolume(customer),
                    "outstandingDebts", getOutstandingDebts(customer)
                ))
                .justification(buildJustification(customer, requestedLimit))
                .build()
        );
    }
}
```

### Integration mit M5 Customer Management
```java
@Path("/api/customers/{id}/credit")
public class CustomerCreditResource {
    
    @POST
    @Path("/check")
    @RequiresPermission("customer.credit.check")
    public Response performCreditCheck(@PathParam("id") UUID customerId) {
        var customer = customerService.findById(customerId);
        var result = creditCheckService.performCreditCheck(customer);
        
        // In Customer-Timeline dokumentieren
        timelineService.addEvent(
            customerId,
            "CREDIT_CHECK_PERFORMED",
            Map.of("result", result)
        );
        
        return Response.ok(result).build();
    }
    
    @POST
    @Path("/approval")
    @RequiresPermission("customer.credit.request")
    public Response requestCreditApproval(
        @PathParam("id") UUID customerId,
        CreditApprovalRequest request
    ) {
        var customer = customerService.findById(customerId);
        
        var approvalRequest = creditCheckService.requestCreditApproval(
            customer,
            request.creditLimit,
            request.paymentTerms
        );
        
        if (approvalRequest == null) {
            // Auto-approved
            return Response.ok(Map.of(
                "status", "AUTO_APPROVED",
                "creditLimit", request.creditLimit,
                "paymentTerms", request.paymentTerms
            )).build();
        }
        
        return Response.status(Status.ACCEPTED)
            .entity(Map.of(
                "approvalRequestId", approvalRequest.id,
                "status", "PENDING_APPROVAL",
                "message", "Credit approval request submitted"
            ))
            .build();
    }
}
```

## 🔄 Integration mit anderen Features

### M4 Opportunity Pipeline
```java
@POST
@Path("/{id}/discount")
public Response applyDiscount(@PathParam("id") UUID id, DiscountRequest request) {
    var opportunity = opportunityService.findById(id);
    
    // Check if approval needed
    if (request.percentage > getCurrentUserMaxDiscount()) {
        var approvalRequest = approvalService.createApprovalRequest(
            CreateApprovalRequest.builder()
                .workflowKey("discount_approval")
                .type(ApprovalType.DISCOUNT)
                .resourceType("Opportunity")
                .resourceId(id)
                .data(Map.of(
                    "discount_percentage", request.percentage,
                    "opportunity_value", opportunity.value
                ))
                .justification(request.justification)
                .build()
        );
        
        return Response.status(Status.ACCEPTED)
            .entity(Map.of(
                "approvalRequestId", approvalRequest.id,
                "message", "Discount requires approval"
            ))
            .build();
    }
    
    // Direct approval
    return opportunityService.applyDiscount(id, request);
}
```

## ⚠️ Wichtige Überlegungen

1. **Deadlock Prevention**
   - Zirkuläre Abhängigkeiten vermeiden
   - Timeout für alle Approvals

2. **Compliance**
   - Vollständige Audit Trail
   - Vier-Augen-Prinzip wo nötig

3. **Performance**
   - Approval Status cachen
   - Batch-Notifications

## 🔗 Verknüpfungen

- **Nächstes:** [Permission Management UI](./permission-management-ui.md)
- **Siehe auch:** [FC-012 Audit Trail](/Users/joergstreeck/freshplan-sales-tool/docs/features/2025-07-24_TECH_CONCEPT_FC-012-audit-trail-system.md)
- **API Docs:** [Approval API Reference](./api/approval-api.md)