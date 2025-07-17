# 🗺️ MASTER FEATURE OVERVIEW - FreshPlan Sales Tool 2.0

**Letzte Aktualisierung:** 17.07.2025  
**Status:** Lebendiges Dokument - wird kontinuierlich gepflegt  

## 📊 Feature Status Dashboard

### 🟢 ACTIVE (In Arbeit)

| Feature | Code | Status | Progress | Nächster Schritt | Dokumente |
|---------|------|--------|----------|------------------|-----------|
| Security Foundation | FC-008 | 🟡 Tests deaktiviert | 85% | TODO-024/028 | [KOMPAKT](../ACTIVE/01_security_foundation/FC-008_KOMPAKT.md) • [IMPL](../ACTIVE/01_security_foundation/IMPLEMENTATION_GUIDE.md) |
| Opportunity Pipeline | M4 | 📋 Ready to Start | 0% | Backend Entities | [KOMPAKT](../ACTIVE/02_opportunity_pipeline/M4_KOMPAKT.md) • [IMPL](../ACTIVE/02_opportunity_pipeline/IMPLEMENTATION_GUIDE.md) |
| Calculator Modal | M8 | 📋 Ready to Start | 0% | Modal Wrapper | [KOMPAKT](../ACTIVE/03_calculator_modal/M8_KOMPAKT.md) • [IMPL](../ACTIVE/03_calculator_modal/IMPLEMENTATION_GUIDE.md) |
| **Hinweis:** | | | | | M8 ist eigenständig, da es Legacy-Calculator integriert |
| Permissions System | FC-009 | 📋 Planned | 0% | Technical Review | [KOMPAKT](../ACTIVE/04_permissions_system/FC-009_KOMPAKT.md) • [IMPL](../ACTIVE/04_permissions_system/IMPLEMENTATION_GUIDE.md) |
| UI Foundation | M1-M3-M7 | 🟡 Enhancement | 60% | **BLOCKIERT**: D1-D3 Entscheidungen | [NAV-40%](../ACTIVE/05_ui_foundation/M1_NAVIGATION_KOMPAKT.md) • [COCKPIT-60%](../ACTIVE/05_ui_foundation/M3_SALES_COCKPIT_KOMPAKT.md) • [SETTINGS-50%](../ACTIVE/05_ui_foundation/M7_SETTINGS_KOMPAKT.md) • [DECISIONS](../ACTIVE/05_ui_foundation/DECISION_LOG.md) 🚨 |
| Quick Create | M2 | 📋 Ready to Start | 0% | FAB + Dialogs | [KOMPAKT](../ACTIVE/05_ui_foundation/M2_QUICK_CREATE_KOMPAKT.md) • Teil von UI Foundation |

### 🔵 PLANNED (Geplant)

| Feature | Code | Priority | Effort | Dependencies | Business Value |
|---------|------|----------|--------|--------------|----------------|
| Customer Import | FC-010 | ⭐ KRITISCH | 10-16d | - | 5000+ Bestandskunden |
| Verkäuferschutz | FC-004 | ⭐ KRITISCH | 2d | FC-008, FC-009 | Provisionsschutz |
| E-Mail Integration | FC-003 | HIGH | 5d | - | Zentrale Kommunikation |
| Chef-Dashboard | FC-007 | HIGH | 4d | M4 | Management KPIs |
| Customer Refactor | M5 | HIGH* | 3d | FC-010 | Performance (*erhöht wegen FC-010) |
| Xentral Integration | FC-005 | MEDIUM | 5d | - | Automatisierung |
| Analytics | M6 | MEDIUM | 5d | M4, FC-007 | Insights |
| Mobile App | FC-006 | LOW | 7d | Alle | Unterwegs-Zugriff |

### ✅ COMPLETED (Abgeschlossen)

| Feature | Code | Completion Date | Notes | Details |
|---------|------|-----------------|-------|---------|
| Technical Foundation | - | 14.07.2025 | Monorepo + CI/CD + Walking Skeleton ✅ | [Docs](../COMPLETED/00_technical_foundation/README.md) |

**⚠️ Noch nicht verschoben (nicht 100%):**
| Basic Auth | FC-008 | 85% (17.07.2025) | Tests deaktiviert | [KOMPAKT](../ACTIVE/01_security_foundation/FC-008_KOMPAKT.md) |

**🚨 Wichtiger Hinweis:** Technical Foundation hat nur 3 hardcoded Rollen (admin, manager, sales). FC-009 bringt das echte Permission System!

## 🔗 Feature Dependencies Graph

```
FC-008 Security Foundation (85%)
    ├─→ FC-009 Permissions System
    │      └─→ FC-004 Verkäuferschutz
    │
    ├─→ M4 Opportunity Pipeline
    │      ├─→ M8 Calculator Modal
    │      ├─→ FC-007 Chef-Dashboard
    │      └─→ M6 Analytics
    │
    └─→ FC-003 E-Mail Integration
           └─→ FC-006 Mobile App

UI Foundation (M1/M3/M7) (50%)
    ├─→ M2 Quick Create
    ├─→ M3 Sales Cockpit Enhancement
    │      ├─→ FC-010 Customer Import (für echte Daten)
    │      └─→ KI-Integration (OpenAI)
    └─→ M7 Settings Enhancement

FC-010 Customer Import
    └─→ M5 Customer Refactor (Performance)
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

- [V5 Master Plan](../../CRM_COMPLETE_MASTER_PLAN_V5.md)
- [Complete Roadmap](../2025-07-12_COMPLETE_FEATURE_ROADMAP.md)
- [Technical Standards](../../CLAUDE.md)
- [Way of Working](../../WAY_OF_WORKING.md)

---

**⚡ Quick Actions für Claude:**
- Status Update? → Editiere diese Tabelle
- Neues Feature? → Erstelle Ordner in ACTIVE/PLANNED
- Abhängigkeit geändert? → Update Graph
- Blocker? → Markiere in Kritische Pfade