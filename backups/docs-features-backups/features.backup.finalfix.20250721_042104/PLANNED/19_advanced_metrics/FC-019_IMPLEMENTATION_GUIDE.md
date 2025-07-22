# 📊 FC-019 ADVANCED SALES METRICS

**Feature Code:** FC-019  
**Feature-Typ:** 🔀 FULLSTACK  
**Geschätzter Aufwand:** 6-7 Tage  
**Priorität:** HIGH - Management braucht Insights  
**ROI:** Bessere Entscheidungen durch Daten  

---

## 🎯 PROBLEM & LÖSUNG

**Problem:** "Läuft's gut?" - "Keine Ahnung!" Gefühl statt Fakten, wichtige Trends werden verpasst  
**Lösung:** Real-time Sales Metrics Dashboard mit Velocity, Win Rates, Trends  
**Impact:** Datengetriebene Entscheidungen, frühe Problemerkennung  

---

## 📈 METRICS DASHBOARD KONZEPT

```
SALES METRICS DASHBOARD
┌─────────────────────────────────────┐
│ 📊 SALES VELOCITY                   │
│ ⚡ 23 Tage (Ø Pipeline → Close)     │
│ ↗️ +15% schneller als letzter Monat │
├─────────────────────────────────────┤
│ WIN RATE BY STAGE                   │
│ Lead → Qualified:      75% ████░    │
│ Qualified → Proposal:  85% █████    │
│ Proposal → Negotiation: 60% ███░    │
│ Negotiation → Closed:  80% ████░    │
│ Overall Win Rate:      38.5% ██░    │
├─────────────────────────────────────┤
│ DEAL SIZE TREND         ↗️ +12%     │
│ Ø Q1: 35.000€                       │
│ Ø Q2: 42.000€                       │
│ Ø Q3: 47.000€ (aktuell)             │
├─────────────────────────────────────┤
│ BOTTLENECKS 🚨                      │
│ • 15 Deals stuck in "Proposal" >30d │
│ • Win rate drop at Negotiation      │
│ [Analyze] [Take Action]             │
└─────────────────────────────────────┘
```

---

## 📋 FEATURES IM DETAIL

### 1. Sales Velocity Calculation

```typescript
interface SalesVelocity {
  averageDealSize: number;
  numberOfDeals: number;
  winRate: number;
  salesCycleLength: number;
  velocity: number; // € per Tag
}

const calculateSalesVelocity = (period: DateRange): SalesVelocity => {
  // Sales Velocity = (# of Deals × Avg Deal Size × Win Rate) / Sales Cycle Length
  
  const deals = getDealsInPeriod(period);
  const wonDeals = deals.filter(d => d.status === 'WON');
  
  const metrics = {
    numberOfDeals: wonDeals.length,
    averageDealSize: average(wonDeals.map(d => d.value)),
    winRate: wonDeals.length / deals.length,
    salesCycleLength: average(wonDeals.map(d => 
      daysBetween(d.createdAt, d.closedAt)
    ))
  };
  
  metrics.velocity = 
    (metrics.numberOfDeals * metrics.averageDealSize * metrics.winRate) 
    / metrics.salesCycleLength;
  
  return metrics;
};

// Velocity Trend Component
const VelocityTrendCard: React.FC = () => {
  const velocityData = useVelocityTrend('6months');
  const currentVelocity = velocityData[velocityData.length - 1];
  const previousVelocity = velocityData[velocityData.length - 2];
  const change = ((currentVelocity.velocity - previousVelocity.velocity) 
    / previousVelocity.velocity) * 100;
  
  return (
    <Card>
      <CardContent>
        <Box display="flex" alignItems="center" justifyContent="space-between">
          <Box>
            <Typography color="textSecondary" gutterBottom>
              Sales Velocity
            </Typography>
            <Typography variant="h4">
              {formatCurrency(currentVelocity.velocity)}/Tag
            </Typography>
            <Typography 
              variant="body2" 
              color={change > 0 ? 'success.main' : 'error.main'}
            >
              {change > 0 ? '↗️' : '↘️'} {Math.abs(change).toFixed(1)}% vs. letzter Monat
            </Typography>
          </Box>
          <Box>
            <SpeedIcon sx={{ fontSize: 48, color: 'primary.main' }} />
          </Box>
        </Box>
        
        {/* Breakdown */}
        <Box mt={3}>
          <Grid container spacing={2}>
            <Grid item xs={6}>
              <Typography variant="caption" color="textSecondary">
                Ø Deal Size
              </Typography>
              <Typography variant="h6">
                {formatCurrency(currentVelocity.averageDealSize)}
              </Typography>
            </Grid>
            <Grid item xs={6}>
              <Typography variant="caption" color="textSecondary">
                Ø Cycle Time
              </Typography>
              <Typography variant="h6">
                {currentVelocity.salesCycleLength} Tage
              </Typography>
            </Grid>
          </Grid>
        </Box>
        
        {/* Mini Chart */}
        <Box mt={2} height={100}>
          <ResponsiveContainer width="100%" height="100%">
            <LineChart data={velocityData}>
              <Line 
                type="monotone" 
                dataKey="velocity" 
                stroke="#94C456"
                strokeWidth={2}
                dot={false}
              />
              <Tooltip 
                formatter={(value) => formatCurrency(value) + '/Tag'}
                labelFormatter={(label) => formatMonth(label)}
              />
            </LineChart>
          </ResponsiveContainer>
        </Box>
      </CardContent>
    </Card>
  );
};
```

### 2. Win Rate by Stage Analysis

```typescript
interface StageConversion {
  fromStage: string;
  toStage: string;
  conversionRate: number;
  averageTime: number;
  volume: number;
  trends: TrendData[];
}

const WinRateByStageChart: React.FC = () => {
  const stageData = useStageConversions();
  
  return (
    <Card>
      <CardHeader
        title="Win Rate by Stage"
        subheader="Conversion rates between pipeline stages"
        action={
          <ToggleButtonGroup size="small">
            <ToggleButton value="month">Monat</ToggleButton>
            <ToggleButton value="quarter">Quartal</ToggleButton>
            <ToggleButton value="year">Jahr</ToggleButton>
          </ToggleButtonGroup>
        }
      />
      <CardContent>
        {/* Funnel Visualization */}
        <Box sx={{ position: 'relative', height: 400 }}>
          <SalesFunnel>
            {stageData.map((stage, index) => (
              <FunnelStage key={stage.name}>
                <Box 
                  sx={{ 
                    width: `${100 - index * 15}%`,
                    margin: '0 auto',
                    backgroundColor: getStageColor(stage.conversionRate),
                    padding: 2,
                    borderRadius: 1
                  }}
                >
                  <Typography variant="h6" align="center">
                    {stage.name}
                  </Typography>
                  <Typography variant="h4" align="center">
                    {stage.count}
                  </Typography>
                  <Typography variant="body2" align="center">
                    {formatCurrency(stage.totalValue)}
                  </Typography>
                </Box>
                
                {index < stageData.length - 1 && (
                  <ConversionArrow>
                    <Typography variant="h6" color="primary">
                      {stage.conversionToNext}%
                    </Typography>
                    <Typography variant="caption">
                      Ø {stage.avgDaysToNext} Tage
                    </Typography>
                  </ConversionArrow>
                )}
              </FunnelStage>
            ))}
          </SalesFunnel>
        </Box>
        
        {/* Detailed Breakdown */}
        <Box mt={4}>
          <Typography variant="h6" gutterBottom>
            Stage Performance Details
          </Typography>
          <TableContainer>
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>Stage Transition</TableCell>
                  <TableCell align="right">Conversion Rate</TableCell>
                  <TableCell align="right">Avg. Time</TableCell>
                  <TableCell align="right">Stuck Deals</TableCell>
                  <TableCell align="right">Action</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {stageData.map(stage => (
                  <TableRow key={stage.name}>
                    <TableCell>{stage.fromStage} → {stage.toStage}</TableCell>
                    <TableCell align="right">
                      <Chip
                        label={`${stage.conversionRate}%`}
                        size="small"
                        color={stage.conversionRate > 70 ? 'success' : 'warning'}
                      />
                    </TableCell>
                    <TableCell align="right">{stage.averageTime}d</TableCell>
                    <TableCell align="right">
                      {stage.stuckDeals > 0 && (
                        <Chip
                          label={stage.stuckDeals}
                          size="small"
                          color="error"
                        />
                      )}
                    </TableCell>
                    <TableCell align="right">
                      <IconButton size="small" onClick={() => analyzeStage(stage)}>
                        <Analytics />
                      </IconButton>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </Box>
      </CardContent>
    </Card>
  );
};
```

### 3. Deal Size Trend Analysis

```typescript
interface DealSizeTrend {
  period: string;
  averageSize: number;
  medianSize: number;
  totalVolume: number;
  dealCount: number;
  breakdown: {
    small: number;    // < 10k
    medium: number;   // 10k-50k
    large: number;    // 50k-100k
    enterprise: number; // > 100k
  };
}

const DealSizeTrendChart: React.FC = () => {
  const trendData = useDealSizeTrend('12months');
  const currentMonth = trendData[trendData.length - 1];
  const yearAgo = trendData[0];
  const growth = ((currentMonth.averageSize - yearAgo.averageSize) / yearAgo.averageSize) * 100;
  
  return (
    <Card>
      <CardHeader
        title="Average Deal Size Trend"
        subheader={`${growth > 0 ? '📈' : '📉'} ${Math.abs(growth).toFixed(1)}% YoY`}
      />
      <CardContent>
        {/* Main Chart */}
        <Box height={300}>
          <ResponsiveContainer width="100%" height="100%">
            <ComposedChart data={trendData}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="period" />
              <YAxis yAxisId="left" />
              <YAxis yAxisId="right" orientation="right" />
              <Tooltip />
              <Legend />
              
              <Bar yAxisId="left" dataKey="dealCount" fill="#8884d8" name="Anzahl Deals" />
              <Line 
                yAxisId="right" 
                type="monotone" 
                dataKey="averageSize" 
                stroke="#94C456" 
                strokeWidth={3}
                name="Ø Deal Size"
              />
              <Line 
                yAxisId="right" 
                type="monotone" 
                dataKey="medianSize" 
                stroke="#ff7300" 
                strokeDasharray="5 5"
                name="Median Size"
              />
            </ComposedChart>
          </ResponsiveContainer>
        </Box>
        
        {/* Size Distribution */}
        <Box mt={4}>
          <Typography variant="h6" gutterBottom>
            Deal Size Distribution (Current Quarter)
          </Typography>
          <Grid container spacing={2}>
            {[
              { label: 'Small (<10k)', value: currentMonth.breakdown.small, color: '#8884d8' },
              { label: 'Medium (10-50k)', value: currentMonth.breakdown.medium, color: '#82ca9d' },
              { label: 'Large (50-100k)', value: currentMonth.breakdown.large, color: '#ffc658' },
              { label: 'Enterprise (>100k)', value: currentMonth.breakdown.enterprise, color: '#ff7c7c' }
            ].map(segment => (
              <Grid item xs={3} key={segment.label}>
                <Paper sx={{ p: 2, textAlign: 'center' }}>
                  <Typography variant="h4" style={{ color: segment.color }}>
                    {segment.value}%
                  </Typography>
                  <Typography variant="caption">
                    {segment.label}
                  </Typography>
                </Paper>
              </Grid>
            ))}
          </Grid>
        </Box>
        
        {/* Insights */}
        <Alert severity="info" sx={{ mt: 3 }}>
          <AlertTitle>Insights</AlertTitle>
          <ul style={{ margin: 0, paddingLeft: 20 }}>
            {growth > 20 && (
              <li>Starkes Wachstum bei Deal-Größen - Upselling funktioniert!</li>
            )}
            {currentMonth.breakdown.enterprise > 30 && (
              <li>Enterprise-Segment wächst - längere Sales Cycles beachten</li>
            )}
            {currentMonth.medianSize < currentMonth.averageSize * 0.6 && (
              <li>Große Ausreißer verzerren Durchschnitt - Median beachten</li>
            )}
          </ul>
        </Alert>
      </CardContent>
    </Card>
  );
};
```

### 4. Sales Cycle Length Analysis

```typescript
interface SalesCycleMetrics {
  overall: {
    average: number;
    median: number;
    fastest: number;
    slowest: number;
  };
  byStage: StageTime[];
  byDealSize: SizeCycleTime[];
  bySalesperson: PersonCycleTime[];
  trends: CycleTrend[];
}

const SalesCycleDashboard: React.FC = () => {
  const metrics = useSalesCycleMetrics();
  
  return (
    <Grid container spacing={3}>
      {/* Overview Card */}
      <Grid item xs={12} md={6}>
        <Card>
          <CardHeader title="Sales Cycle Overview" />
          <CardContent>
            <Box display="flex" justifyContent="space-around">
              <Statistic
                title="Average"
                value={`${metrics.overall.average}d`}
                icon={<Timer />}
              />
              <Statistic
                title="Median"
                value={`${metrics.overall.median}d`}
                icon={<Schedule />}
              />
              <Statistic
                title="Fastest"
                value={`${metrics.overall.fastest}d`}
                subtitle="Top 10%"
                icon={<Speed />}
                color="success"
              />
            </Box>
            
            {/* Distribution Chart */}
            <Box mt={3} height={200}>
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={metrics.distribution}>
                  <XAxis dataKey="range" />
                  <YAxis />
                  <Tooltip />
                  <Bar dataKey="count" fill="#8884d8">
                    {metrics.distribution.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={getCycleColor(entry.days)} />
                    ))}
                  </Bar>
                </BarChart>
              </ResponsiveContainer>
            </Box>
          </CardContent>
        </Card>
      </Grid>
      
      {/* By Stage Breakdown */}
      <Grid item xs={12} md={6}>
        <Card>
          <CardHeader title="Time Spent per Stage" />
          <CardContent>
            <List>
              {metrics.byStage.map(stage => (
                <ListItem key={stage.name}>
                  <ListItemText
                    primary={stage.name}
                    secondary={
                      <LinearProgress
                        variant="determinate"
                        value={(stage.avgDays / metrics.overall.average) * 100}
                        sx={{ mt: 1 }}
                      />
                    }
                  />
                  <ListItemSecondaryAction>
                    <Typography variant="body2">
                      {stage.avgDays}d ({stage.percentage}%)
                    </Typography>
                  </ListItemSecondaryAction>
                </ListItem>
              ))}
            </List>
            
            {/* Bottleneck Alert */}
            {metrics.bottlenecks.length > 0 && (
              <Alert severity="warning" sx={{ mt: 2 }}>
                <AlertTitle>Bottlenecks Detected</AlertTitle>
                {metrics.bottlenecks.map(bottleneck => (
                  <Typography key={bottleneck.stage} variant="body2">
                    • {bottleneck.stage}: {bottleneck.count} deals stuck >30 days
                  </Typography>
                ))}
              </Alert>
            )}
          </CardContent>
        </Card>
      </Grid>
      
      {/* Cycle Time Trends */}
      <Grid item xs={12}>
        <Card>
          <CardHeader 
            title="Sales Cycle Trends"
            subheader="Average days from Lead to Close"
          />
          <CardContent>
            <Box height={300}>
              <ResponsiveContainer width="100%" height="100%">
                <LineChart data={metrics.trends}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="month" />
                  <YAxis />
                  <Tooltip />
                  <Legend />
                  
                  <Line 
                    type="monotone" 
                    dataKey="average" 
                    stroke="#8884d8" 
                    name="Average"
                    strokeWidth={2}
                  />
                  <Line 
                    type="monotone" 
                    dataKey="target" 
                    stroke="#ff7300" 
                    strokeDasharray="5 5"
                    name="Target (30d)"
                  />
                  
                  <ReferenceLine y={30} stroke="red" strokeDasharray="3 3">
                    <Label value="Target" position="right" />
                  </ReferenceLine>
                </LineChart>
              </ResponsiveContainer>
            </Box>
            
            {/* Improvement Suggestions */}
            <Box mt={3}>
              <Typography variant="h6" gutterBottom>
                Acceleration Opportunities
              </Typography>
              <Grid container spacing={2}>
                {metrics.accelerationOpportunities.map(opp => (
                  <Grid item xs={12} md={4} key={opp.id}>
                    <Paper sx={{ p: 2 }}>
                      <Typography variant="subtitle2" color="primary">
                        {opp.title}
                      </Typography>
                      <Typography variant="body2" paragraph>
                        {opp.description}
                      </Typography>
                      <Typography variant="caption" color="textSecondary">
                        Potential: -{opp.potentialDaysSaved} days
                      </Typography>
                    </Paper>
                  </Grid>
                ))}
              </Grid>
            </Box>
          </CardContent>
        </Card>
      </Grid>
    </Grid>
  );
};
```

### 5. Real-time Alerts & Insights

```typescript
interface MetricAlert {
  id: string;
  type: 'warning' | 'success' | 'info';
  metric: string;
  message: string;
  action?: {
    label: string;
    onClick: () => void;
  };
  timestamp: Date;
}

const MetricAlertsSystem = () => {
  const [alerts, setAlerts] = useState<MetricAlert[]>([]);
  
  // Real-time metric monitoring
  useEffect(() => {
    const monitorMetrics = async () => {
      const metrics = await fetchCurrentMetrics();
      
      // Velocity Drop Alert
      if (metrics.velocity.change < -15) {
        addAlert({
          type: 'warning',
          metric: 'Sales Velocity',
          message: `Sales Velocity dropped ${Math.abs(metrics.velocity.change)}% - investigate pipeline health`,
          action: {
            label: 'Analyze',
            onClick: () => navigate('/analytics/velocity')
          }
        });
      }
      
      // Win Rate Improvement
      if (metrics.winRate.change > 10) {
        addAlert({
          type: 'success',
          metric: 'Win Rate',
          message: `Win Rate improved to ${metrics.winRate.current}% (+${metrics.winRate.change}%)`,
        });
      }
      
      // Stuck Deals Alert
      if (metrics.stuckDeals.count > 10) {
        addAlert({
          type: 'warning',
          metric: 'Pipeline Health',
          message: `${metrics.stuckDeals.count} deals stuck in pipeline >30 days`,
          action: {
            label: 'Review Deals',
            onClick: () => navigate('/pipeline?filter=stuck')
          }
        });
      }
    };
    
    // Check every hour
    const interval = setInterval(monitorMetrics, 60 * 60 * 1000);
    monitorMetrics(); // Initial check
    
    return () => clearInterval(interval);
  }, []);
  
  return (
    <Snackbar
      open={alerts.length > 0}
      anchorOrigin={{ vertical: 'top', horizontal: 'right' }}
    >
      <Alert 
        severity={alerts[0]?.type} 
        onClose={() => setAlerts(alerts.slice(1))}
        action={alerts[0]?.action && (
          <Button 
            color="inherit" 
            size="small"
            onClick={alerts[0].action.onClick}
          >
            {alerts[0].action.label}
          </Button>
        )}
      >
        <AlertTitle>{alerts[0]?.metric}</AlertTitle>
        {alerts[0]?.message}
      </Alert>
    </Snackbar>
  );
};
```

### 6. Backend Metrics Service

```java
@ApplicationScoped
public class SalesMetricsService {
    
    @Inject
    OpportunityRepository opportunityRepo;
    
    @Inject
    MetricsCache metricsCache;
    
    @Scheduled(every = "1h")
    void calculateMetrics() {
        log.info("Calculating sales metrics...");
        
        SalesMetrics metrics = SalesMetrics.builder()
            .velocity(calculateVelocity())
            .winRates(calculateWinRates())
            .dealSizeTrends(calculateDealSizeTrends())
            .cycleMetrics(calculateCycleMetrics())
            .timestamp(Instant.now())
            .build();
        
        metricsCache.put(metrics);
        
        // Check for alerts
        checkMetricAlerts(metrics);
    }
    
    private SalesVelocity calculateVelocity() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(1);
        
        List<Opportunity> wonDeals = opportunityRepo.findWonDeals(startDate, endDate);
        
        if (wonDeals.isEmpty()) {
            return SalesVelocity.ZERO;
        }
        
        double avgDealSize = wonDeals.stream()
            .mapToDouble(Opportunity::getValue)
            .average()
            .orElse(0);
        
        double avgCycleLength = wonDeals.stream()
            .mapToLong(o -> ChronoUnit.DAYS.between(
                o.getCreatedAt(), 
                o.getClosedAt()
            ))
            .average()
            .orElse(0);
        
        double winRate = (double) wonDeals.size() / 
            opportunityRepo.countByPeriod(startDate, endDate);
        
        double velocity = (wonDeals.size() * avgDealSize * winRate) / avgCycleLength;
        
        return SalesVelocity.builder()
            .numberOfDeals(wonDeals.size())
            .averageDealSize(avgDealSize)
            .winRate(winRate)
            .salesCycleLength(avgCycleLength)
            .velocity(velocity)
            .build();
    }
    
    private void checkMetricAlerts(SalesMetrics current) {
        SalesMetrics previous = metricsCache.getPrevious();
        
        if (previous == null) return;
        
        // Velocity Drop
        double velocityChange = (current.getVelocity().getVelocity() - 
            previous.getVelocity().getVelocity()) / 
            previous.getVelocity().getVelocity() * 100;
        
        if (velocityChange < -15) {
            alertService.send(Alert.builder()
                .type(AlertType.WARNING)
                .title("Sales Velocity Drop")
                .message(String.format("Velocity dropped %.1f%%", Math.abs(velocityChange)))
                .metric("velocity")
                .build());
        }
    }
}
```

---

## 🎯 BUSINESS VALUE

- **Early Warning System:** Probleme 2-4 Wochen früher erkennen
- **Performance Optimization:** Bottlenecks identifizieren und beheben
- **Revenue Prediction:** Genauere Forecasts durch Trend-Analyse
- **Team Coaching:** Datenbasierte Verbesserungsvorschläge

---

## 🚀 IMPLEMENTIERUNGS-PHASEN

1. **Phase 1:** Basic Metrics (Velocity, Win Rate)
2. **Phase 2:** Advanced Analytics (Trends, Breakdowns)
3. **Phase 3:** Real-time Alerts
4. **Phase 4:** Predictive Analytics

---

## 📊 SUCCESS METRICS

- **Metric Accuracy:** > 95% korrekte Berechnungen
- **Dashboard Load Time:** < 2s für alle Metriken
- **Alert Relevance:** > 80% der Alerts führen zu Aktionen
- **Decision Impact:** 30% bessere Forecast-Genauigkeit

---

**Nächster Schritt:** Metrics Calculation Engine im Backend implementieren