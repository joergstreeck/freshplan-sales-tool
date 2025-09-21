# 📋 Sprint 2 Step 2 Implementierungs-Checkliste für Claude

**Erstellt:** 30.07.2025  
**Zweck:** Vollständige Anleitung für neuen Claude zur Step 2 Umstrukturierung  
**Priorität:** 🔴 KRITISCH - Muss als erstes umgesetzt werden!

---

## 🎯 Übersicht der Änderungen

**ALT:** Angebotsstruktur → Pain Points → Potenzial  
**NEU:** Pain Points (global) → Umsatzerwartung → Zusatzgeschäft → Standortauswahl → Angebotsstruktur (pro Filiale)

---

## ✅ Implementierungs-Checkliste

### 1️⃣ Field Catalog erweitern (fieldCatalogExtensions.json)

```json
// NEU hinzufügen in "globalBusiness" Sektion:
{
  "key": "expectedAnnualRevenue",
  "label": "Geplantes Jahresvolumen Freshfoodz (EUR)",
  "entityType": "customer",
  "fieldType": "number",
  "required": true,
  "category": "business",
  "sizeHint": "groß",
  "salesRelevance": "CRITICAL",
  "helpText": "Basis für Partnerschaftsvertrag und Rahmenkonditionen",
  "min": 0,
  "placeholder": "z.B. 250000"
}
```

**Ort:** `/frontend/src/features/customers/data/fieldCatalogExtensions.json`

### 2️⃣ LocationSelector Component erstellen

**Datei:** `/frontend/src/features/customers/components/location/LocationSelector.tsx`

**Specs:** [LocationSelector Component Spec](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/implementation/LOCATION_SELECTOR_COMPONENT.md)

**Wichtig:** 
- MUI Select mit "Alle Standorte" Option
- Checkbox für "Für alle übernehmen"
- Integration mit customerOnboardingStore

### 3️⃣ Step2AngebotPainpoints.tsx umstrukturieren

**Neue Reihenfolge implementieren:**

```tsx
// STRUKTUR:
return (
  <Box>
    {/* 1. Globale Herausforderungen */}
    <GlobalChallengesSection />
    
    {/* 2. Umsatzpotenzial */}
    <RevenueExpectationSection />
    
    {/* 3. Zusatzgeschäft */}
    <AdditionalBusinessSection />
    
    <Divider sx={{ my: 4 }} />
    
    {/* 4. Standortauswahl */}
    <LocationSelector />
    
    {/* 5. Angebotsstruktur */}
    <LocationServicesSection />
  </Box>
);
```

### 4️⃣ Neue Section Components

#### GlobalChallengesSection.tsx
```tsx
// Pain Points mit AdaptiveFormContainer
<Box>
  <Typography variant="h6">🎯 Ihre aktuellen Herausforderungen</Typography>
  <Typography variant="body2" color="text.secondary">
    (Unternehmensweit)
  </Typography>
  <AdaptiveFormContainer>
    <DynamicFieldRenderer
      fields={painPointFields}
      useAdaptiveLayout={true}
    />
  </AdaptiveFormContainer>
</Box>
```

#### RevenueExpectationSection.tsx
```tsx
// Umsatzerwartung mit Kalkulations-Hilfe
<Box>
  <Typography variant="h6">💰 Geschäftspotenzial</Typography>
  <AdaptiveFormContainer>
    <DynamicFieldRenderer
      fields={[expectedAnnualRevenueField]}
      useAdaptiveLayout={true}
    />
  </AdaptiveFormContainer>
  {/* Automatische Kalkulation anzeigen */}
  <CalculationHelper 
    locations={totalLocationsEU}
    painPoints={activePainPoints}
  />
</Box>
```

### 5️⃣ Store erweitern (customerOnboardingStore.ts)

```typescript
interface CustomerOnboardingState {
  // NEU:
  expectedAnnualRevenue: number;
  selectedLocationId: string | 'all';
  applyToAllLocations: boolean;
  locationServices: Map<string, ServiceData>;
  
  // Actions NEU:
  setExpectedRevenue: (amount: number) => void;
  setSelectedLocation: (locationId: string | 'all') => void;
  setApplyToAll: (value: boolean) => void;
  saveLocationServices: (locationId: string, data: ServiceData) => void;
}
```

### 6️⃣ Theme Integration beibehalten

**WICHTIG:** AdaptiveFormContainer aus Step 1 verwenden!

```tsx
import { AdaptiveFormContainer } from '../adaptive/AdaptiveFormContainer';

// Nutze vorhandene CSS-Klassen:
// .field-dropdown-auto
// .field-number-compact
// .field-select-medium
```

### 7️⃣ Backend Vorbereitung

**Check:** Customer Entity hat bereits `expectedAnnualVolume` ✅

**TODO für später:** LocationServices an CustomerLocation Entity
```java
// Später in CustomerLocation.java ergänzen:
@Column(name = "offers_breakfast")
private Boolean offersBreakfast;

@Column(name = "breakfast_guests_per_day")
private Integer breakfastGuestsPerDay;
// etc.
```

---

## 🚀 Reihenfolge der Umsetzung

1. **fieldCatalogExtensions.json** erweitern ⏱️ 5 Min
2. **LocationSelector** Component ⏱️ 45 Min
3. **Store** erweitern ⏱️ 30 Min
4. **Section Components** erstellen ⏱️ 60 Min
5. **Step2AngebotPainpoints.tsx** umbauen ⏱️ 45 Min
6. **Tests** anpassen ⏱️ 30 Min

**Gesamt:** ~3.5 Stunden

---

## ⚠️ Wichtige Hinweise

### TypeScript Import Types
```typescript
// IMMER so:
import type { FieldDefinition, Customer } from './types';

// NIEMALS so:
import { FieldDefinition, Customer } from './types';
```

### Branchen-Logik
- Aktuell nur Hotels implementiert
- Krankenhäuser & Betriebsrestaurants: Placeholder vorbereiten
- Switch-Case für industry in serviceFieldGroups

### Test-Daten
```typescript
// Für Tests:
const mockCustomerWithLocations = {
  totalLocationsEU: 45,
  locations: [
    { id: '1', name: 'Berlin', isMainLocation: true },
    { id: '2', name: 'München', isMainLocation: false }
  ]
};
```

---

## 📚 Referenz-Dokumente

1. **Hauptplan:** [Step 2 Restructure Plan](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/STEP2_RESTRUCTURE_PLAN.md)
2. **Neue Struktur:** [Step 2 V2](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/wizard/STEP2_ANGEBOT_PAINPOINTS_V2.md)
3. **Theme System:** [Adaptive Layout Guide](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/prototypes/ADAPTIVE_LAYOUT_IMPLEMENTATION_GUIDE.md)
4. **Review:** [Sprint 2 Complete Review](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/SPRINT2_COMPLETE_REVIEW.md)

---

## 🎯 Definition of Done

- [ ] expectedAnnualRevenue Feld funktioniert
- [ ] LocationSelector zeigt alle Standorte
- [ ] "Für alle übernehmen" Logic implementiert
- [ ] Reihenfolge: Pain Points → Umsatz → Angebote
- [ ] AdaptiveFormContainer überall genutzt
- [ ] Tests grün
- [ ] Build erfolgreich

---

**Bei Fragen:** Dokumentation ist vollständig - alles Nötige steht hier und in den verlinkten Docs!