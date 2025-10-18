/**
 * LeadOpportunitiesList Component Tests
 * Sprint 2.1.7.1 - Lead → Opportunity Conversion
 *
 * @description Tests für Loading/Empty/Error States und Opportunity-Anzeige
 * @since 2025-10-18
 */

import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { ThemeProvider } from '@mui/material/styles';
import freshfoodzTheme from '../../../../theme/freshfoodz';
import { LeadOpportunitiesList } from '../LeadOpportunitiesList';
import { apiClient } from '../../../../lib/apiClient';
import type { Opportunity } from '../../../opportunity/types';

// Mock API Client
vi.mock('../../../../lib/apiClient', () => ({
  apiClient: {
    get: vi.fn(),
  },
}));

// Helper function to render with theme and router
const renderWithProviders = (component: React.ReactElement) => {
  return render(
    <BrowserRouter>
      <ThemeProvider theme={freshfoodzTheme}>{component}</ThemeProvider>
    </BrowserRouter>
  );
};

// Mock Opportunity Data
const mockOpportunities: Opportunity[] = [
  {
    id: '1',
    name: 'Test Opportunity 1',
    stage: 'QUALIFICATION',
    opportunityType: 'NEUGESCHAEFT',
    value: 25000,
    probability: 60,
    customerName: 'Test Customer GmbH',
    leadId: 123,
    leadCompanyName: 'Test Catering GmbH',
    expectedCloseDate: '2025-12-31T00:00:00Z',
    description: 'Test opportunity from lead conversion',
    createdAt: '2025-10-01T10:00:00Z',
    updatedAt: '2025-10-15T14:30:00Z',
  },
  {
    id: '2',
    name: 'Test Opportunity 2',
    stage: 'PROPOSAL',
    opportunityType: 'SORTIMENTSERWEITERUNG',
    value: 50000,
    probability: 80,
    customerName: 'Another Customer AG',
    leadId: 123,
    leadCompanyName: 'Test Catering GmbH',
    expectedCloseDate: '2025-11-30T00:00:00Z',
    createdAt: '2025-10-05T10:00:00Z',
    updatedAt: '2025-10-16T14:30:00Z',
  },
];

describe('LeadOpportunitiesList', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('Loading State', () => {
    it('shows loading spinner while fetching opportunities', () => {
      // Mock slow API response
      vi.mocked(apiClient.get).mockImplementation(
        () => new Promise(() => {}) // Never resolves
      );

      renderWithProviders(<LeadOpportunitiesList leadId={123} />);

      expect(screen.getByRole('progressbar')).toBeInTheDocument();
    });
  });

  describe('Empty State', () => {
    it('shows empty message when no opportunities exist', async () => {
      vi.mocked(apiClient.get).mockResolvedValueOnce({ data: [] });

      renderWithProviders(<LeadOpportunitiesList leadId={123} />);

      await waitFor(() => {
        expect(screen.getByText(/Noch keine Opportunities für diesen Lead erstellt/i)).toBeInTheDocument();
      });
    });
  });

  describe('Error State', () => {
    it('shows error message when API call fails', async () => {
      const errorMessage = 'Lead not found';
      vi.mocked(apiClient.get).mockRejectedValueOnce({
        response: {
          data: {
            error: errorMessage,
          },
        },
      });

      renderWithProviders(<LeadOpportunitiesList leadId={999} />);

      await waitFor(() => {
        expect(screen.getByText(errorMessage)).toBeInTheDocument();
      });
    });

    it('shows generic error message when response has no detail', async () => {
      vi.mocked(apiClient.get).mockRejectedValueOnce(new Error('Network error'));

      renderWithProviders(<LeadOpportunitiesList leadId={123} />);

      await waitFor(() => {
        expect(screen.getByText(/Fehler beim Laden der Opportunities/i)).toBeInTheDocument();
      });
    });
  });

  describe('Opportunities Display', () => {
    it('renders list of opportunities successfully', async () => {
      vi.mocked(apiClient.get).mockResolvedValueOnce({ data: mockOpportunities });

      renderWithProviders(<LeadOpportunitiesList leadId={123} />);

      await waitFor(() => {
        expect(screen.getByText('Test Opportunity 1')).toBeInTheDocument();
        expect(screen.getByText('Test Opportunity 2')).toBeInTheDocument();
      });
    });

    it('shows opportunity type badges correctly', async () => {
      vi.mocked(apiClient.get).mockResolvedValueOnce({ data: mockOpportunities });

      renderWithProviders(<LeadOpportunitiesList leadId={123} />);

      await waitFor(() => {
        expect(screen.getByText(/🆕 Neugeschäft/i)).toBeInTheDocument();
        expect(screen.getByText(/📈 Sortimentserweiterung/i)).toBeInTheDocument();
      });
    });

    it('displays opportunity values correctly formatted', async () => {
      vi.mocked(apiClient.get).mockResolvedValueOnce({ data: mockOpportunities });

      renderWithProviders(<LeadOpportunitiesList leadId={123} />);

      await waitFor(() => {
        expect(screen.getByText(/25\.000\s*€/i)).toBeInTheDocument();
        expect(screen.getByText(/50\.000\s*€/i)).toBeInTheDocument();
      });
    });

    it('shows customer names', async () => {
      vi.mocked(apiClient.get).mockResolvedValueOnce({ data: mockOpportunities });

      renderWithProviders(<LeadOpportunitiesList leadId={123} />);

      await waitFor(() => {
        expect(screen.getByText('Test Customer GmbH')).toBeInTheDocument();
        expect(screen.getByText('Another Customer AG')).toBeInTheDocument();
      });
    });

    it('displays expected close dates', async () => {
      vi.mocked(apiClient.get).mockResolvedValueOnce({ data: mockOpportunities });

      renderWithProviders(<LeadOpportunitiesList leadId={123} />);

      await waitFor(() => {
        expect(screen.getByText(/Abschluss: 31\.12\.2025/i)).toBeInTheDocument();
        expect(screen.getByText(/Abschluss: 30\.11\.2025/i)).toBeInTheDocument();
      });
    });

    it('shows opportunity stage chips', async () => {
      vi.mocked(apiClient.get).mockResolvedValueOnce({ data: mockOpportunities });

      renderWithProviders(<LeadOpportunitiesList leadId={123} />);

      await waitFor(() => {
        expect(screen.getByText('Qualifizierung')).toBeInTheDocument();
        expect(screen.getByText('Angebot')).toBeInTheDocument();
      });
    });

    it('displays opportunity description when present', async () => {
      vi.mocked(apiClient.get).mockResolvedValueOnce({ data: [mockOpportunities[0]] });

      renderWithProviders(<LeadOpportunitiesList leadId={123} />);

      await waitFor(() => {
        expect(screen.getByText('Test opportunity from lead conversion')).toBeInTheDocument();
      });
    });

    it('shows summary with correct opportunity count', async () => {
      vi.mocked(apiClient.get).mockResolvedValueOnce({ data: mockOpportunities });

      renderWithProviders(<LeadOpportunitiesList leadId={123} />);

      await waitFor(() => {
        // Text is split across <strong> tags, so we need to search separately
        expect(screen.getByText('2')).toBeInTheDocument();
        expect(screen.getByText(/Opportunities aus diesem Lead erstellt/i)).toBeInTheDocument();
      });
    });

    it('uses singular form for single opportunity', async () => {
      vi.mocked(apiClient.get).mockResolvedValueOnce({ data: [mockOpportunities[0]] });

      renderWithProviders(<LeadOpportunitiesList leadId={123} />);

      await waitFor(() => {
        // Text is split across <strong> tags, so we need to search separately
        expect(screen.getByText('1')).toBeInTheDocument();
        expect(screen.getByText(/Opportunity aus diesem Lead erstellt/i)).toBeInTheDocument();
      });
    });

    it('renders links to opportunity detail pages', async () => {
      vi.mocked(apiClient.get).mockResolvedValueOnce({ data: [mockOpportunities[0]] });

      renderWithProviders(<LeadOpportunitiesList leadId={123} />);

      await waitFor(() => {
        const link = screen.getByRole('link', { name: /Test Opportunity 1/i });
        expect(link).toHaveAttribute('href', '/opportunities/1');
      });
    });
  });

  describe('API Integration', () => {
    it('calls correct API endpoint with leadId', async () => {
      vi.mocked(apiClient.get).mockResolvedValueOnce({ data: [] });

      renderWithProviders(<LeadOpportunitiesList leadId={456} />);

      await waitFor(() => {
        expect(apiClient.get).toHaveBeenCalledWith('/api/leads/456/opportunities');
      });
    });

    it('refetches opportunities when leadId changes', async () => {
      vi.mocked(apiClient.get).mockResolvedValueOnce({ data: [] });

      const { rerender } = renderWithProviders(<LeadOpportunitiesList leadId={123} />);

      await waitFor(() => {
        expect(apiClient.get).toHaveBeenCalledWith('/api/leads/123/opportunities');
      });

      vi.mocked(apiClient.get).mockResolvedValueOnce({ data: [] });

      rerender(
        <BrowserRouter>
          <ThemeProvider theme={freshfoodzTheme}>
            <LeadOpportunitiesList leadId={456} />
          </ThemeProvider>
        </BrowserRouter>
      );

      await waitFor(() => {
        expect(apiClient.get).toHaveBeenCalledWith('/api/leads/456/opportunities');
        expect(apiClient.get).toHaveBeenCalledTimes(2);
      });
    });
  });

  describe('Edge Cases', () => {
    it('handles opportunity without description', async () => {
      const opportunityWithoutDesc: Opportunity = {
        ...mockOpportunities[0],
        description: undefined,
      };

      vi.mocked(apiClient.get).mockResolvedValueOnce({ data: [opportunityWithoutDesc] });

      renderWithProviders(<LeadOpportunitiesList leadId={123} />);

      await waitFor(() => {
        expect(screen.getByText('Test Opportunity 1')).toBeInTheDocument();
      });

      // Description should not be rendered
      expect(screen.queryByText('Test opportunity from lead conversion')).not.toBeInTheDocument();
    });

    it('handles opportunity without value', async () => {
      const opportunityWithoutValue: Opportunity = {
        ...mockOpportunities[0],
        value: undefined,
      };

      vi.mocked(apiClient.get).mockResolvedValueOnce({ data: [opportunityWithoutValue] });

      renderWithProviders(<LeadOpportunitiesList leadId={123} />);

      await waitFor(() => {
        expect(screen.getByText(/Kein Wert/i)).toBeInTheDocument();
      });
    });

    it('handles opportunity without expectedCloseDate', async () => {
      const opportunityWithoutDate: Opportunity = {
        ...mockOpportunities[0],
        expectedCloseDate: undefined,
      };

      vi.mocked(apiClient.get).mockResolvedValueOnce({ data: [opportunityWithoutDate] });

      renderWithProviders(<LeadOpportunitiesList leadId={123} />);

      await waitFor(() => {
        expect(screen.getByText('Test Opportunity 1')).toBeInTheDocument();
      });

      // Date section should not be present
      expect(screen.queryByText(/Abschluss:/i)).not.toBeInTheDocument();
    });

    it('displays leadCompanyName when customerName is not available', async () => {
      const opportunityWithLeadCompany: Opportunity = {
        ...mockOpportunities[0],
        customerName: undefined,
        leadCompanyName: 'Lead Company Name',
      };

      vi.mocked(apiClient.get).mockResolvedValueOnce({ data: [opportunityWithLeadCompany] });

      renderWithProviders(<LeadOpportunitiesList leadId={123} />);

      await waitFor(() => {
        expect(screen.getByText('Lead Company Name')).toBeInTheDocument();
      });
    });
  });
});
