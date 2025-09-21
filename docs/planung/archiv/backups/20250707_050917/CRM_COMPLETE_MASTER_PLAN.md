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

* **Phase 1: Das begeisternde Fundament (Unser aktueller Fokus)**
    1.  **Backend Finalisierung (JETZT!):** Die Integration-Tests werden mit Testcontainers repariert. **Das ist der Gatekeeper für alles Weitere.**
    2.  **Frontend Foundation:** Das 3-Spalten-Layout des Cockpits wird mit `Zustand` als State Manager implementiert.
    3.  **Mock-Endpunkte für Entwicklung:** Dedizierte Backend-Mock-Endpunkte unter `/api/dev/*` für saubere Entwicklung ohne DB-Verschmutzung (siehe [ADR-001](./adr/ADR-001_backend_mock_endpoint_for_dev.md)).
    4.  **Erste funktionale Integration:** Die `CustomerList` wird in die mittlere Spalte des Cockpits integriert.
    5.  **Activity Timeline (Backend):** Die Services und API-Endpunkte für die Kunden-Zeitleiste werden gebaut.

* **Phase 2: Prozess-Exzellenz & Integration**
    * **Backend-for-Frontend (BFF)** implementieren.
    * **Opportunity & Aktivitäten Management** (Backend & Frontend).
    * **Xentral-Integration** (Proof-of-Concept, dann volle Integration).
    * **"BCC-to-CRM"** und die **Triage-Inbox**.

* **Phase 3 & 4: Intelligenz & Proaktive Unterstützung**
    * **Data Health Dashboard**, **Partner-Lifecycle**-Automatisierung, **Lead Scoring** und der **AI Sales Assistant**.