---
module: "02_neukundengewinnung"
domain: "shared"
doc_type: "guideline"
status: "approved"
owner: "team/leads"
updated: "2025-09-27"
---

# 🤝 Shared – Modul 02 Neukundengewinnung

**📍 Navigation:** Home → Planung → 02 Neukundengewinnung → Shared

**Zweck:** Kanonische Contracts und Cross-cutting Artefakte (einmalige Quelle, keine Duplikate)

## 🎯 Executive Summary

Zentrale Verträge und Standards, die von Frontend und Backend gemeinsam genutzt werden. Verweist auf kanonische Quellen, vermeidet Duplikation.

## 📋 **Kanonische Contracts (Links)**

### **Event-System (Single Source of Truth):**
- **Event-Envelope v2:** Siehe [API_CONTRACT.md](../frontend/analyse/API_CONTRACT.md#event-system)
- **PostgreSQL Channels:** `dashboard_updates`, `cross_module_events`
- **Event-Types:** `dashboard.lead_status_changed`, `dashboard.followup_completed`
- **Architecture Decision:** [ADR-0002](../../../adr/ADR-0002-listen-notify-over-eventbus.md)

### **RBAC-Matrix (Rollen & Sichtbarkeiten):**
- **Vollständige Matrix:** Siehe [API_CONTRACT.md](../frontend/analyse/API_CONTRACT.md#rbac-security)
- **Rollen:** SALES | MANAGER | ADMIN
- **Territory-Scoping:** Deutschland/Schweiz RLS
- **Lead-Ownership:** User-basiert, kein Gebietsschutz

### **Performance-Contracts:**
- **API Response:** <200ms P95 (aktuell <7ms)
- **Bundle Size:** <200KB Frontend Target
- **Event Latency:** `listen_notify_lag_ms < 10000`
- **Coverage:** ≥80% Tests (Backend + Frontend)

## 🔧 **Shared Patterns & Standards**

### **Event-Patterns:**
```yaml
Idempotenz-Keys:
  Non-Batch: UUID.v5(leadId|followUpType|processedAt)
  Batch: UUID.v5(userId|t3Count|t7Count|minute-window)

Truncation (8KB Limit):
  Pattern: Preserve envelope, truncate data field
  Fallback: {truncated: true, reference: idempotencyKey}

AFTER_COMMIT Pattern:
  Publishers: Ja (Transaction-safe)
  Listeners: Nein (außerhalb TX, Caffeine Cache)
```

### **Security-Patterns:**
```yaml
Territory RLS:
  Scope: Deutschland/Schweiz Datenraum
  Implementation: Row-Level-Security + current_setting('app.territory_id')

RBAC Enforcement:
  Backend: @PreAuthorize Annotations
  Frontend: useAuth() Hook + Role-Checks
  Test-Bypass: freshplan.security.allow-unauthenticated-publisher
```

## 📊 **Metrics & Monitoring (Prometheus)**

### **Event-Metrics (kanonisch):**
```yaml
Counter (ohne _total suffix):
  freshplan_events_published{event_type,module,result}
  freshplan_events_consumed{event_type,module,result}

Histogramme:
  freshplan_event_latency{event_type,path}

Gauges:
  freshplan_dedupe_cache_entries
  freshplan_dedupe_cache_hit_rate
```

### **SLO-Targets:**
- **API P95:** <200ms normal, <500ms peak
- **Event-Lag:** <10s (freshplan_event_latency)
- **Cache-Hit-Rate:** >90% (Dedupe-Cache)
- **Availability:** >99.5% (internes Tool)

## 🎨 **Corporate Identity (FreshFoodz CI)**

### **Verbindliche Farben:**
```css
:root {
  --color-primary: #94C456;      /* FreshFoodz Grün */
  --color-secondary: #004F7B;    /* Dunkelblau */
  --color-success: #94C456;
  --color-error: #DC3545;
}
```

### **Typography (Google Fonts):**
```css
--font-headline: 'Antonio', sans-serif;  /* Bold 700 */
--font-body: 'Poppins', sans-serif;      /* Regular 400 */
```

## 🔗 **Cross-Module Dependencies**

### **Infrastruktur-Integration:**
- **Security Foundation:** [Sprint 1.2](../../../TRIGGER_SPRINT_1_2.md)
- **CQRS Foundation:** [Sprint 1.3](../../../TRIGGER_SPRINT_1_3.md)
- **Performance Foundation:** [Sprint 1.5](../../../TRIGGER_SPRINT_1_5.md)

### **Module-zu-Modul:**
```yaml
03_kundenmanagement:
  Events: lead_status_changed → customer_timeline
  API: Customer-Lead Linking

04_auswertungen:
  Events: followup_completed → analytics_pipeline
  API: Lead-Performance Metrics

05_kommunikation:
  Events: follow_up_due → email_notification
  API: Template-System Integration
```

## 📁 **Verzeichnisstruktur (künftig)**

```
shared/
├── _index.md                    # Diese Übersicht
└── contracts/                   # Kanonische Verträge (künftig)
    ├── events/                  # Event-Schemas (JSON Schema)
    ├── api/                     # OpenAPI Specifications
    ├── rbac/                    # RBAC-Matrix & Rules
    └── metrics/                 # Prometheus-Metriken Sammlung
```

**Hinweis:** Aktuell verweisen wir auf bestehende kanonische Quellen. Bei Bedarf können Contracts schrittweise hierher überführt werden (mit Redirect-READMEs).