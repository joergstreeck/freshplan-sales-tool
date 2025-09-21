# 🧪 Dropdown Auto-Width Testing Guide

**Status:** 🔍 Test-Anleitung  
**Teil von:** Dropdown Auto-Width Feature  
**Sprint:** 2

## 📚 Navigation

- [← Technical Details](./DROPDOWN_AUTO_WIDTH_TECHNICAL.md)
- [← Implementation Guide](./DROPDOWN_AUTO_WIDTH_IMPLEMENTATION.md)
- [← Plan Overview](./DROPDOWN_AUTO_WIDTH_PLAN.md)

## ✅ Test-Szenarien

### 1️⃣ Manuelle Tests

#### Desktop Tests
- [ ] **Geschäftsmodell**: "Privatwirtschaftlich" → ~250px
- [ ] **Vertragsende**: "Unbefristet / Kein Vertrag" → ~300px
- [ ] **Wechselbereitschaft**: "Mittel - offen für Alternativen" → ~340px
- [ ] **Entscheidungszeitpunkt**: Alle Optionen lesbar

#### Mobile Tests (< 768px)
- [ ] Alle Dropdowns 100% Breite
- [ ] Kein horizontales Scrolling
- [ ] Text in Dropdown lesbar

#### Edge Cases
- [ ] Dropdown ohne Optionen → 200px
- [ ] Sehr langer Placeholder → Breite angepasst
- [ ] Disabled Dropdown → Breite bleibt

### 2️⃣ Browser-Konsole Debug

```javascript
// In Browser-Konsole ausführen:
// Prüfe berechnete Breiten
document.querySelectorAll('.field-dropdown-auto').forEach(el => {
  console.log(el.querySelector('label')?.textContent, el.offsetWidth);
});
```

### 3️⃣ Unit Tests

```typescript
// SelectField.test.tsx
describe('Dropdown width calculation', () => {
  it('calculates width based on longest option', () => {
    const options = [
      { value: 'short', label: 'Kurz' },
      { value: 'long', label: 'Sehr langer Text der die Breite bestimmt' }
    ];
    // Test Implementierung
  });
});
```

## 🔍 Visuelle Prüfung

### Vorher (Problem)
- Dropdowns alle gleich breit (~240px)
- Text abgeschnitten mit "..."
- Nicht lesbar ohne aufklappen

### Nachher (Gelöst)
- Jedes Dropdown individuell breit
- Vollständiger Text sichtbar
- Responsive auf Mobile

## 📋 Checkliste für Review

- [ ] Keine anderen Feldtypen betroffen
- [ ] Theme bleibt konsistent
- [ ] Performance < 10ms
- [ ] Keine Console Errors
- [ ] Accessibility erhalten

## 🐛 Bekannte Probleme & Lösungen

| Problem | Lösung |
|---------|--------|
| Dropdown zu breit | Max-Width Cap bei 500px |
| Mobile overflow | Media Query für 100% |
| Flackern bei Update | CSS Transition smoothing |

---

**Fertig?** [← Zurück zum Sprint 2 README](./README.md)