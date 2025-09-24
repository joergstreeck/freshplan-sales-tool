# Field Catalog Documentation

## Übersicht

Der Field Catalog definiert alle verfügbaren Felder für das dynamische Customer Management System (FC-005).

## Struktur

### Base Fields

Die 10 MVP Basis-Felder, die für alle Kunden gelten:

1. `companyName` - Firmenname
2. `legalForm` - Rechtsform
3. `industry` - Branche
4. `chainCustomer` - Filialunternehmen (ja/nein)
5. `street` - Straße und Hausnummer
6. `postalCode` - PLZ
7. `city` - Ort
8. `contactName` - Ansprechpartner
9. `contactEmail` - E-Mail
10. `contactPhone` - Telefon

### Industry Specific Fields

Zusätzliche Felder je nach ausgewählter Branche:

- **Hotel**: Sterne-Kategorie, Anzahl Zimmer
- **Krankenhaus**: Krankenhaustyp, Anzahl Betten
- **Seniorenresidenz**: Pflegestufen, Anzahl Bewohner
- **Restaurant**: Küchenstil, Anzahl Sitzplätze
- **Betriebsrestaurant**: Mahlzeiten pro Tag, Betriebstage

## Field Types

- `text` - Einfaches Textfeld
- `select` - Dropdown mit vordefinierten Optionen
- `multiselect` - Mehrfachauswahl
- `email` - E-Mail mit Validierung
- `number` - Numerisches Feld mit min/max
- `range` - Slider für Wertebereiche

## Validation Rules

- `germanPostalCode` - 5-stellige deutsche PLZ
- `germanPhone` - Deutsche Telefonnummer mit flexiblem Format
- `email` - Standard E-Mail-Validierung

## Grid System

Responsive Layout mit Material-UI Grid:

- `xs`: Mobile (< 600px)
- `sm`: Tablet (600px - 960px)
- `md`: Desktop (≥ 960px)

## Special Features

### Conditional Logic

Das Feld `chainCustomer` triggert bei "ja" den Wizard-Step für Standorte:

```json
"triggerWizardStep": {
  "when": "ja",
  "step": "locations"
}
```

### Help Text

Optionale Hilfestellungen für bessere UX:

- `placeholder` - Beispielwerte
- `helpText` - Erklärungen unter dem Feld

## Verwendung

```typescript
import fieldCatalog from './fieldCatalog.json';

// Base Fields für Customer
const customerFields = fieldCatalog.customer.base;

// Industry-spezifische Felder
const hotelFields = fieldCatalog.customer.industrySpecific.hotel;

// Validation Rules
const postalCodeValidation = fieldCatalog.validationRules.germanPostalCode;
```
