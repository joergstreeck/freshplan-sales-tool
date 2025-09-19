# Kommunikation - Technical Concept (Hybrid-Synthese)

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

## 🛠️ Implementation Phases (Hybrid-Approach)

### **Phase 1: Shared Communication Core Foundation (Wochen 1-4)**

**Goals:**
- Shared Communication Core mit Domain-Modell (Externe KI) + Foundation Standards (Claude)
- Email vollwertig mit Outbox, Bounce-Handling, Threading
- 95% Foundation Standards Compliance von Anfang an

**Key Deliverables:**
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
- **Business-Context:** → [CRM System Context](../../CRM_SYSTEM_CONTEXT.md)
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