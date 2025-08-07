# ⚠️ DEPRECATED - Step 2 V2 (Ersetzt durch V3)

**Status:** ❌ VERALTET - Nutze [Step 2 V3](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/wizard/STEP2_HERAUSFORDERUNGEN_POTENZIAL_V3.md)  
**Grund:** Angebotsstruktur wurde in Step 4 verschoben  
**Neue Struktur:** [Sprint 2 V3 Final](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/SPRINT2_V3_FINAL_STRUCTURE.md)

---

## 📍 Navigation
**← Zurück:** [Step 1: Basis & Filialstruktur](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/wizard/STEP1_BASIS_FILIALSTRUKTUR.md)  
**→ Weiter:** [Step 3: Ansprechpartner](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/wizard/STEP3_ANSPRECHPARTNER.md)  
**🔧 Planung:** [Step 2 Restructure Plan](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/STEP2_RESTRUCTURE_PLAN.md)

---

## 🎯 Neue Struktur (Best Practice)

### Teil 1: Globale Unternehmensebene
1. **Pain Points** - Strategische Herausforderungen (1x für gesamtes Unternehmen)
2. **Umsatzerwartung** - Jahresvolumen für Vertrag
3. **Zusatzgeschäft** - Vending/Automaten übergeordnet

### ❌ ENTFERNT in V3:
4. ~~**Standortauswahl**~~ - Verschoben nach Step 4
5. ~~**Angebotsstruktur**~~ - Verschoben nach Step 4

**Neue Struktur:** Step 2 enthält NUR noch globale Themen!

---

## 🖼️ UI Design mit AdaptiveFormContainer

### Abschnitt 1: Globale Herausforderungen
```
┌─────────────────────────────────────────────┐
│ Schritt 2: Herausforderungen & Potenzial     │
├─────────────────────────────────────────────┤
│                                              │
│ 🎯 Ihre aktuellen Herausforderungen          │
│ (Unternehmensweit)                           │
│                                              │
│ ┌─── AdaptiveFormContainer ─────────────┐   │
│ │ [Personalmangel/     ] [Schwankende   ]│   │
│ │ [Fachkräftemangel ▼ ] [Qualität    ▼ ]│   │
│ │   └─ sizeHint: mittel    └─ mittel    │   │
│ │                                        │   │
│ │ [Lebensmittel-       ] [Kostendruck ▼ ]│   │
│ │ [verschwendung    ▼ ]                  │   │
│ │   └─ mittel             └─ klein       │   │
│ │                                        │   │
│ │ [Mangelnde Flexibilität] [Komplexe    ]│   │
│ │ [bei Gästezahlen     ▼] [Diätanfor. ▼]│   │
│ │   └─ groß                 └─ mittel    │   │
│ └────────────────────────────────────────┘   │
│                                              │
│ 💡 Je mehr Herausforderungen, desto größer   │
│    das Potenzial mit Freshfoodz!             │
└─────────────────────────────────────────────┘
```

### Abschnitt 2: Umsatzpotenzial
```
┌─────────────────────────────────────────────┐
│ 💰 Geschäftspotenzial                        │
│                                              │
│ ┌─── AdaptiveFormContainer ─────────────┐   │
│ │ Geplantes Jahresvolumen (EUR):        │   │
│ │ [250.000                    ]         │   │
│ │  └─ sizeHint: groß, type: number      │   │
│ │                                        │   │
│ │ ℹ️ Basis für Partnerschaftsvertrag     │   │
│ │    und Rahmenkonditionen               │   │
│ └────────────────────────────────────────┘   │
│                                              │
│ 📊 Automatische Kalkulation:                 │
│ • 45 Standorte × Ø 5.500€ = 247.500€        │
│ • Mit Pain Points: +30% = 321.750€          │
└─────────────────────────────────────────────┘
```

### Abschnitt 3: Zusatzgeschäft
```
┌─────────────────────────────────────────────┐
│ 🤖 Zusatzgeschäft                            │
│                                              │
│ ┌─── AdaptiveFormContainer ─────────────┐   │
│ │ [Interesse an        ] [Mögliche      ]│   │
│ │ [Vending/Automaten ▼] [Standorte: 15 ]│   │
│ │  └─ sizeHint: mittel   └─ klein        │   │
│ └────────────────────────────────────────┘   │
└─────────────────────────────────────────────┘
```

### Abschnitt 4: Standortauswahl (NEU)
```
┌─────────────────────────────────────────────┐
│ 📍 Angebotsstruktur pro Standort            │
│                                              │
│ Für welchen Standort erfassen?              │
│ ┌────────────────────────────────────────┐  │
│ │ [Alle Standorte gleich ▼]              │  │
│ │  ├─ Alle Standorte gleich              │  │
│ │  ├─ Standort 1: Berlin (Hauptsitz)     │  │
│ │  ├─ Standort 2: München                │  │
│ │  └─ + Neuer Standort                   │  │
│ └────────────────────────────────────────┘  │
│                                              │
│ ☑ Für alle 45 Standorte übernehmen         │
└─────────────────────────────────────────────┘
```

### Abschnitt 5: Angebotsstruktur (Filialspezifisch)
```
┌─────────────────────────────────────────────┐
│ 🍳 Angebotsstruktur: Alle Standorte         │
│                                              │
│ Was bieten Sie Ihren Gästen?                │
│                                              │
│ ┌─── AdaptiveFormContainer ─────────────┐   │
│ │ ┌─ Frühstücksgeschäft ────────────┐   │   │
│ │ │ [Frühstück      ] [Warme Komp. ]│   │   │
│ │ │ [angeboten   ▼ ] [angeboten  ▼ ]│   │   │
│ │ │  └─ klein         └─ mittel      │   │   │
│ │ │                                  │   │   │
│ │ │ [Frühstücksgäste/Tag: 120    ]  │   │   │
│ │ │  └─ sizeHint: klein             │   │   │
│ │ └──────────────────────────────────┘   │   │
│ │                                        │   │
│ │ ┌─ Mittag- und Abendessen ────────┐   │   │
│ │ │ [Mittagessen/   ] [Abendessen/ ]│   │   │
│ │ │ [Restaurant  ▼  ] [À la carte ▼]│   │   │
│ │ │  └─ mittel         └─ mittel    │   │   │
│ │ └──────────────────────────────────┘   │   │
│ │                                        │   │
│ │ ┌─ Zusatzservices ─────────────────┐   │   │
│ │ │ [Roomservice ▼  ] [Events/     ]│   │   │
│ │ │  └─ klein         [Bankette  ▼ ]│   │   │
│ │ │                    └─ mittel    │   │   │
│ │ │                                  │   │   │
│ │ │ [Max. Eventgäste: 200        ]  │   │   │
│ │ │  └─ sizeHint: klein             │   │   │
│ │ └──────────────────────────────────┘   │   │
│ │                                        │   │
│ │ ┌─ Kapazität ──────────────────────┐   │   │
│ │ │ [Anzahl Zimmer: ] [Auslastung (%)]│  │   │
│ │ │ [150           ] [75            ]│   │   │
│ │ │  └─ klein         └─ klein       │   │   │
│ │ └──────────────────────────────────┘   │   │
│ └────────────────────────────────────────┘   │
└─────────────────────────────────────────────┘
```

---

## 🎨 Theme-Integration (wie Step 1)

### AdaptiveFormContainer Features:
- ✅ Automatische Dropdown-Breiten (useDropdownWidth Hook)
- ✅ Responsive Flexbox Layout mit Wrapping
- ✅ Field Size Hints (klein, mittel, groß)
- ✅ Gruppen-Container für logische Bereiche
- ✅ Info-Tooltips bei Bedarf

### CSS-Klassen:
```css
.field-dropdown-auto    /* Automatische Breite */
.field-number-compact   /* 60-90px für Zahlen */
.field-text-medium      /* Standard Textfelder */
.field-select-large     /* Große Dropdowns */
```

---

## 📊 Datenstruktur

```typescript
interface Step2Data {
  // TEIL 1: Global (1x pro Unternehmen)
  painPoints: {
    hasStaffingIssues: 'ja' | 'nein';
    hasQualityIssues: 'ja' | 'nein';
    hasFoodWasteIssues: 'ja' | 'nein';
    hasCostPressure: 'ja' | 'nein';
    hasFlexibilityIssues: 'ja' | 'nein';
    hasDietComplexity: 'ja' | 'nein';
  };
  
  expectedAnnualRevenue: number; // NEU!
  
  additionalBusiness: {
    vendingInterest: 'ja' | 'nein';
    vendingLocations?: number;
  };
  
  // TEIL 2: Pro Standort
  locationServiceData: {
    applyToAll: boolean; // "Für alle übernehmen"
    selectedLocationId: string | 'all';
    
    // Diese Daten entweder global oder pro Location
    services: {
      // Hotel-spezifisch
      offersBreakfast?: 'ja' | 'nein';
      breakfastWarm?: 'ja' | 'nein';
      breakfastGuestsPerDay?: number;
      offersLunch?: 'ja' | 'nein';
      offersDinner?: 'ja' | 'nein';
      offersRoomService?: 'ja' | 'nein';
      offersEvents?: 'ja' | 'nein';
      eventCapacity?: number;
      roomCount?: number;
      averageOccupancy?: number;
      
      // Andere Branchen...
    };
  };
}
```

---

## 🔧 Implementierungs-Checkliste für neuen Claude

### 1. Field Catalog erweitern:
```json
{
  "globalBusiness": [
    {
      "key": "expectedAnnualRevenue",
      "label": "Geplantes Jahresvolumen Freshfoodz (EUR)",
      "fieldType": "number",
      "required": true,
      "sizeHint": "groß",
      "salesRelevance": "CRITICAL",
      "helpText": "Basis für Partnerschaftsvertrag",
      "min": 0,
      "placeholder": "z.B. 250000"
    }
  ]
}
```

### 2. Neue Komponenten:
- `LocationSelector.tsx` - Dropdown mit Standortauswahl
- `GlobalChallengesSection.tsx` - Pain Points + Umsatz
- `LocationServicesSection.tsx` - Angebotsstruktur

### 3. Layout anpassen:
- `Step2AngebotPainpoints.tsx` umstrukturieren
- Reihenfolge: Global → Standortauswahl → Services
- AdaptiveFormContainer verwenden (wie Step 1)

### 4. State Management:
- Store erweitern für `expectedAnnualRevenue`
- Location-spezifische Daten verwalten
- "Für alle übernehmen" Logic

### 5. Backend vorbereiten:
- Customer Entity hat bereits `expectedAnnualVolume`
- LocationServices müssen an CustomerLocation Entity
- API Endpoints für standortbasierte Services

---

## 🎯 Pain Point Solutions (unverändert)

```typescript
const PAIN_POINT_SOLUTIONS = {
  hasStaffingIssues: {
    title: 'Personalmangel',
    solution: 'Cook&Fresh® - Keine Fachkräfte nötig!',
    products: ['Convenience Line', 'Ready-to-Serve'],
    impact: '30% weniger Personalkosten'
  },
  // ... rest wie gehabt
};
```

---

## ✅ Vorteile der neuen Struktur

1. **Logischer Flow:** Von strategisch (Pain Points) zu operativ (Services)
2. **Weniger Redundanz:** Pain Points nur 1x erfassen
3. **Vertragsrelevanz:** Umsatzerwartung prominent platziert
4. **Flexibilität:** "Für alle übernehmen" spart Zeit
5. **Best Practice:** Entspricht modernem B2B-CRM Standard

---

## 🔗 Weiterführende Links

**Vorheriger Step:** [Step 1: Basis & Filialstruktur](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/wizard/STEP1_BASIS_FILIALSTRUKTUR.md)  
**Nächster Step:** [Step 3: Ansprechpartner](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/wizard/STEP3_ANSPRECHPARTNER.md)  
**Implementierung:** [Field Catalog Extension](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/implementation/FIELD_CATALOG_EXTENSION.md)  
**Theme System:** [Adaptive Layout Guide](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/prototypes/ADAPTIVE_LAYOUT_IMPLEMENTATION_GUIDE.md)