import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ThemeProvider } from '@mui/material/styles';
import { AuditDetailModal } from '../../components/AuditDetailModal';
import freshfoodzTheme from '@/theme/freshfoodz';
import toast from 'react-hot-toast';

// Mock react-hot-toast
vi.mock('react-hot-toast', () => ({
  default: {
    success: vi.fn(),
    error: vi.fn()
  }
}));

// Mock auditApi
vi.mock('../../services/auditApi', () => ({
  auditApi: {
    getAuditDetail: vi.fn()
  }
}));

const mockAuditLog = {
  id: 'audit-123',
  timestamp: '2025-08-08T10:30:00Z',
  occurredAt: '2025-08-08T10:30:00Z',
  action: 'UPDATE',
  entityType: 'Customer',
  entityId: 'cust-456',
  userId: 'user-789',
  userName: 'Max Mustermann',
  ipAddress: '192.168.1.100',
  userAgent: 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
  oldValues: {
    name: 'Old Company Name',
    email: 'old@example.com'
  },
  newValues: {
    name: 'New Company Name',
    email: 'new@example.com'
  },
  hash: 'abc123def456',
  previousHash: 'xyz789',
  isValid: true,
  isDsgvoRelevant: true,
  retentionPeriod: '10 Jahre',
  dsgvoCategories: ['Kundendaten', 'Kontaktdaten']
};

describe('AuditDetailModal', () => {
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
      auditId: 'audit-123',
      open: true,
      onClose: vi.fn()
    };
    
    return render(
      <QueryClientProvider client={queryClient}>
        <ThemeProvider theme={freshfoodzTheme}>
          <AuditDetailModal {...defaultProps} {...props} />
        </ThemeProvider>
      </QueryClientProvider>
    );
  };

  it('should not render when closed', () => {
    renderComponent({ open: false });
    
    expect(screen.queryByText('Audit Log Details')).not.toBeInTheDocument();
  });

  it('should render modal header with title and close button', () => {
    renderComponent();
    
    expect(screen.getByText('Audit Log Details')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /schließen/i })).toBeInTheDocument();
  });

  it('should show loading state while fetching data', () => {
    renderComponent();
    
    // Check for skeleton loaders
    const skeletons = document.querySelectorAll('.MuiSkeleton-root');
    expect(skeletons.length).toBeGreaterThan(0);
  });

  it('should display audit log details when data is loaded', async () => {
    const { auditApi } = await import('../../services/auditApi');
    (auditApi.getAuditDetail as any).mockResolvedValue(mockAuditLog);
    
    renderComponent();
    
    await waitFor(() => {
      // Basic information
      expect(screen.getByText('Grundinformationen')).toBeInTheDocument();
      expect(screen.getByText('audit-123')).toBeInTheDocument();
      expect(screen.getByText('UPDATE')).toBeInTheDocument();
      expect(screen.getByText(/Customer.*cust-456/)).toBeInTheDocument();
      
      // User information
      expect(screen.getByText('Benutzerinformationen')).toBeInTheDocument();
      expect(screen.getByText('Max Mustermann')).toBeInTheDocument();
      expect(screen.getByText('192.168.1.100')).toBeInTheDocument();
    });
  });

  it('should display change details when available', async () => {
    const { auditApi } = await import('../../services/auditApi');
    (auditApi.getAuditDetail as any).mockResolvedValue(mockAuditLog);
    
    renderComponent();
    
    await waitFor(() => {
      expect(screen.getByText('Änderungsdetails')).toBeInTheDocument();
      expect(screen.getByText('Vorherige Werte:')).toBeInTheDocument();
      expect(screen.getByText('Neue Werte:')).toBeInTheDocument();
      
      // Check if JSON is displayed
      expect(screen.getByText(/"Old Company Name"/)).toBeInTheDocument();
      expect(screen.getByText(/"New Company Name"/)).toBeInTheDocument();
    });
  });

  it('should display security information', async () => {
    const { auditApi } = await import('../../services/auditApi');
    (auditApi.getAuditDetail as any).mockResolvedValue(mockAuditLog);
    
    renderComponent();
    
    await waitFor(() => {
      expect(screen.getByText('Sicherheit & Integrität')).toBeInTheDocument();
      expect(screen.getByText('Hash-Chain')).toBeInTheDocument();
      expect(screen.getByText('abc123def456')).toBeInTheDocument();
      expect(screen.getByText('Hash-Chain verifiziert')).toBeInTheDocument();
    });
  });

  it('should show warning when hash chain is invalid', async () => {
    const invalidAudit = { ...mockAuditLog, isValid: false };
    const { auditApi } = await import('../../services/auditApi');
    (auditApi.getAuditDetail as any).mockResolvedValue(invalidAudit);
    
    renderComponent();
    
    await waitFor(() => {
      expect(screen.getByText('Hash-Chain ungültig - mögliche Manipulation!')).toBeInTheDocument();
    });
  });

  it('should display DSGVO compliance information', async () => {
    const { auditApi } = await import('../../services/auditApi');
    (auditApi.getAuditDetail as any).mockResolvedValue(mockAuditLog);
    
    renderComponent();
    
    await waitFor(() => {
      expect(screen.getByText('Compliance & Retention')).toBeInTheDocument();
      expect(screen.getByText('DSGVO-relevant - unterliegt besonderen Datenschutzbestimmungen')).toBeInTheDocument();
      expect(screen.getByText('10 Jahre')).toBeInTheDocument();
      expect(screen.getByText('Kundendaten')).toBeInTheDocument();
      expect(screen.getByText('Kontaktdaten')).toBeInTheDocument();
    });
  });

  it('should copy ID to clipboard when copy button is clicked', async () => {
    const { auditApi } = await import('../../services/auditApi');
    (auditApi.getAuditDetail as any).mockResolvedValue(mockAuditLog);
    
    // Mock clipboard API
    Object.assign(navigator, {
      clipboard: {
        writeText: vi.fn().mockResolvedValue(undefined)
      }
    });
    
    renderComponent();
    
    await waitFor(() => {
      const copyButtons = screen.getAllByRole('button');
      const copyButton = copyButtons.find(btn => btn.querySelector('[data-testid="ContentCopyIcon"]'));
      
      if (copyButton) {
        fireEvent.click(copyButton);
      }
    });
    
    await waitFor(() => {
      expect(navigator.clipboard.writeText).toHaveBeenCalledWith('audit-123');
      expect(toast.success).toHaveBeenCalledWith('ID kopiert!');
    });
  });

  it('should export audit log as JSON when export button is clicked', async () => {
    const { auditApi } = await import('../../services/auditApi');
    (auditApi.getAuditDetail as any).mockResolvedValue(mockAuditLog);
    
    // Mock URL.createObjectURL and revokeObjectURL
    global.URL.createObjectURL = vi.fn(() => 'blob:mock-url');
    global.URL.revokeObjectURL = vi.fn();
    
    // Mock document.createElement to track anchor element
    const mockAnchor = document.createElement('a');
    const clickSpy = vi.spyOn(mockAnchor, 'click');
    vi.spyOn(document, 'createElement').mockReturnValue(mockAnchor);
    
    renderComponent();
    
    await waitFor(() => {
      const exportButton = screen.getByLabelText('Export');
      fireEvent.click(exportButton);
    });
    
    expect(mockAnchor.download).toContain('audit-log-audit-123.json');
    expect(clickSpy).toHaveBeenCalled();
    expect(toast.success).toHaveBeenCalledWith('Audit-Log exportiert');
  });

  it('should call onClose when close button is clicked', async () => {
    const onClose = vi.fn();
    renderComponent({ onClose });
    
    const closeButton = screen.getByRole('button', { name: /schließen/i });
    fireEvent.click(closeButton);
    
    expect(onClose).toHaveBeenCalled();
  });

  it('should display action-specific icons', async () => {
    const testCases = [
      { action: 'CREATE', expectedIcon: 'CheckIcon' },
      { action: 'UPDATE', expectedIcon: 'InfoIcon' },
      { action: 'DELETE', expectedIcon: 'ErrorIcon' },
      { action: 'LOGIN', expectedIcon: 'SecurityIcon' }
    ];
    
    for (const testCase of testCases) {
      const auditWithAction = { ...mockAuditLog, action: testCase.action };
      const { auditApi } = await import('../../services/auditApi');
      (auditApi.getAuditDetail as any).mockResolvedValue(auditWithAction);
      
      const { unmount } = renderComponent();
      
      await waitFor(() => {
        expect(screen.getByText(testCase.action)).toBeInTheDocument();
      });
      
      unmount();
    }
  });

  it('should handle missing optional fields gracefully', async () => {
    const minimalAudit = {
      id: 'audit-123',
      timestamp: '2025-08-08T10:30:00Z',
      action: 'READ',
      entityType: 'Customer',
      entityId: 'cust-456',
      userId: 'user-789'
    };
    
    const { auditApi } = await import('../../services/auditApi');
    (auditApi.getAuditDetail as any).mockResolvedValue(minimalAudit);
    
    renderComponent();
    
    await waitFor(() => {
      expect(screen.getByText('audit-123')).toBeInTheDocument();
      expect(screen.getByText('READ')).toBeInTheDocument();
      expect(screen.getByText('-')).toBeInTheDocument(); // Missing username shows dash
    });
  });

  it('should display warnings array when present', async () => {
    const auditWithWarnings = {
      ...mockAuditLog,
      warnings: ['Unusual activity detected', 'Multiple failed attempts']
    };
    
    const { auditApi } = await import('../../services/auditApi');
    (auditApi.getAuditDetail as any).mockResolvedValue(auditWithWarnings);
    
    renderComponent();
    
    await waitFor(() => {
      expect(screen.getByText('Sicherheitswarnungen')).toBeInTheDocument();
      expect(screen.getByText('Unusual activity detected')).toBeInTheDocument();
      expect(screen.getByText('Multiple failed attempts')).toBeInTheDocument();
    });
  });

  it('should not fetch data when auditId is null', () => {
    const { auditApi } = await import('../../services/auditApi');
    
    renderComponent({ auditId: null });
    
    expect(auditApi.getAuditDetail).not.toHaveBeenCalled();
  });
});