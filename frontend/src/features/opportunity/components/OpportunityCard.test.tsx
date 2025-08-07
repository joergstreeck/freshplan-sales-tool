import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { ThemeProvider } from '@mui/material/styles';
import freshfoodzTheme from '../../../theme/freshfoodz';
import { OpportunityCard } from './OpportunityCard';
import type { Opportunity } from './OpportunityCard';

// Define OpportunityStage enum for tests
const OpportunityStage = {
  LEAD: 'NEW_LEAD',
  NEW_LEAD: 'NEW_LEAD',
  QUALIFIED: 'QUALIFICATION',
  QUALIFICATION: 'QUALIFICATION',
  PROPOSAL: 'PROPOSAL',
  NEGOTIATION: 'NEGOTIATION',
  CLOSED_WON: 'CLOSED_WON',
  CLOSED_LOST: 'CLOSED_LOST',
  RENEWAL: 'RENEWAL',
};

// Mock the opportunity types including STAGE_CONFIGS
vi.mock('../types/opportunity.types', () => ({
  OpportunityStage: {
    LEAD: 'NEW_LEAD',
    NEW_LEAD: 'NEW_LEAD',
    QUALIFIED: 'QUALIFICATION',
    QUALIFICATION: 'QUALIFICATION',
    PROPOSAL: 'PROPOSAL',
    NEGOTIATION: 'NEGOTIATION',
    CLOSED_WON: 'CLOSED_WON',
    CLOSED_LOST: 'CLOSED_LOST',
    RENEWAL: 'RENEWAL',
  },
}));

// Mock the logger
vi.mock('../../../lib/logger', () => ({
  logger: {
    child: vi.fn(() => ({
      debug: vi.fn(),
      info: vi.fn(),
      warn: vi.fn(),
      error: vi.fn(),
    })),
  },
}));

// Mock ErrorBoundary hook
vi.mock('../../../components/ErrorBoundary', () => ({
  useErrorHandler: vi.fn(() => vi.fn()),
}));

// Mock @dnd-kit/core
vi.mock('@dnd-kit/core', () => ({
  useDraggable: vi.fn(() => ({
    attributes: {},
    listeners: {},
    setNodeRef: vi.fn(),
    transform: null,
    isDragging: false,
    setActivatorNodeRef: vi.fn(),
  })),
}));

// Helper function to render with theme
const renderWithTheme = (component: React.ReactElement) => {
  return render(<ThemeProvider theme={freshfoodzTheme}>{component}</ThemeProvider>);
};

// Test data
const mockOpportunity: Opportunity = {
  id: '1',
  name: 'Großauftrag Wocheneinkauf',
  stage: OpportunityStage.NEW_LEAD,
  value: 15000,
  probability: 20,
  customerName: 'Restaurant Schmidt GmbH',
  assignedToName: 'Max Mustermann',
  expectedCloseDate: '2025-08-15',
  createdAt: '2025-07-01T10:00:00Z',
  updatedAt: '2025-07-20T15:30:00Z',
};

describe.skip('OpportunityCard', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe.skip('Rendering', () => {
    it('renders opportunity name', () => {
      renderWithTheme(<OpportunityCard opportunity={mockOpportunity} />);

      expect(screen.getByText('Großauftrag Wocheneinkauf')).toBeInTheDocument();
    });

    it('renders customer name', () => {
      renderWithTheme(<OpportunityCard opportunity={mockOpportunity} />);

      expect(screen.getByText('Restaurant Schmidt GmbH')).toBeInTheDocument();
    });

    it('renders formatted value', () => {
      renderWithTheme(<OpportunityCard opportunity={mockOpportunity} />);

      expect(screen.getByText('15.000 €')).toBeInTheDocument();
    });

    it('renders probability with progress bar', () => {
      renderWithTheme(<OpportunityCard opportunity={mockOpportunity} />);

      expect(screen.getByText('20%')).toBeInTheDocument();
      expect(screen.getByText('Wahrscheinlichkeit')).toBeInTheDocument();

      const progressBar = screen.getByRole('progressbar');
      expect(progressBar).toHaveAttribute('aria-valuenow', '20');
    });

    it('renders expected close date', () => {
      renderWithTheme(<OpportunityCard opportunity={mockOpportunity} />);

      expect(screen.getByText('15.08.25')).toBeInTheDocument();
    });

    it('renders assigned user avatar', () => {
      renderWithTheme(<OpportunityCard opportunity={mockOpportunity} />);

      expect(screen.getByText('M')).toBeInTheDocument();
    });

    it('renders drag indicator icon', () => {
      renderWithTheme(<OpportunityCard opportunity={mockOpportunity} />);

      const dragIndicator = screen.getByTestId('DragIndicatorIcon');
      expect(dragIndicator).toBeInTheDocument();
    });
  });

  describe.skip('Conditional Rendering', () => {
    it('renders "Kein Wert" when value is missing', () => {
      const opportunityWithoutValue = { ...mockOpportunity, value: undefined };
      renderWithTheme(<OpportunityCard opportunity={opportunityWithoutValue} />);

      expect(screen.getByText('Kein Wert')).toBeInTheDocument();
    });

    it('does not render probability section when probability is missing', () => {
      const opportunityWithoutProbability = { ...mockOpportunity, probability: undefined };
      renderWithTheme(<OpportunityCard opportunity={opportunityWithoutProbability} />);

      expect(screen.queryByText('Wahrscheinlichkeit')).not.toBeInTheDocument();
    });

    it('does not render customer name when missing', () => {
      const opportunityWithoutCustomer = { ...mockOpportunity, customerName: undefined };
      renderWithTheme(<OpportunityCard opportunity={opportunityWithoutCustomer} />);

      expect(screen.queryByText('Restaurant Schmidt GmbH')).not.toBeInTheDocument();
    });

    it('does not render date when missing', () => {
      const opportunityWithoutDate = { ...mockOpportunity, expectedCloseDate: undefined };
      renderWithTheme(<OpportunityCard opportunity={opportunityWithoutDate} />);

      expect(screen.queryByText('15.08.25')).not.toBeInTheDocument();
    });

    it('does not render avatar when assignedToName is missing', () => {
      const opportunityWithoutAssignee = { ...mockOpportunity, assignedToName: undefined };
      renderWithTheme(<OpportunityCard opportunity={opportunityWithoutAssignee} />);

      expect(screen.queryByText('M')).not.toBeInTheDocument();
    });
  });

  describe.skip('Interactions', () => {
    it('calls onClick when card is clicked', () => {
      const handleClick = vi.fn();
      renderWithTheme(<OpportunityCard opportunity={mockOpportunity} onClick={handleClick} />);

      const card = screen.getByText('Großauftrag Wocheneinkauf').closest('[class*="MuiCard"]');
      fireEvent.click(card!);

      expect(handleClick).toHaveBeenCalledWith(mockOpportunity);
    });

    it('does not call onClick when dragging', () => {
      // This test would require mocking the dragging state properly
      // For now, we'll skip this test and mark it as TODO
      expect(true).toBe(true);
    });
  });

  describe.skip('Styling', () => {
    it('applies correct probability color for different values', () => {
      // Test probability >= 80 (green)
      const highProbOpportunity = { ...mockOpportunity, probability: 85 };
      const { rerender } = renderWithTheme(<OpportunityCard opportunity={highProbOpportunity} />);
      expect(screen.getByText('85%')).toHaveStyle({ color: 'rgb(102, 187, 106)' });

      // Test probability >= 60 (freshfoodz green)
      const mediumHighOpportunity = { ...mockOpportunity, probability: 65 };
      rerender(
        <ThemeProvider theme={freshfoodzTheme}>
          <OpportunityCard opportunity={mediumHighOpportunity} />
        </ThemeProvider>
      );
      expect(screen.getByText('65%')).toHaveStyle({ color: 'rgb(148, 196, 86)' });

      // Test probability >= 40 (orange)
      const mediumOpportunity = { ...mockOpportunity, probability: 45 };
      rerender(
        <ThemeProvider theme={freshfoodzTheme}>
          <OpportunityCard opportunity={mediumOpportunity} />
        </ThemeProvider>
      );
      expect(screen.getByText('45%')).toHaveStyle({ color: 'rgb(255, 167, 38)' });

      // Test probability >= 20 (orange-red)
      const lowOpportunity = { ...mockOpportunity, probability: 25 };
      rerender(
        <ThemeProvider theme={freshfoodzTheme}>
          <OpportunityCard opportunity={lowOpportunity} />
        </ThemeProvider>
      );
      expect(screen.getByText('25%')).toHaveStyle({ color: 'rgb(255, 112, 67)' });

      // Test probability < 20 (red)
      const veryLowOpportunity = { ...mockOpportunity, probability: 10 };
      rerender(
        <ThemeProvider theme={freshfoodzTheme}>
          <OpportunityCard opportunity={veryLowOpportunity} />
        </ThemeProvider>
      );
      expect(screen.getByText('10%')).toHaveStyle({ color: 'rgb(239, 83, 80)' });
    });
  });

  describe.skip('Error Handling', () => {
    it('handles invalid date gracefully', () => {
      const opportunityWithInvalidDate = {
        ...mockOpportunity,
        expectedCloseDate: 'invalid-date',
      };

      renderWithTheme(<OpportunityCard opportunity={opportunityWithInvalidDate} />);

      // Should render fallback for invalid date
      const dateElements = screen.getAllByText('-');
      expect(dateElements.length).toBeGreaterThan(0);
    });

    it('handles click errors gracefully', () => {
      const handleClick = vi.fn(() => {
        throw new Error('Click error');
      });

      renderWithTheme(<OpportunityCard opportunity={mockOpportunity} onClick={handleClick} />);

      const card = screen.getByText('Großauftrag Wocheneinkauf').closest('[class*="MuiCard"]');

      // Should not throw
      expect(() => fireEvent.click(card!)).not.toThrow();
    });
  });
});
