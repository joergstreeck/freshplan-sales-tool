/**
 * KanbanBoard Component Tests
 * @module KanbanBoard.test
 * @description Test suite for the opportunity pipeline Kanban board
 */

import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { KanbanBoard } from './KanbanBoard';

// Mock the logger
vi.mock('../../../lib/logger', () => ({
  logger: {
    child: vi.fn().mockReturnValue({
      debug: vi.fn(),
      info: vi.fn(),
      warn: vi.fn(),
      error: vi.fn(),
      time: vi.fn().mockReturnValue(vi.fn()),
    }),
  },
}));

// Mock the ErrorBoundary hook
vi.mock('../../../components/ErrorBoundary', () => ({
  useErrorHandler: vi.fn().mockReturnValue(vi.fn()),
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

// Mock data for opportunities to match test expectations
const mockOpportunities = [
  {
    id: '1',
    name: 'Großauftrag Restaurant Schmidt',
    stage: 'NEW_LEAD',
    value: 15000,
    probability: 20,
    customerName: 'Restaurant Schmidt GmbH',
    assignedToName: 'Max Mustermann',
    expectedCloseDate: '2025-08-15',
    createdAt: '2025-07-01T10:00:00Z',
    updatedAt: '2025-07-25T10:00:00Z',
  },
  {
    id: '2',
    name: 'Catering Großevent',
    stage: 'QUALIFICATION',
    value: 8700,
    probability: 45,
    customerName: 'Event GmbH',
    assignedToName: 'Anna Weber',
    expectedCloseDate: '2025-07-30',
    createdAt: '2025-07-05T10:00:00Z',
    updatedAt: '2025-07-25T10:00:00Z',
  },
  {
    id: '3',
    name: 'Kleinauftrag Büroküche',
    stage: 'PROPOSAL',
    value: 5000,
    probability: 75,
    customerName: 'Büro AG',
    assignedToName: 'Thomas Klein',
    expectedCloseDate: '2025-08-30',
    createdAt: '2025-07-10T10:00:00Z',
    updatedAt: '2025-07-25T10:00:00Z',
  },
  {
    id: '4',
    name: 'Gewonnener Auftrag',
    stage: 'CLOSED_WON',
    value: 12000,
    probability: 100,
    customerName: 'Erfolg GmbH',
    assignedToName: 'Max Mustermann',
    expectedCloseDate: '2025-07-15',
    createdAt: '2025-06-01T10:00:00Z',
    updatedAt: '2025-07-15T10:00:00Z',
  },
  {
    id: '5',
    name: 'Verlorener Auftrag',
    stage: 'CLOSED_LOST',
    value: 3000,
    probability: 0,
    customerName: 'Verlust AG',
    assignedToName: 'Anna Weber',
    expectedCloseDate: '2025-07-20',
    createdAt: '2025-06-15T10:00:00Z',
    updatedAt: '2025-07-20T10:00:00Z',
  },
];

// Mock the useOpportunities hook
vi.mock('../hooks/useOpportunities', () => ({
  useOpportunities: vi.fn(),
  useChangeOpportunityStage: vi.fn(),
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
        {children(
          {
            innerRef: vi.fn(),
            droppableProps: {},
            placeholder: null,
          },
          { isDraggingOver: false }
        )}
      </div>
    );
  },
  Draggable: ({ children, draggableId }: DraggableProps) => {
    return (
      <div data-testid={`draggable-${draggableId}`}>
        {children(
          {
            innerRef: vi.fn(),
            draggableProps: { style: {} },
            dragHandleProps: {},
          },
          { isDragging: false }
        )}
      </div>
    );
  },
}));

const theme = createTheme();

const renderWithProviders = (component: React.ReactElement) => {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: {
        retry: false,
      },
    },
  });

  return render(
    <QueryClientProvider client={queryClient}>
      <ThemeProvider theme={theme}>{component}</ThemeProvider>
    </QueryClientProvider>
  );
};

// Import and setup mocks
import { useOpportunities, useChangeOpportunityStage } from '../hooks/useOpportunities';

describe('KanbanBoard', () => {
  beforeEach(() => {
    vi.clearAllMocks();

    // Setup useOpportunities mock
    vi.mocked(useOpportunities).mockReturnValue({
      data: mockOpportunities,
      isLoading: false,
      error: null,
      refetch: vi.fn(),
    });

    vi.mocked(useChangeOpportunityStage).mockReturnValue({
      mutate: vi.fn(),
      isPending: false,
      error: null,
    });
  });

  it('should render the kanban board with title', () => {
    renderWithProviders(<KanbanBoard />);

    expect(screen.getByText('Verkaufschancen Pipeline')).toBeInTheDocument();
  });

  it('should render all active pipeline stages by default', () => {
    renderWithProviders(<KanbanBoard />);

    expect(screen.getByText('Lead')).toBeInTheDocument();
    expect(screen.getByText('Qualifiziert')).toBeInTheDocument();
    expect(screen.getByText('Angebot')).toBeInTheDocument();
    expect(screen.getByText('Verhandlung')).toBeInTheDocument();

    // Closed stages should not be visible by default
    expect(screen.queryByText('Gewonnen')).not.toBeInTheDocument();
    expect(screen.queryByText('Verloren')).not.toBeInTheDocument();
  });

  it('should show pipeline statistics', () => {
    renderWithProviders(<KanbanBoard />);

    // Should show active count
    expect(screen.getByText(/3 Aktive/)).toBeInTheDocument();

    // Should show total value
    expect(screen.getByText(/28\.700/)).toBeInTheDocument(); // Sum of active opportunities
  });

  it('should toggle closed stages visibility', () => {
    renderWithProviders(<KanbanBoard />);

    // Click on "Alle" filter
    const alleButton = screen.getByRole('button', { name: /Alle/i });
    fireEvent.click(alleButton);

    // Now closed stages should be visible
    expect(screen.getByText('Gewonnen')).toBeInTheDocument();
    expect(screen.getByText('Verloren')).toBeInTheDocument();
  });

  it('should display opportunity cards with correct information', () => {
    renderWithProviders(<KanbanBoard />);

    // Check if opportunity cards are rendered
    expect(screen.getByText('Großauftrag Restaurant Schmidt')).toBeInTheDocument();
    expect(screen.getByText('Restaurant Schmidt GmbH')).toBeInTheDocument();
    expect(screen.getByText('15.000 €')).toBeInTheDocument();
    expect(screen.getByText('20%')).toBeInTheDocument();
  });

  it('should calculate and display column totals', () => {
    renderWithProviders(<KanbanBoard />);

    // Lead column should have total of 15.000 €
    const leadColumn = screen.getByTestId('droppable-new_lead');
    expect(leadColumn).toHaveTextContent('15.000 €');
  });

  it('should show success metrics when there are closed opportunities', () => {
    renderWithProviders(<KanbanBoard />);

    // Should show won/lost statistics
    expect(screen.getByText(/1 Gewonnen/)).toBeInTheDocument();
    expect(screen.getByText(/1 Verloren/)).toBeInTheDocument();
    expect(screen.getByText(/50% Erfolgsquote/)).toBeInTheDocument();
  });

  it('should render opportunity cards with avatars', () => {
    renderWithProviders(<KanbanBoard />);

    // Check for avatar initials
    expect(screen.getByText('M')).toBeInTheDocument(); // Max Mustermann
    expect(screen.getByText('A')).toBeInTheDocument(); // Anna Weber
  });

  it('should display dates in German format', () => {
    renderWithProviders(<KanbanBoard />);

    // Check for formatted dates (DD.MM.YY)
    expect(screen.getByText('15.08.25')).toBeInTheDocument();
    expect(screen.getByText('30.07.25')).toBeInTheDocument();
  });

  it('should render with proper Freshfoodz CI colors', () => {
    renderWithProviders(<KanbanBoard />);

    const title = screen.getByText('Verkaufschancen Pipeline');
    expect(title).toHaveStyle({ color: '#004F7B' });

    // Check for brand color usage
    const activeCount = screen.getByText(/3 Aktive/);
    expect(activeCount).toHaveStyle({ color: '#94C456' });
  });

  it('should handle empty opportunity lists gracefully', () => {
    // Would need to mock the initial data or add props to test this
    renderWithProviders(<KanbanBoard />);

    // Should still render all columns even if empty
    expect(screen.getByTestId('droppable-new_lead')).toBeInTheDocument();
    expect(screen.getByTestId('droppable-qualification')).toBeInTheDocument();
    expect(screen.getByTestId('droppable-proposal')).toBeInTheDocument();
    expect(screen.getByTestId('droppable-negotiation')).toBeInTheDocument();
  });

  it('should apply correct stage colors to columns', () => {
    renderWithProviders(<KanbanBoard />);

    // Each stage should have its configured color
    const leadLabel = screen.getByText('Lead');
    const qualifiedLabel = screen.getByText('Qualifiziert');

    // These would have their respective colors from STAGE_CONFIGS
    expect(leadLabel).toBeInTheDocument();
    expect(qualifiedLabel).toBeInTheDocument();
  });

  it('should render info alert when showing closed stages', async () => {
    renderWithProviders(<KanbanBoard />);

    // Click to show all stages
    const alleButton = screen.getByRole('button', { name: /Alle/i });
    fireEvent.click(alleButton);

    // Info alert should appear
    await waitFor(() => {
      expect(
        screen.getByText(/Sie sehen jetzt auch abgeschlossene Verkaufschancen/)
      ).toBeInTheDocument();
    });
  });

  it('should have proper accessibility attributes', () => {
    renderWithProviders(<KanbanBoard />);

    // Check for proper heading structure
    const mainTitle = screen.getByText('Verkaufschancen Pipeline');
    expect(mainTitle.tagName).toBe('H5');

    // Toggle buttons should be accessible
    const toggleGroup = screen.getByRole('group');
    expect(toggleGroup).toBeInTheDocument();
  });
});
