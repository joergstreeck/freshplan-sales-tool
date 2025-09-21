# 🛠️ Field Theme System - Implementation Guide

**Datum:** 28.07.2025  
**Status:** 📝 Implementation Guide  
**Sprint:** Sprint 2 - Customer UI Integration  
**Scope:** CustomerOnboardingWizard Implementation  

## 📍 Navigation
**← Konzept:** [Field Theme System Prototype](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/prototypes/FIELD_THEME_SYSTEM_PROTOTYPE.md)  
**← Sprint 2:** [Sprint 2 Overview](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/README.md)  
**↑ Übergeordnet:** [FC-005 Customer Management](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/README.md)  

---

## 🎯 Implementierung für Sprint 2

### Schritt 1: Theme-Dateien erstellen (30 Min)

```bash
# Theme-Struktur anlegen
mkdir -p frontend/src/features/customers/theme
mkdir -p frontend/src/features/customers/utils

# Dateien erstellen
touch frontend/src/features/customers/theme/fieldTheme.ts
touch frontend/src/features/customers/utils/fieldSizeCalculator.ts
```

### Schritt 2: Field Theme Core (fieldTheme.ts)

```typescript
// frontend/src/features/customers/theme/fieldTheme.ts
import type { GridSize } from '@mui/material';

export interface FieldSizeTheme {
  compact: GridSize;
  small: GridSize;
  medium: GridSize;
  large: GridSize;
  full: GridSize;
}

// Design-Standard für CustomerOnboardingWizard
export const FIELD_THEME: FieldSizeTheme = {
  compact: { xs: 6, sm: 4, md: 2 },
  small: { xs: 12, sm: 6, md: 3 },
  medium: { xs: 12, sm: 6, md: 4 },
  large: { xs: 12, sm: 12, md: 8 },
  full: { xs: 12, sm: 12, md: 12 }
};

// Erweitert für zukünftige Nutzung
export const FIELD_THEME_EXTENDED = {
  ...FIELD_THEME,
  xlarge: { xs: 12, sm: 12, md: 10 },
  tiny: { xs: 4, sm: 3, md: 1 }
};
```

### Schritt 3: Size Calculator (fieldSizeCalculator.ts)

```typescript
// frontend/src/features/customers/utils/fieldSizeCalculator.ts
import type { FieldDefinition } from '../types/field.types';
import type { GridSize } from '@mui/material';
import { FIELD_THEME } from '../theme/fieldTheme';

// Sprint 2: Optimiert für CustomerOnboardingWizard Felder
const CUSTOMER_FIELD_MAPPINGS: Record<string, keyof typeof FIELD_THEME> = {
  // Kompakte Felder
  'customerNumber': 'small',
  'postalCode': 'compact',
  'houseNumber': 'compact',
  
  // Standard Felder
  'salutation': 'small',
  'firstName': 'medium',
  'lastName': 'medium',
  'city': 'medium',
  'phone': 'medium',
  
  // Große Felder
  'companyName': 'large',
  'email': 'large',
  'street': 'large',
  'website': 'large',
  
  // Volle Breite
  'notes': 'full',
  'internalNotes': 'full'
};

export function getFieldSize(field: FieldDefinition): GridSize {
  // 1. Expliziter sizeHint hat Vorrang
  if (field.sizeHint && FIELD_THEME[field.sizeHint]) {
    return FIELD_THEME[field.sizeHint];
  }
  
  // 2. Customer-spezifische Mappings
  if (CUSTOMER_FIELD_MAPPINGS[field.key]) {
    return FIELD_THEME[CUSTOMER_FIELD_MAPPINGS[field.key]];
  }
  
  // 3. Auto-Berechnung nach fieldType
  switch (field.fieldType) {
    case 'number':
      return FIELD_THEME.compact;
    case 'select':
      return field.options?.length > 10 ? FIELD_THEME.medium : FIELD_THEME.small;
    case 'email':
      return FIELD_THEME.large;
    case 'textarea':
      return FIELD_THEME.full;
    case 'text':
    default:
      // Nach maxLength
      if (field.maxLength <= 5) return FIELD_THEME.compact;
      if (field.maxLength <= 20) return FIELD_THEME.small;
      if (field.maxLength <= 50) return FIELD_THEME.medium;
      return FIELD_THEME.large;
  }
}
```

### Schritt 4: DynamicFieldRenderer anpassen

```typescript
// frontend/src/features/customers/components/fields/DynamicFieldRenderer.tsx
import { Grid } from '@mui/material';
import type { FieldDefinition } from '../../types/field.types';
import { getFieldSize } from '../../utils/fieldSizeCalculator';
import { FieldWrapper } from './FieldWrapper';

interface DynamicFieldRendererProps {
  field: FieldDefinition;
  value: any;
  onChange: (key: string, value: any) => void;
  errors?: Record<string, string>;
  disabled?: boolean;
}

export function DynamicFieldRenderer({ 
  field, 
  ...props 
}: DynamicFieldRendererProps) {
  // NEU: Theme-basierte Größe statt hardcoded gridSize
  const gridProps = getFieldSize(field);
  
  return (
    <Grid size={gridProps}>
      <FieldWrapper field={field} {...props} />
    </Grid>
  );
}
```

### Schritt 5: Field Catalog optimieren

```json
// frontend/src/features/customers/data/fieldCatalog.json
// Beispiel-Anpassungen für Sprint 2
{
  "fields": [
    {
      "key": "customerNumber",
      "label": "Kundennummer",
      "fieldType": "text",
      "maxLength": 20,
      // sizeHint optional - wird automatisch als "small" erkannt
    },
    {
      "key": "companyName",
      "label": "Firmenname",
      "fieldType": "text",
      "maxLength": 100,
      // Automatisch "large" durch Mapping
    },
    {
      "key": "postalCode",
      "label": "PLZ",
      "fieldType": "text",
      "maxLength": 5,
      "sizeHint": "compact" // Explizit für Klarheit
    }
  ]
}
```

### Schritt 6: Tests schreiben

```typescript
// frontend/src/features/customers/utils/__tests__/fieldSizeCalculator.test.ts
import { describe, it, expect } from 'vitest';
import { getFieldSize } from '../fieldSizeCalculator';
import { FIELD_THEME } from '../../theme/fieldTheme';

describe('fieldSizeCalculator', () => {
  it('should return compact size for PLZ field', () => {
    const field = {
      key: 'postalCode',
      fieldType: 'text',
      maxLength: 5
    };
    expect(getFieldSize(field)).toEqual(FIELD_THEME.compact);
  });
  
  it('should return large size for email field', () => {
    const field = {
      key: 'email',
      fieldType: 'email',
      maxLength: 100
    };
    expect(getFieldSize(field)).toEqual(FIELD_THEME.large);
  });
  
  it('should respect explicit sizeHint', () => {
    const field = {
      key: 'customField',
      fieldType: 'text',
      sizeHint: 'full'
    };
    expect(getFieldSize(field)).toEqual(FIELD_THEME.full);
  });
});
```

---

## 📊 Erwartete Ergebnisse in Sprint 2

### CustomerOnboardingWizard vorher:
- Alle Felder gleich groß (33%)
- 3-4x Scrollen nötig
- PLZ-Feld verschwendet Platz

### CustomerOnboardingWizard nachher:
- PLZ kompakt (16%)
- E-Mail/Firma groß (66%)
- Alles auf einen Blick
- Mobile-optimiert

---

## ✅ Sprint 2 Checklist

- [ ] Theme-Dateien angelegt
- [ ] fieldTheme.ts implementiert
- [ ] fieldSizeCalculator.ts implementiert
- [ ] DynamicFieldRenderer angepasst
- [ ] Unit Tests geschrieben
- [ ] CustomerOnboardingWizard getestet
- [ ] Screenshots erstellt
- [ ] Performance gemessen

---

## 🚀 Nach Sprint 2

Diese Implementation dient als **Referenz für alle zukünftigen Module**:
- Lead-Erfassung kann Field Theme nutzen
- Opportunity Forms profitieren
- Calculator Modal wird optimiert

**Wichtig:** Weitere Module werden NICHT in Sprint 2 migriert!