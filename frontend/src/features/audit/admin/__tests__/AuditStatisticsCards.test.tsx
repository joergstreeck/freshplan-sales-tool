import React from 'react';
import { render, screen, within } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import { ThemeProvider } from '@mui/material/styles';
import { AuditStatisticsCards } from '../AuditStatisticsCards';
import freshfoodzTheme from '@/theme/freshfoodz';

const mockStats = {
  totalEvents: 15234,
  criticalEvents: 5,
  dsgvoRelevantEvents: 456,
  activeUsers: 89,
  complianceScore: 92.5,
  openSecurityAlerts: 0,
  integrityValid: true,
  averageResponseTime: 145,
  eventsByType: {
    CREATE: 4567,
    UPDATE: 6789,
    DELETE: 234,
    READ: 3456,
    LOGIN: 234,
    EXPORT: 34
  }
};

const mockDateRange = {
  from: new Date('2025-08-01'),
  to: new Date('2025-08-08')
};

describe('AuditStatisticsCards', () => {
  const renderComponent = (props = {}) => {
    return render(
      <ThemeProvider theme={freshfoodzTheme}>
        <AuditStatisticsCards {...props} />
      </ThemeProvider>
    );
  };

  it('should render loading state when isLoading is true', () => {
    renderComponent({ isLoading: true });
    
    // Should show skeleton loaders
    const skeletons = document.querySelectorAll('.MuiSkeleton-root');
    expect(skeletons.length).toBeGreaterThan(0);
  });

  it('should render empty state when no stats provided', () => {
    renderComponent({ stats: undefined });
    
    expect(screen.getByText('Keine Statistikdaten verfügbar')).toBeInTheDocument();
  });

  it('should render all statistics cards with correct values', () => {
    renderComponent({ stats: mockStats, dateRange: mockDateRange });
    
    // Check main statistics
    expect(screen.getByText('Gesamt-Events')).toBeInTheDocument();
    expect(screen.getByText('15K')).toBeInTheDocument(); // Formatted number
    
    expect(screen.getByText('Kritische Events')).toBeInTheDocument();
    expect(screen.getByText('5')).toBeInTheDocument();
    
    expect(screen.getByText('DSGVO-Relevant')).toBeInTheDocument();
    expect(screen.getByText('456')).toBeInTheDocument();
    
    expect(screen.getByText('Aktive Benutzer')).toBeInTheDocument();
    expect(screen.getByText('89')).toBeInTheDocument();
    
    expect(screen.getByText('Compliance Score')).toBeInTheDocument();
    expect(screen.getByText('92.5%')).toBeInTheDocument();
  });

  it('should show correct severity colors for critical events', () => {
    const statsWithHighCritical = {
      ...mockStats,
      criticalEvents: 15
    };
    
    renderComponent({ stats: statsWithHighCritical });
    
    // Should show error color for high critical events
    const criticalCard = screen.getByText('Kritische Events').closest('.MuiPaper-root');
    expect(criticalCard).toBeTruthy();
  });

  it('should show success indicator when no security alerts', () => {
    renderComponent({ stats: mockStats });
    
    expect(screen.getByText('Sicherheits-Alerts')).toBeInTheDocument();
    expect(screen.getByText('0')).toBeInTheDocument();
    expect(screen.getByText('Keine offenen Alerts')).toBeInTheDocument();
  });

  it('should show error indicator when integrity is compromised', () => {
    const statsWithInvalidIntegrity = {
      ...mockStats,
      integrityValid: false
    };
    
    renderComponent({ stats: statsWithInvalidIntegrity });
    
    expect(screen.getByText('Hash-Chain Status')).toBeInTheDocument();
    expect(screen.getByText('Kompromittiert')).toBeInTheDocument();
    expect(screen.getByText('MANIPULATION ERKANNT!')).toBeInTheDocument();
  });

  it('should display compliance score with correct color coding', () => {
    // Test different compliance scores
    const testCases = [
      { score: 95, expectedColor: 'success' },
      { score: 75, expectedColor: 'warning' },
      { score: 60, expectedColor: 'error' }
    ];
    
    testCases.forEach(({ score }) => {
      const { unmount } = renderComponent({ 
        stats: { ...mockStats, complianceScore: score }
      });
      
      expect(screen.getByText(`${score.toFixed(1)}%`)).toBeInTheDocument();
      
      unmount();
    });
  });

  it('should render event distribution when eventsByType is provided', () => {
    renderComponent({ stats: mockStats });
    
    expect(screen.getByText('Event-Verteilung nach Typ')).toBeInTheDocument();
    
    // Check event types
    Object.keys(mockStats.eventsByType).forEach(type => {
      expect(screen.getByText(type)).toBeInTheDocument();
    });
  });

  it('should format large numbers correctly', () => {
    const statsWithLargeNumbers = {
      ...mockStats,
      totalEvents: 1234567,
      activeUsers: 1234
    };
    
    renderComponent({ stats: statsWithLargeNumbers });
    
    // Should format as 1.2M and 1.2K
    expect(screen.getByText('1.2M')).toBeInTheDocument();
    expect(screen.getByText('1.2K')).toBeInTheDocument();
  });

  it('should display date range in subtitle when provided', () => {
    renderComponent({ stats: mockStats, dateRange: mockDateRange });
    
    // Should show formatted date range
    expect(screen.getByText(/01\.08\. - 08\.08\.2025/)).toBeInTheDocument();
  });

  it('should show performance warning for slow response times', () => {
    const statsWithSlowResponse = {
      ...mockStats,
      averageResponseTime: 350
    };
    
    renderComponent({ stats: statsWithSlowResponse });
    
    expect(screen.getByText('Ø Response Time')).toBeInTheDocument();
    expect(screen.getByText('350ms')).toBeInTheDocument();
  });

  it('should display action required chip for critical values', () => {
    const criticalStats = {
      ...mockStats,
      criticalEvents: 20,
      openSecurityAlerts: 5,
      complianceScore: 65
    };
    
    renderComponent({ stats: criticalStats });
    
    // Should show multiple "Aktion erforderlich" chips
    const actionChips = screen.getAllByText('Aktion erforderlich');
    expect(actionChips.length).toBeGreaterThan(0);
  });

  it('should handle missing optional fields gracefully', () => {
    const minimalStats = {
      totalEvents: 100,
      criticalEvents: 0,
      dsgvoRelevantEvents: 10,
      activeUsers: 5,
      complianceScore: 90,
      openSecurityAlerts: 0,
      integrityValid: true
    };
    
    renderComponent({ stats: minimalStats });
    
    // Should render without errors
    expect(screen.getByText('Gesamt-Events')).toBeInTheDocument();
    expect(screen.getByText('100')).toBeInTheDocument();
  });
});