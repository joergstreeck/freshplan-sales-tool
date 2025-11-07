/**
 * CustomerTable Integration Tests
 *
 * Enterprise-grade integration tests for complete user flows without real backend.
 * Tests the Timeline & Contacts expansion functionality for customers context.
 *
 * Coverage:
 * - Timeline icon click → expansion row opens → data loads → displays correctly
 * - Contacts icon click → expansion row opens → data loads → displays correctly
 * - Toggle functionality (clicking again closes expansion row)
 * - Multiple customers with different expansion states
 * - Context-aware rendering (customers vs leads)
 *
 * @module CustomerTable.integration
 * @since Sprint 2.1.7.2
 *
 * ⚠️ FEATURE NOT IMPLEMENTED YET (TDD Approach)
 * These tests were written ahead of implementation for the Timeline/Contacts expansion feature.
 * CustomerTable.tsx currently only has Edit/Delete icons (lines 314-345).
 *
 * TODO: Implement Timeline & Contacts expansion feature:
 * - Add Timeline icon button with title="Aktivitäten anzeigen"
 * - Add Contacts icon button with title="Kontakte anzeigen"
 * - Add expansion row rendering logic
 * - Add API calls to /api/customers/:id/timeline and /api/customers/:id/contacts
 * - Remove .skip() from describe block below when feature is implemented
 */
/* eslint-disable @typescript-eslint/no-explicit-any */

import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { CustomerTable } from './CustomerTable';
import type { CustomerResponse } from '../../customer/types/customer.types';
import type { Contact } from '../types/contact.types';

// Mock date-fns to avoid locale issues
vi.mock('date-fns', async () => {
  const actual = await vi.importActual('date-fns');
  return {
    ...actual,
    format: vi.fn((date: Date, formatStr: string) => {
      if (formatStr === 'dd.MM.yyyy') return '20.01.2025';
      if (formatStr === 'dd. MMM yyyy') return '20. Jan 2025';
      if (formatStr === 'HH:mm') return '10:30';
      return date.toISOString();
    }),
  };
});

describe.skip('CustomerTable Integration Tests - FEATURE NOT IMPLEMENTED YET', () => {
  const mockCustomers: CustomerResponse[] = [
    {
      id: '123e4567-e89b-12d3-a456-426614174000',
      customerNumber: 'K-2025-001',
      companyName: 'Test GmbH',
      customerType: 'GASTRO',
      status: 'ACTIVE',
      industry: 'RESTAURANT',
      expectedAnnualVolume: 50000,
      lastContactDate: '2025-01-20T10:00:00Z',
      riskScore: 25,
      createdAt: '2025-01-15T10:00:00Z',
      tags: [],
      contactCount: 2,
    },
    {
      id: '987e6543-e21b-43d2-a654-426614174999',
      customerNumber: 'K-2025-002',
      companyName: 'Demo AG',
      customerType: 'RETAIL',
      status: 'ACTIVE',
      industry: 'SUPERMARKET',
      expectedAnnualVolume: 75000,
      lastContactDate: '2025-01-18T14:00:00Z',
      riskScore: 15,
      createdAt: '2025-01-10T10:00:00Z',
      tags: [],
      contactCount: 3,
    },
  ];

  beforeEach(() => {
    // Reset and create new mock for each test
    global.fetch = vi.fn() as any;
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  describe('Timeline Icon Integration', () => {
    it('should open timeline expansion when clicking timeline icon', async () => {
      const user = userEvent.setup();

      // Mock timeline API response
      (global.fetch as any).mockResolvedValue({
        ok: true,
        json: async () => ({
          events: [
            {
              id: 1,
              activityType: 'EMAIL',
              activityDate: '2025-01-20T10:30:00Z',
              description: 'Angebot versendet',
              userId: 'user@example.com',
            },
          ],
        }),
      });

      const columns = [
        { id: 'customerNumber', label: 'Kundennr.', visible: true },
        { id: 'companyName', label: 'Firma', visible: true },
        { id: 'actions', label: 'Aktionen', visible: true },
      ];

      render(<CustomerTable customers={mockCustomers} columns={columns} context="customers" />);

      // Find timeline icon for first customer
      const timelineButtons = screen.getAllByTitle('Aktivitäten anzeigen');
      expect(timelineButtons.length).toBeGreaterThan(0);

      // Click timeline icon
      await user.click(timelineButtons[0]);

      // Wait for API call
      await waitFor(() => {
        expect(global.fetch).toHaveBeenCalledWith(
          `/api/customers/${mockCustomers[0].id}/timeline`,
          { credentials: 'include' }
        );
      });

      // Wait for timeline content to appear
      await waitFor(() => {
        expect(screen.getByText('EMAIL')).toBeInTheDocument();
        expect(screen.getByText('Angebot versendet')).toBeInTheDocument();
      });
    });

    it('should close timeline expansion when clicking timeline icon again', async () => {
      const user = userEvent.setup();

      // Mock timeline API response
      (global.fetch as any).mockResolvedValue({
        ok: true,
        json: async () => ({
          events: [
            {
              id: 1,
              activityType: 'EMAIL',
              activityDate: '2025-01-20T10:30:00Z',
              description: 'Angebot versendet',
              userId: 'user@example.com',
            },
          ],
        }),
      });

      const columns = [
        { id: 'customerNumber', label: 'Kundennr.', visible: true },
        { id: 'companyName', label: 'Firma', visible: true },
        { id: 'actions', label: 'Aktionen', visible: true },
      ];

      render(<CustomerTable customers={mockCustomers} columns={columns} context="customers" />);

      // Find timeline icon
      const timelineButtons = screen.getAllByTitle('Aktivitäten anzeigen');

      // Click to open
      await user.click(timelineButtons[0]);

      // Wait for content
      await waitFor(() => {
        expect(screen.getByText('EMAIL')).toBeInTheDocument();
      });

      // Click again to close
      await user.click(timelineButtons[0]);

      // Timeline content should disappear
      await waitFor(() => {
        expect(screen.queryByText('EMAIL')).not.toBeInTheDocument();
      });
    });

    it('should handle timeline API error gracefully', async () => {
      const user = userEvent.setup();

      // Mock timeline API error
      (global.fetch as any).mockRejectedValue(new Error('Network error'));

      const columns = [
        { id: 'customerNumber', label: 'Kundennr.', visible: true },
        { id: 'companyName', label: 'Firma', visible: true },
        { id: 'actions', label: 'Aktionen', visible: true },
      ];

      render(<CustomerTable customers={mockCustomers} columns={columns} context="customers" />);

      // Find timeline icon
      const timelineButtons = screen.getAllByTitle('Aktivitäten anzeigen');

      // Click timeline icon
      await user.click(timelineButtons[0]);

      // Wait for error message
      await waitFor(() => {
        expect(screen.getByText('Network error')).toBeInTheDocument();
      });
    });

    it('should only show one timeline expansion at a time', async () => {
      const user = userEvent.setup();

      // Mock timeline API responses
      (global.fetch as any).mockImplementation((url: string) => {
        if (url.includes(mockCustomers[0].id)) {
          return Promise.resolve({
            ok: true,
            json: async () => ({
              events: [
                {
                  id: 1,
                  activityType: 'EMAIL',
                  activityDate: '2025-01-20T10:30:00Z',
                  description: 'Email from Customer 1',
                  userId: 'user1@example.com',
                },
              ],
            }),
          });
        }
        if (url.includes(mockCustomers[1].id)) {
          return Promise.resolve({
            ok: true,
            json: async () => ({
              events: [
                {
                  id: 2,
                  activityType: 'CALL',
                  activityDate: '2025-01-19T14:00:00Z',
                  description: 'Call from Customer 2',
                  userId: 'user2@example.com',
                },
              ],
            }),
          });
        }
        return Promise.reject(new Error('Unknown URL'));
      });

      const columns = [
        { id: 'customerNumber', label: 'Kundennr.', visible: true },
        { id: 'companyName', label: 'Firma', visible: true },
        { id: 'actions', label: 'Aktionen', visible: true },
      ];

      render(<CustomerTable customers={mockCustomers} columns={columns} context="customers" />);

      // Find timeline icons
      const timelineButtons = screen.getAllByTitle('Aktivitäten anzeigen');

      // Click first timeline icon
      await user.click(timelineButtons[0]);

      // Wait for first timeline
      await waitFor(() => {
        expect(screen.getByText('Email from Customer 1')).toBeInTheDocument();
      });

      // Click second timeline icon
      await user.click(timelineButtons[1]);

      // Wait for second timeline
      await waitFor(() => {
        expect(screen.getByText('Call from Customer 2')).toBeInTheDocument();
      });

      // First timeline should still be there (different customer)
      expect(screen.queryByText('Email from Customer 1')).not.toBeInTheDocument();
    });
  });

  describe('Contacts Icon Integration', () => {
    it('should open contacts expansion when clicking contacts icon', async () => {
      const user = userEvent.setup();

      const mockContacts: Contact[] = [
        {
          id: '1',
          customerId: mockCustomers[0].id,
          firstName: 'Max',
          lastName: 'Mustermann',
          email: 'max@example.com',
          phone: '+49 123 456789',
          isPrimary: true,
          isActive: true,
          responsibilityScope: 'all',
          decisionLevel: 'entscheider',
          relationshipWarmth: 85,
          createdAt: '2025-01-15T10:00:00Z',
          updatedAt: '2025-01-20T15:30:00Z',
        },
      ];

      // Mock contacts API response
      (global.fetch as any).mockResolvedValue({
        ok: true,
        json: async () => mockContacts,
      });

      const columns = [
        { id: 'customerNumber', label: 'Kundennr.', visible: true },
        { id: 'companyName', label: 'Firma', visible: true },
        { id: 'actions', label: 'Aktionen', visible: true },
      ];

      render(<CustomerTable customers={mockCustomers} columns={columns} context="customers" />);

      // Find contacts icon for first customer
      const contactsButtons = screen.getAllByTitle('Kontakte anzeigen');
      expect(contactsButtons.length).toBeGreaterThan(0);

      // Click contacts icon
      await user.click(contactsButtons[0]);

      // Wait for API call
      await waitFor(() => {
        expect(global.fetch).toHaveBeenCalledWith(
          `/api/customers/${mockCustomers[0].id}/contacts`,
          { credentials: 'include' }
        );
      });

      // Wait for contacts content to appear
      await waitFor(() => {
        expect(screen.getByText('Max Mustermann')).toBeInTheDocument();
        expect(screen.getByText('max@example.com')).toBeInTheDocument();
        expect(screen.getByText('85%')).toBeInTheDocument();
      });
    });

    it('should close contacts expansion when clicking contacts icon again', async () => {
      const user = userEvent.setup();

      const mockContacts: Contact[] = [
        {
          id: '1',
          customerId: mockCustomers[0].id,
          firstName: 'Max',
          lastName: 'Mustermann',
          isPrimary: true,
          isActive: true,
          responsibilityScope: 'all',
          createdAt: '2025-01-15T10:00:00Z',
          updatedAt: '2025-01-20T15:30:00Z',
        },
      ];

      // Mock contacts API response
      (global.fetch as any).mockResolvedValue({
        ok: true,
        json: async () => mockContacts,
      });

      const columns = [
        { id: 'customerNumber', label: 'Kundennr.', visible: true },
        { id: 'companyName', label: 'Firma', visible: true },
        { id: 'actions', label: 'Aktionen', visible: true },
      ];

      render(<CustomerTable customers={mockCustomers} columns={columns} context="customers" />);

      // Find contacts icon
      const contactsButtons = screen.getAllByTitle('Kontakte anzeigen');

      // Click to open
      await user.click(contactsButtons[0]);

      // Wait for content
      await waitFor(() => {
        expect(screen.getByText('Max Mustermann')).toBeInTheDocument();
      });

      // Click again to close
      await user.click(contactsButtons[0]);

      // Contacts content should disappear
      await waitFor(() => {
        expect(screen.queryByText('Max Mustermann')).not.toBeInTheDocument();
      });
    });

    it('should handle contacts API error gracefully', async () => {
      const user = userEvent.setup();

      // Mock contacts API error
      (global.fetch as any).mockRejectedValue(new Error('Network error'));

      const columns = [
        { id: 'customerNumber', label: 'Kundennr.', visible: true },
        { id: 'companyName', label: 'Firma', visible: true },
        { id: 'actions', label: 'Aktionen', visible: true },
      ];

      render(<CustomerTable customers={mockCustomers} columns={columns} context="customers" />);

      // Find contacts icon
      const contactsButtons = screen.getAllByTitle('Kontakte anzeigen');

      // Click contacts icon
      await user.click(contactsButtons[0]);

      // Wait for error message
      await waitFor(() => {
        expect(screen.getByText('Fehler beim Laden der Kontakte')).toBeInTheDocument();
      });
    });

    it('should display empty state when no contacts exist', async () => {
      const user = userEvent.setup();

      // Mock empty contacts response
      (global.fetch as any).mockResolvedValue({
        ok: true,
        json: async () => [],
      });

      const columns = [
        { id: 'customerNumber', label: 'Kundennr.', visible: true },
        { id: 'companyName', label: 'Firma', visible: true },
        { id: 'actions', label: 'Aktionen', visible: true },
      ];

      render(<CustomerTable customers={mockCustomers} columns={columns} context="customers" />);

      // Find contacts icon
      const contactsButtons = screen.getAllByTitle('Kontakte anzeigen');

      // Click contacts icon
      await user.click(contactsButtons[0]);

      // Wait for empty state message
      await waitFor(() => {
        expect(screen.getByText('Noch keine Kontakte erfasst')).toBeInTheDocument();
      });
    });
  });

  describe('Complete User Flow', () => {
    it('should handle complete user flow: open timeline, close, open contacts', async () => {
      const user = userEvent.setup();

      const mockContacts: Contact[] = [
        {
          id: '1',
          customerId: mockCustomers[0].id,
          firstName: 'Max',
          lastName: 'Mustermann',
          isPrimary: true,
          isActive: true,
          responsibilityScope: 'all',
          createdAt: '2025-01-15T10:00:00Z',
          updatedAt: '2025-01-20T15:30:00Z',
        },
      ];

      // Mock API responses
      (global.fetch as any).mockImplementation((url: string) => {
        if (url.includes('timeline')) {
          return Promise.resolve({
            ok: true,
            json: async () => ({
              events: [
                {
                  id: 1,
                  activityType: 'EMAIL',
                  activityDate: '2025-01-20T10:30:00Z',
                  description: 'Test Email',
                  userId: 'user@example.com',
                },
              ],
            }),
          });
        }
        if (url.includes('contacts')) {
          return Promise.resolve({
            ok: true,
            json: async () => mockContacts,
          });
        }
        return Promise.reject(new Error('Unknown URL'));
      });

      const columns = [
        { id: 'customerNumber', label: 'Kundennr.', visible: true },
        { id: 'companyName', label: 'Firma', visible: true },
        { id: 'actions', label: 'Aktionen', visible: true },
      ];

      render(<CustomerTable customers={mockCustomers} columns={columns} context="customers" />);

      // Step 1: Open timeline
      const timelineButtons = screen.getAllByTitle('Aktivitäten anzeigen');
      await user.click(timelineButtons[0]);

      await waitFor(() => {
        expect(screen.getByText('Test Email')).toBeInTheDocument();
      });

      // Step 2: Close timeline
      await user.click(timelineButtons[0]);

      await waitFor(() => {
        expect(screen.queryByText('Test Email')).not.toBeInTheDocument();
      });

      // Step 3: Open contacts
      const contactsButtons = screen.getAllByTitle('Kontakte anzeigen');
      await user.click(contactsButtons[0]);

      await waitFor(() => {
        expect(screen.getByText('Max Mustermann')).toBeInTheDocument();
      });
    });

    it('should allow both timeline and contacts to be open for same customer', async () => {
      const user = userEvent.setup();

      const mockContacts: Contact[] = [
        {
          id: '1',
          customerId: mockCustomers[0].id,
          firstName: 'Max',
          lastName: 'Mustermann',
          isPrimary: true,
          isActive: true,
          responsibilityScope: 'all',
          createdAt: '2025-01-15T10:00:00Z',
          updatedAt: '2025-01-20T15:30:00Z',
        },
      ];

      // Mock API responses
      (global.fetch as any).mockImplementation((url: string) => {
        if (url.includes('timeline')) {
          return Promise.resolve({
            ok: true,
            json: async () => ({
              events: [
                {
                  id: 1,
                  activityType: 'EMAIL',
                  activityDate: '2025-01-20T10:30:00Z',
                  description: 'Test Email',
                  userId: 'user@example.com',
                },
              ],
            }),
          });
        }
        if (url.includes('contacts')) {
          return Promise.resolve({
            ok: true,
            json: async () => mockContacts,
          });
        }
        return Promise.reject(new Error('Unknown URL'));
      });

      const columns = [
        { id: 'customerNumber', label: 'Kundennr.', visible: true },
        { id: 'companyName', label: 'Firma', visible: true },
        { id: 'actions', label: 'Aktionen', visible: true },
      ];

      render(<CustomerTable customers={mockCustomers} columns={columns} context="customers" />);

      // Open timeline
      const timelineButtons = screen.getAllByTitle('Aktivitäten anzeigen');
      await user.click(timelineButtons[0]);

      await waitFor(() => {
        expect(screen.getByText('Test Email')).toBeInTheDocument();
      });

      // Open contacts (timeline should stay open)
      const contactsButtons = screen.getAllByTitle('Kontakte anzeigen');
      await user.click(contactsButtons[0]);

      await waitFor(() => {
        expect(screen.getByText('Max Mustermann')).toBeInTheDocument();
      });

      // Both should be visible
      expect(screen.getByText('Test Email')).toBeInTheDocument();
      expect(screen.getByText('Max Mustermann')).toBeInTheDocument();
    });
  });

  describe('Context-Aware Rendering', () => {
    it('should render timeline and contacts icons in customers context', () => {
      const columns = [
        { id: 'customerNumber', label: 'Kundennr.', visible: true },
        { id: 'companyName', label: 'Firma', visible: true },
        { id: 'actions', label: 'Aktionen', visible: true },
      ];

      render(<CustomerTable customers={mockCustomers} columns={columns} context="customers" />);

      // Should render timeline and contacts icons (2 icons per customer)
      expect(screen.getAllByTitle('Aktivitäten anzeigen').length).toBe(2);
      expect(screen.getAllByTitle('Kontakte anzeigen').length).toBe(2);

      // Should NOT render Stop-the-Clock icons (leads only)
      expect(screen.queryByTitle('Schutzfrist pausieren')).not.toBeInTheDocument();
      expect(screen.queryByTitle('Schutzfrist fortsetzen')).not.toBeInTheDocument();
    });

    it('should not render customer icons when actions column is not visible', () => {
      const columns = [
        { id: 'customerNumber', label: 'Kundennr.', visible: true },
        { id: 'companyName', label: 'Firma', visible: true },
      ];

      render(<CustomerTable customers={mockCustomers} columns={columns} context="customers" />);

      // Should not render any action icons
      expect(screen.queryByTitle('Aktivitäten anzeigen')).not.toBeInTheDocument();
      expect(screen.queryByTitle('Kontakte anzeigen')).not.toBeInTheDocument();
    });
  });

  describe('Icon Click Isolation', () => {
    it('should not trigger row click when clicking timeline icon', async () => {
      const user = userEvent.setup();
      const onRowClick = vi.fn();

      // Mock timeline API response
      (global.fetch as any).mockResolvedValue({
        ok: true,
        json: async () => ({ events: [] }),
      });

      const columns = [
        { id: 'customerNumber', label: 'Kundennr.', visible: true },
        { id: 'companyName', label: 'Firma', visible: true },
        { id: 'actions', label: 'Aktionen', visible: true },
      ];

      render(
        <CustomerTable
          customers={mockCustomers}
          columns={columns}
          context="customers"
          onRowClick={onRowClick}
        />
      );

      // Click timeline icon
      const timelineButtons = screen.getAllByTitle('Aktivitäten anzeigen');
      await user.click(timelineButtons[0]);

      // onRowClick should NOT be called
      expect(onRowClick).not.toHaveBeenCalled();
    });

    it('should not trigger row click when clicking contacts icon', async () => {
      const user = userEvent.setup();
      const onRowClick = vi.fn();

      // Mock contacts API response
      (global.fetch as any).mockResolvedValue({
        ok: true,
        json: async () => [],
      });

      const columns = [
        { id: 'customerNumber', label: 'Kundennr.', visible: true },
        { id: 'companyName', label: 'Firma', visible: true },
        { id: 'actions', label: 'Aktionen', visible: true },
      ];

      render(
        <CustomerTable
          customers={mockCustomers}
          columns={columns}
          context="customers"
          onRowClick={onRowClick}
        />
      );

      // Click contacts icon
      const contactsButtons = screen.getAllByTitle('Kontakte anzeigen');
      await user.click(contactsButtons[0]);

      // onRowClick should NOT be called
      expect(onRowClick).not.toHaveBeenCalled();
    });
  });
});
