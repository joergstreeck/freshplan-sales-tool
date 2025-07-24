import React from 'react';
import { useSortable } from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';
import { OpportunityCard } from './KanbanBoardDndKit';

interface Opportunity {
  id: string;
  name: string;
  stage: string;
  value?: number;
  probability?: number;
  customerName?: string;
  contactName?: string;
  assignedToName?: string;
  expectedCloseDate?: string;
  createdAt: string;
  updatedAt: string;
}

interface SortableOpportunityCardProps {
  opportunity: Opportunity;
  onQuickAction?: (opportunityId: string, action: 'won' | 'lost' | 'reactivate') => void;
  isAnimating?: boolean;
}

export const SortableOpportunityCard: React.FC<SortableOpportunityCardProps> = ({ opportunity, onQuickAction, isAnimating = false }) => {
  const {
    attributes,
    listeners,
    setNodeRef,
    transform,
    transition,
    isDragging,
  } = useSortable({ id: opportunity.id });

  const style = {
    transform: CSS.Transform.toString(transform),
    transition: isAnimating 
      ? 'all 0.5s cubic-bezier(0.4, 0, 0.2, 1)' 
      : transition,
    marginBottom: '12px',
    opacity: isDragging ? 0.5 : isAnimating ? 0 : 1,
    scale: isAnimating ? 1.1 : 1,
  };

  return (
    <div ref={setNodeRef} style={style} {...attributes} {...listeners}>
      <OpportunityCard 
        opportunity={opportunity} 
        onQuickAction={onQuickAction}
        showActions={true}
      />
    </div>
  );
};