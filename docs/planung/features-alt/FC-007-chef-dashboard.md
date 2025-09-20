# FC-007: Chef-Dashboard & Führungs-KPIs

**Feature Code:** FC-007  
**Status:** 📋 Planungsphase  
**Geschätzter Aufwand:** 7-8 Tage  
**Priorität:** HOCH - Führungskräfte brauchen Transparenz  
**Phase:** 4.2 (Führungs-Tools)  

## 🎯 Zusammenfassung

"Was tun meine Verkäufer? Wieviel Geschäft ist in der Pipeline? Wie ist die Abschlussquote?" - Diese Fragen müssen Führungskräfte auf einen Blick beantworten können. Das Chef-Dashboard liefert Echtzeit-Einblicke und Handlungsempfehlungen.

## 📊 Kern-Metriken auf einen Blick

```
Dashboard-Übersicht:
┌─────────────────────────────────────────────────────┐
│ Pipeline-Wert: 2.450.000€  | Forecast: 735.000€     │
│ Abschlussquote: 32%        | ⬆️ +5% zum Vormonat    │
└─────────────────────────────────────────────────────┘
```

## 🏗️ Technische Architektur

### 1. Analytics Engine (Backend)

```java
@ApplicationScoped
public class SalesAnalyticsService {
    
    // Aggregierte Metriken mit Caching
    @Cacheable(cacheName = "sales-metrics", keyGenerator = MetricKeyGenerator.class)
    public SalesMetrics calculateMetrics(MetricRequest request) {
        return SalesMetrics.builder()
            .pipelineValue(calculatePipelineValue(request))
            .forecast(calculateWeightedForecast(request))
            .conversionRate(calculateConversionRate(request))
            .teamPerformance(calculateTeamMetrics(request))
            .build();
    }
    
    // Echtzeit-Aktivitäten-Stream
    public Multi<ActivityEvent> streamActivities() {
        return Multi.createFrom().emitter(emitter -> {
            eventBus.<ActivityEvent>consumer("sales.activities")
                .handler(message -> emitter.emit(message.body()));
        });
    }
}
```

### 2. Performance Tracker

```java
@Entity
public class SalesPerformance {
    @Id UUID id;
    @ManyToOne User salesRep;
    LocalDate period;
    
    // Aktivitäts-Metriken
    Integer callsCount;
    Integer meetingsCount;
    Integer offersCount;
    
    // Conversion-Metriken
    BigDecimal callToMeetingRate;
    BigDecimal meetingToOfferRate;
    BigDecimal offerToCloseRate;
    
    // Umsatz-Metriken
    BigDecimal pipelineValue;
    BigDecimal closedValue;
    BigDecimal averageDealSize;
    
    // Performance-Score (KI-basiert)
    BigDecimal performanceScore;
    String performanceInsights;
}
```

### 3. Dashboard API (GraphQL)

```graphql
type Query {
  dashboardOverview(
    dateRange: DateRange!
    teamIds: [ID!]
  ): DashboardData!
  
  salesRepPerformance(
    repId: ID!
    dateRange: DateRange!
  ): RepPerformance!
  
  pipelineAnalysis(
    filters: PipelineFilters
  ): PipelineAnalysis!
  
  teamComparison(
    metric: MetricType!
    period: Period!
  ): [TeamMetric!]!
}

type Subscription {
  liveActivities(teamIds: [ID!]): ActivityStream!
  alertsStream: Alert!
}
```

### 4. Frontend Dashboard Components

```typescript
// Main Dashboard View
export const ChefDashboard: React.FC = () => {
  const { dateRange, teamFilter } = useDashboardFilters();
  const { data: overview } = useDashboardOverview({ dateRange, teamFilter });
  const { data: activities } = useLiveActivities();
  
  return (
    <DashboardLayout>
      {/* Header KPIs */}
      <KPICards metrics={overview?.keyMetrics} />
      
      {/* Main Grid */}
      <Grid container spacing={3}>
        {/* Live Activity Monitor */}
        <Grid item xs={12} lg={4}>
          <ActivityMonitor 
            activities={activities}
            title="Live: Was läuft gerade?"
          />
        </Grid>
        
        {/* Pipeline Funnel */}
        <Grid item xs={12} lg={8}>
          <PipelineFunnel 
            data={overview?.pipeline}
            onDrillDown={handleDrillDown}
          />
        </Grid>
        
        {/* Team Performance Matrix */}
        <Grid item xs={12}>
          <TeamPerformanceMatrix 
            performers={overview?.teamMetrics}
            onSelectRep={handleRepSelect}
          />
        </Grid>
        
        {/* Insights & Alerts */}
        <Grid item xs={12} lg={6}>
          <InsightsPanel insights={overview?.aiInsights} />
        </Grid>
        
        {/* Action Items */}
        <Grid item xs={12} lg={6}>
          <CoachingActions actions={overview?.suggestedActions} />
        </Grid>
      </Grid>
    </DashboardLayout>
  );
};
```

### 5. Real-Time Activity Monitor

```typescript
// Live Activity Component
export const ActivityMonitor: React.FC<{ activities: Activity[] }> = ({ 
  activities 
}) => {
  return (
    <Card>
      <CardHeader 
        title="Live-Aktivitäten"
        subheader="Was machen die Verkäufer gerade?"
        action={<PulseIndicator />}
      />
      <CardContent>
        <Timeline>
          {activities.map(activity => (
            <TimelineItem key={activity.id}>
              <TimelineSeparator>
                <TimelineDot color={getActivityColor(activity.type)}>
                  {getActivityIcon(activity.type)}
                </TimelineDot>
                <TimelineConnector />
              </TimelineSeparator>
              <TimelineContent>
                <Typography variant="body2">
                  <strong>{activity.salesRep}</strong>
                  {' '}{activity.description}
                </Typography>
                <Typography variant="caption" color="textSecondary">
                  {formatRelativeTime(activity.timestamp)}
                </Typography>
              </TimelineContent>
            </TimelineItem>
          ))}
        </Timeline>
      </CardContent>
    </Card>
  );
};
```

### 6. KI-gestützte Insights

```java
@ApplicationScoped
public class SalesInsightEngine {
    
    @Inject
    MLModelService mlService;
    
    public List<Insight> generateInsights(SalesData data) {
        var insights = new ArrayList<Insight>();
        
        // Mustererkennung
        insights.addAll(detectPatterns(data));
        
        // Anomalie-Erkennung
        insights.addAll(detectAnomalies(data));
        
        // Vorhersagen
        insights.addAll(generatePredictions(data));
        
        // Handlungsempfehlungen
        insights.addAll(generateRecommendations(data));
        
        return insights.stream()
            .sorted(Comparator.comparing(Insight::getPriority))
            .limit(10)
            .collect(toList());
    }
    
    private List<Insight> detectPatterns(SalesData data) {
        // Beispiel: "Thomas hat seine Abschlussquote um 15% gesteigert
        // nachdem er mehr Vor-Ort-Termine gemacht hat"
        return mlService.analyzePatterns(data);
    }
}
```

## 🔗 Abhängigkeiten

### Zu bestehenden Modulen:
- **Activities (M4)**: Aktivitätsdaten für Monitoring
- **Customer (M5)**: Pipeline-Daten
- **Opportunities**: Deal-Werte und Stadien

### Zu neuen Features:
- **FC-004 (Verkäuferschutz)**: Provisions-Konflikte anzeigen
- **FC-005 (Xentral)**: Reale Umsatzzahlen
- **FC-006 (Mobile)**: Mobile Dashboard-Ansicht

### Performance-Anforderungen:
- Dashboard-Load: <2 Sekunden
- Live-Updates: <500ms Latenz
- Concurrent Users: 50+

## 🏛️ Architektur-Entscheidungen

### ADR-007-001: Read Models für Performance
**Entscheidung:** Separate Read Models für Dashboard
- Vorberechnete Metriken
- Event-basierte Updates
- Redis für Real-Time Daten

### ADR-007-002: GraphQL für flexible Queries
**Entscheidung:** GraphQL statt REST für Dashboard
- Reduzierte Roundtrips
- Flexible Aggregationen
- Real-Time Subscriptions

## 🚀 Implementierungsphasen

### Phase 1: Datenmodell & Analytics (2 Tage)
1. Performance-Tracking Entities
2. Metrik-Berechnungen
3. Aggregations-Pipeline
4. Caching-Strategie

### Phase 2: API Layer (2 Tage)
1. GraphQL Schema
2. Resolver Implementation
3. Subscription Setup
4. Performance-Optimierung

### Phase 3: Dashboard UI (2 Tage)
1. Layout & Navigation
2. KPI Cards
3. Charts & Visualisierungen
4. Drill-Down Funktionalität

### Phase 4: Advanced Features (2 Tage)
1. KI-Insights Integration
2. Coaching-Empfehlungen
3. Export-Funktionen
4. Mobile Optimierung

## 🧪 Test-Szenarien

```typescript
describe('ChefDashboard', () => {
  it('should load within 2 seconds', async () => {
    const start = Date.now();
    renderWithProviders(<ChefDashboard />);
    
    await waitFor(() => {
      expect(screen.getByText(/Pipeline-Wert/)).toBeInTheDocument();
    });
    
    expect(Date.now() - start).toBeLessThan(2000);
  });
  
  it('should update in real-time', async () => {
    const { rerender } = renderWithProviders(<ChefDashboard />);
    
    // Simulate activity
    mockWebSocket.emit('activity', createMockActivity());
    
    await waitFor(() => {
      expect(screen.getByText(/Thomas: Anruf mit Kunde XY/)).toBeInTheDocument();
    });
  });
});
```

## 📊 Erfolgsmetriken

- Dashboard-Nutzung: >5x täglich pro Manager
- Entscheidungsgeschwindigkeit: 50% schneller
- Forecast-Genauigkeit: ±10% Abweichung
- Mitarbeiter-Performance: +20% durch Coaching

## 🎨 UI/UX Design-Prinzipien

### Information Hierarchy:
1. **Level 1**: Gesamt-KPIs (Pipeline, Quote)
2. **Level 2**: Team-Performance
3. **Level 3**: Individual-Metriken
4. **Level 4**: Detail-Drill-Downs

### Visualisierungen:
- **Funnel**: Für Pipeline-Stadien
- **Heatmap**: Für Team-Vergleiche  
- **Sparklines**: Für Trends
- **Gauges**: Für Zielerreichung

## 🔍 Spezial-Features

### Provisions-Übersicht (Xentral-Integration):
- Fällige Provisionen diese Woche
- Provisions-Konflikte
- Top-Verdiener Ranking
- Provisions-Forecast

### Coaching-Modul:
- Automatische Schwachstellen-Erkennung
- Best-Practice Sharing
- 1:1 Meeting Vorschläge
- Skill-Gap Analyse

## 💡 Zukunfts-Erweiterungen

- Predictive Analytics
- What-If Szenarien
- Gamification Elements
- Voice-Activated Queries