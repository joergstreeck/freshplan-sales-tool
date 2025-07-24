# M4 Opportunity Pipeline - E-Mail Integration (FC-003)

## Automatische E-Mail-Zuordnung

### E-Mail zu Opportunity Matching

```java
@ApplicationScoped
public class OpportunityEmailMatcher {
    
    @Inject OpportunityRepository opportunityRepository;
    @Inject CustomerRepository customerRepository;
    
    public Optional<Opportunity> matchEmailToOpportunity(EmailMessage email) {
        // 1. Check for Opportunity Reference in Subject/Body
        Optional<String> oppRef = extractOpportunityReference(email);
        if (oppRef.isPresent()) {
            return opportunityRepository.findByReference(oppRef.get());
        }
        
        // 2. Match by Customer and Active Opportunities
        Optional<Customer> customer = customerRepository
            .findByEmailDomain(extractDomain(email.getFrom()));
            
        if (customer.isPresent()) {
            List<Opportunity> activeOpps = opportunityRepository
                .findActiveByCustomerId(customer.get().getId());
                
            // 3. Context-based Matching
            return matchByContext(email, activeOpps);
        }
        
        return Optional.empty();
    }
    
    private Optional<Opportunity> matchByContext(EmailMessage email, 
                                                 List<Opportunity> opportunities) {
        // Prioritize by:
        // 1. Recent activity (last 7 days)
        // 2. Stage (higher priority for active stages)
        // 3. Keyword matching in email content
        
        return opportunities.stream()
            .map(opp -> new ScoredOpportunity(opp, calculateScore(email, opp)))
            .max(Comparator.comparing(ScoredOpportunity::getScore))
            .filter(scored -> scored.getScore() > 0.5)
            .map(ScoredOpportunity::getOpportunity);
    }
}
```

## Stage-spezifische E-Mail Templates

### Template-Konfiguration pro Stage

```java
public enum OpportunityStageTemplates {
    QUALIFIED(List.of(
        "initial_contact",
        "qualification_questions",
        "meeting_request"
    )),
    
    ANGEBOTSERSTELLUNG(List.of(
        "quote_delivery",
        "quote_follow_up",
        "negotiation_response"
    )),
    
    NACHFASSEN(List.of(
        "follow_up_reminder",
        "value_proposition",
        "urgency_creator"
    )),
    
    VERHANDLUNG(List.of(
        "contract_terms",
        "discount_approval",
        "final_offer"
    )),
    
    RENEWAL(List.of(
        "renewal_90_days",
        "renewal_60_days",
        "renewal_30_days"
    ));
}

@ApplicationScoped
public class StageEmailTemplateService {
    
    public List<EmailTemplate> getTemplatesForStage(OpportunityStage stage) {
        List<String> templateIds = OpportunityStageTemplates
            .valueOf(stage.name())
            .getTemplateIds();
            
        return templateRepository.findByIds(templateIds);
    }
}
```

## E-Mail Timeline Integration

### E-Mails in Opportunity Timeline

```typescript
interface OpportunityTimelineEvent {
    id: string;
    type: 'EMAIL_SENT' | 'EMAIL_RECEIVED' | 'NOTE' | 'STAGE_CHANGE';
    timestamp: Date;
    data: EmailData | NoteData | StageChangeData;
}

interface EmailData {
    subject: string;
    from: string;
    to: string[];
    preview: string;
    hasAttachments: boolean;
    wasOpened?: boolean;
    wasClicked?: boolean;
}

// Timeline Component
export const OpportunityTimeline: React.FC<{opportunityId: string}> = ({opportunityId}) => {
    const {data: events} = useTimelineEvents(opportunityId);
    
    return (
        <Timeline>
            {events.map(event => (
                <TimelineItem key={event.id}>
                    {event.type === 'EMAIL_SENT' && (
                        <EmailTimelineCard 
                            email={event.data as EmailData}
                            icon={<SendIcon />}
                        />
                    )}
                    {event.type === 'EMAIL_RECEIVED' && (
                        <EmailTimelineCard 
                            email={event.data as EmailData}
                            icon={<InboxIcon />}
                        />
                    )}
                </TimelineItem>
            ))}
        </Timeline>
    );
};
```

## Quick Email Actions

### Stage-abhängige Quick Actions

```typescript
export const OpportunityQuickActions: React.FC<{opportunity: Opportunity}> = ({opportunity}) => {
    const {sendEmail} = useEmailComposer();
    const templates = useStageTemplates(opportunity.stage);
    
    return (
        <QuickActionBar>
            <Button
                startIcon={<EmailIcon />}
                onClick={() => sendEmail({
                    to: opportunity.contact.email,
                    template: templates[0],
                    context: {opportunity}
                })}
            >
                Send {getStageEmailLabel(opportunity.stage)}
            </Button>
            
            <DropdownButton 
                icon={<TemplateIcon />}
                label="More Templates"
            >
                {templates.map(template => (
                    <MenuItem 
                        key={template.id}
                        onClick={() => sendEmail({
                            template,
                            context: {opportunity}
                        })}
                    >
                        {template.name}
                    </MenuItem>
                ))}
            </DropdownButton>
        </QuickActionBar>
    );
};
```

## E-Mail Tracking in Pipeline View

### Engagement Indicators

```typescript
export const OpportunityCard: React.FC<{opportunity: Opportunity}> = ({opportunity}) => {
    const engagement = useEmailEngagement(opportunity.id);
    
    return (
        <Card>
            <CardHeader>
                <Typography>{opportunity.title}</Typography>
                <EngagementIndicator level={engagement.score} />
            </CardHeader>
            
            <CardContent>
                <Grid container spacing={1}>
                    <Grid item xs={6}>
                        <Metric 
                            icon={<EmailIcon />}
                            value={engagement.emailsSent}
                            label="Sent"
                        />
                    </Grid>
                    <Grid item xs={6}>
                        <Metric 
                            icon={<VisibilityIcon />}
                            value={`${engagement.openRate}%`}
                            label="Open Rate"
                        />
                    </Grid>
                </Grid>
                
                {engagement.lastEmail && (
                    <LastEmailPreview email={engagement.lastEmail} />
                )}
            </CardContent>
        </Card>
    );
};
```

## Automatische Follow-ups

### Stage-basierte Follow-up Regeln

```java
@ApplicationScoped
public class OpportunityFollowUpEngine {
    
    @Scheduled(every = "1h")
    public void processFollowUps() {
        List<Opportunity> staleOpportunities = opportunityRepository
            .findStaleByStage();
            
        for (Opportunity opp : staleOpportunities) {
            FollowUpRule rule = getFollowUpRule(opp.getStage());
            
            if (shouldSendFollowUp(opp, rule)) {
                sendAutomatedFollowUp(opp, rule);
                
                // Create Activity
                activityService.createActivity(
                    ActivityType.AUTOMATED_EMAIL,
                    "Automated follow-up sent",
                    opp.getId()
                );
            }
        }
    }
    
    private FollowUpRule getFollowUpRule(OpportunityStage stage) {
        return switch(stage) {
            case QUALIFIED -> new FollowUpRule(3, "qualification_follow_up");
            case ANGEBOTSERSTELLUNG -> new FollowUpRule(5, "quote_follow_up");
            case NACHFASSEN -> new FollowUpRule(7, "gentle_reminder");
            case VERHANDLUNG -> new FollowUpRule(2, "urgency_follow_up");
            default -> null;
        };
    }
}
```

## E-Mail Performance Analytics

### Pipeline E-Mail Metrics

```java
@Path("/api/opportunities/email-metrics")
public class OpportunityEmailMetricsResource {
    
    @GET
    @Path("/by-stage")
    public Response getEmailMetricsByStage() {
        Map<OpportunityStage, StageEmailMetrics> metrics = 
            metricsService.calculateStageMetrics();
            
        return Response.ok(metrics).build();
    }
    
    @GET
    @Path("/{opportunityId}")
    public Response getOpportunityEmailMetrics(@PathParam("opportunityId") UUID id) {
        OpportunityEmailMetrics metrics = metricsService
            .getMetricsForOpportunity(id);
            
        return Response.ok(metrics).build();
    }
}

public class StageEmailMetrics {
    private OpportunityStage stage;
    private Integer emailsSent;
    private Double avgOpenRate;
    private Double avgClickRate;
    private Double avgResponseTime;
    private List<TopPerformingTemplate> topTemplates;
}
```