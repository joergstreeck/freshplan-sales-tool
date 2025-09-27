---
module: "02_neukundengewinnung"
sprint_id: "2.1.3"
title: "Thin Vertical Slice – Frontend Minimum (Lead-Management)"
doc_type: "trigger"
status: "planned"
owner: "team/frontend"
updated: "2025-09-27"
entry_points:
  # Nur existierende Pfade (Dir → _index.md Fallback) – Compliance-sicher:
  - "features-neu/02_neukundengewinnung/_index.md"
  - "features-neu/02_neukundengewinnung/analyse/_index.md"
  - "features-neu/02_neukundengewinnung/SPRINT_MAP.md"
---

# 🎯 Ziel
Minimal lauffähige **Lead-Management UI** für Modul 02:
- **/leads**: Read-Only Liste (Kernfelder) inkl. Lade-/Fehlerzustand
- **Create Lead**: einfacher Dialog (Minimalfelder) → POST `/api/leads`
- **Theme V2** eingebunden, **RFC7807**-Fehler sauber dargestellt
- **Feature-Flag:** `VITE_FEATURE_LEADGEN` (default: off)

# 🔬 Scope
**In:** Route, Liste, Create, Theme, Basis-Fehlerbehandlung, simple E2E/Smoke
**Out:** Statuswechsel-UI, komplexe Filter/Suche, Realtime/WebSocket, Export

# 📦 Deliverables
- Frontend-Route `/leads` (Navigation erreichbar)
- Komponenten: `LeadList`, `LeadCreateDialog`
- API-Client minimal (GET/POST Leads) + Fehleradapter (RFC7807 → UI)
- Flag‑gesteuertes Rendering (`VITE_FEATURE_LEADGEN`)
- Tests:
  - **Vitest** Unit (Komponenten, Fehlerpfade)
  - **Playwright** Smoke (Seite lädt, Create funktioniert)
- Doku-Updates: SPRINT_MAP (Status), Analyse-Hinweise (kurzer „Was wurde umgesetzt"‑Block)

# ✅ Akzeptanzkriterien
- Liste rendert **erste Seite** Daten, Ladespinner & leere Zustände korrekt
- Create‑Dialog erzeugt neuen Lead und zeigt Bestätigung/Toast
- Fehlerfälle (4xx/5xx, Validation) werden **sichtbar & verständlich** abgebildet
- Tests grün, Coverage ≥ **80%** für neue Komponenten/Client
- CI **grün** (Docs‑Compliance, Lint, Tests)

# ⚙️ Technische Leitplanken
- **Env:** `VITE_API_BASE_URL`, `VITE_FEATURE_LEADGEN`
- **ABAC/JWT** wird aus bestehender App‑Auth genutzt; kein neuer Login‑Flow
- **UI‑Framework:** MUI (Theme V2 Tokens)
- **Error-Format:** RFC7807 → einheitlicher Mapping‑Helper

# 🧪 Testplan
- Unit: Render, Ladezustand, Fehlerdarstellung, Create‑Validierung
- E2E: Route erreichbar, Liste sichtbar, Create happy path
- Performance Smoke: Page‑Load < **2s** im CI‑Profil

# ⚠️ Risiken & Mitigation
- Backend‑Contracts ändern sich → **API‑Typen kapseln**, kleine Adapter
- Realtime folgt später → Architektur offen halten (Hook/Provider‑Schnittstellen)
- Flag vergessen → Default off + Deploy‑Checklist

# ⏪ Rollback
- Flag off → UI unsichtbar, kein Codepfad im Produktivbetrieb

# 🗺️ Nächste Ausbauten (Backlog)
- Filter/Suche, Pagination‑Controls
- Lead‑Statuswechsel (State Machine UI)
- Realtime Updates (WebSocket), leise Refresh‑Strategie
- Export/CSV, Column‑Customizer