# 🎨 Wizard mit Adaptive Theme Integration

**Sprint:** 2  
**Status:** 🔧 Implementierungs-Guide  
**Kern-Feature:** Flexible, intelligente Feld-Anordnung  

---

## 📍 Navigation
**← Wizard V2:** [Wizard Struktur V2](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/WIZARD_STRUCTURE_V2.md)  
**→ Theme System:** [Field Theme Implementation](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/prototypes/FIELD_THEME_IMPLEMENTATION.md)  
**↑ Sprint 2:** [Sprint 2 Overview](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/README.md)  

---

## 🎯 Kern-Prinzip: Adaptive Theme bleibt!

Die neue 3-Schritt-Struktur nutzt **weiterhin** unser intelligentes Theme-System:
- ✅ Dynamische Feldbreiten
- ✅ Intelligenter Zeilenumbruch
- ✅ Responsive bei verschiedenen Kartengrößen
- ✅ Automatische Größenanpassung basierend auf Inhalt

---

## 🖼️ Visuelle Darstellung

### Desktop (1200px+)
```
Step 1: Basis & Filialstruktur
┌─────────────────────────────────────────────────────────┐
│ [Kundennummer:klein] [Firmenname:groß................] │
│ [Rechtsform:klein] [Branche:klein] [Filial?:klein]     │
│ [Straße:groß....................] [Nr:kompakt]         │
│ [PLZ:kompakt] [Ort:groß........................]      │
│                                                         │
│ === Bei Filialunternehmen ===                          │
│ [Deutschland:klein] [Österreich:klein] [Schweiz:klein]  │
│ [Rest-EU:klein] [Gesamt:klein] [Expansion?:mittel]     │
└─────────────────────────────────────────────────────────┘
```

### Tablet (768px)
```
Step 1: Basis & Filialstruktur
┌───────────────────────────────┐
│ [Kundennummer:klein]          │
│ [Firmenname:voll............] │
│ [Rechtsform:mittel] [Branche] │
│ [Filialunternehmen?:mittel]   │
│ [Straße:groß........] [Nr]    │
│ [PLZ] [Ort:groß............]  │
└───────────────────────────────┘
```

### Mobile (< 768px)
```
Step 1: Basis
┌─────────────────┐
│ [Kundennr:voll] │
│ [Firmenname]    │
│ [Rechtsform]    │
│ [Branche]       │
│ [Filial?]       │
│ [Straße]        │
│ [Nr]            │
│ [PLZ]           │
│ [Ort]           │
└─────────────────┘
```

---

## 💻 Code-Integration

### Step Component mit Theme
```tsx
import { CustomerFieldThemeProvider } from '../theme/CustomerFieldThemeProvider';
import { AdaptiveField } from '../components/adaptive/AdaptiveField';
import { DynamicFieldRenderer } from '../components/fields/DynamicFieldRenderer';

export const Step1BasisFilialstruktur: React.FC = () => {
  return (
    <CustomerFieldThemeProvider mode="anpassungsfähig">
      <Box sx={{ 
        display: 'flex', 
        flexWrap: 'wrap', 
        gap: 2,
        '& > *': {
          flexShrink: 0,
          flexGrow: 0
        }
      }}>
        {/* Felder werden automatisch angeordnet */}
        {step1Fields.map(field => (
          <DynamicFieldRenderer
            key={field.key}
            field={field}
            value={formData[field.key]}
            onChange={handleChange}
            useAdaptiveLayout={true}
          />
        ))}
      </Box>
    </CustomerFieldThemeProvider>
  );
};
```

### Field Size Hints im Catalog
```json
{
  "customerNumber": {
    "key": "customerNumber",
    "label": "Kundennummer",
    "sizeHint": "klein",  // 100-120px
    // ...
  },
  "companyName": {
    "key": "companyName",
    "label": "Firmenname",
    "sizeHint": "groß",   // 220-280px, wächst mit Inhalt
    // ...
  },
  "postalCode": {
    "key": "postalCode",
    "label": "PLZ",
    "sizeHint": "kompakt", // 60-80px
    // ...
  }
}
```

---

## 🎯 Adaptive Verhalten pro Step

### Step 1: Basis & Filialstruktur
- **Zeile 1:** Kundennummer + Firmenname (füllt Rest)
- **Zeile 2:** Rechtsform + Branche + Filial (gleiche Größe)
- **Zeile 3:** Adresse intelligent (Straße groß, Nr klein)
- **Conditional:** Filialstruktur-Felder nur wenn nötig

### Step 2: Angebot & Pain Points
- **Services:** Checkboxen in Spalten (2-3 je nach Breite)
- **Pain Points:** Responsive Grid
- **Potenzial:** Prominente Live-Anzeige

### Step 3: Ansprechpartner
- **Name:** Anrede(klein) + Titel(kompakt) + Vor/Nachname(mittel)
- **Kontakt:** E-Mail(groß), Telefonnummern(mittel)
- **Präferenzen:** Intelligent gruppiert

---

## 📱 Responsive Breakpoints

```typescript
const useResponsiveTheme = () => {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('sm'));
  const isTablet = useMediaQuery(theme.breakpoints.down('md'));
  
  // Mobile: Alle Felder 100%
  if (isMobile) {
    return { mode: 'kompakt', forceFullWidth: true };
  }
  
  // Tablet: Reduzierte Flexibilität
  if (isTablet) {
    return { mode: 'anpassungsfähig', maxFieldsPerRow: 2 };
  }
  
  // Desktop: Volle Flexibilität
  return { mode: 'anpassungsfähig' };
};
```

---

## ✅ Vorteile der Theme-Integration

1. **Konsistente UX** über alle Steps
2. **Automatische Anpassung** an Viewport
3. **Intelligente Gruppierung** verwandter Felder
4. **Keine manuellen Breakpoints** nötig
5. **Zukunftssicher** für neue Felder

---

## 🚀 Implementation Checklist

- [ ] Theme Provider um alle Steps wrappen
- [ ] sizeHint für alle neuen Felder definieren
- [ ] Conditional Rendering mit Theme kompatibel
- [ ] Mobile-First Testing
- [ ] Performance bei vielen Feldern prüfen

---

**→ Weiter:** [Step 1 Implementation](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/wizard/STEP1_BASIS_FILIALSTRUKTUR.md)