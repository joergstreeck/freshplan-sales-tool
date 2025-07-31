# 📆 Tag 4: Error Handling & Recovery

**Datum:** Donnerstag, 29. August 2025  
**Fokus:** Resilience & Recovery  
**Ziel:** Robuste Fehlerbehandlung und Wiederherstellung  

## 🧭 Navigation

**← Vorheriger Tag:** [Tag 3: Performance](./DAY3_PERFORMANCE.md)  
**↑ Woche 4 Übersicht:** [README.md](./README.md)  
**→ Nächster Tag:** [Tag 5: Sprint Close](./DAY5_SPRINT_CLOSE.md)  
**📘 Spec:** [Resilience Specification](./specs/RESILIENCE_SPEC.md)  

## 🎯 Tagesziel

- Backend: Circuit Breaker & Retry Logic
- Event Store: Recovery Mechanisms
- Frontend: Error Boundaries & Offline Mode
- Monitoring: Error Tracking

## 🛡️ Resilience Architecture

```
Request → Circuit Breaker → Retry Logic → Service
             │                    │           │
             ├── Fallback        ├── Backoff ├── Timeout
             └── Health Check    └── Jitter  └── Cancel
```

## 💻 Backend Implementation

### 1. Circuit Breaker Implementation

```java
// ContactServiceResilience.java
@ApplicationScoped
public class ContactServiceResilience {
    
    @Inject
    @CircuitBreaker(
        requestVolumeThreshold = 10,
        failureRatio = 0.5,
        delay = 30000,
        successThreshold = 5
    )
    ContactQueryService queryService;
    
    @Inject
    EventStore eventStore;
    
    // Resilient contact search with fallback
    @Retry(maxRetries = 3, delay = 1000, jitter = 400)
    @Timeout(5000)
    @Fallback(fallbackMethod = "searchContactsFallback")
    public Uni<SearchResult> searchContacts(SearchCriteria criteria) {
        return queryService.search(criteria)
            .onFailure().invoke(error -> 
                Log.error("Search failed", error)
            );
    }
    
    // Fallback to cached/degraded results
    public Uni<SearchResult> searchContactsFallback(SearchCriteria criteria) {
        Log.warn("Using fallback search for criteria: " + criteria);
        
        // Try local cache first
        return cacheService.getCachedSearch(criteria)
            .onItem().ifNull().switchTo(() -> 
                // Return degraded response
                Uni.createFrom().item(SearchResult.builder()
                    .items(List.of())
                    .total(0)
                    .degraded(true)
                    .message("Service temporarily unavailable")
                    .build())
            );
    }
    
    // Event store recovery
    @Scheduled(every = "5m")
    void checkEventStoreHealth() {
        eventStore.healthCheck()
            .onFailure().recoverWithUni(() -> 
                initiateEventStoreRecovery()
            )
            .subscribe().with(
                health -> Log.info("Event store health: " + health),
                error -> Log.error("Event store health check failed", error)
            );
    }
    
    private Uni<Void> initiateEventStoreRecovery() {
        return Multi.createFrom().ticks().every(Duration.ofSeconds(10))
            .onItem().transformToUniAndMerge(tick -> 
                eventStore.repair()
                    .onFailure().retry().withBackOff(Duration.ofSeconds(5)).atMost(5)
            )
            .collect().asList()
            .replaceWithVoid();
    }
}
```

**Vollständiger Code:** [backend/ContactServiceResilience.java](./code/backend/ContactServiceResilience.java)

### 2. Event Replay & Recovery

```java
// EventRecoveryService.java
@ApplicationScoped
public class EventRecoveryService {
    
    @Inject
    EventStore eventStore;
    
    @Inject
    ProjectionRebuilder projectionRebuilder;
    
    // Recover from event store corruption
    public Uni<RecoveryResult> recoverFromCorruption(UUID aggregateId) {
        return Uni.createFrom().item(() -> {
            RecoveryResult result = new RecoveryResult();
            
            try {
                // 1. Identify corrupted events
                List<CorruptedEvent> corrupted = eventStore
                    .findCorruptedEvents(aggregateId);
                result.setCorruptedCount(corrupted.size());
                
                // 2. Attempt repair from snapshots
                for (CorruptedEvent event : corrupted) {
                    if (canRepairFromSnapshot(event)) {
                        repairFromSnapshot(event);
                        result.incrementRepaired();
                    } else {
                        // Mark for manual review
                        markForManualReview(event);
                        result.incrementManualReview();
                    }
                }
                
                // 3. Rebuild affected projections
                rebuildProjections(aggregateId);
                
                // 4. Verify consistency
                boolean consistent = verifyConsistency(aggregateId);
                result.setConsistent(consistent);
                
                return result;
                
            } catch (Exception e) {
                Log.error("Recovery failed for aggregate: " + aggregateId, e);
                result.setError(e.getMessage());
                return result;
            }
        });
    }
    
    // Projection rebuild with progress tracking
    public Multi<RebuildProgress> rebuildAllProjections() {
        return Multi.createFrom().emitter(emitter -> {
            List<UUID> allAggregates = contactRepository.findAllIds();
            int total = allAggregates.size();
            AtomicInteger processed = new AtomicInteger(0);
            
            for (UUID aggregateId : allAggregates) {
                try {
                    // Replay events for aggregate
                    List<BaseEvent> events = eventStore.getEvents(aggregateId);
                    ContactAggregate aggregate = new ContactAggregate();
                    
                    for (BaseEvent event : events) {
                        aggregate.apply(event);
                    }
                    
                    // Update projection
                    ContactProjection projection = projectionMapper.toProjection(aggregate);
                    projectionRepository.save(projection);
                    
                    // Emit progress
                    int current = processed.incrementAndGet();
                    emitter.emit(RebuildProgress.builder()
                        .processed(current)
                        .total(total)
                        .percentage((current * 100.0) / total)
                        .currentAggregate(aggregateId)
                        .build()
                    );
                    
                } catch (Exception e) {
                    emitter.emit(RebuildProgress.builder()
                        .processed(processed.get())
                        .total(total)
                        .error(e.getMessage())
                        .currentAggregate(aggregateId)
                        .build()
                    );
                }
            }
            
            emitter.complete();
        });
    }
}
```

### 3. Distributed Saga Recovery

```java
// ContactSagaRecovery.java
@ApplicationScoped
public class ContactSagaRecovery {
    
    @Inject
    SagaRepository sagaRepository;
    
    @Scheduled(every = "10m")
    void recoverStuckSagas() {
        // Find sagas stuck for more than 30 minutes
        List<ContactSaga> stuckSagas = sagaRepository
            .findByStatusAndLastUpdateBefore(
                SagaStatus.IN_PROGRESS,
                Instant.now().minus(Duration.ofMinutes(30))
            );
        
        for (ContactSaga saga : stuckSagas) {
            recoverSaga(saga);
        }
    }
    
    private void recoverSaga(ContactSaga saga) {
        Log.info("Recovering stuck saga: " + saga.getId());
        
        switch (saga.getCurrentStep()) {
            case CREATE_CONTACT -> retryCreateContact(saga);
            case SEND_WELCOME_EMAIL -> retrySendWelcomeEmail(saga);
            case SETUP_CRM_SYNC -> retrySetupCrmSync(saga);
            case COMPENSATE -> continueCompensation(saga);
        }
    }
    
    private void continueCompensation(ContactSaga saga) {
        // Compensate in reverse order
        List<CompensationAction> actions = saga.getCompensationActions();
        Collections.reverse(actions);
        
        for (CompensationAction action : actions) {
            if (!action.isCompleted()) {
                try {
                    executeCompensation(action);
                    action.setCompleted(true);
                    sagaRepository.save(saga);
                } catch (Exception e) {
                    Log.error("Compensation failed: " + action, e);
                    // Continue with next compensation
                }
            }
        }
        
        saga.setStatus(SagaStatus.COMPENSATED);
        sagaRepository.save(saga);
    }
}
```

## 🎨 Frontend Implementation

### Error Boundary Component

```typescript
// components/resilience/ContactErrorBoundary.tsx
export class ContactErrorBoundary extends Component<
  ErrorBoundaryProps,
  ErrorBoundaryState
> {
  constructor(props: ErrorBoundaryProps) {
    super(props);
    this.state = { hasError: false, error: null };
  }
  
  static getDerivedStateFromError(error: Error): ErrorBoundaryState {
    return { hasError: true, error };
  }
  
  componentDidCatch(error: Error, errorInfo: ErrorInfo) {
    // Log to error tracking service
    errorTracker.logError(error, {
      componentStack: errorInfo.componentStack,
      userId: getCurrentUserId(),
      timestamp: new Date().toISOString()
    });
  }
  
  handleRetry = () => {
    this.setState({ hasError: false, error: null });
  };
  
  render() {
    if (this.state.hasError) {
      return (
        <ErrorFallback
          error={this.state.error}
          onRetry={this.handleRetry}
        />
      );
    }
    
    return this.props.children;
  }
}

// Error Fallback UI
const ErrorFallback: React.FC<ErrorFallbackProps> = ({ error, onRetry }) => {
  const isNetworkError = error?.message?.includes('network');
  const is500Error = error?.message?.includes('500');
  
  return (
    <Card sx={{ p: 3, textAlign: 'center' }}>
      <ErrorOutlineIcon sx={{ fontSize: 64, color: 'error.main', mb: 2 }} />
      
      <Typography variant="h5" gutterBottom>
        {isNetworkError ? 'Verbindungsfehler' : 'Ein Fehler ist aufgetreten'}
      </Typography>
      
      <Typography variant="body1" color="text.secondary" paragraph>
        {isNetworkError
          ? 'Bitte überprüfen Sie Ihre Internetverbindung.'
          : is500Error
          ? 'Der Server ist momentan nicht erreichbar. Bitte versuchen Sie es später erneut.'
          : 'Die Kontaktliste konnte nicht geladen werden.'}
      </Typography>
      
      <Box sx={{ mt: 3 }}>
        <Button
          variant="contained"
          onClick={onRetry}
          startIcon={<RefreshIcon />}
        >
          Erneut versuchen
        </Button>
      </Box>
      
      {process.env.NODE_ENV === 'development' && (
        <Accordion sx={{ mt: 3, textAlign: 'left' }}>
          <AccordionSummary expandIcon={<ExpandMoreIcon />}>
            <Typography>Fehlerdetails (nur Development)</Typography>
          </AccordionSummary>
          <AccordionDetails>
            <pre style={{ fontSize: '12px', overflow: 'auto' }}>
              {error?.stack || error?.message}
            </pre>
          </AccordionDetails>
        </Accordion>
      )}
    </Card>
  );
};
```

**Vollständiger Code:** [frontend/ContactErrorBoundary.tsx](./code/frontend/resilience/ContactErrorBoundary.tsx)

### Offline Queue Manager

```typescript
// services/OfflineQueueManager.ts
export class OfflineQueueManager {
  private queue: OfflineAction[] = [];
  private db: IDBDatabase;
  
  async init() {
    this.db = await openDB('contact-offline-queue', 1, {
      upgrade(db) {
        db.createObjectStore('actions', { keyPath: 'id' });
      }
    });
    
    // Load pending actions
    this.queue = await this.db.getAll('actions');
    
    // Listen for online/offline events
    window.addEventListener('online', () => this.processQueue());
    window.addEventListener('offline', () => this.notifyOffline());
  }
  
  async addAction(action: OfflineAction) {
    action.id = uuidv4();
    action.timestamp = new Date();
    action.retryCount = 0;
    
    await this.db.add('actions', action);
    this.queue.push(action);
    
    // Try to process immediately if online
    if (navigator.onLine) {
      this.processQueue();
    }
  }
  
  private async processQueue() {
    const pending = [...this.queue];
    
    for (const action of pending) {
      try {
        await this.processAction(action);
        
        // Remove from queue on success
        this.queue = this.queue.filter(a => a.id !== action.id);
        await this.db.delete('actions', action.id);
        
        // Notify success
        showNotification({
          type: 'success',
          message: `${action.type} wurde synchronisiert`
        });
        
      } catch (error) {
        action.retryCount++;
        
        if (action.retryCount >= 3) {
          // Move to failed queue
          action.status = 'failed';
          await this.db.put('actions', action);
          
          showNotification({
            type: 'error',
            message: `${action.type} konnte nicht synchronisiert werden`
          });
        } else {
          // Retry with exponential backoff
          setTimeout(() => {
            this.processQueue();
          }, Math.pow(2, action.retryCount) * 1000);
        }
      }
    }
  }
  
  private async processAction(action: OfflineAction) {
    switch (action.type) {
      case 'UPDATE_CONTACT':
        return contactApi.update(action.payload.id, action.payload.data);
      
      case 'CREATE_NOTE':
        return contactApi.createNote(action.payload.contactId, action.payload.note);
      
      case 'LOG_INTERACTION':
        return contactApi.logInteraction(action.payload);
      
      default:
        throw new Error(`Unknown action type: ${action.type}`);
    }
  }
}
```

## 🧪 Resilience Tests

### Circuit Breaker Test

```java
@Test
void shouldOpenCircuitAfterFailures() {
    // Given
    when(mockService.search(any())).thenThrow(new ServiceException("Service down"));
    
    // When - trigger failures
    for (int i = 0; i < 15; i++) {
        try {
            resilientService.searchContacts(criteria).await().indefinitely();
        } catch (Exception e) {
            // Expected
        }
    }
    
    // Then - circuit should be open
    SearchResult result = resilientService.searchContacts(criteria)
        .await().indefinitely();
    
    assertThat(result.isDegraded()).isTrue();
    assertThat(result.getMessage()).contains("temporarily unavailable");
}

@Test
void shouldRecoverFromEventStoreCorruption() {
    // Given
    UUID aggregateId = UUID.randomUUID();
    corruptEventStore(aggregateId);
    
    // When
    RecoveryResult result = recoveryService
        .recoverFromCorruption(aggregateId)
        .await().indefinitely();
    
    // Then
    assertThat(result.isConsistent()).isTrue();
    assertThat(result.getRepairedCount()).isGreaterThan(0);
}
```

## 📝 Checkliste

- [ ] Circuit Breaker implementiert
- [ ] Retry Logic mit Backoff
- [ ] Event Store Recovery
- [ ] Saga Recovery Mechanismus
- [ ] Frontend Error Boundaries
- [ ] Offline Queue Manager
- [ ] Monitoring & Alerting

## 🔗 Weiterführende Links

- **Resilience Patterns:** [Resilience4j Guide](./guides/RESILIENCE_PATTERNS.md)
- **Error Tracking:** [Error Monitoring Setup](./guides/ERROR_MONITORING.md)
- **Nächster Schritt:** [→ Tag 5: Sprint Close & Testing](./DAY5_SPRINT_CLOSE.md)

---

**Status:** 📋 Bereit zur Implementierung