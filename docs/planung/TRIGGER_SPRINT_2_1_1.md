# 🚨 TRIGGER SPRINT 2.1.1: FOLLOW-UP INTEGRATION HOTFIX (KONSOLIDIERT)

**STATUS:** 🔴 **KRITISCH - P0 GAPS VOR PRODUCTION**
**Sprint:** 2.1.1 - Follow-up Integration Hotfix
**Priorität:** P0 - Production Blocker
**Timeline:** 2-3 PT (1 konsolidierte PR)
**Owner:** Backend + Frontend Team
**Branch:** `feature/sprint-2-1-1-followup-integration-hotfix-FP-235-hotfix`

## ⚠️ KRITISCHE PRODUCTION GAPS

Nach erfolgreicher Implementation der T+3/T+7 Follow-up Automation (PR #109) wurden kritische Integration-Gaps identifiziert, die **VOR Production** behoben werden müssen.

## 🎯 KONSOLIDIERTE LÖSUNG - 1 PR STATT 3

**Entscheidung (26.09.2025):** Die ursprünglich geplanten 3 PRs werden in EINER konsolidierten PR zusammengeführt:
- ✅ **Vorteil:** Einfacheres Testing der Integration
- ✅ **Vorteil:** Weniger Review-Overhead
- ✅ **Vorteil:** Atomare Deployment-Einheit
- ✅ **Vorteil:** Reduzierte CI-Pipeline-Läufe

**Konsolidierte PR #110: Follow-up Integration Hotfix**
- Branch: `feature/sprint-2-1-1-followup-integration-hotfix-FP-235-hotfix`
- Umfasst: Event Distribution + Cockpit Widget + Prometheus Metrics
- Effort: 2-3 PT kombiniert

## 📊 KONKRETE UMSETZUNG IN EINER PR

### Komponente 1: Event Distribution (Backend)

**Tasks:**
```java
// 1. Event Catalog erweitern
// backend/src/main/resources/event-catalog.yaml
lead.followup.processed:
  producer: Module02.FollowUpAutomationService
  consumers:
    - Module01.CockpitDashboard
    - Module03.CustomerConversion
    - Module04.Analytics
  payload:
    t3Count: integer
    t7Count: integer
    processedAt: timestamp

// 2. Event Publisher in FollowUpAutomationService
@Inject Event<FollowUpProcessedEvent> followUpEvent;

private void publishFollowUpEvent(int t3Count, int t7Count) {
    followUpEvent.fire(new FollowUpProcessedEvent(
        t3Count,
        t7Count,
        LocalDateTime.now(),
        UUID.randomUUID().toString() // IdempotencyKey
    ));
}

// 3. CloudEvents Format
public class FollowUpProcessedEvent implements CloudEvent {
    private String id = UUID.randomUUID().toString();
    private String source = "freshplan.leads.followup";
    private String type = "lead.followup.processed.v1";
    private String specversion = "1.0";
    private LocalDateTime time;
    private Map<String, Object> data;
}
```

### Komponente 2: Cockpit Dashboard Widget (Frontend)

**Tasks:**
```typescript
// 1. Follow-up Widget Component
// frontend/src/features/cockpit/components/FollowUpWidget.tsx
interface FollowUpMetrics {
  pendingT3: number;
  pendingT7: number;
  sentToday: number;
  conversionRate: number;
}

export const FollowUpWidget: React.FC = () => {
  const { data } = useFollowUpMetrics();

  return (
    <Card className="p-4">
      <CardHeader>
        <CardTitle>Follow-up Automation</CardTitle>
      </CardHeader>
      <CardContent>
        <div className="grid grid-cols-2 gap-4">
          <MetricTile
            label="T+3 Pending"
            value={data?.pendingT3 || 0}
            trend={data?.t3Trend}
          />
          <MetricTile
            label="T+7 Pending"
            value={data?.pendingT7 || 0}
            trend={data?.t7Trend}
          />
          <MetricTile
            label="Sent Today"
            value={data?.sentToday || 0}
            icon={<Mail className="h-4 w-4" />}
          />
          <MetricTile
            label="Conversion Rate"
            value={`${data?.conversionRate || 0}%`}
            trend="up"
          />
        </div>
      </CardContent>
    </Card>
  );
};

// 2. WebSocket Integration
const useFollowUpMetrics = () => {
  const [metrics, setMetrics] = useState<FollowUpMetrics>();

  useEffect(() => {
    const eventSource = new EventSource('/api/dashboard/followup-stream');

    eventSource.onmessage = (event) => {
      const data = JSON.parse(event.data);
      setMetrics(data);
    };

    return () => eventSource.close();
  }, []);

  return { data: metrics };
};
```

### Komponente 3: Prometheus Metrics (Backend)

**Tasks:**
```java
// 1. Metrics in FollowUpAutomationService
@ApplicationScoped
public class FollowUpAutomationService {

    @Inject MeterRegistry registry;

    private Counter processedCounter;
    private Counter errorCounter;
    private Gauge pendingGauge;

    @PostConstruct
    void initMetrics() {
        processedCounter = Counter.builder("followup_processed_total")
            .description("Total follow-ups processed")
            .tags("type", "email")
            .register(registry);

        errorCounter = Counter.builder("followup_errors_total")
            .description("Follow-up processing errors")
            .register(registry);

        pendingGauge = Gauge.builder("followup_pending", this::countPendingFollowUps)
            .description("Pending follow-ups")
            .register(registry);
    }

    @Scheduled(cron = "{freshplan.followup.cron:0 0 9 * * ?}")
    @Transactional
    @RlsContext
    public void processScheduledFollowUps() {
        var timer = Timer.start(registry);
        try {
            // ... existing logic ...
            processedCounter.increment(processed);

            // Publish event
            publishFollowUpEvent(t3Count, t7Count);

        } catch (Exception e) {
            errorCounter.increment();
            throw e;
        } finally {
            timer.stop(Timer.Sample.start(registry)
                .stop(registry.timer("followup_processing_duration")));
        }
    }
}

// 2. Metrics Endpoint
@Path("/metrics/followup")
@RolesAllowed("manager")
public class FollowUpMetricsResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public FollowUpMetrics getMetrics() {
        return FollowUpMetrics.builder()
            .pendingT3(countPendingT3())
            .pendingT7(countPendingT7())
            .sentToday(countSentToday())
            .conversionRate(calculateConversionRate())
            .build();
    }

    @GET
    @Path("/stream")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @SseElementType(MediaType.APPLICATION_JSON)
    public Multi<FollowUpMetrics> streamMetrics() {
        return Multi.createFrom().ticks().every(Duration.ofSeconds(5))
            .map(tick -> getMetrics());
    }
}
```

## 🔧 MIGRATION V247

```sql
-- V247__followup_integration_metrics.sql

-- Event distribution table
CREATE TABLE IF NOT EXISTS event_distribution (
    id BIGSERIAL PRIMARY KEY,
    event_type VARCHAR(100) NOT NULL,
    idempotency_key VARCHAR(100) UNIQUE,
    payload JSONB NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP,
    status VARCHAR(20) DEFAULT 'PENDING',
    INDEX idx_event_dist_status (status, created_at)
);

-- Metrics materialized view for performance
CREATE MATERIALIZED VIEW followup_metrics AS
SELECT
    COUNT(CASE WHEN t3_followup_date > CURRENT_DATE THEN 1 END) as pending_t3,
    COUNT(CASE WHEN t7_followup_date > CURRENT_DATE THEN 1 END) as pending_t7,
    COUNT(CASE WHEN t3_sent_at::date = CURRENT_DATE OR t7_sent_at::date = CURRENT_DATE THEN 1 END) as sent_today,
    ROUND(
        COUNT(CASE WHEN status = 'CONVERTED' AND (t3_sent_at IS NOT NULL OR t7_sent_at IS NOT NULL) THEN 1 END) * 100.0 /
        NULLIF(COUNT(CASE WHEN t3_sent_at IS NOT NULL OR t7_sent_at IS NOT NULL THEN 1 END), 0),
        2
    ) as conversion_rate
FROM leads
WHERE deleted = false;

-- Refresh trigger
CREATE OR REPLACE FUNCTION refresh_followup_metrics()
RETURNS TRIGGER AS $$
BEGIN
    REFRESH MATERIALIZED VIEW CONCURRENTLY followup_metrics;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER refresh_metrics_on_lead_update
AFTER INSERT OR UPDATE OR DELETE ON leads
FOR EACH STATEMENT
EXECUTE FUNCTION refresh_followup_metrics();
```

## 🧪 TEST PLAN

### Unit Tests
```java
@Test
void shouldPublishEventAfterProcessing() {
    // Given
    setupPendingLeads(5, 3);

    // When
    service.processScheduledFollowUps();

    // Then
    verify(eventBus).publish(argThat(event ->
        event instanceof FollowUpProcessedEvent &&
        ((FollowUpProcessedEvent) event).getT3Count() == 5 &&
        ((FollowUpProcessedEvent) event).getT7Count() == 3
    ));
}

@Test
void shouldIncrementMetrics() {
    // Given
    var initialCount = getMetricValue("followup_processed_total");

    // When
    service.processScheduledFollowUps();

    // Then
    var newCount = getMetricValue("followup_processed_total");
    assertThat(newCount).isGreaterThan(initialCount);
}
```

### Integration Tests
```typescript
describe('FollowUpWidget', () => {
  it('should display real-time metrics', async () => {
    const { getByText } = render(<FollowUpWidget />);

    // Mock SSE data
    mockEventSource.emit('message', {
      data: JSON.stringify({
        pendingT3: 10,
        pendingT7: 5,
        sentToday: 15,
        conversionRate: 35
      })
    });

    await waitFor(() => {
      expect(getByText('10')).toBeInTheDocument();
      expect(getByText('35%')).toBeInTheDocument();
    });
  });
});
```

## ✅ ACCEPTANCE CRITERIA

1. **Event Distribution:**
   - [ ] FollowUpProcessedEvent wird nach jedem Scheduler-Run gefeuert
   - [ ] CloudEvents Format mit IdempotencyKey
   - [ ] Event erreicht alle Consumer-Module

2. **Cockpit Widget:**
   - [ ] Widget zeigt Real-time Metrics (T+3, T+7, Sent, Conversion)
   - [ ] SSE/WebSocket Updates alle 5 Sekunden
   - [ ] RBAC: Manager sehen alle Daten, User nur ihre eigenen

3. **Prometheus Metrics:**
   - [ ] Alle Metrics in /metrics Endpoint sichtbar
   - [ ] Grafana Dashboard konfiguriert
   - [ ] Alert bei Error-Rate > 5%

4. **Performance:**
   - [ ] Widget Load Time < 200ms
   - [ ] SSE Latency < 100ms
   - [ ] Metrics Collection Overhead < 1% CPU

## 🚀 DEPLOYMENT

```bash
# 1. Migration ausführen
./mvnw flyway:migrate

# 2. Backend Tests
./mvnw test -Dtest="FollowUp*Test"

# 3. Frontend Tests
npm test -- --coverage FollowUpWidget

# 4. Integration Tests
npm run test:e2e:followup

# 5. Metrics validieren
curl http://localhost:8080/metrics | grep followup_

# 6. PR erstellen
gh pr create \
  --title "fix(followup): P0 Integration Gaps - Event Distribution, Dashboard Widget, Metrics" \
  --body "Closes FP-235a, FP-235b, FP-235c. Implements critical integration gaps identified after PR #109." \
  --label "P0,hotfix,followup"
```

## 📋 PR CHECKLIST

- [ ] Event Distribution implementiert und getestet
- [ ] Cockpit Widget mit Real-time Updates
- [ ] Prometheus Metrics und Grafana Dashboard
- [ ] Migration V247 idempotent
- [ ] Unit Tests > 80% Coverage
- [ ] Integration Tests grün
- [ ] Performance Benchmarks erfüllt
- [ ] Documentation aktualisiert
- [ ] No breaking changes

## 🔗 Referenzen

- **Zentrale Übersicht:** [INTEGRATION_STATUS.md](./infrastruktur/INTEGRATION_STATUS.md) - P0/P1 Tasks
- **Sprint Index:** [TRIGGER_INDEX.md](./TRIGGER_INDEX.md) - Alle Sprints
- **Master Plan:** [CRM_COMPLETE_MASTER_PLAN_V5.md](./CRM_COMPLETE_MASTER_PLAN_V5.md) - Projektstand
- **Basis PR:** [PR #109](https://github.com/joergstreeck/freshplan-sales-tool/pull/109) - Follow-up Automation
- **P1 Tasks:**
  - [PR #273 Skeleton](./features-neu/03_kundenmanagement/PR_SKELETON_P1_4_AUTO_CONVERSION.md)
  - [PR #274 Skeleton](./infrastruktur/PR_SKELETON_P1_5_SHARED_EMAIL_CORE.md)
  - [PR #275 Skeleton](./features-neu/04_auswertungen/PR_SKELETON_P1_6_FOLLOWUP_ANALYTICS.md)

---
**🚨 WICHTIG:** Diese konsolidierte PR ist ein Production Blocker und muss mit höchster Priorität behandelt werden!