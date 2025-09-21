# 🏛️ Governance Infrastructure - Technical Concept

---
**Status:** 🟢 Ready for Implementation
**Team:** Infrastructure Team
**Timeline:** Q4 2025 → Q1 2026 (Settings MVP: 4-6 Wochen)
**Priority:** P0 (blockiert Module 06 + 07)
**Effort:** M (Settings MVP), L (Complete)

---

## 📋 Executive Summary

### **Mission Statement**
Implementierung einer Enterprise-Grade Governance-Infrastructure als Foundation für alle FreshPlan Module. Settings Registry als zentraler Policy Store mit <50ms SLO, AI Strategy für Kostenkontrolle (€600-1200/Monat), und Business Logic Governance für neue Rabattlogik.

### **Business Impact**
- ✅ **Module 06 Unblocking:** Settings Registry für Einstellungen-Management
- ✅ **Module 07 Foundation:** AI Strategy für CAR-Strategy Implementation
- ✅ **Cost Control:** AI Budget Management mit messbaren KPIs
- ✅ **Business Rules:** Zentrale Governance für neue Rabattlogik (ab 01.10.2025)
- ✅ **Enterprise Readiness:** Production-ready Governance-Platform

### **Technical Foundation**
- **Settings Registry:** Hybrid Architecture (Meta + JSONB) mit RLS + ABAC
- **AI Strategy:** MVP-Plus mit Budget Gates + Confidence Routing
- **Performance:** <50ms SLO für Settings, <300ms für AI Routing
- **Security:** RLS fail-closed + JWT Integration + JSON Schema Validation

---

## 🏗️ Architecture Overview

### **Core Components**

#### **1. Settings Registry (Hybrid Architecture)**
```yaml
Database Layer:
  - settings_registry: Meta/SoT mit JSON Schema
  - settings_store: Values per Scope (Global/Tenant/Org/User)
  - settings_effective: Pre-computed Projection mit ETag

Service Layer:
  - SettingsService: L1-Cache + Merge Strategies + Metrics
  - SettingsResource: REST API mit ETag Support
  - SettingsNotifyListener: LISTEN/NOTIFY Cache Invalidation

Security Layer:
  - RLS fail-closed auf allen Tabellen
  - JWT → Session Variables (app.user_id, app.roles, etc.)
  - JSON Schema Validation server-side
```

#### **2. AI Strategy Service (MVP-Plus)**
```yaml
Core Features:
  - Budget Gates: €600-1200/Monat per Org
  - Confidence Routing: Small-first (0.7 threshold) → Large fallback
  - Provider Abstraction: OpenAI ↔ Anthropic fallback
  - Cache Strategy: TTL 8h mit ETag + hit rate tracking

Integration:
  - Settings Registry: ai.* keys für configuration
  - Performance SLO: <300ms für AI routing decisions
  - Metrics: Cost/Lead, Cost/Order, confidence histograms
```

#### **3. Business Logic Governance**
```yaml
Policy Storage:
  - Rabattlogik-Parameter in Settings Registry
  - business.discount.annual.tiers[] (2-10% Staffelung)
  - business.discount.welcome.duration (6 Monate)
  - business.discount.skonto.rate (1%)

Implementation:
  - Business Rules Engine als separates Modul 09
  - Settings Registry als Policy Store
  - Clean Separation: Policy (Governance) vs. Logic (Business)
```

### **Integration Architecture**

#### **Module Integration Points**
```yaml
Module 06 (Einstellungen):
  - Settings Registry als Core Service
  - Admin-UI für Settings Management
  - User/Org/Tenant Scope Management

Module 07 (Hilfe & Support):
  - AI Strategy für CAR-Strategy
  - help.space.* settings für Nudge Budgets
  - Confidence-based proactive assistance

Module 02/03/05/08:
  - Migration via RegistryOrConfig Adapter
  - Read-through Pattern (Registry → Config fallback)
  - Telemetry für Migration Progress
```

#### **External Systems**
```yaml
Keycloak OIDC:
  - JWT Claims → Settings Principal
  - Role-based Settings Access (admin/config/user)
  - Territory/Org Claims für Scope Resolution

PostgreSQL:
  - JSONB für flexible Settings Values
  - RLS für scope-basierte Security
  - LISTEN/NOTIFY für Cache Invalidation

Monitoring Stack:
  - Micrometer Metrics für SLO Tracking
  - Grafana Dashboards für Operations
  - Alerting für SLO Violations
```

---

## 🎯 Implementation Phases

### **Phase 1: Settings Registry MVP (Q4 2025, 4-6 Wochen)**

#### **Sprint 1: Foundation Setup (Woche 1-2)**
```yaml
Database:
  - Migration: settings_registry_core.sql deployment
  - RLS Policies: Security model implementation
  - Seeds: Lead protection + AI + Credit + Help settings

Backend:
  - SettingsService: Core service mit L1-Cache
  - SettingsResource: REST API implementation
  - JsonSchemaValidator: Server-side validation
  - SettingsNotifyListener: LISTEN/NOTIFY setup

Testing:
  - Unit Tests: Service layer coverage >80%
  - Integration Tests: Database + Security
  - Performance Tests: <50ms SLO validation
```

#### **Sprint 2: Module Integration (Woche 3-4)**
```yaml
Module 06 Integration:
  - RegistryOrConfig Adapter implementation
  - Read-through Pattern für seamless migration
  - Settings Admin API für Management UI

Monitoring:
  - Micrometer Metrics: settings_fetch_p95_ms
  - Grafana Panels: ETag hit rate, migration progress
  - Alerting: SLO violation detection

Load Testing:
  - k6 Scripts für <50ms SLO validation
  - Concurrent user scenarios (100+)
  - Cache invalidation performance
```

#### **Sprint 3: Rollout & Hardening (Woche 5-6)**
```yaml
Production Rollout:
  - Module 06 als Pilot für Settings Registry
  - Module 02/03 Migration (low-risk settings)
  - Migration Progress Tracking (Registry vs Config)

Quality Assurance:
  - E2E Tests: Complete user workflows
  - Security Tests: RLS + ABAC validation
  - Performance Validation: Real-world load

Documentation:
  - API Documentation: OpenAPI specs
  - Operations Runbook: Troubleshooting guides
  - Migration Guide: Module onboarding
```

### **Phase 2: AI Strategy MVP-Plus (Q1 2026, 3-4 Wochen)**

#### **Sprint 1: AI Router Implementation (Woche 1-2)**
```yaml
Core Services:
  - AIRoutingService: Confidence-based routing
  - ProviderAdapter: OpenAI + Anthropic abstraction
  - BudgetGateService: Monthly cap enforcement

Integration:
  - Settings Registry: ai.* configuration keys
  - Cost Tracking: Database + metrics collection
  - Cache Strategy: TTL + ETag implementation
```

#### **Sprint 2: Advanced Features (Woche 3-4)**
```yaml
Provider Fallback:
  - Health probing für provider availability
  - Automatic failover logic
  - Circuit breaker patterns

KPIs & Monitoring:
  - Cost metrics: Cost/Lead, Cost/Order
  - Performance metrics: Confidence histograms
  - Business metrics: ROI improvement tracking

Module 07 Integration:
  - CAR-Strategy AI routing
  - Proactive assistance confidence thresholds
  - Nudge budget management
```

### **Phase 3: Advanced Governance (Q2 2026, 3-5 Wochen)**

#### **Business Logic Integration**
```yaml
Module 09 (Business Rules Engine):
  - Rabattlogik implementation
  - Settings Registry als Policy Store
  - Real-time rule evaluation

Data Governance Automation:
  - DSAR Worker implementation
  - Retention Policy automation
  - Data classification enforcement
```

---

## 🔧 Implementation Details

### **Settings Registry Implementation**

#### **Database Schema**
```sql
-- Hybrid Architecture: Meta + JSONB Values
CREATE TABLE settings_registry (
  key             text PRIMARY KEY,
  type            text CHECK (type IN ('scalar','list','object')),
  scope           jsonb NOT NULL,
  schema          jsonb,
  default_value   jsonb,
  merge_strategy  text CHECK (merge_strategy IN ('replace','merge','append'))
);

CREATE TABLE settings_store (
  key            text REFERENCES settings_registry(key),
  tenant_id      uuid NULL,
  org_id         uuid NULL,
  user_id        uuid NULL,
  value          jsonb NOT NULL,
  UNIQUE (key, tenant_id, org_id, user_id)
);

CREATE TABLE settings_effective (
  key            text NOT NULL,
  tenant_id      uuid NULL,
  org_id         uuid NULL,
  user_id        uuid NULL,
  value          jsonb NOT NULL,
  etag           text NOT NULL,
  PRIMARY KEY (key, tenant_id, org_id, user_id)
);
```

#### **Security Model (RLS)**
```sql
-- Fail-closed RLS policies
CREATE POLICY rls_store_read ON settings_store
  USING (
    'admin' = ANY(string_to_array(current_setting('app.roles', true), ','))
    OR (tenant_id::text = current_setting('app.tenant_id', true)
        AND org_id::text = current_setting('app.org_id', true))
  );
```

#### **Performance Optimization**
```java
// <50ms SLO through Effective Projection + L1 Cache
private final Map<String, CacheEntry> l1 = new ConcurrentHashMap<>();

public EffectiveResult getEffective(SettingsPrincipal p, Set<String> keys) {
    // 1. Check L1 Cache (0 DB hits)
    CacheEntry ce = l1.get(cacheKey);
    if (ce != null && !expired(ce)) return cachedResult;

    // 2. Query Effective Table (1 DB hit)
    String sql = "SELECT key, value, etag FROM settings_effective WHERE ...";

    // 3. Compute missing on-the-fly
    for (String missingKey : missing) {
        JsonNode merged = mergeChain(connection, principal, missingKey);
        upsertEffective(connection, principal, missingKey, merged);
    }
}
```

### **AI Strategy Implementation**

#### **Budget Management**
```java
@ApplicationScoped
public class AIBudgetService {

    public boolean checkBudget(String orgId, BigDecimal requestCost) {
        // Get monthly cap from Settings Registry
        Optional<JsonNode> cap = settingsService.getEffective(
            principal, Set.of("ai.budget.monthly.cap")
        ).payload().get("ai.budget.monthly.cap");

        // Check current month usage
        BigDecimal currentUsage = getCurrentMonthUsage(orgId);
        return currentUsage.add(requestCost).compareTo(monthlyLimit) <= 0;
    }
}
```

#### **Confidence Routing**
```java
@ApplicationScoped
public class AIRoutingService {

    public AIResponse route(AIRequest request) {
        if (request.requiresLargeModel()) {
            return callLargeModel(request);
        }

        // Try small model first
        AIResponse small = callSmallModel(request);
        double threshold = getConfidenceThreshold(request.userId());

        if (small.confidence() >= threshold) {
            metrics.counter("ai_routing_small_success").increment();
            return small;
        }

        // Fallback to large model (budget check)
        if (checkBudget(request.orgId(), LARGE_MODEL_COST)) {
            return callLargeModel(request);
        }

        return small; // Return small even with low confidence if budget exceeded
    }
}
```

### **Business Logic Integration**

#### **Settings für neue Rabattlogik**
```yaml
business.discount.annual.tiers:
  type: list
  scope: ["global"]
  schema:
    type: array
    items:
      type: object
      properties:
        threshold: {type: integer, minimum: 0}
        rate: {type: number, minimum: 0, maximum: 1}
  default: [
    {threshold: 50000, rate: 0.02},
    {threshold: 100000, rate: 0.04},
    {threshold: 250000, rate: 0.06},
    {threshold: 500000, rate: 0.08},
    {threshold: 999999999, rate: 0.10}
  ]
  merge_strategy: replace

business.discount.welcome.duration:
  type: scalar
  scope: ["global"]
  default: {months: 6}

business.discount.skonto.rate:
  type: scalar
  scope: ["global"]
  default: 0.01
```

---

## 🔒 Security Implementation

### **ABAC + RLS Integration**

#### **JWT → Principal Mapping**
```java
public record SettingsPrincipal(
    UUID userId,
    Optional<UUID> tenantId,
    Optional<UUID> orgId,
    Set<String> roles
) {
    public static SettingsPrincipal fromJwt(JsonWebToken jwt) {
        UUID u = UUID.fromString(jwt.getSubject());
        Optional<UUID> tenant = Optional.ofNullable((String) jwt.getClaim("tenant_id"))
            .map(UUID::fromString);
        Set<String> roles = Optional.ofNullable((Collection<String>) jwt.getClaim("roles"))
            .map(coll -> Set.copyOf(coll))
            .orElseGet(Set::of);
        return new SettingsPrincipal(u, tenant, org, roles);
    }
}
```

#### **Session Variable Setup**
```java
private void setSession(Connection c, SettingsPrincipal p) throws SQLException {
    try (Statement stmt = c.createStatement()) {
        stmt.execute("SET app.user_id = '" + p.userId() + "'");
        stmt.execute("SET app.roles = '" + String.join(",", p.roles()) + "'");
        p.tenantId().ifPresent(tid ->
            stmt.execute("SET app.tenant_id = '" + tid + "'"));
        p.orgId().ifPresent(oid ->
            stmt.execute("SET app.org_id = '" + oid + "'"));
    }
}
```

### **JSON Schema Validation**
```java
@ApplicationScoped
public class JsonSchemaValidator {

    private final JsonSchemaFactory factory =
        JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);

    public void validate(JsonNode schemaNode, JsonNode value) {
        if (schemaNode == null) return;

        JsonSchema schema = factory.getSchema(schemaNode);
        Set<ValidationMessage> errors = schema.validate(value);

        if (!errors.isEmpty()) {
            String message = errors.stream()
                .map(ValidationMessage::getMessage)
                .collect(Collectors.joining("; "));
            throw new IllegalArgumentException("Validation failed: " + message);
        }
    }
}
```

---

## 📊 Performance & SLO Implementation

### **Performance Requirements**

#### **Settings Registry SLOs**
```yaml
Normal Operations:
  - GET /api/settings/effective: p95 < 50ms
  - PATCH /api/settings: p95 < 200ms
  - Cache Hit Rate: > 80%
  - ETag Hit Rate: > 60%

Peak Operations (5x load):
  - GET /api/settings/effective: p95 < 75ms
  - Database connections: < 50% pool utilization
  - Memory usage: < 80% heap
```

#### **AI Strategy SLOs**
```yaml
AI Routing Performance:
  - Small Model Decision: p95 < 100ms
  - Large Model Decision: p95 < 300ms
  - Budget Check: p95 < 50ms
  - Provider Fallback: p95 < 500ms

Cost Management:
  - Monthly Budget Accuracy: > 99%
  - Cost Tracking Latency: < 1 minute
  - Budget Alert Latency: < 5 minutes
```

### **Monitoring Implementation**

#### **Key Metrics**
```java
// Settings Performance Metrics
Timer settingsFetchTimer = metrics.timer("settings_fetch_ms");
Counter etagHitCounter = metrics.counter("settings_etag_hits");
Counter migrationSourceCounter = metrics.counter("settings_lookup_source",
    "module", moduleName, "source", "registry|config");

// AI Strategy Metrics
Timer aiRoutingTimer = metrics.timer("ai_routing_ms", "model", "small|large");
Counter aiCostCounter = metrics.counter("ai_cost_eur_total", "org", orgId);
Gauge aiBudgetUsage = metrics.gauge("ai_budget_usage_percentage", orgId);
Histogram aiConfidenceDistribution = metrics.histogram("ai_confidence_distribution");
```

#### **Grafana Dashboards**
```yaml
Settings Registry Dashboard:
  - settings_fetch_p95_ms (target: 50ms)
  - settings_etag_hit_rate (target: >60%)
  - settings_migration_progress by module
  - settings_cache_invalidation_frequency

AI Strategy Dashboard:
  - ai_cost_eur_daily by org
  - ai_budget_usage_percentage (alert >90%)
  - ai_routing_confidence_histogram
  - ai_provider_failover_frequency
```

---

## 🧪 Testing Strategy

### **Unit Testing (>80% Coverage)**

#### **Settings Registry Tests**
```java
@Test
void getEffective_withValidKey_returnsValue() {
    // Given
    SettingsPrincipal principal = createTestPrincipal();
    seedSetting("ai.cache.ttl", "PT8H", TENANT_SCOPE);

    // When
    EffectiveResult result = settingsService.getEffective(principal, Set.of("ai.cache.ttl"));

    // Then
    assertThat(result.payload().get("ai.cache.ttl").asText()).isEqualTo("PT8H");
    assertThat(result.etag()).isNotNull();
}

@Test
void patchSetting_withInvalidSchema_throwsValidationException() {
    // Given
    PatchRequest request = new PatchRequest("ai.budget.monthly.cap", "global",
        null, null, null, mapper.readTree("-100")); // Invalid negative value

    // When/Then
    assertThatThrownBy(() -> settingsService.patch(principal, request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("minimum");
}
```

#### **AI Strategy Tests**
```java
@Test
void route_smallModelHighConfidence_returnsSmallResult() {
    // Given
    AIRequest request = createTestRequest();
    when(smallModelService.call(request)).thenReturn(
        new AIResponse("result", 0.8)); // High confidence
    when(settingsService.getConfidenceThreshold(any())).thenReturn(0.7);

    // When
    AIResponse response = aiRoutingService.route(request);

    // Then
    assertThat(response.usedModel()).isEqualTo("small");
    verify(largeModelService, never()).call(any());
}

@Test
void route_budgetExceeded_returnsSmallModelResult() {
    // Given
    when(budgetService.checkBudget(any(), any())).thenReturn(false);
    when(smallModelService.call(any())).thenReturn(new AIResponse("result", 0.3));

    // When
    AIResponse response = aiRoutingService.route(createTestRequest());

    // Then
    assertThat(response.usedModel()).isEqualTo("small");
    verify(largeModelService, never()).call(any());
}
```

### **Integration Testing**

#### **Database + Security Integration**
```java
@Test
@TestTransaction
void settingsAccess_withRLS_enforcesScoping() {
    // Given: User in Tenant A
    SettingsPrincipal tenantAUser = createPrincipal("tenant-a", "user-1");
    seedSetting("test.key", "tenant-a-value", TENANT_SCOPE, "tenant-a");
    seedSetting("test.key", "tenant-b-value", TENANT_SCOPE, "tenant-b");

    // When: User queries settings
    EffectiveResult result = settingsService.getEffective(tenantAUser, Set.of("test.key"));

    // Then: Only sees own tenant's value
    assertThat(result.payload().get("test.key").asText()).isEqualTo("tenant-a-value");
}
```

#### **Performance Integration Tests**
```java
@Test
void getEffective_under50ms_withCache() {
    // Given
    SettingsPrincipal principal = createTestPrincipal();
    Set<String> keys = Set.of("ai.cache.ttl", "credit.batch.window.ms");

    // Warm up cache
    settingsService.getEffective(principal, keys);

    // When: Multiple calls
    long start = System.nanoTime();
    for (int i = 0; i < 100; i++) {
        settingsService.getEffective(principal, keys);
    }
    long duration = System.nanoTime() - start;

    // Then: Average < 5ms (well under 50ms SLO)
    double avgMs = duration / 100_000_000.0;
    assertThat(avgMs).isLessThan(5.0);
}
```

### **Load Testing (k6)**

#### **Settings Registry Load Test**
```javascript
import http from 'k6/http';
import { check } from 'k6';

export let options = {
  stages: [
    { duration: '2m', target: 100 }, // Ramp up
    { duration: '5m', target: 100 }, // Steady state
    { duration: '2m', target: 0 },   // Ramp down
  ],
  thresholds: {
    http_req_duration: ['p(95)<50'], // 50ms SLO
    http_req_failed: ['rate<0.01'],  // <1% error rate
  },
};

export default function() {
  const response = http.get(
    'http://localhost:8080/api/settings/effective?keys=ai.cache.ttl,credit.peak.slo.p95.ms',
    { headers: { 'Authorization': 'Bearer ' + __ENV.JWT_TOKEN } }
  );

  check(response, {
    'status is 200': (r) => r.status === 200,
    'response time < 50ms': (r) => r.timings.duration < 50,
    'has etag': (r) => r.headers['Etag'] !== undefined,
  });
}
```

---

## 🚀 Migration Strategy

### **Strangler Pattern Implementation**

#### **Read-Through Adapter**
```java
@ApplicationScoped
public class RegistryOrConfig {

    public Optional<String> getString(String key) {
        return getString(key, "core"); // Default module
    }

    public Optional<String> getString(String key, String module) {
        // 1. Try Settings Registry first
        SettingsPrincipal principal = SettingsPrincipal.fromJwt(jwt);
        EffectiveResult result = settingsService.getEffective(principal, Set.of(key));

        if (result.payload().has(key)) {
            metrics.counter("settings_lookup_source",
                "module", module, "source", "registry").increment();
            return Optional.of(result.payload().get(key).asText());
        }

        // 2. Fallback to @ConfigProperty
        String configValue = ConfigProvider.getConfig()
            .getOptionalValue(key, String.class).orElse(null);

        if (configValue != null) {
            metrics.counter("settings_lookup_source",
                "module", module, "source", "config").increment();
            return Optional.of(configValue);
        }

        return Optional.empty();
    }
}
```

#### **Migration Phases per Module**

**Phase 1: Read-Through Setup**
```java
// In Module 06 (Pilot)
@Inject RegistryOrConfig settings;

// Replace direct @ConfigProperty usage
// OLD:
@ConfigProperty(name = "ui.theme.default") String defaultTheme;

// NEW:
String defaultTheme = settings.getString("ui.theme.default").orElse("light");
```

**Phase 2: Registry Population**
```java
// Admin UI allows writing to Registry
// Telemetry shows migration progress
GET /api/metrics → settings_lookup_source{module="06",source="registry"} = 85%
GET /api/metrics → settings_lookup_source{module="06",source="config"} = 15%
```

**Phase 3: Fallback Removal**
```java
// When Registry hits > 95%, remove fallback
String defaultTheme = settings.getString("ui.theme.default")
    .orElseThrow(() -> new IllegalStateException("Setting not configured"));
```

### **Migration Timeline**

#### **Module-by-Module Rollout**
```yaml
Week 1-2: Module 06 (Einstellungen)
  - Pilot implementation with RegistryOrConfig
  - Admin UI for Settings management
  - Low-risk settings migration

Week 3-4: Module 02 + 03 (Neukundengewinnung + Kundenmanagement)
  - Lead protection settings
  - Territory-based configuration
  - User preference settings

Week 5-6: Module 05 + 07 (Kommunikation + Hilfe)
  - Communication preferences
  - CAR-Strategy settings
  - AI nudge budgets

Week 7-8: Module 01 + 04 + 08 (Cockpit + Auswertungen + Administration)
  - Dashboard configuration
  - Report preferences
  - System administration settings
```

---

## 🛠️ Operational Excellence

### **Monitoring & Alerting**

#### **SLO-based Alerting**
```yaml
Settings Registry Alerts:
  - name: settings_latency_high
    condition: settings_fetch_p95_ms > 50
    for: 5m
    severity: warning

  - name: settings_latency_critical
    condition: settings_fetch_p95_ms > 100
    for: 2m
    severity: critical

AI Strategy Alerts:
  - name: ai_budget_near_limit
    condition: ai_budget_usage_percentage > 90
    for: 1m
    severity: warning

  - name: ai_budget_exceeded
    condition: ai_budget_usage_percentage > 100
    for: immediate
    severity: critical
```

#### **Operational Runbooks**

**Settings Registry Issues:**
```yaml
Playbook: Settings Latency High
  Checks:
    - Database connection pool utilization
    - L1 cache hit rate
    - LISTEN/NOTIFY queue depth
    - Effective table index usage

  Actions:
    - Scale connection pool if util > 80%
    - Clear L1 cache if hit rate < 60%
    - Restart NOTIFY listener if queue > 100
    - Rebuild effective table indexes

  Escalation:
    - Page infrastructure team if critical
    - Engage database team if DB issues
```

**AI Budget Management:**
```yaml
Playbook: AI Budget Exceeded
  Checks:
    - Current month usage by org
    - Large model vs small model ratio
    - Provider cost breakdown
    - Budget approval status

  Actions:
    - Temporarily disable large model routing
    - Increase confidence threshold to 0.9
    - Notify org admins of budget status
    - Request budget increase approval

  Escalation:
    - Business owner approval for budget increase
    - Finance team notification for cost review
```

### **Disaster Recovery**

#### **Backup Strategy**
```yaml
Settings Registry:
  - Daily snapshots of settings tables
  - Configuration export to git repository
  - Point-in-time recovery capability
  - Cross-region backup replication

Recovery Procedures:
  - RTO: 15 minutes (fallback to @ConfigProperty)
  - RPO: 1 hour (last backup)
  - Automated failover to read-only mode
  - Manual configuration restore process
```

---

## 📋 Success Criteria & KPIs

### **Technical Success Metrics**

#### **Performance KPIs**
```yaml
Settings Registry:
  - GET /api/settings/effective p95 < 50ms: ✅ Target
  - Cache hit rate > 80%: ✅ Target
  - ETag hit rate > 60%: ✅ Target
  - Migration progress >95% Registry hits: ✅ Target

AI Strategy:
  - AI routing decision p95 < 300ms: ✅ Target
  - Budget tracking accuracy > 99%: ✅ Target
  - Cost reduction 2-3x through routing: ✅ Target
  - Provider failover < 500ms: ✅ Target
```

#### **Business KPIs**
```yaml
Module Enablement:
  - Module 06 unblocked: ✅ Critical
  - Module 07 CAR-Strategy enabled: ✅ Critical
  - All modules migrated to Registry: ✅ Target

Cost Management:
  - AI costs within €600-1200/month: ✅ Target
  - Business rules automation: ✅ Target
  - Governance overhead < 5% total dev time: ✅ Target
```

### **Quality Gates**

#### **Phase 1 Gates (Settings MVP)**
```yaml
Must Have:
  - ✅ All unit tests passing (>80% coverage)
  - ✅ Integration tests with real database
  - ✅ Load tests demonstrating <50ms SLO
  - ✅ Security tests validating RLS enforcement
  - ✅ Module 06 successful migration

Should Have:
  - ✅ Grafana dashboards operational
  - ✅ Alerting rules configured
  - ✅ Documentation complete
  - ✅ Runbooks validated

Could Have:
  - ✅ Admin UI for Settings management
  - ✅ Advanced cache invalidation
  - ✅ Audit logging
```

#### **Phase 2 Gates (AI Strategy)**
```yaml
Must Have:
  - ✅ Budget enforcement functional
  - ✅ Confidence routing working
  - ✅ Provider fallback tested
  - ✅ Cost tracking accurate

Should Have:
  - ✅ Module 07 CAR integration
  - ✅ KPI dashboards operational
  - ✅ Cost optimization demonstrable
```

---

## 🎯 Risks & Mitigations

### **Technical Risks**

#### **High Risk: Settings Performance**
```yaml
Risk: Settings latency > 50ms SLO
Probability: Medium
Impact: High (blocks all modules)

Mitigations:
  - Extensive load testing before rollout
  - L1 cache + effective projection architecture
  - Database index optimization
  - Horizontal scaling capability
  - Fallback to @ConfigProperty during issues
```

#### **Medium Risk: AI Cost Overrun**
```yaml
Risk: AI costs exceed budget controls
Probability: Low
Impact: Medium

Mitigations:
  - Hard budget enforcement at API level
  - Real-time cost tracking
  - Automatic large model disabling
  - Manager approval workflows
  - Cost alerting at 90% threshold
```

#### **Medium Risk: Migration Complexity**
```yaml
Risk: Module migration breaks existing functionality
Probability: Low
Impact: Medium

Mitigations:
  - Strangler pattern with fallback
  - Module-by-module rollout
  - Extensive testing per module
  - Feature flags for easy rollback
  - Monitoring migration progress
```

### **Business Risks**

#### **Low Risk: Adoption Resistance**
```yaml
Risk: Teams avoid using Settings Registry
Probability: Low
Impact: Low

Mitigations:
  - Clear migration benefits communication
  - Excellent developer experience
  - Comprehensive documentation
  - Training and support
  - Success story sharing
```

---

## 📚 Documentation & Knowledge Transfer

### **Technical Documentation**

#### **API Documentation**
```yaml
Settings Registry API:
  - OpenAPI 3.0 specification
  - Postman collection with examples
  - Authentication setup guide
  - Error response catalog

AI Strategy API:
  - Provider integration guide
  - Cost calculation methods
  - Confidence tuning guide
  - Monitoring setup instructions
```

#### **Operations Documentation**
```yaml
Deployment Guide:
  - Database migration procedures
  - Environment-specific configuration
  - Security setup checklist
  - Performance tuning guide

Troubleshooting Guide:
  - Common issues and solutions
  - Log analysis procedures
  - Performance debugging
  - Emergency procedures
```

### **Knowledge Transfer Sessions**

#### **Team Training Plan**
```yaml
Week 1: Infrastructure Team
  - Settings Registry architecture deep-dive
  - Security model implementation
  - Performance optimization techniques
  - Operational procedures

Week 2: Module Teams
  - Migration guide and timeline
  - RegistryOrConfig adapter usage
  - Best practices and patterns
  - Testing approaches

Week 3: Operations Team
  - Monitoring and alerting setup
  - Incident response procedures
  - SLO tracking and reporting
  - Capacity planning
```

---

## 🚀 Next Steps & Implementation Plan

### **Immediate Actions (Week 1)**

1. **Infrastructure Setup**
   - Deploy settings_registry_core.sql migration
   - Setup monitoring infrastructure (Grafana + Prometheus)
   - Configure CI/CD pipeline for governance module

2. **Development Environment**
   - Copy Settings-MVP Pack artefakte to project
   - Setup development database with seeds
   - Configure IDE and testing environment

3. **Team Preparation**
   - Schedule knowledge transfer sessions
   - Assign team members to components
   - Setup communication channels

### **Sprint Planning (Agile)**

#### **Sprint 1 (Woche 1-2): Foundation**
```yaml
Stories:
  - As a developer, I want Settings Registry database schema deployed
  - As a service, I want to read/write settings with <50ms SLO
  - As a system, I want RLS security enforced for all settings access
  - As an operator, I want monitoring dashboards for settings performance

Acceptance Criteria:
  - Database migration successful in all environments
  - SettingsService passes all unit tests
  - Security tests validate RLS enforcement
  - Grafana dashboards show real-time metrics
```

#### **Sprint 2 (Woche 3-4): Integration**
```yaml
Stories:
  - As Module 06, I want to migrate to Settings Registry
  - As a developer, I want read-through adapter for seamless migration
  - As an admin, I want to manage settings via REST API
  - As a system, I want cache invalidation via LISTEN/NOTIFY

Acceptance Criteria:
  - Module 06 successfully using RegistryOrConfig
  - Migration telemetry shows progress
  - Settings API functional and documented
  - Cache invalidation working within 100ms
```

#### **Sprint 3 (Woche 5-6): Production Readiness**
```yaml
Stories:
  - As a system, I want load testing validation of <50ms SLO
  - As an operator, I want alerting for SLO violations
  - As a team, I want complete documentation and runbooks
  - As a business, I want production rollout plan executed

Acceptance Criteria:
  - k6 load tests pass with <50ms p95
  - Alerting rules configured and tested
  - Documentation complete and reviewed
  - Production deployment successful
```

### **Stakeholder Communication**

#### **Weekly Status Reports**
```yaml
Audience: Engineering Leadership + Product
Content:
  - Sprint progress against timeline
  - SLO compliance status
  - Risk assessment updates
  - Blockers and dependencies
  - Business impact metrics
```

#### **Go-Live Checklist**
```yaml
Pre-Production:
  - ✅ All tests passing (unit, integration, load)
  - ✅ Security review complete
  - ✅ Performance validation successful
  - ✅ Documentation and runbooks ready
  - ✅ Team training completed

Production:
  - ✅ Database migration executed
  - ✅ Services deployed and healthy
  - ✅ Monitoring and alerting active
  - ✅ Module 06 migration successful
  - ✅ SLOs being met consistently

Post-Production:
  - ✅ Performance monitoring for 48h
  - ✅ User feedback collection
  - ✅ Incident response readiness
  - ✅ Phase 2 planning initiated
```

---

**🎯 This Governance Infrastructure Technical Concept provides a complete, production-ready foundation for FreshPlan's Enterprise-Grade Settings, AI Strategy, and Business Logic management with measurable SLOs and proven architectural patterns.**