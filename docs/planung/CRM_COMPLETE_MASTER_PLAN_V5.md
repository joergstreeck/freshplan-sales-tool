# 🚀 CRM Master Plan V5 - Sales Command Center (Kompakt)

**📊 Plan Status:** 🔄 In Progress
**🎯 Owner:** Development Team + Product Team
**⏱️ Timeline:** Q4 2025 → Q2 2026
**🔧 Effort:** L (Large - Multi-Sprint Project)
**📋 Kontext:** → [CRM System Context](./CRM_SYSTEM_CONTEXT.md) (für Feature-Diskussionen)

## 🎯 Executive Summary (für Claude)

**Mission:** Entwicklung eines intelligenten Sales Command Centers für B2B-Gastronomy-Vertrieb mit Convenience-Food-Produkten
**Problem:** Fragmentierte Vertriebsprozesse, manuelle Workflows, fehlende Insights für komplexe B2B-Beratungsverkäufe
**Solution:** Integrierte CRM-Plattform mit Field-Based Architecture und Event-Driven Communication speziell für FreshFoodz Gastronomy-Sales
**Impact:** 3x schnellere Lead-Qualifizierung, 2x höhere Conversion durch ROI-basierte Beratung, vollständige Sales-Process-Automation

## 🍽️ FreshFoodz Business-Kontext (B2B-Gastronomy)

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

**CRM-Anforderungen für Multi-Channel-Sales:**
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

#### **02_neukundengewinnung**
**email-posteingang/** [Technical Concept](./features-neu/02_neukundengewinnung/email-posteingang/technical-concept.md)
- **Status:** 📋 Geplant (FC-003 Migration) | **Timeline:** Woche 3-4 | **Dependencies:** E-Mail Service Integration

**lead-erfassung/** [Technical Concept](./features-neu/02_neukundengewinnung/lead-erfassung/technical-concept.md)
- **Status:** 📋 Geplant | **Timeline:** Woche 5-6 | **Dependencies:** Customer Entity Foundation

**kampagnen/** [Technical Concept](./features-neu/02_neukundengewinnung/kampagnen/technical-concept.md)
- **Status:** 📋 Geplant | **Timeline:** Q1 2026 | **Dependencies:** E-Mail Integration

#### **03_kundenmanagement**
**alle-kunden/** [Technical Concept](./features-neu/03_kundenmanagement/alle-kunden/technical-concept.md)
- **Status:** 🔄 FC-005 Customer Management in Migration | **Timeline:** Woche 1-3 | **Dependencies:** Field-Based Architecture

**neuer-kunde/** [Technical Concept](./features-neu/03_kundenmanagement/neuer-kunde/technical-concept.md)
- **Status:** ✅ Grundfunktion vorhanden | **Timeline:** Woche 4-5 (Enhancement) | **Dependencies:** Customer Form Validation

**verkaufschancen/** [Technical Concept](./features-neu/03_kundenmanagement/verkaufschancen/technical-concept.md)
- **Status:** ✅ M4 Pipeline funktional | **Timeline:** Woche 2-3 (Cockpit-Integration) | **Dependencies:** Cockpit Spalte 2 Integration

**aktivitaeten/** [Technical Concept](./features-neu/03_kundenmanagement/aktivitaeten/technical-concept.md)
- **Status:** 📋 Geplant (FC-013 Migration) | **Timeline:** Woche 6-8 | **Dependencies:** Activity Notes System

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

## 🚑 Debug & Troubleshooting (für Claude)

### 📋 Foundation-Dokumente:
- **[Debug Cookbook](./grundlagen/DEBUG_COOKBOOK.md)** - Symptom-basierte Problemlösung
- **[TypeScript Guide](./grundlagen/TYPESCRIPT_IMPORT_TYPE_GUIDE.md)** - Import Type Probleme
- **[CI Debugging Lessons](./grundlagen/CI_DEBUGGING_LESSONS_LEARNED.md)** - Systematische Debug-Methodik
- **[Code Review Standard](./grundlagen/CODE_REVIEW_STANDARD.md)** - Qualitätssicherung
- **[Coding Standards](./grundlagen/CODING_STANDARDS.md)** - Komplette Code-Rules
- **[Database Migration Guide](./grundlagen/DATABASE_MIGRATION_GUIDE.md)** - Migration-Regeln & Registry
- **[Testing Guide](./grundlagen/TESTING_GUIDE.md)** - Vollständige Test-Strategie (E2E + Playwright)
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