# 🔍 Debug-Analyse: Drag & Drop Cursor-Offset

## Implementierte Fixes

### 1. Box-Model Reset
- Wrapper-div für Drag-Element hinzugefügt
- Margin während Dragging auf 0 gesetzt
- Transitions während Dragging deaktiviert

### 2. Style-Isolation
- Card-Styles vom Draggable getrennt
- Transform-origin explizit gesetzt
- Hover-Effekte optimiert

## 🧪 Test-Anleitung für Jörg

Bitte teste folgende Szenarien:

1. **Browser-Zoom:**
   - Setze Zoom auf 100% und teste
   - Dann auf 125% und 150%
   - Notiere ob Offset sich ändert

2. **Drag-Start Position:**
   - Ziehe von der Mitte der Karte
   - Ziehe vom oberen Rand
   - Ziehe vom unteren Rand
   - Wo genau springt die Karte?

3. **DevTools Test:**
   - Öffne Chrome DevTools
   - Inspiziere eine Karte
   - Schaue nach computed styles mit "transform"
   - Gibt es Parent-Elemente mit transforms?

## 🔧 Weitere Debug-Optionen

Falls Problem weiterhin besteht:

### Option A: Expliziter Offset-Fix
```tsx
style={{
  ...provided.draggableProps.style,
  left: provided.draggableProps.style.left - 280, // Kartenbreite
}}
```

### Option B: Custom Drag Layer
Komplett eigene Drag-Implementierung ohne Library-Magic

### Option C: Migration zu @dnd-kit
Modernere Library mit besserer Cursor-Kontrolle

## 📊 Vermutung

Das Problem könnte von einem der folgenden Faktoren kommen:
1. Global CSS transforms auf Parent-Elementen
2. Browser-spezifisches Rendering (Chrome vs Firefox)
3. Konflikt mit MUI's internen Styles
4. @hello-pangea/dnd Bug bei bestimmten DOM-Strukturen