---
Title: Simple Monitoring für CQRS Light (5-50 Benutzer)
Purpose: Angemessenes Monitoring für FreshFoodz als internes Tool
Audience: Operations, DevOps
Last Updated: 2025-09-21
Status: CQRS Light Optimized
---

## 30-Second Summary
FreshFoodz als internes Tool mit 5-50 Benutzern benötigt keine komplexe Peak-Load-Infrastruktur. Basic Monitoring + PostgreSQL LISTEN/NOTIFY Events + Essential Business-KPIs sind vollkommen ausreichend.

---

## Monitoring-Scope für Interne Tools

### Was wir BRAUCHEN:
- **Basis-Verfügbarkeit:** Service up/down
- **Performance:** <200ms P95 für CQRS Light Queries
- **User-Lead-Protection:** State-Machine funktioniert
- **Database-Health:** Connections, Query-Time, Disk-Space
- **Essential Business-KPIs:** Neue Leads, Sample-Requests

### Was wir NICHT brauchen:
- War-Rooms für Seasonal Peaks
- 5x Load Pre-Provisioning
- Complex Capacity Planning
- Multi-Region Failover
- Enterprise SLAs

---

## Essential Monitoring Setup

### 1. Basic Service Health
```yaml
# Simple Prometheus Alerts
- alert: ServiceDown
  expr: up{service="freshplan-api"} == 0
  for: 5m
  annotations:
    summary: "FreshPlan API ist down"

- alert: SlowQueries
  expr: http_request_duration_seconds{quantile="0.95"} > 0.2
  for: 10m
  annotations:
    summary: "CQRS Light Queries >200ms P95"
```

### 2. PostgreSQL LISTEN/NOTIFY Events
```sql
-- Monitor Event-Backlog
CREATE VIEW v_event_monitoring AS
SELECT
  COUNT(*) FILTER (WHERE processed_at IS NULL) as unprocessed_events,
  AVG(processed_at - created_at) as avg_processing_time,
  MAX(created_at) as last_event_created
FROM domain_events
WHERE created_at > NOW() - INTERVAL '1 hour';

-- Alert wenn Backlog >100 (für 5-50 Benutzer)
```

### 3. User-Lead-Protection Monitoring
```sql
-- Essential KPIs
SELECT
  COUNT(*) as active_leads,
  COUNT(*) FILTER (WHERE protection_expires_at > NOW()) as protected_leads,
  COUNT(*) FILTER (WHERE reminder_sent_at IS NULL AND protection_expires_at < NOW() + INTERVAL '7 days') as reminders_pending
FROM leads
WHERE created_at > NOW() - INTERVAL '30 days';
```

---

## Business-Hours Operations

### Standard-Load (Mo-Fr, 8-18 Uhr):
- **Expected Users:** 5-50 concurrent
- **Expected RPS:** <10 requests/second
- **Database Connections:** 20-50
- **Response Time:** <200ms P95

### Pseudo-"Peak" Zeiten:
- **Montag Morgen:** Leicht erhöhte Last (10-20% mehr)
- **Monatsende:** Reporting-Aktivitäten
- **Seasonal:** Minimal impact für interne Tools

---

## Simplified Alert-Escalation

### Level 1: Info (Email)
- Performance leicht degradiert
- Non-critical errors

### Level 2: Warning (Slack)
- Service degradiert aber funktional
- Database near limits

### Level 3: Critical (SMS/Call)
- Service down
- Data loss risk
- Security incident

**Keine War-Rooms, keine 24/7 On-Call für interne Tools!**

---

## Resource-Guidelines für CQRS Light

### Application Server:
```yaml
# Für 5-50 Benutzer völlig ausreichend:
resources:
  requests:
    memory: "512Mi"
    cpu: "250m"
  limits:
    memory: "1Gi"
    cpu: "500m"
replicas: 2  # Basic HA
```

### PostgreSQL:
```yaml
# One-Database für CQRS Light:
- 4 CPU cores
- 8GB RAM
- 100GB SSD
- max_connections: 100
- No read-replicas needed
```

---

## Cost-Optimization

### Was wir sparen durch CQRS Light:
- **Keine Event-Bus:** -$500/Monat (Kafka/NATS)
- **Keine Read-Replicas:** -$300/Monat
- **Keine Peak-Provisioning:** -$1000/Monat
- **Simplified Monitoring:** -$200/Monat

**Total Savings: ~$2000/Monat durch CQRS Light!**

---

## Daily Operations Checklist

### Morning Check (5 Minuten):
1. ✅ Service health dashboard grün?
2. ✅ Keine critical alerts overnight?
3. ✅ Database connections <50?
4. ✅ Event-backlog <100?

### Weekly Review (30 Minuten):
1. Performance-Trends reviewen
2. Error-Logs checken
3. Business-KPIs validieren
4. Disk-Space prüfen

**Das reicht vollkommen für 5-50 interne Benutzer!**

---

**🎯 CQRS Light Operations: Simple, Cost-Efficient, Angemessen für FreshFoodz als internes Tool!**