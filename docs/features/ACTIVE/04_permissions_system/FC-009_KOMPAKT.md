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