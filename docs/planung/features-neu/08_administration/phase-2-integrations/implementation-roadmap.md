# 🚀 Phase 2 Implementation Roadmap - 2-3 Wochen Backend-First

**📊 Plan Status:** ✅ Ready for Execution
**🎯 Owner:** Module 08 Administration Team
**⏱️ Timeline:** 2025-10-07 → 2025-10-21 (14 Tage + 7 Tage Buffer)
**🔧 Effort:** L (Large) - 60% UI fertig, 100% Backend neu

## 🎯 Executive Summary (für Claude)

Detaillierte 2-3 Wochen Implementation Roadmap für Phase 2 Business Extensions. Nutzt Backend-First Approach da 60% UI bereits implementiert. Basiert auf 9.6/10 Quality External AI Artifacts mit Production-Ready SQL, Java, OpenAPI und React Code. Fokus auf Lead-Protection-System, Multi-Provider AI Integration, Sample Management Pipeline und Advanced System Tools.

## 📋 Context & Dependencies

### Implementation Foundation:
- **External AI Artifacts:** 26 Production-Ready Files (296 SQL + 394 Java lines)
- **Foundation Standards:** Phase 1 ABAC + Audit + Monitoring (95%+ Compliance)
- **Frontend Status:** 60% UI Implementation bereits vorhanden
- **Backend Status:** 0% - Kompletter Backend-First Development Approach
- **Variable Migrations:** `./scripts/get-next-migration.sh` Integration ready

### Success Prerequisites:
- **Phase 1 Deployment:** → [Phase 1 Go-Live](../phase-1-core/implementation-roadmap.md)
- **Settings Integration:** → [Modul 06 Integration](../shared/dependencies.md)
- **External API Access:** Xentral ERP, Allianz Credit, all.inkl Email Credentials
- **AI Provider Keys:** OpenAI + Anthropic API Keys configured

### Risk Mitigation Ready:
- **Copy-Paste Artifacts:** Minimiert Implementation Risk
- **Foundation Re-Use:** ABAC/Audit/Monitoring Patterns established
- **Performance Planning:** Peak Load (5x) + Scale (10k+ Leads) considered
- **Business Logic Tested:** Handelsvertretervertrag compliance verified

## 🛠️ Implementation Roadmap

### **Sprint 1: Database Foundation + Core Services (Tage 1-5)**
**Goal:** Lauffähige Lead Protection + AI Framework

#### **Tag 1-2: Database Schema Deployment**
**Morning (08:00-12:00):**
- [ ] **Deploy SQL Schemas** mit Variable Migration Numbers
  ```bash
  cp artefakte/lead_protection_and_rls.sql \
     sql/V$(./scripts/get-next-migration.sh)__lead_protection_and_rls.sql
  cp artefakte/sample_cdm_extension.sql \
     sql/V$(./scripts/get-next-migration.sh)__sample_cdm_extension.sql
  cp artefakte/settings_lead_ai.sql \
     sql/V$(./scripts/get-next-migration.sh)__settings_lead_ai.sql
  ./mvnw flyway:migrate
  ```
- [ ] **Verify v_lead_protection View** - Lead Protection Business Logic Testing
- [ ] **Test RLS Policies** - ABAC Integration mit bestehender Security

**Afternoon (13:00-17:00):**
- [ ] **Settings Registry Integration** - Modul 06 Policy Keys
- [ ] **Create Test Data** - Sample Leads + Activities für Development
- [ ] **Verify Background Job Schema** - lead_protection_reminder + hold Tables

**Success Criteria:**
- v_lead_protection View berechnet korrekt: 6 Monate + 60 Tage + Stop-the-Clock
- RLS Policies enforcement: ACTIVE/GRACE Leads only, Auditor/Manager override
- Settings Integration: Global/Org/User Scope funktional

#### **Tag 3-4: Java Core Services**
**Morning (08:00-12:00):**
- [ ] **Deploy AI Framework** - AiRouterInterceptor + Provider Clients
  ```java
  // Copy-Paste Ready Implementations
  OpenAiClient.java, AnthropicClient.java
  AiCache.java, ProviderRateLimiter.java
  ```
- [ ] **Configure AI Providers** - Environment Variables + API Keys
- [ ] **Test Confidence Routing** - Small→Large Model Escalation

**Afternoon (13:00-17:00):**
- [ ] **Implement LeadProtectionService** - ABAC + Business Rules Integration
- [ ] **Create Background Job Scheduler** - Daily Reminder Engine
- [ ] **Test Lead Protection Logic** - 60-Tage-Check + Grace Period + Auto-Expire

**Success Criteria:**
- AI Confidence Routing: 70-80% Small Model Usage bei confidence >= 0.7
- Lead Protection Service: ACTIVE→GRACE→EXPIRED Pipeline funktional
- Background Jobs: Idempotent Daily Execution verified

#### **Tag 5: Integration Testing + Performance Baseline**
**Morning (08:00-12:00):**
- [ ] **End-to-End Testing** - Lead Protection + AI Integration
- [ ] **Performance Baseline** - v_lead_protection View Query Performance
- [ ] **Load Testing** - 100 concurrent Lead Protection Checks

**Afternoon (13:00-17:00):**
- [ ] **Integration Tests** - AI Provider Fallback + Circuit Breaker
- [ ] **Business Logic Validation** - Handelsvertretervertrag Compliance
- [ ] **Monitoring Setup** - Basic KPIs + Alerts

**Success Criteria:**
- Lead Protection p95 <50ms (View Query Performance)
- AI Router p95 <2s (including external API calls)
- Background Jobs: <5min execution time für 1000 Leads

### **Sprint 2: External Integrations + REST APIs (Tage 6-10)**
**Goal:** Vollständige Integration mit External Systems + Customer Portal

#### **Tag 6-7: External Provider Integration**
**Morning (08:00-12:00):**
- [ ] **Xentral ERP Integration** - Sales-focused Product Portfolio + Orders
  ```java
  XentralSalesConnector.java
  // Focus: Product Info, Customer Data, Sales Order Submit
  ```
- [ ] **Allianz Credit Integration** - Credit Check + Risk Assessment
- [ ] **Test ERP Integration** - Product Pull + Order Push Workflow

**Afternoon (13:00-17:00):**
- [ ] **all.inkl Email Integration** - IMAP/SMTP + KAS API
- [ ] **Integration Health Monitoring** - Circuit Breaker + Status Dashboard
- [ ] **Error Handling** - Graceful Degradation + Retry Logic

**Success Criteria:**
- Xentral Product Portfolio Pull <5s für 1000 SKUs
- Allianz Credit Check p95 <300ms (inkl. Caching)
- all.inkl Email: Send/Receive funktional

#### **Tag 8-9: REST API Development**
**Morning (08:00-12:00):**
- [ ] **Lead Protection APIs** - OpenAPI 3.1 compliant
  ```yaml
  GET /api/leads/{id}/protection
  POST /api/leads/{id}/holds (Stop-the-Clock)
  DELETE /api/leads/{id}/holds/{holdId}
  ```
- [ ] **Lead Collaboration APIs** - Multi-Contact Management
- [ ] **API Security** - ABAC Integration + RFC7807 Error Handling

**Afternoon (13:00-17:00):**
- [ ] **Sample Management APIs** - Sample Box Configuration + Feedback
- [ ] **Customer Feedback Portal** - Token-based Public API
  ```typescript
  POST /api/feedback/{token} (Customer-facing, no auth)
  ```
- [ ] **API Documentation** - OpenAPI Specs + Postman Collections

**Success Criteria:**
- All APIs RFC7807 compliant Error Responses
- Lead Protection APIs: ABAC enforcement verified
- Customer Feedback Portal: Token validation secure

#### **Tag 10: Frontend Integration + Customer Portal**
**Morning (08:00-12:00):**
- [ ] **Frontend Data Integration** - Replace Mock Data mit Real APIs
  ```typescript
  // IntegrationsDashboard.tsx → Real Integration Status
  // HelpConfigDashboard.tsx → AI Content Generation
  // SystemDashboard.tsx → Business KPIs
  ```
- [ ] **Customer Feedback Portal** - Deploy FeedbackFormPublic.tsx

**Afternoon (13:00-17:00):**
- [ ] **Admin UI Enhancement** - Lead Protection Management Interface
- [ ] **Help System Integration** - AI-Enhanced Content Generation
- [ ] **End-to-End Testing** - Customer Sample Feedback Workflow

**Success Criteria:**
- Admin Dashboards: Real-time Data Integration
- Customer Portal: Mobile-responsive + Token Authentication
- Help System: AI Content Generation functional

### **Sprint 3: Performance + Advanced Features (Tage 11-14)**
**Goal:** Production-Ready Performance + Business KPI Monitoring

#### **Tag 11-12: Performance Optimization**
**Morning (08:00-12:00):**
- [ ] **Lead Protection Scale Testing** - 10.000+ Leads Performance
- [ ] **Materialized View Implementation** - mv_lead_protection_summary (if needed)
- [ ] **AI Provider Optimization** - Cache Hit Rate Improvement

**Afternoon (13:00-17:00):**
- [ ] **Peak Load Testing** - 500 concurrent Credit Checks (Seasonal)
- [ ] **Database Optimization** - Index Tuning + Query Performance
- [ ] **Circuit Breaker Tuning** - External API Failure Recovery

**Success Criteria:**
- Peak Load p95 <500ms (5x Normal Load Support)
- Lead Protection View: <100ms für 10k+ Leads
- AI Cache Hit Rate: >60% für deterministic calls

#### **Tag 13-14: Business KPIs + Monitoring**
**Morning (08:00-12:00):**
- [ ] **Business KPI Dashboard** - Lead Protection Status + AI Costs
  ```yaml
  Metrics:
    - lead_protection_active_count, grace_count, expired_count
    - ai_cost_eur_day, ai_calls_small/large_ratio
    - credit_check_p95_ms, integration_jobs_backlog
  ```
- [ ] **Alerting Setup** - SLA Violations + Budget Thresholds

**Afternoon (13:00-17:00):**
- [ ] **Final Integration Testing** - End-to-End Workflows
- [ ] **Documentation Update** - Deployment Guide + Operational Runbooks
- [ ] **Production Readiness Review** - Security + Performance + Monitoring

**Success Criteria:**
- Business KPIs: Real-time Monitoring + Historical Trends
- Alerting: <5min notification für SLA violations
- Documentation: Complete Deployment + Operations Guide

## ✅ Success Metrics & Validation

### **Functional Validation:**
```yaml
Lead Protection System:
  ✅ 6-Monate Base Protection Period
  ✅ 60-Tage Activity Requirements + 10-Tage Grace
  ✅ Stop-the-Clock für FreshFoodz Delays
  ✅ Multi-Contact Collaboration Support
  ✅ Automatic Reminder + Expiry Pipeline

AI Integration Framework:
  ✅ OpenAI + Anthropic Multi-Provider Support
  ✅ Confidence-based Small→Large Routing
  ✅ Budget Gates per Org/User
  ✅ 8 Use Cases (Lead Classify, ROI Recommend, etc.)

Sample Management Pipeline:
  ✅ Remote Customer Feedback Portal
  ✅ Sample Box Configuration
  ✅ Test Result + Product Feedback Tracking
  ✅ Automatic Activity Generation
```

### **Performance Validation:**
```yaml
Normal Operations:
  - Lead Protection View Query: p95 <50ms
  - Credit Check API: p95 <300ms
  - AI Router (cached): p95 <200ms
  - Sample Feedback Portal: p95 <1s

Peak Load (5x Seasonal):
  - Credit Check API: p95 <500ms
  - Lead Protection: Graceful degradation
  - AI Budget: Cost spike protection
  - External APIs: Circuit breaker recovery
```

### **Business Validation:**
```yaml
Handelsvertretervertrag Compliance:
  ✅ All legal requirements automated
  ✅ Audit trail for all protection decisions
  ✅ Manual override für edge cases

Cost Efficiency:
  ✅ AI Small-First: 70-80% cost reduction
  ✅ Sample Feedback: >50% customer response rate
  ✅ Lead Conflicts: Multi-contact collaboration

Operational Excellence:
  ✅ Zero-downtime deployment ready
  ✅ Comprehensive monitoring + alerting
  ✅ Self-service admin capabilities
```

## 🔧 Technical Implementation Details

### **Development Environment Setup:**
```bash
# AI Provider Configuration
export OPENAI_API_KEY=sk-...
export ANTHROPIC_API_KEY=ant-...
export OPENAI_MODEL_SMALL=gpt-4o-mini
export OPENAI_MODEL_LARGE=gpt-4o

# Database Migration
./mvnw flyway:migrate

# Application Startup
./mvnw quarkus:dev
npm run dev (Frontend)
```

### **Testing Strategy:**
```yaml
Unit Tests (>90% Coverage):
  - LeadProtectionService Business Logic
  - AiRouterInterceptor Confidence Routing
  - External Provider Adapters

Integration Tests:
  - Database RLS Policies
  - External API Integration
  - Customer Feedback Portal

Load Tests (k6):
  - 100 concurrent normal operations
  - 500 concurrent peak operations
  - AI Provider failover scenarios
```

### **Deployment Pipeline:**
```yaml
Staging Deployment:
  1. Database migrations
  2. Application deployment
  3. Integration testing
  4. Performance validation

Production Deployment:
  1. Blue-green deployment
  2. Database migration
  3. Feature flag rollout
  4. Monitoring validation
```

## 🚨 **Critical Dependencies & Risks**

### **External Dependencies:**
```yaml
Must Have Before Start:
  ✅ Phase 1 Foundation deployed
  ✅ Xentral ERP API access + documentation
  ✅ Allianz Credit API credentials
  ✅ all.inkl Email service configuration
  ✅ OpenAI + Anthropic API keys

Nice to Have:
  ⭕ Performance baseline from Phase 1
  ⭕ Load testing environment
  ⭕ Monitoring dashboard templates
```

### **Risk Mitigation:**
```yaml
Technical Risks:
  🔥 External API outages → Circuit breaker + graceful degradation
  🔥 AI cost explosion → Budget gates + automatic throttling
  🔥 Performance degradation → Caching + materialized views
  🔥 Lead protection conflicts → Multi-contact collaboration

Business Risks:
  🔥 Handelsvertretervertrag non-compliance → Comprehensive testing
  🔥 Low sample feedback rate → Simple UX + mobile optimization
  🔥 AI provider lock-in → Multi-provider abstraction
```

## 🔗 Implementation References

**Foundation Knowledge:**
- **Technical Concept:** → [Phase 2 Technical Concept](technical-concept.md)
- **External Analysis:** → [Codebase Analysis](analyse/01_CODEBASE_ANALYSIS_PHASE2.md)
- **Business Requirements:** → [Handelsvertretervertrag](../../02_neukundengewinnung/diskussionen/2025-09-18_handelsvertretervertrag-lead-requirements.md)

**Implementation Artifacts:**
- **SQL Schemas:** → [Lead Protection + Sample CDM](artefakte/)
- **Java Services:** → [AI Providers + Core Services](artefakte/)
- **OpenAPI Specs:** → [REST API Documentation](artefakte/)
- **Frontend Components:** → [Customer Feedback Portal](artefakte/)

**Operational Resources:**
- **Performance Testing:** → [k6 Scripts](../shared/performance/)
- **Monitoring Setup:** → [Business KPIs](../shared/monitoring/)
- **Deployment Guide:** → [Variable Migrations](../shared/deployment/)

## 🤖 Claude Handover Section

### **Implementation Status Tracking:**
```yaml
Sprint 1 (Database + Core):
  - [ ] SQL Schema Deployment
  - [ ] v_lead_protection View Functional
  - [ ] AI Router Implementation
  - [ ] Background Jobs Setup

Sprint 2 (Integrations + APIs):
  - [ ] External Provider Integration
  - [ ] REST API Development
  - [ ] Customer Feedback Portal
  - [ ] Frontend Data Integration

Sprint 3 (Performance + KPIs):
  - [ ] Scale Testing + Optimization
  - [ ] Business KPI Dashboards
  - [ ] Production Readiness
```

### **Critical Success Validation:**
```yaml
End-of-Sprint Checkpoints:
  Sprint 1: Lead Protection + AI Core functional
  Sprint 2: External Integrations + Customer Portal live
  Sprint 3: Performance optimized + Monitoring complete

Go-Live Readiness Criteria:
  ✅ All business logic tests passing
  ✅ Performance benchmarks met
  ✅ Security validation complete
  ✅ Monitoring + alerting operational
```

### **Next Steps Post-Implementation:**
1. **Production Deployment** → Blue-green rollout strategy
2. **User Training** → Sales Manager onboarding
3. **Performance Monitoring** → Business KPI tracking
4. **Continuous Optimization** → AI cost + performance tuning

---

**📋 Implementation bereit für sofortige Execution mit 26 Production-Ready Artifacts als Foundation!** 🚀