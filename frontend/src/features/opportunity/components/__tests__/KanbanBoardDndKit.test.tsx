import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { KanbanBoardDndKit } from '../kanban/KanbanBoardDndKit';

// Create a test theme with status colors
const testTheme = createTheme({
  palette: {
    status: {
      won: '#4caf50',
      lost: '#f44336',
      inProgress: '#2196f3',
      new: '#ff9800',
    },
  },
});

// Create query client for tests
const queryClient = new QueryClient({
  defaultOptions: {
    queries: { retry: false },
    mutations: { retry: false },
  },
});

// Helper function to render with providers
const renderWithProviders = (component: React.ReactElement) => {
  return render(
    <QueryClientProvider client={queryClient}>
      <ThemeProvider theme={testTheme}>
        {component}
      </ThemeProvider>
    </QueryClientProvider>
  );
};

// Mock the httpClient
vi.mock('../../../../lib/apiClient', () => ({
  httpClient: {
    get: vi.fn().mockResolvedValue({ data: [] }),
    post: vi.fn().mockResolvedValue({ data: {} }),
    put: vi.fn().mockResolvedValue({ data: {} }),
  },
}));

// Mock the logger
vi.mock('../../../../lib/logger', () => ({
  logger: {
    child: () => ({
      debug: vi.fn(),
      info: vi.fn(),
      error: vi.fn(),
      warn: vi.fn(),
    }),
  },
}));

describe('KanbanBoardDndKit', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('should render the pipeline overview', () => {
    renderWithProviders(<KanbanBoardDndKit />);
    
    // Check if pipeline overview is rendered
    expect(screen.getByText('Pipeline Übersicht')).toBeInTheDocument();
  });

  it('should display pipeline statistics', () => {
    renderWithProviders(<KanbanBoardDndKit />);
    
    // Check for statistic sections
    expect(screen.getByText('Aktive Opportunities')).toBeInTheDocument();
    // Use getAllByText since "Gewonnen" appears multiple times
    const gewonnenElements = screen.getAllByText('Gewonnen');
    expect(gewonnenElements.length).toBeGreaterThan(0);
    // Use getAllByText since "Verloren" might appear multiple times
    const verlorenElements = screen.getAllByText('Verloren');
    expect(verlorenElements.length).toBeGreaterThan(0);
    expect(screen.getByText('Conversion Rate')).toBeInTheDocument();
  });

  it('should render kanban columns for opportunities', () => {
    renderWithProviders(<KanbanBoardDndKit />);
    
    // The component should render stage columns
    // These are defined in the component's OpportunityStage enum
    expect(screen.getByText(/Lead/i)).toBeInTheDocument();
  });

  it('should handle loading state', () => {
    renderWithProviders(<KanbanBoardDndKit />);
    
    // Component should handle loading gracefully
    // Even if no opportunities are loaded, the structure should be present
    expect(screen.getByText('Pipeline Übersicht')).toBeInTheDocument();
  });

  it('should display formatted currency values', () => {
    renderWithProviders(<KanbanBoardDndKit />);
    
    // Check that currency is formatted (the component uses formatCurrency)
    // The initial data includes formatted values
    const currencyElements = screen.getAllByText(/€/);
    expect(currencyElements.length).toBeGreaterThan(0);
  });

  it('should have proper theme integration', () => {
    const { container } = renderWithProviders(<KanbanBoardDndKit />);
    
    // Check if MUI Paper component is rendered (component uses Paper)
    const paperElement = container.querySelector('.MuiPaper-root');
    expect(paperElement).toBeInTheDocument();
  });

  it('should render with drag and drop context', () => {
    renderWithProviders(<KanbanBoardDndKit />);
    
    // The component uses DndContext internally
    // We can verify it renders without errors
    expect(screen.getByText('Pipeline Übersicht')).toBeInTheDocument();
  });
});