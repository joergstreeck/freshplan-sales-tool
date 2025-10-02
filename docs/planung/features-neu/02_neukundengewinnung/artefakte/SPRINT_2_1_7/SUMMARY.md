---
module: "02_neukundengewinnung"
domain: "shared"
doc_type: "konzept"
status: "planned"
sprint: "2.1.7"
owner: "team/leads-backend"
updated: "2025-10-02"
---

# Sprint 2.1.7 – Artefakte Summary

**📍 Navigation:** Home → Planung → 02 Neukundengewinnung → Artefakte → Sprint 2.1.7

## Übersicht

Sprint 2.1.7 implementiert **Lead-Scoring Algorithmus**, **Activity-Templates System**, **Mobile-First UI Optimierung**, **Offline-Fähigkeit** (PWA) und **QR-Code-Scanner** für schnelle Kontakterfassung.

## Sprint-Status

**Status:** 📅 PLANNED (19-25.10.2025)
**Abhängigkeiten:** Sprint 2.1.5 (Progressive Profiling UI) muss abgeschlossen sein

## Features & Artefakte

### 1. Lead-Scoring Algorithmus
**NEU erstellt 02.10.2025**

#### Backend
- **V259 Migration:** `lead.score INT` Feld
- **LeadScoringService:** Konfigurier human
bare Gewichtungen
  - Stage (0=20, 1=50, 2=80 Punkte)
  - Estimated Volume (>5000€ = +10, >10000€ = +15)
  - Business Type (Restaurant = +5, Hotel = +10, Catering = +8)
  - Activity Frequency (Call/Week = +2, Meeting/Month = +5)
  - Response Time (< 24h Antwort = +5)
  - Territory (Prioritäts-Territorien = +3)

#### Scheduled Job
- **Cron:** Täglich 1 AM (Score-Recalc für alle aktiven Leads)
- **API:** GET /api/leads?sort=score,desc (Default-Sorting nach Score)

---

### 2. Activity-Templates System
**NEU erstellt 02.10.2025**

#### Backend
- **V260 Migration:** `activity_templates` Tabelle
- **CRUD-API:** POST/GET/PUT/DELETE /api/activity-templates
- **Standard-Templates (Seeds):**
  - "Erstkontakt Küchenchef" (summary vorgefüllt, countsAsProgress=true)
  - "Sample-Box Versand" (outcome="pending", nextAction="Follow-up in 7 Tagen")
  - "ROI-Präsentation" (countsAsProgress=true, type=ROI_PRESENTATION)
  - "Follow-up nach Demo" (nextAction vorgefüllt, nextActionDate=+3 Tage)

#### Frontend
- `ActivityTemplateSelector.tsx` - "Template verwenden" Dropdown in ActivityTimeline
- User-spezifische Templates erstellen/editieren/löschen
- Template-Nutzung in Activity-Audit protokollieren

---

### 3. Mobile-First UI Optimierung

#### LeadWizard
- Touch-optimiert (Button-Größen ≥ 44px)
- Mobile-Breakpoints (<768px): Single-Column Layout
- Stepper kompakt (Icons only, Text on Tap)
- Auto-Save bei Netzwerk-Unterbrechung (localStorage)

#### LeadList
- Card-View auf Mobile (statt Table)
- Infinite Scroll (statt Pagination)
- Swipe-Aktionen (Swipe-Left: Details, Swipe-Right: Activity)

#### Performance
- Bundle <200KB (Code-Splitting)
- First Contentful Paint <1.5s (3G Network)
- Time to Interactive <3.5s (3G Network)

---

### 4. Offline-Fähigkeit (Progressive Web App)

#### Service Worker
- Offline-Support für Lead-Erfassung
- IndexedDB für lokale Lead-Speicherung
- Sync-Strategy:
  - **Online:** Direkt POST /api/leads
  - **Offline:** Speichern in IndexedDB
  - **Bei Reconnect:** Background-Sync (max. 10 Leads/Batch)

#### UI-Indikator
- `OfflineIndicator.tsx` - "Offline-Modus aktiv" Badge in Header
- Conflict-Resolution: Server-Wins bei gleichzeitigen Edits
- Installation-Prompt: "Zur Startseite hinzufügen"

---

### 5. QR-Code-Scanner Integration

#### Frontend
- `QRCodeScanner.tsx` Component (Camera-API)
- Unterstützte Formate:
  - **vCard:** Kontakt-Daten
  - **meCard:** Japanese Contact Format
  - **Plain Text:** Email/Phone
- Automatisches Parsing in LeadWizard Felder
- Fallback: Manuelle Eingabe bei Scan-Fehler
- Permission-Handling: Camera-Zugriff anfordern
- Desktop-Fallback: File-Upload (QR-Code Bild)

---

### 6. Lead-Scoring UI & Filter

#### LeadList
- **Score-Spalte mit Color-Coding:**
  - 0-30: Rot (Low Priority)
  - 31-60: Gelb (Medium Priority)
  - 61-100: Grün (High Priority)
- Default-Sorting: score DESC
- Filter: "Nur Leads > 60 Punkte"
- Filter: "Nur Leads < 30 Punkte (Cleanup-Kandidaten)"

#### LeadDetail
- `LeadScoreBreakdown.tsx` - Score-Detail-Ansicht mit Faktoren
  - Stage: +50
  - Volume: +15
  - Business Type: +10
  - Activity Frequency: +10
  - GESAMT: 85 Punkte
- "Score neu berechnen" Button (Admin/Manager)

---

## Definition of Done (Sprint)

- [ ] **Lead-Scoring-Algorithmus implementiert & getestet**
- [ ] **Activity-Templates CRUD funktioniert**
- [ ] **Mobile UI-Optimierung (Touch, Breakpoints, Performance)**
- [ ] **Offline-Fähigkeit (Service Worker + IndexedDB + Sync)**
- [ ] **QR-Code-Scanner funktioniert (Camera + vCard)**
- [ ] **Lead-Scoring UI mit Filter & Breakdown**
- [ ] **Lighthouse Score: Performance >90, PWA >90**
- [ ] **Offline-Test: 10 Leads erfassen → Sync erfolgreich**

---

## Risiken & Mitigationen

| Risiko | Mitigation | Status |
|--------|------------|--------|
| Scoring-Genauigkeit | Iteratives Tuning mit Sales-Team Feedback | PLANNED |
| Offline-Konflikte | Server-Wins Strategie + User-Benachrichtigung | PLANNED |
| Camera-Permissions | Klarer Onboarding-Flow + Fallback File-Upload | PLANNED |
| Performance Mobile | Code-Splitting + Lazy-Loading Components | PLANNED |
| IndexedDB Storage-Limits | Max. 50 Offline-Leads, ältere löschen | CONFIGURED |

---

## Monitoring & KPIs

- **Lead-Score Distribution:** Histogram (Low/Medium/High)
- **Template-Nutzung:** Top 5 meistgenutzte Templates
- **Offline-Sync Success-Rate:** >95% erfolgreiche Syncs
- **Mobile Performance:** P95 Load-Time <3.5s (3G)
- **QR-Code Scan Success-Rate:** >90%

---

## Links

- [TRIGGER_SPRINT_2_1_7.md](../../../TRIGGER_SPRINT_2_1_7.md)
- [SPRINT_MAP.md](../../SPRINT_MAP.md)

---

**Dokument-Owner:** Jörg Streeck + Claude Code
**Letzte Änderung:** 2025-10-02
**Version:** 1.0 (Planned for Sprint 2.1.7)
