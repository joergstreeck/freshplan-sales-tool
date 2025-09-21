# 👩‍💻 Dropdown Auto-Width Developer Guide

**Status:** 📖 Entwickler-Leitfaden  
**Teil von:** Dropdown Auto-Width Feature  
**Sprint:** 2

## 📚 Navigation

- [← Plan Overview](./DROPDOWN_AUTO_WIDTH_PLAN.md)
- [Implementation →](./DROPDOWN_AUTO_WIDTH_IMPLEMENTATION.md)
- [Accessibility Guide →](./DROPDOWN_AUTO_WIDTH_ACCESSIBILITY.md)

## 🎯 Goldene Regel

> **JEDES Dropdown im System MUSS den `useDropdownWidth` Hook verwenden!**

## 📋 Quick Start

```typescript
import { useDropdownWidth } from '@/features/customers/hooks/useDropdownWidth';

const MyDropdown = ({ field }) => {
  const { style } = useDropdownWidth({
    options: field.options,
    placeholder: field.placeholder
  });
  
  return (
    <FormControl 
      className="field-dropdown-auto"
      sx={style}
    >
      <Select>...</Select>
    </FormControl>
  );
};
```

## 🔧 Hook API

### useDropdownWidth(props)

| Parameter | Type | Default | Beschreibung |
|-----------|------|---------|--------------|
| options | FieldOption[] | [] | Dropdown-Optionen |
| placeholder | string | '' | Placeholder-Text |
| minWidth | number | 200 | Minimum-Breite in px |
| maxWidth | number | 500 | Maximum-Breite in px |
| charWidth | number | 8 | Pixel pro Zeichen |
| padding | number | 80 | Extra für Icon/Arrow |

### Returns

```typescript
{
  width: number;        // Berechnete Breite
  cssVar: string;       // CSS-Variable-Wert
  style: {              // Fertiges Style-Objekt
    '--dropdown-width': string;
    minWidth: string;
    maxWidth: string;
  }
}
```

## 🎨 CSS-Klasse

**WICHTIG:** Immer `field-dropdown-auto` verwenden!

```tsx
<FormControl className="field-dropdown-auto">
```

Diese Klasse:
- Entfernt maxWidth-Beschränkungen
- Aktiviert dynamisches Wachstum
- Sorgt für korrekte Mobile-Darstellung

## 📱 Responsive Verhalten

```typescript
const MyDropdown = () => {
  const theme = useTheme();
  const { style } = useDropdownWidth({...});
  
  return (
    <FormControl
      sx={{
        ...style,
        [theme.breakpoints.down('sm')]: {
          width: '100%' // Mobile immer 100%
        }
      }}
    >
  );
};
```

## ⚠️ Häufige Fehler

### ❌ FALSCH: Ohne Hook
```tsx
// NIEMALS so!
<Select style={{ width: '300px' }}>
```

### ❌ FALSCH: Falsche CSS-Klasse
```tsx
// NICHT field-mittel verwenden!
<FormControl className="field-mittel">
```

### ✅ RICHTIG: Mit Hook und Klasse
```tsx
const { style } = useDropdownWidth({...});
<FormControl className="field-dropdown-auto" sx={style}>
```

## 🧪 Testing

```typescript
// Unit Test Beispiel
import { calculateDropdownWidth } from '../hooks/useDropdownWidth';

test('calculates correct width', () => {
  const options = [
    { value: '1', label: 'Kurz' },
    { value: '2', label: 'Sehr langer Text hier' }
  ];
  
  const width = calculateDropdownWidth(options);
  expect(width).toBeGreaterThan(200);
  expect(width).toBeLessThan(500);
});
```

## 🔄 Migration bestehender Dropdowns

1. Import hinzufügen: `useDropdownWidth`
2. Hook verwenden für Style
3. CSS-Klasse ändern zu `field-dropdown-auto`
4. Testen auf allen Breakpoints

---

**Accessibility?** [→ Siehe Accessibility Guide](./DROPDOWN_AUTO_WIDTH_ACCESSIBILITY.md)