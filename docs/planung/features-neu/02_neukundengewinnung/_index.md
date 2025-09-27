---
module: "02_neukundengewinnung"
domain: "shared"
doc_type: "guideline"
status: "approved"
owner: "team/leads"
updated: "2025-09-27"
---

# 🎯 Modul 02 Neukundengewinnung - Vollständige Planungsdokumentation

**📍 Navigation:** Home → Planung → 02 Neukundengewinnung

**📅 Letzte Aktualisierung:** 2025-09-27
**🎯 Status:** PRODUCTION-READY (Backend) + RESEARCH COMPLETE (Frontend)
**📊 Vollständigkeit:** Backend 100%, Frontend Research 100%, Implementation MVP planned

## ✅ Status (Kurzübersicht)
- **Backend:** ✅ *Production* (Sprint 2.1 + 2.1.1)
- **Frontend:** 📋 *Research complete* (Sprint 2.1.2) – bereit für Thin Vertical Slice
- **Patterns:** ✅ *Copy‑Paste ready* (Security/Performance/Events)
- **Legacy:** 📚 *Archiviert* → [legacy-planning/](./legacy-planning/)

## 🚀 Start here (Reihenfolge)
1) **Sprint‑Kontext:** [SPRINT_MAP.md](./SPRINT_MAP.md)
2) **Domain‑Einstieg:** [backend/_index.md](./backend/_index.md) **oder** [frontend/_index.md](./frontend/_index.md)
3) **Details bei Bedarf:** [analyse/](./analyse/)
4) **Produktionsmuster:** [artefakte/](./artefakte/)

## 🎯 EXECUTIVE SUMMARY

**Mission:** Lead-Management System mit Territory-Scoping und automatisierten T+3/T+7 Follow-ups
**Problem:** Manuelle Follow-up-Prozesse führen zu verlorenen Leads und ineffizienter Neukundengewinnung
**Solution:** Event-getriebenes Lead-Management mit PostgreSQL LISTEN/NOTIFY, RBAC-Sichtbarkeiten und Real-time Dashboard

> ⚠️ **STRUKTUR-HINWEIS für neue Claude-Instanzen:**
> Diese Struktur enthält **sowohl neue Hybrid-Overlays als auch Legacy-Planungsartefakte**.
> **START HIER:** _index.md → SPRINT_MAP.md → backend/frontend/shared/
> **Legacy/Planung:** lead-erfassung/, email-posteingang/, kampagnen/, diskussionen/ (historisch)

## 🚀 **STARTPUNKTE**

**Zentrale Sprints:**
- **Trigger-Index:** [../../TRIGGER_INDEX.md](../../TRIGGER_INDEX.md)
- **Sprint 2.1 (Backend):** [../../TRIGGER_SPRINT_2_1.md](../../TRIGGER_SPRINT_2_1.md)
- **Sprint 2.1.1 (Hotfix):** [../../TRIGGER_SPRINT_2_1_1.md](../../TRIGGER_SPRINT_2_1_1.md)

**Modul-spezifisch:**
- **Sprint-Map (diese Modul):** [./SPRINT_MAP.md](./SPRINT_MAP.md)
- **Frontend Research (Sprint 2.1.2):** [./frontend/analyse/_index.md](./frontend/analyse/_index.md)

## 🏗️ MODULSTRUKTUR-ÜBERSICHT

```
02_neukundengewinnung/
├── _index.md                    # Diese Übersicht
├── SPRINT_MAP.md                # Links zu zentralen Sprints (2.1, 2.1.1, 2.1.2)
├── backend/
│   ├── _index.md                # Backend-Einstieg + Links zu ADRs
│   ├── analyse/                 # Backend-Analysen
│   └── konzepte/                # Backend-Konzepte
├── frontend/
│   ├── _index.md                # Frontend-Einstieg
│   ├── analyse/                 # ✅ Research-Dokumente (INVENTORY, API_CONTRACT, etc.)
│   └── konzepte/                # UI-Patterns, Component-Specs
└── shared/
    ├── _index.md                # Kanonische Contracts
    └── contracts/               # Event-Envelopes, RBAC-Matrix, Metrics
```

## 📁 QUICK START

### 🔥 **Für neue Claude-Instanzen:**

1. **Sprint-Kontext verstehen:** → [SPRINT_MAP.md](./SPRINT_MAP.md) (Links zu zentralen Triggern)
2. **Backend-Implementation:** → [backend/_index.md](./backend/_index.md) (ADRs + Production Code)
3. **Frontend-Research:** → [frontend/_index.md](./frontend/_index.md) (Komplette Analyse + API Contract)
4. **Kanonische Contracts:** → [shared/_index.md](./shared/_index.md) (Event-System + RBAC)

### 🎯 QUICK DECISION MATRIX

```yaml
"Ich implementiere Backend-Features":
  → Start: backend/_index.md (ADR-0002 Events, Security Patterns)

"Ich plane Frontend-Implementation":
  → Start: frontend/analyse/INVENTORY.md (Stack + Gaps + Foundation Patterns)

"Ich brauche API-Contracts":
  → Start: frontend/analyse/API_CONTRACT.md (Event-System + RBAC + Endpoints)

"Ich arbeite an Cross-Module Integration":
  → Start: shared/_index.md (Event-Envelopes + Metrics + RBAC-Matrix)

"Ich will Sprint-Kontext":
  → Start: SPRINT_MAP.md (zentrale Trigger-Links)
```

## 🚀 CURRENT STATUS & DEPENDENCIES

### ✅ **Completed (Production-Ready):**
- **Sprint 2.1:** Backend Lead-Management (PR #103, #105, #110)
- **Sprint 2.1.1:** P0 Hotfix - Event Integration (PR #111)
- **Sprint 2.1.2:** Frontend Research - Vollständige Analyse (PR #112, Draft)

### 🔄 **In Progress:**
- Frontend Thin Vertical Slice Planung (nach Research-Merge)
- Feature-Flag `VITE_FEATURE_LEADGEN` Integration

### 📋 **Dependencies:**
- **Backend Events:** ✅ PostgreSQL LISTEN/NOTIFY (ADR-0002)
- **Security:** ✅ RBAC + Territory RLS (ADR-0004)
- **Performance:** ✅ Bundle <200KB Target (Foundation Patterns)
- **Frontend Stack:** ✅ React/Vite/TanStack Query (INVENTORY validiert)

## 🔗 **Zentrale Referenzen**

- **Master Plan:** [CRM_COMPLETE_MASTER_PLAN_V5.md](../../CRM_COMPLETE_MASTER_PLAN_V5.md)
- **Sprints:** [TRIGGER_INDEX.md](../../TRIGGER_INDEX.md)
- **ADRs:** [adr/ADR-0002-listen-notify-over-eventbus.md](../../adr/ADR-0002-listen-notify-over-eventbus.md)
- **Foundation:** [grundlagen/DESIGN_SYSTEM.md](../../grundlagen/DESIGN_SYSTEM.md)

## 📚 **Erweiterte Dokumentation**

- **Legacy Planning:** [./legacy-planning/](./legacy-planning/) – Historische Planungsartefakte & Detail-Spezifikationen