# 🌐 Gateway-Policies Deployment Plan

**📊 Plan Status:** 🟢 Production-Ready (External AI Validated 9.2/10)
**🎯 Owner:** Infrastructure Team + DevOps
**⏱️ Timeline:** Tag 2-3 (4-6 Stunden aktive Implementation)
**🔧 Effort:** M (Medium - Multi-Gateway-Deployment mit umfassender Validation)

## 🎯 Executive Summary (für Claude)

**Mission:** Kong + Envoy Gateway-Policies Production-Ready deployen mit OIDC, Rate-Limiting, Multi-Tenancy und Header-Enforcement

**Problem:** FreshPlan 8-Module-Ecosystem braucht Enterprise-Grade API-Gateway-Layer für sichere, performante, und tenant-isolierte API-Kommunikation - ohne Gateway-Policies sind APIs ungeschützt und unperformant

**Solution:** Kong-Policy-Bundle für OIDC + Rate-Limiting + Idempotency + Envoy-Policy-Bundle für Header-Enforcement + CORS + Circuit-Breaker mit dynamischer Settings-Registry-Integration

**Timeline:** 4-6 Stunden von Policy-Generation bis Production-Validation mit kompletter Multi-Tenancy-Isolation

**Impact:** Alle 8 Module erhalten sofort Enterprise-Grade API-Protection + Foundation für sichere Cross-Module-Kommunikation

## 📋 Context & Dependencies

### Current State:
- ✅ **Kong Infrastructure:** v3.4+ deployed mit Admin-API + decK CLI v1.28+
- ✅ **Envoy Infrastructure:** v1.27+ deployed mit Admin-API + Configuration-Management
- ✅ **Keycloak OIDC:** Realm freshplan + Client freshplan-api konfiguriert
- ✅ **Policy-Bundles:** kong-policy-bundle.yaml + envoy-policy-bundle.yaml (External AI erstellt)
- ✅ **Settings-Integration:** Dynamic Policy-Generation via Settings-Sync-Job

### Target State:
- 🎯 **Kong Gateway:** OIDC + JWT-Validation + Tenant-based Rate-Limiting + Idempotency operational
- 🎯 **Envoy Gateway:** Header-Enforcement + CORS + Circuit-Breaker + Upstream-Health-Checks
- 🎯 **Multi-Tenancy:** Vollständige Tenant-Isolation via Gateway-Policies
- 🎯 **Performance:** <5ms Gateway-Overhead + <200ms P95 Response-Times
- 🎯 **Security:** Zero-Trust API-Access mit kompletter Request-Validation

### Dependencies:
- **Kong Admin API:** https://kong-admin.freshplan.com:8001 (READY)
- **Envoy Admin API:** https://envoy-admin.freshplan.com:9901 (READY)
- **Keycloak:** https://auth.freshplan.com/realms/freshplan (READY)
- **Settings-Registry:** Gateway-Settings für Rate-Limits + OIDC-Parameters (READY)
- **TLS-Certificates:** Wildcard *.freshplan.com für HTTPS-Termination (READY)

## 🛠️ Implementation Phases (3 Phasen = 4-6 Stunden Gesamt)

### Phase 1: Kong Policy-Bundle Deployment + OIDC-Integration (2 Stunden)

**Goal:** Kong Gateway mit OIDC, Rate-Limiting, und Idempotency operational

**Actions:**
1. **Kong Policy-Bundle Pre-Deployment Validation:**
   ```bash
   cd ../artefakte/gateway/kong/

   # Policy-Bundle Syntax-Check
   deck validate -s kong-policy-bundle.yaml

   # Dry-Run gegen Production-Kong
   deck diff -s kong-policy-bundle.yaml \
     --kong-addr https://kong-admin.freshplan.com:8001
   ```
   - **Syntax-Validation:** YAML korrekt formatiert + alle Plugins verfügbar
   - **Diff-Analysis:** Nur erwartete Changes vs. Current-Kong-State
   - **Success Criteria:** deck validate + deck diff ohne Errors

2. **OIDC-Plugin Configuration + Keycloak-Integration:**
   ```yaml
   # Kong OIDC-Plugin Configuration
   plugins:
   - name: oidc
     config:
       client_id: freshplan-api
       client_secret: ${KEYCLOAK_CLIENT_SECRET}
       discovery: https://auth.freshplan.com/realms/freshplan/.well-known/openid_configuration
       bearer_only: "yes"
       realm: freshplan
       introspection_endpoint_auth_method: client_secret_post
   ```
   - **Keycloak-Connection-Test:** OIDC-Discovery-Endpoint erreichbar
   - **JWT-Validation-Test:** Valid + Invalid Tokens testen
   - **Success Criteria:** OIDC-Authentication für alle API-Routes operational

3. **Kong Production-Deployment + Rate-Limiting:**
   ```bash
   # Production-Deployment mit Backup
   deck dump -o kong-backup-$(date +%Y%m%d-%H%M%S).yaml
   deck sync -s kong-policy-bundle.yaml \
     --kong-addr https://kong-admin.freshplan.com:8001

   # Rate-Limiting Validation
   for tenant in tenant1 tenant2 tenant3; do
     for i in {1..50}; do
       curl -H "X-Tenant-ID: $tenant" \
            -H "Authorization: Bearer $VALID_TOKEN" \
            https://api.freshplan.com/api/health
     done
   done
   ```
   - **Backup-Strategy:** Automatic Kong-Config-Backup vor jedem Deployment
   - **Rate-Limiting-Test:** Tenant-spezifische Limits (20/min, 50/min, 100/min)
   - **Idempotency-Test:** Duplicate Idempotency-Keys → 200/409 Response
   - **Success Criteria:** Kong-Gateway mit OIDC + Rate-Limiting + Idempotency operational

**Timeline:** Tag 2 (2 Stunden)
**Rollback-Plan:** deck sync mit Backup-Configuration bei kritischen Fehlern

### Phase 2: Envoy Policy-Bundle Deployment + Header-Enforcement (1-2 Stunden)

**Goal:** Envoy Gateway mit Header-Enforcement, CORS, und Circuit-Breaker operational

**Actions:**
1. **Envoy Configuration Pre-Deployment Validation:**
   ```bash
   cd ../artefakte/gateway/envoy/

   # Envoy-Config Syntax-Check
   envoy --mode validate --config-path envoy-policy-bundle.yaml

   # Current Envoy-Config Backup
   curl -s http://envoy-admin.freshplan.com:9901/config_dump > \
     envoy-backup-$(date +%Y%m%d-%H%M%S).json
   ```
   - **Syntax-Validation:** YAML korrekt + alle Filters/Clusters verfügbar
   - **Config-Backup:** Complete Envoy-State für Rollback
   - **Success Criteria:** envoy --mode validate ohne Errors

2. **Header-Enforcement Configuration + Lua-Filters:**
   ```yaml
   # Envoy HTTP-Filter für Header-Enforcement
   http_filters:
   - name: envoy.filters.http.lua
     typed_config:
       "@type": type.googleapis.com/envoy.extensions.filters.http.lua.v3.Lua
       inline_code: |
         function envoy_on_request(request_handle)
           -- Idempotency-Key enforcement
           local idempotency_key = request_handle:headers():get("idempotency-key")
           if not idempotency_key and request_handle:headers():get(":method") == "POST" then
             request_handle:respond({[":status"] = "400"}, "Missing Idempotency-Key")
           end

           -- Correlation-ID generation
           if not request_handle:headers():get("correlation-id") then
             request_handle:headers():add("correlation-id", generate_uuid())
           end
         end
   ```
   - **Idempotency-Key:** POST/PUT/PATCH Requests müssen Idempotency-Key haben
   - **Correlation-ID:** Automatic Generation für Request-Tracing
   - **If-Match:** ETag-based Optimistic-Locking-Enforcement
   - **Success Criteria:** Header-Enforcement für alle API-Routes aktiv

3. **Envoy Production-Deployment + CORS-Configuration:**
   ```bash
   # Production-Deployment via Admin-API
   curl -X POST http://envoy-admin.freshplan.com:9901/config_dump \
     -H "Content-Type: application/json" \
     -d @envoy-policy-bundle.yaml

   # CORS-Validation für Frontend-Apps
   for origin in https://app.freshplan.com https://admin.freshplan.com; do
     curl -H "Origin: $origin" \
          -H "Access-Control-Request-Method: POST" \
          -X OPTIONS https://api.freshplan.com/api/leads
   done
   ```
   - **CORS-Origins:** Multi-Origin Support für Frontend + Admin-Apps
   - **Preflight-Handling:** OPTIONS-Requests korrekt beantwortet
   - **Circuit-Breaker:** Automatic Upstream-Failure-Detection + Recovery
   - **Success Criteria:** Envoy-Gateway mit CORS + Circuit-Breaker operational

**Timeline:** Tag 2-3 (1-2 Stunden)
**Rollback-Plan:** Envoy-Config-Restore via Admin-API bei kritischen Issues

### Phase 3: Multi-Tenancy + Security-Hardening + Performance-Optimization (1-2 Stunden)

**Goal:** Complete Enterprise-Grade Gateway-Security mit Multi-Tenancy und Performance-Optimization

**Actions:**
1. **Multi-Tenancy Isolation Testing:**
   ```bash
   # Tenant-Isolation-Tests
   # Tenant A sollte nur Tenant A Daten sehen
   TOKEN_A=$(get_token user_a tenant_a)
   TOKEN_B=$(get_token user_b tenant_b)

   # Cross-Tenant-Access-Test (should fail)
   curl -H "Authorization: Bearer $TOKEN_A" \
        -H "X-Tenant-ID: tenant_b" \
        https://api.freshplan.com/api/leads
   # Expected: 403 Forbidden

   # Correct-Tenant-Access-Test (should succeed)
   curl -H "Authorization: Bearer $TOKEN_A" \
        -H "X-Tenant-ID: tenant_a" \
        https://api.freshplan.com/api/leads
   # Expected: 200 OK mit Tenant A Daten
   ```
   - **Tenant-Header-Validation:** X-Tenant-ID muss mit JWT-Claims matchen
   - **Cross-Tenant-Protection:** Tenant A kann nicht Tenant B Daten zugreifen
   - **Org-Level-Isolation:** Organisational-Boundaries zusätzlich zu Tenant-Boundaries
   - **Success Criteria:** Vollständige Multi-Tenancy-Isolation via Gateway-Policies

2. **Security-Hardening + Attack-Protection:**
   ```yaml
   # Kong Security-Plugins
   - name: rate-limiting-advanced
     config:
       limit: [100, 1000]
       window_size: [60, 3600]
       identifier: header
       header_name: X-Tenant-ID

   - name: bot-detection
     config:
       allow: []
       deny: ["curl", "python-requests", "postman"]

   - name: ip-restriction
     config:
       allow: ["10.0.0.0/8", "192.168.0.0/16"]
       deny: []
   ```
   - **DDoS-Protection:** Advanced Rate-Limiting mit Tenant-based Buckets
   - **Bot-Detection:** Suspicious User-Agents blockiert
   - **IP-Allowlisting:** Only trusted Networks für Admin-APIs
   - **Request-Size-Limits:** Maximum Payload-Size Protection
   - **Success Criteria:** Gateway resistant gegen common Attack-Vectors

3. **Performance-Optimization + Monitoring:**
   ```bash
   # Gateway-Performance-Test
   ab -n 1000 -c 50 \
     -H "Authorization: Bearer $VALID_TOKEN" \
     -H "X-Tenant-ID: tenant1" \
     https://api.freshplan.com/api/health

   # Gateway-Latency-Analysis
   curl -w "@curl-format.txt" \
     -H "Authorization: Bearer $VALID_TOKEN" \
     https://api.freshplan.com/api/leads
   ```
   - **Latency-Target:** <5ms Gateway-Overhead für alle API-Calls
   - **Throughput-Target:** >1000 RPS per Gateway-Instance
   - **Connection-Pooling:** Optimized Upstream-Connections
   - **Caching-Strategy:** JWT-Validation-Cache + Rate-Limit-Cache
   - **Success Criteria:** Gateway-Performance-Targets erreicht

**Timeline:** Tag 3 (1-2 Stunden)
**Rollback-Plan:** Performance-Rollback zu previous Gateway-Configuration

## ✅ Success Metrics

### **Immediate Success (Phase 1-3 = 4-6 Stunden):**
1. **Kong Gateway:** OIDC + Rate-Limiting + Idempotency + Multi-Tenancy operational
2. **Envoy Gateway:** Header-Enforcement + CORS + Circuit-Breaker operational
3. **Security:** Zero successful Cross-Tenant-Access-Attempts
4. **Performance:** <5ms Gateway-Overhead + >99% Uptime
5. **OIDC-Integration:** JWT-Validation für alle API-Routes operational

### **Operational Success (1-2 Wochen):**
1. **API-Protection:** Zero successful Attack-Vectors gegen Gateway-Layer
2. **Multi-Tenancy:** 100% Tenant-Isolation ohne Data-Leakage
3. **Performance:** P95 <200ms für alle API-Calls inklusive Gateway-Processing
4. **Developer-Experience:** Frontend-Teams können sofort CORS + Headers nutzen
5. **Monitoring:** Complete Gateway-Metrics in Prometheus + Grafana

### **External Validation:**
- **Gateway-Policies:** 9.2/10 (External AI) - Enterprise-Grade Kong + Envoy Configuration
- **Security-Model:** Multi-Tenancy + OIDC + Rate-Limiting + Attack-Protection
- **Performance:** Optimized für High-Throughput B2B-Food-Platform

## 🔗 Related Documentation

### **Policy Implementation:**
- [Kong Policy Bundle](../artefakte/gateway/kong/kong-policy-bundle.yaml) - Complete Kong Configuration
- [Envoy Policy Bundle](../artefakte/gateway/envoy/envoy-policy-bundle.yaml) - Complete Envoy Configuration
- [Settings-Sync Integration](01_SETTINGS_SYNC_JOB_IMPLEMENTATION_PLAN.md) - Dynamic Policy-Updates

### **Security Foundation:**
- [Security Model](../../sicherheit/README.md) - Multi-Tenancy + OIDC Standards
- [API Standards](../../../grundlagen/API_STANDARDS.md) - Headers + Idempotency + CORS
- [Integration Charter](../artefakte/docs/INTEGRATION_CHARTER.md) - Gateway-Standards

### **Operations:**
- [Operations Runbook](../../betrieb/README.md) - Gateway-Operations + Monitoring
- [Performance SLOs](../../leistung/README.md) - Gateway-Performance-Targets
- [Production Deployment](../artefakte/README.md) - Copy-Paste Gateway-Deployment

## 🤖 Claude Handover Section

### **Current Priority (für neue Claude):**
```bash
# Phase 1: Kong-Deployment
cd integration/implementation-plans/
→ 02_GATEWAY_POLICIES_DEPLOYMENT_PLAN.md (CURRENT)

# Start Here:
cd ../artefakte/gateway/kong/
deck validate -s kong-policy-bundle.yaml

# Success Criteria: Validation grün + OIDC-Integration ready
# Next: Envoy-Deployment + Multi-Tenancy-Testing
```

### **Context für neue Claude:**
- **Gateway-Policies:** Atomarer Implementation-Plan für Kong + Envoy Production-Deployment
- **Timeline:** 4-6 Stunden von Policy-Validation bis Production-Hardening
- **External AI Validation:** 9.2/10 Enterprise-Grade Gateway-Configuration
- **Dependencies:** Kong + Envoy Infrastructure ready + Keycloak OIDC operational

### **Key Success-Factors:**
- **OIDC-Integration:** JWT-Validation für Zero-Trust API-Access
- **Multi-Tenancy:** Vollständige Tenant-Isolation via Gateway-Policies
- **Performance:** <5ms Gateway-Overhead für High-Throughput B2B-Platform
- **Security-Hardening:** DDoS + Bot-Detection + IP-Restrictions

**🚀 Ready für Enterprise-Grade Gateway-Policies Production-Deployment!**