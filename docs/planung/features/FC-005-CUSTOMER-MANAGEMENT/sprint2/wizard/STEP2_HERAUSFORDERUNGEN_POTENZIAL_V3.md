# 🎯 Step 2: Herausforderungen & Potenzial V3

**Status:** 🔄 Bereinigt - NUR globale Themen  
**Component:** Step2HerausforderungenPotenzialV3.tsx  
**Theme:** AdaptiveFormContainer

---

## 📍 Navigation
**← Zurück:** [Step 1](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/wizard/STEP1_BASIS_FILIALSTRUKTUR.md)  
**→ Weiter:** [Step 3 V2](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/wizard/STEP3_ANSPRECHPARTNER_V2.md)  
**⬆️ Wizard:** [Wizard Structure V3](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/WIZARD_STRUCTURE_V3.md)

---

## 🎯 Fokus: Unternehmensweite Strategie

### Was wurde ENTFERNT:
- ❌ LocationSelector
- ❌ LocationServicesSection  
- ❌ Alle standortbezogenen Felder
- ❌ "Für alle Filialen übernehmen" Logic

### Was bleibt/wird verbessert:
- ✅ Pain Points (unternehmensweite Herausforderungen)
- ✅ Geplantes Jahresvolumen (NEU: EUR-Format, großes Feld)
- ✅ Zusatzgeschäft (Vending/Automaten)
- ✅ Pain Point → Solution Mapping

---

## 🖼️ UI Design

```
┌─────────────────────────────────────────────┐
│ Schritt 2: Herausforderungen & Potenzial     │
├─────────────────────────────────────────────┤
│                                              │
│ 🎯 Ihre aktuellen Herausforderungen          │
│ (Unternehmensweit)                           │
│                                              │
│ ┌─── AdaptiveFormContainer ─────────────┐   │
│ │ [✓] Personalmangel     [✓] Schwankende│   │
│ │     Fachkräftemangel       Qualität   │   │
│ │                                        │   │
│ │ [✓] Lebensmittel-      [ ] Kostendruck│   │
│ │     verschwendung                      │   │
│ │                                        │   │
│ │ Live-Counter: 3 Herausforderungen     │   │
│ └────────────────────────────────────────┘   │
│                                              │
│ 💰 Geschäftspotenzial                        │
│                                              │
│ ┌─── AdaptiveFormContainer ─────────────┐   │
│ │ Geplantes Jahresvolumen (EUR):        │   │
│ │ ┌────────────────────────────────┐    │   │
│ │ │        250.000 €                │    │   │
│ │ └────────────────────────────────┘    │   │
│ │  ↑ Großes Feld, 18px, rechtsbündig    │   │
│ │                                        │   │
│ │ 💡 Kalkulator:                         │   │
│ │ 45 Standorte × 5.500€ = 247.500€      │   │
│ │ + 30% Pain Point Bonus = 321.750€     │   │
│ └────────────────────────────────────────┘   │
│                                              │
│ 🤖 Zusatzgeschäft bonPeti                    │
│ ┌─── AdaptiveFormContainer ─────────────┐   │
│ │ [Interesse an      ] [Standorte: 15  ]│   │
│ │ [Vending/Automaten▼]                   │   │
│ └────────────────────────────────────────┘   │
└─────────────────────────────────────────────┘
```

---

## 💰 EUR-Feld Spezifikation (NEU)

```typescript
// Field Definition
{
  key: "expectedAnnualRevenue",
  label: "Geplantes Jahresvolumen Freshfoodz (EUR)",
  fieldType: "number",
  format: "currency",
  currency: "EUR",
  thousandSeparator: true,
  width: "large",
  fontSize: "18px",
  textAlign: "right",
  min: 0,
  validationWarning: 1000000,
  tooltip: "Brutto-Jahresvolumen an Freshfoodz-Produkten...",
  showCalculator: true
}

// Custom EUR Input Component
const EURInput: React.FC<{
  value: number;
  onChange: (value: number) => void;
  showCalculator?: boolean;
}> = ({ value, onChange, showCalculator }) => {
  const formattedValue = formatEUR(value); // "250.000 €"
  
  return (
    <Box>
      <TextField
        value={formattedValue}
        onChange={handleNumericInput}
        sx={{
          '& input': {
            fontSize: '18px',
            textAlign: 'right',
            fontWeight: 500
          }
        }}
        fullWidth
      />
      {showCalculator && <RevenueCalculator />}
    </Box>
  );
};
```

---

## 📊 Component Struktur

```typescript
// Step2HerausforderungenPotenzialV3.tsx
export const Step2HerausforderungenPotenzialV3: React.FC = () => {
  const {
    customerData,
    setCustomerField,
    validateField,
    // KEINE location-bezogenen Hooks mehr!
  } = useCustomerOnboardingStore();
  
  const { getFieldByKey } = useFieldDefinitions();
  
  // Nur noch 3 Sections:
  return (
    <Box>
      {/* 1. Pain Points */}
      <GlobalChallengesSection />
      
      {/* 2. Umsatzerwartung */}
      <RevenueExpectationSectionV2 /> // Mit EUR-Format
      
      {/* 3. Zusatzgeschäft */}
      <AdditionalBusinessSection />
      
      {/* Pain Point Solutions bleiben */}
      {activePainPoints.length > 0 && (
        <PainPointSolutions />
      )}
    </Box>
  );
};
```

---

## 🧮 Revenue Calculator Component

```typescript
const RevenueCalculator: React.FC = () => {
  const { customerData } = useCustomerOnboardingStore();
  const locationCount = customerData.totalLocationsEU || 1;
  const painPointCount = countActivePainPoints(customerData);
  
  // Basis-Kalkulation
  const baseRevenue = locationCount * 5500;
  const painPointMultiplier = 1 + (painPointCount * 0.1); // +10% pro Pain Point
  const projectedRevenue = baseRevenue * painPointMultiplier;
  
  return (
    <Paper variant="outlined" sx={{ p: 2, mt: 1 }}>
      <Typography variant="caption" color="text.secondary">
        💡 Kalkulationshilfe:
      </Typography>
      <Box sx={{ mt: 1 }}>
        <Typography variant="body2">
          {locationCount} Standorte × Ø 5.500€ = {formatEUR(baseRevenue)}
        </Typography>
        {painPointCount > 0 && (
          <Typography variant="body2" color="success.main">
            + {(painPointMultiplier - 1) * 100}% Pain Point Potenzial = {formatEUR(projectedRevenue)}
          </Typography>
        )}
      </Box>
    </Paper>
  );
};
```

---

## 🔄 Migration von V2 zu V3

### Zu entfernen aus Step2AngebotPainpoints_V2.tsx:

```typescript
// ENTFERNEN: Imports
- import { LocationSelector } from '../location/LocationSelector';
- import { LocationServicesSection } from '../sections/LocationServicesSection';
- import type { LocationServiceData } from '../../stores/customerOnboardingStore.extensions';

// ENTFERNEN: Hooks
- selectedLocationId,
- applyToAllLocations,
- completedLocationIds,
- setSelectedLocation,
- setApplyToAll,
- saveLocationServices,
- getLocationServices,

// ENTFERNEN: Service Field Groups (Zeilen 110-156)
- const serviceFieldGroups = useMemo(() => { ... });

// ENTFERNEN: Location Handlers (Zeilen 174-180)
- const handleLocationChange = ...
- const handleApplyToAllChange = ...

// ENTFERNEN: JSX (Zeilen 266-308)
- <Divider sx={{ my: 4 }} />
- {/* TEIL 2: Filialspezifische Ebene */}
- {customerData.chainCustomer === 'ja' && locations.length > 0 && (
-   <LocationSelector ... />
- )}
- {serviceFieldGroups.length > 0 && (
-   <LocationServicesSection ... />
- )}
```

### Datei umbenennen:
```bash
mv Step2AngebotPainpoints_V2.tsx Step2HerausforderungenPotenzialV3.tsx
```

---

## ✅ Checkliste für Implementierung

### EUR-Feld implementieren:
- [ ] formatEUR Utility Function
- [ ] EURInput Component
- [ ] RevenueCalculator Component
- [ ] Field Definition Update

### Step 2 bereinigen:
- [ ] Location-Imports entfernen
- [ ] Location-State entfernen
- [ ] Location-JSX entfernen
- [ ] Tests anpassen

### Store anpassen:
- [ ] expectedAnnualRevenue Action
- [ ] Location-bezogene Actions entfernen
- [ ] Persist Middleware Update

---

## 🧪 Test-Szenarien

```typescript
describe('Step2HerausforderungenPotenzialV3', () => {
  it('zeigt keine Location-Komponenten', () => {
    expect(screen.queryByText(/Standortauswahl/)).not.toBeInTheDocument();
    expect(screen.queryByText(/Für alle Filialen/)).not.toBeInTheDocument();
  });
  
  it('formatiert EUR-Eingaben korrekt', () => {
    const input = screen.getByLabelText(/Jahresvolumen/);
    userEvent.type(input, '250000');
    expect(input).toHaveValue('250.000 €');
  });
  
  it('zeigt Kalkulator bei Focus', () => {
    const input = screen.getByLabelText(/Jahresvolumen/);
    userEvent.click(input);
    expect(screen.getByText(/Kalkulationshilfe/)).toBeInTheDocument();
  });
});
```

---

## 🔗 Weiterführende Links

**Implementation Details:**
- [EUR Field Implementation](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/implementation/EUR_FIELD_IMPLEMENTATION.md)
- [Step 2 Cleanup Guide](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/implementation/STEP2_CLEANUP_GUIDE.md)

**Nächster Step:**
- [Step 3 Ansprechpartner V2](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/wizard/STEP3_ANSPRECHPARTNER_V2.md)