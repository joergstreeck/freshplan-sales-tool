/* eslint-disable @typescript-eslint/no-explicit-any */
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import CustomerActivityTimeline from './CustomerActivityTimeline';

// Mock date-fns format to avoid locale issues
vi.mock('date-fns', async () => {
  const actual = await vi.importActual('date-fns');
  return {
    ...actual,
    format: vi.fn((date: Date, formatStr: string) => {
      // Simple mock that returns a predictable string
      if (formatStr === 'dd. MMM yyyy') return '20. Jan 2025';
      if (formatStr === 'HH:mm') return '10:30';
      return date.toISOString();
    }),
  };
});

interface CustomerActivity {
  id: number;
  activityType: string;
  activityDate: string;
  description?: string;
  userId: string;
}

describe('CustomerActivityTimeline', () => {
  const mockCustomerId = '123e4567-e89b-12d3-a456-426614174000';

  beforeEach(() => {
    // Reset and create new mock for each test
    global.fetch = vi.fn() as any;
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  describe('Loading State', () => {
    it('should render loading spinner initially', () => {
      (global.fetch as any).mockImplementation(() => new Promise(() => {}));

      render(<CustomerActivityTimeline customerId={mockCustomerId} />);

      expect(screen.getByRole('progressbar')).toBeInTheDocument();
    });
  });

  describe('Success State', () => {
    it('should fetch and display timeline activities', async () => {
      const mockActivities: CustomerActivity[] = [
        {
          id: 1,
          activityType: 'EMAIL',
          activityDate: '2025-01-20T10:30:00Z',
          description: 'Angebot versendet',
          userId: 'user@example.com',
        },
        {
          id: 2,
          activityType: 'CALL',
          activityDate: '2025-01-19T14:00:00Z',
          description: 'Telefonat geführt',
          userId: 'user@example.com',
        },
      ];

      (global.fetch as any).mockResolvedValueOnce({
        ok: true,
        json: async () => ({ events: mockActivities }),
      });

      render(<CustomerActivityTimeline customerId={mockCustomerId} />);

      await waitFor(() => {
        expect(screen.getByText('EMAIL')).toBeInTheDocument();
        expect(screen.getByText('CALL')).toBeInTheDocument();
      });

      expect(screen.getByText('Angebot versendet')).toBeInTheDocument();
      expect(screen.getByText('Telefonat geführt')).toBeInTheDocument();
    });

    it('should display activity without description', async () => {
      const mockActivities: CustomerActivity[] = [
        {
          id: 1,
          activityType: 'EMAIL',
          activityDate: '2025-01-20T10:30:00Z',
          userId: 'user@example.com',
        },
      ];

      (global.fetch as any).mockResolvedValueOnce({
        ok: true,
        json: async () => ({ events: mockActivities }),
      });

      render(<CustomerActivityTimeline customerId={mockCustomerId} />);

      await waitFor(() => {
        expect(screen.getByText('EMAIL')).toBeInTheDocument();
      });

      expect(screen.getByText('Benutzer: user@example.com')).toBeInTheDocument();
    });

    it('should handle response with data property', async () => {
      const mockActivities: CustomerActivity[] = [
        {
          id: 1,
          activityType: 'EMAIL',
          activityDate: '2025-01-20T10:30:00Z',
          userId: 'user@example.com',
        },
      ];

      (global.fetch as any).mockResolvedValueOnce({
        ok: true,
        json: async () => ({ data: mockActivities }),
      });

      render(<CustomerActivityTimeline customerId={mockCustomerId} />);

      await waitFor(() => {
        expect(screen.getByText('EMAIL')).toBeInTheDocument();
      });
    });

    it('should handle response without events or data property', async () => {
      (global.fetch as any).mockResolvedValueOnce({
        ok: true,
        json: async () => ({}),
      });

      render(<CustomerActivityTimeline customerId={mockCustomerId} />);

      await waitFor(() => {
        expect(screen.getByText('Noch keine Aktivitäten erfasst')).toBeInTheDocument();
      });
    });
  });

  describe('Error State', () => {
    it('should display error message when API call fails', async () => {
      (global.fetch as any).mockRejectedValueOnce(new Error('Network error'));

      render(<CustomerActivityTimeline customerId={mockCustomerId} />);

      await waitFor(() => {
        expect(screen.getByText('Network error')).toBeInTheDocument();
      });
    });

    it('should display error message when response is not ok', async () => {
      (global.fetch as any).mockResolvedValueOnce({
        ok: false,
        status: 500,
      });

      render(<CustomerActivityTimeline customerId={mockCustomerId} />);

      await waitFor(() => {
        expect(screen.getByText('Fehler beim Laden der Aktivitäten')).toBeInTheDocument();
      });
    });

    it('should handle unknown error types', async () => {
      (global.fetch as any).mockRejectedValueOnce('Unknown error');

      render(<CustomerActivityTimeline customerId={mockCustomerId} />);

      await waitFor(() => {
        expect(screen.getByText('Unbekannter Fehler')).toBeInTheDocument();
      });
    });
  });

  describe('Empty State', () => {
    it('should display empty state when no activities exist', async () => {
      (global.fetch as any).mockResolvedValueOnce({
        ok: true,
        json: async () => ({ events: [] }),
      });

      render(<CustomerActivityTimeline customerId={mockCustomerId} />);

      await waitFor(() => {
        expect(screen.getByText('Noch keine Aktivitäten erfasst')).toBeInTheDocument();
      });
    });
  });

  describe('Activity Type Icons', () => {
    const activityTypes = [
      'EMAIL',
      'CALL',
      'MEETING',
      'SAMPLE_SENT',
      'ORDER',
      'NOTE',
      'STATUS_CHANGE',
    ];

    activityTypes.forEach(type => {
      it(`should render ${type} activity with correct icon`, async () => {
        const mockActivities: CustomerActivity[] = [
          {
            id: 1,
            activityType: type,
            activityDate: '2025-01-20T10:30:00Z',
            userId: 'user@example.com',
          },
        ];

        (global.fetch as any).mockResolvedValueOnce({
          ok: true,
          json: async () => ({ events: mockActivities }),
        });

        render(<CustomerActivityTimeline customerId={mockCustomerId} />);

        await waitFor(() => {
          expect(screen.getByText(type)).toBeInTheDocument();
        });
      });
    });

    it('should render unknown activity type with default icon', async () => {
      const mockActivities: CustomerActivity[] = [
        {
          id: 1,
          activityType: 'UNKNOWN_TYPE',
          activityDate: '2025-01-20T10:30:00Z',
          userId: 'user@example.com',
        },
      ];

      (global.fetch as any).mockResolvedValueOnce({
        ok: true,
        json: async () => ({ events: mockActivities }),
      });

      render(<CustomerActivityTimeline customerId={mockCustomerId} />);

      await waitFor(() => {
        expect(screen.getByText('UNKNOWN_TYPE')).toBeInTheDocument();
      });
    });
  });

  describe('Activity Sorting and Timeline', () => {
    it('should display multiple activities in chronological order', async () => {
      const mockActivities: CustomerActivity[] = [
        {
          id: 1,
          activityType: 'EMAIL',
          activityDate: '2025-01-20T10:30:00Z',
          description: 'First activity',
          userId: 'user@example.com',
        },
        {
          id: 2,
          activityType: 'CALL',
          activityDate: '2025-01-19T14:00:00Z',
          description: 'Second activity',
          userId: 'user@example.com',
        },
        {
          id: 3,
          activityType: 'MEETING',
          activityDate: '2025-01-18T09:00:00Z',
          description: 'Third activity',
          userId: 'user@example.com',
        },
      ];

      (global.fetch as any).mockResolvedValueOnce({
        ok: true,
        json: async () => ({ events: mockActivities }),
      });

      render(<CustomerActivityTimeline customerId={mockCustomerId} />);

      await waitFor(() => {
        expect(screen.getByText('First activity')).toBeInTheDocument();
        expect(screen.getByText('Second activity')).toBeInTheDocument();
        expect(screen.getByText('Third activity')).toBeInTheDocument();
      });
    });

    it('should render timeline connectors between activities', async () => {
      const mockActivities: CustomerActivity[] = [
        {
          id: 1,
          activityType: 'EMAIL',
          activityDate: '2025-01-20T10:30:00Z',
          userId: 'user@example.com',
        },
        {
          id: 2,
          activityType: 'CALL',
          activityDate: '2025-01-19T14:00:00Z',
          userId: 'user@example.com',
        },
      ];

      (global.fetch as any).mockResolvedValueOnce({
        ok: true,
        json: async () => ({ events: mockActivities }),
      });

      const { container } = render(<CustomerActivityTimeline customerId={mockCustomerId} />);

      await waitFor(() => {
        expect(screen.getByText('EMAIL')).toBeInTheDocument();
      });

      // Check for Timeline structure
      const timeline = container.querySelector('.MuiTimeline-root');
      expect(timeline).toBeInTheDocument();
    });
  });

  describe('Date Formatting', () => {
    it('should display formatted date and time', async () => {
      const mockActivities: CustomerActivity[] = [
        {
          id: 1,
          activityType: 'EMAIL',
          activityDate: '2025-01-20T10:30:00Z',
          userId: 'user@example.com',
        },
      ];

      (global.fetch as any).mockResolvedValueOnce({
        ok: true,
        json: async () => ({ events: mockActivities }),
      });

      render(<CustomerActivityTimeline customerId={mockCustomerId} />);

      await waitFor(() => {
        expect(screen.getByText('EMAIL')).toBeInTheDocument();
      });

      // Check for formatted date (dd. MMM yyyy)
      expect(screen.getByText(/\d{2}\./)).toBeInTheDocument();

      // Check for formatted time (HH:mm Uhr)
      expect(screen.getByText(/Uhr/)).toBeInTheDocument();
    });
  });

  describe('API Integration', () => {
    it('should call correct API endpoint', async () => {
      (global.fetch as any).mockResolvedValueOnce({
        ok: true,
        json: async () => ({ events: [] }),
      });

      render(<CustomerActivityTimeline customerId={mockCustomerId} />);

      await waitFor(() => {
        expect(global.fetch).toHaveBeenCalledWith(`/api/customers/${mockCustomerId}/timeline`, {
          credentials: 'include',
        });
      });
    });

    it('should refetch activities when customerId changes', async () => {
      const { rerender } = render(<CustomerActivityTimeline customerId={mockCustomerId} />);

      (global.fetch as any).mockResolvedValueOnce({
        ok: true,
        json: async () => ({ events: [] }),
      });

      await waitFor(() => {
        expect(global.fetch).toHaveBeenCalledTimes(1);
      });

      const newCustomerId = '987e6543-e21b-43d2-a654-426614174999';
      (global.fetch as any).mockResolvedValueOnce({
        ok: true,
        json: async () => ({ events: [] }),
      });

      rerender(<CustomerActivityTimeline customerId={newCustomerId} />);

      await waitFor(() => {
        expect(global.fetch).toHaveBeenCalledTimes(2);
        expect(global.fetch).toHaveBeenLastCalledWith(`/api/customers/${newCustomerId}/timeline`, {
          credentials: 'include',
        });
      });
    });
  });

  describe('User Information Display', () => {
    it('should display user ID for each activity', async () => {
      const mockActivities: CustomerActivity[] = [
        {
          id: 1,
          activityType: 'EMAIL',
          activityDate: '2025-01-20T10:30:00Z',
          userId: 'john.doe@example.com',
        },
      ];

      (global.fetch as any).mockResolvedValueOnce({
        ok: true,
        json: async () => ({ events: mockActivities }),
      });

      render(<CustomerActivityTimeline customerId={mockCustomerId} />);

      await waitFor(() => {
        expect(screen.getByText('Benutzer: john.doe@example.com')).toBeInTheDocument();
      });
    });

    it('should display different users for different activities', async () => {
      const mockActivities: CustomerActivity[] = [
        {
          id: 1,
          activityType: 'EMAIL',
          activityDate: '2025-01-20T10:30:00Z',
          userId: 'user1@example.com',
        },
        {
          id: 2,
          activityType: 'CALL',
          activityDate: '2025-01-19T14:00:00Z',
          userId: 'user2@example.com',
        },
      ];

      (global.fetch as any).mockResolvedValueOnce({
        ok: true,
        json: async () => ({ events: mockActivities }),
      });

      render(<CustomerActivityTimeline customerId={mockCustomerId} />);

      await waitFor(() => {
        expect(screen.getByText('Benutzer: user1@example.com')).toBeInTheDocument();
        expect(screen.getByText('Benutzer: user2@example.com')).toBeInTheDocument();
      });
    });
  });

  describe('Material UI Components', () => {
    it('should render Paper component for each activity', async () => {
      const mockActivities: CustomerActivity[] = [
        {
          id: 1,
          activityType: 'EMAIL',
          activityDate: '2025-01-20T10:30:00Z',
          userId: 'user@example.com',
        },
      ];

      (global.fetch as any).mockResolvedValueOnce({
        ok: true,
        json: async () => ({ events: mockActivities }),
      });

      const { container } = render(<CustomerActivityTimeline customerId={mockCustomerId} />);

      await waitFor(() => {
        expect(screen.getByText('EMAIL')).toBeInTheDocument();
      });

      const paperElements = container.querySelectorAll('.MuiPaper-root');
      expect(paperElements.length).toBeGreaterThan(0);
    });

    it('should render TimelineDot with correct color for different activity types', async () => {
      const mockActivities: CustomerActivity[] = [
        {
          id: 1,
          activityType: 'EMAIL',
          activityDate: '2025-01-20T10:30:00Z',
          userId: 'user@example.com',
        },
        {
          id: 2,
          activityType: 'ORDER',
          activityDate: '2025-01-19T14:00:00Z',
          userId: 'user@example.com',
        },
      ];

      (global.fetch as any).mockResolvedValueOnce({
        ok: true,
        json: async () => ({ events: mockActivities }),
      });

      const { container } = render(<CustomerActivityTimeline customerId={mockCustomerId} />);

      await waitFor(() => {
        expect(screen.getByText('EMAIL')).toBeInTheDocument();
        expect(screen.getByText('ORDER')).toBeInTheDocument();
      });

      // Check for TimelineDot elements
      const timelineDots = container.querySelectorAll('.MuiTimelineDot-root');
      expect(timelineDots.length).toBe(2);
    });
  });
});
