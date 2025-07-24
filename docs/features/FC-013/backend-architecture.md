# FC-013: Backend Architecture für Activity & Notes System

**Parent:** [/Users/joergstreeck/freshplan-sales-tool/docs/features/2025-07-24_TECH_CONCEPT_FC-013-activity-notes-system.md](/Users/joergstreeck/freshplan-sales-tool/docs/features/2025-07-24_TECH_CONCEPT_FC-013-activity-notes-system.md)  
**Fokus:** Java/Quarkus Backend Implementation

## 🏗️ Domain Model

### Activity Entity
```java
@Entity
@Table(name = "activities")
@EntityListeners(AuditingEntityListener.class)
public class Activity {
    
    @Id
    @GeneratedValue
    private UUID id;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityType type;
    
    @Column(nullable = false)
    private String entityType;
    
    @Column(nullable = false)
    private UUID entityId;
    
    // Content
    @Column(nullable = false, length = 200)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata;
    
    // User & Time
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    private LocalDateTime completedAt;
    
    // Task/Reminder fields
    private LocalDateTime dueDate;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    private User assignedTo;
    
    private boolean reminderSent = false;
    
    @Enumerated(EnumType.STRING)
    private ActivityStatus status = ActivityStatus.OPEN;
    
    // Relations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_activity_id")
    private Activity parentActivity;
    
    @OneToMany(mappedBy = "activity", cascade = CascadeType.ALL)
    private List<ActivityAttachment> attachments = new ArrayList<>();
    
    // Indexes
    @Index(name = "idx_activity_entity", columnList = "entityType, entityId, createdAt DESC")
    @Index(name = "idx_activity_due", columnList = "dueDate, status, reminderSent")
    @Index(name = "idx_activity_assigned", columnList = "assigned_to, status, dueDate")
}
```

### Predefined Activity Templates
```java
@ApplicationScoped
public class ActivityTemplates {
    
    public static final Map<String, ActivityTemplate> QUICK_ACTIONS = Map.of(
        "CUSTOMER_INFORMED", new ActivityTemplate(
            "Kunde informiert",
            ActivityType.CUSTOMER_INFORMED,
            "Kunde wurde über ${subject} informiert"
        ),
        "CONTRACT_SENT", new ActivityTemplate(
            "Vertrag versendet",
            ActivityType.CONTRACT_SENT,
            "Vertrag wurde per ${method} an ${recipient} gesendet"
        ),
        "FOLLOW_UP_NEEDED", new ActivityTemplate(
            "Follow-up erforderlich",
            ActivityType.TASK_CREATED,
            "Follow-up für ${reason} bis ${dueDate}"
        )
    );
    
    public Activity createFromTemplate(
        String templateKey,
        UUID entityId,
        String entityType,
        Map<String, String> variables
    ) {
        var template = QUICK_ACTIONS.get(templateKey);
        
        return Activity.builder()
            .type(template.getType())
            .title(template.getTitle())
            .content(substituteVariables(template.getContent(), variables))
            .entityId(entityId)
            .entityType(entityType)
            .build();
    }
}
```

## 📊 Service Layer

### Activity Service
```java
@ApplicationScoped
@Transactional
public class ActivityService {
    
    @Inject
    ActivityRepository repository;
    
    @Inject
    NotificationService notificationService;
    
    @Inject
    AuditService auditService;
    
    @Auditable(eventType = AuditEventType.ACTIVITY_CREATED)
    public Activity createActivity(
        CreateActivityRequest request,
        @AuditReason String reason
    ) {
        var activity = Activity.builder()
            .type(request.getType())
            .entityType(request.getEntityType())
            .entityId(request.getEntityId())
            .title(request.getTitle())
            .content(request.getContent())
            .metadata(request.getMetadata())
            .createdBy(getCurrentUser())
            .build();
            
        // Set task fields if applicable
        if (request.getDueDate() != null) {
            activity.setDueDate(request.getDueDate());
            activity.setAssignedTo(request.getAssignedTo());
            activity.setStatus(ActivityStatus.PENDING);
        }
        
        repository.persist(activity);
        
        // Send notifications
        if (activity.getAssignedTo() != null) {
            notificationService.notifyTaskAssigned(activity);
        }
        
        // Update opportunity last activity
        updateEntityLastActivity(
            activity.getEntityType(),
            activity.getEntityId()
        );
        
        return activity;
    }
    
    public Activity addQuickAction(
        UUID entityId,
        String entityType,
        String templateKey,
        Map<String, String> variables
    ) {
        var activity = activityTemplates.createFromTemplate(
            templateKey,
            entityId,
            entityType,
            variables
        );
        
        return createActivity(
            CreateActivityRequest.from(activity),
            "Quick action: " + templateKey
        );
    }
    
    @Auditable(eventType = AuditEventType.TASK_COMPLETED)
    public Activity completeTask(
        UUID activityId,
        @AuditReason String completionNote
    ) {
        var activity = repository.findById(activityId);
        
        if (activity.getStatus() != ActivityStatus.PENDING) {
            throw new IllegalStateException("Activity is not pending");
        }
        
        activity.setStatus(ActivityStatus.COMPLETED);
        activity.setCompletedAt(LocalDateTime.now());
        
        if (completionNote != null) {
            activity.setContent(
                activity.getContent() + "\n\nAbgeschlossen: " + completionNote
            );
        }
        
        return repository.persist(activity);
    }
}
```

### Inactivity Detection Service
```java
@ApplicationScoped
public class InactivityDetectionService {
    
    @ConfigProperty(name = "activity.inactivity.threshold.days")
    int inactivityThresholdDays;
    
    @Inject
    OpportunityRepository opportunityRepository;
    
    @Inject
    ActivityService activityService;
    
    @Scheduled(every = "24h", identity = "inactivity-check")
    void checkInactiveOpportunities() {
        Log.info("Starting inactivity check");
        
        var threshold = LocalDateTime.now()
            .minusDays(inactivityThresholdDays);
            
        var inactiveOpportunities = opportunityRepository
            .findInactiveSince(threshold);
            
        for (var opportunity : inactiveOpportunities) {
            createInactivityReminder(opportunity);
        }
        
        Log.infof("Created %d inactivity reminders", 
            inactiveOpportunities.size());
    }
    
    private void createInactivityReminder(Opportunity opportunity) {
        var request = CreateActivityRequest.builder()
            .type(ActivityType.TASK_CREATED)
            .entityType("opportunity")
            .entityId(opportunity.getId())
            .title("Inaktivitäts-Erinnerung")
            .content(String.format(
                "Diese Opportunity hatte seit %d Tagen keine Aktivität. " +
                "Bitte prüfen und Follow-up durchführen.",
                inactivityThresholdDays
            ))
            .dueDate(LocalDateTime.now().plusDays(1))
            .assignedTo(opportunity.getOwnerId())
            .metadata(Map.of(
                "lastActivityDate", opportunity.getLastActivityDate(),
                "daysSinceActivity", inactivityThresholdDays
            ))
            .build();
            
        activityService.createActivity(
            request,
            "Automatic inactivity reminder"
        );
    }
}
```

## 🔔 Reminder Engine

### Reminder Service
```java
@ApplicationScoped
public class ReminderService {
    
    @Inject
    ActivityRepository activityRepository;
    
    @Inject
    NotificationService notificationService;
    
    @Scheduled(every = "30m", identity = "reminder-check")
    void processReminders() {
        // Find due activities
        var dueActivities = activityRepository.findDueActivities(
            LocalDateTime.now(),
            ActivityStatus.PENDING,
            false // reminderSent = false
        );
        
        for (var activity : dueActivities) {
            sendReminder(activity);
        }
        
        // Escalation for overdue tasks
        var overdueActivities = activityRepository.findOverdueActivities(
            LocalDateTime.now().minusDays(3),
            ActivityStatus.PENDING
        );
        
        for (var activity : overdueActivities) {
            escalateActivity(activity);
        }
    }
    
    private void sendReminder(Activity activity) {
        try {
            // Send notification
            notificationService.sendTaskReminder(
                activity.getAssignedTo(),
                activity
            );
            
            // Mark as sent
            activity.setReminderSent(true);
            activityRepository.persist(activity);
            
            // Log in audit trail
            auditService.logEvent(
                AuditEventType.REMINDER_SENT,
                "activity",
                activity.getId(),
                null,
                Map.of("recipientId", activity.getAssignedTo().getId()),
                "Task reminder sent"
            );
            
        } catch (Exception e) {
            Log.errorf(e, "Failed to send reminder for activity %s", 
                activity.getId());
        }
    }
    
    private void escalateActivity(Activity activity) {
        // Find manager
        var manager = userService.findManagerOf(activity.getAssignedTo());
        
        if (manager != null) {
            // Create escalation activity
            var escalation = Activity.builder()
                .type(ActivityType.TASK_CREATED)
                .entityType(activity.getEntityType())
                .entityId(activity.getEntityId())
                .title("ESKALATION: " + activity.getTitle())
                .content(String.format(
                    "Diese Aufgabe von %s ist seit %d Tagen überfällig.\n\n" +
                    "Ursprüngliche Aufgabe: %s",
                    activity.getAssignedTo().getName(),
                    ChronoUnit.DAYS.between(activity.getDueDate(), LocalDateTime.now()),
                    activity.getContent()
                ))
                .assignedTo(manager)
                .dueDate(LocalDateTime.now().plusHours(4))
                .parentActivity(activity)
                .build();
                
            activityRepository.persist(escalation);
            
            // Notify
            notificationService.sendEscalation(manager, escalation, activity);
        }
    }
}
```

## 🔍 Repository & Queries

### Activity Repository
```java
@ApplicationScoped
public class ActivityRepository implements PanacheRepositoryBase<Activity, UUID> {
    
    public List<Activity> findByEntity(String entityType, UUID entityId) {
        return find(
            "entityType = ?1 and entityId = ?2 order by createdAt desc",
            entityType, entityId
        ).list();
    }
    
    public Page<Activity> findByEntityPaged(
        String entityType, 
        UUID entityId,
        int page,
        int size
    ) {
        return find(
            "entityType = ?1 and entityId = ?2 order by createdAt desc",
            entityType, entityId
        ).page(page, size);
    }
    
    public List<Activity> findDueActivities(
        LocalDateTime before,
        ActivityStatus status,
        boolean reminderSent
    ) {
        return find(
            "dueDate <= ?1 and status = ?2 and reminderSent = ?3",
            before, status, reminderSent
        ).list();
    }
    
    public List<Activity> findUserTasks(
        UUID userId,
        ActivityStatus status,
        LocalDateTime from,
        LocalDateTime to
    ) {
        return find(
            "assignedTo.id = ?1 and status = ?2 and dueDate between ?3 and ?4 " +
            "order by dueDate asc",
            userId, status, from, to
        ).list();
    }
    
    public long countPendingTasks(UUID userId) {
        return count(
            "assignedTo.id = ?1 and status = ?2",
            userId, ActivityStatus.PENDING
        );
    }
    
    // For dashboard widgets
    public List<ActivitySummary> getActivitySummaryByType(
        LocalDateTime from,
        LocalDateTime to
    ) {
        return getEntityManager()
            .createQuery(
                "SELECT new ActivitySummary(a.type, COUNT(a)) " +
                "FROM Activity a " +
                "WHERE a.createdAt BETWEEN :from AND :to " +
                "GROUP BY a.type",
                ActivitySummary.class
            )
            .setParameter("from", from)
            .setParameter("to", to)
            .getResultList();
    }
}
```

## 🔄 Integration Points

### WebSocket for Real-time Updates
```java
@ServerEndpoint("/activities/{entityType}/{entityId}")
@ApplicationScoped
public class ActivityWebSocket {
    
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();
    
    @OnOpen
    public void onOpen(
        Session session,
        @PathParam("entityType") String entityType,
        @PathParam("entityId") String entityId
    ) {
        String key = entityType + ":" + entityId;
        sessions.put(key, session);
    }
    
    public void broadcastActivity(Activity activity) {
        String key = activity.getEntityType() + ":" + activity.getEntityId();
        Session session = sessions.get(key);
        
        if (session != null && session.isOpen()) {
            session.getAsyncRemote().sendObject(
                ActivityDTO.from(activity),
                result -> {
                    if (!result.isOK()) {
                        Log.error("Failed to send activity update", 
                            result.getException());
                    }
                }
            );
        }
    }
}
```