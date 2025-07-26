# Customer Onboarding Wizard Steps

**Stand:** 26.07.2025  
**Status:** ✅ Implementiert  
**Modul:** FC-005 Customer Management - Wizard Steps

## 📋 Übersicht

Die Step-Komponenten bilden den mehrstufigen Onboarding-Prozess für neue Kunden. Jeder Step hat spezifische Funktionen und Validierungen.

## 🏗️ Step-Struktur

### 1. CustomerDataStep
**Zweck:** Erfassung der Kundenstammdaten  
**Trigger:** Immer angezeigt (Schritt 1)

**Features:**
- Dynamische Felder basierend auf Field Catalog
- Industry-spezifische Felder
- Real-time Validation
- Chain Customer Trigger für weitere Steps

### 2. LocationsStep  
**Zweck:** Verwaltung von Standorten für Ketten-Kunden  
**Trigger:** Nur wenn `chainCustomer === 'ja'`

**Features:**
- Mehrere Standorte verwalten
- Industry-spezifische Standort-Felder
- Drag & Drop Sortierung (geplant)
- Mindestens 1 Standort erforderlich

### 3. DetailedLocationsStep
**Zweck:** Erfassung von Detail-Standorten innerhalb der Hauptstandorte  
**Trigger:** Optional nach LocationsStep

**Features:**
- Batch-Import für schnelle Erfassung
- Industry-spezifische Templates
- Kategorisierung (Restaurant, Station, etc.)
- Kopieren zwischen Standorten
- Accordion-basierte UI für Übersichtlichkeit

## 🔧 Verwendung im Wizard

```typescript
import { 
  CustomerDataStep, 
  LocationsStep, 
  DetailedLocationsStep 
} from '@/features/customers/components/steps';

// In CustomerOnboardingWizard
const steps = [
  {
    label: 'Kundendaten',
    component: CustomerDataStep,
    isVisible: true
  },
  {
    label: 'Standorte',
    component: LocationsStep,
    isVisible: customerData.chainCustomer === 'ja'
  },
  {
    label: 'Detail-Standorte',
    component: DetailedLocationsStep,
    isVisible: customerData.chainCustomer === 'ja' && locations.length > 0
  }
];
```

## 📊 DetailedLocationsStep Features

### Industry Templates

**Hotel:**
- Restaurant Haupthaus
- Frühstücksraum
- Bar/Lounge
- Poolbar
- Bankett/Konferenz

**Krankenhaus:**
- Cafeteria Haupteingang
- Personalrestaurant
- Stationen (1A, 1B, 2A, etc.)
- Intensivstation
- Kiosk

**Seniorenresidenz:**
- Speisesaal
- Wohnbereiche
- Demenzbereich
- Cafeteria

### Kategorien

| Kategorie | Icon | Verwendung |
|-----------|------|------------|
| restaurant | 🍽️ | Hauptrestaurants, Speisesäle |
| cafeteria | ☕ | Cafeterien, Bistros |
| kiosk | 🏪 | Kleine Verkaufsstellen |
| station | 🏥 | Krankenhaus-Stationen, Wohnbereiche |
| kitchen | 👨‍🍳 | Küchen, Produktionsbereiche |
| storage | 📦 | Lager, Vorratsräume |
| other | 📍 | Sonstige Bereiche |

### Batch-Add Dialog

Ermöglicht schnelle Erfassung mehrerer Detail-Standorte:

```typescript
interface BatchAddFields {
  name: string;        // Pflichtfeld
  category: string;    // Pflichtfeld
  floor?: string;      // Optional (z.B. "1. OG")
  capacity?: number;   // Optional (Sitzplätze/Betten)
}
```

## 🔗 Store Integration

Der DetailedLocationsStep nutzt folgende Store-Funktionen:

```typescript
const {
  detailedLocations,
  addDetailedLocation,
  updateDetailedLocation,
  removeDetailedLocation,
  addBatchDetailedLocations
} = useCustomerOnboardingStore();
```

## 🎨 UI/UX Guidelines

1. **Accordion Pattern:** Jeder Hauptstandort ist ein Accordion
2. **Visual Feedback:** Chips zeigen Anzahl der Detail-Standorte
3. **Icons:** Kategorien haben spezifische Icons für schnelle Erkennung
4. **Templates:** Industry-spezifische Vorlagen für schnellen Start
5. **Batch Operations:** Mehrere Einträge gleichzeitig erstellen

## ✅ Validierung

- Mindestens ein Name pro Detail-Standort
- Kategorie ist Pflichtfeld
- Kapazität muss positive Zahl sein
- Keine doppelten Namen innerhalb eines Standorts (Warning)

## 📚 Weiterführende Dokumentation

- [Component Architecture](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/03-FRONTEND/01-components.md)
- [State Management](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/03-FRONTEND/02-state-management.md)
- [Data Model](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/01-TECH-CONCEPT/03-data-model.md)
- [Implementation Checklist](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/08-IMPLEMENTATION/README.md)