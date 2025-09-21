# 🎯 Plan: Drag & Drop Cursor-Offset Fix

**Datum:** 24.07.2025  
**Problem:** Karte verspringt beim Drag-Start um eine Kartenbreite nach rechts

## 📋 Lösungsplan (3 Schritte)

### Schritt 1: Quick-Fix für Box-Model Reset
**Ziel:** Box-Model während des Draggings zurücksetzen
**Warum:** @hello-pangea/dnd kann mit komplexen margin/padding-Werten Probleme haben

```tsx
style={{
  ...provided.draggableProps.style,
  ...(snapshot.isDragging && {
    margin: 0,
    boxSizing: 'border-box',
  }),
}}
```

### Schritt 2: Transform-Origin und Position Fix
**Ziel:** Sicherstellen dass keine CSS Transforms interferieren
**Warum:** Parent-Container Transforms können Offset-Probleme verursachen

- Explizit transform-origin setzen
- Position relative während Dragging vermeiden
- Wrapper-Div für bessere Isolation

### Schritt 3: Alternative - Custom Drag Preview
**Ziel:** Falls Schritte 1-2 nicht helfen
**Warum:** Vollständige Kontrolle über das Drag-Element

```tsx
const dragPreview = (provided, snapshot) => {
  if (snapshot.isDragging) {
    return <CustomDragPreview />;
  }
  return null;
};
```

## 🧪 Test-Kriterien
- [ ] Karte bleibt beim Drag unter dem Cursor
- [ ] Kein Versatz nach rechts beim Drag-Start
- [ ] Smooth Drag-Animation
- [ ] Funktioniert bei verschiedenen Zoom-Levels
- [ ] Keine visuellen Glitches

## ⏱️ Geschätzte Zeit
- Quick-Fix: 10 Minuten
- Vollständige Lösung: 30 Minuten