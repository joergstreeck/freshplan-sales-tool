# 🚀 CRM Master Plan V5 - Sales Command Center (Kompakt)

**📊 Plan Status:** 🔄 In Progress - Foundation Standards Update Required
**🎯 Owner:** Development Team + Product Team
**⏱️ Timeline:** Q4 2025 → Q2 2026
**🔧 Effort:** L (Large - Multi-Sprint Project)
**📋 Kontext:** → [CRM System Context](./CRM_SYSTEM_CONTEXT.md) (für Feature-Diskussionen)
**🚨 URGENT:** Foundation Standards Compliance für Module 02 & 03 (KI-Anfragen bereit)

## 🎯 Executive Summary (für Claude)

**Mission:** Entwicklung eines intelligenten Sales Command Centers für B2B-Convenience-Food-Vertrieb an Gastronomiebetriebe
**Problem:** Fragmentierte Vertriebsprozesse, manuelle Workflows, fehlende Insights für komplexe B2B-Beratungsverkäufe von Convenience-Food-Produkten
**Solution:** Integrierte CRM-Plattform mit Field-Based Architecture und Event-Driven Communication speziell für Cook&Fresh® B2B-Food-Vertrieb
**Impact:** 3x schnellere Lead-Qualifizierung, 2x höhere Conversion durch ROI-basierte Beratung, vollständige Sales-Process-Automation

## 🍽️ FreshFoodz Business-Kontext (B2B-Convenience-Food-Hersteller)

**Unser Geschäftsmodell:**
- **Produkt:** Cook&Fresh® Convenience-Food mit patentierter Konservierungstechnologie
- **Haltbarkeit:** Bis 40 Tage ohne künstliche Konservierungsstoffe
- **Zielgruppen:** Multi-Channel B2B-Vertrieb
  - **Direktkunden:** Restaurants, Hotels, Betriebsgastronomie, Vending-Betreiber
  - **Partner-Channel:** Lieferanten, Händler, Wiederverkäufer (B2B2B)
- **Verkaufsansatz:** "Genussberater" - kanal-spezifische ROI-basierte Beratung

**Sales-Prozess-Besonderheiten (Multi-Channel):**
```yaml
DIREKTKUNDEN (Restaurants, Hotels, Betriebsgastronomie, Vending):
1. Lead-Qualifizierung → Betriebstyp, Größe, Konzept, Entscheidungsstruktur
2. Bedarf-Analyse → Setup, Personal, aktuelle Herausforderungen, Volumen
3. ROI-Demonstration → Kosteneinsparung vs. Investition (kanal-spezifisch)
4. Produkt-Sampling → Gratis Produktkatalog + individualisierte Sample-Boxes
5. Test-Phase → Kunde testet im echten Betrieb (2-4 Wochen)
6. Individuelles Angebot → Basierend auf Produktmix und Volumen
7. Verhandlung → Mengenrabatte, Lieferkonditionen, Service-Level
8. Abschluss → Langfristige Lieferverträge + Account-Management

PARTNER-CHANNEL (Lieferanten, Händler, Wiederverkäufer):
1. Partner-Qualifizierung → Kundenbasis, Vertriebskapazität, Markt-Coverage
2. Portfolio-Analyse → Wie passt Cook&Fresh® in deren Sortiment?
3. Margin-Struktur → Partnerkonditionen, Support-Level, Incentives
4. Pilot-Programm → Test mit ausgewählten End-Kunden
5. Rollout-Planung → Schrittweise Expansion, Marketing-Support
6. Partner-Enablement → Training, Sales-Tools, Produkt-Schulungen
7. Performance-Tracking → Umsatz-Ziele, Market-Penetration
8. Strategic Partnership → Langfristige Kooperation, exklusive Gebiete
```

**CRM-Anforderungen für Multi-Channel B2B-Food-Vertrieb:**
- **Channel-Management:** Direktkunden vs. Partner-Channel mit verschiedenen Prozessen
- **ROI-Kalkulation:** Kanal-spezifische Berechnungen (Restaurant vs. Hotel vs. Partner-Margin)
- **Produkt-Matching:** 200+ Cook&Fresh® Produkte für verschiedene Betriebstypen
- **Sample-Management:** Tracking für End-Kunden UND Partner-Demos
- **Partner-Enablement:** Tools und Materialien für Wiederverkäufer
- **Territory-Management:** Gebietsschutz und Konflikt-Vermeidung zwischen Kanälen

## 🗺️ Sidebar-Navigation & Feature-Struktur

**Navigation-Architektur:**
```
├── 🏠 Mein Cockpit                # Dashboard & Insights
├── 👤 Neukundengewinnung          # Lead Generation & Campaigns
├── 👥 Kundenmanagement            # CRM Core (M4 Pipeline)
├── 📊 Auswertungen               # Analytics & Reports
├── 💬 Kommunikation              # Team Communication
├── ⚙️ Einstellungen              # User Configuration
├── 🆘 Hilfe & Support            # Help System
└── 🔐 Administration             # Admin Functions
```

**Feature-Module-Mapping:** [Sidebar-basierte Module](./features-neu/)

## 🔗 Infrastruktur-Koordination

**Kritische Infrastructure Dependencies:**
| Infrastructure Plan | Status | Impact auf Features |
|---------------------|--------|-------------------|
| [Test Debt Recovery](./infrastruktur/TEST_DEBT_RECOVERY_PLAN.md) | 🔴 Critical | Blockiert Feature-Velocity |
| [SmartLayout Migration](./infrastruktur/SMARTLAYOUT_MIGRATION_PLAN.md) | 🔄 In Progress | UI Performance +50% |
| [CQRS Migration](./infrastruktur/CQRS_MIGRATION_PLAN.md) | 🟡 Review | Read-Performance +200% |

**Infrastructure-Koordination:** [Infrastructure Master Index](./infrastruktur/00_MASTER_INDEX.md)

## 🗺️ Feature Implementation Roadmap

### **Q4 2025: Foundation & Core Features**

#### **01_mein-cockpit** [Technical Concept](./features-neu/01_mein-cockpit/technical-concept.md)
- **Status:** ✅ Technical Concept abgeschlossen, bereit für Implementation Phase 1
- **Timeline:** 6-8 Wochen (Phase 1: 2-3 Wochen) | **Dependencies:** ChannelType Entity, ROI-Calculator
- **Code-Basis:** SalesCockpitV2.tsx Production-Ready, Backend CQRS optimiert (19/19 Tests)

#### **02_neukundengewinnung** [Complete Module Planning](./features-neu/02_neukundengewinnung/)
- **Status:** 🚨 **Foundation Standards Update Required** (aktuell ~55% → Ziel 92%)
- **Timeline:** 20-24 Wochen Complete Module Development (Phase 1: 12w, Phase 2: 8w, Phase 3: 4w)
- **Dependencies:** all.inkl Mail-Provider, UserLeadSettings Entity
- **🎯 URGENT:** [Foundation Standards Compliance Request](./features-neu/02_neukundengewinnung/diskussionen/2025-09-19_FOUNDATION_STANDARDS_COMPLIANCE_REQUEST.md) | [Reference Bundle](./features-neu/02_neukundengewinnung/diskussionen/2025-09-19_FOUNDATION_STANDARDS_REFERENCE_BUNDLE.md)
- **Planning:** [Finale Roadmap](./features-neu/02_neukundengewinnung/diskussionen/2025-09-18_finale-entwicklungsroadmap.md) | [KI Production-Specs](./features-neu/02_neukundengewinnung/diskussionen/2025-09-18_finale-ki-specs-bewertung.md)

**email-posteingang/** [Technical Concept](./features-neu/02_neukundengewinnung/email-posteingang/technical-concept.md)
- **Status:** ✅ Technical Concept abgeschlossen, KI-Production-Specs integriert | **Timeline:** Phase 1 (Woche 1-12) | **Dependencies:** all.inkl Integration

**lead-erfassung/** [Technical Concept](./features-neu/02_neukundengewinnung/lead-erfassung/technical-concept.md)
- **Status:** ✅ Technical Concept abgeschlossen, Handelsvertretervertrag-konform | **Timeline:** Phase 1 (Woche 1-12) | **Dependencies:** UserLeadSettings Entity

**kampagnen/** [Technical Concept](./features-neu/02_neukundengewinnung/kampagnen/technical-concept.md)
- **Status:** ✅ Technical Concept abgeschlossen, Multi-Touch-Attribution | **Timeline:** Phase 2 (Woche 13-20) | **Dependencies:** Email+Lead Foundation

#### **03_kundenmanagement** [Technical Concept](./features-neu/03_kundenmanagement/technical-concept.md) | [KI-Diskussion](./features-neu/03_kundenmanagement/diskussionen/)
**🏛️ Enterprise CRM-Platform Status:** ✅ Production-Ready + 🚨 **Foundation Standards Update Required**

**Platform-Optimierung** [Artefakte](./features-neu/03_kundenmanagement/artefakte/)
- **Status:** ✅ **Foundation Standards Update ABGESCHLOSSEN** (100% Compliance erreicht!)
- **Timeline:** Ready for Implementation - Alle Foundation Standards erfüllt
- **🎯 COMPLETED:** Foundation Standards Compliance durch systematische Artefakte-Harmonisierung
- **Achievement:** Von ~50% auf 100% Foundation Standards Compliance (Design System V2, ABAC Security, API Standards, Testing 80%+)
- **Dependencies:** ✅ Alle Artefakte harmonisiert und production-ready

**customer-management/** Dashboard-Hub (Route: `/customer-management`)
- **Status:** ✅ Production-Ready (389 LOC) + "Neuer Kunde" Button | **Timeline:** Wartung | **Issues:** 🔴 Dashboard-Bug (falsche Route-Pfade)

**customers/** Enterprise Customer-Liste (Route: `/customers` → `/customer-management/customers`)
- **Status:** ✅ Production-Ready (400+276 LOC) + "Neuer Kunde" Button | **Timeline:** Route-Migration | **Dependencies:** Routen-Konsolidierung

**opportunities/** Kanban-Pipeline (Route: `/customer-management/opportunities`)
- **Status:** ✅ Production-Ready (799 LOC Drag&Drop) | **Timeline:** Integration-Tests | **Dependencies:** Dashboard-Bug-Fix

**activities/** Activity-Timeline (Route: `/customer-management/activities`)
- **Status:** 🔴 Navigation vorhanden, kein Code | **Timeline:** Woche 6-8 | **Dependencies:** Activity-Implementation

**🚨 Kritische Gaps:** Field-Backend-Mismatch (Frontend field-ready, Backend entity-based)

#### **08_administration**
**audit-dashboard/** [Technical Concept](./features-neu/08_administration/audit-dashboard/technical-concept.md)
- **Status:** ✅ FC-012 funktional | **Timeline:** Woche 1 (Integration-Check) | **Dependencies:** Admin-Role-Check

**benutzerverwaltung/** [Technical Concept](./features-neu/08_administration/benutzerverwaltung/technical-concept.md)
- **Status:** ✅ Keycloak-Integration funktional | **Timeline:** Woche 1 (UI-Polish) | **Dependencies:** Role-Management

**system/api-status/** [Technical Concept](./features-neu/08_administration/system/api-status/technical-concept.md)
- **Status:** ✅ Funktional | **Timeline:** Woche 1 (Monitoring-Enhancement) | **Dependencies:** Health-Check-APIs

**system/system-logs/** [Technical Concept](./features-neu/08_administration/system/system-logs/technical-concept.md)
- **Status:** 📋 Geplant | **Timeline:** Woche 4-5 | **Dependencies:** Log-Aggregation Service

**system/performance/** [Technical Concept](./features-neu/08_administration/system/performance/technical-concept.md)
- **Status:** 📋 Geplant | **Timeline:** Woche 6-7 | **Dependencies:** Metrics Collection

**system/backup-recovery/** [Technical Concept](./features-neu/08_administration/system/backup-recovery/technical-concept.md)
- **Status:** 📋 Geplant | **Timeline:** Q1 2026 | **Dependencies:** Backup-Strategy Definition

**hilfe-konfiguration/hilfe-system-demo/** [Technical Concept](./features-neu/08_administration/hilfe-konfiguration/hilfe-system-demo/technical-concept.md)
- **Status:** ✅ Funktional | **Timeline:** Woche 1 (Integration-Test) | **Dependencies:** Help-System Framework

### **Q1 2026: Communication & Settings**

#### **05_kommunikation**
**team-chat/** [Technical Concept](./features-neu/05_kommunikation/team-chat/technical-concept.md)
- **Status:** 📋 Geplant | **Timeline:** Woche 1-2 | **Dependencies:** WebSocket Infrastructure

**ankuendigungen/** [Technical Concept](./features-neu/05_kommunikation/ankuendigungen/technical-concept.md)
- **Status:** 📋 Geplant | **Timeline:** Woche 3-4 | **Dependencies:** Notification System

**notizen/** [Technical Concept](./features-neu/05_kommunikation/notizen/technical-concept.md)
- **Status:** 📋 Geplant | **Timeline:** Woche 5-6 | **Dependencies:** Rich Text Editor

**interne-nachrichten/** [Technical Concept](./features-neu/05_kommunikation/interne-nachrichten/technical-concept.md)
- **Status:** 📋 Geplant | **Timeline:** Woche 7-8 | **Dependencies:** Message Threading

#### **06_einstellungen**
**mein-profil/** [Technical Concept](./features-neu/06_einstellungen/mein-profil/technical-concept.md)
- **Status:** 📋 Geplant | **Timeline:** Woche 1-2 | **Dependencies:** User Profile API

**benachrichtigungen/** [Technical Concept](./features-neu/06_einstellungen/benachrichtigungen/technical-concept.md)
- **Status:** 📋 Geplant | **Timeline:** Woche 3-4 | **Dependencies:** Notification Preferences

**darstellung/** [Technical Concept](./features-neu/06_einstellungen/darstellung/technical-concept.md)
- **Status:** 📋 Geplant | **Timeline:** Woche 5-6 | **Dependencies:** Theme System

**sicherheit/** [Technical Concept](./features-neu/06_einstellungen/sicherheit/technical-concept.md)
- **Status:** 📋 Geplant (FC-015 Migration) | **Timeline:** Woche 7-8 | **Dependencies:** Rights & Roles System

### **Q2 2026: Analytics & Help System**

#### **04_auswertungen**
**umsatzuebersicht/** [Technical Concept](./features-neu/04_auswertungen/umsatzuebersicht/technical-concept.md)
- **Status:** 📋 Geplant | **Timeline:** Woche 1-2 | **Dependencies:** Sales Data Aggregation

**kundenanalyse/** [Technical Concept](./features-neu/04_auswertungen/kundenanalyse/technical-concept.md)
- **Status:** 📋 Geplant | **Timeline:** Woche 3-4 | **Dependencies:** Customer Analytics Service

**aktivitaetsbericht/** [Technical Concept](./features-neu/04_auswertungen/aktivitaetsbericht/technical-concept.md)
- **Status:** 📋 Geplant (FC-016 Migration) | **Timeline:** Woche 5-6 | **Dependencies:** KPI Tracking System

#### **07_hilfe-support**
**erste-schritte/** [Technical Concept](./features-neu/07_hilfe-support/erste-schritte/technical-concept.md)
- **Status:** 📋 Geplant | **Timeline:** Woche 1-2 | **Dependencies:** Onboarding Framework

**handbuecher/** [Technical Concept](./features-neu/07_hilfe-support/handbuecher/technical-concept.md)
- **Status:** 📋 Geplant | **Timeline:** Woche 3-4 | **Dependencies:** Documentation System

**video-tutorials/** [Technical Concept](./features-neu/07_hilfe-support/video-tutorials/technical-concept.md)
- **Status:** 📋 Geplant | **Timeline:** Woche 5-6 | **Dependencies:** Video Streaming

**haeufige-fragen/** [Technical Concept](./features-neu/07_hilfe-support/haeufige-fragen/technical-concept.md)
- **Status:** 📋 Geplant | **Timeline:** Woche 7-8 | **Dependencies:** FAQ System

**support-kontaktieren/** [Technical Concept](./features-neu/07_hilfe-support/support-kontaktieren/technical-concept.md)
- **Status:** 📋 Geplant | **Timeline:** Woche 9-10 | **Dependencies:** Ticketing System

## 🎯 Aktuelle Sprint-Woche: Q4 2025, Woche 1

### 🔥 Nächste 3 konkrete Aktionen:
1. **01_mein-cockpit:** ✅ Technical Concept abgeschlossen → Phase 1 Implementation starten
2. **Trigger-Texte V3.0:** ✅ **ABGESCHLOSSEN** - Vollständig implementiert
3. **Feature-Diskussion:** Mit anderen KIs über CRM_SYSTEM_CONTEXT.md möglich

### ⚠️ Aktuelle Blocker:
- **Dokumentations-Strategie:** ✅ **GELÖST** - Duale Strategie implementiert
- **Compact-Problem:** ✅ **UMGANGEN** - Robust Handover System etabliert
- **M8 Calculator** Integration fehlt für ActionCenter (bleibt)

## 🎯 Critical Success Metrics

### Q4 2025 Targets:
- **Cockpit + Kundenmanagement + Administration:** 100% functional
- **Page Load:** <200ms P95 (via SmartLayout + CQRS)
- **Test Coverage:** 80%+ (via Test Debt Recovery)

### Business Impact Ziele:
- **Lead-Processing:** 3x schneller durch Automation
- **Conversion Rate:** 2x höher durch Guided Workflows
- **Sales Cycle:** -30% durch proaktive Workflows

## 🤖 Claude Handover Section

**Aktueller Master-Plan-Stand:**
CRM Master Plan V5 kompakt refactoriert nach PLANUNGSMETHODIK. Infrastructure-Koordination über Master Index etabliert, Feature-Development mit klarer Q4 2025 → Q2 2026 Timeline. Sidebar-basierte Feature-Struktur implementiert.

**Nächste strategische Aktionen:**
1. 01_mein-cockpit: ✅ Technical Concept abgeschlossen → ChannelType Entity erweitern für Phase 1
2. Test Debt Recovery starten (kritische Infrastruktur-Blockade)
3. FC-005 Customer Management: Field-Based Architecture finalisieren

**Kritische Koordinations-Punkte:**
- Infrastructure und Feature-Development parallel ausführen
- Test Debt Recovery blockiert Feature-Velocity → höchste Priorität
- SmartLayout Performance-Gains unterstützen CRM User-Experience
- Alle Technical Concepts nur Claude-optimiert verlinken (keine Diskussionen/Human-Guides)

**Master-Plan-Integration:**
Einziger strategischer Master Plan. Alle Infrastructure-Pläne über Master Index koordiniert. Feature-Development folgt Sidebar-Navigation mit Technical Concepts als einzige Detail-Referenz.

## 📚 Foundation Knowledge (für Claude)

### 🎯 **Core Standards - IMMER ZUERST LESEN:**
- **[Design System](./grundlagen/DESIGN_SYSTEM.md)** - FreshFoodz CI (#94C456, #004F7B, Antonio Bold, Material-UI v5+)
- **[API Standards](./grundlagen/API_STANDARDS.md)** - OpenAPI 3.1, RBAC-Patterns, Error-Handling
- **[Coding Standards](./grundlagen/CODING_STANDARDS.md)** - TypeScript import type, PascalCase, 80-100 chars
- **[Security Guidelines](./grundlagen/SECURITY_GUIDELINES.md)** - ABAC, Territory-Scoping, Audit-Trail
- **[Performance Standards](./grundlagen/PERFORMANCE_STANDARDS.md)** - P95 <200ms, Bundle <500KB, Coverage >90%
- **[Testing Guide](./grundlagen/TESTING_GUIDE.md)** - Given-When-Then, 80% Coverage, Integration-Tests

### 🛠️ **Development & Quality:**
- **[Component Library](./grundlagen/COMPONENT_LIBRARY.md)** - Reusable UI-Components
- **[Development Workflow](./grundlagen/DEVELOPMENT_WORKFLOW.md)** - Git-Flow, PR-Process, CI/CD
- **[Code Review Standard](./grundlagen/CODE_REVIEW_STANDARD.md)** - Qualitätssicherung
- **[Database Migration Guide](./grundlagen/DATABASE_MIGRATION_GUIDE.md)** - Migration-Regeln & Registry
- **[Business Logic Standards](./grundlagen/BUSINESS_LOGIC_STANDARDS.md)** - Domain-Logic-Patterns

### 🚑 **Debug & Troubleshooting:**
- **[Debug Cookbook](./grundlagen/DEBUG_COOKBOOK.md)** - Symptom-basierte Problemlösung
- **[TypeScript Guide](./grundlagen/TYPESCRIPT_IMPORT_TYPE_GUIDE.md)** - Import Type Probleme
- **[CI Debugging Lessons](./grundlagen/CI_DEBUGGING_LESSONS_LEARNED.md)** - Systematische Debug-Methodik
- **[Keycloak Setup](./grundlagen/KEYCLOAK_SETUP.md)** - Authentication Setup

### 🔄 Workflow-Dokumente:
- **[CI Debugging Strategy](./workflows/CI_DEBUGGING_STRATEGY.md)** - CI-Methodik
- **[ESLint Cleanup](./workflows/ESLINT_CLEANUP_SAFE_APPROACH.md)** - Maintenance

### ⚡ Quick-Troubleshooting:
- **Frontend Issues:** White Screen, Failed to fetch → Debug Cookbook
- **Backend Issues:** 401 Unauthorized, No Test Data → Debug Cookbook
- **CI Issues:** HTTP 500, Race Conditions → CI Debugging Lessons
- **TypeScript:** Import Errors → TypeScript Guide

---
**📋 Dokument-Zweck:** Kompakte Planungsübersicht für Claude
**🔗 Für Feature-Diskussionen:** → [CRM System Context](./CRM_SYSTEM_CONTEXT.md)
**🔄 Letzte Aktualisierung:** 2025-09-18 - Debug-Guides konsolidiert