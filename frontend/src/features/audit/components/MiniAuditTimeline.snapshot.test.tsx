/**
 * Snapshot Tests for MiniAuditTimeline Component
 *
 * Tests visual consistency of the audit timeline component.
 *
 * @module MiniAuditTimeline.snapshot.test
 * @since FC-005 PR4
 */

import { describe, it, expect, vi } from 'vitest';
import { render, act, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import React from 'react';
import { MiniAuditTimeline } from './MiniAuditTimeline';
import type { AuditEntry } from '../types/audit.types';
import { auditApi } from '../services/auditApi';

// Mock the API
vi.mock('../services/auditApi', () => ({
  auditApi: {
    getEntityAuditTrail: vi.fn(() =>
      Promise.resolve({
        entries: [
          {
            id: '1',
            entityType: 'CUSTOMER',
            entityId: '1',
            action: 'UPDATE',
            fieldName: 'status',
            oldValue: 'INACTIVE',
            newValue: 'ACTIVE',
            userId: 'user-1',
            userName: 'Max Mustermann',
            timestamp: '2024-01-15T10:30:00Z',
            details: 'Status wurde auf aktiv gesetzt',
          },
          {
            id: '2',
            entityType: 'CUSTOMER',
            entityId: '1',
            action: 'UPDATE',
            fieldName: 'revenue',
            oldValue: '500000',
            newValue: '750000',
            userId: 'user-2',
            userName: 'Anna Schmidt',
            timestamp: '2024-01-14T15:45:00Z',
            details: 'Umsatz aktualisiert',
          },
          {
            id: '3',
            entityType: 'CUSTOMER',
            entityId: '1',
            action: 'CREATE',
            userId: 'user-3',
            userName: 'Peter Weber',
            timestamp: '2024-01-10T09:00:00Z',
            details: 'Kunde angelegt',
          },
        ] as AuditEntry[],
        totalCount: 3,
        hasMore: false,
      })
    ),
    getLatestChange: vi.fn(() =>
      Promise.resolve({
        id: '1',
        entityType: 'CUSTOMER',
        entityId: '1',
        action: 'UPDATE',
        fieldName: 'status',
        oldValue: 'INACTIVE',
        newValue: 'ACTIVE',
        userId: 'user-1',
        userName: 'Max Mustermann',
        timestamp: '2024-01-15T10:30:00Z',
      } as AuditEntry)
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

// Mock date-fns for consistent snapshots
vi.mock('date-fns', () => ({
  formatDistanceToNow: () => 'vor 2 Stunden',
  format: () => '15.01.2024 10:30',
}));

// Test wrapper
const createWrapper = () => {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: { retry: false, staleTime: Infinity },
      mutations: { retry: false },
    },
  });
  const theme = createTheme();

  return ({ children }: { children: React.ReactNode }) => (
    <QueryClientProvider client={queryClient}>
      <ThemeProvider theme={theme}>{children}</ThemeProvider>
    </QueryClientProvider>
  );
};

describe('MiniAuditTimeline - Snapshot Tests', () => {
  const defaultProps = {
    entityType: 'CUSTOMER',
    entityId: '1',
  };

  beforeEach(() => {
    // Reset mocks before each test
    vi.clearAllMocks();

    // Set default mock response
    vi.mocked(auditApi.getEntityAuditTrail).mockResolvedValue({
      entries: [
        {
          id: '1',
          entityType: 'CUSTOMER',
          entityId: '1',
          action: 'UPDATE',
          fieldName: 'status',
          oldValue: 'INACTIVE',
          newValue: 'ACTIVE',
          userId: 'user-1',
          userName: 'Max Mustermann',
          timestamp: '2024-01-15T10:30:00Z',
          details: 'Status wurde auf aktiv gesetzt',
        },
      ] as AuditEntry[],
      totalCount: 1,
      hasMore: false,
    });
  });

  it('should match snapshot - compact mode (default)', async () => {
    let renderResult;

    await act(async () => {
      renderResult = render(<MiniAuditTimeline {...defaultProps} />, {
        wrapper: createWrapper(),
      });
    });

    // Wait for data to load (or empty state)
    await waitFor(() => {
      expect(renderResult.container.textContent.length).toBeGreaterThan(0);
    });

    expect(renderResult.container.firstChild).toMatchSnapshot('compact-mode');
  });

  it('should match snapshot - expanded mode', async () => {
    let renderResult;

    await act(async () => {
      renderResult = render(<MiniAuditTimeline {...defaultProps} compact={false} />, {
        wrapper: createWrapper(),
      });
    });

    await waitFor(() => {
      expect(renderResult.container.textContent.length).toBeGreaterThan(0);
    });

    expect(renderResult.container.firstChild).toMatchSnapshot('expanded-mode');
  });

  it('should match snapshot - with details', async () => {
    let renderResult;

    await act(async () => {
      renderResult = render(
        <MiniAuditTimeline {...defaultProps} showDetails={true} compact={false} />,
        { wrapper: createWrapper() }
      );
    });

    await waitFor(() => {
      expect(renderResult.container.textContent.length).toBeGreaterThan(0);
    });

    expect(renderResult.container.firstChild).toMatchSnapshot('with-details');
  });

  it('should match snapshot - limited entries', async () => {
    let renderResult;

    await act(async () => {
      renderResult = render(
        <MiniAuditTimeline {...defaultProps} maxEntries={2} compact={false} />,
        { wrapper: createWrapper() }
      );
    });

    await waitFor(() => {
      expect(renderResult.container.textContent.length).toBeGreaterThan(0);
    });

    expect(renderResult.container.firstChild).toMatchSnapshot('limited-entries');
  });

  it('should match snapshot - loading state', () => {
    // Mock loading state
    vi.mocked(auditApi.getEntityAuditTrail).mockImplementation(
      () => new Promise(() => {}) // Never resolves
    );

    const { container } = render(<MiniAuditTimeline {...defaultProps} />, {
      wrapper: createWrapper(),
    });

    expect(container.firstChild).toMatchSnapshot('loading-state');
  });

  it('should match snapshot - empty state', async () => {
    // Mock empty response
    vi.mocked(auditApi.getEntityAuditTrail).mockResolvedValueOnce({
      entries: [],
      totalCount: 0,
      hasMore: false,
    });

    const { container } = render(<MiniAuditTimeline {...defaultProps} />, {
      wrapper: createWrapper(),
    });

    await vi.waitFor(() => {
      expect(container.textContent).toContain('Keine Änderungshistorie');
    });

    expect(container.firstChild).toMatchSnapshot('empty-state');
  });

  it('should match snapshot - error state', async () => {
    // Mock error
    vi.mocked(auditApi.getEntityAuditTrail).mockRejectedValueOnce(
      new Error('Failed to load audit history')
    );

    let renderResult;

    await act(async () => {
      renderResult = render(<MiniAuditTimeline {...defaultProps} />, {
        wrapper: createWrapper(),
      });
    });

    await waitFor(() => {
      expect(renderResult.container.textContent).toContain(
        'Audit-Historie konnte nicht geladen werden'
      );
    });

    expect(renderResult.container.firstChild).toMatchSnapshot('error-state');
  });

  it('should match snapshot - with show more button', async () => {
    // Mock response with more entries available
    vi.mocked(auditApi.getEntityAuditTrail).mockResolvedValueOnce({
      entries: Array(5)
        .fill(null)
        .map((_, i) => ({
          id: `${i}`,
          entityType: 'CUSTOMER',
          entityId: '1',
          action: 'UPDATE',
          userId: 'user-1',
          userName: 'Test User',
          timestamp: new Date().toISOString(),
        })),
      totalCount: 20,
      hasMore: true,
    });

    let renderResult;

    await act(async () => {
      renderResult = render(<MiniAuditTimeline {...defaultProps} onShowMore={vi.fn()} />, {
        wrapper: createWrapper(),
      });
    });

    await waitFor(() => {
      expect(renderResult.container.textContent.length).toBeGreaterThan(0);
    });

    expect(renderResult.container.firstChild).toMatchSnapshot('with-show-more');
  });

  it('should match snapshot - different entity types', async () => {
    let renderResult;

    await act(async () => {
      renderResult = render(<MiniAuditTimeline entityType="CONTACT" entityId="contact-1" />, {
        wrapper: createWrapper(),
      });
    });

    await waitFor(() => {
      expect(renderResult.container.textContent.length).toBeGreaterThan(0);
    });

    expect(renderResult.container.firstChild).toMatchSnapshot('contact-entity');
  });

  it('should match snapshot - dark mode', async () => {
    const darkTheme = createTheme({
      palette: {
        mode: 'dark',
      },
    });

    const DarkWrapper = ({ children }: { children: React.ReactNode }) => (
      <QueryClientProvider client={new QueryClient()}>
        <ThemeProvider theme={darkTheme}>{children}</ThemeProvider>
      </QueryClientProvider>
    );

    let renderResult;

    await act(async () => {
      renderResult = render(<MiniAuditTimeline {...defaultProps} />, { wrapper: DarkWrapper });
    });

    await waitFor(() => {
      expect(renderResult.container.textContent.length).toBeGreaterThan(0);
    });

    expect(renderResult.container.firstChild).toMatchSnapshot('dark-mode');
  });

  it('should match snapshot - all action types', async () => {
    // Mock with different action types
    vi.mocked(auditApi.getEntityAuditTrail).mockResolvedValueOnce({
      entries: [
        {
          id: '1',
          entityType: 'CUSTOMER',
          entityId: '1',
          action: 'CREATE',
          userId: 'user-1',
          userName: 'User 1',
          timestamp: '2024-01-15T10:00:00Z',
        },
        {
          id: '2',
          entityType: 'CUSTOMER',
          entityId: '1',
          action: 'UPDATE',
          fieldName: 'name',
          oldValue: 'Old Name',
          newValue: 'New Name',
          userId: 'user-2',
          userName: 'User 2',
          timestamp: '2024-01-15T11:00:00Z',
        },
        {
          id: '3',
          entityType: 'CUSTOMER',
          entityId: '1',
          action: 'DELETE',
          userId: 'user-3',
          userName: 'User 3',
          timestamp: '2024-01-15T12:00:00Z',
        },
      ],
      totalCount: 3,
      hasMore: false,
    });

    let renderResult;

    await act(async () => {
      renderResult = render(
        <MiniAuditTimeline {...defaultProps} compact={false} showDetails={true} />,
        { wrapper: createWrapper() }
      );
    });

    await waitFor(() => {
      expect(renderResult.container.textContent.length).toBeGreaterThan(0);
    });

    expect(renderResult.container.firstChild).toMatchSnapshot('all-action-types');
  });
});
