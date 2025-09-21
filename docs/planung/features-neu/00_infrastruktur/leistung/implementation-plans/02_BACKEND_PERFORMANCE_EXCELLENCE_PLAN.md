# Backend Performance-Excellence Plan - <100ms API-Response

**📊 Plan Status:** 🟢 PRODUCTION-READY
**🎯 Owner:** Backend Team + External AI Excellence
**⏱️ Timeline:** Tag 4-8 (Hot-Projections + ETag) → Tag 6-10 (Advanced Optimization)
**🔧 Effort:** L (Large - aber mit External AI Artefakten optimiert)

## 🎯 Executive Summary (für Claude)

**Mission:** >500ms → <100ms API-Response durch Hot-Projections + ETag/Conditional GET + PgBouncer
**Problem:** Live-JOINs + fehlende Caching + Database-Connection-Bottlenecks → Field-Sales-Impact
**Solution:** External AI Backend-Excellence mit Hot-Projections + ETag-Patterns + Query-Optimization
**Timeline:** 6-8 Stunden Implementation mit External AI Copy-Paste-Ready Artefakten
**Impact:** Business-Critical-APIs <100ms + Seasonal-5x-Load-Capability + Database-Performance-Excellence

## 📋 Context & Dependencies

### Current Backend-Performance-Reality:
- **API-Response-Times:** >500ms p95 (Ziel: <100ms) - 80% Improvement erforderlich
- **Database-Queries:** Live-JOINs für Cockpit/Leads (Ziel: Hot-Projections O(1))
- **Caching-Strategy:** Minimal (Ziel: ETag/Conditional GET + Cache-Hit-Rate >80%)
- **Connection-Pooling:** Basic Quarkus (Ziel: PgBouncer Transaction-Pooling)

### FreshFoodz-Specific Requirements:
- **Field-Sales-Critical:** Lead-Erfassung + Customer-Search <100ms mobile
- **Cockpit-Performance:** Dashboard-real-time updates ohne Live-JOINs
- **Sample-Management:** Sample-request + roi-calculation performance-optimized
- **Seasonal-Scaling:** 5x Load-capability für Weihnachts-Business-Peaks

### Dependencies:
→ **Database-Schema:** ✅ Ready für Hot-Projections (leads, customers, activities)
→ **External AI Artefakte:** ✅ VXXX__hot_projections.sql + EtagSupport.java ready
→ **Migration-Scripts:** ✅ get-next-migration.sh für Database-Schema-Updates

## 🏗️ Implementation Strategy

### **Phase 1: Hot-Projections Foundation (Tag 4-5)**
**Goal:** O(1) reads für Cockpit/Lead-Listen durch denormalized projections

**Database-Schema (External AI Ready):**
```sql
-- VXXX__hot_projections.sql (External AI Optimized)
CREATE TABLE cockpit_leads_hot (
  lead_id uuid PRIMARY KEY,
  customer_name text NOT NULL,
  owner_user_id uuid NOT NULL,        -- User-based (kein Territory)
  status text NOT NULL,
  last_activity_at timestamptz,
  sample_state text,                  -- Sample-Management integration
  roi_potential numeric(6,2),         -- Performance-oriented business-logic
  updated_at timestamptz NOT NULL DEFAULT now(),
  version_etag text NOT NULL          -- ETag-Support für Conditional GET
);

-- Recompute-Function (idempotent upserts)
CREATE OR REPLACE FUNCTION recompute_cockpit_lead_hot(p_lead uuid) RETURNS void AS $$
BEGIN
  INSERT INTO cockpit_leads_hot(lead_id, customer_name, owner_user_id, status, ...)
  SELECT l.id, c.name, o.user_id, l.status,
         (SELECT max(a.created_at) FROM activities a WHERE a.lead_id=l.id),
         s.status, l.roi_potential, now(),
         encode(digest(l.id::text || coalesce(l.updated_at::text,''), 'sha256'),'hex')
  FROM leads l
  JOIN customers c ON c.id=l.customer_id
  JOIN lead_ownership o ON o.lead_id=l.id  -- User-based ownership
  LEFT JOIN sample_request s ON s.lead_id=l.id AND s.latest = true
  WHERE l.id=p_lead
  ON CONFLICT (lead_id) DO UPDATE SET ...;
END;
$$ LANGUAGE plpgsql;
```

**Trigger-Integration:**
```sql
-- Automatic recomputation triggers
CREATE TRIGGER trg_lead_hot
AFTER INSERT OR UPDATE OF customer_id, status, updated_at ON leads
FOR EACH ROW EXECUTE FUNCTION recompute_cockpit_lead_hot(NEW.id);

-- Activity-updates trigger hot-projection updates
CREATE TRIGGER trg_activity_hot
AFTER INSERT OR UPDATE ON activities
FOR EACH ROW EXECUTE FUNCTION recompute_cockpit_lead_hot(NEW.lead_id);
```

**Success Criteria:**
- Hot-projections für Cockpit + Lead-Listen operational
- O(1) read-performance für dashboard-queries
- Version-ETag für ETag/Conditional GET integration ready

### **Phase 2: ETag/Conditional GET Implementation (Tag 5-6)**
**Goal:** 80% Cache-Hit-Rate durch intelligente ETag-based caching

**ETag-Support-Service (External AI Ready):**
```java
// EtagSupport.java - Production-Ready Helper
@ApplicationScoped
public class EtagSupport {
  public static String sha256Hex(String input) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(hash);
    } catch (Exception e) { throw new RuntimeException(e); }
  }

  public static Response conditional(String etag, String ifNoneMatch,
                                     java.util.function.Supplier<Response> supplier) {
    if (ifNoneMatch != null && !ifNoneMatch.isBlank() && ifNoneMatch.equals(etag)) {
      return Response.notModified().tag(new EntityTag(etag)).build();
    }
    Response r = supplier.get();
    return Response.fromResponse(r).tag(new EntityTag(etag)).build();
  }
}
```

**Customer/Lead-Resource ETag-Integration:**
```java
// CustomerResource.java - External AI Pattern
@GET @Path("/customers")
public Response listCustomers(@HeaderParam("If-None-Match") String ifNoneMatch) {
  // Hot-projection query mit version_etag
  List<CustomerProjection> customers = customerRepo.findAllHotProjections();

  // ETag aus Hot-Projection aggregated version
  String etag = EtagSupport.sha256Hex(
    customers.stream()
      .map(CustomerProjection::getVersionEtag)
      .collect(Collectors.joining())
  );

  // Conditional GET mit 304 Not-Modified support
  return EtagSupport.conditional(etag, ifNoneMatch,
    () -> Response.ok(customers).tag(new EntityTag(etag)).build());
}
```

**Success Criteria:**
- ETag/Conditional GET für Top-3 APIs (Customers, Leads, Cockpit)
- Cache-Hit-Rate >80% für read-heavy operations
- 304 Not-Modified responses functional + tested

### **Phase 3: Database-Connection-Optimization (Tag 6-7)**
**Goal:** PgBouncer Transaction-Pooling für Seasonal-5x-Load-Capability

**PgBouncer-Configuration (External AI Optimized):**
```ini
# pgbouncer.ini - FreshFoodz Seasonal-Load Tuned
[databases]
freshfoodz = host=postgres-host port=5432 dbname=freshfoodz

[pgbouncer]
pool_mode = transaction                # Optimal für Quarkus-Workload
max_client_conn = 1000                # Seasonal 5x peak-ready
default_pool_size = 25                # Per-database connection-pool
server_round_robin = 1                # Load-balancing
ignore_startup_parameters = extra_float_digits

# Connection-limits für Seasonal-Scaling
reserve_pool_size = 5                 # Emergency connections
max_db_connections = 100              # Database-server limits
```

**Quarkus-Database-Configuration:**
```properties
# application.properties - PgBouncer Integration
quarkus.datasource.jdbc.url=jdbc:postgresql://pgbouncer:6432/freshfoodz
quarkus.datasource.jdbc.max-size=30                    # Conservative pool-size
quarkus.datasource.jdbc.min-size=5                     # Always-ready connections
quarkus.datasource.jdbc.additional-jdbc-properties.reWriteBatchedInserts=true
quarkus.datasource.jdbc.additional-jdbc-properties.cachePrepStmts=true
```

**Success Criteria:**
- PgBouncer transaction-pooling operational
- Connection-pool tuned für Seasonal 5x load-capability
- Zero connection-timeout errors unter peak-load

### **Phase 4: Query-Performance-Optimization (Tag 7-8)**
**Goal:** Database-queries <50ms für business-critical operations

**Covering-Indices (External AI Optimized):**
```sql
-- query_optimization_snippets.sql
-- Covering index für Cockpit-Lead-Queries
CREATE INDEX CONCURRENTLY idx_leads_cockpit_covering
ON leads (owner_user_id, status, updated_at)           -- User-based indexing
INCLUDE (id, customer_id, roi_potential, created_at);   -- Covering-data

-- Customer-search performance-index
CREATE INDEX CONCURRENTLY idx_customers_search_gin
ON customers USING GIN (to_tsvector('german', name || ' ' || coalesce(description, '')));

-- Activity-timeline performance
CREATE INDEX CONCURRENTLY idx_activities_timeline
ON activities (lead_id, created_at DESC)               -- Timeline-optimized
WHERE deleted_at IS NULL;                              -- Filter deleted
```

**Seek-Pagination (Performance-Optimized):**
```java
// Seek-based pagination statt OFFSET (performance)
@Query("""
  SELECT c FROM Customer c
  WHERE (:cursor IS NULL OR c.id > :cursor)
  AND c.status = :status
  ORDER BY c.id ASC
  LIMIT :limit
""")
List<Customer> findCustomersSeekPaginated(
  @Param("cursor") UUID cursor,
  @Param("status") String status,
  @Param("limit") int limit
);
```

**Named-Parameters-Optimization:**
```java
// Named parameters für PreparedStatement-Caching
@Query(name = "Customer.findByStatusAndTerritory")
List<Customer> findByStatus(@Param("status") String status,
                           @Param("userId") UUID userId);  // User-based filtering
```

**Success Criteria:**
- Business-critical queries <50ms p95
- Covering-indices für hot-path-queries operational
- Seek-pagination für large-dataset-queries implemented

### **Phase 5: Advanced Performance-Monitoring (Tag 8)**
**Goal:** Micrometer + PromQL-gates für continuous performance-governance

**Micrometer-Performance-Metrics:**
```properties
# application.properties.snippet - External AI Ready
quarkus.micrometer.enabled=true
quarkus.micrometer.export.prometheus.enabled=true
quarkus.micrometer.binder.http-server.enabled=true
quarkus.micrometer.binder.jvm.enabled=true
```

**Custom Performance-Metrics:**
```java
// Performance-KPI-Tracking
@Timed(name = "api_response_time", description = "API response times by endpoint")
@GET @Path("/leads")
public Response getLeads() {
  Timer.Sample sample = Timer.start(meterRegistry);
  try {
    // Business logic
  } finally {
    sample.stop(Timer.builder("business_operation_time")
      .tag("operation", "lead_list")
      .tag("user_type", "field_sales")
      .register(meterRegistry));
  }
}
```

**PromQL Performance-Gates:**
```promql
# promql-gates.promql - CI-Integration ready
# API p95 < 100ms gate
histogram_quantile(0.95,
  sum(rate(http_server_requests_seconds_bucket{route=~"/api/(customers|leads)"}[5m]))
  by (le,route)) < 0.1

# Database query performance
histogram_quantile(0.95,
  sum(rate(database_query_duration_seconds_bucket[5m]))
  by (le)) < 0.05
```

**Success Criteria:**
- Performance-metrics für Business-KPI-tracking operational
- PromQL-gates für CI/CD performance-regression-prevention
- Real-time performance-monitoring dashboards functional

## ✅ Validation & Success Metrics

### **API-Performance-Targets (CI-Enforced):**
- **Business-Critical APIs:** <100ms p95 (Cockpit, Leads, Customers)
- **Database-Queries:** <50ms p95 für hot-path operations
- **Cache-Hit-Rate:** >80% für ETag/Conditional GET operations
- **Seasonal-Load:** 5x capability ohne performance-degradation

### **Performance-Impact-Measurement:**
- **Field-Sales-Mobile:** Lead-Erfassung <100ms response-time
- **Dashboard-Performance:** Cockpit-refresh <200ms real-time
- **Sample-Management:** ROI-calculation + sample-request <150ms
- **Database-Connection-Efficiency:** Zero timeout-errors unter peak-load

### **Business-Value-Validation:**
- **Field-Sales-Productivity:** API-performance-correlation mit Lead-Conversion
- **Seasonal-Business-Excellence:** Zero-downtime während Weihnachts-5x-Peaks
- **Operational-Efficiency:** Database-cost-reduction durch connection-pooling
- **User-Experience:** Perceived-performance-improvement measurement

## 🔗 Related Documentation

### **External AI Excellence-Artefakte:**
- **VXXX__hot_projections.sql:** ✅ Database-schema ready für migration
- **EtagSupport.java + CustomerResource.java:** ✅ JAX-RS patterns
- **pgbouncer.ini:** ✅ Production-tuned configuration
- **query_optimization_snippets.sql:** ✅ Index + query-patterns

### **Implementation-Dependencies:**
- **Database-Migration:** V226+ für hot-projections schema
- **Infrastructure:** PgBouncer sidecar/daemon deployment
- **Monitoring:** Prometheus + Grafana für performance-tracking

### **Cross-Module-Integration:**
- **Module 01 Cockpit:** Hot-projections für dashboard-performance
- **Module 02 Leads:** Field-Sales <100ms response-time optimization
- **Module 03 Customers:** Search-performance + ETag-caching

## 🚀 Next Steps

### **Immediate Implementation (Tag 4):**
1. **Migration-Deployment:** VXXX__hot_projections.sql with get-next-migration.sh
2. **ETag-Service-Setup:** EtagSupport.java + base-pattern implementation
3. **Performance-Baseline:** Current API-response-time measurement

### **Core-Optimization (Tag 5-7):**
1. **Hot-Projections-Integration:** Cockpit + Lead-APIs conversion
2. **ETag-Implementation:** Top-3 business-critical endpoints
3. **PgBouncer-Deployment:** Transaction-pooling + connection-optimization

### **Advanced-Excellence (Tag 8):**
1. **Query-Optimization:** Covering-indices + seek-pagination
2. **Performance-Monitoring:** Micrometer + PromQL-gates setup
3. **Load-Testing:** 5x seasonal-peak capability validation

---

**🎯 STRATEGIC IMPACT:** >500ms → <100ms API-Response-Transformation etabliert FreshFoodz Backend-Performance-Excellence und garantiert 5x Seasonal-Load-Capability für Business-Growth!