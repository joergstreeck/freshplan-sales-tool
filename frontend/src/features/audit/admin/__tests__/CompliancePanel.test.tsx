import React from 'react';
import { render, screen, fireEvent, within } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { ThemeProvider } from '@mui/material/styles';
import { CompliancePanel } from '../CompliancePanel';
import freshfoodzTheme from '@/theme/freshfoodz';

const mockComplianceData = {
  score: 92.5,
  status: 'good' as const,
  lastAudit: '2025-08-08T10:00:00Z',
  issues: [
    {
      id: 'issue-1',
      severity: 'warning' as const,
      category: 'DSGVO',
      description: 'Fehlende Rechtsgrundlage für 5 Verarbeitungen',
      affectedCount: 5,
      recommendation: 'Rechtsgrundlage dokumentieren'
    },
    {
      id: 'issue-2',
      severity: 'error' as const,
      category: 'Retention',
      description: 'Überfällige Löschung von 12 Datensätzen',
      affectedCount: 12,
      recommendation: 'Automatische Löschung aktivieren'
    }
  ],
  metrics: {
    dsgvoCompliance: 95,
    retentionCompliance: 88,
    integrityStatus: 100,
    encryptionStatus: 100,
    accessControlStatus: 92
  },
  recommendations: [
    'Implementierung automatischer Löschprozesse',
    'Verbesserung der Rechtsdokumentation',
    'Regelmäßige Compliance-Schulungen'
  ],
  nextAuditDate: '2025-09-08T10:00:00Z'
};

describe('CompliancePanel', () => {
  const renderComponent = (props = {}) => {
    const defaultProps = {
      data: mockComplianceData,
      onRefresh: vi.fn(),
      onGenerateReport: vi.fn()
    };
    
    return render(
      <ThemeProvider theme={freshfoodzTheme}>
        <CompliancePanel {...defaultProps} {...props} />
      </ThemeProvider>
    );
  };

  it('should render compliance panel with title', () => {
    renderComponent();
    
    expect(screen.getByText('Compliance Status')).toBeInTheDocument();
  });

  it('should display compliance score with correct color', () => {
    renderComponent();
    
    expect(screen.getByText('92.5%')).toBeInTheDocument();
    expect(screen.getByText('Compliance Score')).toBeInTheDocument();
    
    // Good score should show success color
    const scoreCard = screen.getByText('92.5%').closest('.MuiPaper-root');
    expect(scoreCard).toHaveStyle({ borderColor: expect.stringContaining('94C456') });
  });

  it('should show warning for low compliance score', () => {
    const lowScoreData = {
      ...mockComplianceData,
      score: 65,
      status: 'critical' as const
    };
    
    renderComponent({ data: lowScoreData });
    
    const scoreCard = screen.getByText('65.0%').closest('.MuiPaper-root');
    expect(scoreCard).toHaveStyle({ borderColor: expect.stringContaining('f44336') });
    
    expect(screen.getByText('Kritischer Compliance-Status')).toBeInTheDocument();
  });

  it('should display all compliance metrics', () => {
    renderComponent();
    
    expect(screen.getByText('DSGVO Compliance')).toBeInTheDocument();
    expect(screen.getByText('95%')).toBeInTheDocument();
    
    expect(screen.getByText('Retention Compliance')).toBeInTheDocument();
    expect(screen.getByText('88%')).toBeInTheDocument();
    
    expect(screen.getByText('Datenintegrität')).toBeInTheDocument();
    expect(screen.getByText('100%')).toBeInTheDocument();
    
    expect(screen.getByText('Verschlüsselung')).toBeInTheDocument();
    expect(screen.getByText('100%')).toBeInTheDocument();
    
    expect(screen.getByText('Zugriffskontrolle')).toBeInTheDocument();
    expect(screen.getByText('92%')).toBeInTheDocument();
  });

  it('should display compliance issues', () => {
    renderComponent();
    
    expect(screen.getByText('Compliance Issues')).toBeInTheDocument();
    expect(screen.getByText('Fehlende Rechtsgrundlage für 5 Verarbeitungen')).toBeInTheDocument();
    expect(screen.getByText('Überfällige Löschung von 12 Datensätzen')).toBeInTheDocument();
  });

  it('should show issue severity with correct icons', () => {
    renderComponent();
    
    // Warning issue
    const warningIssue = screen.getByText('Fehlende Rechtsgrundlage für 5 Verarbeitungen')
      .closest('.MuiListItem-root');
    expect(within(warningIssue!).getByTestId('WarningIcon')).toBeInTheDocument();
    
    // Error issue
    const errorIssue = screen.getByText('Überfällige Löschung von 12 Datensätzen')
      .closest('.MuiListItem-root');
    expect(within(errorIssue!).getByTestId('ErrorIcon')).toBeInTheDocument();
  });

  it('should display recommendations', () => {
    renderComponent();
    
    expect(screen.getByText('Empfehlungen')).toBeInTheDocument();
    expect(screen.getByText('Implementierung automatischer Löschprozesse')).toBeInTheDocument();
    expect(screen.getByText('Verbesserung der Rechtsdokumentation')).toBeInTheDocument();
    expect(screen.getByText('Regelmäßige Compliance-Schulungen')).toBeInTheDocument();
  });

  it('should show last audit date', () => {
    renderComponent();
    
    expect(screen.getByText('Letztes Audit')).toBeInTheDocument();
    expect(screen.getByText(/08.08.2025/)).toBeInTheDocument();
  });

  it('should show next audit date', () => {
    renderComponent();
    
    expect(screen.getByText('Nächstes Audit')).toBeInTheDocument();
    expect(screen.getByText(/08.09.2025/)).toBeInTheDocument();
  });

  it('should handle refresh button click', () => {
    const onRefresh = vi.fn();
    renderComponent({ onRefresh });
    
    const refreshButton = screen.getByRole('button', { name: /Aktualisieren/i });
    fireEvent.click(refreshButton);
    
    expect(onRefresh).toHaveBeenCalled();
  });

  it('should handle generate report button click', () => {
    const onGenerateReport = vi.fn();
    renderComponent({ onGenerateReport });
    
    const reportButton = screen.getByRole('button', { name: /Report generieren/i });
    fireEvent.click(reportButton);
    
    expect(onGenerateReport).toHaveBeenCalled();
  });

  it('should show loading state', () => {
    renderComponent({ isLoading: true, data: undefined });
    
    const skeletons = document.querySelectorAll('.MuiSkeleton-root');
    expect(skeletons.length).toBeGreaterThan(0);
  });

  it('should show empty state when no data', () => {
    renderComponent({ data: undefined });
    
    expect(screen.getByText('Keine Compliance-Daten verfügbar')).toBeInTheDocument();
  });

  it('should display progress bars for metrics', () => {
    renderComponent();
    
    const progressBars = document.querySelectorAll('.MuiLinearProgress-root');
    expect(progressBars.length).toBeGreaterThanOrEqual(5); // One for each metric
  });

  it('should color code progress bars based on value', () => {
    renderComponent();
    
    // High value (100%) should be green
    const integrityBar = screen.getByText('Datenintegrität')
      .closest('.MuiBox-root')
      ?.querySelector('.MuiLinearProgress-root');
    expect(integrityBar).toHaveStyle({ 
      '& .MuiLinearProgress-bar': { backgroundColor: expect.stringContaining('94C456') }
    });
    
    // Medium value (88%) should be warning
    const retentionBar = screen.getByText('Retention Compliance')
      .closest('.MuiBox-root')
      ?.querySelector('.MuiLinearProgress-root');
    expect(retentionBar).toBeTruthy();
  });

  it('should expand/collapse issue details', () => {
    renderComponent();
    
    const issueItem = screen.getByText('Fehlende Rechtsgrundlage für 5 Verarbeitungen')
      .closest('.MuiListItem-root');
    
    const expandButton = within(issueItem!).getByRole('button', { name: /Details/i });
    fireEvent.click(expandButton);
    
    expect(screen.getByText('Rechtsgrundlage dokumentieren')).toBeInTheDocument();
  });

  it('should show issue count badge', () => {
    renderComponent();
    
    const issuesTab = screen.getByText(/Compliance Issues.*2/);
    expect(issuesTab).toBeInTheDocument();
  });

  it('should categorize issues by severity', () => {
    renderComponent();
    
    expect(screen.getByText(/1 Warning/)).toBeInTheDocument();
    expect(screen.getByText(/1 Error/)).toBeInTheDocument();
  });

  it('should display compliance trend chart placeholder', () => {
    renderComponent();
    
    expect(screen.getByText('Compliance Trend')).toBeInTheDocument();
    // Chart would be rendered here with actual charting library
    expect(screen.getByText(/Chart Placeholder/)).toBeInTheDocument();
  });

  it('should show action buttons for issues', () => {
    renderComponent();
    
    const issueItem = screen.getByText('Überfällige Löschung von 12 Datensätzen')
      .closest('.MuiListItem-root');
    
    const fixButton = within(issueItem!).getByRole('button', { name: /Beheben/i });
    expect(fixButton).toBeInTheDocument();
    
    const ignoreButton = within(issueItem!).getByRole('button', { name: /Ignorieren/i });
    expect(ignoreButton).toBeInTheDocument();
  });

  it('should handle fix issue action', () => {
    const onFixIssue = vi.fn();
    renderComponent({ onFixIssue });
    
    const fixButton = screen.getAllByRole('button', { name: /Beheben/i })[0];
    fireEvent.click(fixButton);
    
    expect(onFixIssue).toHaveBeenCalledWith('issue-1');
  });

  it('should display certification status', () => {
    renderComponent();
    
    expect(screen.getByText('Zertifizierungen')).toBeInTheDocument();
    expect(screen.getByText('ISO 27001')).toBeInTheDocument();
    expect(screen.getByText('DSGVO')).toBeInTheDocument();
    expect(screen.getByText('SOC 2')).toBeInTheDocument();
  });

  it('should show export options', () => {
    renderComponent();
    
    const exportButton = screen.getByRole('button', { name: /Export/i });
    fireEvent.click(exportButton);
    
    expect(screen.getByText('PDF')).toBeInTheDocument();
    expect(screen.getByText('Excel')).toBeInTheDocument();
    expect(screen.getByText('CSV')).toBeInTheDocument();
  });

  it('should display audit history', () => {
    renderComponent();
    
    const historyButton = screen.getByRole('button', { name: /Audit Historie/i });
    fireEvent.click(historyButton);
    
    expect(screen.getByText('Audit Historie')).toBeInTheDocument();
    // Previous audits would be listed here
  });

  it('should show quick actions', () => {
    renderComponent();
    
    expect(screen.getByRole('button', { name: /Sofort-Audit/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Einstellungen/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Benachrichtigungen/i })).toBeInTheDocument();
  });

  it('should display risk assessment', () => {
    renderComponent();
    
    expect(screen.getByText('Risikobewertung')).toBeInTheDocument();
    expect(screen.getByText(/Niedrig|Mittel|Hoch/)).toBeInTheDocument();
  });

  it('should show compliance score trend indicator', () => {
    renderComponent();
    
    // Trend indicator (up/down arrow)
    expect(screen.getByTestId('TrendingUpIcon')).toBeInTheDocument();
    expect(screen.getByText('+2.5%')).toBeInTheDocument();
  });
});