# 📝 Field Catalog Erweiterung

**Sprint:** 2  
**File:** `/frontend/src/features/customers/data/fieldCatalog.json`  
**Status:** 🆕 Zu implementieren  

---

## 📍 Navigation
**← Zurück:** [Sprint 2 Overview](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/README.md)  
**↔️ Verwandt:** [Potential Calculation](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/implementation/POTENTIAL_CALCULATION.md)  
**🧪 Test:** [Field Catalog Tests](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/tests/FIELD_CATALOG_TESTS.md)

---

## 🎯 Neue Felder für Filialstruktur

### Auf Customer-Ebene (global)
```json
{
  "chainStructure": {
    "totalLocationsEU": {
      "key": "totalLocationsEU",
      "label": "Standorte gesamt (EU)",
      "fieldType": "number",
      "required": false,
      "category": "chain",
      "condition": "isChainCustomer === true",
      "helpText": "Alle Standorte in Europa",
      "min": 1,
      "placeholder": "0"
    },
    "locationsGermany": {
      "key": "locationsGermany",
      "label": "davon Deutschland",
      "fieldType": "number",
      "required": false,
      "category": "chain",
      "condition": "isChainCustomer === true",
      "placeholder": "0"
    },
    "locationsAustria": {
      "key": "locationsAustria",
      "label": "davon Österreich",
      "fieldType": "number",
      "required": false,
      "category": "chain",
      "condition": "isChainCustomer === true",
      "placeholder": "0"
    },
    "locationsSwitzerland": {
      "key": "locationsSwitzerland",
      "label": "davon Schweiz",
      "fieldType": "number",
      "required": false,
      "category": "chain",
      "condition": "isChainCustomer === true",
      "placeholder": "0"
    },
    "locationsRestEU": {
      "key": "locationsRestEU",
      "label": "davon Rest-EU",
      "fieldType": "number",
      "required": false,
      "category": "chain",
      "condition": "isChainCustomer === true",
      "placeholder": "0"
    }
  }
}
```

---

## 🏢 Geschäftsmodell-Felder

```json
{
  "businessModel": {
    "primaryFinancing": {
      "key": "primaryFinancing",
      "label": "Geschäftsmodell",
      "fieldType": "radio",
      "required": true,
      "category": "business",
      "options": [
        { "value": "private", "label": "Privat finanziert" },
        { "value": "public", "label": "Öffentlich/Kasse" },
        { "value": "mixed", "label": "Mischmodell" }
      ],
      "helpText": "Bestimmt Produktempfehlungen",
      "salesRelevance": "HIGH"
    }
  }
}
```

---

## 🍳 Angebotsstruktur (Branchenspezifisch)

### Hotels
```json
{
  "hotelServices": {
    "breakfast": {
      "key": "offersBreakfast",
      "label": "Frühstück",
      "fieldType": "checkbox",
      "category": "services"
    },
    "breakfastWarm": {
      "key": "breakfastWarm",
      "label": "Warme Komponenten",
      "fieldType": "checkbox",
      "condition": "offersBreakfast === true",
      "salesImpact": "high-margin"
    },
    "breakfastGuests": {
      "key": "breakfastGuestsPerDay",
      "label": "Gäste/Tag",
      "fieldType": "number",
      "condition": "offersBreakfast === true",
      "placeholder": "0"
    }
  }
}
```

---

## 👥 Pain Points (Universal)

```json
{
  "painPoints": {
    "staffingIssues": {
      "key": "hasStaffingIssues",
      "label": "Fachkräftemangel in der Küche",
      "fieldType": "checkbox",
      "category": "painPoints",
      "salesRelevance": "CRITICAL",
      "triggerProducts": ["convenience", "ready-to-serve"],
      "solutionText": "Cook&Fresh® - Keine Köche nötig!"
    },
    "qualityFluctuations": {
      "key": "hasQualityIssues",
      "label": "Schwankende Qualität",
      "fieldType": "checkbox",
      "category": "painPoints",
      "salesRelevance": "HIGH",
      "triggerProducts": ["standardized"],
      "solutionText": "Immer gleiche Premium-Qualität"
    }
  }
}
```

---

## 🔗 Integration in Wizard

1. **Step 1:** chainStructure + businessModel
2. **Step 2:** branchenServices + painPoints
3. **Step 3:** contacts (bereits vorhanden)

---

## ⚡ Conditional Logic

```typescript
// Beispiel für bedingte Anzeige
const showChainFields = formData.isChainCustomer === true;
const showBreakfastDetails = formData.offersBreakfast === true;
```

---

## 🔗 Weiterführende Links

**Potenzial-Logik:** [Potential Calculation](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/implementation/POTENTIAL_CALCULATION.md)  
**Pain Points:** [Pain Point Mapping](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/implementation/PAIN_POINT_MAPPING.md)