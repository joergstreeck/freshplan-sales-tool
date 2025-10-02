import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { ThemeProvider, CssBaseline } from '@mui/material';
import userEvent from '@testing-library/user-event';
import '@testing-library/jest-dom';
import { I18nextProvider } from 'react-i18next';
import i18n from '../../../i18n';
import LeadList from '../LeadList';
import * as api from '../api';
import freshfoodzTheme from '../../../theme/freshfoodz';

// Mock the API
vi.mock('../api');
const mockApi = api as {
  listLeads: ReturnType<typeof vi.fn>;
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

// ⚠️ LEGACY TEST - Testet alte LeadList mit LeadCreateDialog (pre-Sprint 2.1.5)
// Sprint 2.1.5 hat LeadWizard (neue UI) integriert
// TODO Sprint 2.1.6: Tests auf LeadWizard umstellen
describe.skip('LeadList', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('shows loading spinner initially', () => {
    mockApi.listLeads.mockImplementation(
      () => new Promise(() => {}) // Never resolves
    );

    render(<LeadList />, { wrapper: Wrapper });

    expect(screen.getByRole('progressbar', { name: /laden/i })).toBeInTheDocument();
  });

  it('shows error message when API fails', async () => {
    const error = { title: 'Network Error', detail: 'Connection failed' };
    mockApi.listLeads.mockRejectedValue(error);

    render(<LeadList />, { wrapper: Wrapper });

    await waitFor(() => {
      expect(screen.getByText(/network error/i)).toBeInTheDocument();
      expect(screen.getByText(/connection failed/i)).toBeInTheDocument();
    });
  });

  it('shows empty state when no leads exist', async () => {
    mockApi.listLeads.mockResolvedValue([]);

    render(<LeadList />, { wrapper: Wrapper });

    await waitFor(() => {
      expect(screen.getByText(/keine leads vorhanden/i)).toBeInTheDocument();
      expect(screen.getByRole('button', { name: /create lead/i })).toBeInTheDocument();
    });
  });

  it('displays leads when data is available', async () => {
    const mockLeads = [
      { id: '1', name: 'Test Lead 1', email: 'test1@example.com', createdAt: '2025-01-01' },
      { id: '2', name: 'Test Lead 2', email: 'test2@example.com', createdAt: '2025-01-02' },
    ];
    mockApi.listLeads.mockResolvedValue(mockLeads);

    render(<LeadList />, { wrapper: Wrapper });

    await waitFor(() => {
      expect(screen.getByText('Test Lead 1')).toBeInTheDocument();
      expect(screen.getByText('test1@example.com')).toBeInTheDocument();
      expect(screen.getByText('Test Lead 2')).toBeInTheDocument();
      expect(screen.getByText('test2@example.com')).toBeInTheDocument();
    });
  });

  it('opens create dialog when button is clicked', async () => {
    mockApi.listLeads.mockResolvedValue([]);
    const user = userEvent.setup();

    render(<LeadList />, { wrapper: Wrapper });

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /create lead/i })).toBeInTheDocument();
    });

    await user.click(screen.getByRole('button', { name: /create lead/i }));

    expect(screen.getByRole('dialog')).toBeInTheDocument();
    expect(screen.getByLabelText(/name/i)).toBeInTheDocument();
  });

  it('refreshes list after lead creation', async () => {
    mockApi.listLeads
      .mockResolvedValueOnce([]) // Initial empty state
      .mockResolvedValueOnce([{ id: '1', name: 'New Lead', email: 'new@example.com' }]); // After creation
    mockApi.createLead.mockResolvedValue({ id: '1', name: 'New Lead', email: 'new@example.com' });

    const user = userEvent.setup();

    render(<LeadList />, { wrapper: Wrapper });

    // Wait for empty state
    await waitFor(() => {
      expect(screen.getByText(/keine leads vorhanden/i)).toBeInTheDocument();
    });

    // Open create dialog
    await user.click(screen.getByRole('button', { name: /create lead/i }));

    // Fill form
    await user.type(screen.getByLabelText(/name/i), 'New Lead');
    await user.type(screen.getByLabelText(/e.?mail/i), 'new@example.com');

    // Submit
    await user.click(screen.getByRole('button', { name: /create lead/i }));

    // Verify API calls
    expect(mockApi.createLead).toHaveBeenCalledWith({
      name: 'New Lead',
      email: 'new@example.com',
    });

    // Verify list was refreshed
    await waitFor(() => {
      expect(mockApi.listLeads).toHaveBeenCalledTimes(2);
    });
  });
});
