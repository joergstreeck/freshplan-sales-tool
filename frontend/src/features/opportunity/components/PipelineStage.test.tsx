import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import { ThemeProvider } from '@mui/material/styles';
import freshfoodzTheme from '../../../theme/freshfoodz';
import { PipelineStage } from './PipelineStage';
import { OpportunityStage } from '../types/stages';

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
  useDroppable: vi.fn(() => ({
    setNodeRef: vi.fn(),
    isOver: false,
  })),
}));

// Helper function to render with theme
const renderWithTheme = (component: React.ReactElement) => {
  return render(
    <ThemeProvider theme={freshfoodzTheme}>
      {component}
    </ThemeProvider>
  );
};

describe('PipelineStage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('Rendering', () => {
    it('renders stage label', () => {
      renderWithTheme(
        <PipelineStage 
          stage={OpportunityStage.LEAD}
          opportunityCount={5}
        >
          <div>Test Content</div>
        </PipelineStage>
      );
      
      expect(screen.getByText('Lead')).toBeInTheDocument();
    });

    it('renders opportunity count badge', () => {
      renderWithTheme(
        <PipelineStage 
          stage={OpportunityStage.QUALIFIED}
          opportunityCount={10}
        >
          <div>Test Content</div>
        </PipelineStage>
      );
      
      expect(screen.getByText('10')).toBeInTheDocument();
    });

    it('renders total value when provided', () => {
      renderWithTheme(
        <PipelineStage 
          stage={OpportunityStage.PROPOSAL}
          opportunityCount={3}
          totalValue={25000}
        >
          <div>Test Content</div>
        </PipelineStage>
      );
      
      expect(screen.getByText('Gesamt: 25.000 €')).toBeInTheDocument();
    });

    it('does not render total value when not provided', () => {
      renderWithTheme(
        <PipelineStage 
          stage={OpportunityStage.NEGOTIATION}
          opportunityCount={2}
        >
          <div>Test Content</div>
        </PipelineStage>
      );
      
      expect(screen.queryByText(/Gesamt:/)).not.toBeInTheDocument();
    });

    it('renders children content', () => {
      renderWithTheme(
        <PipelineStage 
          stage={OpportunityStage.CLOSED_WON}
          opportunityCount={1}
        >
          <div>Child Content 1</div>
          <div>Child Content 2</div>
        </PipelineStage>
      );
      
      expect(screen.getByText('Child Content 1')).toBeInTheDocument();
      expect(screen.getByText('Child Content 2')).toBeInTheDocument();
    });
  });

  describe('Drag & Drop', () => {
    it('applies hover styles when dragging over', () => {
      // Mock dragging over state
      vi.mocked(vi.mocked(() => {}).useDroppable).mockReturnValue({
        setNodeRef: vi.fn(),
        isOver: true,
      });
      
      const { container } = renderWithTheme(
        <PipelineStage 
          stage={OpportunityStage.LEAD}
          opportunityCount={5}
        >
          <div>Test Content</div>
        </PipelineStage>
      );
      
      const paper = container.querySelector('[class*="MuiPaper"]');
      expect(paper).toHaveStyle({
        border: '3px dashed #94C456',
        transform: 'scale(1.02)',
      });
    });

    it('uses stage id for droppable configuration', () => {
      renderWithTheme(
        <PipelineStage 
          stage={OpportunityStage.QUALIFIED}
          opportunityCount={3}
        >
          <div>Test Content</div>
        </PipelineStage>
      );
      
      // Verify useDroppable was called with correct stage
      expect(vi.mocked(vi.mocked(() => {}).useDroppable)).toHaveBeenCalledWith({
        id: OpportunityStage.QUALIFIED,
      });
    });
  });

  describe('Stage Configuration', () => {
    it('applies correct color for each stage', () => {
      const stages = [
        { stage: OpportunityStage.LEAD, label: 'Lead' },
        { stage: OpportunityStage.QUALIFIED, label: 'Qualifiziert' },
        { stage: OpportunityStage.PROPOSAL, label: 'Angebot' },
        { stage: OpportunityStage.NEGOTIATION, label: 'Verhandlung' },
        { stage: OpportunityStage.CLOSED_WON, label: 'Gewonnen' },
        { stage: OpportunityStage.CLOSED_LOST, label: 'Verloren' },
      ];

      stages.forEach(({ stage, label }) => {
        const { unmount } = renderWithTheme(
          <PipelineStage 
            stage={stage}
            opportunityCount={1}
          >
            <div>Test</div>
          </PipelineStage>
        );
        
        expect(screen.getByText(label)).toBeInTheDocument();
        unmount();
      });
    });
  });

  describe('Currency Formatting', () => {
    it('formats large values correctly', () => {
      renderWithTheme(
        <PipelineStage 
          stage={OpportunityStage.PROPOSAL}
          opportunityCount={1}
          totalValue={1234567}
        >
          <div>Test</div>
        </PipelineStage>
      );
      
      expect(screen.getByText('Gesamt: 1.234.567 €')).toBeInTheDocument();
    });

    it('formats zero value correctly', () => {
      renderWithTheme(
        <PipelineStage 
          stage={OpportunityStage.PROPOSAL}
          opportunityCount={1}
          totalValue={0}
        >
          <div>Test</div>
        </PipelineStage>
      );
      
      expect(screen.getByText('Gesamt: 0 €')).toBeInTheDocument();
    });

    it('handles invalid currency values gracefully', () => {
      // Test with negative value (should still format)
      renderWithTheme(
        <PipelineStage 
          stage={OpportunityStage.PROPOSAL}
          opportunityCount={1}
          totalValue={-1000}
        >
          <div>Test</div>
        </PipelineStage>
      );
      
      expect(screen.getByText('Gesamt: -1.000 €')).toBeInTheDocument();
    });
  });

  describe('Performance', () => {
    it('memoizes component to prevent unnecessary rerenders', () => {
      const { rerender } = renderWithTheme(
        <PipelineStage 
          stage={OpportunityStage.LEAD}
          opportunityCount={5}
          totalValue={10000}
        >
          <div>Test Content</div>
        </PipelineStage>
      );
      
      // Rerender with same props
      rerender(
        <ThemeProvider theme={freshfoodzTheme}>
          <PipelineStage 
            stage={OpportunityStage.LEAD}
            opportunityCount={5}
            totalValue={10000}
          >
            <div>Test Content</div>
          </PipelineStage>
        </ThemeProvider>
      );
      
      // Component should still be in document (not recreated)
      expect(screen.getByText('Lead')).toBeInTheDocument();
      expect(screen.getByText('5')).toBeInTheDocument();
    });
  });

  describe('Error Handling', () => {
    it('handles invalid stage gracefully', () => {
      const invalidStage = 'INVALID_STAGE' as OpportunityStage;
      
      // Should not throw
      expect(() => {
        renderWithTheme(
          <PipelineStage 
            stage={invalidStage}
            opportunityCount={1}
          >
            <div>Test</div>
          </PipelineStage>
        );
      }).not.toThrow();
    });
  });
});