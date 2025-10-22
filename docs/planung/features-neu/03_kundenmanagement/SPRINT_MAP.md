---
module: "03_kundenmanagement"
doc_type: "sprint_map"
status: "active"
owner: "team/leads"
updated: "2025-10-22"
---

# 🗺️ SPRINT MAP - Modul 03 Kundenmanagement

**📍 Navigation:** [_index.md](./_index.md) → SPRINT_MAP.md

## 🎯 SPRINT-ÜBERSICHT

### ✅ **Abgeschlossene Sprints**

**Sprint 2.1.7.4 - CustomerStatus Architecture & Lead Parity** *(Complete)*
- Status: ✅ COMPLETE
- PR: [#143](https://github.com/freshplan/freshplan-sales-tool/pull/143)
- Commit: ade7fc2fa
- Merged: 2025-10-22 17:06:22 UTC
- Scope: CustomerStatus Architecture + Lead Parity + Manual Activation + Seasonal Business
- Deliverables: 8/8 COMPLETE
- Tests: 1617/1617 GREEN
- Migrations: V10032 (Lead Parity), V10033 (Status Cleanup + Seasonal), V90008 (DEV-SEED)
- Link: [SPRINT_2_1_7_4](./artefakte/SPRINT_2_1_7_4/SUMMARY.md)

### 📋 **Geplante Sprints (TBD)**

**Sprint 3.1 - Kundenmanagement Foundation** *(Geplant)*
- Status: 📋 Planned
- Scope: Grundlegende Kundenentitäten und CRUD-Operationen
- Dependencies: Modul 02 Event-System
- Link: *TBD*

**Sprint 3.2 - Aktivitäten & Timeline** *(Geplant)*
- Status: 📋 Planned
- Scope: Kundenaktivitäten-Tracking und Timeline-Views
- Dependencies: Sprint 3.1
- Link: *TBD*

**Sprint 3.3 - Verkaufschancen-Pipeline** *(Geplant)*
- Status: 📋 Planned
- Scope: Sales-Pipeline und Opportunity-Management
- Dependencies: Sprint 3.2
- Link: *TBD*

## 🔗 **Zentrale Sprint-Trigger**

### 🎯 **Trigger-Index (Übergeordnet)**
- **Hauptindex:** [../../TRIGGER_INDEX.md](../../TRIGGER_INDEX.md)
- **Produktions-Roadmap:** [../../PRODUCTION_ROADMAP_2025.md](../../PRODUCTION_ROADMAP_2025.md)

### 🔄 **Abhängigkeiten zu anderen Modulen**

**Von Modul 02 (Neukundengewinnung):**
- Event-System für Lead-to-Customer Conversion
- RBAC-Patterns für Datenzugriff
- Security-Patterns für Kundendaten

**Zu anderen Modulen:**
- Modul 05 (Kommunikation): Kundenkommunikation-Events
- Modul 06 (Einstellungen): User-Permissions für Kundenmanagement

## 📅 **Sprint-Planung Timeline**

```
Q4 2025 (Geplant):
├── Sprint 3.1 Foundation     [4 Wochen]
├── Sprint 3.2 Aktivitäten    [3 Wochen]
└── Sprint 3.3 Pipeline       [4 Wochen]

Q1 2026 (Erweitert):
├── Sprint 3.4 Analytics      [3 Wochen]
└── Sprint 3.5 Integration    [2 Wochen]
```

## 🎯 **Quick Sprint Access**

**Aktueller Fokus:** *Strukturelle Vorbereitung*
**Nächster Sprint:** *Sprint 3.1 Foundation (TBD)*

**Für Implementation:**
1. Trigger-Dokument erstellen für Sprint 3.1
2. Backend-Architektur definieren → [backend/_index.md](./backend/_index.md)
3. Frontend-Konzepte planen → [frontend/_index.md](./frontend/_index.md)

## 🔄 **Sprint-Status Tracking**

| Sprint | Status | Completion | PR | Notes |
|--------|--------|------------|----|----|
| 2.1.7.4 CustomerStatus | ✅ COMPLETE | 100% | [#143](https://github.com/freshplan/freshplan-sales-tool/pull/143) | CustomerStatus Architecture + Lead Parity |
| 3.1 Foundation | 📋 Planned | 0% | - | Kundenentitäten + CRUD |
| 3.2 Aktivitäten | 📋 Planned | 0% | - | Activity-Streams |
| 3.3 Pipeline | 📋 Planned | 0% | - | Sales-Opportunities |

*Aktualisierung: 2025-10-22*