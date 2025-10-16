import React, { useState, useMemo, useEffect } from 'react';
import { Box, Typography, Alert, CircularProgress, Stack } from '@mui/material';
import { useTheme } from '@mui/material/styles';
import { DndContext, DragOverlay, closestCorners } from '@dnd-kit/core';
import type { DragEndEvent, DragStartEvent } from '@dnd-kit/core';
import { httpClient } from '../../../lib/apiClient';

// Import unserer neuen Komponenten
import { PipelineStage, OpportunityStage } from './PipelineStage';
import { OpportunityCard } from './OpportunityCard';
import type { Opportunity } from './OpportunityCard';

// Mock-Daten entfernt - OpportunityPipeline nutzt jetzt ausschließlich echte API-Daten

/**
 * M4 Opportunity Pipeline - Kanban Board für Verkaufschancen
 *
 * Diese Komponente stellt das Herzstück des prozessorientierten Vertriebs dar.
 * Zeigt alle Opportunities in einem Kanban-Board mit Drag & Drop zwischen Stages.
 */
export const OpportunityPipeline: React.FC = () => {
  const theme = useTheme();
  const [activeOpportunity, setActiveOpportunity] = useState<Opportunity | null>(null);

  // State für Opportunities (aus API oder Fallback)
  const [opportunities, setOpportunities] = useState<Opportunity[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Lade Opportunities aus der API
  useEffect(() => {
    const fetchOpportunities = async () => {
      try {
        setIsLoading(true);
        const response = await httpClient.get<Opportunity[]>('/api/opportunities');

        // Transformiere die API-Daten falls nötig (Sprint 2.1.7.1 - Lead-Felder beibehalten)
        const apiOpportunities = response.data.map((opp: Partial<Opportunity>) => ({
          ...opp,
          // Stelle sicher, dass alle erforderlichen Felder vorhanden sind
          assignedToName: opp.assignedToName || 'Nicht zugewiesen',
          probability: opp.probability || 0,
          createdAt: opp.createdAt || new Date().toISOString(),
          updatedAt: opp.updatedAt || new Date().toISOString(),
          // Sprint 2.1.7.1: Lead-Origin Felder explizit übernehmen
          leadId: opp.leadId ?? undefined,
          leadCompanyName: opp.leadCompanyName ?? undefined,
          stageColor: opp.stageColor ?? undefined,
        }));

        setOpportunities(apiOpportunities);
        setError(null);
      } catch {
        setError('Fehler beim Laden der Opportunities - Backend nicht erreichbar');
        // Leere Liste statt Mock-Daten bei Fehler
        setOpportunities([]);
      } finally {
        setIsLoading(false);
      }
    };

    fetchOpportunities();
  }, []);

  // Pipeline-Daten basierend auf aktuellem State berechnen
  const pipelineData = useMemo(
    () => ({
      totalOpportunities: opportunities.length,
      totalValue: opportunities.reduce((sum, opp) => sum + (opp.value || 0), 0),
      conversionRate: 0.35,
      stageDistribution: Object.values(OpportunityStage).reduce(
        (acc, stage) => {
          const stageOpps = opportunities.filter(opp => opp.stage === stage);
          acc[stage] = {
            count: stageOpps.length,
            value: stageOpps.reduce((sum, opp) => sum + (opp.value || 0), 0),
            opportunities: stageOpps,
          };
          return acc;
        },
        {} as Record<
          OpportunityStage,
          { count: number; value: number; opportunities: Opportunity[] }
        >
      ),
    }),
    [opportunities]
  );

  // Drag & Drop Handlers
  const handleDragStart = (event: DragStartEvent) => {
    const opportunity = event.active.data.current?.opportunity as Opportunity;
    setActiveOpportunity(opportunity);
  };

  const handleDragEnd = (event: DragEndEvent) => {
    const { active, over } = event;
    setActiveOpportunity(null);

    if (!over) {
      return;
    }

    const opportunityId = active.id as string;
    const newStage = over.id as OpportunityStage;
    const opportunity = active.data.current?.opportunity as Opportunity;

    // Nur ändern wenn Stage wirklich unterschiedlich
    if (opportunity && opportunity.stage !== newStage) {
      // State aktualisieren - Opportunity Stage ändern
      setOpportunities(prevOpportunities =>
        prevOpportunities.map(opp =>
          opp.id === opportunityId
            ? { ...opp, stage: newStage, updatedAt: new Date().toISOString() }
            : opp
        )
      );

      // TODO: In echter App würde hier API-Call stehen
      // changeStage.mutate({ id: opportunityId, newStage, reason: '...' });
    }
  };

  // Loading State
  if (isLoading) {
    return (
      <Box
        sx={{
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'center',
          minHeight: 400,
          flexDirection: 'column',
          gap: 2,
        }}
      >
        <CircularProgress size={48} sx={{ color: '#94C456' }} />
        <Typography variant="body1" sx={{ color: theme.palette.text.secondary }}>
          Lade Pipeline-Daten...
        </Typography>
      </Box>
    );
  }

  // Error State
  if (error) {
    return (
      <Box sx={{ p: 3 }}>
        <Alert severity="error" sx={{ mb: 2 }}>
          <Typography variant="h6" sx={{ mb: 1 }}>
            Fehler beim Laden der Pipeline
          </Typography>
          <Typography variant="body2">
            {error instanceof Error ? error.message : 'Unbekannter Fehler aufgetreten'}
          </Typography>
        </Alert>
      </Box>
    );
  }

  // Empty State
  if (!pipelineData || pipelineData.totalOpportunities === 0) {
    return (
      <Box sx={{ p: 3 }}>
        <Typography
          variant="h4"
          sx={{
            color: '#004F7B',
            fontFamily: 'Antonio, sans-serif',
            fontWeight: 700,
            mb: 3,
          }}
        >
          Verkaufschancen Pipeline
        </Typography>

        <Alert severity="info" sx={{ textAlign: 'center' }}>
          <Typography variant="h6" sx={{ mb: 1 }}>
            Keine Verkaufschancen vorhanden
          </Typography>
          <Typography variant="body2">
            Erstellen Sie Ihre erste Verkaufschance, um mit der Pipeline zu beginnen.
          </Typography>
        </Alert>
      </Box>
    );
  }

  return (
    <Box sx={{ p: 3 }}>
      {/* Header */}
      <Box sx={{ mb: 3 }}>
        <Typography
          variant="h4"
          sx={{
            color: '#004F7B',
            fontFamily: 'Antonio, sans-serif',
            fontWeight: 700,
            mb: 1,
          }}
        >
          Verkaufschancen Pipeline
        </Typography>
        <Typography variant="body1" sx={{ color: theme.palette.text.secondary }}>
          Verwalten Sie Ihre Verkaufschancen in einem übersichtlichen Kanban-Board
        </Typography>
      </Box>

      {/* Pipeline Statistics */}
      <Box
        sx={{
          display: 'flex',
          gap: 3,
          mb: 3,
          flexWrap: 'wrap',
        }}
      >
        <Box sx={{ textAlign: 'center' }}>
          <Typography variant="h5" sx={{ color: '#94C456', fontWeight: 700 }}>
            {pipelineData.totalOpportunities}
          </Typography>
          <Typography variant="body2" sx={{ color: theme.palette.text.secondary }}>
            Gesamt
          </Typography>
        </Box>
        <Box sx={{ textAlign: 'center' }}>
          <Typography variant="h5" sx={{ color: '#004F7B', fontWeight: 700 }}>
            {new Intl.NumberFormat('de-DE', {
              style: 'currency',
              currency: 'EUR',
              minimumFractionDigits: 0,
            }).format(pipelineData.totalValue)}
          </Typography>
          <Typography variant="body2" sx={{ color: theme.palette.text.secondary }}>
            Gesamtwert
          </Typography>
        </Box>
        <Box sx={{ textAlign: 'center' }}>
          <Typography variant="h5" sx={{ color: '#FFA726', fontWeight: 700 }}>
            {Math.round(pipelineData.conversionRate * 100)}%
          </Typography>
          <Typography variant="body2" sx={{ color: theme.palette.text.secondary }}>
            Conversion Rate
          </Typography>
        </Box>
      </Box>

      {/* Kanban Board */}
      <DndContext
        collisionDetection={closestCorners}
        onDragStart={handleDragStart}
        onDragEnd={handleDragEnd}
      >
        <Box
          sx={{
            display: 'flex',
            gap: 2,
            overflowX: 'auto',
            pb: 2,
            minHeight: 500,
          }}
        >
          {Object.values(OpportunityStage).map(stage => {
            const stageData = pipelineData.stageDistribution[stage];
            const opportunities: Opportunity[] = stageData?.opportunities || [];

            return (
              <PipelineStage
                key={stage}
                stage={stage}
                opportunityCount={stageData?.count || 0}
                totalValue={stageData?.value}
              >
                <Stack spacing={1.5}>
                  {opportunities.map(opportunity => (
                    <OpportunityCard
                      key={opportunity.id}
                      opportunity={opportunity}
                      onClick={_opp => {
                        // TODO: Open opportunity detail modal
                      }}
                    />
                  ))}
                </Stack>
              </PipelineStage>
            );
          })}
        </Box>

        {/* Drag Overlay - Sprint 2.1.7.1: transformOrigin removed (Drag & Drop Fix) */}
        <DragOverlay
          adjustScale={false}
          wrapperElement="div"
          dropAnimation={{
            duration: 250,
            easing: 'cubic-bezier(0.18, 0.67, 0.6, 1.22)',
          }}
        >
          {activeOpportunity && (
            <Box
              sx={{
                opacity: 0.9,
                boxShadow: 4,
                pointerEvents: 'none', // Drag-Performance optimieren
              }}
            >
              <OpportunityCard opportunity={activeOpportunity} />
            </Box>
          )}
        </DragOverlay>
      </DndContext>

      {/* TODO: Mutation Loading/Error für API Integration */}
    </Box>
  );
};

export default OpportunityPipeline;
