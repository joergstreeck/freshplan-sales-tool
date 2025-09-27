# 🗂️ Vollständige Planungsstruktur-Analyse

**Erstellt:** 2025-09-27
**Zweck:** Bestandsaufnahme vor Strukturoptimierung
**Scope:** Module 01-08, Infrastruktur, Sprints, ADRs

## 📊 Executive Summary

**Aktuelle Struktur:** Zentrale Sprints + Modulare Features + Cross-cutting ADRs
**Status:** Hybrid-Modell bereits teilweise vorhanden
**Problem:** Fehlende Einstiegspunkte für neue Claude-Instanzen

## 🏗️ Modul-Struktur (features-neu/)

### Module 01-08 Hauptkomponenten

```
docs/planung/features-neu/
├── 00_infrastruktur/           # Cross-cutting Infrastructure
│   ├── betrieb/               # Operations
│   ├── integration/           # API Gateway, Event-Driven
│   ├── leistung/             # Performance & Bundle Optimization
│   ├── migrationen/          # Database Migrations
│   ├── sicherheit/           # Security Patterns
│   ├── skalierung/           # Territory + Seasonal Scaling
│   ├── standards/            # Code Standards
│   └── verwaltung/           # Management
│
├── 01_mein-cockpit/           # Dashboard + ROI
│   ├── artefakte/            # 44 Production-Ready Deliverables
│   ├── diskussionen/         # Planning Discussions
│   └── zukunft/              # Future Features
│
├── 02_neukundengewinnung/     # Lead Management
│   ├── analyse/              # ✅ Frontend Research (Sprint 2.1.2)
│   ├── artefakte/            # Security/Performance/Event Patterns
│   ├── backend/              # Backend-specific (minimal)
│   ├── diskussionen/         # Planning History
│   ├── email-posteingang/    # Email Integration
│   ├── kampagnen/            # Campaign Management
│   ├── lead-erfassung/       # Lead Capture
│   ├── shared/               # Cross-cutting (minimal)
│   └── testing/              # Test Patterns
│
├── 03_kundenmanagement/       # Customer Relations
│   ├── alle-kunden/          # Customer List
│   ├── analyse/              # Gap Analysis
│   ├── artefakte/            # 39 Production-Ready Deliverables
│   ├── neuer-kunde/          # Customer Creation
│   └── verkaufschancen/      # Opportunities
│
├── 04_auswertungen/          # Business Intelligence
│   ├── analyse/              # Analytics Planning
│   ├── artefakte/            # BI Deliverables
│   ├── kundenanalyse/        # Customer Analytics
│   └── umsatzuebersicht/     # Revenue Overview
│
├── 05_kommunikation/         # Omni-Channel Communication
│   ├── analyse/              # Communication Analysis
│   ├── artefakte/            # Email Engine Patterns
│   ├── interne-nachrichten/  # Internal Messages
│   └── team-chat/            # Team Communication
│
├── 06_einstellungen/         # Settings Platform
│   ├── analyse/              # Settings Analysis
│   ├── artefakte/            # 4 Weltklasse Technical Concepts
│   ├── benachrichtigungen/   # Notification Settings
│   ├── mein-profil/          # User Profile
│   └── sicherheit/           # Security Settings
│
├── 07_hilfe_support/         # CAR-Innovation
│   ├── analyse/              # AI-assisted Help Analysis
│   ├── artefakte/            # 25 AI-Artefakte (9.4/10)
│   ├── erste-schritte/       # Onboarding
│   ├── haeufige-fragen/      # FAQ
│   └── video-tutorials/      # Training Materials
│
└── 08_administration/        # Enterprise Admin
    ├── benutzerverwaltung/   # User Management
    ├── compliance-reports/   # DSGVO Reports
    ├── integration/          # External Integrations
    ├── phase-1-core/         # Core Admin Features
    ├── phase-2-integrations/ # Advanced Integrations
    └── system/               # System Administration
```

## 🔧 Infrastruktur Mini-Module (00_infrastruktur/)

### Cross-cutting Infrastructure Components

| Mini-Modul | Zweck | Status | Key Deliverables |
|------------|-------|--------|------------------|
| **betrieb** | Operations Excellence | 95% Ready | 6M+60T+10T State-Machine |
| **integration** | API Gateway + Events | 95% Ready | CQRS Light + LISTEN/NOTIFY |
| **leistung** | Performance Engineering | Ready | <200ms P95 + Bundle <200KB |
| **migrationen** | Database Evolution | Ready | Flyway + Schema Versioning |
| **sicherheit** | Security Foundation | Ready | ABAC + RLS + Territory Scoping |
| **skalierung** | Seasonal Autoscaling | 98% Ready | KEDA + Territory Labels |
| **standards** | Code Guidelines | Ready | SOLID + DRY + KISS Patterns |
| **verwaltung** | Admin Infrastructure | Ready | Multi-Tenancy + DSGVO |

## 📋 Sprint-Struktur (zentral)

### Phase 1: Foundation (6 Sprints) - COMPLETE
```
TRIGGER_SPRINT_1_1.md  ✅ Infrastructure Foundation
TRIGGER_SPRINT_1_2.md  ✅ Security Foundation
TRIGGER_SPRINT_1_3.md  ✅ CQRS Foundation
TRIGGER_SPRINT_1_4.md  ✅ Settings Foundation
TRIGGER_SPRINT_1_5.md  ✅ Performance Foundation
TRIGGER_SPRINT_1_6.md  ✅ Integration Foundation
```

### Phase 2: Core Business (5 Sprints) - IN PROGRESS
```
TRIGGER_SPRINT_2_1.md     ✅ Neukundengewinnung (Backend)
TRIGGER_SPRINT_2_1_1.md   ✅ P0 Hotfix (Integration Gaps)
TRIGGER_SPRINT_2_2.md     📋 Kundenmanagement
TRIGGER_SPRINT_2_3.md     📋 Kommunikation
TRIGGER_SPRINT_2_4.md     🟡 Cockpit
TRIGGER_SPRINT_2_5.md     🟡 Einstellungen
```

### Phase 3: Advanced Features (3 Sprints) - PLANNED
```
TRIGGER_SPRINT_3_1.md     🟡 Auswertungen
TRIGGER_SPRINT_3_2.md     🟡 Hilfe & Support
TRIGGER_SPRINT_3_3.md     🟡 Administration
```

## 🏛️ Architecture Decision Records (adr/)

### Zentrale Entscheidungen
```
adr/
├── README.md                                    # ADR Process
├── ADR-0001-cqrs-light.md                     # CQRS Light Pattern
├── ADR-0002-listen-notify-over-eventbus.md    # Event Architecture
├── ADR-0003-settings-registry-hybrid.md       # Settings Engine
├── ADR-0004-territory-rls-vs-lead-ownership.md # Security Model
├── ADR-0005-nginx-oidc-gateway.md             # Auth Gateway
├── ADR-0006-mock-governance.md                # Test Strategy
└── ADR-0007-rls-connection-affinity.md        # Database Performance
```

## 📑 Zentrale Master-Dokumente

### Strategic Documents (docs/planung/)
```
CRM_COMPLETE_MASTER_PLAN_V5.md      # Single Source of Truth
CRM_AI_CONTEXT_SCHNELL.md           # KI-optimiertes System-Verständnis
CRM_HUMAN_GUIDE_KOMPLETT.md         # Human-readable Guide
PRODUCTION_ROADMAP_2025.md          # Phase-basierte Roadmap
TRIGGER_INDEX.md                    # Sprint-Übersicht (Entry Point)
SPRINT_2_1_1_DELTA_LOG.md           # Hotfix-spezifisches Log
```

## 🎯 Strukturelle Erkenntnisse

### Stärken der aktuellen Struktur:
1. **Klare Modul-Trennung** - Jedes Modul hat eigenen Bereich
2. **Zentrale Sprints** - Keine Link-Brüche, stabile Referenzen
3. **Cross-cutting Infrastruktur** - 00_infrastruktur löst querschnittliche Themen
4. **Reife Artefakte** - Viele Production-Ready Deliverables vorhanden

### Gaps für Claude-Effizienz:
1. **Fehlende Einstiegspunkte** - Keine _index.md in Modulen
2. **Keine Sprint-Mapping** - Module → Sprint Zuordnung nicht klar
3. **Shared-Logik unklar** - Was ist kanonisch vs. modulspezifisch?
4. **Metadaten fehlen** - Keine Front-Matter für Filterung

### Sprint-Crosscutting-Realität:
```yaml
# Beispiel: Sprint 2.1.1 (Hotfix)
Module: [02_neukundengewinnung, 00_infrastruktur]
Facets: [backend, events, metrics]
Scope: Cross-module Integration

# Sprint 2.1.2 (Frontend Research)
Module: [02_neukundengewinnung]
Facets: [frontend]
Scope: Single-module Research
```

## 💡 Optimierungsvorschläge

### Minimal-Intervention (empfohlen):
1. **Modul-Einstiegsseiten** erstellen (`_index.md`)
2. **Sprint-Maps pro Modul** (Overlay auf zentrale Sprints)
3. **START_HERE_FOR_AI.md** in kritischen Modulen
4. **Front-Matter Tags** in zentralen Sprint-Docs
5. **Shared-Navigation** ohne physische Moves

### Nie anfassen (Link-Stabilität):
- Zentrale Sprint-Dokumente (TRIGGER_SPRINT_*.md)
- ADR-Verzeichnis und -Inhalte
- Master-Plan-Dokumente
- Bestehende Modul-Artefakte

## ✅ Validierung: Hybrid-Modell ist richtig

Die Analyse bestätigt: **Zentrale Sprints + Modulare Overlays** ist optimal, weil:

1. **Sprint 2.1.1** betrifft Module 02 + Infrastruktur → gehört nicht in ein Modul
2. **Sprint 2.1.2** ist rein Frontend/Modul 02 → könnte theoretisch ins Modul, aber würde Konsistenz brechen
3. **Bestehende Links** in Hunderten von Dokumenten bleiben stabil
4. **Cross-cutting ADRs** sind naturgemäß zentral

## 🎯 Nächste Schritte

1. **Zuerst:** Sprint 2.1.2 Frontend Research in bestehender Struktur finalisieren
2. **Dann:** Minimal-Overlays für Module 01, 02, 00_infrastruktur
3. **Optional:** Front-Matter Tags für bessere Navigation
4. **Niemals:** Physische Sprint-Moves oder ADR-Umstrukturierung