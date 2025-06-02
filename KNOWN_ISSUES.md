# Known Issues - FreshPlan Sales Tool

## Stand: 2. Juni 2025

### 1. Übersetzungen bei dynamischen Elementen

**Problem:**  
Dynamisch generierte HTML-Elemente (z.B. neue Standorte im Standort-Details Tab) zeigen Übersetzungsschlüssel statt übersetzten Text.

**Beispiel:**  
Statt "Standortname" wird "location.name" angezeigt.

**Ursache:**  
Die Übersetzungsfunktion läuft nur einmal beim Seitenstart. Nachträglich hinzugefügte Elemente werden nicht automatisch übersetzt.

**Auswirkung:**  
- Rein kosmetisch
- Funktionalität ist nicht beeinträchtigt
- Betrifft nur dynamisch erstellte Formulare

**Workaround:**  
Seite neu laden, dann werden alle Texte korrekt übersetzt.

**Lösung:**  
Wird in Phase 2 behoben durch:
- Refactoring des Übersetzungssystems
- Automatische Übersetzung bei DOM-Änderungen
- Oder: Direkte Übersetzung beim Erstellen der Elemente

### 2. Placeholder-Tabs ohne Funktionalität

**Betrifft:**
- Profil-Tab
- Angebot-Tab (PDF-Generierung)
- Einstellungen-Tab

**Status:**  
Diese Tabs waren auch im Original nur Platzhalter. Keine Regression.

**Geplant für Phase 2:**
- PDF-Generierung mit jsPDF
- Benutzerprofil-Verwaltung
- Einstellungen für Standardwerte

---

## Behobene Issues

### ✅ Logo-Einbettung (behoben am 2.6.2025)
- Problem: Base64-String zu groß für API
- Lösung: Dateipfad in Development, Base64 nur im Build

### ✅ Rabattberechnung Anzeige (behoben am 2.6.2025)
- Problem: Klinikgruppe zeigte 13% statt 12%
- Lösung: Statischen Text korrigiert

### ✅ Single-File Build (behoben am 2.6.2025)
- Problem: JavaScript startete nicht
- Lösung: Legacy-Script korrekt integriert