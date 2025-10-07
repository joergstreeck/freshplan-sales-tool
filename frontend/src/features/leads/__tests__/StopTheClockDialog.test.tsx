import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import StopTheClockDialog from '../StopTheClockDialog';
import { useAuth } from '../../../hooks/useAuth';
import type { Lead } from '../types';

// Mock useAuth hook
vi.mock('../../../hooks/useAuth', () => ({
  useAuth: vi.fn(),
}));

// Mock API
vi.mock('../api', () => ({
  updateLead: vi.fn().mockResolvedValue({}),
}));

const mockUseAuth = useAuth as ReturnType<typeof vi.fn>;

describe('StopTheClockDialog - RBAC Tests (Sprint 2.1.6 Phase 4 - Test Gap Analysis)', () => {
  const mockLead: Lead = {
    id: 1,
    companyName: 'Test GmbH',
    contactPerson: 'Max Mustermann',
    email: 'test@example.com',
    phone: '+49 123 456789',
    website: 'https://test.de',
    street: 'Teststraße 1',
    postalCode: '12345',
    city: 'Berlin',
    countryCode: 'DE',
    territory: {
      id: 'DE',
      name: 'Deutschland',
      countryCode: 'DE',
      currencyCode: 'EUR',
    },
    businessType: 'Restaurant',
    kitchenSize: 'medium',
    employeeCount: 15,
    estimatedVolume: 25000,
    status: 'REGISTERED',
    ownerUserId: 'user1',
    collaboratorUserIds: [],
    source: 'web',
    sourceCampaign: null,
    stage: 'REGISTRIERUNG',
    registeredAt: '2025-01-15T10:00:00',
    lastActivityAt: '2025-02-01T14:30:00',
    reminderSentAt: null,
    gracePeriodStartAt: null,
    expiredAt: null,
    progressWarningSentAt: null,
    progressDeadline: '2025-01-25T10:00:00',
    protectionUntil: '2025-07-15T10:00:00',
    clockStoppedAt: null,
    stopReason: null,
    stopApprovedBy: null,
    progressPauseTotalSeconds: 0,
    leadScore: 65,
    createdAt: '2025-01-15T10:00:00',
    createdBy: 'user1',
    updatedAt: '2025-02-01T14:30:00',
    updatedBy: 'user1',
    version: 0,
  };

  const mockOnClose = vi.fn();
  const mockOnSuccess = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
  });

  // ================= RBAC Permission Tests =================

  describe('Permission Checks', () => {
    it('should show permission error dialog for USER role', () => {
      mockUseAuth.mockReturnValue({
        hasRole: (role: string) => role === 'USER',
      });

      render(
        <StopTheClockDialog
          open={true}
          lead={mockLead}
          onClose={mockOnClose}
          onSuccess={mockOnSuccess}
        />
      );

      expect(screen.getByText('Keine Berechtigung')).toBeInTheDocument();
      expect(
        screen.getByText(
          /Sie haben keine Berechtigung für diese Funktion/
        )
      ).toBeInTheDocument();
      expect(
        screen.getByText(/Nur Administratoren und Manager können die Uhr anhalten/)
      ).toBeInTheDocument();
      expect(screen.getByRole('button', { name: 'Schließen' })).toBeInTheDocument();
    });

    it('should show pause form for ADMIN role', () => {
      mockUseAuth.mockReturnValue({
        hasRole: (role: string) => role === 'ADMIN',
      });

      render(
        <StopTheClockDialog
          open={true}
          lead={mockLead}
          onClose={mockOnClose}
          onSuccess={mockOnSuccess}
        />
      );

      expect(screen.getByText('Schutzfrist pausieren')).toBeInTheDocument();
      expect(screen.getByLabelText('Grund für Pause')).toBeInTheDocument();
      expect(screen.getByRole('button', { name: 'Pausieren' })).toBeEnabled();
    });

    it('should show pause form for MANAGER role', () => {
      mockUseAuth.mockReturnValue({
        hasRole: (role: string) => role === 'MANAGER',
      });

      render(
        <StopTheClockDialog
          open={true}
          lead={mockLead}
          onClose={mockOnClose}
          onSuccess={mockOnSuccess}
        />
      );

      expect(screen.getByText('Schutzfrist pausieren')).toBeInTheDocument();
      expect(screen.getByLabelText('Grund für Pause')).toBeInTheDocument();
      expect(screen.getByRole('button', { name: 'Pausieren' })).toBeEnabled();
    });

    it('should show error for role with no permissions', () => {
      mockUseAuth.mockReturnValue({
        hasRole: (role: string) => role === 'SALES',
      });

      render(
        <StopTheClockDialog
          open={true}
          lead={mockLead}
          onClose={mockOnClose}
          onSuccess={mockOnSuccess}
        />
      );

      expect(screen.getByText('Keine Berechtigung')).toBeInTheDocument();
      expect(screen.queryByLabelText('Grund für Pause')).not.toBeInTheDocument();
    });
  });

  // ================= UI Interaction Tests =================

  describe('Pause Action (ADMIN)', () => {
    beforeEach(() => {
      mockUseAuth.mockReturnValue({
        hasRole: (role: string) => role === 'ADMIN',
      });
    });

    it('should require reason selection before pausing', async () => {
      const user = userEvent.setup();

      render(
        <StopTheClockDialog
          open={true}
          lead={mockLead}
          onClose={mockOnClose}
          onSuccess={mockOnSuccess}
        />
      );

      const pauseButton = screen.getByRole('button', { name: 'Pausieren' });
      await user.click(pauseButton);

      await waitFor(() => {
        expect(screen.getByText(/Bitte wählen Sie einen Grund aus/)).toBeInTheDocument();
      });
    });

    it('should show custom reason field when "Sonstiges" is selected', async () => {
      const user = userEvent.setup();

      render(
        <StopTheClockDialog
          open={true}
          lead={mockLead}
          onClose={mockOnClose}
          onSuccess={mockOnSuccess}
        />
      );

      // Open select dropdown
      const selectButton = screen.getByLabelText('Grund für Pause');
      await user.click(selectButton);

      // Select "Sonstiges"
      const sonstigesOption = screen.getByRole('option', {
        name: 'Sonstiges (bitte angeben)',
      });
      await user.click(sonstigesOption);

      await waitFor(() => {
        expect(
          screen.getByLabelText('Benutzerdefinierter Grund')
        ).toBeInTheDocument();
      });
    });

    it('should require custom reason text when "Sonstiges" is selected', async () => {
      const user = userEvent.setup();

      render(
        <StopTheClockDialog
          open={true}
          lead={mockLead}
          onClose={mockOnClose}
          onSuccess={mockOnSuccess}
        />
      );

      // Select "Sonstiges"
      const selectButton = screen.getByLabelText('Grund für Pause');
      await user.click(selectButton);
      const sonstigesOption = screen.getByRole('option', {
        name: 'Sonstiges (bitte angeben)',
      });
      await user.click(sonstigesOption);

      // Try to save without custom text
      const pauseButton = screen.getByRole('button', { name: 'Pausieren' });
      await user.click(pauseButton);

      await waitFor(() => {
        expect(
          screen.getByText(/Bitte geben Sie einen benutzerdefinierten Grund an/)
        ).toBeInTheDocument();
      });
    });
  });

  // ================= Resume Action Tests =================

  describe('Resume Action (ADMIN)', () => {
    beforeEach(() => {
      mockUseAuth.mockReturnValue({
        hasRole: (role: string) => role === 'ADMIN',
      });
    });

    it('should show resume dialog when clock is stopped', () => {
      const stoppedLead = {
        ...mockLead,
        clockStoppedAt: '2025-02-05T10:00:00',
        stopReason: 'Kunde ist im Urlaub',
      };

      render(
        <StopTheClockDialog
          open={true}
          lead={stoppedLead}
          onClose={mockOnClose}
          onSuccess={mockOnSuccess}
        />
      );

      expect(screen.getByText('Schutzfrist fortsetzen')).toBeInTheDocument();
      expect(screen.getByText('Aktueller Grund:')).toBeInTheDocument();
      expect(screen.getByText('Kunde ist im Urlaub')).toBeInTheDocument();
      expect(screen.getByRole('button', { name: 'Fortsetzen' })).toBeInTheDocument();
    });

    it('should not require reason for resume action', () => {
      const stoppedLead = {
        ...mockLead,
        clockStoppedAt: '2025-02-05T10:00:00',
        stopReason: 'Kunde ist im Urlaub',
      };

      render(
        <StopTheClockDialog
          open={true}
          lead={stoppedLead}
          onClose={mockOnClose}
          onSuccess={mockOnSuccess}
        />
      );

      // Resume button should be enabled without selecting a reason
      expect(screen.getByRole('button', { name: 'Fortsetzen' })).toBeEnabled();
      expect(screen.queryByLabelText('Grund für Pause')).not.toBeInTheDocument();
    });
  });

  // ================= Edge Cases =================

  describe('Edge Cases', () => {
    it('should handle null lead gracefully', () => {
      mockUseAuth.mockReturnValue({
        hasRole: (role: string) => role === 'ADMIN',
      });

      const { container } = render(
        <StopTheClockDialog
          open={true}
          lead={null}
          onClose={mockOnClose}
          onSuccess={mockOnSuccess}
        />
      );

      expect(container.firstChild).toBeNull();
    });

    it('should close dialog when user clicks cancel', async () => {
      const user = userEvent.setup();
      mockUseAuth.mockReturnValue({
        hasRole: (role: string) => role === 'ADMIN',
      });

      render(
        <StopTheClockDialog
          open={true}
          lead={mockLead}
          onClose={mockOnClose}
          onSuccess={mockOnSuccess}
        />
      );

      const cancelButton = screen.getByRole('button', { name: 'Abbrechen' });
      await user.click(cancelButton);

      expect(mockOnClose).toHaveBeenCalledTimes(1);
    });

    it('should close permission error dialog when user clicks close', async () => {
      const user = userEvent.setup();
      mockUseAuth.mockReturnValue({
        hasRole: (role: string) => role === 'USER',
      });

      render(
        <StopTheClockDialog
          open={true}
          lead={mockLead}
          onClose={mockOnClose}
          onSuccess={mockOnSuccess}
        />
      );

      const closeButton = screen.getByRole('button', { name: 'Schließen' });
      await user.click(closeButton);

      expect(mockOnClose).toHaveBeenCalledTimes(1);
    });
  });
});
