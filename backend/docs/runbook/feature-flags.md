# Runbook – CQRS Feature Flags

## 🎛️ Feature Flags Übersicht

### Verfügbare Flags
- **`features.cqrs.enabled`** (bool, default: `true`)
  - Aktiviert CQRS global für Detail, Dashboard und Commands
  - Steuert NICHT die Kundenliste (siehe separates Flag)
  
- **`features.cqrs.customers.list.enabled`** (bool, default: `false`)
  - Steuert ausschließlich GET /api/customers (Liste)
  - Unabhängig vom globalen Flag steuerbar

## 🚀 Standard-Setup (Production)

```properties
# application.properties oder ENV-Variablen
features.cqrs.enabled=true
features.cqrs.customers.list.enabled=false
```

## 🔄 Umschalten Liste → CQRS

### Voraussetzungen
- ✅ Performance-Tests erfolgreich (p95 ≤ 35ms, p99 ≤ 60ms)
- ✅ Shadow-Mode Vergleich zeigt Datengleichheit
- ✅ Monitoring & Alerts aktiv

### Durchführung
1. **Flag aktivieren**
   ```bash
   # Via Environment Variable
   export FEATURES_CQRS_CUSTOMERS_LIST_ENABLED=true
   
   # Oder in application.properties
   features.cqrs.customers.list.enabled=true
   ```

2. **Monitoring beobachten (30-60 Min)**
   - Dashboard: Grafana → CQRS Performance
   - Metriken prüfen:
     - p95/p99 Latency der Liste
     - Error Rate (sollte 0 bleiben)
     - DB Load (Slow Queries)
   
3. **Bei Auffälligkeiten: Sofort-Rollback**
   ```bash
   export FEATURES_CQRS_CUSTOMERS_LIST_ENABLED=false
   # Rollback erfolgt < 1 Minute
   ```

## 🔙 Rollback-Optionen

### Option 1: Nur Liste zurück auf Legacy
```properties
features.cqrs.customers.list.enabled=false
```
- Betrifft NUR GET /api/customers
- Andere Endpoints bleiben auf CQRS

### Option 2: Kompletter Rollback zu Legacy
```properties
features.cqrs.enabled=false
features.cqrs.customers.list.enabled=false
```
- ALLE Endpoints zurück auf Legacy
- Notfall-Option bei kritischen Problemen

## 📊 Observability

### Request-Tagging
Jeder Request wird automatisch getaggt:
- `mode=legacy|cqrs` - Welcher Service wurde genutzt
- `path=list|detail|dashboard|command` - Welcher Endpoint
- `duration_ms` - Response-Zeit in Millisekunden

### Monitoring-Queries

#### Prometheus
```promql
# p95 Latency nach Mode
histogram_quantile(0.95, 
  sum(rate(http_server_requests_seconds_bucket{uri="/api/customers"}[5m])) 
  by (le,mode)
)

# Error Rate
sum(rate(http_server_requests_seconds_count{status=~"5.."}[5m])) 
/ sum(rate(http_server_requests_seconds_count[5m]))
```

#### PostgreSQL Performance
```sql
-- Top langsame Queries
SELECT query, calls, mean_exec_time, rows
FROM pg_stat_statements
WHERE query LIKE '%customers%'
ORDER BY mean_exec_time DESC
LIMIT 10;

-- Index-Nutzung prüfen
SELECT indexrelname, idx_scan, idx_tup_read
FROM pg_stat_user_indexes
WHERE relname = 'customers'
ORDER BY idx_scan DESC;
```

## ⚠️ Schwellwerte & Alerts

### Performance-Schwellwerte
| Metrik | Warnung | Kritisch | Aktion |
|--------|---------|----------|--------|
| p95 Liste | > 35ms | > 50ms | Review & ggf. Rollback |
| p99 Liste | > 60ms | > 100ms | Sofort Rollback |
| Error Rate | > 0.1% | > 1% | Sofort Rollback |
| DB Slow Queries | > 5/min | > 20/min | Review Queries |

### Alert-Beispiele
- **ApiLatencyP95ListHigh**: Liste p95 > 35ms für 10 Min
- **ApiLatencyP99ListHigh**: Liste p99 > 60ms für 10 Min
- **ApiErrorRate**: HTTP 5xx > 0.1% für 5 Min

## 📋 Checkliste für Production-Umschaltung

### Vor der Umschaltung
- [ ] Performance-Baseline dokumentiert
- [ ] Shadow-Mode läuft seit min. 24h
- [ ] Keine Daten-Diskrepanzen im Shadow-Log
- [ ] Team-Mitglieder informiert
- [ ] Rollback-Plan kommuniziert

### Während der Umschaltung
- [ ] Flag aktiviert
- [ ] Monitoring-Dashboard offen
- [ ] Erste 5 Min: Intensive Beobachtung
- [ ] Nach 30 Min: Status-Check
- [ ] Nach 60 Min: Go/No-Go Entscheidung

### Nach erfolgreicher Umschaltung
- [ ] Success-Meldung an Team
- [ ] Metriken für 24h beobachten
- [ ] Performance-Report erstellen
- [ ] Legacy-Code Removal planen (Q2/2025)

## 🆘 Troubleshooting

### Problem: Hohe Latency nach Umschaltung
1. Prüfe aktive DB-Queries: `SELECT * FROM pg_stat_activity WHERE state = 'active';`
2. Check Index-Nutzung (siehe oben)
3. Rollback wenn > 60ms p99

### Problem: Unterschiedliche Ergebnisse Legacy vs CQRS
1. Shadow-Logs prüfen: `grep "DIFF" /var/log/app/shadow.log`
2. Rollback zu Legacy
3. Bug-Ticket erstellen mit Diff-Details

### Problem: Memory/CPU Anstieg
1. Heap-Dump erstellen: `jcmd <pid> GC.heap_dump /tmp/heap.hprof`
2. Thread-Dump: `jcmd <pid> Thread.print`
3. Rollback wenn > 80% CPU/Memory

## 📞 Kontakte & Eskalation

- **On-Call Engineer**: Siehe PagerDuty
- **Team Lead**: Via Slack #freshplan-backend
- **Rollback-Entscheidung**: Team Lead oder On-Call

## 📚 Weiterführende Dokumentation

- [CQRS Architecture Decision](../adr/ADR-013-cqrs-pattern.md)
- [Performance Test Results](../performance/cqrs-baseline.md)
- [Shadow Mode Implementation](../guides/shadow-mode.md)