# 🚀 Finale Entwicklungsroadmap - Neukundengewinnung Complete Modules

**📊 Plan Status:** ✅ Approved (nach KI-Consultation + Jörgs Inputs)
**🎯 Owner:** Development Team
**⏱️ Timeline:** Q4 2025 → Q1 2026
**🔧 Effort:** L (20-24 Wochen, 3 Module komplett)

## 🎯 Executive Summary (für Claude)

**Mission:** Complete Module Development für alle 3 Neukundengewinnung-Module statt künstlicher V1/V2/V3-Phasen.
**Problem:** KI empfahl MVP-Ansatz, aber Tool ist noch nicht live - Complete Development ist effizienter.
**Solution:** E-Mail-Posteingang + Lead-Erfassung + Kampagnen jeweils vollständig mit allen Advanced Features.
**Timeline:** 20-24 Wochen für 3 komplett ausgebaute Module.
**Impact:** Produktionsreifes Neukundengewinnung-System ohne technische Schulden.

**Quick Context:** Handelsvertretervertrag-Requirements integriert, benutzerspezifische Provisionen/Lead-Zeiträume, all.inkl als Mail-Provider spezifiziert, TestDataBuilder-Integration für Mock-Replacement.

## 📋 Context & Dependencies

### Current State:
- ✅ Handelsvertretervertrag-Requirements analysiert (6-Monats-Schutz, 60-Tage-Aktivitätscheck)
- ✅ KI Complete-Module-Development Empfehlungen erhalten und gewürdigt
- ✅ all.inkl Mail-Provider spezifiziert (IMAP/SMTP + KAS API)
- ✅ Benutzerspezifische Settings-Requirements definiert
- ✅ TestDataBuilder-System verfügbar für Mock-Replacement

### Target State:
- ✅ Email-Posteingang: Thread-Erkennung + Intelligence + all.inkl-Integration
- ✅ Lead-Erfassung: State-Machine + Vertrags-Compliance + User-Settings
- ✅ Kampagnen: A/B-Testing + Attribution + Automation komplett
- ✅ Cockpit-Integration: Smart Updates + KPIs + Actions
- ✅ Mock-free Development Environment

### Dependencies:
→ [Mock Replacement Strategy](../../infrastruktur/MOCK_REPLACEMENT_STRATEGY_PLAN.md) - TestDataBuilder parallel
→ [all.inkl Integration Spec](./2025-09-18_all-inkl-integration-spezifikation.md) - Mail-Provider Details
→ [Handelsvertretervertrag Requirements](./2025-09-18_handelsvertretervertrag-lead-requirements.md) - Business-Logic

## 🛠️ Implementation Phases

### Phase 1: E-Mail + Lead Parallel (Woche 1-12)
**Goal:** Beide Core-Module gleichzeitig entwickeln (wenig gegenseitige Abhängigkeiten)

**E-Mail-Posteingang (komplett):**
- [ ] all.inkl IMAP/SMTP-Integration (Server: w1234567.kasserver.com)
- [ ] Thread-Erkennung über Message-ID/In-Reply-To/References
- [ ] Triage-Inbox mit Intelligence (regelbasiert + ML-Hook)
- [ ] Customer/Lead-Matching (Domain, Name, Signatur)
- [ ] Aktivitäts-Integration für Lead-System
- [ ] DSGVO-Compliance (7 Jahre Aufbewahrung, 2 Jahre Maskierung)

**Lead-Erfassung (komplett):**
- [ ] Lead-Entity + State-Machine (REGISTERED→ACTIVE→REMINDER_SENT→GRACE_PERIOD→EXPIRED/EXTENDED/STOP_CLOCK)
- [ ] Handelsvertretervertrag-Compliance (6-Monats-Schutz, 60-Tage-Check, 10-Tage-Nachfrist)
- [ ] UserLeadSettings-Entity für benutzerspezifische Provisionen/Zeiträume
- [ ] Stop-the-Clock-Funktion mit Reason-Tracking
- [ ] Lead→Customer-Konvertierung + Opportunity-Hook
- [ ] Provisions-Berechnung mit User-Settings

**Success Criteria:**
- all.inkl E-Mail-Empfang + Threading funktional
- Lead-State-Machine 100% vertragskonform
- Benutzerspezifische Settings funktional
- TestDataBuilder-Integration statt Mocks

**Next:** → [Phase 2](#phase-2)

### Phase 2: Integration + Kampagnen-Core (Woche 13-20)
**Goal:** E-Mail↔Lead Integration + Campaign-Engine mit allen Features

**Integration:**
- [ ] E-Mail→Lead Aktivitäts-Tracking automatisch
- [ ] Lead-Alerts via E-Mail-Events
- [ ] Customer-Matching zwischen E-Mail und Lead-System
- [ ] Cockpit Smart-Updates (WebSocket für lead.*, mention.created)

**Kampagnen (komplett):**
- [ ] Template-System + Content-Editor (Blöcke, Variablen)
- [ ] Audience-Management (statisch/dynamisch, Segmente)
- [ ] all.inkl SMTP-basiertes Sending mit Queue-System
- [ ] A/B-Testing-Framework mit Signifikanz-Check
- [ ] Multi-Touch-Attribution (Last/First/Linear/Time-Decay)
- [ ] Automation-Workflows (Send/Wait/Condition/WebHook)
- [ ] Open/Click/Reply-Tracking + Analytics

**Success Criteria:**
- E-Mail→Lead Integration seamless
- Campaign-Engine mit A/B-Testing funktional
- Multi-Touch-Attribution implementiert
- Cockpit-Integration vollständig

**Next:** → [Phase 3](#phase-3)

### Phase 3: Advanced Features + Polish (Woche 21-24)
**Goal:** AI-Features, Performance-Optimierung, Production-Readiness

**Advanced Features:**
- [ ] AI-Lead-Scoring (optional, Feature-Flag)
- [ ] E-Mail-Intelligence-Verbesserungen
- [ ] Campaign-Analytics-Dashboard
- [ ] Advanced Attribution-Reports
- [ ] Rate-Limiting + Deliverability-Optimierung

**Production-Readiness:**
- [ ] Performance-Tuning (P95 <200ms)
- [ ] Security-Hardening + Penetration-Tests
- [ ] Load-Testing (10k Threads, 100k Messages)
- [ ] Documentation + Runbooks
- [ ] Training-Material für User

**Success Criteria:**
- P95 Performance-Targets erreicht
- Security-Audit bestanden
- Load-Tests erfolgreich
- Complete User-Documentation

## ✅ Success Metrics

**Quantitative:**
- **E-Mail-Threading:** >95% korrekte Thread-Erkennung
- **Lead-Compliance:** 100% Handelsvertretervertrag-konform
- **API-Performance:** P95 <200ms für alle Endpoints
- **Campaign-Delivery:** >98% Success-Rate via all.inkl
- **Test-Coverage:** >90% für alle Module

**Qualitative:**
- Alle Module vollständig funktional ohne Mock-Dependencies
- Benutzerspezifische Provisionen/Lead-Zeiträume konfigurierbar
- E-Mail-Intelligence klassifiziert Leads automatisch
- A/B-Testing + Attribution funktional für Kampagnen
- Cockpit-Integration mit Smart-Updates aktiv

**Timeline:**
- Phase 1: Woche 1-12 (E-Mail + Lead parallel)
- Phase 2: Woche 13-20 (Integration + Kampagnen)
- Phase 3: Woche 21-24 (Advanced + Production-Ready)

## 🔗 Related Documentation

**Foundation Knowledge:**
- **all.inkl Integration:** → [all.inkl Integration Spec](./2025-09-18_all-inkl-integration-spezifikation.md)
- **Business Requirements:** → [Handelsvertretervertrag Requirements](./2025-09-18_handelsvertretervertrag-lead-requirements.md)
- **System Decisions:** → [KI Empfehlungen Würdigung](./2025-09-18_ki-complete-module-wuerdigung.md)

**Implementation Details:**
- **Code Location:** `/08_administration/integration/email-service/` für all.inkl
- **TestDataBuilder:** `/backend/src/main/java/de/freshplan/test/TestDataBuilder.java`
- **Mock-Replacement:** → [Mock Replacement Strategy](../../infrastruktur/MOCK_REPLACEMENT_STRATEGY_PLAN.md)

**Related Plans:**
- **Dependencies:** → [TestDataBuilder System](../../infrastruktur/MOCK_REPLACEMENT_STRATEGY_PLAN.md)
- **Follow-ups:** → Technical Concepts für alle 3 Module
- **Parallel:** → [Cockpit Complete Planning](../01_mein-cockpit/)

## 🤖 Claude Handover Section

**Für nächsten Claude:**

**Aktueller Stand:**
Finale Entwicklungsroadmap erstellt nach Complete-Module-Development-Prinzip. Alle KI-Empfehlungen gewürdigt, Jörgs Zusatzanforderungen (benutzerspezifische Settings + all.inkl) integriert, 20-24 Wochen Timeline für 3 komplette Module.

**Nächster konkreter Schritt:**
KI-Request für vollständige OpenAPI-Spezifikationen + SQL-Migrationsskripte erstellen und senden. Alle finalen Entscheidungen und Requirements sind dokumentiert.

**Wichtige Dateien für Context:**
- `2025-09-18_ki-complete-module-wuerdigung.md` - Bewertung der KI-Empfehlungen
- `2025-09-18_all-inkl-integration-spezifikation.md` - Mail-Provider-Details
- `2025-09-18_handelsvertretervertrag-lead-requirements.md` - Business-Requirements
- `2025-09-18_system-entscheidungen-ki-empfehlungen.md` - Finale Entscheidungen

**Offene Entscheidungen:**
- Rate-Limits für all.inkl müssen real getestet werden
- KAS API-Credentials beschaffen für Account-Management
- DSGVO Legal-Review in Woche 2-3 durchführen

**Kontext-Links:**
- **Grundlagen:** → [Planungsmethodik](../../../PLANUNGSMETHODIK.md)
- **Dependencies:** → [Mock Replacement Strategy](../../infrastruktur/MOCK_REPLACEMENT_STRATEGY_PLAN.md)
- **Business Context:** → [CRM Master Plan V5](../../../CRM_COMPLETE_MASTER_PLAN_V5.md)