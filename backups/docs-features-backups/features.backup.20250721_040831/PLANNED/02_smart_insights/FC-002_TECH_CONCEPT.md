# FC-002 Smart Customer Insights - Tech Concept

**Feature-Code:** FC-002  
**Feature-Name:** Smart Customer Insights  
**Feature-Typ:** 🤖 AI-ENHANCED FULLSTACK  
**Erstellt:** 2025-07-20  
**Status:** Tech Concept  
**Priorität:** HIGH - AI Core  
**Geschätzter Aufwand:** 10-12 Tage  

---

## 🧠 CLAUDE WORKING SECTION (15-Min Context Chunk)

### 📍 Kontext laden für produktive Arbeit

**Was ist FC-002?** KI-gestützte Customer Intelligence Engine mit Predictive Analytics und Business Insights

**Warum kritisch?** Kernstück der Sales-Intelligence - verwandelt Rohdaten in actionable insights für bessere Verkaufsentscheidungen

**Abhängigkeiten verstehen:**
- **Benötigt:** FC-026 Analytics Platform (Datengrundlage), FC-001 Customer Acquisition (Lead-Daten)
- **Blockiert:** FC-034 Instant Insights (UI-Layer), FC-027 Magic Moments (Timing-Insights)
- **Integriert mit:** FC-036 Beziehungsmanagement (Relationship-Scoring), M3 Sales Cockpit (UI-Integration)

**Technischer Kern - KI-Pipeline:**
```java
@ApplicationScoped
public class CustomerIntelligenceService {
    public CompletionStage<CustomerInsights> generateInsights(UUID customerId) {
        return enrichmentPipeline
            .compose(dataAggregation(customerId))
            .compose(aiAnalysis())
            .compose(insightGeneration())
            .compose(actionRecommendation());
    }
}
```

**Frontend-Integration:**
```typescript
export const useCustomerInsights = (customerId: string) => {
    return useQuery({
        queryKey: ['customer-insights', customerId],
        queryFn: () => insightsService.getInsights(customerId),
        staleTime: 300000 // 5 min cache for AI results
    });
};
```

---

## 🎯 ÜBERSICHT

### Business Value
**Problem:** Verkäufer haben Zugang zu Kundendaten, aber keine intelligenten Insights für bessere Verkaufsentscheidungen

**Lösung:** KI-gestützte Customer Intelligence Engine die automatisch Patterns erkennt und actionable insights generiert

**ROI:** 
- **Kosten:** 10-12 Entwicklertage (~€12.000)
- **Ersparnis:** 2h/Tag manuelle Datenanalyse (~520h/Jahr = €26.000) + 15% mehr Abschlüsse (~€75.000/Jahr)
- **ROI-Ratio:** 8.4:1 (Break-even nach 1.5 Monaten)

### Kernfunktionen
1. **Customer Health Scoring** - KI-basierte Bewertung der Kundengesundheit
2. **Predictive Analytics** - Vorhersage von Churn-Risk und Upselling-Potentiale
3. **Behavioral Pattern Recognition** - Erkennung von Kaufmustern und Präferenzen
4. **Automated Insights Generation** - OpenAI-gestützte natürlichsprachige Insights
5. **Action Recommendations** - Konkrete Handlungsempfehlungen für Sales-Team

---

## 🏗️ ARCHITEKTUR

### KI-Pipeline Design
```
Raw Data → Data Enrichment → ML Analysis → Insight Generation → Action Recommendations
    ↓             ↓             ↓              ↓                     ↓
Customer      Aggregation   Scoring        OpenAI GPT-4        Sales Actions
Orders        Patterns      Prediction     Natural Language    Next Best Actions
Interactions  Trends        Clustering     Summaries           Timeline Suggestions
External      Metrics       Anomalies      Explanations        Opportunity Flags
```

### Database Schema - Customer Intelligence
```sql
-- Customer Intelligence & Scoring
CREATE TABLE customer_intelligence (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL REFERENCES customers(id),
    health_score INTEGER NOT NULL CHECK (health_score BETWEEN 0 AND 100),
    churn_risk_score INTEGER NOT NULL CHECK (churn_risk_score BETWEEN 0 AND 100),
    upselling_potential INTEGER NOT NULL CHECK (upselling_potential BETWEEN 0 AND 100),
    engagement_score INTEGER NOT NULL CHECK (engagement_score BETWEEN 0 AND 100),
    last_interaction_days INTEGER,
    total_revenue DECIMAL(12,2),
    average_order_value DECIMAL(10,2),
    order_frequency_days INTEGER,
    support_ticket_count INTEGER DEFAULT 0,
    nps_score INTEGER CHECK (nps_score BETWEEN -100 AND 100),
    insights_generated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    insights_version VARCHAR(20) DEFAULT '1.0',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- AI-Generated Insights
CREATE TABLE customer_insights (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL REFERENCES customers(id),
    insight_type VARCHAR(50) NOT NULL, -- 'health', 'churn_risk', 'opportunity', 'behavioral'
    insight_category VARCHAR(50) NOT NULL, -- 'warning', 'opportunity', 'recommendation', 'trend'
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    confidence_score DECIMAL(3,2) NOT NULL CHECK (confidence_score BETWEEN 0 AND 1),
    priority_level VARCHAR(20) NOT NULL DEFAULT 'medium', -- 'low', 'medium', 'high', 'critical'
    data_sources JSONB, -- Which data contributed to this insight
    recommended_actions JSONB, -- Structured action recommendations
    expires_at TIMESTAMP, -- When insight becomes stale
    acknowledged BOOLEAN DEFAULT false,
    acknowledged_by UUID REFERENCES users(id),
    acknowledged_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Behavioral Patterns
CREATE TABLE customer_behavioral_patterns (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL REFERENCES customers(id),
    pattern_type VARCHAR(50) NOT NULL, -- 'purchase_cycle', 'seasonal', 'communication_preference'
    pattern_name VARCHAR(100) NOT NULL,
    pattern_data JSONB NOT NULL, -- Flexible pattern storage
    confidence_level DECIMAL(3,2) NOT NULL,
    first_observed TIMESTAMP NOT NULL,
    last_observed TIMESTAMP NOT NULL,
    frequency_days INTEGER, -- Pattern repetition frequency
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Predictive Models Results
CREATE TABLE predictive_model_results (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL REFERENCES customers(id),
    model_name VARCHAR(100) NOT NULL, -- 'churn_predictor', 'upsell_recommender', 'health_scorer'
    model_version VARCHAR(20) NOT NULL,
    prediction_type VARCHAR(50) NOT NULL,
    prediction_value DECIMAL(10,4),
    prediction_category VARCHAR(50),
    input_features JSONB, -- Features used for prediction
    explanation JSONB, -- Model explainability data
    prediction_date TIMESTAMP NOT NULL,
    valid_until TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Customer Timeline Events (for AI analysis)
CREATE TABLE customer_timeline_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL REFERENCES customers(id),
    event_type VARCHAR(50) NOT NULL, -- 'order', 'interaction', 'support', 'payment', 'milestone'
    event_category VARCHAR(50) NOT NULL,
    event_title VARCHAR(200) NOT NULL,
    event_description TEXT,
    event_data JSONB, -- Flexible event data
    event_date TIMESTAMP NOT NULL,
    impact_score INTEGER CHECK (impact_score BETWEEN -10 AND 10), -- Positive/negative impact
    created_by UUID REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance
CREATE INDEX idx_customer_intelligence_customer ON customer_intelligence(customer_id);
CREATE INDEX idx_customer_intelligence_health ON customer_intelligence(health_score DESC);
CREATE INDEX idx_customer_intelligence_churn ON customer_intelligence(churn_risk_score DESC);
CREATE INDEX idx_customer_insights_customer ON customer_insights(customer_id);
CREATE INDEX idx_customer_insights_type ON customer_insights(insight_type, priority_level);
CREATE INDEX idx_behavioral_patterns_customer ON customer_behavioral_patterns(customer_id);
CREATE INDEX idx_timeline_events_customer_date ON customer_timeline_events(customer_id, event_date DESC);
```

### Backend-Architecture - AI Intelligence Service
```java
// AI Intelligence Service
@ApplicationScoped
@Transactional
public class CustomerIntelligenceService {
    
    @Inject CustomerRepository customerRepository;
    @Inject OrderRepository orderRepository;
    @Inject InteractionRepository interactionRepository;
    @Inject OpenAIService openAIService;
    @Inject PredictiveModelService predictiveModelService;
    @Inject InsightsRepository insightsRepository;
    
    public CompletionStage<CustomerInsights> generateInsights(UUID customerId) {
        return CompletableFuture
            .supplyAsync(() -> aggregateCustomerData(customerId))
            .thenCompose(this::calculateScores)
            .thenCompose(this::runPredictiveModels)
            .thenCompose(this::generateAIInsights)
            .thenCompose(this::createActionRecommendations)
            .thenApply(this::buildInsightsResponse);
    }
    
    private CustomerDataAggregation aggregateCustomerData(UUID customerId) {
        // Multi-source data aggregation
        Customer customer = customerRepository.findByIdOrThrow(customerId);
        List<Order> orders = orderRepository.findByCustomerId(customerId);
        List<Interaction> interactions = interactionRepository.findByCustomerId(customerId);
        List<SupportTicket> tickets = supportRepository.findByCustomerId(customerId);
        
        return CustomerDataAggregation.builder()
            .customer(customer)
            .orders(orders)
            .interactions(interactions)
            .supportTickets(tickets)
            .timeRange(LocalDate.now().minusYears(2), LocalDate.now())
            .build();
    }
    
    private CompletionStage<ScoredCustomerData> calculateScores(CustomerDataAggregation data) {
        return CompletableFuture.supplyAsync(() -> {
            // Health Score Calculation
            int healthScore = calculateHealthScore(data);
            
            // Churn Risk Score
            int churnRisk = calculateChurnRisk(data);
            
            // Upselling Potential
            int upsellingPotential = calculateUpsellingPotential(data);
            
            // Engagement Score
            int engagementScore = calculateEngagementScore(data);
            
            return ScoredCustomerData.builder()
                .aggregation(data)
                .healthScore(healthScore)
                .churnRisk(churnRisk)
                .upsellingPotential(upsellingPotential)
                .engagementScore(engagementScore)
                .build();
        });
    }
    
    private int calculateHealthScore(CustomerDataAggregation data) {
        int score = 50; // Base score
        
        // Revenue contribution (+/- 30 points)
        if (data.getTotalRevenue().compareTo(BigDecimal.valueOf(10000)) > 0) {
            score += 30;
        } else if (data.getTotalRevenue().compareTo(BigDecimal.valueOf(1000)) < 0) {
            score -= 20;
        }
        
        // Order frequency (+/- 20 points)
        long daysSinceLastOrder = data.getDaysSinceLastOrder();
        if (daysSinceLastOrder < 30) {
            score += 20;
        } else if (daysSinceLastOrder > 180) {
            score -= 30;
        }
        
        // Support tickets (-10 points per ticket)
        score -= Math.min(40, data.getSupportTickets().size() * 10);
        
        // Payment behavior (+/- 15 points)
        if (data.hasLatePayments()) {
            score -= 15;
        } else if (data.hasEarlyPayments()) {
            score += 10;
        }
        
        return Math.max(0, Math.min(100, score));
    }
    
    private CompletionStage<ModelPredictions> runPredictiveModels(ScoredCustomerData data) {
        return CompletableFuture.supplyAsync(() -> {
            // Feature engineering for ML models
            Map<String, Double> features = extractFeatures(data);
            
            // Run multiple prediction models
            Double churnProbability = predictiveModelService.predictChurn(features);
            Double upsellingProbability = predictiveModelService.predictUpselling(features);
            List<String> recommendedProducts = predictiveModelService.recommendProducts(features);
            
            return ModelPredictions.builder()
                .churnProbability(churnProbability)
                .upsellingProbability(upsellingProbability)
                .recommendedProducts(recommendedProducts)
                .features(features)
                .build();
        });
    }
    
    private CompletionStage<AIGeneratedInsights> generateAIInsights(ModelPredictions predictions) {
        return openAIService.generateCustomerInsights(predictions)
            .thenApply(aiResponse -> {
                return AIGeneratedInsights.builder()
                    .summary(aiResponse.getSummary())
                    .keyInsights(aiResponse.getInsights())
                    .recommendations(aiResponse.getRecommendations())
                    .confidenceScore(aiResponse.getConfidence())
                    .build();
            });
    }
}

// OpenAI Integration Service
@ApplicationScoped
public class OpenAIService {
    
    @ConfigProperty(name = "openai.api.key")
    String apiKey;
    
    @ConfigProperty(name = "openai.model", defaultValue = "gpt-4")
    String model;
    
    public CompletionStage<AIInsightResponse> generateCustomerInsights(ModelPredictions predictions) {
        String prompt = buildInsightPrompt(predictions);
        
        return webClient
            .post()
            .uri("https://api.openai.com/v1/chat/completions")
            .header("Authorization", "Bearer " + apiKey)
            .header("Content-Type", "application/json")
            .bodyValue(buildOpenAIRequest(prompt))
            .retrieve()
            .bodyToMono(OpenAIResponse.class)
            .map(this::parseInsightResponse)
            .toFuture();
    }
    
    private String buildInsightPrompt(ModelPredictions predictions) {
        return """
            Als Sales Intelligence Experte, analysiere diese Kundendaten und generiere actionable insights:
            
            Kunde-Metriken:
            - Health Score: %d/100
            - Churn Risk: %.2f%%
            - Upselling Potential: %.2f%%
            - Letzte Bestellung: %s
            - Gesamtumsatz: €%.2f
            
            Aufgabe:
            1. Fasse den Kundenstatus zusammen (max. 2 Sätze)
            2. Identifiziere die 3 wichtigsten Insights
            3. Gib konkrete Handlungsempfehlungen für den Verkäufer
            
            Format: JSON mit Feldern 'summary', 'insights', 'recommendations', 'confidence'
            """.formatted(
                predictions.getHealthScore(),
                predictions.getChurnProbability() * 100,
                predictions.getUpsellingProbability() * 100,
                predictions.getLastOrderDate(),
                predictions.getTotalRevenue()
            );
    }
}

// Predictive Model Service (Simplified)
@ApplicationScoped
public class PredictiveModelService {
    
    public Double predictChurn(Map<String, Double> features) {
        // Simplified heuristic model (in production: ML model)
        double score = 0.0;
        
        // Days since last order (major factor)
        Double daysSinceOrder = features.get("days_since_last_order");
        if (daysSinceOrder != null) {
            if (daysSinceOrder > 180) score += 0.4;
            else if (daysSinceOrder > 90) score += 0.2;
        }
        
        // Support tickets (negative signal)
        Double supportTickets = features.get("support_tickets");
        if (supportTickets != null && supportTickets > 2) {
            score += 0.3;
        }
        
        // Revenue trend (declining = risk)
        Double revenueDecline = features.get("revenue_decline_percentage");
        if (revenueDecline != null && revenueDecline > 0.3) {
            score += 0.25;
        }
        
        return Math.min(1.0, score);
    }
    
    public Double predictUpselling(Map<String, Double> features) {
        // Simplified upselling probability
        double score = 0.0;
        
        // High engagement
        Double engagementScore = features.get("engagement_score");
        if (engagementScore != null && engagementScore > 70) {
            score += 0.4;
        }
        
        // Growing order values
        Double avgOrderGrowth = features.get("avg_order_growth");
        if (avgOrderGrowth != null && avgOrderGrowth > 0.1) {
            score += 0.3;
        }
        
        // Recent activity
        Double daysSinceOrder = features.get("days_since_last_order");
        if (daysSinceOrder != null && daysSinceOrder < 30) {
            score += 0.2;
        }
        
        return Math.min(1.0, score);
    }
}
```

### Frontend-Architecture - Intelligence Dashboard
```typescript
// Customer Intelligence Dashboard
interface CustomerInsights {
    customerId: string;
    healthScore: number;
    churnRisk: number;
    upsellingPotential: number;
    engagementScore: number;
    insights: InsightItem[];
    recommendations: ActionRecommendation[];
    lastUpdated: string;
}

export const CustomerIntelligenceDashboard: React.FC<{ customerId: string }> = ({ 
    customerId 
}) => {
    const { data: insights, isLoading, error } = useCustomerInsights(customerId);
    const { triggerInsightGeneration } = useInsightGeneration();
    
    if (isLoading) return <InsightsSkeleton />;
    if (error) return <InsightsError error={error} />;
    
    return (
        <Grid container spacing={3}>
            {/* Health Score Overview */}
            <Grid item xs={12}>
                <CustomerHealthOverview insights={insights} />
            </Grid>
            
            {/* Score Cards */}
            <Grid item xs={12} md={3}>
                <ScoreCard
                    title="Health Score"
                    score={insights.healthScore}
                    trend={insights.healthTrend}
                    color="primary"
                />
            </Grid>
            <Grid item xs={12} md={3}>
                <ScoreCard
                    title="Churn Risk"
                    score={insights.churnRisk}
                    trend={insights.churnTrend}
                    color="error"
                    inverted // Higher is worse
                />
            </Grid>
            <Grid item xs={12} md={3}>
                <ScoreCard
                    title="Upselling Potential"
                    score={insights.upsellingPotential}
                    trend={insights.upsellingTrend}
                    color="success"
                />
            </Grid>
            <Grid item xs={12} md={3}>
                <ScoreCard
                    title="Engagement"
                    score={insights.engagementScore}
                    trend={insights.engagementTrend}
                    color="info"
                />
            </Grid>
            
            {/* AI-Generated Insights */}
            <Grid item xs={12} md={8}>
                <Paper sx={{ p: 2 }}>
                    <Typography variant="h6" gutterBottom>
                        🤖 AI Insights
                    </Typography>
                    <InsightsList insights={insights.insights} />
                </Paper>
            </Grid>
            
            {/* Action Recommendations */}
            <Grid item xs={12} md={4}>
                <Paper sx={{ p: 2 }}>
                    <Typography variant="h6" gutterBottom>
                        💡 Empfohlene Aktionen
                    </Typography>
                    <ActionRecommendationsList 
                        recommendations={insights.recommendations}
                        onActionTaken={handleActionTaken}
                    />
                </Paper>
            </Grid>
            
            {/* Predictive Analytics */}
            <Grid item xs={12}>
                <PredictiveAnalyticsCharts insights={insights} />
            </Grid>
        </Grid>
    );
};

// AI Insights List Component
const InsightsList: React.FC<{ insights: InsightItem[] }> = ({ insights }) => {
    return (
        <List>
            {insights.map((insight) => (
                <ListItem key={insight.id} divider>
                    <ListItemIcon>
                        <InsightIcon type={insight.type} priority={insight.priority} />
                    </ListItemIcon>
                    <ListItemText
                        primary={
                            <Box display="flex" alignItems="center" gap={1}>
                                <Typography variant="body1">
                                    {insight.title}
                                </Typography>
                                <Chip 
                                    size="small" 
                                    label={`${(insight.confidence * 100).toFixed(0)}%`}
                                    color={insight.confidence > 0.8 ? 'success' : 'default'}
                                />
                            </Box>
                        }
                        secondary={
                            <Box>
                                <Typography variant="body2" color="text.secondary">
                                    {insight.description}
                                </Typography>
                                {insight.dataSource && (
                                    <Typography variant="caption" color="text.disabled">
                                        Basis: {insight.dataSource.join(', ')}
                                    </Typography>
                                )}
                            </Box>
                        }
                    />
                    {insight.priority === 'critical' && (
                        <ListItemSecondaryAction>
                            <IconButton edge="end" color="error">
                                <PriorityHighIcon />
                            </IconButton>
                        </ListItemSecondaryAction>
                    )}
                </ListItem>
            ))}
        </List>
    );
};

// Score Card Component with Trend
const ScoreCard: React.FC<{
    title: string;
    score: number;
    trend?: number;
    color: 'primary' | 'error' | 'success' | 'info';
    inverted?: boolean;
}> = ({ title, score, trend, color, inverted = false }) => {
    const getScoreColor = (score: number, inverted: boolean) => {
        const adjustedScore = inverted ? 100 - score : score;
        if (adjustedScore >= 80) return 'success';
        if (adjustedScore >= 60) return 'warning';
        return 'error';
    };
    
    const scoreColor = getScoreColor(score, inverted);
    
    return (
        <Card>
            <CardContent>
                <Box display="flex" justifyContent="space-between" alignItems="center">
                    <Typography variant="body2" color="text.secondary">
                        {title}
                    </Typography>
                    {trend !== undefined && (
                        <TrendIndicator trend={trend} />
                    )}
                </Box>
                
                <Typography variant="h3" color={`${scoreColor}.main`} sx={{ mt: 1 }}>
                    {score}
                </Typography>
                
                <LinearProgress 
                    variant="determinate" 
                    value={score} 
                    color={scoreColor}
                    sx={{ mt: 1, height: 6, borderRadius: 3 }}
                />
            </CardContent>
        </Card>
    );
};

// Custom Hooks
export const useCustomerInsights = (customerId: string) => {
    return useQuery({
        queryKey: ['customer-insights', customerId],
        queryFn: () => insightsService.getInsights(customerId),
        staleTime: 300000, // 5 minutes
        refetchOnWindowFocus: false
    });
};

export const useInsightGeneration = () => {
    const queryClient = useQueryClient();
    
    return useMutation({
        mutationFn: (customerId: string) => 
            insightsService.generateInsights(customerId),
        onSuccess: (_, customerId) => {
            queryClient.invalidateQueries(['customer-insights', customerId]);
        }
    });
};

// Insights Service
class CustomerInsightsService {
    async getInsights(customerId: string): Promise<CustomerInsights> {
        const response = await apiClient.get(`/api/customers/${customerId}/insights`);
        return response.data;
    }
    
    async generateInsights(customerId: string): Promise<void> {
        await apiClient.post(`/api/customers/${customerId}/insights/generate`);
    }
    
    async acknowledgeInsight(insightId: string): Promise<void> {
        await apiClient.patch(`/api/insights/${insightId}/acknowledge`);
    }
}
```

---

## 🔄 ABHÄNGIGKEITEN

### Benötigt diese Features:
- **FC-026 Analytics Platform** - Datengrundlage für Insights-Generierung
- **FC-001 Customer Acquisition** - Lead-Daten für umfassende Customer Journey
- **OpenAI API Key** - KI-gestützte Insight-Generierung (AI-001 TODO)

### Ermöglicht diese Features:
- **FC-034 Instant Insights** - UI-Integration der generierten Insights
- **FC-027 Magic Moments** - Timing-basierte Verkaufschancen-Erkennung
- **M3 Sales Cockpit Enhancement** - Integration in Hauptdashboard

### Integriert mit:
- **FC-036 Beziehungsmanagement** - Relationship-Scoring und -analyse
- **FC-015 Deal Loss Analysis** - Verbesserung durch Customer Insights
- **FC-019 Advanced Sales Metrics** - KI-Metriken für Sales Performance

---

## 🧪 TESTING-STRATEGIE

### Unit Tests - AI Service
```java
@QuarkusTest
class CustomerIntelligenceServiceTest {
    
    @Test
    void testHealthScoreCalculation_withHighValueCustomer_shouldReturnHighScore() {
        // Given
        CustomerDataAggregation data = CustomerDataAggregation.builder()
            .totalRevenue(BigDecimal.valueOf(50000))
            .daysSinceLastOrder(15)
            .supportTickets(List.of())
            .hasLatePayments(false)
            .build();
        
        // When
        int healthScore = intelligenceService.calculateHealthScore(data);
        
        // Then
        assertThat(healthScore).isBetween(80, 100);
    }
    
    @Test
    void testChurnPrediction_withInactiveCustomer_shouldReturnHighRisk() {
        // Test churn risk calculation...
    }
    
    @Test
    void testInsightGeneration_withValidData_shouldGenerateInsights() {
        // Test AI insight generation...
    }
}
```

### Integration Tests - OpenAI
```java
@QuarkusTest
@TestProfile(IntegrationTestProfile.class)
class OpenAIIntegrationTest {
    
    @Test
    void testOpenAIInsightGeneration_withMockData_shouldReturnValidInsights() {
        // Test OpenAI API integration
    }
}
```

### Frontend Tests
```typescript
describe('CustomerIntelligenceDashboard', () => {
    it('should display customer insights correctly', async () => {
        const mockInsights = {
            healthScore: 85,
            churnRisk: 15,
            insights: [
                { title: 'High engagement', confidence: 0.9 }
            ]
        };
        
        jest.mocked(insightsService.getInsights).mockResolvedValue(mockInsights);
        
        render(<CustomerIntelligenceDashboard customerId="test-id" />);
        
        await waitFor(() => {
            expect(screen.getByText('85')).toBeInTheDocument(); // Health score
            expect(screen.getByText('High engagement')).toBeInTheDocument();
        });
    });
});
```

---

## 🚀 IMPLEMENTATION GUIDE

### Phase 1: Data Foundation & Scoring (4 Tage)
1. **Database Schema** - Customer Intelligence Tabellen
2. **Data Aggregation Service** - Multi-Source Datensammlung
3. **Basic Scoring Algorithms** - Health, Churn, Upselling Scores
4. **Batch Processing** - Nächtliche Score-Updates

### Phase 2: AI Integration (3 Tage)
1. **OpenAI Service Integration** - GPT-4 API Anbindung
2. **Predictive Models** - Heuristische ML-Modelle
3. **Insight Generation Pipeline** - Automatisierte Insight-Erstellung
4. **Confidence Scoring** - Vertrauenswerte für Predictions

### Phase 3: Frontend Dashboard (3 Tage)
1. **Intelligence Dashboard** - Übersichtliche Insight-Darstellung
2. **Score Visualizations** - Charts und Trend-Anzeigen
3. **Action Recommendations UI** - Handlungsempfehlungen
4. **Real-time Updates** - WebSocket oder Polling

### Phase 4: Advanced Features (2 Tage)
1. **Pattern Recognition** - Behavioral Pattern Detection
2. **Timeline Integration** - Customer Journey Insights
3. **Notification System** - Critical Insight Alerts
4. **A/B Testing Framework** - Insight Effectiveness Measurement

---

## 📊 SUCCESS CRITERIA

### Funktionale Anforderungen
- ✅ Automatische Generierung von Customer Health Scores
- ✅ KI-basierte Insight-Generation mit 80%+ Accuracy
- ✅ Real-time Dashboard mit < 3s Ladezeit
- ✅ Integration in bestehende Sales Workflows
- ✅ Actionable Recommendations für Sales Team

### Performance-Anforderungen
- ✅ Insight-Generation < 10 Sekunden P95
- ✅ Dashboard-Loading < 3 Sekunden
- ✅ OpenAI API Calls < 5 Sekunden
- ✅ Support für 10.000+ Kunden gleichzeitig
- ✅ 99.9% Uptime für Intelligence Service

### Business-Metriken
- ✅ 25%+ Verbesserung in Sales-Entscheidungsqualität
- ✅ 40%+ Reduktion in manueller Datenanalyse
- ✅ 15%+ Increase in Upselling Success Rate
- ✅ 20%+ frühere Churn-Erkennung
- ✅ ROI Break-even nach 1.5 Monaten

---

## 🔗 NAVIGATION ZU ALLEN 40 FEATURES

### Core Sales Features
- [FC-001 Customer Acquisition](/docs/features/PLANNED/01_customer_acquisition/FC-001_TECH_CONCEPT.md) | [M3 Sales Cockpit](/docs/features/ACTIVE/05_ui_foundation/M3_SALES_COCKPIT_KOMPAKT.md) | [M4 Opportunity Pipeline](/docs/features/ACTIVE/02_opportunity_pipeline/M4_KOMPAKT.md) | [M8 Calculator Modal](/docs/features/ACTIVE/03_calculator_modal/M8_KOMPAKT.md)
- [FC-004 Verkäuferschutz](/docs/features/PLANNED/07_verkaeuferschutz/FC-004_TECH_CONCEPT.md) | [FC-013 Duplicate Detection](/docs/features/PLANNED/15_duplicate_detection/FC-013_TECH_CONCEPT.md) | [FC-014 Activity Timeline](/docs/features/PLANNED/16_activity_timeline/FC-014_TECH_CONCEPT.md)
- [FC-015 Deal Loss Analysis](/docs/features/PLANNED/17_deal_loss_analysis/FC-015_TECH_CONCEPT.md) | [FC-016 Opportunity Cloning](/docs/features/PLANNED/18_opportunity_cloning/FC-016_TECH_CONCEPT.md) | [FC-019 Advanced Sales Metrics](/docs/features/PLANNED/19_advanced_metrics/FC-019_TECH_CONCEPT.md)

### Security & Foundation Features  
- [FC-008 Security Foundation](/docs/features/ACTIVE/01_security_foundation/FC-008_KOMPAKT.md) | [FC-009 Permissions System](/docs/features/ACTIVE/04_permissions_system/FC-009_KOMPAKT.md) | [FC-025 DSGVO Compliance](/docs/features/PLANNED/25_dsgvo_compliance/FC-025_TECH_CONCEPT.md)
- [FC-023 Event Sourcing](/docs/features/PLANNED/23_event_sourcing/FC-023_TECH_CONCEPT.md) | [FC-038 Multi-Tenant Architecture](/docs/features/PLANNED/38_multitenant/FC-038_TECH_CONCEPT.md) | [FC-039 API Gateway](/docs/features/PLANNED/39_api_gateway/FC-039_TECH_CONCEPT.md)

### Mobile & Field Service Features
- [FC-006 Mobile App](/docs/features/PLANNED/09_mobile_app/FC-006_TECH_CONCEPT.md) | [FC-018 Mobile PWA](/docs/features/PLANNED/09_mobile_app/FC-018_MOBILE_FIELD_SALES.md) | [FC-022 Mobile Light](/docs/features/PLANNED/22_mobile_light/FC-022_TECH_CONCEPT.md)
- [FC-029 Voice-First Interface](/docs/features/PLANNED/29_voice_first/FC-029_TECH_CONCEPT.md) | [FC-030 One-Tap Actions](/docs/features/PLANNED/30_one_tap_actions/FC-030_TECH_CONCEPT.md) | [FC-032 Offline-First](/docs/features/PLANNED/32_offline_first/FC-032_TECH_CONCEPT.md)

### Communication Features
- [FC-003 E-Mail Integration](/docs/features/PLANNED/06_email_integration/FC-003_TECH_CONCEPT.md) | [FC-012 Team Communication](/docs/features/PLANNED/14_team_communication/FC-012_TECH_CONCEPT.md) | [FC-028 WhatsApp Business](/docs/features/PLANNED/28_whatsapp_integration/FC-028_TECH_CONCEPT.md)
- [FC-035 Social Selling Helper](/docs/features/PLANNED/35_social_selling/FC-035_TECH_CONCEPT.md)

### Analytics & Intelligence Features
- [M6 Analytics Module](/docs/features/PLANNED/13_analytics_m6/M6_KOMPAKT.md) | [FC-007 Chef-Dashboard](/docs/features/PLANNED/10_chef_dashboard/FC-007_TECH_CONCEPT.md) | [FC-026 Analytics Platform](/docs/features/PLANNED/26_analytics_platform/FC-026_TECH_CONCEPT.md)
- [FC-034 Instant Insights](/docs/features/PLANNED/34_instant_insights/FC-034_TECH_CONCEPT.md) | [FC-037 Advanced Reporting](/docs/features/PLANNED/37_advanced_reporting/FC-037_TECH_CONCEPT.md) | [FC-040 Performance Monitoring](/docs/features/PLANNED/40_performance_monitoring/FC-040_TECH_CONCEPT.md)

### Integration & Infrastructure Features
- [FC-005 Xentral Integration](/docs/features/PLANNED/08_xentral_integration/FC-005_TECH_CONCEPT.md) | [FC-010 Customer Import](/docs/features/PLANNED/11_customer_import/FC-010_KOMPAKT.md) | [FC-021 Integration Hub](/docs/features/PLANNED/21_integration_hub/FC-021_TECH_CONCEPT.md)
- [FC-024 File Management](/docs/features/PLANNED/24_file_management/FC-024_TECH_CONCEPT.md) | [M5 Customer Refactor](/docs/features/PLANNED/12_customer_refactor_m5/M5_KOMPAKT.md)

### UI & Productivity Features
- [M1 Navigation](/docs/features/ACTIVE/05_ui_foundation/M1_NAVIGATION_KOMPAKT.md) | [M2 Quick Create](/docs/features/ACTIVE/05_ui_foundation/M2_QUICK_CREATE_KOMPAKT.md) | [M7 Settings](/docs/features/ACTIVE/05_ui_foundation/M7_SETTINGS_KOMPAKT.md)
- [FC-020 Quick Wins](/docs/features/PLANNED/20_quick_wins/FC-020_TECH_CONCEPT.md) | [FC-031 Smart Templates](/docs/features/PLANNED/31_smart_templates/FC-031_TECH_CONCEPT.md) | [FC-033 Visual Customer Cards](/docs/features/PLANNED/33_visual_cards/FC-033_TECH_CONCEPT.md)

### Advanced Features
- [FC-011 Bonitätsprüfung](/docs/features/ACTIVE/02_opportunity_pipeline/integrations/FC-011_KOMPAKT.md) | [FC-017 Sales Gamification](/docs/features/PLANNED/99_sales_gamification/FC-017_KOMPAKT.md) | [FC-027 Magic Moments](/docs/features/PLANNED/27_magic_moments/FC-027_TECH_CONCEPT.md)
- [FC-036 Beziehungsmanagement](/docs/features/PLANNED/36_relationship_mgmt/FC-036_TECH_CONCEPT.md)

---

**🤖 FC-002 Smart Customer Insights - Ready for Implementation!**  
**⏱️ Geschätzter Aufwand:** 10-12 Tage | **ROI:** 8.4:1 | **Priorität:** HIGH