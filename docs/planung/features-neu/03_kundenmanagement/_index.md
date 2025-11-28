---
module: "03_kundenmanagement"
domain: "shared"
doc_type: "guideline"
status: "active"
owner: "team/leads"
updated: "2025-11-28"
---

# 🎯 Modul 03 Kundenmanagement - Vollständige Planungsdokumentation

**📍 Navigation:** Home → Planung → 03 Kundenmanagement

**📅 Letzte Aktualisierung:** 2025-11-28
**🎯 Status:** ACTIVE (Sprint 2.1.7.7 Complete)
**📊 Vollständigkeit:** Struktur 100%, Multi-Location Management COMPLETE

## ✅ Status (Kurzübersicht)
- **Current Sprint:** Sprint 2.1.7.7 - Multi-Location Management ✅ COMPLETE (PR #145)
- **Backend:** ✅ *Active* (BranchService + HierarchyMetrics + Address-Matching)
- **Frontend:** ✅ *Active* (HierarchyDashboard + CreateBranchDialog + TreeView)
- **Shared:** ✅ *Active* (Server-Driven Architecture + LocationServiceSchema)
- **Legacy:** 📚 *Archiviert* → [legacy-planning/](./legacy-planning/)

## 🚀 Start here (Reihenfolge)
1) **Sprint-Kontext:** [SPRINT_MAP.md](./SPRINT_MAP.md)
2) **Technical Concept:** [technical-concept.md](./technical-concept.md)
3) **Domain-Einstieg:** [backend/_index.md](./backend/_index.md) **oder** [frontend/_index.md](./frontend/_index.md)
4) **Details bei Bedarf:** [analyse/](./analyse/)
5) **Produktionsmuster:** [artefakte/](./artefakte/)

## 🎯 EXECUTIVE SUMMARY

**Mission:** Zentrale Kundenmanagement-Platform mit 360°-Kundensicht und Aktivitäten-Tracking
**Problem:** Fragmentierte Kundendaten und manuelle Prozesse führen zu ineffizientem Kundenmanagement
**Solution:** Integrierte Kundenplattform mit Activity-Streams, Verkaufschancen-Pipeline und Kontakthistorie

## 🏗️ MODULSTRUKTUR-ÜBERSICHT

```
03_kundenmanagement/
├── _index.md                    # Diese Übersicht
├── SPRINT_MAP.md                # Links zu zentralen Sprints
├── technical-concept.md         # Überblick (keine Feature-Details)
├── backend/
│   └── _index.md                # Backend-Einstieg + Konzepte
├── frontend/
│   └── _index.md                # Frontend-Einstieg + UI-Konzepte
├── shared/
│   └── _index.md                # Kanonische Contracts
├── analyse/
│   └── _index.md                # Analysen und Recherchen
├── artefakte/
│   └── _index.md                # Produktionsmuster und Guidelines
└── legacy-planning/             # Historie/Detailplanungen
    ├── aktivitaeten/
    ├── alle-kunden/
    ├── diskussionen/
    ├── neuer-kunde/
    └── verkaufschancen/
```

## 📁 QUICK START

### 🔥 **Für neue Claude-Instanzen:**

1. **Sprint-Kontext verstehen:** → [SPRINT_MAP.md](./SPRINT_MAP.md)
2. **Technical Overview:** → [technical-concept.md](./technical-concept.md)
3. **Backend-Konzepte:** → [backend/_index.md](./backend/_index.md)
4. **Frontend-Konzepte:** → [frontend/_index.md](./frontend/_index.md)
5. **Kanonische Contracts:** → [shared/_index.md](./shared/_index.md)

### 🎯 QUICK DECISION MATRIX

```yaml
"Ich plane Backend-Features":
  → Start: backend/_index.md (Architektur + Datenmodell)

"Ich plane Frontend-Implementation":
  → Start: frontend/_index.md (UI-Konzepte + Components)

"Ich brauche API-Contracts":
  → Start: shared/_index.md (Kundenmanagement APIs + Events)

"Ich arbeite an Cross-Module Integration":
  → Start: shared/_index.md (Event-Integration + Data Flow)

"Ich will historische Planungen":
  → Start: legacy-planning/ (Detailspezifikationen)
```

## 🚀 CURRENT STATUS & DEPENDENCIES

### ✅ **Completed:**
- **Sprint 2.1.7.7 (28.11.2025):** Multi-Location Management & Enterprise Architecture [PR #145](https://github.com/joergstreeck/freshplan-sales-tool/pull/145)
  - Parent-Child Hierarchie für Filialisten (HierarchyType: STANDALONE/HEADQUARTER/FILIALE)
  - Server-Driven Architecture: fieldCatalog.json entfernt, Backend als Single Source of Truth
  - BranchService + Address-Matching + HierarchyMetrics Services
  - HierarchyDashboard + CreateBranchDialog + TreeView Components
  - Tests: 1617+ Tests GREEN
  - Migrations: V10034-V10035 (location services schema)

- **Sprint 2.1.7.4 (22.10.2025):** CustomerStatus Architecture + Lead Parity [PR #143](https://github.com/freshplan/freshplan-sales-tool/pull/143)
  - CustomerStatus Enum + EnumResource Integration
  - Lead Parity Fields (leadStatus, leadSource, sourceDetails)
  - Manual Activation Workflow + Seasonal Business Support
  - Tests: 1617/1617 GREEN
  - Migrations: V10032, V10033, V90008

- **Strukturelle Vorbereitung:** Standard-Verzeichnisse angelegt
- **Legacy-Konsolidierung:** Historische Planungen archiviert

### 🔄 **Recent Changes (Sprint 2.1.7.7):**
- **Backend:**
  - BranchService: createBranch(), validateParent(), getBranches()
  - HierarchyMetricsService: Roll-up Umsätze für HEADQUARTER
  - AddressMatchingService: Xentral-Integration für Adresserkennung
  - LocationServiceSchemaResource: Server-Driven Field Definitions
- **Frontend:**
  - HierarchyDashboard: Branch-Übersicht mit Metriken
  - CreateBranchDialog: Formular für neue Filialen + Tests
  - HierarchyTreeView: Visuelle Hierarchie-Darstellung
  - UI-Aktivierung: FILIALE Option enabled + Parent-Selection
- **Architecture:**
  - fieldCatalog.json + fieldCatalogExtensions.json ENTFERNT
  - Backend LocationServiceSchema als Single Source of Truth
  - useLocationServiceSchema() Hook für Field Definitions
- **Migrations:**
  - V10034: Location Services Schema
  - V10035: Additional Location Fields

### 📋 **Dependencies:**
- **Integration:** Event-System von Modul 02 (Lead-Handover) ✅
- **Security:** RBAC-System (Kundendaten-Zugriff)
- **Performance:** Optimierte Datenabfragen für Kundenhistorie

## 🔗 **Zentrale Referenzen**

- **Master Plan:** [CRM_COMPLETE_MASTER_PLAN_V5.md](../../CRM_COMPLETE_MASTER_PLAN_V5.md)
- **Sprints:** [TRIGGER_INDEX.md](../../TRIGGER_INDEX.md)
- **Modul 02 Integration:** [../02_neukundengewinnung/shared/_index.md](../02_neukundengewinnung/shared/_index.md)

## 📚 **Erweiterte Dokumentation**

- **Legacy Planning:** [./legacy-planning/](./legacy-planning/) – Historische Planungsartefakte & Detail-Spezifikationen
- **Analyse-Dokumente:** [./analyse/](./analyse/) – Recherchen und Bewertungen