# 🛠️ Adaptive Theme Implementation Guide

**Datum:** 30.07.2025  
**Sprint:** 2 - Customer UI Integration  
**Geschätzte Zeit:** 2 Stunden  

---

## 📍 Navigation
**← Zurück:** [Fix Plan](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/ADAPTIVE_THEME_FIX_PLAN.md)  
**⬆️ Sprint 2:** [Overview](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/README.md)

---

## 📋 Implementierungs-Schritte

### Step 1: FieldWrapper Integration {#step1}

**Problem:** AdaptiveField zeigt keine Labels und UI-Elemente

#### 1.1 AdaptiveField anpassen
```tsx
// /frontend/src/features/customers/components/adaptive/AdaptiveField.tsx

// ENTFERNE Label-Rendering aus AdaptiveField
// AdaptiveField soll NUR das TextField rendern, OHNE Container

export const AdaptiveField: React.FC<AdaptiveFieldProps> = ({
  field,
  value,
  onChange,
  onBlur,
  error,
  disabled = false,
  readOnly = false,
}) => {
  const { theme } = useCustomerFieldTheme();
  const [dynamicWidth, setDynamicWidth] = useState<number>();
  const measureRef = useRef<HTMLSpanElement>(null);
  
  // Size mapping bleibt
  const sizeMap = { /* ... */ };
  const fieldSizeClass = `field-${sizeMap[field.sizeHint || 'medium'] || 'mittel'}`;
  
  // Width-Berechnung
  useEffect(() => {
    if (measureRef.current && theme.darstellung === 'anpassungsfähig') {
      const measuredWidth = measureRef.current.offsetWidth + 32; // +Padding
      const fieldSize = getFieldSize(field);
      const minWidth = parseInt(theme.feldgrößen[fieldSize].minBreite);
      const maxWidth = parseInt(theme.feldgrößen[fieldSize].maxBreite);
      
      const calculatedWidth = Math.min(Math.max(measuredWidth, minWidth), maxWidth);
      setDynamicWidth(calculatedWidth);
    }
  }, [value, field, theme]);
  
  return (
    <>
      {/* Measure Span */}
      <MeasureSpan ref={measureRef}>
        {value || field.placeholder || field.label}
      </MeasureSpan>
      
      {/* NUR TextField, OHNE eigenen Container */}
      <StyledTextField
        id={field.key}
        name={field.key}
        label={field.label} // MUI übernimmt Label
        value={value}
        onChange={(e) => onChange(e.target.value)}
        onBlur={onBlur}
        error={!!error}
        helperText={error}
        required={field.required}
        disabled={disabled}
        style={{ width: dynamicWidth ? `${dynamicWidth}px` : '100%' }}
        // Rest bleibt gleich
      />
    </>
  );
};
```

#### 1.2 DynamicFieldRenderer anpassen
```tsx
// /frontend/src/features/customers/components/fields/DynamicFieldRenderer.tsx

// In renderField Funktion:
if (useAdaptiveLayout && ['text', 'email', 'number'].includes(field.fieldType)) {
  return (
    <FieldWrapper field={field} error={error}>
      <AdaptiveField
        field={field}
        value={value}
        onChange={(newValue) => onChange(field.key, newValue)}
        onBlur={() => onBlur(field.key)}
        error={error}
        disabled={loading || field.disabled}
        readOnly={readOnly || field.readonly}
      />
    </FieldWrapper>
  );
}
```

---

### Step 2: Dynamisches Wachstum aktivieren {#step2}

**Problem:** Felder wachsen nicht mit Inhalt

#### 2.1 AdaptiveFieldCalculator nutzen
```tsx
// /frontend/src/features/customers/components/adaptive/AdaptiveField.tsx

import { getFieldSize } from '../../utils/fieldSizeCalculator';

// In useEffect für Width-Berechnung:
useEffect(() => {
  if (measureRef.current && theme.darstellung === 'anpassungsfähig') {
    const textWidth = measureRef.current.offsetWidth;
    const padding = 32; // TextField Padding
    const measuredWidth = textWidth + padding;
    
    // Nutze fieldSizeCalculator für Size-Kategorie
    const sizeInfo = getFieldSize(field);
    const sizeCategory = getSizeCategoryFromGrid(sizeInfo.md);
    
    const minWidth = parseInt(theme.feldgrößen[sizeCategory].minBreite);
    const maxWidth = parseInt(theme.feldgrößen[sizeCategory].maxBreite);
    
    const calculatedWidth = Math.min(Math.max(measuredWidth, minWidth), maxWidth);
    setDynamicWidth(calculatedWidth);
  }
}, [value, field, theme]);

// Helper-Funktion
function getSizeCategoryFromGrid(gridSize: number): string {
  if (gridSize <= 2) return 'kompakt';
  if (gridSize <= 3) return 'klein';
  if (gridSize <= 6) return 'mittel';
  if (gridSize <= 10) return 'groß';
  return 'voll';
}
```

---

### Step 3: Größen-System korrigieren {#step3}

**Problem:** Größen werden nicht korrekt angewendet

#### 3.1 Container-Größen setzen
```tsx
// /frontend/src/features/customers/components/fields/DynamicFieldRenderer.tsx

import { getFieldSize } from '../../utils/fieldSizeCalculator';

// Adaptive Layout Rendering
if (useAdaptiveLayout) {
  return (
    <AdaptiveFormContainer variant="flexbox">
      {visibleFields.map(field => {
        const sizeInfo = getFieldSize(field);
        const sizeCategory = getSizeCategoryFromGrid(sizeInfo.md || 6);
        const sizeClass = `field-${sizeMap[sizeCategory] || 'mittel'}`;
        
        return (
          <Box key={field.key} className={sizeClass}>
            <FieldWrapper field={field} error={errors[field.key]}>
              {renderField(field)}
            </FieldWrapper>
          </Box>
        );
      })}
    </AdaptiveFormContainer>
  );
}

// Helper für Size-Mapping
function getSizeCategoryFromGrid(gridSize: number): string {
  if (gridSize <= 2) return 'compact';
  if (gridSize <= 3) return 'small';
  if (gridSize <= 6) return 'medium';
  if (gridSize <= 10) return 'large';
  return 'full';
}
```

---

### Step 4: Info-Boxen wiederherstellen {#step4}

**Problem:** Filialunternehmen-Info fehlt

#### 4.1 CustomerDataStep Info-Box
```tsx
// /frontend/src/features/customers/components/steps/CustomerDataStep.tsx

// Nach dem Typography für die Beschreibung:
{customerData.chainCustomer === 'ja' && (
  <Alert severity="info" sx={{ mb: 3 }}>
    <AlertTitle>Filialunternehmen</AlertTitle>
    Sie haben angegeben, dass es sich um ein Filialunternehmen handelt. 
    Im nächsten Schritt können Sie die einzelnen Standorte erfassen.
  </Alert>
)}
```

---

## 🧪 Test-Checkliste

Nach jeder Änderung testen:

1. **Step 1 Test:**
   - [ ] Labels werden angezeigt
   - [ ] Pflichtfeld-Sternchen (*) sichtbar
   - [ ] Info-Icons bei Feldern mit helpText

2. **Step 2 Test:**
   - [ ] Felder wachsen beim Tippen
   - [ ] Mindestbreiten eingehalten (PLZ: 60px)
   - [ ] Maximalbreiten funktionieren

3. **Step 3 Test:**
   - [ ] Felder in Zeilen angeordnet
   - [ ] Automatischer Umbruch bei Fensterverkleinerung
   - [ ] Verschiedene Feldgrößen sichtbar

4. **Step 4 Test:**
   - [ ] Info-Box erscheint bei "Filialunternehmen: Ja"
   - [ ] Wizard-Schritte konsistent

---

## 📸 Erwartetes Ergebnis

```
[Kundennummer:120px] [Firmenname:400px]               
[Rechtsform:160px]   [Branche:160px]    [Filial:160px]
[PLZ:60px]          [Ort:340px]
[Straße:300px]      [Hausnummer:100px]
[E-Mail:400px]
[Telefon:200px]     [Ansprechpartner:200px]
```

Felder brechen intelligent um und wachsen mit Inhalt!