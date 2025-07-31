# 🧙 Wizard Structure V3 - Enterprise Best Practice

**Status:** 🆕 FINAL VERSION  
**Datum:** 31.07.2025  
**Theme:** AdaptiveFormContainer überall

---

## 📍 Navigation
**← Hauptdoku:** [Sprint 2 V3 Final](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/SPRINT2_V3_FINAL_STRUCTURE.md)  
**→ Steps:** [Step 1](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/wizard/STEP1_BASIS_FILIALSTRUKTUR.md) | [Step 2 V3](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/wizard/STEP2_HERAUSFORDERUNGEN_POTENZIAL_V3.md) | [Step 3 V2](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/wizard/STEP3_ANSPRECHPARTNER_V2.md) | [Step 4](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/wizard/STEP4_ANGEBOT_SERVICES.md)

---

## 🎯 4-Step Wizard Flow

```
┌─────────────────┐     ┌──────────────────┐     ┌─────────────────┐     ┌──────────────────┐
│   Step 1        │     │    Step 2        │     │    Step 3       │     │    Step 4        │
│                 │     │                  │     │                 │     │                  │
│ Basis &         │ --> │ Herausforderungen│ --> │ Ansprechpartner │ --> │ Angebot &        │
│ Filialstruktur  │     │ & Potenzial      │     │                 │     │ Leistungen       │
│                 │     │                  │     │                 │     │ je Filiale       │
│ ✅ Unverändert  │     │ 🔄 Nur Global    │     │ 🔄 + Zuordnung  │     │ 🆕 Komplett neu  │
└─────────────────┘     └──────────────────┘     └─────────────────┘     └──────────────────┘

         ↓                      ↓                       ↓                       ↓
   Wer ist der           Was sind die            Wer sind die           Was braucht jeder
    Kunde?              Herausforderungen?      Ansprechpartner?          Standort?
```

---

## 📋 Step Details

### Step 1: Basis & Filialstruktur
**Status:** ✅ Bleibt unverändert  
**Fokus:** Unternehmensdaten & Standorte

```typescript
// Einzelbetrieb-Logik
if (chainCustomer === 'nein') {
  // Automatisch 1 Standort erstellen
  locations = [{
    id: 'main',
    name: companyName,
    isHeadquarter: true,
    address: businessAddress
  }];
}
```

### Step 2: Herausforderungen & Potenzial V3
**Status:** 🔄 Bereinigt - NUR globale Themen  
**Fokus:** Unternehmensweite Strategie

**Entfernt:**
- LocationSelector
- LocationServicesSection
- Jegliche standortbasierte Logik

**Verbessert:**
- EUR-Feld mit Live-Formatierung
- Mini-Calculator für Potenzial
- Pain Point → Solution Mapping bleibt

### Step 3: Ansprechpartner V2
**Status:** 🔄 Erweitert um Zuordnung  
**Fokus:** Kontakte & Zuständigkeiten

```typescript
interface ContactV2 {
  // Bestehende Felder...
  
  // NEU: Zuständigkeitsbereich
  responsibilityScope: 'all' | 'specific';
  assignedLocationIds?: string[]; // Bei 'specific'
}
```

**UI-Erweiterung:**
```
Zuständigkeitsbereich:
○ Für alle Standorte
● Für bestimmte Standorte: [Berlin ✓] [München ✓] [Hamburg ]
```

### Step 4: Angebot & Leistungen je Filiale
**Status:** 🆕 Komplett neu  
**Fokus:** Standortspezifische Services

**Features:**
- Standort-Navigation mit Progress
- "Kopieren von" Dropdown
- "Für alle restlichen" Checkbox
- Speichern & später fortsetzen

**Einzelbetrieb-Modus:**
```typescript
if (locations.length === 1) {
  // Keine Navigation zeigen
  // Direkt Service-Felder anzeigen
  // Titel: "Angebot & Leistungen"
}
```

---

## 🎨 UI Konsistenz mit AdaptiveFormContainer

### Durchgängiges Theme-System:

```typescript
// In ALLEN Steps
<AdaptiveFormContainer theme={theme}>
  <DynamicFieldRenderer
    fields={fields}
    values={values}
    onChange={handleChange}
    sizeHints={fieldSizeHints}
  />
</AdaptiveFormContainer>
```

### Field Size Hints überall:
- `klein`: 60-120px (Zahlen, kurze Dropdowns)
- `mittel`: 150-250px (Standard-Felder)
- `groß`: 300-400px (EUR-Feld, lange Texte)

---

## 💾 Store-Struktur V3

```typescript
interface CustomerOnboardingStoreV3 {
  // Step 1 (unverändert)
  customerData: Customer;
  locations: CustomerLocation[];
  
  // Step 2 (bereinigt)
  painPoints: PainPointData;
  expectedAnnualRevenue: number;
  vendingInterest: VendingData;
  
  // Step 3 (erweitert)
  contacts: ContactV2[];
  
  // Step 4 (NEU)
  locationServices: Record<string, LocationServiceData>;
  currentLocationIndex: number;
  completedLocationIds: string[];
  
  // Actions
  setExpectedRevenue: (amount: number) => void;
  setContactResponsibility: (contactId: string, scope: 'all' | 'specific', locationIds?: string[]) => void;
  saveLocationServices: (locationId: string, services: LocationServiceData) => void;
  copyLocationServices: (fromId: string, toIds: string[]) => void;
}
```

---

## 🔄 Migration von altem Code

### Step 2 Cleanup:
```typescript
// ENTFERNEN aus Step2AngebotPainpoints_V2.tsx:
- import { LocationSelector } from '../location/LocationSelector';
- import { LocationServicesSection } from '../sections/LocationServicesSection';
- Alle location-bezogenen State-Variablen
- Zeilen 272-308 (LocationSelector & LocationServicesSection JSX)

// BEHALTEN:
- GlobalChallengesSection
- RevenueExpectationSection (mit EUR-Update)
- AdditionalBusinessSection
- Pain Point Solutions
```

### Store Cleanup:
```typescript
// VERSCHIEBEN von customerOnboardingStore zu step4Store:
- selectedLocationId
- applyToAllLocations
- locationServices
- completedLocationIds
- Alle location-service Actions
```

---

## ✅ Checkliste für Implementierung

### TODO-14: EUR-Feld ⚡
- [ ] Field Definition erweitern
- [ ] Live-Formatierung implementieren
- [ ] Mini-Calculator Component
- [ ] Tooltip mit Berechnungshilfe

### TODO-15: Step 2 bereinigen 🧹
- [ ] Location-Imports entfernen
- [ ] Location-JSX entfernen
- [ ] Store-Calls anpassen
- [ ] Tests aktualisieren

### TODO-16: Step 3 erweitern 📝
- [ ] ContactV2 Interface
- [ ] Zuständigkeits-UI
- [ ] Store-Action hinzufügen
- [ ] Validierung

### TODO-17: Step 4 erstellen 🆕
- [ ] Step4AngebotServices.tsx
- [ ] LocationNavigator Component
- [ ] ServiceFieldGroups
- [ ] Progress Indicator

### TODO-18: Backend anpassen 🔧
- [ ] CustomerLocation Entity
- [ ] Migration V7
- [ ] API Endpoints
- [ ] DTOs erweitern

---

## 🚀 Quick Start für neue Claudes

```bash
# 1. Branch checken
git checkout feature/sprint-2-customer-ui-integration

# 2. Aktuelle Struktur verstehen
cat docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/SPRINT2_V3_FINAL_STRUCTURE.md

# 3. Mit EUR-Feld starten (Quick Win)
cd frontend/src/features/customers/data
# fieldCatalogExtensions.json bearbeiten

# 4. Step 2 bereinigen
cd ../components/steps
# Step2AngebotPainpoints_V2.tsx aufräumen

# 5. Tests laufen lassen
npm test -- --watch
```

---

## 🔗 Weiterführende Links

**Details pro Step:**
- [Step 2 Implementation V3](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/implementation/STEP2_IMPLEMENTATION_V3.md)
- [Step 3 Implementation V2](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/implementation/STEP3_IMPLEMENTATION_V2.md)
- [Step 4 Implementation](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/implementation/STEP4_IMPLEMENTATION.md)

**Backend:**
- [Location Services API](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/backend/LOCATION_SERVICES_API.md)
- [Migration V7 Guide](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/backend/MIGRATION_V7_GUIDE.md)