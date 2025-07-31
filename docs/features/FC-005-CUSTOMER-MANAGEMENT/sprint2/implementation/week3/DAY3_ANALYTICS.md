# 📆 Tag 3: Analytics Events Implementation

**Datum:** Mittwoch, 21. August 2025  
**Fokus:** Analytics Pipeline & Events  
**Ziel:** Datengetriebene Insights ermöglichen  

## 🧭 Navigation

**← Vorheriger Tag:** [Tag 2: Relationship](./DAY2_RELATIONSHIP.md)  
**↑ Woche 3 Übersicht:** [README.md](./README.md)  
**→ Nächster Tag:** [Tag 4: Timeline](./DAY4_TIMELINE.md)  
**📘 Spec:** [Analytics Specification](./specs/ANALYTICS_SPEC.md)  

## 🎯 Tagesziel

- Backend: Analytics Event Publisher
- Events: Interaction, Relationship, Cross-Sell
- Frontend: Analytics Dashboard Widget
- Metriken: Real-time Updates

## 📊 Analytics Architecture

```
User Action → Domain Event → Analytics Event
                                      │
                              ┌──────┴──────┐
                              │  Enrichment  │
                              └──────┬──────┘
                                      │
                              ┌──────┴──────┐
                              │    Metrics   │
                              │  Aggregation │
                              └──────┬──────┘
                                      │
                                 Dashboards
```

## 💻 Backend Implementation

### 1. Analytics Event Publisher

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
}
```

**Vollständiger Code:** [backend/AnalyticsEventPublisher.java](./code/backend/AnalyticsEventPublisher.java)

### 2. Cross-Sell Opportunity Detection

```java
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
```

### 3. Analytics Aggregation Service

```java
// AnalyticsAggregationService.java
@ApplicationScoped
public class AnalyticsAggregationService {
    
    @Scheduled(every = "1m")
    void aggregateMetrics() {
        // Aggregate interaction metrics
        InteractionMetrics hourly = calculateHourlyMetrics();
        persistMetrics(hourly);
        
        // Update relationship scores
        updateRelationshipScores();
        
        // Detect patterns
        detectEngagementPatterns();
    }
    
    private void detectEngagementPatterns() {
        // Find contacts with declining engagement
        List<UUID> decliningContacts = contactRepository
            .findWithDecliningEngagement(30); // Last 30 days
        
        for (UUID contactId : decliningContacts) {
            EngagementAlertEvent alert = EngagementAlertEvent.builder()
                .contactId(contactId)
                .pattern("DECLINING_ENGAGEMENT")
                .recommendation("Schedule proactive check-in")
                .build();
            
            eventBus.publish(alert);
        }
    }
}
```

## 🎨 Frontend Implementation

### Analytics Dashboard Widget

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

**Vollständiger Code:** [frontend/RelationshipAnalytics.tsx](./code/frontend/analytics/RelationshipAnalytics.tsx)

### Real-time Analytics Hook

```typescript
// hooks/useRealtimeAnalytics.ts
export const useRealtimeAnalytics = (contactId: string) => {
  const [metrics, setMetrics] = useState<RealtimeMetrics>();
  
  useEffect(() => {
    // WebSocket connection for real-time updates
    const ws = new WebSocket(`wss://api.freshplan.de/analytics/stream`);
    
    ws.onopen = () => {
      ws.send(JSON.stringify({
        action: 'subscribe',
        contactId
      }));
    };
    
    ws.onmessage = (event) => {
      const update = JSON.parse(event.data);
      setMetrics(prev => ({
        ...prev,
        ...update
      }));
    };
    
    return () => ws.close();
  }, [contactId]);
  
  return metrics;
};
```

## 📐 Event Types

### Analytics Event Schema

```typescript
interface AnalyticsEvent {
  eventId: string;
  eventType: AnalyticsEventType;
  aggregateId: string;
  timestamp: Date;
  dimensions: Record<string, string>;
  measures: Record<string, number>;
  userId: string;
  metadata?: {
    source: 'ui' | 'api' | 'system';
    version: string;
  };
}

enum AnalyticsEventType {
  CONTACT_INTERACTION = 'CONTACT_INTERACTION',
  RELATIONSHIP_CHANGE = 'RELATIONSHIP_CHANGE',
  CROSS_SELL_OPPORTUNITY = 'CROSS_SELL_OPPORTUNITY',
  ENGAGEMENT_PATTERN = 'ENGAGEMENT_PATTERN',
  MILESTONE_REACHED = 'MILESTONE_REACHED'
}
```

## 🧪 Tests

### Analytics Integration Test

```typescript
describe('Analytics Events', () => {
  it('should track interactions correctly', async () => {
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
    expect(events[0].measures.duration).toBe(300);
  });
  
  it('should detect cross-sell opportunities', async () => {
    const customer = await createTestCustomer();
    
    // Place order without certain categories
    await orderApi.placeOrder({
      customerId: customer.id,
      items: [
        { category: 'main-dishes', productId: 'p1' }
      ]
    });
    
    // Check for opportunities
    const opportunities = await analyticsApi.getCrossSellOpportunities(customer.id);
    
    expect(opportunities).toContainEqual(
      expect.objectContaining({
        missingCategories: expect.arrayContaining(['desserts', 'beverages'])
      })
    );
  });
});
```

## 📝 Checkliste

- [ ] Analytics Event Publisher implementiert
- [ ] Event Types definiert
- [ ] Cross-Sell Detection
- [ ] Relationship Tracking
- [ ] Frontend Dashboard
- [ ] Real-time Updates
- [ ] Tests geschrieben

## 🔗 Weiterführende Links

- **Event Patterns:** [Analytics Event Design](./guides/ANALYTICS_EVENT_DESIGN.md)
- **Metrics Guide:** [KPI Definitions](./guides/KPI_DEFINITIONS.md)
- **Nächster Schritt:** [→ Tag 4: Timeline Implementation](./DAY4_TIMELINE.md)

---

**Status:** 📋 Bereit zur Implementierung