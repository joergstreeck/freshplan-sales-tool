# 🗺️ MASTER FEATURE OVERVIEW - FreshPlan Sales Tool 2.0

**Letzte Aktualisierung:** 19.07.2025  
**Status:** Lebendiges Dokument - wird kontinuierlich gepflegt  
**Version:** 2.0 - Vollständig mit allen 40 Features  

## 📊 Feature Status Dashboard

### 🟢 ACTIVE (In Arbeit)

| Feature | Code | Status | Progress | Nächster Schritt | Dokumente |
|---------|------|--------|----------|------------------|-----------|
| Security Foundation | FC-008 | 🟡 Tests deaktiviert | 85% | TODO-024/028 | [CLAUDE_TECH](/docs/features/ACTIVE/01_security_foundation/FC-008_CLAUDE_TECH.md) • [IMPL](/docs/features/ACTIVE/01_security_foundation/IMPLEMENTATION_GUIDE.md) |
| Opportunity Pipeline | M4 | 📋 Ready to Start | 0% | Backend Entities | [CLAUDE_TECH](/docs/features/ACTIVE/02_opportunity_pipeline/M4_CLAUDE_TECH.md) • [IMPL](/docs/features/ACTIVE/02_opportunity_pipeline/IMPLEMENTATION_GUIDE.md) |
| Calculator Modal | M8 | 📋 Ready to Start | 0% | Modal Wrapper | [CLAUDE_TECH](/docs/features/ACTIVE/03_calculator_modal/M8_CLAUDE_TECH.md) • [IMPL](/docs/features/ACTIVE/03_calculator_modal/IMPLEMENTATION_GUIDE.md) |
| **Hinweis:** | | | | | M8 ist eigenständig, da es Legacy-Calculator integriert |
| Permissions System | FC-009 | 📋 Planned | 0% | Technical Review | [CLAUDE_TECH](/docs/features/ACTIVE/04_permissions_system/FC-009_CLAUDE_TECH.md) • [IMPL](/docs/features/ACTIVE/04_permissions_system/IMPLEMENTATION_GUIDE.md) |
| UI Foundation | M1-M3-M7 | 🟡 Enhancement | 60% | **BLOCKIERT**: D1-D3 Entscheidungen | [NAV-40%](/docs/features/ACTIVE/05_ui_foundation/M1_CLAUDE_TECH.md) • [COCKPIT-60%](/docs/features/ACTIVE/05_ui_foundation/M3_SALES_COCKPIT_CLAUDE_TECH.md) • [SETTINGS-50%](/docs/features/ACTIVE/05_ui_foundation/M7_SETTINGS_CLAUDE_TECH.md) • [DECISIONS](/docs/features/ACTIVE/05_ui_foundation/DECISION_LOG.md) 🚨 |
| Quick Create | M2 | 📋 Ready to Start | 0% | FAB + Dialogs | [CLAUDE_TECH](/docs/features/ACTIVE/05_ui_foundation/M2_CLAUDE_TECH.md) • Teil von UI Foundation |

### 🔵 PLANNED (Geplant)

#### Core Business Features
| Feature | Code | Priority | Effort | Dependencies | Business Value |
|---------|------|----------|--------|--------------|----------------|
| E-Mail Integration | FC-003 | ⭐ HIGH | 5d | - | Zentrale Kommunikation |
| Verkäuferschutz | FC-004 | ⭐ KRITISCH | 2d | FC-008, FC-009 | Provisionsschutz |
| Xentral Integration | FC-005 | MEDIUM | 5d | - | ERP-Anbindung |
| Mobile App | FC-006 | LOW | 7d | Alle | Unterwegs-Zugriff |
| Chef-Dashboard | FC-007 | HIGH | 4d | M4 | Management KPIs |
| Customer Import | FC-010 | ⭐ KRITISCH | 10-16d | - | 5000+ Bestandskunden |
| Customer Refactor | M5 | HIGH* | 3d | FC-010 | Performance (*erhöht wegen FC-010) |
| Analytics Module | M6 | MEDIUM | 5d | M4, FC-007 | Insights & Reports |

#### Team & Communication
| Feature | Code | Priority | Effort | Dependencies | Business Value |
|---------|------|----------|--------|--------------|----------------|
| Team Communication | FC-012 | MEDIUM | 4d | - | Team-Kollaboration |

#### Pipeline Enhancement Features
| Feature | Code | Priority | Effort | Dependencies | Business Value |
|---------|------|----------|--------|--------------|----------------|
| Duplicate Detection | FC-013 | HIGH | 3d | M4 | Datenqualität |
| Activity Timeline | FC-014 | HIGH | 4d | M4 | Kundenhistorie |
| Deal Loss Analysis | FC-015 | MEDIUM | 3d | M4 | Vertriebsoptimierung |
| Opportunity Cloning | FC-016 | MEDIUM | 2d | M4 | Effizienz |

#### Advanced Features
| Feature | Code | Priority | Effort | Dependencies | Business Value |
|---------|------|----------|--------|--------------|----------------|
| Sales Gamification | FC-017 | LOW | 10-12d | M4, FC-019 | Motivation + Team-Spirit |
| Mobile PWA | FC-018 | HIGH | 6d | - | Offline-Fähigkeit |
| Advanced Sales Metrics | FC-019 | HIGH | 5d | M4, M6 | Vertriebssteuerung |

#### Quick Wins & Productivity
| Feature | Code | Priority | Effort | Dependencies | Business Value |
|---------|------|----------|--------|--------------|----------------|
| Quick Wins | FC-020 | MEDIUM | 4d | - | Produktivität++ |
| Integration Hub | FC-021 | MEDIUM | 6d | - | Tool-Anbindung |

#### Mobile & Field Service Features (NEU!)
| Feature | Code | Priority | Effort | Dependencies | Business Value |
|---------|------|----------|--------|--------------|----------------|
| Mobile Light | FC-022 | HIGH | 3d | - | Außendienst Basic |
| Event Sourcing | FC-023 | HIGH | 8d | - | Audit & History |
| File Management | FC-024 | HIGH | 4d | - | Dokumente zentral |
| DSGVO Compliance | FC-025 | HIGH | 5d | - | Rechtssicherheit |

#### Analytics & Intelligence
| Feature | Code | Priority | Effort | Dependencies | Business Value |
|---------|------|----------|--------|--------------|----------------|
| Analytics Platform | FC-026 | MEDIUM | 6d | M6 | Business Intelligence |
| Magic Moments | FC-027 | MEDIUM | 4d | M4 | Verkaufschancen |

#### Communication & Automation
| Feature | Code | Priority | Effort | Dependencies | Business Value |
|---------|------|----------|--------|--------------|----------------|
| WhatsApp Business | FC-028 | HIGH | 5d | - | Kundenkontakt |
| Voice-First Interface | FC-029 | HIGH | 7d | - | Hands-free Bedienung |

#### UX & Productivity Boosters
| Feature | Code | Priority | Effort | Dependencies | Business Value |
|---------|------|----------|--------|--------------|----------------|
| One-Tap Actions | FC-030 | HIGH | 3d | - | Schnellaktionen |
| Smart Templates | FC-031 | HIGH | 4d | - | Zeitersparnis |
| Offline-First | FC-032 | HIGH | 6d | FC-018 | Überall arbeiten |

#### Visual & Social Features
| Feature | Code | Priority | Effort | Dependencies | Business Value |
|---------|------|----------|--------|--------------|----------------|
| Visual Customer Cards | FC-033 | MEDIUM | 3d | M3 | Bessere Übersicht |
| Instant Insights | FC-034 | MEDIUM | 4d | FC-019 | Schnelle Entscheidungen |
| Social Selling Helper | FC-035 | MEDIUM | 3d | - | Social Media Sales |
| Beziehungsmanagement | FC-036 | MEDIUM | 5d | M4 | Kundenbeziehungen |

### ✅ COMPLETED (Abgeschlossen)

| Feature | Code | Completion Date | Notes | Details |
|---------|------|-----------------|-------|---------|
| Technical Foundation | - | 14.07.2025 | Monorepo + CI/CD + Walking Skeleton ✅ | [Docs](/docs/features/COMPLETED/00_technical_foundation/README.md) |

**⚠️ Noch nicht verschoben (nicht 100%):**
| Basic Auth | FC-008 | 85% (17.07.2025) | Tests deaktiviert | [CLAUDE_TECH](/docs/features/ACTIVE/01_security_foundation/FC-008_CLAUDE_TECH.md) |

**🚨 Wichtiger Hinweis:** Technical Foundation hat nur 3 hardcoded Rollen (admin, manager, sales). FC-009 bringt das echte Permission System!

## 🔗 Feature Dependencies Graph

```
FC-008 Security Foundation (85%)
    ├─→ FC-009 Permissions System
    │      └─→ FC-004 Verkäuferschutz
    │
    ├─→ M4 Opportunity Pipeline + FC-011 Bonitätsprüfung
    │      ├─→ M8 Calculator Modal
    │      ├─→ FC-013 Duplicate Detection
    │      ├─→ FC-014 Activity Timeline
    │      ├─→ FC-015 Deal Loss Analysis
    │      ├─→ FC-016 Opportunity Cloning
    │      ├─→ FC-007 Chef-Dashboard
    │      ├─→ M6 Analytics Module
    │      │      ├─→ FC-019 Advanced Sales Metrics
    │      │      ├─→ FC-026 Analytics Platform
    │      │      └─→ FC-027 Magic Moments
    │      └─→ FC-036 Beziehungsmanagement
    │
    └─→ FC-003 E-Mail Integration
           ├─→ FC-006 Mobile App
           └─→ FC-012 Team Communication

UI Foundation (M1/M3/M7) (50%)
    ├─→ M2 Quick Create
    ├─→ M3 Sales Cockpit Enhancement
    │      ├─→ FC-033 Visual Customer Cards
    │      ├─→ FC-034 Instant Insights
    │      └─→ KI-Integration (OpenAI)
    └─→ M7 Settings Enhancement

FC-010 Customer Import
    └─→ M5 Customer Refactor (Performance)

FC-018 Mobile PWA
    └─→ FC-032 Offline-First Architecture

FC-023 Event Sourcing (Infrastructure für alle)
    ├─→ FC-025 DSGVO Compliance
    └─→ Alle Features mit History/Audit

Unabhängige Feature-Gruppen:
- FC-020 Quick Wins (Command Palette, etc.)
- FC-021 Integration Hub
- FC-022 Mobile Light
- FC-024 File Management
- FC-028 WhatsApp Business
- FC-029 Voice-First Interface
- FC-030 One-Tap Actions
- FC-031 Smart Templates
- FC-035 Social Selling Helper
- FC-017 Sales Gamification (nach M4 + FC-019)
```

## 📈 Velocity Tracking

| Sprint | Planned | Delivered | Velocity |
|--------|---------|-----------|----------|
| Sprint 0 | FC-008 | FC-008 (85%) | 0.85 |
| Sprint 1 | M4, M8 | - | - |
| Sprint 2 | FC-009, FC-004 | - | - |

## 🎯 Business Priorities (Stand: 17.07.2025)

### Must Have (MVP)
1. ✅ Authentication & Security
2. 🔄 Opportunity Pipeline
3. 🔄 Calculator Integration
4. 📋 Verkäuferschutz Basis

### Should Have (V1.0)
5. 📋 E-Mail Integration
6. 📋 Chef-Dashboard
7. 📋 Permissions System

### Nice to Have (V2.0)
8. 📋 Xentral Integration
9. 📋 Advanced Analytics
10. 📋 Mobile App

## 🚨 Kritische Pfade

### Pfad 1: Sales Enablement
```
Security → Pipeline → Calculator → Verkäuferschutz
```
**Blockiert durch:** Security Tests (TODO-024/028)

### Pfad 2: Management Tools
```
Pipeline → Dashboard → Analytics
```
**Blockiert durch:** Pipeline Implementation

### Pfad 3: Communication Hub
```
E-Mail → Mobile → Notifications
```
**Unabhängig:** Kann parallel starten

## 📝 Entscheidungs-Log

| Datum | Entscheidung | Begründung | Impact |
|-------|--------------|------------|--------|
| 15.07.2025 | Security Tests deaktiviert | CI blockiert | FC-008 incomplete |
| 16.07.2025 | V5 Master Plan eingeführt | Claude-Optimierung | Bessere Übergaben |
| 17.07.2025 | FC-009 vor FC-004 | Technische Abhängigkeit | Verzögerung Verkäuferschutz |

## 🔄 Nächste Reviews

- **Weekly:** Jeden Montag - Progress Review
- **Sprint:** Alle 2 Wochen - Sprint Planning
- **Monthly:** Business Priority Alignment
- **Quarterly:** Roadmap Adjustment

## 📚 Referenzen

- [V5 Master Plan](/docs/CRM_COMPLETE_MASTER_PLAN_V5.md)
- [Complete Roadmap](/docs/features/2025-07-12_COMPLETE_FEATURE_ROADMAP.md)
- [Technical Standards](/CLAUDE.md)
- [Way of Working](/WAY_OF_WORKING.md)

---

**⚡ Quick Actions für Claude:**
- Status Update? → Editiere diese Tabelle
- Neues Feature? → Erstelle Ordner in ACTIVE/PLANNED
- Abhängigkeit geändert? → Update Graph
- Blocker? → Markiere in Kritische Pfade