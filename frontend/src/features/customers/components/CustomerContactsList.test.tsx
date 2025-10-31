/* eslint-disable @typescript-eslint/no-explicit-any */
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import CustomerContactsList from './CustomerContactsList';
import type { Contact } from '../types/contact.types';

describe('CustomerContactsList', () => {
  const mockCustomerId = '123e4567-e89b-12d3-a456-426614174000';

  beforeEach(() => {
    // Reset and create new mock for each test
    global.fetch = vi.fn() as any;
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  describe('Loading State', () => {
    it('should render loading spinner initially', () => {
      (global.fetch as any).mockImplementation(() => new Promise(() => {}));

      render(<CustomerContactsList customerId={mockCustomerId} />);

      expect(screen.getByRole('progressbar')).toBeInTheDocument();
    });
  });

  describe('Success State', () => {
    it('should fetch and display contacts successfully', async () => {
      const mockContacts: Contact[] = [
        {
          id: '1',
          customerId: mockCustomerId,
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

      (global.fetch as any).mockResolvedValueOnce({
        ok: true,
        json: async () => mockContacts,
      });

      render(<CustomerContactsList customerId={mockCustomerId} />);

      await waitFor(() => {
        expect(screen.getByText('Max Mustermann')).toBeInTheDocument();
      });

      expect(screen.getByText('max@example.com')).toBeInTheDocument();
      expect(screen.getByText('+49 123 456789')).toBeInTheDocument();
      expect(screen.getByText('Haupt')).toBeInTheDocument();
      expect(screen.getByText('Entscheider')).toBeInTheDocument();
      expect(screen.getByText('85%')).toBeInTheDocument();
    });

    it('should display multiple contacts', async () => {
      const mockContacts: Contact[] = [
        {
          id: '1',
          customerId: mockCustomerId,
          firstName: 'Max',
          lastName: 'Mustermann',
          isPrimary: true,
          isActive: true,
          responsibilityScope: 'all',
          createdAt: '2025-01-15T10:00:00Z',
          updatedAt: '2025-01-20T15:30:00Z',
        },
        {
          id: '2',
          customerId: mockCustomerId,
          firstName: 'Anna',
          lastName: 'Schmidt',
          isPrimary: false,
          isActive: true,
          responsibilityScope: 'all',
          createdAt: '2025-01-15T10:00:00Z',
          updatedAt: '2025-01-20T15:30:00Z',
        },
      ];

      (global.fetch as any).mockResolvedValueOnce({
        ok: true,
        json: async () => mockContacts,
      });

      render(<CustomerContactsList customerId={mockCustomerId} />);

      await waitFor(() => {
        expect(screen.getByText('Max Mustermann')).toBeInTheDocument();
        expect(screen.getByText('Anna Schmidt')).toBeInTheDocument();
      });
    });

    it('should display contact with position', async () => {
      const mockContacts: Contact[] = [
        {
          id: '1',
          customerId: mockCustomerId,
          firstName: 'Max',
          lastName: 'Mustermann',
          position: 'Geschäftsführer',
          isPrimary: true,
          isActive: true,
          responsibilityScope: 'all',
          createdAt: '2025-01-15T10:00:00Z',
          updatedAt: '2025-01-20T15:30:00Z',
        },
      ];

      (global.fetch as any).mockResolvedValueOnce({
        ok: true,
        json: async () => mockContacts,
      });

      render(<CustomerContactsList customerId={mockCustomerId} />);

      await waitFor(() => {
        expect(screen.getByText('Geschäftsführer')).toBeInTheDocument();
      });
    });

    it('should display contact with mobile number', async () => {
      const mockContacts: Contact[] = [
        {
          id: '1',
          customerId: mockCustomerId,
          firstName: 'Max',
          lastName: 'Mustermann',
          mobile: '+49 170 1234567',
          isPrimary: true,
          isActive: true,
          responsibilityScope: 'all',
          createdAt: '2025-01-15T10:00:00Z',
          updatedAt: '2025-01-20T15:30:00Z',
        },
      ];

      (global.fetch as any).mockResolvedValueOnce({
        ok: true,
        json: async () => mockContacts,
      });

      render(<CustomerContactsList customerId={mockCustomerId} />);

      await waitFor(() => {
        expect(screen.getByText('+49 170 1234567')).toBeInTheDocument();
      });
    });

    it('should display contact with personal notes', async () => {
      const mockContacts: Contact[] = [
        {
          id: '1',
          customerId: mockCustomerId,
          firstName: 'Max',
          lastName: 'Mustermann',
          personalNotes: 'Spielt gerne Golf am Wochenende',
          isPrimary: true,
          isActive: true,
          responsibilityScope: 'all',
          createdAt: '2025-01-15T10:00:00Z',
          updatedAt: '2025-01-20T15:30:00Z',
        },
      ];

      (global.fetch as any).mockResolvedValueOnce({
        ok: true,
        json: async () => mockContacts,
      });

      render(<CustomerContactsList customerId={mockCustomerId} />);

      await waitFor(() => {
        expect(screen.getByText('Spielt gerne Golf am Wochenende')).toBeInTheDocument();
      });
    });
  });

  describe('Error State', () => {
    it('should display error message when API call fails', async () => {
      (global.fetch as any).mockRejectedValueOnce(new Error('Network error'));

      render(<CustomerContactsList customerId={mockCustomerId} />);

      await waitFor(() => {
        expect(screen.getByText('Fehler beim Laden der Kontakte')).toBeInTheDocument();
      });
    });

    it('should display error message when response is not ok', async () => {
      (global.fetch as any).mockResolvedValueOnce({
        ok: false,
        status: 500,
      });

      render(<CustomerContactsList customerId={mockCustomerId} />);

      await waitFor(() => {
        expect(screen.getByText('Fehler beim Laden der Kontakte')).toBeInTheDocument();
      });
    });
  });

  describe('Empty State', () => {
    it('should display empty state when no contacts exist', async () => {
      (global.fetch as any).mockResolvedValueOnce({
        ok: true,
        json: async () => [],
      });

      render(<CustomerContactsList customerId={mockCustomerId} />);

      await waitFor(() => {
        expect(screen.getByText('Noch keine Kontakte erfasst')).toBeInTheDocument();
      });
    });

    it('should display empty state when contacts array is null', async () => {
      (global.fetch as any).mockResolvedValueOnce({
        ok: true,
        json: async () => null,
      });

      render(<CustomerContactsList customerId={mockCustomerId} />);

      await waitFor(() => {
        expect(screen.getByText('Noch keine Kontakte erfasst')).toBeInTheDocument();
      });
    });
  });

  describe('Contact Sorting', () => {
    it('should sort contacts with primary contact first', async () => {
      const mockContacts: Contact[] = [
        {
          id: '2',
          customerId: mockCustomerId,
          firstName: 'Anna',
          lastName: 'Schmidt',
          isPrimary: false,
          isActive: true,
          responsibilityScope: 'all',
          createdAt: '2025-01-15T10:00:00Z',
          updatedAt: '2025-01-20T15:30:00Z',
        },
        {
          id: '1',
          customerId: mockCustomerId,
          firstName: 'Max',
          lastName: 'Mustermann',
          isPrimary: true,
          isActive: true,
          responsibilityScope: 'all',
          createdAt: '2025-01-15T10:00:00Z',
          updatedAt: '2025-01-20T15:30:00Z',
        },
      ];

      (global.fetch as any).mockResolvedValueOnce({
        ok: true,
        json: async () => mockContacts,
      });

      render(<CustomerContactsList customerId={mockCustomerId} />);

      await waitFor(() => {
        const names = screen.getAllByText(/Mustermann|Schmidt/);
        // Primary contact (Mustermann) should be first
        expect(names[0]).toHaveTextContent('Mustermann');
        expect(names[1]).toHaveTextContent('Schmidt');
      });
    });
  });

  describe('Decision Level Badges', () => {
    it('should render Entscheider badge with correct color', async () => {
      const mockContacts: Contact[] = [
        {
          id: '1',
          customerId: mockCustomerId,
          firstName: 'Max',
          lastName: 'Mustermann',
          decisionLevel: 'entscheider',
          isPrimary: true,
          isActive: true,
          responsibilityScope: 'all',
          createdAt: '2025-01-15T10:00:00Z',
          updatedAt: '2025-01-20T15:30:00Z',
        },
      ];

      (global.fetch as any).mockResolvedValueOnce({
        ok: true,
        json: async () => mockContacts,
      });

      render(<CustomerContactsList customerId={mockCustomerId} />);

      await waitFor(() => {
        expect(screen.getByText('Entscheider')).toBeInTheDocument();
      });
    });

    it('should render Mitentscheider badge', async () => {
      const mockContacts: Contact[] = [
        {
          id: '1',
          customerId: mockCustomerId,
          firstName: 'Max',
          lastName: 'Mustermann',
          decisionLevel: 'mitentscheider',
          isPrimary: true,
          isActive: true,
          responsibilityScope: 'all',
          createdAt: '2025-01-15T10:00:00Z',
          updatedAt: '2025-01-20T15:30:00Z',
        },
      ];

      (global.fetch as any).mockResolvedValueOnce({
        ok: true,
        json: async () => mockContacts,
      });

      render(<CustomerContactsList customerId={mockCustomerId} />);

      await waitFor(() => {
        expect(screen.getByText('Mitentscheider')).toBeInTheDocument();
      });
    });

    it('should render Einflussnehmer badge', async () => {
      const mockContacts: Contact[] = [
        {
          id: '1',
          customerId: mockCustomerId,
          firstName: 'Max',
          lastName: 'Mustermann',
          decisionLevel: 'einflussnehmer',
          isPrimary: true,
          isActive: true,
          responsibilityScope: 'all',
          createdAt: '2025-01-15T10:00:00Z',
          updatedAt: '2025-01-20T15:30:00Z',
        },
      ];

      (global.fetch as any).mockResolvedValueOnce({
        ok: true,
        json: async () => mockContacts,
      });

      render(<CustomerContactsList customerId={mockCustomerId} />);

      await waitFor(() => {
        expect(screen.getByText('Einflussnehmer')).toBeInTheDocument();
      });
    });

    it('should render Nutzer badge', async () => {
      const mockContacts: Contact[] = [
        {
          id: '1',
          customerId: mockCustomerId,
          firstName: 'Max',
          lastName: 'Mustermann',
          decisionLevel: 'nutzer',
          isPrimary: true,
          isActive: true,
          responsibilityScope: 'all',
          createdAt: '2025-01-15T10:00:00Z',
          updatedAt: '2025-01-20T15:30:00Z',
        },
      ];

      (global.fetch as any).mockResolvedValueOnce({
        ok: true,
        json: async () => mockContacts,
      });

      render(<CustomerContactsList customerId={mockCustomerId} />);

      await waitFor(() => {
        expect(screen.getByText('Nutzer')).toBeInTheDocument();
      });
    });

    it('should render Gatekeeper badge', async () => {
      const mockContacts: Contact[] = [
        {
          id: '1',
          customerId: mockCustomerId,
          firstName: 'Max',
          lastName: 'Mustermann',
          decisionLevel: 'gatekeeper',
          isPrimary: true,
          isActive: true,
          responsibilityScope: 'all',
          createdAt: '2025-01-15T10:00:00Z',
          updatedAt: '2025-01-20T15:30:00Z',
        },
      ];

      (global.fetch as any).mockResolvedValueOnce({
        ok: true,
        json: async () => mockContacts,
      });

      render(<CustomerContactsList customerId={mockCustomerId} />);

      await waitFor(() => {
        expect(screen.getByText('Gatekeeper')).toBeInTheDocument();
      });
    });
  });

  describe('Warmth Score Display', () => {
    it('should display warmth score with percentage', async () => {
      const mockContacts: Contact[] = [
        {
          id: '1',
          customerId: mockCustomerId,
          firstName: 'Max',
          lastName: 'Mustermann',
          relationshipWarmth: 75,
          isPrimary: true,
          isActive: true,
          responsibilityScope: 'all',
          createdAt: '2025-01-15T10:00:00Z',
          updatedAt: '2025-01-20T15:30:00Z',
        },
      ];

      (global.fetch as any).mockResolvedValueOnce({
        ok: true,
        json: async () => mockContacts,
      });

      render(<CustomerContactsList customerId={mockCustomerId} />);

      await waitFor(() => {
        expect(screen.getByText('75%')).toBeInTheDocument();
      });
    });

    it('should not display warmth score when undefined', async () => {
      const mockContacts: Contact[] = [
        {
          id: '1',
          customerId: mockCustomerId,
          firstName: 'Max',
          lastName: 'Mustermann',
          isPrimary: true,
          isActive: true,
          responsibilityScope: 'all',
          createdAt: '2025-01-15T10:00:00Z',
          updatedAt: '2025-01-20T15:30:00Z',
        },
      ];

      (global.fetch as any).mockResolvedValueOnce({
        ok: true,
        json: async () => mockContacts,
      });

      render(<CustomerContactsList customerId={mockCustomerId} />);

      await waitFor(() => {
        expect(screen.getByText('Max Mustermann')).toBeInTheDocument();
      });

      expect(screen.queryByText(/%$/)).not.toBeInTheDocument();
    });
  });

  describe('API Integration', () => {
    it('should call correct API endpoint', async () => {
      (global.fetch as any).mockResolvedValueOnce({
        ok: true,
        json: async () => [],
      });

      render(<CustomerContactsList customerId={mockCustomerId} />);

      await waitFor(() => {
        expect(global.fetch).toHaveBeenCalledWith(`/api/customers/${mockCustomerId}/contacts`, {
          credentials: 'include',
        });
      });
    });

    it('should refetch contacts when customerId changes', async () => {
      const { rerender } = render(<CustomerContactsList customerId={mockCustomerId} />);

      (global.fetch as any).mockResolvedValueOnce({
        ok: true,
        json: async () => [],
      });

      await waitFor(() => {
        expect(global.fetch).toHaveBeenCalledTimes(1);
      });

      const newCustomerId = '987e6543-e21b-43d2-a654-426614174999';
      (global.fetch as any).mockResolvedValueOnce({
        ok: true,
        json: async () => [],
      });

      rerender(<CustomerContactsList customerId={newCustomerId} />);

      await waitFor(() => {
        expect(global.fetch).toHaveBeenCalledTimes(2);
        expect(global.fetch).toHaveBeenLastCalledWith(`/api/customers/${newCustomerId}/contacts`, {
          credentials: 'include',
        });
      });
    });
  });

  describe('Relative Time Formatting', () => {
    it('should display relative time when updatedAt is present', async () => {
      const mockContacts: Contact[] = [
        {
          id: '1',
          customerId: mockCustomerId,
          firstName: 'Max',
          lastName: 'Mustermann',
          isPrimary: true,
          isActive: true,
          responsibilityScope: 'all',
          createdAt: '2025-01-15T10:00:00Z',
          updatedAt: new Date().toISOString(), // Today
        },
      ];

      (global.fetch as any).mockResolvedValueOnce({
        ok: true,
        json: async () => mockContacts,
      });

      render(<CustomerContactsList customerId={mockCustomerId} />);

      await waitFor(() => {
        expect(screen.getByText('Heute')).toBeInTheDocument();
      });
    });
  });
});
