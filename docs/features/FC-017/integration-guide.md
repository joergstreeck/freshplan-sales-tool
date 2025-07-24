# FC-017: Integration Guide - Error Handling System

**Parent:** [FC-017 Error Handling System](../2025-07-25_TECH_CONCEPT_FC-017-error-handling-system.md)  
**Datum:** 2025-07-25  
**Status:** Draft  

## 📋 Übersicht

Dieses Dokument beschreibt, wie das Error Handling System in bestehende und neue Features integriert wird. Es zeigt die notwendigen Änderungen und Best Practices für eine konsistente Fehlerbehandlung.

## 🎯 Integration Matrix

| Feature | Backend Changes | Frontend Changes | Priority | Aufwand |
|---------|----------------|------------------|----------|---------|
| M4 Opportunity Pipeline | Circuit Breaker für Stage-Updates | Status Indicators auf Cards | HIGH | 2 Tage |
| M5 Customer Management | Fallback für Kundendaten | Offline-Mode Support | HIGH | 1.5 Tage |
| FC-003 E-Mail Integration | Retry für Provider-Ausfälle | Send-Status Anzeige | HIGH | 1 Tag |
| FC-009 Contract Renewal | Queue für Xentral-Updates | Sync-Status auf Timeline | MEDIUM | 1 Tag |
| FC-011 Pipeline Cockpit | Aggregierte Health-Anzeige | Service Status Widget | MEDIUM | 0.5 Tage |
| FC-012 Audit Trail | Error Logging Integration | - | LOW | 0.5 Tage |
| FC-013 Activity Notes | Offline Queue für Notes | Local-First Editing | MEDIUM | 1 Tag |
| FC-015 Rights & Roles | Permission Error Details | Better Error Messages | LOW | 0.5 Tage |

## 🔧 Feature-spezifische Integrationen

### M4 Opportunity Pipeline

#### Backend Integration

```java
// OpportunityService.java
@ApplicationScoped
public class OpportunityService {
    
    @Inject
    XentralServiceClient xentralClient;
    
    @Inject
    ServiceCircuitBreaker circuitBreaker;
    
    @Inject
    FallbackService fallbackService;
    
    @Inject
    ErrorTrackingService errorTracker;
    
    public CompletionStage<OpportunityResponse> updateStage(
        UUID opportunityId, 
        OpportunityStage newStage
    ) {
        var opportunity = repository.findById(opportunityId)
            .orElseThrow(() -> new OpportunityNotFoundException(opportunityId));
        
        // Optimistic update
        var previousStage = opportunity.getStage();
        opportunity.setStage(newStage);
        repository.persist(opportunity);
        
        // Sync with Xentral
        return circuitBreaker.executeAsync(
            "xentral-opportunity-sync",
            () -> xentralClient.updateOpportunityStage(
                opportunity.getXentralId(), 
                newStage
            ),
            error -> {
                // Fallback: Queue for later sync
                fallbackService.queueOpportunityUpdate(opportunity);
                
                // Track error
                errorTracker.track(
                    ErrorCategory.INTEGRATION_ERROR,
                    "xentral",
                    error,
                    Map.of("opportunityId", opportunityId)
                );
                
                // Return with pending status
                return mapper.toResponse(opportunity)
                    .withSyncStatus(SyncStatus.PENDING)
                    .withSyncError(error.getMessage());
            }
        ).handle((result, error) -> {
            if (error != null) {
                // Rollback optimistic update
                opportunity.setStage(previousStage);
                repository.persist(opportunity);
                throw new CompletionException(error);
            }
            return result;
        });
    }
}
```

#### Frontend Integration

```tsx
// OpportunityKanbanBoard.tsx
const OpportunityKanbanBoard: React.FC = () => {
    const { showError } = useNotifications();
    const queryClient = useQueryClient();
    
    const updateStageMutation = useMutation({
        mutationFn: ({ opportunityId, newStage }) => 
            opportunityApi.updateStage(opportunityId, newStage),
        onMutate: async ({ opportunityId, newStage }) => {
            // Optimistic update
            await queryClient.cancelQueries(['opportunities']);
            const previousData = queryClient.getQueryData(['opportunities']);
            
            queryClient.setQueryData(['opportunities'], old => ({
                ...old,
                items: old.items.map(opp => 
                    opp.id === opportunityId 
                        ? { ...opp, stage: newStage, syncStatus: 'syncing' }
                        : opp
                )
            }));
            
            return { previousData };
        },
        onError: (error, variables, context) => {
            // Rollback
            queryClient.setQueryData(['opportunities'], context.previousData);
            
            showError({
                title: 'Stage-Wechsel fehlgeschlagen',
                message: error.response?.data?.userMessage || 
                    'Die Änderung konnte nicht gespeichert werden',
                action: {
                    label: 'Erneut versuchen',
                    onClick: () => updateStageMutation.mutate(variables)
                }
            });
        },
        onSuccess: (data) => {
            if (data.syncStatus === 'pending') {
                showWarning({
                    title: 'Änderung wird synchronisiert',
                    message: 'Die Änderung wurde lokal gespeichert und wird synchronisiert sobald die Verbindung wiederhergestellt ist'
                });
            }
        }
    });
    
    return (
        <DragDropContext onDragEnd={handleDragEnd}>
            {stages.map(stage => (
                <StageColumn 
                    key={stage.id}
                    stage={stage}
                    opportunities={getOpportunitiesForStage(stage)}
                    renderCard={(opportunity) => (
                        <OpportunityCardWithStatus
                            opportunity={opportunity}
                            onRetrySync={() => retrySyncMutation.mutate(opportunity.id)}
                        />
                    )}
                />
            ))}
        </DragDropContext>
    );
};
```

### FC-003 E-Mail Integration

#### Backend Integration

```java
// EmailService.java
@ApplicationScoped
public class EmailService {
    
    @Inject
    List<EmailProvider> providers; // Multiple providers for fallback
    
    @Inject
    RetryManager retryManager;
    
    @Inject
    NotificationService notificationService;
    
    public CompletionStage<EmailSendResult> sendEmail(EmailMessage message) {
        return retryManager.executeWithRetry(
            () -> sendWithFallback(message, 0),
            RetryPolicy.builder()
                .maxAttempts(3)
                .retryOn(EmailProviderException.class)
                .backoffMultiplier(2.0)
                .build(),
            "email-send-" + message.getId()
        ).handle((result, error) -> {
            if (error != null) {
                // All providers failed
                notificationService.notifyUser(
                    message.getSenderId(),
                    "E-Mail konnte nicht gesendet werden",
                    "Die E-Mail an " + message.getRecipient() + 
                    " konnte nicht zugestellt werden. Bitte versuchen Sie es später erneut."
                );
                
                return EmailSendResult.failed(
                    message.getId(),
                    error.getMessage()
                );
            }
            return result;
        });
    }
    
    private CompletionStage<EmailSendResult> sendWithFallback(
        EmailMessage message, 
        int providerIndex
    ) {
        if (providerIndex >= providers.size()) {
            throw new EmailProviderException("All email providers failed");
        }
        
        var provider = providers.get(providerIndex);
        
        return provider.send(message)
            .exceptionally(error -> {
                Log.warn("Provider {} failed: {}", 
                    provider.getName(), error.getMessage());
                // Try next provider
                return sendWithFallback(message, providerIndex + 1)
                    .toCompletableFuture()
                    .join();
            });
    }
}
```

#### Frontend Integration

```tsx
// EmailComposer.tsx
const EmailComposer: React.FC = () => {
    const [sendStatus, setSendStatus] = useState<SendStatus>('idle');
    const { showNotification } = useNotifications();
    
    const sendEmailMutation = useMutation({
        mutationFn: emailApi.sendEmail,
        onMutate: () => {
            setSendStatus('sending');
        },
        onSuccess: (result) => {
            if (result.status === 'queued') {
                setSendStatus('queued');
                showNotification({
                    type: 'info',
                    title: 'E-Mail wird gesendet',
                    message: 'Die E-Mail wurde in die Warteschlange eingereiht und wird in Kürze versendet'
                });
            } else {
                setSendStatus('sent');
                showNotification({
                    type: 'success',
                    title: 'E-Mail gesendet'
                });
            }
        },
        onError: (error) => {
            setSendStatus('failed');
            showNotification({
                type: 'error',
                title: 'E-Mail konnte nicht gesendet werden',
                message: error.response?.data?.userMessage,
                action: {
                    label: 'Erneut versuchen',
                    onClick: () => sendEmailMutation.mutate(emailData)
                }
            });
        }
    });
    
    return (
        <Box>
            <EmailForm onSubmit={handleSubmit} />
            
            {sendStatus !== 'idle' && (
                <EmailStatusIndicator
                    status={sendStatus}
                    onRetry={() => sendEmailMutation.mutate(emailData)}
                />
            )}
        </Box>
    );
};
```

### FC-009 Contract Renewal Management

#### Backend Integration

```java
// ContractRenewalService.java
@ApplicationScoped
public class ContractRenewalService {
    
    @Inject
    XentralServiceClient xentralClient;
    
    @Inject
    OfflineQueueService offlineQueue;
    
    @Inject
    Event<ContractRenewalEvent> renewalEvent;
    
    @Transactional
    public CompletionStage<ContractRenewalResponse> initiateRenewal(
        UUID contractId,
        RenewalRequest request
    ) {
        var contract = contractRepository.findById(contractId)
            .orElseThrow(() -> new ContractNotFoundException(contractId));
        
        // Create renewal record
        var renewal = ContractRenewal.builder()
            .contract(contract)
            .newEndDate(request.getNewEndDate())
            .status(RenewalStatus.INITIATED)
            .createdAt(Instant.now())
            .build();
        
        renewalRepository.persist(renewal);
        
        // Fire event for audit trail
        renewalEvent.fire(new ContractRenewalEvent(renewal));
        
        // Sync with Xentral
        return xentralClient.createRenewal(contract.getXentralId(), request)
            .handle((xentralResponse, error) -> {
                if (error != null) {
                    // Queue for later processing
                    offlineQueue.enqueue(
                        OfflineOperation.builder()
                            .type(OperationType.CONTRACT_RENEWAL)
                            .entityId(contractId.toString())
                            .payload(toJson(request))
                            .build()
                    );
                    
                    renewal.setStatus(RenewalStatus.PENDING_SYNC);
                    renewal.setSyncError(error.getMessage());
                } else {
                    renewal.setStatus(RenewalStatus.SYNCED);
                    renewal.setXentralId(xentralResponse.getRenewalId());
                }
                
                renewalRepository.persist(renewal);
                return mapper.toResponse(renewal);
            });
    }
    
    @Scheduled(every = "5m")
    void processPendingRenewals() {
        var pendingRenewals = renewalRepository.findPendingSync();
        
        for (var renewal : pendingRenewals) {
            try {
                var xentralResponse = xentralClient.createRenewal(
                    renewal.getContract().getXentralId(),
                    mapper.toRequest(renewal)
                ).toCompletableFuture().get(30, TimeUnit.SECONDS);
                
                renewal.setStatus(RenewalStatus.SYNCED);
                renewal.setXentralId(xentralResponse.getRenewalId());
                renewal.setSyncError(null);
                renewal.setSyncedAt(Instant.now());
                
                Log.info("Successfully synced renewal {} with Xentral", 
                    renewal.getId());
            } catch (Exception e) {
                Log.error("Failed to sync renewal {}: {}", 
                    renewal.getId(), e.getMessage());
                renewal.setSyncRetryCount(renewal.getSyncRetryCount() + 1);
                
                if (renewal.getSyncRetryCount() >= 5) {
                    renewal.setStatus(RenewalStatus.SYNC_FAILED);
                    notificationService.notifyUser(
                        renewal.getCreatedBy(),
                        "Vertragsverlängerung konnte nicht synchronisiert werden",
                        "Die Verlängerung für Vertrag " + 
                        renewal.getContract().getNumber() + 
                        " konnte nicht mit Xentral synchronisiert werden."
                    );
                }
            }
        }
    }
}
```

### FC-011 Pipeline Cockpit Integration

#### Service Health Widget

```tsx
// ServiceHealthWidget.tsx
const ServiceHealthWidget: React.FC = () => {
    const { data: health, isLoading } = useQuery({
        queryKey: ['service-health'],
        queryFn: healthApi.getServiceStatus,
        refetchInterval: 30000 // Every 30 seconds
    });
    
    if (isLoading) return <WidgetSkeleton />;
    
    const criticalServices = ['xentral', 'email', 'keycloak'];
    const allHealthy = criticalServices.every(
        service => health?.services[service]?.status === 'UP'
    );
    
    return (
        <Widget
            title="System Status"
            icon={allHealthy ? <CheckCircleIcon color="success" /> : 
                  <WarningIcon color="warning" />}
        >
            <Grid container spacing={1}>
                {criticalServices.map(service => (
                    <Grid item xs={4} key={service}>
                        <ServiceStatusCard
                            service={service}
                            status={health?.services[service]}
                            compact
                        />
                    </Grid>
                ))}
            </Grid>
            
            {!allHealthy && (
                <Alert severity="warning" sx={{ mt: 1 }}>
                    <AlertTitle>Eingeschränkter Betrieb</AlertTitle>
                    Einige Funktionen sind möglicherweise nicht verfügbar
                </Alert>
            )}
        </Widget>
    );
};
```

### FC-012 Audit Trail Integration

#### Error Event Logging

```java
// AuditEventListener.java
@ApplicationScoped
public class AuditEventListener {
    
    @Inject
    AuditLogService auditService;
    
    public void onErrorTracked(@Observes ErrorTrackedEvent event) {
        auditService.log(
            AuditEntry.builder()
                .eventType(AuditEventType.ERROR_OCCURRED)
                .userId(event.getUserId())
                .entityType("system")
                .entityId(event.getErrorId())
                .details(Map.of(
                    "category", event.getCategory(),
                    "service", event.getService(),
                    "message", event.getMessage()
                ))
                .severity(mapErrorSeverity(event.getCategory()))
                .timestamp(event.getTimestamp())
                .build()
        );
    }
    
    public void onServiceStatusChanged(@Observes ServiceStatusChangedEvent event) {
        auditService.log(
            AuditEntry.builder()
                .eventType(AuditEventType.SERVICE_STATUS_CHANGED)
                .userId("system")
                .entityType("service")
                .entityId(event.getServiceName())
                .details(Map.of(
                    "previousStatus", event.getPreviousStatus(),
                    "newStatus", event.getNewStatus(),
                    "reason", event.getReason()
                ))
                .severity(
                    event.getNewStatus() == ServiceStatus.DOWN ? 
                    AuditSeverity.HIGH : AuditSeverity.INFO
                )
                .timestamp(event.getTimestamp())
                .build()
        );
    }
}
```

### FC-013 Activity & Notes - Offline Support

#### Local-First Note Creation

```tsx
// ActivityNotesEditor.tsx
const ActivityNotesEditor: React.FC<{ entityId: string }> = ({ entityId }) => {
    const { isOnline } = useOfflineMode();
    const [localNotes, setLocalNotes] = useLocalStorage<Note[]>(
        `notes-${entityId}`,
        []
    );
    
    const createNoteMutation = useMutation({
        mutationFn: async (note: CreateNoteRequest) => {
            if (!isOnline) {
                // Save locally
                const localNote: Note = {
                    ...note,
                    id: `local-${Date.now()}`,
                    syncStatus: 'pending',
                    createdAt: new Date()
                };
                setLocalNotes([...localNotes, localNote]);
                return localNote;
            }
            
            return notesApi.createNote(entityId, note);
        },
        onSuccess: (note) => {
            if (note.syncStatus === 'pending') {
                showInfo('Notiz wird synchronisiert sobald die Verbindung wiederhergestellt ist');
            }
        }
    });
    
    // Sync local notes when back online
    useEffect(() => {
        if (isOnline && localNotes.some(n => n.syncStatus === 'pending')) {
            syncLocalNotes();
        }
    }, [isOnline]);
    
    const syncLocalNotes = async () => {
        for (const note of localNotes.filter(n => n.syncStatus === 'pending')) {
            try {
                const synced = await notesApi.createNote(entityId, note);
                setLocalNotes(prev => 
                    prev.map(n => n.id === note.id ? synced : n)
                );
            } catch (error) {
                console.error('Failed to sync note:', error);
            }
        }
    };
    
    return (
        <Box>
            <NotesList 
                notes={[...serverNotes, ...localNotes]}
                renderNote={(note) => (
                    <NoteCard 
                        note={note}
                        showSyncStatus={note.syncStatus === 'pending'}
                    />
                )}
            />
            <NoteEditor onSubmit={createNoteMutation.mutate} />
        </Box>
    );
};
```

## 🔄 Migration Strategy

### Phase 1: Core Infrastructure (1 Tag)
1. Deploy Error Handling Backend Services
2. Integrate Global Exception Mapper
3. Setup Health Check Endpoints
4. Configure Circuit Breakers

### Phase 2: Critical Features (3 Tage)
1. **Tag 1**: M4 Opportunity Pipeline
   - Backend Circuit Breaker Integration
   - Frontend Status Indicators
   - Fallback für Stage Updates

2. **Tag 2**: FC-003 E-Mail Integration
   - Multi-Provider Fallback
   - Retry Logic
   - Status Tracking

3. **Tag 3**: M5 Customer Management
   - Offline Mode Support
   - Local Data Caching
   - Sync Queue

### Phase 3: Supporting Features (2 Tage)
1. **Tag 4**: 
   - FC-009 Contract Renewal Queue
   - FC-011 Health Dashboard
   - FC-012 Audit Integration

2. **Tag 5**:
   - FC-013 Offline Notes
   - FC-015 Better Error Messages
   - Final Testing

## 🧪 Testing Checklist

### Integration Tests

```java
@QuarkusTest
class ErrorHandlingIntegrationTest {
    
    @Test
    void testCircuitBreakerOpensAfterFailures() {
        // Simulate Xentral being down
        mockXentral.stubFor(
            WireMock.get(urlEqualTo("/api/health"))
                .willReturn(serverError())
        );
        
        // Make requests until circuit opens
        for (int i = 0; i < 5; i++) {
            given()
                .when().put("/api/opportunities/123/stage")
                .then().statusCode(200); // Fallback response
        }
        
        // Verify circuit is open
        var health = given()
            .when().get("/health/services")
            .then().extract().as(ServiceHealthStatus.class);
        
        assertThat(health.getServices().get("xentral").getStatus())
            .isEqualTo(ServiceStatus.DOWN);
    }
    
    @Test
    void testOfflineQueueProcessing() {
        // Create operation while offline
        mockXentral.stubFor(
            WireMock.post(urlEqualTo("/api/contracts/renewal"))
                .willReturn(serverError())
        );
        
        var response = given()
            .contentType(ContentType.JSON)
            .body(renewalRequest)
            .when().post("/api/contracts/123/renew")
            .then().statusCode(200)
            .extract().as(ContractRenewalResponse.class);
        
        assertThat(response.getSyncStatus()).isEqualTo(SyncStatus.PENDING);
        
        // Simulate service recovery
        mockXentral.stubFor(
            WireMock.post(urlEqualTo("/api/contracts/renewal"))
                .willReturn(ok())
        );
        
        // Wait for scheduled sync
        await().atMost(Duration.ofMinutes(6))
            .until(() -> {
                var renewal = renewalRepository.findById(response.getId());
                return renewal.getStatus() == RenewalStatus.SYNCED;
            });
    }
}
```

### Frontend Tests

```tsx
describe('Error Handling Integration', () => {
    it('shows status indicator on sync error', async () => {
        server.use(
            rest.put('/api/opportunities/:id/stage', (req, res, ctx) => {
                return res(
                    ctx.status(503),
                    ctx.json({
                        errorId: '123',
                        category: 'SERVICE_UNAVAILABLE',
                        message: 'Xentral nicht erreichbar',
                        userAction: 'Wird automatisch wiederholt'
                    })
                );
            })
        );
        
        render(<OpportunityKanbanBoard />);
        
        // Drag opportunity to new stage
        const opportunity = screen.getByText('Test Opportunity');
        const newStage = screen.getByText('Negotiation');
        
        fireEvent.dragStart(opportunity);
        fireEvent.drop(newStage);
        
        // Check for error indicator
        await waitFor(() => {
            expect(screen.getByTestId('sync-error-icon')).toBeInTheDocument();
            expect(screen.getByText(/Xentral nicht erreichbar/)).toBeInTheDocument();
        });
    });
});
```

## 📊 Monitoring & Alerts

### Key Metrics

```yaml
# prometheus-rules.yml
groups:
  - name: error-handling
    rules:
      - alert: HighErrorRate
        expr: rate(errors_total[5m]) > 0.1
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High error rate detected"
          
      - alert: CircuitBreakerOpen
        expr: circuit_breaker_state{state="open"} == 1
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "Circuit breaker {{ $labels.service }} is open"
          
      - alert: OfflineQueueGrowing
        expr: offline_queue_size > 100
        for: 10m
        labels:
          severity: warning
        annotations:
          summary: "Offline queue has {{ $value }} pending operations"
```

### Dashboards

```json
// grafana-dashboard.json
{
  "panels": [
    {
      "title": "Error Rate by Category",
      "targets": [{
        "expr": "sum(rate(errors_total[5m])) by (category)"
      }]
    },
    {
      "title": "Service Health Status",
      "targets": [{
        "expr": "service_health_status"
      }]
    },
    {
      "title": "Recovery Time",
      "targets": [{
        "expr": "histogram_quantile(0.95, recovery_time_seconds)"
      }]
    }
  ]
}
```

---

**Zusammenfassung:**

Das Error Handling System ist so designed, dass es schrittweise in alle Features integriert werden kann ohne Breaking Changes. Die wichtigsten Features (M4, M5, FC-003) sollten zuerst integriert werden, da sie den größten Business Impact haben.