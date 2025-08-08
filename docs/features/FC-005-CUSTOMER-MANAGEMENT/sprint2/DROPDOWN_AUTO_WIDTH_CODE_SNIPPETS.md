# 📝 Dropdown Auto-Width Code Snippets

**Status:** 🔨 Ready-to-Copy Code  
**Teil von:** Dropdown Auto-Width Feature  
**Sprint:** 2

## 📚 Navigation

- [← Implementation Guide](./DROPDOWN_AUTO_WIDTH_IMPLEMENTATION.md)
- [← Plan Overview](./DROPDOWN_AUTO_WIDTH_PLAN.md)

## 1️⃣ AdaptiveFormContainer.tsx

```typescript
// In FlexContainer styled component, nach '& .field-voll' hinzufügen:

// Spezielle Behandlung für Dropdown-Felder - Text muss immer lesbar sein
'& .field-dropdown-auto': {
  flex: '0 1 auto',
  minWidth: '200px',
  maxWidth: 'none', // Keine Begrenzung - wächst mit Inhalt
  width: 'var(--dropdown-width, auto)',
  transition: 'width 0.2s ease-in-out',
},
```

## 2️⃣ DynamicFieldRenderer.tsx

```typescript
// Ersetze die bestehende Zeile 180:
// const sizeClass = `field-${sizeMap[sizeCategory] || 'mittel'}`;

// Durch:
const sizeClass = (field.fieldType === 'select' || field.fieldType === 'dropdown')
  ? 'field-dropdown-auto'
  : `field-${sizeMap[sizeCategory] || 'mittel'}`;

// Und ersetze Zeilen 182-191 (style Berechnung) durch:
const style: React.CSSProperties = {};
```

## 3️⃣ SelectField.tsx - Mit Hook

```typescript
// Nach den imports hinzufügen:
import { useDropdownWidth } from '../../hooks/useDropdownWidth';

// Nach Zeile 56 (const displayValue = value || '';) hinzufügen:

// Nutze den neuen Hook für Breiten-Berechnung
const dropdownWidth = useDropdownWidth({
  options: field.options,
  placeholder: field.placeholder
});

// Und ändere das FormControl (ersetze komplett ab Zeile 58):
return (
  <FormControl 
    fullWidth 
    size="small" 
    error={error}
    required={required}
    disabled={disabled || readOnly}
    className="field-dropdown-auto"
    sx={{
      ...dropdownWidth.style,
      [theme.breakpoints?.down('sm')]: {
        width: '100%'
      }
    }}
  >
```

## 4️⃣ Theme import in SelectField.tsx

```typescript
// Nach den MUI imports:
import { useTheme } from '@mui/material/styles';

// Im Component:
const theme = useTheme();
```

## 5️⃣ Accessibility sicherstellen

```typescript
// Im SelectField, stelle sicher dass IDs stabil bleiben:
<MuiSelect
  id={field.key}
  name={field.key}
  labelId={`${field.key}-label`}
  aria-label={field.label}
  aria-required={required}
  aria-invalid={error}
  aria-describedby={helperText ? `${field.key}-helper` : undefined}
  // ... rest
>
```

## ⚠️ Wichtige Hinweise

1. **Reihenfolge beachten!** CSS-Klasse muss nach anderen Größen definiert werden
2. **useMemo import** nicht vergessen in SelectField
3. **Theme import** für responsive Breakpoints
4. **Keine anderen Änderungen** in den Dateien vornehmen

---

**Fertig zum Kopieren!** Diese Snippets können direkt eingefügt werden.