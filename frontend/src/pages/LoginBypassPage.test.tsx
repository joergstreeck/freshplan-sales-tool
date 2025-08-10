import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { LoginBypassPage } from './LoginBypassPage';
import { useAuth } from '../contexts/AuthContext';

// Mock dependencies
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

vi.mock('../contexts/AuthContext');

const mockNavigate = vi.fn();
const mockLogin = vi.fn();
const mockUseAuth = useAuth as ReturnType<typeof vi.fn>;

// Mock localStorage
const localStorageMock = {
  getItem: vi.fn(),
  setItem: vi.fn(),
  removeItem: vi.fn(),
  clear: vi.fn(),
};
Object.defineProperty(window, 'localStorage', { value: localStorageMock });

// Mock window.location
delete (window as Record<string, unknown>).location;
window.location = { href: '' } as Location;

describe('LoginBypassPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockUseAuth.mockReturnValue({
      login: mockLogin,
      logout: vi.fn(),
      user: null,
      token: null,
    });
    window.location.href = '';
  });

  it('renders development login bypass UI', () => {
    render(
      <BrowserRouter>
        <LoginBypassPage />
      </BrowserRouter>
    );

    expect(screen.getByText('Development Login Bypass')).toBeInTheDocument();
    expect(
      screen.getByText(
        'Choose a role to login with mock credentials. This is only available in development mode.'
      )
    ).toBeInTheDocument();
  });

  it('displays all role buttons', () => {
    render(
      <BrowserRouter>
        <LoginBypassPage />
      </BrowserRouter>
    );

    expect(screen.getByRole('button', { name: /login as admin/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /login as manager/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /login as sales/i })).toBeInTheDocument();
  });

  it('displays role descriptions', () => {
    render(
      <BrowserRouter>
        <LoginBypassPage />
      </BrowserRouter>
    );

    // Check for role descriptions in the list - German text
    expect(
      screen.getByText(/Vollzugriff auf alle Funktionen, User-Verwaltung/)
    ).toBeInTheDocument();
    expect(screen.getByText(/Geschäftsleitung, alle Berichte, Credit Checks/)).toBeInTheDocument();
    expect(screen.getByText(/Verkäufer, Kunden anlegen, Kalkulationen/)).toBeInTheDocument();
  });

  it('sets localStorage and redirects when admin button is clicked', () => {
    render(
      <BrowserRouter>
        <LoginBypassPage />
      </BrowserRouter>
    );

    const adminButton = screen.getByRole('button', { name: /login as admin/i });
    fireEvent.click(adminButton);

    expect(localStorageMock.setItem).toHaveBeenCalledWith(
      'auth-token',
      expect.stringContaining('eyJhbGciOiJSUzI1NiI')
    );
    expect(localStorageMock.setItem).toHaveBeenCalledWith(
      'auth-user',
      expect.stringContaining('admin@freshplan.de')
    );
    expect(window.location.href).toBe('/');
  });

  it('navigates back to home when back button is clicked', () => {
    render(
      <BrowserRouter>
        <LoginBypassPage />
      </BrowserRouter>
    );

    const backButton = screen.getByRole('button', { name: /back to home/i });
    fireEvent.click(backButton);

    expect(mockNavigate).toHaveBeenCalledWith('/');
  });
});
