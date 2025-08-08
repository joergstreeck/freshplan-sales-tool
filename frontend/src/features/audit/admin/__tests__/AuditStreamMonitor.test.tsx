import React from 'react';
import { render, screen, fireEvent, waitFor, act } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { ThemeProvider } from '@mui/material/styles';
import { AuditStreamMonitor } from '../AuditStreamMonitor';
import freshfoodzTheme from '@/theme/freshfoodz';

// Mock the WebSocket
class MockWebSocket {
  url: string;
  readyState: number;
  onopen: ((event: Event) => void) | null = null;
  onmessage: ((event: MessageEvent) => void) | null = null;
  onclose: ((event: CloseEvent) => void) | null = null;
  onerror: ((event: Event) => void) | null = null;

  constructor(url: string) {
    this.url = url;
    this.readyState = WebSocket.CONNECTING;
    
    // Simulate connection open
    setTimeout(() => {
      this.readyState = WebSocket.OPEN;
      if (this.onopen) {
        this.onopen(new Event('open'));
      }
    }, 10);
  }

  send(data: string) {
    // Mock send
  }

  close() {
    this.readyState = WebSocket.CLOSED;
    if (this.onclose) {
      this.onclose(new CloseEvent('close'));
    }
  }

  simulateMessage(data: any) {
    if (this.onmessage) {
      this.onmessage(new MessageEvent('message', { data: JSON.stringify(data) }));
    }
  }
}

// Store original WebSocket
const OriginalWebSocket = global.WebSocket;

describe('AuditStreamMonitor', () => {
  let mockWebSocket: MockWebSocket;
  
  beforeEach(() => {
    // Replace global WebSocket with mock
    (global as any).WebSocket = vi.fn((url: string) => {
      mockWebSocket = new MockWebSocket(url);
      return mockWebSocket;
    });
    
    vi.useFakeTimers();
  });
  
  afterEach(() => {
    // Restore original WebSocket
    global.WebSocket = OriginalWebSocket;
    vi.clearAllTimers();
    vi.useRealTimers();
  });

  const renderComponent = (props = {}) => {
    const defaultProps = {
      maxEntries: 10,
      autoScroll: true
    };
    
    return render(
      <ThemeProvider theme={freshfoodzTheme}>
        <AuditStreamMonitor {...defaultProps} {...props} />
      </ThemeProvider>
    );
  };

  const mockAuditEntry = {
    id: 'audit-123',
    timestamp: new Date().toISOString(),
    action: 'UPDATE',
    entityType: 'Customer',
    entityId: 'cust-456',
    userId: 'user-789',
    userName: 'Max Mustermann',
    ipAddress: '192.168.1.100',
    details: 'Updated customer information',
    severity: 'info' as const
  };

  it('should render stream monitor with title', () => {
    renderComponent();
    
    expect(screen.getByText('Live Audit Stream')).toBeInTheDocument();
  });

  it('should show connection status indicator', async () => {
    renderComponent();
    
    // Initially connecting
    expect(screen.getByText('Verbinde...')).toBeInTheDocument();
    
    // Wait for connection
    await act(async () => {
      vi.advanceTimersByTime(20);
    });
    
    await waitFor(() => {
      expect(screen.getByText('Verbunden')).toBeInTheDocument();
    });
  });

  it('should display incoming audit entries', async () => {
    renderComponent();
    
    // Wait for connection
    await act(async () => {
      vi.advanceTimersByTime(20);
    });
    
    // Simulate incoming message
    act(() => {
      mockWebSocket.simulateMessage(mockAuditEntry);
    });
    
    await waitFor(() => {
      expect(screen.getByText('Max Mustermann')).toBeInTheDocument();
      expect(screen.getByText('UPDATE')).toBeInTheDocument();
      expect(screen.getByText(/Customer.*cust-456/)).toBeInTheDocument();
    });
  });

  it('should limit entries to maxEntries prop', async () => {
    renderComponent({ maxEntries: 3 });
    
    await act(async () => {
      vi.advanceTimersByTime(20);
    });
    
    // Send 5 messages
    for (let i = 0; i < 5; i++) {
      act(() => {
        mockWebSocket.simulateMessage({
          ...mockAuditEntry,
          id: `audit-${i}`,
          userName: `User ${i}`
        });
      });
    }
    
    await waitFor(() => {
      // Should only show last 3 entries
      expect(screen.getByText('User 4')).toBeInTheDocument();
      expect(screen.getByText('User 3')).toBeInTheDocument();
      expect(screen.getByText('User 2')).toBeInTheDocument();
      expect(screen.queryByText('User 1')).not.toBeInTheDocument();
      expect(screen.queryByText('User 0')).not.toBeInTheDocument();
    });
  });

  it('should display different severity levels with correct colors', async () => {
    renderComponent();
    
    await act(async () => {
      vi.advanceTimersByTime(20);
    });
    
    const severities = ['info', 'warning', 'error', 'critical'];
    
    for (const severity of severities) {
      act(() => {
        mockWebSocket.simulateMessage({
          ...mockAuditEntry,
          id: `audit-${severity}`,
          severity,
          action: severity.toUpperCase()
        });
      });
    }
    
    await waitFor(() => {
      // Check that all severity actions are displayed
      expect(screen.getByText('INFO')).toBeInTheDocument();
      expect(screen.getByText('WARNING')).toBeInTheDocument();
      expect(screen.getByText('ERROR')).toBeInTheDocument();
      expect(screen.getByText('CRITICAL')).toBeInTheDocument();
    });
  });

  it('should pause and resume stream', async () => {
    renderComponent();
    
    await act(async () => {
      vi.advanceTimersByTime(20);
    });
    
    // Click pause button
    const pauseButton = screen.getByRole('button', { name: /Pause/i });
    fireEvent.click(pauseButton);
    
    expect(screen.getByText('Pausiert')).toBeInTheDocument();
    
    // Send message while paused
    act(() => {
      mockWebSocket.simulateMessage({
        ...mockAuditEntry,
        userName: 'Paused User'
      });
    });
    
    // Should not display the paused message
    expect(screen.queryByText('Paused User')).not.toBeInTheDocument();
    
    // Resume
    const playButton = screen.getByRole('button', { name: /Play/i });
    fireEvent.click(playButton);
    
    // Send new message
    act(() => {
      mockWebSocket.simulateMessage({
        ...mockAuditEntry,
        userName: 'Active User'
      });
    });
    
    await waitFor(() => {
      expect(screen.getByText('Active User')).toBeInTheDocument();
    });
  });

  it('should clear all entries', async () => {
    renderComponent();
    
    await act(async () => {
      vi.advanceTimersByTime(20);
    });
    
    // Add some entries
    act(() => {
      mockWebSocket.simulateMessage(mockAuditEntry);
    });
    
    await waitFor(() => {
      expect(screen.getByText('Max Mustermann')).toBeInTheDocument();
    });
    
    // Click clear button
    const clearButton = screen.getByRole('button', { name: /Clear/i });
    fireEvent.click(clearButton);
    
    await waitFor(() => {
      expect(screen.queryByText('Max Mustermann')).not.toBeInTheDocument();
      expect(screen.getByText('Keine neuen Ereignisse')).toBeInTheDocument();
    });
  });

  it('should filter entries by type', async () => {
    renderComponent();
    
    await act(async () => {
      vi.advanceTimersByTime(20);
    });
    
    // Send different action types
    const actions = ['CREATE', 'UPDATE', 'DELETE', 'READ'];
    actions.forEach(action => {
      act(() => {
        mockWebSocket.simulateMessage({
          ...mockAuditEntry,
          id: `audit-${action}`,
          action,
          userName: `${action} User`
        });
      });
    });
    
    await waitFor(() => {
      expect(screen.getByText('CREATE User')).toBeInTheDocument();
    });
    
    // Open filter menu
    const filterButton = screen.getByRole('button', { name: /Filter/i });
    fireEvent.click(filterButton);
    
    // Select only UPDATE actions
    const updateCheckbox = screen.getByRole('checkbox', { name: /UPDATE/i });
    fireEvent.click(updateCheckbox);
    
    // Apply filter
    const applyButton = screen.getByRole('button', { name: /Anwenden/i });
    fireEvent.click(applyButton);
    
    await waitFor(() => {
      expect(screen.getByText('UPDATE User')).toBeInTheDocument();
      expect(screen.queryByText('CREATE User')).not.toBeInTheDocument();
      expect(screen.queryByText('DELETE User')).not.toBeInTheDocument();
    });
  });

  it('should handle connection errors', async () => {
    renderComponent();
    
    await act(async () => {
      vi.advanceTimersByTime(20);
    });
    
    // Simulate connection error
    act(() => {
      if (mockWebSocket.onerror) {
        mockWebSocket.onerror(new Event('error'));
      }
    });
    
    await waitFor(() => {
      expect(screen.getByText(/Verbindungsfehler/i)).toBeInTheDocument();
    });
  });

  it('should reconnect after connection loss', async () => {
    renderComponent();
    
    await act(async () => {
      vi.advanceTimersByTime(20);
    });
    
    expect(screen.getByText('Verbunden')).toBeInTheDocument();
    
    // Simulate connection close
    act(() => {
      mockWebSocket.close();
    });
    
    await waitFor(() => {
      expect(screen.getByText(/Getrennt/i)).toBeInTheDocument();
    });
    
    // Should attempt reconnection
    await act(async () => {
      vi.advanceTimersByTime(5000);
    });
    
    // New connection should be established
    await waitFor(() => {
      expect(screen.getByText('Verbunden')).toBeInTheDocument();
    });
  });

  it('should display entry details on click', async () => {
    renderComponent();
    
    await act(async () => {
      vi.advanceTimersByTime(20);
    });
    
    act(() => {
      mockWebSocket.simulateMessage(mockAuditEntry);
    });
    
    await waitFor(() => {
      expect(screen.getByText('Max Mustermann')).toBeInTheDocument();
    });
    
    // Click on entry
    const entry = screen.getByText('Max Mustermann').closest('[role="button"]');
    if (entry) {
      fireEvent.click(entry);
    }
    
    // Should show details dialog
    await waitFor(() => {
      expect(screen.getByText('Audit Details')).toBeInTheDocument();
      expect(screen.getByText('192.168.1.100')).toBeInTheDocument();
    });
  });

  it('should export stream data', async () => {
    const onExport = vi.fn();
    renderComponent({ onExport });
    
    await act(async () => {
      vi.advanceTimersByTime(20);
    });
    
    // Add some entries
    act(() => {
      mockWebSocket.simulateMessage(mockAuditEntry);
    });
    
    await waitFor(() => {
      expect(screen.getByText('Max Mustermann')).toBeInTheDocument();
    });
    
    // Click export button
    const exportButton = screen.getByRole('button', { name: /Export/i });
    fireEvent.click(exportButton);
    
    expect(onExport).toHaveBeenCalledWith(expect.arrayContaining([
      expect.objectContaining({
        userName: 'Max Mustermann'
      })
    ]));
  });

  it('should show statistics', async () => {
    renderComponent();
    
    await act(async () => {
      vi.advanceTimersByTime(20);
    });
    
    // Send multiple entries
    for (let i = 0; i < 5; i++) {
      act(() => {
        mockWebSocket.simulateMessage({
          ...mockAuditEntry,
          id: `audit-${i}`
        });
      });
    }
    
    await waitFor(() => {
      expect(screen.getByText(/5 Ereignisse/)).toBeInTheDocument();
    });
  });

  it('should highlight critical events', async () => {
    renderComponent();
    
    await act(async () => {
      vi.advanceTimersByTime(20);
    });
    
    act(() => {
      mockWebSocket.simulateMessage({
        ...mockAuditEntry,
        severity: 'critical',
        action: 'DELETE',
        userName: 'Critical User'
      });
    });
    
    await waitFor(() => {
      const criticalEntry = screen.getByText('Critical User').closest('.MuiPaper-root');
      expect(criticalEntry).toHaveStyle({ borderLeftColor: expect.stringContaining('f44336') });
    });
  });

  it('should auto-scroll to new entries when enabled', async () => {
    const { container } = renderComponent({ autoScroll: true });
    
    await act(async () => {
      vi.advanceTimersByTime(20);
    });
    
    const scrollContainer = container.querySelector('[data-testid="stream-container"]');
    const scrollSpy = vi.spyOn(scrollContainer as Element, 'scrollTop', 'set');
    
    // Send new entry
    act(() => {
      mockWebSocket.simulateMessage(mockAuditEntry);
    });
    
    await waitFor(() => {
      expect(scrollSpy).toHaveBeenCalled();
    });
  });

  it('should disable auto-scroll when manually scrolled', async () => {
    const { container } = renderComponent({ autoScroll: true });
    
    await act(async () => {
      vi.advanceTimersByTime(20);
    });
    
    const scrollContainer = container.querySelector('[data-testid="stream-container"]');
    
    // Simulate manual scroll
    if (scrollContainer) {
      fireEvent.scroll(scrollContainer, { target: { scrollTop: 100 } });
    }
    
    // Auto-scroll should be disabled
    const autoScrollToggle = screen.getByRole('checkbox', { name: /Auto-Scroll/i });
    expect(autoScrollToggle).not.toBeChecked();
  });

  it('should show loading spinner while connecting', () => {
    renderComponent();
    
    expect(screen.getByRole('progressbar')).toBeInTheDocument();
  });
});