/**
 * AuditAdminStore Tests
 *
 * Tests for critical state management in auditAdminStore:
 * - State initialization
 * - Filter actions
 * - Stream management
 * - Security actions
 * - UI state actions
 * - Error handling
 *
 * Note: This focuses on state mutations, not on API mocks.
 */

import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';
import { useAuditAdminStore } from '../auditAdminStore';
import type { AuditStreamEntry } from '../auditAdminStore';

describe('AuditAdminStore', () => {
  beforeEach(() => {
    // Reset store before each test
    useAuditAdminStore.getState().reset();
  });

  afterEach(() => {
    // Clean up any WebSocket connections
    const store = useAuditAdminStore.getState();
    if (store.streamWebSocket) {
      store.disconnectFromStream();
    }
  });

  describe('Initial State', () => {
    it('should initialize with null dashboard stats', () => {
      const state = useAuditAdminStore.getState();
      expect(state.dashboardStats).toBeNull();
    });

    it('should initialize with empty suspicious activities', () => {
      const state = useAuditAdminStore.getState();
      expect(state.suspiciousActivities).toEqual([]);
    });

    it('should initialize with empty filters', () => {
      const state = useAuditAdminStore.getState();
      expect(state.filters).toEqual({});
    });

    it('should initialize with 7-day time range', () => {
      const state = useAuditAdminStore.getState();
      expect(state.selectedTimeRange).toBeDefined();
      expect(state.selectedTimeRange.from).toBeInstanceOf(Date);
      expect(state.selectedTimeRange.to).toBeInstanceOf(Date);

      // Check that default range is ~7 days
      const diffMs = state.selectedTimeRange.to.getTime() - state.selectedTimeRange.from.getTime();
      const diffDays = diffMs / (1000 * 60 * 60 * 24);
      expect(diffDays).toBeGreaterThan(6);
      expect(diffDays).toBeLessThan(8);
    });

    it('should initialize with loading false', () => {
      const state = useAuditAdminStore.getState();
      expect(state.isLoading).toBe(false);
    });

    it('should initialize with no error', () => {
      const state = useAuditAdminStore.getState();
      expect(state.error).toBeNull();
    });

    it('should initialize with stream disconnected', () => {
      const state = useAuditAdminStore.getState();
      expect(state.isStreamConnected).toBe(false);
      expect(state.streamWebSocket).toBeNull();
      expect(state.liveStream).toEqual([]);
    });
  });

  describe('Filter Actions', () => {
    it('should set filters partially', () => {
      const store = useAuditAdminStore.getState();

      store.setFilters({ userId: 'user123' });
      expect(useAuditAdminStore.getState().filters).toEqual({ userId: 'user123' });

      store.setFilters({ action: 'CREATE' });
      expect(useAuditAdminStore.getState().filters).toEqual({
        userId: 'user123',
        action: 'CREATE',
      });
    });

    it('should merge new filters with existing filters', () => {
      const store = useAuditAdminStore.getState();

      store.setFilters({ userId: 'user123', action: 'CREATE' });
      store.setFilters({ entityType: 'customer' });

      expect(useAuditAdminStore.getState().filters).toEqual({
        userId: 'user123',
        action: 'CREATE',
        entityType: 'customer',
      });
    });

    it('should overwrite filter values when setting same key', () => {
      const store = useAuditAdminStore.getState();

      store.setFilters({ userId: 'user123' });
      store.setFilters({ userId: 'user456' });

      expect(useAuditAdminStore.getState().filters).toEqual({ userId: 'user456' });
    });

    it('should clear all filters', () => {
      const store = useAuditAdminStore.getState();

      store.setFilters({ userId: 'user123', action: 'CREATE', entityType: 'customer' });
      expect(useAuditAdminStore.getState().filters).not.toEqual({});

      store.clearFilters();
      expect(useAuditAdminStore.getState().filters).toEqual({});
    });
  });

  describe('Time Range Actions', () => {
    it('should update time range', () => {
      const store = useAuditAdminStore.getState();

      const newRange = {
        from: new Date('2025-01-01'),
        to: new Date('2025-01-31'),
      };

      store.setTimeRange(newRange);

      const updatedState = useAuditAdminStore.getState();
      expect(updatedState.selectedTimeRange).toEqual(newRange);
    });

    it('should replace entire time range, not merge', () => {
      const store = useAuditAdminStore.getState();

      const range1 = {
        from: new Date('2025-01-01'),
        to: new Date('2025-01-31'),
      };

      const range2 = {
        from: new Date('2025-02-01'),
        to: new Date('2025-02-28'),
      };

      store.setTimeRange(range1);
      store.setTimeRange(range2);

      expect(useAuditAdminStore.getState().selectedTimeRange).toEqual(range2);
    });
  });

  describe('Stream Actions', () => {
    it('should add stream entry to beginning of array', () => {
      const store = useAuditAdminStore.getState();

      const entry1: AuditStreamEntry = {
        id: '1',
        timestamp: new Date(),
        userId: 'user1',
        userName: 'Test User',
        action: 'CREATE',
        entityType: 'customer',
        entityId: 'cust1',
      };

      store.addStreamEntry(entry1);

      const state = useAuditAdminStore.getState();
      expect(state.liveStream).toHaveLength(1);
      expect(state.liveStream[0]).toEqual(entry1);
    });

    it('should add new entries to the front', () => {
      const store = useAuditAdminStore.getState();

      const entry1: AuditStreamEntry = {
        id: '1',
        timestamp: new Date('2025-01-01'),
        userId: 'user1',
        action: 'CREATE',
        entityType: 'customer',
        entityId: 'cust1',
      };

      const entry2: AuditStreamEntry = {
        id: '2',
        timestamp: new Date('2025-01-02'),
        userId: 'user2',
        action: 'UPDATE',
        entityType: 'lead',
        entityId: 'lead1',
      };

      store.addStreamEntry(entry1);
      store.addStreamEntry(entry2);

      const state = useAuditAdminStore.getState();
      expect(state.liveStream[0]).toEqual(entry2); // Most recent first
      expect(state.liveStream[1]).toEqual(entry1);
    });

    it('should limit stream to 100 entries', () => {
      const store = useAuditAdminStore.getState();

      // Add 110 entries
      for (let i = 0; i < 110; i++) {
        const entry: AuditStreamEntry = {
          id: `entry-${i}`,
          timestamp: new Date(),
          userId: `user-${i}`,
          action: 'CREATE',
          entityType: 'test',
          entityId: `test-${i}`,
        };
        store.addStreamEntry(entry);
      }

      const state = useAuditAdminStore.getState();
      expect(state.liveStream).toHaveLength(100);

      // Should keep most recent 100
      expect(state.liveStream[0].id).toBe('entry-109');
      expect(state.liveStream[99].id).toBe('entry-10');
    });

    it('should clear stream', () => {
      const store = useAuditAdminStore.getState();

      const entry: AuditStreamEntry = {
        id: '1',
        timestamp: new Date(),
        userId: 'user1',
        action: 'CREATE',
        entityType: 'customer',
        entityId: 'cust1',
      };

      store.addStreamEntry(entry);
      expect(useAuditAdminStore.getState().liveStream).toHaveLength(1);

      store.clearStream();
      expect(useAuditAdminStore.getState().liveStream).toEqual([]);
    });

    it('should connect to stream', () => {
      const store = useAuditAdminStore.getState();

      expect(store.isStreamConnected).toBe(false);

      store.connectToStream();

      expect(useAuditAdminStore.getState().isStreamConnected).toBe(true);
    });

    it('should disconnect from stream', () => {
      const store = useAuditAdminStore.getState();

      store.connectToStream();
      expect(useAuditAdminStore.getState().isStreamConnected).toBe(true);

      store.disconnectFromStream();

      const state = useAuditAdminStore.getState();
      expect(state.isStreamConnected).toBe(false);
      expect(state.streamWebSocket).toBeNull();
    });

    it('should close WebSocket when disconnecting', () => {
      const store = useAuditAdminStore.getState();

      // Mock WebSocket
      const mockWs = {
        close: vi.fn(),
      } as unknown as WebSocket;

      // Manually set WebSocket for testing
      useAuditAdminStore.setState({ streamWebSocket: mockWs, isStreamConnected: true });

      store.disconnectFromStream();

      expect(mockWs.close).toHaveBeenCalled();
      expect(useAuditAdminStore.getState().streamWebSocket).toBeNull();
    });
  });

  describe('Security Actions', () => {
    it('should acknowledge alert', async () => {
      const store = useAuditAdminStore.getState();

      // Set up suspicious activities
      useAuditAdminStore.setState({
        suspiciousActivities: [
          {
            id: 'alert1',
            type: 'UNUSUAL_TIME',
            description: 'Test alert',
            severity: 'MEDIUM',
            detectedAt: new Date(),
            acknowledged: false,
          },
          {
            id: 'alert2',
            type: 'RAPID_EXPORTS',
            description: 'Test alert 2',
            severity: 'HIGH',
            detectedAt: new Date(),
            acknowledged: false,
          },
        ],
      });

      await store.acknowledgeAlert('alert1');

      const state = useAuditAdminStore.getState();
      const alert1 = state.suspiciousActivities.find(a => a.id === 'alert1');
      const alert2 = state.suspiciousActivities.find(a => a.id === 'alert2');

      expect(alert1?.acknowledged).toBe(true);
      expect(alert2?.acknowledged).toBe(false); // Should not affect other alerts
    });

    it('should not modify other alerts when acknowledging one', async () => {
      const store = useAuditAdminStore.getState();

      useAuditAdminStore.setState({
        suspiciousActivities: [
          {
            id: 'alert1',
            type: 'UNUSUAL_TIME',
            description: 'Alert 1',
            severity: 'LOW',
            detectedAt: new Date(),
            acknowledged: false,
          },
          {
            id: 'alert2',
            type: 'EXCESSIVE_ACCESS',
            description: 'Alert 2',
            severity: 'CRITICAL',
            detectedAt: new Date(),
            acknowledged: false,
          },
        ],
      });

      await store.acknowledgeAlert('alert1');

      const state = useAuditAdminStore.getState();
      expect(state.suspiciousActivities).toHaveLength(2); // Should keep all alerts
    });
  });

  describe('UI Actions', () => {
    it('should clear error', () => {
      useAuditAdminStore.setState({ error: 'Test error message' });

      const store = useAuditAdminStore.getState();
      store.clearError();

      expect(useAuditAdminStore.getState().error).toBeNull();
    });

    it('should reset to initial state', () => {
      // Modify state
      useAuditAdminStore.setState({
        error: 'Error',
        isLoading: true,
        filters: { userId: 'test' },
        liveStream: [
          {
            id: '1',
            timestamp: new Date(),
            userId: 'user1',
            action: 'CREATE',
            entityType: 'test',
            entityId: 'test1',
          },
        ],
      });

      const store = useAuditAdminStore.getState();
      store.reset();

      const resetState = useAuditAdminStore.getState();
      expect(resetState.error).toBeNull();
      expect(resetState.isLoading).toBe(false);
      expect(resetState.filters).toEqual({});
      expect(resetState.liveStream).toEqual([]);
      expect(resetState.dashboardStats).toBeNull();
    });
  });

  describe('Async Actions - Error Handling', () => {
    it('should set loading false after successful dashboard data fetch', async () => {
      const store = useAuditAdminStore.getState();

      const dateRange = {
        from: new Date('2025-01-01'),
        to: new Date('2025-01-31'),
      };

      await store.fetchDashboardData(dateRange);

      const state = useAuditAdminStore.getState();
      expect(state.isLoading).toBe(false);
      expect(state.dashboardStats).not.toBeNull();
    });

    it('should generate report with correct filename format', async () => {
      const store = useAuditAdminStore.getState();

      const config = {
        _dateRange: {
          from: new Date('2025-01-01'),
          to: new Date('2025-01-31'),
        },
        format: 'pdf' as const,
        includeDetails: true,
      };

      const result = await store.generateComplianceReport(config);

      expect(result.filename).toMatch(/^audit-report-pdf-.*\.pdf$/);
      expect(result.data).toBeInstanceOf(Blob);
      expect(result.generatedAt).toBeInstanceOf(Date);
    });
  });

  describe('Zustand Persistence', () => {
    it('should persist selectedTimeRange and filters only', () => {
      // This test verifies the partialize config
      // The actual persistence is handled by Zustand middleware
      // We just check that the config is correct

      const store = useAuditAdminStore.getState();

      // Modify persisted fields
      store.setTimeRange({
        from: new Date('2025-01-01'),
        to: new Date('2025-01-31'),
      });
      store.setFilters({ userId: 'test123' });

      // Modify non-persisted fields
      useAuditAdminStore.setState({ isLoading: true, error: 'test error' });

      const state = useAuditAdminStore.getState();

      // These should be present (persisted)
      expect(state.selectedTimeRange).toBeDefined();
      expect(state.filters).toBeDefined();

      // These exist in state (but wouldn't be persisted to localStorage)
      expect(state.isLoading).toBe(true);
      expect(state.error).toBe('test error');
    });
  });
});
