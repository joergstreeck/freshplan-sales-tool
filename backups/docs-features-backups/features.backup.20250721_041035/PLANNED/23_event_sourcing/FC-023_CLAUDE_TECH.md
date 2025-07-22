# FC-023 CLAUDE_TECH: Event Sourcing Infrastructure

**CLAUDE TECH** | **Original:** 1784 Zeilen → **Optimiert:** 450 Zeilen (75% Reduktion!)  
**Feature-Typ:** 🔧 BACKEND | **Priorität:** HOCH | **Geschätzter Aufwand:** 3-4 Tage

## ⚡ QUICK-LOAD (30 Sekunden bis produktiv!)

**Event Sourcing Infrastructure für vollständige Audit-Trails und Event-basierte Architekturen**

### 🎯 Das macht es:
- **Unveränderliche Event-Historie**: Jede Geschäftslogik-Änderung wird als Event gespeichert
- **Activity Timeline**: Chronologische Darstellung aller Kundeninteraktionen aus Events
- **Time-Travel Debugging**: Systemzustand zu jedem Zeitpunkt rekonstruierbar  
- **Event-Driven Integrations**: Standardisierte Events für alle Services

### 🚀 ROI:
- **100% Audit-Compliance**: DSGVO und GoBD durch vollständige Event-Historie
- **Real-time Insights**: Live Activity Feed aus Event-Stream für bessere Kundenbetreuung
- **Replay-Fähigkeit**: Events können neu verarbeitet werden für neue Features

### 🏗️ Architektur:
```
Command → Aggregate → Events → Event Store → Event Bus → Projections
    ↓         ↓         ↓          ↓            ↓           ↓
 REST API  Business  Domain    PostgreSQL   CDI Events  Read Models
          Logic     Events    + JSONB      Async       Timeline Views
```

---

## 📋 COPY-PASTE READY RECIPES

### 🔧 Backend Starter Kit

#### 1. Event Store Service:
```java
@ApplicationScoped
@Transactional
public class EventStoreImpl implements EventStore {
    
    @Inject EntityManager em;
    @Inject Event<DomainEvent> eventBus;
    @Inject MeterRegistry metrics;
    
    private final AtomicLong globalPosition = new AtomicLong();
    
    @PostConstruct
    void init() {
        // Initialize global position from database
        Long maxPosition = em.createQuery(
            "SELECT MAX(e.globalPosition) FROM StoredEvent e", Long.class)
            .getSingleResult();
        globalPosition.set(maxPosition != null ? maxPosition : 0);
    }
    
    @Override
    public void append(List<DomainEvent> events) {
        if (events.isEmpty()) return;
        
        // Group by aggregate for version checking
        Map<UUID, List<DomainEvent>> eventsByAggregate = events.stream()
            .collect(Collectors.groupingBy(DomainEvent::getAggregateId));
        
        for (Map.Entry<UUID, List<DomainEvent>> entry : eventsByAggregate.entrySet()) {
            UUID aggregateId = entry.getKey();
            List<DomainEvent> aggregateEvents = entry.getValue();
            
            // Check expected version for optimistic concurrency
            Long currentVersion = getCurrentVersion(aggregateId);
            Long expectedVersion = aggregateEvents.get(0).getVersion() - 1;
            
            if (!currentVersion.equals(expectedVersion)) {
                throw new ConcurrencyException(
                    "Expected version " + expectedVersion + " but was " + currentVersion
                );
            }
            
            // Store events
            for (DomainEvent event : aggregateEvents) {
                StoredEvent stored = new StoredEvent();
                stored.eventId = event.getEventId();
                stored.aggregateId = event.getAggregateId();
                stored.aggregateType = event.getAggregateType();
                stored.version = event.getVersion();
                stored.eventType = event.getClass().getSimpleName();
                stored.eventData = serializeEvent(event);
                stored.metadata = event.getMetadata();
                stored.occurredAt = event.getOccurredAt();
                stored.user = User.findById(event.getUserId());
                stored.globalPosition = globalPosition.incrementAndGet();
                
                em.persist(stored);
                
                // Publish to event bus
                eventBus.fireAsync(event);
                
                // Metrics
                metrics.counter("events.stored",
                    "type", event.getClass().getSimpleName(),
                    "aggregate", event.getAggregateType()
                ).increment();
            }
        }
    }
    
    @Override
    public List<DomainEvent> getEvents(UUID aggregateId, Long fromVersion) {
        List<StoredEvent> storedEvents = StoredEvent.find(
            "aggregateId = ?1 AND version > ?2 ORDER BY version",
            aggregateId, fromVersion
        ).list();
        
        return storedEvents.stream()
                          .map(this::deserializeEvent)
                          .collect(Collectors.toList());
    }
    
    private Long getCurrentVersion(UUID aggregateId) {
        Long version = em.createQuery(
            "SELECT MAX(e.version) FROM StoredEvent e WHERE e.aggregateId = :id",
            Long.class
        ).setParameter("id", aggregateId)
         .getSingleResult();
        return version != null ? version : 0L;
    }
    
    private Map<String, Object> serializeEvent(DomainEvent event) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper.convertValue(event, new TypeReference<Map<String, Object>>() {});
    }
    
    private DomainEvent deserializeEvent(StoredEvent stored) {
        try {
            Class<?> eventClass = Class.forName("de.freshplan.events." + stored.eventType);
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            return (DomainEvent) mapper.convertValue(stored.eventData, eventClass);
        } catch (Exception e) {
            throw new EventDeserializationException(
                "Failed to deserialize event " + stored.eventId, e
            );
        }
    }
}
```

#### 2. Domain Event Base Classes:
```java
// Base Event
public abstract class DomainEvent {
    private final UUID eventId = UUID.randomUUID();
    private final UUID aggregateId;
    private final String aggregateType;
    private final Long version;
    private final LocalDateTime occurredAt = LocalDateTime.now();
    private final UUID userId;
    private final Map<String, Object> metadata = new HashMap<>();
    
    protected DomainEvent(UUID aggregateId, String aggregateType, Long version, UUID userId) {
        this.aggregateId = aggregateId;
        this.aggregateType = aggregateType;
        this.version = version;
        this.userId = userId;
    }
    
    // Getters...
}

// Customer Events
public class CustomerCreatedEvent extends DomainEvent {
    private final UUID customerId;
    private final String name;
    private final String email;
    private final CustomerType type;
    private final BigDecimal creditLimit;
    
    public CustomerCreatedEvent(UUID customerId, String name, String email, 
                               CustomerType type, BigDecimal creditLimit, UUID userId) {
        super(customerId, "Customer", 1L, userId);
        this.customerId = customerId;
        this.name = name;
        this.email = email;
        this.type = type;
        this.creditLimit = creditLimit;
    }
    // Getters...
}

public class CreditLimitChangedEvent extends DomainEvent {
    private final UUID customerId;
    private final BigDecimal oldLimit;
    private final BigDecimal newLimit;
    private final String reason;
    
    public CreditLimitChangedEvent(UUID customerId, BigDecimal oldLimit, 
                                  BigDecimal newLimit, String reason, UUID userId) {
        super(customerId, "Customer", null, userId); // Version set by aggregate
        this.customerId = customerId;
        this.oldLimit = oldLimit;
        this.newLimit = newLimit;
        this.reason = reason;
    }
    // Getters...
}

// Opportunity Events
public class OpportunityStageChangedEvent extends DomainEvent {
    private final UUID opportunityId;
    private final UUID customerId;
    private final String fromStage;
    private final String toStage;
    private final BigDecimal value;
    private final int probability;
    
    public OpportunityStageChangedEvent(UUID opportunityId, UUID customerId,
                                       String fromStage, String toStage,
                                       BigDecimal value, int probability, UUID userId) {
        super(opportunityId, "Opportunity", null, userId);
        this.opportunityId = opportunityId;
        this.customerId = customerId;
        this.fromStage = fromStage;
        this.toStage = toStage;
        this.value = value;
        this.probability = probability;
    }
    // Getters...
}
```

#### 3. Event-Sourced Aggregate Base:
```java
public abstract class EventSourcedAggregate {
    
    private final List<DomainEvent> pendingEvents = new ArrayList<>();
    private Long version = 0L;
    protected UUID id;
    
    protected void raiseEvent(DomainEvent event) {
        // Set version on event
        event.setVersion(version + 1);
        pendingEvents.add(event);
        apply(event);
        version++;
    }
    
    public List<DomainEvent> getPendingEvents() {
        return new ArrayList<>(pendingEvents);
    }
    
    public void markEventsAsCommitted() {
        pendingEvents.clear();
    }
    
    public void loadFromHistory(List<DomainEvent> events) {
        events.forEach(event -> {
            apply(event);
            version = event.getVersion();
        });
    }
    
    protected abstract void apply(DomainEvent event);
    
    public Long getVersion() { return version; }
    public UUID getId() { return id; }
}

// Customer Aggregate with Event Sourcing
public class Customer extends EventSourcedAggregate {
    
    private String name;
    private String email;
    private CustomerType type;
    private BigDecimal creditLimit;
    private boolean active;
    
    // Constructor for new customers
    public Customer(CreateCustomerCommand command) {
        CustomerCreatedEvent event = new CustomerCreatedEvent(
            command.customerId, command.name, command.email,
            command.type, command.creditLimit, command.userId
        );
        raiseEvent(event);
    }
    
    // Constructor for event sourcing
    public Customer(UUID customerId, List<DomainEvent> events) {
        this.id = customerId;
        loadFromHistory(events);
    }
    
    // Business methods
    public void updateCreditLimit(BigDecimal newLimit, String reason, UUID userId) {
        if (newLimit.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Credit limit cannot be negative");
        }
        
        CreditLimitChangedEvent event = new CreditLimitChangedEvent(
            id, creditLimit, newLimit, reason, userId
        );
        raiseEvent(event);
    }
    
    @Override
    protected void apply(DomainEvent event) {
        switch (event) {
            case CustomerCreatedEvent e -> {
                this.id = e.getCustomerId();
                this.name = e.getName();
                this.email = e.getEmail();
                this.type = e.getType();
                this.creditLimit = e.getCreditLimit();
                this.active = true;
            }
            case CreditLimitChangedEvent e -> {
                this.creditLimit = e.getNewLimit();
            }
            case CustomerDeactivatedEvent e -> {
                this.active = false;
            }
            default -> {
                Log.warn("Unknown event type: " + event.getClass().getSimpleName());
            }
        }
    }
}
```

#### 4. Event Handlers for Projections:
```java
@ApplicationScoped
public class CustomerProjectionHandler {
    
    @Inject EntityManager em;
    @Inject CustomerTimelineService timelineService;
    
    void onCustomerCreated(@ObservesAsync CustomerCreatedEvent event) {
        // Update read model
        CustomerReadModel model = new CustomerReadModel();
        model.customerId = event.getCustomerId();
        model.name = event.getName();
        model.email = event.getEmail();
        model.type = event.getType();
        model.creditLimit = event.getCreditLimit();
        model.active = true;
        model.createdAt = event.getOccurredAt();
        model.lastModifiedAt = event.getOccurredAt();
        em.persist(model);
        
        // Update timeline
        timelineService.addEntry(
            event.getCustomerId(),
            "customer_created",
            "Kunde angelegt",
            "Neuer Kunde " + event.getName() + " wurde angelegt",
            event.getUserId(),
            event.getOccurredAt()
        );
    }
    
    void onCreditLimitChanged(@ObservesAsync CreditLimitChangedEvent event) {
        CustomerReadModel model = em.find(CustomerReadModel.class, event.getCustomerId());
        if (model != null) {
            BigDecimal oldLimit = model.creditLimit;
            model.creditLimit = event.getNewLimit();
            model.lastModifiedAt = event.getOccurredAt();
            em.merge(model);
            
            // Timeline entry
            timelineService.addEntry(
                event.getCustomerId(),
                "credit_limit_changed",
                "Kreditlimit geändert",
                String.format("Kreditlimit von %s auf %s geändert. Grund: %s",
                    formatCurrency(oldLimit),
                    formatCurrency(event.getNewLimit()),
                    event.getReason()
                ),
                event.getUserId(),
                event.getOccurredAt()
            );
        }
    }
}
```

#### 5. Database Schema:
```sql
-- V7.0__create_event_sourcing_tables.sql

-- Main Event Store
CREATE TABLE domain_events (
    event_id UUID PRIMARY KEY,
    aggregate_id UUID NOT NULL,
    aggregate_type VARCHAR(100) NOT NULL,
    version BIGINT NOT NULL,
    event_type VARCHAR(255) NOT NULL,
    event_data JSONB NOT NULL,
    metadata JSONB DEFAULT '{}',
    occurred_at TIMESTAMP NOT NULL,
    user_id UUID REFERENCES users(id),
    global_position BIGSERIAL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE(aggregate_id, version)
);

-- Indexes for efficient querying
CREATE INDEX idx_domain_events_aggregate ON domain_events(aggregate_id, version);
CREATE INDEX idx_domain_events_type ON domain_events(event_type);
CREATE INDEX idx_domain_events_occurred ON domain_events(occurred_at);
CREATE INDEX idx_domain_events_global_position ON domain_events(global_position);

-- Customer Timeline View (Projection)
CREATE TABLE customer_timeline_view (
    entry_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    event_category VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    details JSONB DEFAULT '{}',
    performed_by_id UUID REFERENCES users(id),
    occurred_at TIMESTAMP NOT NULL,
    icon_type VARCHAR(50),
    color_scheme VARCHAR(50),
    
    CHECK (event_category IN ('interaction', 'transaction', 'system', 'integration'))
);

CREATE INDEX idx_timeline_customer_date ON customer_timeline_view(customer_id, occurred_at DESC);

-- Event Subscriptions for Projections
CREATE TABLE event_subscriptions (
    subscription_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    subscription_name VARCHAR(255) NOT NULL UNIQUE,
    event_types TEXT NOT NULL, -- Comma-separated
    last_processed_position BIGINT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    last_processed_at TIMESTAMP,
    
    CHECK (status IN ('ACTIVE', 'PAUSED', 'FAILED'))
);
```

#### 6. REST API:
```java
@Path("/api/events")
@Authenticated
public class EventResource {
    
    @Inject EventStore eventStore;
    @Inject EventQueryService queryService;
    
    @GET
    @Path("/aggregate/{aggregateId}")
    @RolesAllowed({"admin", "manager"})
    public List<EventDTO> getAggregateEvents(
            @PathParam("aggregateId") UUID aggregateId,
            @QueryParam("from") @DefaultValue("0") Long fromVersion,
            @QueryParam("limit") @DefaultValue("100") int limit) {
        return eventStore.getEvents(aggregateId, fromVersion).stream()
                        .limit(limit)
                        .map(EventDTO::from)
                        .collect(Collectors.toList());
    }
    
    @GET
    @Path("/timeline/{entityType}/{entityId}")
    @RolesAllowed({"admin", "manager", "sales"})
    public List<TimelineEventDTO> getTimeline(
            @PathParam("entityType") String entityType,
            @PathParam("entityId") UUID entityId,
            @QueryParam("days") @DefaultValue("30") int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return queryService.getTimelineEvents(entityType, entityId, since);
    }
    
    @POST
    @Path("/replay")
    @RolesAllowed("admin")
    public Response replayEvents(@Valid ReplayRequest request) {
        // Trigger event replay for projections
        eventStore.replayEvents(
            request.streamName,
            request.fromPosition,
            request.toPosition,
            request.projectionName
        );
        return Response.accepted().build();
    }
}
```

### 🎨 Frontend Starter Kit

#### 1. Activity Timeline Component:
```typescript
export const ActivityTimeline: React.FC<{
  entityType: 'customer' | 'opportunity';
  entityId: string;
}> = ({ entityType, entityId }) => {
  const { timeline, isLoading, refresh } = useTimeline({
    entityType, entityId, days: 90
  });

  const groupedEvents = useMemo(() => {
    const groups = new Map<string, TimelineEvent[]>();
    
    timeline.forEach(event => {
      const date = new Date(event.occurredAt).toLocaleDateString('de-DE');
      if (!groups.has(date)) {
        groups.set(date, []);
      }
      groups.get(date)!.push(event);
    });
    
    return Array.from(groups.entries()).map(([date, events]) => ({
      date,
      events: events.sort((a, b) => 
        new Date(b.occurredAt).getTime() - new Date(a.occurredAt).getTime()
      ),
    }));
  }, [timeline]);

  if (isLoading) {
    return (
      <Box>
        {[1, 2, 3].map(i => (
          <Skeleton key={i} variant="rectangular" height={100} sx={{ mb: 2 }} />
        ))}
      </Box>
    );
  }

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 2 }}>
        <Typography variant="h6">Aktivitäten</Typography>
        <Tooltip title="Aktualisieren">
          <IconButton size="small" onClick={() => refresh()}>
            <RefreshIcon />
          </IconButton>
        </Tooltip>
      </Box>

      {groupedEvents.length === 0 ? (
        <Typography color="text.secondary">
          Noch keine Aktivitäten vorhanden
        </Typography>
      ) : (
        <Timeline position="right">
          {groupedEvents.map(({ date, events }) => (
            <React.Fragment key={date}>
              <TimelineItem>
                <TimelineOppositeContent sx={{ display: 'none' }} />
                <TimelineSeparator>
                  <TimelineDot variant="outlined" />
                  <TimelineConnector />
                </TimelineSeparator>
                <TimelineContent>
                  <Typography variant="subtitle2" color="text.secondary">
                    {date}
                  </Typography>
                </TimelineContent>
              </TimelineItem>
              
              {events.map((event) => (
                <TimelineItem key={event.id}>
                  <TimelineOppositeContent
                    sx={{ m: 'auto 0', flex: 0.2 }}
                    align="right"
                    variant="body2"
                    color="text.secondary"
                  >
                    {formatDistanceToNow(new Date(event.occurredAt), {
                      addSuffix: true, locale: de
                    })}
                  </TimelineOppositeContent>
                  
                  <TimelineSeparator>
                    <TimelineConnector />
                    <TimelineDot color={getEventColor(event.type)}>
                      {getEventIcon(event.type)}
                    </TimelineDot>
                    <TimelineConnector />
                  </TimelineSeparator>
                  
                  <TimelineContent sx={{ py: '12px', px: 2 }}>
                    <Card variant="outlined">
                      <CardContent>
                        <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                          <Typography variant="subtitle1" component="span">
                            {event.title}
                          </Typography>
                          {event.category && (
                            <Chip
                              label={event.category}
                              size="small"
                              sx={{ ml: 1 }}
                              color={getCategoryColor(event.category)}
                            />
                          )}
                        </Box>
                        
                        {event.description && (
                          <Typography variant="body2" color="text.secondary">
                            {event.description}
                          </Typography>
                        )}
                        
                        {event.user && (
                          <Box sx={{ display: 'flex', alignItems: 'center', mt: 1 }}>
                            <Avatar sx={{ width: 24, height: 24, mr: 1 }} src={event.user.avatar}>
                              {event.user.name[0]}
                            </Avatar>
                            <Typography variant="caption" color="text.secondary">
                              {event.user.name}
                            </Typography>
                          </Box>
                        )}
                      </CardContent>
                    </Card>
                  </TimelineContent>
                </TimelineItem>
              ))}
            </React.Fragment>
          ))}
        </Timeline>
      )}
    </Box>
  );
};
```

#### 2. React Query Hooks:
```typescript
// useTimeline.ts
export const useTimeline = ({ entityType, entityId, days = 30 }) => {
  const { data, error, mutate } = useSWR<TimelineEvent[]>(
    `/api/events/timeline/${entityType}/${entityId}?days=${days}`,
    apiClient.get,
    { refreshInterval: 10000 } // Refresh every 10 seconds
  );

  // Subscribe to real-time updates
  useEventStream({
    streamName: `${entityType}-${entityId}`,
    onEvent: (event) => {
      // Optimistically update timeline
      mutate(current => {
        if (!current) return [mapToTimelineEvent(event)];
        return [mapToTimelineEvent(event), ...current];
      }, false);
    },
  });

  return {
    timeline: data || [],
    isLoading: !error && !data,
    isError: error,
    refresh: mutate,
  };
};

// useEventStream.ts
export const useEventStream = ({ streamName, fromPosition = 0, onEvent }) => {
  const [events, setEvents] = useState<DomainEvent[]>([]);
  const [isConnected, setIsConnected] = useState(false);
  const eventSourceRef = useRef<EventSource | null>(null);

  useEffect(() => {
    const token = localStorage.getItem('auth_token');
    const url = `/api/events/stream/${streamName}?from=${fromPosition}`;
    
    const eventSource = new EventSourcePolyfill(url, {
      headers: { 'Authorization': `Bearer ${token}` }
    });

    eventSourceRef.current = eventSource;

    eventSource.onopen = () => setIsConnected(true);
    eventSource.onmessage = (event) => {
      try {
        const domainEvent = JSON.parse(event.data) as DomainEvent;
        setEvents(prev => [...prev, domainEvent]);
        onEvent?.(domainEvent);
      } catch (err) {
        console.error('Failed to parse event:', err);
      }
    };

    eventSource.onerror = () => {
      setIsConnected(false);
      // Reconnect after 5 seconds
      setTimeout(() => {
        if (eventSourceRef.current?.readyState === EventSource.CLOSED) {
          eventSource.close();
        }
      }, 5000);
    };

    return () => {
      eventSource.close();
      eventSourceRef.current = null;
    };
  }, [streamName, fromPosition]);

  return { events, isConnected, clearEvents: () => setEvents([]) };
};
```

---

## 📊 IMPLEMENTIERUNGSPLAN

### Phase 1: Core Infrastructure in M4 (1.5 Tage)
1. **Event Store Basics** (0.5 Tage)
   - Database Schema für Events
   - DomainEvent Base Classes
   - EventStore Service Interface
   - Event Persistence

2. **First Projections** (1 Tag)  
   - Opportunity Aggregate mit Events
   - OpportunityReadModel Projection
   - Timeline Projection für Activities
   - Integration in M4 Pipeline UI

### Phase 2: Extended Features (2 Tage)
1. **Advanced Event Processing** (1 Tag)
   - Subscription Management
   - Projection Rebuild Capability
   - Event Stream API (SSE)

2. **UI Components** (1 Tag)
   - Activity Timeline Component
   - Event Stream Monitor
   - Audit Trail Viewer
   - Integration Tests

---

## 🎯 BUSINESS VALUE

### ROI Metriken:
- **100% Audit-Compliance**: DSGVO und GoBD durch vollständige Event-Historie
- **Real-time Insights**: Live Activity Feed aus Event-Stream für bessere Kundenbetreuung  
- **Time-Travel Debugging**: Systemzustand zu jedem Zeitpunkt rekonstruierbar

### Technische Vorteile:
- **Eventual Consistency**: Entkopplung durch asynchrone Event-Verarbeitung
- **Read/Write Separation**: Optimierte Read Models für Performance
- **Replay-Fähigkeit**: Events können neu verarbeitet werden für neue Features
- **Integration Foundation**: Standardisiertes Event-Format für alle Services

---

## 🔗 INTEGRATION POINTS

### Dependencies:
- **M4 Opportunity Pipeline**: Erste Event Implementation (1.5 Tage integriert)
- **PostgreSQL**: Event Store in JSONB für flexible Schema-Evolution

### Enables:
- **FC-014 Activity Timeline**: Hauptkonsument aller Events
- **FC-025 DSGVO Compliance**: Audit Trail aus Events
- **FC-026 Analytics Platform**: Event Analytics und Metrics
- **FC-027 Magic Moments**: Pattern Detection aus Event-Stream

---

## ⚠️ WICHTIGE ENTSCHEIDUNGEN

1. **Event Store**: Embedded in PostgreSQL mit JSONB (einfachere Operations vs. EventStore DB)
2. **Event Serialization**: JSON mit Schema Registry Light (Developer Experience vs. Protobuf)  
3. **Snapshot Strategy**: Automatisch alle 100 Events (Performance vs. Storage)
4. **Eventual Consistency**: Klare UI-Indikatoren für Processing-Status

---

**Status:** Ready for Implementation | **Phase 1:** Database Schema in M4 | **Next:** Opportunity Events definieren