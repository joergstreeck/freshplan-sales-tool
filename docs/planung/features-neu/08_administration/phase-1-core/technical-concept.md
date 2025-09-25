# Administration Module - Technical Concept

> **RLS-Status (Sprint 1.6):** ✅ @RlsContext CDI-Interceptor verpflichtend
> 🔎 Details: [ADR-0007](../../adr/ADR-0007-rls-connection-affinity.md) · [Security Update](../../SECURITY_UPDATE_SPRINT_1_5.md)

**📊 Plan Status:** 🔵 Draft
**🎯 Owner:** Development Team
**⏱️ Timeline:** 2025-09-23 → 2025-10-07 (2 Wochen)
**🔧 Effort:** L (Large) - 50 Artefakte, 3 Sub-Domains

## 🎯 Executive Summary (für Claude)

**Mission:** Enterprise-Grade Administration mit ABAC Security, Risk-Tiered Approvals und vollständigem Audit-System implementieren
**Problem:** Fehlende Admin-Tools für Multi-Tenant B2B-Food Plattform mit Territory/Org-Scoping
**Solution:** Modular-Monolith mit 3 Sub-Domains: Security & Access Control, Operations & Compliance, Monitoring & Integrations
**Timeline:** 6-8 Tage mit Jörgs Speed-Faktor (externe AI schätzte 2 Wochen)
**Impact:** Production-ready Admin-Suite mit 95%+ Foundation Standards Compliance

**Quick Context:** 50 copy-paste-ready Artefakte wurden von externem AI geliefert und mit 9.2/10 bewertet. Revolutionäre Risk-Tiered Approvals lösen Two-Person-Rule Problem. ABAC + RLS für Multi-Tenant Security. Time-Delay statt komplexe Approval-Chains.

## 📋 Context & Dependencies

### Current State:
- ✅ Bestehende Admin-Routes (65% implementiert) in Legacy-System
- ✅ Audit-System Grundlagen vorhanden
- ✅ User-Management mit Keycloak Integration
- ✅ Settings-Registry (Modul 06) als Foundation
- ❌ ABAC Security fehlt (noch RBAC)

### Target State:
- ✅ Enterprise-Grade ABAC mit Territory/Org-Scoping
- ✅ Risk-Tiered Approval-Workflows (TIER1/2/3)
- ✅ Vollständiges Audit-System mit Search & Export
- ✅ SMTP + DSGVO Operations mit Outbox-Pattern
- ✅ Production-ready Monitoring (Grafana + Prometheus)

### Dependencies:
- **Completed:** → [Modul 06 Settings](../06_einstellungen/technical-concept.md) - Settings Registry
- **Parallel:** → [Modul 07 Help](../07_hilfe_support/technical-concept.md) - CAR Strategy lernen
- **Required:** → Keycloak OIDC Integration (bestehend)

## 🛠️ Implementation Phases

### Phase 1: Security & Access Control (08A) - Week 1
**Goal:** ABAC + RLS + Risk-Tiered Approvals implementieren
**Actions:**
- [ ] SQL Migrations deployen (V226-V229 via `./scripts/get-next-migration.sh`)
- [ ] ABAC Security Services (AdminSecurityService, RlsSessionFilter)
- [ ] Risk-Tiered Approval-Workflow (Time-Delay statt Two-Person-Rule)
- [ ] OpenAPI 3.1 Specs für Admin APIs

**Code Changes:**
```sql
-- VXXX__admin_audit.sql (wird V226)
CREATE TABLE admin_audit (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id uuid NOT NULL,
  risk_tier risk_tier_enum NOT NULL DEFAULT 'TIER3'
);
ALTER TABLE admin_audit ENABLE ROW LEVEL SECURITY;
```

**Success Criteria:**
- ABAC Permissions pro Territory/Org funktional
- Emergency Override mit Justification möglich
- RLS für Multi-Tenant Isolation aktiv

**Next:** → [Phase 2: Operations](#phase-2)

### Phase 2: Operations & Compliance (08B) - Week 1-2
**Goal:** SMTP + DSGVO + Audit-System operativ
**Actions:**
- [ ] Admin Operations Services (SMTP Rate Limiting, DSGVO Workflows)
- [ ] Enhanced Audit System (Search, Export, Event Correlation)
- [ ] User Management mit Claims-Sync zu Keycloak
- [ ] Outbox-Pattern für reliable Event Processing

**Code Changes:**
```java
@ApplicationScoped
public class AdminOperationsService {
  public void enforceSmtpRateLimit(String territory, int dailyLimit) {
    // Rate limiting per territory
  }
}
```

**Success Criteria:**
- SMTP Rate Limits pro Territory funktional
- DSGVO Export/Delete Workflows operativ
- Audit Events mit Sub-Second Precision

**Next:** → [Phase 3: Monitoring](#phase-3)

### Phase 3: Monitoring & Integrations (08C) - Week 2
**Goal:** Production-ready Monitoring und CI/CD
**Actions:**
- [ ] Grafana Dashboards (Admin Overview, Security, Operations)
- [ ] Prometheus Alerts für kritische Admin-KPIs
- [ ] CI/CD Pipeline mit Security Scans
- [ ] Performance Tests (k6) für Admin APIs

**Code Changes:**
```yaml
# admin-overview.json Grafana Dashboard
{
  "title": "Admin Overview",
  "panels": [
    {"title": "API p95 (ms)", "expr": "histogram_quantile(0.95, ...)"},
    {"title": "ABAC Deny Rate", "expr": "rate(abac_denied_total[5m])"}
  ]
}
```

**Success Criteria:**
- 5 Core Admin KPIs in Grafana Dashboard
- Alert-Thresholds für ABAC Deny Rate < 5%
- CI Pipeline mit 85%+ Coverage Gate

**Next:** → [Production Deployment](#deployment)

### Phase 4: Production Deployment & Polish - Week 2
**Goal:** Go-Live mit allen Admin-Features
**Actions:**
- [ ] Frontend Components mit MUI v5 (React Query Integration)
- [ ] E2E Tests für kritische Admin-Workflows
- [ ] Security Penetration Tests (OWASP ZAP)
- [ ] Performance Tuning (Target: API <200ms P95)

**Success Criteria:**
- Alle 6 Admin Pages funktional (Dashboard, Users, Audit, Email, DSGVO, Policies)
- Security Scan ohne kritische Findings
- Performance Budget eingehalten

## ✅ Success Metrics

**Quantitative:**
- **ABAC Performance:** <50ms per authorization check (current: n/a)
- **API Response Time:** <200ms P95 für alle Admin APIs
- **Test Coverage:** >90% für neue Admin Services (baseline: 67%)
- **Audit Events:** 100% Coverage für kritische Admin Operations

**Qualitative:**
- Risk-Tiered Approvals lösen Two-Person-Rule Problematik
- Emergency Override mit vollständigem Audit Trail
- Territory/Org-Scoping für B2B-Food Franchise-Modell
- Production-ready Monitoring ohne manuelle Eingriffe

**Timeline:**
- **Phase 1 (Security):** Tag 1-3
- **Phase 2 (Operations):** Tag 3-5
- **Phase 3 (Monitoring):** Tag 5-7
- **Phase 4 (Deployment):** Tag 7-8
- **Complete:** Ende Woche 2 (bzw. Tag 8 mit Speed-Faktor)

## 🔗 Related Documentation

**Foundation Knowledge:**
- **Design Standards:** → [DESIGN_SYSTEM.md](../../grundlagen/DESIGN_SYSTEM.md)
- **API Standards:** → [API_STANDARDS.md](../../grundlagen/API_STANDARDS.md)
- **Security Standards:** → [SECURITY_STANDARDS.md](../../grundlagen/SECURITY_STANDARDS.md)

**Implementation Details:**
- **Code Location:** `backend/modules/admin/`, `frontend/src/admin/`
- **Config Files:** `application.yml` (ABAC Settings), `prometheus.yml`
- **Test Files:** `AdminAbacContractTest.java`, `admin-load-test.js`

**Related Plans:**
- **Dependencies:** → [Modul 06 Settings](../06_einstellungen/technical-concept.md)
- **Follow-ups:** → [Modul 09 Integrations](../09_integrations/) (planned)
- **Parallel Work:** → [Modul 07 Help & Support](../07_hilfe_support/technical-concept.md)

**Critical Reviews:**
- **Artefakte Bewertung:** → [Claude Review 9.2/10](diskussionen/2025-09-20_CLAUDE_ARTEFAKTE_REVIEW.md)
- **Architecture Decision:** → [Modular-Monolith vs Microservices](diskussionen/2025-09-20_CLAUDE_INITIAL_CONTROVERSY.md)
- **External AI Consultation:** → [Implementation Timeline](diskussionen/2025-09-20_CLAUDE_FINAL_ASSESSMENT.md)

## 🤖 Claude Handover Section

**Für nächsten Claude:**

**Aktueller Stand:**
Technical Concept für Modul 08 Administration erstellt mit 3-Sub-Domain Struktur. 50 copy-paste-ready Artefakte wurden bewertet (9.2/10) und sind bereit für Implementation. Modular-Monolith Approach gewählt für optimale Claude-Verständlichkeit.

**Nächster konkreter Schritt:**
1. Migration Nummern bestimmen: `./scripts/get-next-migration.sh` ausführen
2. SQL Migrations kopieren und VXXX durch echte Nummern ersetzen
3. Phase 1 (Security & Access Control) starten mit AdminSecurityService

**Wichtige Dateien für Context:**
- `artefakte/AdminSecurityService.java` - ABAC fail-closed Implementation
- `artefakte/VXXX__admin_audit.sql` - RLS + Risk-Tier Tables
- `artefakte/admin-users-api.yaml` - OpenAPI 3.1 mit RFC7807
- `diskussionen/2025-09-20_CLAUDE_ARTEFAKTE_REVIEW.md` - Vollständige Qualitätsbewertung

**Offene Entscheidungen:**
- ScopeContext Interface Definition (für Dependency Injection)
- Integration mit bestehender Settings-Registry (Modul 06)
- Frontend Theme Variables vs. hardcoded MUI Colors

**Kontext-Links:**
- **Grundlagen:** → [Planungsmethodik](../../PLANUNGSMETHODIK.md) für Template Compliance
- **Dependencies:** → [Settings Registry](../06_einstellungen/technical-concept.md) für Configuration Management
- **Timeline Reference:** → [Master Plan V5](../../CRM_COMPLETE_MASTER_PLAN_V5.md) für Business Context