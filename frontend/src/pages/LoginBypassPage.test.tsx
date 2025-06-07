import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
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

describe('LoginBypassPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockUseAuth.mockReturnValue({
      login: mockLogin,
      logout: vi.fn(),
      user: null,
      token: null,
    });
  });

  it('renders loading message', () => {
    render(
      <BrowserRouter>
        <LoginBypassPage />
      </BrowserRouter>
    );

    expect(screen.getByText('Test Login...')).toBeInTheDocument();
    expect(screen.getByText('Logging in as e2e@test.de')).toBeInTheDocument();
  });

  it('does not call login when credentials are not configured', () => {
    // Mock environment without credentials
    const originalEmail = import.meta.env.VITE_TEST_USER_EMAIL;
    const originalPassword = import.meta.env.VITE_TEST_USER_PASSWORD;

    delete import.meta.env.VITE_TEST_USER_EMAIL;
    delete import.meta.env.VITE_TEST_USER_PASSWORD;

    render(
      <BrowserRouter>
        <LoginBypassPage />
      </BrowserRouter>
    );

    expect(mockLogin).not.toHaveBeenCalled();

    // Restore environment
    import.meta.env.VITE_TEST_USER_EMAIL = originalEmail;
    import.meta.env.VITE_TEST_USER_PASSWORD = originalPassword;
  });

  it('calls login with test credentials when configured', async () => {
    // Mock environment with credentials
    import.meta.env.VITE_TEST_USER_EMAIL = 'test@example.com';
    import.meta.env.VITE_TEST_USER_PASSWORD = 'test-password';

    render(
      <BrowserRouter>
        <LoginBypassPage />
      </BrowserRouter>
    );

    expect(mockLogin).toHaveBeenCalledWith('test@example.com', 'test-password');
  });

  it('navigates to home page after login', async () => {
    import.meta.env.VITE_TEST_USER_EMAIL = 'test@example.com';
    import.meta.env.VITE_TEST_USER_PASSWORD = 'test-password';

    render(
      <BrowserRouter>
        <LoginBypassPage />
      </BrowserRouter>
    );

    await waitFor(
      () => {
        expect(mockNavigate).toHaveBeenCalledWith('/');
      },
      { timeout: 200 }
    );
  });

  it('logs error in development when credentials missing', () => {
    const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {});

    // Remove credentials for this test
    delete import.meta.env.VITE_TEST_USER_EMAIL;
    delete import.meta.env.VITE_TEST_USER_PASSWORD;

    render(
      <BrowserRouter>
        <LoginBypassPage />
      </BrowserRouter>
    );

    expect(consoleSpy).toHaveBeenCalledWith('Test credentials not configured');

    consoleSpy.mockRestore();

    // Restore credentials
    import.meta.env.VITE_TEST_USER_EMAIL = 'test@example.com';
    import.meta.env.VITE_TEST_USER_PASSWORD = 'test-password';
  });
});
