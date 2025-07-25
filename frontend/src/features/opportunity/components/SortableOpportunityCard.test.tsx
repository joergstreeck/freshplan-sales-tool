import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import { DndContext } from '@dnd-kit/core';
import { SortableOpportunityCard } from './SortableOpportunityCard';
import '@testing-library/jest-dom';

// Mock @dnd-kit/sortable
vi.mock('@dnd-kit/sortable', () => ({
  useSortable: vi.fn(() => ({
    attributes: {},
    listeners: {},
    setNodeRef: vi.fn(),
    transform: null,
    transition: '',
    isDragging: false,
  })),
}));

// Import after mock
import { useSortable } from '@dnd-kit/sortable';

const mockOpportunity = {
  id: '1',
  name: 'Test Opportunity',
  customerName: 'Test Customer',
  value: 5000,
  stage: 'lead',
  probability: 20,
  expectedCloseDate: '2025-08-15',
  lastActivity: '2025-07-20',
  assignedTo: 'Test User',
  notes: 'Test notes',
  createdAt: '2025-07-01',
  updatedAt: '2025-07-20',
};

describe('SortableOpportunityCard', () => {
  const defaultProps = {
    opportunity: mockOpportunity,
    onQuickAction: vi.fn(),
    isAnimating: false,
  };

  beforeEach(() => {
    // Reset mock before each test
    vi.mocked(useSortable).mockReturnValue({
      attributes: {},
      listeners: {},
      setNodeRef: vi.fn(),
      transform: null,
      transition: '',
      isDragging: false,
    });
  });

  it('renders without crashing', () => {
    render(
      <DndContext>
        <SortableOpportunityCard {...defaultProps} />
      </DndContext>
    );
  });

  it('applies correct styles when not dragging', () => {
    const { container } = render(
      <DndContext>
        <SortableOpportunityCard {...defaultProps} />
      </DndContext>
    );
    
    const card = container.firstChild;
    expect(card).toHaveStyle({
      opacity: '1',
      width: '100%',
      marginBottom: '12px',
    });
  });

  it('passes through all props to OpportunityCard', () => {
    render(
      <DndContext>
        <SortableOpportunityCard {...defaultProps} />
      </DndContext>
    );
    
    // Verify opportunity data is displayed
    expect(screen.getByText('Test Opportunity')).toBeInTheDocument();
    expect(screen.getByText('Test Customer')).toBeInTheDocument();
  });

  it('handles animation state correctly', () => {
    const { rerender, container } = render(
      <DndContext>
        <SortableOpportunityCard {...defaultProps} isAnimating={true} />
      </DndContext>
    );
    
    // When animating, opacity should be 0
    const card = container.firstChild;
    expect(card).toHaveStyle({
      opacity: '0',
      scale: '1.1',
    });
    
    // When not animating
    rerender(
      <DndContext>
        <SortableOpportunityCard {...defaultProps} isAnimating={false} />
      </DndContext>
    );
    
    expect(card).toHaveStyle({
      opacity: '1',
      scale: '1',
    });
  });

  it('applies transform styles when dragging', () => {
    // Mock dragging state
    vi.mocked(useSortable).mockReturnValue({
      attributes: {},
      listeners: {},
      setNodeRef: vi.fn(),
      transform: { x: 10, y: 20, scaleX: 1, scaleY: 1 },
      transition: 'transform 200ms ease',
      isDragging: true,
    });
    
    const { container } = render(
      <DndContext>
        <SortableOpportunityCard {...defaultProps} />
      </DndContext>
    );
    
    const card = container.firstChild;
    expect(card).toHaveStyle({
      opacity: '0.6',
    });
  });
});