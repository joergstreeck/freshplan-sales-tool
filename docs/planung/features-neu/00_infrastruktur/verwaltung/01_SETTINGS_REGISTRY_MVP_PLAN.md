# Settings Registry MVP - Implementation Plan

**📊 Plan Status:** 🟢 Ready for Implementation
**🎯 Owner:** Infrastructure Team
**⏱️ Timeline:** Q4 2025 (4-6 Wochen)
**🔧 Effort:** M (Medium)

## 🎯 Executive Summary (für Claude)

**Mission:** Enterprise-Grade Settings Registry mit <50ms SLO als Foundation für Module 06 + 07
**Problem:** Module blockiert durch fehlende zentrale Settings-Infrastructure, @ConfigProperty nicht skalierbar
**Solution:** Hybrid Architecture (Meta + JSONB) mit RLS Security + L1 Cache + LISTEN/NOTIFY
**Timeline:** 4-6 Wochen Sprint-ready Implementation mit Copy-Paste Code
**Impact:** Unblocking kritischer Module + Settings-as-a-Service für gesamte Platform

**Quick Context:** Production-ready Settings-MVP Pack (9.7/10) von External AI liefert komplette Implementation mit SQL Schema, Java Services, REST API und Zero-Risk Migration Strategy.

## 📋 Context & Dependencies

### Current State:
- Settings via @ConfigProperty verstreut über alle Module
- Keine zentrale Governance oder Scope-Hierarchie
- Module 06 (Einstellungen) + 07 (Hilfe) blockiert
- Keine Type Safety oder Validation
- Performance unkontrolliert (keine SLOs)

### Target State:
- Hybrid Settings Registry (Meta in Tabellen + Values in JSONB)
- <50ms SLO für GET /api/settings/effective
- 5-Level Scope-Hierarchie (Global → Tenant → Org → User)
- RLS + ABAC Security mit JWT Integration
- Zero-Risk Migration via Strangler Pattern

### Dependencies:
- ✅ [PostgreSQL Database](../../database/) - Ready
- ✅ [Keycloak OIDC](../../sicherheit/) - JWT Claims verfügbar
- ✅ [Quarkus Backend](../../../../backend/) - CDI + REST ready
- 🔄 [Module 06](../../../06_einstellungen/) - BLOCKED, wartet auf Settings Registry

## 🛠️ Implementation Phases

### Phase 1: Database Foundation (Week 1)
**Goal:** Settings Registry Schema mit Security deployed

**Actions:**
- [ ] Deploy settings_registry_core.sql Migration
- [ ] Setup RLS Policies (fail-closed)
- [ ] Seed lead.protection.*, ai.*, credit.*, help.* settings
- [ ] Test Database Security mit verschiedenen JWT Claims

**Code Changes:**
```sql
-- Migration: V_XXX__settings_registry_core.sql
CREATE TABLE settings_registry (
  key             text PRIMARY KEY,
  type            text CHECK (type IN ('scalar','list','object')),
  scope           jsonb NOT NULL,
  schema          jsonb,
  default_value   jsonb,
  merge_strategy  text CHECK (merge_strategy IN ('replace','merge','append'))
);

-- RLS Security
ALTER TABLE settings_registry ENABLE ROW LEVEL SECURITY;
CREATE POLICY rls_registry_read ON settings_registry USING (true);
```

**Success Criteria:**
- Database Migration successful in all environments
- RLS Policies block unauthorized access
- Seeds für alle SoT-Document Settings verfügbar

### Phase 2: Core Services (Week 2)
**Goal:** Java Services mit <50ms SLO operational

**Actions:**
- [ ] Implement SettingsService mit L1-Cache + Merge Logic
- [ ] Implement SettingsResource mit ETag Support
- [ ] Setup LISTEN/NOTIFY Cache Invalidation
- [ ] JSON Schema Validation serverseitig

**Code Changes:**
```java
@ApplicationScoped
public class SettingsService {
    private final Map<String, CacheEntry> l1 = new ConcurrentHashMap<>();

    public EffectiveResult getEffective(SettingsPrincipal p, Set<String> keys) {
        // L1 Cache check + Database query + Merge chain
        return new EffectiveResult(etag, effectiveValues);
    }
}
```

**Success Criteria:**
- Unit Tests >80% coverage
- Performance Tests <50ms p95
- Cache invalidation <100ms nach NOTIFY

### Phase 3: API Integration (Week 3)
**Goal:** REST API Production-ready mit Module 06 Integration

**Actions:**
- [ ] Complete REST API mit OpenAPI Documentation
- [ ] Implement RegistryOrConfig Migration Adapter
- [ ] Module 06 Integration als Pilot
- [ ] Monitoring + Grafana Dashboards

**Code Changes:**
```java
@Path("/api/settings")
public class SettingsResource {
    @GET @Path("/effective")
    public Response getEffective(@QueryParam("keys") String keys,
                                @HeaderParam("If-None-Match") String etag) {
        // ETag support für HTTP Caching
    }
}
```

**Success Criteria:**
- API functional mit ETag Support
- Module 06 erfolgreich migriert
- Telemetry zeigt Registry vs Config Hits

### Phase 4: Production Hardening (Week 4-6)
**Goal:** Enterprise-ready Deployment mit SLO Compliance

**Actions:**
- [ ] Load Testing für <50ms SLO validation
- [ ] Security Testing (RLS + ABAC validation)
- [ ] Module 02+03 Migration (Territory Settings)
- [ ] Operational Excellence (Alerting + Runbooks)

**Success Criteria:**
- k6 Load Tests bestehen <50ms SLO
- Security Tests validieren RLS enforcement
- >2 Module erfolgreich migriert
- SLO-based Alerting operational

## ✅ Success Metrics

### Technical KPIs:
- Settings GET /api/settings/effective p95 <50ms
- Cache Hit Rate >80%
- ETag Hit Rate >60%
- Migration Progress >95% Registry hits (vs Config fallback)
- JSON Schema Validation 100% server-side

### Business KPIs:
- Module 06 unblocked + development enabled
- Module 07 preparation ready (AI settings available)
- Settings-as-a-Service für 8 Module operational
- Zero production incidents during migration

### Quality Gates:
- Unit Tests >80% coverage für alle Services
- Integration Tests mit real database + security
- Load Tests demonstrating SLO compliance
- Security Tests validating ABAC + RLS

## 🔗 Related Documentation

**Foundation:**
- → [Settings Registry SoT](./artefakte/SETTINGS_REGISTRY_COMPLETE.md)
- → [Production-Ready Artefakte](./artefakte/) - Copy-Paste Implementation
- → [Grundlagen Integration](./analyse/03_GRUNDLAGEN_INTEGRATION_GOVERNANCE.md)

**Cross-Module Integration:**
- → [Module 06 Einstellungen](../../../06_einstellungen/) - Primary Integration
- → [Module 07 Hilfe](../../../07_hilfe_support/) - AI Settings Consumer
- → [Security Guidelines](../../../../grundlagen/SECURITY_GUIDELINES.md)

**Implementation Reference:**
- → [Critical Quality Assessment](./diskussionen/2025-09-21_SETTINGS_MVP_PACK_KRITISCHE_BEWERTUNG.md)
- → [External AI Strategy Discussion](./diskussionen/2025-09-20_GOVERNANCE_STRATEGIEDISKUSSION_VORBEREITUNG.md)

## 🤖 Claude Handover Section

**Context:** Settings Registry MVP als kritischer Pfad für Module 06 + 07. Production-ready Implementation (9.7/10) verfügbar als Copy-Paste Code. Hybrid Architecture (Option C) nach Strategic Discussion mit External AI entwickelt.

**Implementation Status:** Complete Artefakte in `/artefakte/` - SQL Schema, Java Services, REST API, Migration Adapter alle ready. Grundlagen-Integration shows Business Logic Standards ab 01.10.2025 require Settings Registry als Policy Store.

**Next Actions:** Deploy Phase 1 (Database Foundation) sofort möglich. Module 06 als Pilot für Migration Strategy. Load Testing + SLO validation kritisch für Enterprise-readiness.

**Critical Dependencies:** Module 06 BLOCKED until Settings Registry deployed. Module 07 CAR-Strategy requires ai.* settings für Nudge Budgets. Timeline-critical für Q4 2025 Module-development.