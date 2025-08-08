# 💰 Potenzialberechnung

**Sprint:** 2  
**Service:** `PotentialCalculationService`  
**Status:** 🆕 MVP mit Erfahrungswerten  

---

## 📍 Navigation
**← Zurück:** [Sprint 2 Overview](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/README.md)  
**↔️ Verwandt:** [Field Catalog Extension](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/implementation/FIELD_CATALOG_EXTENSION.md)  
**🧪 Test:** [Calculation Tests](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/tests/POTENTIAL_CALCULATION_TESTS.md)

---

## 🎯 MVP-Ansatz: Erfahrungswerte

### Basis-Formel
```typescript
const calculatePotential = (customer: CustomerData): PotentialResult => {
  let basePotential = 0;
  
  // Branchenspezifische Basis
  switch (customer.industry) {
    case 'hotel':
      basePotential = calculateHotelPotential(customer);
      break;
    case 'hospital':
      basePotential = calculateHospitalPotential(customer);
      break;
    case 'restaurant':
      basePotential = calculateRestaurantPotential(customer);
      break;
  }
  
  // Multiplikatoren
  if (customer.hasStaffingIssues) {
    basePotential *= 1.3; // +30% bei Personalmangel
  }
  
  if (customer.isChainCustomer) {
    basePotential *= customer.totalLocationsEU * 0.8; // 80% Rollout
  }
  
  return {
    annual: Math.round(basePotential),
    confidence: 'Erfahrungswert',
    calculation: getCalculationDetails(customer)
  };
};
```

---

## 🏨 Hotel-Berechnung

```typescript
const calculateHotelPotential = (hotel: HotelData): number => {
  let potential = 0;
  
  // Frühstück
  if (hotel.offersBreakfast) {
    const breakfastGuests = hotel.breakfastGuestsPerDay || 0;
    const avgPrice = hotel.breakfastWarm ? 3.50 : 2.00; // €/Gast
    potential += breakfastGuests * avgPrice * 365 * 0.7; // 70% Auslastung
  }
  
  // Events
  if (hotel.offersEvents) {
    const eventCapacity = hotel.eventCapacity || 0;
    const eventsPerMonth = 4; // Durchschnitt
    const avgPerPerson = 15; // € pro Person
    potential += eventCapacity * eventsPerMonth * 12 * avgPerPerson;
  }
  
  // Restaurant
  if (hotel.hasRestaurant) {
    potential += 50000; // Pauschale für À la carte
  }
  
  return potential;
};
```

---

## 🏥 Krankenhaus-Berechnung

```typescript
const calculateHospitalPotential = (hospital: HospitalData): number => {
  const beds = hospital.bedCount || 0;
  const occupancy = 0.85; // 85% Auslastung
  const mealsPerDay = 3;
  const avgMealCost = 4.50; // € pro Mahlzeit
  
  let basePotential = beds * occupancy * 365 * mealsPerDay * avgMealCost;
  
  // Finanzierungsmodell berücksichtigen
  if (hospital.primaryFinancing === 'public') {
    basePotential *= 0.6; // GKV = niedrigere Margen
  } else if (hospital.primaryFinancing === 'private') {
    basePotential *= 1.2; // Privat = höhere Margen
  }
  
  return basePotential;
};
```

---

## 🎯 Quick-Win-Generator

```typescript
const generateQuickWins = (customer: CustomerData): QuickWin[] => {
  const quickWins: QuickWin[] = [];
  
  if (customer.hasStaffingIssues) {
    quickWins.push({
      title: 'Cook&Fresh® löst Personalproblem',
      description: 'Spart 1-2 Vollzeitstellen in der Küche',
      products: ['Convenience-Sortiment'],
      impact: 'HIGH'
    });
  }
  
  if (customer.hasQualityIssues) {
    quickWins.push({
      title: '40 Tage Haltbarkeit',
      description: 'Reduziert Food Waste um 30-50%',
      products: ['Alle Cook&Fresh® Produkte'],
      impact: 'MEDIUM'
    });
  }
  
  return quickWins.slice(0, 3); // Max 3 anzeigen
};
```

---

## 📊 UI-Integration

```tsx
// In Step 2 des Wizards
const PotentialDisplay: React.FC = () => {
  const potential = calculatePotential(formData);
  const quickWins = generateQuickWins(formData);
  
  return (
    <Box>
      <Typography variant="h6">
        💰 Ihr Potenzial mit Freshfoodz:
      </Typography>
      <Typography variant="h4">
        {potential.annual.toLocaleString('de-DE')} €/Jahr
      </Typography>
      <Typography variant="caption">
        ({potential.confidence})
      </Typography>
      
      {quickWins.map(win => (
        <Alert severity="success" key={win.title}>
          <AlertTitle>{win.title}</AlertTitle>
          {win.description}
        </Alert>
      ))}
    </Box>
  );
};
```

---

## 🔮 Zukunft (Sprint 3+)

- KI-basierte Berechnung
- Historische Daten einbeziehen
- Saisonalität berücksichtigen
- Wettbewerbsvergleich

---

## 🔗 Weiterführende Links

**Field Definitions:** [Field Catalog Extension](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/implementation/FIELD_CATALOG_EXTENSION.md)  
**Pain Points:** [Pain Point Mapping](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/implementation/PAIN_POINT_MAPPING.md)