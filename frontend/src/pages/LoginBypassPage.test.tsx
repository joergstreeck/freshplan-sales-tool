import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent } from '../test/test-utils';
import { LoginBypassPage } from './LoginBypassPage';

// Mock dependencies
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

// Mock keycloak to prevent auth initialization
vi.mock('../lib/keycloak');

const mockNavigate = vi.fn();

// Mock localStorage
const localStorageMock = {
  getItem: vi.fn(),
  setItem: vi.fn(),
  removeItem: vi.fn(),
  clear: vi.fn(),
};
Object.defineProperty(window, 'localStorage', { value: localStorageMock, writable: true });

// Mock window.location
delete (window as Record<string, unknown>).location;
window.location = { href: '' } as Location;

describe('LoginBypassPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    // Disable auth bypass to prevent test-utils providers from interfering
    vi.stubEnv('VITE_AUTH_BYPASS', 'false');
    vi.stubEnv('DEV', 'false');
    window.location.href = '';
    localStorageMock.getItem.mockReturnValue(null);
  });

  afterEach(() => {
    vi.unstubAllEnvs();
  });

  it('renders development login bypass UI', () => {
    render(<LoginBypassPage />);

    expect(screen.getByText('Development Login Bypass')).toBeInTheDocument();
    expect(
      screen.getByText(
        'Choose a role to login with mock credentials. This is only available in development mode.'
      )
    ).toBeInTheDocument();
  });

  it('displays all role buttons', () => {
    render(<LoginBypassPage />);

    expect(screen.getByRole('button', { name: /login as admin/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /login as manager/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /login as sales/i })).toBeInTheDocument();
  });

  it('displays role descriptions', () => {
    render(<LoginBypassPage />);

    // Check for role descriptions in the list - German text
    expect(
      screen.getByText(/Vollzugriff auf alle Funktionen, User-Verwaltung/)
    ).toBeInTheDocument();
    expect(screen.getByText(/Geschäftsleitung, alle Berichte, Credit Checks/)).toBeInTheDocument();
    expect(screen.getByText(/Verkäufer, Kunden anlegen, Kalkulationen/)).toBeInTheDocument();
  });

  it('sets localStorage and redirects when admin button is clicked', () => {
    render(<LoginBypassPage />);

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
    expect(window.location.href).toBe('/cockpit');
  });

  it('navigates back to home when back button is clicked', () => {
    render(<LoginBypassPage />);

    const backButton = screen.getByRole('button', { name: /back to home/i });
    fireEvent.click(backButton);

    expect(mockNavigate).toHaveBeenCalledWith('/');
  });
});
