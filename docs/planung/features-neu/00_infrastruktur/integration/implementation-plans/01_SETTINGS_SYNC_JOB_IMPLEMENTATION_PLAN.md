# 🔄 Settings-Sync-Job Implementation Plan

**📊 Plan Status:** 🟢 Production-Ready (External AI Validated 9.9/10)
**🎯 Owner:** Integration Team + Infrastructure
**⏱️ Timeline:** Tag 1-2 (6-8 Stunden aktive Implementierung)
**🔧 Effort:** M (Medium - fokussierte Implementation mit klaren Erfolgs-Kriterien)

## 🎯 Executive Summary (für Claude)

**Mission:** Production-Ready Settings-Sync-Job implementieren der dynamisch Gateway-Policies aus Settings-Registry generiert

**Problem:** Kong + Envoy Gateways benötigen dynamische Policy-Updates basierend auf Tenant/Org-spezifischen Settings (Rate-Limits, OIDC-Parameter, Multi-Tenancy-Rules) - manuelle Konfiguration ist fehleranfällig und skaliert nicht

**Solution:** Quarkus-basierter Sync-Service der Settings-Registry abfragt, Kong decK + Envoy-Configs generiert, und Policies automatisch deployt mit kompletter Audit-Trail und Rollback-Fähigkeiten

**Timeline:** 6-8 Stunden von Build-Test bis Production-Deployment mit umfassender Validierung

**Impact:** Alle 8 Module können sofort Tenant/Org-spezifische Gateway-Policies nutzen + Foundation für Event-Bus-Migration vorbereitet

## 📋 Context & Dependencies

### Current State:
- ✅ **Settings-Registry API:** Produktiv mit Tenant/Org/User-Scopes + JSON-Schema-Validation
- ✅ **Kong Infrastructure:** Available für Policy-Bundle-Deployment via decK CLI
- ✅ **Envoy Infrastructure:** Ready für Configuration-Updates via Admin-API
- ✅ **Java-Code:** SyncJob.java + KongDeckGenerator.java + AuditLog.java (External AI erstellt)
- ✅ **Maven Build:** pom.xml mit Quarkus + PostgreSQL + Kong-decK Dependencies

### Target State:
- 🎯 **Automated Sync:** Settings-Registry → Gateway-Policies alle 5 Minuten + Manual-Trigger
- 🎯 **Multi-Gateway Support:** Kong + Envoy Policies parallel generiert und deployed
- 🎯 **Audit Trail:** Vollständiges Logging aller Policy-Changes mit Tenant/User-Attribution
- 🎯 **Error Recovery:** Automatic Retry + Manual-Rollback bei fehlgeschlagenen Deployments
- 🎯 **Monitoring:** Prometheus-Metrics für Sync-Success/Failure/Duration

### Dependencies:
- **Settings-Registry:** /api/settings/effective?scope=gateway.* (READY)
- **Kong decK CLI:** Version 1.28+ für Policy-Bundle-Deployment (READY)
- **Envoy Admin API:** /config_dump + /clusters Endpoints (READY)
- **PostgreSQL:** Audit-Trail-Tables + Connection-Pool (READY)
- **Quarkus Runtime:** 3.2+ mit Scheduler + REST-Client Extensions (READY)

## 🛠️ Implementation Phases (3 Phasen = 6-8 Stunden Gesamt)

### Phase 1: Build + Unit-Tests + Local-Validation (2-3 Stunden)

**Goal:** Settings-Sync-Job lokal kompiliert, alle Unit-Tests grün, Mock-Integration funktional

**Actions:**
1. **Maven Build + Dependencies-Validation:**
   ```bash
   cd artefakte/src/
   mvn clean compile
   mvn dependency:tree | grep -E "(kong|quarkus|postgresql)"
   ```
   - **Success Criteria:** Build erfolgreich, alle Dependencies resolved
   - **Error-Handling:** Missing Dependencies → pom.xml Updates mit korrekten Versionen

2. **Unit-Tests Execution + Coverage-Analysis:**
   ```bash
   mvn test
   mvn jacoco:report
   open target/site/jacoco/index.html
   ```
   - **Success Criteria:** ≥85% Coverage für SyncJob + KongDeckGenerator + AuditLog
   - **Tests:** Settings-Registry-Mock, Kong-Policy-Generation, Error-Scenarios
   - **Edge Cases:** Leere Settings, malformed JSON, Network-Timeouts getestet

3. **Local Mock-Integration Test:**
   ```bash
   ./run-local.sh
   # Prüft: Mock Settings-Registry → Kong-Config-Generation → Dry-Run decK
   ```
   - **Mock Settings:** 3 Tenants mit unterschiedlichen Rate-Limits + OIDC-Settings
   - **Generated Policies:** Kong YAML + Envoy JSON korrekt formatiert
   - **Validation:** decK sync --dry-run ohne Fehler
   - **Success Criteria:** End-to-End Mock-Flow funktional

**Timeline:** Tag 1 (2-3 Stunden)
**Rollback-Plan:** Git-Reset auf vorherigen Working-State bei kritischen Build-Fehlern

### Phase 2: Production-Integration + Gateway-Deployment (2-3 Stunden)

**Goal:** Settings-Sync-Job gegen echte Settings-Registry + Kong/Envoy Production-Deployment

**Actions:**
1. **Production Settings-Registry Integration:**
   ```bash
   # Settings-Registry API-Test
   curl -H "Authorization: Bearer $TOKEN" \
        "https://api.freshplan.com/api/settings/effective?scope=gateway.*"

   # SyncJob Configuration Update
   vi application.properties
   # settings.registry.url=https://api.freshplan.com
   # kong.deck.url=https://kong-admin.freshplan.com
   # envoy.admin.url=https://envoy-admin.freshplan.com
   ```
   - **Success Criteria:** Live Settings-Registry-Daten erfolgreich abgerufen
   - **Validation:** JSON-Schema-Compliance für alle Gateway-Settings
   - **Error-Handling:** Connection-Timeouts → Exponential-Backoff-Retry

2. **Kong Policy-Bundle Production-Deployment:**
   ```bash
   # Generated Kong-Policies Deploy
   java -jar sync-job.jar --mode=kong --dry-run=false

   # Validation
   deck diff -s kong-policy-bundle.yaml
   deck sync -s kong-policy-bundle.yaml
   ```
   - **OIDC-Integration:** Keycloak Realm + Client korrekt referenziert
   - **Rate-Limiting:** Tenant-Header-basierte Limits aktiv
   - **Multi-Tenancy:** Tenant-Isolation via Kong-Plugins validiert
   - **Success Criteria:** Kong-Gateway mit dynamischen Policies operational

3. **Envoy Policy-Bundle Production-Deployment:**
   ```bash
   # Generated Envoy-Config Deploy
   java -jar sync-job.jar --mode=envoy --dry-run=false

   # Validation
   curl -X POST http://envoy-admin:9901/config_dump
   ```
   - **Header-Enforcement:** Idempotency-Key + Correlation-ID + If-Match validiert
   - **CORS-Configuration:** Multi-Origin Support für Frontend-Apps
   - **Circuit-Breaker:** Upstream-Health-Checks konfiguriert
   - **Success Criteria:** Envoy-Gateway mit Header-Enforcement operational

**Timeline:** Tag 1-2 (2-3 Stunden)
**Rollback-Plan:** Automated Kong/Envoy-Config-Backup vor jedem Deployment

### Phase 3: Monitoring + Scheduler + Production-Hardening (2 Stunden)

**Goal:** Complete Production-Readiness mit Monitoring, Alerting, und Operational-Excellence

**Actions:**
1. **Quarkus-Service Production-Deployment:**
   ```bash
   # Service-Build für Production
   mvn package -Pnative

   # Docker-Container-Deployment
   docker build -t freshplan/settings-sync-job:v1.0 .
   docker run -d --name settings-sync-job \
     -e SETTINGS_REGISTRY_URL=https://api.freshplan.com \
     -e KONG_ADMIN_URL=https://kong-admin.freshplan.com \
     freshplan/settings-sync-job:v1.0
   ```
   - **Health-Checks:** /health/ready + /health/live Endpoints
   - **Graceful-Shutdown:** Signal-Handling für Rolling-Updates
   - **Success Criteria:** Service läuft stabil in Production-Environment

2. **Scheduler + Manual-Trigger Configuration:**
   ```java
   @Scheduled(cron = "0 */5 * * * ?") // Alle 5 Minuten
   public void scheduledSync() {
       syncService.performSync();
   }

   @POST
   @Path("/trigger-sync")
   public Response manualTrigger() {
       // Manual-Trigger für Emergency-Updates
   }
   ```
   - **Cron-Schedule:** 5-Minuten-Intervall für normale Updates
   - **Manual-Trigger:** REST-API für Emergency-Policy-Updates
   - **Concurrency-Protection:** Nur ein Sync-Process gleichzeitig
   - **Success Criteria:** Automated + Manual-Sync operational

3. **Monitoring + Alerting Setup:**
   ```yaml
   # Prometheus-Metrics
   settings_sync_duration_seconds: Timer
   settings_sync_success_total: Counter
   settings_sync_errors_total: Counter
   kong_policy_deployment_status: Gauge
   envoy_config_deployment_status: Gauge

   # Alert-Rules
   - alert: SettingsSyncFailure
     expr: settings_sync_errors_total > 0
     for: 2m
     annotations:
       summary: "Settings-Sync-Job failing"
   ```
   - **Golden-Signals:** Success-Rate, Error-Rate, Duration, Policy-Deployment-Status
   - **Alerting:** Slack + Email bei wiederholten Fehlern
   - **Dashboard:** Grafana-Dashboard für Sync-Operations
   - **Success Criteria:** Complete Observability für Production-Operations

**Timeline:** Tag 2 (2 Stunden)
**Rollback-Plan:** Service-Stop + Previous-Version-Restore bei kritischen Production-Issues

## ✅ Success Metrics

### **Immediate Success (Phase 1-3 = 6-8 Stunden):**
1. **Build + Tests:** Maven Build grün + ≥85% Unit-Test-Coverage
2. **Kong-Integration:** Dynamic Rate-Limiting + OIDC + Multi-Tenancy operational
3. **Envoy-Integration:** Header-Enforcement + CORS + Circuit-Breaker operational
4. **Audit-Trail:** Vollständiges Logging aller Policy-Changes
5. **Monitoring:** Prometheus-Metrics + Alerting für Production-Operations

### **Operational Success (1-2 Wochen):**
1. **Sync-Reliability:** >99% Success-Rate für Scheduled-Syncs
2. **Performance:** <30s für komplette Gateway-Policy-Updates
3. **Error-Recovery:** Automatic-Retry + Manual-Rollback bei <1% der Deployments
4. **Multi-Tenancy:** Vollständige Tenant-Isolation via Gateway-Policies
5. **Developer-Experience:** Copy-Paste Integration für neue Module

### **External Validation:**
- **Add-On Integration-Pack:** 9.9/10 (External AI) - Production-Ready Implementation
- **Settings-Sync-Job:** Enterprise-Grade Java-Code mit kompletter Error-Handling
- **Gateway-Integration:** Kong + Envoy beide unterstützt mit Fallback-Strategien

## 🔗 Related Documentation

### **Implementation Foundation:**
- [Integration Charter](../artefakte/docs/INTEGRATION_CHARTER.md) - Master SoT für Integration Standards
- [Foundation Integration Guide](../artefakte/docs/FOUNDATION_INTEGRATION_GUIDE.md) - Settings-Registry Enhancement
- [Production Deployment Guide](../artefakte/README.md) - Copy-Paste Operations

### **Code Artifacts:**
- [SyncJob.java](../artefakte/src/SyncJob.java) - Main Service Implementation
- [KongDeckGenerator.java](../artefakte/src/KongDeckGenerator.java) - Kong-Policy-Generation
- [AuditLog.java](../artefakte/src/AuditLog.java) - Complete Audit-Trail

### **Foundation Dependencies:**
- [Settings-Registry](../../verwaltung/README.md) - Dynamic Configuration API
- [Operations Runbook](../../betrieb/README.md) - Production Operations
- [Security Model](../../sicherheit/README.md) - Multi-Tenancy + OIDC

## 🤖 Claude Handover Section

### **Current Priority (für neue Claude):**
```bash
# Phase 1: Immediate Action
cd integration/implementation-plans/
→ 01_SETTINGS_SYNC_JOB_IMPLEMENTATION_PLAN.md (CURRENT)

# Start Here:
cd ../artefakte/src/
mvn clean test

# Success Criteria: Build grün + alle Unit-Tests pass
# Next: Kong/Envoy Production-Integration
```

### **Context für neue Claude:**
- **Settings-Sync-Job:** Atomarer Implementation-Plan für Enterprise-Grade Gateway-Policy-Sync
- **Timeline:** 6-8 Stunden von Build zu Production-Deployment
- **External AI Validation:** 9.9/10 Production-Ready Code-Quality
- **Dependencies:** Alle kritischen Services (Settings-Registry, Kong, Envoy) Production-Ready

### **Key Success-Factors:**
- **Unit-Tests:** ≥85% Coverage für Production-Confidence
- **Error-Handling:** Comprehensive Retry + Rollback-Strategien
- **Monitoring:** Complete Observability für Operational-Excellence
- **Multi-Gateway:** Kong + Envoy parallel unterstützt

**🚀 Ready für Enterprise-Grade Settings-Sync-Job Implementation!**