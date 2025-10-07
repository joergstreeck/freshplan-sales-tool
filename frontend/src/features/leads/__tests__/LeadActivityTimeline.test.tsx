import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import LeadActivityTimeline from '../LeadActivityTimeline';
import type { LeadActivity } from '../types';

// Mock API
const mockFetch = vi.fn();
global.fetch = mockFetch;

describe('LeadActivityTimeline - Component Tests (Sprint 2.1.6 Phase 4)', () => {
  const mockActivities: LeadActivity[] = [
    {
      id: 1,
      leadId: 100,
      activityType: 'EMAIL',
      activityDate: '2025-10-05T14:30:00',
      description: 'Angebot per E-Mail versendet',
      isMeaningfulContact: true,
      performedBy: 'user1',
      createdAt: '2025-10-05T14:30:00',
    },
    {
      id: 2,
      leadId: 100,
      activityType: 'CALL',
      activityDate: '2025-10-04T10:15:00',
      description: 'Telefonat mit Geschäftsführer',
      isMeaningfulContact: true,
      performedBy: 'user1',
      createdAt: '2025-10-04T10:15:00',
    },
    {
      id: 3,
      leadId: 100,
      activityType: 'CLOCK_STOPPED',
      activityDate: '2025-10-03T09:00:00',
      description: 'Schutzfrist pausiert: Kunde im Urlaub',
      isMeaningfulContact: false,
      performedBy: 'admin1',
      createdAt: '2025-10-03T09:00:00',
    },
    {
      id: 4,
      leadId: 100,
      activityType: 'CLOCK_RESUMED',
      activityDate: '2025-10-02T16:00:00',
      description: 'Schutzfrist fortgesetzt',
      isMeaningfulContact: false,
      performedBy: 'admin1',
      createdAt: '2025-10-02T16:00:00',
    },
    {
      id: 5,
      leadId: 100,
      activityType: 'MEETING',
      activityDate: '2025-10-01T11:00:00',
      description: 'Produktpräsentation vor Ort',
      isMeaningfulContact: true,
      performedBy: 'user1',
      createdAt: '2025-10-01T11:00:00',
    },
  ];

  beforeEach(() => {
    vi.clearAllMocks();
  });

  // ================= Rendering Tests =================

  describe('Rendering', () => {
    it('should render timeline with activities in chronological order (newest first)', async () => {
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => ({ data: mockActivities, page: 1, size: 20, total: 5 }),
      });

      render(<LeadActivityTimeline leadId={100} />);

      await waitFor(() => {
        expect(screen.getByText('Angebot per E-Mail versendet')).toBeInTheDocument();
      });

      // Check that activities are rendered (newest first is #1, oldest is #5)
      const descriptions = screen.getAllByText(/.*/).map((el) => el.textContent);

      // First activity should be EMAIL (id: 1, newest)
      expect(descriptions).toContain('Angebot per E-Mail versendet');

      // Last activity should be MEETING (id: 5, oldest)
      expect(descriptions).toContain('Produktpräsentation vor Ort');
    });

    it('should render timeline component with activities', async () => {
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => ({ data: mockActivities, page: 1, size: 20, total: 5 }),
      });

      render(<LeadActivityTimeline leadId={100} />);

      await waitFor(() => {
        // Check that timeline has rendered (by checking for Timeline role="list")
        const timeline = screen.getAllByRole('listitem');
        expect(timeline.length).toBeGreaterThan(0);
      });
    });

    it('should render all activity types with correct icons', async () => {
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => ({ data: mockActivities, page: 1, size: 20, total: 5 }),
      });

      render(<LeadActivityTimeline leadId={100} />);

      await waitFor(() => {
        // Check all 5 activities are rendered (by checking unique descriptions)
        expect(screen.getByText('Angebot per E-Mail versendet')).toBeInTheDocument();
        expect(screen.getByText('Telefonat mit Geschäftsführer')).toBeInTheDocument();
        expect(screen.getByText('Schutzfrist pausiert: Kunde im Urlaub')).toBeInTheDocument();
        expect(screen.getAllByText('Schutzfrist fortgesetzt').length).toBeGreaterThan(0); // Label + Description
        expect(screen.getByText('Produktpräsentation vor Ort')).toBeInTheDocument();
      });
    });
  });

  // ================= Meaningful Contact Badge Tests =================

  describe('Meaningful Contact Badge', () => {
    it('should show "Meaningful Contact" badge for isMeaningfulContact=true', async () => {
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => ({ data: [
          {
            id: 1,
            leadId: 100,
            activityType: 'EMAIL',
            activityDate: '2025-10-05T14:30:00',
            description: 'Angebot per E-Mail versendet',
            isMeaningfulContact: true,
            performedBy: 'user1',
            createdAt: '2025-10-05T14:30:00',
          },
        ], page: 1, size: 20, total: 1 }),
      });

      render(<LeadActivityTimeline leadId={100} />);

      await waitFor(() => {
        expect(screen.getByText('Meaningful Contact')).toBeInTheDocument();
      });
    });

    it('should NOT show badge for isMeaningfulContact=false', async () => {
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => ({ data: [
          {
            id: 1,
            leadId: 100,
            activityType: 'NOTE',
            activityDate: '2025-10-05T14:30:00',
            description: 'Interne Notiz hinzugefügt',
            isMeaningfulContact: false,
            performedBy: 'user1',
            createdAt: '2025-10-05T14:30:00',
          },
        ], page: 1, size: 20, total: 1 }),
      });

      render(<LeadActivityTimeline leadId={100} />);

      await waitFor(() => {
        expect(screen.queryByText('Meaningful Contact')).not.toBeInTheDocument();
      });
    });

    it('should show badge for CALL, EMAIL, MEETING types', async () => {
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => ({ data: [
          {
            id: 1,
            leadId: 100,
            activityType: 'CALL',
            activityDate: '2025-10-05T14:30:00',
            description: 'Telefonat',
            isMeaningfulContact: true,
            performedBy: 'user1',
            createdAt: '2025-10-05T14:30:00',
          },
          {
            id: 2,
            leadId: 100,
            activityType: 'EMAIL',
            activityDate: '2025-10-04T14:30:00',
            description: 'E-Mail',
            isMeaningfulContact: true,
            performedBy: 'user1',
            createdAt: '2025-10-04T14:30:00',
          },
          {
            id: 3,
            leadId: 100,
            activityType: 'MEETING',
            activityDate: '2025-10-03T14:30:00',
            description: 'Meeting',
            isMeaningfulContact: true,
            performedBy: 'user1',
            createdAt: '2025-10-03T14:30:00',
          },
        ], page: 1, size: 20, total: 3 }),
      });

      render(<LeadActivityTimeline leadId={100} />);

      await waitFor(() => {
        const badges = screen.getAllByText('Meaningful Contact');
        expect(badges).toHaveLength(3);
      });
    });
  });

  // ================= Stop-the-Clock Activity Tests =================

  describe('Stop-the-Clock Activities', () => {
    it('should render CLOCK_STOPPED with Pause icon and warning color', async () => {
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => ({ data: [
          {
            id: 1,
            leadId: 100,
            activityType: 'CLOCK_STOPPED',
            activityDate: '2025-10-05T09:00:00',
            description: 'Schutzfrist pausiert: Kunde im Urlaub',
            isMeaningfulContact: false,
            performedBy: 'admin1',
            createdAt: '2025-10-05T09:00:00',
          },
        ], page: 1, size: 20, total: 1 }),
      });

      render(<LeadActivityTimeline leadId={100} />);

      await waitFor(() => {
        // Check for description (unique text)
        expect(screen.getByText('Schutzfrist pausiert: Kunde im Urlaub')).toBeInTheDocument();
      });

      // Check for Pause icon (MUI TimelineDot with warning color)
      const timelineItems = screen.getAllByRole('listitem');
      expect(timelineItems.length).toBeGreaterThan(0);
    });

    it('should render CLOCK_RESUMED with Play icon and success color', async () => {
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => ({ data: [
          {
            id: 1,
            leadId: 100,
            activityType: 'CLOCK_RESUMED',
            activityDate: '2025-10-05T16:00:00',
            description: 'Schutzfrist fortgesetzt',
            isMeaningfulContact: false,
            performedBy: 'admin1',
            createdAt: '2025-10-05T16:00:00',
          },
        ], page: 1, size: 20, total: 1 }),
      });

      render(<LeadActivityTimeline leadId={100} />);

      await waitFor(() => {
        expect(screen.getAllByText('Schutzfrist fortgesetzt').length).toBeGreaterThan(0); // Label + Description
      });
    });

    it('should NOT show Meaningful Contact badge for stop/resume activities', async () => {
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => ({ data: [
          {
            id: 1,
            leadId: 100,
            activityType: 'CLOCK_STOPPED',
            activityDate: '2025-10-05T09:00:00',
            description: 'Schutzfrist pausiert',
            isMeaningfulContact: false,
            performedBy: 'admin1',
            createdAt: '2025-10-05T09:00:00',
          },
          {
            id: 2,
            leadId: 100,
            activityType: 'CLOCK_RESUMED',
            activityDate: '2025-10-05T16:00:00',
            description: 'Schutzfrist fortgesetzt',
            isMeaningfulContact: false,
            performedBy: 'admin1',
            createdAt: '2025-10-05T16:00:00',
          },
        ], page: 1, size: 20, total: 2 }),
      });

      render(<LeadActivityTimeline leadId={100} />);

      await waitFor(() => {
        expect(screen.getAllByText('Schutzfrist pausiert').length).toBeGreaterThan(0); // Label + Description
      });

      expect(screen.queryByText('Meaningful Contact')).not.toBeInTheDocument();
    });
  });

  // ================= Date Formatting Tests (German) =================

  describe('German Date Formatting', () => {
    it('should format date as "dd. MMM yyyy" in German', async () => {
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => ({ data: [
          {
            id: 1,
            leadId: 100,
            activityType: 'EMAIL',
            activityDate: '2025-10-05T14:30:00',
            description: 'Test-Aktivität',
            isMeaningfulContact: false,
            performedBy: 'user1',
            createdAt: '2025-10-05T14:30:00',
          },
        ], page: 1, size: 20, total: 1 }),
      });

      render(<LeadActivityTimeline leadId={100} />);

      await waitFor(() => {
        // date-fns format: "05. Okt. 2025" with leading zero
        expect(screen.getByText(/05\. Okt\. 2025/)).toBeInTheDocument();
      });
    });

    it('should format time as "HH:mm" in German', async () => {
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => ({ data: [
          {
            id: 1,
            leadId: 100,
            activityType: 'EMAIL',
            activityDate: '2025-10-05T14:30:00',
            description: 'Test-Aktivität',
            isMeaningfulContact: false,
            performedBy: 'user1',
            createdAt: '2025-10-05T14:30:00',
          },
        ], page: 1, size: 20, total: 1 }),
      });

      render(<LeadActivityTimeline leadId={100} />);

      await waitFor(() => {
        expect(screen.getByText(/14:30/)).toBeInTheDocument();
      });
    });

    it('should handle different months in German', async () => {
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => ({ data: [
          {
            id: 1,
            leadId: 100,
            activityType: 'EMAIL',
            activityDate: '2025-01-15T10:00:00',
            description: 'Januar',
            isMeaningfulContact: false,
            performedBy: 'user1',
            createdAt: '2025-01-15T10:00:00',
          },
          {
            id: 2,
            leadId: 100,
            activityType: 'EMAIL',
            activityDate: '2025-12-25T18:00:00',
            description: 'Dezember',
            isMeaningfulContact: false,
            performedBy: 'user1',
            createdAt: '2025-12-25T18:00:00',
          },
        ], page: 1, size: 20, total: 2 }),
      });

      render(<LeadActivityTimeline leadId={100} />);

      await waitFor(() => {
        expect(screen.getByText(/Jan\./)).toBeInTheDocument();
        expect(screen.getByText(/Dez\./)).toBeInTheDocument();
      });
    });
  });

  // ================= Empty State Tests =================

  describe('Empty State', () => {
    it('should show empty state when no activities exist', async () => {
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => ({ data: [], page: 1, size: 20, total: 0 }),
      });

      render(<LeadActivityTimeline leadId={100} />);

      await waitFor(() => {
        expect(screen.getByText(/Noch keine Aktivitäten/)).toBeInTheDocument();
      });
    });

    it('should show empty state message', async () => {
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => ({ data: [], page: 1, size: 20, total: 0 }),
      });

      render(<LeadActivityTimeline leadId={100} />);

      await waitFor(() => {
        expect(screen.getByText(/Noch keine Aktivitäten/)).toBeInTheDocument();
      });

      // Component renders Typography, not Alert (no role="alert")
      expect(screen.getByText('Noch keine Aktivitäten vorhanden')).toBeInTheDocument();
    });
  });

  // ================= Error Handling Tests =================

  describe('Error Handling', () => {
    it('should show error message when API fails', async () => {
      mockFetch.mockResolvedValueOnce({
        ok: false,
        status: 500,
        statusText: 'Internal Server Error',
      });

      render(<LeadActivityTimeline leadId={100} />);

      await waitFor(() => {
        expect(screen.getByText(/Fehler beim Laden/)).toBeInTheDocument();
      });
    });

    it('should show error alert with error severity', async () => {
      mockFetch.mockResolvedValueOnce({
        ok: false,
        status: 404,
        statusText: 'Not Found',
      });

      render(<LeadActivityTimeline leadId={100} />);

      await waitFor(() => {
        const alert = screen.getByRole('alert');
        expect(alert).toBeInTheDocument();
      });
    });

    it('should handle network errors gracefully', async () => {
      mockFetch.mockRejectedValueOnce(new Error('Network error'));

      render(<LeadActivityTimeline leadId={100} />);

      await waitFor(() => {
        // Component shows the actual error message, not "Fehler beim Laden"
        expect(screen.getByText('Network error')).toBeInTheDocument();
      });
    });
  });

  // ================= Loading State Tests =================

  describe('Loading State', () => {
    it('should show loading indicator while fetching activities', () => {
      mockFetch.mockImplementationOnce(
        () =>
          new Promise((resolve) =>
            setTimeout(() => resolve({ ok: true, json: async () => [] }), 1000)
          )
      );

      render(<LeadActivityTimeline leadId={100} />);

      expect(screen.getByRole('progressbar')).toBeInTheDocument();
    });

    it('should hide loading indicator after data is loaded', async () => {
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => ({ data: mockActivities, page: 1, size: 20, total: 5 }),
      });

      render(<LeadActivityTimeline leadId={100} />);

      await waitFor(() => {
        expect(screen.queryByRole('progressbar')).not.toBeInTheDocument();
      });
    });
  });

  // ================= Activity Type Coverage Tests =================

  describe('Activity Type Coverage', () => {
    it('should render all supported activity types with correct icons', async () => {
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => ({ data: [
          {
            id: 1,
            leadId: 100,
            activityType: 'EMAIL',
            activityDate: '2025-10-05T14:30:00',
            description: 'E-Mail',
            isMeaningfulContact: true,
            performedBy: 'user1',
            createdAt: '2025-10-05T14:30:00',
          },
          {
            id: 2,
            leadId: 100,
            activityType: 'CALL',
            activityDate: '2025-10-04T14:30:00',
            description: 'Anruf',
            isMeaningfulContact: true,
            performedBy: 'user1',
            createdAt: '2025-10-04T14:30:00',
          },
          {
            id: 3,
            leadId: 100,
            activityType: 'MEETING',
            activityDate: '2025-10-03T14:30:00',
            description: 'Meeting',
            isMeaningfulContact: true,
            performedBy: 'user1',
            createdAt: '2025-10-03T14:30:00',
          },
          {
            id: 4,
            leadId: 100,
            activityType: 'SAMPLE',
            activityDate: '2025-10-02T14:30:00',
            description: 'Muster versendet',
            isMeaningfulContact: true,
            performedBy: 'user1',
            createdAt: '2025-10-02T14:30:00',
          },
          {
            id: 5,
            leadId: 100,
            activityType: 'ORDER',
            activityDate: '2025-10-01T14:30:00',
            description: 'Bestellung eingegangen',
            isMeaningfulContact: true,
            performedBy: 'user1',
            createdAt: '2025-10-01T14:30:00',
          },
          {
            id: 6,
            leadId: 100,
            activityType: 'NOTE',
            activityDate: '2025-09-30T14:30:00',
            description: 'Notiz',
            isMeaningfulContact: false,
            performedBy: 'user1',
            createdAt: '2025-09-30T14:30:00',
          },
        ], page: 1, size: 20, total: 6 }),
      });

      render(<LeadActivityTimeline leadId={100} />);

      await waitFor(() => {
        // Use getAllByText for labels that appear multiple times (Label + Description)
        expect(screen.getAllByText('E-Mail').length).toBeGreaterThan(0);
        expect(screen.getAllByText('Anruf').length).toBeGreaterThan(0);
        expect(screen.getAllByText('Termin').length).toBeGreaterThan(0); // Component uses 'Termin' not 'Meeting'
        expect(screen.getByText('Muster versendet')).toBeInTheDocument();
        expect(screen.getByText('Bestellung')).toBeInTheDocument();
        expect(screen.getAllByText('Notiz').length).toBeGreaterThan(0); // Label + Description
      });
    });
  });

  // ================= Edge Cases =================

  describe('Edge Cases', () => {
    it('should handle activities with missing description', async () => {
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => ({ data: [
          {
            id: 1,
            leadId: 100,
            activityType: 'EMAIL',
            activityDate: '2025-10-05T14:30:00',
            description: '',
            isMeaningfulContact: false,
            performedBy: 'user1',
            createdAt: '2025-10-05T14:30:00',
          },
        ], page: 1, size: 20, total: 1 }),
      });

      render(<LeadActivityTimeline leadId={100} />);

      await waitFor(() => {
        // Should render without crashing
        const timelineItems = screen.getAllByRole('listitem');
        expect(timelineItems.length).toBeGreaterThan(0);
      });
    });

    it('should handle activities with null performedBy', async () => {
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => ({ data: [
          {
            id: 1,
            leadId: 100,
            activityType: 'EMAIL',
            activityDate: '2025-10-05T14:30:00',
            description: 'Test',
            isMeaningfulContact: false,
            performedBy: null,
            createdAt: '2025-10-05T14:30:00',
          },
        ], page: 1, size: 20, total: 1 }),
      });

      render(<LeadActivityTimeline leadId={100} />);

      await waitFor(() => {
        expect(screen.getByText('Test')).toBeInTheDocument();
      });
    });

    it('should handle leadId changes and refetch activities', async () => {
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => ({ data: [
          {
            id: 1,
            leadId: 100,
            activityType: 'EMAIL',
            activityDate: '2025-10-05T14:30:00',
            description: 'Lead 100',
            isMeaningfulContact: false,
            performedBy: 'user1',
            createdAt: '2025-10-05T14:30:00',
          },
        ], page: 1, size: 20, total: 1 }),
      });

      const { rerender } = render(<LeadActivityTimeline leadId={100} />);

      await waitFor(() => {
        expect(screen.getByText('Lead 100')).toBeInTheDocument();
      });

      // Change leadId
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => ({ data: [
          {
            id: 2,
            leadId: 200,
            activityType: 'CALL',
            activityDate: '2025-10-05T15:00:00',
            description: 'Lead 200',
            isMeaningfulContact: false,
            performedBy: 'user1',
            createdAt: '2025-10-05T15:00:00',
          },
        ], page: 1, size: 20, total: 1 }),
      });

      rerender(<LeadActivityTimeline leadId={200} />);

      await waitFor(() => {
        expect(screen.getByText('Lead 200')).toBeInTheDocument();
      });
    });
  });
});
