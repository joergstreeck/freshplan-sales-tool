---
module: "02_neukundengewinnung"
domain: "backend"
doc_type: "guideline"
status: "approved"
owner: "team/leads"
updated: "2025-09-28"
---

# 🔧 Backend – Modul 02 Neukundengewinnung

**📍 Navigation:** Home → Planung → 02 Neukundengewinnung → Backend

**Status:** 🔧 IN DEVELOPMENT (Sprint 2.1.4 in progress)

> **⚠️ DATENBANK-MIGRATIONEN BEACHTEN!**
> Vor Migration-Arbeit IMMER [`/docs/planung/MIGRATIONS.md`](../../../MIGRATIONS.md) lesen!
> - Alle Migrations V1-V257 dokumentiert
> - V10xxx Test/Dev-Range erklärt (Production Skip)
> - CONCURRENTLY-Regeln für Production
> - Nächste Nummer: `./scripts/get-next-migration.sh`

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

## 📋 **Current Sprint (2.1.4)**

### **Sprint 2.1.4 – Lead Deduplication & Data Quality**
**Status:** 🔧 IN_PROGRESS (2025-09-28)

#### **Datenmodell-Erweiterungen:**
- `email_normalized` (CITEXT) - Normalisierte E-Mail
- `name_normalized` (TEXT) - Normalisierter Name
- `phone_e164` (TEXT) - E.164 Telefonnummer
- Partial UNIQUE Index auf `(tenant_id, email_normalized) WHERE email_normalized IS NOT NULL`

#### **Normalisierungsregeln:**
- **E-Mail:** `lower(trim(email))` + optional Plus-Tag-Entfernung
- **Name:** Whitespace normalisieren, Kleinbuchstaben, Diakritika entfernen
- **Telefon:** libphonenumber → E.164 Format

#### **Idempotenz-Support:**
- Header: `Idempotency-Key: <uuid|hash>`
- Store: 24h TTL mit Request-Hash-Validierung
- Semantik: Identische Response bei Wiederholung

#### **Migration:** V247 (dynamisch ermittelt)

## 📋 **Next Steps**

1. **Sprint 2.1.5:** Lead Matching & Review-Flow (Phase 2)
2. **Sprint 2.1.6:** Merge/Unmerge + Historie
3. **Cross-Module Events:** Integration mit anderen Modulen
4. **Seasonal Scaling:** KEDA-Integration für Peak-Loads