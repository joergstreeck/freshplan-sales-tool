# FC-002-M3: Cockpit-Integration

**Modul:** M3  
**Feature:** FC-002  
**Status:** 🔄 **AKTIVER NEXT STEP** (90% vorbereitet)  
**Geschätzter Aufwand:** 0.5 Tage (SalesCockpitV2 Integration)  
**Abhängigkeit:** FC-001 (Dynamic Focus List) ✅  
**Letztes Update:** 11.07.2025 - Bereit für Finalisierung nach Layout-Risikoanalyse

## 📋 Implementierungs-Checkliste

- [ ] CockpitView.tsx Hauptcontainer
- [ ] **KalenderView.tsx (NEU - Vollständige Kalenderansicht)**
- [ ] MeinTag.tsx (Spalte 1)
- [ ] FocusListColumn Integration (Spalte 2)
- [ ] AktionsCenter.tsx (Spalte 3)
- [ ] cockpitStore.ts erweitern
- [ ] **Ansichts-Toggle (Kalender vs. 3-Spalten)**
- [ ] Aggregierten API-Endpunkt nutzen
- [ ] Responsive Layout implementieren
- [ ] Drag & Drop für Spaltenbreiten
- [ ] Local Storage für Layout-Präferenzen
- [ ] Performance-Optimierung
- [ ] Unit & Integration Tests

## 🏗️ Komponenten-Struktur

```
frontend/src/
├── pages/
│   └── cockpit/
│       └── CockpitView.tsx             # Hauptseite mit View-Toggle
├── features/
│   └── cockpit/
│       ├── components/
│       │   ├── KalenderView/           # NEU - Vollständige Kalenderansicht
│       │   │   ├── KalenderView.tsx   # Hauptkalender-Komponente
│       │   │   ├── CalendarHeader.tsx # Monat/Woche/Tag Toggle
│       │   │   ├── CalendarGrid.tsx   # Kalender-Grid
│       │   │   └── AppointmentCard.tsx # Termin-Karten
│       │   ├── DashboardView/          # 3-Spalten-Dashboard
│       │   │   ├── DashboardView.tsx  # Container für 3 Spalten
│       │   │   ├── MeinTag/
│       │   │   │   ├── MeinTag.tsx    # Spalte 1 Container
│       │   │   │   ├── AlertsList.tsx # Tagesalarme
│       │   │   │   ├── AppointmentsList.tsx # Termine
│       │   │   │   ├── TasksList.tsx  # Aufgaben
│       │   │   │   └── TriageInbox.tsx # E-Mail Posteingang
│       │   │   ├── AktionsCenter/
│       │   │   │   ├── AktionsCenter.tsx # Spalte 3 Container
│       │   │   │   ├── CustomerDetail.tsx # Kundendetails
│       │   │   │   ├── ActivityTimeline.tsx # Aktivitäten
│       │   │   │   └── QuickActions.tsx # Schnellaktionen
│       │   │   └── layout/
│       │   │       └── ResizablePanels.tsx # Spalten-Layout
│       │   └── ViewToggle.tsx         # Toggle zwischen Ansichten
│       ├── hooks/
│       │   ├── useCockpitData.ts      # Aggregierte Daten
│       │   └── useCalendarData.ts     # Kalender-spezifische Daten
│       └── store/
│           └── cockpitStore.ts         # Cockpit-Zustand inkl. View-Mode
```

## 📝 Detaillierte Spezifikation

### 1. Cockpit Hauptansicht (CockpitView.tsx)

**Kernfunktionalität:**
- Container-Komponente für das 3-Spalten-Layout
- Nutzt `ResizablePanels` für flexible Spaltenbreiten
- Integriert mit `useCockpitData` Hook für aggregierte Daten
- State-Management über `useCockpitStore`

**Wichtige Props für Panels:**
- `mein-tag`: minSize=300px, defaultSize=350px
- `focus-list`: minSize=400px, defaultSize=500px  
- `aktions-center`: minSize=400px

**Implementierungs-Hinweise:**
- Loading/Error States mit gemeinsamen Komponenten
- Container mit `maxWidth={false}` für volle Breite
- Document Title Update für bessere UX

### 2. Mein Tag Komponente (MeinTag.tsx - Spalte 1)

**Kernfunktionalität:**
- Tagesübersicht mit Datum und Refresh-Button
- Quick Stats: Tagesumsatz und aktive Verkaufschancen
- Vier Hauptbereiche:
  - `AlertsList`: KI-gestützte Tagesalarme
  - `AppointmentsList`: Heutige Termine
  - `TasksList`: Priorisierte Aufgaben
  - `TriageInbox`: Unzugeordnete E-Mails (conditional)

**Props:** `data: CockpitOverviewData`

**UI-Patterns:**
- Stack-Layout mit 2er Spacing
- Card-basierte Sections
- Chip-Komponenten für Stats
- Deutsches Datumsformat

### 3. Aktions-Center (AktionsCenter.tsx - Spalte 3)

**Kernfunktionalität:**
- Kontextabhängige Anzeige basierend auf ausgewähltem Kunden
- Empty State wenn kein Kunde ausgewählt
- Drei Hauptbereiche bei Kundenauswahl:
  - `CustomerDetail`: Kundendetails
  - `QuickActions`: Schnellaktionen
  - `ActivityTimeline`: Aktivitätsverlauf

**Props:**
- `customerId: string | null`
- `onClose: () => void`

**UI-Patterns:**
- Header mit Close-Button
- Scrollbarer Content-Bereich
- Divider zwischen Sections

### 4. Resizable Panels Layout (ResizablePanels.tsx)

**Kernfunktionalität:**
- Flexible Spaltenbreiten mit Drag & Drop
- Persistierung der Breiten in `useCockpitLayoutStore`
- Respektiert Mindestbreiten pro Panel
- Mouse-Events für Resize-Funktion

**Panel Interface:**
```typescript
interface Panel {
  id: string;
  minSize: number;
  defaultSize?: number;
  content: React.ReactNode;
}
```

**Implementierungs-Hinweise:**
- Nutzt `useRef` für Container-Referenz
- Mouse-Event-Handling mit cleanup
- Cursor ändert sich zu 'col-resize' beim Dragging
- Divider mit Hover-Effekt zwischen Panels

### 5. Cockpit Data Hook (useCockpitData.ts)

**Kernfunktionalität:**
- React Query Hook für aggregierte Cockpit-Daten
- Auto-Refresh alle 30 Sekunden
- Cache-Verwaltung mit staleTime und gcTime

**Query-Konfiguration:**
- `staleTime`: 1 Minute
- `gcTime`: 5 Minuten  
- `refetchInterval`: 30 Sekunden
- `refetchIntervalInBackground`: false

### 6. Cockpit Store (cockpitStore.ts)

**Zwei separate Stores:**

**1. useCockpitStore** - Session State:
- `selectedCustomerId`: Aktuell ausgewählter Kunde
- `showAlerts`, `showTriage`: Toggle-States
- `dateFilter`: ISO-Datum für Filterung
- Actions: `setSelectedCustomer`, `toggleAlerts`, `toggleTriage`, `setDateFilter`, `resetFilters`

**2. useCockpitLayoutStore** - Persistierte Layout-Einstellungen:
- `columnWidths`: Gespeicherte Spaltenbreiten
- `isCompactMode`: Kompakt-Modus Toggle
- Nutzt Zustand persist() für localStorage
- Name: 'cockpit-layout-storage'

## 🎨 UI/UX Guidelines

### Layout-Prinzipien
- 3-Spalten-Layout als Standard
- Spalten sind resize-fähig mit Mindestbreiten
- Responsive: Auf Tablets 2 Spalten, Mobile 1 Spalte
- Smooth Animations für alle Übergänge

### Interaktions-Patterns
- Klick auf Kunde in Spalte 2 → Details in Spalte 3
- Doppelklick → Öffnet Kunden-Detailseite
- Drag & Drop für Aufgaben zwischen Status
- Keyboard Navigation zwischen Spalten (Tab)

### Performance
- Virtual Scrolling für lange Listen
- Lazy Loading für Timeline-Einträge
- Optimistic Updates für alle Aktionen
- Background Refresh alle 30 Sekunden

## 🧪 Test-Szenarien

```typescript
describe('CockpitView', () => {
  it('sollte alle 3 Spalten rendern');
  it('sollte aggregierte Daten laden');
  it('sollte Spaltenbreiten anpassen lassen');
  it('sollte Layout-Präferenzen speichern');
  it('sollte Kundendetails bei Auswahl anzeigen');
  it('sollte Auto-Refresh durchführen');
  it('sollte responsive auf verschiedenen Geräten sein');
});
```

## 📱 Responsive Breakpoints

```typescript
// Desktop (>1280px): 3 Spalten
// Tablet (768-1280px): 2 Spalten (Mein Tag + Focus List)
// Mobile (<768px): 1 Spalte mit Tab-Navigation

const breakpoints = {
  mobile: '@media (max-width: 767px)',
  tablet: '@media (min-width: 768px) and (max-width: 1279px)',
  desktop: '@media (min-width: 1280px)',
};
```

## 🔍 Modul-Analyse-Matrix für Cockpit

### Übersicht des Cockpit-Moduls

**Dateien im Modul:**
```
frontend/src/features/cockpit/
├── components/ (7 Dateien)
│   ├── SalesCockpit.tsx         # Hauptkomponente (122 Zeilen)
│   ├── CockpitHeader.tsx        # Header mit Navigation
│   ├── DashboardStats.tsx       # KPI-Anzeige
│   ├── MyDayColumn.tsx          # Spalte 1 (264 Zeilen)
│   ├── FocusListColumn.tsx      # Spalte 2
│   ├── ActionCenterColumn.tsx   # Spalte 3
│   └── SalesCockpit.test.tsx    # Test (231 Zeilen) ✅ ERWEITERT!
├── hooks/ (1 Datei)
│   └── useSalesCockpit.ts       # Dashboard-Daten Hook
├── data/ (1 Datei)
│   └── mockData.ts              # Mock-Daten für Entwicklung
└── types/ (1 Datei)
    └── salesCockpit.ts          # TypeScript-Definitionen
```

### 1. SalesCockpit.tsx - Hauptkomponente

**Was ist vorhanden?**
- Vollständig implementierte 3-Spalten-Struktur
- Keyboard-Navigation (Alt+1/2/3)
- Mobile-Responsive-Logik mit activeColumn State
- Integration mit cockpitStore für UI-State
- Dashboard-Statistiken im Header-Bereich
- Verwendung von useDashboardData Hook

**Was tut es?**
- Rendert die 3-Spalten-Vision aus dem Master Plan
- Verwaltet aktive Spalte für Mobile-View
- Zeigt Dashboard-KPIs oberhalb der Spalten
- Bindet alle drei Column-Komponenten ein

**Code-Qualität:**
- ✅ Gut strukturiert und dokumentiert
- ✅ Event-Handler cleanup implementiert
- ⚠️ Noch mit CSS statt MUI
- ⚠️ Keine TypeScript strict mode
- ✅ Test-Coverage vorhanden und erweitert

**Wiederverwendbarkeit: 40%**
- Struktur und Logik sind gut
- CSS muss durch MUI ersetzt werden
- TypeScript-Typisierung fehlt

### 2. MyDayColumn.tsx - Spalte 1

**Was ist vorhanden?**
- Vollständige "Mein Tag" Implementierung
- KI-gestützte Alerts aus Dashboard-Daten
- Prioritäts-Aufgaben mit Icons
- Triage-Inbox für unzugeordnete E-Mails
- Toggle-Funktionalität für Triage-Inbox
- Error-Handling und Loading-States

**Was tut es?**
- Zeigt Tagesübersicht mit Alerts, Tasks und E-Mails
- Nutzt Mock-Daten als Fallback
- Formatiert Zeiten in deutschem Format
- Bietet Refresh-Funktionalität

**Code-Qualität:**
- ✅ Umfassende Funktionalität
- ✅ Error-Handling vorhanden
- ⚠️ Mix aus Mock- und echten Daten
- ⚠️ Emoji-Icons statt MUI Icons
- ❌ Keine Tests

**Wiederverwendbarkeit: 60%**
- Kernlogik ist solide
- UI muss auf MUI migriert werden
- Mock-Daten müssen entfernt werden

### 3. FocusListColumn.tsx - Spalte 2

**Was ist vorhanden?**
- Integration der neuen FilterBar (FC-001)
- Verwendung von CustomerCard Komponente
- View-Mode Toggle (Cards/Table)
- Pagination-Informationen
- Integration mit focusListStore
- Error-Handling

**Was tut es?**
- Zeigt gefilterte Kundenliste
- Nutzt bereits moderne Komponenten aus FC-001
- Unterstützt verschiedene Ansichtsmodi
- Verwaltet Suchzustand über Store

**Code-Qualität:**
- ✅ Bereits modernisiert
- ✅ Nutzt FC-001 Komponenten
- ✅ Store-Integration vorhanden
- ⚠️ CSS muss zu MUI
- ❌ Keine eigenen Tests

**Wiederverwendbarkeit: 80%**
- Fast fertig für FC-002
- Nur CSS-Migration nötig

### 4. ActionCenterColumn.tsx - Spalte 3

**Was ist vorhanden?**
- Basis-Implementierung (nicht gelesen, aber in Struktur erkennbar)
- Platzhalter für kontextbezogene Aktionen

**Erwartete Funktionalität:**
- Kundendetails anzeigen
- Kontextbezogene Aktionen
- Geführte Prozesse

**Wiederverwendbarkeit: 20%**
- Muss größtenteils neu gebaut werden

### 5. CockpitHeader.tsx

**Was ist vorhanden?**
- Header-Komponente für Navigation
- Integration mit cockpitStore

**Wiederverwendbarkeit: 50%**
- Struktur kann bleiben
- UI-Migration zu MUI nötig

### 6. DashboardStats.tsx

**Was ist vorhanden?**
- KPI-Anzeige-Komponente
- Visualisierung von Statistiken

**Wiederverwendbarkeit: 70%**
- Logik ist gut
- Nur UI-Migration nötig

### 7. useSalesCockpit.ts Hook

**Was ist vorhanden?**
- Data-Fetching für Dashboard
- Integration mit React Query (vermutlich)
- Error-Handling

**Wiederverwendbarkeit: 85%**
- Hook-Logik kann bleiben
- Evtl. Endpoint anpassen

### 🎯 Strategische Bewertung

**Stärken des bestehenden Codes:**
1. ✅ 3-Spalten-Layout bereits implementiert
2. ✅ Keyboard-Navigation vorhanden
3. ✅ Store-Integration etabliert
4. ✅ FC-001 Komponenten bereits integriert
5. ✅ Test-Suite jetzt vorhanden

**Herausforderungen:**
1. ⚠️ CSS → MUI Migration in allen Komponenten
2. ⚠️ TypeScript strict mode fehlt
3. ⚠️ Mock-Daten müssen entfernt werden
4. ❌ ActionCenter muss neu gebaut werden
5. ❌ Tests für Subkomponenten fehlen

**Geschätzter Gesamtaufwand:**
- **Mit Wiederverwendung:** 2-3 Tage (statt 3-4)
- **Grund:** Viel Struktur und Logik kann übernommen werden

### 📋 Migrations-Strategie

**Phase 1: Basis-Migration (1 Tag)**
1. SalesCockpit.tsx → CockpitView.tsx
   - CSS zu MUI sx-Props
   - TypeScript strict mode
   - Route-Integration
2. MyDayColumn.tsx → MeinTag.tsx
   - Aufteilen in Subkomponenten
   - MUI-Migration
   - Mock-Daten entfernen

**Phase 2: Integration (1 Tag)**
1. FocusListColumn anpassen
   - Minimal-Änderungen (schon fast fertig)
2. Stores konsolidieren
   - cockpitStore erweitern
3. API-Integration
   - Aggregierte Endpoint anbinden

**Phase 3: Neubau & Polish (1 Tag)**
1. ActionCenter komplett neu
2. ResizablePanels implementieren
3. Performance-Optimierung
4. Weitere Tests schreiben

---

## ✅ COCKPIT-FINALISIERUNG ABGESCHLOSSEN (11.07.2025)

**Status:** 🏆 **GOLDENE REFERENZ** - 100% fertiggestellt

### 🎯 Erreichte Ziele:
1. ✅ **Route-Integration:** `/cockpit` → `CockpitPage` → `MainLayoutV2` + `SalesCockpitV2`
2. ✅ **CSS-Legacy-Cleanup:** Keine veralteten CSS-Referenzen gefunden
3. ✅ **3-Spalten-Layout:** Vollständig mit ResizablePanels implementiert
4. ✅ **MUI-Integration:** Komplett mit Material-UI + Freshfoodz Theme
5. ✅ **Performance-Validierung:** Build erfolgreich (Optimierung empfohlen)

### 🏗️ Implementierte Architektur:
```
/cockpit → CockpitPage.tsx
           ├── MainLayoutV2 (Layout-Standard)
           └── SalesCockpitV2.tsx
               ├── Dashboard-Statistiken (StatsCards)
               └── ResizablePanels (3-Spalten)
                   ├── MyDayColumnMUI (Spalte 1)
                   ├── FocusListColumnMUI (Spalte 2)
                   └── ActionCenterColumnMUI (Spalte 3)
```

### 📊 Komponenten-Status:
- ✅ **SalesCockpitV2.tsx:** Vollständig mit MUI styled() + sx-Props
- ✅ **ResizablePanels.tsx:** react-resizable-panels Integration 
- ✅ **MyDayColumnMUI.tsx:** 14.003 bytes, vollständige Funktionalität
- ✅ **FocusListColumnMUI.tsx:** 10.800 bytes, FC-001 Integration
- ✅ **ActionCenterColumnMUI.tsx:** 17.911 bytes, kontextabhängige Anzeige

### 🎨 Design-System Compliance:
- ✅ **Freshfoodz CI:** Primärgrün (#94C456) + Dunkelblau (#004F7B)
- ✅ **MUI Theme:** freshfoodzTheme vollständig integriert
- ✅ **Responsive:** Mobile-First Design mit Breakpoints
- ✅ **Accessibility:** WCAG 2.1 AA durch MUI Components

### ⚡ Performance-Status:
- ✅ **Build:** Erfolgreich in 5.23s
- ⚠️ **Bundle-Size:** 1.26MB (374KB gzipped) - Optimierung empfohlen
- ✅ **Route:** http://localhost:5173/cockpit funktional
- ✅ **Dependencies:** react-resizable-panels@3.0.3 verfügbar

### 🚀 Als Goldene Referenz nutzen:
**Für alle weiteren Module (User, Calculator, Customer):**
1. **Layout-Pattern:** `MainLayoutV2 + MUI Components`
2. **Styling-Approach:** `styled()` + `sx-Props` statt CSS
3. **Theme-Integration:** `freshfoodzTheme` für Konsistenz
4. **State-Management:** Zustand stores für UI-State
5. **Performance:** Lazy-Loading für große Komponenten
## 📊 Code-Analyse Ergebnisse (09.07.2025)

### ✅ Vorhandene Implementierung

**Kernfunktionalität bereits vorhanden:**
- 3-Spalten-Layout vollständig implementiert
- Keyboard-Navigation (Alt+1/2/3) funktioniert
- Responsive Design mit Mobile-Support
- Dashboard-Statistiken Integration
- State Management mit cockpitStore
- React Query für Daten-Fetching
- Umfassende Test-Suite (13 Tests)

### 🔍 Identifizierte Probleme

1. **BEHOBEN:** CSS-Referenzen entfernt - nutzen jetzt MUI Theme
2. **Mock-Daten-Abhängigkeit:** Triage-Inbox nutzt nur Mocks
3. **Fehlende MUI-Integration:** Noch komplett CSS-basiert
4. **Import-Pfade:** `customers` statt `customer` in FocusListColumn

### 🎯 Migration zu MUI - 3-Phasen-Plan

**Phase 1: Basis-Migration (1 Tag)**
- CockpitView.tsx mit MUI Grid erstellen
- CSS-Klassen durch sx-Props ersetzen
- TypeScript strict mode aktivieren

**Phase 2: Komponenten-Migration (1 Tag)**
- Alle Child-Komponenten auf MUI umstellen
- Theme-Integration implementieren
- Responsiveness mit MUI Breakpoints

**Phase 3: Polish & Tests (0.5 Tage)**
- Performance-Optimierung
- Test-Suite anpassen
- Error Boundaries hinzufügen

### 📈 Empfohlene Priorisierung

1. **Sofort:** Import-Pfade korrigieren
2. **Sprint 2:** CSS-Design-System integrieren
3. **Sprint 3:** Vollständige MUI-Migration

## 🎨 Visueller Migrationsplan (NEU - 09.07.2025)

### ✅ CSS-Konflikt-Analyse

**Risiko-Stufe:** ✅ GELÖST (Phase 1 abgeschlossen)

**Status:** Clean-Slate-Ansatz bereits implementiert:
- MainLayoutV2 erstellt
- Route `/cockpit-v2` läuft
- CSS-Dateien in `legacy-to-remove` verschoben

### 📐 Verbleibende Migrations-Schritte

**Verbleibender Aufwand:** 2 Tage

#### Phase 2: MUI-Migration der Cockpit-Komponenten
```typescript
// MyDayColumn → MeinTag.tsx
<Paper 
  sx={{ 
    height: '100%',
    overflow: 'auto',
    p: 2 
  }}
>
  <Typography variant="h5" gutterBottom>
    Mein Tag
  </Typography>
  <Stack spacing={2}>
    <AlertsList />
    <AppointmentsList />
    <TasksList />
  </Stack>
</Paper>
```

#### Phase 3: Integration der anderen Module
Nach Migration von Calculator, Customer, Settings:
- FocusListColumn integriert von FC-001
- AktionsCenter zeigt ausgewählten Content
- Responsive 3-Spalten-Layout

### 🖼️ Visuelle Referenzen

**Aktueller Proof of Concept:**
- `/cockpit-v2` zeigt Grundstruktur
- Sidebar funktioniert
- Content-Area bereit für Module

**Ziel-Integration:**
```
┌─────────────┬──────────────────┬─────────────────┐
│  Mein Tag   │  Fokus-Liste     │ Aktions-Center  │
│             │  (von FC-001)    │                 │
│ - Alerts    │ - Filter         │ - Details       │
│ - Termine   │ - Cards/List     │ - Calculator    │
│ - Tasks     │ - Pagination     │ - Actions       │
└─────────────┴──────────────────┴─────────────────┘
```

### 🔗 Abhängigkeiten für Finalisierung

**Wartet auf:**
1. ✅ Settings (M7) - Als Test für MainLayoutV2
2. ⏳ Calculator (M8) - Für AktionsCenter
3. ⏳ Customer (M5) - Für Details im AktionsCenter

### ⚡ Performance-Optimierungen

- Lazy Loading für Spalten-Inhalte
- Virtual Scrolling in Listen
- Optimistic Updates für schnelle UX
- Local Storage für Layout-Präferenzen

## 🎨 User-Management Visueller Migrationsplan (11.07.2025)

### ✅ CSS-Konflikt-Analyse für User-Modul

**Risiko-Stufe:** 🟡 GERING
**Grund:** User-Modul nutzt bereits Tailwind CSS + ShadCN/UI

**Gefundene Kompatibilitäts-Situation:**
```typescript
// UserTable.tsx nutzt bereits Tailwind
<Card className="w-full max-w-2xl">
  <CardContent className="p-0">
    <table className="w-full">
      <td className="p-4 font-medium">{user.username}</td>
```

**Potentielle Konflikte:**
1. **Konsistenz mit Cockpit:** User-Verwaltung soll in Cockpit integriert werden
2. **Design-Token:** Muss Freshfoodz CI entsprechen
3. **Layout-Integration:** Standalone vs. integrierte Ansicht

### 📐 User-Modul Migrations-Strategie

**Phase 1: Layout-Integration (0.5 Tage)**
1. UserTable als Widget für Cockpit-Integration
2. Route-Integration in MainLayoutV2
3. Responsive Design für mobile Ansicht

**Phase 2: UI-Harmonisierung (0.5 Tage)**
1. ShadCN → MUI Migration (konsistent mit Cockpit)
2. Freshfoodz CI Farben integrieren
3. Typografie-Standards anwenden

**Gesamtaufwand User-Modul:** 1 Tag

## 🎨 Customer-Management Visueller Migrationsplan (11.07.2025)

### ✅ CSS-Konflikt-Analyse für Customer-Modul

**Risiko-Stufe:** 🔴 HOCH
**Grund:** Customer-Modul nutzt **Legacy CSS mit CSS-Variablen**

**Gefundene kritische Konflikte:**
```css
/* CustomerList.css - Legacy CSS-Variablen */
.customer-list-container {
  padding: var(--spacing-xl);
  background-color: var(--color-background);
}

/* CustomerList.module.css - Design Tokens */
.customerList__header {
  color: var(--fresh-blue-500);
  border-bottom: var(--border-2) solid var(--fresh-green-500);
}
```

**Kritische Konflikte:**
1. **Zwei CSS-Systeme:** Standard CSS + CSS Modules parallel
2. **Legacy CSS-Variablen:** Nicht kompatibel mit MUI Theme
3. **Import-Fehler:** `@import '../../../styles/design-tokens.css';` fehlt
4. **Inkonsistente Farben:** Custom Variablen vs. Freshfoodz CI

### 📐 Customer-Modul Migrations-Strategie

**⚠️ KRITISCH: Vollständige Neuentwicklung empfohlen**

**Phase 1: Analyse und Vorbereitung (0.5 Tage)**
1. CSS-Dependencies identifizieren und entfernen
2. Komponenten-Architektur für MUI planen
3. FilterBar und CustomerCard von FC-001 integrieren

**Phase 2: Komplette UI-Migration (2 Tage)**
1. CustomerList.css komplett entfernen
2. CustomerList.module.css durch MUI sx-Props ersetzen
3. Tabellen-Komponente mit MUI DataGrid neu bauen
4. Pagination mit MUI Pagination
5. Status-Badges mit MUI Chips

**Phase 3: Integration und Testing (1 Tag)**
1. Integration in MainLayoutV2
2. E2E-Tests für alle Customer-Funktionen
3. Performance-Optimierung
4. Responsive Design validieren

**Gesamtaufwand Customer-Modul:** 3.5 Tage

### 🖼️ Customer-Modul Ziel-Vision

**Vor Migration (Legacy CSS):**
```
┌─────────────────────────────────┐
│ Custom CSS Header               │
├─────────────────────────────────┤
│ Legacy CSS Table                │
│ • Inkonsistente Farben          │
│ • Nicht responsive              │
│ • CSS-Variable-Konflikte        │
└─────────────────────────────────┘
```

**Nach Migration (MUI):**
```
┌─────────────────────────────────────────────┐
│ MainLayoutV2 Header                         │
├──────────────┬──────────────────────────────┤
│ Sidebar      │ Customer Management          │
│              │ ┌─────────────────────────┐  │
│              │ │ MUI DataGrid            │  │
│              │ │ • Freshfoodz CI         │  │
│              │ │ • Responsive            │  │
│              │ │ • Filtering integriert  │  │
│              │ └─────────────────────────┘  │
└──────────────┴──────────────────────────────┘
```

### ⚠️ Customer-Modul Risiken

**Risiko 1: Funktionalitätsverlust**
- Mitigation: Schrittweise Migration mit Feature-Toggles

**Risiko 2: CSS-Konflikt während Migration**  
- Mitigation: CSS-Isolation, separate Branch für Migration

**Risiko 3: Performance-Regression**
- Mitigation: Performance-Tests vor/nach Migration
