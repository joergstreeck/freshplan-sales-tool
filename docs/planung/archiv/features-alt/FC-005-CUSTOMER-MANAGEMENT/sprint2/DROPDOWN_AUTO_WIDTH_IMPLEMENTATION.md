# 🛠️ Dropdown Auto-Width Implementation Guide

**Status:** 📝 Implementierungsanleitung  
**Teil von:** Dropdown Auto-Width Feature  
**Sprint:** 2

## 📚 Navigation

- [← Zurück zum Plan](./DROPDOWN_AUTO_WIDTH_PLAN.md)
- [Technical Details →](./DROPDOWN_AUTO_WIDTH_TECHNICAL.md)
- [Testing Guide →](./DROPDOWN_AUTO_WIDTH_TESTING.md)

## 📋 Implementierungsschritte

### 1️⃣ AdaptiveFormContainer erweitern

**Datei:** `frontend/src/features/customers/components/adaptive/AdaptiveFormContainer.tsx`

```typescript
// Neue CSS-Klasse hinzufügen
'& .field-dropdown-auto': {
  flex: '0 1 auto',
  minWidth: '200px',
  maxWidth: 'none', // Keine Begrenzung!
  width: 'auto',
  transition: 'width 0.2s ease-in-out'
}
```

### 2️⃣ DynamicFieldRenderer anpassen

**Datei:** `frontend/src/features/customers/components/fields/DynamicFieldRenderer.tsx`

```typescript
// Spezielle Klasse für Dropdowns zuweisen
const getFieldClass = (field: FieldDefinition) => {
  if (field.fieldType === 'select' || field.fieldType === 'dropdown') {
    return 'field-dropdown-auto';
  }
  // Standard-Logik für andere Felder
  const sizeInfo = getFieldSize(field);
  const sizeCategory = getSizeCategoryFromGrid(sizeInfo.md || 6);
  return `field-${sizeMap[sizeCategory] || 'mittel'}`;
};
```

### 3️⃣ SelectField Breitenberechnung

**Datei:** `frontend/src/features/customers/components/fields/fieldTypes/SelectField.tsx`

```typescript
// Dynamische Breite berechnen
const calculateDropdownWidth = useMemo(() => {
  if (!field.options || field.options.length === 0) return 200;
  
  const longestLabel = Math.max(
    ...field.options.map(opt => opt.label.length),
    field.placeholder?.length || 0,
    15 // Minimum
  );
  
  // 8px pro Zeichen + 80px für Icon/Padding
  const width = longestLabel * 8 + 80;
  
  // Min 200px, Max 500px
  return Math.min(Math.max(width, 200), 500);
}, [field.options, field.placeholder]);
```

### 4️⃣ CSS-Variable setzen

```typescript
return (
  <FormControl
    sx={{
      '--dropdown-width': `${calculateDropdownWidth}px`,
      width: 'var(--dropdown-width)',
      [theme.breakpoints.down('sm')]: {
        width: '100%'
      }
    }}
  >
```

## ⚠️ Wichtige Hinweise

1. **Andere Feldtypen nicht beeinflussen!**
2. **Mobile First:** Bei xs immer 100%
3. **Performance:** useMemo für Berechnungen
4. **Fallback:** Mindestens 200px Breite

---

## 📋 Ready-to-Copy Code

Für kopierfertigen Code siehe: [Code Snippets →](./DROPDOWN_AUTO_WIDTH_CODE_SNIPPETS.md)

---

**Nächster Schritt:** [Technical Details →](./DROPDOWN_AUTO_WIDTH_TECHNICAL.md)