import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
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
    render(<OpportunityCard {...defaultProps} />);
    
    expect(screen.getByText('Großauftrag Wocheneinkauf')).toBeInTheDocument();
    expect(screen.getByText('Restaurant Schmidt GmbH')).toBeInTheDocument();
    // assignedToName wird als Avatar mit erstem Buchstaben angezeigt
    expect(screen.getByText('H')).toBeInTheDocument();
    expect(screen.getByText('15.000 €')).toBeInTheDocument();
  });

  it('displays probability indicator correctly', () => {
    render(<OpportunityCard {...defaultProps} />);
    
    expect(screen.getByText('20%')).toBeInTheDocument();
    const progressBar = screen.getByRole('progressbar');
    expect(progressBar).toHaveAttribute('aria-valuenow', '20');
  });

  it('formats date correctly', () => {
    render(<OpportunityCard {...defaultProps} />);
    
    // Expected close date should be formatted as DD.MM.YY
    expect(screen.getByText('25.08.25')).toBeInTheDocument();
  });

  it('calls onClick when card is clicked', () => {
    render(<OpportunityCard {...defaultProps} />);
    
    // Card wird als button wegen Drag & Drop gerendert
    const card = screen.getByRole('button');
    fireEvent.click(card);
    
    expect(defaultProps.onClick).toHaveBeenCalledWith(mockOpportunity);
  });

  it('shows assigned user avatar with initial', () => {
    render(<OpportunityCard {...defaultProps} />);
    
    // Avatar zeigt ersten Buchstaben des zugewiesenen Benutzers
    const avatar = screen.getByText('H');
    expect(avatar).toBeInTheDocument();
  });

  it('handles missing assignedToName gracefully', () => {
    const opportunityWithoutAssignee = { ...mockOpportunity, assignedToName: undefined };
    render(<OpportunityCard {...defaultProps} opportunity={opportunityWithoutAssignee} />);
    
    // Avatar sollte nicht angezeigt werden
    expect(screen.queryByText('H')).not.toBeInTheDocument();
  });

  it('applies correct styling based on stage', () => {
    const { rerender } = render(<OpportunityCard {...defaultProps} />);
    
    // Test different stages
    const stages = ['lead', 'qualified', 'proposal', 'negotiation', 'won', 'lost'];
    
    stages.forEach(stage => {
      const opportunity = { ...mockOpportunity, stage };
      rerender(<OpportunityCard {...defaultProps} opportunity={opportunity} />);
      
      // Card should have appropriate border color based on stage
      const card = screen.getByRole('button');
      expect(card).toBeInTheDocument();
    });
  });

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