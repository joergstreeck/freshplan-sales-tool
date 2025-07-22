# M6 Analytics Module - Tech Concept

**Feature-Code:** M6  
**Feature-Name:** Analytics Module  
**Feature-Typ:** 🔀 FULLSTACK ANALYTICS  
**Erstellt:** 2025-07-20  
**Status:** Tech Concept (New Development)  
**Priorität:** HIGH - Business Intelligence  
**Geschätzter Aufwand:** 8-10 Tage (Full Analytics Platform)  

---

## 🧠 CLAUDE WORKING SECTION (15-Min Context Chunk)

### 📍 Kontext laden für produktive Arbeit

**Was ist M6?** Analytics Module - Comprehensive Business Intelligence Platform für Sales Performance, Customer Analytics und Predictive Insights

**Warum New Development?** Kein bestehendes Analytics-System - wir bauen ein modernes BI-Dashboard mit Real-Time Analytics und KI-Insights

**Aktueller Zustand:**
- ❌ **Kein Analytics:** Keine Business Intelligence oder Reporting-Features
- ❌ **Keine Metrics:** Keine Sales Performance Tracking
- ❌ **Keine Dashboards:** Keine visuellen Analytics oder Charts
- ❌ **Keine Insights:** Keine automatisierten Business Insights
- ✅ **Daten verfügbar:** Customer, Opportunity, User Daten in Database

**Abhängigkeiten verstehen:**
- **Benötigt:** M5 Customer Refactor (✅ VERFÜGBAR) - Enhanced Customer Data für Analytics
- **Benötigt:** M4 Pipeline (✅ VERFÜGBAR) - Opportunity Data für Sales Analytics
- **Benötigt:** FC-008 Security (✅ VERFÜGBAR) - User-Context für personalisierte Analytics
- **Integriert mit:** FC-037 Advanced Reporting, FC-040 Performance Monitoring

**Technischer Kern - Real-Time Analytics Platform:**
```typescript
export const AnalyticsDashboard: React.FC = () => {
    const { user } = useAuth();
    const { salesMetrics } = useRealTimeAnalytics(user.id);
    const { customerInsights } = useCustomerAnalytics(user.id);
    const { predictions } = usePredictiveAnalytics(user.id);
    
    return (
        <Grid container spacing={3}>
            {/* Executive Summary */}
            <Grid item xs={12}>
                <ExecutiveSummaryCard metrics={salesMetrics} />
            </Grid>
            
            {/* Sales Performance Charts */}
            <Grid item xs={12} md={8}>
                <SalesPerformanceChart data={salesMetrics.trends} />
            </Grid>
            
            {/* Pipeline Health */}
            <Grid item xs={12} md={4}>
                <PipelineHealthCard pipeline={salesMetrics.pipeline} />
            </Grid>
            
            {/* Customer Analytics */}
            <Grid item xs={12} md={6}>
                <CustomerSegmentChart data={customerInsights.segments} />
            </Grid>
            
            {/* Predictive Insights */}
            <Grid item xs={12} md={6}>
                <PredictiveInsightsCard predictions={predictions} />
            </Grid>
        </Grid>
    );
};
```

**Business Value - Comprehensive BI Platform:**
- **Problem:** Keine Business Intelligence für datenbasierte Entscheidungen
- **Lösung:** Real-Time Analytics mit Predictive Insights und Interactive Dashboards
- **ROI:** 8-10 Entwicklertage (~€12.000) für 200% bessere Sales Performance durch Data-Driven Decisions

---

## 🎯 ÜBERSICHT

### Business Value
**Problem:** Sales Teams arbeiten ohne datenbasierte Insights - Entscheidungen basieren auf Bauchgefühl statt Fakten

**Lösung:** Comprehensive Analytics Platform mit Real-Time Dashboards, Predictive Analytics und Automated Insights

**ROI:** 
- **Kosten:** 8-10 Entwicklertage (~€12.000) - Complete BI Platform Development
- **Sales Performance:** +200% durch data-driven decision making
- **Forecast Accuracy:** +150% durch predictive analytics
- **Time Saving:** 4-6 Stunden pro Manager/Woche durch automated insights
- **Revenue Impact:** +25% durch optimierte Sales-Prozesse
- **ROI-Ratio:** 20:1 (Break-even nach 3 Wochen)

### Kernfunktionen (New Development)
1. **Executive Dashboard** - High-level KPIs und Trends für Management
2. **Sales Performance Analytics** - Individual und Team Performance Tracking
3. **Customer Analytics** - Segmentation, Behavior Analysis, Churn Prediction
4. **Pipeline Analytics** - Deal Flow, Conversion Rates, Bottleneck Analysis
5. **Predictive Insights** - AI-powered Forecasting und Opportunity Scoring
6. **Interactive Reports** - Drill-down Analytics mit Custom Filters

---

## 🏗️ ARCHITEKTUR

### Analytics Architecture - Modern BI Platform
```
ANALYTICS PLATFORM ARCHITECTURE:
Frontend (React + D3.js)              Backend (Quarkus + Analytics Engine)
├── Executive Dashboard                ├── AnalyticsService
├── Sales Performance View             ├── MetricsAggregationService  
├── Customer Analytics View            ├── PredictiveAnalyticsService
├── Pipeline Analytics View            ├── ReportingService
├── Custom Reports Builder             └── DataWarehouseService
└── Real-Time Updates (WebSocket)
```

### Database Schema - Analytics Data Warehouse
```sql
-- Analytics Aggregation Tables
CREATE TABLE sales_metrics_daily (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    metric_date DATE NOT NULL,
    user_id UUID REFERENCES users(id),
    team_id UUID, -- For team aggregations
    
    -- Sales Metrics
    calls_made INTEGER DEFAULT 0,
    emails_sent INTEGER DEFAULT 0,
    meetings_held INTEGER DEFAULT 0,
    demos_given INTEGER DEFAULT 0,
    
    -- Pipeline Metrics
    opportunities_created INTEGER DEFAULT 0,
    opportunities_won INTEGER DEFAULT 0,
    opportunities_lost INTEGER DEFAULT 0,
    revenue_won DECIMAL(12, 2) DEFAULT 0.00,
    revenue_lost DECIMAL(12, 2) DEFAULT 0.00,
    
    -- Activity Metrics
    customers_contacted INTEGER DEFAULT 0,
    new_leads_generated INTEGER DEFAULT 0,
    follow_ups_completed INTEGER DEFAULT 0,
    
    -- Calculated Fields
    conversion_rate DECIMAL(5, 2), -- Won / Created
    average_deal_size DECIMAL(10, 2),
    sales_cycle_days DECIMAL(5, 1),
    
    -- Metadata
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE(metric_date, user_id)
);

-- Customer Analytics Aggregations
CREATE TABLE customer_analytics_daily (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    metric_date DATE NOT NULL,
    
    -- Customer Counts by Segment
    total_customers INTEGER DEFAULT 0,
    a_segment_customers INTEGER DEFAULT 0,
    b_segment_customers INTEGER DEFAULT 0,
    c_segment_customers INTEGER DEFAULT 0,
    
    -- Customer Acquisition
    new_customers INTEGER DEFAULT 0,
    churned_customers INTEGER DEFAULT 0,
    reactivated_customers INTEGER DEFAULT 0,
    
    -- Revenue Metrics
    total_customer_value DECIMAL(15, 2) DEFAULT 0.00,
    avg_customer_value DECIMAL(10, 2) DEFAULT 0.00,
    customer_lifetime_value DECIMAL(12, 2) DEFAULT 0.00,
    
    -- Engagement Metrics
    customers_contacted INTEGER DEFAULT 0,
    active_customers INTEGER DEFAULT 0, -- Customers with activity in last 30 days
    
    -- Geographic Distribution
    customers_by_region JSONB, -- {"DE": 150, "AT": 25, "CH": 12}
    customers_by_industry JSONB, -- {"Technology": 50, "Manufacturing": 30}
    
    -- Metadata
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Pipeline Analytics
CREATE TABLE pipeline_analytics_daily (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    metric_date DATE NOT NULL,
    user_id UUID REFERENCES users(id),
    
    -- Pipeline Stage Metrics
    stage_discovery_count INTEGER DEFAULT 0,
    stage_qualification_count INTEGER DEFAULT 0,
    stage_proposal_count INTEGER DEFAULT 0,
    stage_negotiation_count INTEGER DEFAULT 0,
    stage_closing_count INTEGER DEFAULT 0,
    
    -- Pipeline Value by Stage
    stage_discovery_value DECIMAL(12, 2) DEFAULT 0.00,
    stage_qualification_value DECIMAL(12, 2) DEFAULT 0.00,
    stage_proposal_value DECIMAL(12, 2) DEFAULT 0.00,
    stage_negotiation_value DECIMAL(12, 2) DEFAULT 0.00,
    stage_closing_value DECIMAL(12, 2) DEFAULT 0.00,
    
    -- Pipeline Health Indicators
    total_pipeline_value DECIMAL(12, 2) DEFAULT 0.00,
    weighted_pipeline_value DECIMAL(12, 2) DEFAULT 0.00, -- Value * Win Probability
    avg_days_in_pipeline DECIMAL(5, 1),
    stalled_opportunities INTEGER DEFAULT 0, -- No activity > 14 days
    
    -- Conversion Rates Between Stages
    discovery_to_qualification_rate DECIMAL(5, 2),
    qualification_to_proposal_rate DECIMAL(5, 2),
    proposal_to_negotiation_rate DECIMAL(5, 2),
    negotiation_to_closing_rate DECIMAL(5, 2),
    
    -- Metadata
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Predictive Analytics Results
CREATE TABLE predictive_insights (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    insight_type VARCHAR(50) NOT NULL, -- 'CHURN_RISK', 'OPPORTUNITY_SCORE', 'FORECAST'
    entity_type VARCHAR(50) NOT NULL, -- 'CUSTOMER', 'OPPORTUNITY', 'USER'
    entity_id UUID NOT NULL,
    
    -- Prediction Data
    prediction_score DECIMAL(5, 4), -- 0.0000 to 1.0000
    confidence_level DECIMAL(5, 4), -- Model confidence
    prediction_date DATE NOT NULL,
    valid_until DATE,
    
    -- Prediction Details
    factors JSONB, -- Contributing factors as JSON
    recommendations JSONB, -- Recommended actions
    
    -- Model Information
    model_version VARCHAR(20),
    model_accuracy DECIMAL(5, 4),
    
    -- Metadata
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_predictive_insights_type_entity (insight_type, entity_type, entity_id),
    INDEX idx_predictive_insights_date (prediction_date)
);

-- Custom Analytics Reports
CREATE TABLE analytics_reports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    report_name VARCHAR(255) NOT NULL,
    report_type VARCHAR(50) NOT NULL, -- 'DASHBOARD', 'CHART', 'TABLE', 'EXPORT'
    
    -- Report Configuration
    config JSONB NOT NULL, -- Chart type, filters, columns, etc.
    sql_query TEXT, -- For custom SQL reports
    
    -- Access Control
    created_by UUID REFERENCES users(id),
    is_public BOOLEAN DEFAULT false,
    allowed_roles TEXT[], -- ['admin', 'manager', 'sales']
    
    -- Scheduling
    is_scheduled BOOLEAN DEFAULT false,
    schedule_cron VARCHAR(100), -- Cron expression for automated reports
    last_executed TIMESTAMP,
    
    -- Metadata
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Analytics Cache (for performance)
CREATE TABLE analytics_cache (
    cache_key VARCHAR(500) PRIMARY KEY,
    cache_data JSONB NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Performance Indexes
CREATE INDEX idx_sales_metrics_date_user ON sales_metrics_daily(metric_date, user_id);
CREATE INDEX idx_sales_metrics_date ON sales_metrics_daily(metric_date);
CREATE INDEX idx_customer_analytics_date ON customer_analytics_daily(metric_date);
CREATE INDEX idx_pipeline_analytics_date_user ON pipeline_analytics_daily(metric_date, user_id);
CREATE INDEX idx_analytics_cache_expires ON analytics_cache(expires_at);
```

### Backend-Architecture - Analytics Engine
```java
// Analytics Service - Core Business Intelligence
@ApplicationScoped
@Transactional
public class AnalyticsService {
    
    @Inject
    SalesMetricsRepository salesMetricsRepository;
    
    @Inject
    CustomerAnalyticsRepository customerAnalyticsRepository;
    
    @Inject
    PipelineAnalyticsRepository pipelineAnalyticsRepository;
    
    @Inject
    PredictiveAnalyticsService predictiveService;
    
    @Inject
    AnalyticsCacheService cacheService;
    
    // Executive Dashboard Data
    public ExecutiveDashboardResponse getExecutiveDashboard(UUID userId, DateRange dateRange) {
        String cacheKey = "executive_dashboard_" + userId + "_" + dateRange.toString();
        
        return cacheService.getOrCompute(cacheKey, Duration.ofMinutes(15), () -> {
            
            // Aggregate Sales Metrics
            SalesMetricsSummary salesSummary = salesMetricsRepository
                .getAggregatedMetrics(userId, dateRange);
            
            // Customer Analytics
            CustomerMetricsSummary customerSummary = customerAnalyticsRepository
                .getCustomerMetrics(dateRange);
            
            // Pipeline Health
            PipelineMetricsSummary pipelineSummary = pipelineAnalyticsRepository
                .getPipelineMetrics(userId, dateRange);
            
            // Predictive Insights
            List<PredictiveInsight> insights = predictiveService
                .getTopInsights(userId, 5);
            
            return ExecutiveDashboardResponse.builder()
                .salesMetrics(salesSummary)
                .customerMetrics(customerSummary)
                .pipelineMetrics(pipelineSummary)
                .predictiveInsights(insights)
                .generatedAt(LocalDateTime.now())
                .build();
        });
    }
    
    // Sales Performance Analytics
    public SalesPerformanceResponse getSalesPerformance(UUID userId, DateRange dateRange, String granularity) {
        return switch (granularity.toLowerCase()) {
            case "daily" -> getDailySalesPerformance(userId, dateRange);
            case "weekly" -> getWeeklySalesPerformance(userId, dateRange);
            case "monthly" -> getMonthlySalesPerformance(userId, dateRange);
            default -> throw new IllegalArgumentException("Invalid granularity: " + granularity);
        };
    }
    
    private SalesPerformanceResponse getDailySalesPerformance(UUID userId, DateRange dateRange) {
        List<SalesMetricsDaily> metrics = salesMetricsRepository
            .findByUserAndDateRange(userId, dateRange.getStart(), dateRange.getEnd());
        
        // Calculate trends and comparisons
        List<SalesDataPoint> dataPoints = metrics.stream()
            .map(this::mapToDataPoint)
            .collect(Collectors.toList());
        
        // Calculate performance indicators
        PerformanceIndicators indicators = calculatePerformanceIndicators(metrics);
        
        // Generate insights
        List<PerformanceInsight> insights = generatePerformanceInsights(metrics);
        
        return SalesPerformanceResponse.builder()
            .dataPoints(dataPoints)
            .indicators(indicators)
            .insights(insights)
            .dateRange(dateRange)
            .granularity("daily")
            .build();
    }
    
    // Customer Analytics
    public CustomerAnalyticsResponse getCustomerAnalytics(DateRange dateRange) {
        String cacheKey = "customer_analytics_" + dateRange.toString();
        
        return cacheService.getOrCompute(cacheKey, Duration.ofMinutes(30), () -> {
            
            // Customer Segmentation
            CustomerSegmentation segmentation = customerAnalyticsRepository
                .getCustomerSegmentation(dateRange);
            
            // Customer Acquisition Funnel
            AcquisitionFunnel funnel = customerAnalyticsRepository
                .getAcquisitionFunnel(dateRange);
            
            // Customer Lifetime Value Analysis
            CLVAnalysis clvAnalysis = calculateCustomerLifetimeValue(dateRange);
            
            // Geographic Distribution
            GeographicDistribution geoDistribution = customerAnalyticsRepository
                .getGeographicDistribution();
            
            // Industry Analysis
            IndustryAnalysis industryAnalysis = customerAnalyticsRepository
                .getIndustryAnalysis();
            
            return CustomerAnalyticsResponse.builder()
                .segmentation(segmentation)
                .acquisitionFunnel(funnel)
                .clvAnalysis(clvAnalysis)
                .geographicDistribution(geoDistribution)
                .industryAnalysis(industryAnalysis)
                .dateRange(dateRange)
                .build();
        });
    }
    
    // Pipeline Analytics
    public PipelineAnalyticsResponse getPipelineAnalytics(UUID userId, DateRange dateRange) {
        // Pipeline Flow Analysis
        PipelineFlow pipelineFlow = pipelineAnalyticsRepository
            .getPipelineFlow(userId, dateRange);
        
        // Conversion Rates by Stage
        List<StageConversionRate> conversionRates = pipelineAnalyticsRepository
            .getStageConversionRates(userId, dateRange);
        
        // Pipeline Health Metrics
        PipelineHealth health = calculatePipelineHealth(userId, dateRange);
        
        // Bottleneck Analysis
        List<PipelineBottleneck> bottlenecks = identifyPipelineBottlenecks(userId, dateRange);
        
        // Forecast
        PipelineForecast forecast = generatePipelineForecast(userId);
        
        return PipelineAnalyticsResponse.builder()
            .pipelineFlow(pipelineFlow)
            .conversionRates(conversionRates)
            .health(health)
            .bottlenecks(bottlenecks)
            .forecast(forecast)
            .dateRange(dateRange)
            .build();
    }
    
    // Real-Time Metrics Aggregation
    @Scheduled(cron = "0 0 1 * * ?") // Daily at 1 AM
    public void aggregateDailyMetrics() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        
        // Aggregate sales metrics for all users
        List<User> activeUsers = userRepository.findActiveUsers();
        
        for (User user : activeUsers) {
            aggregateUserSalesMetrics(user.getId(), yesterday);
            aggregateUserPipelineMetrics(user.getId(), yesterday);
        }
        
        // Aggregate customer metrics (global)
        aggregateCustomerMetrics(yesterday);
        
        // Update predictive models
        predictiveService.updateModels();
    }
    
    private void aggregateUserSalesMetrics(UUID userId, LocalDate date) {
        // Calculate daily metrics from raw data
        SalesMetricsDaily metrics = new SalesMetricsDaily();
        metrics.setMetricDate(date);
        metrics.setUserId(userId);
        
        // Count activities
        metrics.setCallsMade(activityRepository.countCallsByUserAndDate(userId, date));
        metrics.setEmailsSent(activityRepository.countEmailsByUserAndDate(userId, date));
        metrics.setMeetingsHeld(activityRepository.countMeetingsByUserAndDate(userId, date));
        
        // Count opportunities
        metrics.setOpportunitiesCreated(opportunityRepository.countCreatedByUserAndDate(userId, date));
        metrics.setOpportunitiesWon(opportunityRepository.countWonByUserAndDate(userId, date));
        metrics.setOpportunitiesLost(opportunityRepository.countLostByUserAndDate(userId, date));
        
        // Calculate revenue
        metrics.setRevenueWon(opportunityRepository.sumWonRevenueByUserAndDate(userId, date));
        metrics.setRevenueLost(opportunityRepository.sumLostRevenueByUserAndDate(userId, date));
        
        // Calculate derived metrics
        if (metrics.getOpportunitiesCreated() > 0) {
            metrics.setConversionRate(
                metrics.getOpportunitiesWon().doubleValue() / 
                metrics.getOpportunitiesCreated().doubleValue() * 100
            );
        }
        
        if (metrics.getOpportunitiesWon() > 0) {
            metrics.setAverageDealSize(
                metrics.getRevenueWon().divide(
                    BigDecimal.valueOf(metrics.getOpportunitiesWon()), 
                    2, RoundingMode.HALF_UP
                )
            );
        }
        
        salesMetricsRepository.persist(metrics);
    }
    
    // Custom Report Builder
    public ReportResponse generateCustomReport(CustomReportRequest request) {
        // Validate request
        validateReportRequest(request);
        
        // Build dynamic query
        String sqlQuery = buildReportQuery(request);
        
        // Execute query
        List<Map<String, Object>> rawData = jdbcTemplate.queryForList(sqlQuery);
        
        // Process and format data
        ReportData processedData = processReportData(rawData, request);
        
        // Generate visualizations if requested
        List<ChartConfiguration> charts = generateReportCharts(processedData, request);
        
        return ReportResponse.builder()
            .reportId(UUID.randomUUID())
            .reportName(request.getReportName())
            .data(processedData)
            .charts(charts)
            .generatedAt(LocalDateTime.now())
            .build();
    }
}

// Predictive Analytics Service
@ApplicationScoped
public class PredictiveAnalyticsService {
    
    @Inject
    MLModelService mlModelService;
    
    @Inject
    PredictiveInsightRepository insightRepository;
    
    // Opportunity Scoring
    public OpportunityScore calculateOpportunityScore(UUID opportunityId) {
        Opportunity opportunity = opportunityRepository.findById(opportunityId);
        
        // Feature extraction
        Map<String, Double> features = extractOpportunityFeatures(opportunity);
        
        // ML Model prediction
        double score = mlModelService.predictOpportunityWinProbability(features);
        double confidence = mlModelService.getModelConfidence();
        
        // Generate explanation
        List<String> factors = explainOpportunityScore(features, score);
        List<String> recommendations = generateOpportunityRecommendations(opportunity, score);
        
        OpportunityScore opportunityScore = OpportunityScore.builder()
            .opportunityId(opportunityId)
            .score(score)
            .confidence(confidence)
            .factors(factors)
            .recommendations(recommendations)
            .modelVersion(mlModelService.getModelVersion())
            .build();
        
        // Save insight for tracking
        saveOpportunityInsight(opportunityScore);
        
        return opportunityScore;
    }
    
    // Customer Churn Prediction
    public ChurnRiskAssessment assessChurnRisk(UUID customerId) {
        Customer customer = customerRepository.findById(customerId);
        
        // Feature extraction
        Map<String, Double> features = extractCustomerFeatures(customer);
        
        // ML Model prediction
        double churnProbability = mlModelService.predictChurnProbability(features);
        double confidence = mlModelService.getModelConfidence();
        
        // Risk categorization
        ChurnRiskLevel riskLevel = categorizeChurnRisk(churnProbability);
        
        // Generate retention strategies
        List<RetentionStrategy> strategies = generateRetentionStrategies(customer, churnProbability);
        
        return ChurnRiskAssessment.builder()
            .customerId(customerId)
            .churnProbability(churnProbability)
            .confidence(confidence)
            .riskLevel(riskLevel)
            .retentionStrategies(strategies)
            .assessmentDate(LocalDate.now())
            .build();
    }
    
    // Sales Forecasting
    public SalesForecast generateSalesForecast(UUID userId, Period forecastPeriod) {
        // Historical data analysis
        List<SalesMetricsDaily> historicalData = salesMetricsRepository
            .findHistoricalData(userId, forecastPeriod.getHistoricalPeriod());
        
        // Time series forecasting
        TimeSeriesModel model = mlModelService.buildTimeSeriesModel(historicalData);
        
        // Generate forecast
        List<ForecastDataPoint> forecast = model.predict(forecastPeriod);
        
        // Calculate confidence intervals
        forecast.forEach(point -> {
            point.setLowerBound(point.getValue() * 0.85); // 85% confidence
            point.setUpperBound(point.getValue() * 1.15); // 115% confidence
        });
        
        // Identify trends and seasonality
        TrendAnalysis trends = analyzeTrends(historicalData);
        SeasonalityAnalysis seasonality = analyzeSeasonality(historicalData);
        
        return SalesForecast.builder()
            .userId(userId)
            .forecastPeriod(forecastPeriod)
            .forecast(forecast)
            .trends(trends)
            .seasonality(seasonality)
            .accuracy(model.getAccuracy())
            .build();
    }
}

// Real-Time Analytics WebSocket
@ApplicationScoped
public class AnalyticsWebSocketService {
    
    @Inject
    @ChannelEmitter("analytics-updates")
    Emitter<AnalyticsUpdate> analyticsEmitter;
    
    // Broadcast real-time updates
    public void broadcastMetricsUpdate(UUID userId, MetricsUpdate update) {
        AnalyticsUpdate analyticsUpdate = AnalyticsUpdate.builder()
            .userId(userId)
            .updateType("METRICS")
            .data(update)
            .timestamp(LocalDateTime.now())
            .build();
        
        analyticsEmitter.send(analyticsUpdate);
    }
    
    public void broadcastPipelineUpdate(UUID userId, PipelineUpdate update) {
        AnalyticsUpdate analyticsUpdate = AnalyticsUpdate.builder()
            .userId(userId)
            .updateType("PIPELINE")
            .data(update)
            .timestamp(LocalDateTime.now())
            .build();
        
        analyticsEmitter.send(analyticsUpdate);
    }
}
```

### Frontend-Architecture - Interactive Analytics Dashboard
```typescript
// Analytics Dashboard Main Component
interface AnalyticsDashboardProps {
    user: User;
}

export const AnalyticsDashboard: React.FC<AnalyticsDashboardProps> = ({ user }) => {
    const [selectedDateRange, setSelectedDateRange] = useState<DateRange>(getDefaultDateRange());
    const [activeView, setActiveView] = useState<string>('executive');
    
    // Real-time data hooks
    const { executiveData, isLoading: executiveLoading } = useExecutiveDashboard(user.id, selectedDateRange);
    const { salesPerformance } = useSalesPerformance(user.id, selectedDateRange);
    const { customerAnalytics } = useCustomerAnalytics(selectedDateRange);
    const { pipelineAnalytics } = usePipelineAnalytics(user.id, selectedDateRange);
    
    // Real-time updates
    useAnalyticsWebSocket(user.id, {
        onMetricsUpdate: (update) => {
            // Refresh relevant data
            invalidateQueries(['executive-dashboard', 'sales-performance']);
        },
        onPipelineUpdate: (update) => {
            invalidateQueries(['pipeline-analytics']);
        }
    });
    
    if (executiveLoading) {
        return <AnalyticsDashboardSkeleton />;
    }
    
    return (
        <Box sx={{ height: '100vh', display: 'flex', flexDirection: 'column' }}>
            {/* Analytics Header with Navigation */}
            <AnalyticsHeader 
                user={user}
                activeView={activeView}
                onViewChange={setActiveView}
                dateRange={selectedDateRange}
                onDateRangeChange={setSelectedDateRange}
            />
            
            {/* Main Analytics Content */}
            <Box sx={{ flex: 1, overflow: 'auto', p: 3 }}>
                {activeView === 'executive' && (
                    <ExecutiveView 
                        data={executiveData}
                        dateRange={selectedDateRange}
                    />
                )}
                
                {activeView === 'sales' && (
                    <SalesPerformanceView 
                        data={salesPerformance}
                        user={user}
                        dateRange={selectedDateRange}
                    />
                )}
                
                {activeView === 'customers' && (
                    <CustomerAnalyticsView 
                        data={customerAnalytics}
                        dateRange={selectedDateRange}
                    />
                )}
                
                {activeView === 'pipeline' && (
                    <PipelineAnalyticsView 
                        data={pipelineAnalytics}
                        user={user}
                        dateRange={selectedDateRange}
                    />
                )}
                
                {activeView === 'custom' && (
                    <CustomReportsView user={user} />
                )}
            </Box>
        </Box>
    );
};

// Executive Dashboard View
interface ExecutiveViewProps {
    data: ExecutiveDashboardData;
    dateRange: DateRange;
}

export const ExecutiveView: React.FC<ExecutiveViewProps> = ({ data, dateRange }) => {
    return (
        <Grid container spacing={3}>
            {/* Key Performance Indicators */}
            <Grid item xs={12}>
                <Paper sx={{ p: 3 }}>
                    <Typography variant="h5" gutterBottom sx={{ fontFamily: 'Antonio' }}>
                        📊 Executive Summary
                    </Typography>
                    
                    <Grid container spacing={3}>
                        <Grid item xs={12} sm={6} md={3}>
                            <KPICard
                                title="Total Revenue"
                                value={formatCurrency(data.salesMetrics.totalRevenue)}
                                trend={data.salesMetrics.revenueTrend}
                                icon={<AttachMoneyIcon />}
                                color="#4CAF50"
                            />
                        </Grid>
                        
                        <Grid item xs={12} sm={6} md={3}>
                            <KPICard
                                title="Pipeline Value"
                                value={formatCurrency(data.pipelineMetrics.totalValue)}
                                trend={data.pipelineMetrics.valueTrend}
                                icon={<TrendingUpIcon />}
                                color="#2196F3"
                            />
                        </Grid>
                        
                        <Grid item xs={12} sm={6} md={3}>
                            <KPICard
                                title="Win Rate"
                                value={`${data.salesMetrics.winRate}%`}
                                trend={data.salesMetrics.winRateTrend}
                                icon={<EmojiEventsIcon />}
                                color="#FF9800"
                            />
                        </Grid>
                        
                        <Grid item xs={12} sm={6} md={3}>
                            <KPICard
                                title="Active Customers"
                                value={data.customerMetrics.activeCustomers.toString()}
                                trend={data.customerMetrics.customerGrowth}
                                icon={<PeopleIcon />}
                                color="#9C27B0"
                            />
                        </Grid>
                    </Grid>
                </Paper>
            </Grid>
            
            {/* Revenue Trend Chart */}
            <Grid item xs={12} md={8}>
                <Paper sx={{ p: 3 }}>
                    <Typography variant="h6" gutterBottom>
                        📈 Revenue Trend
                    </Typography>
                    <RevenueTrendChart 
                        data={data.salesMetrics.dailyRevenue}
                        height={300}
                    />
                </Paper>
            </Grid>
            
            {/* Pipeline Health */}
            <Grid item xs={12} md={4}>
                <Paper sx={{ p: 3 }}>
                    <Typography variant="h6" gutterBottom>
                        🎯 Pipeline Health
                    </Typography>
                    <PipelineHealthGauge 
                        health={data.pipelineMetrics.healthScore}
                        size={200}
                    />
                </Paper>
            </Grid>
            
            {/* Top Performing Sales Reps */}
            <Grid item xs={12} md={6}>
                <Paper sx={{ p: 3 }}>
                    <Typography variant="h6" gutterBottom>
                        🏆 Top Performers
                    </Typography>
                    <TopPerformersChart 
                        data={data.salesMetrics.topPerformers}
                    />
                </Paper>
            </Grid>
            
            {/* Customer Acquisition Funnel */}
            <Grid item xs={12} md={6}>
                <Paper sx={{ p: 3 }}>
                    <Typography variant="h6" gutterBottom>
                        🔄 Acquisition Funnel
                    </Typography>
                    <AcquisitionFunnelChart 
                        data={data.customerMetrics.acquisitionFunnel}
                    />
                </Paper>
            </Grid>
            
            {/* Predictive Insights */}
            <Grid item xs={12}>
                <Paper sx={{ p: 3 }}>
                    <Typography variant="h6" gutterBottom>
                        🔮 AI Insights & Recommendations
                    </Typography>
                    <Grid container spacing={2}>
                        {data.predictiveInsights.map((insight) => (
                            <Grid item xs={12} md={6} lg={4} key={insight.id}>
                                <PredictiveInsightCard insight={insight} />
                            </Grid>
                        ))}
                    </Grid>
                </Paper>
            </Grid>
        </Grid>
    );
};

// Sales Performance View with Advanced Charts
interface SalesPerformanceViewProps {
    data: SalesPerformanceData;
    user: User;
    dateRange: DateRange;
}

export const SalesPerformanceView: React.FC<SalesPerformanceViewProps> = ({ data, user, dateRange }) => {
    const [selectedMetric, setSelectedMetric] = useState<string>('revenue');
    const [comparisonMode, setComparisonMode] = useState<string>('previous_period');
    
    return (
        <Grid container spacing={3}>
            {/* Performance Controls */}
            <Grid item xs={12}>
                <Paper sx={{ p: 3 }}>
                    <Box sx={{ display: 'flex', gap: 2, alignItems: 'center', mb: 3 }}>
                        <Typography variant="h5" sx={{ fontFamily: 'Antonio', flex: 1 }}>
                            📊 Sales Performance Analysis
                        </Typography>
                        
                        <FormControl size="small" sx={{ minWidth: 150 }}>
                            <InputLabel>Metric</InputLabel>
                            <Select
                                value={selectedMetric}
                                label="Metric"
                                onChange={(e) => setSelectedMetric(e.target.value)}
                            >
                                <MenuItem value="revenue">Revenue</MenuItem>
                                <MenuItem value="opportunities">Opportunities</MenuItem>
                                <MenuItem value="activities">Activities</MenuItem>
                                <MenuItem value="conversion">Conversion Rate</MenuItem>
                            </Select>
                        </FormControl>
                        
                        <FormControl size="small" sx={{ minWidth: 150 }}>
                            <InputLabel>Compare to</InputLabel>
                            <Select
                                value={comparisonMode}
                                label="Compare to"
                                onChange={(e) => setComparisonMode(e.target.value)}
                            >
                                <MenuItem value="previous_period">Previous Period</MenuItem>
                                <MenuItem value="last_year">Last Year</MenuItem>
                                <MenuItem value="team_average">Team Average</MenuItem>
                                <MenuItem value="target">Target</MenuItem>
                            </Select>
                        </FormControl>
                    </Box>
                    
                    {/* Main Performance Chart */}
                    <SalesPerformanceChart 
                        data={data.dataPoints}
                        metric={selectedMetric}
                        comparisonMode={comparisonMode}
                        height={400}
                    />
                </Paper>
            </Grid>
            
            {/* Activity Breakdown */}
            <Grid item xs={12} md={4}>
                <Paper sx={{ p: 3 }}>
                    <Typography variant="h6" gutterBottom>
                        📞 Activity Breakdown
                    </Typography>
                    <ActivityBreakdownChart 
                        data={data.activityMetrics}
                        type="donut"
                    />
                </Paper>
            </Grid>
            
            {/* Conversion Funnel */}
            <Grid item xs={12} md={4}>
                <Paper sx={{ p: 3 }}>
                    <Typography variant="h6" gutterBottom>
                        🎯 Conversion Funnel
                    </Typography>
                    <ConversionFunnelChart 
                        data={data.conversionMetrics}
                    />
                </Paper>
            </Grid>
            
            {/* Performance Goals */}
            <Grid item xs={12} md={4}>
                <Paper sx={{ p: 3 }}>
                    <Typography variant="h6" gutterBottom>
                        🎯 Goal Progress
                    </Typography>
                    <GoalProgressChart 
                        data={data.goalProgress}
                    />
                </Paper>
            </Grid>
            
            {/* Detailed Metrics Table */}
            <Grid item xs={12}>
                <Paper sx={{ p: 3 }}>
                    <Typography variant="h6" gutterBottom>
                        📋 Detailed Performance Metrics
                    </Typography>
                    <SalesMetricsTable 
                        data={data.detailedMetrics}
                        onExport={() => exportSalesData(data, dateRange)}
                    />
                </Paper>
            </Grid>
        </Grid>
    );
};

// Interactive Chart Components using Recharts
interface SalesPerformanceChartProps {
    data: SalesDataPoint[];
    metric: string;
    comparisonMode: string;
    height: number;
}

export const SalesPerformanceChart: React.FC<SalesPerformanceChartProps> = ({ 
    data, 
    metric, 
    comparisonMode, 
    height 
}) => {
    const chartData = useMemo(() => {
        return data.map(point => ({
            date: format(new Date(point.date), 'MMM dd'),
            current: point[metric],
            previous: point[`${metric}_previous`],
            target: point[`${metric}_target`]
        }));
    }, [data, metric]);
    
    return (
        <ResponsiveContainer width="100%" height={height}>
            <LineChart data={chartData}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="date" />
                <YAxis />
                <Tooltip 
                    formatter={(value, name) => [
                        typeof value === 'number' ? formatNumber(value) : value,
                        name
                    ]}
                />
                <Legend />
                
                <Line 
                    type="monotone" 
                    dataKey="current" 
                    stroke="#2196F3" 
                    strokeWidth={3}
                    name="Current Period"
                />
                
                {comparisonMode !== 'none' && (
                    <Line 
                        type="monotone" 
                        dataKey="previous" 
                        stroke="#FF9800" 
                        strokeDasharray="5 5"
                        name="Comparison"
                    />
                )}
                
                <Line 
                    type="monotone" 
                    dataKey="target" 
                    stroke="#4CAF50" 
                    strokeDasharray="10 5"
                    name="Target"
                />
            </LineChart>
        </ResponsiveContainer>
    );
};

// Real-time Analytics Hook
export const useRealTimeAnalytics = (userId: string) => {
    const [metrics, setMetrics] = useState<RealTimeMetrics | null>(null);
    const [isConnected, setIsConnected] = useState(false);
    
    useEffect(() => {
        const websocket = new WebSocket(`ws://localhost:8080/analytics/ws/${userId}`);
        
        websocket.onopen = () => {
            setIsConnected(true);
        };
        
        websocket.onmessage = (event) => {
            const update: AnalyticsUpdate = JSON.parse(event.data);
            
            if (update.updateType === 'METRICS') {
                setMetrics(prevMetrics => ({
                    ...prevMetrics,
                    ...update.data
                }));
            }
        };
        
        websocket.onclose = () => {
            setIsConnected(false);
        };
        
        return () => {
            websocket.close();
        };
    }, [userId]);
    
    return { metrics, isConnected };
};

// Custom Report Builder Component
export const CustomReportBuilder: React.FC = () => {
    const [reportConfig, setReportConfig] = useState<ReportConfig>({
        reportName: '',
        reportType: 'CHART',
        dataSource: 'SALES_METRICS',
        filters: [],
        groupBy: [],
        metrics: [],
        chartType: 'LINE'
    });
    
    const [previewData, setPreviewData] = useState<any>(null);
    const [isGenerating, setIsGenerating] = useState(false);
    
    const generatePreview = async () => {
        setIsGenerating(true);
        try {
            const response = await apiClient.post('/api/analytics/reports/preview', reportConfig);
            setPreviewData(response.data);
        } catch (error) {
            console.error('Preview generation failed:', error);
        } finally {
            setIsGenerating(false);
        }
    };
    
    const saveReport = async () => {
        try {
            await apiClient.post('/api/analytics/reports', reportConfig);
            // Show success message
        } catch (error) {
            console.error('Report save failed:', error);
        }
    };
    
    return (
        <Grid container spacing={3}>
            {/* Report Configuration */}
            <Grid item xs={12} md={4}>
                <Paper sx={{ p: 3 }}>
                    <Typography variant="h6" gutterBottom>
                        ⚙️ Report Configuration
                    </Typography>
                    
                    <ReportConfigForm 
                        config={reportConfig}
                        onChange={setReportConfig}
                    />
                    
                    <Box sx={{ mt: 3, display: 'flex', gap: 2 }}>
                        <Button 
                            variant="outlined" 
                            onClick={generatePreview}
                            disabled={isGenerating}
                            fullWidth
                        >
                            Preview
                        </Button>
                        <Button 
                            variant="contained" 
                            onClick={saveReport}
                            disabled={!previewData}
                            fullWidth
                        >
                            Save Report
                        </Button>
                    </Box>
                </Paper>
            </Grid>
            
            {/* Report Preview */}
            <Grid item xs={12} md={8}>
                <Paper sx={{ p: 3 }}>
                    <Typography variant="h6" gutterBottom>
                        👁️ Report Preview
                    </Typography>
                    
                    {isGenerating && <CircularProgress />}
                    
                    {previewData && (
                        <CustomReportPreview 
                            data={previewData}
                            config={reportConfig}
                        />
                    )}
                </Paper>
            </Grid>
        </Grid>
    );
};
```

---

## 🔄 ABHÄNGIGKEITEN

### Benötigt diese Features:
- **M5 Customer Refactor** - Enhanced Customer Data für Customer Analytics ✅ VERFÜGBAR
- **M4 Opportunity Pipeline** - Opportunity Data für Sales Analytics ✅ VERFÜGBAR
- **FC-008 Security Foundation** - User Context für personalisierte Analytics ✅ VERFÜGBAR

### Ermöglicht diese Features:
- **FC-037 Advanced Reporting** - Enhanced Reporting mit BI Platform Integration
- **FC-040 Performance Monitoring** - System Performance Analytics
- **FC-007 Chef-Dashboard** - Executive BI Dashboard für Management
- **All Analytics-dependent Features** - Foundation für alle Analytics-Features

### Integriert mit:
- **M3 Sales Cockpit** - Real-time Analytics Integration in Dashboard
- **FC-002 Smart Customer Insights** - Customer Analytics Data
- **FC-034 Instant Insights** - Real-time Business Intelligence

---

## 🧪 TESTING-STRATEGIE

### Component Tests - Analytics Components
```typescript
describe('AnalyticsDashboard', () => {
    it('should render executive dashboard with metrics', () => {
        const mockData = {
            salesMetrics: {
                totalRevenue: 150000,
                revenueTrend: 'up',
                winRate: 75
            },
            customerMetrics: {
                activeCustomers: 120,
                customerGrowth: 'up'
            }
        };
        
        render(<AnalyticsDashboard user={mockUser} />);
        
        expect(screen.getByText('📊 Executive Summary')).toBeInTheDocument();
        expect(screen.getByText('€150,000')).toBeInTheDocument();
        expect(screen.getByText('75%')).toBeInTheDocument();
        expect(screen.getByText('120')).toBeInTheDocument();
    });
    
    it('should update data in real-time via WebSocket', async () => {
        const mockWebSocket = new MockWebSocket();
        global.WebSocket = jest.fn(() => mockWebSocket);
        
        render(<AnalyticsDashboard user={mockUser} />);
        
        // Simulate WebSocket message
        const update = {
            updateType: 'METRICS',
            data: { totalRevenue: 160000 }
        };
        
        mockWebSocket.onmessage({ data: JSON.stringify(update) });
        
        await waitFor(() => {
            expect(screen.getByText('€160,000')).toBeInTheDocument();
        });
    });
});

describe('SalesPerformanceChart', () => {
    it('should render performance chart with data', () => {
        const mockData = [
            { date: '2025-07-01', revenue: 10000, opportunities: 5 },
            { date: '2025-07-02', revenue: 12000, opportunities: 6 }
        ];
        
        render(
            <SalesPerformanceChart 
                data={mockData}
                metric="revenue"
                comparisonMode="previous_period"
                height={300}
            />
        );
        
        // Check that Recharts components are rendered
        expect(document.querySelector('.recharts-wrapper')).toBeInTheDocument();
    });
    
    it('should switch between different metrics', () => {
        const mockData = [
            { date: '2025-07-01', revenue: 10000, opportunities: 5 }
        ];
        
        const { rerender } = render(
            <SalesPerformanceChart 
                data={mockData}
                metric="revenue"
                comparisonMode="none"
                height={300}
            />
        );
        
        // Change metric
        rerender(
            <SalesPerformanceChart 
                data={mockData}
                metric="opportunities"
                comparisonMode="none"
                height={300}
            />
        );
        
        // Chart should update with new metric
        expect(document.querySelector('.recharts-wrapper')).toBeInTheDocument();
    });
});

describe('CustomReportBuilder', () => {
    it('should build and preview custom reports', async () => {
        const mockPreviewData = {
            data: [{ metric: 'revenue', value: 50000 }],
            charts: []
        };
        
        jest.mocked(apiClient.post).mockResolvedValue({ data: mockPreviewData });
        
        render(<CustomReportBuilder />);
        
        // Configure report
        fireEvent.change(screen.getByLabelText('Report Name'), { 
            target: { value: 'Monthly Sales Report' } 
        });
        
        // Generate preview
        fireEvent.click(screen.getByText('Preview'));
        
        await waitFor(() => {
            expect(apiClient.post).toHaveBeenCalledWith('/api/analytics/reports/preview', expect.any(Object));
        });
    });
});
```

### Integration Tests - Analytics Service
```java
@QuarkusTest
class AnalyticsServiceTest {
    
    @Inject
    AnalyticsService analyticsService;
    
    @Test
    @Transactional
    void testGetExecutiveDashboard() {
        // Given
        UUID userId = createTestUser().getId();
        DateRange dateRange = DateRange.lastMonth();
        
        // Create test data
        createTestSalesMetrics(userId, dateRange);
        createTestCustomerData();
        createTestPipelineData(userId);
        
        // When
        ExecutiveDashboardResponse response = analyticsService.getExecutiveDashboard(userId, dateRange);
        
        // Then
        assertNotNull(response);
        assertNotNull(response.getSalesMetrics());
        assertNotNull(response.getCustomerMetrics());
        assertNotNull(response.getPipelineMetrics());
        assertTrue(response.getPredictiveInsights().size() > 0);
    }
    
    @Test
    @Transactional
    void testSalesPerformanceAnalytics() {
        // Given
        UUID userId = createTestUser().getId();
        DateRange dateRange = DateRange.lastWeek();
        
        createTestSalesMetrics(userId, dateRange);
        
        // When
        SalesPerformanceResponse response = analyticsService.getSalesPerformance(userId, dateRange, "daily");
        
        // Then
        assertNotNull(response);
        assertEquals("daily", response.getGranularity());
        assertTrue(response.getDataPoints().size() > 0);
        assertNotNull(response.getIndicators());
    }
    
    @Test
    @Transactional
    void testCustomerAnalytics() {
        // Given
        DateRange dateRange = DateRange.lastMonth();
        createTestCustomerAnalyticsData(dateRange);
        
        // When
        CustomerAnalyticsResponse response = analyticsService.getCustomerAnalytics(dateRange);
        
        // Then
        assertNotNull(response);
        assertNotNull(response.getSegmentation());
        assertNotNull(response.getAcquisitionFunnel());
        assertNotNull(response.getGeographicDistribution());
    }
    
    @Test
    void testAnalyticsCaching() {
        // Given
        UUID userId = createTestUser().getId();
        DateRange dateRange = DateRange.lastWeek();
        
        // When - First call
        long startTime1 = System.currentTimeMillis();
        ExecutiveDashboardResponse response1 = analyticsService.getExecutiveDashboard(userId, dateRange);
        long duration1 = System.currentTimeMillis() - startTime1;
        
        // When - Second call (should be cached)
        long startTime2 = System.currentTimeMillis();
        ExecutiveDashboardResponse response2 = analyticsService.getExecutiveDashboard(userId, dateRange);
        long duration2 = System.currentTimeMillis() - startTime2;
        
        // Then
        assertEquals(response1.getSalesMetrics().getTotalRevenue(), 
                    response2.getSalesMetrics().getTotalRevenue());
        assertTrue(duration2 < duration1 / 2); // Cached call should be much faster
    }
}

@QuarkusTest
class PredictiveAnalyticsServiceTest {
    
    @Inject
    PredictiveAnalyticsService predictiveService;
    
    @Test
    @Transactional
    void testOpportunityScoring() {
        // Given
        Opportunity opportunity = createTestOpportunity();
        
        // When
        OpportunityScore score = predictiveService.calculateOpportunityScore(opportunity.getId());
        
        // Then
        assertNotNull(score);
        assertTrue(score.getScore() >= 0.0 && score.getScore() <= 1.0);
        assertTrue(score.getConfidence() >= 0.0 && score.getConfidence() <= 1.0);
        assertFalse(score.getFactors().isEmpty());
        assertFalse(score.getRecommendations().isEmpty());
    }
    
    @Test
    @Transactional
    void testChurnRiskAssessment() {
        // Given
        Customer customer = createTestCustomerWithHistory();
        
        // When
        ChurnRiskAssessment assessment = predictiveService.assessChurnRisk(customer.getId());
        
        // Then
        assertNotNull(assessment);
        assertTrue(assessment.getChurnProbability() >= 0.0 && assessment.getChurnProbability() <= 1.0);
        assertNotNull(assessment.getRiskLevel());
        assertFalse(assessment.getRetentionStrategies().isEmpty());
    }
}
```

### E2E Tests - Analytics Dashboard Flow
```typescript
describe('Analytics Dashboard E2E', () => {
    it('should navigate through all analytics views', async () => {
        await page.goto('/analytics');
        
        // Executive Dashboard
        await expect(page.locator('text=📊 Executive Summary')).toBeVisible();
        await expect(page.locator('[data-testid=kpi-cards]')).toBeVisible();
        
        // Switch to Sales Performance
        await page.click('[data-testid=sales-performance-tab]');
        await expect(page.locator('text=📊 Sales Performance Analysis')).toBeVisible();
        await expect(page.locator('[data-testid=performance-chart]')).toBeVisible();
        
        // Switch to Customer Analytics
        await page.click('[data-testid=customer-analytics-tab]');
        await expect(page.locator('text=👥 Customer Analytics')).toBeVisible();
        await expect(page.locator('[data-testid=customer-segmentation-chart]')).toBeVisible();
        
        // Switch to Pipeline Analytics
        await page.click('[data-testid=pipeline-analytics-tab]');
        await expect(page.locator('text=🎯 Pipeline Analytics')).toBeVisible();
        await expect(page.locator('[data-testid=pipeline-flow-chart]')).toBeVisible();
    });
    
    it('should create and save custom report', async () => {
        await page.goto('/analytics?view=custom');
        
        // Configure report
        await page.fill('[data-testid=report-name]', 'Test Custom Report');
        await page.selectOption('[data-testid=data-source]', 'SALES_METRICS');
        await page.selectOption('[data-testid=chart-type]', 'BAR');
        
        // Add metrics
        await page.click('[data-testid=add-metric-button]');
        await page.selectOption('[data-testid=metric-select]', 'revenue');
        
        // Generate preview
        await page.click('[data-testid=preview-button]');
        await expect(page.locator('[data-testid=report-preview]')).toBeVisible();
        
        // Save report
        await page.click('[data-testid=save-report-button]');
        await expect(page.locator('text=Report saved successfully')).toBeVisible();
    });
    
    it('should display real-time updates', async () => {
        await page.goto('/analytics');
        
        // Check initial metric value
        const initialRevenue = await page.textContent('[data-testid=total-revenue]');
        
        // Simulate backend data change (via API call)
        await apiCall('/api/test/simulate-sale', { amount: 10000 });
        
        // Wait for WebSocket update
        await page.waitForFunction(
            (initial) => {
                const current = document.querySelector('[data-testid=total-revenue]')?.textContent;
                return current !== initial;
            },
            initialRevenue,
            { timeout: 5000 }
        );
        
        // Verify update
        const updatedRevenue = await page.textContent('[data-testid=total-revenue]');
        expect(updatedRevenue).not.toBe(initialRevenue);
    });
});
```

---

## 🚀 IMPLEMENTATION GUIDE

### Phase 1: Data Warehouse & Aggregation (3 Tage)
1. **Database Schema** - Analytics tables und aggregation structures
2. **Metrics Aggregation Service** - Daily/Weekly/Monthly data aggregation
3. **Data Pipeline** - ETL processes für real-time und batch analytics
4. **Caching Layer** - Redis/In-memory caching für performance

### Phase 2: Analytics Engine (3 Tage)
1. **AnalyticsService** - Core business intelligence calculations
2. **Predictive Analytics** - ML models für forecasting und scoring
3. **Real-time Updates** - WebSocket integration für live data
4. **Custom Report Builder** - Dynamic query generation

### Phase 3: Frontend Dashboard (3 Tage)
1. **Dashboard Components** - Interactive charts mit Recharts/D3.js
2. **Navigation & Filtering** - Date ranges, user filters, drill-down
3. **Real-time Integration** - WebSocket updates und data refresh
4. **Export Functionality** - PDF/Excel export für reports

### Phase 4: Advanced Features (1 Tag)
1. **Custom Report Builder UI** - Drag-and-drop report configuration
2. **Performance Optimization** - Query optimization und lazy loading
3. **Mobile Responsive** - Tablet und mobile dashboard views
4. **Testing & Documentation** - Comprehensive testing suite

---

## 📊 SUCCESS CRITERIA

### Funktionale Anforderungen
- ✅ Executive Dashboard mit Key Performance Indicators
- ✅ Sales Performance Analytics mit Trend Analysis
- ✅ Customer Analytics mit Segmentation und Behavior Analysis
- ✅ Pipeline Analytics mit Conversion Rates und Forecasting
- ✅ Predictive Insights mit AI-powered Recommendations
- ✅ Custom Report Builder mit Interactive Configuration

### Performance-Anforderungen
- ✅ Dashboard Load Time < 3s
- ✅ Chart Rendering < 1s
- ✅ Real-time Updates < 500ms
- ✅ Complex Queries < 2s
- ✅ Export Generation < 10s für standard reports

### Business-Anforderungen
- ✅ Data-driven Decision Making Support
- ✅ Real-time Business Intelligence
- ✅ Predictive Analytics für proactive actions
- ✅ Customizable Reports für verschiedene Stakeholder
- ✅ Mobile-friendly Analytics für Field Sales

---

## 🔗 NAVIGATION ZU ALLEN 40 FEATURES

### Core Sales Features
- [FC-001 Customer Acquisition](/docs/features/PLANNED/01_customer_acquisition/FC-001_TECH_CONCEPT.md) | [FC-002 Smart Customer Insights](/docs/features/PLANNED/02_smart_insights/FC-002_TECH_CONCEPT.md) | [M3 Sales Cockpit](/docs/features/ACTIVE/05_ui_foundation/M3_TECH_CONCEPT.md) | [M4 Opportunity Pipeline](/docs/features/ACTIVE/02_opportunity_pipeline/M4_TECH_CONCEPT.md)
- [FC-004 Verkäuferschutz](/docs/features/PLANNED/07_verkaeuferschutz/FC-004_TECH_CONCEPT.md) | [FC-013 Duplicate Detection](/docs/features/PLANNED/15_duplicate_detection/FC-013_TECH_CONCEPT.md) | [FC-014 Activity Timeline](/docs/features/PLANNED/16_activity_timeline/FC-014_TECH_CONCEPT.md)
- [FC-015 Deal Loss Analysis](/docs/features/PLANNED/17_deal_loss_analysis/FC-015_TECH_CONCEPT.md) | [FC-016 Opportunity Cloning](/docs/features/PLANNED/18_opportunity_cloning/FC-016_TECH_CONCEPT.md) | [FC-019 Advanced Sales Metrics](/docs/features/PLANNED/19_advanced_metrics/FC-019_TECH_CONCEPT.md)

### Security & Foundation Features  
- [FC-008 Security Foundation](/docs/features/ACTIVE/01_security_foundation/FC-008_TECH_CONCEPT.md) | [FC-009 Advanced Permissions](/docs/features/ACTIVE/04_permissions_system/FC-009_TECH_CONCEPT.md) | [FC-025 DSGVO Compliance](/docs/features/PLANNED/25_dsgvo_compliance/FC-025_TECH_CONCEPT.md)
- [FC-023 Event Sourcing](/docs/features/PLANNED/23_event_sourcing/FC-023_TECH_CONCEPT.md) | [FC-038 Multi-Tenant Architecture](/docs/features/PLANNED/38_multitenant/FC-038_TECH_CONCEPT.md) | [FC-039 API Gateway](/docs/features/PLANNED/39_api_gateway/FC-039_TECH_CONCEPT.md)

### Mobile & Field Service Features
- [FC-006 Mobile App](/docs/features/PLANNED/09_mobile_app/FC-006_TECH_CONCEPT.md) | [FC-018 Mobile PWA](/docs/features/PLANNED/09_mobile_app/FC-018_MOBILE_FIELD_SALES.md) | [FC-022 Mobile Light](/docs/features/PLANNED/22_mobile_light/FC-022_TECH_CONCEPT.md)
- [FC-029 Voice-First Interface](/docs/features/PLANNED/29_voice_first/FC-029_TECH_CONCEPT.md) | [FC-030 One-Tap Actions](/docs/features/PLANNED/30_one_tap_actions/FC-030_TECH_CONCEPT.md) | [FC-032 Offline-First](/docs/features/PLANNED/32_offline_first/FC-032_TECH_CONCEPT.md)

### Communication Features
- [FC-003 E-Mail Integration](/docs/features/PLANNED/06_email_integration/FC-003_TECH_CONCEPT.md) | [FC-012 Team Communication](/docs/features/PLANNED/14_team_communication/FC-012_TECH_CONCEPT.md) | [FC-028 WhatsApp Business](/docs/features/PLANNED/28_whatsapp_integration/FC-028_TECH_CONCEPT.md)
- [FC-035 Social Selling Helper](/docs/features/PLANNED/35_social_selling/FC-035_TECH_CONCEPT.md)

### Analytics & Intelligence Features
- [FC-007 Chef-Dashboard](/docs/features/PLANNED/10_chef_dashboard/FC-007_TECH_CONCEPT.md) | [FC-026 Analytics Platform](/docs/features/PLANNED/26_analytics_platform/FC-026_TECH_CONCEPT.md) | [FC-034 Instant Insights](/docs/features/PLANNED/34_instant_insights/FC-034_TECH_CONCEPT.md)
- [FC-037 Advanced Reporting](/docs/features/PLANNED/37_advanced_reporting/FC-037_TECH_CONCEPT.md) | [FC-040 Performance Monitoring](/docs/features/PLANNED/40_performance_monitoring/FC-040_TECH_CONCEPT.md)

### Integration & Infrastructure Features
- [FC-005 Xentral Integration](/docs/features/PLANNED/08_xentral_integration/FC-005_TECH_CONCEPT.md) | [FC-010 Customer Import](/docs/features/PLANNED/11_customer_import/FC-010_TECH_CONCEPT.md) | [FC-021 Integration Hub](/docs/features/PLANNED/21_integration_hub/FC-021_TECH_CONCEPT.md)
- [FC-024 File Management](/docs/features/PLANNED/24_file_management/FC-024_TECH_CONCEPT.md) | [M5 Customer Refactor](/docs/features/PLANNED/12_customer_refactor_m5/M5_TECH_CONCEPT.md)

### UI & Productivity Features
- [M1 Navigation](/docs/features/ACTIVE/05_ui_foundation/M1_TECH_CONCEPT.md) | [M2 Quick Create](/docs/features/ACTIVE/05_ui_foundation/M2_TECH_CONCEPT.md) | [M3 Sales Cockpit](/docs/features/ACTIVE/05_ui_foundation/M3_TECH_CONCEPT.md) | [M7 Settings](/docs/features/ACTIVE/05_ui_foundation/M7_SETTINGS_TECH_CONCEPT.md)
- [FC-020 Quick Wins](/docs/features/PLANNED/20_quick_wins/FC-020_TECH_CONCEPT.md) | [FC-031 Smart Templates](/docs/features/PLANNED/31_smart_templates/FC-031_TECH_CONCEPT.md) | [FC-033 Visual Customer Cards](/docs/features/PLANNED/33_visual_cards/FC-033_TECH_CONCEPT.md)

### Advanced Features
- [FC-011 Bonitätsprüfung](/docs/features/ACTIVE/02_opportunity_pipeline/integrations/FC-011_TECH_CONCEPT.md) | [FC-017 Sales Gamification](/docs/features/PLANNED/99_sales_gamification/FC-017_TECH_CONCEPT.md) | [FC-027 Magic Moments](/docs/features/PLANNED/27_magic_moments/FC-027_TECH_CONCEPT.md)
- [FC-036 Beziehungsmanagement](/docs/features/PLANNED/36_relationship_mgmt/FC-036_TECH_CONCEPT.md) | [M8 Calculator Modal](/docs/features/ACTIVE/03_calculator_modal/M8_TECH_CONCEPT.md)

---

**📊 M6 Analytics Module - Ready for Business Intelligence!**  
**⏱️ Geschätzter Aufwand:** 8-10 Tage | **New Development:** Complete BI Platform + Real-Time Analytics + Predictive Insights