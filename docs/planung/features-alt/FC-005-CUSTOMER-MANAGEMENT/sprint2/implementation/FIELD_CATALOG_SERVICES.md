# 🍳 Field Catalog - Services & Pain Points

**Sprint:** 2  
**Status:** 🆕 Step 2 Felder  
**Kategorie:** Angebotsstruktur & Schmerzpunkte  

---

## 📍 Navigation
**← Teil 1:** [Field Catalog Complete](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/implementation/FIELD_CATALOG_COMPLETE.md)  
**→ Teil 3:** [Field Catalog Contacts](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/implementation/FIELD_CATALOG_CONTACTS.md)  
**↑ Sprint 2:** [Sprint 2 Overview](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/README.md)  

---

## 🏨 Hotel-spezifische Services

```json
{
  "hotelServices": {
    "offersBreakfast": {
      "key": "offersBreakfast",
      "label": "Frühstück angeboten",
      "fieldType": "checkbox",
      "category": "services",
      "displayCondition": "industry === 'hotel'",
      "validBranches": ["hotel"],
      "salesRelevance": "HIGH"
    },
    "breakfastWarm": {
      "key": "breakfastWarm",
      "label": "Warme Frühstückskomponenten",
      "fieldType": "checkbox",
      "category": "services",
      "displayCondition": "industry === 'hotel' && offersBreakfast === true",
      "validBranches": ["hotel"],
      "salesImpact": "high-margin",
      "triggerProducts": ["breakfast-convenience"]
    },
    "breakfastGuestsPerDay": {
      "key": "breakfastGuestsPerDay",
      "label": "Frühstücksgäste/Tag (Durchschnitt)",
      "fieldType": "number",
      "category": "services",
      "displayCondition": "industry === 'hotel' && offersBreakfast === true",
      "validBranches": ["hotel"],
      "placeholder": "0",
      "min": 0,
      "helpText": "Basis für Potenzialberechnung"
    },
    "offersLunch": {
      "key": "offersLunch",
      "label": "Mittagessen/Restaurant",
      "fieldType": "checkbox",
      "category": "services",
      "displayCondition": "industry === 'hotel'",
      "validBranches": ["hotel"]
    },
    "offersDinner": {
      "key": "offersDinner",
      "label": "Abendessen/À la carte",
      "fieldType": "checkbox",
      "category": "services",
      "displayCondition": "industry === 'hotel'",
      "validBranches": ["hotel"]
    },
    "offersRoomService": {
      "key": "offersRoomService",
      "label": "Roomservice",
      "fieldType": "checkbox",
      "category": "services",
      "displayCondition": "industry === 'hotel'",
      "validBranches": ["hotel"],
      "salesImpact": "premium-products"
    },
    "offersEvents": {
      "key": "offersEvents",
      "label": "Events/Bankette",
      "fieldType": "checkbox",
      "category": "services",
      "displayCondition": "industry === 'hotel'",
      "validBranches": ["hotel"],
      "salesImpact": "high-volume"
    },
    "eventCapacity": {
      "key": "eventCapacity",
      "label": "Max. Eventgäste",
      "fieldType": "number",
      "category": "services",
      "displayCondition": "industry === 'hotel' && offersEvents === true",
      "validBranches": ["hotel"],
      "placeholder": "0"
    },
    "roomCount": {
      "key": "roomCount",
      "label": "Anzahl Zimmer",
      "fieldType": "number",
      "category": "capacity",
      "displayCondition": "industry === 'hotel'",
      "validBranches": ["hotel"],
      "required": false,
      "min": 1
    },
    "averageOccupancy": {
      "key": "averageOccupancy",
      "label": "Durchschnittliche Auslastung (%)",
      "fieldType": "number",
      "category": "capacity",
      "displayCondition": "industry === 'hotel'",
      "validBranches": ["hotel"],
      "min": 0,
      "max": 100,
      "placeholder": "75"
    }
  }
}
```

---

## 🏥 Krankenhaus-spezifische Services

```json
{
  "hospitalServices": {
    "bedCount": {
      "key": "bedCount",
      "label": "Anzahl Betten",
      "fieldType": "number",
      "category": "capacity",
      "displayCondition": "industry === 'hospital'",
      "validBranches": ["hospital"],
      "required": false,
      "min": 1
    },
    "cateringSystem": {
      "key": "cateringSystem",
      "label": "Verpflegungssystem",
      "fieldType": "select",
      "category": "services",
      "displayCondition": "industry === 'hospital'",
      "validBranches": ["hospital"],
      "options": [
        { "value": "cook_serve", "label": "Cook & Serve" },
        { "value": "cook_chill", "label": "Cook & Chill" },
        { "value": "cook_freeze", "label": "Cook & Freeze" },
        { "value": "mixed", "label": "Mischsystem" }
      ],
      "salesRelevance": "HIGH"
    },
    "dietRequirements": {
      "key": "dietRequirements",
      "label": "Spezielle Diätanforderungen",
      "fieldType": "checkbox",
      "category": "services",
      "displayCondition": "industry === 'hospital'",
      "validBranches": ["hospital"],
      "helpText": "z.B. Diabetes, Allergien, Schluckstörungen",
      "triggerProducts": ["special-diets"]
    },
    "privatePatientShare": {
      "key": "privatePatientShare",
      "label": "Anteil Privatpatienten (%)",
      "fieldType": "number",
      "category": "business",
      "displayCondition": "industry === 'hospital'",
      "validBranches": ["hospital"],
      "min": 0,
      "max": 100,
      "salesImpact": "premium-potential"
    }
  }
}
```

---

## 👥 Universelle Pain Points

```json
{
  "painPoints": {
    "hasStaffingIssues": {
      "key": "hasStaffingIssues",
      "label": "Personalmangel/Fachkräftemangel",
      "fieldType": "checkbox",
      "category": "painPoints",
      "displayCondition": "always",
      "validBranches": ["all"],
      "required": false,
      "salesRelevance": "CRITICAL",
      "triggerProducts": ["convenience", "ready-to-serve"],
      "solutionText": "Cook&Fresh® - Keine Fachkräfte nötig!",
      "potentialMultiplier": 1.3
    },
    "hasQualityIssues": {
      "key": "hasQualityIssues",
      "label": "Schwankende Qualität",
      "fieldType": "checkbox",
      "category": "painPoints",
      "displayCondition": "always",
      "validBranches": ["all"],
      "required": false,
      "salesRelevance": "HIGH",
      "triggerProducts": ["standardized"],
      "solutionText": "Immer gleiche Premium-Qualität"
    },
    "hasFoodWasteIssues": {
      "key": "hasFoodWasteIssues",
      "label": "Zu viel Lebensmittelverschwendung",
      "fieldType": "checkbox",
      "category": "painPoints",
      "displayCondition": "always",
      "validBranches": ["all"],
      "required": false,
      "salesRelevance": "HIGH",
      "triggerProducts": ["long-shelf-life"],
      "solutionText": "40 Tage Haltbarkeit ohne Qualitätsverlust"
    },
    "hasCostPressure": {
      "key": "hasCostPressure",
      "label": "Kostendruck",
      "fieldType": "checkbox",
      "category": "painPoints",
      "displayCondition": "always",
      "validBranches": ["all"],
      "required": false,
      "salesRelevance": "HIGH",
      "triggerProducts": ["efficiency"],
      "solutionText": "Kalkulierbare Kosten, weniger Personal"
    },
    "hasFlexibilityIssues": {
      "key": "hasFlexibilityIssues",
      "label": "Mangelnde Flexibilität bei Gästezahlen",
      "fieldType": "checkbox",
      "category": "painPoints",
      "displayCondition": "always",
      "validBranches": ["all"],
      "required": false,
      "salesRelevance": "MEDIUM",
      "triggerProducts": ["portion-exact"],
      "solutionText": "Portionsgenaue Bestellung möglich"
    },
    "hasDietComplexity": {
      "key": "hasDietComplexity",
      "label": "Komplexe Diät-/Allergieanforderungen",
      "fieldType": "checkbox",
      "category": "painPoints",
      "displayCondition": "always",
      "validBranches": ["all"],
      "required": false,
      "salesRelevance": "MEDIUM",
      "triggerProducts": ["special-diets", "allergen-free"],
      "solutionText": "Komplettes Sortiment für alle Anforderungen"
    }
  }
}
```

---

## 🤖 Zusatzgeschäft

```json
{
  "additionalBusiness": {
    "vendingInterest": {
      "key": "vendingInterest",
      "label": "Interesse an Vending/Automaten (bonPeti)",
      "fieldType": "checkbox",
      "category": "additional",
      "displayCondition": "always",
      "validBranches": ["all"],
      "required": false,
      "salesRelevance": "MEDIUM",
      "helpText": "24/7 Versorgung für Mitarbeiter/Gäste",
      "triggerProducts": ["vending", "bonpeti"]
    },
    "vendingLocations": {
      "key": "vendingLocations",
      "label": "Mögliche Automaten-Standorte",
      "fieldType": "number",
      "category": "additional",
      "displayCondition": "vendingInterest === true",
      "validBranches": ["all"],
      "placeholder": "0",
      "helpText": "z.B. Pausenräume, Lobbys, Stationen"
    }
  }
}
```

---

## 📊 Branchen-Mapping

```typescript
// Übersicht welche Felder für welche Branche
const branchFieldMapping = {
  hotel: [
    ...universalPainPoints,
    ...hotelServices,
    ...additionalBusiness
  ],
  hospital: [
    ...universalPainPoints,
    ...hospitalServices,
    ...additionalBusiness
  ],
  restaurant: [
    ...universalPainPoints,
    ...restaurantServices,
    ...additionalBusiness
  ],
  betriebsrestaurant: [
    ...universalPainPoints,
    ...canteenServices,
    ...additionalBusiness
  ]
};
```

---

**→ Weiter:** [Field Catalog Contacts](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/implementation/FIELD_CATALOG_CONTACTS.md) für Step 3 Felder