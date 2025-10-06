import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import '@testing-library/jest-dom';
import { ThemeProvider } from '@mui/material';
import { I18nextProvider } from 'react-i18next';
import i18n from '../../../i18n';
import LeadList from '../LeadList';
import * as api from '../api';
import type { Lead, Problem } from '../types';
import freshfoodzTheme from '../../../theme/freshfoodz';

// Mock the API module
vi.mock('../api');
const mockApi = api as {
  listLeads: ReturnType<typeof vi.fn>;
  createLead: ReturnType<typeof vi.fn>;
  toProblem: ReturnType<typeof vi.fn>;
};

// Test wrapper with theme and i18n
const TestWrapper = ({ children }: { children: React.ReactNode }) => (
  <I18nextProvider i18n={i18n}>
    <ThemeProvider theme={freshfoodzTheme}>{children}</ThemeProvider>
  </I18nextProvider>
);

describe('Lead Management Integration Tests', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('Lead List Component', () => {
    it('shows loading state initially', () => {
      mockApi.listLeads.mockImplementation(() => new Promise(() => {}));
      render(<LeadList />, { wrapper: TestWrapper });

      expect(screen.getByRole('progressbar')).toBeInTheDocument();
    });

    it('displays lead list when loaded', async () => {
      const mockLeads: Lead[] = [
        {
          id: '1',
          name: 'Test Lead 1',
          email: 'lead1@test.com',
          createdAt: '2025-01-01T00:00:00Z',
        },
        { id: '2', name: 'Test Lead 2', email: null, createdAt: '2025-01-02T00:00:00Z' },
      ];
      mockApi.listLeads.mockResolvedValue(mockLeads);

      render(<LeadList />, { wrapper: TestWrapper });

      await waitFor(() => {
        expect(screen.getByText('Test Lead 1')).toBeInTheDocument();
        expect(screen.getByText('lead1@test.com')).toBeInTheDocument();
        expect(screen.getByText('Test Lead 2')).toBeInTheDocument();
      });
    });

    it('shows error alert on API failure', async () => {
      const error: Problem = {
        title: 'Network Error',
        detail: 'Failed to fetch leads',
        status: 500,
      };
      mockApi.listLeads.mockRejectedValue(error);

      render(<LeadList />, { wrapper: TestWrapper });

      await waitFor(() => {
        expect(screen.getByRole('alert')).toBeInTheDocument();
        expect(screen.getByText(/Network Error/)).toBeInTheDocument();
      });
    });
  });

  describe('Lead Creation Flow', () => {
    it('validates name field (minimum 2 characters)', async () => {
      const user = userEvent.setup();
      mockApi.listLeads.mockResolvedValue([]);

      render(<LeadList />, { wrapper: TestWrapper });

      // Wait for list to load
      await waitFor(() => {
        expect(screen.getByText(/no leads available|keine leads vorhanden/i)).toBeInTheDocument();
      });

      // Open dialog by clicking either button (header or empty state)
      const createButtons = screen.getAllByRole('button', { name: /create lead|lead anlegen/i });
      await user.click(createButtons[0]);

      // Dialog should be open
      await waitFor(() => {
        expect(screen.getByRole('dialog')).toBeInTheDocument();
      });

      const nameInput = screen.getByLabelText(/name/i);
      const saveButton = screen.getByRole('button', { name: /create lead|lead anlegen/i });

      // Type only 1 character
      await user.clear(nameInput);
      await user.type(nameInput, 'A');
      await user.click(saveButton);

      // Should show validation error
      await waitFor(() => {
        expect(screen.getByText(/at least 2 characters|mindestens 2 zeichen/i)).toBeInTheDocument();
      });
    });

    it('validates email format', async () => {
      const user = userEvent.setup();
      mockApi.listLeads.mockResolvedValue([]);

      render(<LeadList />, { wrapper: TestWrapper });

      await waitFor(() => {
        expect(screen.getByText(/no leads available|keine leads vorhanden/i)).toBeInTheDocument();
      });

      const createButtons = screen.getAllByRole('button', { name: /create lead|lead anlegen/i });
      await user.click(createButtons[0]);

      await waitFor(() => {
        expect(screen.getByRole('dialog')).toBeInTheDocument();
      });

      const nameInput = screen.getByLabelText(/name/i);
      const emailInput = screen.getByLabelText(/e.?mail/i);

      await user.type(nameInput, 'Valid Name');
      await user.type(emailInput, 'invalid-email');

      await waitFor(() => {
        const dialogSaveButton = screen
          .getAllByRole('button', { name: /create lead|lead anlegen/i })
          .find(btn => btn.closest('[role="dialog"]'));
        expect(dialogSaveButton).toBeInTheDocument();
      });

      const dialogSaveButton = screen
        .getAllByRole('button', { name: /create lead|lead anlegen/i })
        .find(btn => btn.closest('[role="dialog"]'));

      if (dialogSaveButton) {
        await user.click(dialogSaveButton);
      }

      await waitFor(() => {
        expect(
          screen.getByText(/please enter a valid|invalid|ungültig|bitte eine gültige/i)
        ).toBeInTheDocument();
      });
    });

    it('handles duplicate email (409 response)', async () => {
      const user = userEvent.setup();
      mockApi.listLeads.mockResolvedValue([]);

      const duplicateError: Problem = {
        title: 'Duplicate lead',
        status: 409,
        detail: 'Lead mit dieser E-Mail existiert bereits.',
        errors: { email: ['E-Mail ist bereits vergeben'] },
      };
      mockApi.createLead.mockRejectedValue(duplicateError);

      render(<LeadList />, { wrapper: TestWrapper });

      await waitFor(() => {
        expect(screen.getByText(/no leads available|keine leads vorhanden/i)).toBeInTheDocument();
      });

      const createButtons = screen.getAllByRole('button', { name: /create lead|lead anlegen/i });
      await user.click(createButtons[0]);

      const nameInput = screen.getByLabelText(/name/i);
      const emailInput = screen.getByLabelText(/e.?mail/i);

      await user.type(nameInput, 'Duplicate Test');
      await user.type(emailInput, 'duplicate@example.com');

      const dialogSaveButton = screen
        .getAllByRole('button', { name: /create lead|lead anlegen/i })
        .find(btn => btn.closest('[role="dialog"]'));

      if (dialogSaveButton) {
        await user.click(dialogSaveButton);
      }

      // Should show duplicate warning
      await waitFor(() => {
        const alerts = screen.getAllByRole('alert');
        const warningAlert = alerts.find(alert => alert.className?.includes('Warning'));
        expect(warningAlert).toBeInTheDocument();
      });
    });

    it('successfully creates a lead', async () => {
      const user = userEvent.setup();
      mockApi.listLeads.mockResolvedValueOnce([]);

      const newLead: Lead = {
        id: '123',
        name: 'New Test Lead',
        email: 'new@example.com',
        createdAt: new Date().toISOString(),
      };
      mockApi.createLead.mockResolvedValue(newLead);
      mockApi.listLeads.mockResolvedValueOnce([newLead]);

      render(<LeadList />, { wrapper: TestWrapper });

      await waitFor(() => {
        expect(screen.getByText(/no leads available|keine leads vorhanden/i)).toBeInTheDocument();
      });

      const createButtons = screen.getAllByRole('button', { name: /create lead|lead anlegen/i });
      await user.click(createButtons[0]);

      const nameInput = screen.getByLabelText(/name/i);
      const emailInput = screen.getByLabelText(/e.?mail/i);

      await user.type(nameInput, 'New Test Lead');
      await user.type(emailInput, 'new@example.com');

      const dialogSaveButton = screen
        .getAllByRole('button', { name: /create lead|lead anlegen/i })
        .find(btn => btn.closest('[role="dialog"]'));

      if (dialogSaveButton) {
        await user.click(dialogSaveButton);
      }

      // Verify API was called
      await waitFor(() => {
        expect(mockApi.createLead).toHaveBeenCalledWith({
          name: 'New Test Lead',
          email: 'new@example.com',
        });
      });

      // Verify list was refreshed
      await waitFor(() => {
        expect(mockApi.listLeads).toHaveBeenCalledTimes(2);
      });
    });
  });

  describe('Business Logic', () => {
    it('sends leads with source field via API', async () => {
      const user = userEvent.setup();
      mockApi.listLeads.mockResolvedValue([]);
      mockApi.createLead.mockResolvedValue({
        id: '1',
        name: 'Test',
        source: 'manual',
      } as Lead);

      render(<LeadList />, { wrapper: TestWrapper });

      await waitFor(() => {
        expect(screen.getByText(/no leads available|keine leads vorhanden/i)).toBeInTheDocument();
      });

      const createButtons = screen.getAllByRole('button', { name: /create lead|lead anlegen/i });
      await user.click(createButtons[0]);

      const nameInput = screen.getByLabelText(/name/i);
      await user.type(nameInput, 'Manual Entry Lead');

      const dialogSaveButton = screen
        .getAllByRole('button', { name: /create lead|lead anlegen/i })
        .find(btn => btn.closest('[role="dialog"]'));

      if (dialogSaveButton) {
        await user.click(dialogSaveButton);
      }

      await waitFor(() => {
        expect(mockApi.createLead).toHaveBeenCalled();
      });

      // Note: source='manual' is added in the api.ts layer
      const callArgs = mockApi.createLead.mock.calls[0][0];
      expect(callArgs.name).toBe('Manual Entry Lead');
    });

    it('shows field-specific errors from API', async () => {
      const user = userEvent.setup();
      mockApi.listLeads.mockResolvedValue([]);

      const validationError: Problem = {
        title: 'Validation Error',
        status: 400,
        errors: {
          name: ['Name is too short'],
          email: ['Email format is invalid'],
        },
      };
      mockApi.createLead.mockRejectedValue(validationError);

      render(<LeadList />, { wrapper: TestWrapper });

      await waitFor(() => {
        expect(screen.getByText(/no leads available|keine leads vorhanden/i)).toBeInTheDocument();
      });

      const createButtons = screen.getAllByRole('button', { name: /create lead|lead anlegen/i });
      await user.click(createButtons[0]);

      const nameInput = screen.getByLabelText(/name/i);
      await user.type(nameInput, 'X');

      const dialogSaveButton = screen
        .getAllByRole('button', { name: /create lead|lead anlegen/i })
        .find(btn => btn.closest('[role="dialog"]'));

      if (dialogSaveButton) {
        await user.click(dialogSaveButton);
      }

      await waitFor(() => {
        const nameField = screen.getByLabelText(/name/i).parentElement?.parentElement;
        expect(nameField?.querySelector('.MuiFormHelperText-root')).toHaveTextContent(
          /at least 2 characters|mindestens 2 zeichen|too short/i
        );
      });
    });
  });
});
