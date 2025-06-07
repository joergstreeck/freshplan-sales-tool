import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import App from './App';
import { ApiService } from './services/api';
import { useAuth } from './contexts/AuthContext';

// Mock dependencies
vi.mock('./contexts/AuthContext');
vi.mock('./services/api');

const mockUseAuth = useAuth as ReturnType<typeof vi.fn>;
const mockApiService = ApiService as jest.Mocked<typeof ApiService>;

const renderWithRouter = (component: React.ReactNode) => {
  return render(<BrowserRouter>{component}</BrowserRouter>);
};

describe('App', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockUseAuth.mockReturnValue({
      user: null,
      token: null,
      login: vi.fn(),
      logout: vi.fn(),
    });
  });

  it('renders the main heading', () => {
    renderWithRouter(<App />);

    expect(screen.getByText('FreshPlan 2.0')).toBeInTheDocument();
    expect(screen.getByText('Sprint 0 - Walking Skeleton')).toBeInTheDocument();
  });

  it('renders all three cards', () => {
    renderWithRouter(<App />);

    expect(screen.getByText('Counter Test')).toBeInTheDocument();
    expect(screen.getByText('API Test')).toBeInTheDocument();
    expect(screen.getByText('User Management')).toBeInTheDocument();
  });

  it('increments counter when button is clicked', () => {
    renderWithRouter(<App />);

    const button = screen.getByRole('button', { name: /count is 0/i });
    fireEvent.click(button);

    expect(screen.getByRole('button', { name: /count is 1/i })).toBeInTheDocument();
  });

  it('shows error when ping is clicked without authentication', async () => {
    renderWithRouter(<App />);

    const pingButton = screen.getByRole('button', { name: /ping api/i });
    fireEvent.click(pingButton);

    await waitFor(() => {
      expect(screen.getByText(/error: not authenticated/i)).toBeInTheDocument();
    });
  });

  it('calls ping API when authenticated', async () => {
    mockUseAuth.mockReturnValue({
      user: { id: '1', name: 'Test User' },
      token: 'test-token',
      login: vi.fn(),
      logout: vi.fn(),
    });

    mockApiService.ping.mockResolvedValue({
      message: 'pong',
      timestamp: '2025-01-07T04:00:00Z',
    });

    renderWithRouter(<App />);

    const pingButton = screen.getByRole('button', { name: /ping api/i });
    fireEvent.click(pingButton);

    await waitFor(() => {
      expect(mockApiService.ping).toHaveBeenCalledWith('test-token');
    });
  });

  it('displays error message when API call fails', async () => {
    mockUseAuth.mockReturnValue({
      user: { id: '1', name: 'Test User' },
      token: 'test-token',
      login: vi.fn(),
      logout: vi.fn(),
    });

    mockApiService.ping.mockRejectedValue(new Error('Network error'));

    renderWithRouter(<App />);

    const pingButton = screen.getByRole('button', { name: /ping api/i });
    fireEvent.click(pingButton);

    await waitFor(() => {
      expect(screen.getByText(/error: network error/i)).toBeInTheDocument();
    });
  });

  it('renders user management link', () => {
    renderWithRouter(<App />);

    const userManagementLink = screen.getByRole('link', { name: /benutzerverwaltung öffnen/i });
    expect(userManagementLink).toBeInTheDocument();
    expect(userManagementLink).toHaveAttribute('href', '/users');
  });
});
