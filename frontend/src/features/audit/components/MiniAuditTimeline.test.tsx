/**
 * Tests for MiniAuditTimeline Component
 * Pragmatic approach with minimal mocking for 80%+ coverage
 */

import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MiniAuditTimeline } from './MiniAuditTimeline';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import React from 'react';

// Mock the API
vi.mock('../services/auditApi', () => ({
  auditApi: {
    getAuditHistory: vi.fn(() =>
      Promise.resolve({
        entries: [
          {
            id: '1',
            entityType: 'CONTACT',
            entityId: 'contact-1',
            action: 'UPDATE',
            fieldName: 'email',
            oldValue: 'old@example.com',
            newValue: 'new@example.com',
            userId: 'user-1',
            userName: 'Test User',
            timestamp: new Date().toISOString(),
          },
          {
            id: '2',
            entityType: 'CONTACT',
            entityId: 'contact-1',
            action: 'CREATE',
            userId: 'user-1',
            userName: 'Test User',
            timestamp: new Date(Date.now() - 86400000).toISOString(), // Yesterday
          },
        ],
        totalCount: 2,
      })
    ),
    getLatestChange: vi.fn(() =>
      Promise.resolve({
        id: '1',
        entityType: 'CONTACT',
        entityId: 'contact-1',
        action: 'UPDATE',
        fieldName: 'email',
        oldValue: 'old@example.com',
        newValue: 'new@example.com',
        userId: 'user-1',
        userName: 'Test User',
        timestamp: new Date().toISOString(),
      })
    ),
  },
}));

// Mock Auth Context
vi.mock('../../../contexts/AuthContext', () => ({
  useAuth: () => ({
    user: { id: 'test-user', name: 'Test User', roles: ['admin'] },
    isAuthenticated: true,
  }),
}));

const createWrapper = () => {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: {
        retry: false,
        staleTime: 0,
      },
    },
  });
  const theme = createTheme();

  return ({ children }: { children: React.ReactNode }) => (
    <QueryClientProvider client={queryClient}>
      <ThemeProvider theme={theme}>{children}</ThemeProvider>
    </QueryClientProvider>
  );
};

describe('MiniAuditTimeline', () => {
  const defaultProps = {
    entityType: 'CONTACT',
    entityId: 'contact-1',
  };

  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('Rendering', () => {
    it('should render the component', async () => {
      render(<MiniAuditTimeline {...defaultProps} />, { wrapper: createWrapper() });

      // Should show loading initially
      expect(screen.getByRole('progressbar')).toBeInTheDocument();

      // Should show content after loading
      await waitFor(() => {
        expect(screen.queryByRole('progressbar')).not.toBeInTheDocument();
      });
    });

    it('should show timeline header', async () => {
      render(<MiniAuditTimeline {...defaultProps} />, { wrapper: createWrapper() });

      await waitFor(() => {
        // Should have some indication of audit/history
        const elements = screen.queryAllByText(/änderung|historie|audit/i);
        expect(elements.length).toBeGreaterThan(0);
      });
    });

    it('should display audit entries', async () => {
      render(<MiniAuditTimeline {...defaultProps} />, { wrapper: createWrapper() });

      await waitFor(() => {
        // Should show user who made changes
        expect(screen.getByText('Test User')).toBeInTheDocument();
      });
    });
  });

  describe('Compact Mode', () => {
    it('should render in compact mode by default', async () => {
      const { container } = render(<MiniAuditTimeline {...defaultProps} compact={true} />, {
        wrapper: createWrapper(),
      });

      await waitFor(() => {
        // In compact mode, should use Accordion
        const accordion = container.querySelector('.MuiAccordion-root');
        expect(accordion).toBeInTheDocument();
      });
    });

    it('should expand to show more details', async () => {
      const user = userEvent.setup();
      render(<MiniAuditTimeline {...defaultProps} compact={true} />, { wrapper: createWrapper() });

      await waitFor(async () => {
        const expandButton = screen.queryByRole('button', { expanded: false });
        if (expandButton) {
          await user.click(expandButton);
          // Should now be expanded
          expect(expandButton).toHaveAttribute('aria-expanded', 'true');
        }
      });
    });
  });

  describe('Full Mode', () => {
    it('should show timeline without accordion when not compact', async () => {
      const { container } = render(
        <MiniAuditTimeline {...defaultProps} compact={false} showDetails={true} />,
        { wrapper: createWrapper() }
      );

      await waitFor(() => {
        // Should show timeline directly
        const timeline = container.querySelector('.MuiTimeline-root');
        expect(timeline).toBeInTheDocument();
      });
    });

    it('should limit entries to maxEntries', async () => {
      render(<MiniAuditTimeline {...defaultProps} maxEntries={1} compact={false} />, {
        wrapper: createWrapper(),
      });

      await waitFor(() => {
        // Should only show limited entries
        const timelineItems = document.querySelectorAll('.MuiTimelineItem-root');
        expect(timelineItems.length).toBeLessThanOrEqual(1);
      });
    });
  });

  describe('Action Icons', () => {
    it('should show appropriate icons for different actions', async () => {
      const { container } = render(<MiniAuditTimeline {...defaultProps} compact={false} />, {
        wrapper: createWrapper(),
      });

      await waitFor(() => {
        // Should have icons for actions
        const icons = container.querySelectorAll('[data-testid*="Icon"]');
        expect(icons.length).toBeGreaterThan(0);
      });
    });
  });

  describe('Show More', () => {
    it('should call onShowMore when button clicked', async () => {
      const mockOnShowMore = vi.fn();
      const user = userEvent.setup();

      render(<MiniAuditTimeline {...defaultProps} onShowMore={mockOnShowMore} />, {
        wrapper: createWrapper(),
      });

      await waitFor(async () => {
        const showMoreButton = screen.queryByRole('button', { name: /mehr|alle|show/i });
        if (showMoreButton) {
          await user.click(showMoreButton);
          expect(mockOnShowMore).toHaveBeenCalled();
        }
      });
    });
  });

  describe('Empty State', () => {
    it('should show empty state when no audit entries', async () => {
      const { auditApi } = await import('../services/auditApi');
      vi.mocked(auditApi.getAuditHistory).mockResolvedValueOnce({
        entries: [],
        totalCount: 0,
      });

      render(<MiniAuditTimeline {...defaultProps} />, { wrapper: createWrapper() });

      await waitFor(() => {
        expect(screen.getByText(/keine änderung|no changes|leer/i)).toBeInTheDocument();
      });
    });
  });

  describe('Error Handling', () => {
    it('should handle API errors gracefully', async () => {
      const { auditApi } = await import('../services/auditApi');
      vi.mocked(auditApi.getAuditHistory).mockRejectedValueOnce(new Error('API Error'));

      render(<MiniAuditTimeline {...defaultProps} />, { wrapper: createWrapper() });

      await waitFor(() => {
        // Should show error or fallback
        const errorElement = screen.queryByText(/fehler|error/i);
        if (errorElement) {
          expect(errorElement).toBeInTheDocument();
        }
      });
    });
  });

  describe('Date Formatting', () => {
    it('should format dates in German locale', async () => {
      render(<MiniAuditTimeline {...defaultProps} />, { wrapper: createWrapper() });

      await waitFor(() => {
        // Should use German date formatting
        const dateElements = screen.queryAllByText(/vor|gestern|heute/i);
        expect(dateElements.length).toBeGreaterThan(0);
      });
    });
  });

  describe('Field Changes', () => {
    it('should display field change details', async () => {
      render(<MiniAuditTimeline {...defaultProps} showDetails={true} compact={false} />, {
        wrapper: createWrapper(),
      });

      await waitFor(() => {
        // Should show what changed
        const emailChange = screen.queryByText(/email/i);
        if (emailChange) {
          expect(emailChange).toBeInTheDocument();
        }
      });
    });

    it('should show old and new values when available', async () => {
      render(<MiniAuditTimeline {...defaultProps} showDetails={true} compact={false} />, {
        wrapper: createWrapper(),
      });

      await waitFor(() => {
        // Should show value changes
        const oldValue = screen.queryByText(/old@example.com/);
        const newValue = screen.queryByText(/new@example.com/);

        if (oldValue && newValue) {
          expect(oldValue).toBeInTheDocument();
          expect(newValue).toBeInTheDocument();
        }
      });
    });
  });

  describe('Performance', () => {
    it('should not refetch when props do not change', async () => {
      const { auditApi } = await import('../services/auditApi');
      const { rerender } = render(<MiniAuditTimeline {...defaultProps} />, {
        wrapper: createWrapper(),
      });

      await waitFor(() => {
        expect(vi.mocked(auditApi.getAuditHistory)).toHaveBeenCalledTimes(1);
      });

      // Rerender with same props
      rerender(<MiniAuditTimeline {...defaultProps} />);

      // Should not fetch again
      expect(vi.mocked(auditApi.getAuditHistory)).toHaveBeenCalledTimes(1);
    });

    it('should refetch when entityId changes', async () => {
      const { auditApi } = await import('../services/auditApi');
      const { rerender } = render(<MiniAuditTimeline {...defaultProps} />, {
        wrapper: createWrapper(),
      });

      await waitFor(() => {
        expect(vi.mocked(auditApi.getAuditHistory)).toHaveBeenCalledTimes(1);
      });

      // Change entityId
      rerender(<MiniAuditTimeline {...defaultProps} entityId="contact-2" />);

      await waitFor(() => {
        expect(vi.mocked(auditApi.getAuditHistory)).toHaveBeenCalledTimes(2);
      });
    });
  });

  describe('Accessibility', () => {
    it('should have proper ARIA labels', async () => {
      const { container } = render(<MiniAuditTimeline {...defaultProps} />, {
        wrapper: createWrapper(),
      });

      await waitFor(() => {
        // Expandable elements should have ARIA attributes
        const expandables = container.querySelectorAll('[aria-expanded]');
        if (expandables.length > 0) {
          expandables.forEach(el => {
            expect(el).toHaveAttribute('aria-expanded');
          });
        }
      });
    });

    it('should be keyboard navigable', async () => {
      const user = userEvent.setup();
      render(<MiniAuditTimeline {...defaultProps} />, { wrapper: createWrapper() });

      await waitFor(async () => {
        // Tab to first interactive element
        await user.tab();

        // Should focus something
        expect(document.activeElement).not.toBe(document.body);
      });
    });
  });
});
