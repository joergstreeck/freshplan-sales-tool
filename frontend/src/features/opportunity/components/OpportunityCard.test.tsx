import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { DndContext } from '@dnd-kit/core';
import { OpportunityCard } from './OpportunityCard';
import '@testing-library/jest-dom';

const mockOpportunity = {
  id: '1',
  name: 'Großauftrag Wocheneinkauf',
  customerName: 'Restaurant Schmidt GmbH',
  assignedToName: 'Hans Schmidt',
  value: 15000,
  stage: 'lead',
  probability: 20,
  expectedCloseDate: '2025-08-25',
  lastActivity: '2025-07-15',
  notes: 'Interessiert an wöchentlicher Belieferung',
  createdAt: '2025-07-01',
  updatedAt: '2025-07-15',
};

describe('OpportunityCard', () => {
  const defaultProps = {
    opportunity: mockOpportunity,
    onClick: vi.fn(),
  };

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders opportunity information correctly', () => {
    render(
      <DndContext>
        <OpportunityCard {...defaultProps} />
      </DndContext>
    );
    
    expect(screen.getByText('Großauftrag Wocheneinkauf')).toBeInTheDocument();
    expect(screen.getByText('Restaurant Schmidt GmbH')).toBeInTheDocument();
    // assignedToName wird als Avatar mit erstem Buchstaben angezeigt
    expect(screen.getByText('H')).toBeInTheDocument();
    expect(screen.getByText('15.000 €')).toBeInTheDocument();
  });

  it('displays probability indicator correctly', () => {
    render(
      <DndContext>
        <OpportunityCard {...defaultProps} />
      </DndContext>
    );
    
    expect(screen.getByText('20%')).toBeInTheDocument();
    const progressBar = screen.getByRole('progressbar');
    expect(progressBar).toHaveAttribute('aria-valuenow', '20');
  });

  it('formats date correctly', () => {
    render(
      <DndContext>
        <OpportunityCard {...defaultProps} />
      </DndContext>
    );
    
    // Expected close date should be formatted as DD.MM.YY
    expect(screen.getByText('25.08.25')).toBeInTheDocument();
  });

  it('calls onClick when card is clicked', () => {
    render(
      <DndContext>
        <OpportunityCard {...defaultProps} />
      </DndContext>
    );
    
    // Card wird als button wegen Drag & Drop gerendert
    const card = screen.getByRole('button');
    fireEvent.click(card);
    
    expect(defaultProps.onClick).toHaveBeenCalledWith(mockOpportunity);
  });

  it('shows assigned user avatar with initial', () => {
    render(
      <DndContext>
        <OpportunityCard {...defaultProps} />
      </DndContext>
    );
    
    // Avatar zeigt ersten Buchstaben des zugewiesenen Benutzers
    const avatar = screen.getByText('H');
    expect(avatar).toBeInTheDocument();
  });

  it('handles missing assignedToName gracefully', () => {
    const opportunityWithoutAssignee = { ...mockOpportunity, assignedToName: undefined };
    render(
      <DndContext>
        <OpportunityCard {...defaultProps} opportunity={opportunityWithoutAssignee} />
      </DndContext>
    );
    
    // Avatar sollte nicht angezeigt werden
    expect(screen.queryByText('H')).not.toBeInTheDocument();
  });

  // Dieser Test ist irreführend und wird entfernt
  // Die OpportunityCard hat keine stage-basierte Styling-Logik
  it.todo('should apply stage-based styling when implemented');

  it('handles missing optional fields gracefully', () => {
    const minimalOpportunity = {
      ...mockOpportunity,
      notes: undefined,
      expectedCloseDate: undefined,
    };
    
    render(<OpportunityCard {...defaultProps} opportunity={minimalOpportunity} />);
    
    // Should still render without errors
    expect(screen.getByText('Großauftrag Wocheneinkauf')).toBeInTheDocument();
  });
});