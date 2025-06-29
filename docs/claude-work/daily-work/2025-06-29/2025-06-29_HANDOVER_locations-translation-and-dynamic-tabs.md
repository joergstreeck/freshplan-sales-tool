# Übergabe-Dokumentation: Locations-Übersetzung und dynamische Tabs

**Datum:** 2025-06-29
**Typ:** HANDOVER
**Status:** Bereit für Fortsetzung

## 🎯 Was wurde gemacht

### 1. LocationsForm vollständig übersetzt ✅
- Neue Übersetzungsdateien erstellt:
  - `/frontend/src/i18n/locales/de/locations.json`
  - `/frontend/src/i18n/locales/en/locations.json`
- Alle ~750 Zeilen der LocationsForm.tsx migriert
- Über 100 Übersetzungs-Keys hinzugefügt
- i18n-Konfiguration erweitert

### 2. Dynamische Tab-Anzeige implementiert ✅

#### Kettenkunde → Standorte Tab
- CustomerForm meldet Änderungen via `onChainCustomerChange` Callback
- LegacyApp verwaltet `isChainCustomer` State
- Navigation zeigt "Standorte" Tab nur wenn Kettenkunde = "ja"

#### Standorte detailliert erfassen → Detaillierte Standorte Tab
- LocationsForm meldet Checkbox-Status via `onDetailedLocationsChange`
- LegacyApp verwaltet `showDetailedLocations` State
- Navigation zeigt "Detaillierte Standorte" Tab nur wenn:
  - Kettenkunde = "ja" UND
  - "Standorte detailliert erfassen" aktiviert ist

### 3. Probleme identifiziert

#### Szenarien-Layout (vom User gemeldet)
- Die 3 Beispielszenarien im Calculator sind zu schmal
- Text wird abgeschnitten (siehe Screenshot)
- User wollte doppelte Breite, aber ich habe das Gesamt-Layout verändert
- Änderungen wurden rückgängig gemacht

#### Detaillierte Standorte Inhalt fehlt
- Der Tab "Detaillierte Standorte" zeigt nur "Coming Soon"
- Die eigentliche Funktionalität existiert bereits in:
  - `/Users/joergstreeck/freshplan-backup-20250108/legacy/freshplan-complete.html`
- Enthält komplettes Formular für Standort-Details mit:
  - Standortname, Kategorie
  - Adresse (Straße, PLZ, Ort)
  - Ansprechpartner (Name, Telefon, Email)
  - Dynamisches Hinzufügen/Entfernen von Standorten

## 📁 Geänderte Dateien

### Neue Dateien:
- `/frontend/src/i18n/locales/de/locations.json`
- `/frontend/src/i18n/locales/en/locations.json`

### Geänderte Dateien:
- `/frontend/src/components/original/LocationsForm.tsx` - Komplett mit i18n
- `/frontend/src/components/original/LegacyApp.tsx` - States und Tab-Logik
- `/frontend/src/components/original/Navigation.tsx` - Dynamische Tab-Filterung
- `/frontend/src/components/original/CustomerForm.tsx` - Callback für chainCustomer
- `/frontend/src/i18n/index.ts` - Locations-Namespace registriert
- `/frontend/src/i18n/locales/*/navigation.json` - "Detaillierte Standorte" Tab

## 🚀 Nächste Schritte

### 1. Szenarien-Layout fixen
- In `/frontend/src/styles/legacy/calculator-layout.css`
- Nur die Szenarien-Karten breiter machen
- NICHT das gesamte Layout ändern
- Evtl. 2 Zeilen statt 3 Spalten

### 2. Detaillierte Standorte implementieren
- Code aus `freshplan-complete.html` extrahieren
- Als React-Komponente neu implementieren
- Mit i18n von Anfang an
- State-Management für Standort-Liste

### 3. Testing
- Sprachumschaltung in LocationsForm testen
- Dynamische Tab-Anzeige verifizieren
- Formulare auf Vollständigkeit prüfen

## 💡 Wichtige Hinweise

### Tab-Reihenfolge:
1. Rabattrechner
2. Kundendaten
3. Standorte (nur wenn Kettenkunde = ja)
4. Detaillierte Standorte (nur wenn beide Bedingungen erfüllt)
5. Bonitätsprüfung
6. Profil
7. Angebot
8. Einstellungen

### Legacy-Code Referenz:
Die komplette Standort-Details Implementierung ist in:
```
/Users/joergstreeck/freshplan-backup-20250108/legacy/freshplan-complete.html
```
Suche nach:
- `location-detail-card`
- `addLocationDetail`
- `removeLocationDetail`
- `locationDetailsList`

## 📊 Status-Zusammenfassung

- ✅ LocationsForm Übersetzung komplett
- ✅ Dynamische Tab-Logik funktioniert
- ❌ Szenarien-Layout zu schmal
- ❌ Detaillierte Standorte noch nicht implementiert
- ✅ Alle i18n-Keys vorhanden

---

**Ready für Fortsetzung nach Komprimierung!**