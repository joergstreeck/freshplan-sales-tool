import React from 'react';
import { render, screen } from '@testing-library/react';
import { ThemeProvider } from '@mui/material/styles';
import { createTheme } from '@mui/material/styles';
import ThreadList from '../../components/communication/ThreadList';

/**
 * Hybrid Test: KI-Test-Framework + echte Business-Logic aus alter Planung
 * Basiert auf der echten ThreadList.tsx Component-Logic
 */

const theme = createTheme();

// Mock the useCommunication hook
const mockUseThreads = jest.fn();
jest.mock('../../hooks/useCommunication', () => ({
  useThreads: () => mockUseThreads()
}));

const renderWithTheme = (component: React.ReactElement) => {
  return render(
    <ThemeProvider theme={theme}>
      {component}
    </ThemeProvider>
  );
};

describe('ThreadList Component', () => {
  test('renders loading state correctly', () => {
    // KI-Framework + echte Loading-Logic aus ThreadList.tsx
    mockUseThreads.mockReturnValue({
      data: null,
      loading: true,
      error: null
    });

    renderWithTheme(<ThreadList />);
    expect(screen.getByText('Lade Threads…')).toBeInTheDocument();
  });

  test('renders error state correctly', () => {
    // Echte Error-Handling-Logic aus ThreadList.tsx
    mockUseThreads.mockReturnValue({
      data: null,
      loading: false,
      error: 'Network error'
    });

    renderWithTheme(<ThreadList />);
    expect(screen.getByText(/Fehler: Network error/)).toBeInTheDocument();
  });

  test('renders thread list with business data correctly', () => {
    // Echte Business-Logic: Channel-Display, Unread-Count, Territory-Filtering
    const mockThreads = {
      items: [
        {
          id: '123',
          subject: 'Sample Follow-up T+3',
          channel: 'EMAIL',
          unreadCount: 2,
          territory: 'BER',
          lastMessageAt: '2025-09-19T10:00:00Z'
        },
        {
          id: '456',
          subject: 'Phone Call Meeting',
          channel: 'PHONE',
          unreadCount: 0,
          territory: 'MUNICH',
          lastMessageAt: '2025-09-19T09:00:00Z'
        }
      ]
    };

    mockUseThreads.mockReturnValue({
      data: mockThreads,
      loading: false,
      error: null
    });

    renderWithTheme(<ThreadList />);

    // Business-Logic-Tests basierend auf echter ThreadList.tsx
    expect(screen.getByText('Sample Follow-up T+3')).toBeInTheDocument();
    expect(screen.getByText('Phone Call Meeting')).toBeInTheDocument();

    // Channel-Display-Logic
    const emailChip = screen.getByText('EMAIL');
    const phoneChip = screen.getByText('PHONE');
    expect(emailChip).toBeInTheDocument();
    expect(phoneChip).toBeInTheDocument();

    // Unread-Count-Logic (nur wenn > 0)
    expect(screen.getByText('2 neu')).toBeInTheDocument();
    expect(screen.queryByText('0 neu')).not.toBeInTheDocument();

    // Territory-Display-Logic
    expect(screen.getByText(/BER/)).toBeInTheDocument();
    expect(screen.getByText(/MUNICH/)).toBeInTheDocument();
  });

  test('renders empty state when no threads', () => {
    // Edge-Case: Leere Thread-Liste
    mockUseThreads.mockReturnValue({
      data: { items: [] },
      loading: false,
      error: null
    });

    renderWithTheme(<ThreadList />);

    // ThreadList.tsx rendert leere Box, keine explizite "Keine Threads" Nachricht
    const container = screen.getByRole('generic'); // Box element
    expect(container).toBeInTheDocument();
    expect(container.children).toHaveLength(0);
  });

  test('handles channel colors correctly', () => {
    // Business-Logic: EMAIL = primary, andere = secondary
    const mockThreads = {
      items: [
        {
          id: '1',
          subject: 'Email Thread',
          channel: 'EMAIL',
          unreadCount: 0,
          territory: 'BER',
          lastMessageAt: '2025-09-19T10:00:00Z'
        },
        {
          id: '2',
          subject: 'Phone Thread',
          channel: 'PHONE',
          unreadCount: 0,
          territory: 'BER',
          lastMessageAt: '2025-09-19T10:00:00Z'
        }
      ]
    };

    mockUseThreads.mockReturnValue({
      data: mockThreads,
      loading: false,
      error: null
    });

    renderWithTheme(<ThreadList />);

    // Echte Channel-Color-Logic aus ThreadList.tsx
    const emailChip = screen.getByText('EMAIL').closest('.MuiChip-root');
    const phoneChip = screen.getByText('PHONE').closest('.MuiChip-root');

    // EMAIL sollte primary color haben, PHONE secondary
    expect(emailChip).toHaveClass('MuiChip-colorPrimary');
    expect(phoneChip).toHaveClass('MuiChip-colorSecondary');
  });

  test('filters by customerId when provided', () => {
    // Business-Logic: customerId-Filtering
    mockUseThreads.mockReturnValue({
      data: { items: [] },
      loading: false,
      error: null
    });

    const customerId = 'customer-123';
    renderWithTheme(<ThreadList customerId={customerId} />);

    // Verify that useThreads was called with customerId
    expect(mockUseThreads).toHaveBeenCalledWith({ customerId });
  });
});