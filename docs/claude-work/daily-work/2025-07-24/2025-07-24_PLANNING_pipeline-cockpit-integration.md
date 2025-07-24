# 📋 FC-011: Pipeline-Cockpit Integration - Planungs-Session

**Datum:** 24.07.2025  
**Feature:** FC-011 Pipeline-Cockpit Integration  
**Status:** Technisches Konzept erstellt mit hybrider Dokumentenstruktur

## 🎯 Zusammenfassung

Wir haben heute das technische Konzept für FC-011 (Pipeline-Cockpit Integration) erstellt. Dieses Feature transformiert das FreshPlan CRM in ein echtes "Sales Command Center" durch die nahtlose Integration von Pipeline und Cockpit.

## 💡 Kern-Innovation: Click-to-Load

**Die Killer-Feature-Idee:** Ein Klick auf eine Opportunity-Karte in der Pipeline lädt automatisch alle relevanten Kundendaten in den Cockpit-Arbeitsbereich.

### Warum das revolutionär ist:
1. **Natürlicher Workflow** - Keine Kontextwechsel mehr
2. **Cockpit als Command Center** - Alles an einem Ort
3. **Massive Effizienzsteigerung** - Ein Klick statt vieler Navigationsschritte

## 🏗️ Technische Architektur

### Frontend-Komponenten:
- **Enhanced OpportunityCard** mit Multi-Gesten Support
- **CockpitContext Provider** für State Management
- **Split-View Container** mit flexiblen Layouts
- **Intelligent Preloading** mit Hover-Detection

### Performance-Optimierungen:
- **Multi-Layer Cache** (React Query + LRU)
- **Priority-based Preloading**
- **Virtual DOM Optimization**
- **Optimistic Updates**

### API-Erweiterungen:
- `/api/customers/{id}/with-context` - Lädt Kunde mit Opportunity-Kontext
- `/api/customers/batch-summary` - Batch-Preloading
- `/api/opportunities/{id}/quick-action` - Quick Actions API
- WebSocket Events für Real-time Updates

## 📊 Auswirkungen auf andere Features

### M4 Opportunity Pipeline:
- OpportunityCard erweitert mit Click-Handler
- Visual Feedback für aktive Karten
- Integration mit Drag & Drop erhalten

### M5 Customer Management:
- Customer-Context API erweitert
- Batch-Loading implementiert
- Cache-Sharing zwischen Features

### FC-003 Email Integration:
- Context-basierte Email-Templates
- Quick-Email aus Kontextmenü
- Activity-Tracking Integration

### FC-009 Contract Renewal:
- Contract-Daten in Customer-Context
- Renewal Quick-Action integriert
- Warning-Badges in Pipeline

### FC-010 Pipeline Scalability:
- Shared Filter-State
- Gemeinsame UI-Komponenten
- Performance-Optimierungen teilen

## 🔑 Key Design Decisions

### 1. Multi-Gesten Interaktion:
- **Single Click:** Lädt in Cockpit
- **Double Click:** Öffnet Detail-Modal
- **Right Click:** Kontextmenü
- **Hover:** Preload + Preview

### 2. Split-View Layouts:
- Standard: 30/70 (Pipeline/Cockpit)
- Flexible Resize mit Min-Widths
- Mobile: Stack-View mit Swipes

### 3. Performance-Ziele:
- Click-to-Load: < 500ms
- Preload Hit Rate: > 60%
- Memory Footprint: < 50MB zusätzlich

## 📝 Erstellte Dokumente

1. **Hauptdokument:** `/docs/features/2025-07-24_TECH_CONCEPT_FC-011-pipeline-cockpit-integration.md`
2. **Frontend-Architektur:** `/docs/features/FC-011/frontend-architecture.md`
3. **Interaction Patterns:** `/docs/features/FC-011/interaction-patterns.md`
4. **Performance Strategy:** `/docs/features/FC-011/performance-optimization.md`
5. **API Requirements:** `/docs/features/FC-011/api-requirements.md`

## 🚀 Implementierungs-Empfehlung

**PRIORITÄT: HOCH** - Direkt nach M4 Backend-Integration!

### Phasen:
1. **Phase 1:** Basis Click-to-Load (2 Tage)
2. **Phase 2:** Multi-Gesten & Actions (2 Tage)
3. **Phase 3:** Split-View & Layouts (2 Tage)
4. **Phase 4:** Performance & Polish (1 Tag)

## 💭 Philosophische Übereinstimmung

FC-011 verkörpert perfekt unsere drei Kernprinzipien:

1. **Geführte Freiheit:** Standard-Workflow (Click-to-Load) mit Alternativen
2. **Alles verbunden:** Pipeline → Cockpit → Actions nahtlos
3. **Skalierbare Exzellenz:** Performance von Anfang an mitgedacht

## 🎨 UX-Highlights

- **"Erneut ins Rennen schicken"** statt "Reaktivieren"
- **WIP-Warnings:** "Bühne räumen" Metapher
- **Activity Indicators:** Pulsierende Punkte für Dringlichkeit
- **Keyboard Shortcuts:** Space = Load, G = Gewonnen, L = Verloren

Die Kombination aus intuitiver Bedienung, durchdachten Workflows und technischer Exzellenz macht FC-011 zu einem echten Differenzierungsmerkmal für FreshPlan!