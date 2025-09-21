# 🤔 Cockpit Planning-Diskussion - Strategische Entscheidungen

**📊 Plan Status:** 🔵 Draft - Multi-AI Diskussion
**🎯 Owner:** Jörg + Claude Team
**⏱️ Timeline:** Planungsphase Q4 2025
**🔧 Effort:** XS (Strategische Entscheidungen)

## 🎯 Kontext für AI-Assistenten

**Was ist das Sales Cockpit:**
Das Cockpit ist die zentrale Schaltzentrale für Verkäufer im CRM-System. Es zeigt in 3 Spalten: (1) Tagesaufgaben & Alarme, (2) priorisierte Kundenliste mit Filtern, (3) Details & Aktionen zum ausgewählten Kunden. Das Ziel: Alle wichtigen Informationen und häufigen Aktionen auf einer Seite, ohne zwischen verschiedenen Bereichen navigieren zu müssen.

**Technischer Kontext:**
- Frontend: React + TypeScript + MUI
- Backend: Quarkus + PostgreSQL mit REST APIs
- Bestehender Code: 3 verschiedene Implementierungen (Original, MUI, V2)
- Aktuelle Route: `/cockpit` zeigt `SalesCockpitV2` in `MainLayoutV2`
- Integration: Daten von Kundenmanagement, E-Mail, Aktivitäten, KPIs

**Business-Kontext:**
- Zielgruppe: Verkäufer (auch Manager nutzen dieselbe Ansicht)
- Hauptnutzen: Produktivitätssteigerung durch zentrale Information
- Kritische User-Journeys: Lead bearbeiten, Kunde kontaktieren, Angebot erstellen
- Performance-Anforderung: 30s Update-Intervall ausreichend

## 🎯 Entscheidungen bereits getroffen

**Durch Jörg bestätigt:**
1. **Basis-Version:** `SalesCockpitV2` als Foundation (läuft auf `/cockpit`)
2. **Zielgruppe:** Eine Ansicht für alle User-Rollen (Manager = Verkäufer-Sicht)
3. **Interaktion:** Einfach-Klick für Details, Doppel-Klick für Detailseite
4. **Performance:** 30 Sekunden Update-Intervall reicht aus

## 💭 Offene strategische Fragen zur Diskussion

## 💭 DISKUSSIONSPUNKT 1: Cockpit-Architektur-Ansatz

### 🔍 **Das Problem erklärt:**
Wir haben 3 verschiedene Cockpit-Implementierungen gefunden:
- **Original:** CSS-basiert, funktional aber legacy
- **MUI-Version:** Modernere UI, teilweise implementiert
- **V2-Experimental:** Neue Ansätze, unvollständig

### 🤔 **Die strategische Frage:**
**Welchen Architektur-Ansatz wählen wir als Basis für unsere Sidebar-Integration?**

#### Option A: "Evolutionär"
- Beste Teile aus allen 3 Versionen kombinieren
- Schrittweise Migration zu einheitlicher Basis
- **Pro:** Bewährte Funktionen bleiben erhalten
- **Contra:** Mehr Technical Debt kurzfristig

#### Option B: "Clean Slate"
- Komplett neu basierend auf neuer Sidebar-Struktur
- Nur bewährte Backend-APIs wiederverwenden
- **Pro:** Saubere Architektur von Anfang an
- **Contra:** Mehr Planungsaufwand

#### Option C: "Best-of-Breed"
- MUI-Version als Basis, erweitert um V2-Features
- Backend-Services 1:1 übernehmen
- **Pro:** Balance aus Bewährtem und Modernem
- **Contra:** Requires careful component analysis

**Jörgs Präferenz?** Welcher Ansatz passt besser zu deiner Vision?

---

## 💭 DISKUSSIONSPUNKT 2: Dashboard-Flexibilität vs. Struktur

### 🔍 **Das Problem erklärt:**
Ein Dashboard kann zwei Philosophien folgen:

#### **Strukturiert (wie aktuelle Implementation):**
```
┌─────────────┬──────────────────┬─────────────────┐
│  Mein Tag   │  Fokus-Liste     │ Aktions-Center  │
│  (fest)     │  (fest)          │  (fest)         │
└─────────────┴──────────────────┴─────────────────┘
```

#### **Flexibel (Widget-System):**
```
┌─────────────┬──────────────────┬─────────────────┐
│ [Widget A]  │ [Widget C]       │ [Widget E]      │
│ [Widget B]  │ [Widget D]       │ [Widget F]      │
└─────────────┴──────────────────┴─────────────────┘
```

### 🤔 **Die strategische Frage:**
**Wie flexibel soll das Cockpit für verschiedene User-Typen sein?**

#### Scenario 1: "Sales Manager"
- Braucht KPIs, Team-Performance, Risk-Customers
- Weniger tägliche Tasks, mehr Überblick

#### Scenario 2: "Sales Rep"
- Braucht Today's Tasks, Customer-Focus, Quick-Actions
- Mehr operativ, weniger strategisch

#### Scenario 3: "Admin"
- Braucht System-Health, User-Activity, Audit-Logs
- Komplett andere Informationen

**Strategische Frage:** Soll das Cockpit role-based verschiedene Layouts haben oder für alle gleich bleiben?

---

## 💭 DISKUSSIONSPUNKT 3: Integration mit anderen Modulen

### 🔍 **Das Problem erklärt:**
Das Cockpit ist der "Hub" - es zeigt Informationen aus allen anderen Modulen:

```
Cockpit zeigt Daten von:
├── 02_neukundengewinnung → Neue Leads, E-Mail-Inbox
├── 03_kundenmanagement → Customer-Focus, Activities
├── 04_auswertungen → KPIs, Performance-Metrics
├── 05_kommunikation → Team-Updates, Notifications
├── 06_einstellungen → User-Preferences, Layout
└── 08_administration → System-Alerts (für Admins)
```

### 🤔 **Die strategische Frage:**
**Wie stark soll das Cockpit in andere Module "hineingreifen"?**

#### Option A: "Cockpit als Viewer"
- Cockpit zeigt nur Zusammenfassungen
- Actions führen zu anderen Modulen
- **Pro:** Klare Modul-Trennung
- **Contra:** Mehr Klicks für User

#### Option B: "Cockpit als Command Center"
- Cockpit ermöglicht direkte Actions
- Kunde erstellen, E-Mail schreiben, etc. direkt möglich
- **Pro:** Weniger Navigation für User
- **Contra:** Komplexere Architektur

#### Option C: "Hybrid-Ansatz"
- Häufige Actions direkt im Cockpit
- Komplexe Actions führen zu Detail-Modulen
- **Pro:** Balance aus Convenience und Klarheit
- **Contra:** Requires definition of "häufige Actions"

**Deine Präferenz:** Wie soll sich das Cockpit verhalten wenn User auf Kunden/Tasks/E-Mails klicken?

---

## 💭 DISKUSSIONSPUNKT 4: Performance vs. Real-Time Updates

### 🔍 **Das Problem erklärt:**
Ein Dashboard kann verschiedene Update-Strategien haben:

#### **Conservative (30s Auto-Refresh):**
- Weniger Server-Load
- Delayed Information
- Einfache Implementation

#### **Aggressive (Real-Time with WebSockets):**
- Sofortiges Feedback
- Höhere Server-Load
- Komplexere Architecture

#### **Smart (Context-Aware Updates):**
- Real-Time für kritische Daten (neue Leads)
- Scheduled für statische Daten (KPIs)
- User-triggered für on-demand Daten

### 🤔 **Die strategische Frage:**
**Wie "live" muss das Cockpit für produktive Sales-Arbeit sein?**

#### Sales-Scenario 1: "Teammitglied erstellt neuen Lead"
- **Conservative:** Erscheint nach 30s in anderem User's Cockpit
- **Real-Time:** Erscheint sofort mit Notification
- **Smart:** Erscheint sofort wenn User Lead-Liste offen hat

#### Sales-Scenario 2: "Kunde ändert Status zu 'Interessiert'"
- **Conservative:** Update bei nächstem Refresh
- **Real-Time:** Sofortiges Update + Toast-Notification
- **Smart:** Update + Notification nur wenn Customer ausgewählt

**Strategische Frage:** Welche Information-Latenz ist für Sales-Teams akzeptabel?

---

## 🔗 Kritische Feature-Interaktionen & Dependencies

### **Input zum Cockpit (was es von anderen Modulen braucht):**

**02_neukundengewinnung → Cockpit:**
- Neue Leads (Anzahl + Liste für Spalte 2)
- Unbearbeitete E-Mails (Badge in Spalte 1)
- Status laufender Kampagnen

**03_kundenmanagement → Cockpit:**
- Gefilterte Kundenliste (Hauptinhalt Spalte 2)
- Offene Verkaufschancen (Spalte 1 Tasks)
- Risiko-Kunden (Spalte 1 Alarme)
- Fällige Aktivitäten (Spalte 1 Today's Tasks)

**04_auswertungen → Cockpit:**
- Tagesumsatz (Header-KPI)
- Performance-Metriken (Header)
- Zielerreichung (Spalte 1 Status)

**05_kommunikation → Cockpit:**
- Ungelesene Team-Nachrichten (Notification Badge)
- Wichtige Ankündigungen (Spalte 1 Alerts)

**08_administration → Cockpit (Admin-Only):**
- System-Alerts (Spalte 1)
- Performance-Warnungen (Header)

### **Output vom Cockpit (was es zu anderen Modulen sendet):**

**Cockpit → 03_kundenmanagement:**
- Klick auf Kunde → Kundendetails (Spalte 3 oder neue Seite)
- "Neuer Kunde" → Erfassungsformular
- "Verkaufschance" → Pipeline-Dialog

**Cockpit → M8_calculator:**
- "Angebot erstellen" → Calculator-Modal mit vorgewähltem Kunden
- "Preiskalkulation" → Calculator öffnen

**Cockpit → 02_neukundengewinnung:**
- "Neue E-Mails" → E-Mail Posteingang
- "Lead bearbeiten" → Lead-Detail

**Cockpit → 05_kommunikation:**
- "Team benachrichtigen" → Chat öffnen
- Notification-Klick → entsprechender Bereich

### **Kritische Missing Dependencies:**

**Blocker für vollständige Funktion:**
1. **FC-005 Kundenmanagement** → Ohne dies ist Spalte 2 (Fokus-Liste) leer
2. **M8 Calculator** → "Angebot erstellen" Button führt ins Leere
3. **FC-013 Activity System** → Spalte 3 Timeline ist nicht funktional
4. **Notification-System** → Alerts/Badges funktionieren nicht

**Workflow-Abhängigkeiten:**
- Lead → Kunde → Verkaufschance → Angebot (durchgängige User-Journey)
- Termin → Follow-up → Abschluss (zeitbasierte Aktions-Kette)

## 💭 Zusätzliche strategische Frage

### **DISKUSSIONSPUNKT 5: Implementation-Reihenfolge**

**Das Problem:** Cockpit kann standalone funktionieren, aber ist ohne andere Module nur begrenzt nützlich.

**Option A - Cockpit First:**
- Cockpit vollständig implementieren mit Mock-Daten
- Dann andere Module integrieren wenn verfügbar
- **Pro:** Schnell sichtbares Ergebnis, sofort testbar
- **Contra:** Teilfunktional bis Integration abgeschlossen

**Option B - Dependencies First:**
- Erst FC-005 (Kundenmanagement), M8 (Calculator), FC-013 (Activities) fertigstellen
- Dann Cockpit mit echten Integrationen implementieren
- **Pro:** Cockpit ist bei Launch vollständig funktional
- **Contra:** Längere Zeit bis sichtbares Cockpit-Ergebnis

**Option C - Parallel Development:**
- Cockpit-Basis parallel zu anderen Modulen entwickeln
- Integration-Punkte definieren und schrittweise verknüpfen
- **Pro:** Optimale Zeitnutzung, koordinierte Entwicklung
- **Contra:** Erfordert präzise Koordination der APIs

**Strategische Frage:** Welche Reihenfolge passt besser zur Projekt-Timeline und den verfügbaren Ressourcen?

---

## 🤖 **DISKUSSIONSBEITRAG: ChatGPT (2025-09-18)**

**Eingereicht:** 2025-09-18 via Jörg
**Umfang:** Umfassende Architektur-Empfehlung + konkrete Implementierungs-Roadmap

### **Kernempfehlungen:**

#### **1. Cockpit-Philosophie: Hybrid "Command Center"**
- Häufige Micro-Actions direkt im Cockpit (Notiz, Aufgabe, Stage-Wechsel, Angebot erstellen)
- Komplexe Workflows per Deep-Link in Fachmodule
- **Begründung:** Reduziert Klicks ohne Architektur-Verkomplizierung

#### **2. Layout-Struktur: 3-Spalten + Gespeicherte Ansichten**
- **Spalte 1:** "Mein Tag" (25-30%) - Alerts, Tasks, Team-Updates
- **Spalte 2:** "Fokusliste" (40-50%) - Priorisierte Kunden mit Filtern
- **Spalte 3:** "Aktions-Center" (25-30%) - Details + Quick-Actions + Timeline
- **Innovation:** Gespeicherte Ansichten statt freie Widget-Layouts
- **Begründung:** Einheitlich aber individuell nutzbar

#### **3. Update-Strategie: Smart Real-Time**
- **Baseline:** 30s Auto-Refresh (bestätigt)
- **Live:** Neue Leads + @Mentions via WebSocket
- **Geplant:** KPIs, Dashboard-Metriken
- **On-Demand:** Bei User-Interaktion

#### **4. Technischer Ansatz: SalesCockpitV2 + Best-of-Components**
- SalesCockpitV2 als Shell (unsere Entscheidung bestätigt)
- MUI-Komponenten gezielt portieren (DataGridPro, Drawer, SpeedDial)
- Legacy CSS nur "bridgen", parallel Refactor

#### **5. KPI-Definition konkretisiert:**
- **Header-KPIs:** Tagesumsatz, Pipeline-Coverage, SLA Lead-Response-Time, Stuck Deals, Data-Quality-Score
- **Performance-Budgets:** P95 <200ms API, <2s Initial Load
- **Success-Metrics:** Messbare Outcomes definiert

### **Roadmap-Vorschlag:**
```
Phase 0: API-Contracts & Thin Slices (1-2 Sprints)
Phase 1: Cockpit Core (2-3 Sprints)
Phase 2: Hybrid-Actions & Smart-Updates (2 Sprints)
Phase 3: Gespeicherte Ansichten & A/B-Tests (fortlaufend)
```

### **Konkrete Arbeitsaufträge:**
- Define "häufige Aktionen" (Product Owner)
- KPI-Definition & SQL-Skizzen (BI/Backend)
- API-Contracts versionieren (Backend)
- UI-Spike: DataGridPro mit 10k-Zeilen-Test (Frontend)
- Smart-Update-PoC: WebSocket-Integration (Platform)

### **Anmerkungen zu Migration V3 & M8:**
**ChatGPT Limitation:** Hatte keinen Zugang zu aktueller Infrastruktur-Planung
- Erwähnt M8 Calculator als "veraltet" aber ohne Details der neuen Planung
- V3 Migration erwähnt aber keine Integration in Roadmap

---

## 🤖 Claude Planning Notes

**Context für AI-Diskussion:** Diese Entscheidungen bestimmen die gesamte Cockpit-Architektur und beeinflussen alle anderen Module. Das Cockpit ist der zentrale Hub des Systems - seine Entscheidungen werden zur Referenz für alle anderen Feature-Integrationen.

**Nach Diskussion erforderlich:**
1. Technical Concept mit finalen Architektur-Entscheidungen
2. Migration-Plan von bestehenden 3 Code-Versionen
3. API-Contracts für alle Feature-Integrationen
4. Implementation-Roadmap mit Dependencies

**Impact auf Gesamtsystem:**
- Performance-Standards für alle Module
- User-Experience-Patterns für Navigation
- Integration-Patterns für Feature-Kommunikation
- Mobile-Responsive-Standards