# 📊 FC-034 INSTANT INSIGHTS - TESTING

[← Zurück zur Übersicht](./FC-034_OVERVIEW.md) | [← API & ML Integration](./FC-034_API.md)

---

## 🧪 TEST-STRATEGIE

### Test-Coverage-Ziele
- **Unit Tests:** 90% Coverage (kritische KI-Logik)
- **Integration Tests:** Alle API Endpoints & ML Pipeline
- **E2E Tests:** Insight-Generierung & Notification Flow
- **Performance Tests:** Insight-Generierung < 2s

---

## 🔍 UNIT TESTS

### Insight Engine Tests

```typescript
// insightEngine.test.ts
import { generateBrief } from './insightEngine';

describe('Insight Engine', () => {
  it('generiert Pre-Meeting Brief mit allen Elementen', async () => {
    const mockData = createMockCustomerData();
    const brief = await generateBrief(mockData);
    
    expect(brief.highlights).toHaveLength(4);
    expect(brief.highlights[0]).toContain('Letzter Kontakt');
    expect(brief.talkingPoints).toHaveLength(3);
    expect(brief.warnings).toBeDefined();
  });

  it('erkennt Warnungen korrekt', async () => {
    const dataWithWarnings = {
      ...createMockCustomerData(),
      overdueInvoices: [{ id: '1234', daysOverdue: 7 }],
      competitorActivity: { lastVisit: '2025-07-12' }
    };
    
    const brief = await generateBrief(dataWithWarnings);
    
    expect(brief.warnings).toHaveLength(2);
    expect(brief.warnings[0]).toContain('Rechnung #1234 überfällig');
    expect(brief.warnings[1]).toContain('Konkurrent');
  });

  it('personalisiert Gesprächsthemen', async () => {
    const dataWithPreferences = {
      ...createMockCustomerData(),
      productPreferences: ['bio', 'regional'],
      businessType: 'restaurant'
    };
    
    const brief = await generateBrief(dataWithPreferences);
    
    expect(brief.talkingPoints).toContain(
      expect.stringContaining('Bio-Linie')
    );
  });
});
```

### Behavior Analysis Tests

```java
// BehaviorAnalyzerTest.java
@QuarkusTest
class BehaviorAnalyzerTest {
    
    @Inject
    BehaviorAnalyzer analyzer;
    
    @Test
    void testChurnRiskCalculation() {
        Customer customer = createCustomerWithHistory();
        customer.setLastOrderDate(LocalDate.now().minusDays(90));
        
        double churnRisk = analyzer.calculateChurnProbability(customer);
        
        assertThat(churnRisk).isGreaterThan(0.7);
    }
    
    @Test
    void testSeasonalityDetection() {
        List<Order> orders = createOrdersWithSeasonalPattern();
        
        Map<Month, Double> seasonality = analyzer.detectSeasonality(orders);
        
        // Summer months should have higher factors
        assertThat(seasonality.get(Month.JULY)).isGreaterThan(1.2);
        assertThat(seasonality.get(Month.JANUARY)).isLessThan(0.8);
    }
    
    @Test
    void testNextOrderPrediction() {
        Customer regularCustomer = createRegularCustomer();
        
        LocalDate prediction = analyzer.predictNextOrderDate(regularCustomer);
        
        // Should be ~30 days from last order (regular pattern)
        long daysDiff = ChronoUnit.DAYS.between(
            regularCustomer.getLastOrderDate(), 
            prediction
        );
        assertThat(daysDiff).isBetween(28L, 32L);
    }
}
```

---

## 🔌 INTEGRATION TESTS

### API Endpoint Tests

```java
// InsightResourceTest.java
@QuarkusTest
class InsightResourceTest {
    
    @Test
    void testGeneratePreMeetingBrief() {
        given()
            .auth().oauth2(getValidToken())
            .contentType(ContentType.JSON)
            .body("""
                {
                    "customerId": "%s",
                    "meetingDate": "2025-07-19T14:00:00Z",
                    "context": "quarterly_review"
                }
                """.formatted(TEST_CUSTOMER_ID))
            .when()
            .post("/api/insights/pre-meeting")
            .then()
            .statusCode(201)
            .body("type", equalTo("PRE_MEETING"))
            .body("highlights", hasSize(greaterThan(0)))
            .body("talkingPoints", hasSize(3));
    }
    
    @Test
    void testDailyDigest() {
        given()
            .auth().oauth2(getValidToken())
            .when()
            .get("/api/insights/daily-digest")
            .then()
            .statusCode(200)
            .body("date", equalTo(LocalDate.now().toString()))
            .body("insights", hasSize(greaterThan(0)));
    }
    
    @Test
    @TestTransaction
    void testFeedbackLoop() {
        // Create insight
        Insight insight = createTestInsight();
        
        // Submit feedback
        given()
            .auth().oauth2(getValidToken())
            .contentType(ContentType.JSON)
            .body("""
                {
                    "helpful": true,
                    "accuracy": 0.85,
                    "comment": "Very accurate prediction"
                }
                """)
            .when()
            .post("/api/insights/{id}/feedback", insight.id)
            .then()
            .statusCode(200);
        
        // Verify feedback stored
        InsightFeedback feedback = InsightFeedback.findByInsightId(insight.id);
        assertThat(feedback.accuracy).isEqualTo(0.85);
    }
}
```

---

## 🎭 E2E TESTS

### Insight Generation Flow

```typescript
// e2e/insights.spec.ts
import { test, expect } from '@playwright/test';

test.describe('Instant Insights', () => {
  test('Pre-Meeting Brief Generation', async ({ page }) => {
    // Navigate to customer
    await page.goto('/customers/123');
    
    // Open insights panel
    await page.click('[aria-label="Open insights"]');
    
    // Should show loading state
    await expect(page.locator('.insight-loading')).toBeVisible();
    
    // Wait for insights
    await expect(page.locator('.insight-card')).toBeVisible({ timeout: 3000 });
    
    // Verify content
    const highlights = page.locator('.insight-highlights .MuiChip-root');
    await expect(highlights).toHaveCount(4);
    
    // Expand talking points
    await page.click('.MuiAccordion-root');
    const talkingPoints = page.locator('.talking-points li');
    await expect(talkingPoints).toHaveCount(3);
  });

  test('Notification Flow', async ({ page, context }) => {
    // Grant notification permission
    await context.grantPermissions(['notifications']);
    
    // Trigger insight generation
    await page.goto('/api/test/trigger-insight');
    
    // Wait for notification
    const notification = await page.waitForEvent('notification');
    expect(notification.title).toBe('💡 Neuer Insight');
    
    // Click notification
    await notification.click();
    
    // Should navigate to insight
    await expect(page).toHaveURL(/\/insights\/[\w-]+$/);
  });
});
```

---

## ⚡ PERFORMANCE TESTS

### Insight Generation Performance

```typescript
// performance/insightPerf.test.ts
test('Insight Generation unter 2 Sekunden', async () => {
  const times = [];
  
  for (let i = 0; i < 20; i++) {
    const start = performance.now();
    await generateInsights(testCustomerId);
    const end = performance.now();
    times.push(end - start);
  }
  
  const p95 = percentile(times, 95);
  expect(p95).toBeLessThan(2000); // < 2s for 95th percentile
});
```

### ML Model Performance

```java
// MLPerformanceTest.java
@Test
void testPredictionLatency() {
    // Prepare batch of customers
    List<Customer> customers = generateTestCustomers(100);
    
    long start = System.currentTimeMillis();
    
    customers.parallelStream()
        .forEach(customer -> {
            behaviorAnalyzer.predict(customer);
        });
    
    long duration = System.currentTimeMillis() - start;
    
    // Should process 100 customers in < 1 second
    assertThat(duration).isLessThan(1000);
}
```

---

## 🐛 EDGE CASES & ERROR HANDLING

### Test Scenarios

1. **Neue Kunden:** Ohne Historie
2. **Inaktive Kunden:** > 1 Jahr keine Aktivität  
3. **Datenqualität:** Fehlende Felder
4. **Concurrent Updates:** Mehrere User gleichzeitig
5. **Rate Limiting:** Zu viele Anfragen

### Error Handling Tests

```typescript
test('handhabt fehlende Kundendaten', async () => {
  const minimalCustomer = { id: '123', name: 'Test' };
  
  const insights = await generateInsights(minimalCustomer);
  
  expect(insights).toBeDefined();
  expect(insights.highlights).toContain('Neue Kundenbeziehung');
});

test('verhindert Insight-Spam', async () => {
  // Generate multiple times
  await generateInsights(customerId);
  await generateInsights(customerId);
  const result = await generateInsights(customerId);
  
  expect(result.cached).toBe(true);
  expect(result.message).toContain('Bereits kürzlich generiert');
});
```

---

## 📊 ML MODEL VALIDATION

### Accuracy Metrics

```python
# ml_validation.py
def validate_churn_model():
    # Load test data
    X_test, y_true = load_test_data()
    
    # Get predictions
    y_pred = model.predict(X_test)
    
    # Calculate metrics
    accuracy = accuracy_score(y_true, y_pred)
    precision = precision_score(y_true, y_pred)
    recall = recall_score(y_true, y_pred)
    f1 = f1_score(y_true, y_pred)
    
    assert accuracy > 0.75, f"Accuracy {accuracy} below threshold"
    assert precision > 0.70, f"Precision {precision} below threshold"
    assert recall > 0.65, f"Recall {recall} below threshold"
```

---

## 📈 SUCCESS TRACKING

### Metrics Collection

```sql
-- Track insight effectiveness
CREATE VIEW insight_effectiveness AS
SELECT 
  i.type,
  COUNT(*) as total_generated,
  COUNT(f.id) as feedback_received,
  AVG(f.accuracy) as avg_accuracy,
  AVG(CASE WHEN f.helpful THEN 1 ELSE 0 END) as helpfulness_rate
FROM insights i
LEFT JOIN insight_feedback f ON i.id = f.insight_id
WHERE i.generated_at > NOW() - INTERVAL '30 days'
GROUP BY i.type;
```