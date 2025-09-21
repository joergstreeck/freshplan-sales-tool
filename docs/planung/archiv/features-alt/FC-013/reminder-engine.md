# FC-013: Reminder Engine für Activity & Notes System

**Parent:** [/Users/joergstreeck/freshplan-sales-tool/docs/features/2025-07-24_TECH_CONCEPT_FC-013-activity-notes-system.md](/Users/joergstreeck/freshplan-sales-tool/docs/features/2025-07-24_TECH_CONCEPT_FC-013-activity-notes-system.md)  
**Fokus:** Automatisierte Erinnerungen und Eskalationen

## 🔔 Reminder-Typen

### 1. Inaktivitäts-Reminder
```java
@ConfigProperty(name = "activity.inactivity.threshold.days", defaultValue = "14")
int inactivityThresholdDays;

@Scheduled(every = "24h", identity = "inactivity-check")
void checkInactiveOpportunities() {
    // Opportunities ohne Activity seit X Tagen
    var inactive = opportunityRepository.findInactiveSince(
        LocalDateTime.now().minusDays(inactivityThresholdDays)
    );
    
    for (var opp : inactive) {
        createInactivityReminder(opp);
    }
}
```

### 2. Task-Reminder
```java
@Scheduled(every = "30m", identity = "task-reminder")
void processTaskReminders() {
    var dueTasks = activityRepository.findDueTasks(
        LocalDateTime.now().plusHours(24),
        false // reminderSent = false
    );
    
    for (var task : dueTasks) {
        sendTaskReminder(task);
    }
}
```

### 3. Contract Renewal Reminder (Integration mit FC-009)
```java
// Synchronisation mit FC-009 Contract Monitoring
@Observes
void onContractNearExpiry(ContractExpiryEvent event) {
    createActivity(Activity.builder()
        .type(TASK_CREATED)
        .entityType("contract")
        .entityId(event.getContractId())
        .title("Vertragsverlängerung vorbereiten")
        .dueDate(event.getExpiryDate().minusDays(30))
        .assignedTo(event.getAccountManagerId())
        .build());
}
```

## 🚀 Eskalations-Logik

### Eskalations-Matrix
| Stufe | Bedingung | Aktion | Empfänger |
|-------|-----------|--------|-----------|
| 1 | Task überfällig | Reminder | Assigned User |
| 2 | +3 Tage überfällig | Eskalation | Team Lead |
| 3 | +7 Tage überfällig | Eskalation | Sales Manager |
| 4 | +14 Tage überfällig | Alert | C-Level |

### Implementation
```java
@ApplicationScoped
public class EscalationService {
    
    public void escalateOverdueTasks() {
        var overdueTasks = activityRepository.findOverdueTasks();
        
        for (var task : overdueTasks) {
            var daysPastDue = ChronoUnit.DAYS.between(
                task.getDueDate(), 
                LocalDateTime.now()
            );
            
            if (daysPastDue >= 14) {
                escalateToCLevel(task);
            } else if (daysPastDue >= 7) {
                escalateToSalesManager(task);
            } else if (daysPastDue >= 3) {
                escalateToTeamLead(task);
            }
        }
    }
}
```

## 📧 Notification Templates

### Email Templates
```java
public enum ReminderTemplate {
    TASK_DUE("task-due.ftl"),
    TASK_OVERDUE("task-overdue.ftl"),
    INACTIVITY("inactivity.ftl"),
    ESCALATION("escalation.ftl");
}
```

### Push Notifications (WebSocket)
```java
@ServerEndpoint("/reminders/{userId}")
public class ReminderWebSocket {
    
    public void sendReminder(String userId, Reminder reminder) {
        var session = sessions.get(userId);
        if (session != null && session.isOpen()) {
            session.getAsyncRemote().sendObject(reminder);
        }
    }
}
```

## ⚙️ Konfiguration

### Reminder-Settings pro User
```java
@Entity
public class UserReminderSettings {
    @Id
    private UUID userId;
    
    private boolean emailReminders = true;
    private boolean pushReminders = true;
    private int reminderAdvanceHours = 24;
    private String preferredReminderTime = "09:00";
    
    // Quiet hours
    private String quietHoursStart = "18:00";
    private String quietHoursEnd = "08:00";
}
```

## 🔄 Integration Points

### Mit FC-012 Audit Trail
- Alle Reminder werden auditiert
- Eskalationen als kritische Events

### Mit FC-003 Email Integration
- Email-Templates nutzen
- Delivery-Tracking

### Mit M1 Dashboard
- Reminder-Widget
- Overdue Task Counter

## 📊 Monitoring & Analytics

### Reminder Effectiveness Metrics
```java
public class ReminderMetrics {
    // Response-Rate nach erstem Reminder
    public double getFirstReminderResponseRate();
    
    // Durchschnittliche Zeit bis Task-Completion
    public Duration getAverageTaskCompletionTime();
    
    // Eskalations-Rate
    public double getEscalationRate();
}
```