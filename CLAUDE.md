# Arbeitsrichtlinien für Claude im FreshPlan Sales Tool Projekt

## 1. Projektübersicht und Ziele

**Projektname:** FreshPlan Sales Tool
**Hauptziel:** Entwicklung eines modernen, robusten und benutzerfreundlichen Verkaufstools.
**Aktuelle Phase:** Phase 2 - Refactoring und Stabilisierung. Fokus auf Code-Qualität, Testabdeckung und Vorbereitung für zukünftige Features.
**Wichtigstes aktuelles Ziel:** Sicherstellung eines stabilen Standalone-Builds (`npm run build:standalone`) und Behebung der "Known Issues".

## 2. Kommunikation und Vorgehensweise

1.  **Sprache:** Deutsch.
2.  **Proaktivität:** Fasse dein Verständnis zusammen und frage nach, bevor du codest. Bei Unklarheiten oder Alternativen, stelle diese zur Diskussion.
3.  **Inkrementell Arbeiten:** Implementiere in kleinen, nachvollziehbaren Schritten. Teste häufig.
4.  **Fokus:** Konzentriere dich auf die aktuelle Aufgabe. Vermeide Scope Creep.
5.  **Claude-Protokoll:** Führe ein Markdown-Protokoll über deine Schritte, Entscheidungen und Testergebnisse für die aktuelle Aufgabe.

## 3. Wichtige Befehle und Werkzeuge

* `npm install`: Dependencies installieren (einmalig nach Clone oder bei neuen Dependencies).
* `npm run dev`: Startet den Vite-Dev-Server für die Hauptanwendung.
* `npm run build`: Erstellt einen Production-Build der Hauptanwendung.
* `npm run build:standalone`: Erstellt einen Production-Build der Standalone-Version (wichtig!).
* `npm run test`: Führt Vitest Unit- und Integrationstests aus.
* `npm run coverage`: Erstellt einen Test-Coverage-Bericht.
* `npm run lint`: Überprüft den Code mit ESLint.
* `npm run format`: Formatiert den Code mit Prettier.
* `npm run check`: Führt `lint`, `format` und TypeScript-Typüberprüfung aus.
* **Git:** Committe regelmäßig mit klaren Commit-Messages (z.B. "Refactor: CalculatorModule optimiert"). Arbeite im aktuellen Feature-Branch.

## 4. Architektur und Code-Struktur

* **Build-System:** Vite für Entwicklung und Build-Prozesse. Vitest für Tests.
* **Sprache:** TypeScript.
* **UI:** Direktes DOM-Management über `DOMHelper.ts` und spezifische UI-Module. Kein externes UI-Framework.
* **State Management:** `StateManager.ts` (Legacy: `StateManagerLegacy.ts` soll abgelöst werden).
* **Modularität:** Code ist in Modulen organisiert (siehe `src/modules/`). Basisklasse `src/core/Module.ts`.
* **Projektstruktur:**
    * `src/`: Haupt-Quellcode.
        * `core/`: Kernfunktionalitäten (DOMHelper, EventBus, Module, StateManager).
        * `modules/`: Anwendungsmodule (Calculator, Customer, i18n, PDF, etc.).
        * `services/`: Backend-Interaktionen, externe Dienste.
        * `store/`: Zustandsspeicher-Logik (Redux-ähnlich, aber vereinfacht).
        * `styles/`: CSS-Dateien.
        * `assets/`: Statische Dateien wie Logos.
        * `locales/`: Übersetzungsdateien (z.B. `de.json`).
    * `public/`: Statische Assets, die direkt kopiert werden.
    * `config/`: Konfigurationsdateien für das Tool.
    * `e2e/`: Playwright End-to-End Tests.
* **Logo-Handling:** Das Logo wird über CSS (`header-logo.css`) eingebunden und ist in `src/assets/` zu finden. Die dynamische Anpassung erfolgt ggf. über CSS-Variablen oder JavaScript je nach Konfiguration.
* **Architektur-Dokumente:** `ARCHITECTURE.md`, `DASHBOARD_ARCHITECTURE.md`.

## 5. Bekannte Probleme (Known Issues) und Workarounds

* Siehe `KNOWN_ISSUES.md` für eine aktuelle Liste.
* **Übersetzung dynamischer Tabs:** Ein bekanntes Problem. Workaround wird in Phase 2 gesucht.
* **Performance bei großen Datenmengen:** Bei der Verarbeitung sehr vieler Positionen im Calculator kann es zu Verzögerungen kommen. Optimierungen sind für spätere Phasen geplant.

## 6. Plan für Phase 2 (Refactoring)

(Basierend auf dem Plan, den wir mit Gemini entwickelt haben)

**Block A: Vorbereitende Maßnahmen und kritische Fixes**
    1. `CLAUDE.md` Finalisieren und Anwenden. (Dieser Schritt ist hiermit abgeschlossen, sobald die Datei gespeichert ist)
    2. Übersetzungsproblem dynamische Tabs lösen.

**Block B: Kernkomponenten Refactoring**
    3. State Management Konsolidierung (`StateManager.ts` vs. `StateManagerLegacy.ts`).
    4. CalculatorModule Konsolidierung (`CalculatorModule.ts`, `V2`, `V3`).
    5. Refactoring `DOMHelper.ts` und UI-Interaktionen.

**Block C: Allgemeine Code-Qualität und Testabdeckung**
    6. Verbesserung der Testabdeckung (Allgemein).
    7. Code-Review und Konsistenz.
    8. Entfernung von Altlasten (Legacy Code).

**Block D: Dokumentation und Abschluss**
    9. Technische Dokumentation aktualisieren.
    10. Finale Testrunde und Stabilitätsprüfung.