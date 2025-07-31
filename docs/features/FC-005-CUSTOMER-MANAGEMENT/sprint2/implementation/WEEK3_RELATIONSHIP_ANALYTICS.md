# 📅 Woche 3: Relationship + Analytics

**Sprint:** Sprint 2 - Contact Management  
**Woche:** 3 (19.-23. August 2025)  
**Fokus:** Relationship Features + Analytics Integration  

## 🎯 Wochenziel

Implementierung der Relationship-Features und Analytics-Integration. Am Ende der Woche haben wir:
- ✅ Relationship Warmth Indicator
- ✅ Beziehungsebene-Features komplett
- ✅ Analytics Events implementiert
- ✅ Timeline-Ansicht funktional
- ✅ KI-Ready Data Structure

## 📋 Tagesplan

### Montag: Relationship Warmth Calculator

#### Backend: Warmth Calculation Service
```java
// RelationshipWarmthService.java
@ApplicationScoped
public class RelationshipWarmthService {
    
    @Inject
    EventStore eventStore;
    
    @Inject
    CommunicationService communicationService;
    
    public RelationshipWarmth calculateWarmth(UUID contactId) {
        // Collect metrics
        WarmthMetrics metrics = collectMetrics(contactId);
        
        // Calculate score
        WarmthScore score = calculateScore(metrics);
        
        // Generate suggestions
        List<ActionSuggestion> suggestions = generateSuggestions(score, metrics);
        
        return RelationshipWarmth.builder()
            .contactId(contactId)
            .temperature(score.getTemperature())
            .score(score.getValue())
            .lastInteraction(metrics.getLastInteraction())
            .interactionFrequency(metrics.getFrequency())
            .responseRate(metrics.getResponseRate())
            .suggestions(suggestions)
            .calculatedAt(Instant.now())
            .build();
    }
    
    private WarmthScore calculateScore(WarmthMetrics metrics) {
        double score = 100.0;
        
        // Recency factor (40% weight)
        long daysSinceContact = metrics.getDaysSinceLastContact();
        if (daysSinceContact > 60) score -= 40;
        else if (daysSinceContact > 30) score -= 20;
        else if (daysSinceContact > 14) score -= 10;
        
        // Frequency factor (30% weight)
        double monthlyInteractions = metrics.getMonthlyInteractionRate();
        if (monthlyInteractions < 1) score -= 30;
        else if (monthlyInteractions < 2) score -= 15;
        else if (monthlyInteractions > 4) score += 10;
        
        // Response rate factor (20% weight)
        double responseRate = metrics.getResponseRate();
        score += (responseRate - 0.5) * 40; // -20 to +20
        
        // Business value factor (10% weight)
        if (metrics.getAverageOrderValue() > 10000) score += 10;
        else if (metrics.getAverageOrderValue() > 5000) score += 5;
        
        // Determine temperature
        Temperature temp;
        if (score >= 80) temp = Temperature.HOT;
        else if (score >= 60) temp = Temperature.WARM;
        else if (score >= 40) temp = Temperature.COOLING;
        else temp = Temperature.COLD;
        
        return new WarmthScore(score, temp);
    }
    
    private List<ActionSuggestion> generateSuggestions(WarmthScore score, WarmthMetrics metrics) {
        List<ActionSuggestion> suggestions = new ArrayList<>();
        
        if (score.getTemperature() == Temperature.COOLING) {
            suggestions.add(ActionSuggestion.builder()
                .type(ActionType.REACH_OUT)
                .reason("Kontakt kühlt ab - Zeit für eine persönliche Note")
                .urgency(Urgency.HIGH)
                .suggestedAction("Anruf mit offenem Gespräch über aktuelle Herausforderungen")
                .build());
        }
        
        // Birthday reminder
        if (metrics.getDaysUntilBirthday() <= 7 && metrics.getDaysUntilBirthday() > 0) {
            suggestions.add(ActionSuggestion.builder()
                .type(ActionType.SEND_WISHES)
                .reason("Geburtstag in " + metrics.getDaysUntilBirthday() + " Tagen")
                .urgency(Urgency.MEDIUM)
                .suggestedAction("Persönliche Geburtstagsgrüße vorbereiten")
                .build());
        }
        
        // Cross-sell opportunity
        if (metrics.getProductGaps().size() > 0) {
            suggestions.add(ActionSuggestion.builder()
                .type(ActionType.CROSS_SELL)
                .reason("Kunde nutzt noch nicht: " + String.join(", ", metrics.getProductGaps()))
                .urgency(Urgency.LOW)
                .suggestedAction("Produktvorstellung für fehlende Services")
                .build());
        }
        
        return suggestions;
    }
}
```

#### Frontend: Warmth Indicator Component
```typescript
// components/RelationshipWarmthIndicator.tsx
export const RelationshipWarmthIndicator: React.FC<WarmthIndicatorProps> = ({
  contact,
  showDetails = false,
  onActionClick
}) => {
  const { warmth, loading } = useRelationshipWarmth(contact.id);
  
  const getTemperatureColor = (temp: Temperature): string => {
    const colors = {
      hot: '#ff6b6b',      // Rot
      warm: '#ffd93d',     // Gelb
      cooling: '#6bcf7f',  // Grün
      cold: '#4dabf7'      // Blau
    };
    return colors[temp] || '#868e96';
  };
  
  const getTemperatureIcon = (temp: Temperature) => {
    const icons = {
      hot: '🔥',
      warm: '☀️',
      cooling: '🌤️',
      cold: '❄️'
    };
    return icons[temp] || '❓';
  };
  
  if (loading) return <Skeleton variant="circular" width={40} height={40} />;
  if (!warmth) return null;
  
  return (
    <Box>
      <Tooltip
        title={
          <Box>
            <Typography variant="body2">
              Beziehungstemperatur: {warmth.score.toFixed(0)}/100
            </Typography>
            <Typography variant="caption">
              Letzter Kontakt: vor {warmth.daysSinceContact} Tagen
            </Typography>
          </Box>
        }
      >
        <Box
          sx={{
            display: 'inline-flex',
            alignItems: 'center',
            backgroundColor: getTemperatureColor(warmth.temperature),
            borderRadius: '20px',
            px: 2,
            py: 0.5,
            cursor: showDetails ? 'pointer' : 'default'
          }}
          onClick={() => showDetails && onActionClick?.(warmth)}
        >
          <Typography variant="h6" sx={{ mr: 1 }}>
            {getTemperatureIcon(warmth.temperature)}
          </Typography>
          <Typography variant="body2" fontWeight="bold">
            {warmth.temperature}
          </Typography>
        </Box>
      </Tooltip>
      
      {showDetails && warmth.suggestions.length > 0 && (
        <Alert 
          severity={warmth.temperature === 'cold' ? 'warning' : 'info'}
          sx={{ mt: 1 }}
        >
          <AlertTitle>Empfohlene Aktionen</AlertTitle>
          {warmth.suggestions.map((suggestion, idx) => (
            <Box key={idx} sx={{ mb: 1 }}>
              <Typography variant="body2">
                • {suggestion.suggestedAction}
              </Typography>
              <Typography variant="caption" color="text.secondary">
                {suggestion.reason}
              </Typography>
            </Box>
          ))}
        </Alert>
      )}
    </Box>
  );
};
```

### Dienstag: Beziehungsebene Deep Features

#### Backend: Relationship Data Enhanced
```java
// RelationshipDataService.java
@ApplicationScoped
public class RelationshipDataService {
    
    @Inject
    ContactRepository contactRepository;
    
    @Inject
    EventBus eventBus;
    
    // Smart hobby matching for conversation starters
    public List<ConversationStarter> generateConversationStarters(UUID contactId) {
        Contact contact = contactRepository.findById(contactId);
        RelationshipData data = contact.getRelationshipData();
        
        List<ConversationStarter> starters = new ArrayList<>();
        
        // Hobby-based starters
        if (data.getHobbies().contains("golf")) {
            starters.add(ConversationStarter.builder()
                .topic("Golf")
                .opener("Waren Sie in letzter Zeit auf dem Golfplatz?")
                .followUp("Haben Sie von dem neuen Golfplatz in " + contact.getCity() + " gehört?")
                .build());
        }
        
        // Family-based starters
        if (data.getChildren() > 0) {
            starters.add(ConversationStarter.builder()
                .topic("Familie")
                .opener("Wie geht es Ihrer Familie?")
                .followUp(getSeasonalFamilyTopic())
                .build());
        }
        
        // Business-relevant starters
        starters.addAll(generateBusinessStarters(contact));
        
        return starters;
    }
    
    // Intelligent birthday reminder system
    @Scheduled(every = "24h")
    void checkUpcomingBirthdays() {
        LocalDate today = LocalDate.now();
        LocalDate weekFromNow = today.plusDays(7);
        
        List<Contact> upcomingBirthdays = contactRepository
            .findByBirthdayBetween(today, weekFromNow);
        
        for (Contact contact : upcomingBirthdays) {
            int daysUntil = Period.between(today, contact.getBirthday()).getDays();
            
            BirthdayReminderEvent event = BirthdayReminderEvent.builder()
                .contactId(contact.getId())
                .contactName(contact.getFullName())
                .birthday(contact.getBirthday())
                .daysUntil(daysUntil)
                .age(Period.between(contact.getBirthday(), today).getYears())
                .build();
            
            eventBus.publish(event);
        }
    }
}
```

#### Frontend: Enhanced Relationship Fields
```typescript
// components/RelationshipDataForm.tsx
export const RelationshipDataForm: React.FC<RelationshipDataFormProps> = ({
  contact,
  onSave
}) => {
  const [data, setData] = useState<RelationshipData>(
    contact.relationshipData || getEmptyRelationshipData()
  );
  
  const hobbyOptions = [
    { value: 'golf', label: 'Golf', icon: '⛳' },
    { value: 'tennis', label: 'Tennis', icon: '🎾' },
    { value: 'sailing', label: 'Segeln', icon: '⛵' },
    { value: 'wine', label: 'Wein', icon: '🍷' },
    { value: 'cooking', label: 'Kochen', icon: '👨‍🍳' },
    { value: 'travel', label: 'Reisen', icon: '✈️' },
    { value: 'art', label: 'Kunst', icon: '🎨' },
    { value: 'music', label: 'Musik', icon: '🎵' }
  ];
  
  return (
    <Box>
      <Typography variant="h6" gutterBottom>
        Beziehungsebene pflegen
      </Typography>
      
      <Grid container spacing={3}>
        {/* Geburtstag mit Erinnerung */}
        <Grid item xs={12} md={6}>
          <DatePicker
            label="Geburtstag"
            value={data.birthday}
            onChange={(date) => setData({...data, birthday: date})}
            renderInput={(params) => (
              <TextField 
                {...params} 
                fullWidth
                helperText="Automatische Erinnerung 7 Tage vorher"
              />
            )}
          />
        </Grid>
        
        {/* Hobbys mit Icons */}
        <Grid item xs={12}>
          <Autocomplete
            multiple
            options={hobbyOptions}
            value={data.hobbies || []}
            onChange={(_, newValue) => setData({...data, hobbies: newValue})}
            renderOption={(props, option) => (
              <Box component="li" {...props}>
                <span style={{ marginRight: 8 }}>{option.icon}</span>
                {option.label}
              </Box>
            )}
            renderInput={(params) => (
              <TextField
                {...params}
                label="Hobbys & Interessen"
                placeholder="Für Gesprächsthemen"
              />
            )}
          />
        </Grid>
        
        {/* Familienstand */}
        <Grid item xs={12} md={6}>
          <FormControl fullWidth>
            <InputLabel>Familienstand</InputLabel>
            <Select
              value={data.maritalStatus || ''}
              onChange={(e) => setData({...data, maritalStatus: e.target.value})}
            >
              <MenuItem value="single">Ledig</MenuItem>
              <MenuItem value="married">Verheiratet</MenuItem>
              <MenuItem value="divorced">Geschieden</MenuItem>
              <MenuItem value="widowed">Verwitwet</MenuItem>
            </Select>
          </FormControl>
        </Grid>
        
        {/* Kinder */}
        <Grid item xs={12} md={6}>
          <TextField
            type="number"
            label="Anzahl Kinder"
            value={data.children || 0}
            onChange={(e) => setData({...data, children: parseInt(e.target.value)})}
            InputProps={{ inputProps: { min: 0, max: 10 } }}
            fullWidth
          />
        </Grid>
        
        {/* Persönliche Notizen mit KI-Vorschlägen */}
        <Grid item xs={12}>
          <TextField
            multiline
            rows={4}
            label="Persönliche Notizen"
            value={data.personalNotes || ''}
            onChange={(e) => setData({...data, personalNotes: e.target.value})}
            placeholder="z.B. Mag keinen Smalltalk, bevorzugt direkte Kommunikation..."
            fullWidth
            helperText="Diese Notizen helfen bei der Vorbereitung von Gesprächen"
          />
        </Grid>
        
        {/* Bevorzugter Kommunikationskanal */}
        <Grid item xs={12}>
          <FormControl component="fieldset">
            <FormLabel>Bevorzugter Kommunikationsweg</FormLabel>
            <RadioGroup
              row
              value={data.preferredContactMethod || 'email'}
              onChange={(e) => setData({...data, preferredContactMethod: e.target.value})}
            >
              <FormControlLabel value="email" control={<Radio />} label="E-Mail" />
              <FormControlLabel value="phone" control={<Radio />} label="Telefon" />
              <FormControlLabel value="personal" control={<Radio />} label="Persönlich" />
              <FormControlLabel value="whatsapp" control={<Radio />} label="WhatsApp" />
            </RadioGroup>
          </FormControl>
        </Grid>
      </Grid>
      
      <Box sx={{ mt: 3, display: 'flex', justifyContent: 'flex-end', gap: 2 }}>
        <Button variant="outlined">Abbrechen</Button>
        <Button variant="contained" onClick={() => onSave(data)}>
          Speichern
        </Button>
      </Box>
    </Box>
  );
};
```

### Mittwoch: Analytics Events Implementation

#### Backend: Analytics Event Publisher
```java
// AnalyticsEventPublisher.java
@ApplicationScoped
public class AnalyticsEventPublisher {
    
    @Inject
    EventBus eventBus;
    
    @Inject
    MeterRegistry metrics;
    
    // Contact interaction tracking
    public void trackInteraction(InteractionEvent event) {
        // Metrics for monitoring
        metrics.counter("contact.interactions", 
            "type", event.getType().toString(),
            "channel", event.getChannel().toString()
        ).increment();
        
        // Analytics event for later processing
        AnalyticsEvent analyticsEvent = AnalyticsEvent.builder()
            .eventType("CONTACT_INTERACTION")
            .aggregateId(event.getContactId())
            .timestamp(Instant.now())
            .dimensions(Map.of(
                "interactionType", event.getType().toString(),
                "channel", event.getChannel().toString(),
                "duration", String.valueOf(event.getDurationSeconds()),
                "outcome", event.getOutcome().toString()
            ))
            .measures(Map.of(
                "responseTime", event.getResponseTimeMinutes(),
                "sentimentScore", event.getSentimentScore()
            ))
            .userId(event.getUserId())
            .build();
        
        eventBus.publish(analyticsEvent);
    }
    
    // Relationship quality tracking
    public void trackRelationshipChange(RelationshipChangeEvent event) {
        double scoreDelta = event.getNewScore() - event.getOldScore();
        
        // Alert if significant drop
        if (scoreDelta < -20) {
            RelationshipAlertEvent alert = RelationshipAlertEvent.builder()
                .contactId(event.getContactId())
                .alertType(AlertType.RELATIONSHIP_DEGRADING)
                .severity(Severity.HIGH)
                .message("Beziehung verschlechtert sich rapide")
                .suggestedAction("Dringender persönlicher Kontakt empfohlen")
                .build();
            
            eventBus.publish(alert);
        }
    }
    
    // Cross-sell opportunity detection
    @Observes
    void onOrderPlaced(OrderPlacedEvent event) {
        analyzeCrossSellOpportunities(event.getCustomerId(), event.getOrderItems());
    }
    
    private void analyzeCrossSellOpportunities(UUID customerId, List<OrderItem> items) {
        Set<String> orderedCategories = items.stream()
            .map(OrderItem::getCategory)
            .collect(Collectors.toSet());
        
        Set<String> missingCategories = new HashSet<>(ALL_CATEGORIES);
        missingCategories.removeAll(orderedCategories);
        
        if (!missingCategories.isEmpty()) {
            CrossSellOpportunityEvent opportunity = CrossSellOpportunityEvent.builder()
                .customerId(customerId)
                .missingCategories(missingCategories)
                .estimatedValue(calculatePotentialValue(missingCategories))
                .confidence(0.75) // Based on historical data
                .build();
            
            eventBus.publish(opportunity);
        }
    }
}
```

#### Frontend: Analytics Dashboard Widget
```typescript
// components/analytics/RelationshipAnalytics.tsx
export const RelationshipAnalytics: React.FC<AnalyticsProps> = ({
  customerId
}) => {
  const { data: analytics, loading } = useAnalytics(customerId);
  
  if (loading) return <CircularProgress />;
  if (!analytics) return null;
  
  return (
    <Card>
      <CardContent>
        <Typography variant="h6" gutterBottom>
          Beziehungsanalyse
        </Typography>
        
        <Grid container spacing={3}>
          {/* Interaction Frequency Chart */}
          <Grid item xs={12} md={6}>
            <Box sx={{ height: 200 }}>
              <ResponsiveContainer width="100%" height="100%">
                <LineChart data={analytics.interactionHistory}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="month" />
                  <YAxis />
                  <Tooltip />
                  <Line 
                    type="monotone" 
                    dataKey="count" 
                    stroke="#8884d8"
                    name="Interaktionen"
                  />
                </LineChart>
              </ResponsiveContainer>
            </Box>
          </Grid>
          
          {/* Relationship Score Trend */}
          <Grid item xs={12} md={6}>
            <Box sx={{ height: 200 }}>
              <ResponsiveContainer width="100%" height="100%">
                <AreaChart data={analytics.scoreHistory}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="date" />
                  <YAxis domain={[0, 100]} />
                  <Tooltip />
                  <Area 
                    type="monotone" 
                    dataKey="score" 
                    stroke="#82ca9d"
                    fill="#82ca9d"
                    fillOpacity={0.6}
                    name="Beziehungsscore"
                  />
                </AreaChart>
              </ResponsiveContainer>
            </Box>
          </Grid>
          
          {/* Key Metrics */}
          <Grid item xs={12}>
            <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap' }}>
              <MetricCard
                title="Durchschn. Antwortzeit"
                value={`${analytics.avgResponseTime}h`}
                trend={analytics.responseTimeTrend}
                icon={<TimerIcon />}
              />
              <MetricCard
                title="Interaktionsrate"
                value={`${analytics.interactionRate}/Monat`}
                trend={analytics.interactionTrend}
                icon={<TrendingUpIcon />}
              />
              <MetricCard
                title="Cross-Sell Potenzial"
                value={formatCurrency(analytics.crossSellPotential)}
                subtitle={analytics.missingProducts.join(', ')}
                icon={<ShoppingCartIcon />}
              />
            </Box>
          </Grid>
        </Grid>
      </CardContent>
    </Card>
  );
};
```

### Donnerstag: Timeline Implementation

#### Backend: Timeline Projection
```java
// TimelineProjection.java
@ApplicationScoped
public class TimelineProjection {
    
    @Inject
    EntityManager em;
    
    @EventHandler
    @Transactional
    public void on(BaseEvent event) {
        // Create timeline entry for every event
        TimelineEntry entry = TimelineEntry.builder()
            .entryId(UUID.randomUUID())
            .contactId(event.getAggregateId())
            .timestamp(event.getCreatedAt())
            .eventType(mapToTimelineType(event))
            .title(generateTitle(event))
            .description(generateDescription(event))
            .icon(getIconForEvent(event))
            .userId(event.getMetadata().getUserId())
            .metadata(extractRelevantMetadata(event))
            .build();
        
        em.persist(entry);
    }
    
    private TimelineEventType mapToTimelineType(BaseEvent event) {
        return switch (event.getClass().getSimpleName()) {
            case "ContactCreatedEvent" -> TimelineEventType.CONTACT_CREATED;
            case "EmailSentEvent" -> TimelineEventType.EMAIL_SENT;
            case "CallLoggedEvent" -> TimelineEventType.CALL_LOGGED;
            case "MeetingScheduledEvent" -> TimelineEventType.MEETING_SCHEDULED;
            case "OrderPlacedEvent" -> TimelineEventType.ORDER_PLACED;
            case "RelationshipDataUpdatedEvent" -> TimelineEventType.INFO_UPDATED;
            default -> TimelineEventType.OTHER;
        };
    }
    
    public TimelineResponse getTimeline(UUID contactId, TimelineFilter filter) {
        String query = "SELECT t FROM TimelineEntry t WHERE t.contactId = :contactId";
        
        if (filter.getStartDate() != null) {
            query += " AND t.timestamp >= :startDate";
        }
        if (filter.getEventTypes() != null && !filter.getEventTypes().isEmpty()) {
            query += " AND t.eventType IN :types";
        }
        
        query += " ORDER BY t.timestamp DESC";
        
        TypedQuery<TimelineEntry> typedQuery = em.createQuery(query, TimelineEntry.class)
            .setParameter("contactId", contactId);
        
        if (filter.getStartDate() != null) {
            typedQuery.setParameter("startDate", filter.getStartDate());
        }
        if (filter.getEventTypes() != null && !filter.getEventTypes().isEmpty()) {
            typedQuery.setParameter("types", filter.getEventTypes());
        }
        
        List<TimelineEntry> entries = typedQuery
            .setFirstResult(filter.getOffset())
            .setMaxResults(filter.getLimit())
            .getResultList();
        
        return TimelineResponse.builder()
            .entries(entries)
            .hasMore(entries.size() == filter.getLimit())
            .build();
    }
}
```

#### Frontend: Timeline Component
```typescript
// components/ContactTimeline.tsx
export const ContactTimeline: React.FC<TimelineProps> = ({
  contactId,
  compact = false
}) => {
  const [filter, setFilter] = useState<TimelineFilter>({
    eventTypes: [],
    limit: 20
  });
  
  const { data: timeline, loading, fetchMore } = useTimeline(contactId, filter);
  
  const getEventIcon = (type: TimelineEventType): ReactNode => {
    const icons: Record<TimelineEventType, ReactNode> = {
      CONTACT_CREATED: <PersonAddIcon />,
      EMAIL_SENT: <EmailIcon />,
      CALL_LOGGED: <PhoneIcon />,
      MEETING_SCHEDULED: <EventIcon />,
      ORDER_PLACED: <ShoppingCartIcon />,
      INFO_UPDATED: <EditIcon />,
      NOTE_ADDED: <NoteIcon />
    };
    return icons[type] || <MoreHorizIcon />;
  };
  
  const getEventColor = (type: TimelineEventType): string => {
    const colors: Record<TimelineEventType, string> = {
      CONTACT_CREATED: 'primary',
      EMAIL_SENT: 'info',
      CALL_LOGGED: 'success',
      MEETING_SCHEDULED: 'warning',
      ORDER_PLACED: 'error',
      INFO_UPDATED: 'default',
      NOTE_ADDED: 'default'
    };
    return colors[type] || 'default';
  };
  
  return (
    <Box>
      {/* Filter Bar */}
      {!compact && (
        <Box sx={{ mb: 2, display: 'flex', gap: 2 }}>
          <FormControl size="small">
            <InputLabel>Event-Typ</InputLabel>
            <Select
              multiple
              value={filter.eventTypes}
              onChange={(e) => setFilter({...filter, eventTypes: e.target.value})}
              sx={{ minWidth: 200 }}
            >
              <MenuItem value="EMAIL_SENT">E-Mails</MenuItem>
              <MenuItem value="CALL_LOGGED">Anrufe</MenuItem>
              <MenuItem value="MEETING_SCHEDULED">Meetings</MenuItem>
              <MenuItem value="ORDER_PLACED">Bestellungen</MenuItem>
            </Select>
          </FormControl>
          
          <DateRangePicker
            startDate={filter.startDate}
            endDate={filter.endDate}
            onChange={(dates) => setFilter({...filter, ...dates})}
          />
        </Box>
      )}
      
      {/* Timeline */}
      <Timeline position={compact ? 'right' : 'alternate'}>
        {timeline?.entries.map((entry, index) => (
          <TimelineItem key={entry.id}>
            <TimelineOppositeContent
              sx={{ m: 'auto 0' }}
              align="right"
              variant="body2"
              color="text.secondary"
            >
              {format(new Date(entry.timestamp), 'dd.MM.yyyy HH:mm')}
            </TimelineOppositeContent>
            
            <TimelineSeparator>
              <TimelineConnector sx={{ bgcolor: 'grey.300' }} />
              <TimelineDot color={getEventColor(entry.eventType)}>
                {getEventIcon(entry.eventType)}
              </TimelineDot>
              <TimelineConnector sx={{ bgcolor: 'grey.300' }} />
            </TimelineSeparator>
            
            <TimelineContent sx={{ py: '12px', px: 2 }}>
              <Typography variant="h6" component="span">
                {entry.title}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                {entry.description}
              </Typography>
              {entry.metadata?.outcome && (
                <Chip 
                  label={entry.metadata.outcome} 
                  size="small" 
                  sx={{ mt: 1 }}
                  color={entry.metadata.outcome === 'positive' ? 'success' : 'default'}
                />
              )}
            </TimelineContent>
          </TimelineItem>
        ))}
      </Timeline>
      
      {/* Load More */}
      {timeline?.hasMore && (
        <Box sx={{ display: 'flex', justifyContent: 'center', mt: 2 }}>
          <Button 
            onClick={() => fetchMore()} 
            disabled={loading}
          >
            {loading ? <CircularProgress size={20} /> : 'Mehr laden'}
          </Button>
        </Box>
      )}
    </Box>
  );
};
```

### Freitag: KI-Ready Structure & Testing

#### Backend: ML-Ready Data Preparation
```java
// MLDataPreparationService.java
@ApplicationScoped
public class MLDataPreparationService {
    
    @Inject
    EventStore eventStore;
    
    // Prepare feature vectors for ML models
    public ContactFeatureVector prepareFeatures(UUID contactId) {
        List<BaseEvent> events = eventStore.getEvents(contactId);
        
        // Basic features
        int totalInteractions = countInteractions(events);
        double avgResponseTime = calculateAvgResponseTime(events);
        int daysAsCustomer = calculateCustomerAge(events);
        
        // Behavioral features
        Map<String, Integer> channelPreferences = extractChannelPreferences(events);
        Map<Integer, Integer> hourlyActivity = extractHourlyActivityPattern(events);
        Map<String, Double> productAffinities = calculateProductAffinities(events);
        
        // Temporal features
        double interactionTrend = calculateInteractionTrend(events);
        double seasonality = detectSeasonality(events);
        
        // Text features (for future NLP)
        List<String> communicationTexts = extractCommunicationTexts(events);
        
        return ContactFeatureVector.builder()
            .contactId(contactId)
            .basicFeatures(Map.of(
                "totalInteractions", (double) totalInteractions,
                "avgResponseTime", avgResponseTime,
                "customerAge", (double) daysAsCustomer
            ))
            .channelPreferences(channelPreferences)
            .hourlyActivity(hourlyActivity)
            .productAffinities(productAffinities)
            .temporalFeatures(Map.of(
                "trend", interactionTrend,
                "seasonality", seasonality
            ))
            .textData(communicationTexts)
            .build();
    }
    
    // Export training data for external ML systems
    public void exportTrainingData(ExportFormat format) {
        List<ContactFeatureVector> allVectors = contactRepository.findAll()
            .stream()
            .map(contact -> prepareFeatures(contact.getId()))
            .collect(Collectors.toList());
        
        switch (format) {
            case CSV -> exportAsCSV(allVectors);
            case PARQUET -> exportAsParquet(allVectors);
            case JSON -> exportAsJSON(allVectors);
        }
    }
}
```

#### Frontend: AI Suggestions Display
```typescript
// components/AIInsights.tsx
export const AIInsights: React.FC<AIInsightsProps> = ({
  contact
}) => {
  const { insights, loading } = useAIInsights(contact.id);
  
  if (loading) return <Skeleton variant="rectangular" height={200} />;
  if (!insights || insights.suggestions.length === 0) return null;
  
  return (
    <Card sx={{ backgroundColor: 'background.paper', border: '1px solid', borderColor: 'primary.main' }}>
      <CardContent>
        <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
          <AutoAwesomeIcon sx={{ mr: 1, color: 'primary.main' }} />
          <Typography variant="h6">
            KI-basierte Empfehlungen
          </Typography>
          <Chip 
            label="Beta" 
            size="small" 
            sx={{ ml: 1 }}
            color="primary"
            variant="outlined"
          />
        </Box>
        
        <List>
          {insights.suggestions.map((suggestion, idx) => (
            <ListItem key={idx} sx={{ px: 0 }}>
              <ListItemIcon>
                {suggestion.type === 'communication' && <ChatIcon />}
                {suggestion.type === 'opportunity' && <TrendingUpIcon />}
                {suggestion.type === 'risk' && <WarningIcon />}
              </ListItemIcon>
              <ListItemText
                primary={suggestion.title}
                secondary={
                  <Box>
                    <Typography variant="body2" color="text.secondary">
                      {suggestion.description}
                    </Typography>
                    <Box sx={{ mt: 1, display: 'flex', gap: 1 }}>
                      <Chip 
                        label={`Konfidenz: ${(suggestion.confidence * 100).toFixed(0)}%`}
                        size="small"
                        variant="outlined"
                      />
                      <Chip 
                        label={suggestion.impact}
                        size="small"
                        color={suggestion.impact === 'high' ? 'error' : 'default'}
                      />
                    </Box>
                  </Box>
                }
              />
              <ListItemSecondaryAction>
                <IconButton size="small" onClick={() => handleAction(suggestion)}>
                  <ArrowForwardIcon />
                </IconButton>
              </ListItemSecondaryAction>
            </ListItem>
          ))}
        </List>
        
        <Typography variant="caption" color="text.secondary" sx={{ mt: 2, display: 'block' }}>
          Basierend auf {insights.dataPoints} Datenpunkten • 
          Zuletzt aktualisiert: {format(new Date(insights.lastUpdated), 'dd.MM.yyyy HH:mm')}
        </Typography>
      </CardContent>
    </Card>
  );
};
```

#### Integration Tests
```typescript
// __tests__/relationship-features.integration.test.ts
describe('Relationship Features Integration', () => {
  it('should calculate warmth correctly', async () => {
    // Setup test contact with history
    const contact = await createTestContactWithHistory();
    
    // Get warmth calculation
    const warmth = await relationshipApi.getWarmth(contact.id);
    
    expect(warmth.temperature).toBe('warm');
    expect(warmth.score).toBeGreaterThan(60);
    expect(warmth.suggestions).toHaveLength(2);
  });
  
  it('should track analytics events', async () => {
    const contact = await createTestContact();
    
    // Log interaction
    await contactApi.logInteraction(contact.id, {
      type: 'call',
      duration: 300,
      outcome: 'positive'
    });
    
    // Verify analytics event created
    const events = await analyticsApi.getEvents({
      aggregateId: contact.id,
      eventType: 'CONTACT_INTERACTION'
    });
    
    expect(events).toHaveLength(1);
    expect(events[0].dimensions.interactionType).toBe('call');
  });
  
  it('should build timeline correctly', async () => {
    const contact = await createTestContactWithHistory();
    
    // Get timeline
    const timeline = await contactApi.getTimeline(contact.id);
    
    expect(timeline.entries).toHaveLength(5);
    expect(timeline.entries[0].eventType).toBe('ORDER_PLACED');
    expect(timeline.hasMore).toBe(false);
  });
});
```

## 📊 Wochenergebnis

### Deliverables
- ✅ Relationship Warmth Calculator funktioniert
- ✅ Beziehungsebene-UI komplett
- ✅ Analytics Events implementiert
- ✅ Timeline-Ansicht mit Filterung
- ✅ KI-Ready Data Structure
- ✅ Conversation Starters
- ✅ Birthday Reminders
- ✅ Cross-Sell Detection

### Metriken
- Feature Coverage: 95%
- Test Coverage: 90%
- Performance: Timeline < 100ms
- Analytics Latency: < 500ms

### Tech Debt
- [ ] ML Model Training Pipeline (Phase 3)
- [ ] Advanced Sentiment Analysis (später)
- [ ] Real-time Analytics (später)

## 🔗 Nächste Woche

[→ Woche 4: Polish & Integration](./WEEK4_POLISH_INTEGRATION.md)

---

**Navigation:**
- [← Woche 2: Features + Compliance](./WEEK2_FEATURES_COMPLIANCE.md)
- [→ Woche 4: Polish & Integration](./WEEK4_POLISH_INTEGRATION.md)