# 📊 FC-034 INSTANT INSIGHTS - API & ML INTEGRATION

[← Zurück zur Übersicht](./FC-034_OVERVIEW.md) | [← Implementation](./FC-034_IMPLEMENTATION.md)

---

## 🔌 REST API ENDPOINTS

### Insight Management

```yaml
# Generate pre-meeting brief
POST /api/insights/pre-meeting
  Body:
    {
      "customerId": "uuid",
      "meetingDate": "2025-07-19T14:00:00Z",
      "context": "quarterly_review"
    }
  Response: 201
    {
      "id": "uuid",
      "type": "PRE_MEETING",
      "highlights": [...],
      "talkingPoints": [...],
      "warnings": [...],
      "generatedAt": "2025-07-19T13:30:00Z"
    }

# Get customer insights
GET /api/customers/{customerId}/insights
  Query:
    type?: "PRE_MEETING" | "ANALYSIS" | "TREND"
    limit?: number
    since?: datetime
  Response: 200
    {
      "insights": [...],
      "total": 15,
      "hasNew": true
    }

# Get daily digest
GET /api/insights/daily-digest
  Response: 200
    {
      "date": "2025-07-19",
      "insights": [...],
      "summary": "5 wichtige Updates heute"
    }

# Mark insight as read
PATCH /api/insights/{insightId}/read
  Response: 204

# Feedback on insight
POST /api/insights/{insightId}/feedback
  Body:
    {
      "helpful": true,
      "accuracy": 0.8,
      "comment": "Timing-Vorhersage war sehr präzise"
    }
```

---

## 💾 DATENMODELL

### Database Schema

```sql
-- Insights table
CREATE TABLE insights (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  customer_id UUID REFERENCES customers(id),
  type VARCHAR(50) NOT NULL,
  priority VARCHAR(20) DEFAULT 'MEDIUM',
  data JSONB NOT NULL,
  metadata JSONB DEFAULT '{}',
  generated_at TIMESTAMP NOT NULL DEFAULT NOW(),
  expires_at TIMESTAMP,
  read_at TIMESTAMP,
  read_by UUID REFERENCES users(id),
  
  INDEX idx_insights_customer (customer_id),
  INDEX idx_insights_type (type),
  INDEX idx_insights_generated (generated_at DESC)
);

-- Insight feedback for ML improvement
CREATE TABLE insight_feedback (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  insight_id UUID REFERENCES insights(id),
  user_id UUID REFERENCES users(id),
  helpful BOOLEAN,
  accuracy FLOAT CHECK (accuracy >= 0 AND accuracy <= 1),
  comment TEXT,
  created_at TIMESTAMP DEFAULT NOW()
);

-- Customer behavior metrics
CREATE TABLE customer_behavior (
  customer_id UUID PRIMARY KEY REFERENCES customers(id),
  order_frequency FLOAT,
  avg_order_value DECIMAL(10,2),
  last_order_date DATE,
  total_orders INTEGER,
  churn_risk_score FLOAT,
  upsell_potential_score FLOAT,
  preferred_contact_time VARCHAR(20),
  seasonality_pattern JSONB,
  updated_at TIMESTAMP DEFAULT NOW()
);

-- Prediction tracking
CREATE TABLE predictions (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  customer_id UUID REFERENCES customers(id),
  type VARCHAR(50),
  prediction JSONB,
  confidence FLOAT,
  predicted_at TIMESTAMP DEFAULT NOW(),
  actual_outcome JSONB,
  outcome_recorded_at TIMESTAMP
);
```

### Entity Models

```java
// Insight.java
@Entity
@Table(name = "insights")
public class Insight extends PanacheEntityBase {
    @Id
    @GeneratedValue
    public UUID id;
    
    @Column(name = "customer_id")
    public UUID customerId;
    
    @Enumerated(EnumType.STRING)
    public InsightType type;
    
    @Enumerated(EnumType.STRING)
    public Priority priority = Priority.MEDIUM;
    
    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    public InsightData data;
    
    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    public Map<String, Object> metadata;
    
    @Column(name = "generated_at")
    public LocalDateTime generatedAt;
    
    @Column(name = "expires_at")
    public LocalDateTime expiresAt;
    
    @Column(name = "read_at")
    public LocalDateTime readAt;
    
    @Column(name = "read_by")
    public UUID readBy;
}

// InsightData.java
@JsonSerialize
public class InsightData {
    public List<String> highlights;
    public List<String> talkingPoints;
    public List<String> warnings;
    public Map<String, Object> analysis;
    public List<Recommendation> recommendations;
}
```

---

## 🤖 ML INTEGRATION

### Simple ML Models

```typescript
// ml/churnPrediction.ts
export const churnPredictor = {
  // Simple logistic regression
  predictChurn: async (features: CustomerFeatures) => {
    // Feature weights (pre-trained)
    const weights = {
      daysSinceLastOrder: 0.015,
      orderFrequencyDecline: 0.3,
      avgOrderValueDecline: 0.2,
      supportTickets: 0.1,
      competitorMentions: 0.25
    };
    
    // Calculate weighted score
    let score = 0;
    Object.keys(weights).forEach(key => {
      score += features[key] * weights[key];
    });
    
    // Apply sigmoid
    return 1 / (1 + Math.exp(-score));
  },
  
  // Next Best Action recommendation
  recommendAction: async (context: CustomerContext) => {
    const actions = {
      call: calculateCallScore(context),
      email: calculateEmailScore(context),
      visit: calculateVisitScore(context),
      wait: calculateWaitScore(context)
    };
    
    // Return action with highest score
    return Object.entries(actions)
      .sort(([,a], [,b]) => b - a)[0][0];
  }
};
```

### Pattern Detection

```java
// services/PatternDetectionService.java
@ApplicationScoped
public class PatternDetectionService {
    
    public OrderPattern detectPatterns(List<Order> orders) {
        OrderPattern pattern = new OrderPattern();
        
        // Frequency analysis
        pattern.setAverageInterval(calculateAverageInterval(orders));
        pattern.setIntervalStdDev(calculateStdDev(orders));
        
        // Seasonality detection
        Map<Month, Double> monthlyAvg = calculateMonthlyAverages(orders);
        pattern.setSeasonalFactors(detectSeasonality(monthlyAvg));
        
        // Product preferences
        Map<String, Integer> productCounts = countProducts(orders);
        pattern.setTopProducts(getTopN(productCounts, 5));
        
        // Time preferences
        pattern.setPreferredOrderDay(getMostFrequentDay(orders));
        pattern.setPreferredOrderTime(getMostFrequentTimeSlot(orders));
        
        return pattern;
    }
    
    private Map<Month, Double> detectSeasonality(Map<Month, Double> data) {
        double yearlyAvg = data.values().stream()
            .mapToDouble(Double::doubleValue)
            .average()
            .orElse(0.0);
        
        return data.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> e.getValue() / yearlyAvg
            ));
    }
}
```

### Real-time Analysis Pipeline

```java
// services/RealtimeAnalysisPipeline.java
@ApplicationScoped
public class RealtimeAnalysisPipeline {
    
    @Inject
    @Channel("customer-events")
    Emitter<CustomerEvent> eventEmitter;
    
    @Incoming("customer-events")
    @Outgoing("insights")
    public Multi<Insight> processEvents(Multi<CustomerEvent> events) {
        return events
            .group().by(CustomerEvent::getCustomerId)
            .flatMap(grouped -> 
                grouped
                    .collect().asList()
                    .map(this::analyzeEvents)
            )
            .filter(insight -> insight.getPriority().isHigherThan(Priority.LOW));
    }
    
    private Insight analyzeEvents(List<CustomerEvent> events) {
        // Aggregate recent events
        EventSummary summary = summarizeEvents(events);
        
        // Detect anomalies
        if (summary.hasAnomaly()) {
            return createAnomalyInsight(summary);
        }
        
        // Check for opportunities
        if (summary.hasOpportunity()) {
            return createOpportunityInsight(summary);
        }
        
        return null;
    }
}
```

---

## 🔔 NOTIFICATION SYSTEM

### Push Notification Integration

```typescript
// services/insightNotifications.ts
export const insightNotifications = {
  // Web Push API
  pushInsight: async (userId: string, insight: Insight) => {
    const subscription = await getUserSubscription(userId);
    
    if (subscription) {
      await webpush.sendNotification(subscription, JSON.stringify({
        title: '💡 Neuer Insight',
        body: insight.summary,
        icon: '/icon-192.png',
        badge: '/badge-72.png',
        data: { 
          customerId: insight.customerId,
          insightId: insight.id
        },
        actions: [
          { action: 'view', title: 'Ansehen' },
          { action: 'dismiss', title: 'Später' }
        ]
      }));
    }
  },
  
  // In-app notifications
  createInAppNotification: (insight: Insight) => {
    return {
      id: generateId(),
      type: 'INSIGHT',
      title: getInsightTitle(insight),
      message: insight.summary,
      priority: insight.priority,
      actionUrl: `/customers/${insight.customerId}/insights/${insight.id}`,
      createdAt: new Date()
    };
  }
};
```

[→ Weiter zur Test-Strategie](./FC-034_TESTING.md)