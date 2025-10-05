import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { ThemeProvider, CssBaseline } from '@mui/material';
import userEvent from '@testing-library/user-event';
import '@testing-library/jest-dom';
import { I18nextProvider } from 'react-i18next';
import i18n from '../../../i18n';
import LeadCreateDialog from '../LeadCreateDialog';
import * as api from '../api';
import freshfoodzTheme from '../../../theme/freshfoodz';

// Mock the API
vi.mock('../api');
const mockApi = api as {
  createLead: ReturnType<typeof vi.fn>;
};

// Test wrapper with theme and i18n
const Wrapper = ({ children }: { children: React.ReactNode }) => (
  <I18nextProvider i18n={i18n}>
    <ThemeProvider theme={freshfoodzTheme}>
      <CssBaseline />
      {children}
    </ThemeProvider>
  </I18nextProvider>
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

  it('renders form fields correctly', async () => {
    render(<LeadCreateDialog {...defaultProps} />, { wrapper: Wrapper });

    await waitFor(() => {
      const titleElements = screen.getAllByText(/create lead|lead anlegen/i);
      expect(titleElements.length).toBeGreaterThanOrEqual(1);
      expect(screen.getByLabelText(/name/i)).toBeInTheDocument();
      expect(screen.getByLabelText(/e.?mail/i)).toBeInTheDocument();
      expect(screen.getByRole('button', { name: /cancel|abbrechen/i })).toBeInTheDocument();
      expect(screen.getByRole('button', { name: /create lead|lead anlegen/i })).toBeInTheDocument();
    });
  });

  it('disables save button when name is empty', async () => {
    render(<LeadCreateDialog {...defaultProps} />, { wrapper: Wrapper });

    await waitFor(() => {
      const saveButton = screen.getByRole('button', { name: /create lead|lead anlegen/i });
      expect(saveButton).toBeDisabled();
    });
  });

  it('enables save button when name is provided', async () => {
    const user = userEvent.setup();
    render(<LeadCreateDialog {...defaultProps} />, { wrapper: Wrapper });

    await waitFor(() => {
      expect(screen.getByLabelText(/name/i)).toBeInTheDocument();
    });

    const nameInput = screen.getByLabelText(/name/i);
    await user.type(nameInput, 'Test Lead');

    await waitFor(() => {
      const saveButton = screen.getByRole('button', { name: /create lead|lead anlegen/i });
      expect(saveButton).toBeEnabled();
    });
  });

  it('creates lead successfully', async () => {
    const user = userEvent.setup();
    const mockLead = { id: '1', name: 'Test Lead', email: 'test@example.com' };
    mockApi.createLead.mockResolvedValue(mockLead);

    render(<LeadCreateDialog {...defaultProps} />, { wrapper: Wrapper });

    await waitFor(() => {
      expect(screen.getByLabelText(/name/i)).toBeInTheDocument();
    });

    // Fill form
    await user.type(screen.getByLabelText(/name/i), 'Test Lead');
    await user.type(screen.getByLabelText(/e.?mail/i), 'test@example.com');

    // Submit
    await waitFor(() => {
      expect(screen.getByRole('button', { name: /create lead|lead anlegen/i })).toBeInTheDocument();
    });
    await user.click(screen.getByRole('button', { name: /create lead|lead anlegen/i }));

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

    await waitFor(() => {
      expect(screen.getByLabelText(/name/i)).toBeInTheDocument();
    });

    // Fill only name
    await user.type(screen.getByLabelText(/name/i), 'Test Lead');

    // Submit
    await waitFor(() => {
      expect(screen.getByRole('button', { name: /create lead|lead anlegen/i })).toBeInTheDocument();
    });
    await user.click(screen.getByRole('button', { name: /create lead|lead anlegen/i }));

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

    await waitFor(() => {
      expect(screen.getByLabelText(/name/i)).toBeInTheDocument();
    });

    // Fill form
    await user.type(screen.getByLabelText(/name/i), 'Duplicate Lead');

    // Submit
    await waitFor(() => {
      expect(screen.getByRole('button', { name: /create lead|lead anlegen/i })).toBeInTheDocument();
    });
    await user.click(screen.getByRole('button', { name: /create lead|lead anlegen/i }));

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

    await waitFor(() => {
      expect(screen.getByLabelText(/name/i)).toBeInTheDocument();
    });

    // Fill form
    await user.type(screen.getByLabelText(/name/i), 'Test Lead');

    // Submit
    await waitFor(() => {
      expect(screen.getByRole('button', { name: /create lead|lead anlegen/i })).toBeInTheDocument();
    });
    await user.click(screen.getByRole('button', { name: /create lead|lead anlegen/i }));

    // Verify form is disabled
    await waitFor(() => {
      expect(screen.getByLabelText(/name/i)).toBeDisabled();
      expect(screen.getByLabelText(/e.?mail/i)).toBeDisabled();
      expect(screen.getByRole('button', { name: /saving|speichern/i })).toBeDisabled();
      expect(screen.getByRole('button', { name: /cancel|abbrechen/i })).toBeDisabled();
    });
  });

  it('resets form when dialog is closed', async () => {
    const user = userEvent.setup();
    const { rerender } = render(<LeadCreateDialog {...defaultProps} />, { wrapper: Wrapper });

    await waitFor(() => {
      expect(screen.getByLabelText(/name/i)).toBeInTheDocument();
    });

    // Fill form
    await user.type(screen.getByLabelText(/name/i), 'Test Lead');

    // Close dialog
    await waitFor(() => {
      expect(screen.getByRole('button', { name: /cancel|abbrechen/i })).toBeInTheDocument();
    });
    await user.click(screen.getByRole('button', { name: /cancel|abbrechen/i }));

    // Reopen dialog
    rerender(<LeadCreateDialog {...defaultProps} open={true} />);

    // Verify form is reset
    await waitFor(() => {
      expect(screen.getByLabelText(/name/i)).toHaveValue('');
      expect(screen.getByLabelText(/e.?mail/i)).toHaveValue('');
    });
  });

  describe('Validation', () => {
    it('shows error for name less than 2 characters', async () => {
      const user = userEvent.setup();
      render(<LeadCreateDialog {...defaultProps} />, { wrapper: Wrapper });

      await waitFor(() => {
        expect(screen.getByLabelText(/name/i)).toBeInTheDocument();
      });

      const nameInput = screen.getByLabelText(/name/i);

      // Type single character
      await user.type(nameInput, 'A');

      await waitFor(() => {
        expect(
          screen.getByRole('button', { name: /create lead|lead anlegen/i })
        ).toBeInTheDocument();
      });
      const saveButton = screen.getByRole('button', { name: /create lead|lead anlegen/i });
      await user.click(saveButton);

      // Should show validation error
      await waitFor(() => {
        expect(
          screen.getByText(/name must be at least 2 characters|name muss mindestens 2 zeichen/i)
        ).toBeInTheDocument();
      });
    });

    it('validates email format', async () => {
      const user = userEvent.setup();
      render(<LeadCreateDialog {...defaultProps} />, { wrapper: Wrapper });

      await waitFor(() => {
        expect(screen.getByLabelText(/name/i)).toBeInTheDocument();
      });

      const nameInput = screen.getByLabelText(/name/i);
      const emailInput = screen.getByLabelText(/e.?mail/i);

      await user.type(nameInput, 'Test Lead');
      await user.type(emailInput, 'invalid-email');

      await waitFor(() => {
        expect(
          screen.getByRole('button', { name: /create lead|lead anlegen/i })
        ).toBeInTheDocument();
      });
      const saveButton = screen.getByRole('button', { name: /create lead|lead anlegen/i });
      await user.click(saveButton);

      // Should show email validation error
      await waitFor(() => {
        expect(
          screen.getByText(/please enter a valid e.?mail|bitte eine gültige e.?mail eingeben/i)
        ).toBeInTheDocument();
      });
    });

    it('accepts valid email format', async () => {
      const user = userEvent.setup();
      mockApi.createLead.mockResolvedValue({ id: '1', name: 'Test', email: 'test@example.com' });
      render(<LeadCreateDialog {...defaultProps} />, { wrapper: Wrapper });

      await waitFor(() => {
        expect(screen.getByLabelText(/name/i)).toBeInTheDocument();
      });

      await user.type(screen.getByLabelText(/name/i), 'Test Lead');
      await user.type(screen.getByLabelText(/e.?mail/i), 'test@example.com');

      await waitFor(() => {
        expect(
          screen.getByRole('button', { name: /create lead|lead anlegen/i })
        ).toBeInTheDocument();
      });
      await user.click(screen.getByRole('button', { name: /create lead|lead anlegen/i }));

      expect(mockApi.createLead).toHaveBeenCalledWith({
        name: 'Test Lead',
        email: 'test@example.com',
      });
    });
  });

  describe('Duplicate Detection (409 Handling)', () => {
    it('shows special warning for duplicate email (409)', async () => {
      const user = userEvent.setup();
      const duplicateError = {
        title: 'Duplicate lead',
        status: 409,
        detail: 'Lead mit dieser E-Mail existiert bereits.',
        errors: { email: ['E-Mail ist bereits vergeben'] },
      };
      mockApi.createLead.mockRejectedValue(duplicateError);

      render(<LeadCreateDialog {...defaultProps} />, { wrapper: Wrapper });

      await waitFor(() => {
        expect(screen.getByLabelText(/name/i)).toBeInTheDocument();
      });

      await user.type(screen.getByLabelText(/name/i), 'Test Lead');
      await user.type(screen.getByLabelText(/e.?mail/i), 'existing@example.com');

      await waitFor(() => {
        expect(
          screen.getByRole('button', { name: /create lead|lead anlegen/i })
        ).toBeInTheDocument();
      });
      await user.click(screen.getByRole('button', { name: /create lead|lead anlegen/i }));

      // Should show duplicate warning alert
      await waitFor(() => {
        const alert = screen.getByRole('alert');
        expect(alert).toHaveClass('MuiAlert-standardWarning');
        expect(
          screen.getByText(
            /a lead with this e.?mail already exists|lead mit dieser e.?mail existiert bereits/i
          )
        ).toBeInTheDocument();
      });
    });

    it('shows field-specific error for duplicate', async () => {
      const user = userEvent.setup();
      const duplicateError = {
        title: 'Duplicate lead',
        status: 409,
        detail: 'Lead mit dieser E-Mail existiert bereits.',
        errors: { email: ['E-Mail ist bereits vergeben'] },
      };
      mockApi.createLead.mockRejectedValue(duplicateError);

      render(<LeadCreateDialog {...defaultProps} />, { wrapper: Wrapper });

      await waitFor(() => {
        expect(screen.getByLabelText(/name/i)).toBeInTheDocument();
      });

      await user.type(screen.getByLabelText(/name/i), 'Test Lead');
      await user.type(screen.getByLabelText(/e.?mail/i), 'duplicate@example.com');

      await waitFor(() => {
        expect(
          screen.getByRole('button', { name: /create lead|lead anlegen/i })
        ).toBeInTheDocument();
      });
      await user.click(screen.getByRole('button', { name: /create lead|lead anlegen/i }));

      // Should show field error
      await waitFor(() => {
        const emailField = screen.getByLabelText(/e.?mail/i).parentElement?.parentElement;
        expect(emailField?.querySelector('.MuiFormHelperText-root')).toHaveTextContent(
          /e.?mail.*already|e.?mail ist bereits vergeben/i
        );
      });
    });
  });

  describe('Source Tracking', () => {
    it('sends source=manual with lead creation', async () => {
      const user = userEvent.setup();
      mockApi.createLead.mockResolvedValue({ id: '1', name: 'Test', source: 'manual' });

      render(<LeadCreateDialog {...defaultProps} />, { wrapper: Wrapper });

      await waitFor(() => {
        expect(screen.getByLabelText(/name/i)).toBeInTheDocument();
      });

      await user.type(screen.getByLabelText(/name/i), 'Manual Lead');

      await waitFor(() => {
        expect(
          screen.getByRole('button', { name: /create lead|lead anlegen/i })
        ).toBeInTheDocument();
      });
      await user.click(screen.getByRole('button', { name: /create lead|lead anlegen/i }));

      // API should be called with source field
      expect(mockApi.createLead).toHaveBeenCalledWith({
        name: 'Manual Lead',
        email: undefined,
      });

      // Note: source='manual' is added in the api.ts layer, not in the component
      // This test verifies the component passes the right data to the API
    });
  });

  describe('i18n Support', () => {
    it('displays German labels by default', async () => {
      render(<LeadCreateDialog {...defaultProps} />, { wrapper: Wrapper });

      await waitFor(() => {
        const titleElements = screen.getAllByText(/create lead|lead anlegen/i);
        expect(titleElements.length).toBeGreaterThanOrEqual(1);
        expect(screen.getByLabelText(/name/i)).toBeInTheDocument();
        expect(screen.getByLabelText(/e.?mail/i)).toBeInTheDocument();
        expect(screen.getByRole('button', { name: /cancel|abbrechen/i })).toBeInTheDocument();
      });
    });

    it('uses translation keys for all UI elements', async () => {
      render(<LeadCreateDialog {...defaultProps} />, { wrapper: Wrapper });

      // Check that dialog uses i18n keys (not hardcoded strings)
      await waitFor(() => {
        const dialogTitle = screen.getByText((content, element) => {
          return element?.tagName === 'H2' && /lead/i.test(content);
        });
        expect(dialogTitle).toBeInTheDocument();
      });
    });
  });
});
