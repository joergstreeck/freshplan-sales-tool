import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { ThemeProvider, CssBaseline } from '@mui/material';
import userEvent from '@testing-library/user-event';
import '@testing-library/jest-dom';
import LeadCreateDialog from '../LeadCreateDialog';
import * as api from '../api';
import freshfoodzTheme from '../../../theme/freshfoodz';

// Mock the API
vi.mock('../api');
const mockApi = api as {
  createLead: ReturnType<typeof vi.fn>;
};

// Test wrapper with theme
const Wrapper = ({ children }: { children: React.ReactNode }) => (
  <ThemeProvider theme={freshfoodzTheme}>
    <CssBaseline />
    {children}
  </ThemeProvider>
);

const defaultProps = {
  open: true,
  onClose: vi.fn(),
  onCreated: vi.fn(),
};

describe('LeadCreateDialog', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders form fields correctly', () => {
    render(<LeadCreateDialog {...defaultProps} />, { wrapper: Wrapper });

    expect(screen.getByText(/lead anlegen/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/name/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/e.mail/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /abbrechen/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /speichern/i })).toBeInTheDocument();
  });

  it('disables save button when name is empty', () => {
    render(<LeadCreateDialog {...defaultProps} />, { wrapper: Wrapper });

    const saveButton = screen.getByRole('button', { name: /speichern/i });
    expect(saveButton).toBeDisabled();
  });

  it('enables save button when name is provided', async () => {
    const user = userEvent.setup();
    render(<LeadCreateDialog {...defaultProps} />, { wrapper: Wrapper });

    const nameInput = screen.getByLabelText(/name/i);
    const saveButton = screen.getByRole('button', { name: /speichern/i });

    await user.type(nameInput, 'Test Lead');

    expect(saveButton).toBeEnabled();
  });

  it('creates lead successfully', async () => {
    const user = userEvent.setup();
    const mockLead = { id: '1', name: 'Test Lead', email: 'test@example.com' };
    mockApi.createLead.mockResolvedValue(mockLead);

    render(<LeadCreateDialog {...defaultProps} />, { wrapper: Wrapper });

    // Fill form
    await user.type(screen.getByLabelText(/name/i), 'Test Lead');
    await user.type(screen.getByLabelText(/e.mail/i), 'test@example.com');

    // Submit
    await user.click(screen.getByRole('button', { name: /speichern/i }));

    // Verify API call
    expect(mockApi.createLead).toHaveBeenCalledWith({
      name: 'Test Lead',
      email: 'test@example.com',
    });

    // Verify callbacks
    await waitFor(() => {
      expect(defaultProps.onCreated).toHaveBeenCalled();
    });
  });

  it('creates lead without email', async () => {
    const user = userEvent.setup();
    const mockLead = { id: '1', name: 'Test Lead' };
    mockApi.createLead.mockResolvedValue(mockLead);

    render(<LeadCreateDialog {...defaultProps} />, { wrapper: Wrapper });

    // Fill only name
    await user.type(screen.getByLabelText(/name/i), 'Test Lead');

    // Submit
    await user.click(screen.getByRole('button', { name: /speichern/i }));

    // Verify API call without email
    expect(mockApi.createLead).toHaveBeenCalledWith({
      name: 'Test Lead',
      email: undefined,
    });
  });

  it('shows error message on API failure', async () => {
    const user = userEvent.setup();
    const error = {
      title: 'Validation Error',
      detail: 'Name already exists',
      errors: { name: ['Name must be unique'] },
    };
    mockApi.createLead.mockRejectedValue(error);

    render(<LeadCreateDialog {...defaultProps} />, { wrapper: Wrapper });

    // Fill form
    await user.type(screen.getByLabelText(/name/i), 'Duplicate Lead');

    // Submit
    await user.click(screen.getByRole('button', { name: /speichern/i }));

    // Wait for error to appear
    await waitFor(() => {
      expect(screen.getByText(/validation error/i)).toBeInTheDocument();
      expect(screen.getByText(/name already exists/i)).toBeInTheDocument();
      expect(screen.getByText(/name must be unique/i)).toBeInTheDocument();
    });
  });

  it('disables form during submission', async () => {
    const user = userEvent.setup();
    mockApi.createLead.mockImplementation(
      () => new Promise(() => {}) // Never resolves
    );

    render(<LeadCreateDialog {...defaultProps} />, { wrapper: Wrapper });

    // Fill form
    await user.type(screen.getByLabelText(/name/i), 'Test Lead');

    // Submit
    await user.click(screen.getByRole('button', { name: /speichern/i }));

    // Verify form is disabled
    await waitFor(() => {
      expect(screen.getByLabelText(/name/i)).toBeDisabled();
      expect(screen.getByLabelText(/e.mail/i)).toBeDisabled();
      expect(screen.getByRole('button', { name: /speichern.../i })).toBeDisabled();
      expect(screen.getByRole('button', { name: /abbrechen/i })).toBeDisabled();
    });
  });

  it('resets form when dialog is closed', async () => {
    const user = userEvent.setup();
    const { rerender } = render(<LeadCreateDialog {...defaultProps} />, { wrapper: Wrapper });

    // Fill form
    await user.type(screen.getByLabelText(/name/i), 'Test Lead');

    // Close dialog
    await user.click(screen.getByRole('button', { name: /abbrechen/i }));

    // Reopen dialog
    rerender(<LeadCreateDialog {...defaultProps} open={true} />, { wrapper: Wrapper });

    // Verify form is reset
    expect(screen.getByLabelText(/name/i)).toHaveValue('');
    expect(screen.getByLabelText(/e.mail/i)).toHaveValue('');
  });
});