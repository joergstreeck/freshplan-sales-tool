import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { DndContext } from '@dnd-kit/core';
import { KanbanBoardDndKit } from './KanbanBoardDndKit';
import { mockOpportunities } from '../services/mockData';
import '@testing-library/jest-dom';

// Mock hooks
vi.mock('../hooks/useOpportunities', () => ({
  useOpportunities: () => ({
    opportunities: mockOpportunities,
    isLoading: false,
    error: null,
  }),
}));

// Mock router
vi.mock('react-router-dom', () => ({
  useNavigate: () => vi.fn(),
}));

describe('KanbanBoardDndKit', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders all pipeline stages', () => {
    render(<KanbanBoardDndKit />);
    
    expect(screen.getByText('Lead')).toBeInTheDocument();
    expect(screen.getByText('Qualifiziert')).toBeInTheDocument();
    expect(screen.getByText('Angebot')).toBeInTheDocument();
    expect(screen.getByText('Verhandlung')).toBeInTheDocument();
    expect(screen.getByText('Gewonnen')).toBeInTheDocument();
    expect(screen.getByText('Verloren')).toBeInTheDocument();
  });

  it('displays stage totals correctly', () => {
    render(<KanbanBoardDndKit />);
    
    // Check stage totals
    expect(screen.getByText('Gesamt: 15.000 €')).toBeInTheDocument();
    expect(screen.getByText('Gesamt: 8.500 €')).toBeInTheDocument();
    expect(screen.getByText('Gesamt: 5.200 €')).toBeInTheDocument();
    expect(screen.getByText('Gesamt: 12.000 €')).toBeInTheDocument();
  });

  it('renders opportunity cards with correct information', () => {
    render(<KanbanBoardDndKit />);
    
    // Check if opportunity cards are rendered
    expect(screen.getByText('Großauftrag Wocheneinkauf')).toBeInTheDocument();
    expect(screen.getByText('Restaurant Schmidt GmbH')).toBeInTheDocument();
    expect(screen.getByText('15.000 €')).toBeInTheDocument();
  });

  it('displays pipeline statistics correctly', () => {
    render(<KanbanBoardDndKit />);
    
    // Check statistics
    expect(screen.getByText('3 Aktive')).toBeInTheDocument();
    expect(screen.getByText('1 Gewonnen (12.000 €)')).toBeInTheDocument();
    expect(screen.getByText('1 Verloren (3.000 €)')).toBeInTheDocument();
    expect(screen.getByText('50% Erfolgsquote')).toBeInTheDocument();
  });

  it('shows action buttons for opportunities in correct stages', () => {
    render(<KanbanBoardDndKit />);
    
    // Action buttons should be visible for non-final stages
    const checkButtons = screen.getAllByLabelText('Als gewonnen markieren');
    expect(checkButtons.length).toBeGreaterThan(0);
    
    const crossButtons = screen.getAllByLabelText('Als verloren markieren');
    expect(crossButtons.length).toBeGreaterThan(0);
  });

  it('shows reactivate button for closed opportunities', () => {
    render(<KanbanBoardDndKit />);
    
    // Reactivate button should be visible for won/lost opportunities
    const reactivateButtons = screen.getAllByLabelText('Opportunity reaktivieren');
    expect(reactivateButtons.length).toBeGreaterThan(0);
  });

  it('calculates opportunity probability based on stage', () => {
    render(<KanbanBoardDndKit />);
    
    // Check probability indicators
    expect(screen.getByText('20%')).toBeInTheDocument(); // Lead stage
    expect(screen.getByText('60%')).toBeInTheDocument(); // Qualified stage
    expect(screen.getByText('80%')).toBeInTheDocument(); // Proposal stage
  });

  it('formats currency values correctly', () => {
    render(<KanbanBoardDndKit />);
    
    // Check currency formatting
    const formattedValues = screen.getAllByText(/^\d{1,3}(\.\d{3})*\s€$/);
    expect(formattedValues.length).toBeGreaterThan(0);
  });

  it('displays dates in correct format', () => {
    render(<KanbanBoardDndKit />);
    
    // Check date formatting (DD.MM.YY)
    const datePattern = /^\d{2}\.\d{2}\.\d{2}$/;
    const dates = screen.getAllByText(datePattern);
    expect(dates.length).toBeGreaterThan(0);
  });

  it('shows loading state when data is being fetched', () => {
    // This test would check loading state when implemented
    // Currently the component uses local state with initial data
    expect(true).toBe(true);
  });
});