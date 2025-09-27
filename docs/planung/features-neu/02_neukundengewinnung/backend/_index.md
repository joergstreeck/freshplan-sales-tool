---
module: "02_neukundengewinnung"
domain: "backend"
doc_type: "guideline"
status: "approved"
owner: "team/leads"
updated: "2025-09-27"
---

# 🔧 Backend – Modul 02 Neukundengewinnung

**📍 Navigation:** Home → Planung → 02 Neukundengewinnung → Backend

**Status:** ✅ PRODUCTION-READY (Sprint 2.1 + 2.1.1 complete)

## 🎯 Executive Summary

Lead-Management Backend mit PostgreSQL LISTEN/NOTIFY Event-System, Territory-Scoping und T+3/T+7 Follow-up Automation. Production-ready seit PR #111 (2025-09-26).

## 🏗️ Architecture Overview

### **Event-Driven Architecture**
- **Transport:** PostgreSQL LISTEN/NOTIFY (ADR-0002)
- **Pattern:** AFTER_COMMIT in Publishers, Caffeine Cache in Listeners
- **Channels:** `dashboard_updates`, `cross_module_events`
- **Envelope:** CloudEvents v2 (id, source, type, time, idempotencyKey, data)

### **Security Model**
- **Territory RLS:** Deutschland/Schweiz Datenraum-Trennung
- **Lead Ownership:** User-basiert, kein Gebietsschutz
- **RBAC:** MANAGER | SALES | ADMIN Roles
- **Test Bypass:** `freshplan.security.allow-unauthenticated-publisher`

## 📁 Key Components

### **Production Artefakte (verfügbar):**
- [Security Test Pattern](../artefakte/SECURITY_TEST_PATTERN.md)
- [Performance Test Pattern](../artefakte/PERFORMANCE_TEST_PATTERN.md)
- [Event System Pattern](../artefakte/EVENT_SYSTEM_PATTERN.md)

### **Core Services:**
```java
// Lead Management
LeadService              # CRUD + Territory Scoping
FollowUpService         # T+3/T+7 Automation
ActivityService         # Lead Timeline

// Event System
EventPublisher          # AFTER_COMMIT Pattern
DashboardEventListener  # Real-time Updates
MetricsCollector        # freshplan_* Prometheus Metrics
```

## 🔗 **Zentrale Referenzen**

### **Architecture Decision Records:**
- [ADR-0002: PostgreSQL LISTEN/NOTIFY](../../../adr/ADR-0002-listen-notify-over-eventbus.md)
- [ADR-0004: Territory RLS vs Lead-Ownership](../../../adr/ADR-0004-territory-rls-vs-lead-ownership.md)

### **Sprint-Dokumentation:**
- [Sprint 2.1: Backend Implementation](../../../TRIGGER_SPRINT_2_1.md)
- [Sprint 2.1.1: Hotfix Integration](../../../TRIGGER_SPRINT_2_1_1.md)
- [Sprint Delta Log](../../../SPRINT_2_1_1_DELTA_LOG.md)

### **API Specifications:**
Siehe Frontend [API_CONTRACT.md](../frontend/analyse/API_CONTRACT.md) für vollständige REST-Endpoints und Event-Schemas.

## 🚀 **Performance & Metrics**

### **Current Benchmarks:**
- **API Response:** P95 < 7ms (gemessen in PR #111)
- **Event Latency:** `freshplan_event_latency` < 10s SLO
- **Test Coverage:** 23 Tests für Event-Pipeline

### **Monitoring:**
```yaml
Metrics (ohne _total suffix):
  freshplan_events_published{event_type,module,result}
  freshplan_events_consumed{event_type,module,result}
  freshplan_dedupe_cache_entries
  freshplan_dedupe_cache_hit_rate
```

## 🧪 **Testing Strategy**

### **Test Patterns:**
- **@TestSecurity:** RBAC Role-Tests
- **TestPgNotifySender:** Event-Publishing ohne echte DB
- **Testcontainers:** Integration-Tests mit PostgreSQL
- **Idempotenz-Tests:** UUID.nameUUIDFromBytes Validierung

### **Quality Gates:**
- ✅ 23 Tests für Event-Pipeline (PR #111)
- ✅ Performance <7ms P95
- ✅ Security Gates (Gemini Review adressiert)

## 📋 **Next Steps (Frontend Integration)**

1. **API Stabilität:** Backend-Endpoints für Frontend Thin Vertical Slice
2. **SSE/WebSocket:** Optional für Phase 2 (nach Polling v1)
3. **Cross-Module Events:** Integration mit anderen Modulen
4. **Seasonal Scaling:** KEDA-Integration für Peak-Loads