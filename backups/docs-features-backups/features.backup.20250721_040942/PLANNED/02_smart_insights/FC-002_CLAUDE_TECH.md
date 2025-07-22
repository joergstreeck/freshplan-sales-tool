# FC-002 Smart Customer Insights - CLAUDE TECH 🤖

## 🧠 QUICK-LOAD (30 Sekunden bis zur Produktivität)

**Feature:** KI-gestützte Customer Intelligence mit ML-Scoring und OpenAI Insights  
**Stack:** Quarkus + OpenAI API + React Query + Material-UI  
**Status:** 📋 Geplant - AI Core Feature  
**Dependencies:** FC-026 Analytics (Daten), FC-001 Customer Acquisition | Blockiert: FC-034 Instant Insights, FC-027 Magic Moments  

**Jump to:** [📚 Recipes](#-implementation-recipes) | [🧪 Tests](#-test-patterns) | [🔌 Integration](#-integration-cookbook) | [🤖 AI Patterns](#-ai-patterns)

**Core Purpose in 1 Line:** `Customer Data → ML Analysis → OpenAI Insights → Action Recommendations → Sales Success`

---

## 🍳 IMPLEMENTATION RECIPES

### Recipe 1: Health Score in 5 Minuten
```java
// 1. Customer Health Score Service (copy-paste ready)
@ApplicationScoped
public class CustomerHealthScoreService {
    
    public CustomerHealthScore calculateHealthScore(UUID customerId) {
        CustomerData data = loadCustomerData(customerId);
        
        // Simple weighted scoring (replace with ML later)
        double churnRisk = calculateChurnRisk(data);
        double upsellPotential = calculateUpsellPotential(data);
        double engagementScore = calculateEngagementScore(data);
        
        return CustomerHealthScore.builder()
            .customerId(customerId)
            .churnRisk(churnRisk)  // 0-100 (100 = high risk)
            .upsellPotential(upsellPotential)  // 0-100
            .engagementScore(engagementScore)  // 0-100
            .overallHealth((100 - churnRisk + upsellPotential + engagementScore) / 3)
            .calculatedAt(LocalDateTime.now())
            .build();
    }
    
    private double calculateChurnRisk(CustomerData data) {
        double score = 0;
        
        // Days since last activity (max 30 points)
        score += Math.min(30, data.getDaysSinceLastActivity() * 0.5);
        
        // Order frequency decrease (max 40 points)
        if (data.getOrderFrequencyTrend() < -0.3) score += 40;
        else if (data.getOrderFrequencyTrend() < 0) score += 20;
        
        // Support tickets (max 30 points)
        score += Math.min(30, data.getOpenSupportTickets() * 10);
        
        return Math.min(100, score);
    }
}
```

### Recipe 2: OpenAI Integration für Insights
```java
// 2. OpenAI Insights Service (production-ready)
@ApplicationScoped
public class OpenAIInsightsService {
    
    @ConfigProperty(name = "openai.api.key")
    String apiKey;
    
    @ConfigProperty(name = "openai.model", defaultValue = "gpt-4")
    String model;
    
    public CompletionStage<CustomerInsights> generateInsights(CustomerHealthScore healthScore, CustomerData data) {
        String prompt = buildInsightPrompt(healthScore, data);
        
        return openAIClient.createCompletion(
            CompletionRequest.builder()
                .model(model)
                .prompt(prompt)
                .maxTokens(500)
                .temperature(0.7)
                .build()
        ).thenApply(response -> parseInsights(response, healthScore));
    }
    
    private String buildInsightPrompt(CustomerHealthScore score, CustomerData data) {
        return String.format("""
            Analysiere diesen B2B-Kunden und gib 3-5 konkrete Handlungsempfehlungen für den Vertrieb:
            
            Kunde: %s
            Branche: %s
            Kunde seit: %s
            
            Health Metriken:
            - Gesundheitsscore: %.1f/100
            - Abwanderungsrisiko: %.1f%%
            - Upselling-Potenzial: %.1f/100
            - Engagement: %.1f/100
            
            Letzte Aktivitäten:
            - Letzter Kontakt: vor %d Tagen
            - Letzte Bestellung: %s
            - Offene Support-Tickets: %d
            
            Fokussiere auf:
            1. Konkrete nächste Schritte für den Vertrieb
            2. Warnsignale die sofortiges Handeln erfordern
            3. Cross-Selling/Upselling Opportunitäten
            
            Antworte im JSON Format:
            {
                "insights": [
                    {"type": "warning|opportunity|action", "priority": "high|medium|low", "message": "...", "recommendation": "..."}
                ]
            }
            """,
            data.getCompanyName(),
            data.getIndustry(),
            data.getCustomerSince(),
            score.getOverallHealth(),
            score.getChurnRisk(),
            score.getUpsellPotential(),
            score.getEngagementScore(),
            data.getDaysSinceLastActivity(),
            data.getLastOrderDate(),
            data.getOpenSupportTickets()
        );
    }
}
```

### Recipe 3: Frontend Dashboard Integration
```typescript
// 3. Customer Insights Dashboard (copy-paste ready)
export const CustomerInsightsDashboard: React.FC<{ customerId: string }> = ({ customerId }) => {
    // Fetch insights with caching
    const { data: insights, isLoading, error } = useQuery({
        queryKey: ['customer-insights', customerId],
        queryFn: () => customerInsightsApi.getInsights(customerId),
        staleTime: 5 * 60 * 1000, // 5 min cache
        cacheTime: 10 * 60 * 1000
    });
    
    if (isLoading) return <InsightsSkeleton />;
    if (error) return <InsightsError onRetry={() => queryClient.invalidateQueries(['customer-insights', customerId])} />;
    
    return (
        <Grid container spacing={3}>
            {/* Health Score Overview */}
            <Grid item xs={12} md={4}>
                <HealthScoreCard score={insights.healthScore} trend={insights.healthTrend} />
            </Grid>
            
            {/* AI Insights */}
            <Grid item xs={12} md={8}>
                <Card>
                    <CardHeader 
                        title="KI-Insights" 
                        avatar={<SmartToyIcon />}
                        action={
                            <IconButton onClick={() => queryClient.invalidateQueries(['customer-insights', customerId])}>
                                <RefreshIcon />
                            </IconButton>
                        }
                    />
                    <CardContent>
                        <Stack spacing={2}>
                            {insights.aiInsights.map((insight, index) => (
                                <InsightCard key={index} insight={insight} onAction={handleInsightAction} />
                            ))}
                        </Stack>
                    </CardContent>
                </Card>
            </Grid>
            
            {/* Action Recommendations */}
            <Grid item xs={12}>
                <ActionRecommendations 
                    recommendations={insights.recommendations}
                    onExecute={handleRecommendationExecute}
                />
            </Grid>
        </Grid>
    );
};

// Health Score Visual Component
export const HealthScoreCard: React.FC<{ score: number; trend: number }> = ({ score, trend }) => {
    const getScoreColor = (score: number) => {
        if (score >= 80) return 'success';
        if (score >= 60) return 'warning';
        return 'error';
    };
    
    return (
        <Card>
            <CardContent>
                <Typography variant="h6" gutterBottom>
                    Customer Health Score
                </Typography>
                <Box sx={{ position: 'relative', display: 'inline-flex' }}>
                    <CircularProgress
                        variant="determinate"
                        value={score}
                        size={120}
                        color={getScoreColor(score)}
                    />
                    <Box sx={{ position: 'absolute', inset: 0, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                        <Typography variant="h4" component="div" color="text.secondary">
                            {score}
                        </Typography>
                    </Box>
                </Box>
                <Box sx={{ display: 'flex', alignItems: 'center', mt: 2 }}>
                    {trend > 0 ? <TrendingUpIcon color="success" /> : <TrendingDownIcon color="error" />}
                    <Typography variant="body2" sx={{ ml: 1 }}>
                        {Math.abs(trend)}% vs. letzten Monat
                    </Typography>
                </Box>
            </CardContent>
        </Card>
    );
};
```

---

## 🧪 TEST PATTERNS

### Pattern 1: ML Scoring Test
```java
// Health Score Calculation Test
@QuarkusTest
class CustomerHealthScoreServiceTest {
    @Inject CustomerHealthScoreService service;
    
    @Test
    void calculateHealthScore_withHighRiskIndicators_shouldReturnLowScore() {
        // Given
        CustomerData testData = CustomerData.builder()
            .customerId(UUID.randomUUID())
            .daysSinceLastActivity(60)  // Very inactive
            .orderFrequencyTrend(-0.5)   // Declining orders
            .openSupportTickets(3)       // Multiple issues
            .build();
        
        // When
        CustomerHealthScore score = service.calculateHealthScore(testData.getCustomerId());
        
        // Then
        assertThat(score.getChurnRisk()).isGreaterThan(70);
        assertThat(score.getOverallHealth()).isLessThan(40);
    }
}
```

### Pattern 2: OpenAI Mock Test
```java
@QuarkusTest
class OpenAIInsightsServiceTest {
    @InjectMock OpenAIClient openAIClient;
    @Inject OpenAIInsightsService service;
    
    @Test
    void generateInsights_shouldParseOpenAIResponse() {
        // Mock OpenAI response
        when(openAIClient.createCompletion(any())).thenReturn(
            CompletableFuture.completedFuture(mockOpenAIResponse())
        );
        
        // Test insight generation
        CustomerInsights insights = service.generateInsights(testHealthScore(), testData())
            .toCompletableFuture().join();
        
        assertThat(insights.getInsights()).hasSize(3);
        assertThat(insights.getInsights().get(0).getType()).isEqualTo("warning");
        assertThat(insights.getInsights().get(0).getPriority()).isEqualTo("high");
    }
}
```

---

## 🔌 INTEGRATION COOKBOOK

### Mit Analytics Platform (FC-026)
```java
// Pull customer metrics from Analytics
@ApplicationScoped
public class CustomerDataAggregator {
    @Inject AnalyticsPlatformClient analyticsClient;
    
    public CustomerData aggregateCustomerData(UUID customerId) {
        // Parallel data fetching
        CompletableFuture<OrderMetrics> ordersFuture = 
            analyticsClient.getOrderMetrics(customerId);
        CompletableFuture<ActivityMetrics> activityFuture = 
            analyticsClient.getActivityMetrics(customerId);
        CompletableFuture<SupportMetrics> supportFuture = 
            analyticsClient.getSupportMetrics(customerId);
        
        return CompletableFuture.allOf(ordersFuture, activityFuture, supportFuture)
            .thenApply(v -> CustomerData.builder()
                .customerId(customerId)
                .orderMetrics(ordersFuture.join())
                .activityMetrics(activityFuture.join())
                .supportMetrics(supportFuture.join())
                .build()
            ).join();
    }
}
```

### Mit Sales Cockpit (M3)
```typescript
// Embed Insights in Sales Cockpit
export const SalesCockpitWithInsights: React.FC = () => {
    const { selectedCustomerId } = useCockpitContext();
    
    return (
        <Grid container spacing={3}>
            <Grid item xs={12} md={8}>
                <MyDayColumn />
            </Grid>
            <Grid item xs={12} md={4}>
                {selectedCustomerId && (
                    <CustomerInsightsSidebar customerId={selectedCustomerId} />
                )}
            </Grid>
        </Grid>
    );
};
```

---

## 🤖 AI PATTERNS

### Prompt Engineering Best Practices
```typescript
// Structured prompts for consistent results
const INSIGHT_PROMPT_TEMPLATE = {
    system: "Du bist ein erfahrener B2B Sales Analyst für die Lebensmittelbranche.",
    
    userTemplate: (data: CustomerData) => `
        Analysiere ${data.companyName} und identifiziere:
        1. Abwanderungsrisiken
        2. Upselling-Chancen
        3. Sofort-Maßnahmen
        
        Kontext: ${JSON.stringify(data.metrics)}
    `,
    
    outputFormat: {
        insights: [
            {
                type: "warning|opportunity|action",
                priority: "high|medium|low",
                message: "string",
                recommendation: "string",
                expectedImpact: "string"
            }
        ]
    }
};
```

### ML Model Integration Pattern
```java
// Pluggable ML Models
public interface HealthScoreModel {
    double calculateScore(CustomerData data);
}

@ApplicationScoped
public class MLHealthScoreModel implements HealthScoreModel {
    @Inject MLModelService mlService;
    
    public double calculateScore(CustomerData data) {
        // Convert to feature vector
        double[] features = extractFeatures(data);
        
        // Run through trained model
        return mlService.predict("customer-health-v2", features);
    }
}
```

---

## 📚 DEEP KNOWLEDGE (Bei Bedarf expandieren)

<details>
<summary>🧮 ML Pipeline Architecture</summary>

### Feature Engineering Pipeline
```java
public class FeatureExtractor {
    public double[] extractFeatures(CustomerData data) {
        return new double[] {
            // Recency features
            data.getDaysSinceLastOrder(),
            data.getDaysSinceLastActivity(),
            
            // Frequency features
            data.getOrderFrequency(),
            data.getAverageOrdersPerMonth(),
            
            // Monetary features
            data.getTotalRevenue(),
            data.getAverageOrderValue(),
            
            // Engagement features
            data.getEmailOpenRate(),
            data.getSupportTicketCount(),
            
            // Trend features
            data.getRevenueGrowthRate(),
            data.getOrderFrequencyTrend()
        };
    }
}
```

### Model Training Process
```python
# Training pipeline (Python/Scikit-learn)
from sklearn.ensemble import RandomForestRegressor
from sklearn.model_selection import train_test_split

# Load historical customer data
X_train, X_test, y_train, y_test = train_test_split(features, health_scores)

# Train model
model = RandomForestRegressor(n_estimators=100, max_depth=10)
model.fit(X_train, y_train)

# Export for Java integration
joblib.dump(model, 'customer_health_model_v2.pkl')
```

</details>

<details>
<summary>🔧 Performance Optimization</summary>

### Caching Strategy
```java
@ApplicationScoped
public class InsightsCacheService {
    @Inject @Channel("insights-cache") Emitter<InsightsCacheEvent> cacheEmitter;
    
    private final Cache<UUID, CustomerInsights> insightsCache = 
        Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();
    
    public CompletionStage<CustomerInsights> getInsights(UUID customerId) {
        return CompletableFuture.supplyAsync(() -> 
            insightsCache.get(customerId, id -> calculateInsights(id))
        );
    }
}
```

### Batch Processing
```java
// Process multiple customers efficiently
@Scheduled(cron = "0 0 2 * * ?")  // 2 AM daily
void batchCalculateHealthScores() {
    customerRepository.streamAll()
        .parallel()
        .forEach(customer -> {
            try {
                CustomerHealthScore score = calculateHealthScore(customer.getId());
                healthScoreRepository.persist(score);
            } catch (Exception e) {
                log.error("Failed to calculate score for {}", customer.getId(), e);
            }
        });
}
```

</details>

---

**🎯 Nächster Schritt:** Health Score Service implementieren mit Basic ML Scoring