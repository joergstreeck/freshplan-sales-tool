# 🎯 Alternative Lösungen für Drag & Drop Problem

## Option 1: @dnd-kit (Empfohlen)
**Vorteile:**
- Moderne Library mit besserer Touch-Support
- Explizite Cursor-Offset Kontrolle
- Bereits installiert laut package.json
- Bessere Performance

```tsx
import { DndContext, closestCenter, DragOverlay } from '@dnd-kit/core';
import { SortableContext, verticalListSortingStrategy } from '@dnd-kit/sortable';
```

## Option 2: Native HTML5 Drag & Drop
**Vorteile:**
- Keine externe Library
- Volle Kontrolle über Offset
- Beste Performance

```tsx
const handleDragStart = (e: DragEvent, opportunity: Opportunity) => {
  const rect = (e.target as HTMLElement).getBoundingClientRect();
  const offsetX = e.clientX - rect.left;
  const offsetY = e.clientY - rect.top;
  e.dataTransfer?.setData('offsetX', offsetX.toString());
  e.dataTransfer?.setData('offsetY', offsetY.toString());
};
```

## Option 3: react-sortable-hoc
**Vorteile:**
- Battle-tested
- Einfache API
- Gute Dokumentation

```bash
npm install react-sortable-hoc
```

## Option 4: Cursor-Offset Fix für @hello-pangea/dnd
Falls wir bei der aktuellen Library bleiben wollen:

```tsx
// Dynamische Offset-Berechnung basierend auf Klick-Position
const calculateOffset = (e: MouseEvent) => {
  const rect = e.currentTarget.getBoundingClientRect();
  const offsetX = e.clientX - rect.left - (rect.width / 2);
  return offsetX;
};
```

## Empfehlung
Ich empfehle **@dnd-kit** da es:
1. Bereits installiert ist
2. Moderne API mit besserer Kontrolle
3. Aktiv maintained wird
4. Das Offset-Problem explizit löst

Soll ich die Migration zu @dnd-kit durchführen?