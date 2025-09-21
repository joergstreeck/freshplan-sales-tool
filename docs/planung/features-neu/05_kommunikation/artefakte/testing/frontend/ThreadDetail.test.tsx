import React from 'react';
import { render, screen, fireEvent, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { vi, describe, it, expect, beforeEach, afterEach } from 'vitest';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ThemeProvider } from '@mui/material/styles';
import ThreadDetail from '../../frontend/ThreadDetail';
import { theme } from '../../frontend/theme-v2.mui';
import type { Thread, Message } from '../../frontend/communication';

// Mock API client
vi.mock('../../frontend/apiClient', () => ({
  apiClient: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
  }
}));

const mockThread: Thread = {
  id: '11111111-1111-1111-1111-111111111111',
  customerId: '22222222-2222-2222-2222-222222222222',
  customerName: 'Restaurant Adler',
  territory: 'BER',
  channel: 'EMAIL',
  subject: 'Sample Follow-up T+3',
  status: 'OPEN',
  createdAt: '2025-09-15T10:00:00Z',
  createdBy: 'sales@freshfoodz.de',
  lastMessageAt: '2025-09-18T14:30:00Z',
  messageCount: 3,
  unreadCount: 1,
  version: 5,
  participants: [
    { email: 'kuechenchef@restaurant-adler.de', name: 'Thomas Schmidt', role: 'Küchenchef' },
    { email: 'einkauf@restaurant-adler.de', name: 'Maria Weber', role: 'Einkauf' }
  ],
  tags: ['sample_delivered', 'follow_up_required'],
};

const mockMessages: Message[] = [
  {
    id: 'msg-001',
    threadId: mockThread.id,
    sender: 'sales@freshfoodz.de',
    senderName: 'Jörg Müller',
    recipients: ['kuechenchef@restaurant-adler.de'],
    subject: 'Cook&Fresh Sample Delivery',
    body: 'Sehr geehrter Herr Schmidt, vielen Dank für Ihr Interesse an unseren Cook&Fresh Produkten...',
    channel: 'EMAIL',
    status: 'SENT',
    createdAt: '2025-09-15T10:00:00Z',
    sentAt: '2025-09-15T10:01:00Z',
    isRead: true,
  },
  {
    id: 'msg-002',
    threadId: mockThread.id,
    sender: 'kuechenchef@restaurant-adler.de',
    senderName: 'Thomas Schmidt',
    recipients: ['sales@freshfoodz.de'],
    subject: 'Re: Cook&Fresh Sample Delivery',
    body: 'Hallo Herr Müller, die Produkte sind angekommen. Wir testen sie morgen.',
    channel: 'EMAIL',
    status: 'SENT',
    createdAt: '2025-09-16T09:00:00Z',
    sentAt: '2025-09-16T09:00:00Z',
    isRead: true,
  },
  {
    id: 'msg-003',
    threadId: mockThread.id,
    sender: 'sales@freshfoodz.de',
    senderName: 'Jörg Müller',
    recipients: ['kuechenchef@restaurant-adler.de', 'einkauf@restaurant-adler.de'],
    subject: 'Sample Follow-up T+3',
    body: 'Guten Tag Herr Schmidt, wie hat Ihnen unser Cook&Fresh Gulasch geschmeckt?',
    channel: 'EMAIL',
    status: 'SENT',
    createdAt: '2025-09-18T14:30:00Z',
    sentAt: '2025-09-18T14:30:00Z',
    isRead: false,
  },
];

describe('ThreadDetail Component', () => {
  let queryClient: QueryClient;
  const user = userEvent.setup();

  beforeEach(() => {
    queryClient = new QueryClient({
      defaultOptions: {
        queries: { retry: false },
        mutations: { retry: false },
      },
    });
    vi.clearAllMocks();
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  const renderComponent = (props = {}) => {
    return render(
      <QueryClientProvider client={queryClient}>
        <ThemeProvider theme={theme}>
          <ThreadDetail threadId={mockThread.id} {...props} />
        </ThemeProvider>
      </QueryClientProvider>
    );
  };

  describe('Thread Header Display', () => {
    it('should display thread subject and metadata', async () => {
      const { apiClient } = await import('../../frontend/apiClient');
      vi.mocked(apiClient.get).mockResolvedValueOnce({ data: mockThread });
      vi.mocked(apiClient.get).mockResolvedValueOnce({ data: { items: mockMessages } });

      renderComponent();

      await waitFor(() => {
        expect(screen.getByText('Sample Follow-up T+3')).toBeInTheDocument();
        expect(screen.getByText('Restaurant Adler')).toBeInTheDocument();
        expect(screen.getByText(/BER/)).toBeInTheDocument();
        expect(screen.getByText(/EMAIL/)).toBeInTheDocument();
      });
    });

    it('should show unread count badge', async () => {
      const { apiClient } = await import('../../frontend/apiClient');
      vi.mocked(apiClient.get).mockResolvedValueOnce({ data: mockThread });
      vi.mocked(apiClient.get).mockResolvedValueOnce({ data: { items: mockMessages } });

      renderComponent();

      await waitFor(() => {
        const badge = screen.getByText('1 unread');
        expect(badge).toBeInTheDocument();
        expect(badge).toHaveClass('MuiChip-root');
      });
    });

    it('should display participant list', async () => {
      const { apiClient } = await import('../../frontend/apiClient');
      vi.mocked(apiClient.get).mockResolvedValueOnce({ data: mockThread });
      vi.mocked(apiClient.get).mockResolvedValueOnce({ data: { items: mockMessages } });

      renderComponent();

      await waitFor(() => {
        expect(screen.getByText('Thomas Schmidt (Küchenchef)')).toBeInTheDocument();
        expect(screen.getByText('Maria Weber (Einkauf)')).toBeInTheDocument();
      });
    });

    it('should show thread status correctly', async () => {
      const { apiClient } = await import('../../frontend/apiClient');
      const closedThread = { ...mockThread, status: 'RESOLVED' };
      vi.mocked(apiClient.get).mockResolvedValueOnce({ data: closedThread });
      vi.mocked(apiClient.get).mockResolvedValueOnce({ data: { items: mockMessages } });

      renderComponent();

      await waitFor(() => {
        expect(screen.getByText('RESOLVED')).toBeInTheDocument();
      });
    });
  });

  describe('Message Timeline Display', () => {
    it('should render all messages in chronological order', async () => {
      const { apiClient } = await import('../../frontend/apiClient');
      vi.mocked(apiClient.get).mockResolvedValueOnce({ data: mockThread });
      vi.mocked(apiClient.get).mockResolvedValueOnce({ data: { items: mockMessages } });

      renderComponent();

      await waitFor(() => {
        const messages = screen.getAllByTestId(/^message-/);
        expect(messages).toHaveLength(3);

        // Check order
        expect(within(messages[0]).getByText(/Cook&Fresh Sample Delivery/)).toBeInTheDocument();
        expect(within(messages[1]).getByText(/die Produkte sind angekommen/)).toBeInTheDocument();
        expect(within(messages[2]).getByText(/wie hat Ihnen unser Cook&Fresh Gulasch/)).toBeInTheDocument();
      });
    });

    it('should highlight unread messages', async () => {
      const { apiClient } = await import('../../frontend/apiClient');
      vi.mocked(apiClient.get).mockResolvedValueOnce({ data: mockThread });
      vi.mocked(apiClient.get).mockResolvedValueOnce({ data: { items: mockMessages } });

      renderComponent();

      await waitFor(() => {
        const unreadMessage = screen.getByTestId('message-msg-003');
        expect(unreadMessage).toHaveStyle({ fontWeight: 'bold' });
        expect(unreadMessage.querySelector('.unread-indicator')).toBeInTheDocument();
      });
    });

    it('should show sender information for each message', async () => {
      const { apiClient } = await import('../../frontend/apiClient');
      vi.mocked(apiClient.get).mockResolvedValueOnce({ data: mockThread });
      vi.mocked(apiClient.get).mockResolvedValueOnce({ data: { items: mockMessages } });

      renderComponent();

      await waitFor(() => {
        expect(screen.getByText('Jörg Müller')).toBeInTheDocument();
        expect(screen.getByText('Thomas Schmidt')).toBeInTheDocument();
      });
    });

    it('should display message timestamps correctly', async () => {
      const { apiClient } = await import('../../frontend/apiClient');
      vi.mocked(apiClient.get).mockResolvedValueOnce({ data: mockThread });
      vi.mocked(apiClient.get).mockResolvedValueOnce({ data: { items: mockMessages } });

      renderComponent();

      await waitFor(() => {
        expect(screen.getByText(/15\.09\.2025/)).toBeInTheDocument();
        expect(screen.getByText(/16\.09\.2025/)).toBeInTheDocument();
        expect(screen.getByText(/18\.09\.2025/)).toBeInTheDocument();
      });
    });
  });

  describe('Reply Functionality', () => {
    it('should show reply button when thread is open', async () => {
      const { apiClient } = await import('../../frontend/apiClient');
      vi.mocked(apiClient.get).mockResolvedValueOnce({ data: mockThread });
      vi.mocked(apiClient.get).mockResolvedValueOnce({ data: { items: mockMessages } });

      renderComponent();

      await waitFor(() => {
        const replyButton = screen.getByRole('button', { name: /Reply/i });
        expect(replyButton).toBeInTheDocument();
        expect(replyButton).toBeEnabled();
      });
    });

    it('should open reply composer on click', async () => {
      const { apiClient } = await import('../../frontend/apiClient');
      vi.mocked(apiClient.get).mockResolvedValueOnce({ data: mockThread });
      vi.mocked(apiClient.get).mockResolvedValueOnce({ data: { items: mockMessages } });

      renderComponent();

      await waitFor(() => {
        const replyButton = screen.getByRole('button', { name: /Reply/i });
        fireEvent.click(replyButton);
      });

      expect(screen.getByPlaceholderText(/Type your reply/i)).toBeInTheDocument();
      expect(screen.getByRole('button', { name: /Send/i })).toBeInTheDocument();
    });

    it('should handle reply submission with ETag', async () => {
      const { apiClient } = await import('../../frontend/apiClient');
      vi.mocked(apiClient.get).mockResolvedValueOnce({ data: mockThread });
      vi.mocked(apiClient.get).mockResolvedValueOnce({ data: { items: mockMessages } });
      vi.mocked(apiClient.post).mockResolvedValueOnce({
        data: { id: 'msg-004', body: 'Test reply' }
      });

      renderComponent();

      await waitFor(() => {
        const replyButton = screen.getByRole('button', { name: /Reply/i });
        fireEvent.click(replyButton);
      });

      const textarea = screen.getByPlaceholderText(/Type your reply/i);
      await user.type(textarea, 'Thank you for your feedback!');

      const sendButton = screen.getByRole('button', { name: /Send/i });
      await user.click(sendButton);

      await waitFor(() => {
        expect(apiClient.post).toHaveBeenCalledWith(
          `/api/comm/threads/${mockThread.id}/reply`,
          expect.objectContaining({
            body: 'Thank you for your feedback!',
          }),
          expect.objectContaining({
            headers: expect.objectContaining({
              'If-Match': '"v5"',
            }),
          })
        );
      });
    });

    it('should handle ETag conflict (412) on reply', async () => {
      const { apiClient } = await import('../../frontend/apiClient');
      vi.mocked(apiClient.get).mockResolvedValueOnce({ data: mockThread });
      vi.mocked(apiClient.get).mockResolvedValueOnce({ data: { items: mockMessages } });
      vi.mocked(apiClient.post).mockRejectedValueOnce({
        response: { status: 412, data: { detail: 'Thread was modified' } }
      });

      renderComponent();

      await waitFor(() => {
        const replyButton = screen.getByRole('button', { name: /Reply/i });
        fireEvent.click(replyButton);
      });

      const textarea = screen.getByPlaceholderText(/Type your reply/i);
      await user.type(textarea, 'Test reply');

      const sendButton = screen.getByRole('button', { name: /Send/i });
      await user.click(sendButton);

      await waitFor(() => {
        expect(screen.getByText(/Thread was modified/i)).toBeInTheDocument();
      });
    });

    it('should disable reply for resolved threads', async () => {
      const { apiClient } = await import('../../frontend/apiClient');
      const resolvedThread = { ...mockThread, status: 'RESOLVED' };
      vi.mocked(apiClient.get).mockResolvedValueOnce({ data: resolvedThread });
      vi.mocked(apiClient.get).mockResolvedValueOnce({ data: { items: mockMessages } });

      renderComponent();

      await waitFor(() => {
        const replyButton = screen.queryByRole('button', { name: /Reply/i });
        expect(replyButton).toBeDisabled();
      });
    });
  });

  describe('Thread Actions', () => {
    it('should allow marking thread as resolved', async () => {
      const { apiClient } = await import('../../frontend/apiClient');
      vi.mocked(apiClient.get).mockResolvedValueOnce({ data: mockThread });
      vi.mocked(apiClient.get).mockResolvedValueOnce({ data: { items: mockMessages } });
      vi.mocked(apiClient.put).mockResolvedValueOnce({ data: { ...mockThread, status: 'RESOLVED' } });

      renderComponent();

      await waitFor(() => {
        const resolveButton = screen.getByRole('button', { name: /Resolve/i });
        expect(resolveButton).toBeInTheDocument();
      });

      const resolveButton = screen.getByRole('button', { name: /Resolve/i });
      await user.click(resolveButton);

      await waitFor(() => {
        expect(apiClient.put).toHaveBeenCalledWith(
          `/api/comm/threads/${mockThread.id}/resolve`,
          expect.any(Object)
        );
      });
    });

    it('should allow marking all messages as read', async () => {
      const { apiClient } = await import('../../frontend/apiClient');
      vi.mocked(apiClient.get).mockResolvedValueOnce({ data: mockThread });
      vi.mocked(apiClient.get).mockResolvedValueOnce({ data: { items: mockMessages } });
      vi.mocked(apiClient.post).mockResolvedValueOnce({ data: { success: true } });

      renderComponent();

      await waitFor(() => {
        const markReadButton = screen.getByRole('button', { name: /Mark all as read/i });
        expect(markReadButton).toBeInTheDocument();
      });

      const markReadButton = screen.getByRole('button', { name: /Mark all as read/i });
      await user.click(markReadButton);

      await waitFor(() => {
        expect(apiClient.post).toHaveBeenCalledWith(
          `/api/comm/threads/${mockThread.id}/mark-read`
        );
      });
    });

    it('should show archive option for old resolved threads', async () => {
      const { apiClient } = await import('../../frontend/apiClient');
      const oldResolvedThread = {
        ...mockThread,
        status: 'RESOLVED',
        resolvedAt: '2025-08-01T10:00:00Z',
      };
      vi.mocked(apiClient.get).mockResolvedValueOnce({ data: oldResolvedThread });
      vi.mocked(apiClient.get).mockResolvedValueOnce({ data: { items: mockMessages } });

      renderComponent();

      await waitFor(() => {
        const archiveButton = screen.getByRole('button', { name: /Archive/i });
        expect(archiveButton).toBeInTheDocument();
      });
    });
  });

  describe('B2B Food Specific Features', () => {
    it('should highlight sample follow-up messages', async () => {
      const { apiClient } = await import('../../frontend/apiClient');
      vi.mocked(apiClient.get).mockResolvedValueOnce({ data: mockThread });
      vi.mocked(apiClient.get).mockResolvedValueOnce({ data: { items: mockMessages } });

      renderComponent();

      await waitFor(() => {
        const followUpMessage = screen.getByText(/Sample Follow-up T\+3/);
        expect(followUpMessage.closest('.message-card')).toHaveClass('sample-followup');
      });
    });

    it('should show SLA indicator for follow-ups', async () => {
      const { apiClient } = await import('../../frontend/apiClient');
      const threadWithSLA = {
        ...mockThread,
        slaDeadline: '2025-09-19T14:30:00Z',
        priority: 'HIGH',
      };
      vi.mocked(apiClient.get).mockResolvedValueOnce({ data: threadWithSLA });
      vi.mocked(apiClient.get).mockResolvedValueOnce({ data: { items: mockMessages } });

      renderComponent();

      await waitFor(() => {
        expect(screen.getByText(/SLA: 19\.09\.2025/)).toBeInTheDocument();
        expect(screen.getByText('HIGH')).toHaveClass('priority-high');
      });
    });

    it('should display Cook&Fresh product mentions', async () => {
      const { apiClient } = await import('../../frontend/apiClient');
      vi.mocked(apiClient.get).mockResolvedValueOnce({ data: mockThread });
      vi.mocked(apiClient.get).mockResolvedValueOnce({ data: { items: mockMessages } });

      renderComponent();

      await waitFor(() => {
        const productMentions = screen.getAllByText(/Cook&Fresh/);
        expect(productMentions.length).toBeGreaterThan(0);
        productMentions.forEach(mention => {
          expect(mention).toHaveClass('product-highlight');
        });
      });
    });

    it('should show multi-contact indicator', async () => {
      const { apiClient } = await import('../../frontend/apiClient');
      vi.mocked(apiClient.get).mockResolvedValueOnce({ data: mockThread });
      vi.mocked(apiClient.get).mockResolvedValueOnce({ data: { items: mockMessages } });

      renderComponent();

      await waitFor(() => {
        const multiContactBadge = screen.getByText('Multi-Contact');
        expect(multiContactBadge).toBeInTheDocument();
        expect(screen.getByText(/Küchenchef/)).toBeInTheDocument();
        expect(screen.getByText(/Einkauf/)).toBeInTheDocument();
      });
    });
  });

  describe('Loading and Error States', () => {
    it('should show loading state', () => {
      const { apiClient } = await import('../../frontend/apiClient');
      vi.mocked(apiClient.get).mockImplementation(() => new Promise(() => {}));

      renderComponent();

      expect(screen.getByText(/Loading thread/i)).toBeInTheDocument();
    });

    it('should handle API errors gracefully', async () => {
      const { apiClient } = await import('../../frontend/apiClient');
      vi.mocked(apiClient.get).mockRejectedValueOnce(new Error('Network error'));

      renderComponent();

      await waitFor(() => {
        expect(screen.getByText(/Error loading thread/i)).toBeInTheDocument();
        expect(screen.getByRole('button', { name: /Retry/i })).toBeInTheDocument();
      });
    });

    it('should handle 403 forbidden for wrong territory', async () => {
      const { apiClient } = await import('../../frontend/apiClient');
      vi.mocked(apiClient.get).mockRejectedValueOnce({
        response: { status: 403, data: { detail: 'Territory access denied' } }
      });

      renderComponent();

      await waitFor(() => {
        expect(screen.getByText(/Territory access denied/i)).toBeInTheDocument();
      });
    });
  });

  describe('Accessibility', () => {
    it('should have proper ARIA labels', async () => {
      const { apiClient } = await import('../../frontend/apiClient');
      vi.mocked(apiClient.get).mockResolvedValueOnce({ data: mockThread });
      vi.mocked(apiClient.get).mockResolvedValueOnce({ data: { items: mockMessages } });

      renderComponent();

      await waitFor(() => {
        expect(screen.getByRole('article', { name: /Thread detail/i })).toBeInTheDocument();
        expect(screen.getByRole('region', { name: /Message timeline/i })).toBeInTheDocument();
        expect(screen.getByRole('button', { name: /Reply to thread/i })).toBeInTheDocument();
      });
    });

    it('should support keyboard navigation', async () => {
      const { apiClient } = await import('../../frontend/apiClient');
      vi.mocked(apiClient.get).mockResolvedValueOnce({ data: mockThread });
      vi.mocked(apiClient.get).mockResolvedValueOnce({ data: { items: mockMessages } });

      renderComponent();

      await waitFor(() => {
        screen.getByRole('button', { name: /Reply/i });
      });

      // Tab to reply button
      await user.tab();
      expect(screen.getByRole('button', { name: /Reply/i })).toHaveFocus();

      // Enter to open reply
      await user.keyboard('{Enter}');
      expect(screen.getByPlaceholderText(/Type your reply/i)).toBeInTheDocument();

      // Tab to text area
      await user.tab();
      expect(screen.getByPlaceholderText(/Type your reply/i)).toHaveFocus();
    });
  });
});