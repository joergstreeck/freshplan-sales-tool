# 📊 Contact Analytics - Datengetriebene Beziehungseinblicke

**Phase:** 3 - Compliance & Ethics  
**Tag:** 2 der Woche 3  
**Status:** 📋 Specification Ready  

## 🧭 Navigation

**← Zurück:** [DSGVO Consent](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/step3/DSGVO_CONSENT.md)  
**→ Nächster:** [AI Features](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/step3/AI_FEATURES.md)  
**↑ Übergeordnet:** [Step 3 Main Guide](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/step3/README.md)  

## 🎯 Vision: Insights die den Unterschied machen

**Contact Analytics** verwandelt Beziehungsdaten in **actionable Insights**:

> "Verstehen, vorhersehen, handeln - datengetriebene Kundenbeziehungen"

## 📈 Analytics Data Architecture

### Analytics Aggregation Model

```typescript
// types/analytics.types.ts
export interface ContactAnalytics {
  // Overview Metrics
  overview: {
    totalContacts: number;
    activeContacts: number;
    inactiveContacts: number;
    averageRelationshipAge: number; // days
    churnRisk: number; // percentage
    growthRate: number; // month over month
  };
  
  // Relationship Health
  relationshipHealth: {
    hot: number;
    warm: number;
    cooling: number;
    cold: number;
    averageWarmthScore: number;
    warmthTrend: TrendData;
  };
  
  // Interaction Analytics
  interactions: {
    totalInteractions: number;
    interactionsByType: Record<InteractionType, number>;
    averageResponseTime: number; // hours
    peakInteractionHours: number[];
    interactionTrend: TrendData;
    topInteractionPartners: ContactRanking[];
  };
  
  // Location Coverage
  locationCoverage: {
    coveredLocations: number;
    uncoveredLocations: number;
    locationsPerContact: number;
    coverageMap: LocationHeatmap;
    underservedAreas: LocationAlert[];
  };
  
  // Business Impact
  businessImpact: {
    revenuePerContact: number;
    lifetimeValue: number;
    conversionRate: number;
    averageDealSize: number;
    revenueTrend: TrendData;
  };
  
  // Predictive Insights
  predictions: {
    churnPrediction: ChurnPrediction[];
    opportunityScore: OpportunityScore[];
    nextBestActions: NextBestAction[];
    seasonalPatterns: SeasonalPattern[];
  };
}

export interface TrendData {
  values: number[];
  dates: Date[];
  trend: 'up' | 'down' | 'stable';
  changePercentage: number;
}

export interface ContactRanking {
  contactId: string;
  name: string;
  score: number;
  metric: string;
  change: number;
}
```

## 🎨 Analytics Dashboard Components

### Main Analytics Dashboard

```typescript
// components/ContactAnalyticsDashboard.tsx
import React, { useState, useMemo } from 'react';
import {
  Box,
  Grid,
  Card,
  CardContent,
  Typography,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
  ToggleButtonGroup,
  ToggleButton,
  Skeleton
} from '@mui/material';
import {
  TrendingUp as TrendingUpIcon,
  TrendingDown as TrendingDownIcon,
  CalendarMonth as CalendarIcon,
  FilterList as FilterIcon
} from '@mui/icons-material';
import { useContactAnalytics } from '../hooks/useContactAnalytics';
import { AnalyticsTimeframe } from '../types/analytics.types';

export const ContactAnalyticsDashboard: React.FC<{
  customerId: string;
}> = ({ customerId }) => {
  const [timeframe, setTimeframe] = useState<AnalyticsTimeframe>('30d');
  const [view, setView] = useState<'overview' | 'detailed'>('overview');
  
  const { data: analytics, isLoading, error } = useContactAnalytics(
    customerId,
    timeframe
  );
  
  if (isLoading) return <DashboardSkeleton />;
  if (error) return <ErrorState error={error} />;
  if (!analytics) return null;
  
  return (
    <Box>
      {/* Dashboard Header */}
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h5">
          Contact Analytics Dashboard
        </Typography>
        
        <Box display="flex" gap={2}>
          <FormControl size="small" sx={{ minWidth: 120 }}>
            <InputLabel>Zeitraum</InputLabel>
            <Select
              value={timeframe}
              onChange={(e) => setTimeframe(e.target.value as AnalyticsTimeframe)}
              label="Zeitraum"
            >
              <MenuItem value="7d">7 Tage</MenuItem>
              <MenuItem value="30d">30 Tage</MenuItem>
              <MenuItem value="90d">90 Tage</MenuItem>
              <MenuItem value="1y">1 Jahr</MenuItem>
              <MenuItem value="all">Gesamt</MenuItem>
            </Select>
          </FormControl>
          
          <ToggleButtonGroup
            value={view}
            exclusive
            onChange={(_, value) => value && setView(value)}
            size="small"
          >
            <ToggleButton value="overview">
              Übersicht
            </ToggleButton>
            <ToggleButton value="detailed">
              Details
            </ToggleButton>
          </ToggleButtonGroup>
        </Box>
      </Box>
      
      {/* Key Metrics Row */}
      <Grid container spacing={3} mb={3}>
        <Grid item xs={12} sm={6} md={3}>
          <MetricCard
            title="Aktive Kontakte"
            value={analytics.overview.activeContacts}
            total={analytics.overview.totalContacts}
            trend={analytics.overview.growthRate}
            format="percentage"
          />
        </Grid>
        
        <Grid item xs={12} sm={6} md={3}>
          <MetricCard
            title="Ø Beziehungswärme"
            value={analytics.relationshipHealth.averageWarmthScore}
            trend={analytics.relationshipHealth.warmthTrend.changePercentage}
            format="score"
            color={getWarmthColor(analytics.relationshipHealth.averageWarmthScore)}
          />
        </Grid>
        
        <Grid item xs={12} sm={6} md={3}>
          <MetricCard
            title="Abwanderungsrisiko"
            value={analytics.overview.churnRisk}
            format="percentage"
            inverse
            alert={analytics.overview.churnRisk > 20}
          />
        </Grid>
        
        <Grid item xs={12} sm={6} md={3}>
          <MetricCard
            title="Ø Lifetime Value"
            value={analytics.businessImpact.lifetimeValue}
            trend={analytics.businessImpact.revenueTrend.changePercentage}
            format="currency"
          />
        </Grid>
      </Grid>
      
      {view === 'overview' ? (
        <OverviewCharts analytics={analytics} />
      ) : (
        <DetailedAnalytics analytics={analytics} />
      )}
    </Box>
  );
};

// Metric Card Component
const MetricCard: React.FC<{
  title: string;
  value: number;
  total?: number;
  trend?: number;
  format: 'percentage' | 'score' | 'currency' | 'number';
  color?: string;
  inverse?: boolean;
  alert?: boolean;
}> = ({ title, value, total, trend, format, color, inverse, alert }) => {
  const formatValue = (val: number): string => {
    switch (format) {
      case 'percentage':
        return `${Math.round(val)}%`;
      case 'score':
        return val.toFixed(1);
      case 'currency':
        return new Intl.NumberFormat('de-DE', {
          style: 'currency',
          currency: 'EUR'
        }).format(val);
      default:
        return val.toLocaleString('de-DE');
    }
  };
  
  const getTrendIcon = () => {
    if (!trend) return null;
    const isPositive = inverse ? trend < 0 : trend > 0;
    
    return isPositive ? (
      <TrendingUpIcon color="success" />
    ) : (
      <TrendingDownIcon color="error" />
    );
  };
  
  return (
    <Card sx={{ 
      height: '100%',
      borderLeft: alert ? '4px solid' : undefined,
      borderLeftColor: alert ? 'error.main' : undefined
    }}>
      <CardContent>
        <Typography color="text.secondary" gutterBottom variant="overline">
          {title}
        </Typography>
        
        <Box display="flex" alignItems="baseline" gap={1}>
          <Typography variant="h4" color={color}>
            {formatValue(value)}
          </Typography>
          
          {total && (
            <Typography variant="body2" color="text.secondary">
              von {total}
            </Typography>
          )}
        </Box>
        
        {trend !== undefined && (
          <Box display="flex" alignItems="center" gap={0.5} mt={1}>
            {getTrendIcon()}
            <Typography
              variant="body2"
              color={trend > 0 ? (inverse ? 'error.main' : 'success.main') : 
                    (inverse ? 'success.main' : 'error.main')}
            >
              {Math.abs(trend).toFixed(1)}%
            </Typography>
            <Typography variant="body2" color="text.secondary">
              vs. Vorperiode
            </Typography>
          </Box>
        )}
      </CardContent>
    </Card>
  );
};
```

### Relationship Health Visualization

```typescript
// components/RelationshipHealthChart.tsx
import { ResponsivePie } from '@nivo/pie';
import { ResponsiveLine } from '@nivo/line';

export const RelationshipHealthChart: React.FC<{
  data: RelationshipHealthData;
}> = ({ data }) => {
  const pieData = [
    {
      id: 'Hot',
      label: 'Heiß',
      value: data.hot,
      color: '#f44336'
    },
    {
      id: 'Warm',
      label: 'Warm',
      value: data.warm,
      color: '#ff9800'
    },
    {
      id: 'Cooling',
      label: 'Abkühlend',
      value: data.cooling,
      color: '#2196f3'
    },
    {
      id: 'Cold',
      label: 'Kalt',
      value: data.cold,
      color: '#9e9e9e'
    }
  ];
  
  return (
    <Grid container spacing={3}>
      <Grid item xs={12} md={6}>
        <Card>
          <CardHeader title="Beziehungswärme Verteilung" />
          <CardContent sx={{ height: 300 }}>
            <ResponsivePie
              data={pieData}
              margin={{ top: 20, right: 80, bottom: 20, left: 80 }}
              innerRadius={0.5}
              padAngle={0.7}
              cornerRadius={3}
              activeOuterRadiusOffset={8}
              colors={{ datum: 'data.color' }}
              borderWidth={1}
              borderColor={{ from: 'color', modifiers: [['darker', 0.2]] }}
              arcLinkLabelsSkipAngle={10}
              arcLinkLabelsTextColor="#333333"
              arcLabelsSkipAngle={10}
              arcLabelsTextColor="#ffffff"
              enableArcLabels={false}
              legends={[
                {
                  anchor: 'bottom',
                  direction: 'row',
                  justify: false,
                  translateX: 0,
                  translateY: 50,
                  itemsSpacing: 0,
                  itemWidth: 100,
                  itemHeight: 18,
                  itemTextColor: '#999',
                  itemDirection: 'left-to-right',
                  itemOpacity: 1,
                  symbolSize: 18,
                  symbolShape: 'circle'
                }
              ]}
            />
          </CardContent>
        </Card>
      </Grid>
      
      <Grid item xs={12} md={6}>
        <Card>
          <CardHeader title="Wärme-Trend (30 Tage)" />
          <CardContent sx={{ height: 300 }}>
            <ResponsiveLine
              data={data.warmthTrend}
              margin={{ top: 20, right: 20, bottom: 50, left: 60 }}
              xScale={{ type: 'time' }}
              yScale={{ type: 'linear', min: 0, max: 100 }}
              curve="monotoneX"
              axisBottom={{
                format: '%b %d',
                tickRotation: -45
              }}
              axisLeft={{
                legend: 'Wärme Score',
                legendOffset: -50,
                legendPosition: 'middle'
              }}
              pointSize={8}
              pointColor={{ theme: 'background' }}
              pointBorderWidth={2}
              pointBorderColor={{ from: 'serieColor' }}
              useMesh={true}
              enableArea={true}
              areaOpacity={0.1}
            />
          </CardContent>
        </Card>
      </Grid>
    </Grid>
  );
};
```

### Predictive Analytics Components

```typescript
// components/PredictiveInsights.tsx
export const PredictiveInsights: React.FC<{
  predictions: PredictiveAnalytics;
  onAction: (action: NextBestAction) => void;
}> = ({ predictions, onAction }) => {
  const [selectedInsight, setSelectedInsight] = useState<'churn' | 'opportunity'>('churn');
  
  return (
    <Card>
      <CardHeader
        title="Predictive Insights"
        subheader="KI-basierte Vorhersagen und Empfehlungen"
        action={
          <ToggleButtonGroup
            value={selectedInsight}
            exclusive
            onChange={(_, value) => value && setSelectedInsight(value)}
            size="small"
          >
            <ToggleButton value="churn">
              Abwanderung
            </ToggleButton>
            <ToggleButton value="opportunity">
              Chancen
            </ToggleButton>
          </ToggleButtonGroup>
        }
      />
      
      <CardContent>
        {selectedInsight === 'churn' ? (
          <ChurnPredictionList
            predictions={predictions.churnPrediction}
            onAction={onAction}
          />
        ) : (
          <OpportunityList
            opportunities={predictions.opportunityScore}
            onAction={onAction}
          />
        )}
        
        {/* Next Best Actions */}
        <Box mt={3}>
          <Typography variant="subtitle2" gutterBottom>
            Empfohlene Aktionen
          </Typography>
          
          <Stack spacing={1}>
            {predictions.nextBestActions.slice(0, 3).map((action, index) => (
              <NextBestActionCard
                key={index}
                action={action}
                onExecute={() => onAction(action)}
              />
            ))}
          </Stack>
        </Box>
      </CardContent>
    </Card>
  );
};

// Churn Prediction Component
const ChurnPredictionList: React.FC<{
  predictions: ChurnPrediction[];
  onAction: (action: NextBestAction) => void;
}> = ({ predictions, onAction }) => {
  const highRiskContacts = predictions.filter(p => p.riskScore > 70);
  
  return (
    <Box>
      <Alert severity="warning" sx={{ mb: 2 }}>
        <AlertTitle>
          {highRiskContacts.length} Kontakte mit hohem Abwanderungsrisiko
        </AlertTitle>
        Sofortige Maßnahmen empfohlen für kritische Beziehungen.
      </Alert>
      
      <List>
        {highRiskContacts.map(prediction => (
          <ListItem
            key={prediction.contactId}
            secondaryAction={
              <Button
                size="small"
                variant="contained"
                color="warning"
                onClick={() => onAction({
                  type: 'prevent-churn',
                  contactId: prediction.contactId,
                  urgency: 'high',
                  suggestedAction: prediction.recommendedAction
                })}
              >
                Aktion
              </Button>
            }
          >
            <ListItemAvatar>
              <Avatar sx={{ bgcolor: 'error.main' }}>
                {prediction.riskScore}%
              </Avatar>
            </ListItemAvatar>
            
            <ListItemText
              primary={prediction.contactName}
              secondary={
                <Box>
                  <Typography variant="caption" display="block">
                    Gründe: {prediction.riskFactors.join(', ')}
                  </Typography>
                  <Typography variant="caption" color="primary">
                    Empfehlung: {prediction.recommendedAction}
                  </Typography>
                </Box>
              }
            />
          </ListItem>
        ))}
      </List>
    </Box>
  );
};
```

## 🔧 Backend Analytics Services

### Analytics Aggregation Service

```java
// ContactAnalyticsService.java
@ApplicationScoped
public class ContactAnalyticsService {
    
    @Inject
    ContactRepository contactRepository;
    
    @Inject
    InteractionRepository interactionRepository;
    
    @Inject
    RelationshipWarmthService warmthService;
    
    @Inject
    PredictiveAnalyticsService predictiveService;
    
    @Inject
    @CacheName("analytics-cache")
    Cache<String, ContactAnalytics> analyticsCache;
    
    /**
     * Generate comprehensive analytics for a customer
     */
    public ContactAnalytics generateAnalytics(
        UUID customerId,
        AnalyticsTimeframe timeframe
    ) {
        String cacheKey = customerId + "-" + timeframe;
        
        return analyticsCache.get(cacheKey, k -> {
            ContactAnalytics analytics = new ContactAnalytics();
            
            // Calculate time boundaries
            LocalDateTime startDate = calculateStartDate(timeframe);
            LocalDateTime endDate = LocalDateTime.now();
            
            // Overview metrics
            analytics.setOverview(calculateOverviewMetrics(customerId, startDate, endDate));
            
            // Relationship health
            analytics.setRelationshipHealth(
                calculateRelationshipHealth(customerId, startDate, endDate)
            );
            
            // Interaction analytics
            analytics.setInteractions(
                calculateInteractionAnalytics(customerId, startDate, endDate)
            );
            
            // Location coverage
            analytics.setLocationCoverage(
                calculateLocationCoverage(customerId)
            );
            
            // Business impact
            analytics.setBusinessImpact(
                calculateBusinessImpact(customerId, startDate, endDate)
            );
            
            // Predictive insights
            analytics.setPredictions(
                predictiveService.generatePredictions(customerId)
            );
            
            return analytics;
        });
    }
    
    private RelationshipHealthMetrics calculateRelationshipHealth(
        UUID customerId,
        LocalDateTime startDate,
        LocalDateTime endDate
    ) {
        // Get all contacts with warmth data
        List<ContactWithWarmth> contacts = contactRepository
            .findContactsWithWarmth(customerId);
        
        RelationshipHealthMetrics metrics = new RelationshipHealthMetrics();
        
        // Count by temperature
        Map<RelationshipTemperature, Long> temperatureCounts = contacts.stream()
            .filter(c -> c.getWarmth() != null)
            .collect(Collectors.groupingBy(
                c -> c.getWarmth().getTemperature(),
                Collectors.counting()
            ));
        
        metrics.setHot(temperatureCounts.getOrDefault(RelationshipTemperature.HOT, 0L).intValue());
        metrics.setWarm(temperatureCounts.getOrDefault(RelationshipTemperature.WARM, 0L).intValue());
        metrics.setCooling(temperatureCounts.getOrDefault(RelationshipTemperature.COOLING, 0L).intValue());
        metrics.setCold(temperatureCounts.getOrDefault(RelationshipTemperature.COLD, 0L).intValue());
        
        // Calculate average warmth
        double avgWarmth = contacts.stream()
            .filter(c -> c.getWarmth() != null)
            .mapToDouble(c -> c.getWarmth().getScore())
            .average()
            .orElse(0.0);
        
        metrics.setAverageWarmthScore(avgWarmth);
        
        // Calculate trend
        metrics.setWarmthTrend(calculateWarmthTrend(customerId, startDate, endDate));
        
        return metrics;
    }
    
    private TrendData calculateWarmthTrend(
        UUID customerId,
        LocalDateTime startDate,
        LocalDateTime endDate
    ) {
        // Get daily warmth averages
        List<DailyWarmthAverage> dailyAverages = em.createQuery(
            "SELECT new de.freshplan.dto.DailyWarmthAverage(" +
            "   DATE(w.calculatedAt), " +
            "   AVG(w.score) " +
            ") " +
            "FROM RelationshipWarmth w " +
            "JOIN Contact c ON c.id = w.contactId " +
            "WHERE c.customer.id = :customerId " +
            "AND w.calculatedAt BETWEEN :startDate AND :endDate " +
            "GROUP BY DATE(w.calculatedAt) " +
            "ORDER BY DATE(w.calculatedAt)",
            DailyWarmthAverage.class
        )
        .setParameter("customerId", customerId)
        .setParameter("startDate", startDate)
        .setParameter("endDate", endDate)
        .getResultList();
        
        TrendData trend = new TrendData();
        trend.setValues(dailyAverages.stream()
            .map(DailyWarmthAverage::getAverageScore)
            .collect(Collectors.toList())
        );
        trend.setDates(dailyAverages.stream()
            .map(DailyWarmthAverage::getDate)
            .collect(Collectors.toList())
        );
        
        // Calculate trend direction
        if (trend.getValues().size() >= 2) {
            double firstValue = trend.getValues().get(0);
            double lastValue = trend.getValues().get(trend.getValues().size() - 1);
            double change = ((lastValue - firstValue) / firstValue) * 100;
            
            trend.setChangePercentage(change);
            trend.setTrend(change > 5 ? "up" : change < -5 ? "down" : "stable");
        }
        
        return trend;
    }
    
    /**
     * Export analytics data
     */
    public byte[] exportAnalyticsReport(
        UUID customerId,
        AnalyticsTimeframe timeframe,
        ExportFormat format
    ) {
        ContactAnalytics analytics = generateAnalytics(customerId, timeframe);
        
        switch (format) {
            case PDF:
                return generatePdfReport(analytics);
            case EXCEL:
                return generateExcelReport(analytics);
            case CSV:
                return generateCsvReport(analytics);
            default:
                throw new IllegalArgumentException("Unsupported format: " + format);
        }
    }
}
```

### Predictive Analytics Engine

```java
// PredictiveAnalyticsService.java
@ApplicationScoped
public class PredictiveAnalyticsService {
    
    @Inject
    MLModelService mlService;
    
    @Inject
    ContactRepository contactRepository;
    
    public PredictiveInsights generatePredictions(UUID customerId) {
        PredictiveInsights insights = new PredictiveInsights();
        
        // Churn predictions
        insights.setChurnPredictions(predictChurn(customerId));
        
        // Opportunity scoring
        insights.setOpportunityScores(scoreOpportunities(customerId));
        
        // Next best actions
        insights.setNextBestActions(recommendActions(customerId));
        
        // Seasonal patterns
        insights.setSeasonalPatterns(analyzeSeasonalPatterns(customerId));
        
        return insights;
    }
    
    private List<ChurnPrediction> predictChurn(UUID customerId) {
        List<Contact> contacts = contactRepository.findByCustomerId(customerId);
        List<ChurnPrediction> predictions = new ArrayList<>();
        
        for (Contact contact : contacts) {
            // Feature extraction
            ChurnFeatures features = extractChurnFeatures(contact);
            
            // ML prediction
            double churnProbability = mlService.predictChurn(features);
            
            if (churnProbability > 0.3) { // 30% threshold
                ChurnPrediction prediction = new ChurnPrediction();
                prediction.setContactId(contact.getId());
                prediction.setContactName(contact.getFullName());
                prediction.setRiskScore((int)(churnProbability * 100));
                prediction.setRiskFactors(identifyRiskFactors(features));
                prediction.setRecommendedAction(recommendChurnPrevention(features));
                prediction.setPredictedChurnDate(
                    LocalDate.now().plusDays((int)(90 * (1 - churnProbability)))
                );
                
                predictions.add(prediction);
            }
        }
        
        // Sort by risk score descending
        predictions.sort((a, b) -> Integer.compare(b.getRiskScore(), a.getRiskScore()));
        
        return predictions;
    }
    
    private ChurnFeatures extractChurnFeatures(Contact contact) {
        ChurnFeatures features = new ChurnFeatures();
        
        // Interaction frequency
        long daysSinceLastInteraction = ChronoUnit.DAYS.between(
            contact.getLastInteractionDate() != null ? 
                contact.getLastInteractionDate() : contact.getCreatedAt(),
            LocalDateTime.now()
        );
        features.setDaysSinceLastInteraction(daysSinceLastInteraction);
        
        // Interaction trend
        features.setInteractionFrequencyTrend(
            calculateInteractionTrend(contact.getId())
        );
        
        // Response rate
        features.setResponseRate(
            calculateResponseRate(contact.getId())
        );
        
        // Relationship age
        features.setRelationshipAgeDays(
            ChronoUnit.DAYS.between(contact.getCreatedAt(), LocalDateTime.now())
        );
        
        // Business value
        features.setLifetimeValue(
            calculateLifetimeValue(contact.getId())
        );
        
        return features;
    }
}
```

## 🧪 Analytics Testing

```typescript
// __tests__/contact-analytics.test.ts
describe('Contact Analytics', () => {
  it('should calculate relationship health correctly', async () => {
    const mockContacts = [
      createMockContact({ warmthScore: 85 }), // Hot
      createMockContact({ warmthScore: 65 }), // Warm
      createMockContact({ warmthScore: 45 }), // Cooling
      createMockContact({ warmthScore: 25 }), // Cold
    ];
    
    const analytics = calculateRelationshipHealth(mockContacts);
    
    expect(analytics).toMatchObject({
      hot: 1,
      warm: 1,
      cooling: 1,
      cold: 1,
      averageWarmthScore: 55
    });
  });
  
  it('should identify high churn risk contacts', async () => {
    const { result } = renderHook(() => usePredictiveAnalytics('customer-1'));
    
    await waitFor(() => {
      expect(result.current.predictions.churnPrediction).toHaveLength(3);
      expect(result.current.predictions.churnPrediction[0].riskScore).toBeGreaterThan(70);
    });
  });
  
  it('should export analytics in requested format', async () => {
    const exportSpy = jest.spyOn(analyticsApi, 'exportReport');
    
    await act(async () => {
      await exportAnalytics('customer-1', '30d', 'pdf');
    });
    
    expect(exportSpy).toHaveBeenCalledWith(
      'customer-1',
      '30d',
      'pdf'
    );
  });
});
```

## 🎯 Success Metrics

### Analytics Adoption:
- **Dashboard Usage:** > 80% weekly active users
- **Report Generation:** > 50 reports/month
- **Action Conversion:** > 30% of recommendations acted upon

### Prediction Accuracy:
- **Churn Prediction:** > 75% accuracy
- **Opportunity Scoring:** > 70% precision
- **Seasonal Patterns:** > 80% match rate

### Business Impact:
- **Churn Reduction:** -25% after 6 months
- **Revenue per Contact:** +15% improvement
- **Relationship Quality:** +20% warmth score

---

**Nächster Schritt:** [→ AI Features](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/step3/AI_FEATURES.md)

**Data-Driven Relationships = Better Business! 📊✨**