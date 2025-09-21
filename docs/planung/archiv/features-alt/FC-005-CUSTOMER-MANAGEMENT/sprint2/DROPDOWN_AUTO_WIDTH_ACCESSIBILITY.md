# ♿ Dropdown Auto-Width Accessibility Guide

**Status:** 🔍 Barrierefreiheit-Leitfaden  
**Teil von:** Dropdown Auto-Width Feature  
**Sprint:** 2

## 📚 Navigation

- [← Developer Guide](./DROPDOWN_AUTO_WIDTH_DEV_GUIDE.md)
- [← Plan Overview](./DROPDOWN_AUTO_WIDTH_PLAN.md)
- [Testing Guide →](./DROPDOWN_AUTO_WIDTH_TESTING.md)

## 🎯 Accessibility-Prinzipien

### 1. Label-Zuordnung bleibt erhalten

**WICHTIG:** Die dynamische Breite darf NIEMALS die Label-Field-Verbindung zerstören!

```tsx
// ✅ RICHTIG: ID-Verbindung bleibt
<FormControl>
  <InputLabel htmlFor={field.key}>{field.label}</InputLabel>
  <Select
    id={field.key}
    labelId={`${field.key}-label`}
    aria-describedby={`${field.key}-helper`}
  >
</FormControl>
```

### 2. Fokus-Sichtbarkeit

```css
.field-dropdown-auto:focus-within {
  outline: 2px solid var(--primary-color);
  outline-offset: 2px;
}
```

### 3. Screenreader-Ankündigungen

```tsx
// Bei Breiten-Änderung KEINE Ankündigung!
// Nur Inhalt ist relevant, nicht Layout
<Select
  aria-label={field.label}
  aria-required={field.required}
  aria-invalid={!!error}
  aria-describedby={helperTextId}
>
```

## 🔍 Testing mit Screenreader

### NVDA/JAWS Test-Schritte

1. **Tab zu Dropdown** → Label wird vorgelesen
2. **Space/Enter** → Optionen werden vorgelesen
3. **Pfeil runter** → Jede Option wird vorgelesen
4. **Enter** → Auswahl wird bestätigt

### Erwartetes Verhalten

- Breiten-Änderung wird NICHT angekündigt
- Label-Verbindung bleibt immer erhalten
- Helper-Text wird nach Label vorgelesen
- Error-State wird korrekt kommuniziert

## ⚠️ Häufige Accessibility-Fallen

### ❌ FALSCH: Label verliert Verbindung
```tsx
// Dynamisches Rendern kann IDs brechen!
{showLabel && <label htmlFor={dynamicId}>}
```

### ❌ FALSCH: Fokus geht verloren
```tsx
// Bei Breiten-Update darf Fokus nicht springen!
onWidthChange={() => element.blur()} // NIEMALS!
```

### ✅ RICHTIG: Stabile IDs und Fokus
```tsx
const stableId = useId(); // React 18+
// oder
const stableId = `dropdown-${field.key}`;

<Select id={stableId} autoFocus={false}>
```

## 🎨 Kontrast und Lesbarkeit

### Minimum-Kontrast-Verhältnisse

- **Normal Text:** 4.5:1 (WCAG AA)
- **Large Text:** 3:1 (WCAG AA)
- **Fokus-Indikator:** 3:1 gegen Hintergrund

### CSS für bessere Lesbarkeit

```css
.field-dropdown-auto {
  /* Genug Padding für Touch-Targets */
  min-height: 44px; /* iOS Standard */
  
  /* Klare Trennung */
  border: 1px solid rgba(0, 0, 0, 0.23);
}

/* High Contrast Mode Support */
@media (prefers-contrast: high) {
  .field-dropdown-auto {
    border-width: 2px;
  }
}
```

## 📱 Mobile Accessibility

### Touch-Target-Größe
- Minimum: 44x44px (iOS)
- Empfohlen: 48x48px (Material Design)

### Zoom-Verhalten
```css
/* Erlaube Zoom auf Mobile */
.field-dropdown-auto {
  font-size: 16px; /* Verhindert Auto-Zoom iOS */
}
```

## ✅ Accessibility-Checkliste

- [ ] Labels mit `htmlFor` korrekt verbunden
- [ ] ARIA-Attribute vollständig (label, required, invalid, describedby)
- [ ] Fokus-Reihenfolge logisch (tabindex vermeiden)
- [ ] Kontrast-Verhältnisse eingehalten
- [ ] Touch-Targets groß genug
- [ ] Keyboard-Navigation funktioniert
- [ ] Screenreader-Test erfolgreich
- [ ] Keine Layout-Shifts beim Fokussieren

## 🔧 Debugging-Tools

```javascript
// Browser-Konsole: Accessibility-Tree prüfen
document.querySelectorAll('.field-dropdown-auto').forEach(el => {
  console.log({
    element: el,
    label: el.getAttribute('aria-label'),
    labelledBy: el.getAttribute('aria-labelledby'),
    describedBy: el.getAttribute('aria-describedby'),
    required: el.getAttribute('aria-required'),
    invalid: el.getAttribute('aria-invalid')
  });
});
```

---

**Fertig?** [→ Zurück zum Testing Guide](./DROPDOWN_AUTO_WIDTH_TESTING.md)