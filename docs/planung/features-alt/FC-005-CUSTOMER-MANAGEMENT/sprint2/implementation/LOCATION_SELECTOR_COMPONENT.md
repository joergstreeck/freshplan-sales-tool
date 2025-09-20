# 🏢 LocationSelector Component Specification

**Sprint:** 2  
**Component:** LocationSelector  
**Status:** 📋 Zu implementieren  
**Priorität:** Hoch

---

## 📍 Navigation
**← Zurück:** [Step 2 V2](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/wizard/STEP2_ANGEBOT_PAINPOINTS_V2.md)  
**→ Verwandt:** [Field Catalog Extension](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/implementation/FIELD_CATALOG_EXTENSION.md)

---

## 🎯 Zweck

Ermöglicht die Auswahl eines Standorts oder "Alle Standorte" für die Erfassung der Angebotsstruktur.

---

## 🔧 Component Interface

```typescript
interface LocationSelectorProps {
  /** Verfügbare Standorte aus Customer Entity */
  locations: CustomerLocation[];
  
  /** Aktuell ausgewählter Standort */
  selectedLocationId: string | 'all';
  
  /** Callback bei Standortauswahl */
  onLocationChange: (locationId: string | 'all') => void;
  
  /** Option "Für alle übernehmen" aktiviert? */
  applyToAll: boolean;
  
  /** Callback für "Für alle übernehmen" */
  onApplyToAllChange: (value: boolean) => void;
  
  /** Anzahl Gesamt-Standorte (für Display) */
  totalLocations: number;
}
```

---

## 🎨 UI Design

```tsx
<Box sx={{ mb: 3 }}>
  <Typography variant="h6" gutterBottom>
    📍 Angebotsstruktur pro Standort
  </Typography>
  
  <FormControl fullWidth sx={{ mb: 2 }}>
    <InputLabel>Für welchen Standort erfassen?</InputLabel>
    <Select 
      value={selectedLocationId}
      onChange={(e) => onLocationChange(e.target.value)}
    >
      <MenuItem value="all">
        <strong>Alle {totalLocations} Standorte gleich</strong>
      </MenuItem>
      <Divider />
      {locations.map((loc, index) => (
        <MenuItem key={loc.id} value={loc.id}>
          Standort {index + 1}: {loc.name || loc.city}
          {loc.isMainLocation && " (Hauptsitz)"}
        </MenuItem>
      ))}
      <Divider />
      <MenuItem value="new">
        <AddIcon sx={{ mr: 1 }} />
        Neuer Standort
      </MenuItem>
    </Select>
  </FormControl>
  
  {selectedLocationId !== 'all' && (
    <FormControlLabel
      control={
        <Checkbox
          checked={applyToAll}
          onChange={(e) => onApplyToAllChange(e.target.checked)}
        />
      }
      label={`Für alle ${totalLocations} Standorte übernehmen`}
    />
  )}
</Box>
```

---

## 📊 State Management

```typescript
// In customerOnboardingStore
interface LocationServiceState {
  selectedLocationId: string | 'all';
  applyToAll: boolean;
  locationServices: Map<string, ServiceData>;
}

// Helper Functions
const getServiceDataForLocation = (locationId: string) => {
  if (applyToAll || selectedLocationId === 'all') {
    return locationServices.get('all');
  }
  return locationServices.get(locationId) || {};
};

const saveServiceData = (data: ServiceData) => {
  if (selectedLocationId === 'all' || applyToAll) {
    // Speichere für alle Standorte
    locations.forEach(loc => {
      locationServices.set(loc.id, data);
    });
    locationServices.set('all', data);
  } else {
    // Speichere nur für ausgewählten Standort
    locationServices.set(selectedLocationId, data);
  }
};
```

---

## 🏗️ Implementierungs-Schritte

### 1. Component erstellen:
```
frontend/src/features/customers/components/location/LocationSelector.tsx
```

### 2. Store erweitern:
```typescript
// customerOnboardingStore.ts
setSelectedLocation: (locationId: string | 'all') => void;
setApplyToAll: (value: boolean) => void;
getLocationServices: (locationId: string) => ServiceData;
```

### 3. Integration in Step2:
```tsx
<LocationSelector
  locations={customerData.locations || []}
  selectedLocationId={selectedLocationId}
  onLocationChange={setSelectedLocation}
  applyToAll={applyToAll}
  onApplyToAllChange={setApplyToAll}
  totalLocations={customerData.totalLocationsEU || 1}
/>
```

### 4. Validierung:
- Bei Ketten mindestens 1 Standort erfasst
- Warnung bei ungespeicherten Änderungen beim Wechsel

---

## 🎯 UX-Features

### Smart Defaults:
- Bei nur 1 Standort: Automatisch ausgewählt
- Bei Ketten: "Alle Standorte" vorausgewählt

### Visuelle Hinweise:
- ✅ Grüner Haken bei bereits erfassten Standorten
- 🔴 Roter Punkt bei noch offenen Standorten
- 📊 Progress Indicator: "3 von 45 Standorten erfasst"

### Keyboard Navigation:
- `Tab` durch Standorte
- `Enter` zur Auswahl
- `Ctrl+A` für "Alle Standorte"

---

## ⚠️ Edge Cases

1. **Keine Standorte:** 
   - Automatisch 1 Hauptstandort anlegen
   - Hinweis anzeigen

2. **Sehr viele Standorte (>100):**
   - Suche/Filter einbauen
   - Pagination überlegen

3. **Standort löschen:**
   - Bestätigung erforderlich
   - Services-Daten behalten oder löschen?

---

## 🔗 Backend-Integration

```typescript
// API Calls
GET /api/customers/{id}/locations
POST /api/customers/{id}/locations
PUT /api/customers/{id}/locations/{locationId}/services

// Batch Update für "Alle übernehmen"
POST /api/customers/{id}/locations/batch-services
{
  "applyToAll": true,
  "services": { ... }
}
```

---

**Nächster Schritt:** Diese Komponente muss vor der Step 2 Umstrukturierung implementiert werden!