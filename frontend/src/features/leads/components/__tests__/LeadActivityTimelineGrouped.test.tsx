import { describe, test, expect, beforeEach, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { LeadActivityTimelineGrouped } from '../LeadActivityTimelineGrouped';
import { server } from '@/mocks/server';
import { http, HttpResponse } from 'msw';

/**
 * Enterprise-Level Tests for LeadActivityTimelineGrouped
 *
 * Tests cover:
 * - Grouping logic (7 days / 30 days / older)
 * - API integration with MSW
 * - Loading/Error/Empty states
 * - Preview text generation
 * - Accordion expand/collapse behavior
 */

describe('LeadActivityTimelineGrouped', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  // ================================================================================
  // GROUPING LOGIC TESTS
  // ================================================================================

  test('groups activities correctly by time ranges', async () => {
    const now = new Date();
    const twoDaysAgo = new Date(now.getTime() - 2 * 24 * 60 * 60 * 1000);
    const fifteenDaysAgo = new Date(now.getTime() - 15 * 24 * 60 * 60 * 1000);
    const fortyDaysAgo = new Date(now.getTime() - 40 * 24 * 60 * 60 * 1000);

    // Mock API response with activities in different time ranges
    server.use(
      http.get('/api/leads/:id/activities', () => {
        return HttpResponse.json({
          data: [
            {
              id: 1,
              activityType: 'LEAD_CREATED',
              activityDate: twoDaysAgo.toISOString(),
              description: 'Lead erstellt',
              userId: 'user1',
            },
            {
              id: 2,
              activityType: 'CALL',
              activityDate: fifteenDaysAgo.toISOString(),
              description: 'Anruf getätigt',
              userId: 'user1',
            },
            {
              id: 3,
              activityType: 'EMAIL',
              activityDate: fortyDaysAgo.toISOString(),
              description: 'E-Mail gesendet',
              userId: 'user1',
            },
          ],
          page: 0,
          size: 50,
          total: 3,
        });
      })
    );

    render(<LeadActivityTimelineGrouped leadId={123} />);

    // Wait for activities to load
    await waitFor(() => {
      expect(screen.getByText('Letzte 7 Tage')).toBeInTheDocument();
    });

    // Verify grouping
    expect(screen.getByText('Letzte 7 Tage')).toBeInTheDocument();
    expect(screen.getByText('Letzte 30 Tage')).toBeInTheDocument();
    expect(screen.getByText('Älter')).toBeInTheDocument();

    // Verify counts (using badge)
    const badges = screen.getAllByText('1'); // Each group has 1 activity
    expect(badges.length).toBeGreaterThanOrEqual(3);

    // Verify "Letzte 7 Tage" is expanded by default and shows activity
    expect(screen.getByText(/Lead erstellt/i)).toBeInTheDocument();
  });

  test('shows all activities in one group if within 7 days', async () => {
    const now = new Date();
    const oneDayAgo = new Date(now.getTime() - 1 * 24 * 60 * 60 * 1000);
    const twoDaysAgo = new Date(now.getTime() - 2 * 24 * 60 * 60 * 1000);

    server.use(
      http.get('/api/leads/:id/activities', () => {
        return HttpResponse.json({
          data: [
            {
              id: 1,
              activityType: 'CALL',
              activityDate: oneDayAgo.toISOString(),
              description: 'Anruf 1',
              userId: 'user1',
            },
            {
              id: 2,
              activityType: 'EMAIL',
              activityDate: twoDaysAgo.toISOString(),
              description: 'Email 1',
              userId: 'user1',
            },
          ],
          page: 0,
          size: 50,
          total: 2,
        });
      })
    );

    render(<LeadActivityTimelineGrouped leadId={123} />);

    await waitFor(() => {
      expect(screen.getByText('Letzte 7 Tage')).toBeInTheDocument();
    });

    // Should show count badge of 2
    expect(screen.getByText('2')).toBeInTheDocument();

    // Both activities should be visible (expanded by default)
    expect(screen.getByText(/Anruf 1/i)).toBeInTheDocument();
    expect(screen.getByText(/Email 1/i)).toBeInTheDocument();

    // Other groups should not be visible (no activities)
    const last30DaysGroup = screen.queryByText(/Letzte 30 Tage/i);
    expect(last30DaysGroup).not.toBeInTheDocument(); // No activities, so group hidden
  });

  // ================================================================================
  // API INTEGRATION TESTS
  // ================================================================================

  test('handles API errors gracefully', async () => {
    // Mock API error
    server.use(
      http.get('/api/leads/:id/activities', () => {
        return HttpResponse.json(
          { message: 'Internal Server Error' },
          { status: 500 }
        );
      })
    );

    render(<LeadActivityTimelineGrouped leadId={123} />);

    // Should show error message
    await waitFor(() => {
      expect(screen.getByText(/Fehler beim Laden der Aktivitäten/i)).toBeInTheDocument();
    });
  });

  test('shows loading state during fetch', async () => {
    // Mock delayed response
    server.use(
      http.get('/api/leads/:id/activities', async () => {
        await new Promise(resolve => setTimeout(resolve, 100));
        return HttpResponse.json({
          data: [],
          page: 0,
          size: 50,
          total: 0,
        });
      })
    );

    render(<LeadActivityTimelineGrouped leadId={123} />);

    // Should show loading spinner
    expect(screen.getByRole('progressbar')).toBeInTheDocument();

    // Wait for loading to finish
    await waitFor(() => {
      expect(screen.queryByRole('progressbar')).not.toBeInTheDocument();
    });
  });

  test('shows empty state when no activities exist', async () => {
    server.use(
      http.get('/api/leads/:id/activities', () => {
        return HttpResponse.json({
          data: [],
          page: 0,
          size: 50,
          total: 0,
        });
      })
    );

    render(<LeadActivityTimelineGrouped leadId={123} />);

    await waitFor(() => {
      expect(screen.getByText(/Noch keine Aktivitäten vorhanden/i)).toBeInTheDocument();
    });
  });

  // ================================================================================
  // PREVIEW TEXT GENERATION TESTS
  // ================================================================================

  test('shows preview text when accordion is collapsed', async () => {
    const fifteenDaysAgo = new Date(Date.now() - 15 * 24 * 60 * 60 * 1000);

    server.use(
      http.get('/api/leads/:id/activities', () => {
        return HttpResponse.json({
          data: [
            {
              id: 1,
              activityType: 'CALL',
              activityDate: fifteenDaysAgo.toISOString(),
              description: 'This is a very long activity description that should be truncated in the preview',
              userId: 'user1',
            },
          ],
          page: 0,
          size: 50,
          total: 1,
        });
      })
    );

    render(<LeadActivityTimelineGrouped leadId={123} />);

    await waitFor(() => {
      expect(screen.getByText('Letzte 30 Tage')).toBeInTheDocument();
    });

    // Should show preview text (first 40 chars + "...")
    expect(screen.getByText(/Letzte: This is a very long activity descrip.../i)).toBeInTheDocument();
  });

  // ================================================================================
  // ACCORDION EXPAND/COLLAPSE BEHAVIOR TESTS
  // ================================================================================

  test('expands "Letzte 7 Tage" by default', async () => {
    const twoDaysAgo = new Date(Date.now() - 2 * 24 * 60 * 60 * 1000);

    server.use(
      http.get('/api/leads/:id/activities', () => {
        return HttpResponse.json({
          data: [
            {
              id: 1,
              activityType: 'CALL',
              activityDate: twoDaysAgo.toISOString(),
              description: 'Recent activity',
              userId: 'user1',
            },
          ],
          page: 0,
          size: 50,
          total: 1,
        });
      })
    );

    render(<LeadActivityTimelineGrouped leadId={123} />);

    await waitFor(() => {
      expect(screen.getByText('Letzte 7 Tage')).toBeInTheDocument();
    });

    // Activity should be visible (expanded)
    expect(screen.getByText(/Recent activity/i)).toBeInTheDocument();
  });

  test('can expand and collapse accordion groups', async () => {
    const user = userEvent.setup();
    const fifteenDaysAgo = new Date(Date.now() - 15 * 24 * 60 * 60 * 1000);

    server.use(
      http.get('/api/leads/:id/activities', () => {
        return HttpResponse.json({
          data: [
            {
              id: 1,
              activityType: 'CALL',
              activityDate: fifteenDaysAgo.toISOString(),
              description: 'Mid-range activity',
              userId: 'user1',
            },
          ],
          page: 0,
          size: 50,
          total: 1,
        });
      })
    );

    render(<LeadActivityTimelineGrouped leadId={123} />);

    await waitFor(() => {
      expect(screen.getByText('Letzte 30 Tage')).toBeInTheDocument();
    });

    // Initially collapsed (activity not visible)
    expect(screen.queryByText(/Mid-range activity/i)).not.toBeInTheDocument();

    // Click to expand
    const accordion = screen.getByText('Letzte 30 Tage').closest('button');
    if (accordion) {
      await user.click(accordion);
    }

    // Activity should now be visible
    await waitFor(() => {
      expect(screen.getByText(/Mid-range activity/i)).toBeInTheDocument();
    });

    // Click again to collapse
    if (accordion) {
      await user.click(accordion);
    }

    // Activity should be hidden again
    await waitFor(() => {
      expect(screen.queryByText(/Mid-range activity/i)).not.toBeInTheDocument();
    });
  });

  // ================================================================================
  // ACTIVITY ICON & FORMATTING TESTS
  // ================================================================================

  test('displays activity icons and timestamps correctly', async () => {
    const twoDaysAgo = new Date(Date.now() - 2 * 24 * 60 * 60 * 1000);

    server.use(
      http.get('/api/leads/:id/activities', () => {
        return HttpResponse.json({
          data: [
            {
              id: 1,
              activityType: 'LEAD_CREATED',
              activityDate: twoDaysAgo.toISOString(),
              description: 'Lead erstellt',
              userId: 'user1',
            },
          ],
          page: 0,
          size: 50,
          total: 1,
        });
      })
    );

    render(<LeadActivityTimelineGrouped leadId={123} />);

    await waitFor(() => {
      expect(screen.getByText(/Lead erstellt/i)).toBeInTheDocument();
    });

    // Should show relative timestamp (e.g., "vor 2 Tagen")
    expect(screen.getByText(/vor \d+ (Tag|Tagen)/i)).toBeInTheDocument();

    // Should show user ID
    expect(screen.getByText(/user1/i)).toBeInTheDocument();
  });
});
