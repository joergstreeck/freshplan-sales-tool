# FC-013: E-Mail als Activity Type - Integration mit FC-003

## Automatisches Activity Logging

### E-Mail Events als Activities

```java
@ApplicationScoped
public class EmailActivityLogger {
    
    @Inject ActivityService activityService;
    @Inject EmailService emailService;
    
    @ConsumeEvent("email.sent")
    public void logEmailSent(EmailSentEvent event) {
        Activity activity = Activity.builder()
            .type(ActivityType.EMAIL_SENT)
            .entityType(determineEntityType(event))
            .entityId(determineEntityId(event))
            .title("E-Mail gesendet: " + event.getSubject())
            .content(createEmailActivityContent(event))
            .metadata(Map.of(
                "messageId", event.getMessageId(),
                "to", event.getRecipients(),
                "template", event.getTemplateId(),
                "hasAttachments", event.hasAttachments()
            ))
            .createdBy(event.getSentBy())
            .createdAt(event.getSentAt())
            .build();
            
        activityService.create(activity);
    }
    
    @ConsumeEvent("email.received")
    public void logEmailReceived(EmailReceivedEvent event) {
        Activity activity = Activity.builder()
            .type(ActivityType.EMAIL_RECEIVED)
            .entityType(EntityType.OPPORTUNITY)
            .entityId(event.getOpportunityId())
            .title("E-Mail empfangen: " + event.getSubject())
            .content(event.getPreview())
            .metadata(Map.of(
                "messageId", event.getMessageId(),
                "from", event.getFrom(),
                "threadId", event.getThreadId()
            ))
            .createdBy("SYSTEM")
            .createdAt(event.getReceivedAt())
            .build();
            
        activityService.create(activity);
        
        // Trigger Inactivity Reset
        resetInactivityTimer(event.getOpportunityId());
    }
}
```

## Activity Timeline mit E-Mails

### Unified Timeline View

```typescript
export const ActivityTimeline: React.FC<{entityId: string}> = ({entityId}) => {
    const {data: activities} = useActivities(entityId);
    
    return (
        <Timeline>
            {activities.map(activity => (
                <TimelineItem key={activity.id}>
                    {renderActivityItem(activity)}
                </TimelineItem>
            ))}
        </Timeline>
    );
};

const renderActivityItem = (activity: Activity) => {
    switch(activity.type) {
        case 'EMAIL_SENT':
            return (
                <EmailSentActivity 
                    activity={activity}
                    onReply={() => openEmailComposer(activity)}
                    onViewFull={() => openEmailViewer(activity)}
                />
            );
            
        case 'EMAIL_RECEIVED':
            return (
                <EmailReceivedActivity 
                    activity={activity}
                    onReply={() => replyToEmail(activity)}
                    onMarkImportant={() => markAsImportant(activity)}
                />
            );
            
        default:
            return <StandardActivity activity={activity} />;
    }
};
```

## E-Mail Activity Components

### E-Mail Activity Card

```typescript
export const EmailSentActivity: React.FC<{activity: Activity}> = ({activity}) => {
    const tracking = useEmailTracking(activity.metadata.messageId);
    
    return (
        <Card elevation={0} className="email-activity">
            <CardHeader
                avatar={<SendIcon color="primary" />}
                title="E-Mail gesendet"
                subheader={
                    <Box>
                        <Typography variant="caption">
                            An: {activity.metadata.to.join(', ')}
                        </Typography>
                        <Typography variant="caption" display="block">
                            {formatRelativeTime(activity.createdAt)}
                        </Typography>
                    </Box>
                }
                action={
                    <EmailTrackingChips tracking={tracking} />
                }
            />
            
            <CardContent>
                <Typography variant="subtitle1" gutterBottom>
                    {activity.title.replace('E-Mail gesendet: ', '')}
                </Typography>
                
                <Typography variant="body2" color="textSecondary">
                    {activity.content}
                </Typography>
                
                {activity.metadata.hasAttachments && (
                    <Chip 
                        icon={<AttachFileIcon />} 
                        label="Mit Anhängen" 
                        size="small"
                        className="mt-2"
                    />
                )}
            </CardContent>
            
            <CardActions>
                <Button size="small" startIcon={<ReplyIcon />}>
                    Follow-up senden
                </Button>
                <Button size="small">
                    Vollansicht
                </Button>
            </CardActions>
        </Card>
    );
};
```

## Activity-basierte E-Mail Trigger

### Automatische Follow-ups basierend auf Activities

```java
@ApplicationScoped
public class ActivityBasedEmailTrigger {
    
    @Inject EmailService emailService;
    @Inject TemplateService templateService;
    
    @Scheduled(every = "1h")
    public void checkActivityTriggers() {
        // Keine Antwort auf wichtige E-Mail
        List<Activity> unansweredEmails = activityRepository
            .findUnansweredImportantEmails(Duration.ofDays(3));
            
        for (Activity emailActivity : unansweredEmails) {
            if (!hasFollowUp(emailActivity)) {
                sendFollowUpReminder(emailActivity);
            }
        }
        
        // Inaktivität nach Meeting
        List<Activity> meetings = activityRepository
            .findMeetingsWithoutFollowUp(Duration.ofDays(2));
            
        for (Activity meeting : meetings) {
            sendMeetingFollowUp(meeting);
        }
    }
    
    private void sendFollowUpReminder(Activity originalEmail) {
        EmailTemplate template = templateService
            .getTemplate("email_follow_up_reminder");
            
        TemplateContext context = TemplateContext.builder()
            .originalSubject(originalEmail.getTitle())
            .daysSinceSent(calculateDaysSince(originalEmail.getCreatedAt()))
            .opportunity(getOpportunity(originalEmail.getEntityId()))
            .build();
            
        emailService.sendTemplatedEmail(
            getRecipientEmail(originalEmail),
            template,
            context
        );
        
        // Log als neue Activity
        createFollowUpActivity(originalEmail);
    }
}
```

## E-Mail Activity Filters

### Erweiterte Filter für E-Mail Activities

```typescript
export const ActivityFilters: React.FC = () => {
    const [filters, setFilters] = useActivityFilters();
    
    return (
        <FilterPanel>
            <FilterSection title="Activity-Typ">
                <FormGroup>
                    <FormControlLabel
                        control={
                            <Checkbox 
                                checked={filters.types.includes('EMAIL_SENT')}
                                onChange={() => toggleType('EMAIL_SENT')}
                            />
                        }
                        label={
                            <Box display="flex" alignItems="center">
                                <SendIcon fontSize="small" sx={{mr: 1}} />
                                Gesendete E-Mails
                            </Box>
                        }
                    />
                    <FormControlLabel
                        control={
                            <Checkbox 
                                checked={filters.types.includes('EMAIL_RECEIVED')}
                                onChange={() => toggleType('EMAIL_RECEIVED')}
                            />
                        }
                        label={
                            <Box display="flex" alignItems="center">
                                <InboxIcon fontSize="small" sx={{mr: 1}} />
                                Empfangene E-Mails
                            </Box>
                        }
                    />
                </FormGroup>
            </FilterSection>
            
            <FilterSection title="E-Mail Status">
                <FormGroup>
                    <FormControlLabel
                        control={<Checkbox />}
                        label="Geöffnet"
                    />
                    <FormControlLabel
                        control={<Checkbox />}
                        label="Geklickt"
                    />
                    <FormControlLabel
                        control={<Checkbox />}
                        label="Beantwortet"
                    />
                </FormGroup>
            </FilterSection>
        </FilterPanel>
    );
};
```

## Activity-E-Mail Metriken

### E-Mail Performance in Activity Context

```java
@ApplicationScoped
public class ActivityEmailMetrics {
    
    public EmailActivityStats calculateStats(UUID entityId, 
                                             TimeRange range) {
        List<Activity> emailActivities = activityRepository
            .findByEntityAndTypes(
                entityId, 
                List.of(ActivityType.EMAIL_SENT, ActivityType.EMAIL_RECEIVED),
                range
            );
            
        return EmailActivityStats.builder()
            .totalSent(countByType(emailActivities, ActivityType.EMAIL_SENT))
            .totalReceived(countByType(emailActivities, ActivityType.EMAIL_RECEIVED))
            .avgResponseTime(calculateAvgResponseTime(emailActivities))
            .conversationThreads(groupIntoThreads(emailActivities).size())
            .topTemplates(findMostUsedTemplates(emailActivities))
            .engagementScore(calculateEngagementScore(emailActivities))
            .build();
    }
}
```

## Integration mit Reminder Engine

### E-Mail-basierte Reminder

```java
@ApplicationScoped
public class EmailReminderIntegration {
    
    @Inject ReminderEngine reminderEngine;
    @Inject EmailService emailService;
    
    public void setupEmailReminders(Activity emailActivity) {
        // Auto-Reminder wenn keine Antwort
        if (requiresResponse(emailActivity)) {
            Reminder noResponseReminder = Reminder.builder()
                .activityId(emailActivity.getId())
                .type(ReminderType.NO_EMAIL_RESPONSE)
                .triggerAt(calculateReminderTime(emailActivity))
                .assignedTo(emailActivity.getCreatedBy())
                .message("Keine Antwort auf E-Mail an " + 
                        emailActivity.getMetadata().get("to"))
                .build();
                
            reminderEngine.schedule(noResponseReminder);
        }
    }
}
```