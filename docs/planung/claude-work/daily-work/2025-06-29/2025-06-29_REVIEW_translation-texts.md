# Übersetzungstexte im FreshPlan Frontend

**Datum:** 2025-06-29
**Kategorie:** REVIEW
**Bereich:** Frontend Internationalisierung

## Zusammenfassung

Ich habe alle TSX-Dateien im Frontend nach Texten durchsucht, die übersetzt werden müssen. Die Texte sind nach Komponenten/Features gruppiert.

## Gefundene Texte nach Komponenten

### 1. CalculatorLayout.tsx (Rabattrechner)

#### Hauptüberschriften
- "FreshPlan Rabattrechner"

#### Labels und UI-Elemente
- "Bestellwert"
- "Vorlaufzeit"
- "Tage"
- "Abholung (Mindestbestellwert: 5.000€ netto)"
- "Basisrabatt"
- "Frühbucher"
- "Abholung"
- "Gesamtrabatt"
- "Ersparnis"
- "Endpreis"
- "Maximaler Gesamtrabatt: 15%"

#### Informationsbereich
- "Rabattsystem Details" (implizit)
- "Basisrabatt"
- "ab"
- "Frühbucherrabatt"
- "ab 10 Tage"
- "ab 15 Tage"
- "ab 30 Tage"
- "Kettenkundenregelung"
- "Für Unternehmen mit mehreren Standorten (z.B. Hotel- oder Klinikgruppen):"
- "Option A:"
- "Bestellungen verschiedener Standorte innerhalb einer"
- "Woche werden zusammengerechnet"
- "Option B:"
- "Zentrale Bestellung mit Mehrfachauslieferung"

#### Beispielszenarien
- "Beispielszenarien"
- "Hotelkette"
- "Klinikgruppe"
- "Restaurant"
- "Rabatt"

### 2. CustomerForm.tsx (Kundendatenformular)

#### Hauptüberschrift
- "Kundendaten erfassen"

#### Sektionsüberschriften
- "Grunddaten"
- "Adressdaten"
- "Ansprechpartner"
- "Geschäftsdaten"
- "Zusatzinformationen"

#### Formularfelder und Labels
- "Firmenname*"
- "Rechtsform*"
- "Bitte wählen"
- "Kundentyp*"
- "Neukunde"
- "Bestandskunde"
- "Branche*"
- "Hotel"
- "Klinik"
- "Seniorenresidenz"
- "Betriebsrestaurant"
- "Restaurant"
- "Kettenkunde"
- "Nein"
- "Ja"
- "Kundennummer (intern)"
- "Straße und Hausnummer*"
- "PLZ*"
- "Ort*"
- "Name*"
- "Position"
- "Telefon*"
- "E-Mail*"
- "Erwartetes Jahresvolumen (€)*"
- "z.B. 500.000"
- "Zahlungsart*"
- "Vorkasse"
- "Barzahlung"
- "Rechnung (30 Tage netto)"
- "Notizen"
- "Interne Notizen zum Kunden..."
- "Freifeld 1"
- "Freifeld 2"

### 3. Navigation.tsx

#### Tab-Labels
- "Rabattrechner"
- "Kundendaten"
- "Standorte"
- "Bonitätsprüfung"
- "Profil"
- "Angebot"
- "Einstellungen"

### 4. Header.tsx

#### Logo und Tagline
- "FreshPlan"
- "So einfach, schnell und lecker!"

#### Buttons und Sprachen
- "Deutsch"
- "English"
- "Formular leeren"
- "Speichern"

### 5. ErrorBoundary.tsx

#### Fehlermeldungen
- "Etwas ist schiefgelaufen"
- "Ein unerwarteter Fehler ist aufgetreten."
- "Fehlerdetails (nur in Entwicklung)"
- "Seite neu laden"

### 6. UserTable.tsx (Benutzerverwaltung)

#### Überschriften und Beschreibungen
- "Benutzerverwaltung"
- "Verwalten Sie Benutzer und deren Rollen"
- "Neuer Benutzer"

#### Such- und Filter-UI
- "Benutzer suchen..."
- "Suchen"
- "Zurücksetzen"

#### Tabellenkopf
- "Benutzername"
- "Name"
- "E-Mail"
- "Rollen"
- "Status"
- "Aktionen"

#### Status und Aktionen
- "Aktiv"
- "Inaktiv"
- "Bearbeiten"
- "Deaktivieren"
- "Aktivieren"
- "Löschen"

#### Meldungen
- "Lade Benutzer..."
- "Keine Benutzer gefunden."
- "Fehler beim Laden der Benutzer:"
- "Unbekannter Fehler"
- 'Benutzer "{username}" wirklich löschen?'

### 7. UserForm.tsx (Benutzerformular)

#### Überschriften
- "Benutzer bearbeiten"
- "Neuen Benutzer erstellen"
- "Bearbeiten Sie die Benutzerdaten und Rollen."
- "Geben Sie die Daten für den neuen Benutzer ein."

#### Formularfelder
- "Benutzername *"
- "z.B. john.doe"
- "E-Mail *"
- "john.doe@example.com"
- "Vorname *"
- "John"
- "Nachname *"
- "Doe"
- "Rollen *"
- "Benutzer aktiviert"

#### Buttons
- "Abbrechen"
- "Speichern"
- "Erstellen"

### 8. CalculatorForm.tsx (Rechner-Formular)

#### Überschriften
- "Rabatt-Kalkulator"
- "Berechnen Sie Ihren individuellen FreshPlan-Rabatt"
- "Szenario:"

#### Formularfelder
- "Bestellwert (€)"
- "z.B. 25000"
- "Netto-Bestellwert ohne Rabatt (0 - 1.000.000 €)"
- "Vorlaufzeit (Tage)"
- "z.B. 14"
- "Tage zwischen Bestellung und gewünschter Lieferung (0 - 365)"
- "Selbstabholung"
- "Abholung am FreshPlan-Standort (+2% Rabatt ab 5.000€)"
- "Kettenkunde"
- "Mehrere Standorte oder Filialen (andere Vorteile verfügbar)"

#### Buttons und Meldungen
- "Berechnung läuft..."
- "Rabatt berechnen"
- "Fehler bei der Berechnung:"

### 9. ScenarioSelector.tsx

#### Überschriften
- "Vordefinierte Szenarien"
- "Wählen Sie ein typisches Bestellszenario oder passen Sie die Werte manuell an"

#### Szenario-Details
- "Aktiv"
- "Bestellwert:"
- "Vorlaufzeit:"
- "Abholung:"
- "Kettenkunde:"
- "Ja"
- "Nein"
- "Eigene Werte eingeben"

### 10. App.tsx (Hauptseite)

#### Überschriften
- "FreshPlan 2.0"
- "Ihr digitales Verkaufstool"

#### Karten-Titel und Beschreibungen
- "FreshPlan Verkaufstool"
- "Komplette Verwaltung für Kunden und Angebote"
- "Kundendaten, Standorte, Kalkulator - Alles in einem Tool"
- "FreshPlan Tool öffnen"
- "Benutzerverwaltung"
- "Verwalten Sie Benutzer und Zugriffsrechte"
- "Moderne Benutzerverwaltung mit Rollen und Rechten"
- "Benutzerverwaltung öffnen"
- "API Verbindungstest"
- "Prüfen Sie die Backend-Verbindung"
- "Testen Sie die Verbindung zum Backend-Server"
- "Verbindung testen"
- "Zähler Demo"
- "Einfache Demonstration der Interaktivität"
- "Zählerstand:"
- "Zähler erhöhen"

#### Fehlermeldungen
- "Error: Not authenticated. Please login first."
- "Error:"
- "An unknown error occurred"

## Zusammenfassung

Insgesamt wurden **über 150 eindeutige Texte** gefunden, die übersetzt werden müssen. Die Texte verteilen sich auf folgende Hauptbereiche:

1. **Rabattrechner** (CalculatorLayout, CalculatorForm, ScenarioSelector)
2. **Kundenverwaltung** (CustomerForm)
3. **Benutzerverwaltung** (UserTable, UserForm)
4. **Navigation und Header**
5. **Allgemeine UI-Elemente** (Buttons, Fehlermeldungen, Status)
6. **Hauptseite** (App.tsx)

### Empfehlungen für die Übersetzung

1. **Konsistente Terminologie**: Begriffe wie "Rabatt", "Bestellwert", "Vorlaufzeit" sollten konsistent übersetzt werden
2. **Platzhalter beachten**: Texte wie `z.B. 25000` oder `{username}` enthalten Platzhalter
3. **Kontextabhängige Übersetzungen**: "Ja"/"Nein" könnte je nach Kontext unterschiedlich übersetzt werden
4. **Länge berücksichtigen**: Deutsche Texte sind oft länger als englische - UI-Layout könnte angepasst werden müssen
5. **Fehlermeldungen**: Sollten klar und hilfreich formuliert sein

### Nächste Schritte

1. Erstellen einer i18n-Struktur mit Namespaces für jeden Bereich
2. Implementierung eines Übersetzungssystems (z.B. react-i18next)
3. Extraktion aller Texte in Übersetzungsdateien
4. Erstellung der englischen Übersetzungen
5. Testing mit beiden Sprachen