# Analyse: Drag & Drop Cursor-Offset Problem im Kanban Board

**Datum:** 2025-07-23  
**Komponente:** KanbanBoard.tsx  
**Library:** @hello-pangea/dnd v18.0.1  
**Problem:** Beim Draggen einer Karte springt diese vom Cursor weg

## 🔍 Analyse-Ergebnis

### 1. Identifizierte Problembereiche

Nach gründlicher Analyse der KanbanBoard.tsx und verwandter Dateien wurden folgende potenzielle Ursachen identifiziert:

#### A) CSS Transform-Konflikte
- **Gefunden:** Keine direkten Transform-Styles auf Container-Elementen in KanbanBoard.tsx
- **Aber:** Mehrere globale CSS-Dateien mit transform-Definitionen:
  - `freshplan-design-system.css`: Transform für Buttons
  - `legacy-to-remove/`: Viele transform-Definitionen
  - Theme: `transform: 'translateY(-1px)'` für Hover-States

#### B) Style-Anwendung während des Draggings
```tsx
// Zeile 175-177 in KanbanBoard.tsx
style={{
  ...provided.draggableProps.style,
}}
```
Die Style-Anwendung ist korrekt, aber es gibt potenzielle Konflikte mit:
- `opacity: snapshot.isDragging ? 0.9 : 1` (Zeile 180)
- `boxShadow` Änderungen während des Draggings
- `cursor` Änderungen

#### C) Box Model und Padding
- Card hat `CardContent` mit `sx={{ pb: '16px !important' }}` (Zeile 194)
- Möglicherweise wird das Padding nicht korrekt in die Drag-Position einberechnet

### 2. Bekannte Issues mit @hello-pangea/dnd

Basierend auf der Recherche sind folgende bekannte Probleme relevant:

1. **CSS Transform auf Parent-Containern**: 
   - Container mit `transform: translate(-50%, -50%)` verursachen Offset-Probleme
   - Die Library berechnet Positionen basierend auf dem normalen Document Flow

2. **Position Fixed Konflikte**:
   - Während des Draggings wird `position: fixed` angewendet
   - Dies kann mit anderen fixed/absolute Elementen kollidieren

3. **Drag-Initiierung Position**:
   - Offset-Probleme treten oft auf, wenn vom unteren Teil des Elements gedraggt wird

### 3. Konkrete Lösungsvorschläge

#### Lösung 1: Clone-Element für Dragging verwenden
```tsx
<Draggable draggableId={opportunity.id} index={index}>
  {(provided, snapshot) => (
    <>
      <Card
        ref={provided.innerRef}
        {...provided.draggableProps}
        {...provided.dragHandleProps}
        style={{
          ...provided.draggableProps.style,
          // Entferne alle transform-basierten Hover-Effekte
          transition: snapshot.isDragging ? 'none' : 'box-shadow 0.2s ease',
        }}
        sx={{
          mb: 1.5,
          // Vereinfache Styles während des Draggings
          opacity: snapshot.isDragging ? 0.5 : 1,
          boxShadow: snapshot.isDragging ? 'none' : theme.shadows[2],
          border: '1px solid rgba(148, 196, 86, 0.2)',
          cursor: 'grab',
          // Entferne Hover-Transform
          '&:hover': {
            boxShadow: theme.shadows[4],
            // transform: 'none', // Kein translateY
          },
        }}
      >
        {/* Content */}
      </Card>
    </>
  )}
</Draggable>
```

#### Lösung 2: Custom Drag Layer implementieren
```tsx
import { DragOverlay } from '@hello-pangea/dnd';

// In KanbanBoard component:
const [draggedItem, setDraggedItem] = useState<Opportunity | null>(null);

const handleDragStart = (result: DragStart) => {
  const dragged = opportunities.find(opp => opp.id === result.draggableId);
  setDraggedItem(dragged || null);
};

// In DragDropContext:
<DragDropContext onDragEnd={handleDragEnd} onDragStart={handleDragStart}>
  {/* Columns */}
  
  {/* Custom Drag Overlay */}
  <DragOverlay>
    {draggedItem && (
      <Card sx={{ /* simplified styles */ }}>
        {/* Render dragged item */}
      </Card>
    )}
  </DragOverlay>
</DragDropContext>
```

#### Lösung 3: Box-Model Reset während Dragging
```tsx
style={{
  ...provided.draggableProps.style,
  // Reset box model während dragging
  ...(snapshot.isDragging && {
    margin: 0,
    padding: 0,
    border: 'none',
    boxSizing: 'border-box',
  }),
}}
```

#### Lösung 4: Drag Handle mit fixer Position
```tsx
// Separater Drag Handle statt ganzer Card
<Card>
  <Box
    {...provided.dragHandleProps}
    sx={{
      position: 'absolute',
      top: 0,
      left: 0,
      right: 0,
      height: 40,
      cursor: 'grab',
      // Visueller Indikator
      '&:hover': {
        bgcolor: 'rgba(0, 0, 0, 0.04)',
      }
    }}
  />
  <CardContent>
    {/* Content */}
  </CardContent>
</Card>
```

### 4. Alternative Ansätze

#### A) Migration zu @dnd-kit
Das Projekt hat bereits @dnd-kit als Dependency. Diese Library bietet:
- Bessere Kontrolle über Drag-Verhalten
- Keine Position-Fixed Issues
- Modernere API mit besserer Performance

#### B) Vereinfachung der Card-Styles
- Entfernen aller Transform-basierten Animationen
- Minimale Style-Änderungen während des Draggings
- Keine komplexen Box-Shadow Transitionen

#### C) Debug-Modus implementieren
```tsx
// Temporär für Debugging
const DEBUG_DRAG = true;

{DEBUG_DRAG && snapshot.isDragging && (
  <Box sx={{ position: 'fixed', top: 10, right: 10, bgcolor: 'white', p: 2 }}>
    <pre>{JSON.stringify({
      dragOffset: provided.draggableProps.style,
      cardDimensions: {
        width: ref.current?.offsetWidth,
        height: ref.current?.offsetHeight,
      }
    }, null, 2)}</pre>
  </Box>
)}
```

### 5. Empfohlene Vorgehensweise

1. **Schritt 1**: Implementiere Lösung 3 (Box-Model Reset) als Quick-Fix
2. **Schritt 2**: Teste mit verschiedenen Browser und Zoom-Levels
3. **Schritt 3**: Falls Problem besteht, implementiere Custom Drag Layer (Lösung 2)
4. **Schritt 4**: Erwäge langfristig Migration zu @dnd-kit für bessere Kontrolle

### 6. Testing-Checkliste

- [ ] Chrome (verschiedene Zoom-Level: 100%, 125%, 150%)
- [ ] Firefox
- [ ] Safari
- [ ] Mit/ohne Browser-DevTools geöffnet
- [ ] Verschiedene Viewport-Größen
- [ ] Drag von verschiedenen Positionen der Karte (oben, mitte, unten)
- [ ] Schnelles vs. langsames Dragging

### 7. Weitere Ressourcen

- [@hello-pangea/dnd GitHub Issues](https://github.com/hello-pangea/dnd/issues)
- [react-beautiful-dnd Known Issues](https://github.com/atlassian/react-beautiful-dnd/issues?q=is%3Aissue+offset)
- [@dnd-kit Migration Guide](https://docs.dndkit.com/guides/migration)