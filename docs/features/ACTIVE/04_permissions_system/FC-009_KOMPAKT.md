# 🔐 FC-009 PERMISSIONS SYSTEM (KOMPAKT)

**Erstellt:** 17.07.2025 13:40  
**Status:** BEREIT FÜR IMPLEMENTATION  
**Feature-Typ:** 🔧 BACKEND  
**Priorität:** KRITISCH nach FC-008  

---

## 🧠 WAS WIR BAUEN

**Problem:** Nur 3 hardcoded Rollen (admin, manager, sales)  
**Lösung:** Menüpunkt-basierte Permissions mit Capabilities  
**Warum:** Flexibilität für Kunden ohne Code-Änderungen  

> **Business Case:** Sales Manager soll NUR seine Team-Kunden sehen, nicht alle 5000 Kunden

---

## 🚀 SOFORT LOSLEGEN (15 MIN)

### 1. **Erste Migration erstellen:**
```bash
cd backend/src/main/resources/db/migration
touch V3.0__create_permission_tables.sql
# → Details: [Abschnitt "Database Schema"](#database-schema)
```

### 2. **Permission Entity anlegen:**
```bash
cd backend/src/main/java/de/freshplan/domain/permission/entity
touch Permission.java
# → Template: [Abschnitt "Entity Code"](#entity-code)
```

### 3. **Offene Fragen klären:**
```bash
cat docs/features/ACTIVE/04_permissions_system/DECISION_LOG.md
# → 5 kritische Entscheidungen für Jörg
```

**Gesamt: 7-10 Tage Aufwand**

---

## 📋 KRITISCHE ENTSCHEIDUNGEN

- **Multi-Team Membership:** JA oder NEIN? → [Details](#multi-team)
- **Orphaned Customers:** Was passiert bei Team-Löschung? → [Details](#orphaned)  
- **Audit Requirements:** Jede Permission-Änderung tracken? → [Details](#audit)
- **Cache Strategy:** 5 Min TTL oder Session-based? → [Details](#cache)

---

## 🔗 VOLLSTÄNDIGE DETAILS

**Technische Lösung:** [TECHNICAL_SOLUTION.md](./TECHNICAL_SOLUTION.md)
- [Database Schema](#database-schema) - 7 neue Tabellen
- [Entity Code](#entity-code) - Copy-paste ready
- [Migration Strategy](#migration-strategy) - Mit Rollback
- [API Endpoints](#api-endpoints) - REST Design
- [Frontend Integration](#frontend-integration) - React Hooks

**Dort findest du:**
- Komplette SQL Scripts
- Java Entity Templates  
- React Component Examples
- Performance Benchmarks
- E2E Test Scenarios

---

## 📞 NÄCHSTE SCHRITTE

1. **DECISION_LOG durchgehen** - Jörg's Input needed
2. **Migration V3.0 schreiben** - Schema first
3. **Entities implementieren** - Permission, Team, Ownership
4. **Service Layer** - Business Logic
5. **REST Endpoints** - CRUD + Special Ops
6. **Frontend Hooks** - usePermissions()
7. **Migration durchführen** - Rollen → Permissions

**WICHTIG:** Ohne FC-009 bleibt alles bei 3 hardcoded Rollen!

---

## 🧭 NAVIGATION & VERWEISE

### 📋 Zurück zum Überblick:
- **[📊 Master Plan V5](/docs/CRM_COMPLETE_MASTER_PLAN_V5.md)** - Vollständige Feature-Roadmap
- **[🗺️ Feature Overview](/docs/features/MASTER/FEATURE_OVERVIEW.md)** - Alle 40 Features im Überblick

### 🔗 Dependencies (Required):
- **[🔒 FC-008 Security Foundation](/docs/features/ACTIVE/01_security_foundation/FC-008_KOMPAKT.md)** - Authentication Layer als Basis

### ⚡ Direkt integriert in:
- **[👥 M5 Customer Refactor](/docs/features/PLANNED/12_customer_refactor_m5/M5_KOMPAKT.md)** - Customer Ownership Model
- **[📊 M4 Opportunity Pipeline](/docs/features/ACTIVE/02_opportunity_pipeline/M4_KOMPAKT.md)** - Opportunity-Permissions
- **[🛡️ FC-004 Verkäuferschutz](/docs/features/PLANNED/07_verkaeuferschutz/FC-004_KOMPAKT.md)** - Protection Rules

### 🚀 Ermöglicht folgende Features:
- **[👨‍💼 FC-007 Chef-Dashboard](/docs/features/PLANNED/10_chef_dashboard/FC-007_KOMPAKT.md)** - Team-based Analytics
- **[📥 FC-010 Customer Import](/docs/features/PLANNED/11_customer_import/FC-010_KOMPAKT.md)** - Import-Permissions
- **[📊 M6 Analytics Module](/docs/features/PLANNED/13_analytics_m6/M6_KOMPAKT.md)** - Data Access Control
- **[📈 FC-019 Advanced Sales Metrics](/docs/features/PLANNED/19_advanced_metrics/FC-019_KOMPAKT.md)** - Team Performance

### 🎨 UI Integration:
- **[⚙️ M7 Settings](/docs/features/ACTIVE/05_ui_foundation/M7_SETTINGS_KOMPAKT.md)** - Permission Management UI
- **[📊 M3 Sales Cockpit](/docs/features/ACTIVE/05_ui_foundation/M3_SALES_COCKPIT_KOMPAKT.md)** - Permission-based Filtering

### 🔧 Technische Details:
- **[TECHNICAL_SOLUTION.md](./TECHNICAL_SOLUTION.md)** - Vollständige technische Umsetzung
- **[DECISION_LOG.md](./DECISION_LOG.md)** - Kritische Entscheidungen für Jörg