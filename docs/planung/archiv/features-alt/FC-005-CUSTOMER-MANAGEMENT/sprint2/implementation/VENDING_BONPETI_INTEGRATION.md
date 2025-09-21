# 🤖 Vending & bonPeti Integration

**Sprint:** 2  
**Feature:** Zusatzgeschäft Automaten  
**Status:** 🆕 Freshfoodz-spezifisch  

---

## 📍 Navigation
**← Zurück:** [Sprint 2 Overview](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/README.md)  
**↔️ Verwandt:** [Pain Point Mapping](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/implementation/PAIN_POINT_MAPPING.md)  
**📊 Field Catalog:** [Field Catalog Extension](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/implementation/FIELD_CATALOG_EXTENSION.md)

---

## 🎯 bonPeti - Die Vending-Marke von Freshfoodz

### Was ist bonPeti?
- Händlermarke speziell für Automaten
- Smart-Fridge-kompatibel
- Unbemannte Verkaufsstellen
- 24/7 Verfügbarkeit

---

## 📊 Vending-Felder im Wizard

### In Step 2 - Angebotsstruktur
```typescript
interface VendingFields {
  // Basis-Erfassung
  hasVending: boolean;              // "Haben Sie Automaten?"
  vendingType?: ('snack' | 'drink' | 'fresh' | 'smart-fridge')[];
  vendingLocations?: number;        // "Anzahl Automaten-Standorte"
  
  // Interesse & Potenzial
  vendingInterest?: boolean;        // "Interesse an Automaten?"
  currentVendingProvider?: string;  // "Aktueller Betreiber"
  vendingSatisfaction?: 1-5;        // "Zufriedenheit"
  
  // Pain Points
  vendingChallenges?: string[];     // ["Leere Automaten", "Schlechte Qualität"]
}
```

---

## 💡 Vending als Verkaufschance

### Wenn hasVending = false && vendingInterest = true:
```typescript
const vendingOpportunity = {
  title: 'bonPeti Automaten-Konzept',
  description: 'Zusatzumsatz ohne Personaleinsatz',
  benefits: [
    '24/7 Verfügbarkeit für Mitarbeiter/Gäste',
    'Kein Personal nötig',
    'Flexible Bestückung',
    'Smart-Fridge mit App-Anbindung möglich'
  ],
  products: ['bonPeti Sortiment'],
  potential: calculateVendingPotential(customer)
};
```

### Potenzialberechnung Vending
```typescript
const calculateVendingPotential = (customer: any): number => {
  let basePotential = 0;
  
  // Hotels: Lobby, Etagen, Fitness
  if (customer.industry === 'hotel') {
    const potentialLocations = Math.ceil(customer.roomCount / 50);
    basePotential = potentialLocations * 12000; // €/Jahr pro Automat
  }
  
  // Betriebe: Kantinen-Ergänzung
  if (customer.industry === 'company') {
    const employees = customer.employeeCount || 0;
    const automats = Math.ceil(employees / 200);
    basePotential = automats * 15000;
  }
  
  return basePotential;
};
```

---

## 🎨 UI-Integration

### Conditional Display
```tsx
{formData.hasVending === false && (
  <Alert severity="info">
    <AlertTitle>💡 Zusatzgeschäft möglich</AlertTitle>
    Mit bonPeti Automaten erschließen Sie neue Umsatzquellen
    ohne zusätzliches Personal. Interesse?
    <Checkbox 
      checked={formData.vendingInterest}
      onChange={(e) => setValue('vendingInterest', e.target.checked)}
    />
  </Alert>
)}
```

### Smart Fridge Teaser
```tsx
{formData.vendingInterest && (
  <Box sx={{ mt: 2, p: 2, bgcolor: 'primary.light' }}>
    <Typography variant="h6">🤖 Smart Fridge Option</Typography>
    <Typography>
      Moderne Kühlautomaten mit App-Bezahlung und 
      automatischer Nachbestellung. Perfekt für Hotels 
      und moderne Büros.
    </Typography>
  </Box>
)}
```

---

## 📊 Integration in Potenzialberechnung

```typescript
// In der Gesamt-Potenzialberechnung
const totalPotential = 
  corePotential + 
  (customer.vendingInterest ? calculateVendingPotential(customer) : 0);

// Quick Win wenn Vending-Interesse
if (customer.vendingInterest && !customer.hasVending) {
  quickWins.push({
    title: 'bonPeti Automaten einführen',
    description: `${Math.round(vendingPotential/1000)}k€ Zusatzumsatz möglich`,
    impact: 'MEDIUM'
  });
}
```

---

## 🔗 Weiterführende Links

**Field Definitions:** [Field Catalog Extension](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/implementation/FIELD_CATALOG_EXTENSION.md)  
**Potenzial gesamt:** [Potential Calculation](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/implementation/POTENTIAL_CALCULATION.md)