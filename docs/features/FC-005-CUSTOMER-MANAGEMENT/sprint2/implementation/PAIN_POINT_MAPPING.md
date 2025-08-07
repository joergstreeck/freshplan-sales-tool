# 🎯 Pain Point Mapping

**Sprint:** 2  
**Service:** `PainPointSolutionService`  
**Status:** 🆕 Freshfoodz-spezifisch  

---

## 📍 Navigation
**← Zurück:** [Sprint 2 Overview](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/README.md)  
**↔️ Verwandt:** [Potential Calculation](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/implementation/POTENTIAL_CALCULATION.md)  
**📊 UI:** [Step 2 Wizard](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/wizard/STEP2_ANGEBOT_PAINPOINTS.md)

---

## 🎯 Pain Point → Solution Mapping

### Kernprinzip
Jeder Pain Point wird zur Verkaufschance mit konkreter Freshfoodz-Lösung.

---

## 🏢 Universelle Pain Points

```typescript
const universalPainPoints: PainPointMap = {
  'fachkraeftemangel': {
    id: 'staffing-shortage',
    label: 'Fachkräftemangel in der Küche',
    solution: {
      title: 'Cook&Fresh® - Keine Köche nötig!',
      description: '15 Minuten Regenerierung im Wasserbad',
      products: [
        'Fertiggerichte',
        'Frühstückskomponenten',
        'Convenience-Sortiment'
      ],
      benefit: 'Spart 1-2 Vollzeitstellen',
      roi: 'ca. 80.000 €/Jahr Personalkosten'
    }
  },
  
  'schwankende-qualitaet': {
    id: 'quality-fluctuation',
    label: 'Schwankende Qualität',
    solution: {
      title: 'Immer gleiche Premium-Qualität',
      description: 'Standardisierte Produktion in Berlin',
      products: ['Alle Cook&Fresh® Produkte'],
      benefit: 'Bessere Gästebewertungen',
      proof: '640 Mitarbeiter, 250.000 Portionen täglich'
    }
  },
  
  'food-waste': {
    id: 'food-waste',
    label: 'Hohe Warenverluste',
    solution: {
      title: '40 Tage Haltbarkeit bei 7°C',
      description: 'Patentiertes Verfahren ohne Konservierung',
      products: ['Alle Cook&Fresh® Produkte'],
      benefit: '30-50% weniger Abfall',
      usp: 'Längste Haltbarkeit am Markt'
    }
  },
  
  'diaet-anforderungen': {
    id: 'dietary-requirements',
    label: 'Viele Diät-/Sonderwünsche',
    solution: {
      title: 'Komplettes Diätkost-Sortiment',
      description: 'Glutenfrei, laktosefrei, vegan verfügbar',
      products: [
        'Diätmenüs',
        'Allergenfreie Produkte',
        'Vegane Linie'
      ],
      benefit: 'Alle Gäste zufrieden',
      certification: 'Bio-zertifiziert'
    }
  },
  
  'personalkosten': {
    id: 'personnel-costs',
    label: 'Zu hohe Personalkosten',
    solution: {
      title: 'Reduzierte Küchenbesetzung möglich',
      description: 'Einfache Regenerierung ohne Fachpersonal',
      products: ['Ready-to-Serve Produkte'],
      benefit: '-40% Personalkosten Küche',
      example: 'Kunde XY spart 120.000€/Jahr'
    }
  },
  
  'planungsunsicherheit': {
    id: 'planning-uncertainty',
    label: 'Planungsunsicherheit',
    solution: {
      title: 'Flexible Bestellmengen',
      description: 'Lange Haltbarkeit = bessere Planung',
      products: ['Gesamtes Sortiment'],
      benefit: 'Keine Überproduktion',
      service: 'Genussberater unterstützt'
    }
  }
};
```

---

## 🏨 Branchenspezifische Pain Points

### Hotels
```typescript
const hotelPainPoints: PainPointMap = {
  'fruehstueck-kosten': {
    label: 'Frühstück zu teuer/aufwendig',
    solution: {
      title: 'Warme Frühstückskomponenten',
      products: ['Rührei', 'Bacon', 'Würstchen'],
      benefit: '-30% Kosten bei besserer Qualität'
    }
  },
  'wochenend-besetzung': {
    label: 'Wochenend-/Feiertagsbesetzung',
    solution: {
      title: 'Cook&Fresh® für Wochenenden',
      description: 'Volle Qualität ohne Fachpersonal',
      benefit: 'Kein Wochenend-Zuschlag'
    }
  }
};
```

### Krankenhäuser
```typescript
const hospitalPainPoints: PainPointMap = {
  'hygiene-anforderungen': {
    label: 'Strenge Hygienevorschriften',
    solution: {
      title: 'HACCP-konforme Produktion',
      certification: 'EU-zertifiziert',
      benefit: 'Rechtssicherheit garantiert'
    }
  },
  '24-7-betrieb': {
    label: '24/7 Verpflegung nötig',
    solution: {
      title: 'Lange Haltbarkeit ideal für Nachtschicht',
      products: ['Suppen', 'Eintöpfe', 'Snacks'],
      benefit: 'Immer verfügbar'
    }
  }
};
```

---

## 🎨 UI-Integration

```tsx
const PainPointSelector: React.FC = () => {
  const [selected, setSelected] = useState<string[]>([]);
  
  const handleToggle = (painPointId: string) => {
    setSelected(prev => 
      prev.includes(painPointId) 
        ? prev.filter(id => id !== painPointId)
        : [...prev, painPointId]
    );
  };
  
  return (
    <FormGroup>
      {Object.entries(universalPainPoints).map(([key, point]) => (
        <FormControlLabel
          key={key}
          control={
            <Checkbox
              checked={selected.includes(key)}
              onChange={() => handleToggle(key)}
            />
          }
          label={
            <Box>
              <Typography>{point.label}</Typography>
              {selected.includes(key) && (
                <Typography variant="caption" color="success.main">
                  ✓ {point.solution.title}
                </Typography>
              )}
            </Box>
          }
        />
      ))}
    </FormGroup>
  );
};
```

---

## 📊 Solution Display

```tsx
const SolutionSummary: React.FC<{painPoints: string[]}> = ({painPoints}) => {
  const solutions = painPoints.map(pp => universalPainPoints[pp].solution);
  
  return (
    <Box>
      <Typography variant="h6">✨ Ihre Quick Wins:</Typography>
      {solutions.map((sol, idx) => (
        <Alert severity="success" key={idx}>
          <AlertTitle>{sol.title}</AlertTitle>
          {sol.benefit}
        </Alert>
      ))}
    </Box>
  );
};
```

---

## 🔗 Weiterführende Links

**UI Integration:** [Step 2 Wizard](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/wizard/STEP2_ANGEBOT_PAINPOINTS.md)  
**Potenzial:** [Potential Calculation](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/implementation/POTENTIAL_CALCULATION.md)