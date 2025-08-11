import { describe, it, expect, vi } from 'vitest';
import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { UserForm } from '../components/UserForm';
import * as userQueries from '../api/userQueries';

// Mock the user queries
vi.mock('../api/userQueries', () => ({
  useCreateUser: vi.fn(),
  useUpdateUser: vi.fn(),
}));

const mockUseCreateUser = userQueries.useCreateUser as ReturnType<typeof vi.fn>;
const mockUseUpdateUser = userQueries.useUpdateUser as ReturnType<typeof vi.fn>;

const createTestQueryClient = () =>
  new QueryClient({
    defaultOptions: {
      queries: { retry: false },
      mutations: { retry: false },
    },
  });

const renderWithProviders = (component: React.ReactNode) => {
  const queryClient = createTestQueryClient();
  return render(<QueryClientProvider client={queryClient}>{component}</QueryClientProvider>);
};

describe('UserForm', () => {
  const mockMutateAsync = vi.fn();
  const mockOnSuccess = vi.fn();
  const mockOnCancel = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();

    mockUseCreateUser.mockReturnValue({
      mutateAsync: mockMutateAsync,
      isPending: false,
    });

    mockUseUpdateUser.mockReturnValue({
      mutateAsync: mockMutateAsync,
      isPending: false,
    });
  });

  it('renders create form with empty fields', () => {
    renderWithProviders(<UserForm onSuccess={mockOnSuccess} onCancel={mockOnCancel} />);

    expect(screen.getByText('Neuen Benutzer erstellen')).toBeInTheDocument();
    expect(screen.getByLabelText(/benutzername/i)).toHaveValue('');
    expect(screen.getByLabelText(/e-mail/i)).toHaveValue('');
    expect(screen.getByLabelText(/vorname/i)).toHaveValue('');
    expect(screen.getByLabelText(/nachname/i)).toHaveValue('');
  });

  it('renders edit form with user data', () => {
    const testUser = {
      id: '123',
      username: 'john.doe',
      email: 'john@example.com',
      firstName: 'John',
      lastName: 'Doe',
      roles: ['admin' as const],
      enabled: true,
    };

    renderWithProviders(
      <UserForm user={testUser} onSuccess={mockOnSuccess} onCancel={mockOnCancel} />
    );

    expect(screen.getByText('Benutzer bearbeiten')).toBeInTheDocument();
    expect(screen.getByLabelText(/benutzername/i)).toHaveValue('john.doe');
    expect(screen.getByLabelText(/e-mail/i)).toHaveValue('john@example.com');
    expect(screen.getByLabelText(/vorname/i)).toHaveValue('John');
    expect(screen.getByLabelText(/nachname/i)).toHaveValue('Doe');
  });

  it('validates required fields', async () => {
    renderWithProviders(<UserForm onSuccess={mockOnSuccess} onCancel={mockOnCancel} />);

    const submitButton = screen.getByRole('button', { name: /erstellen/i });

    // Form should be invalid initially with empty required fields
    expect(submitButton).toBeDisabled();

    // Fill in required fields
    fireEvent.change(screen.getByLabelText(/benutzername/i), {
      target: { value: 'testuser' },
    });
    fireEvent.change(screen.getByLabelText(/e-mail/i), {
      target: { value: 'test@example.com' },
    });
    fireEvent.change(screen.getByLabelText(/vorname/i), {
      target: { value: 'Test' },
    });
    fireEvent.change(screen.getByLabelText(/nachname/i), {
      target: { value: 'User' },
    });

    await waitFor(() => {
      expect(submitButton).not.toBeDisabled();
    });
  });

  it('handles role selection', async () => {
    renderWithProviders(<UserForm onSuccess={mockOnSuccess} onCancel={mockOnCancel} />);

    // Check admin role
    const adminCheckbox = screen.getByRole('checkbox', { name: /admin/i });
    fireEvent.click(adminCheckbox);

    expect(adminCheckbox).toBeChecked();
  });

  it('calls onCancel when cancel button is clicked', () => {
    renderWithProviders(<UserForm onSuccess={mockOnSuccess} onCancel={mockOnCancel} />);

    const cancelButton = screen.getByRole('button', { name: /abbrechen/i });
    fireEvent.click(cancelButton);

    expect(mockOnCancel).toHaveBeenCalledTimes(1);
  });

  it('calls create mutation on form submission', async () => {
    mockMutateAsync.mockResolvedValueOnce({});

    renderWithProviders(<UserForm onSuccess={mockOnSuccess} onCancel={mockOnCancel} />);

    // Fill in form
    fireEvent.change(screen.getByLabelText(/benutzername/i), {
      target: { value: 'testuser' },
    });
    fireEvent.change(screen.getByLabelText(/e-mail/i), {
      target: { value: 'test@example.com' },
    });
    fireEvent.change(screen.getByLabelText(/vorname/i), {
      target: { value: 'Test' },
    });
    fireEvent.change(screen.getByLabelText(/nachname/i), {
      target: { value: 'User' },
    });

    // Wait for validation and submit
    await waitFor(() => {
      expect(screen.getByRole('button', { name: /erstellen/i })).not.toBeDisabled();
    });

    fireEvent.click(screen.getByRole('button', { name: /erstellen/i }));

    await waitFor(() => {
      expect(mockMutateAsync).toHaveBeenCalledWith({
        username: 'testuser',
        email: 'test@example.com',
        firstName: 'Test',
        lastName: 'User',
        roles: ['sales'], // Default role
        enabled: true,
      });
    });

    expect(mockOnSuccess).toHaveBeenCalledTimes(1);
  });

  it('shows loading state during submission', async () => {
    mockUseCreateUser.mockReturnValue({
      mutateAsync: mockMutateAsync,
      isPending: true,
    });

    renderWithProviders(<UserForm onSuccess={mockOnSuccess} onCancel={mockOnCancel} />);

    const submitButton = screen.getByRole('button', { name: /erstellen/i });
    expect(submitButton).toBeDisabled();
    expect(screen.getByText('⏳')).toBeInTheDocument();
  });
});
