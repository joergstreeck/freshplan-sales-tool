# FC-014: Activity Timeline 📅 CLAUDE_TECH

**Feature Code:** FC-014  
**Optimiert für:** Claude's 30-Sekunden-Produktivität  
**Original:** 798 Zeilen → **Optimiert:** ~450 Zeilen (44% Reduktion)

## 🎯 QUICK-LOAD: Sofort produktiv in 30 Sekunden!

### Was macht FC-014?
**Chronologische 360°-Kundenhistorie mit automatischem Activity-Tracking und Smart Follow-ups**

### Die 4 Kern-Features:
1. **Unified Timeline** → Alle Interaktionen (Email, Call, Meeting, Note) in einer Ansicht
2. **Auto-Tracking** → Events aus Email/Phone/Calendar werden automatisch erfasst
3. **Smart Actions** → Kontextuelle Follow-up Buttons basierend auf Activity-Typ
4. **Real-time Sync** → 30-Sekunden Polling für Team-Updates (D2-Decision)

### Sofort starten:
```bash
# Backend: Event System Setup
cd backend
./mvnw quarkus:add-extension -Dextensions="quarkus-scheduler,quarkus-cache"

# Frontend: Timeline Components
cd frontend
npm install react-intersection-observer date-fns react-window
```

---

## 📦 1. BACKEND: Copy-paste Recipes

### 1.1 Activity Domain Model (5 Minuten)
```java
@Entity
@Table(name = "activities")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "activity_type")
public abstract class Activity {
    @Id @GeneratedValue
    private UUID id;
    
    @ManyToOne(optional = false)
    private Customer customer;
    
    @ManyToOne
    private User user;
    
    private LocalDateTime occurredAt;
    
    @Column(length = 500)
    private String description;
    
    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata;
    
    @Enumerated(EnumType.STRING)
    private ActivityStatus status = ActivityStatus.COMPLETED;
    
    private LocalDateTime createdAt = LocalDateTime.now();
    
    // Template method for type-specific rendering
    public abstract ActivityType getType();
    public abstract String getIconName();
    public abstract List<FollowUpAction> getSuggestedActions();
}

// Concrete Activity Types
@Entity
@DiscriminatorValue("EMAIL")
public class EmailActivity extends Activity {
    private String subject;
    private String from;
    private String to;
    private boolean isInbound;
    
    @Override
    public ActivityType getType() {
        return ActivityType.EMAIL;
    }
    
    @Override
    public String getIconName() {
        return isInbound ? "email-inbox" : "email-outbox";
    }
    
    @Override
    public List<FollowUpAction> getSuggestedActions() {
        if (isInbound && getStatus() == ActivityStatus.PENDING) {
            return List.of(
                new FollowUpAction("reply", "Antworten", "/email/reply/" + getId()),
                new FollowUpAction("forward", "Weiterleiten", "/email/forward/" + getId())
            );
        }
        return List.of();
    }
}

@Entity
@DiscriminatorValue("CALL")
public class CallActivity extends Activity {
    private String phoneNumber;
    private Integer duration; // seconds
    private CallDirection direction;
    
    @Override
    public List<FollowUpAction> getSuggestedActions() {
        return List.of(
            new FollowUpAction("note", "Notiz hinzufügen", null),
            new FollowUpAction("schedule", "Follow-up planen", null)
        );
    }
}

@Entity
@DiscriminatorValue("MEETING")
public class MeetingActivity extends Activity {
    private LocalDateTime scheduledFor;
    private Integer durationMinutes;
    private String location;
    
    @ElementCollection
    private List<String> participants;
    
    @Override
    public List<FollowUpAction> getSuggestedActions() {
        if (scheduledFor.isBefore(LocalDateTime.now())) {
            return List.of(
                new FollowUpAction("summary", "Zusammenfassung erstellen", null),
                new FollowUpAction("tasks", "Aufgaben erstellen", null)
            );
        }
        return List.of(
            new FollowUpAction("reschedule", "Verschieben", null),
            new FollowUpAction("cancel", "Absagen", null)
        );
    }
}
```

### 1.2 Activity Collection & Processing (10 Minuten)
```java
@ApplicationScoped
public class ActivityCollectorService {
    
    @Inject ActivityRepository activityRepository;
    @Inject EventBus eventBus;
    
    // Email Activity Collection
    public void onEmailReceived(@Observes EmailReceivedEvent event) {
        EmailActivity activity = new EmailActivity();
        activity.setCustomer(findCustomerByEmail(event.getFrom()));
        activity.setSubject(event.getSubject());
        activity.setFrom(event.getFrom());
        activity.setTo(event.getTo());
        activity.setInbound(true);
        activity.setOccurredAt(event.getReceivedAt());
        activity.setDescription("E-Mail erhalten: " + event.getSubject());
        
        activityRepository.persist(activity);
        
        // Trigger follow-up suggestions
        eventBus.publish(new ActivityCreatedEvent(activity));
    }
    
    // Phone Activity Collection
    public void onCallCompleted(@Observes CallCompletedEvent event) {
        CallActivity activity = new CallActivity();
        activity.setCustomer(findCustomerByPhone(event.getPhoneNumber()));
        activity.setUser(event.getUser());
        activity.setPhoneNumber(event.getPhoneNumber());
        activity.setDuration(event.getDuration());
        activity.setDirection(event.getDirection());
        activity.setOccurredAt(event.getStartTime());
        activity.setDescription(String.format(
            "%s Anruf (%d Min)", 
            event.getDirection() == CallDirection.INBOUND ? "Eingehender" : "Ausgehender",
            event.getDuration() / 60
        ));
        
        activityRepository.persist(activity);
    }
    
    // Manual Activity Creation
    @Transactional
    public Activity createManualActivity(CreateActivityRequest request) {
        Activity activity = switch (request.getType()) {
            case NOTE -> createNoteActivity(request);
            case TASK -> createTaskActivity(request);
            case MEETING -> createMeetingActivity(request);
            default -> throw new IllegalArgumentException("Unsupported type");
        };
        
        activityRepository.persist(activity);
        return activity;
    }
}

// Activity Enrichment Service
@ApplicationScoped
public class ActivityEnricherService {
    
    @Inject UserService userService;
    @Inject CustomerService customerService;
    
    public EnrichedActivity enrich(Activity activity) {
        return EnrichedActivity.builder()
            .activity(activity)
            .userDetails(userService.getDetails(activity.getUser()))
            .customerContext(getCustomerContext(activity.getCustomer()))
            .relatedActivities(findRelatedActivities(activity))
            .suggestedActions(activity.getSuggestedActions())
            .build();
    }
    
    private CustomerContext getCustomerContext(Customer customer) {
        return CustomerContext.builder()
            .lastContactDays(calculateDaysSinceLastContact(customer))
            .openOpportunities(countOpenOpportunities(customer))
            .totalRevenue(calculateTotalRevenue(customer))
            .relationshipScore(calculateRelationshipScore(customer))
            .build();
    }
    
    private List<Activity> findRelatedActivities(Activity activity) {
        // Find activities in same thread (e.g., email chain)
        if (activity instanceof EmailActivity email) {
            String subject = normalizeSubject(email.getSubject());
            return activityRepository.find(
                "customer = ?1 AND type = ?2 AND subject LIKE ?3",
                activity.getCustomer(),
                ActivityType.EMAIL,
                "%" + subject + "%"
            ).list();
        }
        return List.of();
    }
}
```

### 1.3 Timeline API with Pagination (5 Minuten)
```java
@Path("/api/activities")
@Authenticated
public class ActivityResource {
    
    @Inject ActivityService activityService;
    @Inject ActivityEnricherService enricher;
    
    @GET
    @Path("/timeline/{customerId}")
    @CacheResult(cacheName = "timeline-cache")
    public TimelineResponse getTimeline(
            @PathParam("customerId") UUID customerId,
            @QueryParam("from") LocalDateTime from,
            @QueryParam("to") LocalDateTime to,
            @QueryParam("types") List<ActivityType> types,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("50") int size) {
        
        // Build query with filters
        PanacheQuery<Activity> query = activityService
            .findByCustomer(customerId)
            .filter("occurredAt BETWEEN ?1 AND ?2", 
                from != null ? from : LocalDateTime.now().minusMonths(3),
                to != null ? to : LocalDateTime.now()
            );
            
        if (types != null && !types.isEmpty()) {
            query.filter("type IN ?1", types);
        }
        
        // Apply pagination
        Page<Activity> activities = query
            .page(page, size)
            .list();
            
        // Enrich activities
        List<EnrichedActivity> enriched = activities.stream()
            .map(enricher::enrich)
            .collect(Collectors.toList());
            
        // Group by date
        Map<LocalDate, List<EnrichedActivity>> grouped = enriched.stream()
            .collect(Collectors.groupingBy(
                a -> a.getActivity().getOccurredAt().toLocalDate(),
                LinkedHashMap::new,
                Collectors.toList()
            ));
            
        return TimelineResponse.builder()
            .activities(enriched)
            .groupedByDate(grouped)
            .totalElements(query.count())
            .currentPage(page)
            .totalPages((int) Math.ceil(query.count() / (double) size))
            .lastUpdated(LocalDateTime.now())
            .build();
    }
    
    @POST
    @Path("/manual")
    @Transactional
    public Response createActivity(CreateActivityRequest request) {
        Activity activity = activityService.createManualActivity(request);
        
        // Invalidate cache
        cacheManager.getCache("timeline-cache")
            .invalidate(request.getCustomerId());
            
        return Response.ok(enricher.enrich(activity)).build();
    }
    
    @POST
    @Path("/{activityId}/actions/{action}")
    @Transactional
    public Response executeAction(
            @PathParam("activityId") UUID activityId,
            @PathParam("action") String action) {
        
        Activity activity = activityService.findById(activityId);
        
        return switch (action) {
            case "complete" -> completeActivity(activity);
            case "cancel" -> cancelActivity(activity);
            case "snooze" -> snoozeActivity(activity);
            default -> Response.status(400).entity("Unknown action").build();
        };
    }
}

// Real-time Updates via Polling (D2 Decision)
@ApplicationScoped
public class TimelinePollingService {
    
    @ConfigProperty(name = "timeline.polling.interval", defaultValue = "30s")
    Duration pollingInterval;
    
    @Scheduled(every = "{timeline.polling.interval}")
    void checkForUpdates() {
        // This is handled client-side
        // Server just ensures data freshness
        cacheManager.getCache("timeline-cache").invalidateAll();
    }
}
```

---

## 🎨 2. FRONTEND: Timeline UI Components

### 2.1 Main Timeline Component (10 Minuten)
```typescript
export const ActivityTimeline: React.FC<{ customerId: string }> = ({ 
  customerId 
}) => {
  const [filters, setFilters] = useState<TimelineFilters>({
    types: [],
    dateRange: 'last3months'
  });
  
  // Polling-based updates (D2 Decision)
  const { data: timeline, refetch } = useQuery({
    queryKey: ['timeline', customerId, filters],
    queryFn: () => api.getTimeline(customerId, filters),
    refetchInterval: 30000, // 30 seconds
    refetchIntervalInBackground: true
  });
  
  // Infinite scroll
  const { ref: loadMoreRef, inView } = useInView();
  const { fetchNextPage, hasNextPage } = useInfiniteQuery({
    queryKey: ['timeline-infinite', customerId, filters],
    queryFn: ({ pageParam = 0 }) => 
      api.getTimeline(customerId, { ...filters, page: pageParam }),
    getNextPageParam: (lastPage) => 
      lastPage.currentPage < lastPage.totalPages - 1 
        ? lastPage.currentPage + 1 
        : undefined
  });
  
  useEffect(() => {
    if (inView && hasNextPage) {
      fetchNextPage();
    }
  }, [inView, fetchNextPage, hasNextPage]);
  
  return (
    <Box sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
      {/* Filter Bar */}
      <TimelineFilters 
        filters={filters} 
        onChange={setFilters}
        activityCounts={timeline?.typeCounts}
      />
      
      {/* Timeline */}
      <Box sx={{ flex: 1, overflow: 'auto', px: 2 }}>
        {timeline?.groupedByDate.map(([date, activities]) => (
          <TimelineSection key={date} date={date} activities={activities} />
        ))}
        
        {/* Load more trigger */}
        <div ref={loadMoreRef} style={{ height: 20 }} />
        
        {hasNextPage && (
          <Box textAlign="center" py={2}>
            <CircularProgress size={24} />
          </Box>
        )}
      </Box>
      
      {/* Quick Actions FAB */}
      <SpeedDial
        ariaLabel="Quick Actions"
        sx={{ position: 'absolute', bottom: 16, right: 16 }}
        icon={<SpeedDialIcon />}
      >
        <SpeedDialAction
          icon={<NoteAddIcon />}
          tooltipTitle="Notiz hinzufügen"
          onClick={() => openActivityDialog('NOTE')}
        />
        <SpeedDialAction
          icon={<EventIcon />}
          tooltipTitle="Meeting planen"
          onClick={() => openActivityDialog('MEETING')}
        />
        <SpeedDialAction
          icon={<TaskIcon />}
          tooltipTitle="Aufgabe erstellen"
          onClick={() => openActivityDialog('TASK')}
        />
      </SpeedDial>
    </Box>
  );
};

// Timeline Section Component
const TimelineSection: React.FC<{
  date: string;
  activities: EnrichedActivity[];
}> = ({ date, activities }) => {
  const isToday = isSameDay(parseISO(date), new Date());
  
  return (
    <Box sx={{ mb: 4 }}>
      <Typography 
        variant="overline" 
        color="text.secondary"
        sx={{ 
          position: 'sticky', 
          top: 0, 
          bgcolor: 'background.paper',
          py: 1,
          zIndex: 1
        }}
      >
        {isToday ? 'Heute' : format(parseISO(date), 'EEEE, d. MMMM', { locale: de })}
      </Typography>
      
      <Timeline position="right">
        {activities.map((activity, index) => (
          <ActivityItem 
            key={activity.id} 
            activity={activity}
            isFirst={index === 0}
            isLast={index === activities.length - 1}
          />
        ))}
      </Timeline>
    </Box>
  );
};

// Activity Item Component
const ActivityItem: React.FC<{
  activity: EnrichedActivity;
  isFirst: boolean;
  isLast: boolean;
}> = ({ activity, isFirst, isLast }) => {
  const [expanded, setExpanded] = useState(false);
  
  return (
    <TimelineItem>
      <TimelineOppositeContent sx={{ flex: 0.2 }}>
        <Typography variant="caption" color="text.secondary">
          {format(activity.occurredAt, 'HH:mm')}
        </Typography>
      </TimelineOppositeContent>
      
      <TimelineSeparator>
        <TimelineConnector sx={{ visibility: isFirst ? 'hidden' : 'visible' }} />
        <TimelineDot color={getActivityColor(activity.type)}>
          <ActivityIcon type={activity.type} />
        </TimelineDot>
        <TimelineConnector sx={{ visibility: isLast ? 'hidden' : 'visible' }} />
      </TimelineSeparator>
      
      <TimelineContent>
        <Card 
          sx={{ 
            cursor: 'pointer',
            '&:hover': { boxShadow: 2 }
          }}
          onClick={() => setExpanded(!expanded)}
        >
          <CardContent>
            <Box display="flex" justifyContent="space-between" alignItems="start">
              <Box flex={1}>
                <Typography variant="subtitle2">
                  {activity.description}
                </Typography>
                <Typography variant="caption" color="text.secondary">
                  {activity.user?.name || 'System'}
                </Typography>
              </Box>
              
              {activity.suggestedActions.length > 0 && (
                <IconButton size="small" onClick={(e) => {
                  e.stopPropagation();
                  handleQuickAction(activity.suggestedActions[0]);
                }}>
                  <MoreVertIcon />
                </IconButton>
              )}
            </Box>
            
            <Collapse in={expanded}>
              <Divider sx={{ my: 1 }} />
              <ActivityDetails activity={activity} />
              <ActivityActions actions={activity.suggestedActions} />
            </Collapse>
          </CardContent>
        </Card>
      </TimelineContent>
    </TimelineItem>
  );
};
```

### 2.2 Activity Filters & Search (5 Minuten)
```typescript
const TimelineFilters: React.FC<{
  filters: TimelineFilters;
  onChange: (filters: TimelineFilters) => void;
  activityCounts?: Record<ActivityType, number>;
}> = ({ filters, onChange, activityCounts }) => {
  return (
    <Paper sx={{ p: 2, mb: 2 }}>
      <Grid container spacing={2} alignItems="center">
        <Grid item xs={12} md={6}>
          <ToggleButtonGroup
            value={filters.types}
            onChange={(_, types) => onChange({ ...filters, types })}
            aria-label="activity types"
            size="small"
          >
            {Object.values(ActivityType).map(type => (
              <ToggleButton key={type} value={type}>
                <Badge 
                  badgeContent={activityCounts?.[type] || 0} 
                  color="primary"
                  max={99}
                >
                  <ActivityIcon type={type} />
                </Badge>
                <Typography variant="caption" sx={{ ml: 1 }}>
                  {getActivityLabel(type)}
                </Typography>
              </ToggleButton>
            ))}
          </ToggleButtonGroup>
        </Grid>
        
        <Grid item xs={12} md={3}>
          <Select
            value={filters.dateRange}
            onChange={(e) => onChange({ ...filters, dateRange: e.target.value })}
            fullWidth
            size="small"
          >
            <MenuItem value="today">Heute</MenuItem>
            <MenuItem value="week">Diese Woche</MenuItem>
            <MenuItem value="month">Dieser Monat</MenuItem>
            <MenuItem value="last3months">Letzte 3 Monate</MenuItem>
            <MenuItem value="all">Alle</MenuItem>
          </Select>
        </Grid>
        
        <Grid item xs={12} md={3}>
          <TextField
            placeholder="Aktivitäten durchsuchen..."
            size="small"
            fullWidth
            InputProps={{
              startAdornment: <SearchIcon />
            }}
            onChange={(e) => onChange({ 
              ...filters, 
              search: e.target.value 
            })}
          />
        </Grid>
      </Grid>
    </Paper>
  );
};
```

---

## 🗄️ 3. DATENBANK: Schema & Performance

### 3.1 Database Schema (Copy-paste ready)
```sql
-- V1.0.0__activity_timeline.sql

-- Activities table with inheritance
CREATE TABLE activities (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    activity_type VARCHAR(50) NOT NULL,
    customer_id UUID NOT NULL REFERENCES customers(id),
    user_id UUID REFERENCES users(id),
    occurred_at TIMESTAMP NOT NULL,
    description VARCHAR(500),
    metadata JSONB DEFAULT '{}',
    status VARCHAR(50) DEFAULT 'COMPLETED',
    
    -- Email specific fields
    email_subject VARCHAR(255),
    email_from VARCHAR(255),
    email_to VARCHAR(255),
    email_is_inbound BOOLEAN,
    
    -- Call specific fields
    call_phone_number VARCHAR(50),
    call_duration INTEGER,
    call_direction VARCHAR(20),
    
    -- Meeting specific fields
    meeting_scheduled_for TIMESTAMP,
    meeting_duration_minutes INTEGER,
    meeting_location VARCHAR(255),
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance
CREATE INDEX idx_activities_customer_occurred ON activities(customer_id, occurred_at DESC);
CREATE INDEX idx_activities_type ON activities(activity_type);
CREATE INDEX idx_activities_user ON activities(user_id);
CREATE INDEX idx_activities_status ON activities(status) WHERE status != 'COMPLETED';

-- Follow-up actions
CREATE TABLE activity_follow_ups (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    activity_id UUID REFERENCES activities(id) ON DELETE CASCADE,
    action_type VARCHAR(50) NOT NULL,
    scheduled_for TIMESTAMP,
    completed_at TIMESTAMP,
    created_by UUID REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Activity view for better query performance
CREATE MATERIALIZED VIEW activity_summary AS
SELECT 
    a.customer_id,
    COUNT(*) as total_activities,
    COUNT(*) FILTER (WHERE a.activity_type = 'EMAIL') as email_count,
    COUNT(*) FILTER (WHERE a.activity_type = 'CALL') as call_count,
    COUNT(*) FILTER (WHERE a.activity_type = 'MEETING') as meeting_count,
    MAX(a.occurred_at) as last_activity_at,
    AVG(CASE WHEN a.activity_type = 'CALL' THEN a.call_duration END) as avg_call_duration
FROM activities a
GROUP BY a.customer_id;

-- Refresh the materialized view every hour
CREATE INDEX idx_activity_summary_customer ON activity_summary(customer_id);
```

---

## ✅ 4. TESTING

```typescript
describe('ActivityTimeline', () => {
  it('should group activities by date', () => {
    const activities = [
      createActivity({ occurredAt: '2024-01-15T10:00:00' }),
      createActivity({ occurredAt: '2024-01-15T14:00:00' }),
      createActivity({ occurredAt: '2024-01-16T09:00:00' })
    ];
    
    const grouped = groupActivitiesByDate(activities);
    
    expect(Object.keys(grouped)).toHaveLength(2);
    expect(grouped['2024-01-15']).toHaveLength(2);
    expect(grouped['2024-01-16']).toHaveLength(1);
  });
  
  it('should poll for updates every 30 seconds', async () => {
    const { result } = renderHook(() => 
      useQuery({
        queryKey: ['timeline'],
        queryFn: mockApi.getTimeline,
        refetchInterval: 30000
      })
    );
    
    expect(mockApi.getTimeline).toHaveBeenCalledTimes(1);
    
    await act(async () => {
      jest.advanceTimersByTime(30000);
    });
    
    expect(mockApi.getTimeline).toHaveBeenCalledTimes(2);
  });
});
```

---

## 🎯 IMPLEMENTATION PRIORITIES

1. **Phase 1 (1 Tag)**: Activity Model + Basic Timeline API
2. **Phase 2 (1 Tag)**: Event Collection + Enrichment
3. **Phase 3 (1 Tag)**: Timeline UI + Infinite Scroll
4. **Phase 4 (0.5 Tag)**: Follow-up Actions + Polish

**Geschätzter Aufwand:** 3.5 Entwicklungstage