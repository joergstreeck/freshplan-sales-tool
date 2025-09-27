# 🚨 TRIGGER SPRINT 2.1.1 - P0 HOTFIX Integration Gaps

**📅 Erstellt:** 2025-09-26
**🎯 Sprint:** 2.1.1 - P0 HOTFIX
**⏱️ Zeitschätzung:** 4-6h (Klasse B: Code-Skeleton)
**🔴 Priorität:** CRITICAL - Production Blocker für Sprint 2.2
**🏷️ PR:** #111 (feature/sprint-2-1-1-followup-integration-hotfix-FP-235-hotfix)

> **📍 Siehe auch (Modul-Overlay):**
> - **Modul 02 – Sprint-Map:** `features-neu/02_neukundengewinnung/SPRINT_MAP.md`
> - **Event System Patterns:** `features-neu/02_neukundengewinnung/artefakte/EVENT_SYSTEM_PATTERN.md`
> - **Performance Test Pattern:** `features-neu/02_neukundengewinnung/artefakte/PERFORMANCE_TEST_PATTERN.md`

> ### 🎯 Arbeitsanweisung (für Claude & neue Entwickler)
> **Einstiegspunkte für Modul 02:**
> 1. **Sprint-Map (Modulüberblick):** `features-neu/02_neukundengewinnung/SPRINT_MAP.md`
> 2. **Backend-Overlay:** `features-neu/02_neukundengewinnung/backend/_index.md`
> 3. **Frontend-Overlay:** `features-neu/02_neukundengewinnung/frontend/_index.md`
> 4. **Research-Details (nur bei Bedarf):** `features-neu/02_neukundengewinnung/analyse/_index.md`
> 5. **Produktionsmuster:** `features-neu/02_neukundengewinnung/artefakte/`
>
> **Wichtig:** *Stub‑Verzeichnisse im Modul‑Root ignorieren* (temporär, 2 Sprints). Aktuelle Inhalte beginnen immer bei den Overlays.

---

## 🎯 Ziel

**Mission:** Schließe drei kritische Integration Gaps zwischen Lead-Management (Sprint 2.1) und Dashboard/Monitoring, um Production-Ready Status zu erreichen.

**Problem:** Lead-Events erreichen Dashboard nicht, keine Follow-up Metriken sichtbar, kein Monitoring für kritische Automation.

**Solution:** Event-Distribution + Dashboard-Widget + Prometheus-Metrics mit AFTER_COMMIT Pattern und Idempotenz.

**Impact:** Vollständige Cross-Module Integration, Real-time Dashboard Updates, Production Observability.

---

## 🔴 P0 Integration Gaps (CRITICAL)

### **Gap 1: Event Distribution fehlt**
- **IST:** FollowUpProcessedEvent bleibt im Lead-Module isoliert
- **SOLL:** Cross-Module Event Distribution via CQRS EventPublisher
- **Impact:** Dashboard/Analytics/Monitoring erhalten keine Lead-Updates

### **Gap 2: Dashboard Widget Integration fehlt**
- **IST:** Lead-Statistiken nicht im Sales Cockpit sichtbar
- **SOLL:** LeadWidget mit T+3/T+7 Metriken, Live-Updates, RBAC
- **Impact:** Keine Transparenz über Follow-up Performance

### **Gap 3: Monitoring/Observability fehlt**
- **IST:** Keine Metriken für Follow-up Automation
- **SOLL:** Prometheus Counter/Histogram/Gauge + Grafana Alerts
- **Impact:** Keine Alerts bei Störungen, blind in Production

---

## ✅ Definition of Done

### **Funktionale Anforderungen:**
- [ ] Event Distribution operational (AFTER_COMMIT + Idempotenz)
- [ ] Dashboard Widget zeigt Lead-Metriken (T+3, T+7, Conversion)
- [ ] Prometheus Metrics exportiert (Counter, Histogram, Gauge)
- [ ] Cache-Invalidierung für Real-time Updates
- [ ] RBAC implementiert (Manager: Aggregates, User: eigene Daten)

### **Technische Anforderungen:**
- [ ] Performance: P95 < 200ms für alle neuen Endpoints
- [ ] Security: RLS Fail-Closed Pattern angewendet
- [ ] Tests: ≥80% Coverage (Unit + Integration + E2E)
- [ ] Feature-Toggle: `features.dashboard.lead-widget.enabled`
- [ ] Rollback-fähig: No-op Consumer als Fallback

### **Dokumentation:**
- [ ] TRIGGER_SPRINT_2_1_1.md complete
- [ ] Master Plan V5 aktualisiert
- [ ] Pattern-Referenzen dokumentiert
- [ ] API-Changes in OpenAPI spezifiziert

---

## 📋 Implementation Tasks

**🔴 WICHTIG: AFTER_COMMIT gilt ausschließlich für Publisher** (z.B. `LeadEventPublisher`, `DashboardEventPublisher`).
Listener (LISTEN/NOTIFY) laufen außerhalb der DB-Transaktion → dort wird Idempotenz/Dedupe angewendet, kein AFTER_COMMIT.

### **Task 1: Event Distribution implementieren (2h) - MIT EVENT-KONVERGENZ**

```java
// DashboardEventPublisher.java - AFTER_COMMIT NUR HIER, NICHT IN LISTENERN!
@ApplicationScoped
@RlsContext
public class DashboardEventPublisher {

    @Inject EventPublisher eventPublisher;
    @Inject TransactionSynchronizationRegistry txRegistry;

    /**
     * WICHTIG: Verwende LeadStatusChangeEvent aus PR #110 (nicht LeadStatusChangedEvent!)
     */
    public void onLeadStatusChange(@Observes LeadStatusChangeEvent event) {
        // Wiederverwende AFTER_COMMIT Pattern aus LeadEventPublisher
        txRegistry.registerInterposedSynchronization(new Synchronization() {
            public void afterCompletion(int status) {
                if (status == Status.STATUS_COMMITTED) {
                    publishToDashboard(event);
                }
            }
        });
    }

    public void onFollowUpProcessed(@Observes FollowUpProcessedEvent event) {
        // AFTER_COMMIT für Ghost-Event Prevention
        txRegistry.registerInterposedSynchronization(new Synchronization() {
            public void afterCompletion(int status) {
                if (status == Status.STATUS_COMMITTED) {
                    publishCrossModuleEvent(event);
                }
            }
        });
    }

    private void publishCrossModuleEvent(FollowUpProcessedEvent event) {
        // Idempotency Key - deterministisch!
        String idempotencyKey = generateIdempotencyKey(event);

        // Event Payload (abwärtskompatibel)
        JsonObject payload = new JsonObject()
            .put("leadId", event.getLeadId())
            .put("followUpType", event.getFollowUpType())
            .put("t3Count", event.getT3Count())
            .put("t7Count", event.getT7Count())
            .put("success", event.isSuccess())
            .put("userId", event.getUserId())
            .put("processedAt", event.processedAt());

        EventPublisher.DomainEvent dashboardEvent = EventPublisher.DomainEvent.builder()
            .eventType("lead.followup.processed")
            .aggregateId(event.getLeadId() != null ? event.getLeadId() : UUID.randomUUID())
            .aggregateType("Lead")
            .correlationId(idempotencyKey) // String, nicht UUID erzwingen
            .causationId(UUID.randomUUID())
            .userId(event.getUserId())
            .payload(payload)
            .build();

        eventPublisher.publishEvent(dashboardEvent);
    }

    private String generateIdempotencyKey(FollowUpProcessedEvent event) {
        String data = String.format("%s|%s|%s",
            event.getLeadId() != null ? event.getLeadId() : "BATCH",
            event.getFollowUpType(),
            event.processedAt()
        );
        return UUID.nameUUIDFromBytes(data.getBytes()).toString();
    }
}
```

### **Task 2: Dashboard Widget Integration (2h)**

```java
// LeadWidget.java - Dashboard Component
public class LeadWidget {
    // Core Metriken
    private LeadStatistics leadStats;
    private List<FollowUpMetric> recentFollowUps;

    // T+3/T+7 Spezifika
    private int pendingT3Count;
    private int pendingT7Count;
    private double t3ConversionRate;
    private double t7ConversionRate;

    // Trend-Daten (7-Tage Vergleich)
    private TrendData conversionTrend;
    private TrendData responseTrend;

    // RBAC-gefilterte Daten
    private boolean isManagerView;
    private String userId;
    private String territory;
}

// SalesCockpitService.java - Widget Builder
private LeadWidget buildLeadWidget(UUID userId) {
    LeadWidget widget = new LeadWidget();

    // RBAC Check
    boolean isManager = securityService.hasRole(userId, "MANAGER");
    widget.setManagerView(isManager);

    if (isManager) {
        // Aggregierte Daten für Manager
        widget.setLeadStats(leadService.getAggregatedStatistics());
        widget.setPendingT3Count(followUpService.getTotalPendingT3());
        widget.setPendingT7Count(followUpService.getTotalPendingT7());
    } else {
        // User-spezifische Daten
        widget.setLeadStats(leadService.getStatistics(userId));
        widget.setPendingT3Count(followUpService.getUserPendingT3(userId));
        widget.setPendingT7Count(followUpService.getUserPendingT7(userId));
    }

    // Cache mit TTL für Performance
    return cacheManager.cache("lead-widget:" + userId, widget, 60);
}
```

### **Task 3: Prometheus Metrics implementieren (1-2h) - MICROMETER**

**Metrik-Stack:** Quarkus 3.x → Micrometer + Prometheus-Registry

```java
// DashboardMetricsCollector.java - Micrometer statt MP Metrics
@ApplicationScoped
public class DashboardMetricsCollector {

    @Inject MeterRegistry registry;

    Counter eventsPublished;
    Counter eventsConsumed;
    Timer eventLatency;
    Gauge queueSize;
    AtomicInteger queueSizeValue = new AtomicInteger(0);

    @PostConstruct
    void initMetrics() {
        // Counter mit niedriger Kardinalität (max 3 Labels)
        eventsPublished = registry.counter("freshplan_events_published_total",
                                          "event_type", "lead_status_changed",
                                          "module", "leads");

        eventsConsumed = registry.counter("freshplan_events_consumed_total",
                                         "event_type", "lead_status_changed",
                                         "module", "cockpit");

        // Timer für Latenzmessung
        eventLatency = registry.timer("freshplan_event_latency_ms",
                                     "event_type", "lead_status_changed",
                                     "path", "leads_to_cockpit");

        // Gauge für Queue-Size
        queueSize = registry.gauge("freshplan_followup_queue_size",
                                  Tags.of("type", "t3"),
                                  queueSizeValue);
    }

    public void recordEventPublished() {
        eventsPublished.increment();
    }

    public void recordEventConsumed(long durationMs) {
        eventsConsumed.increment();
        eventLatency.record(Duration.ofMillis(durationMs));
    }

    public void updateQueueSize(int size) {
        queueSizeValue.set(size);
    }
}

// Grafana Alert Rules (IaC)
alert: HighFollowUpFailureRate
expr: rate(leads_followup_sent_total{result="failure"}[5m]) > 0.1
for: 5m
annotations:
  summary: "High follow-up failure rate detected"
  description: "{{ $value | humanizePercentage }} failure rate"
```

---

## ⚠️ Risiko

### **Risiko 1: Duplicate Events**
- **Problem:** Mehrfache Event-Verarbeitung bei Retry
- **Mitigation:** Idempotency-Key + Deduplizierung im Consumer
- **Implementation:** UUID.nameUUIDFromBytes() deterministisch

### **Risiko 2: PII Data Leaks**
- **Problem:** User-Daten in falschen Territories sichtbar
- **Mitigation:** RLS Fail-Closed + RBAC Checks
- **Implementation:** @RlsContext + SecurityService.hasRole()

### **Risiko 3: Metrics Cardinality Explosion**
- **Problem:** Zu viele Label-Kombinationen
- **Mitigation:** Limitierte Labels (type, territory, result)
- **Implementation:** Max 3 Labels pro Metric

---

## 🧪 Test-Strategie

### **E2E Tests:**
```java
@Test
void shouldDistributeEventsToDashboard() {
    // Given: Lead mit Follow-up
    Lead lead = createTestLead();

    // When: Follow-up processed
    followUpService.processT3(lead);

    // Then: Dashboard receives event
    await().atMost(5, SECONDS).until(() ->
        dashboardService.getLeadWidget(userId).getPendingT3Count() == 1
    );
}
```

### **Contract Tests:**
```java
@Test
void shouldEmitCloudEventsCompliantEvent() {
    // Event Schema Validation
    JsonSchema schema = loadSchema("cloudevents-v1.0.json");
    DomainEvent event = createFollowUpEvent();

    assertThat(event.toJson()).matchesSchema(schema);
    assertThat(event.getSpecVersion()).isEqualTo("1.0");
}
```

### **Performance Tests:**
```java
@Test
void shouldMeetP95Target() {
    List<Long> durations = IntStream.range(0, 1000)
        .mapToLong(i -> measureDuration(() ->
            dashboardService.getLeadWidget(userId)
        ))
        .collect(Collectors.toList());

    long p95 = calculatePercentile(durations, 95);
    assertThat(p95).isLessThan(200); // ms
}
```

---

## ⚡ Performance-Nachweis

### Ziele
- **Publish → Notify** (DB NOTIFY): P95 < 50 ms
- **Notify → Listener verarbeitet**: P95 < 150 ms
- **Widget-Query (LeadWidget)**: P95 < 50 ms
- **Keine Drops/Backlogs** in Listener-Threads

### Lastform
- **Burst**: 200–400 Events/s für 5 min
- **Sustained**: 100 Events/s für 10 min (Soak)
- Messung in drei Teilpfaden (s. Ziele)

### Mess-Setup (Skizze)
- k6 Szenario erzeugt Statuswechsel/Follow-ups → Events
- Micrometer/Prometheus sammelt:
  - `freshplan_events_published_total` / `freshplan_events_consumed_total`
  - `freshplan_event_latency_ms{path="publish_notify"|"notify_process"}`
  - `freshplan_dashboard_cache_invalidations_total{widget="lead"}`
- Grafana Dashboard mit drei Panels (o.g. Latenzen + Throughput)

### **Test-Implementation:**
```java
@Test
void performanceNachweis_EventDistribution() {
    // Warmup
    IntStream.range(0, 100).forEach(i ->
        followUpService.processT3(createLead()));

    // Messung über 1000 Iterationen
    List<Long> latencies = new ArrayList<>();
    for (int i = 0; i < 1000; i++) {
        long start = System.nanoTime();

        // Event-Flow simulieren
        Lead lead = createLead();
        followUpService.processT3(lead);

        // Warte auf Dashboard-Update
        await().atMost(500, MILLISECONDS).until(() ->
            dashboardService.getLeadWidget(userId).getPendingT3Count() > 0
        );

        long duration = (System.nanoTime() - start) / 1_000_000; // ms
        latencies.add(duration);
    }

    // P95 berechnen
    Collections.sort(latencies);
    long p95 = latencies.get((int)(latencies.size() * 0.95));

    assertThat(p95).isLessThan(200); // ms
    LOG.info("P95 Latency: {}ms", p95);
}
```

### **Kernmetriken:**
| Metrik | Ziel | Gemessen | Status |
|--------|------|----------|--------|
| Event E2E P95 | < 200ms | TBD | PENDING |
| Dashboard Query P95 | < 50ms | TBD | PENDING |
| Dropped Events | 0 | TBD | PENDING |
| Cache Invalidation | < 10ms | TBD | PENDING |

### **Nachweis-Artefakte:**
- Screenshot: Grafana Dashboard während Last-Test
- JFR Recording: CPU/Memory Profile
- k6 Load Test Report: `reports/k6-sprint-2-1-1.html`

---

## 🔒 Security-Checks

### Territory-Isolation (@TestSecurity)
- GIVEN: Lead in Territory "AT", Test-User "user-de" (DE)
- WHEN: Dashboard-Widget geladen
- THEN: Keine Activity/Recent-Eintrags-IDs des AT-Leads sichtbar

```java
@Test
@TestSecurity(user = "user-de", roles = "sales")
void shouldNotSeeAustriaLeads() {
    // Given: Lead in Austria
    Lead atLead = createLead("AT");

    // When: Dashboard loaded
    LeadWidget widget = cockpitService.buildLeadWidget();

    // Then: AT Lead not in recent activities
    assertThat(widget.getRecentActivities())
        .noneMatch(a -> a.getLeadId().equals(atLead.getId()));
}
```

### Event-Isolation
- GIVEN: TestEventCollector aktiv
- WHEN: Fremder Lead (user2) erzeugt Statuswechsel
- THEN: Collector erhält **kein** Event mit dieser Lead-ID

```java
@Test
void shouldNotReceiveForeignEvents() {
    // Given: Collector for user1
    TestEventCollector collector = new TestEventCollector();

    // When: user2 creates event
    asUser("user2", () -> leadService.changeStatus(lead2, ACTIVE));

    // Then: No events for user1 (using poll API)
    assertThat(collector.poll(Duration.ofSeconds(1))).isEmpty();
}
```

### **PII-Maskierung:**
```java
@Test
void shouldMaskPIIInLogs() {
    // Given: Lead mit PII
    Lead lead = createLead();
    lead.setContactEmail("max.mustermann@example.com");

    // When: Event processing
    followUpService.processT3(lead);

    // Then: Logs enthalten keine Email
    String logs = logCapture.getOutput();
    assertThat(logs)
        .doesNotContain("max.mustermann@example.com")
        .contains("***@example.com");
}
```

### Idempotenz (Dedup)
- Publish zwei identische Events (gleicher `idempotencyKey`)
- **Erwartung:** Collector registriert **max. 1** Verarbeitung

```java
@Test
void shouldProcessEventOnlyOnce() {
    // Given: Same event published twice
    String idempotencyKey = UUID.randomUUID().toString();
    DomainEvent event1 = createTestEvent(idempotencyKey);
    DomainEvent event2 = createTestEvent(idempotencyKey);

    // When: Both events published
    eventPublisher.publish(event1);
    eventPublisher.publish(event2);

    // Then: Collector sees only one unique event
    List<DomainEvent> received = collector.pollAll(Duration.ofSeconds(2));
    long uniqueKeys = received.stream()
        .map(e -> e.getIdempotencyKey())
        .distinct()
        .count();
    assertThat(uniqueKeys).isEqualTo(1);
}
```

### **Security-Checkliste:**
- [ ] @RlsContext auf allen Event-Handlers
- [ ] Territory-Scope in Queries validiert
- [ ] Owner/Collaborator Checks implementiert
- [ ] PII nur in verschlüsselten Feldern
- [ ] Audit-Logs für kritische Operationen
- [ ] Fail-Closed Pattern bei Fehlern
- [ ] Idempotency-Keys deterministisch

---

## Monitoring Metrics

### Event Metrics (Micrometer-konform)
```prometheus
# Event publishing mit Result-Dimensionen
freshplan_events_published{event_type, module, result="success|no_tx|truncated|denied|unauthenticated|failure"}
freshplan_events_consumed{event_type, module, result="success|duplicate|denied"}
freshplan_event_latency{event_type, path="publish_notify|notify_process"}

# Cache Metrics (Gauges)
freshplan_dedupe_cache_entries  # Aktuelle Anzahl Keys im Cache
freshplan_dedupe_cache_hit_rate  # Hit-Rate (0.0 - 1.0)

# Specific counters
freshplan_followup_automated{type="t3|t7"}
freshplan_dashboard_updates{update_type}
```

### Alerts Empfehlungen
- `freshplan_events_published{result="denied"} > 0` - Unauthorized Attempts
- `freshplan_events_published{result="truncated"} > Baseline` - Payload Size Issues
- `freshplan_dedupe_cache_entries > 450000` - Cache Fast Voll (90%)
- `freshplan_dedupe_cache_hit_rate < 0.5` - Cache Ineffizienz

## Configuration

### Production Config
```properties
# Security - Default geschlossen!
freshplan.security.allow-unauthenticated-publisher=false
freshplan.security.rls.enabled=true
```

### Test Config
```properties
# Tests erlauben unauthentifizierte Events
%test.freshplan.security.allow-unauthenticated-publisher=true
%test.quarkus.arc.selected-alternatives=de.freshplan.infrastructure.pg.TestPgNotifySender
%test.freshplan.security.rls.enabled=false
```

## 🔄 Migrations-Schritte + Rollback

### **Datenbank-Migrationen:**
**KEINE DB-Migrationen erforderlich in diesem Sprint**
- Nur Event-Payload-Erweiterungen (backward-compatible)
- Keine Schema-Änderungen
- Keine neuen Tabellen oder Indizes

### **Event-Konvergenz (WICHTIG):**
**Single Source of Truth für Events:**
- **BEIBEHALTEN:** `LeadStatusChangeEvent` aus PR #110 (bereits vorhanden mit idempotencyKey)
- **ENTFERNEN:** Neu eingeführtes `LeadStatusChangedEvent` → wird NICHT verwendet
- **ERWEITERN:** `FollowUpProcessedEvent` abwärtskompatibel (Legacy-Konstruktor vorhanden)

**Event-Contract (JSON):**

### Event Envelope (Korrigiert)
```json
// Dashboard Lead Status Changed Event
{
  "id": "uuid-v4",
  "source": "lead-management",
  "type": "dashboard.lead_status_changed",  // NICHT eventType!
  "time": "2025-09-26T20:00:00Z",
  "idempotencyKey": "deterministic-uuid-v5",
  "data": {
    "leadId": "uuid",
    "companyName": "Fresh Foods GmbH",
    "oldStatus": "REGISTERED",
    "newStatus": "QUALIFIED",
    "userId": "user-uuid",
    "changedAt": "2025-09-26T20:00:00Z"
  }
}

// Dashboard Follow-up Completed Event
{
  "id": "uuid-v4",
  "source": "lead-management",
  "type": "dashboard.followup_completed",
  "time": "2025-09-26T20:00:00Z",
  "idempotencyKey": "deterministic-uuid-v5",
  "data": {
    "leadId": "uuid",           // optional für BATCH
    "followUpType": "T3|T7|BATCH",
    "t3Count": 1,
    "t7Count": 0,
    "success": true,
    "userId": "string",
    "processedAt": "2025-09-26T20:00:00Z"
  }
}

// Bei Truncation (>8KB):
{
  "id": "uuid-v4",
  "source": "lead-management",
  "type": "dashboard.followup_completed",
  "time": "2025-09-26T20:00:00Z",
  "idempotencyKey": "deterministic-uuid-v5",
  "data": {
    "truncated": true,
    "reference": "deterministic-uuid-v5",
    "original_size_bytes": 9234,
    "hint": "payload >8KB, fetch details via API"
  }
}
```

### **Feature-Toggle:**
```properties
# application.properties
features.dashboard.lead-widget.enabled=true
features.events.cross-module.enabled=true
features.metrics.prometheus.enabled=true
```

### **No-op Consumer Fallback:**
```java
if (!featureToggle.isEnabled("events.cross-module")) {
    // Silent drop - keine Verarbeitung
    return CompletableFuture.completedFuture(null);
}
```

### **Database Rollback:**
```sql
-- Keine Schema-Änderungen in diesem Sprint
-- Nur Event-Payload Erweiterungen (backward-compatible)
```

---

## 📚 SoT-Referenzen

### **Single Source of Truth - Event-System:**
- **LeadStatusChangeEvent:** `/backend/src/main/java/de/freshplan/modules/leads/events/LeadStatusChangeEvent.java` (PR #110)
- **LeadEventPublisher:** `/backend/src/main/java/de/freshplan/modules/leads/events/LeadEventPublisher.java` (PR #110)
- **CrossModuleEventListener:** `/backend/src/main/java/de/freshplan/modules/leads/events/CrossModuleEventListener.java` (PR #110)
- **EventPublisher (CQRS):** `/backend/src/main/java/de/freshplan/infrastructure/cqrs/EventPublisher.java`

### **Prometheus-Metriken-Katalog:**
```properties
# Counter
freshplan_events_published_total{event_type="lead_status_changed", module="leads", result="success|failure"}
freshplan_events_consumed_total{event_type="lead_status_changed", module="cockpit", result="success|failure"}
freshplan_dashboard_cache_invalidations_total{widget="lead", trigger="event|manual"}

# Histogram (Buckets: 5, 10, 25, 50, 100, 250, 500, 1000ms)
freshplan_event_latency_ms{event_type="lead_status_changed", path="leads->cockpit"}
freshplan_followup_batch_duration_ms{type="T3|T7"}

# Gauge
freshplan_followup_queue_size{type="pending|processing"}
freshplan_dashboard_active_widgets{type="lead|customer|task"}
```

### **Genutzte Patterns aus PR #110:**
- **[Security Test Pattern](../features-neu/02_neukundengewinnung/artefakte/SECURITY_TEST_PATTERN.md)**
  - RBAC Tests für Manager vs User View
  - RLS Fail-Closed für Territory Isolation

- **[Performance Test Pattern](../features-neu/02_neukundengewinnung/artefakte/PERFORMANCE_TEST_PATTERN.md)**
  - P95 Validation Helper-Methoden
  - Load Test Framework Setup

- **[Event System Pattern](../features-neu/02_neukundengewinnung/artefakte/EVENT_SYSTEM_PATTERN.md)**
  - AFTER_COMMIT Transaction Pattern
  - Idempotency Key Generation
  - PostgreSQL LISTEN/NOTIFY

### **Architecture Decision Records (ADRs):**
- **ADR-0002:** PostgreSQL LISTEN/NOTIFY statt Event-Bus
- **ADR-0007:** RLS Connection Affinity Pattern für alle Module

### **Dokumentations-Links:**
- **Master Plan:** [CRM_COMPLETE_MASTER_PLAN_V5.md](./CRM_COMPLETE_MASTER_PLAN_V5.md)
- **Roadmap:** [PRODUCTION_ROADMAP_2025.md](./PRODUCTION_ROADMAP_2025.md)
- **Trigger Index:** [TRIGGER_INDEX.md](./TRIGGER_INDEX.md)
- **Modul 01 Cockpit:** [technical-concept.md](../features-neu/01_mein-cockpit/technical-concept.md)
- **Modul 02 Neukundengewinnung:** [technical-concept.md](../features-neu/02_neukundengewinnung/technical-concept.md)

### **Dependencies:**
- Sprint 2.1 (Lead-Management) ✅ COMPLETE (PR #103, #105, #110)
- CQRS Light Foundation ✅ COMPLETE (PR #94)
- Settings Registry ✅ COMPLETE (PR #99)

---

## 🚀 Deployment & Monitoring

### **Deployment-Reihenfolge:**
1. Backend mit Metrics-Endpoints
2. Dashboard-Frontend mit Widget
3. Grafana Dashboard Import
4. Alert Rules aktivieren

### **Health-Check Endpoints:**
```http
GET /q/health/ready
{
  "status": "UP",
  "checks": [{
    "name": "lead-event-handler",
    "status": "UP"
  }]
}

GET /q/metrics
# HELP leads_followup_sent_total Total follow-ups sent
# TYPE leads_followup_sent_total counter
leads_followup_sent_total{type="T3",territory="DE",result="success"} 42
```

### **Monitoring Dashboard:**
- **Grafana Dashboard ID:** 15234 (FreshPlan Lead Management)
- **Key Metrics:** Follow-up Success Rate, P95 Latency, Queue Size
- **Alert Channels:** Slack #freshplan-alerts, PagerDuty

---

## ✅ Abschluss-Checkliste

- [ ] Alle Tests grün (Unit, Integration, E2E)
- [ ] Performance-Ziele erreicht (P95 < 200ms)
- [ ] Security-Review passed (RBAC, RLS)
- [ ] Feature-Toggles konfiguriert
- [ ] Monitoring operational (Metrics, Alerts)
- [ ] Dokumentation aktualisiert
- [ ] PR Review completed
- [ ] Merge nach main

---

**🎯 Success Criteria:** Nach diesem Sprint sind Lead-Events vollständig in Dashboard und Monitoring integriert, mit Real-time Updates und Production-Ready Observability.

**⚠️ Blocker für Sprint 2.2:** Ohne diese Integration kann Kundenmanagement nicht mit Lead-Daten arbeiten!