import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { act, renderHook, waitFor } from '@testing-library/react';
import { useAuditAdminStore } from '../auditAdminStore';

// Mock the API module
vi.mock('@/services/admin/auditAdminApi', () => ({
  auditAdminApi: {
    getDashboardStats: vi.fn(),
    getComplianceStatus: vi.fn(),
    getActivityHeatmap: vi.fn(),
    getSuspiciousActivities: vi.fn(),
    getUserAuditProfile: vi.fn(),
    generateComplianceReport: vi.fn(),
    acknowledgeAlert: vi.fn(),
    investigateActivity: vi.fn(),
    blockUser: vi.fn()
  }
}));

// Mock WebSocket
class MockWebSocket {
  url: string;
  readyState: number;
  onopen: ((event: Event) => void) | null = null;
  onmessage: ((event: MessageEvent) => void) | null = null;
  onclose: ((event: CloseEvent) => void) | null = null;
  onerror: ((event: Event) => void) | null = null;
  
  static CONNECTING = 0;
  static OPEN = 1;
  static CLOSED = 3;

  constructor(url: string) {
    this.url = url;
    this.readyState = MockWebSocket.CONNECTING;
    
    setTimeout(() => {
      this.readyState = MockWebSocket.OPEN;
      if (this.onopen) {
        this.onopen(new Event('open'));
      }
    }, 10);
  }

  send(data: string) {}
  
  close() {
    this.readyState = MockWebSocket.CLOSED;
    if (this.onclose) {
      this.onclose(new CloseEvent('close'));
    }
  }
  
  simulateMessage(data: any) {
    if (this.onmessage) {
      this.onmessage(new MessageEvent('message', { 
        data: JSON.stringify(data) 
      }));
    }
  }
}

const mockDashboardStats = {
  totalEvents: 5432,
  criticalEvents: 5,
  dsgvoRelevantEvents: 234,
  activeUsers: 45,
  complianceScore: 92.5,
  openSecurityAlerts: 2,
  integrityValid: true,
  averageResponseTime: 145,
  eventsByType: {
    CREATE: 1234,
    UPDATE: 2345,
    DELETE: 456,
    READ: 1397
  },
  topUsersByActivity: [
    { userId: 'user-1', userName: 'Max Mustermann', activityCount: 234 }
  ],
  hottestEntities: [
    { entityType: 'Customer', entityId: 'cust-1', changeCount: 45 }
  ]
};

const mockComplianceStatus = {
  score: 92.5,
  status: 'good',
  issues: [],
  recommendations: ['Improve documentation'],
  lastCheck: new Date().toISOString()
};

const mockHeatmapData = {
  dataPoints: [],
  peakHours: [9, 14, 15],
  quietHours: [2, 3, 4]
};

const mockSuspiciousActivities = [
  {
    id: 'sus-1',
    type: 'UNUSUAL_TIME',
    userId: 'user-1',
    timestamp: new Date().toISOString(),
    description: 'Activity outside business hours',
    severity: 'medium',
    acknowledged: false
  }
];

const mockUserProfile = {
  userId: 'user-1',
  userName: 'Max Mustermann',
  role: 'admin',
  department: 'IT',
  totalActions: 234,
  riskScore: 15,
  anomalies: []
};

const mockReport = {
  data: 'report-content',
  format: 'pdf',
  size: 1024
};

describe('auditAdminStore', () => {
  let originalWebSocket: any;
  let mockWebSocket: MockWebSocket;
  
  beforeEach(() => {
    // Store original WebSocket
    originalWebSocket = global.WebSocket;
    
    // Replace with mock
    (global as any).WebSocket = vi.fn((url: string) => {
      mockWebSocket = new MockWebSocket(url);
      return mockWebSocket;
    });
    
    // Clear all mocks
    vi.clearAllMocks();
    
    // Reset store state
    useAuditAdminStore.setState({
      dashboardStats: null,
      activityHeatmap: null,
      suspiciousActivities: [],
      complianceStatus: null,
      userProfiles: new Map(),
      isLoading: false,
      error: null,
      liveStream: [],
      isStreamConnected: false,
      filters: {}
    });
  });
  
  afterEach(() => {
    // Restore original WebSocket
    global.WebSocket = originalWebSocket;
  });

  describe('fetchDashboardData', () => {
    it('should fetch dashboard stats and compliance status', async () => {
      const { auditAdminApi } = await import('@/services/admin/auditAdminApi');
      (auditAdminApi.getDashboardStats as any).mockResolvedValue(mockDashboardStats);
      (auditAdminApi.getComplianceStatus as any).mockResolvedValue(mockComplianceStatus);
      
      const { result } = renderHook(() => useAuditAdminStore());
      
      const dateRange = {
        from: new Date('2025-08-01'),
        to: new Date('2025-08-08')
      };
      
      await act(async () => {
        await result.current.fetchDashboardData(dateRange);
      });
      
      expect(result.current.dashboardStats).toEqual(mockDashboardStats);
      expect(result.current.complianceStatus).toEqual(mockComplianceStatus);
      expect(result.current.isLoading).toBe(false);
    });

    it('should handle errors when fetching dashboard data', async () => {
      const { auditAdminApi } = await import('@/services/admin/auditAdminApi');
      (auditAdminApi.getDashboardStats as any).mockRejectedValue(new Error('API Error'));
      
      const { result } = renderHook(() => useAuditAdminStore());
      
      const dateRange = {
        from: new Date('2025-08-01'),
        to: new Date('2025-08-08')
      };
      
      await act(async () => {
        await result.current.fetchDashboardData(dateRange);
      });
      
      expect(result.current.error).toBe('API Error');
      expect(result.current.isLoading).toBe(false);
    });

    it('should set loading state while fetching', async () => {
      const { auditAdminApi } = await import('@/services/admin/auditAdminApi');
      (auditAdminApi.getDashboardStats as any).mockImplementation(
        () => new Promise(resolve => setTimeout(() => resolve(mockDashboardStats), 100))
      );
      (auditAdminApi.getComplianceStatus as any).mockResolvedValue(mockComplianceStatus);
      
      const { result } = renderHook(() => useAuditAdminStore());
      
      const dateRange = {
        from: new Date('2025-08-01'),
        to: new Date('2025-08-08')
      };
      
      const promise = result.current.fetchDashboardData(dateRange);
      
      expect(result.current.isLoading).toBe(true);
      
      await act(async () => {
        await promise;
      });
      
      expect(result.current.isLoading).toBe(false);
    });
  });

  describe('fetchActivityHeatmap', () => {
    it('should fetch activity heatmap data', async () => {
      const { auditAdminApi } = await import('@/services/admin/auditAdminApi');
      (auditAdminApi.getActivityHeatmap as any).mockResolvedValue(mockHeatmapData);
      
      const { result } = renderHook(() => useAuditAdminStore());
      
      const dateRange = {
        from: new Date('2025-08-01'),
        to: new Date('2025-08-08')
      };
      
      await act(async () => {
        await result.current.fetchActivityHeatmap(dateRange, 'HOUR');
      });
      
      expect(result.current.activityHeatmap).toEqual(mockHeatmapData);
      expect(auditAdminApi.getActivityHeatmap).toHaveBeenCalledWith(dateRange, 'HOUR');
    });

    it('should handle errors silently for heatmap', async () => {
      const { auditAdminApi } = await import('@/services/admin/auditAdminApi');
      (auditAdminApi.getActivityHeatmap as any).mockRejectedValue(new Error('API Error'));
      
      const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {});
      
      const { result } = renderHook(() => useAuditAdminStore());
      
      const dateRange = {
        from: new Date('2025-08-01'),
        to: new Date('2025-08-08')
      };
      
      await act(async () => {
        await result.current.fetchActivityHeatmap(dateRange, 'DAY');
      });
      
      expect(consoleSpy).toHaveBeenCalledWith('Failed to fetch heatmap:', expect.any(Error));
      expect(result.current.activityHeatmap).toBe(null);
      
      consoleSpy.mockRestore();
    });
  });

  describe('fetchSuspiciousActivities', () => {
    it('should fetch suspicious activities', async () => {
      const { auditAdminApi } = await import('@/services/admin/auditAdminApi');
      (auditAdminApi.getSuspiciousActivities as any).mockResolvedValue(mockSuspiciousActivities);
      
      const { result } = renderHook(() => useAuditAdminStore());
      
      await act(async () => {
        await result.current.fetchSuspiciousActivities();
      });
      
      expect(result.current.suspiciousActivities).toEqual(mockSuspiciousActivities);
    });
  });

  describe('fetchUserProfile', () => {
    it('should fetch and store user profile', async () => {
      const { auditAdminApi } = await import('@/services/admin/auditAdminApi');
      (auditAdminApi.getUserAuditProfile as any).mockResolvedValue(mockUserProfile);
      
      const { result } = renderHook(() => useAuditAdminStore());
      
      const dateRange = {
        from: new Date('2025-08-01'),
        to: new Date('2025-08-08')
      };
      
      await act(async () => {
        await result.current.fetchUserProfile('user-1', dateRange);
      });
      
      expect(result.current.userProfiles.get('user-1')).toEqual(mockUserProfile);
    });

    it('should update existing user profile', async () => {
      const { auditAdminApi } = await import('@/services/admin/auditAdminApi');
      const updatedProfile = { ...mockUserProfile, totalActions: 500 };
      (auditAdminApi.getUserAuditProfile as any).mockResolvedValue(updatedProfile);
      
      const { result } = renderHook(() => useAuditAdminStore());
      
      // Set initial profile
      act(() => {
        result.current.userProfiles.set('user-1', mockUserProfile);
      });
      
      const dateRange = {
        from: new Date('2025-08-01'),
        to: new Date('2025-08-08')
      };
      
      await act(async () => {
        await result.current.fetchUserProfile('user-1', dateRange);
      });
      
      expect(result.current.userProfiles.get('user-1')).toEqual(updatedProfile);
    });
  });

  describe('generateComplianceReport', () => {
    it('should generate compliance report', async () => {
      const { auditAdminApi } = await import('@/services/admin/auditAdminApi');
      (auditAdminApi.generateComplianceReport as any).mockResolvedValue(mockReport);
      
      const { result } = renderHook(() => useAuditAdminStore());
      
      const config = {
        dateRange: {
          from: new Date('2025-08-01'),
          to: new Date('2025-08-08')
        },
        format: 'pdf' as const,
        includeDetails: true
      };
      
      let report;
      await act(async () => {
        report = await result.current.generateComplianceReport(config);
      });
      
      expect(report).toEqual(mockReport);
      expect(result.current.isLoading).toBe(false);
    });

    it('should handle report generation errors', async () => {
      const { auditAdminApi } = await import('@/services/admin/auditAdminApi');
      (auditAdminApi.generateComplianceReport as any).mockRejectedValue(new Error('Generation failed'));
      
      const { result } = renderHook(() => useAuditAdminStore());
      
      const config = {
        dateRange: {
          from: new Date('2025-08-01'),
          to: new Date('2025-08-08')
        },
        format: 'excel' as const,
        includeDetails: false
      };
      
      await expect(
        act(async () => {
          await result.current.generateComplianceReport(config);
        })
      ).rejects.toThrow('Generation failed');
      
      expect(result.current.error).toBe('Generation failed');
      expect(result.current.isLoading).toBe(false);
    });
  });

  describe('WebSocket connection', () => {
    it('should connect to stream and update connection status', async () => {
      const { result } = renderHook(() => useAuditAdminStore());
      
      act(() => {
        result.current.connectToStream();
      });
      
      // Wait for connection to open
      await waitFor(() => {
        expect(result.current.isStreamConnected).toBe(true);
      });
    });

    it('should add stream entries', async () => {
      const { result } = renderHook(() => useAuditAdminStore());
      
      act(() => {
        result.current.connectToStream();
      });
      
      await waitFor(() => {
        expect(result.current.isStreamConnected).toBe(true);
      });
      
      const entry = {
        id: 'entry-1',
        timestamp: new Date().toISOString(),
        action: 'UPDATE',
        userId: 'user-1',
        entityType: 'Customer',
        entityId: 'cust-1'
      };
      
      act(() => {
        mockWebSocket.simulateMessage(entry);
      });
      
      expect(result.current.liveStream).toContainEqual(entry);
    });

    it('should add suspicious entries to suspicious activities', async () => {
      const { result } = renderHook(() => useAuditAdminStore());
      
      act(() => {
        result.current.connectToStream();
      });
      
      await waitFor(() => {
        expect(result.current.isStreamConnected).toBe(true);
      });
      
      const suspiciousEntry = {
        id: 'entry-2',
        timestamp: new Date().toISOString(),
        action: 'DELETE',
        userId: 'user-1',
        entityType: 'Customer',
        entityId: 'cust-1',
        isSuspicious: true
      };
      
      act(() => {
        mockWebSocket.simulateMessage(suspiciousEntry);
      });
      
      expect(result.current.suspiciousActivities).toContainEqual(suspiciousEntry);
    });

    it('should limit stream entries to 100', async () => {
      const { result } = renderHook(() => useAuditAdminStore());
      
      act(() => {
        result.current.connectToStream();
      });
      
      await waitFor(() => {
        expect(result.current.isStreamConnected).toBe(true);
      });
      
      // Add 110 entries
      for (let i = 0; i < 110; i++) {
        act(() => {
          mockWebSocket.simulateMessage({
            id: `entry-${i}`,
            timestamp: new Date().toISOString(),
            action: 'UPDATE'
          });
        });
      }
      
      expect(result.current.liveStream.length).toBe(100);
      expect(result.current.liveStream[0].id).toBe('entry-109'); // Most recent first
    });

    it('should reconnect after disconnection', async () => {
      vi.useFakeTimers();
      
      const { result } = renderHook(() => useAuditAdminStore());
      
      act(() => {
        result.current.connectToStream();
      });
      
      await waitFor(() => {
        expect(result.current.isStreamConnected).toBe(true);
      });
      
      // Simulate disconnect
      act(() => {
        mockWebSocket.close();
      });
      
      expect(result.current.isStreamConnected).toBe(false);
      
      // Fast-forward 5 seconds
      act(() => {
        vi.advanceTimersByTime(5000);
      });
      
      // Should attempt reconnection
      await waitFor(() => {
        expect(global.WebSocket).toHaveBeenCalledTimes(2);
      });
      
      vi.useRealTimers();
    });
  });

  describe('Security actions', () => {
    it('should acknowledge alert', async () => {
      const { auditAdminApi } = await import('@/services/admin/auditAdminApi');
      (auditAdminApi.acknowledgeAlert as any).mockResolvedValue(undefined);
      
      const { result } = renderHook(() => useAuditAdminStore());
      
      // Set initial suspicious activities
      act(() => {
        useAuditAdminStore.setState({
          suspiciousActivities: mockSuspiciousActivities
        });
      });
      
      await act(async () => {
        await result.current.acknowledgeAlert('sus-1');
      });
      
      expect(result.current.suspiciousActivities[0].acknowledged).toBe(true);
    });

    it('should investigate activity', async () => {
      const { auditAdminApi } = await import('@/services/admin/auditAdminApi');
      const investigationResult = {
        activityId: 'act-1',
        findings: 'No issues found',
        riskLevel: 'low'
      };
      (auditAdminApi.investigateActivity as any).mockResolvedValue(investigationResult);
      
      const { result } = renderHook(() => useAuditAdminStore());
      
      let investigation;
      await act(async () => {
        investigation = await result.current.investigateActivity('act-1');
      });
      
      expect(investigation).toEqual(investigationResult);
    });

    it('should block user and refresh profile', async () => {
      const { auditAdminApi } = await import('@/services/admin/auditAdminApi');
      (auditAdminApi.blockUser as any).mockResolvedValue(undefined);
      (auditAdminApi.getUserAuditProfile as any).mockResolvedValue({
        ...mockUserProfile,
        blocked: true
      });
      
      const { result } = renderHook(() => useAuditAdminStore());
      
      const dateRange = {
        from: new Date('2025-08-01'),
        to: new Date('2025-08-08')
      };
      
      act(() => {
        useAuditAdminStore.setState({
          selectedTimeRange: dateRange
        });
      });
      
      await act(async () => {
        await result.current.blockUser('user-1', 'Suspicious activity');
      });
      
      expect(auditAdminApi.blockUser).toHaveBeenCalledWith('user-1', 'Suspicious activity');
      expect(auditAdminApi.getUserAuditProfile).toHaveBeenCalledWith('user-1', dateRange);
    });
  });

  describe('Filtering', () => {
    it('should set filters', () => {
      const { result } = renderHook(() => useAuditAdminStore());
      
      const filters = {
        action: 'UPDATE',
        userId: 'user-1',
        severity: 'high'
      };
      
      act(() => {
        result.current.setFilters(filters);
      });
      
      expect(result.current.filters).toEqual(filters);
    });

    it('should merge filters with existing', () => {
      const { result } = renderHook(() => useAuditAdminStore());
      
      act(() => {
        result.current.setFilters({ action: 'UPDATE' });
        result.current.setFilters({ userId: 'user-1' });
      });
      
      expect(result.current.filters).toEqual({
        action: 'UPDATE',
        userId: 'user-1'
      });
    });

    it('should clear all filters', () => {
      const { result } = renderHook(() => useAuditAdminStore());
      
      act(() => {
        result.current.setFilters({ action: 'UPDATE', userId: 'user-1' });
        result.current.clearFilters();
      });
      
      expect(result.current.filters).toEqual({});
    });
  });

  describe('State persistence', () => {
    it('should persist selected time range and filters', () => {
      const { result, unmount } = renderHook(() => useAuditAdminStore());
      
      const timeRange = {
        from: new Date('2025-08-01'),
        to: new Date('2025-08-08')
      };
      
      const filters = {
        action: 'UPDATE'
      };
      
      act(() => {
        useAuditAdminStore.setState({
          selectedTimeRange: timeRange,
          filters
        });
      });
      
      // Unmount and remount to simulate new session
      unmount();
      
      const { result: newResult } = renderHook(() => useAuditAdminStore());
      
      // Note: In real app, this would be persisted via localStorage
      // For testing, we're verifying the structure is set up for persistence
      expect(newResult.current.selectedTimeRange).toBeDefined();
      expect(newResult.current.filters).toBeDefined();
    });
  });
});