# 📝 Field Catalog - Vollständige Definition

**Sprint:** 2  
**Status:** 🆕 Komplette Spezifikation  
**Datei:** `/frontend/src/features/customers/data/fieldCatalog.json`  

---

## 📍 Navigation
**← Zurück:** [Field Catalog Extension](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/implementation/FIELD_CATALOG_EXTENSION.md)  
**→ Teil 2:** [Field Catalog Services](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/implementation/FIELD_CATALOG_SERVICES.md)  
**→ Teil 3:** [Field Catalog Contacts](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/implementation/FIELD_CATALOG_CONTACTS.md)  
**↑ Sprint 2:** [Sprint 2 Overview](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/README.md)  

---

## 🎯 Übersicht der neuen Felder

### Step 1: Basis & Filialstruktur
- ✅ Filialstruktur (5 Felder)
- ✅ Geschäftsmodell (1 Feld)
- ✅ Erweiterte Basis-Felder (4 Felder)

### Step 2: Angebot & Pain Points
- ✅ Universelle Pain Points (6 Felder)
- ✅ Branchenspezifische Services
- ✅ Zusatzgeschäft (Vending/bonPeti)

### Step 3: Ansprechpartner
- ✅ Strukturierte Kontaktdaten
- ✅ Kommunikationspräferenzen
- ✅ Beziehungsaufbau

---

## 📋 Step 1: Basis & Filialstruktur

### Filialstruktur-Block
```json
{
  "chainStructure": {
    "totalLocationsEU": {
      "key": "totalLocationsEU",
      "label": "Standorte gesamt (EU)",
      "fieldType": "number",
      "required": false,
      "category": "chain",
      "displayCondition": "isChainCustomer === true",
      "validBranches": ["all"],
      "helpText": "Alle Standorte in Europa",
      "min": 1,
      "placeholder": "0",
      "sizeHint": "klein"
    },
    "locationsGermany": {
      "key": "locationsGermany",
      "label": "davon Deutschland",
      "fieldType": "number",
      "required": false,
      "category": "chain",
      "displayCondition": "isChainCustomer === true",
      "validBranches": ["all"],
      "placeholder": "0",
      "sizeHint": "klein"
    },
    "locationsAustria": {
      "key": "locationsAustria",
      "label": "davon Österreich",
      "fieldType": "number",
      "required": false,
      "category": "chain",
      "displayCondition": "isChainCustomer === true",
      "validBranches": ["all"],
      "placeholder": "0",
      "sizeHint": "klein"
    },
    "locationsSwitzerland": {
      "key": "locationsSwitzerland",
      "label": "davon Schweiz",
      "fieldType": "number",
      "required": false,
      "category": "chain",
      "displayCondition": "isChainCustomer === true",
      "validBranches": ["all"],
      "placeholder": "0",
      "sizeHint": "klein"
    },
    "locationsRestEU": {
      "key": "locationsRestEU",
      "label": "davon Rest-EU",
      "fieldType": "number",
      "required": false,
      "category": "chain",
      "displayCondition": "isChainCustomer === true",
      "validBranches": ["all"],
      "placeholder": "0",
      "sizeHint": "klein"
    },
    "expansionPlanned": {
      "key": "expansionPlanned",
      "label": "Expansion geplant",
      "fieldType": "checkbox",
      "required": false,
      "category": "chain",
      "displayCondition": "isChainCustomer === true",
      "validBranches": ["all"],
      "salesRelevance": "HIGH",
      "helpText": "Wachstumspotenzial für Rahmenverträge"
    },
    "decisionStructure": {
      "key": "decisionStructure",
      "label": "Entscheidungsstruktur",
      "fieldType": "select",
      "required": false,
      "category": "chain",
      "displayCondition": "isChainCustomer === true",
      "validBranches": ["all"],
      "options": [
        { "value": "central", "label": "Zentral (Hauptverwaltung)" },
        { "value": "decentral", "label": "Dezentral (je Standort)" },
        { "value": "mixed", "label": "Gemischt" }
      ],
      "salesRelevance": "HIGH"
    }
  }
}
```

### Geschäftsmodell
```json
{
  "businessModel": {
    "primaryFinancing": {
      "key": "primaryFinancing",
      "label": "Geschäftsmodell",
      "fieldType": "radio",
      "required": true,
      "category": "business",
      "displayCondition": "always",
      "validBranches": ["all"],
      "options": [
        { "value": "private", "label": "Privat finanziert" },
        { "value": "public", "label": "Öffentlich/Kasse" },
        { "value": "mixed", "label": "Mischmodell" }
      ],
      "helpText": "Bestimmt Produktempfehlungen und Preisgestaltung",
      "salesRelevance": "CRITICAL"
    }
  }
}
```

### Erweiterte Basis-Felder
```json
{
  "additionalBase": {
    "currentSupplier": {
      "key": "currentSupplier",
      "label": "Aktueller Lieferant",
      "fieldType": "text",
      "required": false,
      "category": "competitor",
      "displayCondition": "always",
      "validBranches": ["all"],
      "placeholder": "z.B. Apetito, eigene Küche",
      "salesRelevance": "HIGH"
    },
    "contractEndDate": {
      "key": "contractEndDate",
      "label": "Vertragsende",
      "fieldType": "date",
      "required": false,
      "category": "competitor",
      "displayCondition": "currentSupplier !== null && currentSupplier !== ''",
      "validBranches": ["all"],
      "helpText": "Wann endet der aktuelle Vertrag?",
      "salesRelevance": "CRITICAL"
    },
    "switchWillingness": {
      "key": "switchWillingness",
      "label": "Wechselbereitschaft",
      "fieldType": "select",
      "required": false,
      "category": "competitor",
      "displayCondition": "currentSupplier !== null && currentSupplier !== ''",
      "validBranches": ["all"],
      "options": [
        { "value": "high", "label": "Hoch - aktiv auf der Suche" },
        { "value": "medium", "label": "Mittel - offen für Alternativen" },
        { "value": "low", "label": "Niedrig - zufrieden" }
      ],
      "salesRelevance": "CRITICAL"
    },
    "decisionTimeline": {
      "key": "decisionTimeline",
      "label": "Entscheidungszeitpunkt",
      "fieldType": "select",
      "required": false,
      "category": "sales",
      "displayCondition": "always",
      "validBranches": ["all"],
      "options": [
        { "value": "immediate", "label": "Sofort (< 1 Monat)" },
        { "value": "q1_2025", "label": "Q1 2025" },
        { "value": "q2_2025", "label": "Q2 2025" },
        { "value": "q3_2025", "label": "Q3 2025" },
        { "value": "q4_2025", "label": "Q4 2025" },
        { "value": "2026", "label": "2026 oder später" },
        { "value": "unknown", "label": "Noch unklar" }
      ],
      "salesRelevance": "HIGH"
    }
  }
}
```

---

## ⚡ Display Conditions erklärt

```typescript
// displayCondition bestimmt, wann ein Feld angezeigt wird
// validBranches bestimmt, für welche Branchen es gilt

// Beispiele:
displayCondition: "always"                    // Immer anzeigen
displayCondition: "isChainCustomer === true"  // Nur bei Ketten
displayCondition: "industry === 'hotel'"      // Nur bei Hotels
displayCondition: "offersBreakfast === true"  // Wenn Frühstück angeboten

// Kombinationen:
displayCondition: "industry === 'hotel' && isChainCustomer === true"
```

---

**→ Weiter:** [Field Catalog Services](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/implementation/FIELD_CATALOG_SERVICES.md) für Step 2 Felder