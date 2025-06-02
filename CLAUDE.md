# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

# FreshPlan Sales Tool - Migrationskontext & Arbeitsanweisungen

## 0. Kommunikationssprache

- **Bitte antworte und kommuniziere während unserer gesamten Interaktion ausschließlich auf Deutsch.**

## 1. Primäre Referenzdatei ("Goldene Referenz")

- **Datei:** `/Users/joergstreeck/freshplan-sales-tool/GOLDEN_REFERENCE.html`
  - Dies ist eine exakte Kopie der ursprünglichen, funktionierenden `freshplan-complete.html`.
  - Sie ist die **alleinige und verbindliche Vorlage** für HTML-Struktur, CSS-Styling und die initiale JavaScript-Logik für Phase 1 der Migration (1:1-Portierung).
- **GitHub-Ursprung (Information für Jörg Streeck):** Der Inhalt dieser Datei stammt aus dem ursprünglichen GitHub-Repository-Stand des Projekts `https://github.com/joergstreeck/freshplan-sales-tool`. Du, Claude Code, hast vollen Zugriff auf dieses Repository, um bei Bedarf den genauen Commit/Branch für die `GOLDEN_REFERENCE.html` und zugehörige Original-Assets zu verifizieren/abzurufen.

## 2. Zielarchitektur

- **Basis:** Vite mit TypeScript.
- **Einstiegspunkt HTML:** `index.html` im Root-Verzeichnis.
- **Einstiegspunkt TypeScript:** `src/main.ts`.
- **Struktur:** Modulare Aufteilung gemäß dem `src/`-Verzeichnis (Module, Services, Store, Utils etc.).
- **CSS:** Externalisiert nach `src/styles/original-imported-styles.css` (1:1 Kopie des CSS aus `GOLDEN_REFERENCE.html`) und importiert in `src/main.ts`.
- **Build für Standalone:** `npm run build:standalone` erzeugt eine `freshplan-complete.html` mittels `vite-plugin-singlefile`.

## 3. Aktueller Projektstand (Beginn Logo-Einbettung in Phase 1b)

- **Phase 1a (HTML/CSS 1:1-Portierung):** Abgeschlossen und von Jörg Streeck visuell für alle Tabs als korrekt bestätigt.
- **Phase 1b (Original-JavaScript 1:1 als Wrapper in `src/legacy-script.ts` portiert):** Weitgehend abgeschlossen. Folgende Bereiche wurden im `dev`-Modus positiv getestet und entsprechen dem Verhalten der `GOLDEN_REFERENCE.html`:
    - Rabattrechner-Logik.
    - Kundendaten-Tab Kernfunktionen (inkl. sofortiger Neukunden-Warnung und Pflichtfeld-Validierung).
    - Standort-Tab branchenspezifische Felder und Gesamtanzahl-Logik für alle 5 Branchen.
    - Standort-Details-Tab Funktionalität (Hinzufügen, Löschen mit Bestätigung, Synchronisationswarnung).
    - Bonitätsprüfung-Tab (Demo-Verhalten als für Phase 1b ausreichend akzeptiert).
    - LocalStorage-Persistenz für Rabattrechner, Kundendaten, Standortübersicht und Standort-Details als korrekt implementiert analysiert und von Jörg Streeck positiv getestet.
    - Tab-Navigation und Sprachumschaltung (mit bekannter Lücke bei dynamischen Standort-Details, die dem Originalverhalten entspricht).
    - Die Tabs "Profil", "Angebot", "Einstellungen" sind reine Platzhalter (gemäß Original).

## 4. Unmittelbare nächste Aufgabe

- **Einbettung des optimierten Logos:** Das optimierte Logo `Freshfoodzlogo.png` (ca. 19KB, Pfad im Projekt z.B. `public/assets/images/Freshfoodzlogo.png` oder `/Users/joergstreeck/freshplan-sales-tool/dist-standalone/assets/images/Freshfoodzlogo.png` - bitte den korrekten Pfad für die Kodierung verwenden) soll als Base64-String in die `index.html`-Vorlage eingebettet werden. Ersetze dabei den aktuellen Pfad oder Platzhalter im `src`-Attribut des Logo-`<img>`-Tags.
- **Ziel:** Eine via `npm run build:standalone` generierte `freshplan-complete.html`, die das Logo korrekt anzeigt und ansonsten voll funktionsfähig ist (entsprechend dem Abschluss von Phase 1b).

## 5. Langfristiger Migrationsplan

- Der 10-Punkte-Plan (strikte 1:1-Portierung in Phase 1, dann schrittweise Modernisierung des Codes aus `legacy-script.ts` zu sauberen TypeScript-Modulen in Phase 2) bleibt gültig.

## 6. Wichtige Referenzdokumente im Projekt

- `FEATURE_CHECKLIST.md`
- `MIGRATION_STATUS.md`
- `Anlage 1 FreshPlan Rabattsystem fu_r Endkunden.pdf`
- `FRESH-FOODZ_CI.md`