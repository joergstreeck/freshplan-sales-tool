# 📐 Adaptive Behavior Specification

**Sprint:** 2  
**Status:** 🎨 Design-Spezifikation  
**Kern-Feature:** Intelligente Feld-Größenanpassung  

---

## 📍 Navigation
**← Field Catalog:** [Field Catalog Complete](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/implementation/FIELD_CATALOG_COMPLETE.md)  
**→ Wizard Integration:** [Wizard Adaptive Integration](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/wizard/WIZARD_ADAPTIVE_INTEGRATION.md)  
**↑ Sprint 2:** [Sprint 2 Overview](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/README.md)  

---

## 🎯 Size Hint Definitionen

### Größenkategorien
```typescript
type SizeHint = 'kompakt' | 'klein' | 'mittel' | 'groß' | 'voll';

const SIZE_DEFINITIONS = {
  kompakt: {
    minWidth: 60,
    maxWidth: 80,
    baseWidth: 70,
    growWithContent: false,
    description: "PLZ, Hausnummer, kurze Codes"
  },
  klein: {
    minWidth: 100,
    maxWidth: 120,
    baseWidth: 110,
    growWithContent: false,
    description: "Zahlenfelder, kurze Selects"
  },
  mittel: {
    minWidth: 140,
    maxWidth: 180,
    baseWidth: 160,
    growWithContent: true,
    description: "Namen, Telefonnummern"
  },
  groß: {
    minWidth: 220,
    maxWidth: 280,
    baseWidth: 250,
    growWithContent: true,
    description: "Firmenname, E-Mail, Straße"
  },
  voll: {
    minWidth: '100%',
    maxWidth: '100%',
    baseWidth: '100%',
    growWithContent: false,
    description: "Textarea, lange Beschreibungen"
  }
};
```

---

## 📊 Adaptive Behavior für jeden Feldtyp

### Text-Felder
```json
{
  "adaptiveBehavior": {
    "growWithContent": true,
    "characterThresholds": {
      "compact": 5,      // bis 5 Zeichen
      "normal": 20,      // bis 20 Zeichen
      "expanded": 40     // über 40 Zeichen
    },
    "maxGrowthFactor": 1.5
  }
}
```

### Zahlen-Felder
```json
{
  "adaptiveBehavior": {
    "growWithContent": false,
    "fixedWidth": true,
    "alignRight": true
  }
}
```

### Select/Dropdown
```json
{
  "adaptiveBehavior": {
    "growWithContent": true,
    "basedOnLongestOption": true,
    "minWidthFromLabel": true
  }
}
```

### Checkboxen
```json
{
  "adaptiveBehavior": {
    "growWithContent": false,
    "inlineLabel": true,
    "groupingBehavior": "horizontal"
  }
}
```

---

## 🎨 Responsive Anpassungen

### Desktop (> 1200px)
```typescript
{
  flexBehavior: {
    itemsPerRow: "auto",        // Intelligent basierend auf Breite
    minItemsPerRow: 2,
    maxItemsPerRow: 5,
    gap: 16,
    alignLastRow: "left"
  }
}
```

### Tablet (768px - 1200px)
```typescript
{
  flexBehavior: {
    itemsPerRow: "auto",
    minItemsPerRow: 1,
    maxItemsPerRow: 3,
    gap: 12,
    forceFullWidthAt: ["groß", "voll"]
  }
}
```

### Mobile (< 768px)
```typescript
{
  flexBehavior: {
    itemsPerRow: 1,
    gap: 8,
    forceFullWidth: true,      // Alle Felder 100%
    verticalStacking: true
  }
}
```

---

## 🔧 Implementierungs-Details

### Field Size Calculator Enhanced
```typescript
export const calculateAdaptiveWidth = (
  field: FieldDefinition,
  content: string,
  viewport: ViewportSize
): number => {
  const sizeConfig = SIZE_DEFINITIONS[field.sizeHint || 'mittel'];
  
  // Mobile: Immer 100%
  if (viewport.width < 768) {
    return viewport.width - viewport.padding;
  }
  
  // Basis-Breite
  let width = sizeConfig.baseWidth;
  
  // Content-basiertes Wachstum
  if (sizeConfig.growWithContent && content) {
    const contentLength = content.length;
    const growthFactor = Math.min(
      contentLength / 20,
      field.adaptiveBehavior?.maxGrowthFactor || 1.5
    );
    width = Math.min(
      width * growthFactor,
      sizeConfig.maxWidth
    );
  }
  
  // Viewport-Constraints
  const maxAllowedWidth = viewport.width * 0.9;
  return Math.min(width, maxAllowedWidth);
};
```

### Intelligente Zeilen-Bildung
```typescript
export const createAdaptiveRows = (
  fields: FieldDefinition[],
  viewport: ViewportSize
): FieldDefinition[][] => {
  const rows: FieldDefinition[][] = [];
  let currentRow: FieldDefinition[] = [];
  let currentRowWidth = 0;
  
  fields.forEach(field => {
    const fieldWidth = calculateAdaptiveWidth(
      field, 
      '', 
      viewport
    );
    
    // Neue Zeile starten wenn nötig
    if (currentRowWidth + fieldWidth > viewport.width - 32) {
      if (currentRow.length > 0) {
        rows.push(currentRow);
        currentRow = [];
        currentRowWidth = 0;
      }
    }
    
    currentRow.push(field);
    currentRowWidth += fieldWidth + 16; // Gap
  });
  
  if (currentRow.length > 0) {
    rows.push(currentRow);
  }
  
  return rows;
};
```

---

## 📱 Touch-Optimierungen

```typescript
const TOUCH_OPTIMIZATIONS = {
  mobile: {
    minTouchTarget: 44,        // iOS Human Interface Guidelines
    inputPadding: 12,
    increasedLineHeight: 1.5,
    largerFonts: true
  },
  tablet: {
    minTouchTarget: 40,
    inputPadding: 10,
    increasedLineHeight: 1.4
  }
};
```

---

## ✅ Best Practices

1. **Immer sizeHint definieren** für neue Felder
2. **growWithContent** nur bei sinnvollen Feldern
3. **Mobile First** - Desktop erweitert, nicht umgekehrt
4. **Performance** - Keine Neuberechnung bei jedem Keystroke
5. **Accessibility** - Minimum Touch Targets einhalten

---

**→ Integration:** [Wizard Adaptive Integration](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/wizard/WIZARD_ADAPTIVE_INTEGRATION.md)