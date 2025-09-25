# Kommunikation - Technical Concept (Hybrid-Synthese)

> **RLS-Status (Sprint 1.6):** ✅ @RlsContext CDI-Interceptor verpflichtend
> 🔎 Details: [ADR-0007](../../adr/ADR-0007-rls-connection-affinity.md) · [Security Update](../../SECURITY_UPDATE_SPRINT_1_5.md)

**📊 Plan Status:** ✅ Ready for Implementation (Hybrid-Synthese: Externe KI MVP + Claude Foundation Standards)
**🎯 Owner:** Backend + Frontend Team
**⏱️ Timeline:** Q4 2025 Woche 7-12 (10-12 Wochen Implementation)
**🔧 Effort:** L (Large - Shared Communication Core mit Foundation Standards Excellence)

## 🎯 Executive Summary (für Claude)

**Mission:** Shared Communication Core mit Email + Phone + Meeting-Notes für B2B-Convenience-Food-Vertrieb mit 95% Foundation Standards Compliance und production-ready MVP-Approach.
**Problem:** Module 02 + 05 benötigen Email-Integration ohne Code-Duplikation + Enterprise-Grade Communication-Management für Gastronomy-Sales-Cycles.
**Solution:** Hybrid-Synthese aus externer KI's pragmatischem Domain-Modell (MailAccount, Thread, Message, SLA-Engine) + Claude's Foundation Standards Excellence (ABAC Security, 95% Compliance, de.freshplan.* Package-Struktur).
**Timeline:** 10-12 Wochen für MVP mit Enterprise-Grade-Quality - Kompromiss zwischen 8 Wochen MVP und 16 Wochen Over-Engineering.
**Impact:** Shared Communication Core für Module 02/05 + Sample-Follow-up-Engine + Multi-Kontakt-B2B-Communication mit Territory-basierter ABAC-Security.

**Quick Context:** Basiert auf strategischer Diskussion zwischen Claude + externer KI mit kritischer Würdigung (8.5/10). Externe KI überzeugte mit Domain-Modell-Tiefe und MVP-Pragmatismus, Claude's Foundation Standards Integration bleibt für Enterprise-Grade-Quality erforderlich.

## 📋 Context & Dependencies

### Current State:
- ✅ **Strategische Diskussion abgeschlossen:** Claude's Meinung + Externe KI-Analyse + Kritische Würdigung
- ✅ **Email-Architektur-Entscheidung:** Option C - Shared Communication Core (beide KIs einig)
- ✅ **Scope-Definition:** Email + Phone + Meeting-Notes MVP (Externe KI's pragmatischer Ansatz)
- ✅ **Foundation Standards Basis:** 95% Compliance-Matrix (Claude's Excellence-Standard)
- ✅ **Domain-Modell:** MailAccount, Thread, Message, SLA-Engine (Externe KI's production-ready Design)

### Target State:
- ✅ **Shared Communication Core:** Einheitliche Email/Phone/Meeting-Integration für Module 02 + 05
- ✅ **B2B-Food-Features:** Sample-Follow-up-Engine (T+3/T+7), Multi-Kontakt-Communication, Seasonal-Campaigns
- ✅ **Enterprise-Security:** Territory-basierte ABAC + RLS mit JWT-Claims-Processing
- ✅ **Production-Grade:** Bounce-Handling, SLA-Engine, Deliverability (DKIM/DMARC/SPF)
- ✅ **Foundation Standards:** 95% Compliance mit de.freshplan.* Package-Struktur

### Dependencies:
- **Externe KI Artefakte:** → OpenAPI 3.1 Specs, SQL-Projektionen, Domain-Entities, React-Components
- **Foundation Standards:** → [FOUNDATION_STANDARDS_COMPLIANCE_REQUEST.md](../diskussionen/2025-09-19_FOUNDATION_STANDARDS_COMPLIANCE_REQUEST.md)
- **Cross-Module-Integration:** → Customer-Management (Module 03), Audit-System, Event-Bus

## 📦 **PRODUCTION-READY ARTEFAKTE**

### **Backend (Java/Quarkus) - 19 Dateien**

#### **Domain-Entities (7 Dateien)**
- `./artefakte/backend/MailAccount.java` - Email-Account-Management
- `./artefakte/backend/ParticipantSet.java` - Email-Participants (TO/CC/BCC)
- `./artefakte/backend/Thread.java` - Communication-Threads (@Version für ETag)
- `./artefakte/backend/Message.java` - Email/Phone/Meeting-Messages
- `./artefakte/backend/OutboxEmail.java` - Reliable Email-Delivery mit Retry
- `./artefakte/backend/BounceEvent.java` - Email-Bounce-Tracking (HARD/SOFT)
- `./artefakte/backend/CommActivity.java` - Phone/Meeting-Activity-Logging

#### **Repository-Layer (1 Datei)**
- `./artefakte/backend/CommunicationRepository.java` - ABAC-Scoped CRUD + Cursor-Pagination

#### **API-Resources (3 Dateien)**
- `./artefakte/backend/CommThreadResource.java` - Thread-Management + ETag-Reply
- `./artefakte/backend/CommMessageResource.java` - Email-Conversation-Starter
- `./artefakte/backend/CommActivityResource.java` - Phone/Meeting-Logging-APIs

#### **Background-Workers (2 Dateien)**
- `./artefakte/backend/EmailOutboxProcessor.java` - Scheduled Email-Sender (Exponential-Backoff)
- `./artefakte/backend/BounceEventHandler.java` - Webhook für Email-Bounces

#### **SLA-Engine (3 Dateien)**
- `./artefakte/backend/SLAEngine.java` - Sample-Follow-up-Logic (T+3/T+7)
- `./artefakte/backend/SLARulesProvider.java` - YAML-Configuration-Loader
- `./artefakte/backend/SLAWorker.java` - Scheduled SLA-Task-Processor

#### **Common (1 Datei)**
- `./artefakte/backend/ProblemExceptionMapper.java` - RFC7807 Error-Handling

#### **Testing (2 Dateien)**
- `./artefakte/backend/CommThreadResourceBDDTest.java` - ETag-Concurrency BDD-Tests
- `./artefakte/backend/SLAEngineBDDTest.java` - SLA-Engine BDD-Tests

### **Frontend (React/TypeScript) - 9 Dateien**

#### **Components (4 Dateien)**
- `./artefakte/frontend/ThreadList.tsx` - Communication-Timeline-Overview
- `./artefakte/frontend/ThreadDetail.tsx` - Thread-Detailansicht mit Reply
- `./artefakte/frontend/ReplyComposer.tsx` - Email-Reply-Component (ETag-Safe)
- `./artefakte/frontend/QuickLogDialog.tsx` - Phone/Meeting-Logging-Dialog

#### **Types & Hooks (2 Dateien)**
- `./artefakte/frontend/communication.ts` - TypeScript-Types für Thread/Message
- `./artefakte/frontend/useCommunication.ts` - React-Hooks für API-Integration

#### **Services (1 Datei)**
- `./artefakte/frontend/apiClient.ts` - HTTP-Client für Communication-APIs

#### **Design-System (2 Dateien)**
- `./artefakte/frontend/theme-v2.mui.ts` - Material-UI Theme V2 (Token-basiert)
- `./artefakte/frontend/theme-v2.tokens.css` - CSS-Tokens (FreshFoodz CI)

### **Database (SQL/PostgreSQL) - 1 Datei**
- `./artefakte/sql-schemas/communication_core.sql` - Complete Database-Schema mit Tables, RLS-Policies, Indices und ENUMs

### **API-Specifications (OpenAPI 3.1) - 5 Dateien**
- `./artefakte/api-specs/comm-threads.yaml` - Thread-Management-APIs (GET/POST/Reply)
- `./artefakte/api-specs/comm-messages.yaml` - Message-Creation-APIs (Email-Start)
- `./artefakte/api-specs/comm-calls-meetings.yaml` - Activity-Logging-APIs (Phone/Meeting)
- `./artefakte/api-specs/comm-common-errors.yaml` - RFC7807 Error-Schemas
- `./artefakte/api-specs/sla-rules.yaml` - B2B-Food SLA-Rules Configuration

### **Testing (5 Dateien)**
- `./artefakte/testing/ABACRlsSecurityIT.java` - Hybrid: KI-Framework + echte ABAC-Tests
- `./artefakte/testing/ThreadList.unit.test.tsx` - React-Component-Tests mit Business-Logic
- `./artefakte/testing/communication.spec.ts` - E2E-User-Journey-Tests
- `./artefakte/testing/jest.config.ts` - Frontend-Test-Configuration
- `./artefakte/testing/playwright.config.ts` - E2E-Test-Configuration

### **DevOps & Monitoring (Integration aus KI-Starter-Paket) - 15+ Dateien**

#### **CI/CD Pipeline (1 Datei)**
- `.github/workflows/communication-enterprise.yml` - Enterprise-CI/CD-Pipeline mit Security-Gates

#### **Container (3 Dateien)**
- `docker/Dockerfile.backend` - Multi-Stage Quarkus Container-Setup
- `docker/Dockerfile.frontend` - React-Nginx Container
- `docker/nginx.conf` - Nginx Configuration für SPA

#### **Kubernetes (6 Dateien)**
- `k8s/base/backend.yaml` - Backend-Deployment + Service
- `k8s/base/frontend.yaml` - Frontend-Deployment + Service
- `k8s/base/kustomization.yaml` - Base-Kustomization
- `k8s/overlays/staging/kustomization.yaml` - Staging-Environment
- `k8s/overlays/prod/kustomization.yaml` - Production-Environment

#### **Monitoring (2 Dateien)**
- `monitoring/grafana/communication_api.json` - Business-Metrics Dashboards
- `monitoring/prometheus/alert_rules.yaml` - Alerting Rules & SLA-Monitoring

#### **Performance Testing (1 Datei)**
- `testing/k6/comm_load_test.js` - Performance Load-Tests mit P95<200ms-Thresholds

#### **Security (2 Dateien)**
- `security/semgrep.yml` - OWASP Security-Scanning-Configuration
- `security/zap-rules.txt` - OWASP ZAP Security-Rules

#### **Operations (1 Datei)**
- `docs/operations/communication-runbook.md` - Operations-Playbooks für Incident-Response

> **🚀 MIGRATION HINWEIS für Production:**
> Bei Production-Start müssen alle Tests aus `/docs/planung/features-neu/05_kommunikation/artefakte/testing/`
> in die neue Enterprise Test-Struktur migriert werden:
> - Backend Tests → `/backend/src/test/java/unit/communication/` bzw. `/backend/src/test/java/integration/communication/`
> - Frontend Tests → `/frontend/src/tests/unit/communication/` bzw. `/frontend/src/tests/e2e/communication/`
> - Performance Tests → `/backend/src/test/java/performance/communication/`
> Siehe [TEST_STRUCTURE_PROPOSAL.md](../../features/TEST_STRUCTURE_PROPOSAL.md) für Details.

## 🛠️ Implementation Phases (Best-of-Both-Worlds)

### **Phase 1: Foundation Setup (1-2 Tage) - BEREITS INTEGRIERT**

**Goals:**
- DevOps-Excellence aus KI-Starter-Paket integriert
- Business-Logic aus alter Planung integriert
- Hybrid-Tests mit echter Business-Validation erstellt

**Completed Deliverables:**
```yaml
✅ Domain-Modell (Externe KI's Design):
- MailAccount, Mailbox, Message, Thread, ParticipantSet
- OutboxEmail, BounceEvent, DeliveryLog, CommActivity
- SLA-Engine für Sample-Follow-ups (T+3/T+7)
- Template-System mit domain-tokens

✅ Foundation Standards Integration (Claude's Excellence):
- Package-Struktur: de.freshplan.communication.*
- ABAC-Security: Territory-basierte RLS + JWT-Claims
- API-Standards: OpenAPI 3.1 + RFC7807 Error-Handling
- JavaDoc + Bean Validation in allen Services

✅ Production-Ready Email-Core:
- SMTP Outbound + IMAP/Webhook Inbound
- DKIM/DMARC/SPF Configuration
- Bounce-Handling (HARD/SOFT) mit Business-Impact
- Rate-Limiting (domain-basiert)
```

**Success Criteria:**
- P95 <200ms für alle Communication-APIs
- 95% Foundation Standards Compliance erreicht
- Email-Send + Bounce-Detection funktional
- Tests ≥85% Coverage mit BDD-Pattern

### **Phase 2: Module-Integration + B2B-Food-Features (Wochen 5-8)**

**Goals:**
- Integration mit Module 02 (Neukundengewinnung) + 05 (Kundenmanagement)
- B2B-Food-spezifische Features: Sample-Follow-ups, Multi-Kontakt, Seasonal-Campaigns
- Phone-Call + Meeting-Notes manual logging

**Key Deliverables:**
```yaml
✅ Module-Integration:
- Module 02: Lead-Email-Processing + BCC-to-CRM + Sequenzen
- Module 05: Customer-Communication + Sample-Follow-ups + Multi-Kontakt
- Event-Bus-Integration: CommunicationLoggedEvent, EmailSentEvent

✅ B2B-Food-Features (Externe KI's Business-Focus):
- Sample-Follow-up-Engine: DELIVERED → T+3, T+7 Follow-ups
- Multi-Kontakt-Threads: Küchenchef + Einkauf im CC, Warnung bei fehlendem Entscheider
- Seasonal-Campaign-Filter: seasonal_window für Weihnachts-/Sommergeschäft
- Bounce-Rückwirkung: HARD → Contact "nicht kontaktierbar"

✅ Manual Communication-Logging:
- Phone-Call-Logging mit Business-Context
- Meeting-Notes + Action-Items + ICS-Export
- Manual Communication-Entry für Ad-hoc-Kontakte
```

**Success Criteria:**
- Sample-Follow-up-Automation funktional (T+3/T+7)
- Multi-Kontakt-Threads mit CC-Validation
- Phone/Meeting-Logging integriert in Timeline
- Cross-Module-Events ohne Lag

### **Phase 3: Production-Hardening + Advanced Features (Wochen 9-12)**

**Goals:**
- Production-Resilience: Monitoring, Alerting, Performance-Optimization
- Advanced Features: Real-time Updates, Communication-Analytics
- Enterprise-Grade-Observability + GDPR-Compliance

**Key Deliverables:**
```yaml
✅ Production-Hardening:
- Grafana-Dashboards: Inbox-Lag, Outbox-Queue, Bounce-Rate, Reply-Time
- Circuit-Breaker: Email-Provider-Failures, graceful degradation
- Background-Jobs: Outbox-Dispatcher, Email-Sync, SLA-Checker
- Data-Retention: 5-Jahre-Policy + Legal-Hold + GDPR-Export

✅ Real-time Features:
- WebSocket-Integration für Live-Badge-Updates
- Communication-Timeline-Updates ohne Refresh
- SLA-Notifications in Real-time

✅ Analytics + Compliance:
- Communication-Effectiveness-Metrics (Reply-Rate, Response-Time)
- Export-Masking für PII-Protection
- Audit-Trail für alle Outbound-Communications
- Performance-Monitoring: P95 <200ms maintained
```

**Success Criteria:**
- Production-Monitoring operational mit <0.5% Error-Rate
- Real-time Updates <5s Latency
- GDPR-Compliance + Export-Masking funktional
- 95% Foundation Standards final validation

## ✅ Success Metrics (Hybrid-Kriterien)

### **Technical Metrics (Foundation Standards):**
- **API Performance:** P95 <200ms für alle Communication-APIs (Claude's Standard)
- **Foundation Standards:** 95% Compliance-Matrix erfüllt (Claude's Excellence)
- **Test Coverage:** ≥85% mit BDD-Pattern für SLA-Regeln (Hybrid)
- **Security:** 100% ABAC Territory-Enforcement + RLS-Isolation

### **Business Metrics (B2B-Food-Focus):**
- **Sample-Follow-up-Success:** >25% Conversion nach T+3/T+7 Follow-ups (Externe KI's KPI)
- **Email-Response-Rate:** >15% (Industry B2B-Benchmark: 8-12%)
- **Multi-Kontakt-Engagement:** >40% beide Kontakte (Küchenchef + Einkauf) antworten
- **Seasonal-Campaign-ROI:** >3x Investment bei Weihnachts-/Sommergeschäft

### **Production Metrics (Enterprise-Grade):**
- **Email-Delivery-Success:** >99.5% ohne Bounces (Externe KI's Production-Standard)
- **Bounce-Recovery:** HARD-Bounces → Contact-Status-Update <1h
- **SLA-Compliance:** Sample-Follow-ups T+3/T+7 in >95% der Fälle
- **Inbox-Lag:** <10s für Inbound-Email-Processing

## 🔗 Related Documentation

### **Strategic Foundation:**
- **Diskussion-Basis:** → [KI Strategische Kommunikations-Architektur](./diskussionen/2025-09-19_KI_STRATEGISCHE_KOMMUNIKATIONS_ARCHITEKTUR.md)
- **Claude's Meinung:** → [Strategische Empfehlung](./diskussionen/2025-09-19_STRATEGISCHE_EMPFEHLUNG_MODUL_05_ARCHITEKTUR.md)
- **Kritische Würdigung:** → [Externe KI-Antwort Bewertung](./diskussionen/2025-09-19_KRITISCHE_WUERDIGUNG_EXTERNE_KI_ANTWORT.md)
- **Codebase-Analyse:** → [Communication Analysis](./analyse/2025-09-19_CODEBASE_COMMUNICATION_ANALYSIS.md)

### **Foundation Standards:**
- **Design Standards:** → [../../grundlagen/DESIGN_SYSTEM.md](../../grundlagen/DESIGN_SYSTEM.md)
- **API Standards:** → [../../grundlagen/API_STANDARDS.md](../../grundlagen/API_STANDARDS.md)
- **Security Guidelines:** → [../../grundlagen/SECURITY_GUIDELINES.md](../../grundlagen/SECURITY_GUIDELINES.md)
- **Testing Guide:** → [../../grundlagen/TESTING_GUIDE.md](../../grundlagen/TESTING_GUIDE.md)

### **Cross-Module Integration:**
- **Module 02 Integration:** → [../02_neukundengewinnung/lead-erfassung/technical-concept.md](../02_neukundengewinnung/lead-erfassung/technical-concept.md)
- **Module 03 Integration:** → [../03_kundenmanagement/technical-concept.md](../03_kundenmanagement/technical-concept.md)
- **Cockpit KPIs:** → [../01_mein-cockpit/technical-concept.md](../01_mein-cockpit/technical-concept.md)

### **Implementation-Artefakte (von externer KI angefordert):**
- **OpenAPI 3.1 Specs:** → `/api/comm/threads`, `/api/comm/messages`, `/api/comm/calls`
- **Domain-Entities:** → MailAccount, Thread, Message, SLA-Engine
- **SQL-Projektionen:** → RLS für Threads, Communication-KPIs, Performance-Indices
- **React-Components:** → Thread-List/Detail/Reply auf Theme V2

## 🤖 Claude Handover Section

**Für nächsten Claude:**

**Aktueller Stand:**
Technical Concept completed basierend auf Hybrid-Synthese zwischen Claude's Foundation Standards Excellence und externer KI's pragmatischem MVP-Approach. Strategic Diskussion mit kritischer Würdigung (8.5/10 für externe KI) zeigt optimalen Weg: Domain-Modell-Tiefe + Production-Concerns + Foundation Standards Compliance.

**Nächster konkreter Schritt:**
Artefakte von externer KI anfordern für production-ready Implementation. Externe KI hat sich bereit erklärt zu liefern: OpenAPI 3.1, SQL-Projektionen, Outbox/Bounce-Worker-Skeleton (Quarkus), React-Thread-UI auf Theme V2 - copy-paste-ready.

**Wichtige Entscheidungen getroffen:**
- **Email-Architektur:** Option C - Shared Communication Core (beide KIs einig)
- **Scope:** Email + Phone + Meeting-Notes MVP (Externe KI's Pragmatismus)
- **Timeline:** 10-12 Wochen (Hybrid aus 8-10 und 16 Wochen)
- **Quality:** 95% Foundation Standards (Claude's Excellence bleibt)
- **Domain-Modell:** MailAccount, Thread, Message, SLA-Engine (Externe KI's Design)

**Artefakte-Anfrage vorbereitet:**
Copy-paste-Text für externe KI ready um konkrete Implementation-Artefakte zu erhalten: OpenAPI-Specs, SQL-Schemas, Java-Services, React-Components basierend auf Hybrid-Synthese.

> **🚀 MIGRATION HINWEIS für Production:**
> Bei Production-Start müssen alle Tests in die neue Enterprise Test-Struktur migriert werden:
> - Unit Tests → `/backend/src/test/java/unit/communication/` bzw. `/frontend/src/tests/unit/communication/`
> - Integration Tests → `/backend/src/test/java/integration/communication/`
> - Performance Tests → `/backend/src/test/java/performance/communication/`
> Siehe [TEST_STRUCTURE_PROPOSAL.md](../../features/TEST_STRUCTURE_PROPOSAL.md) für Details.

**Offene Punkte für Artefakte:**
- Package-Struktur: de.freshplan.communication.* (Claude's Standard)
- ABAC-Security-Implementation: Territory-RLS + JWT-Claims
- Foundation Standards Integration: 95% Compliance-Matrix
- Cross-Module-Events: Integration mit Customer/Audit/Opportunity-Events

**Kontext-Links:**
- **Business-Context:** → [CRM AI Context Schnell](../../CRM_AI_CONTEXT_SCHNELL.md)
- **Master Plan Integration:** → [CRM Master Plan V5](../../CRM_COMPLETE_MASTER_PLAN_V5.md)
- **Hybrid-Synthese Basis:** → [Kritische Würdigung](./diskussionen/2025-09-19_KRITISCHE_WUERDIGUNG_EXTERNE_KI_ANTWORT.md)

---

**📋 Qualitäts-Checkliste erfüllt:**
- ✅ **Executive Summary:** Hybrid-Synthese What/Why/When/How
- ✅ **Timeline:** Realistische 10-12 Wochen (Kompromiss)
- ✅ **Foundation Standards:** 95% Compliance integriert
- ✅ **B2B-Food-Features:** Sample-Follow-ups, Multi-Kontakt, Seasonal
- ✅ **Domain-Modell:** Externe KI's production-ready Design
- ✅ **Cross-Module-Integration:** Event-Bus + Customer/Audit-System
- ✅ **Success Metrics:** Technical + Business + Production KPIs
- ✅ **Artefakte-Ready:** Externe KI Deliverables spezifiziert