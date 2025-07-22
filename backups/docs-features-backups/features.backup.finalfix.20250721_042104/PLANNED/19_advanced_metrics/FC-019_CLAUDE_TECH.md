# FC-019 CLAUDE_TECH: Advanced Sales Metrics

**CLAUDE TECH** | **Original:** 1221 Zeilen → **Optimiert:** 450 Zeilen (63% Reduktion!)  
**Feature-Typ:** 🔀 FULLSTACK | **Priorität:** HOCH | **Geschätzter Aufwand:** 5-6 Tage

## ⚡ QUICK-LOAD (30 Sekunden bis produktiv!)

**Real-time Analytics Dashboard mit intelligenter KPI-Berechnung und Bottleneck-Detection**

### 🎯 Das macht es:
- **Real-time Metrics**: Sales Velocity, Win Rate, Deal Size, Pipeline Value in <30s
- **Bottleneck Detection**: Erkennt steckengebliebene Deals automatisch mit Empfehlungen
- **Predictive Analytics**: ML-basierte Prognosen für Forecast-Genauigkeit >85%
- **Alert Engine**: Kritische Metriken lösen sofortige Benachrichtigungen aus

### 🏗️ Architektur:
```
Event Sources → Metrics Engine → Cache Layer → API → Dashboard
     ↓              ↓               ↓          ↓       ↓
Opportunities  Calculator      Redis       REST    Charts
Activities     Aggregator      MaterialV.  WebSocket Alerts
Payments       Predictions     EventStore  Polling  Export
```

### 📊 Key Metrics:
- **Sales Velocity**: Ø Deal-Durchlaufzeit nach Stage
- **Win Rate**: Conversion-Rate nach Stage/Team/User  
- **Bottlenecks**: Deals >30 Tage in Stage + Handlungsempfehlungen
- **Forecast**: Predictive Revenue basierend auf Pipeline + Trends

---

## 📋 COPY-PASTE READY RECIPES

### 🔧 Backend Starter Kit

#### 1. Metrics API Resource:
```java
@Path("/api/metrics")
@ApplicationScoped
public class MetricsResource {
    
    @Inject MetricsService metricsService;
    
    @GET @Path("/dashboard")
    @RolesAllowed({"admin", "manager", "sales"})
    public Response getDashboardMetrics(
        @QueryParam("period") @DefaultValue("30d") String period,
        @QueryParam("team") String teamId
    ) {
        MetricsFilter filter = MetricsFilter.builder()
            .period(parsePeriod(period))
            .teamId(parseUUID(teamId))
            .build();
            
        DashboardMetrics metrics = metricsService.getDashboardMetrics(filter);
        return Response.ok(metrics).build();
    }
    
    @GET @Path("/bottlenecks")
    public Response getBottlenecks(
        @QueryParam("threshold") @DefaultValue("30") int thresholdDays
    ) {
        BottleneckAnalysis bottlenecks = metricsService.detectBottlenecks(thresholdDays);
        return Response.ok(bottlenecks).build();
    }
}
```

#### 2. Metrics Business Logic:
```java
@ApplicationScoped
@Transactional
public class MetricsService {
    
    @Inject OpportunityRepository opportunityRepo;
    @Inject MetricsCache cache;
    @Inject Event<MetricCalculatedEvent> metricEvents;
    
    public DashboardMetrics getDashboardMetrics(MetricsFilter filter) {
        String cacheKey = generateCacheKey("dashboard", filter);
        
        return cache.getOrCalculate(cacheKey, Duration.ofMinutes(5), () -> {
            return DashboardMetrics.builder()
                .velocity(calculateVelocity(filter))
                .winRates(calculateWinRates(filter))
                .dealSizes(calculateDealSizes(filter))
                .bottlenecks(detectBottlenecks(filter))
                .alerts(generateAlerts(filter))
                .build();
        });
    }
    
    public BottleneckAnalysis detectBottlenecks(int thresholdDays) {
        List<Opportunity> stuckOpportunities = opportunityRepo
            .findStuckInStage(thresholdDays);
            
        Map<OpportunityStage, List<Opportunity>> bottlenecksByStage = 
            stuckOpportunities.stream()
                .collect(Collectors.groupingBy(Opportunity::getStage));
                
        List<Bottleneck> bottlenecks = bottlenecksByStage.entrySet().stream()
            .map(entry -> Bottleneck.builder()
                .stage(entry.getKey())
                .stuckDeals(entry.getValue().size())
                .averageStuckTime(calculateAverageStuckTime(entry.getValue()))
                .potentialValue(calculateTotalValue(entry.getValue()))
                .recommendations(generateRecommendations(entry.getKey()))
                .build())
            .collect(Collectors.toList());
            
        return BottleneckAnalysis.builder()
            .bottlenecks(bottlenecks)
            .totalStuckValue(calculateTotalValue(stuckOpportunities))
            .actionItems(generateActionItems(bottlenecks))
            .build();
    }
}
```

#### 3. Database Schema:
```sql
-- V7.0__create_metrics_tables.sql
CREATE TABLE metrics_snapshots (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    snapshot_date DATE NOT NULL,
    metric_type VARCHAR(50) NOT NULL,
    metric_value DECIMAL(15,4) NOT NULL,
    team_id UUID REFERENCES teams(id),
    user_id UUID REFERENCES users(id),
    dimensions JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE metrics_realtime (
    metric_key VARCHAR(200) PRIMARY KEY,
    metric_value DECIMAL(15,4) NOT NULL,
    last_calculated TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    calculation_metadata JSONB
);

-- Performance-Indizes
CREATE INDEX idx_metrics_snapshots_date_type 
    ON metrics_snapshots(snapshot_date, metric_type);
CREATE INDEX idx_metrics_snapshots_team_date 
    ON metrics_snapshots(team_id, snapshot_date);

-- Materialized View für Sales Velocity
CREATE MATERIALIZED VIEW mv_sales_velocity AS
SELECT 
    DATE_TRUNC('week', closed_at) as week,
    team_id,
    AVG(EXTRACT(DAYS FROM (closed_at - created_at))) as avg_velocity,
    COUNT(*) as deals_count
FROM opportunities 
WHERE status = 'WON' 
AND closed_at >= CURRENT_DATE - INTERVAL '1 year'
GROUP BY DATE_TRUNC('week', closed_at), team_id;
```

### 🎨 Frontend Starter Kit

#### 1. Metrics Dashboard Component:
```typescript
// MetricsDashboard.tsx
export const MetricsDashboard: React.FC = () => {
  const [filters, setFilters] = useState<MetricsFilters>({
    period: '30d',
    teamId: null
  });
  
  const { data: metrics, isLoading } = useMetricsDashboard(filters);
  const { data: realtimeUpdates } = useMetricsRealtime(filters);
  
  // Real-time Updates via Polling
  useInterval(() => refetch(), 30000);
  
  if (isLoading) return <MetricsDashboardSkeleton />;
  
  return (
    <Box sx={{ p: 3 }}>
      <MetricsHeader 
        filters={filters} 
        onFiltersChange={setFilters}
        lastUpdated={metrics?.lastUpdated}
      />
      
      <Grid container spacing={3}>
        {/* Key Metrics Row */}
        <Grid item xs={12}>
          <Grid container spacing={2}>
            <Grid item xs={3}>
              <MetricCard
                title="Sales Velocity"
                value={metrics?.velocity.overall}
                unit="Tage"
                trend={metrics?.velocity.trend}
                color="primary"
              />
            </Grid>
            <Grid item xs={3}>
              <MetricCard
                title="Win Rate"
                value={metrics?.winRates.overall}
                unit="%"
                trend={metrics?.winRates.trend}
                color="success"
              />
            </Grid>
            <Grid item xs={3}>
              <MetricCard
                title="Avg. Deal Size"
                value={metrics?.dealSizes.average}
                unit="€"
                trend={metrics?.dealSizes.trend}
                color="info"
              />
            </Grid>
            <Grid item xs={3}>
              <MetricCard
                title="Pipeline Value"
                value={metrics?.pipeline.totalValue}
                unit="€"
                trend={metrics?.pipeline.trend}
                color="warning"
              />
            </Grid>
          </Grid>
        </Grid>
        
        {/* Charts Row */}
        <Grid item xs={8}>
          <Paper sx={{ p: 3, height: 400 }}>
            <Typography variant="h6">Sales Velocity Trend</Typography>
            <VelocityChart data={metrics?.velocity.chartData} />
          </Paper>
        </Grid>
        
        <Grid item xs={4}>
          <Paper sx={{ p: 3, height: 400 }}>
            <Typography variant="h6">Win Rate by Stage</Typography>
            <WinRateChart data={metrics?.winRates.byStage} />
          </Paper>
        </Grid>
        
        {/* Bottlenecks Section */}
        <Grid item xs={12}>
          <BottleneckAnalysisWidget 
            bottlenecks={metrics?.bottlenecks}
            onActionClick={handleBottleneckAction}
          />
        </Grid>
      </Grid>
    </Box>
  );
};
```

#### 2. Metric Card Component:
```typescript
// MetricCard.tsx
export const MetricCard: React.FC<MetricCardProps> = ({
  title, value, unit, trend, color, onClick
}) => {
  const trendIcon = trend?.direction === 'up' ? 
    <TrendingUpIcon color="success" /> : 
    <TrendingDownIcon color="error" />;
    
  return (
    <Card onClick={onClick} sx={{ cursor: onClick ? 'pointer' : 'default' }}>
      <CardContent>
        <Box display="flex" justifyContent="space-between">
          <Box>
            <Typography variant="body2" color="text.secondary">
              {title}
            </Typography>
            <Typography variant="h4" color={`${color}.main`}>
              {formatMetricValue(value, unit)}
            </Typography>
            {trend && (
              <Box display="flex" alignItems="center" mt={1}>
                {trendIcon}
                <Typography variant="body2" sx={{ ml: 0.5 }}>
                  {trend.percentage}% {trend.direction === 'up' ? 'höher' : 'niedriger'}
                </Typography>
              </Box>
            )}
          </Box>
          <Avatar sx={{ bgcolor: `${color}.light` }}>
            <TrendingUpIcon />
          </Avatar>
        </Box>
      </CardContent>
    </Card>
  );
};
```

#### 3. Bottleneck Analysis Widget:
```typescript
// BottleneckAnalysisWidget.tsx
export const BottleneckAnalysisWidget: React.FC<BottleneckAnalysisProps> = ({
  bottlenecks, onActionClick
}) => {
  const criticalBottlenecks = bottlenecks?.filter(b => b.severity === 'CRITICAL') || [];
  
  return (
    <Paper sx={{ p: 3 }}>
      <Box display="flex" justifyContent="space-between" mb={3}>
        <Typography variant="h6">Pipeline Bottlenecks</Typography>
        <Chip 
          label={`${criticalBottlenecks.length} kritisch`}
          color={criticalBottlenecks.length > 0 ? 'error' : 'success'}
        />
      </Box>
      
      {bottlenecks?.length === 0 ? (
        <Box textAlign="center" py={4}>
          <CheckCircleIcon color="success" sx={{ fontSize: 48, mb: 2 }} />
          <Typography variant="h6" color="success.main">
            Keine Bottlenecks erkannt
          </Typography>
        </Box>
      ) : (
        <Grid container spacing={2}>
          {bottlenecks?.map((bottleneck, index) => (
            <Grid item xs={6} key={index}>
              <Card variant="outlined">
                <CardContent>
                  <Box display="flex" justifyContent="space-between">
                    <Box>
                      <Typography variant="subtitle1" fontWeight="bold">
                        {bottleneck.stage.label}
                      </Typography>
                      <Typography variant="body2" color="text.secondary">
                        {bottleneck.stuckDeals} Deals seit {bottleneck.averageStuckTime} Tagen
                      </Typography>
                      <Typography variant="h6" color="error.main" sx={{ mt: 1 }}>
                        {formatCurrency(bottleneck.potentialValue)} blockiert
                      </Typography>
                    </Box>
                    <Chip 
                      label={bottleneck.severity}
                      color={getSeverityColor(bottleneck.severity)}
                    />
                  </Box>
                  
                  <Box mt={2}>
                    <Typography variant="body2" fontWeight="medium">
                      Empfehlungen:
                    </Typography>
                    {bottleneck.recommendations.map((rec, idx) => (
                      <Typography key={idx} variant="body2" sx={{ mb: 0.5 }}>
                        • {rec}
                      </Typography>
                    ))}
                  </Box>
                  
                  <Button 
                    size="small" 
                    variant="outlined"
                    onClick={() => onActionClick?.(bottleneck)}
                  >
                    Details anzeigen
                  </Button>
                </CardContent>
              </Card>
            </Grid>
          ))}
        </Grid>
      )}
    </Paper>
  );
};
```

#### 4. React Query Hooks:
```typescript
// useMetricsDashboard.ts
export const useMetricsDashboard = (filters: MetricsFilters) => {
  return useQuery(
    ['metrics', 'dashboard', filters],
    () => fetchDashboardMetrics(filters),
    {
      staleTime: 25000, // 25s
      refetchInterval: 30000, // 30s Polling
      retry: 3
    }
  );
};

// useMetricsRealtime.ts  
export const useMetricsRealtime = (filters: MetricsFilters) => {
  return useQuery(
    ['metrics', 'realtime', filters],
    () => fetchRealtimeMetrics(filters),
    {
      refetchInterval: 10000, // 10s für kritische Metriken
      refetchIntervalInBackground: true
    }
  );
};
```

### 🧠 Smart Alerts Engine:
```typescript
// MetricsAlertEngine.ts
export class MetricsAlertEngine {
  
  static checkVelocityAlert(velocity: VelocityMetrics): Alert[] {
    const alerts: Alert[] = [];
    
    if (velocity.trend.direction === 'down' && velocity.trend.percentage > 20) {
      alerts.push({
        type: 'WARNING',
        title: 'Sales Velocity verschlechtert',
        message: `Velocity ist um ${velocity.trend.percentage}% gesunken`,
        action: 'Bottleneck-Analyse durchführen',
        priority: 'HIGH'
      });
    }
    
    return alerts;
  }
  
  static checkBottleneckAlert(bottlenecks: BottleneckAnalysis): Alert[] {
    const alerts: Alert[] = [];
    
    const criticalValue = bottlenecks.bottlenecks
      .filter(b => b.severity === 'CRITICAL')
      .reduce((sum, b) => sum + b.potentialValue, 0);
      
    if (criticalValue > 100000) {
      alerts.push({
        type: 'CRITICAL',
        title: 'Kritische Pipeline-Blockade',
        message: `${formatCurrency(criticalValue)} in kritischen Bottlenecks`,
        action: 'Sofortige Intervention erforderlich',
        priority: 'CRITICAL'
      });
    }
    
    return alerts;
  }
}
```

---

## 📊 IMPLEMENTIERUNGSPLAN

### Phase 1: Core Metrics (3 Tage)
1. **Backend Foundation** (1.5 Tage)
   - MetricsSnapshot Entity + Flyway Migration
   - MetricsService mit Velocity/Win Rate Berechnung
   - MetricsResource API mit Caching
   
2. **Dashboard Frontend** (1.5 Tage)
   - MetricsDashboard mit Grid-Layout
   - MetricCard Component + Chart.js Integration
   - Real-time Updates via Polling

### Phase 2: Advanced Analytics (2 Tage)
1. **Bottleneck Detection** (1 Tag)
   - Stuck Deals Algorithm + Recommendations
   - BottleneckAnalysisWidget mit Severity-Anzeige
   
2. **Predictive Features** (1 Tag)
   - Forecast Engine mit Linear Regression
   - Alert Engine für kritische Metriken

### Phase 3: Integration (1 Tag)
1. **M3 Sales Cockpit Integration** (0.5 Tage)
2. **Performance Optimization** (0.5 Tage)

---

## 🎯 BUSINESS VALUE

### ROI Metriken:
- **Forecast-Genauigkeit**: 60% → 85% durch datengetriebene Pipeline-Analyse
- **Problem-Erkennung**: 2-4 Wochen → Real-time durch automatische Bottleneck-Detection  
- **Sales Velocity**: +20% durch systematische Schwachstellen-Beseitigung
- **Team-Performance**: Objektive Metriken statt subjektive Einschätzungen

### Technische Vorteile:
- **Real-time Performance**: <30s Metrik-Updates, <2s Dashboard-Load
- **Skalierbarkeit**: 10.000+ Opportunities mit komplexen Aggregationen
- **Integration**: Nahtlos mit M6 Analytics + FC-005 Xentral
- **Predictive Analytics**: ML-basierte Trends und Anomalie-Detection

---

## 🔗 INTEGRATION POINTS

### Dependencies:
- **M6 Analytics Module**: Chart-Infrastructure und Analytics Framework
- **FC-005 Xentral**: Payment Events für Revenue Metrics  
- **M4 Opportunity Pipeline**: Hauptdatenquelle für Sales Metrics

### Enables:
- **FC-007 Chef-Dashboard**: Executive Metrics View
- **FC-026 Analytics Platform**: Advanced Analytics Foundation
- **FC-027 Magic Moments**: Insight-driven Recommendations

---

## ⚠️ WICHTIGE ENTSCHEIDUNGEN

1. **Data Storage**: Hybrid Real-time Cache + tägliche Snapshots für Performance + Historie
2. **Charts**: Chart.js für MVP (einfach), D3.js für komplexe Features später
3. **Predictions**: Linear Regression für MVP, ML-Integration in Phase 4
4. **Updates**: 30s Polling (konsistent mit D2-Entscheidung aus UI Foundation)

---

**Status:** Ready for Implementation | **Next:** Backend Foundation starten