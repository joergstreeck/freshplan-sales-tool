# FC-003: E-Mail Sync Engine - Detailspezifikation

## Synchronisations-Architektur

### 1. Sync Engine Core

```java
@ApplicationScoped
public class EmailSyncEngine {
    
    @Inject EmailProviderFactory providerFactory;
    @Inject EmailSyncQueue syncQueue;
    @Inject EmailRepository emailRepository;
    
    @Scheduled(every = "5m")
    void scheduleSyncJobs() {
        List<EmailAccount> activeAccounts = 
            emailAccountRepository.findActive();
            
        for (EmailAccount account : activeAccounts) {
            if (shouldSync(account)) {
                syncQueue.enqueue(new SyncJob(account.getId()));
            }
        }
    }
    
    private boolean shouldSync(EmailAccount account) {
        // Intelligent sync scheduling based on activity
        Instant lastSync = account.getLastSyncAt();
        Instant now = Instant.now();
        
        // Häufigere Syncs während Geschäftszeiten
        if (isBusinessHours()) {
            return Duration.between(lastSync, now).toMinutes() >= 5;
        }
        
        return Duration.between(lastSync, now).toMinutes() >= 30;
    }
}
```

### 2. Delta Sync Implementation

```java
@ApplicationScoped
public class DeltaSyncStrategy {
    
    public SyncResult performDeltaSync(EmailAccount account, 
                                       EmailConnection connection) {
        SyncState lastState = getSyncState(account.getId());
        
        return switch (account.getProvider()) {
            case GMAIL -> syncGmailDelta(connection, lastState);
            case EXCHANGE -> syncExchangeDelta(connection, lastState);
            case IMAP_SMTP -> syncImapDelta(connection, lastState);
        };
    }
    
    private SyncResult syncGmailDelta(EmailConnection connection, 
                                      SyncState lastState) {
        GmailConnection gmail = (GmailConnection) connection;
        
        // Gmail History API für effiziente Delta-Syncs
        List<History> changes = gmail.users().history()
            .list("me")
            .setStartHistoryId(lastState.getHistoryId())
            .execute()
            .getHistory();
            
        return processGmailChanges(changes);
    }
}
```

### 3. Message Processing Pipeline

```java
@ApplicationScoped
public class EmailMessageProcessor {
    
    @Inject CustomerMatcher customerMatcher;
    @Inject OpportunityMatcher opportunityMatcher;
    @Inject EmailParser emailParser;
    @Inject EventBus eventBus;
    
    public ProcessedEmail processIncomingEmail(RawEmailMessage raw) {
        // 1. Parse E-Mail
        ParsedEmail parsed = emailParser.parse(raw);
        
        // 2. Customer Matching
        Optional<Customer> customer = customerMatcher
            .matchByEmail(parsed.getFrom());
            
        // 3. Thread Detection
        Optional<EmailThread> thread = findThread(parsed);
        
        // 4. Opportunity Matching
        Optional<Opportunity> opportunity = opportunityMatcher
            .matchByCustomerAndContext(customer, parsed);
            
        // 5. Create/Update Database Record
        EmailMessage message = createEmailMessage(parsed, 
            customer.map(Customer::getId).orElse(null),
            opportunity.map(Opportunity::getId).orElse(null),
            thread.map(EmailThread::getId).orElse(null)
        );
        
        // 6. Trigger Events
        eventBus.publish(new EmailReceivedEvent(message));
        
        return new ProcessedEmail(message, customer, opportunity);
    }
}
```

### 4. Customer & Opportunity Matching

```java
@ApplicationScoped
public class CustomerMatcher {
    
    public Optional<Customer> matchByEmail(String email) {
        // 1. Direkter Match über Kontakt-E-Mail
        Optional<Customer> direct = customerRepository
            .findByContactEmail(email);
        if (direct.isPresent()) return direct;
        
        // 2. Domain-basierter Match
        String domain = extractDomain(email);
        return customerRepository.findByEmailDomain(domain);
    }
}

@ApplicationScoped
public class OpportunityMatcher {
    
    public Optional<Opportunity> matchByCustomerAndContext(
            Optional<Customer> customer, ParsedEmail email) {
        
        if (customer.isEmpty()) return Optional.empty();
        
        // 1. Check Subject für Opportunity-Referenz
        Optional<String> oppRef = extractOpportunityRef(email.getSubject());
        if (oppRef.isPresent()) {
            return opportunityRepository.findByReference(oppRef.get());
        }
        
        // 2. Check aktive Opportunities
        List<Opportunity> activeOpps = opportunityRepository
            .findActiveByCustomerId(customer.get().getId());
            
        // 3. Context Matching
        return findBestMatch(activeOpps, email);
    }
}
```

### 5. Queue-basierte Verarbeitung

```java
@ApplicationScoped
public class EmailSyncQueue {
    
    @Inject Vertx vertx;
    
    private final EventBus eventBus;
    
    public void enqueue(SyncJob job) {
        eventBus.send("email.sync.queue", job);
    }
    
    @ConsumeEvent("email.sync.queue")
    public CompletionStage<Void> processSyncJob(SyncJob job) {
        return vertx.executeBlocking(promise -> {
            try {
                performSync(job);
                promise.complete();
            } catch (Exception e) {
                handleSyncError(job, e);
                promise.fail(e);
            }
        });
    }
}
```

### 6. Conflict Resolution

```java
@ApplicationScoped
public class ConflictResolver {
    
    public EmailMessage resolveConflict(EmailMessage local, 
                                        EmailMessage remote) {
        // Server-Version gewinnt bei Konflikten
        if (remote.getModifiedAt().isAfter(local.getModifiedAt())) {
            return mergeChanges(local, remote);
        }
        
        // Lokale Änderungen beibehalten, wenn neuer
        return local;
    }
    
    private EmailMessage mergeChanges(EmailMessage local, 
                                      EmailMessage remote) {
        return EmailMessage.builder()
            .id(local.getId())
            .messageId(remote.getMessageId())
            .subject(remote.getSubject())
            .body(remote.getBody())
            // Lokale Zuordnungen beibehalten
            .customerId(local.getCustomerId())
            .opportunityId(local.getOpportunityId())
            // Remote-Status übernehmen
            .isRead(remote.isRead())
            .labels(remote.getLabels())
            .build();
    }
}
```

### 7. Performance & Batching

```java
@ApplicationScoped
public class BatchSyncOptimizer {
    
    private static final int BATCH_SIZE = 100;
    
    public void syncMessages(EmailConnection connection, 
                             SyncState state) {
        // Batch-Verarbeitung für bessere Performance
        connection.getMessages(state.getLastSyncedId(), BATCH_SIZE)
            .buffer(BATCH_SIZE)
            .parallel(4) // Parallel Processing
            .runOn(Infrastructure.getDefaultWorkerPool())
            .transformToUniAndMerge(batch -> 
                processBatch(batch)
                    .onFailure()
                    .retry()
                    .atMost(3)
            )
            .collect()
            .asList()
            .await()
            .indefinitely();
    }
}
```

### 8. Sync State Management

```java
@Entity
public class EmailSyncState {
    @Id UUID accountId;
    
    String lastSyncToken;      // Provider-spezifisch
    Long lastHistoryId;        // Gmail
    String deltaLink;          // Exchange
    Instant lastSyncAt;
    Instant lastFullSyncAt;
    
    @ElementCollection
    Map<String, String> providerMetadata;
    
    // Fehler-Tracking
    Integer consecutiveErrors;
    String lastError;
    Instant lastErrorAt;
}

@ApplicationScoped
@Transactional
public class SyncStateManager {
    
    public void updateSyncState(UUID accountId, 
                                SyncResult result) {
        EmailSyncState state = findOrCreate(accountId);
        
        if (result.isSuccess()) {
            state.setLastSyncAt(Instant.now());
            state.setLastSyncToken(result.getNextToken());
            state.setConsecutiveErrors(0);
        } else {
            state.setConsecutiveErrors(state.getConsecutiveErrors() + 1);
            state.setLastError(result.getError());
            state.setLastErrorAt(Instant.now());
            
            // Disable account after too many errors
            if (state.getConsecutiveErrors() >= 5) {
                disableAccount(accountId);
            }
        }
        
        syncStateRepository.persist(state);
    }
}
```

### 9. Real-time Push Notifications (Gmail/Exchange)

```java
@Path("/api/email/webhook")
public class EmailWebhookResource {
    
    @POST
    @Path("/gmail/{accountId}")
    public Response handleGmailPush(@PathParam("accountId") UUID accountId,
                                    GmailNotification notification) {
        // Verify notification authenticity
        if (!verifyGmailNotification(notification)) {
            return Response.status(401).build();
        }
        
        // Trigger immediate sync for account
        emailSyncEngine.triggerSync(accountId);
        
        return Response.ok().build();
    }
    
    @POST
    @Path("/exchange/{accountId}")
    public Response handleExchangeWebhook(@PathParam("accountId") UUID accountId,
                                          ExchangeNotification notification) {
        // Process Exchange streaming notification
        processExchangeNotification(accountId, notification);
        
        return Response.ok().build();
    }
}
```