# Step 2 Implementierungs-Status

**Datum:** 30.07.2025  
**Status:** 🟡 Teilweise implementiert - Ready für Build-Test

---

## ✅ Was wurde umgesetzt

### 1. Field Catalog erweitert
- ✅ `expectedAnnualRevenue` Feld hinzugefügt in `fieldCatalogExtensions.json`
- ✅ Neue Sektion "globalBusiness" erstellt

### 2. Neue Komponenten erstellt
- ✅ `LocationSelector.tsx` - Standortauswahl mit "Für alle übernehmen"
- ✅ `GlobalChallengesSection.tsx` - Pain Points Erfassung
- ✅ `RevenueExpectationSection.tsx` - Umsatzerwartung mit Kalkulation
- ✅ `AdditionalBusinessSection.tsx` - Vending/Automaten
- ✅ `LocationServicesSection.tsx` - Angebotsstruktur pro Standort

### 3. Step 2 V2 vorbereitet
- ✅ `Step2AngebotPainpoints_V2.tsx` - Neue Struktur implementiert
- ✅ Reihenfolge: Pain Points → Umsatz → Zusatz → Standorte → Angebote

### 4. Store Extensions definiert
- ✅ `customerOnboardingStore.extensions.ts` - Neue State/Actions definiert

---

## 🟡 TODO für vollständige Integration

### 1. Store Integration
```typescript
// In customerOnboardingStore.ts die Extensions einbinden:
import { createStoreExtensions } from './customerOnboardingStore.extensions';

// Im Store hinzufügen:
...createStoreExtensions(set, get),
```

### 2. Component Switch
```typescript
// In CustomerOnboardingWizard.tsx:
// ALT: import { Step2AngebotPainpoints } from './steps/Step2AngebotPainpoints';
// NEU: import { Step2AngebotPainpointsV2 } from './steps/Step2AngebotPainpoints_V2';
```

### 3. Types aktualisieren
```typescript
// customer.types.ts erweitern um:
expectedAnnualRevenue?: number;
```

---

## 🧪 Build-Test

```bash
cd frontend
npm run build
```

**Erwartete Warnings:**
- Store Extensions noch nicht integriert
- Placeholder-Werte in Step2AngebotPainpoints_V2

**Keine Errors erwartet!** Alle Komponenten sind TypeScript-konform.

---

## 📊 Fortschritt

| Komponente | Status | Anmerkung |
|------------|--------|-----------|
| Field Catalog | ✅ | Vollständig |
| LocationSelector | ✅ | Ready |
| Section Components | ✅ | Alle 4 fertig |
| Step 2 V2 | 🟡 | Placeholder für Store |
| Store Extensions | 🟡 | Definiert, nicht integriert |
| Build | ⏳ | Bereit zum Testen |

---

## 🚀 Nächste Schritte für neuen Claude

1. Store Extensions integrieren
2. Step2AngebotPainpoints_V2 aktivieren
3. Tests schreiben
4. Backend-Anpassungen für LocationServices

**Geschätzte Zeit bis Production-Ready:** 1-2 Stunden