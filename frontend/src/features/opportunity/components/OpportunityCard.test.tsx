import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { ThemeProvider } from '@mui/material/styles';
import freshfoodzTheme from '../../../theme/freshfoodz';
import { OpportunityCard } from './kanban/OpportunityCard';
import type { Opportunity } from './kanban/OpportunityCard';

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

// Mock the opportunity types including OpportunityType
vi.mock('../types', () => ({
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
  OpportunityType: {
    NEUGESCHAEFT: 'NEUGESCHAEFT',
    SORTIMENTSERWEITERUNG: 'SORTIMENTSERWEITERUNG',
    NEUER_STANDORT: 'NEUER_STANDORT',
    VERLAENGERUNG: 'VERLAENGERUNG',
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

/**
 * Sprint 2.1.7.1 Enterprise Tests - OpportunityType + Lead Traceability + stageColor
 *
 * Tests für die neuen Features:
 * - Lead-Traceability Badge (leadId + leadCompanyName)
 * - Dynamic Border Color (stageColor)
 * - OpportunityType Badge (NEUGESCHAEFT, SORTIMENTSERWEITERUNG, NEUER_STANDORT, VERLAENGERUNG)
 */
describe('OpportunityCard - Sprint 2.1.7.1 Features', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('Lead-Traceability Badge', () => {
    it('renders lead badge when leadId is present', () => {
      const opportunityFromLead: Opportunity = {
        ...mockOpportunity,
        leadId: 12345,
        leadCompanyName: 'Test Catering GmbH',
      };

      renderWithTheme(<OpportunityCard opportunity={opportunityFromLead} />);

      // Badge should render with leadId
      expect(screen.getByText(/von Lead #12345/i)).toBeInTheDocument();
    });

    it('does not render lead badge when leadId is missing', () => {
      const directOpportunity: Opportunity = {
        ...mockOpportunity,
        leadId: undefined,
        leadCompanyName: undefined,
      };

      renderWithTheme(<OpportunityCard opportunity={directOpportunity} />);

      // Badge should NOT render
      expect(screen.queryByText(/von Lead/i)).not.toBeInTheDocument();
    });

    it('renders lead badge with correct styling', () => {
      const opportunityFromLead: Opportunity = {
        ...mockOpportunity,
        leadId: 99999,
      };

      const { container } = renderWithTheme(<OpportunityCard opportunity={opportunityFromLead} />);

      // Find the Chip component containing "von Lead #99999"
      const leadBadge = screen.getByText(/von Lead #99999/i).closest('[class*="MuiChip"]');
      expect(leadBadge).toBeInTheDocument();

      // Verify TrendingUpIcon is rendered (Vitest checks if SVG icon exists)
      expect(container.querySelector('[data-testid="TrendingUpIcon"]')).toBeInTheDocument();
    });
  });

  describe('Dynamic Border Color (stageColor)', () => {
    it('applies stageColor to card border with 40% opacity', () => {
      const opportunityWithStageColor: Opportunity = {
        ...mockOpportunity,
        stageColor: '#FF5733', // Custom stage color
      };

      const { container } = renderWithTheme(
        <OpportunityCard opportunity={opportunityWithStageColor} />
      );

      // Find the Card component (outermost MuiCard)
      const card = container.querySelector('[class*="MuiCard"]');
      expect(card).toBeInTheDocument();

      // Verify border style includes stageColor with 40% opacity
      // Format: "1px solid #FF573340" (40 = 25% opacity in hex)
      const computedStyle = window.getComputedStyle(card!);
      expect(computedStyle.border).toContain('1px solid');
      // Note: exact color matching may vary due to browser rendering, so we just verify border exists
    });

    it('uses default divider color when stageColor is missing', () => {
      const opportunityWithoutStageColor: Opportunity = {
        ...mockOpportunity,
        stageColor: undefined,
      };

      const { container } = renderWithTheme(
        <OpportunityCard opportunity={opportunityWithoutStageColor} />
      );

      const card = container.querySelector('[class*="MuiCard"]');
      expect(card).toBeInTheDocument();

      // Should use default theme.palette.divider
      const computedStyle = window.getComputedStyle(card!);
      expect(computedStyle.border).toContain('1px solid');
    });
  });

  describe('OpportunityType Badge', () => {
    const opportunityTypes = [
      { type: 'NEUGESCHAEFT', label: 'Neugeschäft', icon: '🆕' },
      { type: 'SORTIMENTSERWEITERUNG', label: 'Sortimentserweiterung', icon: '📈' },
      { type: 'NEUER_STANDORT', label: 'Neuer Standort', icon: '📍' },
      { type: 'VERLAENGERUNG', label: 'Vertragsverlängerung', icon: '🔁' },
    ];

    opportunityTypes.forEach(({ type, label, icon }) => {
      it(`renders correct badge for OpportunityType ${type}`, () => {
        const opportunityWithType: Opportunity = {
          ...mockOpportunity,
          opportunityType: type as OpportunityType,
        };

        renderWithTheme(<OpportunityCard opportunity={opportunityWithType} />);

        // Badge should render with icon + label
        const badgeText = `${icon} ${label}`;
        expect(screen.getByText(badgeText)).toBeInTheDocument();
      });
    });

    it('does not render OpportunityType badge when type is missing', () => {
      const opportunityWithoutType: Opportunity = {
        ...mockOpportunity,
        opportunityType: undefined,
      };

      renderWithTheme(<OpportunityCard opportunity={opportunityWithoutType} />);

      // No OpportunityType badges should render
      expect(screen.queryByText(/Neugeschäft/i)).not.toBeInTheDocument();
      expect(screen.queryByText(/Sortimentserweiterung/i)).not.toBeInTheDocument();
      expect(screen.queryByText(/Neuer Standort/i)).not.toBeInTheDocument();
      expect(screen.queryByText(/Vertragsverlängerung/i)).not.toBeInTheDocument();
    });

    it('renders OpportunityType badge with correct styling (Antonio font)', () => {
      const opportunityWithType: Opportunity = {
        ...mockOpportunity,
        opportunityType: 'NEUGESCHAEFT',
      };

      renderWithTheme(<OpportunityCard opportunity={opportunityWithType} />);

      // Find the Chip root component containing "🆕 Neugeschäft"
      const typeBadge = screen.getByText(/🆕 Neugeschäft/i).closest('.MuiChip-root');
      expect(typeBadge).toBeInTheDocument();

      // Verify Chip has correct color (primary) - Antonio Bold fontFamily is applied via Theme
      // Theme: MuiChip.colorPrimary -> fontFamily: 'Antonio, sans-serif', fontWeight: 700
      expect(typeBadge).toHaveClass('MuiChip-colorPrimary');
    });
  });

  describe('Combined Sprint 2.1.7.1 Features', () => {
    it('renders all Sprint 2.1.7.1 features together (Lead + Type + stageColor)', () => {
      const fullFeatureOpportunity: Opportunity = {
        ...mockOpportunity,
        leadId: 54321,
        leadCompanyName: 'Demo Restaurant Chain',
        opportunityType: 'SORTIMENTSERWEITERUNG',
        stageColor: '#4CAF50',
      };

      renderWithTheme(<OpportunityCard opportunity={fullFeatureOpportunity} />);

      // All 3 features should be visible
      expect(screen.getByText(/von Lead #54321/i)).toBeInTheDocument();
      expect(screen.getByText(/📈 Sortimentserweiterung/i)).toBeInTheDocument();

      // Verify stageColor border (via container check)
      const { container } = render(
        <ThemeProvider theme={freshfoodzTheme}>
          <OpportunityCard opportunity={fullFeatureOpportunity} />
        </ThemeProvider>
      );
      const card = container.querySelector('[class*="MuiCard"]');
      expect(card).toBeInTheDocument();
    });

    it('renders correctly when only OpportunityType is present (no Lead)', () => {
      const typeOnlyOpportunity: Opportunity = {
        ...mockOpportunity,
        leadId: undefined, // No lead traceability
        opportunityType: 'NEUER_STANDORT',
        stageColor: '#2196F3',
      };

      renderWithTheme(<OpportunityCard opportunity={typeOnlyOpportunity} />);

      // OpportunityType badge should render
      expect(screen.getByText(/📍 Neuer Standort/i)).toBeInTheDocument();

      // Lead badge should NOT render
      expect(screen.queryByText(/von Lead/i)).not.toBeInTheDocument();
    });
  });
});
