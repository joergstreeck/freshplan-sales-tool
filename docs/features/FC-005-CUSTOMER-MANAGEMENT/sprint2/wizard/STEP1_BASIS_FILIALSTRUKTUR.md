# 📋 Step 1: Basis & Filialstruktur

**Sprint:** 2  
**Component:** CustomerOnboardingWizard  
**Status:** 🆕 Neu definiert  

---

## 📍 Navigation
**← Zurück:** [Wizard Struktur V2](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/WIZARD_STRUCTURE_V2.md)  
**→ Weiter:** [Step 2: Angebot & Pain Points](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/wizard/STEP2_ANGEBOT_PAINPOINTS.md)  
**⚙️ Implementierung:** [Field Catalog Extension](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/implementation/FIELD_CATALOG_EXTENSION.md)

---

## 🎯 Zweck

Erfassung der Basisdaten UND sofortige Potenzial-Einschätzung bei Ketten.

---

## 🖼️ UI Design

```
┌─────────────────────────────────────┐
│ Schritt 1: Unternehmensdaten        │
├─────────────────────────────────────┤
│                                     │
│ Kundennummer: [1234]                │
│ Firmenname: [Marriott Hotels GmbH]* │
│ Rechtsform: [GmbH ▼]*               │
│ Branche: [Hotel ▼]*                 │
│                                     │
│ 📍 Adresse Hauptstandort:           │
│ Straße: [___________] Nr: [__]     │
│ PLZ: [_____] Ort: [____________]   │
│                                     │
│ ─────────────────────────────────── │
│                                     │
│ 🏢 Filialunternehmen? [Ja ▼]*      │
│                                     │
│ 📍 Ihre Standorte in Europa:        │
│ Deutschland:    [28]                │
│ Österreich:     [8]                 │
│ Schweiz:        [5]                 │
│ Rest-EU:        [4]                 │
│                                     │
│ 💡 45 Standorte = Rahmenvertrag     │
│    mit großem Potenzial!            │
│                                     │
│ ─────────────────────────────────── │
│                                     │
│ 💰 Geschäftsmodell:                 │
│ ○ Privat finanziert                 │
│ ● Public/Kasse                      │
│ ○ Mischmodell                       │
│                                     │
│ ℹ️ Info: Bei Ketten erfassen wir    │
│ im nächsten Schritt die einzelnen   │
│ Standorte detailliert.              │
│                                     │
│ [Zurück] [Weiter →]                 │
└─────────────────────────────────────┘
```

---

## 📊 Neue Felder

### Filialstruktur (bei isChainCustomer = true)
```typescript
{
  totalLocationsEU: number;      // Gesamt
  locationsGermany: number;      // Deutschland
  locationsAustria: number;      // Österreich
  locationsSwitzerland: number;  // Schweiz
  locationsRestEU: number;       // Rest-EU
  expansionPlanned?: boolean;    // Wachstumspotenzial
  decisionStructure?: 'central' | 'decentral' | 'mixed';
}
```

### Geschäftsmodell
```typescript
{
  primaryFinancing: 'private' | 'public' | 'mixed';
  priceSegment?: 'budget' | 'mid' | 'premium';
  decisionCriteria?: ('price' | 'quality' | 'service' | 'innovation')[];
}
```

### Adresse Hauptstandort
```typescript
{
  street: string;
  houseNumber: string;
  postalCode: string;
  city: string;
  // Wird als Standort 1 gespeichert
}
```

---

## 🧮 Live-Potenzial-Indikator

```typescript
const showPotentialHint = (locations: number) => {
  if (locations >= 50) return "Großkunden-Potenzial!";
  if (locations >= 20) return "Rahmenvertrag möglich";
  if (locations >= 5) return "Interessante Größe";
  return "";
};
```

---

## ✅ Validierung

- Firmenname: Pflicht
- Branche: Pflicht (bestimmt spätere Felder)
- Filialunternehmen: Pflicht
- Bei "Ja": Mindestens 1 Standort

---

## 🔗 Weiterführende Links

**Nächster Step:** [Step 2: Angebot & Pain Points](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/wizard/STEP2_ANGEBOT_PAINPOINTS.md)  
**Field Definitions:** [Field Catalog Extension](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/implementation/FIELD_CATALOG_EXTENSION.md)