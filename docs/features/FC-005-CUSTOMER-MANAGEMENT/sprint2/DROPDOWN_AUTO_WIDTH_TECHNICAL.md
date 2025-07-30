# 🔧 Dropdown Auto-Width Technical Details

**Status:** 📐 Technische Spezifikation  
**Teil von:** Dropdown Auto-Width Feature  
**Sprint:** 2

## 📚 Navigation

- [← Implementation Guide](./DROPDOWN_AUTO_WIDTH_IMPLEMENTATION.md)
- [← Plan Overview](./DROPDOWN_AUTO_WIDTH_PLAN.md)
- [Testing Guide →](./DROPDOWN_AUTO_WIDTH_TESTING.md)

## 🏗️ Architektur-Entscheidungen

### Warum CSS-Klassen statt Inline-Styles?

1. **Theme-Konsistenz**: Integration ins bestehende System
2. **Performance**: CSS-Klassen sind performanter
3. **Wartbarkeit**: Zentrale Stelle für Anpassungen
4. **Überschreibbarkeit**: Spezifische Overrides möglich

### Breiten-Berechnung

```typescript
// Formel: (Zeichen * 8px) + Padding
// Beispiele:
// "Kurz" (4 Zeichen): 4 * 8 + 80 = 112px → 200px (min)
// "Mittel - offen für Alternativen" (32): 32 * 8 + 80 = 336px
// "Sehr langer Text..." (50+): 50 * 8 + 80 = 480px → 500px (max)
```

## 📊 Betroffene Felder

### Sprint 2 Dropdowns
| Feld | Längste Option | Zeichen | Berechnete Breite |
|------|----------------|---------|-------------------|
| financingType | "Öffentlich finanziert" | 21 | 248px |
| contractEndDate | "Unbefristet / Kein Vertrag" | 27 | 296px |
| switchWillingness | "Mittel - offen für Alternativen" | 32 | 336px |
| decisionTimeline | "Sofortige Entscheidung möglich" | 31 | 328px |

## 🔄 CSS-Klassen-Hierarchie

```css
.adaptive-form-container
  └── .field-dropdown-auto (NEU)
       ├── minWidth: 200px
       ├── maxWidth: none (überschreibt Theme!)
       └── width: var(--dropdown-width, auto)
```

## ⚡ Performance-Überlegungen

1. **useMemo**: Berechnung nur bei Options-Änderung
2. **CSS-Variablen**: Keine Re-Renders bei Breiten-Updates
3. **Transition**: Sanfte Animation bei Änderungen

## 🐛 Edge Cases

1. **Keine Options**: Default 200px
2. **Sehr lange Texte**: Max 500px Cap
3. **Mobile**: Immer 100% unabhängig von Berechnung
4. **Disabled State**: Breite bleibt erhalten

---

**Nächster Schritt:** [Testing Guide →](./DROPDOWN_AUTO_WIDTH_TESTING.md)