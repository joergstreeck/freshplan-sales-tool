# 🚀 CRM COMPLETE MASTER PLAN V4 (FINAL) - Das Sales Command Center

**Version:** 4.0
**Datum:** 06.07.2025
**Status:** VERBINDLICH - Dies ist unsere einzige Wahrheit und ersetzt alle vorherigen Pläne.

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

* **Spalte 1: Mein Tag (Übersicht & Prioritäten):**
    * Zeigt proaktiv die wichtigsten Aufgaben, Termine (via **Google Kalender-Integration**) und KI-gestützten Alarme für den aktuellen Tag.
    * Beinhaltet die **"Triage-Inbox"** für nicht zugeordnete Kommunikation.

* **Spalte 2: Fokus-Liste (Arbeitsvorrat):**
    * Die dynamische Arbeitsliste für Kunden.
    * Ermöglicht Wechsel zwischen Listen-, Karten- und Kanban-Ansicht.
    * Bietet mächtige Filter und speicherbare Ansichten.

* **Spalte 3: Aktions-Center (Der Arbeitsbereich):**
    * Hier findet die kontextbezogene Arbeit statt.
    * Bietet einen **geführten Prozess** je nach Kundenstatus (z.B. Neukunden-Akquise).
    * Funktionen wie **Bonitätsprüfung**, **Rabattrechner** und **Angebotserstellung** sind als logische Schritte in diesen Prozess integriert.
    * Ermöglicht das direkte **Schreiben von E-Mails** und die Dokumentation aller Aktivitäten.

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
    
    **Ziel:** Transformation von einer funktionsgetriebenen zu einer prozessorientierten Anwendung durch eine intuitive 5-Punkte-Navigation und kontextbezogene Aktionen.
    
    **Kernelemente:**
    - **5-Punkte-Navigation:** Mein Cockpit | Neukundengewinnung | Kundenmanagement | Auswertungen & Berichte | Einstellungen
    - **Globaler "+ Neu" Button:** Schnellzugriff für neue Entitäten von überall
    - **Prozessorientierte Werkzeuge:** Funktionen erscheinen dort, wo sie im Arbeitsablauf benötigt werden
    - **Klare deutsche Begriffe:** Gemäß unserem neuen [Way of Working](./WAY_OF_WORKING.md)
    
    **📋 Detailliertes Konzept:** [FC-002: UI/UX-Neuausrichtung & Prozessorientierung](./features/2025-07-08_TECH_CONCEPT_ui-ux-refactoring.md)

* **Phase 4: Activity Timeline & Prozess-Integration**
    1.  **Activity Timeline:** Services und API-Endpunkte für die Kunden-Zeitleiste
    2.  **Backend-for-Frontend (BFF)** implementieren
    3.  **Opportunity & Aktivitäten Management** (Backend & Frontend)
    4.  **Xentral-Integration** (Proof-of-Concept, dann volle Integration)
    5.  **"BCC-to-CRM"** und die **Triage-Inbox**

* **Phase 5: Intelligenz & Proaktive Unterstützung**
    * **Data Health Dashboard**
    * **Partner-Lifecycle**-Automatisierung
    * **Lead Scoring** 
    * **AI Sales Assistant**

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

- **FC-001**: [Dynamische Fokus-Liste](./features/2025-07-07_TECH_CONCEPT_dynamic-focus-list.md) - Status: Backend ✅ | Frontend ✅
- **FC-002**: [UI/UX-Neuausrichtung & Prozessorientierung](./features/2025-07-08_TECH_CONCEPT_ui-ux-refactoring.md) - Status: 📋 In Planung