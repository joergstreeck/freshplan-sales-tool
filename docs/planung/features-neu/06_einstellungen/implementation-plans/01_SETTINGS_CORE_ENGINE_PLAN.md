# ⚙️ Settings Core Engine Implementation Plan

**📊 Plan Status:** 🟢 Production-Ready
**🎯 Owner:** Backend Team + Settings-Architecture Team
**⏱️ Timeline:** Woche 1-2 (6-8h Implementation)
**🔧 Effort:** M (Medium - Scope-Hierarchie + Merge-Engine + Cache)

## 🎯 Executive Summary (für Claude)

**Mission:** Hochperformante Settings Core Engine mit 7-Level Scope-Hierarchie und <50ms SLO für Enterprise-Scale

**Problem:** FreshFoodz B2B-Food-Platform benötigt komplexe Settings-Vererbung (GLOBAL→TENANT→TERRITORY→ACCOUNT→CONTACT_ROLE→CONTACT→USER) mit Merge-Engine und L1-Cache

**Solution:** Settings Core Engine mit Scope-Hierarchie + Merge-Strategien + LISTEN/NOTIFY Cache + ETag-Optimierung + ABAC-Security

**Timeline:** 6-8h von Database-Schema bis Production-Deployment mit <50ms Performance-SLO

**Impact:** Enterprise-Grade Settings-Foundation für alle FreshPlan-Module + Territory-Management + Multi-Contact-Rollen

## 📋 Context & Dependencies

### Current State:
- ✅ **PostgreSQL Infrastructure:** Database + JSONB + RLS ready
- ✅ **ABAC Security Framework:** Territory-basierte Zugriffskontrolle operational
- ✅ **Settings Registry Schema:** JSON-Schema-Validation ready
- ✅ **Performance Requirements:** <50ms SLO für Settings-API definiert

### Target State:
- 🎯 **7-Level Scope-Hierarchie:** GLOBAL→TENANT→TERRITORY→ACCOUNT→CONTACT_ROLE→CONTACT→USER
- 🎯 **Merge-Engine:** Konflikt-Resolution + Override-Strategien + Default-Fallbacks
- 🎯 **L1 Cache:** Memory-Cache + LISTEN/NOTIFY + ETag-Caching
- 🎯 **Performance:** <50ms API-Response-Time + Intelligent Cache-Invalidation
- 🎯 **Enterprise-Security:** ABAC-Integration + Audit-Logging + JSON-Schema-Validation

### Dependencies:
- **Database-Infrastructure:** PostgreSQL + JSONB + RLS (READY)
- **Security-Framework:** ABAC + Territory-Management (READY)
- **Performance-Monitoring:** Prometheus + Grafana (READY)
- **JSON-Schema-Registry:** settings_registry_keys.json (READY)

## 🛠️ Implementation Phases (3 Phasen = 6-8h Gesamt)

### Phase 1: Database-Schema + Scope-Hierarchie (2-3h)

**Goal:** Settings-Database mit 7-Level Scope-Hierarchie und optimierten Indices

**Actions:**
1. **Database-Schema:**
   ```sql
   CREATE TABLE settings (
       id BIGSERIAL PRIMARY KEY,
       scope_type scope_type_enum NOT NULL,
       scope_id VARCHAR(255) NOT NULL,
       key VARCHAR(255) NOT NULL,
       value JSONB NOT NULL,
       created_at TIMESTAMP DEFAULT NOW(),
       updated_at TIMESTAMP DEFAULT NOW(),
       etag VARCHAR(255) GENERATED ALWAYS AS (md5(value::text)) STORED,
       UNIQUE(scope_type, scope_id, key)
   );

   CREATE TYPE scope_type_enum AS ENUM (
       'GLOBAL', 'TENANT', 'TERRITORY', 'ACCOUNT',
       'CONTACT_ROLE', 'CONTACT', 'USER'
   );
   ```

2. **Performance-Indices:**
   ```sql
   CREATE INDEX idx_settings_lookup ON settings(scope_type, scope_id, key);
   CREATE INDEX idx_settings_key_hierarchy ON settings(key, scope_type);
   CREATE INDEX idx_settings_updated_at ON settings(updated_at);
   CREATE INDEX idx_settings_etag ON settings(etag);
   ```

3. **RLS Security-Policies:**
   ```sql
   CREATE POLICY settings_abac_policy ON settings
   USING (auth.has_territory_access(scope_id) OR scope_type = 'GLOBAL');
   ```

**Success Criteria:** Database-Schema operational + Performance-Tests <10ms Single-Setting-Lookup

### Phase 2: Merge-Engine + Core-Services (2-3h)

**Goal:** Settings-Merge-Engine mit Scope-Hierarchie-Resolution und Core-API-Services

**Actions:**
1. **Settings-Merge-Engine:**
   ```java
   @Service
   public class SettingsMergeEngine {
       private static final List<ScopeType> MERGE_ORDER = List.of(
           GLOBAL, TENANT, TERRITORY, ACCOUNT, CONTACT_ROLE, CONTACT, USER
       );

       public JsonNode mergeSettings(String key, List<ScopeContext> scopes) {
           Map<ScopeType, JsonNode> scopeValues = loadScopeValues(key, scopes);

           JsonNode result = scopeValues.get(GLOBAL);
           for (ScopeType scope : MERGE_ORDER) {
               if (scopeValues.containsKey(scope)) {
                   result = deepMerge(result, scopeValues.get(scope));
               }
           }
           return result;
       }
   }
   ```

2. **Settings-Repository:**
   ```java
   @Repository
   public class SettingsRepository {
       public List<Setting> findByKeyAndScopes(String key, List<ScopeContext> scopes) {
           // ABAC-sichere Query mit Territory-Filtering
           String sql = """
               SELECT * FROM settings
               WHERE key = ? AND (scope_type, scope_id) IN (
                   SELECT scope_type, scope_id FROM unnest(?, ?) AS s(scope_type, scope_id)
               ) AND auth.has_territory_access(scope_id)
               ORDER BY CASE scope_type
                   WHEN 'GLOBAL' THEN 1 WHEN 'TENANT' THEN 2
                   WHEN 'TERRITORY' THEN 3 WHEN 'ACCOUNT' THEN 4
                   WHEN 'CONTACT_ROLE' THEN 5 WHEN 'CONTACT' THEN 6
                   WHEN 'USER' THEN 7 END
               """;
           return jdbcTemplate.query(sql, settingRowMapper, key, scopeTypes, scopeIds);
       }
   }
   ```

3. **Settings-Service:**
   ```java
   @Service
   public class SettingsService {
       @Cacheable(value = "settings", key = "#key + '_' + #scopes.hashCode()")
       public JsonNode getSetting(String key, List<ScopeContext> scopes) {
           validateAccess(scopes);
           List<Setting> scopeSettings = settingsRepository.findByKeyAndScopes(key, scopes);
           return settingsMergeEngine.mergeSettings(key, scopeSettings);
       }

       @CacheEvict(value = "settings", key = "#key + '_*'")
       public Setting updateSetting(String key, ScopeContext scope, JsonNode value) {
           validateSchemaCompliance(key, value);
           validateABACAccess(scope);

           Setting setting = settingsRepository.save(new Setting(key, scope, value));
           auditService.logSettingChange(setting);
           return setting;
       }
   }
   ```

**Success Criteria:** Merge-Engine operational + Settings-API functional + Cache-Invalidation working

### Phase 3: Cache-Layer + Performance-Optimization (2h)

**Goal:** L1 Memory-Cache mit LISTEN/NOTIFY + ETag-Caching + <50ms Performance-SLO

**Actions:**
1. **L1 Memory-Cache:**
   ```java
   @Component
   public class SettingsCache {
       private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();

       @EventListener
       public void handleSettingChanged(SettingChangedEvent event) {
           String keyPattern = event.getKey() + "_*";
           cache.entrySet().removeIf(entry -> entry.getKey().startsWith(keyPattern));

           // LISTEN/NOTIFY PostgreSQL Integration
           notificationService.notifyClusterNodes("settings_invalidated", keyPattern);
       }

       public Optional<JsonNode> getCachedSetting(String cacheKey) {
           CacheEntry entry = cache.get(cacheKey);
           return entry != null && !entry.isExpired() ?
               Optional.of(entry.getValue()) : Optional.empty();
       }
   }
   ```

2. **Settings-Resource mit ETag:**
   ```java
   @RestController
   @RequestMapping("/api/settings")
   public class SettingsResource {
       @GetMapping("/{key}")
       public ResponseEntity<JsonNode> getSetting(
           @PathVariable String key,
           @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch
       ) {
           List<ScopeContext> scopes = securityContext.getCurrentScopes();
           JsonNode setting = settingsService.getSetting(key, scopes);

           String etag = calculateETag(setting);
           if (etag.equals(ifNoneMatch)) {
               return ResponseEntity.notModified().build();
           }

           return ResponseEntity.ok()
               .eTag(etag)
               .cacheControl(CacheControl.maxAge(Duration.ofMinutes(5)))
               .body(setting);
       }
   }
   ```

3. **Performance-Monitoring:**
   ```java
   @Component
   public class SettingsMetrics {
       private final Timer settingsGetTimer = Timer.builder("settings_get_duration_seconds")
           .register(meterRegistry);
       private final Counter cacheHitCounter = Counter.builder("settings_cache_hits_total")
           .register(meterRegistry);
       private final Gauge cacheSize = Gauge.builder("settings_cache_size")
           .register(meterRegistry, this, SettingsMetrics::getCacheSize);
   }
   ```

**Success Criteria:** <50ms P95 Settings-API + >80% Cache-Hit-Rate + ETag-Caching functional

## ✅ Success Metrics

### **Immediate Success (6-8h):**
1. **Settings-API:** 7-Level Scope-Hierarchie mit Merge-Engine operational
2. **Performance:** <50ms P95 Response-Time für Settings-API erreicht
3. **Cache-Layer:** L1 Memory-Cache + LISTEN/NOTIFY + >80% Hit-Rate
4. **Security:** ABAC-Integration + Territory-Filtering + Audit-Logging
5. **Schema-Validation:** JSON-Schema-Registry + Runtime-Validation active

### **Business Success (1-2 Wochen):**
1. **Territory-Management:** Deutschland/Schweiz Settings-Hierarchie operational
2. **Multi-Contact-Rollen:** CHEF/BUYER spezifische Settings functional
3. **Performance-Excellence:** <50ms SLO maintained unter Production-Load
4. **Foundation-Ready:** Settings Core für alle FreshPlan-Module verfügbar

### **Technical Excellence:**
- **Merge-Performance:** 7-Level Hierarchie-Resolution <10ms
- **Cache-Efficiency:** >80% Hit-Rate + Intelligent Invalidation
- **Database-Performance:** Optimierte Indices + Query-Performance <5ms
- **Security-Compliance:** 100% ABAC-Territory-Filtering + Audit-Trail

## 🔗 Related Documentation

### **Integration Foundation:**
- [B2B-Food Business Logic Plan](02_B2B_FOOD_BUSINESS_LOGIC_PLAN.md) - Multi-Contact + Territory-Rules
- [Frontend UX-Excellence Plan](03_FRONTEND_UX_EXCELLENCE_PLAN.md) - React-Integration + TypeScript
- [Monitoring Operations Plan](04_MONITORING_OPERATIONS_PLAN.md) - Performance-SLOs + Grafana

### **Settings-Core Artifacts:**
- [Database Schema](../artefakte/database/settings_schema.sql) - Complete PostgreSQL Setup
- [Settings Service](../artefakte/backend/SettingsService.java) - Core Implementation
- [Performance Tests](../artefakte/performance/settings_perf.js) - k6 Load Tests

### **Cross-Module Integration:**
- [ABAC Security Framework](../../grundlagen/SECURITY_GUIDELINES.md) - Territory-Management
- [Performance Standards](../../grundlagen/PERFORMANCE_STANDARDS.md) - <50ms SLO Requirements

## 🤖 Claude Handover Section

### **Current Priority (für neue Claude):**
```bash
# Phase 1: Database-Schema + Scope-Hierarchie
cd implementation-plans/
→ 01_SETTINGS_CORE_ENGINE_PLAN.md (CURRENT)

# Start: Database Schema deployment + Scope-Hierarchie
cd ../artefakte/database/

# Success: 7-Level Scope-Hierarchie + Merge-Engine operational
# Next: B2B-Food Business Logic Integration
```

### **Context für neue Claude:**
- **Settings Core Engine:** 7-Level Scope-Hierarchie mit Merge-Engine + L1-Cache
- **Timeline:** 6-8h von Database-Schema bis <50ms Performance-SLO
- **Dependencies:** PostgreSQL + ABAC Security + JSON-Schema-Registry ready
- **Business-Value:** Foundation für Territory-Management + Multi-Contact-Rollen

### **Key Success-Factors:**
- **Performance-SLO:** <50ms API-Response-Time kritisch für User-Experience
- **Cache-Strategy:** L1 Memory-Cache + LISTEN/NOTIFY für Cluster-Invalidation
- **ABAC-Integration:** Territory-basierte Security in allen Settings-Queries
- **Merge-Engine:** 7-Level Hierarchie-Resolution mit Konflikt-Strategies

**🚀 Ready für hochperformante Settings Core Engine Implementation!**