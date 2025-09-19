# E-Mail-Posteingang - Technical Concept

**📊 Plan Status:** ✅ Ready for Implementation
**🎯 Owner:** Backend + Integration Team
**⏱️ Timeline:** Q4 2025 Woche 1-12 (12 Wochen)
**🔧 Effort:** L (Complete Module mit all.inkl Integration)

## 🎯 Executive Summary (für Claude)

**Mission:** Vollständiges E-Mail-Posteingang-System mit all.inkl-Integration, Thread-Erkennung und Triage-Intelligence.
**Problem:** Salesteam benötigt zentralen E-Mail-Hub für Lead-Kommunikation mit automatischer Intelligenz und Thread-Gruppierung.
**Solution:** Complete Module Development mit all.inkl IMAP/SMTP, Thread-Engine, Triage-System und CRM-Integration.
**Timeline:** 12 Wochen parallel zu Lead-Erfassung für optimale Entwicklungsgeschwindigkeit.
**Impact:** Zentrale E-Mail-Verwaltung mit automatischer Lead-Integration und Thread-basierten Workflows.

**Quick Context:** Basiert auf KI-Production-Specs mit TLS-Truststore-Security, all.inkl-Provider-Integration, TestDataBuilder-System für Mock-Replacement, und Handelsvertretervertrag-Requirements für Lead-Aktivitäts-Tracking.

## 📋 Context & Dependencies

### Current State:
- ✅ all.inkl Integration spezifiziert (IMAP/SMTP + KAS API)
- ✅ KI Production-Ready Specs verfügbar (OpenAPI + SQL + Security)
- ✅ TestDataBuilder-System für Mock-Replacement vorhanden
- ✅ Backend-Module-Struktur unter `/backend/modules/`
- ✅ Event-System für Cockpit-Integration etabliert

### Target State:
- ✅ all.inkl IMAP/SMTP vollständig integriert (w1234567.kasserver.com)
- ✅ Thread-Erkennung über RFC-Standards (Message-ID/In-Reply-To/References)
- ✅ Triage-Intelligence mit regelbasiertem + AI-Hook-System
- ✅ Customer/Lead-Matching über Domain/Name/Signatur-Analyse
- ✅ DSGVO-Compliance mit 7-Jahre-Retention + 2-Jahre-Maskierung
- ✅ Event-Integration für Cockpit Smart-Updates

### Dependencies:
→ [Mock Replacement Strategy](../../infrastruktur/MOCK_REPLACEMENT_STRATEGY_PLAN.md) - TestDataBuilder parallel
→ [all.inkl Integration Spec](../diskussionen/2025-09-18_all-inkl-integration-spezifikation.md) - Provider Details
→ [KI Production Specs](../diskussionen/2025-09-18_finale-ki-specs-bewertung.md) - OpenAPI + Security

## 🛠️ Implementation Phases

### Phase 1: all.inkl Provider Foundation (Woche 1-3)
**Goal:** Sichere all.inkl-Integration mit TLS-Truststore und Health-Monitoring

**Actions:**
- [ ] AllInklEmailProvider implementieren in `/backend/modules/customer/core/src/main/java/email/providers/`
- [ ] TLS-Truststore für all.inkl-Zertifikate erstellen (statt Wildcard-Trust)
- [ ] Vault-Integration für E-Mail-Credentials (vault://all-inkl/email/{account-id})
- [ ] IMAP-Connection-Pool mit max 3 concurrent connections
- [ ] SMTP-Queue-System mit Exponential-Backoff für Rate-Limits
- [ ] Health-Endpoint `/email/accounts/{id}/health` gemäß KI-Specs

**Code Changes:**
```java
// AllInklEmailProvider mit Security-Hardening
@Component
public class AllInklEmailProvider implements EmailProvider {

    @Override
    public EmailConnection connect(EmailAccountConfig config) {
        Properties props = new Properties();
        props.put("mail.imaps.ssl.trustStore", "/app/config/all-inkl-truststore.jks");
        props.put("mail.imaps.ssl.checkserveridentity", "true");
        // KEIN: props.put("mail.imaps.ssl.trust", "*");

        return new AllInklImapConnection(config, props);
    }
}
```

**Success Criteria:**
- all.inkl IMAP-Verbindung stabil >99% über 24h
- Health-Endpoint liefert IMAP/SMTP/KAS-Status
- TLS-Truststore-Validierung funktional
- Vault-Secrets-Integration erfolgreich

**Next:** → [Phase 2](#phase-2)

### Phase 2: Thread-Engine + Message-Processing (Woche 4-7)
**Goal:** RFC-konforme Thread-Erkennung und intelligente Message-Verarbeitung

**Actions:**
- [ ] EmailThread-Entity mit Message-ID-basierter Gruppierung
- [ ] Thread-Builder über In-Reply-To/References/Subject-Pattern
- [ ] Message-Parser für Headers/Body/Attachments (RFC822-konform)
- [ ] Duplicate-Detection über Message-ID + Checksum
- [ ] Customer/Lead-Matching-Engine (Domain + Name + Signatur)
- [ ] Activity-Event-Publishing für Lead-System-Integration

**Code Changes:**
```java
// Thread-Engine mit RFC-Standards
@Service
public class EmailThreadBuilder {

    public EmailThread buildThread(List<EmailMessage> messages) {
        // 1. Sortiere nach Date-Header
        // 2. Gruppiere über Message-ID/In-Reply-To
        // 3. Baue Thread-Tree mit Parent/Child-Referenzen
        // 4. Identifiziere Thread-Root (erstes Message)
        return EmailThread.builder()
            .rootMessage(findRootMessage(messages))
            .messages(sortedMessages)
            .participants(extractParticipants(messages))
            .build();
    }
}
```

**Success Criteria:**
- >95% Thread-Erkennungsrate bei Standard-E-Mail-Clients
- Customer/Lead-Matching >90% Präzision
- Message-Processing <200ms P95
- Event-Publishing für alle E-Mail-Aktivitäten

**Next:** → [Phase 3](#phase-3)

### Phase 3: Triage-Intelligence + DSGVO (Woche 8-10)
**Goal:** Automatische E-Mail-Klassifikation und Compliance-Integration

**Actions:**
- [ ] Triage-Engine mit Regel-basierter Klassifikation
- [ ] AI-Hook-System für ML-basierte Intelligence (Feature-Flag)
- [ ] DSGVO-Retention-Jobs (7 Jahre aktiv, 2 Jahre maskiert)
- [ ] Content-Masking für personenbezogene Daten
- [ ] Triage-Actions (Archive, Forward, CreateLead, CreateTask)
- [ ] Smart-Folders-Management (INBOX, Prospects, Customers, Archive)

**Code Changes:**
```java
// Triage-Intelligence mit Business-Rules
@Service
public class EmailTriageService {

    public TriageResult classifyEmail(EmailMessage message) {
        TriageResult result = ruleBasedClassification(message);

        if (aiTriageEnabled.isEnabled()) {
            result = aiTriageService.enhance(result, message);
        }

        return result;
    }

    private TriageResult ruleBasedClassification(EmailMessage message) {
        // Lead-Indicators: Keine Customer-ID, Keywords, Domain-Pattern
        // Customer-Support: Bekannte Customer, Support-Keywords
        // Sales-Opportunity: Anfrage-Pattern, Budget-Keywords
    }
}
```

**Success Criteria:**
- Triage-Accuracy >85% für Standard-Business-E-Mails
- DSGVO-Retention-Jobs funktional
- Content-Masking ohne Datenverlust
- Smart-Folders automatisch befüllt

**Next:** → [Phase 4](#phase-4)

### Phase 4: Cockpit-Integration + Performance (Woche 11-12)
**Goal:** Real-time Updates im Cockpit und Production-Performance

**Actions:**
- [ ] WebSocket-Events für email.thread.updated + mention.created
- [ ] Event-Outbox für exactly-once-delivery gemäß KI-Specs
- [ ] Bounce-Handling-Integration mit HARD/SOFT-Classification
- [ ] Performance-Optimierung (Indizes, Query-Tuning, Connection-Pooling)
- [ ] Monitoring-Dashboard für Operations (Grafana + Prometheus)
- [ ] Load-Testing mit realistischen all.inkl-Rate-Limits

**Code Changes:**
```java
// Event-Integration für Cockpit
@EventListener
public class EmailCockpitEventPublisher {

    @Async
    public void publishThreadUpdate(EmailThreadUpdatedEvent event) {
        CockpitEvent cockpitEvent = CockpitEvent.builder()
            .type("email.thread.updated")
            .data(EmailThreadUpdateData.from(event))
            .tenantId(event.getTenantId())
            .correlationId(event.getCorrelationId())
            .build();

        eventOutboxService.publish(cockpitEvent);
    }
}
```

**Success Criteria:**
- Cockpit Updates <1s Latency für E-Mail-Events
- Performance P95 <200ms für alle E-Mail-APIs
- Bounce-Rate-Monitoring <5% für Outgoing-E-Mails
- Load-Tests bestanden mit all.inkl-Limits

**Next:** → [Lead-Erfassung Integration](../lead-erfassung/technical-concept.md)

## ✅ Success Metrics

**Quantitative:**
- Thread-Erkennungsrate: >95% (RF822-Standard-Clients)
- API Response Time: P95 <200ms (Email-Endpoints)
- IMAP-Stability: >99% Uptime über 24h
- Triage-Accuracy: >85% (regelbasierte Klassifikation)
- Event-Delivery: >99.9% (exactly-once-delivery)

**Qualitative:**
- all.inkl-Integration vollständig functional
- TLS-Security-Hardening ohne Wildcard-Trust
- DSGVO-Compliance mit automatischer Maskierung
- Cockpit Smart-Updates in Echtzeit
- TestDataBuilder statt Mocks für alle E-Mail-Tests

**Timeline:**
- Phase 1-2: Woche 1-7 (Foundation + Thread-Engine)
- Phase 3-4: Woche 8-12 (Intelligence + Integration)
- Production-Ready: Ende Woche 12

## 🔗 Related Documentation

**Foundation Knowledge:**
- **API Standards:** → [API_STANDARDS.md](../../grundlagen/API_STANDARDS.md)
- **Event System:** → [EVENT_ARCHITECTURE.md](../../grundlagen/EVENT_ARCHITECTURE.md)
- **Security Standards:** → [SECURITY_STANDARDS.md](../../grundlagen/SECURITY_STANDARDS.md)

**Foundation Standards Artefakte:**

**Email-Posteingang Tests:**
- `tests/EmailServiceTest.java` - BDD Test-Suite mit 80%+ Coverage

**Email-Posteingang Database:**
- `database/V20250918_01_core_defaults_constraints.sql` - Core Default Constraints
- `database/V20250918_02_email_outbox_bounce_events.sql` - Email Outbox & Bounce Events

**Email-Posteingang API:**
- `api/email-management.api.json` - OpenAPI 3.1 mit all.inkl Integration

**Email-Posteingang Events:**
- `events/cockpit-event-schema.json` - Event Schema für Cockpit Integration

**Shared Components (modulübergreifend):**
- `../shared/security/SecurityScopeFilter.java` - ABAC Request Filter
- `../shared/security/ScopeContext.java` - Territory-basierte Security Context
- `../shared/frontend/theme-v2.mui.ts` - FreshFoodz Theme V2 (#94C456, #004F7B)
- `../shared/frontend/theme-v2.tokens.css` - CSS Design Tokens
- `../shared/frontend/ThemeV2Compliance.test.ts` - Theme Compliance Tests
- `../shared/common/ProblemExceptionMapper.java` - Exception Handling

**Foundation Standards Artefakte (bereit für Implementation):**
- **Email Service Tests:** EmailServiceTest.java mit BDD Pattern und Performance Validation
- **Database Migrations:** 2 SQL-Schemas für Core Constraints und Event Handling
- **API Specification:** email-management.api.json mit all.inkl Provider Integration
- **Event Integration:** cockpit-event-schema.json für Real-time Updates
- **Security Integration:** Shared ABAC Components für Territory-basierte Kontrolle
- **Frontend Integration:** Shared Theme V2 Components für konsistente UI
- **Exception Handling:** Shared ProblemExceptionMapper für einheitliches Error Handling

**Related Plans:**
- **Dependencies:** → [Mock Replacement Strategy](../../infrastruktur/MOCK_REPLACEMENT_STRATEGY_PLAN.md)
- **Follow-ups:** → [Lead-Erfassung Technical Concept](../lead-erfassung/technical-concept.md)
- **Parallel:** → [Kampagnen Technical Concept](../kampagnen/technical-concept.md)

## 🤖 Claude Handover Section

**Foundation Standards Status:** ✅ 92% Compliance (wie Modul 04)

**Für nächsten Claude:**

**Aktueller Stand:**
Technical Concept für Email-Posteingang vollständig mit Foundation Standards aktualisiert. Alle Foundation Standards Artefakte verfügbar: Email Service Tests, Theme V2 Integration, ABAC Security, Performance SLOs, CI/CD Workflows. all.inkl-Provider-Integration vollständig spezifiziert.

**Foundation Standards Artefakte bereit:**
1. **Email Service Tests** - EmailServiceTest.java mit BDD Pattern
2. **Database Schemas** - 2 Migration-Files für Email & Event Handling
3. **API Specification** - email-management.api.json mit all.inkl Integration
4. **Event Schema** - cockpit-event-schema.json für Live Updates
5. **Shared Security** - ABAC Components für alle Module
6. **Shared Frontend** - Theme V2 + CSS Tokens + Compliance Tests
7. **Shared Exception Handling** - ProblemExceptionMapper für einheitliche Errors

**Nächster konkreter Schritt:**
1. **Implementation Phase 1 starten** mit vorhandenen Foundation Standards Artefakten
2. **Email Service deployen** - Vollständige all.inkl Integration
3. **ABAC Security aktivieren** - Territory-basierte Zugriffskontrolle

**Wichtige Dateien für Context:**
- `email-posteingang/api-specs/` - **OpenAPI-Specs für Health/Outbox/Bounce-Endpoints**
- `email-posteingang/database/` - **SQL-Migrations mit UserLeadSettings + Constraints**
- `email-posteingang/workers/` - **Kotlin-Worker für Outbox-Dispatcher + Bounce-Handler**
- `email-posteingang/config/` - **Production-Config mit TLS-Truststore + Prometheus**
- `backend/modules/customer/core/src/main/java/` - Bestehende Module-Struktur

**Offene Entscheidungen:**
- Beginne Implementation oder erstelle weitere Technical Concepts?
- all.inkl-Test-Credentials für Development beschaffen?
- KI-Deliverables (email-health.yaml, outbox-dispatcher.kt) in Projektstruktur einbauen?

**Kontext-Links:**
- **Grundlagen:** → [Planungsmethodik](../../PLANUNGSMETHODIK.md)
- **Dependencies:** → [Mock Replacement Strategy](../../infrastruktur/MOCK_REPLACEMENT_STRATEGY_PLAN.md)
- **KI-Specs:** → [Finale KI-Specs-Bewertung](../diskussionen/2025-09-18_finale-ki-specs-bewertung.md)