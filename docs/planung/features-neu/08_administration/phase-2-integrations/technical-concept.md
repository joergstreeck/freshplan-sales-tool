# 🔌 Phase 2 Technical Concept - Business Extensions Integration

**📊 Plan Status:** ✅ Approved (basierend auf externen AI Artifacts)
**🎯 Owner:** Module 08 Administration Team
**⏱️ Timeline:** 2025-10-07 → 2025-10-21 (2-3 Wochen)
**🔧 Effort:** L (Large) - Backend-Heavy mit bestehender UI

## 🎯 Executive Summary (für Claude)

Phase 2 implementiert Business-spezifische Admin-Erweiterungen für FreshPlan: Lead-Protection-System nach Handelsvertretervertrag, Multi-Provider AI Integration mit Confidence Routing, Sample Management Pipeline mit Remote Customer Feedback, und Advanced System Tools. Nutzt 60% bereits existierende UI und fokussiert auf Backend-First Approach mit copy-paste-ready Artifacts von externer KI-Consultation.

## 📋 Context & Dependencies

### Current State:
- **Phase 1 Foundation:** ABAC Security + Audit System + Monitoring (95%+ Compliance)
- **60% UI Implementation:** IntegrationsDashboard, HelpConfigDashboard, SystemDashboard bereits vorhanden
- **Backend Services:** 0% implementiert - kompletter Backend-First Approach nötig
- **External AI Consultation:** Production-ready Artifacts (9.6/10 Quality Score) verfügbar
- **Business Requirements:** Handelsvertretervertrag Lead-Protection vollständig analysiert

### Target State:
- **Lead-Protection-System:** 6-Monate + 60-Tage + Stop-the-Clock Automatisierung
- **AI Integration Framework:** Multi-Provider mit Budget Gates und Confidence Routing
- **Sample Management:** Remote Testing Pipeline mit Customer Feedback Portal
- **Integration APIs:** Xentral ERP, Allianz Credit, all.inkl Email, AI Services
- **Help System Configuration:** Sales Manager spezifische Admin-Workflows

### Dependencies:
- **Phase 1 Completion:** → [Phase 1 Core Technical Concept](../phase-1-core/technical-concept.md)
- **Modul 06 Settings:** → [Settings Registry Integration](../shared/dependencies.md)
- **External Systems:** → [Xentral Integration Findings](analyse/03_XENTRAL_ALLIANZ_INTEGRATION_FINDINGS.md)
- **Variable Migrations:** → `./scripts/get-next-migration.sh` für SQL Deployment

## 🏗️ **Technical Architecture Overview**

### **Core Sub-Domains (08D/08E/08F):**
```yaml
08D - External Integrations:
  ✅ Generic Integration Framework (Hybrid Pattern)
  ✅ Provider-Specific Adapters (Xentral, Allianz, all.inkl, AI)
  ✅ Circuit Breaker + Rate Limiting + Health Monitoring
  ✅ Webhook Management + Event Processing

08E - Help System Configuration:
  ✅ One-Engine-Two-Spaces (enduser vs salesops)
  ✅ AI-Enhanced Content Generation
  ✅ Sales Manager Workflow Guidance
  ✅ A/B Testing + Analytics Framework

08F - Advanced System Tools:
  ✅ Lead Protection Background Jobs
  ✅ Performance Monitoring + Business KPIs
  ✅ Backup Management + Log Analytics
  ✅ System Health Dashboards
```

### **Lead-Protection Business Logic:**
```sql
-- v_lead_protection View (Kernstück)
valid_until = GREATEST(
  assigned_at + 6 months + hold_duration_since(),
  last_qual_activity + 60 days + hold_duration_since()
)

Status Pipeline: ACTIVE → GRACE (10 days) → EXPIRED
```

### **AI Integration Framework:**
```java
// Confidence-based Multi-Provider Routing
public AiResponse call(AiUseCase useCase, AiRequest req) {
  // 1) Budget Gate per Org/User
  // 2) Cache Lookup (idempotent)
  // 3) Small-First Routing (confidence threshold)
  // 4) Large Fallback + Audit
}
```

## 🛠️ Implementation Phases

### **Phase 2A: Foundation Backend (Week 1)**
**Goal:** Core Backend Services + Database Schema

**Actions:**
- [ ] Deploy SQL Schemas: Lead Protection + Sample Management + Settings
- [ ] Implement Lead Protection Service with v_lead_protection View
- [ ] Create AI Router Interceptor with Multi-Provider Support
- [ ] Setup Background Job Scheduler für Daily Reminder Engine
- [ ] Configure Settings Registry Integration (Modul 06)

**Code Changes:**
```sql
-- Migration Files (dynamic numbering)
V$(get-next-migration.sh)__lead_protection_and_rls.sql
V$(get-next-migration.sh)__sample_cdm_extension.sql
V$(get-next-migration.sh)__settings_lead_ai.sql
```

```java
// Core Services
LeadProtectionService.java
AiRouterInterceptor.java (mit OpenAI + Anthropic Clients)
SampleManagementService.java
LeadProtectionScheduler.java (Background Jobs)
```

**Success Criteria:**
- v_lead_protection View korrekt berechnet 6M+60T+Stop-the-Clock
- AI Confidence Routing funktional (Small→Large Escalation)
- Background Jobs laufen täglich idempotent
- Settings Integration mit Modul 06 funktional

### **Phase 2B: Integration Services (Week 2)**
**Goal:** External Provider Integration + REST APIs

**Actions:**
- [ ] Implement Xentral ERP Adapter (Sales-focused scope)
- [ ] Implement Allianz Credit Check Integration
- [ ] Implement all.inkl Email Service Integration
- [ ] Create REST APIs für Lead Protection + Collaboration
- [ ] Deploy Sample Feedback Portal (Customer-facing)

**Code Changes:**
```java
// Integration Adapters
XentralSalesConnector.java
AllianzCreditService.java
AllInklEmailService.java

// REST Controllers (OpenAPI 3.1 compliant)
LeadProtectionController.java
LeadCollaborationController.java
SampleManagementController.java
```

```typescript
// Frontend Integration
FeedbackFormPublic.tsx (Customer Portal)
// Backend Integration mit bestehenden Admin Dashboards
```

**Success Criteria:**
- Xentral Product Portfolio Pull funktional
- Allianz Credit Check <300ms P95 Response Time
- Sample Feedback Portal Customer-accessible via Token
- OpenAPI Endpoints RFC7807 compliant
- Lead Collaboration Multi-Contact Support

### **Phase 2C: Advanced Features (Week 3 - Optional)**
**Goal:** Performance Optimization + Advanced AI Features

**Actions:**
- [ ] Implement Materialized Views für Lead Protection (10k+ Scale)
- [ ] Add Advanced AI Use Cases (ROI Recommendations, Sample Matching)
- [ ] Create Business KPI Dashboards (Lead Protection, AI Costs, Performance)
- [ ] Implement Circuit Breaker Patterns für External APIs
- [ ] Add Real-time Monitoring + Alerting

**Code Changes:**
```sql
-- Performance Optimization
CREATE MATERIALIZED VIEW mv_lead_protection_summary...
REFRESH MATERIALIZED VIEW CONCURRENTLY...
```

```java
// Advanced AI Features
RoiRecommendationService.java
SampleProductMatchingService.java
CircuitBreakerManager.java
```

**Success Criteria:**
- 5x Seasonal Load Support (500 concurrent Credit Checks)
- AI Cost Tracking <1% Budget variance
- Business KPI Dashboards real-time functional
- Circuit Breaker prevents cascade failures

## ✅ Success Metrics

### **Functional Metrics:**
- **Lead Protection Compliance:** 100% Handelsvertretervertrag konform
- **AI Integration Coverage:** 8 Use Cases (4 Small Model, 4 Large Model)
- **Sample Management:** End-to-End Pipeline Customer → Feedback → Activity
- **API Performance:** Credit Check p95 <300ms, Peak Load p95 <500ms

### **Business Metrics:**
- **Lead Protection Automation:** 60-Tage-Checks + 10-Tage-Grace vollautomatisch
- **AI Cost Efficiency:** Small-First Routing 70-80% Cost Reduction
- **Sample Feedback Rate:** >50% Customer Response via Remote Portal
- **Admin Efficiency:** Multi-Contact Collaboration reduces Lead conflicts

### **Technical Metrics:**
- **Foundation Standards:** 95%+ Compliance (RLS, ABAC, RFC7807)
- **Test Coverage:** >90% Backend Services, >80% Integration Tests
- **Documentation Coverage:** 100% OpenAPI, SQL Comments, Code Documentation
- **Deployment Ready:** Variable Migration Support, Zero-downtime deployment

## 🔗 Technical References

**Foundation Knowledge:**
- **Security Standards:** → [ABAC Implementation](../../phase-1-core/technical-concept.md#abac-security)
- **API Standards:** → [OpenAPI 3.1 + RFC7807](../../../grundlagen/API_STANDARDS.md)
- **Database Standards:** → [RLS + Named Parameters](../../../grundlagen/DATABASE_STANDARDS.md)

**Implementation Details:**
- **Code Location:** `backend/modules/admin/phase2/`, `frontend/features/admin/`
- **SQL Files:** `backend/db/migrations/V*__lead_protection_*.sql`
- **OpenAPI Specs:** `backend/openapi/phase2/*.yaml`
- **Test Files:** `backend/test/admin/phase2/`, `frontend/test/admin/`

**External Artifacts:**
- **Production-Ready Code:** → [Go-Live Artifacts](artefakte/)
- **SQL Schemas:** → [Lead Protection RLS](artefakte/lead_protection_and_rls.sql)
- **Java Implementations:** → [AI Provider Clients](artefakte/OpenAiClient.java)
- **OpenAPI Specs:** → [REST API Documentation](artefakte/lead-protection.yaml)

## 🔧 **Implementation Architecture**

### **Database Layer:**
```sql
-- Core Tables
lead_protection_hold (Stop-the-Clock Intervals)
lead_protection_reminder (60T/10T Pipeline Tracking)
lead_collaborator (Multi-Contact Management)
sample_box, test_phase, test_result, product_feedback

-- Critical Views
v_lead_protection (Business Logic: 6M+60T+Holds)
v_lead_collaborators (ABAC Integration)

-- RLS Security
Fail-closed Policies für ACTIVE/GRACE Lead Protection
```

### **Service Layer:**
```java
// Core Business Services
LeadProtectionService (ABAC + Business Rules)
AiRouterInterceptor (Multi-Provider + Budget Gates)
SampleManagementService (Remote Testing Pipeline)
IntegrationHealthService (Circuit Breaker + Monitoring)

// External Adapters
XentralSalesConnector (Product Portfolio + Orders)
AllianzCreditService (Credit Checks + Risk Assessment)
AllInklEmailService (IMAP/SMTP + KAS API)
OpenAiClient, AnthropicClient (AI Providers)
```

### **API Layer:**
```yaml
REST Endpoints (OpenAPI 3.1):
  GET /api/leads/{id}/protection
  POST /api/leads/{id}/holds (Stop-the-Clock)
  POST /api/leads/{id}/collaborators (Multi-Contact)
  POST /api/sample-boxes (Test Configuration)
  POST /api/feedback/{token} (Customer Portal - Public)

Authentication: Bearer Token (existing ABAC)
Error Handling: RFC7807 Problem Details
```

### **Frontend Integration:**
```typescript
// Existing Admin Dashboards (60% Complete)
IntegrationsDashboard.tsx → Real Data Integration
HelpConfigDashboard.tsx → AI Content Generation
SystemDashboard.tsx → Business KPI Integration

// New Customer-Facing
FeedbackFormPublic.tsx → Token-based Sample Feedback
```

## 🚨 **Critical Implementation Notes**

### **Variable Migration Strategy:**
```bash
# Dynamic Migration Numbers
cp artefakte/lead_protection_and_rls.sql \
   sql/V$(./scripts/get-next-migration.sh)__lead_protection_and_rls.sql

# Preserves audit trail in SQL header
-- Migration: V123__lead_protection_and_rls.sql
-- External AI Generated: 2025-09-20
```

### **Settings Registry Integration:**
```sql
-- Scope-specific Policy Keys
Global: lead.protection.baseMonths = 6 (Contract)
Org: ai.budget.monthly.cap = 1000 (Business)
User: ai.routing.confidence.threshold = 0.7 (Preference)
```

### **AI Provider Configuration:**
```bash
# Environment Variables
OPENAI_API_KEY=sk-...
ANTHROPIC_API_KEY=ant-...
OPENAI_MODEL_SMALL=gpt-4o-mini
OPENAI_MODEL_LARGE=gpt-4o
```

### **Performance Considerations:**
```yaml
Scale Planning:
  10k+ Leads: Materialized View mv_lead_protection_summary
  500+ Concurrent: Credit Check Caching + Micro-Batching
  Multi-Tenant: RLS Query Optimization + Index Tuning
```

## 🔄 **Risk Mitigation**

### **Technical Risks:**
- **External API Outages:** Circuit Breaker + Graceful Degradation
- **AI Cost Explosion:** Budget Gates + User/Org Limits
- **Lead Protection Conflicts:** Multi-Contact Collaboration Framework
- **Performance Degradation:** Caching + Materialized Views + Monitoring

### **Business Risks:**
- **Handelsvertretervertrag Non-Compliance:** Comprehensive Business Logic Testing
- **Sample Feedback Low Rate:** Token-based Simple UX + Mobile Optimization
- **AI Provider Lock-in:** Multi-Provider Abstraction Layer
- **Data Privacy Issues:** RLS + PII Scrubbing + DSGVO Compliance

## 🤖 Claude Handover Section

### **Context für nächsten Claude:**
```yaml
Status: Phase 2 Technical Concept Complete
Artifacts: Production-ready Code in /artefakte verfügbar
Next Steps: Implementation Roadmap + Deployment Planning

Key Decisions Made:
  - Backend-First Approach (60% UI bereits vorhanden)
  - Hybrid Integration Framework (Core + Provider Extensions)
  - Multi-Provider AI Strategy (Confidence-based Routing)
  - Lead-Protection via v_lead_protection View
  - Sample Management mit Remote Customer Portal

Critical Implementation Ready:
  - SQL Schemas (296 lines tested)
  - Java Services (394 lines production-ready)
  - OpenAPI Specs (3 comprehensive files)
  - React Components (Customer-facing portal)
```

### **Implementation Priority:**
1. **Week 1:** Database + Core Services + AI Framework
2. **Week 2:** External Integrations + REST APIs + Customer Portal
3. **Week 3:** Performance + Advanced Features + Monitoring

### **Success Validation:**
- Lead Protection Business Logic Tests
- AI Confidence Routing Verification
- Sample Feedback End-to-End Testing
- Performance Benchmarking (Normal + Peak Load)

---

**📋 Ready for:** [Implementation Roadmap](implementation-roadmap.md) basierend auf diesem Technical Concept