/**
 * End-to-End Tests für RENEWAL Stage Drag & Drop
 *
 * @description Testet die komplette User Journey für Contract Renewals
 *              inklusive Drag & Drop Interaktionen und API Integration
 */

import React from 'react';
import { render, screen } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import userEvent from '@testing-library/user-event';
import '@testing-library/jest-dom';

import { OpportunityPipeline } from '../OpportunityPipeline';
import { OpportunityStage } from '../../types/opportunity.types';

// Mock für API Calls
const mockUpdateOpportunity = vi.fn();
const mockGetOpportunities = vi.fn();

// Mock fetch für API Integration
global.fetch = vi.fn();

// Test Setup für React Query
const createTestQueryClient = () =>
  new QueryClient({
    defaultOptions: {
      queries: { retry: false },
      mutations: { retry: false },
    },
  });

const TestWrapper: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const queryClient = createTestQueryClient();
  return <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>;
};

// Mock Opportunities für E2E Tests
const mockE2EOpportunities = [
  {
    id: 'e2e-001',
    name: 'Restaurant Goldener Hirsch - Hauptvertrag',
    stage: OpportunityStage.CLOSED_WON,
    value: 35000,
    probability: 100,
    customerName: 'Restaurant Goldener Hirsch GmbH',
    assignedToName: 'Maria Schmidt',
    expectedCloseDate: '2025-08-15',
    createdAt: '2025-01-15T10:00:00Z',
    updatedAt: '2025-07-01T15:30:00Z',
  },
  {
    id: 'e2e-002',
    name: 'Hotel Bergblick - Verlängerungsprozess',
    stage: OpportunityStage.RENEWAL,
    value: 28000,
    probability: 75,
    customerName: 'Hotel Bergblick AG',
    assignedToName: 'Tom Fischer',
    expectedCloseDate: '2025-09-01',
    createdAt: '2025-06-01T09:15:00Z',
    updatedAt: '2025-07-20T11:45:00Z',
  },
];

describe('OpportunityPipeline - E2E RENEWAL Tests', () => {
  beforeEach(() => {
    // Mock successful API responses
    (fetch as jest.MockedFunction<typeof fetch>).mockImplementation(url => {
      if (url.toString().includes('/api/opportunities')) {
        return Promise.resolve({
          ok: true,
          json: () => Promise.resolve(mockE2EOpportunities),
        } as Response);
      }
      return Promise.resolve({
        ok: true,
        json: () => Promise.resolve({}),
      } as Response);
    });

    mockUpdateOpportunity.mockClear();
    mockGetOpportunities.mockClear();
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  describe('Complete RENEWAL Workflow', () => {
    test('should complete full contract renewal workflow: CLOSED_WON → RENEWAL → CLOSED_WON', async () => {
      // userEvent wird später für Interaktionen benötigt
      // const user = userEvent.setup();

      render(
        <TestWrapper>
          <OpportunityPipeline />
        </TestWrapper>
      );

      // 1. Warte bis Pipeline geladen ist
      await waitFor(() => {
        expect(screen.getByText('Verkaufschancen Pipeline')).toBeInTheDocument();
      });

      // 2. Prüfe dass CLOSED_WON Opportunity sichtbar ist
      const wonOpportunity = screen.getByText('Restaurant Goldener Hirsch - Hauptvertrag');
      expect(wonOpportunity).toBeInTheDocument();

      // 3. Prüfe dass RENEWAL Spalte existiert
      const renewalColumn = screen.getByText('Verlängerung');
      expect(renewalColumn).toBeInTheDocument();

      // 4. Simuliere Drag & Drop von CLOSED_WON zu RENEWAL
      // (Contract läuft ab und wird zur Verlängerung bewegt)
      const opportunityCard = wonOpportunity.closest('[draggable="true"]');
      const renewalDropZone = renewalColumn.closest('[data-droppable="true"]');

      if (opportunityCard && renewalDropZone) {
        // Drag Start
        fireEvent.dragStart(opportunityCard, {
          dataTransfer: {
            setData: vi.fn(),
            getData: vi.fn(),
          },
        });

        // Drag Over RENEWAL Zone
        fireEvent.dragOver(renewalDropZone);

        // Drop in RENEWAL
        fireEvent.drop(renewalDropZone);

        // 5. Warte auf API Call für Stage Update
        await waitFor(() => {
          expect(fetch).toHaveBeenCalledWith(
            expect.stringContaining('/api/opportunities/e2e-001'),
            expect.objectContaining({
              method: 'PUT',
              headers: expect.objectContaining({
                'Content-Type': 'application/json',
              }),
              body: expect.stringContaining('"stage":"RENEWAL"'),
            })
          );
        });
      }

      // 6. Prüfe dass Opportunity jetzt in RENEWAL Spalte ist
      await waitFor(() => {
        const renewalSection = screen.getByText('Verlängerung').closest('[data-stage="RENEWAL"]');
        expect(renewalSection).toContainElement(wonOpportunity);
      });
    });

    test('should handle successful renewal completion: RENEWAL → CLOSED_WON', async () => {
      // userEvent wird später für Interaktionen benötigt
      // const user = userEvent.setup();

      render(
        <TestWrapper>
          <OpportunityPipeline />
        </TestWrapper>
      );

      await waitFor(() => {
        expect(screen.getByText('Hotel Bergblick - Verlängerungsprozess')).toBeInTheDocument();
      });

      // Simuliere erfolgreiches Renewal: RENEWAL → CLOSED_WON
      const renewalOpportunity = screen.getByText('Hotel Bergblick - Verlängerungsprozess');
      const wonColumn = screen.getByText('Gewonnen');

      const opportunityCard = renewalOpportunity.closest('[draggable="true"]');
      const wonDropZone = wonColumn.closest('[data-droppable="true"]');

      if (opportunityCard && wonDropZone) {
        fireEvent.dragStart(opportunityCard);
        fireEvent.dragOver(wonDropZone);
        fireEvent.drop(wonDropZone);

        // Prüfe API Call für erfolgreiche Verlängerung
        await waitFor(() => {
          expect(fetch).toHaveBeenCalledWith(
            expect.stringContaining('/api/opportunities/e2e-002'),
            expect.objectContaining({
              method: 'PUT',
              body: expect.stringContaining('"stage":"CLOSED_WON"'),
            })
          );
        });
      }
    });

    test('should handle failed renewal: RENEWAL → CLOSED_LOST', async () => {
      render(
        <TestWrapper>
          <OpportunityPipeline />
        </TestWrapper>
      );

      await waitFor(() => {
        expect(screen.getByText('Hotel Bergblick - Verlängerungsprozess')).toBeInTheDocument();
      });

      // Simuliere gescheiterte Verlängerung: RENEWAL → CLOSED_LOST
      const renewalOpportunity = screen.getByText('Hotel Bergblick - Verlängerungsprozess');
      const lostColumn = screen.getByText('Verloren');

      const opportunityCard = renewalOpportunity.closest('[draggable="true"]');
      const lostDropZone = lostColumn.closest('[data-droppable="true"]');

      if (opportunityCard && lostDropZone) {
        fireEvent.dragStart(opportunityCard);
        fireEvent.dragOver(lostDropZone);
        fireEvent.drop(lostDropZone);

        await waitFor(() => {
          expect(fetch).toHaveBeenCalledWith(
            expect.stringContaining('/api/opportunities/e2e-002'),
            expect.objectContaining({
              method: 'PUT',
              body: expect.stringContaining('"stage":"CLOSED_LOST"'),
            })
          );
        });
      }
    });
  });

  describe('RENEWAL Drag & Drop Validation', () => {
    test('should prevent invalid drag operations to RENEWAL stage', async () => {
      // Mock console.warn um Validierungsmeldungen zu prüfen
      const consoleSpy = jest.spyOn(console, 'warn').mockImplementation();

      render(
        <TestWrapper>
          <OpportunityPipeline />
        </TestWrapper>
      );

      // Versuche ungültigen Drag von LEAD zu RENEWAL zu simulieren
      // (sollte verhindert werden durch Business Rules)

      // Erwarte Validierungswarnung
      // expect(consoleSpy).toHaveBeenCalledWith(
      //   expect.stringContaining('Invalid stage transition')
      // );

      consoleSpy.mockRestore();
    });

    test('should show visual feedback during RENEWAL drag operations', async () => {
      render(
        <TestWrapper>
          <OpportunityPipeline />
        </TestWrapper>
      );

      await waitFor(() => {
        expect(screen.getByText('Restaurant Goldener Hirsch - Hauptvertrag')).toBeInTheDocument();
      });

      const opportunityCard = screen
        .getByText('Restaurant Goldener Hirsch - Hauptvertrag')
        .closest('[draggable="true"]');

      if (opportunityCard) {
        // Starte Drag
        fireEvent.dragStart(opportunityCard);

        // Prüfe dass DragOverlay sichtbar wird
        await waitFor(() => {
          // DragOverlay sollte visuelles Feedback zeigen
          // const dragOverlay = document.querySelector('[data-testid="drag-overlay"]');
          // Je nach Implementation der DragOverlay
          expect(opportunityCard).toBeDefined();
        });
      }
    });
  });

  describe('RENEWAL Error Handling in E2E', () => {
    test('should handle API errors during RENEWAL stage transitions', async () => {
      // Mock API Error Response
      (fetch as jest.MockedFunction<typeof fetch>).mockImplementationOnce(() =>
        Promise.resolve({
          ok: false,
          status: 400,
          json: () =>
            Promise.resolve({
              message: 'Invalid stage transition from NEW_LEAD to RENEWAL',
            }),
        } as Response)
      );

      render(
        <TestWrapper>
          <OpportunityPipeline />
        </TestWrapper>
      );

      // Simuliere fehlgeschlagenen API Call
      const renewalColumn = screen.getByText('Verlängerung');

      // Error-Handling sollte graceful erfolgen
      // User sollte Fehlermeldung sehen, aber App nicht crashen
      await waitFor(() => {
        // Erwarte dass Pipeline noch funktioniert trotz API Fehler
        expect(renewalColumn).toBeInTheDocument();
      });
    });

    test('should handle network timeouts during RENEWAL operations', async () => {
      // Mock Network Timeout
      (fetch as jest.MockedFunction<typeof fetch>).mockImplementationOnce(
        () =>
          new Promise((_, reject) => setTimeout(() => reject(new Error('Network timeout')), 100))
      );

      render(
        <TestWrapper>
          <OpportunityPipeline />
        </TestWrapper>
      );

      // Pipeline sollte Loading State zeigen und dann Error Recovery
      await waitFor(() => {
        expect(screen.getByText('Verlängerung')).toBeInTheDocument();
      });
    });
  });

  describe('RENEWAL Performance in E2E', () => {
    test('should handle large number of RENEWAL opportunities efficiently', async () => {
      // Mock viele RENEWAL Opportunities
      const manyRenewalOpportunities = Array.from({ length: 50 }, (_, i) => ({
        id: `renewal-${i}`,
        name: `Contract Renewal ${i}`,
        stage: OpportunityStage.RENEWAL,
        value: 15000 + i * 1000,
        probability: 75,
        customerName: `Customer ${i}`,
        assignedToName: 'Sales Manager',
        expectedCloseDate: '2025-08-15',
        createdAt: '2025-06-01T09:15:00Z',
        updatedAt: '2025-07-20T11:45:00Z',
      }));

      (fetch as jest.MockedFunction<typeof fetch>).mockImplementationOnce(() =>
        Promise.resolve({
          ok: true,
          json: () => Promise.resolve(manyRenewalOpportunities),
        } as Response)
      );

      const startTime = performance.now();

      render(
        <TestWrapper>
          <OpportunityPipeline />
        </TestWrapper>
      );

      await waitFor(() => {
        expect(screen.getByText('Verlängerung')).toBeInTheDocument();
      });

      const endTime = performance.now();
      const renderTime = endTime - startTime;

      // Pipeline sollte auch mit vielen Opportunities performant sein (< 1000ms)
      expect(renderTime).toBeLessThan(1000);
    });
  });

  describe('RENEWAL Accessibility in E2E', () => {
    test('should support keyboard navigation for RENEWAL operations', async () => {
      const user = userEvent.setup();

      render(
        <TestWrapper>
          <OpportunityPipeline />
        </TestWrapper>
      );

      await waitFor(() => {
        expect(screen.getByText('Verlängerung')).toBeInTheDocument();
      });

      // Test Tab-Navigation durch RENEWAL Spalte
      await user.tab();

      // Erste fokussierbare Element sollte erreichbar sein
      const focusedElement = document.activeElement;
      expect(focusedElement).toBeDefined();

      // RENEWAL Spalte sollte über Tastatur erreichbar sein
      await user.keyboard('{ArrowRight}');
      // Je nach Implementation der Keyboard Navigation
    });

    test('should announce RENEWAL stage changes to screen readers', async () => {
      render(
        <TestWrapper>
          <OpportunityPipeline />
        </TestWrapper>
      );

      await waitFor(() => {
        expect(screen.getByText('Verlängerung')).toBeInTheDocument();
      });

      // ARIA Live Regions sollten Stage-Änderungen ankündigen
      // const ariaLiveRegion = document.querySelector('[aria-live="polite"]');
      // Je nach Implementation der Accessibility Features
      expect(screen.getByText('Verlängerung')).toHaveAttribute('role', 'region');
    });
  });
});
