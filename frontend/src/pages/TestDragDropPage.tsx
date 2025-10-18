/**
 * ISOLATED Drag & Drop Test Page
 * Sprint 2.1.7.1 - Debugging transformOrigin Bug
 *
 * URL: /test-dragdrop
 *
 * MINIMAL Setup - NO Production Code!
 */

import React, { useState } from 'react';
import { Box, Typography, Paper } from '@mui/material';
import {
  DndContext,
  DragOverlay,
  closestCorners,
  PointerSensor,
  useSensor,
  useSensors,
  useDroppable,
  useDraggable,
} from '@dnd-kit/core';
import type { DragEndEvent, DragStartEvent } from '@dnd-kit/core';

// Simple Card Type
interface SimpleCard {
  id: string;
  title: string;
  column: string;
}

// Mock Data - 3 Columns, 3 Cards
const INITIAL_CARDS: SimpleCard[] = [
  { id: '1', title: 'Card 1', column: 'A' },
  { id: '2', title: 'Card 2', column: 'A' },
  { id: '3', title: 'Card 3', column: 'B' },
];

// Draggable Card Component
const DraggableCard: React.FC<{ card: SimpleCard }> = ({ card }) => {
  const { attributes, listeners, setNodeRef, transform, isDragging } = useDraggable({
    id: card.id,
    data: { card },
  });

  const style: React.CSSProperties = {
    transform: transform ? `translate3d(${transform.x}px, ${transform.y}px, 0)` : undefined,
    opacity: isDragging ? 0.3 : 1,
    padding: '16px',
    marginBottom: '8px',
    backgroundColor: 'white',
    border: '1px solid #ddd',
    borderRadius: '4px',
    cursor: 'grab',
  };

  return (
    <div ref={setNodeRef} style={style} {...attributes} {...listeners}>
      <Typography>{card.title}</Typography>
    </div>
  );
};

// Droppable Column Component
const DroppableColumn: React.FC<{ id: string; title: string; children: React.ReactNode }> = ({
  id,
  title,
  children,
}) => {
  const { setNodeRef, isOver } = useDroppable({ id });

  return (
    <Paper
      ref={setNodeRef}
      sx={{
        width: 250,
        minHeight: 400,
        p: 2,
        bgcolor: isOver ? 'rgba(148, 196, 86, 0.1)' : 'background.paper',
        border: isOver ? '2px dashed #94C456' : '1px solid #ddd',
      }}
    >
      <Typography variant="h6" sx={{ mb: 2 }}>
        {title}
      </Typography>
      {children}
    </Paper>
  );
};

export const TestDragDropPage: React.FC = () => {
  const [cards, setCards] = useState<SimpleCard[]>(INITIAL_CARDS);
  const [activeCard, setActiveCard] = useState<SimpleCard | null>(null);

  const sensors = useSensors(
    useSensor(PointerSensor, {
      activationConstraint: {
        distance: 8,
      },
    })
  );

  const handleDragStart = (event: DragStartEvent) => {
    const card = event.active.data.current?.card as SimpleCard;
    setActiveCard(card);
    console.log('[DRAG START]', { card, transform: event.active.rect });
  };

  const handleDragEnd = (event: DragEndEvent) => {
    const { active, over } = event;
    setActiveCard(null);

    if (!over) return;

    const cardId = active.id as string;
    const newColumn = over.id as string;

    setCards((prev) =>
      prev.map((card) => (card.id === cardId ? { ...card, column: newColumn } : card))
    );

    console.log('[DRAG END]', { cardId, newColumn, transform: event.active.rect });
  };

  const columnA = cards.filter((c) => c.column === 'A');
  const columnB = cards.filter((c) => c.column === 'B');
  const columnC = cards.filter((c) => c.column === 'C');

  return (
    <Box sx={{ p: 4 }}>
      <Typography variant="h4" sx={{ mb: 3, fontFamily: 'Antonio', color: '#004F7B' }}>
        🧪 Drag & Drop Test Lab
      </Typography>

      <Typography variant="body2" sx={{ mb: 3, color: 'text.secondary' }}>
        MINIMAL Setup - 3 Columns, 3 Cards. Check Browser Console for Debug Logs.
      </Typography>

      <DndContext
        sensors={sensors}
        collisionDetection={closestCorners}
        onDragStart={handleDragStart}
        onDragEnd={handleDragEnd}
      >
        <Box sx={{ display: 'flex', gap: 2 }}>
          <DroppableColumn id="A" title="Column A">
            {columnA.map((card) => (
              <DraggableCard key={card.id} card={card} />
            ))}
          </DroppableColumn>

          <DroppableColumn id="B" title="Column B">
            {columnB.map((card) => (
              <DraggableCard key={card.id} card={card} />
            ))}
          </DroppableColumn>

          <DroppableColumn id="C" title="Column C">
            {columnC.map((card) => (
              <DraggableCard key={card.id} card={card} />
            ))}
          </DroppableColumn>
        </Box>

        <DragOverlay>
          {activeCard && (
            <Box
              sx={{
                p: 2,
                bgcolor: 'white',
                border: '2px solid #94C456',
                borderRadius: 1,
                boxShadow: 4,
                transform: 'scale(1.05)',
                cursor: 'grabbing',
              }}
            >
              <Typography>{activeCard.title}</Typography>
            </Box>
          )}
        </DragOverlay>
      </DndContext>

      <Box sx={{ mt: 4, p: 2, bgcolor: '#f5f5f5', borderRadius: 1 }}>
        <Typography variant="caption" sx={{ fontFamily: 'monospace' }}>
          DEBUG: Open Browser Console (F12) → Watch [DRAG START] and [DRAG END] logs
        </Typography>
      </Box>
    </Box>
  );
};

export default TestDragDropPage;
