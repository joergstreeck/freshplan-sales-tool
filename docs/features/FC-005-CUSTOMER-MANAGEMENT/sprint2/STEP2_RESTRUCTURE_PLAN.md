# Step 2 Umstrukturierung: Vertriebslogische Reihenfolge

**Datum:** 2025-07-30  
**Status:** 🔄 In Arbeit  
**Ziel:** Logische Neuordnung der Felder für optimalen Vertriebsflow

## 🎯 Neue Struktur

### 1. Globale Unternehmensebene
```
🎯 Pain Points (1x pro Unternehmen)
├── Personalmangel/Fachkräftemangel
├── Schwankende Qualität
├── Lebensmittelverschwendung
├── Kostendruck
├── Mangelnde Flexibilität
└── Diät-/Allergieanforderungen

💰 Umsatzerwartung (NEU)
└── Geplantes Jahresvolumen Freshfoodz [EUR]
    └── Basis für Partnerschaftsvertrag

🤖 Zusatzgeschäft
├── Vending/Automaten-Interesse
└── Anzahl möglicher Standorte
```

### 2. Filialspezifische Ebene
```
🍳 Angebotsstruktur (pro Standort)
├── Standortauswahl/Liste
├── Option: "Für alle Filialen übernehmen"
└── Pro Filiale:
    ├── Frühstück (ja/nein)
    ├── Warme Komponenten
    ├── Mittagessen/Restaurant
    ├── Abendessen/À la carte
    ├── Roomservice
    ├── Events/Bankette
    └── Kapazitäten
```

## 📋 Implementierungsschritte

### Phase 1: Datenmodell erweitern
```json
{
  "globalBusiness": [
    {
      "key": "expectedAnnualRevenue",
      "label": "Geplantes Jahresvolumen Freshfoodz (EUR)",
      "fieldType": "number",
      "required": true,
      "salesRelevance": "CRITICAL",
      "helpText": "Basis für Partnerschaftsvertrag",
      "min": 0,
      "placeholder": "z.B. 250000"
    }
  ]
}
```

### Phase 2: UI-Komponenten
1. **GlobalBusinessSection** - für Pain Points + Umsatz
2. **LocationSelector** - Standortauswahl mit Kopier-Option
3. **LocationSpecificFields** - Pro-Filiale-Angaben

### Phase 3: Datenflow
```typescript
interface Step2Data {
  // Global
  painPoints: PainPointData;
  expectedRevenue: number;
  additionalBusiness: AdditionalData;
  
  // Pro Standort
  locations: {
    [locationId: string]: LocationServiceData;
  };
}
```

## 🔗 Verknüpfungen

**Vorher:** [Step 1 Basis](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/wizard/STEP1_BASIS_FILIALSTRUKTUR.md)  
**Nachher:** [Step 3 Ansprechpartner](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/wizard/STEP3_ANSPRECHPARTNER.md)  
**Hauptdoku:** [FC-005 Customer Management](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/README.md)  
**Master Plan:** [CRM V5](/Users/joergstreeck/freshplan-sales-tool/docs/CRM_COMPLETE_MASTER_PLAN_V5.md#step-2)

## ✅ Vorteile der neuen Struktur

1. **Weniger Doppeleingaben** - Pain Points nur 1x
2. **Klare Vertriebsargumentation** - Von global zu spezifisch
3. **Vertragsrelevante Daten** - Umsatz direkt erfasst
4. **Flexible Standortverwaltung** - Mit Kopier-Option
5. **Saubere Datenauswertung** - Strategie vs. Operation getrennt

## 🚀 Nächste Schritte

- [ ] fieldCatalogExtensions.json erweitern
- [ ] Step2AngebotPainpoints.tsx umbauen
- [ ] LocationSelector Komponente erstellen
- [ ] Tests anpassen