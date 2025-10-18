import React from 'react';
import { useDraggable } from '@dnd-kit/core';
import { OpportunityCard } from './kanban/OpportunityCard';
import type { Opportunity } from '../types';

interface SortableOpportunityCardProps {
  opportunity: Opportunity;
  onQuickAction?: (opportunityId: string, action: 'won' | 'lost' | 'reactivate') => void;
  isAnimating?: boolean;
}

export const SortableOpportunityCard: React.FC<SortableOpportunityCardProps> = ({
  opportunity,
  onQuickAction,
  isAnimating: _isAnimating = false,
}) => {
  const {
    attributes,
    listeners,
    setNodeRef,
    transform,
    isDragging,
  } = useDraggable({
    id: opportunity.id,
    data: { opportunity }, // Sprint 2.1.7.1: Pass opportunity data for DragOverlay
  });

  // Sprint 2.1.7.1: FINAL - Hide original, DragOverlay (portal) shows it
  const style: React.CSSProperties = {
    transform: transform ? `translate3d(${transform.x}px, ${transform.y}px, 0)` : undefined,
    opacity: isDragging ? 0 : 1,  // Hide completely - DragOverlay renders outside DOM
    visibility: isDragging ? 'hidden' : 'visible',
    marginBottom: '12px',
  };

  return (
    <div
      ref={setNodeRef}
      style={style}
      {...attributes}
      {...listeners}
    >
      <OpportunityCard
        opportunity={opportunity}
        onQuickAction={onQuickAction}
        showActions={true}
        isDragging={isDragging}
      />
    </div>
  );
};
