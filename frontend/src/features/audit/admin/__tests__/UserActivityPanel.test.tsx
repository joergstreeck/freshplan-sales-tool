import React from 'react';
import { render, screen, fireEvent, waitFor, within } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ThemeProvider } from '@mui/material/styles';
import { UserActivityPanel } from '../UserActivityPanel';
import freshfoodzTheme from '@/theme/freshfoodz';

// Mock the API
vi.mock('../../services/auditApi', () => ({
  auditApi: {
    getUserActivities: vi.fn(),
    getUserProfile: vi.fn(),
    getUserAnomalies: vi.fn()
  }
}));

const mockUserActivities = [
  {
    userId: 'user-1',
    userName: 'Max Mustermann',
    role: 'admin',
    department: 'IT',
    totalActions: 245,
    lastActive: '2025-08-08T14:30:00Z',
    riskScore: 15,
    anomalies: 0,
    topActions: ['UPDATE', 'READ', 'CREATE'],
    activeHours: [9, 10, 11, 14, 15, 16],
    ipAddresses: ['192.168.1.100', '192.168.1.101']
  },
  {
    userId: 'user-2',
    userName: 'Anna Schmidt',
    role: 'sales',
    department: 'Vertrieb',
    totalActions: 189,
    lastActive: '2025-08-08T13:45:00Z',
    riskScore: 5,
    anomalies: 0,
    topActions: ['READ', 'UPDATE', 'EXPORT'],
    activeHours: [8, 9, 10, 11, 14, 15],
    ipAddresses: ['192.168.1.200']
  },
  {
    userId: 'user-3',
    userName: 'Peter Weber',
    role: 'manager',
    department: 'Management',
    totalActions: 567,
    lastActive: '2025-08-08T15:00:00Z',
    riskScore: 85,
    anomalies: 3,
    topActions: ['DELETE', 'BULK_UPDATE', 'EXPORT'],
    activeHours: [0, 1, 2, 22, 23], // Unusual hours
    ipAddresses: ['192.168.1.50', '10.0.0.1', '203.0.113.0'] // Multiple IPs
  }
];

const mockUserProfile = {
  userId: 'user-1',
  userName: 'Max Mustermann',
  role: 'admin',
  department: 'IT',
  email: 'max.mustermann@example.com',
  lastLogin: '2025-08-08T08:00:00Z',
  accountCreated: '2024-01-15T10:00:00Z',
  statistics: {
    totalActions: 245,
    actionsToday: 45,
    actionsThisWeek: 189,
    actionsThisMonth: 245,
    uniqueEntitiesAccessed: 78,
    dsgvoRelevantActions: 12,
    averageActionsPerDay: 35
  },
  accessPatterns: {
    mostActiveHours: [9, 10, 14, 15],
    mostActiveDays: ['Monday', 'Tuesday', 'Thursday'],
    preferredActions: ['UPDATE', 'READ'],
    typicalSessionDuration: 45 // minutes
  },
  recentActivity: [
    {
      id: 'activity-1',
      timestamp: '2025-08-08T14:30:00Z',
      action: 'UPDATE',
      entityType: 'Customer',
      entityId: 'cust-123',
      details: 'Updated customer contact information'
    },
    {
      id: 'activity-2',
      timestamp: '2025-08-08T14:15:00Z',
      action: 'READ',
      entityType: 'Order',
      entityId: 'order-456',
      details: 'Viewed order details'
    }
  ]
};

const mockAnomalies = [
  {
    id: 'anomaly-1',
    userId: 'user-3',
    type: 'UNUSUAL_TIME',
    severity: 'high',
    timestamp: '2025-08-08T02:30:00Z',
    description: 'Zugriff außerhalb der Geschäftszeiten',
    details: 'Bulk-Export um 02:30 Uhr'
  },
  {
    id: 'anomaly-2',
    userId: 'user-3',
    type: 'EXCESSIVE_EXPORTS',
    severity: 'critical',
    timestamp: '2025-08-08T03:00:00Z',
    description: 'Ungewöhnlich viele Exporte',
    details: '15 Exporte innerhalb von 10 Minuten'
  }
];

describe('UserActivityPanel', () => {
  let queryClient: QueryClient;
  
  beforeEach(() => {
    queryClient = new QueryClient({
      defaultOptions: {
        queries: { retry: false }
      }
    });
    vi.clearAllMocks();
  });

  const renderComponent = (props = {}) => {
    const defaultProps = {
      dateRange: {
        from: new Date('2025-08-01'),
        to: new Date('2025-08-08')
      }
    };
    
    return render(
      <QueryClientProvider client={queryClient}>
        <ThemeProvider theme={freshfoodzTheme}>
          <UserActivityPanel {...defaultProps} {...props} />
        </ThemeProvider>
      </QueryClientProvider>
    );
  };

  it('should render user activity panel with title', () => {
    renderComponent();
    
    expect(screen.getByText('Benutzer-Aktivitäten')).toBeInTheDocument();
  });

  it('should display user list with basic information', async () => {
    const { auditApi } = await import('../../services/auditApi');
    (auditApi.getUserActivities as any).mockResolvedValue(mockUserActivities);
    
    renderComponent();
    
    await waitFor(() => {
      expect(screen.getByText('Max Mustermann')).toBeInTheDocument();
      expect(screen.getByText('Anna Schmidt')).toBeInTheDocument();
      expect(screen.getByText('Peter Weber')).toBeInTheDocument();
    });
  });

  it('should show user roles and departments', async () => {
    const { auditApi } = await import('../../services/auditApi');
    (auditApi.getUserActivities as any).mockResolvedValue(mockUserActivities);
    
    renderComponent();
    
    await waitFor(() => {
      expect(screen.getByText('admin')).toBeInTheDocument();
      expect(screen.getByText('IT')).toBeInTheDocument();
      expect(screen.getByText('sales')).toBeInTheDocument();
      expect(screen.getByText('Vertrieb')).toBeInTheDocument();
    });
  });

  it('should display action counts', async () => {
    const { auditApi } = await import('../../services/auditApi');
    (auditApi.getUserActivities as any).mockResolvedValue(mockUserActivities);
    
    renderComponent();
    
    await waitFor(() => {
      expect(screen.getByText('245 Aktionen')).toBeInTheDocument();
      expect(screen.getByText('189 Aktionen')).toBeInTheDocument();
      expect(screen.getByText('567 Aktionen')).toBeInTheDocument();
    });
  });

  it('should show risk indicators', async () => {
    const { auditApi } = await import('../../services/auditApi');
    (auditApi.getUserActivities as any).mockResolvedValue(mockUserActivities);
    
    renderComponent();
    
    await waitFor(() => {
      // Low risk (green)
      const lowRiskUser = screen.getByText('Anna Schmidt').closest('.MuiListItem-root');
      expect(within(lowRiskUser!).getByText('5')).toBeInTheDocument();
      
      // High risk (red)
      const highRiskUser = screen.getByText('Peter Weber').closest('.MuiListItem-root');
      expect(within(highRiskUser!).getByText('85')).toBeInTheDocument();
      expect(within(highRiskUser!).getByText('3 Anomalien')).toBeInTheDocument();
    });
  });

  it('should filter users by search', async () => {
    const { auditApi } = await import('../../services/auditApi');
    (auditApi.getUserActivities as any).mockResolvedValue(mockUserActivities);
    
    renderComponent();
    
    await waitFor(() => {
      expect(screen.getByText('Max Mustermann')).toBeInTheDocument();
    });
    
    const searchInput = screen.getByPlaceholderText('Benutzer suchen...');
    fireEvent.change(searchInput, { target: { value: 'Anna' } });
    
    await waitFor(() => {
      expect(screen.getByText('Anna Schmidt')).toBeInTheDocument();
      expect(screen.queryByText('Max Mustermann')).not.toBeInTheDocument();
      expect(screen.queryByText('Peter Weber')).not.toBeInTheDocument();
    });
  });

  it('should filter users by role', async () => {
    const { auditApi } = await import('../../services/auditApi');
    (auditApi.getUserActivities as any).mockResolvedValue(mockUserActivities);
    
    renderComponent();
    
    await waitFor(() => {
      expect(screen.getByText('Max Mustermann')).toBeInTheDocument();
    });
    
    const roleFilter = screen.getByLabelText('Rolle filtern');
    fireEvent.mouseDown(roleFilter);
    
    const adminOption = screen.getByRole('option', { name: 'Admin' });
    fireEvent.click(adminOption);
    
    await waitFor(() => {
      expect(screen.getByText('Max Mustermann')).toBeInTheDocument();
      expect(screen.queryByText('Anna Schmidt')).not.toBeInTheDocument();
    });
  });

  it('should sort users by different criteria', async () => {
    const { auditApi } = await import('../../services/auditApi');
    (auditApi.getUserActivities as any).mockResolvedValue(mockUserActivities);
    
    renderComponent();
    
    await waitFor(() => {
      expect(screen.getByText('Max Mustermann')).toBeInTheDocument();
    });
    
    const sortButton = screen.getByRole('button', { name: /Sortieren/i });
    fireEvent.click(sortButton);
    
    const riskOption = screen.getByText('Nach Risiko');
    fireEvent.click(riskOption);
    
    await waitFor(() => {
      const userItems = screen.getAllByRole('listitem');
      expect(within(userItems[0]).getByText('Peter Weber')).toBeInTheDocument(); // Highest risk
    });
  });

  it('should open user detail view on click', async () => {
    const { auditApi } = await import('../../services/auditApi');
    (auditApi.getUserActivities as any).mockResolvedValue(mockUserActivities);
    (auditApi.getUserProfile as any).mockResolvedValue(mockUserProfile);
    
    renderComponent();
    
    await waitFor(() => {
      expect(screen.getByText('Max Mustermann')).toBeInTheDocument();
    });
    
    const userItem = screen.getByText('Max Mustermann').closest('.MuiListItem-root');
    fireEvent.click(userItem!);
    
    await waitFor(() => {
      expect(screen.getByText('Benutzer-Profil')).toBeInTheDocument();
      expect(screen.getByText('max.mustermann@example.com')).toBeInTheDocument();
    });
  });

  it('should display user statistics in detail view', async () => {
    const { auditApi } = await import('../../services/auditApi');
    (auditApi.getUserActivities as any).mockResolvedValue(mockUserActivities);
    (auditApi.getUserProfile as any).mockResolvedValue(mockUserProfile);
    
    renderComponent();
    
    await waitFor(() => {
      const userItem = screen.getByText('Max Mustermann');
      fireEvent.click(userItem);
    });
    
    await waitFor(() => {
      expect(screen.getByText('Aktionen heute')).toBeInTheDocument();
      expect(screen.getByText('45')).toBeInTheDocument();
      
      expect(screen.getByText('Diese Woche')).toBeInTheDocument();
      expect(screen.getByText('189')).toBeInTheDocument();
      
      expect(screen.getByText('DSGVO-relevant')).toBeInTheDocument();
      expect(screen.getByText('12')).toBeInTheDocument();
    });
  });

  it('should show access patterns', async () => {
    const { auditApi } = await import('../../services/auditApi');
    (auditApi.getUserActivities as any).mockResolvedValue(mockUserActivities);
    (auditApi.getUserProfile as any).mockResolvedValue(mockUserProfile);
    
    renderComponent();
    
    await waitFor(() => {
      const userItem = screen.getByText('Max Mustermann');
      fireEvent.click(userItem);
    });
    
    await waitFor(() => {
      expect(screen.getByText('Zugriffsmustern')).toBeInTheDocument();
      expect(screen.getByText('Aktivste Stunden')).toBeInTheDocument();
      expect(screen.getByText(/09:00, 10:00, 14:00, 15:00/)).toBeInTheDocument();
    });
  });

  it('should display recent activity timeline', async () => {
    const { auditApi } = await import('../../services/auditApi');
    (auditApi.getUserActivities as any).mockResolvedValue(mockUserActivities);
    (auditApi.getUserProfile as any).mockResolvedValue(mockUserProfile);
    
    renderComponent();
    
    await waitFor(() => {
      const userItem = screen.getByText('Max Mustermann');
      fireEvent.click(userItem);
    });
    
    await waitFor(() => {
      expect(screen.getByText('Letzte Aktivitäten')).toBeInTheDocument();
      expect(screen.getByText('Updated customer contact information')).toBeInTheDocument();
      expect(screen.getByText('Viewed order details')).toBeInTheDocument();
    });
  });

  it('should highlight anomalies for suspicious users', async () => {
    const { auditApi } = await import('../../services/auditApi');
    (auditApi.getUserActivities as any).mockResolvedValue(mockUserActivities);
    (auditApi.getUserAnomalies as any).mockResolvedValue(mockAnomalies);
    
    renderComponent();
    
    await waitFor(() => {
      const suspiciousUser = screen.getByText('Peter Weber').closest('.MuiListItem-root');
      expect(within(suspiciousUser!).getByTestId('WarningIcon')).toBeInTheDocument();
      expect(within(suspiciousUser!).getByText('3 Anomalien')).toBeInTheDocument();
    });
  });

  it('should show anomaly details', async () => {
    const { auditApi } = await import('../../services/auditApi');
    (auditApi.getUserActivities as any).mockResolvedValue(mockUserActivities);
    (auditApi.getUserAnomalies as any).mockResolvedValue(mockAnomalies);
    
    renderComponent();
    
    await waitFor(() => {
      const viewAnomaliesButton = screen.getByRole('button', { name: /Anomalien anzeigen/i });
      fireEvent.click(viewAnomaliesButton);
    });
    
    await waitFor(() => {
      expect(screen.getByText('Zugriff außerhalb der Geschäftszeiten')).toBeInTheDocument();
      expect(screen.getByText('Ungewöhnlich viele Exporte')).toBeInTheDocument();
    });
  });

  it('should show last active time', async () => {
    const { auditApi } = await import('../../services/auditApi');
    (auditApi.getUserActivities as any).mockResolvedValue(mockUserActivities);
    
    renderComponent();
    
    await waitFor(() => {
      expect(screen.getByText(/vor \d+ (Minuten|Stunden)/)).toBeInTheDocument();
    });
  });

  it('should display IP addresses used', async () => {
    const { auditApi } = await import('../../services/auditApi');
    (auditApi.getUserActivities as any).mockResolvedValue(mockUserActivities);
    (auditApi.getUserProfile as any).mockResolvedValue(mockUserProfile);
    
    renderComponent();
    
    await waitFor(() => {
      const userItem = screen.getByText('Max Mustermann');
      fireEvent.click(userItem);
    });
    
    await waitFor(() => {
      expect(screen.getByText('IP-Adressen')).toBeInTheDocument();
      expect(screen.getByText('192.168.1.100')).toBeInTheDocument();
    });
  });

  it('should allow blocking a user', async () => {
    const { auditApi } = await import('../../services/auditApi');
    (auditApi.getUserActivities as any).mockResolvedValue(mockUserActivities);
    
    renderComponent();
    
    await waitFor(() => {
      const moreButton = screen.getAllByRole('button', { name: /Mehr/i })[0];
      fireEvent.click(moreButton);
    });
    
    const blockButton = screen.getByText('Benutzer sperren');
    fireEvent.click(blockButton);
    
    expect(screen.getByText('Benutzer sperren?')).toBeInTheDocument();
  });

  it('should export user activity report', async () => {
    const { auditApi } = await import('../../services/auditApi');
    (auditApi.getUserActivities as any).mockResolvedValue(mockUserActivities);
    
    const onExport = vi.fn();
    renderComponent({ onExport });
    
    await waitFor(() => {
      const exportButton = screen.getByRole('button', { name: /Export/i });
      fireEvent.click(exportButton);
    });
    
    expect(onExport).toHaveBeenCalled();
  });

  it('should refresh user data', async () => {
    const { auditApi } = await import('../../services/auditApi');
    (auditApi.getUserActivities as any).mockResolvedValue(mockUserActivities);
    
    renderComponent();
    
    await waitFor(() => {
      expect(screen.getByText('Max Mustermann')).toBeInTheDocument();
    });
    
    const refreshButton = screen.getByRole('button', { name: /Aktualisieren/i });
    fireEvent.click(refreshButton);
    
    expect(auditApi.getUserActivities).toHaveBeenCalledTimes(2);
  });

  it('should show loading state', () => {
    renderComponent();
    
    expect(screen.getByRole('progressbar')).toBeInTheDocument();
  });

  it('should show empty state when no users', async () => {
    const { auditApi } = await import('../../services/auditApi');
    (auditApi.getUserActivities as any).mockResolvedValue([]);
    
    renderComponent();
    
    await waitFor(() => {
      expect(screen.getByText('Keine Benutzeraktivitäten im gewählten Zeitraum')).toBeInTheDocument();
    });
  });

  it('should handle error state', async () => {
    const { auditApi } = await import('../../services/auditApi');
    (auditApi.getUserActivities as any).mockRejectedValue(new Error('API Error'));
    
    renderComponent();
    
    await waitFor(() => {
      expect(screen.getByText('Fehler beim Laden der Benutzeraktivitäten')).toBeInTheDocument();
    });
  });

  it('should show activity heatmap for user', async () => {
    const { auditApi } = await import('../../services/auditApi');
    (auditApi.getUserActivities as any).mockResolvedValue(mockUserActivities);
    (auditApi.getUserProfile as any).mockResolvedValue(mockUserProfile);
    
    renderComponent();
    
    await waitFor(() => {
      const userItem = screen.getByText('Max Mustermann');
      fireEvent.click(userItem);
    });
    
    await waitFor(() => {
      expect(screen.getByText('Aktivitäts-Heatmap')).toBeInTheDocument();
      // Heatmap would be rendered here
    });
  });

  it('should compare multiple users', async () => {
    const { auditApi } = await import('../../services/auditApi');
    (auditApi.getUserActivities as any).mockResolvedValue(mockUserActivities);
    
    renderComponent();
    
    await waitFor(() => {
      expect(screen.getByText('Max Mustermann')).toBeInTheDocument();
    });
    
    // Select users for comparison
    const checkboxes = screen.getAllByRole('checkbox');
    fireEvent.click(checkboxes[0]);
    fireEvent.click(checkboxes[1]);
    
    const compareButton = screen.getByRole('button', { name: /Vergleichen/i });
    fireEvent.click(compareButton);
    
    expect(screen.getByText('Benutzervergleich')).toBeInTheDocument();
  });
});