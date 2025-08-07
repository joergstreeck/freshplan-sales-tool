# 🎯 Dropdown Auto-Width Implementation Plan

**Status:** 🔄 In Planung  
**Erstellt:** 30.07.2025  
**Sprint:** 2 - Customer UI Integration

## 📋 Übersicht

Implementierung einer automatischen Breitenanpassung für Dropdown-Felder basierend auf der Textlänge der Optionen.

## 🎯 Ziel

- Dropdown-Felder passen sich automatisch an längste Option an
- Kein Text wird abgeschnitten
- Responsive Verhalten auf Mobile
- Saubere Theme-Integration

## 📚 Verwandte Dokumente

- [Developer Guide →](./DROPDOWN_AUTO_WIDTH_DEV_GUIDE.md) **NEU**
- [Implementation Guide →](./DROPDOWN_AUTO_WIDTH_IMPLEMENTATION.md)
- [Technical Details →](./DROPDOWN_AUTO_WIDTH_TECHNICAL.md)
- [Accessibility Guide →](./DROPDOWN_AUTO_WIDTH_ACCESSIBILITY.md) **NEU**
- [Testing Guide →](./DROPDOWN_AUTO_WIDTH_TESTING.md)
- [Code Snippets →](./DROPDOWN_AUTO_WIDTH_CODE_SNIPPETS.md)
- [← Sprint 2 README](./README.md)

## 🔍 Problem-Analyse

### Aktuelle Situation
- Dropdowns haben feste `maxWidth` durch CSS-Klassen
- `.field-mittel`: maxWidth 240px (zu schmal für lange Texte)
- Inline-Styles werden überschrieben

### Root Cause
- AdaptiveFormContainer nicht für Dropdown-Speziallogik ausgelegt
- Theme-Klassen zu restriktiv für dynamische Inhalte

## ✅ Lösungsansatz

1. **Neue CSS-Klasse** `.field-dropdown-auto`
2. **Dynamische Breitenberechnung** im SelectField
3. **Theme-Integration** im AdaptiveFormContainer
4. **Responsive Handling** für Mobile

## 📊 Erfolgs-Kriterien

- [ ] Alle Dropdown-Texte vollständig lesbar
- [ ] Keine horizontalen Scrollbars
- [ ] Mobile: 100% Breite
- [ ] Desktop: Dynamisch nach Inhalt
- [ ] Performance: < 10ms Berechnung

---

**Nächster Schritt:** [Implementation Guide →](./DROPDOWN_AUTO_WIDTH_IMPLEMENTATION.md)