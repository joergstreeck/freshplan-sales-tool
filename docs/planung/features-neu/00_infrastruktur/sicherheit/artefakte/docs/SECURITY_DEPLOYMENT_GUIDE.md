# 🔐 Security Deployment Guide - FreshFoodz B2B CRM

**📊 Deployment Status:** ✅ **PRODUCTION-READY IMPLEMENTATION GUIDE**
**🎯 Zielgruppe:** DevOps + Backend Team + Security Team
**⏱️ Deployment-Zeit:** 2-4 Stunden (inkl. Testing)
**🔧 Aufwand:** M (Medium - Configuration + Validation Required)

## 🎯 Deployment-Übersicht

**Mission:** Production-Deployment der External AI Security-Excellence Implementation
**Scope:** SessionSettingsFilter + RLS v2 + SecurityContractTests + Keycloak Integration
**Outcome:** Enterprise-Grade Security Foundation für alle 8 Business-Module

## 📋 Pre-Deployment Checklist

### Environment Requirements:
- ✅ **Keycloak:** Version 21+ mit OIDC-Provider konfiguriert
- ✅ **PostgreSQL:** Version 15+ mit Row Level Security Support
- ✅ **Java:** Version 17+ für Quarkus 3.x Compatibility
- ✅ **Maven:** 3.8+ für Build-Pipeline
- ✅ **Database Migration:** Flyway/Liquibase für V227 Migration

### Security Prerequisites:
- ✅ **SSL/TLS:** HTTPS-only für alle Security-Endpoints
- ✅ **Connection Pool:** Hikari CP mit isolierten Connections
- ✅ **Backup Strategy:** Database-Backup vor RLS-Migration
- ✅ **Rollback Plan:** V226 → V227 Rollback-Migration ready

## 🚀 Step-by-Step Deployment

### Phase 1: Backend Security Filter (30 Min)

```bash
# 1. Copy SessionSettingsFilter to Backend
cp artefakte/backend/SessionSettingsFilter.java \
   ../../../../../../backend/src/main/java/de/freshplan/security/

# 2. Add CDI Annotations if needed
# bereits vorhanden: @Provider, @Priority, @Inject

# 3. Update application.properties
echo "quarkus.security.jaxrs.deny-unannotated-endpoints=false" >> backend/src/main/resources/application.properties
echo "quarkus.security.auth.session.same-site=strict" >> backend/src/main/resources/application.properties
```

### Phase 2: Database RLS v2 Migration (45 Min)

```bash
# 1. Ermittele nächste Migration-Nummer
./scripts/get-next-migration.sh
# Output: V227

# 2. Deploy RLS v2 als Production-Migration
cp artefakte/sql/rls_v2.sql \
   backend/src/main/resources/db/migration/V227__security_rls_v2.sql

# 3. Validate Migration Syntax
psql -h localhost -d freshplan_test -f backend/src/main/resources/db/migration/V227__security_rls_v2.sql --dry-run

# 4. Run Migration (Staging first!)
./mvnw flyway:migrate -Dspring.profiles.active=staging
```

### Phase 3: Keycloak Claims Configuration (30 Min)

```json
// Keycloak Client Mapper Configuration
{
  "mappers": [
    {
      "name": "org_id_mapper",
      "protocol": "openid-connect",
      "protocolMapper": "oidc-usermodel-attribute-mapper",
      "config": {
        "user.attribute": "org_id",
        "claim.name": "org_id",
        "jsonType.label": "String"
      }
    },
    {
      "name": "territory_mapper",
      "protocol": "openid-connect",
      "protocolMapper": "oidc-usermodel-attribute-mapper",
      "config": {
        "user.attribute": "territory",
        "claim.name": "territory",
        "jsonType.label": "String"
      }
    },
    {
      "name": "scopes_mapper",
      "protocol": "openid-connect",
      "protocolMapper": "oidc-usermodel-attribute-mapper",
      "config": {
        "user.attribute": "scopes",
        "claim.name": "scopes",
        "jsonType.label": "JSON"
      }
    },
    {
      "name": "contact_roles_mapper",
      "protocol": "openid-connect",
      "protocolMapper": "oidc-usermodel-attribute-mapper",
      "config": {
        "user.attribute": "contact_roles",
        "claim.name": "contact_roles",
        "jsonType.label": "JSON"
      }
    }
  ]
}
```

### Phase 4: Security Contract Tests Integration (30 Min)

```bash
# 1. Copy Contract Tests
cp artefakte/testing/SecurityContractTests.java \
   backend/src/test/java/de/freshplan/security/

# 2. Add Testcontainers dependency (wenn nicht vorhanden)
# <dependency>
#   <groupId>org.testcontainers</groupId>
#   <artifactId>postgresql</artifactId>
#   <scope>test</scope>
# </dependency>

# 3. Run Security Contract Tests
./mvnw test -Dtest=SecurityContractTests

# 4. Add to CI Pipeline
echo "- name: Security Contract Tests" >> .github/workflows/ci.yml
echo "  run: ./mvnw test -Dtest=SecurityContractTests" >> .github/workflows/ci.yml
```

### Phase 5: Production Validation (45 Min)

```bash
# 1. Deploy to Staging Environment
./mvnw package && docker build -t freshplan-backend:security-v1 .
docker run -d --name staging-security -p 8080:8080 freshplan-backend:security-v1

# 2. Smoke Tests
curl -H "Authorization: Bearer $KEYCLOAK_TOKEN" \
     -H "Content-Type: application/json" \
     http://staging:8080/api/leads

# 3. RLS Validation Tests
psql -h staging-db -c "SELECT count(*) FROM leads;" # Should use RLS
psql -h staging-db -c "SET app.territory='DE'; SELECT count(*) FROM leads WHERE territory='CH';" # Should return 0

# 4. Performance Validation
curl -w "@curl-format.txt" -H "Authorization: Bearer $TOKEN" \
     http://staging:8080/api/leads
# Check: Response time < 200ms P95
```

## 🔍 Security Validation Tests

### Manual Security Tests:

```bash
# Test 1: Territory Isolation
# User DE kann keine CH-Leads sehen
TOKEN_DE=$(get-token-de.sh)
curl -H "Authorization: Bearer $TOKEN_DE" http://api/leads?territory=CH
# Expected: Empty result []

# Test 2: Lead Ownership Protection
# User kann nur eigene Leads editieren
TOKEN_USER1=$(get-token-user1.sh)
curl -X PUT -H "Authorization: Bearer $TOKEN_USER1" \
     -d '{"status":"QUALIFIED"}' http://api/leads/other-users-lead-id
# Expected: 403 Forbidden

# Test 3: Multi-Contact Visibility
# BUYER sieht COMMERCIAL, nicht PRODUCT Notes
TOKEN_BUYER=$(get-token-buyer.sh)
curl -H "Authorization: Bearer $TOKEN_BUYER" http://api/lead-notes?category=PRODUCT
# Expected: Empty result []

# Test 4: Session Settings Application
# Check PostgreSQL Session Variables
psql -c "SELECT current_setting('app.user_id', true);"
# Expected: UUID des aktuellen Users

# Test 5: RLS Performance
# Lead-Access unter 50ms P95
time curl -H "Authorization: Bearer $TOKEN" http://api/leads/lead-id
# Expected: < 50ms response time
```

## 🚨 Troubleshooting Guide

### Issue 1: "SET LOCAL app.* not found"
**Symptom:** Error beim Session Settings
**Solution:**
```sql
-- Check session variables existence
SELECT name FROM pg_settings WHERE name LIKE 'app.%';
-- If empty, restart PostgreSQL connection pool
```

### Issue 2: "RLS Policy Violation"
**Symptom:** Unexpected access denied
**Solution:**
```sql
-- Debug RLS policies
SET app.user_id = 'debug-user-uuid';
SET app.territory = 'DE';
SELECT * FROM leads WHERE id = 'problem-lead-uuid';
-- Check ownership in user_lead_assignments
```

### Issue 3: "Keycloak Claims Missing"
**Symptom:** Claims nicht in SecurityIdentity
**Solution:**
```java
// Debug in SessionSettingsFilter
System.out.println("Claims: " + identity.getAttributes());
// Verify Keycloak mapper configuration
```

### Issue 4: "Performance Degradation"
**Symptom:** Lead-Access > 200ms
**Solution:**
```sql
-- Check RLS function performance
EXPLAIN ANALYZE SELECT * FROM leads WHERE id = 'test-uuid';
-- Verify STABLE functions + indexes
```

## 📊 Success Metrics Validation

### Post-Deployment Validation:

```yaml
Performance Metrics:
  - Lead-Access P95: < 50ms ✓
  - Territory-Switch P95: < 150ms ✓
  - RLS-Overhead: < 5ms ✓
  - Connection-Pool-Efficiency: > 95% ✓

Security Metrics:
  - Territory-Isolation: 100% ✓
  - Lead-Protection: User-based Ownership ✓
  - Multi-Contact-Privacy: GF/Buyer/Chef Hierarchy ✓
  - Session-Security: Pool-safe SET LOCAL ✓

Compliance Metrics:
  - GDPR-Audit-Trail: Complete für alle Access-Events ✓
  - Access-Control: Granular per Lead + Note + Activity ✓
  - Data-Retention: Configurable per Territory ✓
```

## 🔄 Rollback Strategy

### Immediate Rollback (if needed):

```bash
# 1. Disable SessionSettingsFilter (Comment @Provider annotation)
# 2. Rollback Database Migration
./mvnw flyway:undo -Dspring.profiles.active=production

# 3. Revert Keycloak Claims (remove custom mappers)
# 4. Deploy previous backend version
docker pull freshplan-backend:previous-stable
docker stop current-backend && docker run freshplan-backend:previous-stable

# 5. Validate rollback success
curl -H "Authorization: Bearer $TOKEN" http://api/health/security
# Expected: Previous security model active
```

## 🎯 Next Steps nach Deployment

### Phase 2 Enhancements (Q1 2026):
1. **Emergency Override Workflows:** Manager-Access mit Time-delay + Two-Person-Approval
2. **Anomaly Detection:** Suspicious-Access-Pattern + Bulk-Export-Alerts
3. **Mobile Device Security:** MDM-Integration + Token-Invalidation
4. **Advanced Monitoring:** PromQL-Dashboards für Security-Metrics

### Integration Opportunities:
- **Module 02 (Neukundengewinnung):** Lead-Protection für Prospecting
- **Module 03 (Kundenmanagement):** Multi-Contact-Security für Account-Teams
- **Module 05 (Kommunikation):** Message-Threading mit Visibility-Scoping

---

**🎯 Diese Deployment-Guide liefert step-by-step Production-Deployment der world-class External AI Security-Implementation! 🔐🚀**