# FC-007: Chef-Dashboard 📊 CLAUDE_TECH

**Feature Code:** FC-007  
**Optimiert für:** Claude's 30-Sekunden-Produktivität  
**Original:** 893 Zeilen → **Optimiert:** ~500 Zeilen (44% Reduktion)

## 🎯 QUICK-LOAD: Sofort produktiv in 30 Sekunden!

### Was macht FC-007?
**Real-time Dashboard für Führungskräfte mit KPIs, Live-Monitor und AI-Insights**

### Die 4 Dashboard-Bereiche:
1. **KPI Cards** → Pipeline, Forecast, Quote, Team-Score (Redis-cached)
2. **Live Monitor** → Wer macht gerade was? (WebSocket updates)
3. **Team Matrix** → Performance-Vergleich mit Trends
4. **AI Insights** → Proaktive Handlungsempfehlungen

### Sofort starten:
```bash
# Backend: GraphQL Schema erweitern
cd backend
./mvnw quarkus:add-extension -Dextensions="quarkus-smallrye-graphql,quarkus-redis-client"

# Frontend: Dashboard Dependencies
cd frontend
npm install @apollo/client subscriptions-transport-ws recharts @tremor/react
```

---

## 📊 1. BACKEND: GraphQL API & Analytics

### 1.1 GraphQL Schema (Copy-paste ready)
```graphql
type Query {
  dashboardData: DashboardData!
  teamPerformance(period: TimePeriod!): TeamPerformance!
  liveActivities(limit: Int = 10): [Activity!]!
  aiInsights(context: InsightContext): [AIInsight!]!
}

type Subscription {
  activityStream: Activity!
  kpiUpdates: KPIUpdate!
}

type DashboardData {
  kpis: KPIOverview!
  pipeline: PipelineMetrics!
  teamMatrix: [TeamMember!]!
  recentActivities: [Activity!]!
}

type KPIOverview {
  totalPipeline: Money!
  forecast: Money!
  conversionRate: Float!
  averageDealSize: Money!
  teamScore: Float!
  trends: KPITrends!
}

type TeamMember {
  id: ID!
  name: String!
  avatar: String
  score: Float!
  trend: Trend!
  metrics: PersonalMetrics!
  currentActivity: Activity
}

type Activity {
  id: ID!
  type: ActivityType!
  user: User!
  subject: String!
  timestamp: DateTime!
  duration: Int
  metadata: JSON
}

enum ActivityType {
  CALL_STARTED
  CALL_ENDED
  MEETING_SCHEDULED
  OFFER_CREATED
  DEAL_WON
  EMAIL_SENT
  NOTE_ADDED
}

type AIInsight {
  id: ID!
  priority: InsightPriority!
  type: InsightType!
  message: String!
  actionable: Boolean!
  suggestedAction: SuggestedAction
  affectedUsers: [User!]
}
```

### 1.2 Analytics Engine (10 Minuten)
```java
@ApplicationScoped
public class AnalyticsEngine {
    
    @Inject RedisClient redis;
    @Inject OpportunityRepository opportunityRepo;
    @Inject ActivityRepository activityRepo;
    
    @CacheResult(cacheName = "dashboard-kpis")
    public KPIOverview calculateKPIs() {
        // Aggregate pipeline value
        BigDecimal totalPipeline = opportunityRepo
            .find("status IN ?1", 
                List.of(OPEN, QUALIFIED, PROPOSAL, NEGOTIATION))
            .stream()
            .map(Opportunity::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
            
        // Calculate forecast (weighted by stage probability)
        BigDecimal forecast = opportunityRepo
            .streamAll()
            .filter(o -> o.getExpectedCloseDate().isBefore(
                LocalDate.now().plusMonths(1)))
            .map(o -> o.getAmount().multiply(
                getStageWeight(o.getStage())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
            
        // Conversion rate (last 30 days)
        long wonDeals = opportunityRepo.count(
            "status = ?1 AND closedAt > ?2", 
            WON, LocalDateTime.now().minusDays(30));
            
        long totalDeals = opportunityRepo.count(
            "closedAt > ?1", 
            LocalDateTime.now().minusDays(30));
            
        double conversionRate = totalDeals > 0 ? 
            (double) wonDeals / totalDeals * 100 : 0;
            
        // Team score calculation
        double teamScore = calculateTeamScore();
        
        return KPIOverview.builder()
            .totalPipeline(totalPipeline)
            .forecast(forecast)
            .conversionRate(conversionRate)
            .teamScore(teamScore)
            .trends(calculateTrends())
            .build();
    }
    
    private double calculateTeamScore() {
        List<TeamMember> team = getTeamMembers();
        
        return team.stream()
            .mapToDouble(member -> {
                double activityScore = getActivityScore(member);
                double conversionScore = getConversionScore(member);
                double responseScore = getResponseTimeScore(member);
                
                return (activityScore * 0.3 + 
                        conversionScore * 0.5 + 
                        responseScore * 0.2);
            })
            .average()
            .orElse(0.0);
    }
    
    @Scheduled(every = "5m")
    void refreshCache() {
        // Invalidate cache to ensure fresh data
        cacheManager.getCache("dashboard-kpis").invalidateAll();
    }
}

// Real-time Activity Tracker
@ApplicationScoped
public class ActivityTracker {
    
    @Inject EventBus eventBus;
    @Inject WebSocketBroadcaster broadcaster;
    
    public void onCallStarted(@Observes CallStartedEvent event) {
        Activity activity = Activity.builder()
            .type(ActivityType.CALL_STARTED)
            .user(event.getUser())
            .subject(event.getCustomerName())
            .timestamp(LocalDateTime.now())
            .metadata(Map.of("phoneNumber", event.getPhoneNumber()))
            .build();
            
        // Broadcast to all dashboard subscribers
        broadcaster.broadcast("activity-stream", activity);
        
        // Update user status
        updateUserStatus(event.getUserId(), "IN_CALL");
    }
    
    public void onOfferCreated(@Observes OfferCreatedEvent event) {
        Activity activity = Activity.builder()
            .type(ActivityType.OFFER_CREATED)
            .user(event.getUser())
            .subject(event.getOfferTitle())
            .timestamp(LocalDateTime.now())
            .metadata(Map.of(
                "amount", event.getAmount(),
                "customerId", event.getCustomerId()
            ))
            .build();
            
        broadcaster.broadcast("activity-stream", activity);
        
        // Trigger KPI update
        eventBus.publish(new KPIUpdateEvent("offer_created"));
    }
}
```

### 1.3 AI Insights Generator (5 Minuten)
```java
@ApplicationScoped
public class InsightGenerator {
    
    @Inject TeamPerformanceAnalyzer analyzer;
    @Inject PipelineAnalyzer pipelineAnalyzer;
    
    public List<AIInsight> generateInsights(InsightContext context) {
        List<AIInsight> insights = new ArrayList<>();
        
        // Performance insights
        insights.addAll(generatePerformanceInsights());
        
        // Pipeline insights
        insights.addAll(generatePipelineInsights());
        
        // Activity insights
        insights.addAll(generateActivityInsights());
        
        // Sort by priority and return top 5
        return insights.stream()
            .sorted(Comparator.comparing(AIInsight::getPriority))
            .limit(5)
            .collect(Collectors.toList());
    }
    
    private List<AIInsight> generatePerformanceInsights() {
        return analyzer.getTopPerformers().stream()
            .filter(member -> member.getTrend() == INCREASING)
            .map(member -> AIInsight.builder()
                .priority(HIGH)
                .type(PERFORMANCE_IMPROVEMENT)
                .message(String.format(
                    "%s zeigt 15%% Steigerung - Zeit für Coaching-Gespräch über Account-Expansion",
                    member.getName()
                ))
                .actionable(true)
                .suggestedAction(new SuggestedAction(
                    "Schedule 1:1",
                    "/calendar/new?type=coaching&user=" + member.getId()
                ))
                .affectedUsers(List.of(member))
                .build())
            .collect(Collectors.toList());
    }
}
```

---

## 🎨 2. FRONTEND: React Dashboard Components

### 2.1 Main Dashboard Component (10 Minuten)
```typescript
export const ChefDashboard: React.FC = () => {
  // GraphQL subscriptions for real-time updates
  const { data, loading } = useQuery(DASHBOARD_QUERY, {
    pollInterval: 30000 // Fallback polling
  });
  
  const { data: liveActivity } = useSubscription(ACTIVITY_SUBSCRIPTION);
  const { data: kpiUpdate } = useSubscription(KPI_UPDATE_SUBSCRIPTION);
  
  // Merge real-time updates
  const activities = useMemo(() => {
    if (!data?.liveActivities) return [];
    
    const merged = [...data.liveActivities];
    if (liveActivity) {
      merged.unshift(liveActivity.activityStream);
      return merged.slice(0, 10); // Keep only 10 most recent
    }
    return merged;
  }, [data?.liveActivities, liveActivity]);
  
  return (
    <DashboardLayout>
      {/* KPI Cards Row */}
      <Grid container spacing={3} sx={{ mb: 3 }}>
        <Grid item xs={12} md={3}>
          <KPICard
            title="Pipeline"
            value={formatCurrency(data?.dashboardData.kpis.totalPipeline)}
            trend={data?.dashboardData.kpis.trends.pipeline}
            icon={<TrendingUpIcon />}
          />
        </Grid>
        <Grid item xs={12} md={3}>
          <KPICard
            title="Forecast (30d)"
            value={formatCurrency(data?.dashboardData.kpis.forecast)}
            trend={data?.dashboardData.kpis.trends.forecast}
            icon={<TimelineIcon />}
          />
        </Grid>
        <Grid item xs={12} md={3}>
          <KPICard
            title="Conversion Rate"
            value={`${data?.dashboardData.kpis.conversionRate.toFixed(1)}%`}
            trend={data?.dashboardData.kpis.trends.conversion}
            icon={<CheckCircleIcon />}
          />
        </Grid>
        <Grid item xs={12} md={3}>
          <KPICard
            title="Team Score"
            value={data?.dashboardData.kpis.teamScore.toFixed(1)}
            subtitle="/10"
            trend={data?.dashboardData.kpis.trends.teamScore}
            icon={<GroupIcon />}
          />
        </Grid>
      </Grid>
      
      {/* Main Content Row */}
      <Grid container spacing={3}>
        {/* Live Monitor */}
        <Grid item xs={12} md={6}>
          <LiveActivityMonitor activities={activities} />
        </Grid>
        
        {/* Team Performance Matrix */}
        <Grid item xs={12} md={6}>
          <TeamPerformanceMatrix 
            team={data?.dashboardData.teamMatrix || []} 
          />
        </Grid>
        
        {/* AI Insights */}
        <Grid item xs={12}>
          <AIInsightsPanel insights={data?.aiInsights || []} />
        </Grid>
      </Grid>
    </DashboardLayout>
  );
};

// Live Activity Monitor Component
const LiveActivityMonitor: React.FC<{ activities: Activity[] }> = ({ 
  activities 
}) => {
  return (
    <Card>
      <CardHeader
        title={
          <Box display="flex" alignItems="center" gap={1}>
            <PulseIndicator />
            <Typography variant="h6">Live Monitor</Typography>
          </Box>
        }
      />
      <CardContent>
        <List>
          {activities.map((activity, index) => (
            <ActivityItem
              key={activity.id}
              activity={activity}
              isNew={index === 0}
            />
          ))}
        </List>
      </CardContent>
    </Card>
  );
};

// Activity Item with Animation
const ActivityItem: React.FC<{ activity: Activity; isNew: boolean }> = ({ 
  activity, 
  isNew 
}) => {
  const spring = useSpring({
    from: { opacity: 0, transform: 'translateX(-20px)' },
    to: { opacity: 1, transform: 'translateX(0px)' },
    config: { tension: 200, friction: 20 }
  });
  
  return (
    <animated.div style={isNew ? spring : {}}>
      <ListItem>
        <ListItemAvatar>
          <Avatar src={activity.user.avatar}>
            {activity.user.name.charAt(0)}
          </Avatar>
        </ListItemAvatar>
        <ListItemText
          primary={
            <Box display="flex" alignItems="center" gap={1}>
              <ActivityIcon type={activity.type} />
              <Typography variant="body2">
                {activity.user.name}
              </Typography>
              <ActivityTypeChip type={activity.type} />
            </Box>
          }
          secondary={
            <>
              {activity.subject}
              <Typography variant="caption" color="text.secondary">
                {' • '}
                {formatRelativeTime(activity.timestamp)}
              </Typography>
            </>
          }
        />
      </ListItem>
    </animated.div>
  );
};
```

### 2.2 Team Performance Matrix (5 Minuten)
```typescript
const TeamPerformanceMatrix: React.FC<{ team: TeamMember[] }> = ({ team }) => {
  return (
    <Card>
      <CardHeader title="Team Performance Matrix" />
      <CardContent>
        <Grid container spacing={2}>
          {team.map(member => (
            <Grid item xs={12} key={member.id}>
              <TeamMemberCard member={member} />
            </Grid>
          ))}
        </Grid>
      </CardContent>
    </Card>
  );
};

const TeamMemberCard: React.FC<{ member: TeamMember }> = ({ member }) => {
  const scoreColor = member.score >= 8 ? 'success' : 
                     member.score >= 6 ? 'warning' : 'error';
  
  return (
    <Paper sx={{ p: 2 }}>
      <Box display="flex" alignItems="center" justifyContent="space-between">
        <Box display="flex" alignItems="center" gap={2}>
          <Avatar src={member.avatar}>{member.name.charAt(0)}</Avatar>
          <Box>
            <Typography variant="subtitle1">{member.name}</Typography>
            {member.currentActivity && (
              <Typography variant="caption" color="text.secondary">
                {member.currentActivity.type.toLowerCase().replace('_', ' ')}
              </Typography>
            )}
          </Box>
        </Box>
        
        <Box display="flex" alignItems="center" gap={2}>
          <Box textAlign="center">
            <Typography variant="h5" color={`${scoreColor}.main`}>
              {member.score.toFixed(1)}
            </Typography>
            <Typography variant="caption">/10</Typography>
          </Box>
          <TrendIndicator trend={member.trend} />
        </Box>
      </Box>
      
      {/* Mini metrics */}
      <Box display="flex" gap={2} mt={2}>
        <Chip 
          size="small" 
          label={`${member.metrics.dealsWon} Deals`}
          icon={<CheckIcon />}
        />
        <Chip 
          size="small" 
          label={`${formatCurrency(member.metrics.revenue)}`}
          icon={<EuroIcon />}
        />
        <Chip 
          size="small" 
          label={`${member.metrics.activities} Activities`}
          icon={<TimelineIcon />}
        />
      </Box>
    </Paper>
  );
};
```

### 2.3 Real-time Updates with WebSocket
```typescript
// WebSocket setup
const wsLink = new WebSocketLink({
  uri: `ws://localhost:8080/graphql`,
  options: {
    reconnect: true,
    connectionParams: {
      authToken: getAuthToken()
    }
  }
});

// Subscription hooks
const ACTIVITY_SUBSCRIPTION = gql`
  subscription OnActivityStream {
    activityStream {
      id
      type
      user {
        id
        name
        avatar
      }
      subject
      timestamp
      metadata
    }
  }
`;

const KPI_UPDATE_SUBSCRIPTION = gql`
  subscription OnKPIUpdate {
    kpiUpdates {
      metric
      newValue
      previousValue
      change
    }
  }
`;
```

---

## 📈 3. PERFORMANCE & CACHING

### 3.1 Redis Caching Strategy
```java
@ApplicationScoped
public class DashboardCache {
    
    @Inject RedisClient redis;
    
    private static final String KPI_KEY = "dashboard:kpis";
    private static final int TTL_SECONDS = 300; // 5 minutes
    
    public CompletionStage<KPIOverview> getCachedKPIs() {
        return redis.get(KPI_KEY)
            .thenApply(response -> {
                if (response != null) {
                    return Json.decodeValue(response.toString(), KPIOverview.class);
                }
                return null;
            });
    }
    
    public CompletionStage<Void> cacheKPIs(KPIOverview kpis) {
        String json = Json.encode(kpis);
        return redis.setex(KPI_KEY, TTL_SECONDS, json)
            .thenApply(r -> null);
    }
}
```

---

## ✅ 4. TESTING & DEPLOYMENT

```typescript
// Real-time update test
describe('Dashboard Real-time Updates', () => {
  it('should update activities when subscription fires', async () => {
    const { result, waitFor } = renderHook(() => 
      useSubscription(ACTIVITY_SUBSCRIPTION)
    );
    
    // Simulate WebSocket message
    mockWebSocket.send({
      type: 'data',
      payload: {
        data: {
          activityStream: mockActivity
        }
      }
    });
    
    await waitFor(() => {
      expect(result.current.data).toBeDefined();
      expect(result.current.data.activityStream).toEqual(mockActivity);
    });
  });
});
```

---

## 🎯 IMPLEMENTATION PRIORITIES

1. **Phase 1 (2 Tage)**: GraphQL Schema + Basic KPIs
2. **Phase 2 (2 Tage)**: Real-time Activity Stream
3. **Phase 3 (2 Tage)**: Team Performance Matrix
4. **Phase 4 (1 Tag)**: AI Insights Integration

**Geschätzter Aufwand:** 7 Entwicklungstage