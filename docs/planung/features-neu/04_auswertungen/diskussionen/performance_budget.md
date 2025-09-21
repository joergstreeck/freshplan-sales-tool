# Performance Budget (Reports V1)

## SLIs / SLOs
- API Latency P95: **< 200 ms** (all /api/reports/*)
- Dashboard FCP: **< 1.5 s**, TTI: **< 2.0 s**
- WS Disconnects: **< 2 / 10 min**
- Error Budget: **< 0.5 %** 5xx on /api/reports/*

## Benchmarks (Dataset: 50k activities / 10k customers)
- GET /api/reports/sales-summary: p50 45ms, p95 160ms
- GET /api/reports/customer-analytics?limit=100: p50 60ms, p95 180ms
- Export JSONL (100k rows): Streamed, memory < 256MB

## Prometheus (Beispiele)
- http_server_requests_seconds_bucket{route="/api/reports/*"}
- ws_reports_events_total
- reports_snapshot_recompute_seconds
- report_export_time_seconds

## Alerts
- ApiLatencyP95High: >200ms for 10m (warning), >300ms (critical)
- ReportExportSlow: p95 > 5s for 10m
- SnapshotStale: now() - rpt_sales_daily.updated_at > 15m
