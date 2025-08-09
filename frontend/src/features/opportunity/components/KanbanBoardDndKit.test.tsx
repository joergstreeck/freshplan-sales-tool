import { describe, it, expect, vi } from 'vitest';
import { render, screen, within } from '@testing-library/react';
import { ThemeProvider } from '@mui/material/styles';
import freshfoodzTheme from '../../../theme/freshfoodz';
import { KanbanBoardDndKit } from './KanbanBoardDndKit';
import { logger } from '../../../lib/logger';

// Mock the logger
vi.mock('../../../lib/logger', () => ({
  logger: {
    child: vi.fn(() => ({
      debug: vi.fn(),
      info: vi.fn(),
      warn: vi.fn(),
      error: vi.fn(),
      time: vi.fn(() => vi.fn()),
    })),
  },
}));

// Mock ErrorBoundary hook
vi.mock('../../../components/ErrorBoundary', () => ({
  useErrorHandler: vi.fn(() => vi.fn()),
}));

// Mock the opportunity types including STAGE_CONFIGS
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
  STAGE_CONFIGS: [
    { stage: 'NEW_LEAD', label: 'Lead', color: '#9e9e9e' },
    { stage: 'QUALIFICATION', label: 'Qualifiziert', color: '#2196f3' },
    { stage: 'PROPOSAL', label: 'Angebot', color: '#ff9800' },
    { stage: 'NEGOTIATION', label: 'Verhandlung', color: '#9c27b0' },
    { stage: 'CLOSED_WON', label: 'Gewonnen', color: '#4caf50' },
    { stage: 'CLOSED_LOST', label: 'Verloren', color: '#f44336' },
    { stage: 'RENEWAL', label: 'Verlängerung', color: '#00bcd4' },
  ],
}));

// Helper function to render with theme
const renderWithTheme = (component: React.ReactElement) => {
  return render(<ThemeProvider theme={freshfoodzTheme}>{component}</ThemeProvider>);
};

describe.skip('KanbanBoardDndKit', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    // Mock requestAnimationFrame
    global.requestAnimationFrame = vi.fn(cb => setTimeout(cb, 0));
  });

  describe.skip('Rendering', () => {
    it('renders the pipeline header with title', () => {
      renderWithTheme(<KanbanBoardDndKit />);

      expect(screen.getByText('Verkaufschancen Pipeline')).toBeInTheDocument();
    });

    it('renders all pipeline stages', () => {
      renderWithTheme(<KanbanBoardDndKit />);

      expect(screen.getByText('Lead')).toBeInTheDocument();
      expect(screen.getByText('Qualifiziert')).toBeInTheDocument();
      expect(screen.getByText('Angebot')).toBeInTheDocument();
      expect(screen.getByText('Verhandlung')).toBeInTheDocument();
      expect(screen.getByText('Gewonnen')).toBeInTheDocument();
      expect(screen.getByText('Verloren')).toBeInTheDocument();
    });

    it('renders opportunities in correct stages', () => {
      renderWithTheme(<KanbanBoardDndKit />);

      // Check that opportunities are rendered
      expect(screen.getByText('Großauftrag Wocheneinkauf')).toBeInTheDocument();
      expect(screen.getByText('Wocheneinkauf Hotelküche')).toBeInTheDocument();
      expect(screen.getByText('Event-Paket Sommerfest')).toBeInTheDocument();
    });

    it('displays scroll indicator', () => {
      renderWithTheme(<KanbanBoardDndKit />);

      const scrollIndicator = document.getElementById('scrollIndicator');
      expect(scrollIndicator).toBeInTheDocument();
    });
  });

  describe.skip('Statistics', () => {
    it('displays active opportunities count', () => {
      renderWithTheme(<KanbanBoardDndKit />);

      expect(screen.getByText('3 Aktive')).toBeInTheDocument();
    });

    it('calculates and displays total active value', () => {
      renderWithTheme(<KanbanBoardDndKit />);

      // Total should be 15000 + 8500 + 5200 = 28.700 €
      expect(screen.getByText('28.700 €')).toBeInTheDocument();
    });

    it('displays success metrics when opportunities are closed', () => {
      renderWithTheme(<KanbanBoardDndKit />);

      expect(screen.getByText(/1 Gewonnen/)).toBeInTheDocument();
      expect(screen.getByText(/1 Verloren/)).toBeInTheDocument();
      expect(screen.getByText(/50% Erfolgsquote/)).toBeInTheDocument();
    });
  });

  describe.skip('Quick Actions', () => {
    it('shows quick action buttons on hover', async () => {
      renderWithTheme(<KanbanBoardDndKit />);

      // Find an opportunity card in active stage
      const opportunityCard = screen
        .getByText('Großauftrag Wocheneinkauf')
        .closest('[class*="MuiCard"]');

      // Hover over the card
      fireEvent.mouseEnter(opportunityCard!);

      // Quick action buttons should be visible
      await waitFor(() => {
        const buttons = within(opportunityCard!).getAllByRole('button');
        expect(buttons.length).toBeGreaterThan(0);
      });
    });

    it('shows reactivate button for lost opportunities', () => {
      renderWithTheme(<KanbanBoardDndKit />);

      // Find lost opportunity
      const lostOpportunity = screen
        .getByText('Testbestellung Kantine')
        .closest('[class*="MuiCard"]');

      fireEvent.mouseEnter(lostOpportunity!);

      // Should have reactivate button
      const reactivateButton = within(lostOpportunity!).getByLabelText('Opportunity reaktivieren');
      expect(reactivateButton).toBeInTheDocument();
    });
  });

  describe.skip('Scroll Handling', () => {
    it('updates scroll indicator on scroll', async () => {
      renderWithTheme(<KanbanBoardDndKit />);

      const scrollContainer = document.getElementById('kanbanScrollContainer');
      const scrollIndicator = document.getElementById('scrollIndicator');

      expect(scrollContainer).toBeInTheDocument();
      expect(scrollIndicator).toBeInTheDocument();

      // Mock scrollWidth to simulate scrollable content
      Object.defineProperty(scrollContainer, 'scrollWidth', { value: 1000 });
      Object.defineProperty(scrollContainer, 'clientWidth', { value: 500 });
      Object.defineProperty(scrollContainer, 'scrollLeft', { value: 100 });

      // Simulate scroll
      fireEvent.scroll(scrollContainer!);

      // Wait for requestAnimationFrame
      await waitFor(() => {
        expect(global.requestAnimationFrame).toHaveBeenCalled();
      });
    });

    it('handles resize events', async () => {
      renderWithTheme(<KanbanBoardDndKit />);

      // Wait for component to mount
      await waitFor(() => {
        const loggerChild = vi.mocked(logger.child).mock.results[0]?.value;
        expect(loggerChild?.debug).toHaveBeenCalledWith('Component mounted');
      });

      // Trigger resize
      fireEvent(window, new Event('resize'));

      // Wait for resize handler
      await waitFor(() => {
        const loggerChild = vi.mocked(logger.child).mock.results[0]?.value;
        expect(loggerChild?.debug).toHaveBeenCalledWith(
          'Window resized, reinitializing scroll indicator'
        );
      });
    });
  });

  describe.skip('Error Handling', () => {
    it('handles scroll errors gracefully', () => {
      renderWithTheme(<KanbanBoardDndKit />);

      const scrollContainer = document.getElementById('kanbanScrollContainer');

      // Remove scroll indicator to cause error
      const indicator = document.getElementById('scrollIndicator');
      indicator?.remove();

      // Scroll should not throw
      expect(() => {
        fireEvent.scroll(scrollContainer!, { target: { scrollLeft: 100 } });
      }).not.toThrow();
    });
  });

  describe.skip('Performance', () => {
    it('uses memoization for expensive calculations', () => {
      const { rerender } = renderWithTheme(<KanbanBoardDndKit />);

      // Initial render
      expect(screen.getByText('3 Aktive')).toBeInTheDocument();

      // Rerender with same props
      rerender(
        <ThemeProvider theme={freshfoodzTheme}>
          <KanbanBoardDndKit />
        </ThemeProvider>
      );

      // Should still show same values (memoized)
      expect(screen.getByText('3 Aktive')).toBeInTheDocument();
    });

    it('logs performance metrics', async () => {
      renderWithTheme(<KanbanBoardDndKit />);

      // Wait for component to mount and logger to be initialized
      await waitFor(() => {
        const loggerChild = vi.mocked(logger.child).mock.results[0]?.value;
        expect(loggerChild).toBeDefined();
        expect(loggerChild?.time).toBeDefined();
        expect(loggerChild?.debug).toHaveBeenCalledWith('Component mounted');
      });
    });
  });
});
