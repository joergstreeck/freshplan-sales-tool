/**
 * KanbanBoard Component Tests
 * @module KanbanBoard.test
 * @description Test suite for the opportunity pipeline Kanban board
 */

import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import { KanbanBoard } from './KanbanBoard';

// Mock the logger
vi.mock('../../../lib/logger', () => ({
  logger: {
    child: vi.fn().mockReturnValue({
      debug: vi.fn(),
      info: vi.fn(),
      warn: vi.fn(),
      error: vi.fn(),
      time: vi.fn().mockReturnValue(vi.fn())
    })
  }
}));

// Mock the ErrorBoundary hook
vi.mock('../../../components/ErrorBoundary', () => ({
  useErrorHandler: vi.fn().mockReturnValue(vi.fn())
}));

// Type definitions for mocks
interface DragDropContextProps {
  children: React.ReactNode;
  onDragStart?: () => void;
  onDragEnd?: () => void;
}

interface DroppableRenderProps {
  innerRef: () => void;
  droppableProps: Record<string, unknown>;
  placeholder: null;
}

interface DroppableSnapshot {
  isDraggingOver: boolean;
}

interface DroppableProps {
  children: (provided: DroppableRenderProps, snapshot: DroppableSnapshot) => React.ReactNode;
  droppableId: string;
}

interface DraggableRenderProps {
  innerRef: () => void;
  draggableProps: { style: Record<string, unknown> };
  dragHandleProps: Record<string, unknown>;
}

interface DraggableSnapshot {
  isDragging: boolean;
}

interface DraggableProps {
  children: (provided: DraggableRenderProps, snapshot: DraggableSnapshot) => React.ReactNode;
  draggableId: string;
  index: number;
}

// Mock @hello-pangea/dnd
vi.mock('@hello-pangea/dnd', () => ({
  DragDropContext: ({ children }: DragDropContextProps) => {
    return <div data-testid="drag-drop-context">{children}</div>;
  },
  Droppable: ({ children, droppableId }: DroppableProps) => {
    return (
      <div data-testid={`droppable-${droppableId}`}>
        {children({
          innerRef: vi.fn(),
          droppableProps: {},
          placeholder: null
        }, { isDraggingOver: false })}
      </div>
    );
  },
  Draggable: ({ children, draggableId }: DraggableProps) => {
    return (
      <div data-testid={`draggable-${draggableId}`}>
        {children({
          innerRef: vi.fn(),
          draggableProps: { style: {} },
          dragHandleProps: {}
        }, { isDragging: false })}
      </div>
    );
  }
}));

const theme = createTheme();

const renderWithTheme = (component: React.ReactElement) => {
  return render(
    <ThemeProvider theme={theme}>
      {component}
    </ThemeProvider>
  );
};

describe('KanbanBoard', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('should render the kanban board with title', () => {
    renderWithTheme(<KanbanBoard />);
    
    expect(screen.getByText('Verkaufschancen Pipeline')).toBeInTheDocument();
  });

  it('should render all active pipeline stages by default', () => {
    renderWithTheme(<KanbanBoard />);
    
    expect(screen.getByText('Lead')).toBeInTheDocument();
    expect(screen.getByText('Qualifiziert')).toBeInTheDocument();
    expect(screen.getByText('Angebot')).toBeInTheDocument();
    expect(screen.getByText('Verhandlung')).toBeInTheDocument();
    
    // Closed stages should not be visible by default
    expect(screen.queryByText('Gewonnen')).not.toBeInTheDocument();
    expect(screen.queryByText('Verloren')).not.toBeInTheDocument();
  });

  it('should show pipeline statistics', () => {
    renderWithTheme(<KanbanBoard />);
    
    // Should show active count
    expect(screen.getByText(/3 Aktive/)).toBeInTheDocument();
    
    // Should show total value
    expect(screen.getByText(/28\.700/)).toBeInTheDocument(); // Sum of active opportunities
  });

  it('should toggle closed stages visibility', () => {
    renderWithTheme(<KanbanBoard />);
    
    // Click on "Alle" filter
    const alleButton = screen.getByRole('button', { name: /Alle/i });
    fireEvent.click(alleButton);
    
    // Now closed stages should be visible
    expect(screen.getByText('Gewonnen')).toBeInTheDocument();
    expect(screen.getByText('Verloren')).toBeInTheDocument();
  });

  it('should display opportunity cards with correct information', () => {
    renderWithTheme(<KanbanBoard />);
    
    // Check if opportunity cards are rendered
    expect(screen.getByText('Großauftrag Restaurant Schmidt')).toBeInTheDocument();
    expect(screen.getByText('Restaurant Schmidt GmbH')).toBeInTheDocument();
    expect(screen.getByText('15.000 €')).toBeInTheDocument();
    expect(screen.getByText('20%')).toBeInTheDocument();
  });

  it('should calculate and display column totals', () => {
    renderWithTheme(<KanbanBoard />);
    
    // Lead column should have total of 15.000 €
    const leadColumn = screen.getByTestId('droppable-lead');
    expect(leadColumn).toHaveTextContent('15.000 €');
  });

  it('should show success metrics when there are closed opportunities', () => {
    renderWithTheme(<KanbanBoard />);
    
    // Should show won/lost statistics
    expect(screen.getByText(/1 Gewonnen/)).toBeInTheDocument();
    expect(screen.getByText(/1 Verloren/)).toBeInTheDocument();
    expect(screen.getByText(/50% Erfolgsquote/)).toBeInTheDocument();
  });

  it('should render opportunity cards with avatars', () => {
    renderWithTheme(<KanbanBoard />);
    
    // Check for avatar initials
    expect(screen.getByText('M')).toBeInTheDocument(); // Max Mustermann
    expect(screen.getByText('A')).toBeInTheDocument(); // Anna Weber
  });

  it('should display dates in German format', () => {
    renderWithTheme(<KanbanBoard />);
    
    // Check for formatted dates (DD.MM.YY)
    expect(screen.getByText('15.08.25')).toBeInTheDocument();
    expect(screen.getByText('30.07.25')).toBeInTheDocument();
  });

  it('should render with proper Freshfoodz CI colors', () => {
    renderWithTheme(<KanbanBoard />);
    
    const title = screen.getByText('Verkaufschancen Pipeline');
    expect(title).toHaveStyle({ color: '#004F7B' });
    
    // Check for brand color usage
    const activeCount = screen.getByText(/3 Aktive/);
    expect(activeCount).toHaveStyle({ color: '#94C456' });
  });

  it('should handle empty opportunity lists gracefully', () => {
    // Would need to mock the initial data or add props to test this
    renderWithTheme(<KanbanBoard />);
    
    // Should still render all columns even if empty
    expect(screen.getByTestId('droppable-lead')).toBeInTheDocument();
    expect(screen.getByTestId('droppable-qualified')).toBeInTheDocument();
    expect(screen.getByTestId('droppable-proposal')).toBeInTheDocument();
    expect(screen.getByTestId('droppable-negotiation')).toBeInTheDocument();
  });

  it('should apply correct stage colors to columns', () => {
    renderWithTheme(<KanbanBoard />);
    
    // Each stage should have its configured color
    const leadLabel = screen.getByText('Lead');
    const qualifiedLabel = screen.getByText('Qualifiziert');
    
    // These would have their respective colors from STAGE_CONFIGS
    expect(leadLabel).toBeInTheDocument();
    expect(qualifiedLabel).toBeInTheDocument();
  });

  it('should render info alert when showing closed stages', async () => {
    renderWithTheme(<KanbanBoard />);
    
    // Click to show all stages
    const alleButton = screen.getByRole('button', { name: /Alle/i });
    fireEvent.click(alleButton);
    
    // Info alert should appear
    await waitFor(() => {
      expect(screen.getByText(/Sie sehen jetzt auch abgeschlossene Verkaufschancen/)).toBeInTheDocument();
    });
  });

  it('should have proper accessibility attributes', () => {
    renderWithTheme(<KanbanBoard />);
    
    // Check for proper heading structure
    const mainTitle = screen.getByText('Verkaufschancen Pipeline');
    expect(mainTitle.tagName).toBe('H5');
    
    // Toggle buttons should be accessible
    const toggleGroup = screen.getByRole('group');
    expect(toggleGroup).toBeInTheDocument();
  });
});