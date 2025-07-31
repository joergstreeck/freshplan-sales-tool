# 🏢 Step 4: Angebot & Leistungen je Filiale

**Status:** 🆕 Komplett neu  
**Component:** Step4AngebotServices.tsx  
**Theme:** AdaptiveFormContainer

---

## 📍 Navigation
**← Zurück:** [Step 3 V2](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/wizard/STEP3_ANSPRECHPARTNER_V2.md)  
**⬆️ Wizard:** [Wizard Structure V3](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/WIZARD_STRUCTURE_V3.md)  
**🏁 Finish:** Wizard abschließen

---

## 🎯 Fokus: Standortspezifische Services

### Kern-Features:
- Standort-für-Standort Erfassung
- Progress-Tracking (X von Y erfasst)
- "Kopieren von" Funktionalität
- "Für alle restlichen übernehmen"
- Speichern & später fortsetzen

### Einzelbetrieb = Vereinfachte UI:
- Keine Standort-Navigation
- Direkt Service-Felder
- Titel ohne "je Filiale"

---

## 🖼️ UI Design - Ketten-Modus

```
┌─────────────────────────────────────────────┐
│ Schritt 4: Angebot & Leistungen je Filiale   │
├─────────────────────────────────────────────┤
│                                              │
│ 📍 Standort-Fortschritt                      │
│ ████████░░░░░░░ 3 von 8 erfasst            │
│                                              │
│ ✅ Berlin Hauptsitz    ✅ München           │
│ ✅ Hamburg            ⏳ Köln (aktuell)      │
│ ○ Frankfurt          ○ Stuttgart            │
│ ○ Düsseldorf         ○ Leipzig              │
│                                              │
│ Aktueller Standort:                          │
│ ┌────────────────────┬─────────────────┐    │
│ │ Köln              ▼│ Kopieren von:  ▼│    │
│ └────────────────────┴─────────────────┘    │
│                      └─ Berlin Hauptsitz     │
│                      └─ München              │
│                      └─ Hamburg              │
│                                              │
│ ☑ Für alle restlichen Standorte übernehmen  │
│                                              │
│ ┌─── AdaptiveFormContainer ─────────────┐   │
│ │ 🍳 Frühstücksgeschäft                 │   │
│ │ [✓] Frühstück      [✓] Warme         │   │
│ │     angeboten          Komponenten    │   │
│ │                                        │   │
│ │ [Frühstücksgäste/Tag: 120          ]  │   │
│ │                                        │   │
│ │ 🍽️ Mittag- und Abendessen            │   │
│ │ [✓] Mittagessen    [ ] Abendessen    │   │
│ │                                        │   │
│ │ 🛎️ Zusatzservices                    │   │
│ │ [ ] Roomservice    [✓] Events        │   │
│ │                                        │   │
│ │ [Max. Eventgäste: 200              ]  │   │
│ └────────────────────────────────────────┘   │
│                                              │
│ [Zurück] [Speichern & später] [Weiter →]     │
└─────────────────────────────────────────────┘
```

---

## 🖼️ UI Design - Einzelbetrieb-Modus

```
┌─────────────────────────────────────────────┐
│ Schritt 4: Angebot & Leistungen             │
├─────────────────────────────────────────────┤
│                                              │
│ Was bieten Sie Ihren Gästen an?             │
│                                              │
│ ┌─── AdaptiveFormContainer ─────────────┐   │
│ │ [Gleiche Service-Felder wie oben]     │   │
│ └────────────────────────────────────────┘   │
│                                              │
│ [Zurück]              [Wizard abschließen]   │
└─────────────────────────────────────────────┘
```

---

## 📊 Component Struktur

```typescript
export const Step4AngebotServices: React.FC = () => {
  const {
    locations,
    chainCustomer,
    customerData
  } = useCustomerOnboardingStore();
  
  const {
    locationServices,
    currentLocationIndex,
    completedLocationIds,
    saveLocationServices,
    copyLocationServices,
    setCurrentLocationIndex
  } = useLocationServicesStore(); // Separater Store für Step 4
  
  // Einzelbetrieb-Check
  const isSingleLocation = chainCustomer === 'nein' || locations.length === 1;
  const currentLocation = locations[currentLocationIndex];
  
  if (isSingleLocation) {
    return <SingleLocationView location={locations[0]} />;
  }
  
  return (
    <Box>
      <Typography variant="h5">
        Schritt 4: Angebot & Leistungen je Filiale
      </Typography>
      
      {/* Progress Bar */}
      <LocationProgress
        total={locations.length}
        completed={completedLocationIds.length}
      />
      
      {/* Location Grid */}
      <LocationGrid
        locations={locations}
        currentIndex={currentLocationIndex}
        completedIds={completedLocationIds}
        onLocationClick={setCurrentLocationIndex}
      />
      
      {/* Location Selector mit Copy-Funktion */}
      <LocationNavigator
        locations={locations}
        currentIndex={currentLocationIndex}
        completedIds={completedLocationIds}
        onNavigate={setCurrentLocationIndex}
        onCopyFrom={handleCopyFrom}
      />
      
      {/* Apply to All Checkbox */}
      <FormControlLabel
        control={
          <Checkbox
            checked={applyToAll}
            onChange={(e) => setApplyToAll(e.target.checked)}
          />
        }
        label="Für alle restlichen Standorte übernehmen"
      />
      
      {/* Service Fields */}
      <ServiceFieldsContainer
        location={currentLocation}
        services={locationServices[currentLocation.id] || {}}
        onChange={handleServiceChange}
        industry={customerData.industry}
      />
      
      {/* Navigation */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', mt: 3 }}>
        <Button onClick={handleBack}>Zurück</Button>
        <Button onClick={handleSaveAndContinueLater}>
          Speichern & später
        </Button>
        <Button 
          onClick={handleNext}
          variant="contained"
          disabled={!canProceed}
        >
          {isLastLocation ? 'Wizard abschließen' : 'Weiter →'}
        </Button>
      </Box>
    </Box>
  );
};
```

---

## 🎯 ServiceFieldsContainer

```typescript
const ServiceFieldsContainer: React.FC<{
  location: CustomerLocation;
  services: LocationServiceData;
  onChange: (field: string, value: any) => void;
  industry: string;
}> = ({ location, services, onChange, industry }) => {
  
  const serviceGroups = getServiceGroupsByIndustry(industry);
  
  return (
    <AdaptiveFormContainer>
      {serviceGroups.map(group => (
        <Box key={group.id} sx={{ mb: 3 }}>
          <Typography variant="h6" gutterBottom>
            {group.icon} {group.title}
          </Typography>
          
          <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 2 }}>
            {group.fields.map(field => (
              <DynamicFieldRenderer
                key={field.key}
                field={field}
                value={services[field.key]}
                onChange={(value) => onChange(field.key, value)}
                sizeHint={field.sizeHint}
              />
            ))}
          </Box>
        </Box>
      ))}
    </AdaptiveFormContainer>
  );
};
```

---

## 🏭 Branchen-spezifische Service-Gruppen

```typescript
const SERVICE_GROUPS = {
  hotel: [
    {
      id: 'breakfast',
      title: 'Frühstücksgeschäft',
      icon: '☕',
      fields: [
        { key: 'offersBreakfast', label: 'Frühstück angeboten', type: 'checkbox' },
        { key: 'breakfastWarm', label: 'Warme Komponenten', type: 'checkbox' },
        { key: 'breakfastGuestsPerDay', label: 'Gäste/Tag', type: 'number', sizeHint: 'klein' }
      ]
    },
    {
      id: 'meals',
      title: 'Mittag- und Abendessen',
      icon: '🍽️',
      fields: [
        { key: 'offersLunch', label: 'Mittagessen', type: 'checkbox' },
        { key: 'offersDinner', label: 'Abendessen', type: 'checkbox' }
      ]
    }
  ],
  
  krankenhaus: [
    {
      id: 'patientMeals',
      title: 'Patientenverpflegung',
      icon: '🏥',
      fields: [
        { key: 'mealSystem', label: 'Verpflegungssystem', type: 'select', options: ['Cook&Serve', 'Cook&Chill', 'Tiefkühl'] },
        { key: 'bedsCount', label: 'Anzahl Betten', type: 'number' },
        { key: 'mealsPerDay', label: 'Mahlzeiten/Tag', type: 'number' }
      ]
    },
    {
      id: 'diets',
      title: 'Diätformen',
      icon: '🥗',
      fields: [
        { key: 'offersVegetarian', label: 'Vegetarisch', type: 'checkbox' },
        { key: 'offersVegan', label: 'Vegan', type: 'checkbox' },
        { key: 'offersHalal', label: 'Halal', type: 'checkbox' },
        { key: 'offersKosher', label: 'Koscher', type: 'checkbox' }
      ]
    }
  ],
  
  betriebsrestaurant: [
    {
      id: 'operation',
      title: 'Betriebszeiten',
      icon: '🏢',
      fields: [
        { key: 'operatingDays', label: 'Betriebstage/Woche', type: 'number', min: 1, max: 7 },
        { key: 'lunchGuests', label: 'Mittagsgäste/Tag', type: 'number' },
        { key: 'subsidized', label: 'Subventioniert', type: 'checkbox' }
      ]
    }
  ]
};
```

---

## 💾 Store für Step 4

```typescript
interface LocationServicesStore {
  // State
  locationServices: Record<string, LocationServiceData>;
  currentLocationIndex: number;
  completedLocationIds: string[];
  applyToAll: boolean;
  
  // Actions
  saveLocationServices: (locationId: string, services: LocationServiceData) => void;
  copyLocationServices: (fromId: string, toIds: string[]) => void;
  setCurrentLocationIndex: (index: number) => void;
  markLocationCompleted: (locationId: string) => void;
  setApplyToAll: (value: boolean) => void;
  
  // Bulk Actions
  applyToAllRemaining: () => void;
  
  // Persistence
  saveProgress: () => void;
  loadProgress: () => void;
}
```

---

## 🚀 Smart Features

### Copy-Funktion:
```typescript
const handleCopyFrom = (sourceLocationId: string) => {
  const sourceServices = locationServices[sourceLocationId];
  if (sourceServices) {
    saveLocationServices(currentLocation.id, { ...sourceServices });
    toast.success(`Services von ${getLocationName(sourceLocationId)} kopiert`);
  }
};
```

### Progress Tracking:
```typescript
const LocationProgress: React.FC<{ total: number; completed: number }> = ({ total, completed }) => {
  const percentage = (completed / total) * 100;
  
  return (
    <Box sx={{ mb: 3 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
        <Typography variant="body2">📍 Standort-Fortschritt</Typography>
        <Typography variant="body2">{completed} von {total} erfasst</Typography>
      </Box>
      <LinearProgress variant="determinate" value={percentage} />
    </Box>
  );
};
```

### Speichern & später:
```typescript
const handleSaveAndContinueLater = () => {
  saveProgress();
  toast.success('Fortschritt gespeichert. Sie können jederzeit fortfahren.');
  // Optional: Link/Code für Wiederaufnahme generieren
  const resumeCode = generateResumeCode();
  copyToClipboard(resumeCode);
};
```

---

## 🔧 Backend Integration

### CustomerLocation Entity erweitern:
```java
@Entity
@Table(name = "customer_locations")
public class CustomerLocation {
    // Bestehende Felder...
    
    // Hotel Services
    @Column(name = "offers_breakfast")
    private Boolean offersBreakfast;
    
    @Column(name = "breakfast_warm")
    private Boolean breakfastWarm;
    
    @Column(name = "breakfast_guests_per_day")
    private Integer breakfastGuestsPerDay;
    
    @Column(name = "offers_lunch")
    private Boolean offersLunch;
    
    @Column(name = "offers_dinner")
    private Boolean offersDinner;
    
    @Column(name = "offers_room_service")
    private Boolean offersRoomService;
    
    @Column(name = "offers_events")
    private Boolean offersEvents;
    
    @Column(name = "event_capacity")
    private Integer eventCapacity;
    
    // Getters/Setters...
}
```

### Migration V7:
```sql
-- V7__add_location_services.sql
ALTER TABLE customer_locations
ADD COLUMN offers_breakfast BOOLEAN DEFAULT FALSE,
ADD COLUMN breakfast_warm BOOLEAN DEFAULT FALSE,
ADD COLUMN breakfast_guests_per_day INTEGER,
ADD COLUMN offers_lunch BOOLEAN DEFAULT FALSE,
ADD COLUMN offers_dinner BOOLEAN DEFAULT FALSE,
ADD COLUMN offers_room_service BOOLEAN DEFAULT FALSE,
ADD COLUMN offers_events BOOLEAN DEFAULT FALSE,
ADD COLUMN event_capacity INTEGER;

-- Indexes für Performance
CREATE INDEX idx_location_services ON customer_locations(customer_id, offers_breakfast);
```

---

## ✅ Best Practices

### Navigation:
- Keyboard Shortcuts (← → für Standorte)
- Swipe-Gesten auf Mobile
- Mini-Map für viele Standorte

### Validierung:
- Mindestens 1 Service pro Standort
- Warnung bei identischen Services (Copy-Hint)
- Pflichtfeld-Check vor Weiter

### Performance:
- Lazy Loading bei vielen Standorten
- Debounced Auto-Save
- Optimistic Updates

---

## 🔗 Weiterführende Links

**Implementation:**
- [Location Services Implementation](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/implementation/LOCATION_SERVICES_IMPLEMENTATION.md)
- [Progress Tracking System](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/implementation/PROGRESS_TRACKING.md)

**Backend:**
- [Location Services API](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/backend/LOCATION_SERVICES_API.md)
- [Migration V7 Details](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/backend/MIGRATION_V7_DETAILS.md)