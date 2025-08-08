import React from 'react';
import { useSortable } from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';
import { OpportunityCard } from './KanbanBoardDndKit';
import type { Opportunity } from '../types';

interface SortableOpportunityCardProps {
  opportunity: Opportunity;
  onQuickAction?: (opportunityId: string, action: 'won' | 'lost' | 'reactivate') => void;
  isAnimating?: boolean;
}

export const SortableOpportunityCard: React.FC<SortableOpportunityCardProps> = ({
  opportunity,
  onQuickAction,
  isAnimating = false,
}) => {
  const { attributes, listeners, setNodeRef, transform, transition, isDragging } = useSortable({
    id: opportunity.id,
  });

  const style = {
    transform: CSS.Transform.toString(transform),
    transition: isAnimating ? 'all 0.5s cubic-bezier(0.4, 0, 0.2, 1)' : transition,
    marginBottom: '12px',
    opacity: isDragging ? 0.6 : isAnimating ? 0 : 1,
    scale: isAnimating ? 1.1 : 1,
    zIndex: isDragging ? 999 : 'auto',
    // CARD LAYOUT FIX
    width: '100%',
    boxSizing: 'border-box',
  };

  return (
    <div ref={setNodeRef} style={style} {...attributes} {...listeners}>
      <OpportunityCard opportunity={opportunity} onQuickAction={onQuickAction} showActions={true} />
    </div>
  );
};
