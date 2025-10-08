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
import { useAuth } from '../../../hooks/useAuth';

// Mock the API
vi.mock('../api');
const mockApi = api as {
  listLeads: ReturnType<typeof vi.fn>;
  createLead: ReturnType<typeof vi.fn>;
};

// Mock useAuth hook
vi.mock('../../../hooks/useAuth', () => ({
  useAuth: vi.fn(),
}));
const mockUseAuth = useAuth as ReturnType<typeof vi.fn>;

// Test wrapper with theme and i18n
const Wrapper = ({ children }: { children: React.ReactNode }) => (
  <I18nextProvider i18n={i18n}>
    <ThemeProvider theme={freshfoodzTheme}>
      <CssBaseline />
      {children}
    </ThemeProvider>
  </I18nextProvider>
);

describe('LeadList', () => {
  beforeEach(() => {
    vi.clearAllMocks();

    // Default: User is ADMIN (for all tests that don't override)
    mockUseAuth.mockReturnValue({
      hasRole: (role: string) => role === 'ADMIN',
    });
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
      expect(screen.getByText(/no leads available|keine leads vorhanden/i)).toBeInTheDocument();
      const buttons = screen.getAllByRole('button', { name: /create lead|lead anlegen/i });
      expect(buttons.length).toBeGreaterThanOrEqual(1);
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
      const buttons = screen.getAllByRole('button', { name: /create lead|lead anlegen/i });
      expect(buttons.length).toBeGreaterThanOrEqual(1);
    });

    const createButtons = screen.getAllByRole('button', { name: /create lead|lead anlegen/i });
    await user.click(createButtons[0]);

    await waitFor(() => {
      expect(screen.getByRole('dialog')).toBeInTheDocument();
      expect(screen.getByLabelText(/name/i)).toBeInTheDocument();
    });
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
      expect(screen.getByText(/no leads available|keine leads vorhanden/i)).toBeInTheDocument();
    });

    // Open create dialog
    const createButtons = screen.getAllByRole('button', { name: /create lead|lead anlegen/i });
    await user.click(createButtons[0]);

    // Fill form
    await user.type(screen.getByLabelText(/name/i), 'New Lead');
    await user.type(screen.getByLabelText(/e.?mail/i), 'new@example.com');

    // Submit - get the dialog button specifically
    await waitFor(() => {
      expect(screen.getByRole('dialog')).toBeInTheDocument();
    });
    const dialogButtons = screen.getAllByRole('button', { name: /create lead|lead anlegen/i });
    const saveButton = dialogButtons.find(btn => btn.closest('[role="dialog"]'));
    if (saveButton) {
      await user.click(saveButton);
    }

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

  // ================= Sprint 2.1.6 Phase 4 Tests =================

  describe('Phase 4 Features (Lead Scoring, Stop-Clock, Timeline)', () => {
    const mockLeadWithScore = {
      id: 1,
      companyName: 'Test GmbH',
      email: 'test@example.com',
      leadScore: 75,
      clockStoppedAt: null,
      stopReason: null,
      status: 'REGISTERED',
      createdAt: '2025-01-15T10:00:00',
    };

    it('should render LeadScoreIndicator for each lead', async () => {
      mockApi.listLeads.mockResolvedValue([mockLeadWithScore]);

      render(<LeadList />, { wrapper: Wrapper });

      await waitFor(() => {
        expect(screen.getByText('Test GmbH')).toBeInTheDocument();
      });

      // Check for LeadScoreIndicator (renders score value as "75/100")
      expect(screen.getByText('75/100')).toBeInTheDocument();
    });

    it('should render Stop-the-Clock button with Pause icon when clock is running', async () => {
      mockApi.listLeads.mockResolvedValue([mockLeadWithScore]);

      render(<LeadList />, { wrapper: Wrapper });

      await waitFor(() => {
        expect(screen.getByText('Test GmbH')).toBeInTheDocument();
      });

      // Check for Pause button (Pause icon visible when clockStoppedAt is null)
      const pauseButtons = screen.getAllByTitle('Schutzfrist pausieren');
      expect(pauseButtons.length).toBeGreaterThan(0);
    });

    it('should render Stop-the-Clock button with Play icon when clock is stopped', async () => {
      const stoppedLead = {
        ...mockLeadWithScore,
        clockStoppedAt: '2025-02-05T10:00:00',
        stopReason: 'Kunde im Urlaub',
      };
      mockApi.listLeads.mockResolvedValue([stoppedLead]);

      render(<LeadList />, { wrapper: Wrapper });

      await waitFor(() => {
        expect(screen.getByText('Test GmbH')).toBeInTheDocument();
      });

      // Check for Play button (PlayArrow icon visible when clockStoppedAt is set)
      const playButtons = screen.getAllByTitle('Schutzfrist fortsetzen');
      expect(playButtons.length).toBeGreaterThan(0);

      // Check for pause chip
      expect(screen.getByText(/⏸️ Pausiert: Kunde im Urlaub/)).toBeInTheDocument();
    });

    it('should render Timeline button for each lead', async () => {
      mockApi.listLeads.mockResolvedValue([mockLeadWithScore]);

      render(<LeadList />, { wrapper: Wrapper });

      await waitFor(() => {
        expect(screen.getByText('Test GmbH')).toBeInTheDocument();
      });

      // Check for Timeline button
      const timelineButtons = screen.getAllByTitle('Aktivitäten anzeigen');
      expect(timelineButtons.length).toBeGreaterThan(0);
    });

    it('should toggle LeadActivityTimeline when Timeline button is clicked', async () => {
      mockApi.listLeads.mockResolvedValue([mockLeadWithScore]);
      const user = userEvent.setup();

      // Mock fetch for activities API (used by LeadActivityTimeline)
      global.fetch = vi.fn().mockResolvedValue({
        ok: true,
        json: async () => ({ data: [], page: 1, size: 20, total: 0 }),
      });

      render(<LeadList />, { wrapper: Wrapper });

      await waitFor(() => {
        expect(screen.getByText('Test GmbH')).toBeInTheDocument();
      });

      // Timeline should NOT be visible initially
      expect(screen.queryByText('Noch keine Aktivitäten vorhanden')).not.toBeInTheDocument();

      // Click Timeline button
      const timelineButton = screen.getByTitle('Aktivitäten anzeigen');
      await user.click(timelineButton);

      // Timeline should now be visible
      await waitFor(() => {
        expect(screen.getByText('Noch keine Aktivitäten vorhanden')).toBeInTheDocument();
      });

      // Click again to hide
      await user.click(timelineButton);

      // Timeline should be hidden again
      await waitFor(() => {
        expect(screen.queryByText('Noch keine Aktivitäten vorhanden')).not.toBeInTheDocument();
      });
    });

    it('should show StopTheClockDialog when Stop-Clock button is clicked', async () => {
      mockApi.listLeads.mockResolvedValue([mockLeadWithScore]);
      const user = userEvent.setup();

      render(<LeadList />, { wrapper: Wrapper });

      await waitFor(() => {
        expect(screen.getByText('Test GmbH')).toBeInTheDocument();
      });

      // Click Pause button
      const pauseButton = screen.getByTitle('Schutzfrist pausieren');
      await user.click(pauseButton);

      // StopTheClockDialog should open
      await waitFor(() => {
        expect(screen.getByText('Schutzfrist pausieren')).toBeInTheDocument();
        expect(screen.getByLabelText('Grund für Pause')).toBeInTheDocument();
      });
    });

    it('should hide Stop-Clock button for USER role (RBAC)', async () => {
      mockUseAuth.mockReturnValue({
        hasRole: (role: string) => role === 'USER',
      });

      mockApi.listLeads.mockResolvedValue([mockLeadWithScore]);

      render(<LeadList />, { wrapper: Wrapper });

      await waitFor(() => {
        expect(screen.getByText('Test GmbH')).toBeInTheDocument();
      });

      // Stop-Clock button should NOT be visible for USER
      expect(screen.queryByTitle('Schutzfrist pausieren')).not.toBeInTheDocument();
      expect(screen.queryByTitle('Schutzfrist fortsetzen')).not.toBeInTheDocument();
    });
  });
});
