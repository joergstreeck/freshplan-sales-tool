# FreshPlan Sales Tool 🥗

Ein professionelles Verkaufstool für die Lebensmittelbranche mit interaktivem Rabatt-Demonstrator, Kundenverwaltung und automatischer Angebotserstellung.

![License](https://img.shields.io/badge/license-MIT-blue.svg)
![Version](https://img.shields.io/badge/version-1.0.0-green.svg)
![HTML5](https://img.shields.io/badge/HTML5-E34F26?logo=html5&logoColor=white)
![JavaScript](https://img.shields.io/badge/JavaScript-F7DF1E?logo=javascript&logoColor=black)
[![E2E Tests](https://github.com/joergstreeck/freshplan-sales-tool/actions/workflows/mini-e2e.yml/badge.svg)](https://github.com/joergstreeck/freshplan-sales-tool/actions/workflows/mini-e2e.yml)
[![Backup Status](https://github.com/joergstreeck/freshplan-sales-tool/workflows/Backup%20Smoke%20Tests/badge.svg)](https://github.com/joergstreeck/freshplan-sales-tool/actions/workflows/backup-smoke.yml)
[![Smoke Tests](https://github.com/joergstreeck/freshplan-sales-tool/workflows/Smoke%20Tests/badge.svg)](https://github.com/joergstreeck/freshplan-sales-tool/actions/workflows/smoke-tests.yml)

## 🚀 Features

### Rabatt-Demonstrator
- **Interaktive Berechnung** mit Echtzeit-Visualisierung
- **Rabattstaffeln**: 3-10% Basis + Frühbucher (bis 3%) + Selbstabholung (2%)
- **Szenario-Vergleiche** zur Optimierung der Bestellstrategie
- **Kombi-Bestellungen** ab 30.000€ mit flexibler Aufteilung

### Kundendatenerfassung
- **Einzelstandorte & Ketten** mit unterschiedlichen Workflows
- **Branchenspezifische Felder** für:
  - Hotels (Zimmer, Auslastung, Restaurant)
  - Pflegeheime (Bewohner, Mahlzeiten, Diäten)
  - Krankenhäuser (Betten, Privatpatienten, Cafeteria)
  - Betriebsrestaurants (Mitarbeiter, Essensteilnehmer)
  - Restaurants/Caterer
- **Vending-Konzept** Integration
- **Umsatzpotenzial-Kalkulator** mit branchenspezifischen Formeln

### Kundenprofil & Verkaufsanalyse
- Automatische **Potenzialanalyse**
- Branchenspezifische **Verkaufsstrategien**
- **Pain Points** und Chancen-Identifikation
- **Empfohlene nächste Schritte** im Verkaufsprozess

### Angebotserstellung
- **PDF-Generierung** mit jsPDF
- Personalisierte **Kalkulationsbeispiele**
- **12 Monate Preisstabilität** als USP
- Modulare **Dokumentauswahl**

## 🛠️ Installation

### Standalone (Keine Installation erforderlich)
1. Datei `index.html` herunterladen
2. In einem modernen Browser öffnen (Chrome, Firefox, Safari, Edge)
3. Fertig! Das Tool läuft vollständig im Browser

### Für Entwickler
```bash
# Repository klonen
git clone https://github.com/IhrUsername/freshplan-sales-tool.git

# In das Verzeichnis wechseln
cd freshplan-sales-tool

# Mit einem lokalen Server starten (optional)
python -m http.server 8000
# oder
npx serve
```

## 📋 Systemanforderungen

- Moderner Webbrowser (Chrome 90+, Firefox 88+, Safari 14+, Edge 90+)
- JavaScript aktiviert
- Keine Server-Installation notwendig
- Läuft auch offline

## 🔧 Technologie-Stack

- **Frontend**: Vanilla JavaScript (ES6+)
- **Styling**: Custom CSS mit CSS Variables
- **PDF-Generierung**: jsPDF mit AutoTable Plugin
- **Datenspeicherung**: In-Memory Storage (Claude.ai-optimiert)
- **Mehrsprachigkeit**: Integriertes DE/EN System

## 📚 Verwendung

### 1. Rabatt-Demo
- Bestellwert mit Schieberegler einstellen
- Vorlaufzeit für Frühbucherrabatt wählen
- Selbstabholung für zusätzliche 2% aktivieren
- Ergebnis in Echtzeit verfolgen

### 2. Kundendaten erfassen
- Kundentyp wählen (Einzelstandort/Kette)
- Stammdaten eingeben
- Branche auswählen für spezifische Felder
- Automatische Speicherung aktiv

### 3. Kundenprofil analysieren
- Wird automatisch aus Kundendaten generiert
- Zeigt Potenziale und Strategien
- Hilft bei der Verkaufsargumentation

### 4. Angebot erstellen
- Verkäuferinformationen eingeben
- Vertragslaufzeit festlegen
- Dokumente auswählen
- PDF generieren und versenden

## 🔐 Datenschutz & Sicherheit

- **Keine Serververbindung**: Alle Daten bleiben im Browser
- **Kein Tracking**: Keine Analytics oder externe Scripts
- **In-Memory Storage**: Daten werden nicht persistent gespeichert
- **DSGVO-konform**: Keine personenbezogenen Daten werden übertragen

## 📄 Dokumentation

### API-Referenz
Das Tool bietet folgende Hauptfunktionen:

```javascript
// Rabattberechnung
updateDiscountCalculation()

// Kundendaten speichern
saveCustomerData()

// PDF generieren
generatePDF()

// Profil erstellen
generateCustomerProfile()
```

### Konfiguration
Anpassbare Elemente:
- Rabattstaffeln in `updateDiscountCalculation()`
- Branchenspezifische Kalkulationen in `calculate[Industry]Potential()`
- Übersetzungen im `translations` Objekt

## 🤝 Beitragen

Wir freuen uns über Beiträge! Bitte beachten Sie:

1. Fork des Repositories erstellen
2. Feature Branch erstellen (`git checkout -b feature/AmazingFeature`)
3. Änderungen committen (`git commit -m 'Add some AmazingFeature'`)
4. Branch pushen (`git push origin feature/AmazingFeature`)
5. Pull Request öffnen

## 📝 Lizenz

Dieses Projekt ist unter der MIT-Lizenz lizenziert - siehe [LICENSE](LICENSE) Datei für Details.

## 👥 Autoren

- **Freshfoodz GmbH** - *Initial work*

## 🙏 Danksagungen

- jsPDF Team für die PDF-Bibliothek
- Alle Mitwirkenden und Tester

## 📞 Support

Bei Fragen oder Problemen:
- Issue auf GitHub erstellen
- E-Mail an: support@freshfoodz.de

---

**FreshPlan** - Eine faire Partnerschaft für Ihren Erfolg! 🚀
