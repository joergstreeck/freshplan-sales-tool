# 🚀 CRM COMPLETE MASTER PLAN V4 (FINAL) - Das Sales Command Center

**Version:** 4.1
**Datum:** 17.09.2025
**Status:** VERBINDLICH - Dies ist unsere einzige Wahrheit und ersetzt alle vorherigen Pläne.
**Letzte Aktualisierung:** Navigation-Struktur und Implementierungsstatus hinzugefügt

---

## 🎯 Die Vision

> **"Wir bauen ein intelligentes Sales Command Center, das unsere Vertriebsmitarbeiter lieben, weil es ihnen proaktiv die Informationen, Insights und geführten Prozesse liefert, die sie brauchen, um erfolgreich zu sein."**

Wir bauen kein Werkzeug, das man benutzen *muss*. Wir bauen einen Partner, den man benutzen *will*.

---

## 🎨 UI/UX & Corporate Identity

**VERBINDLICH: Alle sichtbaren Frontend-Elemente MÜSSEN der Freshfoodz CI entsprechen!**

### Freshfoodz Corporate Identity Standards

#### Farben (Pflicht für alle UI-Komponenten):
- **Primärgrün**: `#94C456` - Hauptfarbe für Buttons, Links, Aktionen
- **Dunkelblau**: `#004F7B` - Sekundärfarbe für Headlines, Navigation
- **Weiß**: `#FFFFFF` - Hintergründe, Cards
- **Schwarz**: `#000000` - Haupttext, Icons

#### Typografie (Verbindlich):
- **Headlines**: Antonio Bold - für alle Überschriften und Titel
- **Fließtext**: Poppins Regular/Medium - für alle Texte und UI-Elemente

#### Markenidentität:
- **Slogan**: "So einfach, schnell und lecker!" - Integration in Onboarding/Marketing
- **Logo-Regeln**: Nur auf neutralen Hintergründen, Schutzzone beachten

#### Implementierungsrichtlinien:
1. **CSS-Variablen definieren** für alle CI-Farben
2. **Font-Loading optimieren** für Antonio Bold und Poppins
3. **Design System erstellen** mit allen CI-konformen Komponenten
4. **Accessibility sicherstellen** bei Farbkontrasten (WCAG 2.1 AA)

**Referenz**: `/docs/FRESH-FOODZ_CI.md` für detaillierte CI-Vorgaben

---

## 🏛️ Unsere Philosophie: Die 3 Kernprinzipien

Diese Prinzipien leiten jede einzelne Design- und Entwicklungsentscheidung.

### 1. Geführte Freiheit (Guided Freedom)
Das System bietet von sich aus klare, auf globalen Best Practices basierende Standard-Workflows an. Der Nutzer wird an die Hand genommen, anstatt mit Optionen überladen zu werden. Er kann von diesem "goldenen Pfad" abweichen, wird aber immer wieder sanft darauf zurückgeführt.
* **Konvention vor Konfiguration:** Sinnvolle Voreinstellungen statt leerer Formulare.
* **Der 80/20-Ansatz:** Die UI ist für die 80% der täglichen Aufgaben optimiert.
* **Intelligenz statt Administration:** Das System arbeitet für den Nutzer, nicht umgekehrt.

### 2. Alles ist miteinander verbunden
Keine Information ist eine Sackgasse. Jeder Kunde, jede E-Mail, jede Aufgabe ist ein Sprungbrett zur nächsten relevanten Aktion.
* **Kontextbezogene Aktionen:** Aus einem Kundenprofil heraus eine E-Mail starten.
* **Globale Aktionen:** Von überall aus eine Notiz oder einen Anruf protokollieren und dann den passenden Kunden suchen.
* **Triage-Inbox:** E-Mails, die nicht zugeordnet werden können, werden nicht verworfen, sondern zu einer Chance zur Lead-Generierung.

### 3. Skalierbare Exzellenz
Wir bauen von Tag 1 an auf einem Fundament, das für Wachstum, Performance und hohe Qualität ausgelegt ist.
* **API-First & Entkopplung:** Stabile Services sind die Basis.
* **Performance als Feature:** Antwortzeiten unter 200ms sind das Ziel.
* **Datenqualität als Fundament:** Proaktive Validierung statt reaktiver Korrektur.

---

## 🖥️ Die konkrete UI-Vision: Das dreigeteilte Sales Cockpit

Die Hauptoberfläche ist ein einziges, dreigeteiltes Cockpit. Es ist **responsiv** und passt sich allen Bildschirmgrößen an.

### Frontend-Architektur (NEU - 09.07.2025)

**Status:** 🔍 40% Analyse abgeschlossen - Wiederverwendbarkeits-Matrix erstellt

**Technische Struktur:**
```
CockpitView.tsx (Hauptcontainer)
├── MeinTag.tsx (Spalte 1)
│   ├── AlertsList.tsx        # Tagesalarme & KI-Insights
│   ├── AppointmentsList.tsx  # Google Kalender Integration
│   ├── TasksList.tsx         # Priorisierte Aufgaben
│   └── TriageInbox.tsx       # Nicht zugeordnete E-Mails
├── FocusListColumn (Spalte 2)
│   └── [Wiederverwendung von FC-001]
└── AktionsCenter.tsx (Spalte 3)
    ├── CustomerDetail.tsx    # 360° Kundenansicht
    ├── ActivityTimeline.tsx  # Alle Interaktionen
    └── QuickActions.tsx      # Kontextuelle Aktionen
```

**Performance-Optimierungen:**
- Aggregierter Backend-Endpunkt: `GET /api/cockpit/overview`
- Lazy Loading für Timeline & Details
- Optimistic UI Updates
- Local Storage für Layout-Präferenzen

### Die drei Säulen im Detail:

* **Spalte 1: Mein Tag (Übersicht & Prioritäten):**
    * Zeigt proaktiv die wichtigsten Aufgaben, Termine (via **Google Kalender-Integration**) und KI-gestützten Alarme für den aktuellen Tag.
    * Beinhaltet die **"Triage-Inbox"** für nicht zugeordnete Kommunikation.
    * **Wiederverwendung:** 70% bestehender Code adaptierbar

* **Spalte 2: Fokus-Liste (Arbeitsvorrat):**
    * Die dynamische Arbeitsliste für Kunden (FC-001 Integration).
    * Ermöglicht Wechsel zwischen Listen-, Karten- und Kanban-Ansicht.
    * Bietet mächtige Filter und speicherbare Ansichten.
    * **Wiederverwendung:** 100% von FC-001

* **Spalte 3: Aktions-Center (Der Arbeitsbereich):**
    * Hier findet die kontextbezogene Arbeit statt.
    * Bietet einen **geführten Prozess** je nach Kundenstatus (z.B. Neukunden-Akquise).
    * Funktionen wie **Bonitätsprüfung**, **~~Rabattrechner~~** [VERALTET - Neue Rabattlogik ab 01.10.2025] und **Angebotserstellung** sind als logische Schritte in diesen Prozess integriert.
    * **NEU ab 01.10.2025:** Jahresumsatz-basierte Rabattstufen statt Bestellwert-Kalkulator (siehe `/docs/business/rabattlogik_2025_NEU.md`)
    * Ermöglicht das direkte **Schreiben von E-Mails** und die Dokumentation aller Aktivitäten.
    * **Wiederverwendung:** 85% aus customer/activities Modulen

**Details:** Siehe `/docs/features/FC-002-M3-cockpit.md`

---

## 📱 Aktuelle Navigation & Implementierungsstatus (Stand: 17.09.2025)

### Navigation-Struktur mit Implementierungsstatus

**✅ = Funktional | ⏳ = Placeholder | 🔄 = In Entwicklung**

```
📍 Mein Cockpit ✅
├── 📧 Neukundengewinnung
│   ├── E-Mail Posteingang ⏳
│   ├── Lead-Erfassung ⏳
│   └── Kampagnen ⏳
├── 👥 Kundenmanagement
│   ├── Alle Kunden ✅
│   ├── Neuer Kunde ✅ (Modal)
│   ├── Verkaufschancen ✅
│   └── Aktivitäten ⏳
├── 📊 Auswertungen
│   ├── Umsatzübersicht ⏳
│   ├── Kundenanalyse ⏳
│   └── Aktivitätsbericht ⏳
├── 💬 Kommunikation
│   ├── Team-Chat ⏳
│   ├── Ankündigungen ⏳
│   ├── Notizen ⏳
│   └── Interne Nachrichten ⏳
├── ⚙️ Einstellungen
│   ├── Mein Profil ⏳
│   ├── Benachrichtigungen ⏳
│   ├── Darstellung ⏳
│   └── Sicherheit ⏳
├── ❓ Hilfe & Support
│   ├── Erste Schritte ⏳
│   ├── Handbücher ⏳
│   ├── Video-Tutorials ⏳
│   ├── Häufige Fragen ⏳
│   └── Support kontaktieren ⏳
└── 🔐 Administrator
    ├── Audit Dashboard ✅
    ├── Benutzerverwaltung ✅
    ├── System
    │   ├── API Status ✅
    │   ├── System-Logs ⏳
    │   ├── Performance ⏳
    │   └── Backup & Recovery ⏳
    ├── Integration
    │   ├── KI-Anbindungen ⏳
    │   ├── Xentral ⏳
    │   ├── E-Mail Service ⏳
    │   ├── Payment Provider ⏳
    │   ├── Webhooks ⏳
    │   └── + Neue Integration ⏳
    ├── Hilfe-Konfiguration
    │   ├── Hilfe-System Demo ✅
    │   ├── Tooltips verwalten ⏳
    │   ├── Touren erstellen ⏳
    │   └── Analytics ⏳
    └── Compliance Reports ⏳
```

### 📈 Implementierungs-Fortschritt

**Gesamtfortschritt: 8 von 42 Features (19%) vollständig implementiert**

#### ✅ Fertige Module:
1. **Mein Cockpit** - Vollständiges Dashboard mit 3-Spalten-Layout
2. **Alle Kunden** - Liste mit Suche, Filter und Sortierung
3. **Neuer Kunde** - Modal-Dialog mit Wizard
4. **Verkaufschancen** - Pipeline-Management
5. **Audit Dashboard** - Vollständige Audit-Trail-Funktionalität
6. **Benutzerverwaltung** - User-CRUD mit Rollen
7. **API Status** - System-Health-Monitoring
8. **Hilfe-System Demo** - Kontextsensitive Hilfe

#### 🔄 In Entwicklung:
- E-Mail Posteingang (FC-003)
- Aktivitäten-Timeline
- Umsatzübersicht mit neuer Rabattlogik

#### 📋 Placeholder-Status:
Alle anderen 34 Features zeigen informative Placeholder-Seiten mit:
- Beschreibung der geplanten Funktionalität
- Erwartetes Release-Datum
- Liste der kommenden Features
- Freshfoodz CI-konforme Darstellung

**Detaillierte Übersicht:** `/docs/SIDEBAR_STRUKTUR_2025.md`

---

## 🗺️ Die finale Roadmap

* **Phase 1: Das begeisternde Fundament (✅ ABGESCHLOSSEN)**
    1.  ✅ **Backend Finalisierung:** Die Integration-Tests wurden mit Testcontainers repariert.
    2.  ✅ **Frontend Foundation:** Das 3-Spalten-Layout des Cockpits wurde implementiert.
    3.  ✅ **Mock-Endpunkte für Entwicklung:** Backend-Mock-Endpunkte unter `/api/dev/*` implementiert.
    4.  ✅ **Test-Daten-Management:** Kontrollierte Test-Szenarien mit Seed/Clean-Funktionalität.
    
* **Phase 2: Dynamische Fokus-Liste (AKTUELLER FOKUS)** - Intelligente, filterbare und adaptive Arbeitsfläche
    1.  **Phase 2.1 (Backend):** Implementierung der dynamischen Such-API (`/api/customers/search`)
        - Query Builder für komplexe Filter-Kombinationen
        - Performance-Optimierung mit Indizes
        - Support für gespeicherte Ansichten
    
    ### Phase 2.2: Hardening & Optimierung (FC-001)
    **Trigger:** Diese Phase beginnt, nachdem die Frontend-Implementierung für die "Dynamische Fokus-Liste" funktional abgeschlossen und erfolgreich integriert ist.
    
    **Ziel:** Beseitigung der bewusst in Kauf genommenen technischen Schulden aus Phase 2.1 und Sicherstellung der Produktionsreife des Features.
    
    **Konkrete Aufgaben:**
    - **Technische Schuld (Tests):** Wiederherstellung und Erweiterung der Integration-Tests für die CustomerSearchResource, um alle Filter-Kombinationen und Edge-Cases abzudecken.
    - **Performance:** Implementierung der in FC-001 definierten Datenbank-Indizes zur Beschleunigung von Suchanfragen.
    - **Last-Tests:** Durchführung von Last-Tests mit einer größeren Datenmenge (z.B. 10.000+ Kunden), um die Performance der Such-API unter realen Bedingungen zu validieren.
    
    3.  **Phase 2.3 (Frontend):** Implementierung der Filterleiste
        - Globale Suche mit Auto-Complete
        - Quick-Filter für häufige Szenarien
        - Erweiterte Filter-Dialoge
    4.  **Phase 2.4 (Frontend):** Implementierung der adaptiven Ansicht
        - Card-Layout als Standard-Ansicht
        - Optionale Tabellen-Ansicht für Analysen
        - Nahtloser Wechsel zwischen Ansichten
    
    **📋 Detailliertes Konzept:** [FC-001: Dynamische Fokus-Liste](./features/2025-07-07_TECH_CONCEPT_dynamic-focus-list.md)

* **Phase 3: Neuausrichtung der Benutzeroberfläche & Arbeitsprozesse (FC-002)**
    
    **Status:** ✅ Planungsphase abgeschlossen | 🚀 Bereit für Implementierung
    
    **Strategische Zusammenfassung:**
    Die UI/UX-Neuausrichtung erfolgt in 3 klar definierten Meilensteinen über 35-40 Personentage:
    
    - **Meilenstein 1 - "Das neue Grundgerüst" (5 Tage):** Navigation & Basis-Funktionalität
    - **Meilenstein 2 - "Das neue Cockpit" (17 Tage):** Kern-Arbeitsbereich & Backend-Modernisierung  
    - **Meilenstein 3 - "Feature-Vervollständigung" (12 Tage):** Alle verbleibenden Module
    
    **Kernvorteile:**
    - 70-100% Code-Wiederverwendung im Backend
    - Schrittweise Migration ohne Big-Bang-Risiko
    - Quick Wins durch frühe Cockpit-Verfügbarkeit
    
    **📋 Vollständiger Implementierungsplan:** [FC-002-IMPLEMENTATION_PLAN.md](./features/FC-002-IMPLEMENTATION_PLAN.md)
    **🎯 Technisches Hub-Dokument:** [FC-002-hub.md](./features/FC-002-hub.md)
    
    _Alle technischen Details, Modul-Analysen und Abhängigkeiten sind im verlinkten Implementierungsplan dokumentiert. Das Hub-Dokument bietet eine detaillierte Übersicht aller Module inkl. der neuen Layout-Architektur._

* **Phase 4: Kritische Vertriebsfunktionen (NEU - 09.07.2025)**
    
    **Basierend auf direktem Nutzerfeedback - Diese Features sind essentiell für den Vertriebserfolg!**
    
    **Phase 4.1 - Team-Funktionen (11-12 Tage):**
    1. **[FC-003: E-Mail-Integration (BCC-to-CRM)](./features/FC-003-email-integration.md):** 
       - Automatische E-Mail-Archivierung beim richtigen Kunden
       - Triage-Inbox für unzugeordnete Kommunikation
       - Thread-Erkennung für Konversationen
    
    2. **Interne Team-Nachrichten:** 
       - Team-Chat pro Kunde/Lead
       - @-Mentions für direkte Benachrichtigungen
       - Aktivitäts-Feed zeigt wer gerade was macht
    
    3. **[FC-004: Verkäuferschutz-System](./features/FC-004-verkaeuferschutz.md):** 
       - Provisions-Sicherung mit 5-stufigem Schutz-System
       - Faire Regeln mit automatischer Eskalation
       - Provisions-Splitting bei Teamarbeit
       - Historie für Konfliktlösung
    
    **Phase 4.2 - Führungs-Tools (7-8 Tage):**
    4. **[FC-007: Chef-Dashboard mit KPIs](./features/FC-007-chef-dashboard.md):** 
       - Live-Aktivitäten-Monitor: Was tun meine Verkäufer gerade?
       - Pipeline-Analyse: Wieviel Geschäft ist in Anbahnung?
       - Performance-Matrix: Wer braucht Coaching?
       - KI-gestützte Insights und Handlungsempfehlungen
    
    **Phase 4.3 - Xentral-Integration (8-10 Tage):**
    5. **[FC-005: Provisions-Management](./features/FC-005-xentral-integration.md):** 
       - Echtzeit-Integration via REST-API und Webhooks
       - Automatische Provisionsberechnung bei Zahlungseingang
       - Individuelle Provisionssätze und Sonderregeln
       - Vollständige Transparenz für alle Beteiligten
       - **📊 [Xentral API Analyse](./technical/XENTRAL_API_ANALYSIS.md)**
    
    **Phase 4.4 - Activity Timeline (5 Tage):**
    6. **Timeline & BFF:** Integration der ursprünglich geplanten Features
    
    **📋 Übersicht aller neuen Features:** [FC-002-PHASE4-ADDITIONS.md](./features/FC-002-PHASE4-ADDITIONS.md)

* **Phase 5: Mobile First & KI-Integration**
    
    **Phase 5.1 - Mobile Companion App (15-20 Tage):**
    * **[FC-006: FreshPlan Mobile Companion](./features/FC-006-mobile-app.md):**
      - Revolutionäre Sprach-zu-Text Erfassung mit KI-Parsing
      - Vollständige Offline-Fähigkeit mit intelligentem Sync
      - Visitenkarten-Scanner mit automatischer Kundenzuordnung
      - Context-Awareness durch GPS und Kalender-Integration
      - One-Handed optimierte UI für Außendienst
    
    **Phase 5.2 - KI & Automation (17-20 Tage):**
    * **Intelligente Lead-Generierung:** 
      - Web-Monitoring für Trigger-Events
      - Social Listening auf LinkedIn/XING
      - Branchen-Crawler für neue Opportunities
    * **Anruf-Integration:** 
      - Automatische Gesprächstranskription
      - Stimmungsanalyse und Next-Best-Action
    * **AI Sales Assistant:** 
      - Proaktive Handlungsempfehlungen
      - Optimale Kontaktzeiten-Vorhersage
    * **Partner-Lifecycle Automation:** 
      - Automatisierte Follow-Ups
      - Intelligente Eskalation bei Inaktivität

---

## 🏗️ Backend-Architektur-Standards (NEU - 09.07.2025)

### Strategische Entscheidung: Modularer Monolith

Nach eingehender Analyse des bestehenden Customer-Monolithen (54 Dateien, eng gekoppelt) haben wir uns für eine **schrittweise Migration zu einem modularen Monolithen** entschieden:

1. **Migration statt Neubau**: Bestehender Code wird refactored, nicht neu geschrieben
2. **Modularer Monolith**: Service-ready Architektur, aber weiterhin als Monolith deployed
3. **Event-Driven Communication**: Lose Kopplung zwischen Modulen über Domain Events
4. **CQRS für Read-Heavy Operations**: Optimierte Read Models für Listen und Suche

### Neue Modul-Struktur (am Beispiel Customer)

```
modules/
├── customer-core/        # Kern-Entity mit Basis-Daten
├── customer-contacts/    # Kontaktpersonen-Verwaltung
├── customer-financials/  # Finanz- und Bonitätsdaten
└── customer-timeline/    # Event-basierte Historie
```

### Architektur-Prinzipien

1. **Bounded Contexts**: Klare Modul-Grenzen ohne zirkuläre Abhängigkeiten
2. **Domain Events**: Kommunikation zwischen Modulen nur über Events
3. **Feature Flags**: Schrittweise Migration mit Parallel-Betrieb
4. **API-Stabilität**: Keine Breaking Changes während der Migration

### Performance-Ziele

- API Response Time: P95 < 200ms
- Event Processing: < 100ms
- Read Model Updates: Eventually Consistent (< 1s)

**Details**: Siehe `/docs/features/FC-002-M5-kundenmanagement.md`

---

## 📋 Feature-Konzepte

Detaillierte technische Konzepte für alle größeren Features:

### Phase 1-3 Features:
- **FC-001**: [Dynamische Fokus-Liste](./features/2025-07-07_TECH_CONCEPT_dynamic-focus-list.md) - Status: Backend ✅ | Frontend ✅
- **FC-002**: [UI/UX-Neuausrichtung & Prozessorientierung](./features/2025-07-08_TECH_CONCEPT_ui-ux-refactoring.md) - Status: 📋 Planung abgeschlossen
  - **[Master-Implementierungsplan](./features/FC-002-IMPLEMENTATION_PLAN.md)** - 3 Meilensteine, 35-40 Personentage

### Phase 4 Features (Kritische Vertriebsfunktionen):
- **FC-003**: [E-Mail-Integration (BCC-to-CRM)](./features/FC-003-email-integration.md) - Status: 📋 Architektur definiert
- **FC-004**: [Verkäuferschutz-System](./features/FC-004-verkaeuferschutz.md) - Status: 📋 Schutz-Stufen definiert
- **FC-005**: [Xentral-Integration](./features/FC-005-xentral-integration.md) - Status: 📋 API analysiert
- **FC-007**: [Chef-Dashboard & KPIs](./features/FC-007-chef-dashboard.md) - Status: 📋 Metriken definiert

### Phase 5 Features:
- **FC-006**: [Mobile Companion App](./features/FC-006-mobile-app.md) - Status: 📋 Konzept erstellt

### Technische Analysen:
- **[Xentral API Analyse](./technical/XENTRAL_API_ANALYSIS.md)** - Status: ✅ Abgeschlossen